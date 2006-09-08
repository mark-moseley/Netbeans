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

package org.openide.filesystems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.openide.util.io.NbMarshalledObject;

/**
 * Holder for system filesystem, used for most of NetBeans' runtime configuration.
 * There is only one useful thing to do with this class:
 * <pre>
 * FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
 * // now use somehow, e.g.
 * FileObject menus = sfs.findResource("Menu");
 * // ...
 * </pre>
 * Formerly (NB 3.x) contained a list of mounted filesystems. This functionality
 * is no longer used and is now deprecated.
 */
public class Repository implements Serializable {
    static final long serialVersionUID = -6344768369160069704L;

    /** list of filesystems (FileSystem) */
    private ArrayList<FileSystem> fileSystems;
    private transient ArrayList<FileSystem> fileSystemsClone;

    /** the system filesystem */
    private FileSystem system;

    /** hashtable that maps system names to FileSystems */
    private Hashtable<String, FileSystem> names;
    private transient FCLSupport fclSupport;

    // [PENDING] access to this hashtable is apparently not propertly synched
    // should use e.g. Collections.synchronizedSet, or just synch methods using it

    /** hashtable for listeners on changes in the filesystem.
    * Its elements are of type (RepositoryListener, RepositoryListener)
    */
    private Hashtable<RepositoryListener,RepositoryListener> listeners =
            new Hashtable<RepositoryListener,RepositoryListener>();

    /** vetoable listener on systemName property of filesystem */
    private VetoableChangeListener vetoListener = new VetoableChangeListener() {
            /** @param ev event with changes */
            public void vetoableChange(PropertyChangeEvent ev)
            throws PropertyVetoException {
                if (ev.getPropertyName().equals("systemName")) {
                    final String ov = (String) ev.getOldValue();
                    final String nv = (String) ev.getNewValue();

                    if (names.get(nv) != null) {
                        throw new PropertyVetoException("system name already exists: " + ov + " -> " + nv, ev); // NOI18N
                    }
                }
            }
        };

