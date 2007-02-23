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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.diff.EncodedReaderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.*;
import java.util.*;

import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Stream source for diffing CVS managed files.
 * 
 * @author Maros Sandor
 */
public class DiffStreamSource extends StreamSource {
        
    private final File      baseFile;
    private final String    revision;
    private final String    title;
    private String          mimeType = "text/plain";    // reasonable default

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;
    private boolean         binary;

    private ExecutorGroup   group;
    private boolean         initialized;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(File baseFile, String revision, String title) {
        this.baseFile = baseFile;
        this.revision = revision;
        this.title = title;
    }

    public String getName() {
        return baseFile.getName();
    }

    public String getTitle() {
        return title;
    }

    public synchronized void setGroup(ExecutorGroup group) {
        this.group = group;
    }

    public synchronized String getMIMEType() {
        try {
            init(null);
        } catch (IOException e) {
            return null;
        }
        return mimeType;
    }

    public synchronized Reader createReader() throws IOException {
        init(group);
        if (revision == null || remoteFile == null) return null;
        if (binary) {
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "BK5001", getTitle()));
        } else {
            FileObject remoteFo = FileUtil.toFileObject(remoteFile);
            return EncodedReaderFactory.getDefault().getReader(remoteFo, null);
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    public boolean isEditable() {
        return VersionsCache.REVISION_CURRENT.equals(revision);
    }

    public synchronized Lookup getLookup() {
        try {
            init(null);
        } catch (IOException e) {
            return Lookups.fixed();
        }
        if (remoteFile == null) return Lookups.fixed();
        FileObject remoteFo = FileUtil.toFileObject(remoteFile);
        if (remoteFo == null) return Lookups.fixed();

        return Lookups.fixed(remoteFo);
    }
    
    private static Set<File> getAllDataObjectFiles(File file) {
        Set<File> filesToCheckout = new HashSet<File>(2);
        filesToCheckout.add(file);
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                Set<FileObject> fileObjects = dao.files();
                for (FileObject fileObject : fileObjects) {
                    filesToCheckout.add(FileUtil.toFile(fileObject));
                }
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return filesToCheckout;
    }
    
    /**
     * Loads data over network.
     *
     * @param group combines multiple loads or <code>null</code>
     * Note that this group must not be executed later on. 
     */
    synchronized void init(ExecutorGroup group) throws IOException {
        if (initialized) return;
        initialized = true;
        if (revision == null) return;
        binary = !CvsVersioningSystem.getInstance().isText(baseFile);
        try {
            if (isEditable()) {
                // we cannot move editable documents because that would break Document sharing
                remoteFile = VersionsCache.getInstance().getRemoteFile(baseFile, revision, group);
            } else {
                File tempFolder = Utils.getTempFolder();
                // To correctly get content of the base file, we need to checkout all files that belong to the same
                // DataObject. One example is Form files: data loader removes //GEN:BEGIN comments from the java file but ONLY
                // if it also finds associate .form file in the same directory
                Set<File> allFiles = getAllDataObjectFiles(baseFile);
                for (File file : allFiles) {
                    boolean isBase = file.equals(baseFile); 
                    File rf = VersionsCache.getInstance().getRemoteFile(file, isBase ? revision : VersionsCache.REVISION_BASE, group);
                    File newRemoteFile = new File(tempFolder, file.getName());
                    Utils.copyStreamsCloseAll(new FileOutputStream(newRemoteFile), new FileInputStream(rf));
                    newRemoteFile.deleteOnExit();
                    if (isBase) {
                        remoteFile = newRemoteFile;
                    }
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                binary = !CvsVersioningSystem.getInstance().isText(remoteFile);
            }
        } catch (Exception e) {
            IOException failure = new IOException("Cannot initialize stream source"); // NOI18N
            failure.initCause(e);
            throw failure;
        }
        FileObject fo = FileUtil.toFileObject(baseFile);
        if (fo == null && remoteFile != null) {
            fo = FileUtil.toFileObject(remoteFile);
        }
        if (fo != null) {
            mimeType = fo.getMIMEType();
        } else if (binary) {
            mimeType = "application/octet-stream"; // NOI18N
        } else {
            mimeType = "text/plain"; // NOI18N
        }
    }
}
