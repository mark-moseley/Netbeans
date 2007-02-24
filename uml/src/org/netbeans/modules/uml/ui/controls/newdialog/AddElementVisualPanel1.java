package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;

public final class AddElementVisualPanel1 extends JPanel {
    
    /** Creates new form AddElementVisualPanel1 */
    public AddElementVisualPanel1(INewDialogElementDetails details) {
        m_Details = details;
        initComponents();
    }
    
    public String getName() {
        return org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("IDS_NEWELEMENT");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        jLabel1.setLabelFor(jList1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("IDS_ELEMENTTYPE"));

        populateList();
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(new ElementListCellRenderer());
        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("ACSD_NEW_ELEMENT_WIZARD_ELEMENTTYPE_LIST"));

        jLabel2.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("IDS_ELEMENTNAME"));

        jTextField1.setText(NewDialogUtilities.getDefaultElementName());
        jTextField1.selectAll();
        jTextField1.requestFocus();
        jTextField1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("ACSD_NEW_ELEMENT_WIZARD_ELEMENTNAME_TEXTFIELD"));

        jLabel3.setLabelFor(jComboBox1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("IDS_NAMESPACE"));

        populateCombobox();
        jComboBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(AddElementVisualPanel1.class).getString("ACSD_NEW_ELEMENT_WIZARD_ELEMENTNAMESPACE_COMBOBOX"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .add(jComboBox1, 0, 304, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void populateList() {
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
                    "//PropertyDefinition/aDefinition[@name='" // NOI18N
                    + "Element" + "']/aDefinition"); // NOI18N
            
            if (jList1 != null) {
                int count = nodeList.size();
                for (int i=0; i<count; i++) {
                    org.dom4j.Element subNode = (org.dom4j.Element)nodeList.get(i);                    
                    String subName = subNode.attributeValue("displayName"); // NOI18N                    
                    subName = NewDialogResources.getString(subName);
                    
                    //add workspace node
                    elements.add(subName);
                }
            }
            jList1.setListData(elements);
            
            UserSettings userSettings = new UserSettings();
            
            if (userSettings != null) {
                String value = userSettings.getSettingValue(
                        "NewDialog", "LastChosenElementType"); // NOI18N
                if (value != null && value.length() > 0) {
                    jList1.setSelectedValue(value, true);
                }
            }
            if (jList1.getSelectedIndex() == -1) {
                jList1.setSelectedIndex(0);
            }
        }
    }

    private void populateCombobox() {        
        if ((jComboBox1 != null) && (m_Details != null)) {   
            NewDialogUtilities.loadNamespace(jComboBox1, m_Details.getNamespace());
        }
    }
    
    protected String getElementName() {
        return jTextField1.getText().trim();
    }
    
    protected Object getSelectedNamespace() {
        return jComboBox1.getSelectedItem();
    }
    
    protected Object getSelectedListElement() {
        return jList1.getSelectedValue();
    }
    
    protected int getSelectedListIndex() {
        return jList1.getSelectedIndex();        
    }
    
    class ElementListCellRenderer extends JLabel implements ListCellRenderer {
        public Icon getImageIcon(String elemName) {
            Icon retIcon = null;
            String displayName = NewDialogResources.getStringKey(elemName);
            String str = "//PropertyDefinition/aDefinition[@name='" + // NOI18N
                    "Element" + "']/aDefinition[@displayName='" +  // NOI18N
                    displayName + "']"; // NOI18N
            
            org.dom4j.Node node = m_doc.selectSingleNode(str);
            if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE) {
                org.dom4j.Element elem = (org.dom4j.Element)node;
                String fileName = elem.attributeValue("image"); // NOI18N
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
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
    private org.dom4j.Document m_doc = null;
    public INewDialogElementDetails m_Details = null;
}

