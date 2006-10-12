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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JTextField;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** WsdlUIPanel.java - bottom panel for WSDL wizard
 *
 * @author  mkuchtiak
 */
public class WsdlUIPanel extends javax.swing.JPanel {
    
    private static final String TARGET_URL_PREFIX = NbBundle.getMessage(WsdlUIPanel.class,"TXT_defaultTNS"); //NOI18N
    
    private WsdlPanel wizardPanel;
    private javax.swing.JTextField fileNameTF;
    
    private boolean hasUserModifiedNamespace = false;
    
    private NamespaceDocListener mListener = new NamespaceDocListener();
    
    /** Creates new form WsdlUIPanel */
    public WsdlUIPanel(WsdlPanel wizardPanel) {
        initComponents();
        this.wizardPanel=wizardPanel;
        nsTF.setText(TARGET_URL_PREFIX);
    }
    
    void attachFileNameListener(javax.swing.JTextField fileNameTF) {
        this.fileNameTF=fileNameTF;
        if (fileNameTF!=null) {
            nsTF.setText(TARGET_URL_PREFIX+fileNameTF.getText());
            DocListener list = new DocListener();
            javax.swing.text.Document doc = fileNameTF.getDocument();
            doc.addDocumentListener(list);
        } else {
            nsTF.setText(TARGET_URL_PREFIX);
        }
        
        nsTF.getDocument().addDocumentListener(mListener);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbImport = new javax.swing.JCheckBox();
        schemaTF = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        schemaLB = new javax.swing.JLabel();
        namespaceLB = new javax.swing.JLabel();
        nsTF = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cbImport, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_importSchema")); // NOI18N
        cbImport.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbImport.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbImport.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbImportItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 5);
        add(cbImport, gridBagConstraints);
        cbImport.getAccessibleContext().setAccessibleDescription(null); // NOI18N

        schemaTF.setEditable(false);
        schemaTF.setToolTipText(null); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
        add(schemaTF, gridBagConstraints);
        schemaTF.getAccessibleContext().setAccessibleDescription(null); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_browse")); // NOI18N
        browseButton.setEnabled(false);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(null); // NOI18N

        schemaLB.setLabelFor(schemaTF);
        org.openide.awt.Mnemonics.setLocalizedText(schemaLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_schemaFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(schemaLB, gridBagConstraints);

        namespaceLB.setLabelFor(nsTF);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_targetNamespace")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 2, 0);
        add(namespaceLB, gridBagConstraints);

        nsTF.setToolTipText(null); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        add(nsTF, gridBagConstraints);
        nsTF.getAccessibleContext().setAccessibleDescription(null); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
// TODO add your handling code here:
        String dialogTitle = NbBundle.getMessage(WsdlUIPanel.class,"TITLE_selectSchema");
        String maskTitle = NbBundle.getMessage(WsdlUIPanel.class,"TXT_schemaFiles");
        java.io.File[] files = org.netbeans.modules.xml.wsdl.ui.wizard.Utilities.selectFiles("xsd XSD", dialogTitle, maskTitle, wizardPanel.getProject());
        if(files == null || files.length ==0) return;
        String original = schemaTF.getText().trim();
        StringBuilder fileString = new StringBuilder(original);
        for(int i = 0; i < files.length; i++) {
            java.io.File f = files[i];
            String location = f.toURI().normalize().toString();
            if (fileString.indexOf(location)>=0) continue;
            if(fileString.length() > 0){
                fileString.append(",");
            }
            fileString.append(location);
        }
        schemaTF.setText(fileString.toString());
        schemaTF.firePropertyChange("VALUE_SET", false, true);
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void cbImportItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbImportItemStateChanged
// TODO add your handling code here:
        if (cbImport.isSelected()) {
            schemaTF.setEditable(true);
            browseButton.setEnabled(true);
        } else {
            schemaTF.setEditable(false);
            browseButton.setEnabled(false);
        }
    }//GEN-LAST:event_cbImportItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cbImport;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel namespaceLB;
    private javax.swing.JTextField nsTF;
    private javax.swing.JLabel schemaLB;
    private javax.swing.JTextField schemaTF;
    // End of variables declaration//GEN-END:variables
    
    private class NamespaceDocListener implements javax.swing.event.DocumentListener {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        private void documentChanged(javax.swing.event.DocumentEvent e) {
            hasUserModifiedNamespace = true;
        }
    }
    
    private class DocListener implements javax.swing.event.DocumentListener {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            documentChanged(e);
        }
        
