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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.ui;

import java.io.InputStream;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Petr Pisl
 */
public class TemplatePanelVisual extends javax.swing.JPanel implements HelpCtx.Provider{
    
    /** Creates new form TemplatePanelVisual */
    public TemplatePanelVisual() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        bgTemplates = new javax.swing.ButtonGroup();
        bgLayout = new javax.swing.ButtonGroup();
        jpTemplateChooser = new javax.swing.JPanel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();

        jpTemplateChooser.setLayout(new java.awt.GridLayout(2, 0, 10, 10));

        bgTemplates.add(jRadioButton2);
        jRadioButton2.setSelected(true);
        jRadioButton2.setActionCommand("1");
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template1-unselected.png")));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template1-selected.png")));
        jpTemplateChooser.add(jRadioButton2);

        bgTemplates.add(jRadioButton3);
        jRadioButton3.setActionCommand("2");
        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template2-unselected.png")));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton3.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template2-selected.png")));
        jpTemplateChooser.add(jRadioButton3);

        bgTemplates.add(jRadioButton4);
        jRadioButton4.setActionCommand("3");
        jRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template8-unselected.png")));
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton4.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template8-selected.png")));
        jpTemplateChooser.add(jRadioButton4);

        bgTemplates.add(jRadioButton5);
        jRadioButton5.setActionCommand("4");
        jRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template7-unselected.png")));
        jRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton5.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template8-selected.png")));
        jpTemplateChooser.add(jRadioButton5);

        bgTemplates.add(jRadioButton6);
        jRadioButton6.setActionCommand("5");
        jRadioButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template3-unselected.png")));
        jRadioButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton6.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template3-selected.png")));
        jpTemplateChooser.add(jRadioButton6);

        bgTemplates.add(jRadioButton7);
        jRadioButton7.setActionCommand("6");
        jRadioButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template6-unselected.png")));
        jRadioButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton7.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template6-selected.png")));
        jpTemplateChooser.add(jRadioButton7);

        bgTemplates.add(jRadioButton8);
        jRadioButton8.setActionCommand("7");
        jRadioButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template4-unselected.png")));
        jRadioButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton8.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template4-selected.png")));
        jpTemplateChooser.add(jRadioButton8);

        bgTemplates.add(jRadioButton9);
        jRadioButton9.setActionCommand("8");
        jRadioButton9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jRadioButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template5-unselected.png")));
        jRadioButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton9.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/frameworks/facelets/resources/template5-selected.png")));
        jpTemplateChooser.add(jRadioButton9);

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/frameworks/facelets/ui/Bundle").getString("LBL_Layout"));

        bgLayout.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/frameworks/facelets/ui/Bundle").getString("LBL_CSS_Layout"));
        jRadioButton1.setActionCommand("css");
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        bgLayout.add(jRadioButton10);
        jRadioButton10.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/frameworks/facelets/ui/Bundle").getString("LBL_Table_Layout"));
        jRadioButton10.setActionCommand("table");
        jRadioButton10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jRadioButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jRadioButton10)
                .add(201, 201, 201))
            .add(jpTemplateChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jRadioButton1)
                    .add(jRadioButton10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jpTemplateChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgLayout;
    private javax.swing.ButtonGroup bgTemplates;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JPanel jpTemplateChooser;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx(TemplatePanelVisual.class);
    }
    
    public InputStream getTemplate(){
        String path = "org/netbeans/modules/web/frameworks/facelets/resources/templates/template-";  //NOI18N
        path = path + bgLayout.getSelection().getActionCommand() + "-";
        path = path + bgTemplates.getSelection().getActionCommand() + ".xhtml";          //NOI18N
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        return is;
    }
    
    public InputStream getDefaultCSS(){
        String path = "org/netbeans/modules/web/frameworks/facelets/resources/templates/default.css";
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }
    
    public InputStream getLayoutCSS(){
        String path = "org/netbeans/modules/web/frameworks/facelets/resources/templates/";
        path = path + bgLayout.getSelection().getActionCommand() + "Layout.css";
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }
    
    public String getLayoutFileName(){
        String name = bgLayout.getSelection().getActionCommand() + "Layout";
        return name;
    }
}
