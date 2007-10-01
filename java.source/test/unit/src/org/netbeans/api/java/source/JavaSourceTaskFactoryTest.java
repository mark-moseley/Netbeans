/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.JavaSourceTaskFactoryManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaSourceTaskFactoryTest extends NbTestCase {
    
    public JavaSourceTaskFactoryTest(String testName) {
        super(testName);
    }
    
    private List<FileObject> files;
    private List<FileObject> filesWithTasks = new ArrayList<FileObject>();
    private Map<FileObject, CancellableTask<CompilationInfo>> file2Task = new HashMap();
    
    private Map<FileObject, CancellableTask<CompilationInfo>> addedTasks = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
    private Map<FileObject, CancellableTask<CompilationInfo>> removedTasks = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
    private Map<FileObject, CancellableTask<CompilationInfo>> rescheduled = new HashMap<FileObject, CancellableTask<CompilationInfo>>();

    private FileObject testDir;
    private FileObject testFile1;
    private FileObject testFile2;
    private DummyCancellableTask<CompilationInfo> task1;
    private DummyCancellableTask<CompilationInfo> task2;
    
    private JavaSourceTaskFactoryImplImpl jstf;
    private ClassPathProvider cpp;
    
    private Lookup.Result<JavaSourceTaskFactory> factories;
    
    protected void setUp() throws Exception {
        cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return createBootPath();
                    return null;
            }
        };
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            cpp
        }, this.getClass().getClassLoader());
        
        JavaSourceTaskFactoryManager.register();
        
        jstf = new JavaSourceTaskFactoryImplImpl();
        JavaSourceTaskFactory.ACCESSOR2 = new AccessorImpl();
        testDir = SourceUtilsTestUtil.makeScratchDir(this);
        testFile1 = testDir.createData("test1.java");
        testFile2 = testDir.createData("test2.java");
        task1 = new DummyCancellableTask<CompilationInfo>();
        task2 = new DummyCancellableTask<CompilationInfo>();
        
        file2Task.put(testFile1, task1);
        file2Task.put(testFile1, task2);
        
        assertNotNull(JavaSource.forFileObject(testFile1));
        assertNotNull(JavaSource.forFileObject(testFile2));
    }

    public void testTasksRegistration() throws Exception {
        JavaSourceTaskFactory.SYNCHRONOUS_EVENTS = true;
        
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
            cpp
        }, this.getClass().getClassLoader());
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        assertEquals(0, removedTasks.size());
        
        files = Arrays.asList(testFile2);
        
        addedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile1, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), removedTasks.values().iterator().next());
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile2, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile2), addedTasks.values().iterator().next());

        files = Collections.emptyList();
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile2, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile2), removedTasks.values().iterator().next());
        
        assertEquals(0, addedTasks.size());
        
        files = Arrays.asList(testFile1);
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        assertEquals(0, removedTasks.size());
        
        files = Collections.emptyList();
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile1, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), removedTasks.values().iterator().next());
        
        assertEquals(0, addedTasks.size());
    }
    
    public void testTasksRescheduling() throws Exception {
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
            cpp
        }, this.getClass().getClassLoader());
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        jstf.reschedule(testFile1);
        
        assertEquals(1, rescheduled.size());
        assertEquals(testFile1, rescheduled.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), rescheduled.values().iterator().next());
        
        //#84783: the IAE was temporarily disabled:
//        //test if the IllegalArgumentException is thrown correctly:
//        try {
//            jstf.reschedule(testFile2);
//            fail("Did not throw an IllegalArgumentException");
//        } catch (IllegalArgumentException e) {
//        }
    }
    
    public void testFileIsReclaimable() throws Exception {
        Reference fileRef = new WeakReference(testFile1);
        Reference jsRef = new WeakReference(JavaSource.forFileObject(testFile1));
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
        }, this.getClass().getClassLoader());
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        files = Collections.emptyList();
        
        jstf.fireChangeEvent();
        
        filesWithTasks.clear();
        file2Task.clear();
        
        addedTasks.clear();
        removedTasks.clear();
        rescheduled.clear();
        
        testDir = null;
        testFile1 = null;
        testFile2 = null;
        task1 = null;
        task2 = null;
        
        assertGC("", fileRef);
        assertGC("", jsRef);
    }
    
    public void testDeadlock88782() throws Exception {
        files = Collections.emptyList();
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
                    jstf,
                    cpp
        }, this.getClass().getClassLoader());
        
        final CountDownLatch l = new CountDownLatch(2);
        final Object lock = new Object();
        
        Logger.getLogger(JavaSourceTaskFactory.class.getName()).setLevel(Level.FINEST);
        
        Logger.getLogger(JavaSourceTaskFactory.class.getName()).addHandler(new Handler() {
            public void publish(LogRecord record) {
                if (JavaSourceTaskFactory.BEFORE_ADDING_REMOVING_TASKS.equals(record.getMessage())) {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e) {
                        Logger.global.log(Level.SEVERE, "", e);
                    }
                    synchronized (lock) {
                    }
                }
                if (JavaSourceTaskFactory.FILEOBJECTS_COMPUTATION.equals(record.getMessage())) {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e) {
                        Logger.global.log(Level.SEVERE, "", e);
                    }
                }
            }
            public void flush() {}
            public void close() throws SecurityException {}
        });
        
        Thread t1 = new Thread() {
            public void run() {
                synchronized (lock) {
                    SourceUtilsTestUtil.setLookup(new Object[] {
                        JavaDataLoader.getLoader(JavaDataLoader.class),
                                jstf,
                                new JavaSourceTaskFactoryImplImpl(),
                                cpp
                    }, this.getClass().getClassLoader());
                }
            }
        };
        
        t1.start();

        Thread t2 = new Thread() {
            public void run() {
                jstf.fireChangeEvent();
            }
        };
        
        t2.start();
        
        t1.join();
        t2.join();
    }
    
    private ClassPath createBootPath () {
        try {
            String bootPath = System.getProperty ("sun.boot.class.path");
            String[] paths = bootPath.split(File.pathSeparator);
            List<URL>roots = new ArrayList<URL> (paths.length);
            for (String path : paths) {
                File f = new File (path);            
                if (!f.exists()) {
                    continue;
                }
                URL url = f.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                }
                roots.add (url);
            }
            return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
        } catch (MalformedURLException ex) {}
        return null;
    }
    
    private class AccessorImpl implements JavaSourceTaskFactory.Accessor2 {
        
        public void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority) throws IOException {
            addedTasks.put(js.getFileObjects().iterator().next(), task);
        }

        public void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task) {
            removedTasks.put(js.getFileObjects().iterator().next(), task);
        }
        
        public void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task) {
            rescheduled.put(js.getFileObjects().iterator().next(), task);
        }
        
    }
    
    private static class DummyCancellableTask<CompilationInfo> implements CancellableTask<CompilationInfo> {
        
        public void cancel() {
        }

        public void run(CompilationInfo parameter) {
        }
        
    }

    private class JavaSourceTaskFactoryImplImpl extends JavaSourceTaskFactory {
        public JavaSourceTaskFactoryImplImpl() {
            super(Phase.UP_TO_DATE, Priority.MAX);
        }

        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            filesWithTasks.add(file);
            return file2Task.get(file);
        }

        public synchronized List<FileObject> getFileObjects() {
            return files;
        }

        private void fireChangeEvent() {
            super.fileObjectsChanged();
        }
        
    }
    
    private static class ChangeableLookup extends ProxyLookup {
        
        public void setLookupsImpl(Lookup[] lookups) {
            setLookups(lookups);
        }
    }
    
}
