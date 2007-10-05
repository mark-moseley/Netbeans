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


package org.netbeans.modules.uml.propertysupport.options.panels;

import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.IShowMessageType;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs.ApplicationColorsAndFonts;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author  krichard
 */
public class GeneralOptionsPanelForm extends javax.swing.JPanel {

    private final String PSK_ALWAYS = "PSK_ALWAYS";
    private final String PSK_NEVER = "PSK_NEVER";
    private final String PSK_SELECTED = "PSK_SELECTED";

    public final String PSK_RESIZE_ASNEEDED = "PSK_RESIZE_ASNEEDED";
    public final String PSK_RESIZE_EXPANDONLY = "PSK_RESIZE_EXPANDONLY";
    public final String PSK_RESIZE_UNLESSMANUAL = "PSK_RESIZE_UNLESSMANUAL";
    public final String PSK_RESIZE_NEVER = "PSK_RESIZE_NEVER";

    //for menu display
    private final String ALWAYS = NbBundle.getMessage(GeneralOptionsPanelForm.class, "ALWAYS");
    private final String NEVER = NbBundle.getMessage(GeneralOptionsPanelForm.class, "NEVER");
    private final String SELECTED = NbBundle.getMessage(GeneralOptionsPanelForm.class, "SELECTED");
    private final String ASNEEDED = NbBundle.getMessage(GeneralOptionsPanelForm.class, "ASNEEDED");
    private final String EXPANDONLY = NbBundle.getMessage(GeneralOptionsPanelForm.class, "EXPANDONLY");
    private final String UNLESSMANUAL = NbBundle.getMessage(GeneralOptionsPanelForm.class, "UNLESSMANUAL");
    private final String RESIZE_NEVER = NbBundle.getMessage(GeneralOptionsPanelForm.class, "RESIZE_NEVER");

            
    //for Display Seq Diagram Messages
    private final String SHOW_NOTHING = NbBundle.getMessage(GeneralOptionsPanelForm.class,"SMT_NOTHING");
    private final String SHOW_OPERATION = NbBundle.getMessage(GeneralOptionsPanelForm.class, "SMT_OPERATION");
    private final String SHOW_NAME = NbBundle.getMessage(GeneralOptionsPanelForm.class, "SMT_NAME");
            
    private final String[] SQD_MSG = { SHOW_NOTHING, SHOW_OPERATION, SHOW_NAME} ;
            
    private final Integer[] mapped_SQD_MSG = {IShowMessageType.SMT_NONE, IShowMessageType.SMT_OPERATION, IShowMessageType.SMT_NAME};
    
    private final String[] displayChoices = {ALWAYS, SELECTED, NEVER};
    private final String[] mappedChoices = {PSK_ALWAYS, PSK_SELECTED, PSK_NEVER};

    private final String[] resizeDisplayChoices = {ASNEEDED, EXPANDONLY, UNLESSMANUAL, RESIZE_NEVER};
    private final String[] resizeMappedChoices = {PSK_RESIZE_ASNEEDED, PSK_RESIZE_EXPANDONLY, PSK_RESIZE_UNLESSMANUAL, PSK_RESIZE_NEVER};

    /** Creates new form GeneralOptionsPanel */
    public GeneralOptionsPanelForm() {
        initComponents();
    }

    public void store() {
        int autoResizeIndex = autoResizeElementsComboBox.getSelectedIndex();
        int displayCompartmentIndex = displayCompartmentTitlesComboBox.getSelectedIndex();
        int sqdDisplayMsgIndex = this.seqDiagMsgCB.getSelectedIndex() ;
        
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);

        prefs.putBoolean("UML_Show_Aliases", showAlias.isSelected());
        prefs.putBoolean("UML_Open_Project_Diagrams", openProjectDiagramsCB.isSelected());

        prefs.put("UML_Automatically_Size_Elements", resizeMappedChoices[autoResizeIndex]);
        prefs.put("UML_Display_Compartment_Titles", mappedChoices[displayCompartmentIndex]);

        prefs.putBoolean("UML_Display_Empty_Lists", displayEmpty.isSelected());
        prefs.putBoolean("UML_Gradient_Background", gradient.isSelected());
        prefs.putBoolean("UML_Reconnect_to_Presentation_Boundary", reconnect.isSelected());
        prefs.putBoolean("UML_Resize_with_Show_Aliases_Mode", resizeCB.isSelected());
        prefs.putBoolean("UML_Show_Stereotype_Icons", showStereotype.isSelected());
        prefs.putBoolean("UML_Ask_Before_Layout", askLayoutCB.isSelected());

