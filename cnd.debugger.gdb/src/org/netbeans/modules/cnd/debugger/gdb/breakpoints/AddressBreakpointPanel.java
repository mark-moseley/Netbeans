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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import javax.swing.JPanel;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Panel for customizing function breakpoints. 
 * This panel is a part of "New Breakpoint" dialog.
 *
 * @author Nik Molchanov (copied and modified from JDPA debugger).
 */

// Implement HelpCtx.Provider interface to provide help ids for help system
// public class FunctionBreakpointPanel extends JPanel implements Controller {
//
public class AddressBreakpointPanel extends JPanel implements Controller {
    
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private AddressBreakpoint           breakpoint;
    private boolean                     createBreakpoint = false;
    
    
    private static AddressBreakpoint createBreakpoint() {
        AddressBreakpoint ab = AddressBreakpoint.create (
            // EditorContextBridge.getCurrentFunction ()
            "0x000000" // DEBUG // NOI18N
        );
        ab.setPrintText(NbBundle.getBundle (AddressBreakpointPanel.class).getString
                ("CTL_Address_Breakpoint_Print_Text")); // NOI18N
        
        return ab;
    }
    
    
    /** 
     * Creates new form FunctionBreakpointPanel
     */
    public AddressBreakpointPanel() {
        this(createBreakpoint());
        createBreakpoint = true;
    }
    
    /** 
     * Creates new form FunctionBreakpointPanel
     */
    public AddressBreakpointPanel(AddressBreakpoint b) {
        breakpoint = b;
        initComponents();

        tfAddress.setText(b.getAddress());
        
        conditionsPanel = new ConditionsPanel(b);
        pConditions.add(conditionsPanel);
        actionsPanel = new ActionsPanel(b);
        pActions.add(actionsPanel, "Center"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        lAddress = new javax.swing.JLabel();
        tfAddress = new javax.swing.JTextField();
        pConditions = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Function_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setMinimumSize(new java.awt.Dimension(249, 80));
        pSettings.setLayout(new java.awt.GridBagLayout());

        lAddress.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_AddressBreakpoint").charAt(0));
        lAddress.setLabelFor(tfAddress);
        lAddress.setText(bundle.getString("L_Address_Breakpoint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(lAddress, gridBagConstraints);
        lAddress.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Function_Breakpoint_Function_Name")); // NOI18N

        tfAddress.setToolTipText(bundle.getString("TTT_TF_Function_Breakpoint_Function_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfAddress, gridBagConstraints);
        tfAddress.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_TF_Function_Breakpoint_Function_Name")); // NOI18N
        tfAddress.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Function_Breakpoint_Function_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        pConditions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pConditions, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok() {
        String msg = valiadateMsg();
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        conditionsPanel.ok();
        actionsPanel.ok();
        String address = tfAddress.getText().trim();
        breakpoint.setAddress(address);
        
        // Check if this breakpoint already set
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Breakpoint[] bs = dm.getBreakpoints();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if (bs[i] instanceof AddressBreakpoint) {
                AddressBreakpoint ab = (AddressBreakpoint) bs[i];
                if (address.equals(ab.getAddress())) {
                    // Compare conditions
                    String condition = breakpoint.getCondition();
                    if (condition != null) {
                        if (!condition.equals(ab.getCondition())) {
                            continue;
                        }
                    } else {
                        if (ab.getCondition() != null) {
                            continue;
                        }
                    }
                    // Check if this breakpoint is enabled
                    if (!ab.isEnabled())
                        bs[i].enable();
                    return true;
                }
            }
        }
        // Create a new breakpoint
        if (createBreakpoint) {
            dm.addBreakpoint(breakpoint);
        }
        return true;
    }
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel() {
        return true;
    }
    
    /**
     * Return <code>true</code> whether value of this customizer 
     * is valid (and OK button can be enabled).
     *
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    private String valiadateMsg() {
        String function = tfAddress.getText().trim();
        // Empty string is not a valid function name
        if (function.length() == 0) {
            return NbBundle.getBundle(AddressBreakpointPanel.class).getString 
                ("MSG_No_Address_Name_Spec"); // NOI18N
        }
        return null;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lAddress;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pConditions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfAddress;
    // End of variables declaration//GEN-END:variables
    
}
