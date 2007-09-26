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

package org.openide.loaders;


import java.beans.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.netbeans.modules.openide.loaders.DataObjectEncodingQueryImplementation;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;

/** Object that represents one or more file objects, with added behavior 
* accessible though {@link #getLookup} lookup pattern. Since version 6.0
* this class implements {@link org.openide.util.Lookup.Provider}.
*
* @author Jaroslav Tulach, Petr Hamernik, Jan Jancura, Ian Formanek
*/
public abstract class DataObject extends Object
implements Node.Cookie, Serializable, HelpCtx.Provider, Lookup.Provider {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = 3328227388376142699L;

    /** Name of the template property. */
    public static final String PROP_TEMPLATE = "template"; // NOI18N

    /** Name of the name property. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of the help context property. */
    public static final String PROP_HELP = "helpCtx"; // NOI18N

    /** Name of the modified property. */
    public static final String PROP_MODIFIED = "modified"; // NOI18N

    /** Name of the property used during notification of changes in the set of cookies attached to this object. */
    public static final String PROP_COOKIE = Node.PROP_COOKIE;

    /** Name of valid property. Allows listening to deletion or disposal of the data object. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /** Name of primary file property. Primary file is changed when the object is moved */
    public static final String PROP_PRIMARY_FILE = "primaryFile"; // NOI18N
    /** Name of files property. Allows listening to set of files handled by this object. */
    public static final String PROP_FILES = "files"; // NOI18N

    /** Extended attribute for holding the class of the loader that should
    * be used to recognize a file object before the normal processing takes
    * place.
    */
    static final String EA_ASSIGNED_LOADER = "NetBeansAttrAssignedLoader"; // NOI18N
    /** Extended attribute which may be used in addition to EA_ASSIGNED_LOADER
     * which indicates the code name base of the module that installed that preferred
     * loader. If the indicated module is not installed, ignore the loader request.
     * See #13816.
     */
    static final String EA_ASSIGNED_LOADER_MODULE = "NetBeansAttrAssignedLoaderModule"; // NOI18N

    /** all modified data objects contains DataObjects.
    * ! Use syncModified for modifications instead !*/
    private static ModifiedRegistry modified = new ModifiedRegistry();
    /** sync modified data (for modification operations) */
    private static Set<DataObject> syncModified = Collections.synchronizedSet(modified);

    /** Modified flag */
    private boolean modif = false;

    /** the node delegate for this data object */
    private transient Node nodeDelegate;

    /** item with info about this data object */
    DataObjectPool.Item item;

    /** the loader for this data object */
    private DataLoader loader;

    /** property change listener support */
    private PropertyChangeSupport changeSupport;
    private VetoableChangeSupport vetoableChangeSupport;

    /** The synchronization lock used only for methods creating listeners 
     * objects. It is static and shared among all DataObjects.
     */
    private static final Object listenersMethodLock = new Object();
    
    /** Lock used for ensuring there will be just one node delegate */
    private Object nodeCreationLock = new Object();
    
    /** Lock for copy/move/rename/etc. operations */
    private static Object synchObject = new Object ();


    /** default logger for whole package */
    static final Logger LOG = Logger.getLogger("org.openide.loaders"); // NOI18N

    static {
        DataObjectAccessor.DEFAULT = new DataObjectAccessorImpl();
    }
    
    /** Create a new data object.
     *
     * @param pf primary file object for this data object
     * @param loader loader that created the data object
     * @exception DataObjectExistsException if there is already a data object
     *    for this primary file
     */
    public DataObject (FileObject pf, DataLoader loader) throws DataObjectExistsException {
        // By registering we'll also get notifications about file changes.
        this (pf, DataObjectPool.getPOOL().register (pf, loader), loader);
    }

    /** Private constructor. At this time the constructor receives
    * the primary file and pool item where it should register itself.
    *
    * @param pf primary file
    * @param item the item to register into
    * @param loader loader that created the data object
    */
    private DataObject (FileObject pf, DataObjectPool.Item item, DataLoader loader) {
        this.item = item;
        this.loader = loader;
        item.setDataObject (this);
    }

    // This method first unregisters the object, then calls method unreferenced.
    // After that it asks the parent folder to regenerate its list of children,
    // so different object is usually created for primary file of this object.
    /** Allows subclasses to discard the object. When an object is discarded,
    * it is released from the list of objects registered in the system.
    * Then the contents of the parent folder (if it still exists) are rescanned, which
    * may result in the creation of a new data object for the primary file.
    * <P>
    * The normal use of this method is to change the type of a data object.
    * Because this would usually only be invoked from
    * the original data object, it is protected.
    */
    protected void dispose () {
        DataObjectPool.Item item = this.item;
        
        if (item != null) {
            item.deregister (true);
            item.setDataObject(null);
            firePropertyChange (PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    }

    /** Setter that allows to destroy this data object. Because such
    * operation can be dangerous and not always possible (if the data object
    * is opened in editor) it can be vetoed. Either by this data object
    * or by any vetoable listener attached to this object (like editor support)
    *
    * @param valid should be false
    * @exception PropertyVetoException if the invalidation has been vetoed
    */
    public void setValid (boolean valid) throws PropertyVetoException {
        if (!valid && isValid ()) {
            markInvalid0 ();
        }
    }
        
    /** Tries to mark the object invalid. Called from setValid or from 
     * MultiDataObject.notifyDeleted
     */
    final void markInvalid0 () throws PropertyVetoException {    
        fireVetoableChange (PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        dispose ();
        setModified(false);
    }

    /** Test whether the data object is still valid and usable.
    * <P>
    * The object can become invalid when it is deleted, its files are deleted, or
    * {@link #dispose} is called.
    * <P>
    * When the validity of the object changes a property change event is fired, so
    * anyone can listen and be notified when the object is deleted/disposed.
    */
    public final boolean isValid () {
        return item.isValid ();
    }



    /** Get the loader that created this data object.
    * @return the data loader
    */
    public final DataLoader getLoader () {
        return loader;
    }

    /** Mark all contained files as belonging to this loader.
     * If the files are rescanned (e.g. after a disposal), the current data loader will be given preference.
    */
    protected final void markFiles () throws IOException {
        Iterator en = files ().iterator ();
        while (en.hasNext ()) {
            FileObject fo = (FileObject)en.next ();
            loader.markFile (fo);
        }
    }

    /** Get all contained files.
     * These file objects should ideally have had the {@linkplain FileObject#setImportant important flag} set appropriately.
    * <P>
    * The default implementation returns a set consisting only of the primary file.
    *
    * @return set of files
    */
    public Set<FileObject> files() {
        return java.util.Collections.singleton (getPrimaryFile ());
    }


    /** Get the node delegate. Either {@link #createNodeDelegate creates it} (if it does not
    * already exist) or
    * returns a previously created instance of it.
    * @return the node delegate (without parent) for this data object
    * @see <a href="doc-files/api.html#delegate">Datasystems API - Node Delegates</a>
    */
    public final Node getNodeDelegate () {
        if (! isValid()) {
            Exception e = new IllegalStateException("The data object " + getPrimaryFile() + " is invalid; you may not call getNodeDelegate on it any more; see #17020 and please fix your code"); // NOI18N
            Logger.getLogger(DataObject.class.getName()).log(Level.WARNING, null, e);
        }
        if (nodeDelegate == null) {
            // synchronize on something private, so only one delegate can be created
            // do not synchronize on this, because we could deadlock with
            // subclasses could synchronize too.
            Children.MUTEX.readAccess (new Runnable() {
                public void run() {
                    synchronized(nodeCreationLock) {
                        if (nodeDelegate == null) {
                            nodeDelegate = createNodeDelegate();
                        }
                    }
                }
            });

            // JST: debuging code
            if (nodeDelegate == null) {
                throw new IllegalStateException("DataObject " + this + " has null node delegate"); // NOI18N
            }
        }
        return nodeDelegate;
    }
    
    /** This method allows DataFolder to filter its nodes.
    *
    * @param filter filter for subdata objects
    * @return the node delegate (without parent) the node is new instance
    *   of node and can be inserted to any place in the hierarchy
    */
    Node getClonedNodeDelegate (DataFilter filter) {
        return getNodeDelegate ().cloneNode ();
    }

    /** Access method for node delagate.
     * @return node delegate or null
     */
    Node getNodeDelegateOrNull () {
        return nodeDelegate;
    }

    /** Provides node that should represent this data object.
    * <p>The default implementation creates an instance of {@link DataNode}.
    * Most subclasses will override this method to provide a <code>DataNode</code>
    * (usually subclassed).
    * <P>
    * This method is called only once per data object.
    * <p>It is strongly recommended that the resulting node will, when asked for
    * the cookie <samp>DataObject.class</samp>, return this same data object.
    * <p>It is also recommended that the node:
    * <ol>
    * <li>Base its name on {@link #getName}.
    * <li>Base its display name additionally on {@link DataNode#getShowFileExtensions}.
    * <li>Tune its display name and icon according to {@link org.openide.filesystems.FileSystem.Status}.
    * </ol>
    * @return the node delegate (without parent) for this data object
    * @see <a href="doc-files/api.html#create-delegate">Datasystems API - Creating a node delegate</a>
    */
    protected Node createNodeDelegate () {
        return new DataNode (this, Children.LEAF);
    }

    /** Obtains lock for primary file.
    *
    * @return the lock
    * @exception IOException if taking the lock fails
    */
    protected FileLock takePrimaryFileLock () throws IOException {
        return getPrimaryFile ().lock ();
    }

    /** Package private method to assign template attribute to a file.
    * Used also from FileEntry.
    *
    * @param fo the file
    * @param newTempl is template or not
    * @return true if the value change/false otherwise
    */
    static boolean setTemplate (FileObject fo, boolean newTempl) throws IOException {
        boolean oldTempl = false;

        Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
        if ((o instanceof Boolean) && ((Boolean)o).booleanValue())
            oldTempl = true;
        if (oldTempl == newTempl)
            return false;

        fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? Boolean.TRUE : null));

        return true;
    }

    /** Set the template status of this data object.
    * @param newTempl <code>true</code> if the object should be a template
    * @exception IOException if setting the template state fails
    */
    public final void setTemplate (boolean newTempl) throws IOException {
        if (!setTemplate (getPrimaryFile(), newTempl)) {
            // no change in state
            return;
        }

        firePropertyChange(DataObject.PROP_TEMPLATE,
                           !newTempl ? Boolean.TRUE : Boolean.FALSE,
                           newTempl ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Get the template status of this data object.
    * @return <code>true</code> if it is a template
    */
    public final boolean isTemplate () {
        Object o = getPrimaryFile().getAttribute(PROP_TEMPLATE);
        boolean ret = false;
        if (o instanceof Boolean)
            ret = ((Boolean) o).booleanValue();
        return ret;
    }


    /** Test whether the object may be deleted.
    * @return <code>true</code> if it may
    */
    public abstract boolean isDeleteAllowed ();

    /** Test whether the object may be copied.
    * @return <code>true</code> if it may
    */
    public abstract boolean isCopyAllowed ();

    /** Test whether the object may be moved.
    * @return <code>true</code> if it may
    */
    public abstract boolean isMoveAllowed ();

    /** Test whether the object may create shadows.
     * <p>The default implementation returns <code>true</code>.
    * @return <code>true</code> if it may
    */
    public boolean isShadowAllowed () {
        return true;
    }

    /** Test whether the object may be renamed.
    * @return <code>true</code> if it may
    */
    public abstract boolean isRenameAllowed ();


    /** Test whether the object is modified.
    * @return <code>true</code> if it is modified
    */
    public boolean isModified() {
        return modif;
    }

    /** Set whether the object is considered modified.
     * Also fires a change event.
    * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
    * If the new value is <code>false</code>,
    * the data object is removed from the registry.
    */
    public void setModified(boolean modif) {
        if (this.modif != modif) {
            this.modif = modif;
            if (modif) {
                syncModified.add (this);
            } else {
                syncModified.remove (this);
            }
            firePropertyChange(DataObject.PROP_MODIFIED,
                               !modif ? Boolean.TRUE : Boolean.FALSE,
                               modif ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /** Get help context for this object.
    * @return the help context
    */
    public abstract HelpCtx getHelpCtx ();

    /** Get the primary file for this data object.
     * For example,
    * Java source uses <code>*.java</code> and <code>*.class</code> files but the primary one is
    * always <code>*.java</code>. Please note that two data objects are {@link #equals equivalent} if
    * they use the same primary file.
    * <p><em>Warning:</em> do not call {@link Node#getHandle} or {@link DefaultHandle#createHandle} in this method.
    *
    * @return the primary file
    */
    public final FileObject getPrimaryFile () {
        return item.primaryFile;
    }

    /** Finds the data object for a specified file object.
    * @param fo file object
    * @return the data object for that file
    * @exception DataObjectNotFoundException if the file does not have a
    *   data object
    */
    public static DataObject find (FileObject fo)
    throws DataObjectNotFoundException {
        if (fo == null)
            throw new IllegalArgumentException("Called DataObject.find on null"); // NOI18N
        
        try {
            if (!fo.isValid())
                throw new FileStateInvalidException(fo.toString());
            
            // try to scan directly the pool (holds only primary files)
            DataObject obj = DataObjectPool.getPOOL().find (fo);
            if (obj != null) {
                return obj;
            }

            // try to use the loaders machinery
            DataLoaderPool p = DataLoaderPool.getDefault();
            assert p != null : "No DataLoaderPool found in " + Lookup.getDefault();
            obj = p.findDataObject (fo);
            if (obj != null) {
                return obj;
            }
                
            throw new DataObjectNotFoundException (fo);
        } catch (DataObjectExistsException ex) {
            return ex.getDataObject ();
        } catch (IOException ex) {
            throw (DataObjectNotFoundException) new DataObjectNotFoundException(fo).initCause(ex);
        }
    }

    /** the only instance */
    private static Registry REGISTRY_INSTANCE = new Registry();
    
    /** Get the registry containing all modified objects.
    *
    * @return the registry
    */
    public static Registry getRegistry () {
        return REGISTRY_INSTANCE;
    }

    /** Get the name of the data object.
    * <p>The default implementation uses the name of the primary file.
    * @return the name
    */
    public String getName () {
        return getPrimaryFile ().getName ();
    }

    public String toString () {
        return super.toString () + '[' + getPrimaryFile () + ']';
    }

    /** Get the folder this data object is stored in.
    * @return the folder; <CODE>null</CODE> if the primary file
    *   is the {@link FileObject#isRoot root} of its filesystem
    */
    public final DataFolder getFolder () {
        FileObject fo = getPrimaryFile ().getParent ();
        // could throw IllegalArgumentException but only if fo is not folder
        // => then there is a bug in filesystem implementation
        return fo == null ? null : DataFolder.findFolder (fo);
    }

    /** Copy this object to a folder. The copy of the object is required to
    * be deletable and movable.
    * <p>An event is fired, and atomicity is implemented.
    * @param f the folder to copy the object to
    * @exception IOException if something went wrong
    * @return the new object
    */
    public final DataObject copy (final DataFolder f) throws IOException {
        final DataObject[] result = new DataObject[1];
        invokeAtomicAction (f.getPrimaryFile (), new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] = handleCopy (f);
                                }
                            }, null);
        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.COPY
        );
        return result[0];
    }

    /** Copy this object to a folder (implemented by subclasses).
    * @param f target folder
    * @return the new data object
    * @exception IOException if an error occures
    */
    protected abstract DataObject handleCopy (DataFolder f) throws IOException;

    /** Copy this object to a folder under a different name and file extension.
     * The copy of the object is required to be deletable and movable.
     * <p>An event is fired, and atomicity is implemented.
     * @param f the folder to copy the object to
     * @exception IOException if something went wrong
     * @return the new object
     * @since 6.3
     */
    final DataObject copyRename (final DataFolder f, final String name, final String ext) throws IOException {
        final DataObject[] result = new DataObject[1];
        invokeAtomicAction (f.getPrimaryFile (), new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] = handleCopyRename (f, name, ext);
                                }
                            }, null);
        fireOperationEvent (
            new OperationEvent(result[0]), OperationEvent.CREATE
        );
        return result[0];
    }
    /** 
     * Copy and rename this object to a folder (implemented by subclasses).
     * @param f target folder
     * @param name new file name
     * @param ext new file extension
     * @return the new data object
     * @exception IOException if an error occures or the file cannot be copied/renamed
     * @since 6.3
     */
    protected DataObject handleCopyRename (DataFolder f, String name, String ext) throws IOException {
        throw new IOException( "Unsupported operation" ); //NOI18N
    }

    /** Delete this object.
     * <p>Events are fired and atomicity is implemented.
    * @exception IOException if an error occures
    */
    public final void delete () throws IOException {
        // the object is ready to be closed
        invokeAtomicAction (getPrimaryFile (), new FileSystem.AtomicAction () {
                public void run () throws IOException {
                    handleDelete ();
                    item.deregister(false);
                    item.setDataObject(null);
                }
            }, synchObject());
        firePropertyChange (PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        fireOperationEvent (new OperationEvent (this), OperationEvent.DELETE);
    }

    /** Delete this object (implemented by subclasses).
    * @exception IOException if an error occures
    */
    protected abstract void handleDelete () throws IOException;


    /** Rename this object.
     * <p>Events are fired and atomicity is implemented.
    *
    * @param name the new name
    *
    * @exception IOException if an error occurs
    */
    public final void rename (String name) throws IOException {
        if (name != null && name.trim ().length ()==0) {
            IllegalArgumentException iae = new IllegalArgumentException (this.getName ());
            String msg = NbBundle.getMessage (DataObject.class,
                                  "MSG_NotValidName", getName ()); // NOI18N
            Exceptions.attachLocalizedMessage(iae, msg);
            throw iae;
        }
        
        
        class Op implements FileSystem.AtomicAction {
            FileObject oldPf;
            FileObject newPf;
            
            String oldName;
            String newName;
            public void run() throws IOException {
                oldName = getName ();

                if (oldName.equals (newName)) return; // the new name is the same as the old one

                oldPf = getPrimaryFile ();
                newPf = handleRename (newName);
                if (oldPf != newPf)
                    item.changePrimaryFile (newPf);
                 newName = getName ();
            }
        }
        
        // executes atomic action with renaming
        Op op = new Op();
        op.newName = name;
        invokeAtomicAction (getPrimaryFile().getParent(), op, synchObject());

        if (op.oldName.equals (op.newName)) {
            return; // the new name is the same as the old one
        }
        
        if (op.oldPf != op.newPf) {
            firePropertyChange (PROP_PRIMARY_FILE, op.oldPf, op.newPf);
        }
        firePropertyChange (PROP_NAME, op.oldName, op.newName);
        firePropertyChange (PROP_FILES, null, null);
        
        fireOperationEvent (new OperationEvent.Rename (this, op.oldName), OperationEvent.RENAME);
    }

    /** Rename this object (implemented in subclasses).
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleRename (String name) throws IOException;

    /** Move this object to another folder.
     * <p>An event is fired and atomicity is implemented.
    * @param df folder to move object to
    * @exception IOException if an error occurs
    */
    public final void move (final DataFolder df) throws IOException {
        class Op implements FileSystem.AtomicAction {
            FileObject old;
            public void run () throws IOException {
                if ((getFolder () == null)) return; // cannot move filesystem root
                if (df.equals (getFolder ())) return; // if the destination folder is the same as the current one ==>> do nothing

                // executes atomic action for moving
                old = getPrimaryFile ();
                FileObject mf = handleMove (df);
                item.changePrimaryFile (mf);
            }
        }
        Op op = new Op();
        
        invokeAtomicAction (df.getPrimaryFile(), op, synchObject());
        
        firePropertyChange (PROP_PRIMARY_FILE, op.old, getPrimaryFile ());
        fireOperationEvent (
            new OperationEvent.Move (this, op.old), OperationEvent.MOVE
        );
    }

    /** Move this object to another folder (implemented in subclasses).
    *
    * @param df target data folder
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    protected abstract FileObject handleMove (DataFolder df) throws IOException;

    /** Creates shadow for this object in specified folder (overridable in subclasses).
     * <p>The default
    * implementation creates a reference data shadow and pastes it into
    * the specified folder.
    *
    * @param f the folder to create a shortcut in
    * @return the shadow
    */
    protected DataShadow handleCreateShadow (DataFolder f) throws IOException {
        return DataShadow.create (f, this);
    }

    /** Creates shadow for this object in specified folder.
     * <p>An event is fired and atomicity is implemented.
    *
    * @param f the folder to create shortcut in
    * @return the shadow
    */
    public final DataShadow createShadow (final DataFolder f) throws IOException {
        final DataShadow[] result = new DataShadow[1];

        invokeAtomicAction (f.getPrimaryFile (), new FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    result[0] =  handleCreateShadow (f);
                                }
                            }, null);
        fireOperationEvent (
            new OperationEvent.Copy (result[0], this), OperationEvent.SHADOW
        );
        return result[0];
    }

    /** Create a new object from template (with a name depending on the template).
    *
    * @param f folder to create object in
    * @return new data object based on this one
    * @exception IOException if an error occured
    * @see #createFromTemplate(DataFolder,String)
    */
    public final DataObject createFromTemplate (DataFolder f)
    throws IOException {
        return createFromTemplate (f, null);
    }

    /** Create a new object from template.
    * Asks {@link #handleCreateFromTemplate}.
    *
    * @param f folder to create object in
    * @param name name of object that should be created, or <CODE>null</CODE> if the
    *    name should be same as that of the template (or otherwise mechanically generated)
    * @return the new data object
    * @exception IOException if an error occured
    */
    public final DataObject createFromTemplate (
        final DataFolder f, final String name
    ) throws IOException {
        return createFromTemplate(f, name, Collections.<String,Object>emptyMap());
    }
    
    /** More generic way how to instantiate a {@link DataObject}. One can
    * not only specify its name, but also pass a map of parameters that
    * can influence the copying of the stream.
    *
    * @param f folder to create object in
    * @param name name of object that should be created, or <CODE>null</CODE> if the
    *    name should be same as that of the template (or otherwise mechanically generated)
    * @param parameters map of named objects that are going to be used when
    *    creating the new object
    * @return the new data object
    * @exception IOException if an error occured
    * @since 6.1
    */
    public final DataObject createFromTemplate(
        final DataFolder f, final String name, final Map<String,? extends Object> parameters
    ) throws IOException {
        CreateAction create = new CreateAction(this, f, name, parameters);
        invokeAtomicAction (f.getPrimaryFile (), create, null);
        fireOperationEvent (
            new OperationEvent.Copy (create.result, this), OperationEvent.TEMPL
        );
        return create.result;
    }

    /** Create a new data object from template (implemented in subclasses).
     * This method should
    * copy the content of the template to the destination folder and assign a new name
    * to the new object.
    *
    * @param df data folder to create object in
    * @param name name to give to the new object (or <CODE>null</CODE>
    *    if the name should be chosen according to the template)
    * @return the new data object
    * @exception IOException if an error occured
    */
    protected abstract DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException;


    /** Fires operation event to data loader pool.
    * @param ev the event
    * @param type OperationEvent.XXXX constant
    */
    private static void fireOperationEvent (OperationEvent ev, int type) {
        DataLoaderPool.getDefault().fireOperationEvent (ev, type);
    }

    /** Provide object used for synchronization. 
     * @return <CODE>this</CODE> in DataObject implementation. Other DataObjects
     *     (MultiDataObject) can rewrite this method and return own synch object.
     */
    Object synchObject() {
        return synchObject;
    }
    
    /** Invokes atomic action. 
     */
    private void invokeAtomicAction (FileObject target, final FileSystem.AtomicAction action, final Object lockTheSession) throws IOException {
        FileSystem.AtomicAction toRun;
        
        if (lockTheSession != null) {
            class WrapRun implements FileSystem.AtomicAction {
                public void run() throws IOException {
                    synchronized (lockTheSession) {
                        action.run();
                    }
                }
            }
            toRun = new WrapRun();
        } else {
            toRun = action;
        }
        
        if (Boolean.getBoolean ("netbeans.dataobject.insecure.operation")) {
            DataObjectPool.getPOOL ().runAtomicActionSimple (target, toRun);
            return;
        }
            
        
        if (this instanceof DataFolder) {
            // action is slow
            DataObjectPool.getPOOL ().runAtomicActionSimple (target, toRun);
        } else {
            // it is quick, make it block DataObject recognition
            DataObjectPool.getPOOL ().runAtomicAction (target, toRun);
        }
    }
     
    
    //
    // Property change support
    //

    /** Add a property change listener.
     * @param l the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        synchronized (listenersMethodLock) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    /** Remove a property change listener.
     * @param l the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    /** Fires property change notification to all listeners registered via
    * {@link #addPropertyChangeListener}.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    */
    protected final void firePropertyChange (String name, Object oldValue, Object newValue) {
        if (changeSupport != null)
            changeSupport.firePropertyChange(name, oldValue, newValue);
    }

    //
    // Property change support
    //

    /** Add a listener to vetoable changes.
     * @param l the listener to add
     * @see #PROP_VALID
    */
    public void addVetoableChangeListener (VetoableChangeListener l) {
        synchronized (listenersMethodLock) {
            if (vetoableChangeSupport == null)
                vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        vetoableChangeSupport.addVetoableChangeListener(l);
    }

    /** Add a listener to vetoable changes.
     * @param l the listener to remove
     * @see #PROP_VALID
    */
    public void removeVetoableChangeListener (VetoableChangeListener l) {
        if (vetoableChangeSupport != null)
            vetoableChangeSupport.removeVetoableChangeListener(l);
    }

    /** Fires vetoable change notification.
    *
    * @param name of property
    * @param oldValue old value
    * @param newValue new value
    * @exception PropertyVetoException if the change has been vetoed
    */
    protected final void fireVetoableChange (String name, Object oldValue, Object newValue)
        throws PropertyVetoException
    {
        if (vetoableChangeSupport != null)
            vetoableChangeSupport.fireVetoableChange(name, oldValue, newValue);
    }

    //
    // Cookie
    //

    /** Obtain a cookie from the data object.
    * May be overridden by subclasses to extend the behaviour of
    * data objects.
    * <P>
    * The default implementation tests if this object is of the requested class and
    * if so, returns it.
    * <p>
    * <b>Warning:</b> the {@link #getCookie} method and {@link #getLookup}
    * method are ment to be interchangable - e.g. if you override one of them
    * be sure to override also the other and try as much as possible to 
    * keep the same content in each of them. The default implementation tries
    * to do that as much as possible.
    *
    * @param c class of requested cookie
    * @return a cookie or <code>null</code> if such cookies are not supported
    */
    public <T extends Node.Cookie> T getCookie(Class<T> c) {
        if (c.isInstance (this)) {
            return c.cast(this);
        }
        return null;
    }
    
    /** Represents a context of the data object. This method is a more 
     * general replacement for {@link #getCookie} and should preferably
     * be used instead of the old method. The default implementation 
     * inside a data object 
     * returns the <code>getNodeDelegate().getLookup()</code>.
     * <p>
     * <b>Warning:</b> the {@link #getCookie} method and {@link #getLookup}
     * method are ment to be interchangable - e.g. if you override one of them
     * be sure to override also the other and try as much as possible to 
     * keep the same content in each of them. The default implementation tries
     * to do that as much as possible.
     * 
     * @return lookup representing this data object and its content
     * @since 6.0
     */
    public Lookup getLookup() {
        Class<?> c = getClass();
        if (warnedClasses.add(c)) {
            LOG.warning("Should override getLookup() in " + c + ", e.g.: [MultiDataObject.this.]getCookieSet().getLookup()");
        }
        if (isValid()) {
            return getNodeDelegate().getLookup();
        } else {
            // Fallback for invalid DO; at least provide something reasonable.
            return createNodeDelegate().getLookup();
        }
    }
    private static final Set<Class<?>> warnedClasses = Collections.synchronizedSet(new WeakSet<Class<?>>());
    
    /** When a request for a cookie is done on a DataShadow of this DataObject
     * this methods gets called (by default) so the DataObject knows which
     * DataShadow is asking and extract some information from the shadow itself.
     * <P>
     * Subclasses can override this method with better logic, but the default
     * implementation just delegates to <code>getCookie (Class)</code>.
     *
     * @param clazz class to search for
     * @param shadow the shadow for which is asking
     * @return the cookie or <code>null</code>
     *
     * @since 1.16
     */
    protected <T extends Node.Cookie> T getCookie(DataShadow shadow, Class<T> clazz) {
        return getCookie (clazz);
    }

    // =======================
    //  Serialization methods
    //

    /** The Serialization replacement for this object stores the primary file instead.
     * @return a replacement
    */
    public Object writeReplace () {
        return new Replace (this);
    }


    /** The default replace for the data object
    */
    private static final class Replace extends Object implements Serializable {
        /** the primary file */
        private FileObject fo;
        /** the object to return */
        private transient DataObject obj;

        private static final long serialVersionUID =-627843044348243058L;
        /** Constructor.
        * @param obj the object to use
        */
        public Replace (DataObject obj) {
            this.obj = obj;
            this.fo = obj.getPrimaryFile ();
        }

        public Object readResolve () {
            return obj;
        }

        /** Read method */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            ois.defaultReadObject ();
            if (fo == null) {
                throw new java.io.FileNotFoundException ();
            }
            // DataObjectNotFoundException extends IOException:
            obj = DataObject.find(fo);
        }
    }

    /** Getter for a text from resource bundle.
    */
    static String getString (String name) {
        return NbBundle.getMessage (DataObject.class, name);
    }
    
    /** Interface for objects that can contain other data objects.
     * For example DataFolder and DataShadow implement this interface
     * to allow others to access the contained objects in uniform maner
     */
    public static interface Container extends Node.Cookie {
        /** Name of property that holds children of this container. */
        public static final String PROP_CHILDREN = "children"; // NOI18N
        
        /** @return the array of contained objects
         */
        public DataObject[] getChildren ();
        
        /** Adds a listener.
         * @param l the listener
         */
        public void addPropertyChangeListener (PropertyChangeListener l);
        
        /** Removes property change listener.
         * @param l the listener
         */
        public void removePropertyChangeListener (PropertyChangeListener l);
    }

    /** Registry of modified data objects.
     * The registry permits attaching of a change listener
    * to be informed when the count of modified objects changes.
    */
    public static final class Registry extends Object {

        /** Private constructor */
        private Registry () {
        }

        /** Add new listener to changes in the set of modified objects.
        * @param chl listener to add
        */
        public void addChangeListener (final ChangeListener chl) {
            modified.addChangeListener(chl);
        }

        /** Remove a listener to changes in the set of modified objects.
        * @param chl listener to remove
        */
        public void removeChangeListener (final ChangeListener chl) {
            modified.removeChangeListener(chl);
        }

        /** Get a set of modified data objects.
        * @return an unmodifiable set of data objects
        */
        public Set<DataObject> getModifiedSet() {
            synchronized (syncModified) {
                return new HashSet<DataObject>(syncModified);
            }
        }

        /** Get modified objects.
        * @return array of objects
        */
        public DataObject[] getModified () {
            return syncModified.toArray(new DataObject[0]);
        }
    }

    private static final class ModifiedRegistry extends HashSet<DataObject> {
        static final long serialVersionUID =-2861723614638919680L;
        
        private final ChangeSupport cs = new ChangeSupport(this);

        ModifiedRegistry() {}

        /** Adds new listener.
        * @param chl new listener
        */
        public final void addChangeListener(final ChangeListener chl) {
            cs.addChangeListener(chl);
        }

        /** Removes listener from the listener list.
        * @param chl listener to remove
        */
        public final void removeChangeListener(final ChangeListener chl) {
            cs.removeChangeListener(chl);
        }

        /***** overriding of methods which change content in order to notify
        * listeners about the content change */
        @Override
        public boolean add (DataObject o) {
            boolean result = super.add(o);
            if (result) {
                cs.fireChange();
            }
            return result;
        }

        @Override
        public boolean remove (Object o) {
            boolean result = super.remove(o);
            if (result) {
                cs.fireChange();
            }
            return result;
        }

    }  // end of ModifiedRegistry inner class

    /** A.N. - profiling shows that MultiLoader.checkFiles() is called too often
    * This method is part of the fix - empty for DataObject.
    */
    void recognizedByFolder() {
    }
    
    // This methods are called by DataObjectPool whenever the primary file
    // gets changed. The Pool listens on the whole FS thus reducing
    // the number of individual listeners created/registered.
    void notifyFileRenamed(FileRenameEvent fe) {
        if (fe.getFile ().equals (getPrimaryFile ())) {
            firePropertyChange(PROP_NAME, fe.getName(), getName());
        }
    }

    void notifyFileDeleted(FileEvent fe) {
    }

    void notifyFileChanged(FileEvent fe) {
    }
    
    void notifyFileDataCreated(FileEvent fe) {
    }
    
    void notifyAttributeChanged(FileAttributeEvent fae) {
       if (! EA_ASSIGNED_LOADER.equals(fae.getName())) {
            // We are interested only in assigned loader
            return;
        }
        FileObject f = fae.getFile();
        if (f != null) {
            String attrFromFO = (String)f.getAttribute(EA_ASSIGNED_LOADER);
            if (attrFromFO == null || (! attrFromFO.equals(getLoader().getClass().getName()))) {
                Set<FileObject> single = new HashSet<FileObject>(); // Collections.singleton is r/o, this must be writable
                single.add(f);
                if (!DataObjectPool.getPOOL().revalidate(single).isEmpty()) {
                    LOG.info("It was not possible to invalidate data object: " + this); // NOI18N
                } else {
                    // we need to refresh parent folder if it is there 
                    // this should be covered by DataLoaderPoolTest.testChangeIsAlsoReflectedInNodes
                    FolderList.changedDataSystem (f.getParent());
                }
            }
        }
    }
    static final class CreateAction implements FileSystem.AtomicAction {
        public DataObject result;
        private String name;
        private DataFolder f;
        private DataObject orig;
        private Map<String, ? extends Object> param;
        
        private static ThreadLocal<CreateAction> CURRENT = new ThreadLocal<CreateAction>();
        
        public CreateAction(DataObject orig, DataFolder f, String name, Map<String, ? extends Object> param) {
            this.orig = orig;
            this.f = f;
            this.name = name;
            this.param = param;
        }
        
        public void run () throws IOException {
            DataFolder prevFold = DataObjectEncodingQueryImplementation.enterIgnoreTargetFolder(f);
            CreateAction prev = CURRENT.get();
            try {
                CURRENT.set(this);
                result = orig.handleCreateFromTemplate(f, name);
            } finally {
                DataObjectEncodingQueryImplementation.exitIgnoreTargetFolder(prevFold);
                CURRENT.set(prev);
            }
        }
        
        public static Map<String,Object> findParameters(String name, String ext) {
            CreateAction c  = CURRENT.get();
            if (c == null) {
                return Collections.emptyMap();
            }
            HashMap<String,Object> all = new HashMap<String,Object>();
            for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
                Map<String,? extends Object> map = provider.attributesFor(c.orig, c.f, c.name);
                if (map != null) {
                    for (Map.Entry<String,? extends Object> e : map.entrySet()) {
                        all.put(e.getKey(), e.getValue());
                    }
                }
            }
            if (c.param != null) {
                for (Map.Entry<String,? extends Object> e : c.param.entrySet()) {
                    all.put(e.getKey(), e.getValue());
                }
            }
            
            if (!all.containsKey("name") && name != null) { // NOI18N
                all.put("name", name); // NOI18N
            }
            if (!all.containsKey("user")) { // NOI18N
                all.put("user", System.getProperty("user.name")); // NOI18N
            }
            Date d = new Date();
            if (!all.containsKey("date")) { // NOI18N
                all.put("date", DateFormat.getDateInstance().format(d)); // NOI18N
            }
            if (!all.containsKey("time")) { // NOI18N
                all.put("time", DateFormat.getTimeInstance().format(d)); // NOI18N
            }
            
            return Collections.unmodifiableMap(all);
        }
        
        public static Map<String,Object> enhanceParameters(Map<String,Object> old, String name, String ext) {
            HashMap<String,Object> all = new HashMap<String,Object>(old);
            if (!all.containsKey("nameAndExt") && name != null) { // NOI18N
                if (ext != null && ext.length() > 0) {
                    all.put("nameAndExt", name + '.' + ext); // NOI18N
                } else {
                    all.put("nameAndExt", name); // NOI18N
                }
            }
            return Collections.unmodifiableMap(all);
        }
        
    } // end of CreateAction
}
