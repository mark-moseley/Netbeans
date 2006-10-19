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

package org.netbeans.upgrade.systemoptions;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 * @author Radek Matous
 */
class PropertiesStorage  {
    private static final String USERROOT_PREFIX = "/Preferences";//NOI18N
    private final static FileObject SFS_ROOT =
            Repository.getDefault().getDefaultFileSystem().getRoot();
    
    private final String folderPath;
    private String filePath;
            
    static PropertiesStorage instance(final String absolutePath) {
        return new PropertiesStorage(absolutePath);
    }
    
    FileObject preferencesRoot() throws IOException {
        return FileUtil.createFolder(SFS_ROOT, USERROOT_PREFIX);
    }
    
    
    /** Creates a new instance */
    private PropertiesStorage(final String absolutePath) {
        StringBuffer sb = new StringBuffer();
        sb.append(USERROOT_PREFIX).append(absolutePath);
        folderPath = sb.toString();
    }
        
    
    public Properties load() throws IOException {
        try {
            Properties retval = new Properties();
            InputStream is = inputStream();
            if (is != null) {
                try {
                    retval.load(is);
                } finally {
                    if (is != null) is.close();
                }
            }
            return retval;
        } finally {
        }
    }
    
    public void save(final Properties properties) throws IOException {
        if (!properties.isEmpty()) {
            OutputStream os = null;
            try {
                os = outputStream();
                properties.store(os,new Date().toString());//NOI18N
            } finally {
                if (os != null) os.close();
            }
        } else {
            FileObject file = toPropertiesFile();
            if (file != null) {
                file.delete();
            }
            FileObject folder = toFolder();
            while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                folder.delete();
                folder = folder.getParent();
            }
        }
    }
    
    private InputStream inputStream() throws IOException {
        FileObject file = toPropertiesFile(false);
        return (file == null) ? null : file.getInputStream();
    }
    
    private OutputStream outputStream() throws IOException {
        FileObject fo = toPropertiesFile(true);
        final FileLock lock = fo.lock();
        final OutputStream os = fo.getOutputStream(lock);
        return new FilterOutputStream(os) {
            public void close() throws IOException {
                super.close();
                lock.releaseLock();
            }
        };
    }
    
    private String folderPath() {
        return folderPath;
    }
    
    private String filePath() {
        if (filePath == null) {
            String[] all = folderPath().split("/");//NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < all.length-1; i++) {
                sb.append(all[i]).append("/");//NOI18N
            }
            if (all.length > 0) {
                sb.append(all[all.length-1]).append(".properties");//NOI18N
            } else {
                sb.append("root.properties");//NOI18N
            }
            filePath = sb.toString();
        }
        return filePath;
    }        
    
    protected FileObject toFolder()  {
        return SFS_ROOT.getFileObject(folderPath);
    }
    
    protected  FileObject toPropertiesFile() {
        return SFS_ROOT.getFileObject(filePath());
    }
    
    protected FileObject toFolder(boolean create) throws IOException {
        FileObject retval = toFolder();
        if (retval == null && create) {
            retval = FileUtil.createFolder(SFS_ROOT, folderPath);
        }
        assert (retval == null && !create) || (retval != null && retval.isFolder());
        return retval;
    }
    
    protected FileObject toPropertiesFile(boolean create) throws IOException {
        FileObject retval = toPropertiesFile();
        if (retval == null && create) {
            retval = FileUtil.createData(SFS_ROOT,filePath());//NOI18N
        }
        assert (retval == null && !create) || (retval != null && retval.isData());
        return retval;
    }
}