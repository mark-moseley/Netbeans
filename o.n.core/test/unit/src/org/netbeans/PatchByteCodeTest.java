/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.io.InputStream;
import java.lang.reflect.*;

/** Test patching of openide.jar byte code for compatibility.
 * @author Jaroslav Tulach
 */
public class PatchByteCodeTest extends NbTestCase {
    
    public PatchByteCodeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PatchByteCodeTest.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testBeanTreeViewLoad () throws Exception {
        checkPatching (
            "org.openide.explorer.view.BeanTreeView", 
            "data/BeanTreeView.clazz",
            null
        );
    }

    public void testCompilerGroupLoad () throws Exception {
        checkPatching (
            "org.openide.compiler.CompilerGroup", 
            "data/CompilerGroup.clazz",
            null
        );
        
        InputStream is = getClass ().getResourceAsStream ("data/CompilerGroup.clazz");
        assertNotNull ("Class has not been found", is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);
        
        
        byte[] res = PatchByteCode.enhance(arr, null, new String[] { "addCompilerListener", "removeCompilerListener" } );
        PatchClassLoader loader = new PatchClassLoader ("org.openide.compiler.CompilerGroup", res, ClassLoader.getSystemClassLoader());
        
        Class c = loader.loadClass ("org.openide.compiler.CompilerGroup");
        
        Method m = c.getDeclaredMethod("addCompilerListener", new Class[] { org.openide.compiler.CompilerListener.class });
        assertTrue ("Is not final", !Modifier.isFinal (m.getModifiers ()));
        
        m = c.getDeclaredMethod("removeCompilerListener", new Class[] { org.openide.compiler.CompilerListener.class });
        assertTrue ("Is not final", !Modifier.isFinal (m.getModifiers ()));
    }
    
    public void testClassCanBeAlsoInstantiated () throws Exception {
        Class c = checkPatching (
            Sample.class.getName (),
            "Sample.class",
            "java.lang.Throwable"
        );
        
        c.newInstance ();
    }
    
    public void testPatchingOfFieldsAndMethodsToPublicAndNonFinal () throws Exception {
        InputStream is = getClass ().getResourceAsStream ("Sample.class");
        assertNotNull ("Class has not been found", is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);

        
        
        byte[] res = PatchByteCode.enhance(arr, null, new String[] { "member", "field", "method", "staticmethod" } );
        PatchClassLoader loader = new PatchClassLoader (Sample.class.getName (), res, ClassLoader.getSystemClassLoader());
        
        Class c = loader.loadClass (Sample.class.getName ());

        assertTrue ("Class should be public", Modifier.isPublic (c.getModifiers()));

        Method m = c.getDeclaredMethod("method", new Class[0]);
        assertNotNull ("Mehtod method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", !Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is not synchronzied", !Modifier.isSynchronized(m.getModifiers()));
        
        m = c.getDeclaredMethod("member", new Class[] { Object.class });
        assertNotNull ("Member method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));        
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", !Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is synchronzied", Modifier.isSynchronized(m.getModifiers()));
        
        m = c.getDeclaredMethod("staticmethod", new Class[] { });
        assertNotNull ("Member method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));        
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is not synchronzied", !Modifier.isSynchronized(m.getModifiers()));
        
        java.lang.reflect.Field f;
        
        f = c.getDeclaredField("member");
        assertNotNull ("Really exists", f);
        assertTrue ("Is public", Modifier.isPublic (f.getModifiers ()));
        assertTrue ("Is not final", !Modifier.isFinal (f.getModifiers ()));
        assertTrue ("Is static", Modifier.isStatic (f.getModifiers ()));
        
        f = c.getDeclaredField("field");
        assertNotNull ("Really exists", f);
        assertTrue ("Is public", Modifier.isPublic (f.getModifiers ()));
        assertTrue ("Is not final", !Modifier.isFinal (f.getModifiers ()));
        assertTrue ("Is static", !Modifier.isStatic (f.getModifiers ()));
        
    }
    
        
    private Class checkPatching (
        String className, String resource, String superclass
    ) throws Exception {
        if (superclass == null) {
            superclass = PatchByteCodeTest.class.getName ();
        }
        
        InputStream is = getClass ().getResourceAsStream (resource);
        assertNotNull ("Resource has been found " + resource, is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);
        
        byte[] res = PatchByteCode.enhance(arr, superclass.replace ('.', '/'));
        PatchClassLoader loader = new PatchClassLoader (className, res);

        Class c = loader.loadClass (className);
        
        assertEquals (
            "Superclass changed appropriately", 
            superclass,
            c.getSuperclass().getName ()
        );
        
        return c;
    }        
    
    
    private static final class PatchClassLoader extends ClassLoader {
        private String res;
        private byte[] arr;
        
        public PatchClassLoader (String res, byte[] arr) {
            this (res, arr, PatchClassLoader.class.getClassLoader ());
        }
        
        public PatchClassLoader (String res, byte[] arr, ClassLoader c) {
            super (c);
            
            this.res = res;
            this.arr = arr;
        }
        
        protected synchronized Class loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {
            if (res.equals (name)) {
                byte[] patch = PatchByteCode.patch(arr);
                
                return defineClass (name, patch, 0, patch.length);
            } else {
                return super.loadClass (name, resolve);
            }
        }
    }
}
