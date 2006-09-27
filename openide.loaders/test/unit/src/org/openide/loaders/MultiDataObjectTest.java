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

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.filesystems.*;
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.openide.*;
import org.openide.util.Enumerations;

/**
 * @author Jaroslav Tulach
 */
public class MultiDataObjectTest extends NbTestCase {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    /** Creates new DataObjectTest */
    public MultiDataObjectTest (String name) {
        super (name);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();
        
        MockServices.setServices(Pool.class);
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createData(fs.getRoot(), "from/x.seconda");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(SimpleObject.class, one.getClass());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
    
    public void testTheSetOfSecondaryEntriesIsSaidToGetInconsistent() throws Exception {
        for (int i = 0; i < 10; i++) {
            err.log(i + " getting children of to");
            DataObject[] to1 = to.getChildren();
            err.log(i + " getting children of from");
            DataObject[] from1 = from.getChildren();
            err.log(i + " getting files of object1");
            Object[] arr1 = one.files().toArray();
            err.log(i + " moving the object");
            one.move(to);
            err.log(i + " 2nd children of to");
            DataObject[] to2 = to.getChildren();
            err.log(i + " 2nd children of from");
            DataObject[] from2 = from.getChildren();
            err.log(i + " 2nd  files of object1");
            Object[] arr2 = one.files().toArray();
            err.log(i + " checking results");
            
            assertEquals("Round " + i + " To is empty: " + Arrays.asList(to1), 0, to1.length);
            assertEquals("Round " + i + " From has one:" + Arrays.asList(from1), 1, from1.length);
            assertEquals("Round " + i + " One has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            assertEquals("Round " + i + " From is empty after move: " + Arrays.asList(from2), 0, from2.length);
            assertEquals("Round " + i + " To has one:" + Arrays.asList(to2), 1, to2.length);
            assertEquals("Round " + i + " One still has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }

    public void testConsistencyWithABitOfAsynchronicity() throws Exception {
        err.log(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.log(" getting children of from");
        DataObject[] from1 = from.getChildren();
        
        
        for (int i = 0; i < 10; i++) {
            err.log(i + " getting files of object1");
            Object[] arr1 = one.files().toArray();
            err.log(i + " moving the object");
            one.move(to);
            Object[] arr2 = one.files().toArray();
            err.log(i + " checking results");
            
            assertEquals("Round " + i + " One has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            assertEquals("Round " + i + " One still has two files" + Arrays.asList(arr1), 2, arr1.length);
            
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }

    public void testConsistencyWithABitOfAsynchronicityAndNoObservationsThatWouldMangeTheState() throws Exception {
        err.log(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.log(" getting children of from");
        DataObject[] from1 = from.getChildren();
        
        
        for (int i = 0; i < 10; i++) {
            err.log(i + " moving the object");
            one.move(to);
            err.log(i + " moving back");
            one.move(from);
            err.log(i + " end of cycle");
        }
    }

    public void testConsistencyWithContinuousQueryingForDeletedFiles() throws Exception {
        err.log(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.log(" getting children of from");
        DataObject[] from1 = from.getChildren();
        
        class Queri extends Thread 
        implements FileChangeListener, DataLoader.RecognizedFiles, PropertyChangeListener {
            public boolean stop;
            private List deleted = Collections.synchronizedList(new ArrayList());
            public Exception problem;
            
            public Queri() {
                super("Query background thread");
                setPriority(MAX_PRIORITY);
            }

            public void fileFolderCreated(FileEvent fe) {
            }

            public void fileDataCreated(FileEvent fe) {
            }

            public void fileChanged(FileEvent fe) {
            }

            public void fileDeleted(FileEvent fe) {
                deleted.add(fe.getFile());
            }

            public void fileRenamed(FileRenameEvent fe) {
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
            }
            
            public void run () {
                while(!stop) {
                    FileObject[] arr = (FileObject[]) deleted.toArray(new FileObject[0]);
                    DataLoader loader = SimpleLoader.getLoader(SimpleLoader.class);
                    err.log("Next round");
                    for (int i = 0; i < arr.length; i++) {
                        try {
                            err.log("Checking " + arr[i]);
                            DataObject x = loader.findDataObject(arr[i], this);
                            err.log("  has dobj: " + x);
                        } catch (IOException ex) {
                            if (problem == null) {
                                problem = ex;
                            }
                        }
                    }
                }
            }

            public void markRecognized(FileObject fo) {
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if ("afterMove".equals(evt.getPropertyName())) {
                    Thread.yield();
                }
            }
        }
        
        Queri que = new Queri();
        
        to.getPrimaryFile().addFileChangeListener(que);
        from.getPrimaryFile().addFileChangeListener(que);
        
        que.start();
        try {
            for (int i = 0; i < 10; i++) {
                err.log(i + " moving the object");
                one.move(to);
                err.log(i + " moving back");
                one.move(from);
                err.log(i + " end of cycle");
            }
        } finally {
            que.stop = true;
        }
        
        que.join();
        if (que.problem != null) {
            throw que.problem;
        }
        
        assertEquals("Fourty deleted files:" + que.deleted, 40, que.deleted.size());
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class);
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                // emulate the behaviour of form data object
                
                /* emulate!? this one is written too well ;-)
                FileObject primary = FileUtil.findBrother(fo, "prima");
                FileObject secondary = FileUtil.findBrother(fo, "seconda");
                
                if (primary == null || secondary == null) {
                    return null;
                }
                
                if (primary != fo && secondary != fo) {
                    return null;
                }
                 */
                
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }

        private void afterMove(FileObject f, FileObject retValue) {
            firePropertyChange("afterMove", null, null);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        public FileObject move(FileObject f, String suffix) throws IOException {
            FileObject retValue;
            retValue = super.move(f, suffix);
            
            SimpleLoader l = (SimpleLoader)getDataObject().getLoader();
            l.afterMove(f, retValue);
            
            return retValue;
        }
        
        
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
    }

}
