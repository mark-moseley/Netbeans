/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Base class for tests.
 * @author Jesse Glick
 */
public abstract class TestBase extends NbTestCase {
    
    static {
        TestBase.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    protected TestBase(String name) {
        super(name);
    }
    
    protected File egdir;
    protected FileObject egdirFO;
    protected FreeformProject simple;
    protected FreeformProject extsrcroot;
    protected FileObject myAppJava;
    protected FileObject specialTaskJava;
    protected FileObject buildProperties;
    
    protected void setUp() throws Exception {
        super.setUp();
        egdir = FileUtil.normalizeFile(new File(System.getProperty("test.eg.dir")));
        assertTrue("example dir exists", egdir.exists());
        egdirFO = FileUtil.toFileObject(egdir);
        assertNotNull("have FileObject for " + egdir);
        FileObject projdir = egdirFO.getFileObject("simple");
        assertNotNull("found projdir", projdir);
        Project _simple = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("have a project", _simple);
        simple = (FreeformProject) _simple;
        myAppJava = projdir.getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("found MyApp.java", myAppJava);
        specialTaskJava = projdir.getFileObject("antsrc/org/foo/ant/SpecialTask.java");
        assertNotNull("found SpecialTask.java", specialTaskJava);
        buildProperties = projdir.getFileObject("build.properties");
        assertNotNull("found build.properties", buildProperties);
        projdir = egdirFO.getFileObject("extsrcroot/proj");
        assertNotNull("found projdir", projdir);
        Project _extsrcroot = ProjectManager.getDefault().findProject(projdir);
        assertNotNull("have a project", _extsrcroot);
        extsrcroot = (FreeformProject) _extsrcroot;
    }
    
    /** ChangeListener for tests. */
    protected static final class TestCL implements ChangeListener {
        private int changed = 0;
        public TestCL() {}
        public synchronized void stateChanged(ChangeEvent changeEvent) {
            changed++;
        }
        /** Return count of change events since last call. Resets count. */
        public synchronized int changeCount() {
            int x = changed;
            changed = 0;
            return x;
        }
    }
    
    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.
     */
    protected File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = workdir.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }
    
    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }
    
    /**
     * Make a temporary copy of a project to test dynamic changes.
     * Note: only copies the main project directory, not any external source roots.
     * (So don't use it on extsrcroot.)
     */
    protected FreeformProject copyProject(FreeformProject p) throws IOException {
        FileObject dir = p.getProjectDirectory();
        File newdir = copyFolder(FileUtil.toFile(dir));
        FileObject newdirFO = FileUtil.toFileObject(newdir);
        return (FreeformProject) ProjectManager.getDefault().findProject(newdirFO);
    }

    // XXX copied from AntBasedTestUtil in ant/project
    protected static final class TestPCL implements PropertyChangeListener {
        
        public final Set/*<String>*/ changed = new HashSet();
        public final Map/*<String,String*/ newvals = new HashMap();
        public final Map/*<String,String*/ oldvals = new HashMap();
        
        public TestPCL() {}
        
        public void reset() {
            changed.clear();
            newvals.clear();
            oldvals.clear();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            String nue = (String)evt.getNewValue();
            String old = (String)evt.getOldValue();
            changed.add(prop);
            if (prop != null) {
                newvals.put(prop, nue);
                oldvals.put(prop, old);
            } else {
                assert nue == null : "null prop name -> null new value";
                assert old == null : "null prop name -> null old value";
            }
        }
        
    }
    
}
