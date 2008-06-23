/*
 * PackagingPanel.java
 *
 * Created on June 20, 2008, 4:19 PM
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
public class PackagingPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener  {
    PackagingConfiguration packagingConfiguration;
    private PropertyEditorSupport editor;
    
    /** Creates new form PackagingPanel */
    public PackagingPanel(PackagingConfiguration packagingConfiguration, PropertyEditorSupport editor, PropertyEnv env) {
        initComponents();
        
        this.packagingConfiguration = packagingConfiguration;
        this.editor = editor;
        
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
        
        // Combobox
        String[] displayNames = packagingConfiguration.getDisplayNames();
        for (int i = 0; i < displayNames.length; i++ ) {
            packagingTypeComboBox.addItem(displayNames[i]);
        }
        packagingTypeComboBox.setSelectedIndex(packagingConfiguration.getType().getValue());
        
        // Add tabs
        tabbedPane.addTab("Info", new PackagingInfoPanel());
        tabbedPane.addTab("Files", new PackagingFilesPanel());
    }

        
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    private Object getPropertyValue() throws IllegalStateException {
	return packagingConfiguration; // FIXUP
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Libraries"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        innerPanel = new javax.swing.JPanel();
        packagingTypeLabel = new javax.swing.JLabel();
        packagingTypeComboBox = new javax.swing.JComboBox();
        tabbedPane = new javax.swing.JTabbedPane();

        setPreferredSize(new java.awt.Dimension(800, 600));
        setLayout(new java.awt.GridBagLayout());

        innerPanel.setLayout(new java.awt.GridBagLayout());

        packagingTypeLabel.setText(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.packagingTypeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerPanel.add(packagingTypeLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerPanel.add(packagingTypeComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        innerPanel.add(tabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(innerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel innerPanel;
    private javax.swing.JComboBox packagingTypeComboBox;
    private javax.swing.JLabel packagingTypeLabel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(PackagingPanel.class);
	}
	return bundle.getString(s);
    }
}
