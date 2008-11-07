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

import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.connections.spi.RemoteClient.PathInfo;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Remote client able to connect/disconnect to a remote server
 * as well as download/upload files from/to a remote server.
 * <p>
 * Every method throws {@link RemoteException} if any error occurs.
 * <p>
 * This class is not threadsafe.
 * @author Tomas Mysik
 */
public class RemoteClient implements Cancellable {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());
    private static final String NB_METADATA_DIR = "nbproject"; // NOI18N
    private static final Set<String> IGNORED_REMOTE_DIRS = new HashSet<String>(Arrays.asList(".", "..")); // NOI18N
    private static final int TRIES_TO_TRANSFER = 3; // number of tries if file download/upload fails
    private static final String TMP_NEW_SUFFIX = ".new~"; // NOI18N
    private static final String TMP_OLD_SUFFIX = ".old~"; // NOI18N

    private final RemoteConfiguration configuration;
    private final InputOutput io;
    private final String baseRemoteDirectory;
    private final org.netbeans.modules.php.project.connections.spi.RemoteClient remoteClient;
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

        // base remote directory
        StringBuilder baseDir = new StringBuilder(configuration.getInitialDirectory());
        if (additionalInitialSubdirectory != null && additionalInitialSubdirectory.length() > 0) {
            if (!additionalInitialSubdirectory.startsWith(TransferFile.SEPARATOR)) {
                throw new IllegalArgumentException("additionalInitialSubdirectory must start with " + TransferFile.SEPARATOR);
            }
            baseDir.append(additionalInitialSubdirectory);
        }
        baseRemoteDirectory = baseDir.toString().replaceAll(TransferFile.SEPARATOR + "{2,}", TransferFile.SEPARATOR); // NOI18N

        assert baseRemoteDirectory.startsWith(TransferFile.SEPARATOR) : "base directory must start with " + TransferFile.SEPARATOR + ": " + baseRemoteDirectory;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Remote client created with configuration: " + configuration + " and base remote directory: " + baseRemoteDirectory);
        }

        // remote client itself
        org.netbeans.modules.php.project.connections.spi.RemoteClient client = null;
        for (RemoteConnectionProvider provider : RemoteConnections.get().getConnectionProviders()) {
            client = provider.getRemoteClient(configuration, io);
            if (client != null) {
                break;
            }
        }
        assert client != null : "no suitable remote client for configuration: " + configuration;
        this.remoteClient = client;
    }

    public void connect() throws RemoteException {
        remoteClient.connect();

        // cd to base remote directory
        if (!cdBaseRemoteDirectory()) {
            if (remoteClient.isConnected()) {
                disconnect();
            }
            throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", baseRemoteDirectory), remoteClient.getReplyString());
        }
    }

    public void disconnect() throws RemoteException {
        remoteClient.disconnect();
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

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToUpload) {
            if (isVisible(FileUtil.toFile(fo))) {
                LOGGER.fine("File " + fo + " added to upload queue");
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            } else {
                LOGGER.fine("File " + fo + " NOT added to upload queue [invisible]");
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare upload cancelled");
                break;
            }

            TransferFile file = queue.poll();

            if (!files.add(file)) {
                // file already in set
                LOGGER.fine("File " + file + " already in queue");
                continue;
            }

            if (file.isDirectory()) {
                File f = getLocalFile(file, baseLocalDir);
                File[] children = f.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (isVisible(child)) {
                            LOGGER.fine("File " + child + " added to upload queue");
                            queue.offer(TransferFile.fromFile(child, baseLocalAbsolutePath));
                        } else {
                            LOGGER.fine("File " + child + " NOT added to upload queue [invisible]");
                        }
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
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
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
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            String fileName = file.getName();
            String tmpFileName = fileName + TMP_NEW_SUFFIX;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + tmpFileName);
            }
            // XXX lock the file?
            InputStream is = new FileInputStream(new File(baseLocalDir, file.getRelativePath(true)));
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (remoteClient.storeFile(tmpFileName, is)) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("The %d. attempt to upload '%s' was successful", i, file.getRelativePath() + TMP_NEW_SUFFIX));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to upload '%s' was NOT successful", i, file.getRelativePath() + TMP_NEW_SUFFIX));
                    }
                }
            } finally {
                is.close();
                if (success) {
                    success = moveRemoteFile(fileName, tmpFileName);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s renamed to %s: %s", tmpFileName, fileName, success));
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(fileName, true));
                    boolean deleted = remoteClient.deleteFile(tmpFileName);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully uploaded file %s deleted: %s", file.getRelativePath() + TMP_NEW_SUFFIX, deleted));
                    }
                }
            }
        }
    }

    private boolean moveRemoteFile(String fileName, String tmpFileName) throws RemoteException {
        String oldPath = fileName + TMP_OLD_SUFFIX;
        boolean moved = remoteClient.rename(tmpFileName, fileName);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("File %s directly renamed to %s: %s", tmpFileName, fileName, moved));
        }
        if (moved) {
            return true;
        }
        // possible cleanup
        remoteClient.deleteFile(oldPath);

        // try to move the old file, move the new file, delete the old file
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
        }
        moved = remoteClient.rename(fileName, oldPath);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(1) File %s renamed to %s: %s", fileName, oldPath, moved));
        }
        if (!moved) {
            return false;
        }
        moved = remoteClient.rename(tmpFileName, fileName);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(2) File %s renamed to %s: %s", tmpFileName, fileName, moved));
        }
        if (!moved) {
            // try to restore the original file
            boolean restored = remoteClient.rename(oldPath, fileName);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPath, fileName, restored));
            }
        } else {
            boolean deleted = remoteClient.deleteFile(oldPath);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(3) File %s deleted: %s", oldPath, deleted));
            }
        }
        return moved;
    }

    public Set<TransferFile> prepareDownload(FileObject baseLocalDirectory, FileObject... filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        ensureConnected();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToDownload) {
            if (isVisible(FileUtil.toFile(fo))) {
                LOGGER.fine("File " + fo + " added to download queue");
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            } else {
                LOGGER.fine("File " + fo + " NOT added to download queue [invisible]");
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare download cancelled");
                break;
            }

            TransferFile file = queue.poll();

            if (!files.add(file)) {
                // file already in set
                LOGGER.fine("File " + file + " already in queue");
                continue;
            }

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
                    String relPath = relativePath.toString();
                    for (RemoteFile child : remoteClient.listFiles(new PathInfo(baseRemoteDirectory, relPath))) {
                        if (isVisible(child)) {
                            LOGGER.fine("File " + child + " added to download queue");
                            queue.offer(TransferFile.fromRemoteFile(child, baseRemoteDirectory, relPath));
                        } else {
                            LOGGER.fine("File " + child + " NOT added to download queue [invisible]");
                        }
                    }
                } catch (RemoteException exc) {
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
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                }
            }
        } finally {
            // refresh filesystem
            FileUtil.refreshFor(baseLocalDir);

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
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getRelativePath()));
                return;
            }
            // in fact, useless but probably expected
            if (!localFile.exists()) {
                if (!mkLocalDirs(localFile)) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", localFile));
                    return;
                }
            } else if (localFile.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            }
            transferSucceeded(transferInfo, file);
        } else if (file.isFile()) {
            // file => simply download it

            // #142682 - because from the ui we get only files (folders are removed) => ensure parent folder exists
            File parent = localFile.getParentFile();
            assert parent != null : "File " + localFile + " has no parent file?!";
            if (!parent.exists()) {
                if (!mkLocalDirs(parent)) {
                    transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", parent));
                    return;
                }
            } else if (parent.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            } else if (localFile.exists() && !localFile.canWrite()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FileNotWritable", localFile));
                return;
            }
            assert parent.isDirectory() : "Parent file of " + localFile + " must be a directory";

            File tmpLocalFile = new File(localFile.getAbsolutePath() + TMP_NEW_SUFFIX);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getRelativePath() + " => " + tmpLocalFile.getAbsolutePath());
            }

            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getParentRelativePath() + " does not exist => ignoring file " + file.getRelativePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            // XXX lock the file?
            OutputStream os = new FileOutputStream(tmpLocalFile);
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (remoteClient.retrieveFile(file.getName(), os)) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("The %d. attempt to download '%s' was successful", i, file.getRelativePath()));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to download '%s' was NOT successful", i, file.getRelativePath()));
                    }
                }
            } finally {
                os.close();
                if (success) {
                    // move the file
                    success = moveLocalFile(localFile, tmpLocalFile);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s renamed to %s: %s", tmpLocalFile, localFile, success));
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(file.getName(), false));
                    boolean deleted = tmpLocalFile.delete();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully downloaded file %s deleted: %s", tmpLocalFile, deleted));
                    }
                }
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpUnknownFileType", file.getRelativePath()));
        }
    }

    private boolean moveLocalFile(final File localFile, final File tmpLocalFile) {
        final boolean[] moved = new boolean[1];
        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                File oldPath = new File(localFile.getAbsolutePath() + TMP_OLD_SUFFIX);
                String tmpLocalFileName = tmpLocalFile.getName();
                String localFileName = localFile.getName();
                String oldPathName = oldPath.getName();

                if (!localFile.exists()) {
                    moved[0] = renameLocalFileTo(tmpLocalFile, localFile);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s directly renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                    }
                    if (moved[0]) {
                        return;
                    }
                }
                // possible cleanup
                deleteLocalFile(oldPath, ""); // NOI18N

                // try to move the old file, move the new file, delete the old file
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
                }
                // intentional usage of java.io.File!!
                //  (if the file is opened in the editor, it's not closed, just refreshed)
                moved[0] = localFile.renameTo(oldPath);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(1) File %s renamed to %s: %s", localFileName, oldPathName, moved[0]));
                }
                if (!moved[0]) {
                    return;
                }
                moved[0] = renameLocalFileTo(tmpLocalFile, localFile);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(2) File %s renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                }
                if (!moved[0] && oldPath.exists() && !localFile.exists()) {
                    // try to restore the original file
                    boolean restored = renameLocalFileTo(oldPath, localFile);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPathName, localFileName, restored));
                    }
                    return;
                }
                deleteLocalFile(oldPath, "(3) "); // NOI18N
            }
        });
        assert moved[0] || !moved[0];
        return moved[0];
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

    private void transferFailed(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isFailed(file)) {
            transferInfo.addFailed(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Failed: " + file + ", reason: " + reason);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Failed: " + file + ", reason: " + reason + " [ignored, failed already]");
            }
        }
    }

    private void transferIgnored(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isIgnored(file)) {
            transferInfo.addIgnored(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Ignored: " + file + ", reason: " + reason);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Ignored: " + file + ", reason: " + reason + " [ignored, ignored already]");
            }
        }
    }

    private String getFailureMessage(String fileName, boolean upload) {
        String message = remoteClient.getNegativeReplyString();
        if (message == null) {
            message = NbBundle.getMessage(RemoteClient.class, upload ? "MSG_FtpCannotUploadFile" : "MSG_FtpCannotDownloadFile", fileName);
        }
        return message;
    }

    private void ensureConnected() throws RemoteException {
        if (!remoteClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private boolean cdBaseRemoteDirectory() throws RemoteException {
        return cdRemoteDirectory(baseRemoteDirectory, true);
    }

    private boolean cdBaseRemoteDirectory(String subdirectory, boolean create) throws RemoteException {
        assert subdirectory == null || !subdirectory.startsWith(TransferFile.SEPARATOR) : "Subdirectory must be null or relative [" + subdirectory + "]" ;

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(TransferFile.CWD)) {
            path = baseRemoteDirectory + TransferFile.SEPARATOR + subdirectory;
        }
        return cdRemoteDirectory(path, create);
    }

    private boolean cdRemoteDirectory(String directory, boolean create) throws RemoteException {
        LOGGER.fine("Changing directory to " + directory);
        boolean success = remoteClient.changeWorkingDirectory(directory);
        if (!success && create) {
            return createAndCdRemoteDirectory(directory);
        }
        return success;
    }

    /**
     * Create file path on FTP server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private boolean createAndCdRemoteDirectory(String filePath) throws RemoteException {
        LOGGER.fine("Creating file path " + filePath);
        if (filePath.startsWith(TransferFile.SEPARATOR)) {
            // enter root directory
            if (!remoteClient.changeWorkingDirectory(TransferFile.SEPARATOR)) {
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", "/"), remoteClient.getReplyString());
            }
        }
        for (String dir : filePath.split(TransferFile.SEPARATOR)) {
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!remoteClient.changeWorkingDirectory(dir)) {
                if (!remoteClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot create directory: " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotCreateDirectory", dir), remoteClient.getReplyString());
                } else if (!remoteClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot enter directory: " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + remoteClient.getReplyString() + "]");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Directory '" + remoteClient.printWorkingDirectory() + "' created and entered");
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

    private static boolean isVisible(File file) {
        assert file != null;
        if (file.getName().equals(NB_METADATA_DIR)) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    private boolean isVisible(RemoteFile file) {
        assert file != null;
        if (file.isDirectory()) {
            return !IGNORED_REMOTE_DIRS.contains(file.getName());
        }
        return true;
    }

    private static boolean mkLocalDirs(File folder) {
        try {
            FileUtil.createFolder(folder);
        } catch (IOException exc) {
            LOGGER.log(Level.INFO, null, exc);
            return false;
        }
        return true;
    }

    /**
     * Similar to {@link File#renameTo(java.io.File)} but uses {@link FileObject}s.
     * @param source a source file, must exist.
     * @param target a target file, cannot exist.
     * @return <code>true</code> if the rename was successful, <code>false</code> otherwise.
     */
    private static boolean renameLocalFileTo(File source, File target) {
        long start = 0L;
        if (LOGGER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
        }
        assert source.exists() : "Source file must exist " + source;
        assert !target.exists() : "Target file cannot exist " + target;

        FileObject sourceFO = FileUtil.toFileObject(source);
        assert sourceFO != null : "Source fileobject must exist " + source;

        String name = getName(target.getName());
        String ext = FileUtil.getExtension(target.getName());

        boolean moved = false;
        try {
            FileLock lock = sourceFO.lock();
            try {
                sourceFO.rename(lock, name, ext);
                moved = true;
            } catch (IOException exc) {
                LOGGER.log(Level.INFO, null, exc);
            } finally {
                lock.releaseLock();
            }
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, null, exc);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Move %s -> %s took: %sms", source, target, (System.currentTimeMillis() - start)));
        }
        return moved;
    }

    private void deleteLocalFile(File file, String logMsgPrefix) {
        if (!file.exists()) {
            return;
        }
        boolean deleted = file.delete();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format(logMsgPrefix + "File %s deleted: %s", file.getName(), deleted));
        }
    }

    private static String getName(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }
}
