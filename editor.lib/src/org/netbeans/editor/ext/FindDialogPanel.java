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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.editor.ext;

import java.awt.Dimension;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import org.netbeans.editor.SettingsNames;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Miloslav Metelka, Petr Nejedly
 * @version 1.0
 * @deprecated Without any replacement.
 */
public class FindDialogPanel extends javax.swing.JPanel {

    static final long serialVersionUID =5048601763767383114L;

    private final ResourceBundle bundle = NbBundle.getBundle(org.netbeans.editor.BaseKit.class);

    /** Initializes the Form */
    public FindDialogPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleName(bundle.getString("find-title")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_find")); // NOI18N
        findWhat.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_" + SettingsNames.FIND_WHAT)); // NOI18N
        replaceWith.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_" + SettingsNames.FIND_REPLACE_WITH)); // NOI18N
        
        // #71956
        Dimension findPrefSize = findWhat.getPreferredSize();
        Dimension replacePrefSize = replaceWith.getPreferredSize();
        if (findPrefSize != null){
            findWhat.setPreferredSize(new Dimension((int)findPrefSize.getWidth(), (int)findPrefSize.getHeight()));
        }
        if (replacePrefSize != null){
            replaceWith.setPreferredSize(new Dimension((int)replacePrefSize.getWidth(), (int)replacePrefSize.getHeight()));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        findWhatPanel = new javax.swing.JPanel();
        findWhatLabel = new javax.swing.JLabel();
        findWhat = new javax.swing.JComboBox();
        replaceWithLabel = new javax.swing.JLabel();
        replaceWith = new javax.swing.JComboBox();
        highlightSearch = createCheckBox( SettingsNames.FIND_HIGHLIGHT_SEARCH);
        incSearch = createCheckBox( SettingsNames.FIND_INC_SEARCH);
        matchCase = createCheckBox( SettingsNames.FIND_MATCH_CASE);
        wholeWords = createCheckBox( SettingsNames.FIND_WHOLE_WORDS);
        bwdSearch = createCheckBox( SettingsNames.FIND_BACKWARD_SEARCH);
        wrapSearch = createCheckBox( SettingsNames.FIND_WRAP_SEARCH);
        regExp = createCheckBox( SettingsNames.FIND_REG_EXP);
        blockSearch = createCheckBox( SettingsNames.FIND_BLOCK_SEARCH);

        setLayout(new java.awt.GridBagLayout());

        findWhatPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(findWhatPanel, gridBagConstraints);

        findWhatLabel.setLabelFor(findWhat);
        findWhatLabel.setText(bundle.getString(SettingsNames.FIND_WHAT ) );
        findWhatLabel.setDisplayedMnemonic(bundle.getString(SettingsNames.FIND_WHAT + "-mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(findWhatLabel, gridBagConstraints);

        findWhat.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 9, 10);
        add(findWhat, gridBagConstraints);

        replaceWithLabel.setLabelFor(replaceWith);
        replaceWithLabel.setText(bundle.getString(SettingsNames.FIND_REPLACE_WITH ) );
        replaceWithLabel.setDisplayedMnemonic(bundle.getString(SettingsNames.FIND_REPLACE_WITH + "-mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 9, 0);
        add(replaceWithLabel, gridBagConstraints);

        replaceWith.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 9, 10);
        add(replaceWith, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 0);
        add(highlightSearch, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 10);
        add(incSearch, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 0);
        add(matchCase, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(wholeWords, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 10);
        add(bwdSearch, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 10);
        add(wrapSearch, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(regExp, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 10);
        add(blockSearch, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox blockSearch;
    protected javax.swing.JCheckBox bwdSearch;
    protected javax.swing.JComboBox findWhat;
    protected javax.swing.JLabel findWhatLabel;
    protected javax.swing.JPanel findWhatPanel;
    protected javax.swing.JCheckBox highlightSearch;
    protected javax.swing.JCheckBox incSearch;
    protected javax.swing.JCheckBox matchCase;
    protected javax.swing.JCheckBox regExp;
    protected javax.swing.JComboBox replaceWith;
    protected javax.swing.JLabel replaceWithLabel;
    protected javax.swing.JCheckBox wholeWords;
    protected javax.swing.JCheckBox wrapSearch;
    // End of variables declaration//GEN-END:variables


    private JCheckBox createCheckBox( String key ) {
        JCheckBox box = new JCheckBox();
        Mnemonics.setLocalizedText(box, bundle.getString(key));
        box.setToolTipText( bundle.getString( key + "-tooltip" ) );
        return box;
    }
    
}
