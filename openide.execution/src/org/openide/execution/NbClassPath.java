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
        Thread.dumpStack();
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
        Thread.dumpStack();
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
        Thread.dumpStack();
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
        Thread.dumpStack();
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
     * @deprecated Use the Java Platform API instead.
    */
    public static NbClassPath createBootClassPath () {
        Thread.dumpStack();
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
    * @param fo file object to convert
    * @return disk file for that file object, or <code>null</code> if there is no corresponding disk file
    * @deprecated You should use {@link org.openide.filesystems.FileUtil#toFile} instead.
    */
    public static File toFile (FileObject fo) {
        Thread.dumpStack();
        return FileUtil.toFile(fo);
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
