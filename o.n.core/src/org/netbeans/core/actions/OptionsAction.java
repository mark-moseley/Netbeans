/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.actions;

import java.io.ObjectStreamException;
import java.awt.*;
import java.awt.event.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.table.JTableHeader;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicBorders;

import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.NodeTableModel;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import org.openide.cookies.InstanceCookie;

import org.netbeans.core.projects.SettingChildren;
import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.NbMainExplorer;
import org.netbeans.core.NbPlaces;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.Mode;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    private static final String HELP_ID = "org.netbeans.core.actions.OptionsAction"; // NOI18N 
    
    /** Weak reference to the dialog showing singleton options. */
    private Reference dialogWRef = new WeakReference(null);
    

    public void performAction () {
        final OptionsPanel optionPanel = OptionsPanel.singleton ();
        
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                Dialog dialog = (Dialog)dialogWRef.get();

                if(dialog == null || !dialog.isShowing()) {
                    JButton closeButton = new JButton(NbBundle.getMessage(OptionsAction.class, "CTL_close_button"));
                    closeButton.setMnemonic(NbBundle.getMessage(OptionsAction.class, "CTL_close_button_mnemonic").charAt(0));
                    closeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OptionsAction.class, "ACSD_close_button"));
                    DialogDescriptor dd = new DialogDescriptor(
                        InitPanel.getDefault(optionPanel),
                        optionPanel.getName(),
                        false,
                        new Object[] {closeButton},
                        closeButton,
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        null);

                    // #37673
                    optionPanel.setDialogDescriptor(dd);
                        
                    dialog = DialogDisplayer.getDefault().createDialog(dd);
                    dialog.show();
                    dialogWRef = new WeakReference(dialog);
                } else {
                    dialog.toFront();
                }
                
                org.openide.awt.StatusDisplayer.getDefault ().setStatusText (""); // NOI18N
            }
        }); // EQ.iL
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public String iconResource () {
        return "org/netbeans/core/resources/session.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }

    public String getName() {
        return NbBundle.getBundle(OptionsAction.class).getString("Options");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class OptionsPanel extends NbMainExplorer.SettingsTab
    implements PropertyChangeListener {
        /** Name of mode in which options panel is docked by default */
        public static final String MODE_NAME = "options";
        /** Singleton instance of options panel */
        private static OptionsPanel singleton;
        
        private static String TEMPLATES_DISPLAY_NAME = NbBundle.getBundle (org.netbeans.core.NbTopManager.class).getString("CTL_Templates_name"); // NOI18N
        
        /** list of String[] that should be expanded when the tree is shown */
        private Collection toExpand;
        private transient boolean expanded;
        /** root node to use */
        private transient Node rootNode;
        
        // XXX #37673
        private transient Reference descriptorRef = new WeakReference(null);
        

        private OptionsPanel () {
            validateRootContext ();
            // show only name of top component is typical case
            putClientProperty("NamingType", "BothOnlyCompName"); // NOI18N
            // Show without tab when alone in container cell.
            putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            
            getExplorerManager().addPropertyChangeListener(this);
        }
        
        /** Overriden to explicitely set persistence type of OptionsPanel
         * to PERSISTENCE_ALWAYS */
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ALWAYS;
        }
        
        // #37673 It was requested to update helpCtx according to node selection in explorer.
        public void propertyChange(PropertyChangeEvent evt) {
            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                DialogDescriptor dd = (DialogDescriptor)descriptorRef.get();
                if(dd != null) {
                    dd.setHelpCtx(getHelpCtx());
                }
            }
        }
        // #37673
        public void setDialogDescriptor(DialogDescriptor dd) {
            descriptorRef = new WeakReference(dd);
        }
        
        public HelpCtx getHelpCtx () {
            HelpCtx defaultHelp = new HelpCtx (HELP_ID);
            HelpCtx help = ExplorerPanel.getHelpCtx (
                getExplorerManager ().getSelectedNodes (),
                defaultHelp
            );
            // bugfix #23551, add help id to subnodes of Templates category
            // this check prevents mixed help ids on more selected nodes
            if (!defaultHelp.equals (help)) {
                // try if selected node isn't template
                Node node = getExplorerManager ().getSelectedNodes ()[0];
                HelpCtx readHelpId = getHelpId (node);
                if (readHelpId != null) return readHelpId;
                
                // next bugfix #23551, children have same helpId as parent if no specific is declared
                while (node != null && !TEMPLATES_DISPLAY_NAME.equals (node.getDisplayName ())) {
                    readHelpId = getHelpId (node);
                    if (readHelpId != null) return readHelpId;
                    node = node.getParentNode ();
                }
                if (node != null && TEMPLATES_DISPLAY_NAME.equals (node.getDisplayName ())) {
                    return new HelpCtx ("org.netbeans.core.actions.OptionsAction$TemplatesSubnode"); // NOI18N
                }
            }
            return help;
        }
        
        private HelpCtx getHelpId (Node node) {
            // it's template, return specific help id
            DataObject dataObj = (DataObject)node.getCookie (DataObject.class);
            if (dataObj != null) {
                Object o = dataObj.getPrimaryFile ().getAttribute ("helpID"); // NOI18N
                if (o != null) {
                    return new HelpCtx (o.toString ());
                }
            }
            return null;
        }

        /** Accessor to the singleton instance */
        static OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
            }
            return singleton;
        }
        
        private transient JSplitPane split=null;
        protected TreeView initGui () {
            TTW retVal = new TTW () ;
            
            
            split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
            PropertySheetView propertyView = new PropertySheetView();
            
            split.setLeftComponent(retVal);
            split.setRightComponent(propertyView);
            // install proper border for split pane
            split.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N

            setLayout (new java.awt.GridBagLayout ());

            GridBagConstraints gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridwidth = 2;
            add (split, gridBagConstraints);

            return retVal;
        }
        
        /** Overridden to provide a larger preferred size if the default font
         *  is larger, for locales that require this.   */
        public Dimension getPreferredSize() {
            //issue 34104, bad sizing/split location for Chinese locales that require
            //a larger default font size
            Dimension result = super.getPreferredSize();
            Font treeFont = UIManager.getFont("Tree.font"); // NOI18N
            int fontsize = treeFont != null ? treeFont.getSize() : 11;
            if (fontsize > 11) {
                int factor = fontsize - 11;
                result.height += 15 * factor;
                result.width += 50 * factor;
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                if (result.height > screen.height) {
                    result.height = screen.height -30;
                }
                if (result.width > screen.width) {
                    result.width = screen.width -30;
                }
            } else {
                result.width += 20;
                result.height +=20;
            }
            
            return result;
        }

        boolean isPrepared () {
            return toExpand != null;
        }
        
        public void prepareNodes() {
            if (toExpand == null) {                        
                ArrayList arr = new ArrayList (101);
                expandNodes(getRootContext (), 2, arr);               
                toExpand = arr;
            }
        }


        protected void componentShowing () {
            super.componentShowing ();
            if (!expanded) {
                ((TTW)view).expandTheseNodes (toExpand, getExplorerManager ().getRootContext ());                
                expanded = true;
            }
            // initialize divider location
            split.setDividerLocation(getPreferredSize().width / 2);
        }
        

        protected void validateRootContext () {
            Node n = initRC ();
            setRootContext (n);
        }
        
        /** Resolves to the singleton instance of options panel. */
        public Object readResolve ()
        throws ObjectStreamException {
            if (singleton == null) {
                singleton = this;
            }
            singleton.scheduleValidation();
            // set deserialized root node
            rootNode = getRootContext ();
            return singleton;
        }
        
        private synchronized Node initRC () {
            if (rootNode == null) {
                rootNode = new OptionsFilterNode ();
            }
            return rootNode;
        }

        /** Expands the node in explorer.
         */
        private static void expandNodes (Node n, final int depth, final Collection list) {
            if (depth == 0) {
                return;
            }
            
            DataObject obj = (DataObject)n.getCookie(DataObject.class);
            if (obj instanceof DataShadow) {
                obj = ((DataShadow)obj).getOriginal();
            }
            
            if (obj != null) {
                if (!obj.getPrimaryFile().getPath().startsWith ("UI/Services")) { // NOI18N
                    return;
                }

                InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
                if (ic != null) {

                    if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf (Node.class)) {
                            return;
                        }
                    } 
                }
            }
            
            // ok, expand this node
            if (!list.contains (n)) {
                list.add (n);
            }
         
            Node[] arr = n.getChildren().getNodes(true);
            for (int i = 0; i < arr.length; i++) {
                final Node p = arr[i];
                expandNodes(p, depth - 1, list);
            }
        }
        
        //
        // Model to implement the special handling of SettingChildren.* properties
        //
        
        /** Model that tries to extract properties from the node.getValue 
         * instead of creating its getPropertySets.
         */
        private static class NTM extends NodeTableModel {
            public NTM () {
                super ();
            }
            
            protected Node.Property getPropertyFor(Node node, Node.Property prop) {
                Object value = node.getValue (prop.getName());
                if (value instanceof Node.Property) {
                    return (Node.Property)value;
                }
                
                return null;
            }
        }

        private static class TTW extends TreeTableView implements MouseListener, PropertyChangeListener, java.awt.event.ActionListener {
            /** Dummy property that can be expanded or collapsed. */
            private final Node.Property indicator = new IndicatorProperty();
            /** Session layer state indicator property */
            private final Node.Property session = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_SESSION);
            /** Modules layer state indicator property */
            private final Node.Property modules = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_MODULES);
            
            /** Active set of properties (columns) */
            private Node.Property active_set [] = null;
            PropertyChangeListener weakL = null;
            
            public TTW () {
                super (new NTM ());
                
                refreshColumns (true);
                addMouseListener (this);
                weakL = org.openide.util.WeakListeners.propertyChange (this, SessionManager.getDefault ());
                SessionManager.getDefault ().addPropertyChangeListener (weakL);
                
                registerKeyboardAction(
                    this,
                    javax.swing.KeyStroke.getKeyStroke('+'),
                    javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                );

                getAccessibleContext().setAccessibleName(NbBundle.getBundle(OptionsAction.class).getString("ACSN_optionsTree"));
                getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(OptionsAction.class).getString("ACSD_optionsTree"));
            }
            public void mouseExited (MouseEvent evt) {
            }
            public void mouseReleased (MouseEvent evt) {
            }
            public void mousePressed (MouseEvent evt) {
            }
            public void mouseClicked (MouseEvent evt) {
                Component c = evt.getComponent ();
                if (c instanceof JTableHeader) {
                    JTableHeader h = (JTableHeader)c;
                    
                    // show/hide additional properties
                    if (1 == h.columnAtPoint (evt.getPoint ())) {
                        refreshColumns (true);
                    }
                }
            }
            public void mouseEntered (MouseEvent evt) {
            }
            public void propertyChange(PropertyChangeEvent evt) {
                if (SessionManager.PROP_OPEN.equals (evt.getPropertyName ())) {
                    refreshColumns (false);
                }
            }
            private void refreshColumns (boolean changeSets) {
                Node.Property new_set [] = active_set;
                int length = active_set == null ? 0 : active_set.length;

                if ((changeSets && length == 1) || (!changeSets && length > 1)) {
                    // build full_set
                    new_set = new Node.Property[] {indicator, session, modules};

                    indicator.setDisplayName (
                        NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Name_Expanded")); //NOI18N
                    indicator.setShortDescription (
                        NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Description_Expanded")); //NOI18N
                }
                else {
                    if (changeSets) {
                        new_set = new Node.Property[] {indicator};
                        indicator.setDisplayName (
                            NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Name")); //NOI18N
                        indicator.setShortDescription (
                            NbBundle.getMessage(OptionsAction.class, "LBL_IndicatorProperty_Description")); //NOI18N
                    }
                }
                
                if (active_set != new_set) {
                    // setup new columns
                    final Node.Property set [] = new_set;
                    if (SwingUtilities.isEventDispatchThread()) {
                        setNewSet(set);
                    } else {
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                setNewSet(set);
                            }
                        });
                    }
                    // remeber the last set of columns
                    active_set = new_set;
                }
            }
            
            private void setNewSet (Node.Property[] set) {
                // change columns
                setProperties (set);
                // set preferred colunm sizes
                setTreePreferredWidth(set.length == 1 ? 480 : 300);
                setTableColumnPreferredWidth (0, 20);
                for (int i = 1; i < set.length; i++) {
                    setTableColumnPreferredWidth (i, 60);
                }
            }
            
            public void actionPerformed(ActionEvent e) {
                refreshColumns(true);
            }
            
            public void expandTheseNodes (Collection paths, Node root) {
                Iterator it = paths.iterator();
                
                Node first = null;
                while (it.hasNext()) {
                    Node n = (Node)it.next();
                    if (first == null) {
                        first = n;
                    }
                    
                    this.expandNode(n);
                }

                if (first != null) {
                    collapseNode (first);
                    expandNode (first);
                }
                
                // move to top
                tree.scrollRowToVisible(0);
            }

            /** Dummy placeholder property. */
            private static final class IndicatorProperty extends PropertySupport.ReadOnly {

                public IndicatorProperty() {
                    super("indicator", String.class, "", ""); // NOI18N
                }

                public Object getValue() {
                    return ""; // NOI18N
                }

            }

        }
            
       
        private static class OptionsFilterNode extends FilterNode {
            public OptionsFilterNode () {
                super (
                    NbPlaces.getDefault().session(),
                    new SettingChildren (NbPlaces.getDefault().session())
                );
            }
            public HelpCtx getHelpCtx () {
                return new HelpCtx (OptionsFilterNode.class);
            }
            
            public Node.Handle getHandle () {
                return new H ();
            }
            
            private static class H implements Node.Handle {
                H() {}
                
                private static final long serialVersionUID = -5158460093499159177L;
                
                public Node getNode () throws java.io.IOException {
                    return new OptionsFilterNode ();
                }
            }
        }
        
    } // end of inner class OptionsPanel    
}
