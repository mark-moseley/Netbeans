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

package org.netbeans.modules.websvc.jaxrpc.client.ui;

import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.core.WsdlRetriever;

/**
 *
 * @author Peter Williams
 */
public class DownloadWsdlPanel extends javax.swing.JPanel implements WsdlRetriever.MessageReceiver {

    private DialogDescriptor descriptor;
    private WsdlRetriever retriever;
    private String newWsdlUrl;
    private String downloadMsg;
    private boolean downloadOk;

    public DownloadWsdlPanel(String newWsdlUrl) {
        this.newWsdlUrl = newWsdlUrl;
        this.retriever = null;
        this.downloadMsg = " "; // NOI18N
        this.downloadOk = false;
        
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLblStatusLabel = new javax.swing.JLabel();
        jTxtStatus = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLblStatusLabel.setText(NbBundle.getMessage(DownloadWsdlPanel.class, "LBL_Status"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 4);
        add(jLblStatusLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 11, 11);
        add(jTxtStatus, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblStatusLabel;
    private javax.swing.JLabel jTxtStatus;
    // End of variables declaration//GEN-END:variables
 
    public void addNotify() {
        super.addNotify();
        
        retriever = new WsdlRetriever(this, newWsdlUrl);
        new Thread(retriever).start();
    }
    
    public void setDescriptor(DialogDescriptor descriptor) {
		this.descriptor = descriptor;
        descriptor.setValid(downloadOk);
	}

    public byte [] getWsdl() {
        byte [] result = null;
        
        if(retriever.getState() == WsdlRetriever.STATUS_COMPLETE) {
            result = retriever.getWsdl();
        }
        
        return result;
    }
    
    public void setWsdlDownloadMessage(String m) {
        downloadMsg = m;
        jTxtStatus.setText(downloadMsg);
        
        if(retriever.getState() == WsdlRetriever.STATUS_COMPLETE) {
            downloadOk = true;

            // !PW FIXME Find a way to press <OK> button from here.
        }
        
        descriptor.setValid(downloadOk);
    }

    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension result = super.getPreferredSize();
        if(result.width < 240) {
            result.width = 240;
        }
        return result;
    }
}
