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
package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import junit.textui.TestRunner;

/**
 * Tests shortcuts folder to ensure it handles wildcard keystrokes correctly. 
 */
public class ShortcutsFolderTest extends NbTestCase {
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(ShortcutsFolderTest.class));
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolderTest(String s) {
        super(s);
    }
    
    public void testHyphenation (String s) {
        HashSet set = new HashSet();
        char[] c = new String("ABCD").toCharArray();
        ShortcutsFolder.createHyphenatedPermutation (c, set, "-F5");
        
        assertTrue (set.contains("A-B-C-D-F5"));
    }
    
    public void testPermutations () {
        HashSet set = new HashSet();
        
        ShortcutsFolder.getAllPossibleOrderings("BANG", "-F5", set);
        String[] permutations = new String[] {
            "BNAG", "BNGA", "BGNA", "BAGN", "BGAN", 
            "ANBG", "ABGN", "AGBN", "ANGB", "ABNG", "AGNB",
            "NBGA", "NGBA", "NABG", "NGAB", "NBAG", "NAGB",
            "GNAB", "GBAN", "GBNA", "GNBA", "GANB", "GABN",
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of BANG not generated: " + permutations[i], 
                set.contains(permutations[i] + "-F5"));
        }
    }
    
    public void testPermutationsIncludeHyphenatedVariants() {
        HashSet set = new HashSet();
        
        ShortcutsFolder.getAllPossibleOrderings("BANG", "-F5", set);
        String[] permutations = new String[] {
            "B-N-A-G", "B-N-G-A", "B-G-N-A", "B-A-G-N", "B-G-A-N", 
            "A-N-B-G", "A-B-G-N", "A-G-B-N", "A-N-G-B", "A-B-N-G", "A-G-N-B",
            "N-B-G-A", "N-G-B-A", "N-A-B-G", "N-G-A-B", "N-B-A-G", "N-A-G-B",
            "G-N-A-B", "G-B-A-N", "G-B-N-A", "G-N-B-A", "G-A-N-B", "G-A-B-N",
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of BANG not generated: " + permutations[i], 
                set.contains(permutations[i] + "-F5"));
        }
    }
    
    public void testPermutationsContainConvertedWildcard () {
        String targetChar = (Utilities.getOperatingSystem() & Utilities.OS_MAC) != 0
            ? "M" : "C";
        
        String[] s = ShortcutsFolder.getPermutations("DA-F5");
        HashSet set = new HashSet (Arrays.asList(s));
        set.add ("DA-F5"); //Permutations will not contain the passed value
        
        String[] permutations = new String[] {
            targetChar+"A-F5", "A" + targetChar + "-F5",
            targetChar+"-A-F5", "A-" + targetChar + "-F5",
            "AD-F5", "A-D-F5", "D-A-F5"
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of DA-F5 not generated:" 
                + permutations[i] + "-(generated:" + set + ")",
                set.contains(permutations[i]));
        }
    }

    public void testPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator () {
        String targetChar = (Utilities.getOperatingSystem() & Utilities.OS_MAC) != 0
            ? "M" : "C";
        
        String[] s = ShortcutsFolder.getPermutations(targetChar + "A-F5");
        
        String[] permutations = new String[] {
            "A" + targetChar + "-F5", "A-" + targetChar + "-F5",
            "DA-F5", "D-A-F5"
        };
        
        HashSet set = new HashSet (Arrays.asList(s));
        set.add (targetChar + "A-F5"); //Permutations will not contain the passed value
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of " + targetChar + "A-F5 not generated:" 
                + permutations[i] + "-(generated:" + set + ")", 
                set.contains(permutations[i].intern()));
        }
    } 
    
    private static String lastDir = null;
    private File getTempDir() {
        String outdir = System.getProperty("java.io.tmpdir"); //NOI18N
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }
        String dirname = Long.toHexString(System.currentTimeMillis());
        lastDir = outdir + dirname + File.separator;
        File dir = new File (outdir + dirname);
        try {
            dir.mkdir();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            fail ("Exception creating temporary dir for tests " + dirname + " - " + ioe.getMessage());
        }
        dir.deleteOnExit();
        return dir;
    }
    
    private static Repository repository = null;
    private FileSystem createTestingFilesystem () {
        try {
            LocalFileSystem result = new LocalFileSystem();
            result.setRootDirectory(getTempDir());
            repository = new Repository (result);
            System.setProperty ("org.openide.util.Lookup", "org.netbeans.core.ShortcutsFolderTest$LKP");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        return null;
    }
    
    private FileObject getFolderForShortcuts(FileSystem fs) {
        FileObject result = null;
        try {
            result = fs.getRoot().getFileObject("Shortcuts");
            if (result == null) {
                result = fs.getRoot().createFolder("Shortcuts");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return result;
    }
    
    private ShortcutsFolder createShortcutsFolder(FileSystem fs) {
        try {
            DataObject dob = DataObject.find(getFolderForShortcuts(fs));
            ShortcutsFolder result = new ShortcutsFolder ((DataFolder)dob);
            ShortcutsFolder.shortcutsFolder = result;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Exception creating shortcuts folder on " + fs);
            return null;
        }
    }
    
    public void testShortcutsFolderDeletesShortcutWhenNameIsMisordered() throws Exception {
        FileSystem fs = createTestingFilesystem();
        FileObject fo = getFolderForShortcuts(fs);
        
        FileObject data1 = fo.createData("AD-F5.instance");
        assertNotNull(data1);
        data1.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");
        
        FileObject data2 = fo.createData("AS-F5.instance");
        assertNotNull(data2);
        data2.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");
        
        File file = new File (lastDir + "Shortcuts" + File.separator + "AD-F5.instance");
        assertTrue ("Actual file not created: " + file.getPath(), file.exists());
        
        
        ShortcutsFolder sf = createShortcutsFolder(fs);
        System.err.println("Created shortcuts folder " + sf);
        
        sf.waitShortcutsFinished();
        
        DataObject ob = DataObject.find (data1);
        assertNotNull("Data object not found: " + data1.getPath(), ob);
        System.err.println("Found data object " + data1);
        
        InstanceCookie ck = (InstanceCookie) ob.getCookie(InstanceCookie.class);
        Object obj = ck.instanceCreate();
        
        assertTrue ("InstanceCookie was not an instanceof TestAction - " + obj, obj instanceof TestAction);
        
        System.err.println("Got an instance: " + obj);
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        Action action = (Action) obj;
        
        ShortcutsFolder.applyChanges(Arrays.asList(new Object[] {new ShortcutsFolder.ChangeRequest (stroke, action, false)}));
        
        ShortcutsFolder.refreshGlobalMap();

        FileObject now = fo.getFileObject ("AD-F5.instance");
        assertNull ("File object should be deleted - ", now);
        
        assertFalse ("File still exists: " + lastDir + "AD-F5.instance", file.exists());
        
        file = new File (lastDir + "Shortcuts" + File.separator + "AS-F5.instance");
        assertTrue ("File should not have been deleted: " + file.getPath(), file.exists());
        
    }
    
    public static class TestAction extends AbstractAction {
        public void actionPerformed (ActionEvent ae) {}
    }
    
    public static class LKP extends Lookup {
        private javax.swing.text.Keymap keymap = new NbKeymap ();
        
        public Object lookup (Class clazz) {
            if (Repository.class == clazz) {
                return repository;
            }
            if (ErrorManager.class == clazz) {
                return new EM();
            }
            if (javax.swing.text.Keymap.class == clazz) {
                return keymap;
            }
            return null;
        }
        
        public Lookup.Result lookup (final Lookup.Template tpl) {
            Lookup.Result r = new Lookup.Result() {
                public Collection allInstances() {
                    if (tpl.getType() == ErrorManager.class) {
                        return Arrays.asList(new Object[] { new EM()});
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                }
                
                public  void addLookupListener (LookupListener l) {}

                public  void removeLookupListener (LookupListener l){}                
            };
            
            return r;
        }
    }
    
    public static class EM extends ErrorManager {
        public Throwable attachAnnotations (Throwable t, Annotation[] arr) {
            return t;
        }

        public void log (int i, String s) {
            System.err.println(s);
        }
        
        public Annotation[] findAnnotations (Throwable t) {
            return new Annotation[0];
        }

        public Throwable annotate (
            Throwable t, int severity,
            String message, String localizedMessage,
            Throwable stackTrace, java.util.Date date
        ) {
            System.err.println(message);
            t.printStackTrace();
            return t;
        }

        public void notify (int severity, Throwable t) {
            t.printStackTrace();
        }

        public ErrorManager getInstance(String name) {
            return this;
        }
    }
    
}
