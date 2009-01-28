/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * RubyTestingOptionsPanel.java
 *
 * Created on Jan 28, 2009, 3:26:08 PM
 */

package org.netbeans.modules.ruby.rubyproject;

/**
 *
 * @author Erno Mononen
 */
final class RubyTestingOptionsPanel extends javax.swing.JPanel {

    /** Creates new form RubyTestingOptionsPanel */
    public RubyTestingOptionsPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        testingOptionsTitle = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        testUnit = new javax.swing.JCheckBox();
        rspec = new javax.swing.JCheckBox();
        autotest = new javax.swing.JCheckBox();

        testingOptionsTitle.setText(org.openide.util.NbBundle.getMessage(RubyTestingOptionsPanel.class, "RubyTestingOptionsPanel.testingOptionsTitle.text")); // NOI18N

        testUnit.setText(org.openide.util.NbBundle.getMessage(RubyTestingOptionsPanel.class, "RubyTestingOptionsPanel.testUnit.text")); // NOI18N
        testUnit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testUnitActionPerformed(evt);
            }
        });

        rspec.setText(org.openide.util.NbBundle.getMessage(RubyTestingOptionsPanel.class, "RubyTestingOptionsPanel.rspec.text")); // NOI18N

        autotest.setText(org.openide.util.NbBundle.getMessage(RubyTestingOptionsPanel.class, "RubyTestingOptionsPanel.autotest.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(testingOptionsTitle)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                    .add(rspec)
                    .add(testUnit)
                    .add(autotest))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(testingOptionsTitle))
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testUnit)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rspec)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autotest))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testUnitActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_testUnitActionPerformed

    boolean isTestUnitSelected() {
        return testUnit.isSelected();
    }

    boolean isRspecSelected() {
        return rspec.isSelected();
    }

    boolean isAutoTestSelected() {
        return autotest.isSelected();
    }

    void setTestUnit(boolean selected) {
        testUnit.setSelected(selected);
    }

    void setRspec(boolean selected) {
        rspec.setSelected(selected);
    }

    void setAutoTest(boolean selected) {
        autotest.setSelected(selected);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autotest;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox rspec;
    private javax.swing.JCheckBox testUnit;
    private javax.swing.JLabel testingOptionsTitle;
    // End of variables declaration//GEN-END:variables

}
