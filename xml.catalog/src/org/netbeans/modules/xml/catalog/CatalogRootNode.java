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
package org.netbeans.modules.xml.catalog;

import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.event.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;
import org.openide.actions.*;

import org.netbeans.modules.xml.catalog.spi.*;
import org.netbeans.modules.xml.catalog.impl.*;
import org.netbeans.modules.xml.catalog.settings.CatalogSettings;
import org.openide.util.HelpCtx;
import java.awt.event.ActionEvent;

/**
 * Node representing catalog root in the Runtime tab. It retrieves all
 * mounted catalogs from current project settings.
 *
 * To be registered in manifest file as:
 * <pre>
 * Name: org.netbeans.modules.xml.catalog.CatalogNode.class
 * OpenIDE-Module-Class: Environment
 * </pre>
 *
 * <p><b>Implementation Note:</b>
 * <p>The node has session lifetime but its model has project lifetime, so there
 * is implemented a logic for model instance changing (see children).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class CatalogRootNode extends AbstractNode implements Node.Cookie {

    /** Creates new CatalogNode */
    public CatalogRootNode() {
        super(new RootChildren());
        setName("XML-CATALOG");                                                 // NOI18N
        setDisplayName (Util.THIS.getString("TEXT_catalog_root"));              // NOI18N
        setIconBase("org/netbeans/modules/xml/catalog/resources/catalog-root"); // NOI18N
        setShortDescription(Util.THIS.getString("PROP_catalog_root_desc"));
        getCookieSet().add(this);
    }
    
    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(CatalogRootNode.MountAction.class),
//            null,
//            SystemAction.get(PropertiesAction.class)
        };
    }

    /** We can mount entity catalogs. */
