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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd.WSWebBndDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author  dlm198383
 */
public class WSWebBndAttributesPanel extends /*javax.swing.JPanel*/ SectionInnerPanel{

    //private WSWebBndRootCustomizer masterPanel;
    WSWebBnd webbnd;
    WSMultiViewDataObject dObj;
    /*
    public WSWebBndAttributesPanel() {
        initComponents();
    }
     */
    /** Creates new form WSWebBndAttributesPanel */
    
    public WSWebBndAttributesPanel(SectionView view, WSMultiViewDataObject dObj,  WSWebBnd webbnd) {
        super(view);
        this.dObj=dObj;
        this.webbnd=webbnd;
        initComponents();
        nameField.setText(webbnd.getXmiId());
        addModifier(nameField);
        virtualHostField.setText(webbnd.getVirtualHostName());
        addModifier(virtualHostField);
        getSectionView().getErrorPanel().clearError();
    }
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==nameField) {
            webbnd.setXmiId((String)value);
        } else if (source==virtualHostField) {
            webbnd.setVirtualHostName((String)value);
        }
    }
    public javax.swing.JTextField getNameField() {
        return nameField;
    }
    public javax.swing.JTextField getVirtualHostNameField() {
        return virtualHostField;
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==virtualHostField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "vhn", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameField==source) {
            nameField.setText(webbnd.getXmiId());
        }
    }
    /*
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }*/
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("name".equals(errorId)) return nameField;
        if ("vhn".equals(errorId)) return virtualHostField;
        return null;
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        // TODO add your handling code here:
	dObj.setChangedFromUI(true);
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
    }    
     
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        virtualHostField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        jLabel1.setText("Virtual Host Name:");

        virtualHostField.setText("virtual_host");

        jLabel2.setText("Name:");

        nameField.setText("WebApp_1_Bnd");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .add(virtualHostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(virtualHostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(56, 56, 56))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField virtualHostField;
    // End of variables declaration//GEN-END:variables
    
}
