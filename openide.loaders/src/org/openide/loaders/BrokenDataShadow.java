/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.*;
import java.text.MessageFormat;
import java.lang.reflect.*;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.nodes.Children;
import org.openide.util.actions.*;
import org.openide.actions.*;

/** For representing data shadows with broken link to original file.
* Since 1.13 it extends MultiDataObject.
* @author Ales Kemr
*/
final class BrokenDataShadow extends MultiDataObject {
    /** Name of filesystem of original fileobject */
    private String origFSName;    
    
    /** Name of original fileobject */
    private String origFOName;
        
    /** Constructs new broken data shadow for given primary file.
    *
    * @param fo the primary file
    * @param loader the loader that created the object
    */
    public BrokenDataShadow (
        FileObject fo, MultiFileLoader loader
    ) throws DataObjectExistsException {        
        super (fo, loader);                                
        
        try {
            BufferedReader ois = new BufferedReader (new InputStreamReader (fo.getInputStream ()));
            origFOName = ois.readLine ();
            origFSName = ois.readLine ();
            ois.close();
        } catch (IOException e) {
        }
        enqueueBrokenDataShadow(this);
    }
        
    /** Map of <String(nameoffileobject), DataShadow> */
    private static Map allDataShadows;
    /** ReferenceQueue for collected DataShadows */
    private static ReferenceQueue rqueue;
    
    private static final long serialVersionUID = -3046981691235483810L;
    
    /** Getter for the Set that contains all DataShadows. */
    private static synchronized Map getDataShadowsSet() {
       if (allDataShadows == null) {
           allDataShadows = new HashMap();
       }
        return allDataShadows;
    }
    
    /** Getter for the ReferenceQueue that contains WeakReferences
     * for discarded DataShadows
     */
    private static synchronized ReferenceQueue getRqueue() {
        if (rqueue == null) {
            rqueue = new ReferenceQueue();
        }
        return rqueue;
    }
    
    /** Removes WeakReference of collected DataShadows. */
    private static void checkQueue() {
        if (rqueue == null) {
            return;
        }
        
        Reference ref = rqueue.poll();
        while (ref != null) {
           getDataShadowsSet().remove(ref);
           ref = rqueue.poll();
        }
    }
    
    private static synchronized void enqueueBrokenDataShadow(BrokenDataShadow ds) {
        checkQueue();
        Map m = getDataShadowsSet ();
        
        String prim = ds.origFOName;
        Reference ref = DataShadow.createReference(ds, getRqueue());
        Set s = (Set)m.get (prim);
        if (s == null) {
            s = java.util.Collections.singleton (ref);
            getDataShadowsSet ().put (prim, s);
        } else {
            if (! (s instanceof HashSet)) {
                s = new HashSet (s);
                getDataShadowsSet ().put (prim, s);
            }
            s.add (ref);
        }
    }

    /** @return all active DataShadows or null */
    private static synchronized List getAllDataShadows() {
        if (allDataShadows == null || allDataShadows.isEmpty()) {
            return null;
        }
        
        List ret = new ArrayList(allDataShadows.size());
        Iterator it = allDataShadows.values ().iterator();
        while (it.hasNext()) {
            Set ref = (Set) it.next();
            Iterator refs = ref.iterator ();
            while (refs.hasNext ()) {
                Reference r = (Reference)refs.next ();
                Object shadow = r.get();
                if (shadow != null) {
                    ret.add(shadow);
                }
            }
        }
        
        return ret;
    }
    
    /** Checks whether a change of the given dataObject
     * does not revalidate a BrokenDataShadow
     */
    static void checkValidity(EventObject ev) {
        DataObject src = null;
        if (ev instanceof OperationEvent) {
            src = ((OperationEvent)ev).getObject();
        }

        Set shadows = null;
        synchronized (BrokenDataShadow.class) {
            if (allDataShadows == null || allDataShadows.isEmpty ()) return;
            
            if (src != null) {
                shadows = (Set)allDataShadows.get (src.getPrimaryFile ().getPath ());
                if (shadows == null) {
                    // we know the source of the event and there are no
                    // shadows with such original
                    return;
                }
            }
        }
        
        
        List all = getAllDataShadows();
        if (all == null) {
            return;
        }
        
        int size = all.size();
        for (int i = 0; i < size; i++) {
            Object obj = all.get(i);
            ((BrokenDataShadow) obj).refresh();
        }
    }
    
