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

package org.netbeans.upgrade;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;


/**
 * @author Jiri Rechtacek
 */
final class AutoUpgradePanel extends JPanel {

    String source;

    /** Creates new form UpgradePanel */
    public AutoUpgradePanel (String directory) {
        this.source = directory;
        initComponents();
        initAccessibility();
    }

    /** Remove a listener to changes of the panel's validity.
     * @param l the listener to remove
     */
    void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    /** Add a listener to changes of the panel's validity.
     * @param l the listener to add
     * @see #isValid
     */
    void addChangeListener(ChangeListener l) {
        if (!changeListeners.contains(l)) {
            changeListeners.add(l);
        }
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Confirmation")); // NOI18N
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtVersions = new javax.swing.JTextArea();

        setName(bundle.getString("LBL_UpgradePanel_Name")); // NOI18N
        setLayout(new java.awt.BorderLayout());

        txtVersions.setBackground(getBackground());
        txtVersions.setColumns(50);
        txtVersions.setEditable(false);
        txtVersions.setFont(new java.awt.Font("Dialog", 0, 12));
        txtVersions.setLineWrap(true);
        txtVersions.setRows(3);
        txtVersions.setText(NbBundle.getMessage (AutoUpgradePanel.class, "MSG_Confirmation", source)); // NOI18N
        txtVersions.setWrapStyleWord(true);
        txtVersions.setFocusable(false);
        txtVersions.setMinimumSize(new java.awt.Dimension(100, 50));
        add(txtVersions, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea txtVersions;
    // End of variables declaration//GEN-END:variables

    private static final ResourceBundle bundle = NbBundle.getBundle(AutoUpgradePanel.class);
    private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>(1);
    
}
