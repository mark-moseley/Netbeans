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

package org.openide.execution;

import java.io.File;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.util.Lookup;

/** Property that can hold informations about class path and
* that can be used to create string representation of the 
* class path.
*/
public final class NbClassPath extends Object implements java.io.Serializable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8458093409814321744L;

    /** Fuj: This is the most overloaded variable in this class.
    * It can hold Object[] with elements of String or Exception
    * or later Exception[] array.
    *
    * Also the array can hold File[] array.
    */
    private Object[] items;
    /** the prepared classpath */
    private String classpath;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (String[] classpathItems) {
        this.items = classpathItems;
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param classpathItems  the classpath to be passed to the process 
    */
    public NbClassPath (File[] classpathItems) {
        this.items = classpathItems;
    }

    /** Private constructor
    * @param arr array of String and Exceptions
    */
    private NbClassPath (Object[] arr) {
        this.items = arr;
    }

    /** Create a class path from the usual string representation.
    * @param path a class path separated by {@link File#pathSeparatorChar}s
    */
    public NbClassPath (String path) {
        this.items = new Exception[0];
        this.classpath = path;
        // [PENDING] what is this here for? *Users* of the classpath should quote it as needed to
        // pass thru shells, according to the type of shell. Right?
        if (path.indexOf(' ') >= 0) {
            if (path.startsWith("\"")) { // NOI18N
                return;
            } else {
                StringBuffer buff = new StringBuffer(path);
                buff.insert(0, '"');
                buff.append('"');
                classpath = buff.toString();
            }
        }
    }

    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @return class path for all reachable systems in the repository
    * @deprecated Please use the <a href="@JAVA/API@/org/netbeans/api/java/classpath/api.html">ClassPath API</a> instead.
    */
    public static NbClassPath createRepositoryPath () {
        return createRepositoryPath (FileSystemCapability.ALL);
    }

    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @param cap the capability that must be satisfied by the file system
    *    added to the class path
    * @return class path for all reachable systems in the repository
    * @deprecated Please use the <a href="@JAVA/API@/org/netbeans/api/java/classpath/api.html">ClassPath API</a> instead.
    */
    public static NbClassPath createRepositoryPath (FileSystemCapability cap) {
        final LinkedList res = new LinkedList ();


        final class Env extends FileSystem.Environment {
            /* method of interface Environment */
            public void addClassPath(String element) {
                res.add (element);
            }
        }


        Env env = new Env ();
        Enumeration en = cap.fileSystems ();
        while (en.hasMoreElements ()) {
            try {
                FileSystem fs = (FileSystem)en.nextElement ();
                fs.prepareEnvironment(env);
            } catch (EnvironmentNotSupportedException ex) {
                // store the exception
                res.add (ex);
            }
        }

        // return it
        return new NbClassPath (res.toArray ());
    }

    /** Creates class path describing additional libraries needed by the system.
     * Never use this class path as part of a user project!
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    public static NbClassPath createLibraryPath () {
        // modules & libs
        ExecutionEngine ee = (ExecutionEngine)Lookup.getDefault().lookup(ExecutionEngine.class);
        if (ee != null) {
            return ee.createLibraryPath();
        } else {
            return new NbClassPath(new File[0]);
        }
    }

    /** Creates class path of the system.
     * Never use this class path as part of a user project!
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    public static NbClassPath createClassPath () {
        // ${java.class.path} minus openide-compat.jar
        String cp = System.getProperty ("java.class.path"); // NOI18N
        if (cp == null || cp.length () == 0) return new NbClassPath (""); // NOI18N
        StringBuffer buf = new StringBuffer (cp.length ());
        StringTokenizer tok = new StringTokenizer (cp, File.pathSeparator);
        boolean appended = false;
        while (tok.hasMoreTokens ()) {
            String piece = tok.nextToken ();
            if (piece.endsWith ("openide-compat.jar")) continue; // NOI18N
            if (appended) {
                buf.append (File.pathSeparatorChar);
            } else {
                appended = true;
            }
            buf.append (piece);
        }
        return new NbClassPath (buf.toString ());
    }

    /** Creates path describing boot class path of the system.
     * Never use this class path as part of a user project!
     * There are generally no excuses to be using this method as part of a normal module.
     * For more information consult the <a href="../doc-files/classpath.html">Module Class Path</a> document.
    * @return class path of system class including extensions
    */
    public static NbClassPath createBootClassPath () {
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        StringBuffer sb = (boot != null ? new StringBuffer(boot) : new StringBuffer());

        // std extensions
        String extensions = System.getProperty("java.ext.dirs"); // NOI18N
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        String name = entries[i].getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            if (sb.length() > 0) {
                                sb.append(File.pathSeparatorChar);
                            }
                            sb.append(entries[i].getPath());
                        }
                    }
                }
            }
        }

        return new NbClassPath (sb.toString());
    }

    /** Take one file object and try to convert it into a local file.
    * The conversion can succeed only if the file object's file system
    * supports work with {@link org.openide.filesystems.FileSystem.Environment}.
    *
    * @param fo file object to convert
    * @return disk file for that file object, or <code>null</code> if there is no corresponding disk file
    * @deprecated You probably should use {@link org.openide.filesystems.FileUtil#toFile} instead.
    */
    public static File toFile (FileObject fo) {
        final String pne = fo.getPath().replace('/', File.separatorChar);

        final class Env extends FileSystem.Environment {
            /** the file found or null */
            public File found;
            /** the file suggested or null */
            //public File suggest;

            /* method of interface Environment */
            public void addClassPath(String element) {
                if (found != null) {
                    // file found, ignore the rest
                    return;
                }

                File p = new File (element);
                if (! p.isDirectory ()) {
                    // JAR entry, for example:
                    return;
                }
                File f = new File (p, pne);

                /* #8928: ought not return nonexistent file
                if (suggest == null) {
                    suggest = f;
                }
                */

                if (f.exists ()) {
                    found = f;
                }
            }
        }

        Env env = new Env ();
        try {
            fo.getFileSystem ().prepareEnvironment(env);
            return /*env.found == null ? env.suggest : */env.found;
        } catch (java.io.IOException ex) {
            return null;
        }
    }

    /** If there were some problems during creation of the class path, they can be identified
    * by asking the method. So this method can be called to test whether it is correct to
    * use the path or there can be some errors.
    * <P>
    * This can happen especially when creating NbClassPath for filesystems in repository and
    * they are not stored on locally accessible disks.
    *
    * @return array of exceptions thrown during creation of the path
    */ 
    public Exception[] getExceptions () {
        try {
            return (Exception[])items;
        } catch (ClassCastException ex) {
            // we have to convert the array first
        }

        synchronized (this) {
            // creates class path
            getClassPath ();

            int first = 0;
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    // should be exception
                    items[first++] = items[i];
                }
            }

            Exception[] list = new Exception[first];
            System.arraycopy (items, 0, list, 0, first);
            items = list;
            return list;
        }
    }



    /** Create class path representation. The implementation <i>will return the string quoted 
     *  (using doublequotes)</i>, if it contains a space character.
    * @return string representing the classpath items separated by File.separatorChar, possibly quoted.
    */
    public String getClassPath () {
        if (classpath != null) return classpath;
        synchronized (this) {
            if (classpath != null) return classpath;

            if (items.length == 0) {
                return classpath = ""; // NOI18N
            } else {
                StringBuffer sb = new StringBuffer ();
                boolean haveone = false;
                for (int i = 0; i < items.length; i++) {
                    Object o = items[i];
                    if (o == null || (! (o instanceof String) && ! (o instanceof File))) {
                        // we accept only strings/files
                        continue;
                    }

                    if (haveone) {
                        sb.append (File.pathSeparatorChar);
                    } else {
                        haveone = true;
                    }
                    sb.append (o.toString ());
                    items[i] = null;
                }
                String clsPth;
                if ((clsPth = sb.toString()).indexOf(' ') >= 0) {
                    sb.insert(0, '"');
                    sb.append('"');
                    classpath = sb.toString();
                } else {
                    classpath = clsPth;
                }
                return classpath;
            }
        }
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbClassPath)) return false;
        NbClassPath him = (NbClassPath) o;
        return getClassPath ().equals (him.getClassPath ());
    }
}
