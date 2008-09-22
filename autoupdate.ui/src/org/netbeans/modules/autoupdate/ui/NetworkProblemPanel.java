/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jiri Rechtacek
 */
public class NetworkProblemPanel extends javax.swing.JPanel {
    private String problem;
    private JButton [] buttons = null;
    private boolean isWarning = false;

    /** Creates new form NetworkProblemPanel */
    public NetworkProblemPanel (String problemDescription) {
        this (problemDescription, false);
    }
    
    public NetworkProblemPanel (String problemDescription, JButton... buttons) {
        this (problemDescription, true, buttons);
    }
    
    private NetworkProblemPanel (String problemDescription, boolean warning, JButton... buttons) {
        this.buttons = buttons;
        this.isWarning = warning;
        problem = problemDescription == null ?
            getBundle("NetworkProblemPanel_taTitle_Text") : // NOI18N
            problemDescription;
        initComponents ();
        taTitle.setToolTipText (problem);
        if (isWarning) {
            if (buttons.length == 2) { // XXX: called from InstallStep
                taMessage.setText (NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taMessage_WarningTextWithReload")); // NOI18N
            } else {
                taMessage.setText(NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taMessage_WarningText")); // NOI18N
            }
        } else {
            taMessage.setText(NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taMessage_ErrorText")); // NOI18N
        }
        for (JButton b : buttons) {
            b.getAccessibleContext ().setAccessibleDescription (b.getText ());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spTitle = new javax.swing.JScrollPane();
        taTitle = new javax.swing.JTextArea (problem);
        spMessage = new javax.swing.JScrollPane();
        taMessage = new javax.swing.JTextArea();

        setMinimumSize(new java.awt.Dimension(250, 200));

        spTitle.setBorder(null);

        taTitle.setEditable(false);
        taTitle.setLineWrap(true);
        taTitle.setWrapStyleWord(true);
        taTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        taTitle.setOpaque(false);
        taTitle.setPreferredSize(new java.awt.Dimension(100, 40));
        spTitle.setViewportView(taTitle);
        taTitle.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taTitle_ACN")); // NOI18N
        taTitle.getAccessibleContext().setAccessibleDescription(problem);

        spMessage.setBorder(null);

        taMessage.setEditable(false);
        taMessage.setLineWrap(true);
        taMessage.setRows(3);
        taMessage.setWrapStyleWord(true);
        taMessage.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        taMessage.setOpaque(false);
        spMessage.setViewportView(taMessage);
        taMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taMessage")); // NOI18N
        taMessage.getAccessibleContext().setAccessibleDescription(problem);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
            .add(spMessage)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(spTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_ACD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane spMessage;
    private javax.swing.JScrollPane spTitle;
    private javax.swing.JTextArea taMessage;
    private javax.swing.JTextArea taTitle;
    // End of variables declaration//GEN-END:variables
    
    public Object showNetworkProblemDialog () {
        DialogDescriptor dd = getDialogDescriptor ();
        DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
        return dd.getValue ();
    }
    
    private DialogDescriptor getDialogDescriptor () {
        Object [] options = null;
        if (buttons == null || buttons.length == 0) {
            options = new Object [] { DialogDescriptor.OK_OPTION };
        } else {
            options = buttons;
        }
        JButton showProxyOptions = new JButton ();
        Mnemonics.setLocalizedText (showProxyOptions, getBundle ("CTL_ShowProxyOptions"));

        DialogDescriptor descriptor = new DialogDescriptor(
             this,
             isWarning ? getBundle ("CTL_Warning") : getBundle ("CTL_Error"),
             true,                                  // Modal
             options, // Option list
             null,                         // Default
             DialogDescriptor.DEFAULT_ALIGN,        // Align
             null, // Help
             null
        );

        showProxyOptions.getAccessibleContext ().setAccessibleDescription (getBundle ("ACSD_ShowProxyOptions"));
        showProxyOptions.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent arg0) {
                OptionsDisplayer.getDefault ().open ("General"); // NOI18N
            }
        });
        
        descriptor.setMessageType (isWarning ? NotifyDescriptor.WARNING_MESSAGE : NotifyDescriptor.ERROR_MESSAGE);
        if (isWarning) {
            descriptor.setAdditionalOptions(new Object [] {showProxyOptions});
        }
        descriptor.setClosingOptions (options);
        return descriptor;
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (NetworkProblemPanel.class, key);
    }
}