    /** property listener on systemName property of filesystem */
    private PropertyChangeListener propListener = new PropertyChangeListener() {
            /** @param ev event with changes */
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getPropertyName().equals("systemName")) {
                    // assign the property to new name
                    String ov = (String) ev.getOldValue();
                    String nv = (String) ev.getNewValue();
                    FileSystem fs = (FileSystem) ev.getSource();

                    if (fs.isValid()) {
                        // when a filesystem is valid then it is attached to a name
                        names.remove(ov);
                    }

                    // register name of the filesystem
                    names.put(nv, fs);

                    // the filesystem becomes valid
                    fs.setValid(true);
                }
            }
        };

    /** Creates new instance of filesystem pool and
    * registers it as the default one. Also registers the default filesystem.
    *
    * @param def the default filesystem
    */
    public Repository(FileSystem def) {
        this.system = def;
        init();
    }

    /** Access method to get default instance of repository in the system.
     * The instance is either taken as a result of
     * <CODE>org.openide.util.Lookup.getDefault ().lookup (Repository.class)</CODE>
     * or (if the lookup query returns null) a default instance is created.
     *
     * @return default repository for the system
     */
    public static Repository getDefault() {
        return ExternalUtil.getRepository();
    }

    /** Initialazes the pool.
    */
    private void init() {
        // empties the pool
        fileSystems = new ArrayList<FileSystem>();
        names = new Hashtable<String, FileSystem>();
        addFileSystem(system);
    }

    /**
     * Gets the NetBeans default (system, configuration) filesystem.
     * @return the default filesystem
     */
    public final FileSystem getDefaultFileSystem() {
        return system;
    }

    /** Adds new filesystem to the pool.
    * <em>Note</em> that a filesystem cannot be assigned to more than one file
    *   system pool at one time (though currently there is only one pool anyway).
    * At any given time, no two filesystems in the pool may share the same {@link FileSystem#getSystemName name}
    * (unless all but one are {@link FileSystem#isValid invalid}). To be sure, that
    * filesystem was really added in Repository, then test that <code>FileSystem</code>
    * is valid.
    * @param fs filesystem to add
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void addFileSystem(FileSystem fs) {

        boolean fireIt = false;

        synchronized (this) {
            // if the filesystem is not assigned yet
            if (!fs.assigned && !fileSystems.contains(fs)) {
                // new filesystem
                fs.setRepository(this);
                fileSystems.add(fs);
                fileSystemsClone = new ArrayList<FileSystem>(fileSystems);

                String systemName = fs.getSystemName();

                boolean isReg = names.get(systemName) == null;

                if (isReg && !systemName.equals("")) { // NOI18N

                    // filesystem with the same name is not there => then it is valid
                    names.put(systemName, fs);
                    fs.setValid(true);
                } else {
                    // there is another filesystem with the same name => it is invalid
                    fs.setValid(false);
                }

                // mark the filesystem as being assigned
                fs.assigned = true;

                // mark as a listener on changes in the filesystem
                fs.addPropertyChangeListener(propListener);
                fs.addVetoableChangeListener(vetoListener);

                // notify filesystem itself that it has been added
                fs.addNotify();

                // fire info about new filesystem
                fireIt = true;
            }
        }

        // postponed firing after synchronized  block to prevent deadlock
        if (fireIt) {
            fireFileSystem(fs, true);
        }
    }

    /** Removes a filesystem from the pool.
    * @param fs filesystem to remove
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void removeFileSystem(FileSystem fs) {
        boolean fireIt = false;

        synchronized (this) {
            if (fs.isDefault()) {
                return;
            }

            if (fireIt = fileSystems.remove(fs)) {
                fs.setRepository(null);
                fileSystemsClone = new ArrayList<FileSystem>(fileSystems);

                // the filesystem realy was here
                if (fs.isValid()) {
                    // if the filesystem is valid then is in names hashtable
                    names.remove(fs.getSystemName());
                    fs.setValid(false);
                }

                // in all cases remove it from listeners
                fs.removePropertyChangeListener(propListener);
                fs.removeVetoableChangeListener(vetoListener);

                // notify filesystem itself that it has been removed
                fs.removeNotify();
            }

            // unassign the filesystem
            fs.assigned = false;
        }

        // postponed firing after synchronized  block to prevent deadlock
        if (fireIt) {
            fireFileSystem(fs, false);
        }
    }

    /** Reorders {@link FileSystem}s by given permutation.
     * For example, if there are three filesystems, <code>new int[] {2, 0, 1}</code> cycles the filesystems forwards.
    * @param perm an array of integers
    * @throws IllegalArgumentException if the array is not a permutation, or is not the same length as the current number of filesystems in the pool
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void reorder(int[] perm) {
        synchronized (this) {
            if (perm == null) {
                throw new IllegalArgumentException("null permutation"); // NOI18N
            } else if (perm.length != fileSystems.size()) {
                throw new IllegalArgumentException(
                    "permutation is wrong size: " + perm.length + " elements but should be " + fileSystems.size()
                ); // NOI18N
            } else if (!isPermutation(perm)) {
                StringBuffer message = new StringBuffer("permutation is not really a permutation:"); // NOI18N

                for (int i = 0; i < perm.length; i++) {
                    message.append(' ');
                    message.append(perm[i]);
                }

                throw new IllegalArgumentException(message.toString());
            }

            ArrayList<FileSystem> newList = new ArrayList<FileSystem>(fileSystems.size());
            int len = perm.length;

            for (int i = 0; i < len; i++) {
                newList.add(fileSystems.get(perm[i]));
            }

            fileSystems = newList;
            fileSystemsClone = new ArrayList<FileSystem>(fileSystems);
        }

        fireFileSystemReordered(perm);
    }

    /** @return true if the parameter describes a permutation */
    private static boolean isPermutation(int[] perm) {
        final int len = perm.length;
        boolean[] bool = new boolean[len];

        try {
            for (int i = 0; i < len; i++) {
                if (bool[perm[i]]) {
                    return false;
                } else {
                    bool[perm[i]] = true;
                }
            }

            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /** Returns enumeration of all filesystems.
    * @return enumeration of type {@link FileSystem}
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileSystem> getFileSystems() {
        ArrayList<FileSystem> tempFileSystems = fileSystemsClone;

        return java.util.Collections.enumeration(tempFileSystems);
    }

    /** Returns enumeration of all filesystems.
    * @return enumeration of type {@link FileSystem}
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileSystem> fileSystems() {
        return getFileSystems();
    }

    /**
     * Returns a sorted array of filesystems.
     * @return a sorted array of filesystems
     * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
     */
    @Deprecated
    public final FileSystem[] toArray() {
        ArrayList<FileSystem> tempFileSystems = fileSystemsClone;

        FileSystem[] fss = new FileSystem[tempFileSystems.size()];
        tempFileSystems.toArray(fss);

        return fss;
    }

    /** Finds filesystem when only its system name is known.
    * @param systemName {@link FileSystem#getSystemName name} of the filesystem
    * @return the filesystem or <CODE>null</CODE> if there is no such
    *   filesystem
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileSystem findFileSystem(String systemName) {
        FileSystem fs = names.get(systemName);

        return fs;
    }

    /** Saves pool to stream by saving all filesystems.
    * The default (system) filesystem, or any persistent filesystems, are skipped.
    *
    * @param oos object output stream
    * @exception IOException if an error occures
    * @deprecated Unused.
    */
    @Deprecated
    public final synchronized void writeExternal(ObjectOutput oos)
    throws IOException {
        Iterator iter = fileSystems.iterator();

        while (iter.hasNext()) {
            FileSystem fs = (FileSystem) iter.next();

            if (!fs.isDefault() && !fs.isPersistent()) {
                oos.writeObject(new NbMarshalledObject(fs));
            }
        }

        oos.writeObject(null);
    }

    /** Reads object from stream.
    * Reads all filesystems. Persistent and system filesystems are untouched; all others are removed and possibly reread.
    * @param ois object input stream
    * @exception IOException if an error occures
    * @exception ClassNotFoundException if read class is not found
    * @deprecated Unused.
    */
    @Deprecated
    public final synchronized void readExternal(ObjectInput ois)
    throws IOException, ClassNotFoundException {
        ArrayList<FileSystem> temp = new ArrayList<FileSystem>(10);

        for (;;) {
            Object obj = ois.readObject();

            if (obj == null) {
                // all system has been read in
                break;
            }

            FileSystem fs;

            if (obj instanceof FileSystem) {
                fs = (FileSystem) obj;
            } else {
                try {
                    NbMarshalledObject mar = (NbMarshalledObject) obj;
                    fs = (FileSystem) mar.get();
                } catch (IOException ex) {
                    ExternalUtil.exception(ex);
                    fs = null;
                } catch (ClassNotFoundException ex) {
                    ExternalUtil.exception(ex);
                    fs = null;
                }
            }

            if (fs != null) {
                // add the new filesystem
                temp.add(fs);
            }
        }

        Enumeration<? extends FileSystem> ee = getFileSystems();
        FileSystem fs;

        while (ee.hasMoreElements()) {
            fs = ee.nextElement();

            if (!fs.isPersistent()) {
                removeFileSystem(fs);
            }
        }

        // in init assigned is checked and we force 'system' to be added again
        system.assigned = false;
        init();

        // all is successfuly read
        for (Iterator iter = temp.iterator(); iter.hasNext();)
            addFileSystem((FileSystem) iter.next());
    }

    /** Finds file when its name is provided. It scans in the list of
    * filesystems and asks them for the specified file by a call to
    * {@link FileSystem#find find}. The first object that is found is returned or <CODE>null</CODE>
    * if none of the filesystems contain such a file.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return {@link FileObject} that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileObject find(String aPackage, String name, String ext) {
        assert FileUtil.assertDeprecatedMethod();

        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Searches for the given resource among all filesystems.
    * @see FileSystem#findResource
    * @param name a name of the resource
    * @return file object or <code>null</code> if the resource can not be found
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final FileObject findResource(String name) {
        assert FileUtil.assertDeprecatedMethod();

        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(name);

            if (fo != null) {
                // object found
                return fo;
            }
        }

        return null;
    }

    /** Searches for the given resource among all filesystems, returning all matches.
    * @param name name of the resource
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileObject> findAllResources(String name) {
        assert FileUtil.assertDeprecatedMethod();

        Vector<FileObject> v = new Vector<FileObject>(8);
        Enumeration<? extends FileSystem> en = getFileSystems();

        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            FileObject fo = fs.findResource(name);

            if (fo != null) {
                v.addElement(fo);
            }
        }

        return v.elements();
    }

    /** Finds all files among all filesystems matching a given name, returning all matches.
    * All filesystems are queried with {@link FileSystem#find}.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return enumeration of {@link FileObject}s
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final Enumeration<? extends FileObject> findAll(String aPackage, String name, String ext) {
        assert FileUtil.assertDeprecatedMethod();

        Enumeration<? extends FileSystem> en = getFileSystems();
        Vector<FileObject> ret = new Vector<FileObject>();

        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem) en.nextElement();
            FileObject fo = fs.find(aPackage, name, ext);

            if (fo != null) {
                ret.addElement(fo);
            }
        }

        return ret.elements();
    }

    /** Fire info about changes in the filesystem pool.
    * @param fs filesystem
    * @param add <CODE>true</CODE> if the filesystem is added,
    *   <CODE>false</CODE> if it is removed
    */
    private void fireFileSystem(FileSystem fs, boolean add) {
        RepositoryEvent ev = new RepositoryEvent(this, fs, add);
        for (RepositoryListener list : new HashSet<RepositoryListener>(listeners.values())) {
            if (add) {
                list.fileSystemAdded(ev);
            } else {
                list.fileSystemRemoved(ev);
            }
        }
    }

    /** Fires info about reordering
    * @param perm
    */
    private void fireFileSystemReordered(int[] perm) {
        RepositoryReorderedEvent ev = new RepositoryReorderedEvent(this, perm);
        for (RepositoryListener list : new HashSet<RepositoryListener>(listeners.values())) {
            list.fileSystemPoolReordered(ev);
        }
    }

    /** Adds new listener.
    * @param list the listener
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void addRepositoryListener(RepositoryListener list) {
        listeners.put(list, list);
    }

    /** Removes listener.
    * @param list the listener
    * @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public final void removeRepositoryListener(RepositoryListener list) {
        listeners.remove(list);
    }

    /** Writes the object to the stream.
    */
    private Object writeReplace() {
        return new Replacer();
    }

    final FCLSupport getFCLSupport() {
        synchronized (FCLSupport.class) {
            if (fclSupport == null) {
                fclSupport = new FCLSupport();
            }
        }

        return fclSupport;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    * @since 2.8
    * @deprecated useless because there is no filesystem but only
    * default filesystem in Repository. Add new listener directly to
    * default filesystem {@link #getDefaultFileSystem}.
    */
    @Deprecated
    public final void addFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().addFileChangeListener(fcl);
    }

    /** Remove listener from this object.
    * @param fcl the listener
    * @since 2.8
    * @deprecated useless because there is no filesystem but only
    * default filesystem in Repository. Add new listener directly to
    * default filesystem {@link #getDefaultFileSystem}.
    */
    @Deprecated
    public final void removeFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().removeFileChangeListener(fcl);
    }

    private static class Replacer implements java.io.Serializable {
        /** serial version UID */
        static final long serialVersionUID = -3814531276726840241L;

        Replacer() {
        }

        private void writeObject(ObjectOutputStream oos)
        throws IOException {
            ExternalUtil.getRepository().writeExternal(oos);
        }

        private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ExternalUtil.getRepository().readExternal(ois);
        }

        /** @return the default pool */
        public Object readResolve() {
            return ExternalUtil.getRepository();
        }
    }
}
