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

package org.netbeans.modules.web.debug;

import java.beans.PropertyChangeListener;
import org.openide.*;
//import org.openide.NotifyDescriptor.Message;
import org.openide.util.NbBundle;

import java.net.URI;
import javax.swing.*;

import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.web.debug.breakpoints.*;

/**
* Customizer of JspLineBreakpoint
*
* @author Martin Grebac
*/
public class JspBreakpointPanel extends JPanel {

    static final long serialVersionUID =-8164649328980808272L;

    private ActionsPanel actionsPanel;
    private JspLineBreakpoint breakpoint;
    private boolean createBreakpoint = false;
    private Controller controller;

    public JspBreakpointPanel() {
        this(JspLineBreakpoint.create(Context.getCurrentURL(), Context.getCurrentLineNumber()));
        createBreakpoint = true;
    }        
    
    /** Creates new form JspBreakpointPanel */
    public JspBreakpointPanel(JspLineBreakpoint b) {

        breakpoint = b;
        controller = new JspBreakpointController();
        initComponents ();
        putClientProperty("HelpID", "jsp_breakpoint");//NOI18N

        // a11y
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JspBreakpointPanel.class, "ACSD_LineBreakpointPanel")); // NOI18N
 
        String url = b.getURL();
        try {
            URI uri = new URI(url);
            cboxJspSourcePath.setText(uri.getPath());
        } catch (Exception e) {
            cboxJspSourcePath.setText(url);
        }

        int lnum = b.getLineNumber();
        if (lnum < 1)  {
            tfLineNumber.setText("");  //NOI18N
        } else {
            tfLineNumber.setText(Integer.toString(lnum));
        }
        
        actionsPanel = new ActionsPanel(b);
        pActions.add(actionsPanel, "Center");
    }

    public Controller getController() {
        return controller;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        lblJspSourcePath = new javax.swing.JLabel();
        cboxJspSourcePath = new javax.swing.JTextField();
        lblLineNumber = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setLayout(new java.awt.GridBagLayout());

        pSettings.setBorder(new javax.swing.border.TitledBorder(NbBundle.getMessage(JspBreakpointPanel.class, "LBL_Settings")));
        lblJspSourcePath.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Source_name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        pSettings.add(lblJspSourcePath, gridBagConstraints);
        lblJspSourcePath.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSN_CTL_Source_name"));

        cboxJspSourcePath.setEditable(false);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        pSettings.add(cboxJspSourcePath, gridBagConstraints);
        cboxJspSourcePath.getAccessibleContext().setAccessibleName(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSN_CTL_Source_name"));
        cboxJspSourcePath.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Source_name"));

        lblLineNumber.setLabelFor(tfLineNumber);
        lblLineNumber.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number"));
        lblLineNumber.setDisplayedMnemonic(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number_mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        pSettings.add(lblLineNumber, gridBagConstraints);
        lblLineNumber.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Line_number"));

        tfLineNumber.setColumns(7);
        tfLineNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusLost(evt);
            }
        });
        tfLineNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfLineNumberKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        pSettings.add(tfLineNumber, gridBagConstraints);
        tfLineNumber.getAccessibleContext().setAccessibleName(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSN_CTL_Line_number"));
        tfLineNumber.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Line_number"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }

    private void tfLineNumberKeyTyped(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
    }

    private void tfLineNumberFocusGained(java.awt.event.FocusEvent evt) {
        if (!evt.isTemporary()) {
            ((JTextField) evt.getComponent()).selectAll();
        }
    }

    private void tfLineNumberFocusLost(java.awt.event.FocusEvent evt) {
//        if (!evt.isTemporary()) {
//            if (tfLineNumber.getText().trim().length() > 0) {
//                try {
//                    int i = Integer.parseInt(tfLineNumber.getText ());
//                    if (i < 1) {
//                        DialogDisplayer.getDefault().notify (
//                            new Message (
//                                NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
//                                NotifyDescriptor.ERROR_MESSAGE
//                            )
//                        );
//                    } else if (event != null) {
//                            event.setLineNumber(i);
//                    }                    
//                } catch (NumberFormatException e) {
//                    DialogDisplayer.getDefault().notify (
//                        new Message (
//                            NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
//                            NotifyDescriptor.ERROR_MESSAGE
//                        )
//                    );
//                }
//            }
//        }
    }
    
    // Variables declaration - do not modify
    private javax.swing.JTextField cboxJspSourcePath;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblJspSourcePath;
    private javax.swing.JLabel lblLineNumber;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfLineNumber;
    // End of variables declaration

    private class JspBreakpointController implements Controller {

        //interface org.netbeans.modules.debugger.Controller
        public boolean ok() {
            if (!isValid()) {
                return false;
            }

            actionsPanel.ok ();
            breakpoint.setLineNumber(Integer.parseInt(tfLineNumber.getText().trim()));

            if (createBreakpoint) {
                DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
            }
            return true;
        }

        //interface org.netbeans.modules.debugger.Controller
        public boolean cancel() {
            return true;
        }

        //interface org.netbeans.modules.debugger.Controller
        public boolean isValid() {
            try {
                int line = Integer.parseInt(tfLineNumber.getText().trim());
                return line > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

    }
}
