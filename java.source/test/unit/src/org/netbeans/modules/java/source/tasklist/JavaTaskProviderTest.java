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

package org.netbeans.modules.java.source.tasklist;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaTaskProviderTest extends NbTestCase {
    
    public JavaTaskProviderTest(String testName) {
        super(testName);
    }

    private FileObject src;
    private FileObject file1;
    private FileObject file2;
    private FileObject file3;
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        super.setUp();
        
        File f = getWorkDir();
        
        clearWorkDir();
        
        FileObject workDir = FileUtil.toFileObject(f);
        
        assertNotNull(f);
        
        src = workDir.createFolder("src");
        
        FileObject cache = workDir.createFolder("cache");
        FileObject build = workDir.createFolder("build");
        FileObject pack = src.createFolder("pack");
        
        file1 = pack.createData("file1.java");
        file2 = pack.createData("file2.java");
        file3 = pack.createData("file3.java");
        
        SourceUtilsTestUtil.prepareTest(src, build, cache);
    }
    
    public void testUpdates() throws Exception {
        JavaTaskProvider jtp = new JavaTaskProvider();
        TaskManagerImpl tm = new TaskManagerImpl();
        TaskScanningScopeImpl scope1 = new TaskScanningScopeImpl(file1, file2);
        
        TaskCache.getDefault().dumpErrors(src.getURL(), file1.getURL(), FileUtil.toFile(file1), Arrays.asList(new DiagnosticImpl(Kind.ERROR, "x", 3)));
        
        jtp.setScope(scope1, Accessor.DEFAULT.createCallback(tm, jtp));
        jtp.waitWorkFinished();
        
        assertTasks(file1, Task.create(file1, "nb-tasklist-error", "x", 3));
        assertTasks(file2);
        
        TaskCache.getDefault().dumpErrors(src.getURL(), file2.getURL(), FileUtil.toFile(file2), Arrays.asList(new DiagnosticImpl(Kind.ERROR, "y", 4)));
        JavaTaskProvider.refresh(file2);
        jtp.waitWorkFinished();
        
        assertTasks(file1, Task.create(file1, "nb-tasklist-error", "x", 3));
        assertTasks(file2, Task.create(file2, "nb-tasklist-error", "y", 4));
    }
    
    private void assertTasks(FileObject file, Task... tasks) {
        Collection<? extends Task> inTasklist = tasklist.get(file);
        
        if (inTasklist == null || inTasklist.isEmpty()) {
            assertTrue(Arrays.asList(tasks).toString(), tasks.length == 0);
        }
        
        assertEquals(tasks.length, inTasklist.size());
        
        int index = 0;
        
        for (Task t : inTasklist) {
            assertEquals(tasks[index++], t);
        }
    }

    private Map<FileObject, Collection<? extends Task>> tasklist = new HashMap<FileObject, Collection<? extends Task>>();
    
    private class TaskManagerImpl extends TaskManager {

        public void refresh(FileTaskScanner scanner, FileObject... files) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void refresh(FileTaskScanner scanner) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void refresh(TaskScanningScope scope) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void started(PushTaskScanner scanner) {}

        public void finished(PushTaskScanner scanner) {}

        public void setTasks(PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks) {
            tasklist.put(resource, tasks);
        }

        public void clearAllTasks(PushTaskScanner scanner) {
            tasklist.clear();
        }
        
    }
    
    private static class TaskScanningScopeImpl extends TaskScanningScope {

        private List<FileObject> files;
        private ChangeableLookup l;
        
        public TaskScanningScopeImpl(FileObject... files) {
            super("", "", null, false);
            this.files = Arrays.asList(files);
            this.l = new ChangeableLookup();
            setFiles(files);
        }
        
        public boolean isInScope(FileObject resource) {
            return files.contains(resource);
        }

        public void attach(Callback callback) {
        }

        public Lookup getLookup() {
            return l;
        }

        public Iterator<FileObject> iterator() {
            return files.iterator();
        }
        
        public void setFiles(FileObject... files) {
            l.setLookupsImpl(Lookups.fixed((Object[]) files));
        }
        
    }
    
    private static class DiagnosticImpl implements Diagnostic {

        private Kind k;
        private String message;
        private int line;

        public DiagnosticImpl(Kind k, String message, int line) {
            this.k = k;
            this.message = message;
            this.line = line;
        }
        
        public Kind getKind() {
            return k;
        }

        public Object getSource() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getPosition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getStartPosition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getLineNumber() {
            return line;
        }

        public long getColumnNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getCode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getMessage(Locale locale) {
            return message;
        }
        
    }
    
    private static class ChangeableLookup extends ProxyLookup {

        public void setLookupsImpl(Lookup... lookups) {
            super.setLookups(lookups);
        }
        
    }
    
}
