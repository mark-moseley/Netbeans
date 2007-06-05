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
        setName(loc ("Indentation_Tab"));
        loc (lStatementContinuationIndent, "Statement_Indent");
        loc (lNumberOfSpacesPerIndent, "Indent");
        loc (lPreview, "Preview");
        loc (cbExpandTabsToSpaces, "Expand_Tabs");
        loc (cbAddLeadingStarInComments, "Add_Leading_Star");
        loc (cbAddNewLineBeforeBrace, "Add_New_Line");
        loc (cbAddSpaceBeforeParenthesis, "Add_Space");
        epPreview.getAccessibleContext ().setAccessibleName (loc ("AN_Preview"));
        epPreview.getAccessibleContext ().setAccessibleDescription (loc ("AD_Preview"));

        //listeners
        epPreview.setBorder (new EtchedBorder ());
        cbAddNewLineBeforeBrace.addActionListener (this);
        cbAddLeadingStarInComments.addActionListener (this);
        cbExpandTabsToSpaces.addActionListener (this);
        cbAddSpaceBeforeParenthesis.addActionListener (this);
        sStatementContinuationIndent.setModel (new SpinnerNumberModel (8, 1, 50, 1));
        sStatementContinuationIndent.addChangeListener (this);
        sNumberOfSpacesPerIndent.setModel (new SpinnerNumberModel (4, 1, 50, 1));
        sNumberOfSpacesPerIndent.addChangeListener (this);
        epPreview.setEnabled (false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lStatementContinuationIndent = new javax.swing.JLabel();
        sStatementContinuationIndent = new javax.swing.JSpinner();
        lNumberOfSpacesPerIndent = new javax.swing.JLabel();
        sNumberOfSpacesPerIndent = new javax.swing.JSpinner();
        cbExpandTabsToSpaces = new javax.swing.JCheckBox();
        cbAddLeadingStarInComments = new javax.swing.JCheckBox();
        cbAddNewLineBeforeBrace = new javax.swing.JCheckBox();
        cbAddSpaceBeforeParenthesis = new javax.swing.JCheckBox();
        lPreview = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        epPreview = new javax.swing.JEditorPane();

        lStatementContinuationIndent.setLabelFor(sStatementContinuationIndent);
        lStatementContinuationIndent.setText("Statement Continuation Indent:");

        lNumberOfSpacesPerIndent.setLabelFor(sNumberOfSpacesPerIndent);
        lNumberOfSpacesPerIndent.setText("Number of Spaces per Indent:");

        cbExpandTabsToSpaces.setText("Expand Tabs to Spaces");
        cbExpandTabsToSpaces.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbExpandTabsToSpaces.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbAddLeadingStarInComments.setText("Add Leading Star in Comments");
        cbAddLeadingStarInComments.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAddLeadingStarInComments.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbAddNewLineBeforeBrace.setText("Add New Line Before Brace");
        cbAddNewLineBeforeBrace.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAddNewLineBeforeBrace.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbAddSpaceBeforeParenthesis.setText("Add Space Before Parenthesis");
        cbAddSpaceBeforeParenthesis.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAddSpaceBeforeParenthesis.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lPreview.setText("Preview:");

        jScrollPane1.setViewportView(epPreview);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lPreview)
                    .add(lNumberOfSpacesPerIndent)
                    .add(lStatementContinuationIndent))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sStatementContinuationIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbExpandTabsToSpaces)
                    .add(cbAddLeadingStarInComments)
                    .add(cbAddNewLineBeforeBrace)
                    .add(cbAddSpaceBeforeParenthesis))
                .addContainerGap(49, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lStatementContinuationIndent)
                    .add(sStatementContinuationIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cbExpandTabsToSpaces))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lNumberOfSpacesPerIndent)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cbAddLeadingStarInComments))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbAddNewLineBeforeBrace)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbAddSpaceBeforeParenthesis)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lPreview)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAddLeadingStarInComments;
    private javax.swing.JCheckBox cbAddNewLineBeforeBrace;
    private javax.swing.JCheckBox cbAddSpaceBeforeParenthesis;
    private javax.swing.JCheckBox cbExpandTabsToSpaces;
    private javax.swing.JEditorPane epPreview;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lNumberOfSpacesPerIndent;
    private javax.swing.JLabel lPreview;
    private javax.swing.JLabel lStatementContinuationIndent;
    private javax.swing.JSpinner sNumberOfSpacesPerIndent;
    private javax.swing.JSpinner sStatementContinuationIndent;
    // End of variables declaration//GEN-END:variables
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (IndentationPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }

    private void updatePreview () {
        model.setJavaFormatLeadingStarInComment (cbAddLeadingStarInComments.isSelected ());
        model.setJavaFormatNewlineBeforeBrace (cbAddNewLineBeforeBrace.isSelected ());
        model.setJavaFormatSpaceBeforeParenthesis (cbAddSpaceBeforeParenthesis.isSelected ());
        model.setExpandTabs (cbExpandTabsToSpaces.isSelected ());
        model.setJavaFormatStatementContinuationIndent (
            (Integer) sStatementContinuationIndent.getValue ()
        );
        model.setSpacesPerTab (
            (Integer) sNumberOfSpacesPerIndent.getValue ()
        );
        
        // start formatter
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                epPreview.setText (originalText);
                Document doc = epPreview.getDocument ();
                if (doc instanceof BaseDocument)
                try {
                    ((BaseDocument) doc).getFormatter ().reformat (
                        (BaseDocument) doc, 
                        0, 
                        ((BaseDocument) doc).getEndPosition ().getOffset () - 1
                    );
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                }
            }
        });
    }
    
    
    // ActionListener ..........................................................
    
    public void stateChanged (ChangeEvent e) {
        if (!listen) return;
        updatePreview ();
        if (changed != model.isChanged ())
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        changed = model.isChanged ();
    }
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        updatePreview ();
        if (changed != model.isChanged ())
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        changed = model.isChanged ();
    }

    public void update () {
        model = new IndentationModel ();
        
        if (originalText == null) {
            // add text to preview
            InputStream is = getClass ().getResourceAsStream 
                ("/org/netbeans/modules/options/indentation/indentationExample");
            BufferedReader r = new BufferedReader (new InputStreamReader (is));
            StringBuffer sb = new StringBuffer ();
            try {
                String line = r.readLine ();
                while (line != null) {
                    sb.append (line).append ('\n');
                    line = r.readLine ();
                }
                originalText = new String (sb);
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        // init components
        listen = false;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                epPreview.setContentType ("text/x-java");
                cbExpandTabsToSpaces.setSelected (model.isExpandTabs ());
                cbAddLeadingStarInComments.setSelected 
                        (model.getJavaFormatLeadingStarInComment ());
                cbAddNewLineBeforeBrace.setSelected 
                        (model.getJavaFormatNewlineBeforeBrace ());
                cbAddSpaceBeforeParenthesis.setSelected 
                        (model.getJavaFormatSpaceBeforeParenthesis ());
                sNumberOfSpacesPerIndent.setValue (model.getSpacesPerTab ());
                sStatementContinuationIndent.setValue 
                        (model.getJavaFormatStatementContinuationIndent ());
                listen = true;

                // update preview
                updatePreview ();
            }
        });
    }
    
    public void applyChanges () {
        if (model != null)
            model.applyChanges ();
    }
    
    public void cancel () {
        if (model != null)
            model.revertChanges ();
    }
    
    public boolean dataValid () {
        return true;
    }
    
    public boolean isChanged () {
        if (model == null) return false;
        return model.isChanged ();
    }
}
