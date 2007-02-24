package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dom4j.Document;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class NewUMLDiagVisualPanel1 extends JPanel
        implements DocumentListener, ListSelectionListener, ActionListener {
    
    private NewUMLDiagWizardPanel1 panel;
    private Document m_doc = null;
    private java.util.List saveNamespaces = new java.util.ArrayList();
    private INewDialogDiagramDetails mDetails = null;
    private java.util.ResourceBundle bundle =
            NbBundle.getBundle(NewUMLDiagVisualPanel1.class);
    private String badCharList = bundle.getString("IDS_INVALID_CHARS");
    
    /** Creates new form NewUMLDiagVisualPanel1 */
    public NewUMLDiagVisualPanel1(NewUMLDiagWizardPanel1 panel) {
        this.panel = panel;
        initComponents();
        
        diagramTypes.addListSelectionListener(this);
        // Register listener for the textFields to validate entered text
        diagName.getDocument().addDocumentListener(this);
        diagName.getDocument().addDocumentListener(this);
        nameSpace.addActionListener(this);
    }
    
    public String getName() {
        return org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("IDS_NEWDIAGRAM");
    }
    
    public void read(WizardDescriptor wizDesc) {
        mDetails = (INewDialogDiagramDetails) wizDesc.getProperty(
                NewUMLDiagWizardIterator.DIAGRAM_DETAILS);
        
        if ( mDetails != null ) {
            populateList();
            populateComboBox();
        }
    }
    
    void store(WizardDescriptor wizDesc) {
        // store the diagram kind
        wizDesc.putProperty(NewUMLDiagWizardIterator.PROP_DIAG_KIND,
                (String) getSelectedDiagramType());
        // store diagram name
        wizDesc.putProperty(NewUMLDiagWizardIterator.PROP_DIAG_NAME,
                (String) getDiagramName());
        // store the namespace
        wizDesc.putProperty(NewUMLDiagWizardIterator.PROP_NAMESPACE,
                (String) getSelectedNamespace());
        
    }
    
    public boolean valid(WizardDescriptor wizDesc) {
        boolean valid = true;
        String errorMsg = "";
        
        String selectedDiagType = (String) getSelectedDiagramType();
        // validate if a diagram type is selected
        if (selectedDiagType == null || selectedDiagType.length() == 0) {
            errorMsg = bundle.getString("IDS_PLEASESELECTADIAGRAM"); // NOI18N
            valid = false;
        }
        
        // validate diagram name
        if (valid) {
            boolean nameHasBadChar = false;
            //StringBuffer badChars = new StringBuffer();
            String sDiagramName = getDiagramName();
            String trimmedName = sDiagramName.trim();
            int trimmedLen = trimmedName.length();
            String charList = "";
            boolean bNameHasSpaces = sDiagramName.length() > trimmedLen;
            
            if (badCharList != null && badCharList.length() > 0) {
                charList = badCharList.trim();
                String []badCharArray = charList.split(" ");
                for (String c : badCharArray) {
                    if (c.length() == 0) {
                        continue;
                    }
                    if ( sDiagramName.indexOf(c) != -1) {   // name contains bad char
                        nameHasBadChar = true;
                        break;
                    }
                }
            }
            
            if (trimmedLen == 0) {
                errorMsg = bundle.getString("IDS_DIAGRAMNAME_EMPTY"); // NOI18N
                valid = false;
            } else if (bNameHasSpaces) {
                errorMsg = bundle.getString("IDS_DIAGRAMNAME_HAS_SPACES"); // NOI18N
                valid = false;
            } else if(nameHasBadChar) {
                errorMsg = NbBundle.getMessage(
                        NewUMLDiagVisualPanel1.class,
                        "IDS_DIAGRAMNAME_HAS_BADCHARS", // NOI18N
                        charList);
                valid = false;
            }
        }
        
        // check if a selected namespace is valid for the selected diagram type
        if (valid) {
            // Get the namespace
            INamespace selectedNamespace = NewDialogUtilities.getNamespace(
                    (String) getSelectedNamespace());
            
            if ( selectedNamespace != null ) {
                ETPairT<Boolean, String> retVal = panel.isValidDiagramForNamespace(
                        selectedDiagType, selectedNamespace);
                if (retVal != null) {
                    valid = ((Boolean) retVal.getParamOne()).booleanValue();
                    String mesg = (String) retVal.getParamTwo();
                    errorMsg = (mesg == null ? "" : mesg.trim());
                }
            }
        }
        
        wizDesc.putProperty(
                NewUMLDiagWizardPanel1.PROP_WIZARD_ERROR_MESSAGE,
                errorMsg);
        
        return valid;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        diagTypeLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        diagramTypes = new javax.swing.JList();
        diagNameLabel = new javax.swing.JLabel();
        diagName = new javax.swing.JTextField();
        nameSpaceLabel = new javax.swing.JLabel();
        nameSpace = new javax.swing.JComboBox();

        diagTypeLabel.setLabelFor(diagramTypes);
        org.openide.awt.Mnemonics.setLocalizedText(diagTypeLabel, org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("IDS_DIAGRAMTYPE"));

        diagramTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        diagramTypes.setCellRenderer(new ElementListCellRenderer());
        jScrollPane1.setViewportView(diagramTypes);
        diagramTypes.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("ACSD_NEW_DIAGRAM_WIZARD_DIAGRAMTYPE_LIST"));

        diagNameLabel.setLabelFor(diagName);
        org.openide.awt.Mnemonics.setLocalizedText(diagNameLabel, org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("IDS_DIAGRAMNAME"));

        diagName.setText(NewDialogUtilities.getDefaultDiagramName());
        diagName.selectAll();
        diagName.requestFocus();
        diagName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("ACSD_NEW_DIAGRAM_WIZARD_DIAGRAMNAME_TEXTFIELD"));

        nameSpaceLabel.setLabelFor(nameSpace);
        org.openide.awt.Mnemonics.setLocalizedText(nameSpaceLabel, org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("IDS_NAMESPACE"));

        nameSpace.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NewUMLDiagVisualPanel1.class).getString("ACSD_NEW_DIAGRAM_WIZARD_NAMESPACE_COMBOBOX"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(diagTypeLabel)
                    .add(diagNameLabel)
                    .add(nameSpaceLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .add(diagName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .add(nameSpace, 0, 307, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(diagTypeLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(diagNameLabel)
                    .add(diagName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameSpace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameSpaceLabel))
                .add(60, 60, 60))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void populateList() {
        
        if (diagramTypes != null) {
            ListModel listDataModel = diagramTypes.getModel();
            if (listDataModel != null && listDataModel.getSize() > 0) {
                return;
            }
            
            IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
            String fileName = conMan.getDefaultConfigLocation();
            fileName += "NewDialogDefinitions.etc"; // NOI18N
            m_doc = XMLManip.getDOMDocument(fileName);
            org.dom4j.Node node = m_doc.selectSingleNode(
                    "//PropertyDefinitions/PropertyDefinition"); // NOI18N
            
            if (node != null) {
                org.dom4j.Element elem = (org.dom4j.Element)node;
                String name = elem.attributeValue("name"); // NOI18N
                
                Vector elements = new Vector();
                List nodeList = m_doc.selectNodes(
                        "//PropertyDefinition/aDefinition[@name='"  // NOI18N
                        + "Diagram" + "']/aDefinition");  // NOI18N
                if (diagramTypes != null) {
                    int diaKind = IDiagramKind.DK_ALL;
                    if (mDetails != null) {
                        diaKind = mDetails.getAvailableDiagramKinds();
                    }
                    int count = nodeList.size();
                    for (int i=0; i<count; i++) {
                        org.dom4j.Element subNode = (org.dom4j.Element)nodeList.get(i);
                        String subName =
                                subNode.attributeValue("displayName"); // NOI18N
                        subName = NewDialogResources.getString(subName);
                        
                        if (diaKind == IDiagramKind.DK_ALL) {
                            elements.add(subName);
                        } else {
                            //only some of diagram kinds are valid
                            if (subName.equals(NewDialogResources
                                    .getString("PSK_CLASS_DIAGRAM"))) // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_CLASS_DIAGRAM)
                                == IDiagramKind.DK_CLASS_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources
                                    .getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_ACTIVITY_DIAGRAM)
                                == IDiagramKind.DK_ACTIVITY_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources
                                    .getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_COLLABORATION_DIAGRAM)
                                == IDiagramKind.DK_COLLABORATION_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources
                                    .getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_COMPONENT_DIAGRAM)
                                == IDiagramKind.DK_COMPONENT_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources
                                    .getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_DEPLOYMENT_DIAGRAM)
                                == IDiagramKind.DK_DEPLOYMENT_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources
                                    .getString("PSK_SEQUENCE_DIAGRAM")))  // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_SEQUENCE_DIAGRAM)
                                == IDiagramKind.DK_SEQUENCE_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources.getString(
                                    "PSK_STATE_DIAGRAM")))  // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_STATE_DIAGRAM)
                                == IDiagramKind.DK_STATE_DIAGRAM) {
                                    elements.add(subName);
                                }
                            } else if (subName.equals(NewDialogResources.getString(
                                    "PSK_USE_CASE_DIAGRAM")))  // NOI18N
                            {
                                if ((diaKind & IDiagramKind.DK_USECASE_DIAGRAM)
                                == IDiagramKind.DK_USECASE_DIAGRAM) {
                                    elements.add(subName);
                                }
                            }
                        }
                    }
                }
                diagramTypes.setListData(elements);
