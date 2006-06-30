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

package org.netbeans.modules.apisupport.project.ui.wizard.loader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * the first panel in loaders wizard
 *
 * @author Milos Kleint
 */
final class FileRecognitionPanel extends BasicWizardIterator.Panel {
    
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("([.]?[a-zA-Z0-9_]+){1}([ ,]+[.]?[a-zA-Z0-9_]+)*[ ]*"); // NOI18N
    private static final Pattern ELEMENT_PATTERN = Pattern.compile("(application/([a-zA-Z0-9_.-])*\\+xml|text/([a-zA-Z0-9_.-])*\\+xml)"); // NOI18N
    private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("(application|text|image|audio|video)/([a-zA-Z0-9_.+-])+"); // NOI18N
    private static final String DEFAULT_MIME_TYPE = "text/x-<type>"; // NOI18N
    
    private NewLoaderIterator.DataModel data;
    private ButtonGroup group;
    private boolean listenersAttached;
    private DocumentListener docList;
    
    /**
     * Creates new form FileRecognitionPanel
     */
    public FileRecognitionPanel(WizardDescriptor setting, NewLoaderIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        group = new ButtonGroup();
        group.add(rbByElement);
        group.add(rbByExtension);
        ActionListener list = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                txtExtension.setEnabled(rbByExtension.isSelected());
                txtNamespace.setEnabled(rbByElement.isSelected());
                checkValidity();
            }
        };
        docList = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };
        
        rbByElement.addActionListener(list);
        rbByExtension.addActionListener(list);
        
        putClientProperty("NewFileWizard_Title", getMessage("LBL_LoaderWizardTitle"));
    }
    
    private void checkValidity() {
        markValid();
        String txt = txtMimeType.getText().trim();
        
        if (txt.length() == 0 || DEFAULT_MIME_TYPE.equals(txt) || (!MIME_TYPE_PATTERN.matcher(txt).matches())) {
            setError(getMessage("MSG_NotValidMimeType"));
        } else {
            if (rbByElement.isSelected()) {
                if (txtNamespace.getText().trim().length() == 0) {
                    setError(getMessage("MSG_NoNamespace"));
                } else {
                    Matcher match = ELEMENT_PATTERN.matcher(txt);
                    if (!match.matches()) {
                        setError(getMessage("MSG_BadMimeTypeForXML"));
                    }
                }
            } else {
                if (txtExtension.getText().trim().length() == 0) {
                    setError(getMessage("MSG_NoExtension"));
                } else {
                    Matcher match = EXTENSION_PATTERN.matcher(txtExtension.getText());
                    if (!match.matches()) {
                        setError(getMessage("MSG_BadExtensionPattern"));
                    }
                }
            }
        }
    }
    
    public void addNotify() {
        super.addNotify();
        attachDocumentListeners();
        checkValidity();
    }
    
    public void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            txtNamespace.getDocument().addDocumentListener(docList);
            txtExtension.getDocument().addDocumentListener(docList);
            txtMimeType.getDocument().addDocumentListener(docList);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            txtNamespace.getDocument().removeDocumentListener(docList);
            txtExtension.getDocument().removeDocumentListener(docList);
            txtMimeType.getDocument().removeDocumentListener(docList);
            listenersAttached = false;
        }
    }
    
    
    protected void storeToDataModel() {
        data.setMimeType(txtMimeType.getText().trim());
        data.setExtensionBased(rbByExtension.isSelected());
        if (data.isExtensionBased()) {
            data.setExtension(txtExtension.getText().trim());
            data.setNamespace(null);
        } else {
            data.setExtension(null);
            data.setNamespace(txtNamespace.getText().trim());
        }
    }
    
    protected void readFromDataModel() {
        String mime = data.getMimeType();
        if (mime == null) {
            mime = DEFAULT_MIME_TYPE;
        }
        txtMimeType.setText(mime);
        if (mime.equals(DEFAULT_MIME_TYPE)) {
            txtMimeType.select(DEFAULT_MIME_TYPE.length() - 6, DEFAULT_MIME_TYPE.length());
        }
        if (data.isExtensionBased()) {
            rbByExtension.setSelected(true);
        } else {
            rbByElement.setSelected(true);
        }
        txtExtension.setEnabled(rbByExtension.isSelected());
        txtNamespace.setEnabled(rbByElement.isSelected());
        txtExtension.setText(data.getExtension());
        txtNamespace.setText(data.getNamespace());
        
        checkValidity();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_FileRecognition_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(FileRecognitionPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(FileRecognitionPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblMimeType = new javax.swing.JLabel();
        txtMimeType = new javax.swing.JTextField();
        rbByExtension = new javax.swing.JRadioButton();
        lblExtension = new javax.swing.JLabel();
        txtExtension = new javax.swing.JTextField();
        rbByElement = new javax.swing.JRadioButton();
        lblNamespace = new javax.swing.JLabel();
        txtNamespace = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        lblMimeType.setLabelFor(txtMimeType);
        org.openide.awt.Mnemonics.setLocalizedText(lblMimeType, org.openide.util.NbBundle.getMessage(FileRecognitionPanel.class, "LBL_MimeType"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(lblMimeType, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(txtMimeType, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rbByExtension, org.openide.util.NbBundle.getMessage(FileRecognitionPanel.class, "LBL_ByExtension"));
        rbByExtension.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbByExtension.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(rbByExtension, gridBagConstraints);

        lblExtension.setLabelFor(txtExtension);
        org.openide.awt.Mnemonics.setLocalizedText(lblExtension, org.openide.util.NbBundle.getMessage(FileRecognitionPanel.class, "LBL_Extension"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(lblExtension, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(txtExtension, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rbByElement, org.openide.util.NbBundle.getMessage(FileRecognitionPanel.class, "LBL_ByElement"));
        rbByElement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbByElement.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(rbByElement, gridBagConstraints);

        lblNamespace.setLabelFor(txtNamespace);
        org.openide.awt.Mnemonics.setLocalizedText(lblNamespace, org.openide.util.NbBundle.getMessage(FileRecognitionPanel.class, "LBL_Element"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(lblNamespace, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(txtNamespace, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblExtension;
    private javax.swing.JLabel lblMimeType;
    private javax.swing.JLabel lblNamespace;
    private javax.swing.JRadioButton rbByElement;
    private javax.swing.JRadioButton rbByExtension;
    private javax.swing.JTextField txtExtension;
    private javax.swing.JTextField txtMimeType;
    private javax.swing.JTextField txtNamespace;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_FileRecognitionPanel"));
        rbByElement.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ByElement"));
        rbByExtension.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ByExtension"));
        txtExtension.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Extension"));
        txtNamespace.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Namespace"));
        txtMimeType.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Mimetype"));
    }
}
