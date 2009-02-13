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

package org.netbeans.modules.server.ui.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 * @author Petr Hejl
 */
public class ServerWizardVisual extends javax.swing.JPanel {

    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private final Map<ServerWizardProvider, String> displayNames = new HashMap<ServerWizardProvider, String>();

    private AddServerInstanceWizard wizard;

    private boolean updatingDisplayName = false;

    public ServerWizardVisual() {
        initComponents();

        Queue<WizardAdapter> selected = new PriorityQueue<WizardAdapter>(5, new WizardPriority());
        for (int i = 0; i < serverListBox.getModel().getSize(); i++) {
            selected.add((WizardAdapter) serverListBox.getModel().getElementAt(i));
        }

        if (!selected.isEmpty()) {
            WizardAdapter selectedItem = selected.peek();
            serverListBox.setSelectedValue(selectedItem, true);
            fillDisplayName(selectedItem.getServerInstanceWizard());
        }

        displayNameEditField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }

            public void removeUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }

            public void changedUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void read(AddServerInstanceWizard wizard) {
        if (this.wizard == null) {
            this.wizard = wizard;
        }

        Object prop = wizard.getProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME);
        if (prop != null) {
            displayNameEditField.setText((String) prop);
        }
    }

    public void store(AddServerInstanceWizard wizard) {
        wizard.putProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME, displayNameEditField.getText());
        Object selectedItem = serverListBox.getSelectedValue();
        if (selectedItem != null) {
            wizard.putProperty(AddServerInstanceWizard.PROP_SERVER_INSTANCE_WIZARD,
                    ((WizardAdapter) selectedItem).getServerInstanceWizard());
        }
    }

    boolean hasValidData() {
        boolean result = isServerValid() && isDisplayNameValid();
        if (result) {
            wizard.setErrorMessage(null);
        }

        return result;
    }

    private boolean isServerValid() {
        boolean result = serverListBox.getSelectedValue() != null;
        if (!result) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_ChooseServer"));
        }
        return result;
    }

    private boolean isDisplayNameValid() {
        String trimmed = displayNameEditField.getText().trim();

        if (trimmed.length() <= 0) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_DisplayName"));
            return false;
        }

        if (existsDisplayName(trimmed)) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_DisplayNameExists"));
            return false;
        }

        return true;
    }

    private boolean existsDisplayName(String displayName) {
        for (ServerInstanceProvider type : ServerRegistry.getInstance().getProviders()) {
            for (ServerInstance instance : type.getInstances()) {
                String instanceName = instance.getDisplayName();
                if (null != instanceName && instanceName.equalsIgnoreCase(displayName)) {
                    return true;
                } else if (null == instanceName) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                            "corrupted ServerInstance: " + instance.toString());
                }
            }
        }
        return false;
    }

    private void displayNameEditFieldUpdate() {
        if (!updatingDisplayName) {
            fireChange();
        }
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    private String generateDisplayName(ServerWizardProvider server) {
        String name;
        int count = 0;

        do {
            name = server.getDisplayName();
            if (count != 0) {
                name += " (" + String.valueOf(count) + ")"; // NOI18N
            }

            count++;
        } while (existsDisplayName(name));

        return name;
    }

    private void fillDisplayName(ServerWizardProvider server) {
        String name = (String) displayNames.get(server);
        if (name == null) {
            name = generateDisplayName(server);
        }
        updatingDisplayName = true; // disable firing from setText
        displayNameEditField.setText(name);
        updatingDisplayName = false;
        fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        displayNameEditField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        serverListBox = new javax.swing.JList();

        setName(org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_Name")); // NOI18N

        jLabel1.setLabelFor(serverListBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_Server")); // NOI18N

        jLabel2.setLabelFor(displayNameEditField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_DisplayName")); // NOI18N

        displayNameEditField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayNameEditFieldKeyReleased(evt);
            }
        });

        serverListBox.setModel(new WizardListModel());
        serverListBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serverListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                serverListBoxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(serverListBox);
        serverListBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME_Server")); // NOI18N
        serverListBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC_Server")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayNameEditField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(displayNameEditField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        displayNameEditField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME_DisplayName")); // NOI18N
        displayNameEditField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC_DisplayName")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void serverListBoxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_serverListBoxValueChanged
       if (!evt.getValueIsAdjusting()) {
           WizardAdapter adapter = (WizardAdapter) serverListBox.getSelectedValue();
           if (adapter != null) {
               ServerWizardProvider server = adapter.getServerInstanceWizard();
               if (server != null) {
                   fillDisplayName(server);
               }
           } else {
               fireChange();
           }
       }
}//GEN-LAST:event_serverListBoxValueChanged

    private void displayNameEditFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_displayNameEditFieldKeyReleased
        WizardAdapter wizardAdapter = (WizardAdapter) serverListBox.getSelectedValue();
        if (wizardAdapter != null) {
            displayNames.put(wizardAdapter.getServerInstanceWizard(), displayNameEditField.getText());
        }
    }//GEN-LAST:event_displayNameEditFieldKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameEditField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList serverListBox;
    // End of variables declaration//GEN-END:variables

    private static class WizardListModel implements ListModel {

        private final List<WizardAdapter> serverWizards = new ArrayList<WizardAdapter>();

        public WizardListModel() {
            for (ServerWizardProvider wizard
                    : Lookups.forPath(ServerRegistry.SERVERS_PATH).lookupAll(ServerWizardProvider.class)) {

                // safety precaution shouldn't ever happen - used because of bridging
                if (wizard.getInstantiatingIterator() != null) {
                    serverWizards.add(new WizardAdapter(wizard));
                }
            }
            Collections.sort(serverWizards);
        }

        public Object getElementAt(int index) {
            return serverWizards.get(index);
        }

        public int getSize() {
            return serverWizards.size();
        }

        public void addListDataListener(ListDataListener l) {
            // not changeable model
        }

        public void removeListDataListener(ListDataListener l) {
            // not changeable model
        }
    }

    private static class WizardAdapter implements Comparable<WizardAdapter> {

        private final ServerWizardProvider serverInstanceWizard;

        public WizardAdapter(ServerWizardProvider serverInstanceWizard) {
            this.serverInstanceWizard = serverInstanceWizard;
        }

        public ServerWizardProvider getServerInstanceWizard() {
            return serverInstanceWizard;
        }

        public int compareTo(WizardAdapter o) {
            return serverInstanceWizard.getDisplayName().compareTo(
                    o.getServerInstanceWizard().getDisplayName());
        }

        @Override
        public String toString() {
            return serverInstanceWizard.getDisplayName();
        }
    }

    private static class WizardPriority implements Comparator<WizardAdapter>, Serializable {

        private static final List<Pattern> PRIORITY_LIST = new ArrayList<Pattern>(4);

        static {
            PRIORITY_LIST.add(Pattern.compile(".*Sailfin.*")); // NOI18N
            PRIORITY_LIST.add(Pattern.compile(".*Sun\\s*Java\\s*System.*")); // NOI18N
            PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*V1.*")); // NOI18N
            PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*V2.*")); // NOI18N
        }

        public int compare(WizardAdapter o1, WizardAdapter o2) {
            Integer priority1 = computePriority(o1.getServerInstanceWizard().getDisplayName());
            Integer priority2 = computePriority(o2.getServerInstanceWizard().getDisplayName());

            return -priority1.compareTo(priority2);
        }

        private int computePriority(String name) {
            int priority = 0;
            for (int i = 0; i < PRIORITY_LIST.size(); i++) {
                if (PRIORITY_LIST.get(i).matcher(name).matches()) {
                    priority = i;
                }
            }
            return priority;
        }

    }
}
