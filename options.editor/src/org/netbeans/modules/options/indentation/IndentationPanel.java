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

package org.netbeans.modules.options.indentation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class IndentationPanel extends JPanel implements ChangeListener, 
ActionListener {
    
    private IndentationModel    model;
    private String              originalText;
    private boolean             listen = false;
    private boolean             changed = false;

    
    /** 
     * Creates new form IndentationPanel.
     */
    public IndentationPanel () {
        initComponents ();
        
        // localization
        setName(loc ("Indentation_Tab")); //NOI18N
        loc (lNumberOfSpacesPerIndent, "Indent"); //NOI18N
        loc (lTabSize, "TabSize"); //NOI18N
        loc (lPreview, "Preview"); //NOI18N
        loc (lExpandTabsToSpaces, "Expand_Tabs"); //NOI18N
        loc (lRightMargin, "Right_Margin"); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleName (loc ("AN_Expand_Tabs")); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleDescription (loc ("AD_Expand_Tabs")); //NOI18N
        epPreview.getAccessibleContext ().setAccessibleName (loc ("AN_Preview")); //NOI18N
        epPreview.getAccessibleContext ().setAccessibleDescription (loc ("AD_Preview")); //NOI18N

        //listeners
        epPreview.setBorder (new EtchedBorder ());
        cbExpandTabsToSpaces.addActionListener (this);
        sNumberOfSpacesPerIndent.setModel (new SpinnerNumberModel (4, 1, 50, 1));
        sNumberOfSpacesPerIndent.addChangeListener (this);
        sTabSize.setModel (new SpinnerNumberModel (4, 1, 50, 1));
        sTabSize.addChangeListener (this);
        sRightMargin.setModel (new SpinnerNumberModel (120, 1, 200, 10));
        sRightMargin.addChangeListener (this);
        epPreview.setEnabled (false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lNumberOfSpacesPerIndent = new javax.swing.JLabel();
        sNumberOfSpacesPerIndent = new javax.swing.JSpinner();
        cbExpandTabsToSpaces = new javax.swing.JCheckBox();
        lPreview = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        epPreview = new javax.swing.JEditorPane();
        lTabSize = new javax.swing.JLabel();
        sTabSize = new javax.swing.JSpinner();
        lExpandTabsToSpaces = new javax.swing.JLabel();
        lRightMargin = new javax.swing.JLabel();
        sRightMargin = new javax.swing.JSpinner();

        lNumberOfSpacesPerIndent.setLabelFor(sNumberOfSpacesPerIndent);
        lNumberOfSpacesPerIndent.setText("Number of Spaces per Indent:");

        cbExpandTabsToSpaces.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbExpandTabsToSpaces.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lPreview.setText("Preview:");

        jScrollPane1.setViewportView(epPreview);

        lTabSize.setLabelFor(sTabSize);
        lTabSize.setText("Tab Size:");

        lExpandTabsToSpaces.setLabelFor(cbExpandTabsToSpaces);
        lExpandTabsToSpaces.setText("Expand Tabs To Spaces:");

        lRightMargin.setLabelFor(sRightMargin);
        lRightMargin.setText("Right Margin:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(lPreview)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                    .add(lTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(lRightMargin)
                                .add(140, 140, 140)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(sRightMargin)
                            .add(sTabSize)
                            .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
                        .add(204, 204, 204))
                    .add(layout.createSequentialGroup()
                        .add(cbExpandTabsToSpaces)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lExpandTabsToSpaces)))
                .add(294, 294, 294))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbExpandTabsToSpaces)
                    .add(lExpandTabsToSpaces))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lNumberOfSpacesPerIndent)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lTabSize)
                    .add(sTabSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lRightMargin)
                    .add(sRightMargin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lPreview)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbExpandTabsToSpaces;
    private javax.swing.JEditorPane epPreview;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lExpandTabsToSpaces;
    private javax.swing.JLabel lNumberOfSpacesPerIndent;
    private javax.swing.JLabel lPreview;
    private javax.swing.JLabel lRightMargin;
    private javax.swing.JLabel lTabSize;
    private javax.swing.JSpinner sNumberOfSpacesPerIndent;
    private javax.swing.JSpinner sRightMargin;
    private javax.swing.JSpinner sTabSize;
    // End of variables declaration//GEN-END:variables
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (IndentationPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key)); //NOI18N
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText ((AbstractButton) c, loc ("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText ((JLabel) c, loc ("CTL_" + key)); //NOI18N
        }
    }

    private void updatePreview () {
        model.setExpandTabs (cbExpandTabsToSpaces.isSelected ());
        model.setSpacesPerTab (
            (Integer) sNumberOfSpacesPerIndent.getValue ()
        );
        model.setTabSize(
            (Integer) sTabSize.getValue ()
        );
        model.setRightMargin(
            (Integer) sRightMargin.getValue ()
        );
        
        // start formatter
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                epPreview.setText (originalText);
                Document doc = epPreview.getDocument ();
                if (doc instanceof BaseDocument) {
                    BaseDocument bdoc = (BaseDocument)doc;
                    Formatter formatter = bdoc.getFormatter();
                    formatter.reformatLock();
                    bdoc.atomicLock();
                    try {
                        formatter.reformat (bdoc, 0, bdoc.getLength());
                    } catch (BadLocationException ex) {
                        ex.printStackTrace ();
                    } finally {
                        bdoc.atomicUnlock();
                        formatter.reformatUnlock();
                    }
                }
            }
        });
    }
    
    
    // ActionListener ..........................................................
    
    public void stateChanged (ChangeEvent e) {
        if (!listen) return;
        updatePreview ();
        if (changed != model.isChanged ()) {
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        }
        changed = model.isChanged ();
    }
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        updatePreview ();
        if (changed != model.isChanged ()) {
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        }
        changed = model.isChanged ();
    }

    public void update () {
        model = new IndentationModel ();
        
        if (originalText == null) {
            // add text to preview
            try {
                InputStream is = getClass ().getResourceAsStream("/org/netbeans/modules/options/indentation/indentationExample"); //NOI18N
                BufferedReader r = new BufferedReader (new InputStreamReader (is));
                try {
                    StringBuffer sb = new StringBuffer ();
                    String line = r.readLine ();
                    while (line != null) {
                        sb.append (line).append ('\n'); //NOI18N
                        line = r.readLine ();
                    }
                        originalText = new String (sb);
                } finally {
                    r.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        // init components
        listen = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                epPreview.setContentType("text/xml");
                cbExpandTabsToSpaces.setSelected(model.isExpandTabs());
                sNumberOfSpacesPerIndent.setValue(model.getSpacesPerTab());
                sTabSize.setValue(model.getTabSize());
                sRightMargin.setValue(model.getRightMargin());
                listen = true;

                // update preview
                updatePreview();
            }
        });
    }
    
    public void applyChanges () {
        if (model != null) {
            model.applyChanges ();
        }
    }
    
    public void cancel () {
        if (model != null) {
            model.revertChanges ();
        }
    }
    
    public boolean dataValid () {
        return true;
    }
    
    public boolean isChanged () {
        if (model == null) {
            return false;
        } else {
            return model.isChanged ();
        }
    }
}
