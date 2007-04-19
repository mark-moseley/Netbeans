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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class NetworkProblemPanel extends javax.swing.JPanel {
    private static Runnable doAgain = null;
    private static JButton continueButton = null;
    
    private OperationException problem;

    /** Creates new form NetworkProblemPanel */
    public NetworkProblemPanel (OperationException ex) {
        this.problem = ex;
        initComponents ();
        postInitComponents ();
    }
    
    private void postInitComponents () {
        // XXX: Hack to set as same background as JLabel.background
        Color c = UIManager.getColor ("Label.background");
        if (c == null) c = new JLabel ().getBackground ();
        c = new Color (c.getRGB ());
        taMessage.setBackground (c);
        taTitle.setBackground (c);
        if (problem != null && problem.getMessage () != null) {
            taTitle.setText (NbBundle.getMessage (NetworkProblemPanel.class, "NetworkProblemPanel_taTitleWithUrl_Text", problem.getMessage ()));
            taTitle.setToolTipText (NbBundle.getMessage (NetworkProblemPanel.class, "NetworkProblemPanel_taTitleWithUrl_Text", problem.getMessage ()));
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        spTitle = new javax.swing.JScrollPane();
        taTitle = new javax.swing.JTextArea();
        spMessage = new javax.swing.JScrollPane();
        taMessage = new javax.swing.JTextArea();

        spTitle.setBorder(null);

        taTitle.setEditable(false);
        taTitle.setLineWrap(true);
        taTitle.setRows(2);
        taTitle.setText(org.openide.util.NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taTitle_Text")); // NOI18N
        taTitle.setWrapStyleWord(true);
        taTitle.setBorder(null);
        spTitle.setViewportView(taTitle);

        spMessage.setBorder(null);

        taMessage.setEditable(false);
        taMessage.setLineWrap(true);
        taMessage.setRows(3);
        taMessage.setText(org.openide.util.NbBundle.getMessage(NetworkProblemPanel.class, "NetworkProblemPanel_taMessage_Text")); // NOI18N
        taMessage.setWrapStyleWord(true);
        taMessage.setBorder(null);
        spMessage.setViewportView(taMessage);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
            .add(spTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(spTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spMessage)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane spMessage;
    private javax.swing.JScrollPane spTitle;
    private javax.swing.JTextArea taMessage;
    private javax.swing.JTextArea taTitle;
    // End of variables declaration//GEN-END:variables
    
    public static void showNetworkProblemDialog () {
        showNetworkProblemDialog (null, null);
    }
    
    public static void showNetworkProblemDialog (JButton cancel, OperationException ex) {
        DialogDescriptor dd = getDialogDescriptor (cancel, ex);
        DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
        if (! continueButton.equals (dd.getValue ())) {
            if (cancel != null) cancel.doClick ();
        }
    }
    
    public static void setPerformAgain (Runnable performAgain) {
        doAgain = performAgain;
    }
    
    private static Runnable getPerformAgain () {
        return doAgain;
    }
    
    private static DialogDescriptor getDialogDescriptor (JButton cancel, OperationException ex) {
        continueButton = new JButton ();
        continueButton.setEnabled (true);
        continueButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                RequestProcessor.getDefault().post (getPerformAgain (), 100);
            }
        });
        Mnemonics.setLocalizedText (continueButton, getBundle ("CTL_Error_Continue"));
        continueButton.getAccessibleContext().setAccessibleDescription(getBundle ("ACSD_Error_Continue"));

        final JButton cancelButton = cancel == null ? new JButton(getBundle ("CTL_Error_Cancel")) : cancel;
        cancelButton.getAccessibleContext().setAccessibleDescription(getBundle ("ACSD_Error_Cancel"));

        JButton showProxyOptions = new JButton ();
        Mnemonics.setLocalizedText (showProxyOptions, getBundle ("CTL_ShowProxyOptions"));

        DialogDescriptor descriptor = new DialogDescriptor(
             new NetworkProblemPanel (ex),
             getBundle ("CTL_Error"),
             true,                                  // Modal
             new Object [] {continueButton, cancelButton}, // Option list
             continueButton,                         // Default
             DialogDescriptor.DEFAULT_ALIGN,        // Align
             null, // Help
             null
        );

        showProxyOptions.getAccessibleContext ().setAccessibleDescription (getBundle ("ACSD_ShowProxyOptions"));
        showProxyOptions.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent arg0) {
                OptionsDisplayer.getDefault ().open ("General");
            }
        });
        
        descriptor.setMessageType (NotifyDescriptor.ERROR_MESSAGE);
        descriptor.setAdditionalOptions (new Object [] {showProxyOptions});
        descriptor.setClosingOptions (new Object [] {continueButton, cancelButton});
        return descriptor;
    }
    
    private static String getBundle (String key) {
        return NbBundle.getMessage (NetworkProblemPanel.class, key);
    }
}
