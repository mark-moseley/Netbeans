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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

// <RAVE> Copy from projects/projectui/src/org/netbeans/modules/project/ui
package org.netbeans.modules.visualweb.project.jsf.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class BrowseFolders extends javax.swing.JPanel implements ExplorerManager.Provider {

    private ExplorerManager manager;
    private SourceGroup[] folders;
    private BeanTreeView btv;
    private Project project;

    private static JScrollPane SAMPLE_SCROLL_PANE = new JScrollPane();

    /** Creates new form BrowseFolders */
    public BrowseFolders( SourceGroup[] folders, Project project, String preselectedFileName ) {
        initComponents();
        this.folders = folders;
        this.project = project;

        manager = new ExplorerManager();
        AbstractNode rootNode = new AbstractNode( new SourceGroupsChildren( folders, project ) );
        manager.setRootContext( rootNode );

        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible( false );
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setBorder( SAMPLE_SCROLL_PANE.getBorder() );
        btv.setPopupAllowed( false );
        btv.getAccessibleContext ().setAccessibleName (NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders_folderPanel"));
        btv.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(BrowseFolders.class, "ACSD_BrowseFolders_folderPanel"));
        expandSelection( preselectedFileName );
        //expandAllNodes( btv, manager.getRootContext() );
        folderPanel.add( btv, java.awt.BorderLayout.CENTER );
    }

    // ExplorerManager.Provider implementation ---------------------------------

    public ExplorerManager getExplorerManager() {
        return manager;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        folderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders"));
        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "MNE_BrowseFolders_jLabel1").charAt(0));
        jLabel1.setLabelFor(folderPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BrowseFolders.class, "LBL_BrowseFolders_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders_jLabel1"));

        folderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(folderPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel folderPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
        
    public static FileObject showDialog( SourceGroup[] folders, Project project, String preselectedFileName ) {
        
        BrowseFolders bf = new BrowseFolders( folders, project, preselectedFileName );
        
        JButton options[] = new JButton[] { 
            new JButton( NbBundle.getMessage( BrowseFolders.class, "BTN_BrowseFolders_Select_Option") ), // NOI18N
            new JButton( NbBundle.getMessage( BrowseFolders.class, "BTN_BrowseFolders_Cancel_Option") ), // NOI18N
        };
                
        OptionsListener optionsListener = new OptionsListener( bf );
        
        options[ 0 ].setActionCommand( OptionsListener.COMMAND_SELECT );
        options[ 0 ].addActionListener( optionsListener );
        options[ 0 ].setMnemonic (NbBundle.getMessage( BrowseFolders.class, "MNE_BrowseFolders_Select_Option").charAt (0) );
        options[ 0 ].getAccessibleContext ().setAccessibleName (NbBundle.getMessage( BrowseFolders.class, "ACSN_BrowseFolders_Select_Option"));
        options[ 0 ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage( BrowseFolders.class, "ACSD_BrowseFolders_Select_Option"));
        options[ 1 ].setActionCommand( OptionsListener.COMMAND_CANCEL );
        options[ 1 ].addActionListener( optionsListener );    
        options[ 1 ].getAccessibleContext ().setAccessibleName (NbBundle.getMessage( BrowseFolders.class, "ACSN_BrowseFolders_Cancel_Option"));
        options[ 1 ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage( BrowseFolders.class, "ACSD_BrowseFolders_Cancel_Option"));
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            bf,                                     // innerPane
            NbBundle.getMessage( BrowseFolders.class, "LBL_BrowseFolders_Dialog"),                       // displayName
            true,                                   // modal
            options,                                // options
            options[ 0 ],                           // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null );                                 // listener 

        dialogDescriptor.setClosingOptions( new Object[] { options[ 0 ], options[ 1 ] } );
            
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.show();
        
        return optionsListener.getResult();
                
    }
    
    private void expandSelection( String preselectedFileName ) {
        
        Node root = manager.getRootContext();
        Children ch = root.getChildren();
        if ( ch == Children.LEAF ) {
            return;
        }
        Node nodes[] = ch.getNodes( true );
        
        Node sel = null;        
        
        if ( preselectedFileName != null && preselectedFileName.length() > 0 ) {
             // Try to find the node
             for ( int i = 0; i < nodes.length; i++ ) {            
                try { 
                    sel = NodeOp.findPath( nodes[i], (Enumeration) new StringTokenizer( preselectedFileName, "/" ) );
                    break;
                }
                catch ( NodeNotFoundException e ) {            
                    System.out.println( e.getMissingChildName() );
                    // Will select the first node
                }
             }
        }
                        
        if ( sel == null ) {
            // Node not found => expand first level
            btv.expandNode( root );
            for ( int i = 0; i < nodes.length; i++ ) {            
                btv.expandNode( nodes[i] );
                if ( i == 0 ) {
                    sel = nodes[i];
                }
            }
        }
        
        
        if ( sel != null ) {
            // Select the node
            try {
                manager.setSelectedNodes( new Node[] { sel } );
            }
            catch ( PropertyVetoException e ) {
                // No selection for some reason
            }
        }
                
    }
    
    
    /*
    private static void expandAllNodes( BeanTreeView btv, Node node ) {
        
        btv.expandNode( node );
        
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {
            return;
        }
        Node nodes[] = ch.getNodes( true );
        
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i] );
        }
        
    }
    */
    
    // Innerclasses ------------------------------------------------------------
    
    /** Children to be used to show FileObjects from given SourceGroups
     */
	 
    private static final class SourceGroupsChildren extends Children.Keys {
        
        private SourceGroup[] groups;
        private SourceGroup group;
        private FileObject fo;
        private Project project;
        
        public SourceGroupsChildren( SourceGroup[] groups, Project project ) {
            this.groups = groups;
            this.project = project;
        }
                
        public SourceGroupsChildren( FileObject fo, SourceGroup group ) {
            this.fo = fo;
            this.group = group;
        }
                
        protected void addNotify() {
            super.addNotify();
            setKeys( getKeys() );
        }
        
        protected void removeNotify() {
            setKeys( Collections.EMPTY_SET );
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            
            FileObject folder = null;
            SourceGroup group = null;
            
            if ( key instanceof SourceGroup ) {
                folder = ((SourceGroup)key).getRootFolder();
                group = (SourceGroup)key;
                FilterNode fn = new FilterNode( 
                    new PhysicalView.GroupNode( project, group, folder.equals( project.getProjectDirectory() ), DataFolder.findFolder( folder ) ), 
                    new SourceGroupsChildren( folder, group ) );
                return new Node[] { fn };
            }
            else if ( key instanceof Key ) {
                folder = ((Key)key).folder;
                group = ((Key)key).group;    
                FilterNode fn = new FilterNode( 
                    DataFolder.findFolder( folder ).getNodeDelegate(), 
                    new SourceGroupsChildren( folder, group ) );
                return new Node[] { fn };
            }
            else {
                return new Node[0];
            }
        }
        
        private Collection getKeys() {
			
            if ( groups != null ) {
                return Arrays.asList( groups );                
            }
            else {
                FileObject files[] = fo.getChildren();
                ArrayList children = new ArrayList( files.length );
                
                for( int i = 0; i < files.length; i++ ) {
                    if ( files[i].isFolder() && group.contains( files[i] ) && VisibilityQuery.getDefault().isVisible( files[i] ) ) {
                        children.add( new Key( files[i], group ) );
                    }
                }
                
                return children;
            }
			
        }
	
        private static class Key {
            
            private FileObject folder;
            private SourceGroup group;
            
            private Key ( FileObject folder, SourceGroup group ) {
                this.folder = folder;
                this.group = group;
            }
            
            
        }
        
    }

    
    private static final class OptionsListener implements ActionListener {
    
        public static final String COMMAND_SELECT = "SELECT";
        public static final String COMMAND_CANCEL = "CANCEL";
            
        private BrowseFolders browsePanel;
        
        private FileObject result;
        
        public OptionsListener( BrowseFolders browsePanel ) {
            this.browsePanel = browsePanel;
        }
        
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();

            if ( COMMAND_SELECT.equals( command ) ) {
                Node selection[] = browsePanel.getExplorerManager().getSelectedNodes();
                
                if ( selection != null && selection.length > 0 ) {
                    DataObject dobj = (DataObject)selection[0].getLookup().lookup( DataObject.class );
                    if ( dobj != null ) {
                        FileObject fo = dobj.getPrimaryFile();
                        if ( fo.isFolder() ) {
                            result = fo;
                        }
                    }
                }
                
                
            }
        }
        
        public FileObject getResult() {
            return result;
        }
    }
    
    
}