        private void documentChanged(javax.swing.event.DocumentEvent e) {
            if(!hasUserModifiedNamespace) {
                nsTF.getDocument().removeDocumentListener(mListener);
                nsTF.setText(TARGET_URL_PREFIX+fileNameTF.getText());
                nsTF.getDocument().addDocumentListener(mListener);
            }
            
        }
    }
    
    /** Class than provides basic informationn about schema file
     */
    static class SchemaInfo {
        private java.net.URL url;
        SchemaInfo(java.net.URL url) {
            this.url=url;
        }
        
        java.net.URL getURL() {
            return url;
        }
        
        String getNamespace() {
            InputSource is = new InputSource(url.toExternalForm());
            try {
                return parse(is);
            } catch (java.io.IOException ex){
            } catch (SAXException ex){}
            return "";
        }
        
        String getSchemaName() {
            return url.toExternalForm();
        }
        
        /** Parses XML document and creates the list of tags
         */
        private String parse(InputSource is) throws java.io.IOException, SAXException {
            XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader();
            NsHandler handler = new NsHandler();
            xmlReader.setContentHandler(handler);
            //xmlReader.setFeature("http://xml.org/sax/features/use-locator2",true);
            try {
                xmlReader.parse(is);
            } catch (SAXException ex) {
                if (!"EXIT".equals(ex.getMessage())) throw ex; //NOI18N
            }
            String ns = handler.getNs();
            if (ns==null) return "";
            else return ns;
        }
        
        private static class NsHandler extends org.xml.sax.helpers.DefaultHandler {
            private String ns;
            
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.endsWith("schema")) { //NOI18N
                    ns=attributes.getValue("targetNamespace"); //NOI18N
                    throw new SAXException("EXIT"); //NOI18N
                }
            }
            
            String getNs() {
                return ns;
            }
        }
        
    }
    
    String getNS() {
        return nsTF.getText();
    }
    
    String getWsName() {
        return fileNameTF.getText();
    }
    
    boolean isImport() {
        return cbImport.isSelected();
    }
    
    SchemaInfo[] getSchemas() {
        if (cbImport.isSelected()) {
            String schemas = schemaTF.getText();
            String[] urls = schemas.split(",");
            java.util.List infos = new java.util.ArrayList();
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                try {
                    java.net.URL url = new java.net.URL(urlString);
                    infos.add(new SchemaInfo(url));
                } catch (java.net.MalformedURLException ex) {
                    // testing if target folder contains XML Schema
                    try {
                        org.openide.loaders.DataFolder folder = wizardPanel.getTemplateWizard().getTargetFolder();
                        org.openide.filesystems.FileObject fo = folder.getPrimaryFile();
                        if ((fo.getFileObject(urlString))!=null) {
                            String parentURL = fo.getURL().toExternalForm();
                            infos.add(new SchemaInfo(new java.net.URL(parentURL+urlString)));
                        }
                    } catch (java.io.IOException ex1) {}
                }
            }
            SchemaInfo[] result = new SchemaInfo[infos.size()];
            infos.toArray(result);
            return result;
        }
        return new SchemaInfo[]{};
    }

    public void validateSchemas() throws WizardValidationException {
        if (cbImport.isSelected()) {
            String schemas = schemaTF.getText();
            String[] urls = schemas.split(",");
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                createSchemaModel(urlString);
            }
        }
        
    }
    
    private void createSchemaModel(String urlString) throws WizardValidationException {
        java.net.URL url = null;
        try {
            url = new java.net.URL(urlString);
        } catch (MalformedURLException e) {
            org.openide.loaders.DataFolder folder;
                try {
                    folder = wizardPanel.getTemplateWizard().getTargetFolder();
                    org.openide.filesystems.FileObject fo = folder.getPrimaryFile();
                    if ((fo.getFileObject(urlString))!=null) {
                        String parentURL = fo.getURL().toExternalForm();
                        try {
                            url = new java.net.URL(parentURL+urlString);
                        } catch (MalformedURLException e1) {
                            throw new WizardValidationException(schemaTF, e1.getMessage(), e1.getLocalizedMessage());
                        }
                    }
                } catch (IOException e1) {
                    throw new WizardValidationException(schemaTF, e1.getMessage(), e1.getLocalizedMessage());
                }
                if (url == null) {
                    String errorString = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
                    throw new WizardValidationException(schemaTF, errorString, errorString);
                }
        }
        ModelSource source;
        try {
            FileObject fo = FileUtil.toFileObject(new File(url.toURI()));
            if (fo == null) {
                String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
                throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
            }
            source = Utilities.createModelSource(fo, false);
        } catch (WizardValidationException e) {
            throw e;
        }   catch (CatalogModelException e) {
            throw new WizardValidationException(schemaTF, e.getMessage(), e.getLocalizedMessage());
        } catch (URISyntaxException e) {
            throw new WizardValidationException(schemaTF, e.getMessage(), e.getLocalizedMessage());
        } catch (Throwable e) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
        SchemaModel model = null;
        try {
            model = SchemaModelFactory.getDefault().getModel(source);
        } catch (Throwable e) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
        
        if (model == null || model.getState().equals(State.NOT_WELL_FORMED)) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
    }
    
    public JTextField getSchemaFileTextField() {
        return schemaTF;
    }
}
