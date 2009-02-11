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

/*
 * CommonMessagePanel.java
 *
 * Created on June 9, 2006, 4:59 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view.common;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author  skini
 */
public class CommonMessagePanel extends javax.swing.JPanel {
    private static Color nbErrorForeground;
    private static Color nbWarningForeground;
    private boolean mValidState = true;

    static {//Got this from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (nbWarningForeground == null) {
            nbWarningForeground = new Color(51, 51, 51); // Label.foreground
        }
        
    }
    /** Creates new form CommonMessagePanel */
    public CommonMessagePanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();

        jScrollPane2.setBorder(null);

        jTextArea2.setEditable(false);
        jTextArea2.setFont(getFont());
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(2);
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setFocusable(false);
        jTextArea2.setOpaque(false);
        jScrollPane2.setViewportView(jTextArea2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
   
    public void setErrorMessage(String errorMsg) {
        jTextArea2.setText(errorMsg);
        jTextArea2.setToolTipText(errorMsg);
        jTextArea2.setForeground (nbErrorForeground);
        jTextArea2.repaint();
        jLabel1.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/xml/wsdl/ui/view/common/resources/error.gif", false));
        jLabel1.setToolTipText(errorMsg);
        mValidState = false;
    }
    
    public void setWarningMessage(String warningMsg) {
        jTextArea2.setText(warningMsg);
        jTextArea2.setToolTipText(warningMsg);
        jLabel1.setToolTipText(warningMsg);
        jTextArea2.setForeground (nbWarningForeground);
        jTextArea2.repaint();
        Image image = ImageUtilities.loadImage ("org/netbeans/modules/xml/wsdl/ui/view/common/resources/warning.gif");
        jLabel1.setIcon(new ImageIcon (image));
        mValidState = true;
    }
    
    public void setMessage(String msg) {
        jTextArea2.setText(msg);
        jTextArea2.setToolTipText(msg);
        jLabel1.setIcon(null);
        jLabel1.setToolTipText(msg);
        mValidState = true;
    }
    
    public boolean isStateValid() {
    	return mValidState;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration//GEN-END:variables
    
}
