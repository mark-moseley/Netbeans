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

package org.netbeans.modules.websvc.metro.samples;

import java.util.Collections;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Profile;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class PanelOptionsVisual extends javax.swing.JPanel {

    private PanelConfigureProject panel;

    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual(PanelConfigureProject panel) {
        this.panel = panel;
        initComponents();
        initServers();        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverInstanceLabel = new javax.swing.JLabel();
        serverInstanceComboBox = new javax.swing.JComboBox();

        serverInstanceLabel.setText(NbBundle.getMessage(PanelOptionsVisual.class, "LBL_NWP1_Server")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(serverInstanceLabel)
                .add(18, 18, 18)
                .add(serverInstanceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 221, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverInstanceLabel)
                    .add(serverInstanceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    public void initServers() {
        serverInstanceComboBox.removeAllItems();
        ServerWrapper bestServer = null;
        String[] sIDs = Deployment.getDefault().getServerInstanceIDs(
                            Collections.singleton(J2eeModule.Type.WAR),
                            Profile.JAVA_EE_5,
                            new String[] {J2eePlatform.TOOL_JSR109});
        for (String serverInstanceID : sIDs) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID);
            if (displayName != null) {
                ServerWrapper wrapper = new ServerWrapper(serverInstanceID, displayName);
                serverInstanceComboBox.addItem(wrapper);
                // decide whether this server should be preselected
                if (bestServer != null) {
                    String shortName = Deployment.getDefault().getServerID(serverInstanceID);
                    if ("J2EE".equals(shortName)) { // NOI18N
                        bestServer = wrapper;
                    }
                }
            }
        }
        
        if (bestServer != null) {
            serverInstanceComboBox.setSelectedItem(bestServer);
        }
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (serverInstanceComboBox.getItemCount() == 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PanelOptionsVisual.class,"MSG_NoJsr109Server")); //NOI18N
            return false;
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
            return true;
        }
    }

    void store(WizardDescriptor d) {
        ServerWrapper sw = (ServerWrapper) serverInstanceComboBox.getSelectedItem();
        if (sw != null) {
            d.putProperty(WizardProperties.SERVER, sw.getServerID());
        }
    }
    
    void read(WizardDescriptor d) {
    }
    
    private class ServerWrapper {
        private String serverID;
        private String displayName;
        
        ServerWrapper(String sID, String displayName) {
            this.serverID = sID;
            this.displayName = displayName;
        }

        public String getServerID() {
            return serverID;
        }

        public void setServerID(String serverID) {
            this.serverID = serverID;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return this.displayName;
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox serverInstanceComboBox;
    private javax.swing.JLabel serverInstanceLabel;
    // End of variables declaration//GEN-END:variables

}

