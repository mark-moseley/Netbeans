/*
 * PanelOptionsVisual.java
 *
 * Created on March 9, 2004, 3:39 PM
 */

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

/** XXX I18N
 *
 * @author  phrebejk
 */
public class PanelOptionsVisual extends javax.swing.JPanel implements ActionListener {
    
    private static boolean lastMainClassCheck = false; // XXX Store somewhere
    
    private PanelConfigureProject panel;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual( PanelConfigureProject panel, boolean isLibrary ) {
        initComponents();
        this.panel = panel;
        
        if ( isLibrary ) {
            setAsMainCheckBox.setVisible( false );
            createMainCheckBox.setVisible( false );
            mainClassTextField.setVisible( false );
        }
        else {        
            createMainCheckBox.addActionListener( this );
            useExistingSourcesCheckBox.addActionListener( this );

            createMainCheckBox.setSelected( lastMainClassCheck );
            mainClassTextField.setEnabled( lastMainClassCheck );
        }
    }

    public void actionPerformed( ActionEvent e ) {
        
        if ( e.getSource() == createMainCheckBox ) {
            lastMainClassCheck = createMainCheckBox.isSelected();
            mainClassTextField.setEnabled( lastMainClassCheck );        
        }        
        else if ( e.getSource() == useExistingSourcesCheckBox && useExistingSourcesCheckBox.isSelected() ) {            
            // XXX            
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                "Not implemented yet", 
                 NotifyDescriptor.INFORMATION_MESSAGE ));
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        useExistingSourcesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setAsMainCheckBox.setSelected(true);
        setAsMainCheckBox.setText("Set as Main Project");
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(setAsMainCheckBox, gridBagConstraints);

        createMainCheckBox.setText("Create Main Class:");
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(createMainCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(mainClassTextField, gridBagConstraints);

        useExistingSourcesCheckBox.setText("Use Existing Sources");
        useExistingSourcesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(useExistingSourcesCheckBox, gridBagConstraints);

    }//GEN-END:initComponents
    
    boolean valid() {
        return true;
    }

    void store( WizardDescriptor d ) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty( /*XXX Define somewhere */ "mainClass", createMainCheckBox.isSelected() ? mainClassTextField.getText() : null ); // NOI18N      
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JCheckBox useExistingSourcesCheckBox;
    // End of variables declaration//GEN-END:variables
    
}

