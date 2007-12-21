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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
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
    WsdlUIPanel(WsdlPanel wizardPanel) {
        initComponents();
        this.wizardPanel=wizardPanel;
        nsTF.setText(TARGET_URL_PREFIX);
    }
    
    void attachFileNameListener(javax.swing.JTextField fileNameTextField) {
        this.fileNameTF = fileNameTextField;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        cbImport.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N
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
        cbImport.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_importSchema")); // NOI18N
        cbImport.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N

        schemaTF.setEditable(false);
        schemaTF.setToolTipText(org.openide.util.NbBundle.getBundle(WsdlUIPanel.class).getString("HINT_schemaFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
        add(schemaTF, gridBagConstraints);
        schemaTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_schemaFiles")); // NOI18N
        schemaTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "A11Y_schemaTF")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_browse")); // NOI18N
        browseButton.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "A11Y_browse")); // NOI18N
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
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_browse")); // NOI18N

        schemaLB.setLabelFor(schemaTF);
        org.openide.awt.Mnemonics.setLocalizedText(schemaLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_schemaFiles")); // NOI18N
        schemaLB.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_schemaFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(schemaLB, gridBagConstraints);

        namespaceLB.setLabelFor(nsTF);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLB, org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_targetNamespace")); // NOI18N
        namespaceLB.setToolTipText(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 2, 0);
        add(namespaceLB, gridBagConstraints);
        namespaceLB.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "LBL_targetNamespace")); // NOI18N
        namespaceLB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/wsdl/ui/wizard/Bundle"); // NOI18N
        nsTF.setToolTipText(bundle.getString("HINT_targetNamespace")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        add(nsTF, gridBagConstraints);
        nsTF.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "WsdlUIPanel.nsTF.AccessibleContext.accessibleName")); // NOI18N
        nsTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsdlUIPanel.class, "HINT_targetNamespace")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // Create a temporary file and model for the import creator to use.
        Project project = wizardPanel.getProject();
        FileObject prjdir = project.getProjectDirectory();
        //XXX:SKINI: Relook this hack
        // HACK: hard-coded NB project directory name
        FileObject privdir = prjdir.getFileObject("nbproject/private");
        // We prefer to use the private directory, but at the very
        // least our file needs to be inside the project.
        File directory = FileUtil.toFile(privdir != null ? privdir : prjdir);
        String fname = fileNameTF.getText();
        if (fname == null || fname.length() == 0) {
            fname = "wizard";
        }
        File file = null;
        try {
            file = File.createTempFile(fname, ".wsdl", directory);
            wizardPanel.populateFileFromTemplate(file);
        } catch (Exception e) {
            // This is quite unexpected.
            ErrorManager.getDefault().notify(e);
            if (file != null) {
                file.delete();
            }
            return;
        }
        WSDLModel model = wizardPanel.prepareModelFromFile(file, fname);
        model.startTransaction();
        WSDLSchema wsdlSchema = model.getFactory().createWSDLSchema();
        Definitions defs = model.getDefinitions();
        defs.getTypes().addExtensibilityElement(wsdlSchema);
        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
        Schema schema = schemaModel.getSchema();
        // Must set namespace on embedded schema for import dialog to work.
        schema.setTargetNamespace(defs.getTargetNamespace());
        model.endTransaction();

        // Use a specialized import creator for selecting files.
        String original = schemaTF.getText().trim();
        ImportSchemaCreator creator = new ImportSchemaCreator(schema, model, original);
        DialogDescriptor descriptor = UIUtilities.getCreatorDialog(
                creator, NbBundle.getMessage(WsdlUIPanel.class,
                "TITLE_selectSchema"), true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        if (result == DialogDescriptor.OK_OPTION) {
            String selections = creator.getSelectedFiles();
            schemaTF.setText(selections);
            schemaTF.firePropertyChange("VALUE_SET", false, true);
        }

        // Must use DataObject to delete the temporary file.
        file = FileUtil.normalizeFile(file);
        FileObject fobj = FileUtil.toFileObject(file);
        try {
            DataObject.find(fobj).delete();
        } catch (IOException ex) {
            // Ignore, either the file isn't there or we can't delete it.
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void cbImportItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbImportItemStateChanged
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
            return ns;
        }
        
        private static class NsHandler extends org.xml.sax.helpers.DefaultHandler {
            private String ns;
            
            @Override
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
    
    boolean isImport() {
        return cbImport.isSelected();
    }
    
    SchemaInfo[] getSchemas() {
        if (cbImport.isSelected()) {
            String schemas = schemaTF.getText();
            String[] urls = schemas.split(",");
            List<SchemaInfo> infos = new ArrayList<SchemaInfo>();
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                URL url = null;
                try {
                    File file = new File(urlString);
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    } else {
                        url = new java.net.URL(urlString);
                    }
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
            return infos.toArray(new SchemaInfo[infos.size()]);
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
        File file = new File(urlString);
        if (!file.exists()) {
            file = null;
        }
        if (file == null) {
            URL url = null;
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
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new WizardValidationException(schemaTF, e.getMessage(), e.getLocalizedMessage());
            }
        }
        
        if (!file.isFile()) {
            throw new WizardValidationException(schemaTF, "INVALID_SCHEMA_FILE", NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString));
        }
        
        ModelSource source;
        try {
            File normFile = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(normFile);
            if (fo == null) {
                String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
                throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
            }
            checkAccessibleFromThisProject(wizardPanel.getProject(), fo, urlString);
            source = org.netbeans.modules.xml.retriever.catalog.Utilities.
                    createModelSource(fo, false);
        } catch (WizardValidationException e) {
            throw e;
        } catch (CatalogModelException e) {
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
        
        if (model == null || model.getState().equals(Model.State.NOT_WELL_FORMED)) {
            String errorMessage = NbBundle.getMessage(WsdlUIPanel.class, "INVALID_SCHEMA_FILE", urlString);
            throw new WizardValidationException(schemaTF, errorMessage, errorMessage);
        }
    }
    
    private void checkAccessibleFromThisProject(Project project, FileObject file, String fileName) throws WizardValidationException {
        Project filesProject = FileOwnerQuery.getOwner(file);
        if (filesProject == null) {
            throw new WizardValidationException(schemaTF, "INACCESSIBLE_FILE", NbBundle.getMessage(WsdlUIPanel.class, "INACCESSIBLE_FILE", fileName));
        }
        if (project == filesProject) {
            return;
        }
        
        DefaultProjectCatalogSupport ctlgSupp = DefaultProjectCatalogSupport.getInstance(project.getProjectDirectory());
        for (Object pr : ctlgSupp.getProjectReferences()) {
            if (pr == filesProject) {
                return;
            }
        }
        throw new WizardValidationException(schemaTF, "INACCESSIBLE_PROJECT", NbBundle.getMessage(WsdlUIPanel.class, "INACCESSIBLE_PROJECT", fileName, filesProject.getProjectDirectory().getName()));
    }

    public JTextField getSchemaFileTextField() {
        return schemaTF;
    }
}
