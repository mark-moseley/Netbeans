/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerJar extends JPanel implements HelpCtx.Provider {
    
    public CustomizerJar( J2SEProjectProperties uiProperties ) {
        initComponents();
        
        jTextFieldDistDir.setDocument( uiProperties.DIST_JAR_MODEL );
        jTextFieldExcludes.setDocument( uiProperties.BUILD_CLASSES_EXCLUDES_MODEL );
        
        uiProperties.JAR_COMPRESS_MODEL.setMnemonic( jCheckBoxCommpress.getMnemonic() );
        jCheckBoxCommpress.setModel( uiProperties.JAR_COMPRESS_MODEL ); 
    } 
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerJar.class );
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelDistDir = new javax.swing.JLabel();
        jTextFieldDistDir = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldExcludes = new javax.swing.JTextField();
        jCheckBoxCommpress = new javax.swing.JCheckBox();
        excludeMessage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelDistDir.setLabelFor(jTextFieldDistDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDistDir, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_DistDir_JTextField"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelDistDir, gridBagConstraints);

        jTextFieldDistDir.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jTextFieldDistDir, gridBagConstraints);
        jTextFieldDistDir.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldDistDir"));

        jLabel2.setLabelFor(jTextFieldExcludes);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Excludes_JTextField"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jTextFieldExcludes, gridBagConstraints);
        jTextFieldExcludes.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldExcludes"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCommpress, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Commpres_JCheckBox"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(jCheckBoxCommpress, gridBagConstraints);
        jCheckBoxCommpress.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jCheckBoxCompress"));

        org.openide.awt.Mnemonics.setLocalizedText(excludeMessage, java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle").getString("LBL_CustomizerJar_ExcludeMessage"));
        excludeMessage.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(excludeMessage, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JCheckBox jCheckBoxCommpress;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDistDir;
    private javax.swing.JTextField jTextFieldDistDir;
    private javax.swing.JTextField jTextFieldExcludes;
    // End of variables declaration//GEN-END:variables
                
}
