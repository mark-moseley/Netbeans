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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
// IMPORTANT! You may need to mount ant.jar before this class will
// compile. So mount the JAR modules/ext/ant-1.4.1.jar (NOT modules/ant.jar)
// from your IDE installation directory in your Filesystems before
// continuing to ensure that it is in your classpath.

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Jaroslav Tulach
 */
public class NbEnhanceClass extends Task {

    /* Path to library containing the patch method */
    private Path patchPath;
    public Path createClasspath() {
        if (patchPath == null) {
            patchPath = new Path(getProject());
        }
        return patchPath.createPath();
    }
    
    /* Name of class with patch method */
    private String patchClass = "org.netbeans.PatchByteCode";
    public void setPatchClass (String f) {
        patchClass = f;
    }
    
    /** Name of the method to call. Must have signature byte[] x(byte[], Map)
     */
    private String enhanceMethod = "enhanceClass";
    public void setEnhanceMethod (String f) {
        enhanceMethod = f;
    }
    
    /* Base dir to find classes relative to */
    private File basedir;
    public void setBasedir (File f) {
        basedir = f;
    }
    
    /* The class to change its super class and the value of the super class.
     */
    public static class Patch {
        String clazz;
        String nbSuperClass;
        ArrayList members;
        
        
        /** Class in form of java/lang/Object */
        public void setClass (String s) {
            clazz = s;
        }
        /** Class in form of java/lang/Object */
        public void setSuper (String s) {
            nbSuperClass = s;
        }
        
        public Object createMember () {
            Member m = new Member();
            if (members == null) {
                members = new ArrayList ();
            }
            members.add (m);
            return m;
        }
        
        public static final class Member extends Object {
            String name;
            String rename;
            
            public void setName (String s) {
                name = s;
            }
            
            public void setRename (String s) {
                rename = s;
            }
        }
    }
    private ArrayList patches = new ArrayList (); // List<Nestme>
    public Patch createPatch () {
        Patch n = new Patch ();
        patches.add(n);
        return n;
    }
    
    
    public void execute() throws BuildException {
        if (basedir == null) {
            throw new BuildException ("Attribute basedir must be specified");
        }
        
        if (patches.isEmpty()) {
            // no work
            return;
        }
        
        //
        // Initialize the method
        //
        
        ClassLoader cl = new AntClassLoader(getProject(), patchPath, false);
        
        java.lang.reflect.Method m;
        try {
            Class c = cl.loadClass (patchClass);
            m = c.getMethod(enhanceMethod, new Class[] { byte[].class, java.util.Map.class });
            if (m.getReturnType() != byte[].class) {
                throw new BuildException ("Method does not return byte[]: " + m);
            }
        } catch (Exception ex) {
            throw new BuildException ("Cannot initialize class " + patchClass + " and method " + enhanceMethod, ex);
        }
            
        /*
        try {
            log ("Testing method " + m);
            byte[] res = (byte[])m.invoke (null, new Object[] { new byte[0], "someString" });
        } catch (Exception ex) {
            throw new BuildException ("Exception during test invocation of the method", ex);
        }
         */
            
        //
        // Ok we have the method and we can do the patching
        //
        
        Iterator it = patches.iterator();
        while (it.hasNext()) {
            Patch p = (Patch)it.next ();
            
            if (p.clazz == null) {
                throw new BuildException ("Attribute class must be specified");
            }
            
            File f = new File (basedir, p.clazz + ".class");
            if (!f.exists ()) {
                throw new BuildException ("File " + f + " for class " + p.clazz + " does not exists");
            }
            
            byte[] arr = new byte[(int)f.length()];
            try {
                FileInputStream is = new FileInputStream (f);
                if (arr.length != is.read (arr)) {
                    throw new BuildException ("Not all bytes read");
                }
                is.close ();
            } catch (IOException ex) {
                throw new BuildException ("Cannot read file " + f, ex);
            }
            
            ArrayList members = null;
            ArrayList rename = null;
            if (p.members != null) {
                members = new ArrayList ();
                Iterator myIt = p.members.iterator();
                int i = 0;
                while (myIt.hasNext ()) {
                    Patch.Member mem = (Patch.Member)myIt.next ();
                    members.add (mem.name);
                    
                    if (mem.rename != null) {
                        if (rename == null) {
                            rename = new ArrayList ();
                        }
                        rename.add (mem.name);
                        rename.add (mem.rename);
                    }
                }
            }
                
             
            byte[] out;
            try {
                java.util.Map args = new java.util.HashMap ();
                if (p.nbSuperClass != null) {
                    args.put ("netbeans.superclass", p.nbSuperClass);
                }
                if (members != null) {
                    args.put ("netbeans.public", members);
                }
                if (rename != null) {
                    args.put ("netbeans.rename", rename);
                }
                
                log("Patching " + p.clazz + " with arguments " + args, Project.MSG_VERBOSE);
                
                out = (byte[])m.invoke (null, new Object[] { arr, args });
                if (out == null) {
                    // no patching needed
                    continue;
                }
            } catch (Exception ex) {
                throw new BuildException (ex);
            }

            if (p.nbSuperClass != null) {
                log ("Enhanced " + f + " to have alternate superclass " + p.nbSuperClass + " and be public");
            } else {
                log ("Enhanced " + f + " to be public");
            }
            
            try {
                FileOutputStream os = new FileOutputStream (f);
                os.write (out);
                os.close ();
            } catch (IOException ex) {
                throw new BuildException ("Cannot overwrite file " + f, ex);
            }
        }
    }
    
}
