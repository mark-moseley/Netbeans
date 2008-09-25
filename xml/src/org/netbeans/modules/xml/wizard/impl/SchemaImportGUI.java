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
package org.netbeans.modules.xml.wizard.impl;


import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import javax.swing.tree.TreeSelectionModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openide.loaders.TemplateWizard;
import org.openide.loaders.DataFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xml.util.Util;
import org.openide.explorer.view.Visualizer;
import org.openide.util.NbBundle;



/**
 * This panel gathers data that are necessary for instantiting of XML
 * document conforming to given XML Schema.
 * <p>
 * Data allows to create a document that respect restrictions of current parser
 * implementations (they use schemaLocation hint specifically).
 *
 * @author  Petr Kuzel
 */
public class SchemaImportGUI extends JPanel implements ExplorerManager.Provider, PropertyChangeListener{
    /** Serial Version UID */
    private static final long serialVersionUID = -7568909683682244030L;    
    private transient ExplorerManager explorerManager;
    private TemplateWizard templateWizard;
    private ExternalReferenceDecorator decorator;
    /** Map of registered nodes, keyed by their representative DataObject. */
    private Map registeredNodes;
    private FileObject primarySchema=null;
    private boolean first= false;
    private boolean removeFlag=false;
    
    /** Creates new form SchemaPanel */
    public SchemaImportGUI(TemplateWizard tw) {
        initComponents();
        initAccessibility();
        this.templateWizard = tw;
        registeredNodes = new HashMap();
        decorator = new ExternalReferenceDecorator(this);            
         // View for selecting an external reference.
        TreeTableView locationView = new LocationView();
        locationView.setDefaultActionAllowed(false);
        locationView.setPopupAllowed(false);
        locationView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        locationView.setRootVisible(false);
        locationView.getAccessibleContext().setAccessibleName(locationLabel.getToolTipText());
        locationView.getAccessibleContext().setAccessibleDescription(locationLabel.getToolTipText());
        Node.Property[] columns = new Node.Property[] {
            new Column(ExternalReferenceDataNode.PROP_NAME, String.class, true),
            new ImportColumn(referenceTypeName()),
            //new Column(ExternalReferenceDataNode.PROP_PREFIX, String.class, false),
        };
        locationView.setProperties(columns);
        locationView.setTreePreferredWidth(200);
        locationView.setTableColumnPreferredWidth(0, 25);
//        locationView.setTableColumnPreferredWidth(1, 25);
        locationPanel.add(locationView, BorderLayout.CENTER);
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        explorerManager.setRootContext(createRootNode());        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationPanel = new javax.swing.JPanel();
        locationLabel = new javax.swing.JLabel();

        setName(Util.THIS.getString(SchemaImportGUI.class, "PROP_schema_panel_name")); // NOI18N

        locationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        locationPanel.setLayout(new java.awt.BorderLayout());

        locationLabel.setLabelFor(locationPanel);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(SchemaImportGUI.class, "LBL_SchemaPanel_Location")); // NOI18N
        locationLabel.setToolTipText(org.openide.util.NbBundle.getMessage(SchemaImportGUI.class, "TIP_SchemaPanel_Location")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, locationLabel)
                    .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(locationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(locationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 247, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void primarySchemaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primarySchemaCheckBoxActionPerformed
 //   fireChange();

}//GEN-LAST:event_primarySchemaCheckBoxActionPerformed

    private void initAccessibility() {

        // memonics
        Util util = Util.THIS;        
        locationLabel.setDisplayedMnemonic(util.getChar(
                SchemaImportGUI.class, "PROP_schema_locationLabel_mne"));
        
    }
    
    protected class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;
        
        /**
         * Constructs a new instance of Column.
         *
         * @param  key   keyword for this column.
         * @param  type  type of the property (e.g. String.class).
         * @param  tree  true if this is the 'tree' column.
         */
        public Column(String key, Class type, boolean tree) {
            super(key, type, NbBundle.getMessage(Column.class,
                    "CTL_SchemaPanel_Column_Name_" + key),
                  NbBundle.getMessage(Column.class,
                    "CTL_SchemaPanel_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
        }
        
        public Object getValue()
        throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }
    
    protected class ImportColumn extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Creates a new instance of ImportColumn.
         *
         * @param  name  the column's name.
         */
        public ImportColumn(String name) {
            super("selected", Boolean.TYPE, name,NbBundle.getMessage(Column.class,
                    "CTL_SchemaPanel_Column_Desc_selected"));
            this.key = "selected";
            setValue("TreeColumnTTV", Boolean.FALSE);
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }
    
     protected Node createRootNode() {
        try {
                DataFolder folder = templateWizard.getTargetFolder();
                Project project = FileOwnerQuery.getOwner(folder.getPrimaryFile());
                SubprojectProvider provider = (SubprojectProvider)project.getLookup().lookup(SubprojectProvider.class);
                Set refProjects= provider.getSubprojects();
                Iterator it = refProjects.iterator();

                Node[] rootNodes = new Node[1 + (refProjects == null ? 0 : refProjects.size())];
                LogicalViewProvider viewProvider = (LogicalViewProvider) project.getLookup().lookup(LogicalViewProvider.class);
                rootNodes[0] = decorator.createExternalReferenceNode(viewProvider.createLogicalView());
                int rootIndex = 1;

                java.util.List projectRoots = new java.util.ArrayList();
                projectRoots.add(project.getProjectDirectory());
                if (refProjects != null) {
                    while(it.hasNext()){
                   // for (Object o : refProjects) {
                        Object o = it.next();
                        Project refPrj = (Project) o;
                        viewProvider = (LogicalViewProvider) refPrj.getLookup().
                                lookup(LogicalViewProvider.class);
                        rootNodes[rootIndex++] = decorator.createExternalReferenceNode(viewProvider.createLogicalView());
                        projectRoots.add(refPrj.getProjectDirectory());
                    }
                }
                FileObject[] roots = (FileObject[])projectRoots.toArray(new FileObject[projectRoots.size()]);
                Children fileChildren = new Children.Array();
                fileChildren.add(rootNodes);
                Node byFilesNode = new FolderNode(fileChildren);
                byFilesNode.setDisplayName(NbBundle.getMessage(
                SchemaImportGUI.class,
                "LBL_SchemaPanel_Category_By_File"));

                // Construct the By Namespace node.
                Children nsChildren = new NamespaceChildren(roots, decorator);
                Node byNsNode = new FolderNode(nsChildren);
                byNsNode.setDisplayName(NbBundle.getMessage(
                SchemaImportGUI.class,
                "LBL_SchemaPanel_Category_By_Namespace"));
                Children categories = new Children.Array();
                categories.add(new Node[] { byFilesNode, byNsNode });
                Node rootNode = new AbstractNode(categories);
                // Surprisingly, this becomes the name and description of the first column.
                rootNode.setDisplayName(NbBundle.getMessage(SchemaImportGUI.class,
                "CTL_SchemaPanel_Column_Name_name"));
                rootNode.setShortDescription(NbBundle.getMessage(SchemaImportGUI.class,
                "CTL_SchemaPanel_Column_Desc_name"));
                return rootNode;
         } catch(Exception e){
             e.printStackTrace();
         }
        return null;
    }
     
     public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
     
     
      public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        DataObject dobj = (DataObject) original.getLookup().lookup(DataObject.class);
        NodeSet set = (NodeSet)registeredNodes.get(dobj);
        if (set == null) {
            set = new NodeSet(this);
            registeredNodes.put(dobj, set);
        }
        ExternalReferenceDataNode erdn = new ExternalReferenceDataNode(original, decorator);
        set.add(erdn);
        if (set.isSelected() && erdn.canSelect()) {
            erdn.setSelected(true);
        }
        erdn.addPropertyChangeListener(this);
        return erdn;
    }
    
    /**
     * Manages the state of a set of nodes.
     */
    private static class NodeSet {
        /** The property change listener for each node. */
        private PropertyChangeListener listener;
        /** Nodes in this set. */
        private List nodes;
        /** True if this set is selected, false otherwise. */
        private boolean selected;

        /**
         * Creates a new instance of NodeSet.
         *
         * @param  listener  listens to the Node.
         */
        public NodeSet(PropertyChangeListener listener) {
            this.listener = listener;
        }

        /**
         * Add the given node to this set.
         *
         * @param  node  node to be added to set.
         */
        public void add(ExternalReferenceDataNode node) {
            if (nodes == null) {
                nodes = new LinkedList();
            }
            nodes.add(node);
        }

        /**
         * Returns the list of nodes in this set.
         *
         * @return  list of nodes.
         */
        public List getNodes() {
            return nodes;
        }

        /**
         * Indicates if this set is selected or not.
         *
         * @return  true if selected, false otherwise.
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Set the prefix for Nodes in this group.
         *
         * @param  prefix  new namespace prefix.
         */
        public void setPrefix(String prefix) {
            for(int i=0; i <nodes.size(); i++ ){
                ExternalReferenceDataNode node = (ExternalReferenceDataNode)nodes.get(i);
                if (!node.getPrefix().equals(prefix)) {
                    node.removePropertyChangeListener(listener);
                    node.setPrefix(prefix);
                    node.addPropertyChangeListener(listener);
                }
            }
        }

        /**
         * Set this group of Nodes as being selected.
         *
         * @param  select  true to select, false to de-select.
         */
        public void setSelected(boolean select) {
            selected = select;
            for(int i=0; i <nodes.size(); i++ ){
                ExternalReferenceDataNode node = (ExternalReferenceDataNode)nodes.get(i);
                if (node.canSelect()) {
                    node.removePropertyChangeListener(listener);
                    node.setSelected(select);
                    node.addPropertyChangeListener(listener);
                }
            }
        }
    }
    
    
    public void propertyChange(PropertyChangeEvent event) {
        String pname = event.getPropertyName();
        if (ExplorerManager.PROP_SELECTED_NODES.equals(pname)) {
            Node[] nodes = (Node[]) event.getNewValue();
            // Validate the node selection.
            if (nodes != null && nodes.length > 0 && nodes[0] instanceof ExternalReferenceDataNode) {
                ExternalReferenceDataNode node = (ExternalReferenceDataNode) nodes[0];
                validateInput(node);
            }
        } else if (pname.equals(ExternalReferenceDataNode.PROP_PREFIX)) {
            ExternalReferenceDataNode erdn =  (ExternalReferenceDataNode) event.getSource();
            // Look up the node in the map of sets, and ensure they all
            // have the same prefix.
            String prefix = (String) event.getNewValue();
            DataObject dobj = (DataObject) erdn.getLookup().lookup(DataObject.class);
            NodeSet set = (NodeSet)registeredNodes.get(dobj);
            // Ideally the set should already exist, but cope gracefully.
            assert set != null : "node not created by customizer";
            if (set == null) {
                set = new NodeSet(this);
                set.add(erdn);
            }
            set.setPrefix(prefix);
            validateInput(erdn);
        } else if (pname.equals(ExternalReferenceDataNode.PROP_SELECTED)) {
            ExternalReferenceDataNode erdn = (ExternalReferenceDataNode) event.getSource();
            // Look up the node in the map of sets, and ensure they are all
            // selected as a unit.
            boolean selected = ((Boolean) event.getNewValue()).booleanValue();
            DataObject dobj = (DataObject) erdn.getLookup().lookup(DataObject.class);
            NodeSet set = (NodeSet)registeredNodes.get(dobj);
            // Ideally the set should already exist, but cope gracefully.
            assert set != null : "node not created by customizer";
            if (set == null) {
                set = new NodeSet(this);
                set.add(erdn);
            }
            set.setSelected(selected);
            //setPrimarySchema(erdn, selected, false); 
            // Check if the current selection is valid.
            validateInput(erdn);
        }
    }
    
     /**
     * Determine if the user's input is valid or not. This will enable
     * or disable the save/reset controls based on the results, as well
     * as issue error messages.
     *
     * @param  node  selected node.
     */
    private void validateInput(ExternalReferenceDataNode erdn) {
        String msg = null;
        String ep = erdn.getPrefix();
            // Must be a non-empty prefix, that is not already in use, and
            // is unique among the selected nodes (and be selected itself).
        if (ep.length() == 0 || (!isValidPrefix(erdn) && erdn.isSelected())) {
                msg = NbBundle.getMessage(SchemaImportGUI.class, "LBL_SchemaPanel_InvalidPrefix");
            }
       
       
        int selected = countSelectedNodes();
        if(selected < 0 )
            msg = "ERROR MSG";
        // Must have selected nodes, and no error messages.
        //setSaveEnabled((allowEmptySelection() || selected > 0) );
    }
    
    /**
     * Check if prefix is unique on UI.
     *
     * @return  true if Prefix is not unique on UI, false otherwise.
     */
    private boolean isValidPrefix(ExternalReferenceDataNode node) {
        DataObject dobj = (DataObject) node.getLookup().lookup(DataObject.class);
        NodeSet nodeSet = (NodeSet)registeredNodes.get(dobj);
        Collection sets = registeredNodes.values();
        Iterator it = sets.iterator();
        while(it.hasNext()) {
            // Ignore the set which contains the given node, and those
            // sets which are not selected.
            NodeSet set = (NodeSet)it.next();
            if (!set.equals(nodeSet) && set.isSelected()) {
                // Only need to check the first node, as all of them have
                // the same prefix (or at least that is the idea).
                ExternalReferenceDataNode other = (ExternalReferenceDataNode)set.getNodes().get(0);
                if (node.getPrefix().equals(other.getPrefix())) {
                    return false;
                }
            }
        }
        return true;
    }
    
   
    /**
     * Determine the number of nodes that the user selected, useful for
     * knowing if any nodes are selected or not.
     *
     * @return  number of selected nodes.
     */
    public int countSelectedNodes() {
        int results = 0;
        Collection sets = registeredNodes.values();
        Iterator it = sets.iterator();
        while(it.hasNext()){
            NodeSet set = (NodeSet)it.next();
            List nodes = set.getNodes();
            if (nodes.size() > 0) {
                results++;
            }
        }
        
        return results;
    }
    
    
    /**
     * A TreeTableView that toggles the selection of the external reference
     * data nodes using a single mouse click.
     */
    private class LocationView extends TreeTableView {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of LocationView.
         */
        public LocationView() {
            super();
            tree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    // Invert the selection of the data node, if such a
                    // node was clicked on.
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        Object comp = path.getLastPathComponent();
                        Node node = Visualizer.findNode(comp);
                        if (node instanceof ExternalReferenceDataNode) {
                            ExternalReferenceDataNode erdn =
                                    (ExternalReferenceDataNode) node;
                            if (erdn.canSelect()) {
                                boolean selected = !erdn.isSelected();
                                erdn.setSelected(selected);
                                //setPrimarySchema(erdn, selected, true);
                                
                            }
                        }
                    }
                }
            });
        }
    }
     
       
    public boolean isPrimarySchemaSelected() {
        if(rootModel.getSize() == 0)
            return false;
        else 
            return true;
    }
    
     /**
     * Retrieve the list of nodes that the user selected.
     *
     * @return  list of selected nodes (empty if none).
     */
    protected List getSelectedNodes() {
        List results = new LinkedList();
        Collection sets = registeredNodes.values();
        Iterator it = sets.iterator();
        while(it.hasNext()){
            NodeSet set = (NodeSet)it.next();
            if (set.isSelected()) {
                List nodes = set.getNodes();
                if (nodes.size() > 0) {
                    // Use just one of the corresponding nodes, as the
                    // others are basically duplicates.
                    results.add(nodes.get(0));
                }
            }
        }
        return results;
    }
    
 /*   private void setPrimarySchema(ExternalReferenceDataNode erdn, boolean selected, boolean fromTreeView) {
        String ns = null;
        DataObject dobj = (DataObject) erdn.getLookup().lookup(DataObject.class);
        FileObject fobj = dobj.getPrimaryFile();
        
         //if the schema was selected by clicking in the import column, then we dont need to set this
        if(fromTreeView) {
            erdn.setSelected(selected);
            return;
        }
        
        if (selected) {
            // Have to collect the namespace value
            // when the node is selected.
            if(!fobj.isFolder()) {
                ns= erdn.getNamespace();
                 String key = fobj.getNameExt() + " (" + ns + ")" ;
                 removeFlag=false;
                 namespaceModel.addElement(new SchemaComboItem(key, fobj));
            }
        } else {
            if(!fobj.isFolder()) {
                ns=erdn.getNamespace();
                String key=fobj.getNameExt() + " (" + ns + ")";
                for(int i = 0 ; i<namespaceModel.getSize()  ; i++ ) {
                    SchemaComboItem item = (SchemaComboItem)namespaceModel.getElementAt(i);
                    if(key.equals(item.toString()) && fobj.equals(item.getValue()) ) {
                        removeFlag=true;
                        namespaceModel.removeElement(item);
                        break;
                    }
                }
                
                
            }
            
        }             
    } */
        
    protected String referenceTypeName() {
        return NbBundle.getMessage(SchemaImportGUI.class,
                "LBL_SchemaPanel_ImportCreator_Type");
    }
    
    private class SchemaComboItem {
        String name;
        FileObject value;
        
        public SchemaComboItem(String key, FileObject val){
            name=key;
            this.value=val;
        }
        
        public FileObject getValue(){
            return value;
        }
        
        public String toString() {
            return name;
        }
    }
    
    private DefaultComboBoxModel nsModel;
    private DefaultComboBoxModel rootModel;
    private DefaultComboBoxModel namespaceModel;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel locationLabel;
    private javax.swing.JPanel locationPanel;
    // End of variables declaration//GEN-END:variables

   
}