//            UserSettings userSettings = new UserSettings();
//            if (userSettings != null) {
//                String value = userSettings.getSettingValue(
//                        "NewDialog", "LastChosenDiagramType");  // NOI18N
//
//                if (value != null && value.length() > 0) {
//                    diagramTypes.setSelectedValue(value, true);
//                }
//            }
//
                if (diagramTypes.getSelectedIndex() == -1) {
                    diagramTypes.setSelectedIndex(0);
                }
            }
        }
    }
    
    private void populateComboBox() {
        //load namespaces
        if (nameSpace != null) {
            NewDialogUtilities.loadNamespace(nameSpace, mDetails.getNamespace());
            // Fix for bug#6283146
            int itemCounts = nameSpace.getItemCount();
            for(int i=0; i < itemCounts; i++)
                saveNamespaces.add(nameSpace.getItemAt(i));
            valueChanged(null);
        }
    }
    
    //list selection listener callback
    public void valueChanged(ListSelectionEvent e) {
        // Fix for bug#6283146
        nameSpace.removeAllItems();
        String diaType = (String)diagramTypes.getSelectedValue();
        if (diaType != null) {
            if (diaType.equals(NewDialogResources.getString(
                    "PSK_SEQUENCE_DIAGRAM"))) // NOI18N
            {
                if(saveNamespaces.size()>0)
                    nameSpace.addItem(saveNamespaces.get(0));
            } else {
                for(int i=0;i<saveNamespaces.size();i++)
                    nameSpace.addItem(saveNamespaces.get(i));
            }
        }
        //fire change event to validate the selection
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }
    
    class ElementListCellRenderer extends JLabel implements ListCellRenderer {
        public Icon getImageIcon(String diaName) {
            Icon retIcon = null;
            String displayName = NewDialogResources.getStringKey(diaName);
            String str = "//PropertyDefinition/aDefinition[@name='" +  // NOI18N
                    "Diagram" + "']/aDefinition[@displayName='" +
                    displayName + "']";  // NOI18N
            
            org.dom4j.Node node = m_doc.selectSingleNode(str);
            if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE) {
                org.dom4j.Element elem = (org.dom4j.Element)node;
                String fileName = elem.attributeValue("image");  // NOI18N
                File file = new File(fileName);
                
                retIcon = CommonResourceManager.instance().getIconForFile(fileName);
            }
            
            return retIcon;
        }
        
        public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            String s = value.toString();
            setText(s);
            setIcon(getImageIcon(s));
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
    
    public Object getSelectedDiagramType() {
        return diagramTypes.getSelectedValue();
    }
    
    public Object getSelectedNamespace() {
        return nameSpace.getSelectedItem();
    }
    
    public String getDiagramName() {
        String str = "";
        str = diagName.getText();
        return str;
    }
    
// implementing method in DocumentListener
    public void changedUpdate(DocumentEvent e) {
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }
    
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void actionPerformed(ActionEvent e) {
        if (panel != null) {
            panel.fireChangeEvent();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField diagName;
    private javax.swing.JLabel diagNameLabel;
    private javax.swing.JLabel diagTypeLabel;
    private javax.swing.JList diagramTypes;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox nameSpace;
    private javax.swing.JLabel nameSpaceLabel;
    // End of variables declaration//GEN-END:variables
    
}

