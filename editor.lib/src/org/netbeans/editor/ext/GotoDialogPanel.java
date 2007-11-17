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

import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.JComponent;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import org.netbeans.editor.EditorState;
import org.openide.util.NbBundle;

/**
 * GotoDialogPanel is an UI object for entering line numbers to move caret to.
 * It maintains its own history (stored in EditorState).
 * For proper history functionality, it is needed to call
 * <CODE>updateHistory()</CODE> for valid inserts.
 *
 * @author Miloslav Metelka, Petr Nejedly
 * @version 2.0
 */
public class GotoDialogPanel extends JPanel implements FocusListener {

    static final long serialVersionUID =-8686958102543713464L;
    private static final String HISTORY_KEY = "GotoDialogPanel.history-goto-line"; // NOI18N
    private static final int MAX_ITEMS = 20;

    /** The variable used during updating combo to prevent firing */
    private boolean dontFire = false;
    private KeyEventBlocker blocker;
    private final ResourceBundle bundle = NbBundle.getBundle(org.netbeans.editor.BaseKit.class);

    /** Initializes the UI and fetches the history */
    public GotoDialogPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleName(bundle.getString("goto-title")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto")); // NOI18N
        gotoCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto-line")); // NOI18N
        List history = (List)EditorState.get( HISTORY_KEY );
        if( history == null ) history = new ArrayList();
        updateCombo( history );
    }

    /** Set the content of the history combo
     * @param content The List of items to be shown in the combo
     */
    protected void updateCombo( List content ) {
        dontFire = true;
        gotoCombo.setModel( new DefaultComboBoxModel( content.toArray() ) );
        dontFire = false;
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        gotoLabel = new javax.swing.JLabel();
        gotoCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        gotoLabel.setLabelFor(gotoCombo);
        gotoLabel.setText(bundle.getString("goto-line"));
        gotoLabel.setDisplayedMnemonic(bundle.getString("goto-line-mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(gotoLabel, gridBagConstraints);

        gotoCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 10);
        add(gotoCombo, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JComboBox gotoCombo;
    protected javax.swing.JLabel gotoLabel;
    // End of variables declaration//GEN-END:variables


    /** @return the current text from the input field */
    public String getValue() {
        return (String)gotoCombo.getEditor().getItem();
    }
    
    /** This method is to be called when caller wishes to add the current 
     * content of the input filed to the history
     */
    public void updateHistory() {
        List history = (List)EditorState.get( HISTORY_KEY );
        if( history == null ) history = new ArrayList();

        Object value = getValue();

        if( history.contains( value ) ) {
            // move it to top
            history.remove( value );
            history.add( 0, value );
        } else {
            // assure it won't hold more than MAX_ITEMS
            if( history.size() >= MAX_ITEMS )
                history = history.subList(0, MAX_ITEMS-1);
            // add the last entered value to the top
            history.add( 0, getValue() );
        }
        EditorState.put( HISTORY_KEY, history );
        
        updateCombo( history );
    }

    /** the method called to ensure that the input field would be a focused
     * component with the content selected
     */
    public void popupNotify(KeyEventBlocker blocker) {
        this.blocker = blocker;
        gotoCombo.getEditor().getEditorComponent().addFocusListener(this);
        gotoCombo.getEditor().selectAll();
        gotoCombo.getEditor().getEditorComponent().requestFocus();
    }

    public javax.swing.JComboBox getGotoCombo()
    {
        return gotoCombo;
    }

    public void focusGained(FocusEvent e) {
        if (blocker != null)
            blocker.stopBlocking();
        ((JComponent)e.getSource()).removeFocusListener(this);
    }

    public void focusLost(FocusEvent e) {
    }
}
