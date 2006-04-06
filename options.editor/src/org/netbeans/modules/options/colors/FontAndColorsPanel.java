/*
 * FontAndColorsPanel1.java
 *
 * Created on January 18, 2006, 2:12 PM
 */

package org.netbeans.modules.options.colors;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Jancura
 */
public class FontAndColorsPanel extends JPanel implements ActionListener {
    
    
    private SyntaxColoringPanel	    syntaxColoringPanel;
    private HighlightingPanel       highlightingPanel;
    private AnnotationsPanel	    annotationsPanel;

    private ColorModel		    colorModel;
    private String		    currentProfile;
    private boolean		    listen = false;
    
    
    /** Creates new form FontAndColorsPanel1 */
    public FontAndColorsPanel () {
        initComponents ();
        
        // init components
        cbProfile.getAccessibleContext ().setAccessibleName (loc ("AN_Profiles"));
        cbProfile.getAccessibleContext ().setAccessibleDescription (loc ("AD_Profiles"));
        bDelete.getAccessibleContext ().setAccessibleName (loc ("AN_Delete"));
        bDelete.getAccessibleContext ().setAccessibleDescription (loc ("AD_Delete"));
        bDuplicate.getAccessibleContext ().setAccessibleName (loc ("AN_Clone"));
        bDuplicate.getAccessibleContext ().setAccessibleDescription (loc ("AD_Clone"));
        tpCustomizers.getAccessibleContext ().setAccessibleName (loc ("AN_Categories"));
        tpCustomizers.getAccessibleContext ().setAccessibleDescription (loc ("AD_Categories"));
        syntaxColoringPanel = new SyntaxColoringPanel (this);
        highlightingPanel = new HighlightingPanel ();
        annotationsPanel = new AnnotationsPanel ();
        cbProfile.addItemListener (new ItemListener () {
            public void itemStateChanged (ItemEvent evt) {
                if (!listen) return;
                setCurrentProfile ((String) cbProfile.getSelectedItem ());
            }
        });
        loc (bDuplicate, "CTL_Create_New");
        bDuplicate.addActionListener (this);
        loc (bDelete, "CTL_Delete");
        bDelete.addActionListener (this);
        
	tpCustomizers.addTab (loc ("Syntax_coloring_tab"), syntaxColoringPanel);
	tpCustomizers.addTab (loc ("Editor_tab"), highlightingPanel);
	tpCustomizers.addTab (loc ("Annotations_tab"), annotationsPanel);
        tpCustomizers.setMnemonicAt (0, loc ("Syntax_coloring_tab_mnemonic").charAt (0));
        tpCustomizers.setMnemonicAt (1, loc ("Editor_tab_mnemonic").charAt (0));
        tpCustomizers.setMnemonicAt (2, loc ("Annotations_tab_mnemonic").charAt (0));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lProfile = new javax.swing.JLabel();
        cbProfile = new javax.swing.JComboBox();
        tpCustomizers = new javax.swing.JTabbedPane();
        bDuplicate = new javax.swing.JButton();
        bDelete = new javax.swing.JButton();

        lProfile.setText("Profile:");

        bDuplicate.setText("Duplicate...");

        bDelete.setText("Delete");
        bDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDeleteActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lProfile)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bDuplicate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bDelete)
                .addContainerGap(166, Short.MAX_VALUE))
            .add(tpCustomizers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {bDelete, bDuplicate}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lProfile)
                    .add(bDuplicate)
                    .add(bDelete)
                    .add(cbProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpCustomizers, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bDeleteActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDeleteActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_bDeleteActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bDuplicate;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JLabel lProfile;
    private javax.swing.JTabbedPane tpCustomizers;
    // End of variables declaration//GEN-END:variables
    
    
    private void setCurrentProfile (String profile) {
        if (colorModel.isCustomProfile (profile))
            loc (bDelete, "CTL_Delete");                              // NOI18N
        else
            loc (bDelete, "CTL_Restore");                             // NOI18N
        currentProfile = profile;
        highlightingPanel.setCurrentProfile (currentProfile);
        syntaxColoringPanel.setCurrentProfile (currentProfile);
        annotationsPanel.setCurrentProfile (currentProfile);
    }
    
    private void deleteCurrentProfile () {
        String currentProfile = (String) cbProfile.getSelectedItem ();
        highlightingPanel.deleteProfile (currentProfile);
        syntaxColoringPanel.deleteProfile (currentProfile);
        annotationsPanel.deleteProfile (currentProfile);
        if (colorModel.isCustomProfile (currentProfile)) {
            cbProfile.removeItem (currentProfile);
            cbProfile.setSelectedIndex (0);
        }
    }
    
    
    // other methods ...........................................................
    
    void update () {
        if (colorModel == null)
            colorModel = new ColorModel ();
        
        highlightingPanel.update (colorModel);
        syntaxColoringPanel.update (colorModel);
        annotationsPanel.update (colorModel);
        
        currentProfile = colorModel.getCurrentProfile ();
        if (colorModel.isCustomProfile (currentProfile))
            loc (bDelete, "CTL_Delete"); // NOI18N
        else
            loc (bDelete, "CTL_Restore"); // NOI18N

        // init schemes
        listen = false;
        Iterator it = colorModel.getProfiles ().iterator ();
        cbProfile.removeAllItems ();
        while (it.hasNext ())
            cbProfile.addItem (it.next ());
        listen = true;
        cbProfile.setSelectedItem (currentProfile);
    }
    
    
    
    void applyChanges () {
        highlightingPanel.applyChanges ();
        syntaxColoringPanel.applyChanges ();
        annotationsPanel.applyChanges ();
        if (colorModel == null) return;
        colorModel.setCurrentProfile (currentProfile);
    }
    
    void cancel () {
        highlightingPanel.cancel ();
        syntaxColoringPanel.cancel ();
        annotationsPanel.cancel ();
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        if (currentProfile != null &&
            colorModel != null &&
            !currentProfile.equals (colorModel.getCurrentProfile ())
        ) return true;
        if (highlightingPanel.isChanged ()) return true;
        if (syntaxColoringPanel.isChanged ()) return true;
        if (annotationsPanel.isChanged ()) return true;
        return false;
    }
   
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        if (e.getSource () == bDuplicate) {
            InputLine il = new InputLine (
                loc ("CTL_Create_New_Profile_Message"),                // NOI18N
                loc ("CTL_Create_New_Profile_Title")                   // NOI18N
            );
            il.setInputText (currentProfile);
            DialogDisplayer.getDefault ().notify (il);
            if (il.getValue () == NotifyDescriptor.OK_OPTION) {
                String newScheme = il.getInputText ();
                Iterator it = colorModel.getProfiles ().iterator ();
                while (it.hasNext ())
                    if (newScheme.equals (it.next ())) {
                        Message md = new Message (
                            loc ("CTL_Duplicate_Profile_Name"),        // NOI18N
                            Message.ERROR_MESSAGE
                        );
                        DialogDisplayer.getDefault ().notify (md);
                        return;
                    }
                setCurrentProfile (newScheme);
                listen = false;
                cbProfile.addItem (il.getInputText ());
                cbProfile.setSelectedItem (il.getInputText ());
                listen = true;
            }
            return;
        }
        if (e.getSource () == bDelete) {
            deleteCurrentProfile ();
            return;
        }
    }
    
    Collection getDefaults () {
        return syntaxColoringPanel.getAllLanguages ();
    }
    
    Collection getHighlights () {
        return highlightingPanel.getHighlightings ();
    }
    
    Collection getSyntaxColorings () {
        return syntaxColoringPanel.getSyntaxColorings ();
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (FontAndColorsPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }
}
