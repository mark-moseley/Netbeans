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

package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.SystemAction;

/**
 * @author Radek Matous
 */
public final class FileBasedFileSystem extends FileSystem {
    private static Map allInstances = new HashMap();
    private final FileObjectFactory factory;

    //only for tests purposes
    public static void reinitForTests() {
        FileBasedFileSystem.allInstances = new HashMap();
    }
    
    public static FileBasedFileSystem getInstance(final File file) {
        FileBasedFileSystem retVal;
        final FileInfo fInfo = new FileInfo(file);
        final FileInfo rootInfo = fInfo.getRoot();

        synchronized (FileBasedFileSystem.allInstances) {
            final File rootFile = rootInfo.getFile();
            retVal = (FileBasedFileSystem) FileBasedFileSystem.allInstances.get(rootFile);
            if (retVal == null) {
                if (rootInfo.isConvertibleToFileObject()) {
                    retVal = new FileBasedFileSystem(rootFile);
                    FileBasedFileSystem.allInstances.put(rootFile, retVal);
                }
            }
        }
        return retVal;
    }
    
    public static final FileObject getFileObject(final File file) {
        FileBasedFileSystem fs = getInstance(file);
        return (fs != null) ? fs.findFileObject(file) : null;
    }
    

    static Collection getInstances() {
        synchronized (FileBasedFileSystem.allInstances) {
            return new ArrayList(allInstances.values());
        }
    }
    
    static int getSize () {
        synchronized (FileBasedFileSystem.allInstances) {
            return allInstances.size();
        }        
    }
    
    private FileBasedFileSystem(final File rootFile) {
        this.factory = FileObjectFactory.getInstance(new FileInfo(rootFile));
    }

    public final org.openide.filesystems.FileObject findResource(final String name) {
        File f = new File(name);
        assert f.getAbsolutePath().replace('\\', '/').equals(name.replace('\\', '/')) : name + " versus " + f.getAbsolutePath();
        return findFileObject(f);
    }

    public final FileObject findFileObject(final File f) {
        return findFileObject(new FileInfo (f));
    }
    
    public final FileObject findFileObject(final FileInfo fInfo) {
        File f = fInfo.getFile();
        boolean issue45485 = fInfo.isWindows() && f.getName().endsWith(".");//NOI18N        
        if (issue45485) {
            File f2 = FileUtil.normalizeFile(f);
            issue45485 = !f2.getName().endsWith(".");
            if (issue45485) return null;
        }
        final FileObject retVal = (getFactory().findFileObject(fInfo));
        return (retVal != null && retVal.isValid()) ? retVal : null;
    }

    public final org.openide.filesystems.FileObject getRoot() {
        return getFactory().getRoot();
    }

    public final String getDisplayName() {
        return getFactory().getRoot().getRealRoot().getPath();
    }

    public final SystemAction[] getActions() {
        return new SystemAction[] {};
    }

    public final SystemAction[] getActions(final Set/*<FileObject>*/ foSet) {
        return new SystemAction[] {};

    }

    public final void refresh(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        stopWatch.start();
        try {
            this.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    getFactory().refreshAll(expected);
                }            
            });
        } catch(IOException iex) {/*method refreshAll doesn't throw IOException*/}
        stopWatch.stop();
	
        // print refresh stats unconditionally in trunk
        Logger.getLogger("org.netbeans.modules.masterfs.REFRESH").fine(
            "FS.refresh statistics (" + Statistics.fileObjects() + "FileObjects):\n  " +
            Statistics.REFRESH_FS.toString() + "\n  " +
            Statistics.LISTENERS_CALLS.toString() + "\n  " + 
            Statistics.REFRESH_FOLDER.toString() + "\n  " + 
            Statistics.REFRESH_FILE.toString() + "\n"
        );

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }

    public final boolean isReadOnly() {
        return false;
    }

    public final String toString() {
        return getDisplayName();
    }

    public final FileObjectFactory getFactory() {
        return factory;
    }
}
