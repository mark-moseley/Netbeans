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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.database.RailsAdapterFactory;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConfiguration;
import org.netbeans.modules.ruby.railsprojects.database.RailsJdbcAsAdapterConnection;
import org.netbeans.modules.ruby.railsprojects.database.RailsJdbcConnection;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * A panel for JDBC connections.
 *
 * @author  Erno Mononen
 */
public class JdbcConnectionsPanel extends SettingsPanel {

    /** Creates new form JdbcConnectionsPanel */
    public JdbcConnectionsPanel() {
        initComponents();
        ConnectionManager connectionManager = ConnectionManager.getDefault();
        DatabaseExplorerUIs.connect(developmentComboBox, connectionManager);
        DatabaseExplorerUIs.connect(productionComboBox, connectionManager);
        DatabaseExplorerUIs.connect(testComboBox, connectionManager);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        developmentLabel = new javax.swing.JLabel();
        productionLabel = new javax.swing.JLabel();
        testLabel = new javax.swing.JLabel();
        developmentComboBox = new javax.swing.JComboBox();
        productionComboBox = new javax.swing.JComboBox();
        testComboBox = new javax.swing.JComboBox();

        developmentLabel.setLabelFor(developmentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(developmentLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_DevelopmentConnection")); // NOI18N

        productionLabel.setLabelFor(productionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(productionLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_ProductionConnection")); // NOI18N

        testLabel.setLabelFor(testComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_TestConnection")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(testLabel)
                    .add(productionLabel)
                    .add(developmentLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(productionComboBox, 0, 384, Short.MAX_VALUE)
                    .add(testComboBox, 0, 384, Short.MAX_VALUE)
                    .add(developmentComboBox, 0, 384, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(developmentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(developmentLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(testLabel)
                    .add(testComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(productionLabel)
                    .add(productionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        developmentLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_DevelopmentConnection")); // NOI18N
        productionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_ProductionConnection")); // NOI18N
        testLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_TestConnection")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCN_JdbcPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCD_JdbcPanel")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox developmentComboBox;
    private javax.swing.JLabel developmentLabel;
    private javax.swing.JComboBox productionComboBox;
    private javax.swing.JLabel productionLabel;
    private javax.swing.JComboBox testComboBox;
    private javax.swing.JLabel testLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    void store(WizardDescriptor settings) {
        DatabaseConnection devel = (DatabaseConnection) developmentComboBox.getSelectedItem();
        DatabaseConnection production = (DatabaseConnection) productionComboBox.getSelectedItem();
        DatabaseConnection test = (DatabaseConnection) testComboBox.getSelectedItem();
        
        RailsDatabaseConfiguration databaseConfiguration = null;
        Boolean jdbc = (Boolean) settings.getProperty(NewRailsProjectWizardIterator.JDBC_WN);
        boolean useJdbc = jdbc != null ? jdbc.booleanValue() : false;
        // see #129332 - if nothing is specified, use the default adapter
        boolean useDefault = !jdbc && devel == null && production == null && test == null;
        
        if (useDefault) {
            databaseConfiguration = RailsAdapterFactory.getDefaultAdapter();
        } else if (useJdbc) {
            databaseConfiguration = new RailsJdbcConnection(devel, test, production);
        } else {
            databaseConfiguration = new RailsJdbcAsAdapterConnection(devel, test, production);
        }
        settings.putProperty(NewRailsProjectWizardIterator.RAILS_DEVELOPMENT_DB, databaseConfiguration);
    }

    @Override
    void read(WizardDescriptor settings) {
    }

    @Override
    boolean valid(WizardDescriptor settings) {
        return true;
    }

    @Override
    void validate(WizardDescriptor settings) throws WizardValidationException {
    }

}
