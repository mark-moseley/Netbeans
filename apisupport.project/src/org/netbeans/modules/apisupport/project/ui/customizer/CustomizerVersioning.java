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

import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Versioning</em> panel in Netbeans Module customizer.
 *
 * @author Martin Krauskopf
 */
final class CustomizerVersioning extends NbPropertyPanel.Single
        implements PropertyChangeListener, BasicCustomizer.SubCategoryProvider {
    
    private static final int CHECKBOX_WIDTH = new JCheckBox().getWidth();
    
    private boolean lastAppImplChecked;
    
    /** Creates new form CustomizerVersioning */
    CustomizerVersioning(SingleModuleProperties props) {
        super(props, CustomizerVersioning.class);
        initComponents();
        initAccesibility();
        initPublicPackageTable();
        refresh();
        attachListeners();
    }
    
    void refresh() {
        UIUtil.setText(majorRelVerValue, getProperties().getMajorReleaseVersion());
        UIUtil.setText(tokensValue, getProperties().getProvidedTokens());
        String specVersion = getProperties().getSpecificationVersion();
        if (null == specVersion || "".equals(specVersion)) { // NOI18N
            appendImpl.setSelected(true);
            UIUtil.setText(specificationVerValue, getProperty(SingleModuleProperties.SPEC_VERSION_BASE));
        } else {
            UIUtil.setText(specificationVerValue, specVersion);
        }
        UIUtil.setText(implVerValue, getProperties().getImplementationVersion());
        friendsList.setModel(getProperties().getFriendListModel());
        UIUtil.setText(cnbValue, getProperties().getCodeNameBase());
        regularMod.setSelected(true);
        autoloadMod.setSelected(getBooleanProperty(SingleModuleProperties.IS_AUTOLOAD));
        eagerMod.setSelected(getBooleanProperty(SingleModuleProperties.IS_EAGER));
        removeFriendButton.setEnabled(false);
        updateAppendImpl();
        checkForm();
    }
    
    private void attachListeners() {
        implVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateAppendImpl();
            }
        });
        friendsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    removeFriendButton.setEnabled(friendsList.getSelectedIndex() != -1);
                }
            }
        });
        majorRelVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkForm();
            }
        });
        implVerValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateAppendImpl();
            }
        });
    }
    
    boolean isCustomizerValid() {
        return checkMajorReleaseVersion();
    }
    
    private boolean checkMajorReleaseVersion() {
        String mrv = majorRelVerValue.getText().trim();
        if (mrv.length() != 0) {
            try {
                if (Integer.parseInt(mrv) < 0) {
                    return false;
                }
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
        return true;
    }
    
    private void checkForm() {
        exportOnlyToFriend.setSelected(getFriendModel().getSize() > 0);
        // check major release version
        if (!checkMajorReleaseVersion()) {
            setErrorMessage(getMessage("MSG_MajorReleaseVersionIsInvalid")); // NOI18N
        } else if (exportOnlyToFriend.isSelected() && getPublicPackagesModel().getSelectedPackages().length < 1) {
            setErrorMessage(getMessage("MSG_PublicPackageMustBeSelected"));
        } else {
            setErrorMessage(null);
        }
    }
    
    private void initPublicPackageTable() {
        publicPkgsTable.setModel(getProperties().getPublicPackagesModel());
        publicPkgsTable.getColumnModel().getColumn(0).setMaxWidth(CHECKBOX_WIDTH + 20);
        publicPkgsTable.setRowHeight(publicPkgsTable.getFontMetrics(publicPkgsTable.getFont()).getHeight() +
                (2 * publicPkgsTable.getRowMargin()));
        publicPkgsTable.setTableHeader(null);
        publicPkgsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publicPkgsSP.getViewport().setBackground(publicPkgsTable.getBackground());
        final Action switchAction = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = publicPkgsTable.getSelectedRow();
                if (row == -1) {
                    // Nothing selected; e.g. user has tabbed into the table but not pressed Down key.
                    return;
                }
                Boolean b = (Boolean) publicPkgsTable.
                        getValueAt(row, 0);
                publicPkgsTable.setValueAt(Boolean.valueOf(!b.booleanValue()),
                        row, 0);
                checkForm();
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
    
    private void updateAppendImpl() {
        boolean isImplVerFiled = !"".equals(implVerValue.getText().trim()); // NOI18N
        boolean shouldEnable = isImplVerFiled || getProperties().dependingOnImplDependency();
        if (shouldEnable && !appendImpl.isEnabled()) {
            appendImpl.setEnabled(true);
            appendImpl.setSelected(lastAppImplChecked);
        } else if (!shouldEnable && appendImpl.isEnabled()) {
            appendImpl.setEnabled(false);
            lastAppImplChecked = appendImpl.isSelected();
            appendImpl.setSelected(false);
        }
    }
    
    public void store() {
        getProperties().setMajorReleaseVersion(majorRelVerValue.getText().trim());
        String specVer = specificationVerValue.getText().trim();
        if (appendImpl.isSelected()) {
            getProperties().setSpecificationVersion(""); // NOI18N
            setProperty(SingleModuleProperties.SPEC_VERSION_BASE, specVer);
        } else {
            getProperties().setSpecificationVersion(specVer);
            setProperty(SingleModuleProperties.SPEC_VERSION_BASE, ""); // NOI18N
        }
        getProperties().setImplementationVersion(implVerValue.getText().trim());
        getProperties().setProvidedTokens(tokensValue.getText().trim());
        setBooleanProperty(SingleModuleProperties.IS_AUTOLOAD, autoloadMod.isSelected());
        setBooleanProperty(SingleModuleProperties.IS_EAGER, eagerMod.isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String pName = evt.getPropertyName();
        if (SingleModuleProperties.DEPENDENCIES_PROPERTY == pName) {
            updateAppendImpl();
        } else if (SingleModuleProperties.PROPERTIES_REFRESHED == pName) {
            refresh();
        }
    }
    
    private CustomizerComponentFactory.FriendListModel getFriendModel() {
        return (CustomizerComponentFactory.FriendListModel) friendsList.getModel();
    }
    
    private CustomizerComponentFactory.PublicPackagesTableModel getPublicPackagesModel() {
        return (CustomizerComponentFactory.PublicPackagesTableModel) publicPkgsTable.getModel();
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
        publicPkgs = new javax.swing.JLabel();
        publicPkgsSP = new javax.swing.JScrollPane();
        publicPkgsTable = new javax.swing.JTable();
        bottomPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        addFriendButton = new javax.swing.JButton();
        removeFriendButton = new javax.swing.JButton();
        filler1 = new javax.swing.JLabel();
        friendsSP = new javax.swing.JScrollPane();
        friendsList = new javax.swing.JList();
        exportOnlyToFriend = new javax.swing.JCheckBox();
        typePanel = new javax.swing.JPanel();
        regularMod = new javax.swing.JRadioButton();
        autoloadMod = new javax.swing.JRadioButton();
        eagerMod = new javax.swing.JRadioButton();
        typeTxt = new javax.swing.JLabel();

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

        majorRelVer.setLabelFor(majorRelVerValue);
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

        tokens.setLabelFor(tokensValue);
        org.openide.awt.Mnemonics.setLocalizedText(tokens, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ProvidedTokens"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(tokens, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(tokensValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(appendImpl, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AppendImplementation"));
        appendImpl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        appendImpl.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(appendImpl, gridBagConstraints);

        publicPkgs.setLabelFor(publicPkgsTable);
        org.openide.awt.Mnemonics.setLocalizedText(publicPkgs, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_PublicPackages"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 12);
        add(publicPkgs, gridBagConstraints);

        publicPkgsTable.setShowHorizontalLines(false);
        publicPkgsSP.setViewportView(publicPkgsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.8;
        add(publicPkgsSP, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AddButton"));
        addFriendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFriend(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addFriendButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeFriendButton, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RemoveButton"));
        removeFriendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFriend(evt);
            }
        });

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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        bottomPanel.add(buttonPanel, gridBagConstraints);

        friendsSP.setViewportView(friendsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        bottomPanel.add(friendsSP, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(bottomPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(exportOnlyToFriend, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_ExportOnlyToFriends"));
        exportOnlyToFriend.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exportOnlyToFriend.setEnabled(false);
        exportOnlyToFriend.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(exportOnlyToFriend, gridBagConstraints);

        typePanel.setLayout(new java.awt.GridBagLayout());

        moduleTypeGroup.add(regularMod);
        regularMod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(regularMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_RegularModule"));
        regularMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        regularMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(regularMod, gridBagConstraints);

        moduleTypeGroup.add(autoloadMod);
        org.openide.awt.Mnemonics.setLocalizedText(autoloadMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_AutoloadModule"));
        autoloadMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoloadMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(autoloadMod, gridBagConstraints);

        moduleTypeGroup.add(eagerMod);
        org.openide.awt.Mnemonics.setLocalizedText(eagerMod, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "CTL_EagerModule"));
        eagerMod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        eagerMod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        typePanel.add(eagerMod, gridBagConstraints);

        typeTxt.setLabelFor(implVerValue);
        org.openide.awt.Mnemonics.setLocalizedText(typeTxt, org.openide.util.NbBundle.getMessage(CustomizerVersioning.class, "LBL_ModuleType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typePanel.add(typeTxt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(typePanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void removeFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFriend
        getFriendModel().removeFriend((String) friendsList.getSelectedValue());
        if (getFriendModel().getSize() > 0) {
            friendsList.setSelectedIndex(0);
        }
        friendsList.requestFocus();
        checkForm();
    }//GEN-LAST:event_removeFriend
    
    private void addFriend(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFriend
        AddFriendPanel addFriend = new AddFriendPanel(getProperties());
        DialogDescriptor descriptor = new DialogDescriptor(addFriend, getMessage("CTL_AddNewFriend_Title"));
        descriptor.setHelpCtx(new HelpCtx(AddFriendPanel.class));
        final JButton okButton = new JButton(getMessage("CTL_OK"));
        JButton cancel = new JButton(getMessage("CTL_Cancel"));
        okButton.setEnabled(false);
        Object[] options = new Object[] { okButton , cancel };
        descriptor.setOptions(options);
        descriptor.setClosingOptions(options);
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        addFriend.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName() == AddFriendPanel.DATA_LOADED_PROPERTY) {
                    d.pack();
                    d.setLocationRelativeTo(null);
                } else if (pce.getPropertyName() == AddFriendPanel.VALID_PROPERTY) {
                    okButton.setEnabled(((Boolean) pce.getNewValue()).booleanValue());
                }
            }
        });
        d.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_AddNewFriend_Title"));
        okButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_OK"));
        cancel.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_Cancel"));
        d.pack();
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        if (descriptor.getValue().equals(okButton)) {
            String newFriendCNB = addFriend.getFriendCNB();
            getFriendModel().addFriend(newFriendCNB);
            friendsList.setSelectedValue(newFriendCNB, true);
        }
        d.dispose();
        friendsList.requestFocus();
        checkForm();
    }//GEN-LAST:event_addFriend
    
    private String getMessage(String key) {
        return NbBundle.getMessage(CustomizerVersioning.class, key);
    }
    
    public void showSubCategory(String name) {
        if (name.equals(CustomizerProviderImpl.SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES)) {
            publicPkgsTable.requestFocus();
            /* XXX does not work quite right under Ocean; have to press TAB once; this does not help:
            if (publicPkgsTable.getModel().getRowCount() > 0) {
                publicPkgsTable.setEditingRow(0);
                publicPkgsTable.setEditingColumn(1);
            }
             */
        } else {
            throw new IllegalArgumentException(name);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFriendButton;
    private javax.swing.JCheckBox appendImpl;
    private javax.swing.JRadioButton autoloadMod;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel cnb;
    private javax.swing.JTextField cnbValue;
    private javax.swing.JRadioButton eagerMod;
    private javax.swing.JCheckBox exportOnlyToFriend;
    private javax.swing.JLabel filler1;
    private javax.swing.JList friendsList;
    private javax.swing.JScrollPane friendsSP;
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
    private javax.swing.JPanel typePanel;
    private javax.swing.JLabel typeTxt;
    // End of variables declaration//GEN-END:variables
    
    private void initAccesibility() {
        addFriendButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AddFriendButton"));
        removeFriendButton.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RemoveFriendButton"));
        cnbValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CnbValue"));
        majorRelVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_MajorRelVerValue"));
        specificationVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_SpecificationVerValuea"));
        appendImpl.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AppendImpl"));
        implVerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ImplVerValue"));
        regularMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_RegularMod"));
        autoloadMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_AutoloadMod"));
        eagerMod.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_EagerMod"));
        publicPkgsTable.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_PublicPkgsTable"));
        tokensValue.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_TokensValue"));
    }
    
}
