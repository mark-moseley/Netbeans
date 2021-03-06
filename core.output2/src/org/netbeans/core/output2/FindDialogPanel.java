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

package org.netbeans.core.output2;

import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Vector;
import org.openide.awt.Mnemonics;

class FindDialogPanel extends javax.swing.JPanel implements Runnable {

    static final long serialVersionUID =5048678953767663114L;

    private static Reference<FindDialogPanel> panel = null;
    private JButton acceptButton;
    private static Vector<Object> history = new Vector<Object>();
    
    /** Initializes the Form */
    FindDialogPanel() {
        initComponents ();
        Mnemonics.setLocalizedText(findWhatLabel, NbBundle.getBundle(FindDialogPanel.class).getString("LBL_Find_What"));
        getAccessibleContext().setAccessibleName(NbBundle.getBundle(FindDialogPanel.class).getString("ACSN_Find"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_Find"));
        findWhat.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_Find_What"));
        findWhat.getEditor().getEditorComponent().addKeyListener( new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if ( evt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    findWhat.setPopupVisible( false );
                    dialogRef.get().setVisible( false );
                }
            }
        });
        findWhat.getEditor().addActionListener( new java.awt.event.ActionListener() {
               public void actionPerformed( java.awt.event.ActionEvent evt ) {
                   //Give the component a chance to update its text field, or on
                   //first invocation the text will be null
                   SwingUtilities.invokeLater (FindDialogPanel.this);
               }
           });

       acceptButton = new JButton();
       Mnemonics.setLocalizedText(acceptButton, NbBundle.getBundle(FindDialogPanel.class).getString("BTN_Find"));
       acceptButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_FindBTN"));

       findWhat.setModel( new DefaultComboBoxModel( history ) );
       findWhatLabel.setFocusable(false);

       JComponent[] order = new JComponent[] {
           findWhat, acceptButton
       };

    }

    public void run() {
        acceptButton.doClick();
    }

    public static FindDialogPanel getPanel() {
        FindDialogPanel result = null;
        if (panel != null) {
            result = panel.get();
        }
        if (result == null) {
            result = new FindDialogPanel();
            panel = new SoftReference<FindDialogPanel> (result);
        }
        return result;
    }
    
    void setFindText(String text) {
        int end = text.indexOf("\n");
        String txt = text;
        if (end  > -1) {
            txt = text.substring(0, end);
        }
        if (!txt.equals(findWhat.getSelectedItem())) {
            findWhat.insertItemAt(txt, 0);
            findWhat.setSelectedIndex(0);
        }
        selectText();
    
    }
    
    private void selectText() {
        Component comp = findWhat.getEditor().getEditorComponent();
        if (comp instanceof JTextField) {
            JTextField fld = (JTextField)comp;
            fld.setSelectionStart(0);
            fld.setSelectionEnd(fld.getText().length());
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        findWhatLabel = new javax.swing.JLabel();
        findWhat = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        findWhatLabel.setLabelFor(findWhat);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(findWhatLabel, gridBagConstraints);

        findWhat.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 12);
        add(findWhat, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JComboBox findWhat;
    protected javax.swing.JLabel findWhatLabel;
    // End of variables declaration//GEN-END:variables



    static void showFindDialog(ActionListener al, String selection) {
        java.awt.Dialog dialog = getDialog();
        FindDialogPanel findPanel = getPanel();
        findPanel.acceptButton.putClientProperty ("panel", findPanel);
	if (selection != null) {
	    findPanel.setFindText(selection);
	}
        if (!Arrays.asList(findPanel.acceptButton.getActionListeners()).contains(al)) {
            findPanel.acceptButton.addActionListener(al);
        }
        dialog.setVisible(true);
        
        // always select the text.
        findPanel.selectText();


        dialog.addWindowListener (new DlgWindowListener(al, findPanel.acceptButton));
    }
    
    private static class DlgWindowListener extends WindowAdapter {
        private ActionListener al;
        private JButton acceptButton;
        DlgWindowListener (ActionListener al, JButton acceptButton) {
            this.al = al;
            this.acceptButton = acceptButton;
        }
        
        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
            acceptButton.removeActionListener (al);
            ((Dialog) windowEvent.getSource()).removeWindowListener(this);
            ((Dialog) windowEvent.getSource()).dispose();
        }
    }

    String getPattern() {
        FindDialogPanel findPanel = panel.get();
        return findPanel != null ? (String) findPanel.findWhat.getSelectedItem() : null;
    }
    
    private void updateHistory() {
        Object pattern = findWhat.getEditor().getItem();

        history.add( 0, pattern );
        for ( int i = history.size() - 1; i > 0; i-- ) {
            if ( history.get( i ).equals( pattern ) ) {
                history.remove( i );
                break;
            }
        }
    }

    private static Dialog createDialog() {
        final FindDialogPanel findPanel = getPanel();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                               findPanel,
                               NbBundle.getBundle(FindDialogPanel.class).getString("LBL_Find_Title"),
                               true,                                                 // Modal
                               new Object[] { findPanel.acceptButton, DialogDescriptor.CANCEL_OPTION }, // Option lineStartList
                               findPanel.acceptButton,                                         // Default
                               DialogDescriptor.RIGHT_ALIGN,                        // Align
                               null,                                                 // Help
                               new java.awt.event.ActionListener() {
                                   public void actionPerformed( java.awt.event.ActionEvent evt ) {
                                       if ( evt.getSource() == findPanel.acceptButton ) {
                                           getPanel().updateHistory();
                                       }
                                       else

                                       getDialog().setVisible( false );
                                   }
                               });
        Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        dialog.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(FindDialogPanel.class, "ACSN_Find")); //NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(FindDialogPanel.class, "ACSD_Find")); //NOI18N
        return dialog;
    }

    private static Reference<Dialog> dialogRef = null;
    private static Dialog getDialog() {
        Dialog result = null;
        if (dialogRef != null) {
            result = dialogRef.get();
        }
        if (result == null) {
            result = createDialog();
            dialogRef = new WeakReference<Dialog>(result);
        }
        return result;
    }

}
