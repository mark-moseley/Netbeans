/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
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
 * "Portions Copyrighted 2006 Ricoh Corporation"
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

package org.netbeans.modules.j2me.cdc.project.ricoh;

import java.awt.Color;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import ricoh.util.dom.DalpDOMHandler;

/**
 *
 * @author  suchys
 */
public class RicohApplicationUIDCustomizer extends javax.swing.JPanel {
    
    boolean uidValid = true;
    private String uidString;
    private File sdkInstallation;
        
    public class UIDVerifier extends InputVerifier
    {
        public boolean verify(JComponent input)
        {
            if (input instanceof JTextField)
                return DalpDOMHandler.isValidUid(((JTextField)input).getText());
            else
                return true;
        }
    }
    
    /** Creates new form ApplicationUIDCustomizer */
    public RicohApplicationUIDCustomizer(String uidString, File sdkInstallation) {
        this.sdkInstallation = sdkInstallation;
        this.uidString = uidString;
        
        initComponents();
        this.idTextField.setInputVerifier(new UIDVerifier());
                                                
        Color nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0); 
        }        
        errorLabel.setForeground(nbErrorForeground);
        
        idTextField.setText(uidString);
        
        this.idTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                computeUID();
            }
            public void insertUpdate(DocumentEvent e) {
                computeUID();
            }
            public void removeUpdate(DocumentEvent e) {
                computeUID();
            }
        });
        computeUID();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RicohApplicationUIDCustomizer.class, "ACSD_UIDCustomizerPanel"));
        jLabel1.setLabelFor(idTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/ricoh/Bundle").getString("LBL_ApplicationUID"));

        idTextField.setColumns(7);
        idTextField.setInputVerifier(idTextField.getInputVerifier());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorLabel)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(17, 17, 17)
                        .add(idTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 29, Short.MAX_VALUE)
                .add(errorLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
    private void computeUID(){
        String text = idTextField.getText();
        int length = text.length();
        if (length == 0){
            errorLabel.setText(NbBundle.getMessage(RicohApplicationUIDCustomizer.class, "ERR_CustomizerMissingUID")); //NOI18N
            uidValid = false;
        } else {
            uidValid = true;
            for (int i = 0; i < length; i++){
                if (!Character.isDigit(text.charAt(i))){
                    uidValid = false;
                    errorLabel.setText(NbBundle.getMessage(RicohApplicationUIDCustomizer.class, "ERR_CustomizerWrongUID")); //NOI18N
                    break;
                }
            }
        } 
        if (uidValid){
            String tmpUid = idTextField.getText().toUpperCase();
        
            boolean duplicity = false;
            if (sdkInstallation != null){
                File f = new File(sdkInstallation, "/mnt/sd3/sdk/dsdk/dist/".replace('/', File.pathSeparatorChar));
                if ( f.exists() && !tmpUid.equals(uidString)){
                    uidValid = false;
                    errorLabel.setText(NbBundle.getMessage(RicohApplicationUIDCustomizer.class, "ERR_CustomiserAlreadyUsedUID", tmpUid)); //NOI18N
                }
            }
        }
        if (!uidValid) {
            firePropertyChange(NotifyDescriptor.PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        } else {
            errorLabel.setText(" "); //NOI18N
            firePropertyChange(NotifyDescriptor.PROP_VALID, Boolean.FALSE, Boolean.TRUE);
        }
    }
    
    String getUID(){
        return idTextField.getText();
    }
}
