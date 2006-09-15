/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import java.io.*;

/**
 * Takes care about retrieving various revisions of a file and caching them locally. 
 * HEAD revisions are not cached.
 * TODO: Files are never deleted from cache, should we address this? 
 * TODO: cache dead files
 * 
 * @author Maros Sandor
 */
public class VersionsCache {
    
    /**
     * Constant representing the current working revision.
     */ 
    public static final String  REVISION_CURRENT = ""; // NOI18N

    /**
     * Constant representing the base revision of the current working revision (the one in Entries).
     */ 
    public static final String  REVISION_BASE = "*"; // NOI18N
    
    /**
     * Constant representing the CVS HEAD revision.
     */ 
    public static final String  REVISION_HEAD    = "HEAD"; // NOI18N
    
    private static final String CACHE_DIR = "CVS/RevisionCache/"; // NOI18N
    
    private static VersionsCache instance = new VersionsCache();

    private long            purgeTimestamp = Long.MAX_VALUE;

    public static VersionsCache getInstance() {
        return instance;
    }
    
    private VersionsCache() {
    }

    /**
     * Retrieves repository version of the file, either from the local cache of revisions or, it that fails,
     * from the remote repository. CURRENT revision is the file itself. HEAD revisions are considered volatile
     * and their cached versions are purged with the {@link #purgeVolatileRevisions()} method.
     * 
     * @param revision revision to fetch
     * @param group that carries shared state. Note that this group must not be executed later on. This parameter can be null. 
     * @return File supplied file in the specified revision (locally cached copy) or null if this file does not exist
     * in the specified revision
     * @throws java.io.IOException
     * @throws org.netbeans.modules.versioning.system.cvss.IllegalCommandException
     * @throws org.netbeans.lib.cvsclient.command.CommandException
     * @throws org.netbeans.lib.cvsclient.connection.AuthenticationException
     * @throws org.netbeans.modules.versioning.system.cvss.NotVersionedException
     */ 
    public synchronized File getRemoteFile(File baseFile, String revision, ExecutorGroup group) throws IOException,
                IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        return getRemoteFile(baseFile, revision, group, false);
    }

    public synchronized File getRemoteFile(File baseFile, String revision, ExecutorGroup group, boolean quiet) throws IOException,
                IllegalCommandException, CommandException, AuthenticationException, NotVersionedException {
        String resolvedRevision = resolveRevision(baseFile, revision);
        File file = getCachedRevision(baseFile, resolvedRevision);
        if (file != null) return file;
        file = checkoutRemoteFile(baseFile, revision, group, quiet);
        if (file == null) return null;
        return saveRevision(baseFile, file, resolvedRevision);
    }

    private String resolveRevision(File baseFile, String revision) throws IOException {
        if (revision == REVISION_BASE) {
            return getBaseRevision(baseFile);
        }
        if (revision.equals(REVISION_HEAD)) {
            Entry entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(baseFile);
            if (entry != null && entry.getTag() != null) {
                return entry.getTag();
            }
        }
        return revision;
    }

    /**
     * Purges volatile revisions (i.g. HEAD) from cache. BEWARE: HEAD revisions are cached but there
     * are multiple HEADs, each branch has its own HEAD!
     */
    public void purgeVolatileRevisions() {
        purgeTimestamp = System.currentTimeMillis();
    }

    private File saveRevision(File baseFile, File file, String revision) {
        // do not create directories if they do not exist (deleted trees) 
        File cacheDir;
        if (baseFile.getParentFile().isDirectory()) {
            cacheDir = new File(baseFile.getParentFile(), CACHE_DIR);
        } else {
            cacheDir = file.getParentFile();
        }
            if (!cacheDir.exists() && !cacheDir.mkdirs()) return file;
        File destFile = new File(cacheDir, cachedName(baseFile, revision));
        try {
            FileInputStream fin = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(destFile);
            FileUtil.copy(fin, fos);
            fin.close();
            fos.close();
            // eventually delete the checked out file
            if (file.equals(baseFile)) {
                // safety check
                if (destFile.lastModified() <= baseFile.lastModified()) {
                    destFile.delete();
                    return null;
                }
            } else {
                file.delete();
            }
            return destFile;
        } catch (IOException e) {
            // ignore errors, cache is not that much important
        }
        return file;
    }

    private File getCachedRevision(File baseFile, String revision) {
        if (revision == REVISION_CURRENT) {
            return baseFile;
        }
        File cachedCopy = new File(baseFile.getParentFile(), CACHE_DIR + cachedName(baseFile, revision));
        if (isVolatile(revision)) {
            if (cachedCopy.lastModified() < purgeTimestamp) {
                cachedCopy.delete();
            }
        }
        if (cachedCopy.canRead()) return cachedCopy;
        return null;
    }

    private boolean isVolatile(String revision) {
        return revision.indexOf('.') == -1;
    }

    private String getBaseRevision(File file) throws IOException {
        Entry entry = CvsVersioningSystem.getInstance().getAdminHandler().getEntry(file);
        if (entry == null) {
            throw new IllegalArgumentException("Cannot get BASE revision, there is no Entry for the file: " + file.getAbsolutePath());
        }
        String rawRev = entry.getRevision();
        if (rawRev != null && rawRev.startsWith("-")) { // NOI18N
            // leading - means removed
            return rawRev.substring(1);
        }
        return rawRev;
    }

    private String cachedName(File baseFile, String revision) {
        return baseFile.getName() + "#" + revision;     // NOI18N   
    }

    private String getRepositoryForDirectory(File directory, String repository) {
        if (directory == null) return null;
        if (!directory.exists() && MetadataAttic.getMetadata(directory) == null) {
            return getRepositoryForDirectory(directory.getParentFile(), repository) + "/" + directory.getName(); // NOI18N
        }
        try {
            return CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(directory.getAbsolutePath(), repository); // NOI18N
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a specific revision of a file from repository. 
     * 
     * @param baseFile location of the file in local workdir (need not exist)
     * @param revision revision number to get
     * @param group that carries shared state. Note that this group must not be executed later on. This parameter can be null.
     * @param quiet
     * @return File file on disk (most probably located in some temp diretory) or null if this file does not exist
     * in repository in the specified revision
     * @throws IOException if some I/O error occurs during checkout
     */ 
    private File checkoutRemoteFile(File baseFile, String revision, ExecutorGroup group, boolean quiet) throws IOException {

        if (revision == REVISION_BASE) {
            // be optimistic, use the file available on disk if possible
            FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().createFileInformation(baseFile);
            if (info.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                return baseFile;
            }
        }
        revision = resolveRevision(baseFile, revision);
        
        GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
        String root = getCvsRoot(baseFile.getParentFile());
        CVSRoot cvsRoot = CVSRoot.parse(root);
        String repository = cvsRoot.getRepository();
        options.setCVSRoot(root);

        String repositoryPath = getRepositoryForDirectory(baseFile.getParentFile(), repository) + "/" + baseFile.getName(); // NOI18N

        CheckoutCommand cmd = new CheckoutCommand();
        cmd.setRecursive(false);
        assert repositoryPath.startsWith(repository) : repositoryPath + " does not start with: " + repository; // NOI18N  

        repositoryPath = repositoryPath.substring(repository.length());
        if (repositoryPath.startsWith("/")) { // NOI18N
            repositoryPath = repositoryPath.substring(1);
        }
        cmd.setModule(repositoryPath);
        cmd.setPipeToOutput(true);
        cmd.setCheckoutByRevision(revision);
        String msg  = NbBundle.getMessage(VersionsCache.class, "MSG_VersionsCache_FetchingProgress", revision, baseFile.getName());
        cmd.setDisplayName(msg);

        VersionsCacheExecutor executor = new VersionsCacheExecutor(cmd, options, quiet);
        if (group != null) {
            group.progress(msg);
            group.addExecutor(executor);
        }
        executor.execute();
        ExecutorSupport.wait(new ExecutorSupport [] { executor });
        if (group == null) {
            executor.getGroup().executed();
        }

        if (executor.isSuccessful()) {
            return executor.getCheckedOutVersion();
        } else {
            // XXX note that executor already handles/notifies failures
            IOException ioe = new IOException(NbBundle.getMessage(VersionsCache.class, "Bk4001", revision, baseFile.getName()));
            ioe.initCause(executor.getFailure());
            throw ioe;
        }

    }

    private String getCvsRoot(File baseFile) throws IOException {
        try {
            return  Utils.getCVSRootFor(baseFile);
        } catch (IOException e) {
            // the file is not versioned or deleted, try the attic
        }
        // Should this functionality be already in Utils.getCVSRootFor?
        CvsMetadata data = MetadataAttic.getMetadata(baseFile);
        if (data != null) {
            return data.getRoot();
        }
        throw new IOException("CVS/Root not found"); // NOI18N
    }

}
