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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;

/**
 * Represents <em>Versioning</em> panel in Netbeans Module customizer.
 *
 * @author Martin Krauskopf
 */
final class CustomizerVersioning extends JPanel implements ComponentFactory.StoragePanel {
    
    private static final int CHECKBOX_WIDTH = new JCheckBox().getWidth();
    
    private NbModuleProperties props;
    private boolean lastAppImplChecked;
    
    /** Creates new form CustomizerVersioning */
    CustomizerVersioning(NbModuleProperties props, TableModel model) {
        this.props = props;
        initComponents();
        initPublicPackageTable(model);
        cnbValue.setText(props.getCodeNameBase());
        majorRelVerValue.setText(props.getMajorReleaseVersion());
        String specVersion = props.getSpecificationVersion();
        if (null == specVersion || "".equals(specVersion)) { // NOI18N
            appendImpl.setSelected(true);
            specificationVerValue.setText(
                    props.getProperty(NbModuleProperties.SPEC_VERSION_BASE));
        } else {
            specificationVerValue.setText(specVersion);
        }
        implVerValue.setText(props.getImplementationVersion());
        regularMod.setSelected(true);
        autoloadMod.setSelected(isTrue(props.getProperty(NbModuleProperties.IS_AUTOLOAD)));
        eagerMod.setSelected(isTrue(props.getProperty(NbModuleProperties.IS_EAGER)));
        implVerValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateAppendImpl();
            }
            public void removeUpdate(DocumentEvent e) { insertUpdate(null); }
            public void changedUpdate(DocumentEvent e) {}
        });
        updateAppendImpl();
    }
    
    private void initPublicPackageTable(TableModel model) {
        publicPkgsTable.setModel(model);
        publicPkgsTable.getColumnModel().getColumn(0).setMaxWidth(CHECKBOX_WIDTH + 20);
        publicPkgsTable.setTableHeader(null);
        publicPkgsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publicPkgsSP.getViewport().setBackground(publicPkgsTable.getBackground());
        final Action switchAction = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Boolean b = (Boolean) publicPkgsTable.
                        getValueAt(publicPkgsTable.getSelectedRow(), 0);
                publicPkgsTable.setValueAt(Boolean.valueOf(!b.booleanValue()),
                        publicPkgsTable.getSelectedRow(), 0);
            }
        };
        publicPkgsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                switchAction.actionPerformed(null);
            }
        });
        publicPkgsTable.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing"); // NOI18N
        publicPkgsTable.getActionMap().put("startEditing", switchAction); // NOI18N
    }
    
    // XXX this is not correct yet. Impl. deps. in Libraries panel have to be
    // considered
    private void updateAppendImpl() {
        boolean isEmpty = "".equals(implVerValue.getText().trim()); // NOI18N
        if (isEmpty && appendImpl.isEnabled()) {
            appendImpl.setEnabled(false);
            lastAppImplChecked = appendImpl.isSelected();
            appendImpl.setSelected(false);
        } else if (!isEmpty && !appendImpl.isEnabled()) {
            appendImpl.setEnabled(true);
            appendImpl.setSelected(lastAppImplChecked);
        }
    }
    
    public void store() {
        props.setMajorReleaseVersion(majorRelVerValue.getText().trim());
        String specVer = specificationVerValue.getText().trim();
        if (appendImpl.isSelected()) {
            props.setSpecificationVersion(""); // NOI18N
            props.setProperty(NbModuleProperties.SPEC_VERSION_BASE, specVer);
        } else {
            props.setSpecificationVersion(specVer);
            props.setProperty(NbModuleProperties.SPEC_VERSION_BASE, ""); // NOI18N
        }
        props.setImplementationVersion(implVerValue.getText().trim());
        props.setProperty(NbModuleProperties.IS_AUTOLOAD,
                Boolean.toString(autoloadMod.isSelected()));
        props.setProperty(NbModuleProperties.IS_EAGER,
                Boolean.toString(eagerMod.isSelected()));
    }
    
    private boolean isTrue(String bString) {
        return bString != null && bString.equalsIgnoreCase("true");
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleTypeGroup = new javax.swing.ButtonGroup();
        cnb = new javax.swing.JLabel();
        cnbValue = new javax.swing.JTextField();
        majorRelVer = new javax.swing.JLabel();
        majorRelVerValue = new javax.swing.JTextField();
        specificationVer = new javax.swing.JLabel();
        specificationVerValue = new javax.swing.JTextField();
        implVer = new javax.swing.JLabel();
        implVerValue = new javax.swing.JTextField();
        tokens = new javax.swing.JLabel();
        tokensValue = new javax.swing.JTextField();
        appendImpl = new javax.swing.JCheckBox();
        regularMod = new javax.swing.JRadioButton();
        autoloadMod = new javax.swing.JRadioButton();
        eagerMod = new javax.swing.JRadioButton();
        publicPkgs = new javax.swing.JLabel();
        friends = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        friendsSP = new javax.swing.JScrollPane();
        friendsValue = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        addFriendButton = new javax.swing.JButton();
        removeFriendButton = new javax.swing.JButton();
        filler1 = new javax.swing.JLabel();
        publicPkgsSP = new javax.swing.JScrollPane();
        publicPkgsTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        cnb.setLabelFor(cnbValue);
        org.openide.awt.Mnemonics.setLocalizedText(cnb, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_CNB"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(cnb, gridBagConstraints);

        cnbValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(cnbValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(majorRelVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_MajorReleaseVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(majorRelVer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(majorRelVerValue, gridBagConstraints);

        specificationVer.setLabelFor(specificationVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(specificationVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_SpecificationVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 12);
        add(specificationVer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(specificationVerValue, gridBagConstraints);

        implVer.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(implVer, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ImplementationVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(implVer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(implVerValue, gridBagConstraints);

        tokens.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(tokens, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ProvidedTokens"));
        tokens.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(tokens, gridBagConstraints);

        tokensValue.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(tokensValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(appendImpl, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AppendImplementation"));
        appendImpl.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        appendImpl.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(appendImpl, gridBagConstraints);

        moduleTypeGroup.add(regularMod);
        regularMod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(regularMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RegularModule"));
        regularMod.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        regularMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(regularMod, gridBagConstraints);

        moduleTypeGroup.add(autoloadMod);
        org.openide.awt.Mnemonics.setLocalizedText(autoloadMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AutoloadModule"));
        autoloadMod.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        autoloadMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(autoloadMod, gridBagConstraints);

        moduleTypeGroup.add(eagerMod);
        org.openide.awt.Mnemonics.setLocalizedText(eagerMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_EagerModule"));
        eagerMod.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        eagerMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(eagerMod, gridBagConstraints);

        publicPkgs.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(publicPkgs, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_PublicPackages"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 12);
        add(publicPkgs, gridBagConstraints);

        friends.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(friends, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_Friends"));
        friends.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(friends, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        friendsValue.setColumns(20);
        friendsValue.setRows(5);
        friendsValue.setEnabled(false);
        friendsSP.setViewportView(friendsValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        bottomPanel.add(friendsSP, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AddButton"));
        addFriendButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addFriendButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RemoveButton"));
        removeFriendButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        buttonPanel.add(removeFriendButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 1.0;
        buttonPanel.add(filler1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        bottomPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(bottomPanel, gridBagConstraints);

        publicPkgsTable.setShowHorizontalLines(false);
        publicPkgsSP.setViewportView(publicPkgsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.8;
        add(publicPkgsSP, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFriendButton;
    private javax.swing.JCheckBox appendImpl;
    private javax.swing.JRadioButton autoloadMod;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel cnb;
    private javax.swing.JTextField cnbValue;
    private javax.swing.JRadioButton eagerMod;
    private javax.swing.JLabel filler1;
    private javax.swing.JLabel friends;
    private javax.swing.JScrollPane friendsSP;
    private javax.swing.JTextArea friendsValue;
    private javax.swing.JLabel implVer;
    private javax.swing.JTextField implVerValue;
    private javax.swing.JLabel majorRelVer;
    private javax.swing.JTextField majorRelVerValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel publicPkgs;
    private javax.swing.JScrollPane publicPkgsSP;
    private javax.swing.JTable publicPkgsTable;
    private javax.swing.JRadioButton regularMod;
    private javax.swing.JButton removeFriendButton;
    private javax.swing.JLabel specificationVer;
    private javax.swing.JTextField specificationVerValue;
    private javax.swing.JLabel tokens;
    private javax.swing.JTextField tokensValue;
    // End of variables declaration//GEN-END:variables
    
}
