/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * UnusedDetectorPrefs.java
 *
 * Created on Nov 10, 2008, 9:48:35 AM
 */
package org.netbeans.modules.python.editor.hints;

import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Preferences where users can configure the unused detector rules
 * 
 * @author Tor Norbye
 */
public class UnusedDetectorPrefs extends javax.swing.JPanel implements ActionListener {
    private Preferences prefs;

    /** Creates new form UnusedDetectorPrefs */
    public UnusedDetectorPrefs(Preferences prefs) {
        initComponents();
        this.prefs = prefs;
        skipParams.setSelected(UnusedDetector.getSkipParameters(prefs));
        skipReturnTuples.setSelected(UnusedDetector.getSkipReturnTuples(prefs));
        String ignore = UnusedDetector.getIgnoreNames(prefs);
        if (ignore == null) {
            ignore = "";
        }
        ignoredNames.setText(ignore);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        skipParams = new javax.swing.JCheckBox();
        skipReturnTuples = new javax.swing.JCheckBox();
        ignoredNames = new javax.swing.JTextField();
        ignoredLabel = new javax.swing.JLabel();

        skipParams.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.skipParams.text")); // NOI18N
        skipParams.addActionListener(this);

        skipReturnTuples.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.skipReturnTuples.text")); // NOI18N
        skipReturnTuples.addActionListener(this);

        ignoredNames.setColumns(25);
        ignoredNames.addActionListener(this);

        ignoredLabel.setLabelFor(ignoredNames);
        ignoredLabel.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.ignoredLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(skipParams)
            .add(skipReturnTuples)
            .add(layout.createSequentialGroup()
                .add(ignoredLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignoredNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(skipParams)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(skipReturnTuples)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ignoredLabel)
                    .add(ignoredNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == skipParams) {
            UnusedDetectorPrefs.this.changed(evt);
        }
        else if (evt.getSource() == skipReturnTuples) {
            UnusedDetectorPrefs.this.changed(evt);
        }
        else if (evt.getSource() == ignoredNames) {
            UnusedDetectorPrefs.this.changed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changed
        Object source = evt.getSource();
        if (source == ignoredNames) {
            UnusedDetector.setIgnoreNames(prefs, ignoredNames.getText().trim());
        } else if (source == skipParams) {
            UnusedDetector.setSkipParameters(prefs, skipParams.isSelected());
        } else if (source == skipReturnTuples) {
            UnusedDetector.setSkipReturnTuples(prefs, skipReturnTuples.isSelected());
        } else {
            assert false : source;
        }
    }//GEN-LAST:event_changed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ignoredLabel;
    private javax.swing.JTextField ignoredNames;
    private javax.swing.JCheckBox skipParams;
    private javax.swing.JCheckBox skipReturnTuples;
    // End of variables declaration//GEN-END:variables
}
