/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;

// XXX
import org.openide.windows.InputOutput;
// check local vs remote file
//  - if remote not found => skip (add to ignored)
//  - if remote is folder and local is file (and vice versa) => skip (add to ignored)
/**
 * Remote client able to connect/disconnect to FTP
 * as well as download/upload files to a FTP server.
 * <p>
 * Every method throws {@link RemoteException} if any error occurs.
 * @author Tomas Mysik
 */
public class RemoteClient implements Cancellable {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());

    private final RemoteConfiguration configuration;
    private final InputOutput io;
    private final String baseRemoteDirectory;
    private FTPClient ftpClient;
    private volatile boolean cancelled = false;

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String)
     */
    public RemoteClient(RemoteConfiguration configuration) {
        this(configuration, null, null);
    }

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String)
     */
    public RemoteClient(RemoteConfiguration configuration, InputOutput io) {
        this(configuration, io, null);
    }

    /**
     * Create a new remote client.
     * @param configuration {@link RemoteConfiguration remote configuration} of a connection.
     * @param io {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
     *           Displays all the commands received from server.
     * @param additionalInitialSubdirectory additional directory which must start with {@value TransferFile#SEPARATOR} and is appended
     *                                      to {@link RemoteConfiguration#getInitialDirectory()} and
     *                                      set as default base remote directory. Can be <code>null</code>.
     */
    public RemoteClient(RemoteConfiguration configuration, InputOutput io, String additionalInitialSubdirectory) {
        assert configuration != null;
        this.configuration = configuration;
        this.io = io;
        StringBuilder baseDir = new StringBuilder(configuration.getInitialDirectory());
        if (additionalInitialSubdirectory != null && additionalInitialSubdirectory.length() > 0) {
            baseDir.append(additionalInitialSubdirectory);
        }
        baseRemoteDirectory = baseDir.toString().replaceAll(TransferFile.SEPARATOR + "{2,}", TransferFile.SEPARATOR); // NOI18N
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Remote client created with configuration: " + configuration + " and base remote directory: " + baseRemoteDirectory);
        }
    }

    public void connect() throws RemoteException {
        init();
        try {
            // connect
            int timeout = configuration.getTimeout() * 1000;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Connecting to " + configuration.getHost() + " [timeout: " + timeout + " ms]");
            }
            ftpClient.setDefaultTimeout(timeout);
            ftpClient.connect(configuration.getHost(), configuration.getPort());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Reply is " + ftpClient.getReplyString());
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOGGER.fine("Disconnecting because of negative reply");
                ftpClient.disconnect();
                // XXX
                throw new RemoteException("FTP server refused connection.");
            }

            // login
            LOGGER.fine("Login as " + configuration.getUserName());
            if (!ftpClient.login(configuration.getUserName(), configuration.getPassword())) {
                LOGGER.fine("Login unusuccessful -> logout");
                ftpClient.logout();
                return;
            }
            LOGGER.fine("Login successful");

            if (configuration.isPassiveMode()) {
                LOGGER.fine("Setting passive mode");
                ftpClient.enterLocalPassiveMode();
            }

            // binary mode as a default
            LOGGER.fine("Setting file type to BINARY");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Remote system is " + ftpClient.getSystemName());
            }

            // cd to base remote directory
            if (!cdBaseRemoteDirectory()) {
                // XXX
                throw new RemoteException("Cannot change to the base remote directory " + baseRemoteDirectory + "[" + ftpClient.getReplyString() + "]");
            }

        } catch (IOException ex) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Exception while disconnecting", ex);
                }
            }
            LOGGER.log(Level.FINE, "Exception while connecting", ex);
            // XXX
            throw new RemoteException("Could not connect to server.", ex);
        }
    }

    public void disconnect() throws RemoteException {
        init();
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (ftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            try {
                ftpClient.logout();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "XXX", ex);
                // XXX
                throw new RemoteException("XXX", ex);
            } finally {
                try {
                    ftpClient.disconnect();
                    LOGGER.log(Level.FINE, "Remote client disconnected");
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, "Remote client disconnected with exception", ex);
                }
            }
        }
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public void reset() {
        cancelled = false;
    }

    public Set<TransferFile> prepareUpload(FileObject baseLocalDirectory, FileObject... filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.length > 0 : "At least one file to upload must be specified";

        // XXX sort files by name and remove all the subdirectories (maybe use stack instead of queue)

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToUpload) {
            if (VisibilityQuery.getDefault().isVisible(fo)) {
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare upload cancelled");
                break;
            }

            TransferFile file = queue.poll();

            files.add(file);

            if (file.isDirectory()) {
                // XXX not nice to re-create file
                File f = getLocalFile(file, baseLocalDir);
                File[] children = f.listFiles();
                if (children != null) {
                    for (File child : children) {
                        queue.offer(TransferFile.fromFile(child, baseLocalAbsolutePath));
                    }
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Prepared for upload: " + files);
        }
        return files;
    }

    public TransferInfo upload(FileObject baseLocalDirectory, Set<TransferFile> filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.size() > 0 : "At least one file to upload must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);

        // XXX order filesToUpload?
        try {
            for (TransferFile file : filesToUpload) {
                if (cancelled) {
                    LOGGER.fine("Upload cancelled");
                    break;
                }

                try {
                    uploadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file);
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file);
                    continue;
                }
            }
        } finally {
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void uploadFile(TransferInfo transferInfo, File baseLocalDir, TransferFile file) throws IOException, RemoteException {
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading directory: " + file);
            }
            // in fact, useless but probably expected
            cdBaseRemoteDirectory(file.getRelativePath(), true);
            transferSucceeded(transferInfo, file);
        } else {
            // file => simply upload it

            assert file.getParentRelativePath() != null : "Must be underneath base remote directory! [" + file + "]";
            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), true)) {
                transferIgnored(transferInfo, file);
                return;
            }

            String fileName = file.getName();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + fileName);
            }
            // XXX lock the file?
            InputStream is = new FileInputStream(new File(baseLocalDir, file.getRelativePath(true)));
            try {
                if (ftpClient.storeFile(fileName, is)) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file);
                }
            } finally {
                is.close();
            }
        }
    }

    public Set<TransferFile> prepareDownload(FileObject baseLocalDirectory, FileObject... filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        ensureConnected();

        // XXX sort files by name and remove all the subdirectories (maybe use stack instead of queue)

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToDownload) {
            if (VisibilityQuery.getDefault().isVisible(fo)) {
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare download cancelled");
                break;
            }

            TransferFile file = queue.poll();

            files.add(file);

            if (file.isDirectory()) {
                try {
                    if (!cdBaseRemoteDirectory(file.getRelativePath(), false)) {
                        LOGGER.fine("Remote directory " + file.getRelativePath() + " cannot be entered or does not exist => ignoring");
                        // XXX maybe return somehow ignored files as well?
                        continue;
                    }
                    StringBuilder relativePath = new StringBuilder(baseRemoteDirectory);
                    if (file.getRelativePath() != TransferFile.CWD) {
                        relativePath.append(TransferFile.SEPARATOR);
                        relativePath.append(file.getRelativePath());
                    }
                    for (FTPFile fTPFile : ftpClient.listFiles()) {
                        queue.offer(TransferFile.fromFtpFile(fTPFile, baseRemoteDirectory,  relativePath.toString()));
                    }
                } catch (IOException exc) {
                    LOGGER.fine("Remote directory " + file.getRelativePath() + "/* cannot be entered or does not exist => ignoring");
                    // XXX maybe return somehow ignored files as well?
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Prepared for download: " + files);
        }
        return files;
    }

    public TransferInfo download(FileObject baseLocalDirectory, Set<TransferFile> filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.size() > 0 : "At least one file to download must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);

        // XXX order filesToDownload?
        try {
            for (TransferFile file : filesToDownload) {
                if (cancelled) {
                    LOGGER.fine("Download cancelled");
                    break;
                }

                try {
                    downloadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file);
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file);
                    continue;
                }
            }
        } finally {
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void downloadFile(TransferInfo transferInfo, File baseLocalDir, TransferFile file) throws IOException, RemoteException {
        File localFile = getLocalFile(file, baseLocalDir);
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading directory: " + file);
            }
            if (!cdBaseRemoteDirectory(file.getRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getRelativePath() + " does not exist => ignoring");
                transferIgnored(transferInfo, file);
                return;
            }
            // in fact, useless but probably expected
            // XXX handle if exists but it is a file
            if (!localFile.exists()) {
                localFile.mkdirs();
            }
            transferSucceeded(transferInfo, file);
        } else if (file.isFile()) {
            // file => simply download it

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getRelativePath() + " => " + localFile.getAbsolutePath());
            }

            // XXX check if the remote file exists?

            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getParentRelativePath() + " does not exist => ignoring file " + file.getRelativePath());
                transferIgnored(transferInfo, file);
                return;
            }

            // XXX lock the file?
            OutputStream os = new FileOutputStream(localFile);
            try {
                if (ftpClient.retrieveFile(file.getName(), os)) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file);
                }
            } finally {
                os.close();
            }
        } else {
            transferIgnored(transferInfo, file);
        }
    }

    private File getLocalFile(TransferFile transferFile, File localFile) {
        if (transferFile.getRelativePath() == TransferFile.CWD) {
            return localFile;
        }
        return new File(localFile, transferFile.getRelativePath(true));
    }

    private void transferSucceeded(TransferInfo transferInfo, TransferFile file) {
        transferInfo.addTransfered(file);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Transfered: " + file);
        }
    }

    private void transferFailed(TransferInfo transferInfo, TransferFile file) {
        transferInfo.addFailed(file);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Failed: " + file);
        }
    }

    private void transferIgnored(TransferInfo transferInfo, TransferFile file) {
        transferInfo.addIgnored(file);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Ignored: " + file);
        }
    }

    private void init() {
        if (ftpClient != null) {
            return;
        }
        LOGGER.log(Level.FINE, "FTP client creating");
        ftpClient = new FTPClient();

        if (io != null) {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(io));
        }
        LOGGER.log(Level.FINE, "Protocol command listener added");
    }

    private void ensureConnected() throws RemoteException {
        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private boolean cdBaseRemoteDirectory() throws IOException, RemoteException {
        return cdRemoteDirectory(baseRemoteDirectory, true);
    }

    private boolean cdBaseRemoteDirectory(String subdirectory, boolean create) throws IOException, RemoteException {
        assert subdirectory == null || !subdirectory.startsWith(TransferFile.SEPARATOR) : "Subdirectory must be null or relative [" + subdirectory + "]" ;

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(".")) { // NOI18N
            path = baseRemoteDirectory + TransferFile.SEPARATOR + subdirectory; // NOI18N
        }
        return cdRemoteDirectory(path, create);
    }

    private boolean cdRemoteDirectory(String directory, boolean create) throws IOException, RemoteException {
        LOGGER.fine("Changing directory to " + directory);
        boolean success = ftpClient.changeWorkingDirectory(directory);
        if (!success && create) {
            return createAndCdRemoteDirectory(directory);
        }
        return success;
    }

    /**
     * Create file path on FTP server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private boolean createAndCdRemoteDirectory(String filePath) throws IOException, RemoteException {
        LOGGER.fine("Creating file path " + filePath);
        if (filePath.startsWith(TransferFile.SEPARATOR)) { // NOI18N
            // enter root directory
            if (!ftpClient.changeWorkingDirectory(TransferFile.SEPARATOR)) { // NOI18N
                throw new RemoteException("Cannot change root directory '/' [" + ftpClient.getReplyString() + "]");
            }
        }
        for (String dir : filePath.split(TransferFile.SEPARATOR)) { // NOI18N
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!ftpClient.changeWorkingDirectory(dir)) {
                if (!ftpClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot create directory: " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    throw new RemoteException("Cannot create directory '" + dir + "' [" + ftpClient.getReplyString() + "]");
                } else if (!ftpClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot enter directory: " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + ftpClient.getReplyString() + "]");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Directory '" + ftpClient.printWorkingDirectory() + "' created and entered");
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [remote configuration: "); // NOI18N
        sb.append(configuration);
        sb.append(", baseRemoteDirectory: "); // NOI18N
        sb.append(baseRemoteDirectory);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    private static class PrintCommandListener implements ProtocolCommandListener {
        private final InputOutput io;

        public PrintCommandListener(InputOutput io) {
            assert io != null;
            this.io = io;
        }

        public void protocolCommandSent(ProtocolCommandEvent event) {
            processEvent(event);
        }

        public void protocolReplyReceived(ProtocolCommandEvent event) {
            processEvent(event);
        }

        private void processEvent(ProtocolCommandEvent event) {
            if (event.isReply()
                    && (FTPReply.isNegativeTransient(event.getReplyCode()) || FTPReply.isNegativePermanent(event.getReplyCode()))) {
                io.getErr().print(event.getMessage());
                io.getErr().flush();
            } else {
                io.getOut().print(event.getMessage());
                io.getOut().flush();
            }
        }
    }
}
