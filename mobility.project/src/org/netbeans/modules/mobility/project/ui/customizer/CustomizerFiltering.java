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

/*
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicBorders;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.regex.CheckedTreeBeanView;
import org.netbeans.modules.mobility.project.ui.customizer.regex.FileObjectCookie;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class CustomizerFiltering extends JPanel implements CustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private static final String GENERAL_EXCLUDES = "^(.*/)?(([^/]*\\.class)|([^/]*\\.form)|(\\.nbintdb)|([^/]*\\.mvd)|([^/]*\\.wsclient)"; //NOI18N
    private static final String STANDARD_ANT_EXCLUDES = "([^/]*~)|(#[^/]*#)|(\\.#[^/]*)|(%[^/]*%)|(\\._[^/]*)|(CVS)|(CVS/.*)|(\\.cvsignore)|(SCCS)|(SCCS/.*)|(vssver\\.scc)|(\\.svn)|(\\.svn/.*)|(\\.DS_Store)";//NOI18N
    private static final String TEST_EXCLUDES = "([^/]*Test\\.java)|(test)|(test/.*)";//NOI18N
    
    static final String[] PROPERTY_GROUP = new String[] {DefaultPropertiesDescriptor.FILTER_USE_STANDARD, DefaultPropertiesDescriptor.FILTER_EXCLUDE_TESTS, DefaultPropertiesDescriptor.FILTER_EXCLUDES};
    
    private VisualPropertySupport vps;
    private CheckedTreeBeanView treeView;
    private ExplorerManager manager;
    /** The treee where to choose from */
    private ExplorerPanel sourceExplorer;
    private Pattern filter;
    private Map<String,Object> properties;
    private String configuration;
    private FileObject srcRoot;
    private String excludesTranslatedPropertyName;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerFiltering() {
        initComponents();
        initAccessibility();
        jPanelTree.setBorder(BasicBorders.getTextFieldBorder());
        sourceExplorer = new ExplorerPanel();
        
        manager = sourceExplorer.getExplorerManager();
        
        treeView = new CheckedTreeBeanView();
        try {
            Field f = CheckedTreeBeanView.class.getDeclaredField("tree");//NOI18N
            f.setAccessible(true);
            jLabelTree.setLabelFor((Component)f.get(treeView));
        } catch (Exception e){}
        treeView.setPopupAllowed(false);
        treeView.setRootVisible(false);
        treeView.setDefaultActionAllowed(false);
        sourceExplorer.setLayout(new BorderLayout());
        sourceExplorer.add(treeView, BorderLayout.CENTER);
        sourceExplorer.setPreferredSize(new Dimension(200, 250));
        jPanelTree.add(sourceExplorer, BorderLayout.CENTER);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheck = new javax.swing.JCheckBox();
        jCheckBoxCVS = new javax.swing.JCheckBox();
        jCheckBoxTests = new javax.swing.JCheckBox();
        jLabelTree = new javax.swing.JLabel();
        jPanelTree = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        defaultCheck.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerFiltering.class).getString("MNM_Use_Default").charAt(0));
        defaultCheck.setText(NbBundle.getMessage(CustomizerFiltering.class, "LBL_Use_Default"));
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(defaultCheck, gridBagConstraints);

        jCheckBoxCVS.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerFiltering.class).getString("MNM_CustFilter_DefaultExcludes").charAt(0));
        jCheckBoxCVS.setText(NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_DefaultExcludes"));
        jCheckBoxCVS.setToolTipText(NbBundle.getMessage(CustomizerFiltering.class, "TTT_CustFilter_DefaultExcludes"));
        jCheckBoxCVS.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jCheckBoxCVS, gridBagConstraints);

        jCheckBoxTests.setMnemonic(org.openide.util.NbBundle.getBundle(CustomizerFiltering.class).getString("MNM_CustFilter_ExcludeTests").charAt(0));
        jCheckBoxTests.setText(NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_ExcludeTests"));
        jCheckBoxTests.setToolTipText(NbBundle.getMessage(CustomizerFiltering.class, "TTT_CustFilter_ExcludeTests"));
        jCheckBoxTests.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBoxTests, gridBagConstraints);

        jLabelTree.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(CustomizerFiltering.class).getString("MNM_CustFilter_SelectFiles").charAt(0));
        jLabelTree.setLabelFor(sourceExplorer);
        jLabelTree.setText(NbBundle.getMessage(CustomizerFiltering.class, "LBL_CustFilter_SelectFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelTree, gridBagConstraints);

        jPanelTree.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jPanelTree, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerFiltering.class, "ACSN_CustFilter"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerFiltering.class, "ACSD_CustFilter"));
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.vps = VisualPropertySupport.getDefault(props);
        this.properties = props;
        this.configuration = configuration;
        this.srcRoot = props.getSourceRoot();
        treeView.setSrcRoot(srcRoot);
        vps.register(defaultCheck, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        jCheckBoxCVS.removeActionListener(this);
        jCheckBoxTests.removeActionListener(this);
        vps.register(jCheckBoxCVS, DefaultPropertiesDescriptor.FILTER_USE_STANDARD, useDefault);
        vps.register(jCheckBoxTests, DefaultPropertiesDescriptor.FILTER_EXCLUDE_TESTS, useDefault);
        this.excludesTranslatedPropertyName = VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.FILTER_EXCLUDES, useDefault);
        initTree();
        treeView.setEditable(!useDefault);
        jLabelTree.setEnabled(!useDefault);
        jCheckBoxCVS.addActionListener(this);
        jCheckBoxTests.addActionListener(this);
    }
    
    private void initTree() {
        String sFilter;
        if (jCheckBoxCVS.isSelected()) {
            if (jCheckBoxTests.isSelected()) {
                sFilter = GENERAL_EXCLUDES + "|" + STANDARD_ANT_EXCLUDES + "|" + TEST_EXCLUDES + ")$"; //NOI18N
            } else {
                sFilter = GENERAL_EXCLUDES + "|" + STANDARD_ANT_EXCLUDES + ")$"; //NOI18N
            }
        } else {
            if (jCheckBoxTests.isSelected()) {
                sFilter = GENERAL_EXCLUDES + "|" + TEST_EXCLUDES + ")$"; //NOI18N
            } else {
                sFilter = GENERAL_EXCLUDES + ")$"; //NOI18N
            }
        }
        this.filter = Pattern.compile(sFilter);
        try {
            final DataObject dob = DataObject.find(srcRoot);
            manager.setRootContext(new FOBNode(dob.getNodeDelegate().cloneNode(), dob.getPrimaryFile()));
        } catch (DataObjectNotFoundException dnfe) {
            manager.setRootContext(Node.EMPTY);
        }
        treeView.registerProperty(properties, excludesTranslatedPropertyName, filter);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JCheckBox jCheckBoxCVS;
    private javax.swing.JCheckBox jCheckBoxTests;
    private javax.swing.JLabel jLabelTree;
    private javax.swing.JPanel jPanelTree;
    // End of variables declaration//GEN-END:variables
    
    boolean acceptFileObject(final FileObject fo) {
        final String path = FileUtil.getRelativePath(srcRoot, fo);
        return path != null && !filter.matcher(path).matches();
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent e) {
        initTree();
    }
    
    private class FOBNode extends FilterNode implements FileObjectCookie {
        private final FileObject fo;
        public FOBNode(Node n, FileObject fo) {
            super(n, fo.isData() ? org.openide.nodes.Children.LEAF : new FOBChildren(n));
            this.fo = fo;
            disableDelegation(DELEGATE_SET_NAME | DELEGATE_GET_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_DISPLAY_NAME);
            setName(fo.getNameExt());
            setDisplayName(fo.getNameExt());
        }
        
        public Node.Cookie getCookie(final Class type) {
            if (FileObjectCookie.class.isAssignableFrom(type)) return this;
            return super.getCookie(type);
        }
        
        public FileObject getFileObject() {
            return fo;
        }
    }
    
    private class FOBChildren extends FilterNode.Children {
        
        public FOBChildren(Node or) {
            super(or);
        }
        
        
        protected Node[] createNodes(final Object k) {
            final Node n = (Node)k;
            final DataObject dob = (DataObject) n.getCookie(DataObject.class);
            if (dob == null) return new Node[0];
            final ArrayList<Node> nodes = new ArrayList<Node>();
            for (FileObject fo : (java.util.Set<FileObject>)dob.files()) {
                if (acceptFileObject(fo)) {
                    nodes.add(new FOBNode(n.cloneNode(), fo));
                }
            }
            return nodes.toArray(new Node[nodes.size()]);
        }
    }
    
    private static class ExplorerPanel extends JPanel implements ExplorerManager.Provider {
        private final ExplorerManager manager = new ExplorerManager();

        private ExplorerPanel() {
            //Just to avoid creation of accessor class
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
    }
}
