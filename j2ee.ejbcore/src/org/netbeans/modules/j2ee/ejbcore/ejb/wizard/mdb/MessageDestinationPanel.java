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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 * Panel for adding message destination.
 * @author Tomas Mysik
 */
public class MessageDestinationPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = MessageDestinationPanel.class.getName() + ".IS_VALID";
    
    // map because of faster searching
    private final Map<String, MessageDestination.Type> destinationMap;
    private NotificationLineSupport statusLine;
    
    // private because correct initialization is needed
    private MessageDestinationPanel(Map<String, MessageDestination.Type> destinationMap) {
        initComponents();
        this.destinationMap = destinationMap;
    }
    
    /**
     * Factory method for creating new instance.
     * @param destinationMap the names and the types of project message destinations.
     * @return MessageDestinationPanel instance.
     */
    public static MessageDestinationPanel newInstance(final Map<String, MessageDestination.Type> destinationMap) {
        MessageDestinationPanel mdp = new MessageDestinationPanel(destinationMap);
        mdp.initialize();
        return mdp;
    }

    public void setNotificationLine(NotificationLineSupport statusLine) {
        this.statusLine = statusLine;
    }

    /**
     * Get the name of the message destination.
     * @return message destination name.
     */
    public String getDestinationName() {
        return destinationNameText.getText().trim();
    }
    
    /**
     * Get the type of the message destination.
     * @return message destination type.
     * @see MessageDestination.Type
     */
    public MessageDestination.Type getDestinationType() {
        if (queueTypeRadio.isSelected()) {
            return MessageDestination.Type.QUEUE;
        }
        return MessageDestination.Type.TOPIC;
    }
    
    private void initialize() {
        registerListeners();
        verifyAndFire();
    }
    
    private void registerListeners() {
        // text field
        destinationNameText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                verifyAndFire();
            }
            public void removeUpdate(DocumentEvent event) {
                verifyAndFire();
            }
            public void changedUpdate(DocumentEvent event) {
                verifyAndFire();
            }
        });
        
        // radio buttons
        queueTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });
        topicTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });

        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorMoved(AncestorEvent event) {
                verifyAndFire();
            }
        });
    }
    
    private void setError(String key) {
        if (statusLine != null) {
            statusLine.setErrorMessage(NbBundle.getMessage(MessageDestinationPanel.class, key));
        }
    }
    
    private void setInfo(String key) {
        if (statusLine != null) {
            statusLine.setInformationMessage(NbBundle.getMessage(MessageDestinationPanel.class, key));
        }
    }

    private void verifyAndFire() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // destination name - form & duplicity
        String destinationName = destinationNameText.getText();
        if (destinationName == null || destinationName.trim().length() == 0) {
            setInfo("ERR_NoDestinationName"); // NOI18N
            return false;
        } else {
            destinationName = destinationName.trim();
            MessageDestination.Type type = destinationMap.get(destinationName);
            if (type != null && type.equals(getDestinationType())) {
                setError("ERR_DuplicateDestination"); // NOI18N
                return false;
            }
        }
        
        // destination type (radio)
        if (destinationTypeGroup.getSelection() == null) {
            setInfo("ERR_NoDestinationType"); // NOI18N
            return false;
        }
        
        // no errors
        statusLine.clearMessages();
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationTypeGroup = new javax.swing.ButtonGroup();
        destinationNameLabel = new javax.swing.JLabel();
        destinationNameText = new javax.swing.JTextField();
        destinationTypeLabel = new javax.swing.JLabel();
        queueTypeRadio = new javax.swing.JRadioButton();
        topicTypeRadio = new javax.swing.JRadioButton();

        destinationNameLabel.setLabelFor(destinationNameText);
        org.openide.awt.Mnemonics.setLocalizedText(destinationNameLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationName")); // NOI18N

        destinationTypeLabel.setLabelFor(queueTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(destinationTypeLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationType")); // NOI18N

        destinationTypeGroup.add(queueTypeRadio);
        queueTypeRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(queueTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Queue")); // NOI18N
        queueTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationTypeGroup.add(topicTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(topicTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Topic")); // NOI18N
        topicTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        topicTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(destinationTypeLabel)
                    .add(destinationNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(topicTypeRadio)
                    .add(queueTypeRadio)
                    .add(destinationNameText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationNameLabel)
                    .add(destinationNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationTypeLabel)
                    .add(queueTypeRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(topicTypeRadio)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        destinationNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.destinationNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        queueTypeRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.queueTypeRadio.AccessibleContext.accessibleDescription")); // NOI18N
        topicTypeRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.topicTypeRadio.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "ACSD_AddMessageDestination")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "ACSD_AddMessageDestination")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.JTextField destinationNameText;
    private javax.swing.ButtonGroup destinationTypeGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private javax.swing.JRadioButton queueTypeRadio;
    private javax.swing.JRadioButton topicTypeRadio;
    // End of variables declaration//GEN-END:variables
    
}