        prefs.putInt("UML_SQD_DEFAULT_MSG", mapped_SQD_MSG[sqdDisplayMsgIndex]);

    }

    public void load() {
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);
        
        if (prefs.getBoolean("UML_Show_Aliases", false)) {
            showAlias.setSelected(true);
        } else {
            showAlias.setSelected(false);
        }
        if (prefs.getBoolean("UML_Open_Project_Diagrams", true)) {
            openProjectDiagramsCB.setSelected(true);
        } else {
            openProjectDiagramsCB.setSelected(false);
        }
        if (prefs.getBoolean("UML_Display_Empty_Lists", true)) {
            displayEmpty.setSelected(true);
        } else {
            displayEmpty.setSelected(false);
        }
        if (prefs.getBoolean("UML_Gradient_Background", true)) {
            gradient.setSelected(true);
        } else {
            gradient.setSelected(false);
        }
        if (prefs.getBoolean("UML_Reconnect_to_Presentation_Boundary", true)) {
            reconnect.setSelected(true);
        } else {
            reconnect.setSelected(false);
        }
        if (prefs.getBoolean("UML_Resize_with_Show_Aliases_Mode", false)) {
            resizeCB.setSelected(true);
        } else {
            resizeCB.setSelected(false);
        }
        if (prefs.getBoolean("UML_Show_Stereotype_Icons", true)) {
            showStereotype.setSelected(true);
        } else {
            showStereotype.setSelected(false);
        }
        if (prefs.getBoolean("UML_Ask_Before_Layout", true)) {
            askLayoutCB.setSelected(true);
        } else {
            askLayoutCB.setSelected(false);
        }
        
        String autoResizeValue = prefs.get("UML_Automatically_Size_Elements", null);
        String displayCompartmentValue = prefs.get("UML_Display_Compartment_Titles", null);
        Integer sqdMsgVal = prefs.getInt("UML_SQD_DEFAULT_MSG", IShowMessageType.SMT_NONE) ;

        int autoResizeIndex = getMappedIndex(resizeMappedChoices, autoResizeValue);
        int compartmentIndex = getMappedIndex(mappedChoices, displayCompartmentValue);
        int sqdMsgIndex = getMappedIndex(mapped_SQD_MSG, sqdMsgVal);

        autoResizeElementsComboBox.setSelectedIndex(autoResizeIndex);
        seqDiagMsgCB.setSelectedIndex(sqdMsgIndex);
        displayCompartmentTitlesComboBox.setSelectedIndex(compartmentIndex);
       
    }

    public void cancel() {
        //do nothing ;
    }

    private int getMappedIndex(Object[] a, Object s) {

        int n = a.length;

        for (int i = 0; i < n; i++) {
            if (a[i].equals(s)) {
                return i;
            }
        }

        return 0;
    }

    public void showFontsAndColorsDialog() {
        new ApplicationColorsAndFonts().setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        advancedPropsPanel = new javax.swing.JPanel();
        openProjectDiagramsCB = new javax.swing.JCheckBox();
        showAlias = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        displayEmpty = new javax.swing.JCheckBox();
        reconnect = new javax.swing.JCheckBox();
        resizeCB = new javax.swing.JCheckBox();
        showStereotype = new javax.swing.JCheckBox();
        gradient = new javax.swing.JCheckBox();
        askLayoutCB = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        autoResizeElementsComboBox = new JComboBox(resizeDisplayChoices);
        displayCompartmentTitlesComboBox = new JComboBox (displayChoices);
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        seqDiagMsgCB = new JComboBox (SQD_MSG);
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        openProjectDiagramsCB.setSelected(true);
        openProjectDiagramsCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.openProjectDiagramsCB.text")); // NOI18N
        openProjectDiagramsCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showAlias.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.showAlias.text")); // NOI18N
        showAlias.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout advancedPropsPanelLayout = new org.jdesktop.layout.GroupLayout(advancedPropsPanel);
        advancedPropsPanel.setLayout(advancedPropsPanelLayout);
        advancedPropsPanelLayout.setHorizontalGroup(
            advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedPropsPanelLayout.createSequentialGroup()
                .add(advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showAlias)
                    .add(openProjectDiagramsCB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        advancedPropsPanelLayout.setVerticalGroup(
            advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedPropsPanelLayout.createSequentialGroup()
                .add(openProjectDiagramsCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showAlias)
                .addContainerGap())
        );

        displayEmpty.setSelected(true);
        displayEmpty.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.displayEmpty.text")); // NOI18N
        displayEmpty.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reconnect.setSelected(true);
        reconnect.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.reconnect.text")); // NOI18N
        reconnect.setMargin(new java.awt.Insets(0, 0, 0, 0));

        resizeCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.resizeCB.text")); // NOI18N
        resizeCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showStereotype.setSelected(true);
        showStereotype.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.showStereotype.text")); // NOI18N
        showStereotype.setMargin(new java.awt.Insets(0, 0, 0, 0));

        gradient.setSelected(true);
        gradient.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.gradient.text")); // NOI18N
        gradient.setMargin(new java.awt.Insets(0, 0, 0, 0));

        askLayoutCB.setSelected(true);
        askLayoutCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.askLayoutCB.text")); // NOI18N
        askLayoutCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel5.text")); // NOI18N

        jButton1.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(askLayoutCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(reconnect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(displayEmpty, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(resizeCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(showStereotype, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .add(gradient, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(displayEmpty))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reconnect)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resizeCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showStereotype)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gradient)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(askLayoutCB)
                .add(18, 18, 18)
                .add(jButton1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jLabel2.text")); // NOI18N

        autoResizeElementsComboBox.setDoubleBuffered(true);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jLabel1.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel6.text")); // NOI18N

        seqDiagMsgCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seqDiagMsgCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(autoResizeElementsComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(displayCompartmentTitlesComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(seqDiagMsgCB, 0, 201, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(autoResizeElementsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(displayCompartmentTitlesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(seqDiagMsgCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel3.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel4.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedPropsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(advancedPropsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(26, 26, 26)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showFontsAndColorsDialog();
}//GEN-LAST:event_jButton1ActionPerformed

    private void seqDiagMsgCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seqDiagMsgCBActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_seqDiagMsgCBActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPropsPanel;
    private javax.swing.JCheckBox askLayoutCB;
    private javax.swing.JComboBox autoResizeElementsComboBox;
    private javax.swing.JComboBox displayCompartmentTitlesComboBox;
    private javax.swing.JCheckBox displayEmpty;
    private javax.swing.JCheckBox gradient;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox openProjectDiagramsCB;
    private javax.swing.JCheckBox reconnect;
    private javax.swing.JCheckBox resizeCB;
    private javax.swing.JComboBox seqDiagMsgCB;
    private javax.swing.JCheckBox showAlias;
    private javax.swing.JCheckBox showStereotype;
    // End of variables declaration//GEN-END:variables
}
