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

package org.openide.loaders;


import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.Action;
import org.netbeans.modules.openide.loaders.UIException;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/** Standard node representing a data object.
*
* @author Jaroslav Tulach
*/
public class DataNode extends AbstractNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** DataObject of this node. */
    private DataObject obj;

    /** property change listener */
    private PropL propL;

    /** should file extensions be displayed? */
    private static boolean showFileExtensions = true;

    /** Create a data node with the given children set for the given data object.
    * @param obj object to work with
    * @param ch children container for the node
    * @see #getShowFileExtensions
    */
    public DataNode (DataObject obj, Children ch) {
        this(obj, ch, null);
    }

    /** Create a data node for a given data object.
    * The provided children object will be used to hold all child nodes.
    * The name is always set to the base name of the primary file;
    * the display name may instead be set to the base name with extension.
    * @param obj object to work with
    * @param ch children container for the node
    * @param lookup the lookup to provide content of {@link #getLookup}
    *   and also {@link #getCookie}
    * @see #getShowFileExtensions
    *
    * @since 5.6
    * @author Libor Kotouc
    */
    public DataNode (DataObject obj, Children ch, Lookup lookup) {
        super (ch, lookup);
        this.obj = obj;

        propL = new PropL ();

        obj.addPropertyChangeListener (org.openide.util.WeakListeners.propertyChange (propL, obj));

        super.setName (obj.getName ());
        updateDisplayName ();
    }

    private void updateDisplayName () {
        FileObject prim = obj.getPrimaryFile ();
        String newDisplayName;
        
        if (prim.isRoot()) {
            // Special case - getName{,Ext} will just return "".
            // Used to be handled by org.netbeans.core.RootFolderNode
            // but might as well do it here.
            // XXX replace with #37549
            File f = FileUtil.toFile(prim);
            if (f == null) {
                // Check for a JAR root explicitly.
                FileObject archiveFile = FileUtil.getArchiveFile(prim);
                if (archiveFile != null) {
                    f = FileUtil.toFile(archiveFile);
                }
            }
            if (f != null) {
                // E.g. /tmp/foo or /tmp/foo.jar
                newDisplayName = f.getAbsolutePath();
            } else {
                try {
                    // E.g. http://webdavhost.nowhere.net/mystuff/
                    newDisplayName = prim.getURL().toExternalForm();
                } catch (FileStateInvalidException e) {
                    // Should not happen in practice.
                    newDisplayName = "???"; // NOI18N
                }
            }
        } else if (showFileExtensions || obj instanceof DataFolder || obj instanceof DefaultDataObject) {
            newDisplayName = prim.getNameExt();
        } else {
            newDisplayName = prim.getName ();
        }

        if (displayFormat != null)
            setDisplayName (displayFormat.format (new Object[] { newDisplayName }));
        else
            setDisplayName (newDisplayName);
    }

    /** Get the represented data object.
     * @return the data object
    */
    public DataObject getDataObject() {
        return obj;
    }

    /** Changes the name of the node and may also rename the data object.
    * If the object is renamed and file extensions are to be shown,
    * the display name is also updated accordingly.
    *
    * @param name new name for the object
    * @param rename rename the data object?
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name, boolean rename) {
        try {
            if (rename) {
                obj.rename (name);
            }

            super.setName (name);
            updateDisplayName ();
        } catch (IOException ex) {
            String msg = null;
            if ((ex.getLocalizedMessage() == null) || 
                (ex.getLocalizedMessage().equals(ex.getMessage()))) {
                msg = NbBundle.getMessage (DataNode.class, "MSG_renameError", getName (), name); // NOI18N
            } else {
                msg = ex.getLocalizedMessage();
            }
            
            RuntimeException e = new IllegalArgumentException();
            UIException.annotateUser(e, null, msg, ex, null);
            throw e;
        }
    }

    /* Rename the data object.
    * @param name new name for the object
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name) {
        setName (name, true);
    }


    /** Get the display name for the node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
     * @return the desired name
    */
    public String getDisplayName () {
        String s = super.getDisplayName ();

        try {
            s = obj.getPrimaryFile ().getFileSystem ().getStatus ().annotateName (s, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return s;
    }

     
     /** Get a display name formatted using the limited HTML subset supported
      * by <code>HtmlRenderer</code>.  If the underlying 
      * <code>FileSystem.Status</code> is an instance of HmlStatus,
      * this method will return non-null if status information is added.
      *
      * @return a string containing compliant HTML markup or null
      * @see org.openide.awt.HtmlRenderer
      * @see org.openide.nodes.Node#getHtmlDisplayName
      * @since 4.13 
      */
     public String getHtmlDisplayName() {
         try {
             FileSystem.Status stat = 
                 obj.getPrimaryFile().getFileSystem().getStatus();
             if (stat instanceof FileSystem.HtmlStatus) {
                 FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;
                 
                 String result = hstat.annotateNameHtml (
                     super.getDisplayName(), new LazyFilesSet());
                 
                 //Make sure the super string was really modified
                 if (!super.getDisplayName().equals(result)) {
                     return result;
                 }
             }
         } catch (FileStateInvalidException e) {
             //do nothing and fall through
         }
         return super.getHtmlDisplayName();
     }    

    /** Get the displayed icon for this node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
     * @param type the icon type from {@link java.beans.BeanInfo}
     * @return the desired icon
    */
    public java.awt.Image getIcon (int type) {
        java.awt.Image img = super.getIcon (type);

        try {
            img = obj.getPrimaryFile ().getFileSystem ().getStatus ().annotateIcon (img, type, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return img;
    }

    /** Get the displayed icon for this node.
    * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * Subclassers overriding this method should consider the recommendations
     * in {@link DataObject#createNodeDelegate}.
    * @param type the icon type from {@link java.beans.BeanInfo}
    * @return the desired icon
    */
    public java.awt.Image getOpenedIcon (int type) {
        java.awt.Image img = super.getOpenedIcon(type);

        try {
            img = obj.getPrimaryFile ().getFileSystem ().getStatus ().annotateIcon (img, type, new LazyFilesSet());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return img;
    }
    
    public HelpCtx getHelpCtx () {
        return obj.getHelpCtx ();
    }

    /** Indicate whether the node may be renamed.
    * @return tests {@link DataObject#isRenameAllowed}
    */
    public boolean canRename () {
        return obj.isRenameAllowed ();
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy () {
        return obj.isDeleteAllowed ();
    }

    /* Destroyes the node
    */
    public void destroy () throws IOException {
        if (obj.isDeleteAllowed ()) {
            obj.delete ();
        }
        super.destroy ();
    }

    /* Returns true if this object allows copying.
    * @returns true if this object allows copying.
    */
    public boolean canCopy () {
        return obj.isCopyAllowed ();
    }

    /* Returns true if this object allows cutting.
    * @returns true if this object allows cutting.
    */
    public boolean canCut () {
        return obj.isMoveAllowed ();
    }

    /** This method returns null to signal that actions
    * provide by DataLoader.getActions should be returned from 
    * method getActions. If overriden to provide some actions,
    * then these actions will be preferred to the loader's ones.
    *
    * @return null
     * @deprecated Use {@link #getActions(boolean)} or do nothing and let the
     *             data loader specify actions.
    */
    @Deprecated
    protected SystemAction[] createActions () {
        return null;
    }

    /** Get actions for this data object.
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    public Action[] getActions (boolean context) {
        if (systemActions == null) {
            systemActions = createActions ();
        }

        if (systemActions != null) {
            return systemActions;
        }

        return obj.getLoader ().getSwingActions ();
    }

    /** Get actions for this data object.
    * @deprecated Use getActions(boolean)
    * @return array of actions or <code>null</code>
    */
    @Deprecated
    public SystemAction[] getActions () {
        if (systemActions == null) {
            systemActions = createActions ();
        }

        if (systemActions != null) {
            return systemActions;
        }

        return obj.getLoader ().getActions ();
    }

    
    /** Get default action. In the current implementation the 
    *<code>null</code> is returned in case the underlying data 
    * object is a template. The templates should not have any default 
    * action.
    * @return no action if the underlying data object is a template. 
    *    Otherwise the abstract node's default action is returned, if <code>null</code> then
    *    the first action returned from getActions (false) method is used.
    */
    public Action getPreferredAction () {
        if (obj.isTemplate ()) {
            return null;
        } else {
            Action action = super.getPreferredAction ();
            if (action != null) {
                return action;
            }
            Action[] arr = getActions(false);
            if (arr != null && arr.length > 0) {
                return arr[0];
            }
            return null;
        }
    }

    /** Get a cookie.
     * First of all {@link DataObject#getCookie} is
    * called. If it produces non-<code>null</code> result, that is returned.
    * Otherwise the superclass is tried.
    * Subclassers overriding this method should consider the recommendations
    * in {@link DataObject#createNodeDelegate}. Since version 5.6, if 
    * non-null {@link Lookup} is passed to the constructor, then this 
    * method directly delegates to <a href="@org-openide-nodes@/org/openide/nodes/Node.html">super.getCookie</a> and does
    * not query data object at all. This is supposed to provide consistency
    * between results in <code>getLookup().lookup</code> and <code>getCookie</code>.
    *
    * @return the cookie or <code>null</code>
    */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        if (ownLookup()) {
            return super.getCookie(cl);
        }
        T c = obj.getCookie(cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /* Initializes sheet of properties. Allow subclasses to
    * overwrite it.
    * @return the default sheet to use
    */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = createNameProperty (obj);
        ss.put (p);

        FileObject fo = getDataObject().getPrimaryFile();
        if (couldBeTemplate(fo) && fo.canWrite()) {
            try {            
                p = new PropertySupport.Reflection<Boolean>(obj, Boolean.TYPE, "isTemplate", "setTemplate"); // NOI18N
                p.setName(DataObject.PROP_TEMPLATE);
                p.setDisplayName(DataObject.getString("PROP_template"));
                p.setShortDescription(DataObject.getString("HINT_template"));
                ss.put(p);
            } catch (Exception ex) {
                throw new InternalError();
            }
        }

        if (fo.isData()) {
            ss.put(new AllFilesProperty());
            ss.put(new SizeProperty());
            ss.put(new LastModifiedProperty());
        }

        return s;
    }
    
    private static boolean couldBeTemplate(FileObject fo) {
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException e) {
            return false;
        }
        return fs.isDefault() && fo.getPath().startsWith("Templates/"); // NOI18N
    }
    
    /**
     * A property with a list of all contained files.
     * Sorted to first show primary file, then all secondary files alphabetically.
     * Shows absolute file path or the closest equivalent.
     */
    private final class AllFilesProperty extends PropertySupport.ReadOnly<String[]> {
        
        public AllFilesProperty() {
            super(DataObject.PROP_FILES, String[].class,
                  DataObject.getString("PROP_files"), DataObject.getString("HINT_files"));
        }
       
        public String[] getValue() {
            Set<FileObject> files = obj.files();
            FileObject primary = obj.getPrimaryFile();
            String[] res = new String[files.size()];
            assert files.contains(primary);

            int i=1;
            for (Iterator<FileObject> it = files.iterator(); it.hasNext(); ) {
                FileObject next = it.next();
                res[next == primary ? 0 : i++] = name(next);
            }
            
            Arrays.sort(res, 1, res.length);
            return res;
        }
        
        private String name(FileObject fo) {
            return FileUtil.getFileDisplayName(fo);
        }
        
    }
    
    private final class SizeProperty extends PropertySupport.ReadOnly<Long> {
        
        public SizeProperty() {
            super("size", Long.TYPE, DataObject.getString("PROP_size"), DataObject.getString("HINT_size"));
        }
        
        public Long getValue() {
            return new Long(getDataObject().getPrimaryFile().getSize());
        }
        
    }
    
    private final class LastModifiedProperty extends PropertySupport.ReadOnly<Date> {
        
        public LastModifiedProperty() {
            super("lastModified", Date.class, DataObject.getString("PROP_lastModified"), DataObject.getString("HINT_lastModified"));
        }
        
        public Date getValue() {
            return getDataObject().getPrimaryFile().lastModified();
        }
        
    }
    
    /** Copy this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one copy flavor
    * @throws IOException if it could not copy
    * @see org.openide.nodes.NodeTransfer
    */
    public Transferable clipboardCopy () throws IOException {
        ExTransferable t = ExTransferable.create (super.clipboardCopy ());
        t.put (LoaderTransfer.transferable (
            getDataObject (), 
            LoaderTransfer.CLIPBOARD_COPY)
        );
        //add extra data flavors to allow dragging the file outside the IDE window
        addExternalFileTransferable( t, getDataObject() );
        return t;
    }

    /** Cut this node to the clipboard.
    *
    * @return {@link org.openide.util.datatransfer.ExTransferable.Single} with one cut flavor
    * @throws IOException if it could not cut
    * @see org.openide.nodes.NodeTransfer
    */
    public Transferable clipboardCut () throws IOException {
        ExTransferable t = ExTransferable.create (super.clipboardCut ());
        t.put (LoaderTransfer.transferable (
            getDataObject (), 
            LoaderTransfer.CLIPBOARD_CUT)
        );
        //add extra data flavors to allow dragging the file outside the IDE window
        addExternalFileTransferable( t, getDataObject() );
        return t;
    }
    
    private void addExternalFileTransferable( ExTransferable t, DataObject d ) {
        FileObject fo = d.getPrimaryFile();
        File file = FileUtil.toFile( fo );
        if( null != file ) {
            //windows & mac
            final ArrayList<File> list = new ArrayList<File>(1);
            list.add( file );
            t.put( new ExTransferable.Single( DataFlavor.javaFileListFlavor ) {
                public Object getData() {
                    return list;
                }
            });
            //linux
            final String uriList = file.toURI().toString() + "\r\n";
            t.put( new ExTransferable.Single( createUriListFlavor() ) {
                public Object getData() {
                    return uriList;
                }
            });
        }
    }

    private DataFlavor createUriListFlavor () {
        try {
            return new DataFlavor("text/uri-list;class=java.lang.String");
        } catch (ClassNotFoundException ex) {
            //cannot happen
            throw new AssertionError(ex);
        }
    }

    /** Creates a name property for given data object.
    */
    static Node.Property createNameProperty (final DataObject obj) {
        Node.Property p = new org.openide.nodes.PropertySupport.ReadWrite<String>(org.openide.loaders.DataObject.PROP_NAME,
                                                        String.class,
                                                        org.openide.loaders.DataObject.getString("PROP_name"),
                                                        org.openide.loaders.DataObject.getString("HINT_name")) {

            public String getValue() {
                return obj.getName();
            }

            public void setValue(String val) throws IllegalAccessException,
                                                              IllegalArgumentException,
                                                              java.lang.reflect.InvocationTargetException {
                if (!canWrite())
                    throw new java.lang.IllegalAccessException();
                if (!(val instanceof java.lang.String))
                    throw new java.lang.IllegalArgumentException();
                try {
                    obj.rename((java.lang.String) val);
                }
                catch (java.io.IOException ex) {
                    java.lang.String msg = null;

                    if ((ex.getLocalizedMessage() == null) ||
                        (ex.getLocalizedMessage().equals(ex.getMessage()))) {
                        msg = org.openide.util.NbBundle.getMessage(org.openide.loaders.DataNode.class,
                                                                   "MSG_renameError",
                                                                   obj.getName(),
                                                                   val);
                    } else {
                        msg = ex.getLocalizedMessage();
                    }
                    UIException.annotateUser(ex, null, msg, null, null);
                    throw new java.lang.reflect.InvocationTargetException(ex);
                }
            }

            public boolean canWrite() {
                return obj.isRenameAllowed();
            }
            // #33296 - suppress custom editor

            public Object getValue(String key) {
                if ("suppressCustomEditor".equals(key)) {
                    return Boolean.TRUE;
                } else {
                    return super.getValue(key);
                }
            }
        };

        return p;
    }

    /** Support for firing property change.
    * @param ev event describing the change
    */
    void fireChange(final PropertyChangeEvent ev) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {

                if (DataFolder.PROP_CHILDREN.equals(ev.getPropertyName())) {
                    // the node is not interested in children changes
                    return;
                }

                if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())) {
                    // the node is not interested in children changes
                    propL.updateStatusListener();
                    setName(obj.getName(), false);
                    return;
                }

                if (DataObject.PROP_NAME.equals(ev.getPropertyName())) {
                    DataNode.super.setName(obj.getName());
                    updateDisplayName();
                }
                if (DataObject.PROP_COOKIE.equals(ev.getPropertyName())) {
                    fireCookieChange();
                    //return;
                } 
        
                // if the DataOjbect is not valid the node should be
                // removed
                if (DataObject.PROP_VALID.equals(ev.getPropertyName())) {
                    Object newVal = ev.getNewValue();
                    if ((newVal instanceof Boolean) && (!((Boolean) newVal).booleanValue())) {
                        fireNodeDestroyed();
                    }
                    return;
                } 
                
                 /*See #31413*/
                List transmitProperties = Arrays.asList(new String[] {
                    DataObject.PROP_NAME, DataObject.PROP_FILES, DataObject.PROP_TEMPLATE});
                if (transmitProperties.contains(ev.getPropertyName())) {
                    firePropertyChange(ev.getPropertyName(), ev.getOldValue(), ev.getNewValue());
                }                
            }
        });
    }

    /** Handle for location of given data object.
    * @return handle that remembers the data object.
    */
    public Node.Handle getHandle () {
        return new ObjectHandle(obj, obj.isValid() ? (this != obj.getNodeDelegate()) : /* to be safe */ true);
    }

    /** Access method to fire icon change.
    */
    final void fireChangeAccess (boolean icon, boolean name) {
        if (name) {
            fireDisplayNameChange (null, null);
        }
        if (icon) {
            fireIconChange ();
        }
    }

    /** Determine whether file extensions should be shown by default.
    * By default, no.
    * @return <code>true</code> if so
    */
    public static boolean getShowFileExtensions () {
        return showFileExtensions;
    }

    /** Set whether file extensions should be shown by default.
    * @param s <code>true</code> if so
    */
    public static void setShowFileExtensions (boolean s) {
        boolean refresh = ( showFileExtensions != s );
        showFileExtensions = s;
        
        if ( refresh ) {
            // refresh current nodes display name
            RequestProcessor.getDefault().post(new Runnable() {
                public void run () { 
                    Iterator it = DataObjectPool.getPOOL().getActiveDataObjects();
                    while ( it.hasNext() ) {
                        DataObject obj = ((DataObjectPool.Item)it.next()).getDataObjectOrNull();
                        if ( obj != null && obj.getNodeDelegate() instanceof DataNode ) {
                            ((DataNode)obj.getNodeDelegate()).updateDisplayName();            
                        }
                    }        
                }
            }, 300, Thread.MIN_PRIORITY);                    
        }        
        
    }

    private static Class defaultLookup;
    /** Returns true if this node is using own lookup and not the standard one.
     */
    private boolean ownLookup() {
        if (defaultLookup == null) {
            try {
                defaultLookup = Class.forName("org.openide.nodes.NodeLookup", false, Node.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        return !defaultLookup.isInstance(getLookup());
    }
    
    /** Request processor task to update a bunch of names/icons.
     * Potentially faster to do many nodes at once; see #16478.
     */
    private static RequestProcessor.Task refreshNamesIconsTask = null;
    /** nodes which should be refreshed */
    private static Set<DataNode> refreshNameNodes = null;
    private static Set<DataNode> refreshIconNodes = null;
    /** whether the task is current scheduled and will still look in above sets */
    private static boolean refreshNamesIconsRunning = false;
    private static final Object refreshNameIconLock = "DataNode.refreshNameIconLock"; // NOI18N
    
    /** Property listener on data object that delegates all changes of
    * properties to this node.
    */
    private class PropL extends Object
        implements PropertyChangeListener, FileStatusListener {
        /** weak version of this listener */
        private FileStatusListener weakL;
        /** previous filesystem we were attached to */
        private FileSystem previous;

        public PropL () {
            updateStatusListener ();
        }

        public void propertyChange (PropertyChangeEvent ev) {
            fireChange (ev);
        }

        /** Updates listening on a status of filesystem.
        */
        private void updateStatusListener () {
            if (previous != null) {
                previous.removeFileStatusListener (weakL);
            }
            try {
                previous = obj.getPrimaryFile ().getFileSystem ();

                if (weakL == null) {
                    weakL = org.openide.filesystems.FileUtil.weakFileStatusListener (this, null);
                }

                previous.addFileStatusListener (weakL);
            } catch (FileStateInvalidException ex) {
                previous = null;
            }
        }

        /** Notifies listener about change in annotataion of a few files.
        * @param ev event describing the change
        */
        public void annotationChanged (FileStatusEvent ev) {
            // #16541: listen for changes in both primary and secondary files
            boolean thisChanged = false;
            Iterator it = obj.files().iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();
                if (ev.hasChanged(fo)) {
                    thisChanged = true;
                    break;
                }
            }
            if (thisChanged) {
                // #12368: fire display name & icon changes asynch
                synchronized (refreshNameIconLock) {
                    boolean post = false;
                    if (ev.isNameChange()) {
                        if (refreshNameNodes == null) {
                            refreshNameNodes = new HashSet<DataNode>();
                        }
                        post |= refreshNameNodes.add(DataNode.this);
                    }
                    if (ev.isIconChange()) {
                        if (refreshIconNodes == null) {
                            refreshIconNodes = new HashSet<DataNode>();
                        }
                        post |= refreshIconNodes.add(DataNode.this);
                    }
                    if (post && !refreshNamesIconsRunning) {
                        refreshNamesIconsRunning = true;
                        if (refreshNamesIconsTask == null) {
                            refreshNamesIconsTask = RequestProcessor.getDefault().post(new NamesUpdater());
                        } else {
                            // Should be OK even if it is running right now.
                            // (Cf. RequestProcessorTest.testScheduleWhileRunning.)
                            refreshNamesIconsTask.schedule(0);
                        }
                    }
                }
            }
        }
    }
            
    private static class NamesUpdater implements Runnable {
        /** Refreshes names and icons for a whole batch of data nodes at once.
         */
        public void run() {
            DataNode[] _refreshNameNodes, _refreshIconNodes;
            synchronized (refreshNameIconLock) {
                if (refreshNameNodes != null) {
                    _refreshNameNodes = refreshNameNodes.toArray(new DataNode[refreshNameNodes.size()]);
                    refreshNameNodes.clear();
                } else {
                    _refreshNameNodes = new DataNode[0];
                }
                if (refreshIconNodes != null) {
                    _refreshIconNodes = refreshIconNodes.toArray(new DataNode[refreshIconNodes.size()]);
                    refreshIconNodes.clear();
                } else {
                    _refreshIconNodes = new DataNode[0];
                }
                refreshNamesIconsRunning = false;
            }
            for (int i = 0; i < _refreshNameNodes.length; i++) {
                _refreshNameNodes[i].fireChangeAccess(false, true);
            }
            for (int i = 0; i < _refreshIconNodes.length; i++) {
                _refreshIconNodes[i].fireChangeAccess(true, false);
            }
        }
        
    }

    /** Handle for data object nodes */
    private static class ObjectHandle implements Node.Handle {
        private FileObject obj;
        private boolean clone;

        static final long serialVersionUID =6616060729084681518L;


        public ObjectHandle (DataObject obj, boolean clone) {
            this.obj = obj.getPrimaryFile ();
            this.clone = clone;
        }

        public Node getNode () throws IOException {
            if (obj == null) {
                // Serialization problem? Seems to occur frequently with connection support:
                // java.lang.IllegalArgumentException: Called DataObject.find on null
                //         at org.openide.loaders.DataObject.find(DataObject.java:435)
                //         at org.openide.loaders.DataNode$ObjectHandle.getNode(DataNode.java:757)
                //         at org.netbeans.modules.java.JavaDataObject$PersistentConnectionHandle.getNode(JavaDataObject.java:977)
                //         at org.openide.loaders.ConnectionSupport$Pair.getNode(ConnectionSupport.java:357)
                //         at org.openide.loaders.ConnectionSupport.register(ConnectionSupport.java:94)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.registerDependency(SourceConnectionSupport.java:475)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.addDependency(SourceConnectionSupport.java:554)
                //         at org.netbeans.modules.java.codesync.ClassDependencyImpl.supertypesAdded(ClassDependencyImpl.java:241)
                //         at org.netbeans.modules.java.codesync.ClassDependencyImpl.refreshClass(ClassDependencyImpl.java:121)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.refreshLinks(SourceConnectionSupport.java:357)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport.access$000(SourceConnectionSupport.java:44)
                //         at org.netbeans.modules.java.codesync.SourceConnectionSupport$2.run(SourceConnectionSupport.java:223)
                throw new IOException("File could not be restored"); // NOI18N
            }
            Node n = DataObject.find (obj).getNodeDelegate ();
            return clone ? n.cloneNode () : n;
        }
    }
    
    /** Wrapping class for obj.files(). Used in getIcon() and getDisplayName()
        to have something lazy to pass to annotateIcon() and annotateName()
        instead of calling obj.files() immediately. */
    private class LazyFilesSet implements Set<FileObject> {
        
        private Set<FileObject> obj_files;
        
        synchronized private void lazyInitialization () {
           obj_files = obj.files();
        }
        
        public boolean add(FileObject o) {
            lazyInitialization();
            return obj_files.add(o);
        }
        
        public boolean addAll(Collection<? extends FileObject> c) {
            lazyInitialization();
            return obj_files.addAll(c);
        }
        
        public void clear() {
            lazyInitialization();
            obj_files.clear();
        }
        
        public boolean contains(Object o) {
            lazyInitialization();
            return obj_files.contains(o);
        }
        
        public boolean containsAll(Collection c) {
            lazyInitialization();
            return obj_files.containsAll(c);
        }
        
        public boolean isEmpty() {
            lazyInitialization();
            return obj_files.isEmpty();
        }
        
        public Iterator<FileObject> iterator() {
            return new FilesIterator ();
        }
        
        public boolean remove(Object o) {
            lazyInitialization();
            return obj_files.remove(o);
        }
        
        public boolean removeAll(Collection c) {
            lazyInitialization();
            return obj_files.removeAll(c);
        }
        
        public boolean retainAll(Collection c) {
            lazyInitialization();
            return obj_files.retainAll(c);
        }
        
        public int size() {
            lazyInitialization();
            return obj_files.size();
        }
        
        public Object[] toArray() {
            lazyInitialization();
            return obj_files.toArray();
        }
        
        public <FileObject> FileObject[] toArray(FileObject[] a) {
            lazyInitialization();
            return obj_files.toArray(a);
        }

        public boolean equals(Object obj) {
            lazyInitialization();
            return obj_files.equals(obj);
        }

        public String toString() {
            lazyInitialization();
            return obj_files.toString();
        }

        public int hashCode() {
            lazyInitialization();
            return obj_files.hashCode();
        }
        
        /** Iterator for FilesSet. It returns the primaryFile first and 
         * then initialize the delegate iterator for secondary files.
         */
        private final class FilesIterator implements Iterator<FileObject> {
            /** Was the first element (primary file) already returned?
             */
            private boolean first = true;

            /** Delegation iterator for secondary files. It is lazy initialized after
             * the first element is returned.
             */
            private Iterator<FileObject> itDelegate = null;

            FilesIterator() {}

            public boolean hasNext() {
                return first ? true : getIteratorDelegate().hasNext();
            }

            public FileObject next() {
                if (first) {
                    first = false;
                    return obj.getPrimaryFile ();
                }
                else {
                    return getIteratorDelegate().next();
                }
            }

            public void remove() {
                getIteratorDelegate().remove();
            }

            /** Initialize the delegation iterator.
             */
            private Iterator<FileObject> getIteratorDelegate() {
                if (itDelegate == null) {
                    lazyInitialization ();
                    // this should return iterator of all files of the MultiDataObject...
                    itDelegate = obj_files.iterator ();
                    // ..., so it is necessary to skip the primary file
                    itDelegate.next();
                }
                return itDelegate;
            }
        }
    }    
    
    
    
}
