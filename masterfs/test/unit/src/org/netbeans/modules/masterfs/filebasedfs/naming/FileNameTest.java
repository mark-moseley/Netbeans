/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radek Matous
 */
public class FileNameTest extends NbTestCase {
    private File f1;    
    private File f2;
    private File f3;
    private FileNaming n1;
    private FileNaming n2;
    private FileNaming n3;    
    
    public FileNameTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        f1 = getTestFile();
        f2 = new File (f1.getAbsolutePath());
        f3 = f1.getParentFile();
        n1 = NamingFactory.fromFile(f1);
        n2 = NamingFactory.fromFile(f2);
        n3 = NamingFactory.fromFile(f3);        
    }

    protected File getTestFile() throws Exception {
        File retVal = new File (getWorkDir(), "namingTest");
        if (!retVal.exists()) {
            retVal.createNewFile();
        }
        return retVal;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        n1 = null;
        n2 = null;
        n3 = null;
    }

    public void test69450() throws Exception {
        File fA = new File(getWorkDir(),"A");
        if (fA.exists()) {
            assertTrue(fA.delete());
        }        
        File fa = new File(getWorkDir(),"a");
        if (!fa.exists()) {
            assertTrue(fa.createNewFile());
        }        
        boolean isCaseSensitive = !fa.equals(fA);                
        FileNaming na = NamingFactory.fromFile(fa);        
        assertEquals(fa.getName(),NamingFactory.fromFile(fa).getName());        
        if (isCaseSensitive) {
            assertFalse(!fa.getName().equals(NamingFactory.fromFile(fA).getName()));
            assertFalse(!NamingFactory.fromFile(fa).equals(NamingFactory.fromFile(fA)));
            assertNotSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertFalse(na.equals(NamingFactory.fromFile(fA)));
            assertFalse(fA.getName().equals(na.getName()));            
        } else {
            assertSame(na,NamingFactory.fromFile(fA));            
            assertEquals(fa.getName(),na.getName());            
            assertEquals(fa.getName(),NamingFactory.fromFile(fA).getName());
            assertEquals(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));
            assertSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            //#69450            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertEquals(na,NamingFactory.fromFile(fA));
            assertSame(na, NamingFactory.fromFile(fA));
            assertFalse(fA.getName() + " / " + na.getName(),fA.getName().equals(na.getName()));
            NamingFactory.checkCaseSensitivity(na,fA);
            assertTrue(fA.getName() + " / " + na.getName(),fA.getName().equals(na.getName()));
            
        }
    }
    
    /**
     * Test of equals method, of class org.netbeans.modules.masterfs.pathtree.PathItem.
     */
   public void testEquals () throws Exception {
        assertEquals(n1, n2);
        assertSame(n1, n2);        
        assertNotSame(n3, n1);
        assertNotSame(n3, n2);
        assertEquals(n3, n1.getParent());
        assertEquals(n3, n2.getParent());
        assertSame(n3, n1.getParent());
        assertSame(n3, n2.getParent());                
    }    

    public void testHashcode () throws Exception {
        assertEquals(n3.hashCode(), n1.getParent().hashCode());                
        assertEquals(n3.hashCode(), n2.getParent().hashCode());                                
    }
    
    public void testWeakReferenced () throws Exception {
        List l = new ArrayList ();
        FileNaming current = n1;
        while (current != null) {
            l.add(new WeakReference (current));
            current = current.getParent();
        }
        
        current = null;        
        n1 = null;
        n2 = null;
        n3 = null;
        
        for (int i = 0; i < l.size(); i++) {
            WeakReference weakReference = (WeakReference) l.get(i);
            assertGC("Shoul be GCed: "+((FileNaming)weakReference.get()),  weakReference);
        }        
    }
    
    public void testFileConversion () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        File[] files = new File [] {f1, f2, f3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            File currentFile = files[i];            
            
            while (current != null) {
                assertEquals (current.getFile(), currentFile);
                current = current.getParent();
                currentFile = currentFile.getParentFile();
            }            
        }        
    }

    public void testFileExist () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            while (current != null) {
                File file = current.getFile();
                assertTrue(file.getAbsolutePath(), file.exists());
                current = current.getParent();
            }            
        }        
    }


    /**
     * Test of rename method, of class org.netbeans.modules.masterfs.naming.PathItem.
     */
    public void testRename() throws Exception {
        File f = f1;
        assertTrue(f.exists());
        FileNaming pi = NamingFactory.fromFile(f);
        assertTrue(pi.rename("renamed3"));
        File f2 = pi.getFile();
        assertFalse(f.exists());
        assertTrue(f2.exists());
        assertFalse(f2.equals(f));
        assertTrue (f2.getName().equals("renamed3"));        
    }
    
}
