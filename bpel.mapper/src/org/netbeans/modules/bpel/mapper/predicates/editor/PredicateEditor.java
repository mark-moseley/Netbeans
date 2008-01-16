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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.mapper.predicates.editor;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.GraphExpandProcessor;
import org.netbeans.modules.bpel.mapper.palette.Palette;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 *
 * @author  nk160297
 */
public class PredicateEditor extends EditorLifeCycleAdapter {
    
    private Mapper mMapper;
    private MapperModel mMapperModel;
    private XPathSchemaContext mSContext;
    private AbstractPredicate mPred;
    private String mDlgTitle;
        
    public PredicateEditor(XPathSchemaContext sContext, 
            AbstractPredicate pred, MapperModel mapperModel) {
        mSContext = sContext;
        mPred = pred;
        mMapperModel = mapperModel;
        //
        createContent();
        initControls();
    }
    
    public Mapper getMapper() {
        return mMapper;
    }
    
    @Override
    public void createContent() {
        assert EventQueue.isDispatchThread();
        mMapper = PredicatesMapperFactory.createMapper(mMapperModel);
        initComponents();
        //
        mDlgTitle = NbBundle.getMessage(PredicateEditor.class,
            "PREDICATE_DLG_TITLE"); // NOI18N
        //
        btnGoToContext.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (mSContext != null) {
                    showSchemaContextInSourceTree();
                }
            }
        });
    }

    @Override
    public boolean initControls() {
        if (mSContext != null) {
            fldContext.setText(mSContext.toString());
            showSchemaContextInSourceTree();
            GraphExpandProcessor.expandAllGraphs(mMapper, mMapperModel);
        } else {
            fldContext.setText(NbBundle.getMessage(PredicateEditor.class,
            "UNKNOWN_SCHEMA_CONTEXT"));
        }
        return true;
    }
    
    public String getDlgTitle() {
        return mDlgTitle;
    }
    
    /**
     * Returns true if the user press Ok
     * @param editor
     * @return
     */
    public static boolean showDlg(PredicateEditor editor) {
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(editor, editor.getDlgTitle());
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUiUtil.setInitialFocusComponentFor(editor);
        dialog.setVisible(true);

        return descriptor.isOkHasPressed();
    }
    
    private JPanel createPalette() {
        return new Palette(mMapper).getPanel();
    }
    
    /**
     * Try open source tree and show the Schema component that the 
     * schema context points to.
     */
    private void showSchemaContextInSourceTree() {
        // TODO: A Variable or a part has to be passed here 
        // TODO: Need constructing finders here
        if (mSContext == null) {
            return;
        }
        //
        // Prepare finders' list
        List<TreeItemFinder> finderList = FinderListBuilder.build(mSContext);
        //
        // Look for the tree node
        TreeModel leftTreeModel = mMapperModel.getLeftTreeModel();
        assert leftTreeModel instanceof MapperSwingTreeModel;
        TreePath schemaContextPath = ((MapperSwingTreeModel)leftTreeModel).
                findFirstNode(finderList);
        //
        // Show context path
        if (schemaContextPath != null) {
            JTree tree = mMapper.getLeftTree();
            tree.expandPath(schemaContextPath);
            tree.scrollPathToVisible(schemaContextPath);
            tree.setSelectionPath(schemaContextPath);
        }
       
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMenu = createPalette();
        javax.swing.JPanel pnlMapper = mMapper;
        lblContext = new javax.swing.JLabel();
        fldContext = new javax.swing.JTextField();
        btnGoToContext = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 600));

        pnlMapper.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        lblContext.setLabelFor(fldContext);
        lblContext.setText(org.openide.util.NbBundle.getMessage(PredicateEditor.class, "LBL_Context")); // NOI18N

        fldContext.setEditable(false);

        btnGoToContext.setText(org.openide.util.NbBundle.getMessage(PredicateEditor.class, "BTN_Context")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pnlMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .add(pnlMenu, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(lblContext)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldContext, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnGoToContext)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pnlMenu, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblContext)
                    .add(btnGoToContext)
                    .add(fldContext, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoToContext;
    private javax.swing.JTextField fldContext;
    private javax.swing.JLabel lblContext;
    private javax.swing.JPanel pnlMenu;
    // End of variables declaration//GEN-END:variables
    
}
