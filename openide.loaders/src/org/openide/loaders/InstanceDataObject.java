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

package org.openide.loaders;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.ServiceType;
import org.openide.actions.DeleteAction;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** A data object whose only purpose is to supply <code>InstanceCookie</code>.
* The instances are created by default instantiation; the name of the class
* to instantiate is stored on disk, typically right in the file name.
* <p>This data object is generally used to configure menus and toolbars,
* though it could be used in any situation requiring instances to be present in
* a folder; for example, anything using {@link FolderInstance}.
* <p>Typical instance classes are subclasses of {@link SystemAction} to make
* menu items or toolbar buttons; {@link javax.swing.JSeparator} for a menu
* separator; or {@link javax.swing.JToolBar.Separator} for a toolbar
* separator.
* <p>Use {@link #create} and {@link #remove} to make the objects.
* Better yet, use an XML filesystem to install them declaratively.
* <p>
* Instance data object by default recognizes all files with <tt>.instance</tt>
* suffix. Such file can have associated optional file attributes:
* <dl>
* <!--  <dt><tt>instanceClass</tt> <dd><code>String</code> identifing class of created instance
*   (otherwise class name is derived from file name). -->
*   <dt><tt>instanceCreate</tt> <dd>instantionalized <code>Object</code> (e.g. created by
*     <tt>methodvalue</tt> at XML filesystem)
*   <dt><tt>instanceOf</tt> <dd><code>String</code> that is tokenized at ':', ',', ';' and
*   whitespace boundaries. Resulting tokens represent class names that created
*   instance is <code>instanceof</code>. Utilizing it may improve performance.
* </dl>
* (optional file attributes documented since 3.34).
*
* @author Ian Formanek
*/
public class InstanceDataObject extends MultiDataObject implements InstanceCookie.Of {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6134784731744777123L;

    private static final String EA_INSTANCE_CLASS = "instanceClass"; // NOI18N
    private static final String EA_INSTANCE_CREATE = "instanceCreate"; // NOI18N
    private static final String EA_INSTANCE_OF = "instanceOf"; // NOI18N
    /** data object name cached in the attribute to prevent instance creation when
     its InstanceNode is displayed. */
    static final String EA_NAME = "name"; // NOI18N
    /** if an instance is modified, what is the delay before it is saved? */
    private static final int SAVE_DELAY = 2000;

    // XXX #27494 Please changes to these two fields apply also into
    // core/naming/src/org/netbeans/core/naming/Utils class.
    /** opening symbol */
    private static final char OPEN = '[';
    /** closing symbol */
    private static final char CLOSE = ']';

    /** File extension for instance data objects. */
    public static final String INSTANCE = "instance"; // NOI18N

    /** File extension for serialized files. */
    static final String SER_EXT = "ser"; // NOI18N
    /** File extension for xml settings. */
    static final String XML_EXT = "settings"; //NOI18N

    /** optional property file key for icon resource */
    private static final String ICON_NAME = "icon"; // NOI18N

    /** the object that handles instance cookie manipulation */
    private Ser ser;

    /** saving task status */
    private boolean savingCanceled = false;


    private static final RequestProcessor PROCESSOR = new RequestProcessor ("Instance processor"); // NOI18N
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.openide.loaders.InstanceDataObject"); // NOI18N

    /** Create a new instance.
    * Do not use this to make instances; use {@link #create}.
    * @param pf primary file object for this data object
    * @param loader the loader
    * @throws DataObjectExistsException if it already exists
    */
    public InstanceDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super (pf, loader);

        if (pf.hasExt (SER_EXT)) { // NOI18N
            // only if we have ser extension the Ser (InstanceCookie)
            // should be accessible
            ser = new Ser (this);
            getCookieSet ().add (ser);
            // I.e. never create one.
        } else if (!pf.hasExt(XML_EXT)) {
            // otherwise we implement just the InstanceCookie directly
            ser = new Ser (this);
        }
        
        try {
            if (!pf.getFileSystem ().isDefault ()) {
                getCookieSet ().add (new DefaultES (this, getPrimaryEntry (), getCookieSet ()));
            }
        } catch (FileStateInvalidException ex) {
            // ok, ignore no editor support added
        }
    }

    /** used for synchronization instead of the IDO object */
    private final Object IDO_LOCK = new Object();
    /** Get the lock of the IDO object. Do not synchronize on the IDO object
     * directly to prevent deadlocks.
     */
    private Object getLock() {
        return IDO_LOCK;
    }

    /** Finds file object of specified name in a given folder.
    * @param folder the folder where search
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param className the name of the class the new object should provide an instance of
    * @return the found file object or null if it does not exist
    */
    private static FileObject findFO (DataFolder folder, String name, String className) {
        FileObject fo = folder.getPrimaryFile ();
        String classNameEnc = className.replace ('.', '-');

        Enumeration en = fo.getChildren(false);
        FileObject newFile;
        while (en.hasMoreElements()) {
            newFile = (FileObject) en.nextElement();
            if (!newFile.hasExt(INSTANCE)) continue;
            if (name != null) {
                if (!name.equals(getName(newFile))) continue;
            } else {
                if (!classNameEnc.equals(getName(newFile))) continue;
            }
            if (className.equals(InstanceDataObject.Ser.getClassName(newFile))) {
                return newFile;
            }
        }
        return null;
    }

    /** get data object name from specified file object */
    private static String getName(FileObject fo) {
        String superName = (String) fo.getAttribute(EA_NAME);
        if (superName != null) return superName;

        superName = fo.getName();
        int bracket = superName.indexOf (OPEN);
        if (bracket == -1) {
            return unescape(superName);
        } else {
            warnAboutBrackets(fo);
            return unescape(superName.substring(0, bracket));
        }
    }

    /** Finds instance of specified name in a given folder.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param className the name of the class the new object should provide an instance of
    * @return the found instance data object or null if it does not exist
    */
    public static InstanceDataObject find (DataFolder folder, String name, String className) {
        FileObject newFile = findFO (folder, name, className);
        if (newFile != null) {
            try {
                return (InstanceDataObject)DataObject.find (newFile);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return null;
    }

    /** Finds instance of specified name in a given folder.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param clazz the class to create instance for (see class header for details)
    * @return the found instance data object or null if it does not exist
    */
    public static InstanceDataObject find (DataFolder folder, String name, Class clazz) {
        return find (folder, name, clazz.getName ());
    }

    /** Create a new <code>InstanceDataObject</code> in a given folder. If object with specified name already exists, it is returned.
    * You should specify the name if there is a chance another file of the same
    * instance class already exists in the folder; or just to provide a more
    * descriptive name, which will appear in the Explorer for example.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    *        but name cannot be empty
    * @param className the name of the class the new object should provide an instance of (see class header for details)
    * @return the newly created or existing instance data object
    * @exception IOException if the file cannot be created
    */
    public static InstanceDataObject create (DataFolder folder, final String name, final String className) throws IOException {
        final FileObject fo = folder.getPrimaryFile ();
        if (name != null && name.length() == 0) {
            throw new IOException("name cannot be empty"); // NOI18N
        }
        FileObject newFile = findFO (folder, name, className);
        if (newFile == null) {
            final FileObject[] fos = new FileObject[1];

            DataObjectPool.getPOOL().runAtomicAction (fo, new FileSystem.AtomicAction() {
                public void run () throws IOException {
                    String fileName;
                    if (name == null) {
                        fileName = FileUtil.findFreeFileName(
                            fo, className.replace ('.', '-'), INSTANCE);
                    } else {
                        fileName = escape(name);
                    }
                    fos[0] = fo.createData (fileName, INSTANCE);
                    fos[0].setAttribute(EA_INSTANCE_CLASS, className);
                }
            });
            newFile = fos[0];
        }
        return (InstanceDataObject)DataObject.find (newFile);
    }

    /** Create a new <code>InstanceDataObject</code> in a given folder. If object with specified name already exists, it is returned.
    * You should specify the name if there is a chance another file of the same
    * instance class already exists in the folder; or just to provide a more
    * descriptive name, which will appear in the Explorer for example.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param clazz the class to create instance for (see class header for details)
    * @return the newly created or existing instance data object
    * @exception IOException if the file cannot be created
    */
    public static InstanceDataObject create (DataFolder folder, String name, Class clazz) throws IOException {
        return create (folder, name, clazz.getName ());
    }

    /** Create a new <code>InstanceDataObject</code> containing settings
    * in a given folder. If object with specified name already exists, it is returned.
    * If the module info is <code>null</code> then the origin module info
    * of an instance class is tried to find out.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    *        but name cannot be empty
    * @param instance the serializable instance
    * @param info the module info describing the settings provenance (can be <code>null</code>)
    * @return the newly created or existing instance data object
    * @exception IOException if the file cannot be created
    * @since 1.28
    */
    public static InstanceDataObject create (DataFolder folder, String name,
        Object instance, ModuleInfo info) throws IOException {
        return create(folder, name, instance, info, false);
    }

    /** Create a new <code>InstanceDataObject</code> containing settings
    * in a given folder.
    * If the module info is <code>null</code> then the origin module info
    * of an instance class is tried to find out.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    *        but name cannot be empty
    * @param instance the serializable instance
    * @param info the module info describing the settings provenance (can be <code>null</code>)
    * @param create <code>true</code> - always create new file; <code>false</code>
    *       - store to existing file if exist
    * @return the newly created or existing instance data object
    * @exception IOException if the file cannot be created
    * @since 2.9
    */
    public static InstanceDataObject create (DataFolder folder, String name,
        Object instance, ModuleInfo info, boolean create) throws IOException {
        if (name != null && name.length() == 0) {
            throw new IOException("name cannot be empty"); // NOI18N
        }
        return Creator.createInstanceDataObject (folder, name, instance, info, create);

    }

    private static InstanceDataObject storeSettings (DataFolder df, String name, Object obj, ModuleInfo mi)
    throws IOException {
        FileObject fo = df.getPrimaryFile ();
        FileObject newFile = fo.getFileObject (name, XML_EXT);
        String fullname = fo.getPath() + '/' + name + '.' + XML_EXT;
        InstanceDataObject ido;
        boolean attachWithSave = false;
        try {
            
            if (newFile == null) {                
                System.setProperty("InstanceDataObject.current.file", fo.getPath() + "/" + name + "." + XML_EXT); // NOI18N
                final ByteArrayOutputStream buf = storeThroughConvertor(obj, new FileObjectContext(fo, name));
                System.setProperty("InstanceDataObject.current.file", ""); // NOI18N
                createdIDOs.add(fullname);
                newFile = fo.createData (name, XML_EXT);
                FileLock flock = null;
                try {
                    flock = newFile.lock();
                    OutputStream os = newFile.getOutputStream(flock);
                    os.write(buf.toByteArray());
                    os.close();
                } finally {
                    if (flock != null) flock.releaseLock();
                }
            } else attachWithSave = true;

            ido = (InstanceDataObject)DataObject.find (newFile);
            // attachToConvertor will store the object
            ido.attachToConvertor(obj, attachWithSave);
        } finally {
            createdIDOs.remove(fullname);
        }
        return ido;
    }

    /** Remove an existing instance data object.
    * If you have the exact file name, just call {@link DataObject#delete};
    * this method lets you delete an instance you do not have an exact record
    * of the file name for, based on the same information used to create it.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to remove the file from
    * @param name the name of the instance (can be <code>null</code>)
    * @param className the name of class the object referred to (see class header for details)
    * @return <code>true</code> if the instance was succesfully removed, <code>false</code> if not
    */
    public static boolean remove (DataFolder folder, String name,
                                  String className) {
        FileLock lock = null;
        try {
            FileObject fileToRemove = findFO (folder, name, className);
            if (fileToRemove == null) // file not found
                return false;
            lock = fileToRemove.lock();
            fileToRemove.delete(lock);
        } catch (IOException exc) {
            // something is bad, instance wasn't removed
            return false;
        } finally {
            if (lock != null)
                lock.releaseLock();
        }
        return true;
    }

    /** Remove an existing instance data object.
    * If you have the exact file name, just call {@link DataObject#delete};
    * this method lets you delete an instance you do not have an exact record
    * of the file name for, based on the same information used to create it.
    * <p><strong>Note:</strong> use of XML layers to install instances is generally preferred.
    * @param folder the folder to remove the file from
    * @param name the name of the instance (can be <code>null</code>)
    * @param clazz the class the object referred to (see class header for details)
    * @return <code>true</code> if the instance was succesfully removed, <code>false</code> if not
    */
    public static boolean remove (DataFolder folder, String name, Class clazz) {
        return remove (folder, name, clazz.getName ());
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        HelpCtx test = InstanceSupport.findHelp (this);
        if (test != null)
            return test;
        else
            return HelpCtx.DEFAULT_HELP;
    }


    /** Little utility method for posting an exception
     *  to the default <CODE>ErrorManager</CODE> with severity
     *  <CODE>ErrorManager.INFORMATIONAL</CODE>
     */
    static void inform(Throwable t) {
	err.notify(ErrorManager.INFORMATIONAL, t);
    }


    /* Provides node that should represent this data object. When a node for representation
    * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
    * with only parent changed. This implementation creates instance
    * <CODE>DataNode</CODE>.
    * <P>
    * This method is called only once.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    protected Node createNodeDelegate () {
        if (getPrimaryFile().hasExt(XML_EXT)) {
            un = new UpdatableNode(createNodeDelegateImpl());
            return un;
        } else {
            return createNodeDelegateImpl();
        }
    }

    private UpdatableNode un;
    /** allows to swap original node */
    private final class UpdatableNode extends FilterNode {
        public UpdatableNode(Node n) {
            super(n);
        }
        public void update() {
            Children.MUTEX.postWriteRequest(new Runnable() {
                    public void run() {
                        changeOriginal(createNodeDelegateImpl(), true);
                    }
                });
        }
    }

    /** create node delegate */
    private Node createNodeDelegateImpl () {
        try {
            if (getPrimaryFile().getFileSystem() != Repository.getDefault().getDefaultFileSystem()) {
                return new DataNode(this, Children.LEAF);
            }
        } catch (FileStateInvalidException ex) {
            inform(ex);
            return new DataNode(this, Children.LEAF);
        }

        if (getPrimaryFile().hasExt(XML_EXT)) {
            // if lookup does not contain any InstanceCookie then the object
            // is considered as unregognized
            if (null == getCookieFromEP(InstanceCookie.class)) {
                return new CookieAdjustingFilter(new UnrecognizedSettingNode());
            }
            Node n = (Node) getCookieFromEP(Node.class);
            if (n != null) return new CookieAdjustingFilter(n);
        }

        // Instances of Node or Node.Handle should be used as is.
        try {
            if (instanceOf (Node.class)) {
                Node n = (Node)instanceCreate ();
                return new CookieAdjustingFilter(n);
            } else if (instanceOf (Node.Handle.class)) {
                Node.Handle h = (Node.Handle) instanceCreate ();
                return new CookieAdjustingFilter(h.getNode());
            }
        } catch (IOException ex) {
            inform(ex);
        } catch (ClassNotFoundException ex) {
            inform(ex);
        }

        return new InstanceNode (this);
    }

    /** Node presents IDO as unregonized setting object which can be just deleted. */
    private final class UnrecognizedSettingNode extends AbstractNode {
        public UnrecognizedSettingNode() {
            super(Children.LEAF);
            setName(NbBundle.getMessage(InstanceDataObject.class, "LBL_BrokenSettings")); //NOI18N
            setIconBase("org/openide/loaders/instanceBroken"); //NOI18N
            setShortDescription(InstanceDataObject.this.getPrimaryFile().toString());
        }

        public boolean canDestroy() {
            return true;
        }
        public boolean canCut() {
            return false;
        }
        public boolean canCopy() {
            return false;
        }
        public boolean canRename() {
            return false;
        }
        public void destroy() throws IOException {
            InstanceDataObject.this.delete();
        }
        protected SystemAction[] createActions() {
            return new SystemAction[] {SystemAction.get(DeleteAction.class)};
        }

    }

    /** A filter which ensures that when some-random-class-impl-Node.instance
     * is created, its node delegate gives itself as the cookie for DataObject,
     * and not some other unrelated data object. E.g. Services/.../TemplatesNode.instance
     * vs. Templates/ and similar. See DataNodeTest unit test.
     */
    private final class CookieAdjustingFilter extends FilterNode {
        public CookieAdjustingFilter(Node n) {
            super(n, null, new ProxyLookup(new Lookup[] {
                n.getLookup (),
                Lookups.singleton(InstanceDataObject.this),
            }));
        }
        
        // If this node is used as the root of a new Explorer window etc.,
        // just save the real underlying node; no need to make it a CAF later.
        public Node.Handle getHandle() {
            return getOriginal().getHandle();
        }
        // #17920: Index cookie works only when equality works
        public boolean equals(Object o) {
            return this == o || getOriginal().equals(o) || (o != null && o.equals(getOriginal()));
        }
        public int hashCode() {
            return getOriginal().hashCode();
        }
    }

    /** delegate .getCookie to Environment.Provider */
    private Object getCookieFromEP(Class clazz) {
        //updateLookup(false);
        return getCookiesLookup().lookup(clazz);
    }

    void notifyFileChanged(FileEvent fe) {
        super.notifyFileChanged(fe);
        if (getPrimaryFile().hasExt(XML_EXT)) {
            if (!Creator.isFiredFromMe(fe)) {
                getCookiesLookup(true);
            }
        }
    }

    /* Serve up editor cookies where requested. */
    public Node.Cookie getCookie(Class clazz) {
        Node.Cookie supe = null;
        if (getPrimaryFile().hasExt(XML_EXT)) {
            // #24683 fix: do not return any cookie until the .settings file is written
            // successfully; PROP_COOKIE is fired when cookies are available.
            String filename = getPrimaryFile().getPath();
            if (createdIDOs.contains(filename)) return null;

            supe = (Node.Cookie) getCookieFromEP(clazz);
            if (InstanceCookie.class.isAssignableFrom(clazz)) return supe;
        }
        if (supe == null) supe = super.getCookie(clazz);
        return supe;
    }

    private Lookup.Result cookieResult = null;
    private Lookup.Result nodeResult = null;
    private Lookup cookiesLkp = null;
    private LookupListener cookiesLsnr = null;
    private LookupListener nodeLsnr = null;

    private Lookup getCookiesLookup() {
        return getCookiesLookup(false);       
    }
    
    private Lookup getCookiesLookup(boolean reinit) {
        synchronized (getLock()) {
            if (!reinit && cookiesLkp != null) {
                return cookiesLkp;
            }
        }
        Lookup envLkp = Environment.findForOne(InstanceDataObject.this);

        synchronized (getLock()) {
            if (cookiesLkp == null || envLkp == null || !envLkp.getClass().equals(cookiesLkp.getClass())) {
                cookiesLkp = (envLkp == null) ? Lookup.EMPTY : envLkp;
                initCookieResult();
                initNodeResult();
            } 
        }
        
        if (nodeResult != null) nodeResult.allItems();
        if (cookieResult != null) cookieResult.allItems();
        
        return cookiesLkp;        
    }

    private void initNodeResult() {
        if (nodeResult != null && nodeLsnr != null) {
            nodeResult.removeLookupListener(nodeLsnr);
        }            
        
        if (cookiesLkp != null && !cookiesLkp.equals(Lookup.EMPTY)) {
            nodeResult = cookiesLkp.lookup(new Lookup.Template(InstanceCookie.class));
            nodeLsnr = new LookupListener() {
                        public void resultChanged(LookupEvent lookupEvent) {
                            if (InstanceDataObject.this.un != null) {
                                un.update();
                            }
                        }
                    };
            nodeResult.addLookupListener(nodeLsnr);
        }
    }

    private void initCookieResult() {
        if (cookieResult != null && cookiesLsnr != null) {
            cookieResult.removeLookupListener(cookiesLsnr);
        }                    
        if (cookiesLkp != null && !cookiesLkp.equals(Lookup.EMPTY)) {
            cookieResult = cookiesLkp.lookup(new Lookup.Template(Node.Cookie.class));
            cookiesLsnr = new LookupListener() {
                public void resultChanged(LookupEvent lookupEvent) {
                    firePropertyChange(DataObject.PROP_COOKIE, null, null);
                }
            };
            cookieResult.addLookupListener(cookiesLsnr);
        }
    }

    /** Finds delegate instance cookie/if provided in cookie set.
    * @return instance cookie or null
    */
    private InstanceCookie.Of delegateIC () {
        //return ser;
        InstanceCookie.Of ic = null;
        if (getPrimaryFile().hasExt(XML_EXT)) {
            ic = (InstanceCookie.Of) getCookieFromEP(InstanceCookie.Of.class);
        } else {
            ic = ser;
        }
        return ic;
    }

    /* The name of the bean for this file or null if the class name is not encoded
    * in the file name and rather the CLASS_NAME property from the file content should be used.
    *
    * @return the name for the instance or null if the class name is not defined in the name
    */
    public String instanceName () {
        InstanceCookie delegateIC = delegateIC ();
        if (delegateIC == null) return this.getName();
        return delegateIC.instanceName ();
    }

    /* The class of the instance represented by this cookie.
    * Can be used to test whether the instance is of valid
    * class before it is created.
    *
    * @return the class of the instance
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Class instanceClass ()
    throws IOException, ClassNotFoundException {
        InstanceCookie delegateIC = delegateIC ();
        if (delegateIC == null) return this.getClass();
        return delegateIC.instanceClass ();
    }

    /** Query if this instance can create object of given type.
    * @param type the type to create
    * @return true or false
    */
    public boolean instanceOf (Class type) {
        InstanceCookie.Of delegateIC = delegateIC ();
        if (delegateIC == null) return type.isAssignableFrom(this.getClass());
        return delegateIC.instanceOf (type);
    }

    /*
    * @return an object to work with
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Object instanceCreate ()
    throws IOException, ClassNotFoundException {
        InstanceCookie delegateIC = delegateIC ();
        if (delegateIC == null) return this;
        return delegateIC.instanceCreate ();
    }
    
    /** Checks whether the instance was created by this object.
     */
    final boolean creatorOf (Object inst) {
        InstanceCookie delegateIC = delegateIC ();
        if (delegateIC instanceof Ser) {
            return ((Ser)delegateIC).creatorOf (inst);
        }
        return false;
    }

    /* Overriden to return only first part till the bracket */
    public String getName () {
        String superName = (String) getPrimaryFile().getAttribute(EA_NAME);
        if (superName != null) return superName;

        superName = super.getName();
        int bracket = superName.indexOf (OPEN);
        if (bracket == -1) {
            return unescape(superName);
        } else {
            warnAboutBrackets(getPrimaryFile());
            return unescape(superName.substring(0, bracket));
        }
    }

    private static final Set warnedAboutBrackets = new WeakSet(); // Set<FileObject>
    /** Make sure people stop using this syntax eventually.
     * It is better to use the file attribute, not least because some VMs
     * do not much like [] in file names (OpenVMS had problems at one point, e.g.).
     */
    private static void warnAboutBrackets(FileObject fo) {
        if (warnedAboutBrackets.add(fo)) {
            err.log(ErrorManager.WARNING, "Use of [] in " + fo + " is deprecated."); // NOI18N
            err.log(ErrorManager.WARNING, "(Please use the string-valued file attribute instanceClass instead.)"); // NOI18N
        }
    }

    // [PENDING] probably setName also needs to be overridden!
    /* Renames all entries and changes their files to new ones.
     */
    protected FileObject handleRename (String name) throws IOException {
        FileObject fo = getPrimaryFile();
        fo.setAttribute(EA_NAME, name);
        return fo;
    }

    // SEE ALSO org.netbeans.core.windows.util.WindowUtils FOR COPIED IMPL OF escape/unescape:

    // XXX #27494 Please changes to this method apply also into
    // core/naming/src/org/netbeans/core/naming/Utils class.

    /** Hex-escapes anything potentially nasty in some text.
     * Package-private for the benefit of the test suite.
     */
    static String escape (String text) {
        boolean spacenasty = text.startsWith(" ") || text.endsWith(" ") || text.indexOf("  ") != -1; // NOI18N
        int len = text.length ();
        StringBuffer escaped = new StringBuffer (len);
        for (int i = 0; i < len; i++) {
            char c = text.charAt (i);
            // For some reason Windoze throws IOException if angle brackets in filename...
            if (c == '/' || c == ':' || c == '\\' || c == OPEN || c == CLOSE || c == '<' || c == '>' ||
                    // ...and also for some other chars (#16479):
                    c == '?' || c == '*' || c == '|' ||
                    (c == ' ' && spacenasty) ||
                    c == '.' || c == '"' || c < '\u0020' || c > '\u007E' || c == '#') {
                // Hex escape.
                escaped.append ('#');
                String hex = Integer.toString (c, 16).toUpperCase ();
                if (hex.length () < 4) escaped.append ('0');
                if (hex.length () < 3) escaped.append ('0');
                if (hex.length () < 2) escaped.append ('0');
                escaped.append (hex);
            } else {
                escaped.append (c);
            }
        }
        return escaped.toString ();
    }

    /** Removes hex escapes and regenerates displayable Unicode. */
    static String unescape (String text) {
        int len = text.length ();
        StringBuffer unesc = new StringBuffer (len);
        for (int i = 0; i < len; i++) {
            char c = text.charAt (i);
            if (c == '#') {
                if (i + 4 >= len) {
                    err.log(ErrorManager.WARNING, "trailing garbage in instance name: " + text); // NOI18N
                    break;
                }
                try {
                    char[] hex = new char[4];
                    text.getChars (i + 1, i + 5, hex, 0);
                    unesc.append ((char) Integer.parseInt (new String (hex), 16));
                } catch (NumberFormatException nfe) {
                    err.notify(ErrorManager.INFORMATIONAL, nfe);
                }
                i += 4;
            } else {
                unesc.append (c);
            }
        }
        return unesc.toString ();
    }

    // XXX #27494 Please changes to this field apply also into
    // core/naming/src/org/netbeans/core/naming/Utils class.
    private final static int MAX_FILENAME_LENGTH = 50;

    // XXX #27494 Please changes to this method apply also into
    // core/naming/src/org/netbeans/core/naming/Utils class.
    /** escape a filename and map it to the name with max length MAX_FILENAME_LENGTH
     * @see issue #17186
     */
    static String escapeAndCut (String name) {
        int maxLen = MAX_FILENAME_LENGTH;

        String ename = escape(name);
        if (ename.length() <= maxLen)  return ename;
        String hash = Integer.toHexString(ename.hashCode());
        maxLen = (maxLen > hash.length()) ? (maxLen-hash.length()) / 2 :1;
        String start = ename.substring(0, maxLen);
        String end = ename.substring(ename.length() - maxLen);

        return start + hash + end;
    }

    /** schedule task to save the instance */
    final void scheduleSave () {
        // just for .ser files
        if (isSavingCanceled() || !getPrimaryFile().hasExt(SER_EXT)) return;
        doFileLock();
        ser.getSaveTask().schedule(SAVE_DELAY);
    }

    private FileLock fileLock;

    /** try to lock the primary file; may return <code>null</code> */
    private FileLock doFileLock() {
        synchronized (getLock()) {
            if (fileLock != null) return fileLock;
            try {
                fileLock = getPrimaryFile().lock();
            } catch (IOException ex) {
                err.annotate(ex, getPrimaryFile().toString());
        	inform(ex);
            }
            return fileLock;
        }
    }

    /** release the file lock if any was taken */
    private void relaseFileLock() {
        synchronized (getLock()) {
            if (fileLock == null) return;
            fileLock.releaseLock();
            fileLock = null;
        }
    }
    /* Creates new object from template.
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        try {
            if (getPrimaryFile().hasExt(XML_EXT)) {
                InstanceCookie ic = (InstanceCookie)this.getCookie(InstanceCookie.class);
                Object obj = ic.instanceCreate();

                DataObject d = createSettingsFile(df, name, obj);
                // reset template instance to null
                attachToConvertor(null);
                return d;
            } else if ( (!getPrimaryFile().hasExt(INSTANCE)) &&
                        Serializable.class.isAssignableFrom( instanceClass()) ) {
                InstanceCookie ic = (InstanceCookie)this.getCookie(InstanceCookie.class);
                Object obj = ic.instanceCreate();

                return DataObject.find( createSerFile( df, name, obj ) );
            }
        } catch (ClassNotFoundException ex) {
    	    inform(ex);
        }

        return super.handleCreateFromTemplate(df, name);
    }

    /* Copy a service sanely. For settings and serializable beans, special
     * methods are used to write out the resulting files, and the name to
     * use is taken from the *display name* of the current file, as this is
     * what the user is accustomed to seeing (for ServiceType's especially).
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=16278">Issue #16278</a>
     */
    protected DataObject handleCopy(DataFolder df) throws IOException {
        if (getPrimaryFile ().getFileSystem().isDefault()) {
            try {
                if (getPrimaryFile ().hasExt(XML_EXT)) {
                    InstanceCookie ic = (InstanceCookie)getCookie(InstanceCookie.class);
                    if (ic != null) {
                        Object obj = ic.instanceCreate();
                        InstanceDataObject ido = createSettingsFile(
                            df, getNodeDelegate().getDisplayName(), obj);
                        ido.attachToConvertor(null);
                        return ido;
                    }
                } else if ( (!getPrimaryFile().hasExt(INSTANCE)) &&
                            Serializable.class.isAssignableFrom(instanceClass()) ) {
                    InstanceCookie ic = (InstanceCookie)getCookie(InstanceCookie.class);
                    if (ic != null) {
                        Object obj = ic.instanceCreate();
                        return DataObject.find(createSerFile(
                            df, getNodeDelegate().getDisplayName(), obj));
                    }
                }
            } catch (ClassNotFoundException ex) {
                inform(ex);
            }
        }
        return super.handleCopy(df);
    }

    /** Is the saving task already canceled? If yes do not schedule it again. */
    private boolean isSavingCanceled() {
        return savingCanceled;
    }

    protected void dispose() {
        if (getPrimaryFile().hasExt(SER_EXT)) {
            savingCanceled = true;
            if (ser != null) {
                RequestProcessor.Task task = ser.getSaveTask();
                if (task.getDelay() > 0 || ser.isSaving() && !task.isFinished()) {
                    task.waitFinished();
                }
            }
            relaseFileLock();
        } else if (getPrimaryFile().hasExt(XML_EXT)) {
            SaveCookie s = (SaveCookie) getCookie(SaveCookie.class);
            try {
                if (s != null) s.save();
            } catch (IOException ex) {
                //ignore
            }
        }
        super.dispose();
    }

    protected void handleDelete() throws IOException {
        savingCanceled = true;
        if (getPrimaryFile().hasExt(XML_EXT)) {
            handleDeleteSettings();
            return;
        }
        if (ser != null) {
            RequestProcessor.Task task = ser.getSaveTask();
            task.cancel();
            if (ser.isSaving() && !task.isFinished()) task.waitFinished();
        }
        relaseFileLock();
        super.handleDelete();
    }

    private void handleDeleteSettings() throws IOException {
        SaveCookie s = (SaveCookie) getCookie(SaveCookie.class);
        try {
            if (s != null) s.save();
        } catch (IOException ex) {
            // ignore
        }
        super.handleDelete();
    }

    private InstanceDataObject createSettingsFile (DataFolder df, String name, Object obj)
    throws IOException {
        boolean isServiceType = false;

        String filename;
        // find name for new service type
        if (obj instanceof ServiceType) {
            isServiceType = true;
            ServiceType sr = (ServiceType) obj;
            name = name == null? sr.getName(): name;
            String stName = name;
            ServiceType.Registry r = (ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class);
            for (int i = 1; r.find(stName) != null; i++) {
                stName = new StringBuffer(name.length() + 2).
                    append(name).append('_').append(i).toString();
            }
            if (!stName.equals(sr.getName())) {
                // Do not modify the original!
                sr = sr.createClone();
                obj = sr;
                sr.setName(stName);
            }
            filename = escapeAndCut(stName);
        } else {
            filename = (name == null)? getPrimaryFile ().getName (): escapeAndCut(name);
        }

        filename = FileUtil.findFreeFileName(
                   df.getPrimaryFile (), filename, getPrimaryFile ().getExt ()
               );

        InstanceDataObject newFile = storeSettings(df, filename, obj, null);
        if (name != null && !isServiceType) {
            newFile.getPrimaryFile().setAttribute(EA_NAME, name);
        }
        return newFile;
    }

    private FileObject createSerFile(
        DataFolder df, String name, Object obj
    ) throws IOException {
        FileLock lock = null;
        OutputStream ostream = null;
        FileObject newFile = null;
        try {
            FileObject fo = df.getPrimaryFile ();

            if (name == null) {
                name = FileUtil.findFreeFileName(
                           df.getPrimaryFile (), getPrimaryFile ().getName (), getPrimaryFile ().getExt ()
                       );
            }

            newFile = fo.getFileObject (name, SER_EXT);
            if (newFile == null) newFile = fo.createData (name, SER_EXT);

            lock = newFile.lock ();
            ostream = newFile.getOutputStream(lock);

            ObjectOutputStream p = new ObjectOutputStream(ostream);
            p.writeObject(obj);
            p.flush();
        } finally {
            if (ostream != null)
                ostream.close();
            if (lock != null)
                lock.releaseLock ();
        }
        return newFile;
    }

    /** Support for serialized objects.
     */
    private static final class Ser extends InstanceSupport
    implements Runnable {
        /** the reference to the bean, so it is created just once when used */
        private Reference bean = new SoftReference(null);
        /** last time the bean was read from a file */
        private long saveTime;

        /** Custom class loader */
        private ClassLoader customClassLoader;
        private InstanceDataObject dobj;

        /** @param dobj IDO containing the serialized instance */
        public Ser (InstanceDataObject dobj) {
            super (dobj.getPrimaryEntry());
            customClassLoader = null;
            this.dobj = dobj;
        }

        public String instanceName () {
            // try the life object if any
            FileObject fo = entry ().getFile ();
            if (fo.lastModified ().getTime () <= saveTime) {
                Object o = bean.get ();
                if (o != null) {
                    return o.getClass().getName();
                }
            }

            if (!fo.hasExt (INSTANCE)) {
                return super.instanceName ();
            }
            return getClassName(fo);
        }

        /** get class name from specified file object*/
        private static String getClassName(FileObject fo) {
            // first of all try "instanceClass" property of the primary file
            Object attr = fo.getAttribute (EA_INSTANCE_CLASS);
            if (attr instanceof String) {
                return Utilities.translate((String) attr);
            } else if (attr != null) {
                err.log(ErrorManager.WARNING,
                    "instanceClass was a " + attr.getClass().getName()); // NOI18N
            }

            attr = fo.getAttribute (EA_INSTANCE_CREATE);
            if (attr != null) {
                return attr.getClass().getName();
            }

            // otherwise extract the name from the filename
            String name = fo.getName ();

            int first = name.indexOf (OPEN) + 1;
            if (first != 0) {
                warnAboutBrackets(fo);
            }

            int last = name.indexOf (CLOSE);
            if (last < 0) {
                last = name.length ();
            }

            // take only a part of the string
            if (first < last) {
                name = name.substring (first, last);
            }

            name = name.replace ('-', '.');
            name = Utilities.translate(name);

            //System.out.println ("Original: " + getPrimaryFile ().getName () + " new one: " + name); // NOI18N
            return name;
        }

        /** Uses cache to remember list of classes to them this object is
        * assignable.
        */
        public Class instanceClass() throws IOException, ClassNotFoundException {
            return super.instanceClass (customClassLoader);
        }

        /** Uses the cache to answer this question without loading the class itself, if the
        * cache exists.
        */
        public boolean instanceOf (Class type) {
            // try the life object if any
            FileObject fo = entry ().getFile ();
            if (fo.lastModified ().getTime () <= saveTime) {
                Object o = bean.get ();
                if (o != null) {
                    return type.isInstance (o);
                }
            }

            // else do checking of classes


            // null means no cache exists
            Boolean res = inListOfClasses (type, entry ().getFile ());
            if (res == null) {
                // uses instanceClass and then assignableFrom
                return super.instanceOf (type);
            }
            return res.booleanValue ();
        }


        public Object instanceCreate () throws IOException, ClassNotFoundException {
            FileObject fo = entry ().getFile ();


            Object o;
            if (fo.lastModified ().getTime () <= saveTime) {
                o = bean.get ();
            } else {
                o = null;
            }

            if (o != null) {
                return o;
            }

            saveTime = fo.lastModified ().getTime ();
            if (saveTime < System.currentTimeMillis ()) {
                saveTime = System.currentTimeMillis ();
            }
            if (fo.hasExt (INSTANCE)) {
                // try to ask for instance creation attribute
                o = fo.getAttribute (EA_INSTANCE_CREATE);
            }

            if (o == null) {
                // try super method
                o = super.instanceCreate ();
            }

            // remember the created value
            bean = new SoftReference(o);
            return o;
        }

        /** Checks whether the instance was created by this object.
         */
        final boolean creatorOf (Object inst) {
            Reference r = bean;
            return r != null && r.get () == inst;
        }
        
        
        public void run () {
            try {
                saving = true;
                runImpl();
            } finally {
                dobj.relaseFileLock();
                saving = false;
            }
        }

        /** Saves the bean to disk.
         */
        private void runImpl () {
            Object bean = this.bean.get ();
            if (bean == null) {
                // nothing to save
                return;
            }

            try {
                FileLock lock = dobj.doFileLock();
                if (lock == null) return;
                ObjectOutputStream oos = new ObjectOutputStream (
                    entry ().getFile ().getOutputStream (lock)
                );
                try {
                    oos.writeObject (bean);
                    // avoid bean reloading
                    saveTime = entry ().getFile ().lastModified ().getTime ();
                } finally {
                    oos.close ();
                }
            } catch (IOException ex) {
                err.annotate (ex, NbBundle.getMessage (
                    InstanceDataObject.class, "EXC_CannotSaveBean", // NOI18N
                    instanceName (), entry ().getFile ().getPath()
                ));
                err.notify (ex);
            }

        }

        /** Check whether a given class is in list of all classes assigned to fo.
        * @param type type to test
        * @param fo file object to check
        * @return true if the class is in the list of objects
        */
        private static Boolean inListOfClasses (Class type, FileObject fo) {
            Object obj = fo.getAttribute (EA_INSTANCE_OF);
            if (obj instanceof String) {
                String typeName = type.getName ();
                StringTokenizer tok = new StringTokenizer ((String)obj, "\n\t ,;:"); // NOI18N
                while (tok.hasMoreTokens ()) {
                    String t = tok.nextToken ().trim();
                    if (typeName.equals (t)) {
                        // we know this class is in the list of otherclasses
                        return Boolean.TRUE;
                    }
                }

                return Boolean.FALSE;
            } else if (obj != null) {
                err.log(ErrorManager.WARNING, "instanceOf was a " + obj.getClass().getName()); // NOI18N
            }
            // means no cache exists
            return null;
        }

        /** Converts type to string.
        * @param type
        * @param sb string buffer to store
        * @param done already added class Set(String)
        * @return true if something was added to the buffer
        */
        private static boolean collectType (
            Class type, StringBuffer sb, HashSet added
        ) {
            if (type == null) {
                // can be null for interfaces
                return false;
            }

            String typeName = type.getName ();
            if (added.contains (typeName)) {
                return false;
            }

            added.add (typeName);

            // add superclasses
            if (collectType (type.getSuperclass (), sb, added)) {
                sb.append (',');
            }

            // add superinterfaces
            Class[] impls = type.getInterfaces ();
            for (int i = 0; i < impls.length; i++) {
                if (collectType (impls[i], sb, added)) {
                    sb.append (',');
                }
            }

            sb.append (typeName);
            return true;
        }

        final void setCustomClassLoader(ClassLoader cl) {
            this.customClassLoader = cl;
        }

        /** save task */
        private RequestProcessor.Task task;

        /** return the instance save task */
        public RequestProcessor.Task getSaveTask() {
            if (task == null) {
                task = PROCESSOR.create(this);
            }
            return task;
        }

        /** save task is running */
        private boolean saving = false;

        public boolean isSaving() {
            return saving;
        }

    } // end of Ser

    final void setCustomClassLoader(ClassLoader cl) {
        if (ser instanceof Ser)
            ((Ser) ser).setCustomClassLoader(cl);
    }

    /** Support for creating instances allowing identify the origin of file events
     * fired as a consequence of this creating.
     * Not thread safe.
     */
    private static class Creator implements FileSystem.AtomicAction {
        private ModuleInfo mi = null;
        private DataFolder folder = null;
        private Object  instance = null;
        private String  name = null;
        private InstanceDataObject result = null;
        private boolean create;

        private final static Creator me = new Creator ();


        private Creator() {
        }

        public void run () throws IOException {
            FileObject fo = folder.getPrimaryFile ();
            String filename = name;
            if (filename == null) {
                filename = instance.getClass().getName().replace ('.', '-');
                filename = FileUtil.findFreeFileName(fo, filename, XML_EXT);
            } else {
                String escapedFileName = escape(filename);
                // do not cut if such file already exist
                FileObject newFile = fo.getFileObject (escapedFileName, XML_EXT);
                if (newFile == null) {
                    filename = escapeAndCut(filename);
                } else {
                    filename = escapedFileName;
                }

                
                if (create /*|| (newFile == null && Utilities.isWindows()) */) {
                    filename = FileUtil.findFreeFileName(fo, filename, XML_EXT);
                }
            }

            result = storeSettings(folder, filename, instance, mi);
        }

        /** see InstanceDataObject.create */
        public static InstanceDataObject createInstanceDataObject (
        DataFolder folder, String name, Object instance, ModuleInfo mi,
        boolean create) throws IOException {
            synchronized (me) {
                me.mi = mi;
                me.folder = folder;
                me.instance = instance;
                me.name = name;
                me.create = create;

                DataObjectPool.getPOOL().runAtomicActionSimple (folder.getPrimaryFile(), me);
                me.mi = null;
                me.folder = null;
                me.instance = null;
                me.name = null;
                InstanceDataObject result = me.result;
                me.result = null;
                return result;
            }
        }
        /** is file event originated by this Creator? */
        public static boolean isFiredFromMe (FileEvent fe)  {
            return fe.firedFrom(me);
        }
    }

    /** store object to strem using convertor */
    private static ByteArrayOutputStream storeThroughConvertor(Object inst, FileObjectContext ctx) throws IOException {
        FileObject fo = resolveConvertor(inst);
        Object convertor = fo.getAttribute("settings.convertor"); // NOI18N
        if (convertor == null) throw new IOException("missing attribute settings.convertor"); // NOI18N
        ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
        Writer w = new OutputStreamWriter(b, "UTF-8"); // NOI18N
        convertorWriteMethod(convertor, new WriterProvider(w, ctx), inst);
        w.close();
        return b;
    }

    /** reflection for void write (java.io.Writer w, Objectinst) method */
    private static void convertorWriteMethod(Object convertor, Writer w, Object inst) throws IOException {
        Throwable e = null;
        try {
            Method method = convertor.getClass().getMethod(
                "write", // NOI18N
                new Class[] {Writer.class, Object.class});
            method.setAccessible(true);
            method.invoke(convertor, new Object[] {w, inst});
        } catch (NoSuchMethodException ex) {
            e = ex;
        } catch (IllegalAccessException ex) {
            e = ex;
        } catch (InvocationTargetException ex) {
            e = ex.getTargetException();
            if (e instanceof IOException) throw (IOException) e;
        }
        if (e != null) {
            throw (IOException)new IOException("Problem with Convertor.write method. "+e).initCause(e); // NOI18N
        }
    }

    /** path where to find convertor/provider definition */
    private final static String EA_PROVIDER_PATH = "settings.providerPath"; // NOI18N

    /** look up appropriate convertor according to obj */
    private static FileObject resolveConvertor(Object obj) throws IOException {
        String prefix = "xml/memory"; //NOI18N
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();

        FileObject memContext = sfs.findResource(prefix);
        if (memContext == null) throw new FileNotFoundException("SFS:xml/memory while converting a " + obj.getClass().getName()); //NOI18N

        String[] classes = new String[] {obj.getClass().getName(), Object.class.getName()};
        for (int i = 0; i < classes.length; i++) {
            String convertorPath = new StringBuffer(200).append(prefix).append('/').
                append(classes[i].replace('.', '/')).toString(); // NOI18N
            FileObject fo = sfs.findResource(convertorPath);
            if (fo != null) {
                String providerPath = (String) fo.getAttribute(EA_PROVIDER_PATH);
                if (providerPath == null) break;
                FileObject ret = sfs.findResource(providerPath);
               if (ret == null) {
                   throw new FileNotFoundException("Invalid settings.providerPath under SFS/xml/memory/ for " + obj.getClass()); // NOI18N
               } else {
                   return ret;
               }
            }
        }
        throw new FileNotFoundException("None convertor was found under SFS/xml/memory/ for " + obj.getClass()); //NOI18N
    }

    private void attachToConvertor(Object obj) throws IOException {
        attachToConvertor (obj, false);
    }

    /** propagate instance to convertor; obj can be null */
    private void attachToConvertor(Object obj, boolean save) throws IOException {        
        // InstanceCookie subclass has to implement
        // void setInstance(Object inst)
        Object ic = getCookiesLookup().lookup(InstanceCookie.class);
        if (ic == null) {
            throw new IllegalStateException(
                "Trying to store object " + obj // NOI18N
                + " which most probably belongs to already disabled module!");// NOI18N
        }
        convertorSetInstanceMethod(ic, obj, save);
    }

    /** reflection for void setInstance(Object inst) */
    private static void convertorSetInstanceMethod(Object convertor, Object inst, boolean save) throws IOException {
        Exception e = null;
        try {
            Method method = convertor.getClass().getMethod(
                "setInstance", // NOI18N
                new Class[] {Object.class, Boolean.TYPE});
            method.setAccessible(true);
            method.invoke(convertor, new Object[] {inst,
            (save ? Boolean.TRUE : Boolean.FALSE)});
        } catch (NoSuchMethodException ex) {
            e = ex;
        } catch (IllegalAccessException ex) {
            e = ex;
        } catch (InvocationTargetException ex) {
            e = ex;
            if (ex.getTargetException() instanceof IOException) {
                throw (IOException) ex.getTargetException();
            }
        }
        if (e != null) {
            ErrorManager.getDefault().annotate(
                e, "Problem with InstanceCookie.setInstance method: " + convertor.getClass()); // NOI18N
            inform(e);
        }
    }

    /** filenames list of just created files; sync purpose */
    private static final List createdIDOs =
        Collections.synchronizedList(new ArrayList(1));

    /** helper allowing a Writer to provide context via Lookup.Provider
     */
    private static final class WriterProvider extends Writer implements Lookup.Provider {
        private final Writer orig;
        private final FileObjectContext ctx;
        private Lookup lookup;

        public WriterProvider(Writer w, FileObjectContext ctx) {
            this.orig = w;
            this.ctx = ctx;
        }

        public void close() throws IOException {
            orig.close();
        }

        public void flush() throws IOException {
            orig.flush();
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            orig.write(cbuf, off, len);
        }

        public Lookup getLookup() {
            if (lookup == null) {
                lookup = Lookups.singleton(ctx);
            }
            return lookup;
        }

    }

    /** The Restricted FileObject implementation allowing to get just
     * read-only informations about name and location. It should prevent
     * any manipulation with file or its content.
     */
    private static final class FileObjectContext extends FileObject {
        private static final String UNSUPPORTED = "The Restricted FileObject" + //NOI18N
            " implementation allowing to get just read-only informations about" + //NOI18N
            " name and location. It should prevent any manipulation with file" + //NOI18N
            " or its content."; //NOI18N
        private final FileObject fo;
        private final FileObject parent;
        private final String name;

        public FileObjectContext(FileObject parent, String name) {
            this.parent = parent;
            this.name = name;
            this.fo = parent.getFileObject(name, XML_EXT);
        }

        public void addFileChangeListener(FileChangeListener fcl) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public FileObject createData(String name, String ext) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public FileObject createFolder(String name) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public void delete(FileLock lock) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public Object getAttribute(String attrName) {
            return fo == null? null: fo.getAttribute(attrName);
        }

        public Enumeration getAttributes() {
            return fo == null? Enumerations.empty(): fo.getAttributes();
        }

        public FileObject[] getChildren() {
            return new FileObject[0];
        }

        public String getExt() {
            return InstanceDataObject.XML_EXT; //NOI18N
        }

        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        public FileSystem getFileSystem() throws FileStateInvalidException {
            return parent.getFileSystem();
        }

        public InputStream getInputStream() throws FileNotFoundException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public String getName() {
            return name;
        }

        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public FileObject getParent() {
            return parent;
        }

        public long getSize() {
            return fo == null? 0: fo.getSize();
        }

        public boolean isData() {
            return true;
        }

        public boolean isFolder() {
            return false;
        }

        public boolean isReadOnly() {
            return parent.isReadOnly();
        }

        public boolean isRoot() {
            return false;
        }

        public boolean isValid() {
            return fo == null? false: fo.isValid();
        }

        public Date lastModified() {
            return fo == null? parent.lastModified(): fo.lastModified();
        }

        public FileLock lock() throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public void removeFileChangeListener(FileChangeListener fcl) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public void setAttribute(String attrName, Object value) throws IOException {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

        public void setImportant(boolean b) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }

    }

}
