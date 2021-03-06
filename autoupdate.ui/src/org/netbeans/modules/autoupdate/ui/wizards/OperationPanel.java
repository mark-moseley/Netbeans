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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class OperationPanel extends javax.swing.JPanel {
    
    static final String RUN_ACTION = "run-action";
    static final String RUN_IN_BACKGROUND = "run-in-background";
    
    public OperationPanel (boolean allowRunInBackground) {
        initComponents ();
        rbRestartNow.setSelected (true);
        cbRunInBackground.setVisible (allowRunInBackground);
        if (allowRunInBackground) {
            cbRunInBackground.setSelected (false);
        }
        setRestartButtonsVisible (false);
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        RequestProcessor.getDefault ().post(new Runnable () {
            public void run () {
                firePropertyChange (RUN_ACTION, null, Boolean.TRUE);
            }
        }, 200);
    }
    
    public void waitAndSetProgressComponents (final JLabel mainLabel, final JComponent progressComponent, final JLabel detailLabel) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setProgressComponents (mainLabel, progressComponent, detailLabel);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    setProgressComponents (mainLabel, progressComponent, detailLabel);
                }
            });
        }
    }
    
    public void setRestartButtonsVisible (final boolean visible) {
        if (SwingUtilities.isEventDispatchThread ()) {
            rbRestartLater.setVisible (visible);
            rbRestartNow.setVisible (visible);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    rbRestartLater.setVisible (visible);
                    rbRestartNow.setVisible (visible);
                }
            });
        }
    }
    
    public boolean restartNow () {
        return rbRestartNow.isSelected ();
    }

    public void hideRunInBackground () {
        if (SwingUtilities.isEventDispatchThread ()) {
            cbRunInBackground.setVisible (false);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    cbRunInBackground.setVisible (false);
                }
            });
        }
    }
    
    private void setProgressComponents (JLabel mainLabel, JComponent progressComponent, JLabel detailLabel) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        mainLabel.setPreferredSize (new Dimension (0, 20));
        detailLabel.setPreferredSize (new Dimension (0, 20));
        progressComponent.setPreferredSize (new Dimension (0, 20));
        pProgress.removeAll ();
        pProgress.add (mainLabel, BorderLayout.NORTH);
        pProgress.add (progressComponent, BorderLayout.CENTER);
        pProgress.add (detailLabel, BorderLayout.SOUTH);
        pAboveSpace.setVisible(true);
        revalidate ();
    }
    
    public void setBody (final String msg, final String text) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setBodyInEQ (msg, text);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    setBodyInEQ (msg, text);
                }
            });
        }
    }
    
    public void setBody (final String msg, final Set<UpdateElement> updateElements) {
        final List<UpdateElement> elements = new ArrayList<UpdateElement> (updateElements);
        if (SwingUtilities.isEventDispatchThread ()) {
            setBodyInEQ (msg, elements);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    setBodyInEQ (msg, elements);
                }
            });
        }
    }
    
    private void setBodyInEQ (String msg, List<UpdateElement> elements) {
        pProgress.removeAll ();
        pProgress.add (getTitleComponent (msg), BorderLayout.NORTH);
        pProgress.add (getElementsComponent (elements), BorderLayout.CENTER);
        pAboveSpace.setVisible(false);
        revalidate ();
    }
    
    private void setBodyInEQ (String msg, String elements) {
        pProgress.removeAll ();
        pProgress.add (getTitleComponent (msg), BorderLayout.NORTH);
        pProgress.add (getElementsComponent (elements), BorderLayout.CENTER);
        pAboveSpace.setVisible(false);
        revalidate ();
    }
    
    private JComponent getTitleComponent (String msg) {
        JTextArea area = new JTextArea (msg);
        area.setWrapStyleWord (true);
        area.setLineWrap (true);
        area.setEditable (false);
        area.setOpaque (false);
        return area;
    }
    
    private JComponent getElementsComponent (List<UpdateElement> elements) {
        JTextPane area = new JTextPane ();
        area.setEditable (false);
        area.setContentType ("text/html"); // NOI18N
        String body = new String ();
        for (UpdateElement el : elements) {
            body = body + el.getDisplayName () + "<br>"; // NOI18N
        }
        area.setText (body);
        area.setOpaque (false);
        return area;
    }
    
    private JComponent getElementsComponent (String msg) {
        JTextPane area = new JTextPane ();
        area.setEditable (false);
        area.setContentType ("text/html"); // NOI18N
        area.setText (msg);
        area.setOpaque (false);
        return area;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgRestartButtons = new javax.swing.ButtonGroup();
        pAboveSpace = new javax.swing.JPanel();
        pProgress = new javax.swing.JPanel();
        pbPlaceHolder = new javax.swing.JProgressBar();
        lMainLabel = new javax.swing.JLabel();
        lDetailLabel = new javax.swing.JLabel();
        rbRestartNow = new javax.swing.JRadioButton();
        rbRestartLater = new javax.swing.JRadioButton();
        cbRunInBackground = new javax.swing.JCheckBox();

        pAboveSpace.setOpaque(false);

        org.jdesktop.layout.GroupLayout pAboveSpaceLayout = new org.jdesktop.layout.GroupLayout(pAboveSpace);
        pAboveSpace.setLayout(pAboveSpaceLayout);
        pAboveSpaceLayout.setHorizontalGroup(
            pAboveSpaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 148, Short.MAX_VALUE)
        );
        pAboveSpaceLayout.setVerticalGroup(
            pAboveSpaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 65, Short.MAX_VALUE)
        );

        pProgress.setLayout(new java.awt.BorderLayout());

        pbPlaceHolder.setPreferredSize(new java.awt.Dimension(0, 20));
        pProgress.add(pbPlaceHolder, java.awt.BorderLayout.CENTER);
        pProgress.add(lMainLabel, java.awt.BorderLayout.NORTH);
        pProgress.add(lDetailLabel, java.awt.BorderLayout.SOUTH);

        bgRestartButtons.add(rbRestartNow);
        org.openide.awt.Mnemonics.setLocalizedText(rbRestartNow, org.openide.util.NbBundle.getMessage(OperationPanel.class, "InstallUnitWizardModel_Buttons_RestartNow")); // NOI18N
        rbRestartNow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRestartNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRestartNowActionPerformed(evt);
            }
        });

        bgRestartButtons.add(rbRestartLater);
        org.openide.awt.Mnemonics.setLocalizedText(rbRestartLater, org.openide.util.NbBundle.getMessage(OperationPanel.class, "InstallUnitWizardModel_Buttons_RestartLater")); // NOI18N
        rbRestartLater.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRestartLater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRestartLaterActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRunInBackground, org.openide.util.NbBundle.getMessage(OperationPanel.class, "OperationPanel.cbRunInBackground.text")); // NOI18N
        cbRunInBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRunInBackgroundActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                    .add(pAboveSpace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rbRestartNow)
                    .add(rbRestartLater)
                    .add(cbRunInBackground))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pAboveSpace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbRunInBackground)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbRestartNow)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbRestartLater)
                .addContainerGap(45, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void rbRestartLaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRestartLaterActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_rbRestartLaterActionPerformed

private void rbRestartNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRestartNowActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_rbRestartNowActionPerformed

private void cbRunInBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRunInBackgroundActionPerformed
    firePropertyChange (RUN_IN_BACKGROUND, null, Boolean.TRUE);
}//GEN-LAST:event_cbRunInBackgroundActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgRestartButtons;
    private javax.swing.JCheckBox cbRunInBackground;
    private javax.swing.JLabel lDetailLabel;
    private javax.swing.JLabel lMainLabel;
    private javax.swing.JPanel pAboveSpace;
    private javax.swing.JPanel pProgress;
    private javax.swing.JProgressBar pbPlaceHolder;
    private javax.swing.JRadioButton rbRestartLater;
    private javax.swing.JRadioButton rbRestartNow;
    // End of variables declaration//GEN-END:variables
    
}