    /** Constructs new broken data shadow for given primary file.
    * @param fo the primary file
    */
    private BrokenDataShadow (FileObject fo) throws DataObjectExistsException {
        this (fo, (MultiFileLoader)DataLoaderPool.getShadowLoader ());
    }
    
    /* Getter for delete action.
    * @return true if the object can be deleted
    */
    public boolean isDeleteAllowed() {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Check if link to original file is still broken */    
    public void refresh() {
        try {
            if (DataShadow.checkOriginal (origFOName, origFSName, 
                getPrimaryFile().getFileSystem()) != null) {
                /* Link to original file was repaired */
                this.setValid(false);
            }
        } catch (Exception e) {
        }
    }
    
    /* Getter for copy action.
    * @return true if the object can be copied
    */
    public boolean isCopyAllowed() {
        return true;
    }

    /* Getter for move action.
    * @return true if the object can be moved
    */
    public boolean isMoveAllowed() {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for rename action.
    * @return true if the object can be renamed
    */
    public boolean isRenameAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* Creates node delegate.
    */    
    protected Node createNodeDelegate () {
        return new BrokenShadowNode (this);
    }
    
    /** Node for a broken shadow object. */
    private static final class BrokenShadowNode extends DataNode {
        
        /** message to create name of node */
        private static MessageFormat format;
        
        /** the sheet computed for this node or null */
        private Sheet sheet;

        private static final String ICON_NAME = "org/openide/loaders/brokenShadow"; // NOI18N

        /** Create a node.
         * @param broken data shadow
         */        
        public BrokenShadowNode (BrokenDataShadow par) {            
            super (par,Children.LEAF);
            setIconBase(ICON_NAME);
        }
        
        /** Get the display name for the node.
         * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
         * @return the desired name
        */
        public String getDisplayName () {
            if (format == null) {
                format = new MessageFormat (DataObject.getString ("FMT_brokenShadowName"));
            }
            return format.format (createArguments ());
        }
        
        /** Create actions for this data object.
        * @return array of actions or <code>null</code>
        */    
        protected SystemAction[] createActions () {
            return new SystemAction[] {
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class)
                    };
        }
    
        /** Returns modified properties of the original node.
        * @return property sets 
        */
        public PropertySet[] getPropertySets () {
            if (sheet == null) {
                sheet = cloneSheet ();                
            }
            return sheet.toArray ();
        }
        
        /** Clones the property sheet of original node.
        */
        private Sheet cloneSheet () {
            PropertySet[] sets = super.getPropertySets ();

            Sheet s = new Sheet ();
            for (int i = 0; i < sets.length; i++) {
                Sheet.Set ss = new Sheet.Set ();
                ss.put (sets[i].getProperties ());
                ss.setName (sets[i].getName ());
                ss.setDisplayName (sets[i].getDisplayName ());
                ss.setShortDescription (sets[i].getShortDescription ());

                // modifies the set if it contains name of object property
                modifySheetSet (ss);
                
                s.put (ss);
            }

            return s;
        }
        
        /** Modifies the sheet set to contain name of property and name of
        * original object.
        */
        private void modifySheetSet (Sheet.Set ss) {
            Property p = ss.remove (DataObject.PROP_NAME);
            if (p != null) {
                p = new PropertySupport.Name (this);
                ss.put (p);

                p = new Name ();
                ss.put (p);
                
                p = new FileSystemProperty ();
                ss.put (p);
            }
        }
        
        /** Creates arguments for given shadow node */
        private Object[] createArguments () {
            return new Object[] {
                       getDataObject().getName ()
                   };
        }    
    
        /** Class for original name property of broken link
        */
        private final class Name extends PropertySupport.ReadWrite {
            
            public Name () {
                super (
                    "BrokenLink", // NOI18N
                    String.class,
                    DataObject.getString ("PROP_brokenShadowOriginalName"),
                    DataObject.getString ("HINT_brokenShadowOriginalName")
                );
            }

            /* Getter */
            public Object getValue () {
                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                return bds.origFOName;
            }
            
            /* Does nothing, property is readonly */
            public void setValue (Object val) {
                String newLink = (String)val;

                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                try {
                    DataShadow.writeOriginal (bds.getPrimaryFile (), newLink, bds.origFSName);
                    bds.origFOName = newLink;
                } catch (IOException ex) {
                    IllegalArgumentException e = new IllegalArgumentException (ex.getMessage ());
                    org.openide.ErrorManager.getDefault ().annotate (e, ex);
                    throw e;
                }
                bds.refresh ();
            }
        }                
        
        /** Class for original filesystem name property of broken link
        */
        private final class FileSystemProperty extends PropertySupport.ReadWrite {
            private java.beans.PropertyChangeSupport supp;
            
            public FileSystemProperty () {
                super (
                    "BrokenLinkFileSystem", // NOI18N
                    String.class,
                    DataObject.getString ("PROP_brokenShadowFileSystem"),
                    DataObject.getString ("HINT_brokenShadowFileSystem")
                );
            }

            /* Getter */
            public Object getValue () {
                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                return bds.origFSName;
            }                        
            
            public void setValue (Object val) throws IllegalArgumentException {
                String newFSName = (String)val;
                
                BrokenDataShadow bds = (BrokenDataShadow)getDataObject();
                try {
                    DataShadow.writeOriginal (bds.getPrimaryFile (), bds.origFOName, newFSName);
                    bds.origFSName = newFSName;
                } catch (IOException ex) {
                    IllegalArgumentException e = new IllegalArgumentException (ex.getMessage ());
                    org.openide.ErrorManager.getDefault ().annotate (e, ex);
                    throw e;
                }
                bds.refresh ();
            }
            
            public java.beans.PropertyEditor getPropertyEditor () {
                return new FileSystemPropertyEditor ();
            }
            
        }
        
        
        private final class FileSystemPropertyEditor 
        implements java.beans.PropertyEditor {
            private java.beans.PropertyChangeSupport supp;
            private String origFSName;
            
            public FileSystemPropertyEditor () {
            }

            /* Getter */
            public Object getValue () {
                return origFSName;
            }                        
            
            public void setValue (Object val) throws IllegalArgumentException {
                origFSName = (String)val;
            }
            
            public synchronized void addPropertyChangeListener (java.beans.PropertyChangeListener listener) {
                if (supp == null) {
                    supp = new java.beans.PropertyChangeSupport (this);
                }
                supp.addPropertyChangeListener (listener);
            }
            
            public synchronized void removePropertyChangeListener (java.beans.PropertyChangeListener listener) {
                if (supp == null) {
                    supp.removePropertyChangeListener (listener);
                }
            }
            
            public String getAsText () {
                String v = (String)getValue ();
                FileSystem[] arr = Repository.getDefault ().toArray ();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].getSystemName ().equals (v)) {
                        return arr[i].getDisplayName ();
                    }
                }
                return v;
            }
            
            public java.awt.Component getCustomEditor () {
                return null;
            }
            
            public String getJavaInitializationString () {
                return null;
            }
            
            public String[] getTags () {
                FileSystem[] arr = Repository.getDefault ().toArray ();
                String[] v = new String[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    v[i] = arr[i].getDisplayName ();
                }
                return v;
            }
            
            public boolean isPaintable () {
                return false;
            }
            
            public void paintValue (java.awt.Graphics gfx, java.awt.Rectangle box) {
            }
            
            public void setAsText (String text) throws java.lang.IllegalArgumentException {
                FileSystem[] arr = Repository.getDefault ().toArray ();
                String[] v = new String[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    if (text.equals (arr[i].getDisplayName ())) {
                        setValue (arr[i].getSystemName ());
                    }
                }
            }
            
            public boolean supportsCustomEditor () {
                return false;
            }
            
        }
        
    }
}