//    public NewType[] getNewTypes() {
//        return new NewType[] {new CatalogMounter()};
//    }
    
    /** 
     * Mounts new catalalog as specified by user. 
     */
    class CatalogMounter extends NewType implements ActionListener {

        CatalogMounterModel model = null;
        Dialog myDialog = null;
        
        public void create() throws IOException {
            
            Iterator it = ProvidersRegistry.getProviderClasses(new Class[] {CatalogReader.class});
            
            model = new CatalogMounterModel(it);
            Object rpanel = new CatalogMounterPanel(model);
            DialogDescriptor dd = new DialogDescriptor(rpanel,
                                  Util.THIS.getString ("PROP_Mount_Catalog"), true, this);
            dd.setHelpCtx(new HelpCtx(CatalogMounterPanel.class));
            myDialog = TopManager.getDefault().createDialog(dd);

            // resize dialog on model change
            
//            final Window win = myDialog;            
//            model.addChangeListener(new ChangeListener() {
//                public void stateChanged(ChangeEvent e) {
//                    win.pack();  
//                }
//            });
            
            myDialog.setSize(450, 250);  //^ packing never creates bigger window :-(
            myDialog.show();
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == DialogDescriptor.OK_OPTION) {
                
                Object catalog = model.getCatalog();
                if (catalog == null) return;
                CatalogSettings mounted = CatalogSettings.getDefault();
                mounted.addCatalog((CatalogReader)catalog);
                
            }
            if (myDialog != null) {
                myDialog.dispose();
                myDialog = null;
            }
        }
        
        public String getName() {
            return Util.THIS.getString ("PROP_Mount_Catalog"); // NOI18N
        }
    }
    

    // ~~~~~~~~~~~~~~~~~~~~~~ Serialization stuff ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Reading CatalogRoot node " + this); // NOI18N

        in.defaultReadObject();        
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Writing " + this); // NOI18N

        out.defaultWriteObject();        
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CatalogRootNode.class);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~ NODE KIDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    /**
     * Kids driven by CatalogSettings. Only one instance may be used
     * since redefined equals() method.
     */
    private static class RootChildren extends Children.Keys implements Comparator, PropertyChangeListener {
        
        /** Contains CatalogReader instances. */
        private final TreeSet keys = new TreeSet(this);
        
        /**
          * Create new keys, register itself as listener.
          */
        public synchronized void addNotify() {            
            CatalogSettings mounted = CatalogSettings.getDefault();
            mounted.addPropertyChangeListener(this);
            createKeys(mounted);                        
        }

        /**
          * Remove listener and keys.
          */
        public synchronized void removeNotify() {
            CatalogSettings mounted = CatalogSettings.getDefault();
            if (mounted != null) mounted.removePropertyChangeListener(this);
            keys.clear();
            setKeys(keys);
        }

        /**
          * Create CatalogNodes initialized by provider instance.
          */
        public Node[] createNodes(Object key) {        
            try {
                return new Node[] { new CatalogNode((CatalogReader)key) };
            } catch (IntrospectionException ex) {
                return new Node[] {};
            }
        }

        /**
          * The only instance (see equals) listens on ProvidersRegistry
          * for its state changes.
          */
        public synchronized void propertyChange(PropertyChangeEvent e) {
            if (CatalogSettings.PROP_MOUNTED_CATALOGS.equals(e.getPropertyName())) {
                createKeys((CatalogSettings)e.getSource());
            } else if (CatalogSettings.PROP_PRJ_INSTANCE.equals(e.getPropertyName())) {
                
                //??? switch model instances it is an ugly hack
                
                CatalogSettings mounted = (CatalogSettings) e.getOldValue();
                if (mounted != null) {
                    mounted.removePropertyChangeListener(this);
                }
                mounted = (CatalogSettings) e.getNewValue();
                if (mounted != null) mounted.addPropertyChangeListener(this);
                createKeys(mounted);
            }
        }
        
        /** 
          * Creates new keys according to CatalogSettings.
          */
        private void createKeys(CatalogSettings mounted) {
            keys.clear();
            if (mounted != null) {
                Iterator it = mounted.getCatalogs(new Class[] {CatalogReader.class});
                while (it.hasNext()) {
                    keys.add(it.next());    //!!! use immutable key wrappers, some
                                            // instances may overwrite equals() so
                                            // they cannot be used as a children key
                }
            }
            setKeys(keys);
        }
        
        /** 
          * We are also comparators. Use class based equality.
          */
        public boolean equals(java.lang.Object peer) {
            return peer.getClass().equals(getClass());
        }        
        
        /**
         * Compare keys giving highest priority to system catalog.
         * Other catalogs sort by display name if available.
         */
        public int compare(java.lang.Object one,java.lang.Object two) {
            if (one == two) return 0;
            if (one instanceof SystemCatalogReader) return -1;
            if (two instanceof SystemCatalogReader) return 1;
            if (one instanceof CatalogDescriptor && two instanceof CatalogDescriptor) {
                int test = (((CatalogDescriptor)one).getDisplayName()).compareTo(
                    ((CatalogDescriptor)two).getDisplayName()
                );
                if (test != 0) return test;
                
            } else {
                if (one instanceof CatalogDescriptor) return -1;
                if (two instanceof CatalogDescriptor) return 1;
            }
            // show all catalogs never return 0
            return (long)one.hashCode() - (long)two.hashCode() > 0L ? 1 : -1;
        }
        
    }

    
    /**
     * Give to action your own name
     */
    private static final class MountAction extends NodeAction {
        /** Serial Version UID */
        private static final long serialVersionUID = -3608629636833099065L;
        
        public MountAction() {
        }
        
        public String getName() {
            return Util.THIS.getString("LBL_mount");
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(MountAction.class);
        }
        
        protected synchronized boolean enable(Node[] activatedNodes) {
            return activatedNodes.length > 0;
        }
        
        protected synchronized void performAction(Node[] activatedNodes) {
            if (enable(activatedNodes) == false) return;
            try {
                Node current = activatedNodes[0];
                CatalogRootNode me = (CatalogRootNode) current.getCookie(CatalogRootNode.class);
                CatalogMounter newType = me.new CatalogMounter();
                newType.create();
            } catch (IOException ex) {
                Util.THIS.debug(ex);
            } finally {
            }
        }
    }
    
}
