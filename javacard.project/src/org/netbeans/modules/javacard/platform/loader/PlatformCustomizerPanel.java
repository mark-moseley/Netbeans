/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform.loader;

import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Properties;

/**
 *
 * @author Tim Boudreau
 */
public class PlatformCustomizerPanel extends javax.swing.JPanel implements ChangeListener {
    private final JavacardPlatformDataObject dob;
    private final Properties properties;

    public PlatformCustomizerPanel(JavacardPlatformDataObject dob) {
        this.dob = dob;
        PropertiesAdapter adap = dob.getLookup().lookup(PropertiesAdapter.class);
        assert adap != null;
        this.properties = adap.asProperties();
        if (!dob.isValid()) {
            setLayout (new BorderLayout());
            add (new JLabel(NbBundle.getMessage(PlatformCustomizerPanel.class,
                    "MSG_INVALID_PLATFORM"))); //NOI18N
        } else {
            initComponents();
            initUi();
        }
    }

    private void initUi() {
        antNameField.setText (dob.getName());
        devicesPanel.add(new DevicesForm(dob));
        classpathPanel.add (new PathPropertyForm(properties,
                JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH));
        sourcesPanel.add (new PathPropertyForm(properties,
                JavacardPlatformKeyNames.PLATFORM_SRC_PATH));
        javadocPanel.add (new PathPropertyForm(properties,
                JavacardPlatformKeyNames.PLATFORM_JAVADOC_PATH));
        classicClasspathPanel.add (new PathPropertyForm(properties,
                JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH));
        int toSelect = NbPreferences.forModule(PlatformCustomizerPanel.class).getInt(
                "selectedTabIndex", 0); //NOI18N
        tabs.setSelectedIndex(toSelect);
        tabs.addChangeListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        antNameLabel = new javax.swing.JLabel();
        antNameField = new javax.swing.JTextField();
        tabs = new javax.swing.JTabbedPane();
        devicesPanel = new javax.swing.JPanel();
        classpathPanel = new javax.swing.JPanel();
        sourcesPanel = new javax.swing.JPanel();
        javadocPanel = new javax.swing.JPanel();
        classicClasspathPanel = new javax.swing.JPanel();

        antNameLabel.setLabelFor(antNameField);
        antNameLabel.setText(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.antNameLabel.text")); // NOI18N

        antNameField.setEditable(false);
        antNameField.setText(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.antNameField.text")); // NOI18N

        devicesPanel.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.devicesPanel.TabConstraints.tabTitle"), devicesPanel); // NOI18N

        classpathPanel.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.classpathPanel.TabConstraints.tabTitle"), classpathPanel); // NOI18N

        sourcesPanel.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.sourcesPanel.TabConstraints.tabTitle"), sourcesPanel); // NOI18N

        javadocPanel.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.javadocPanel.TabConstraints.tabTitle"), javadocPanel); // NOI18N

        classicClasspathPanel.setLayout(new java.awt.BorderLayout());
        tabs.addTab(org.openide.util.NbBundle.getMessage(PlatformCustomizerPanel.class, "PlatformCustomizerPanel.classicClasspathPanel.TabConstraints.tabTitle"), classicClasspathPanel); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(antNameLabel)
                .add(18, 18, 18)
                .add(antNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))
            .add(tabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(antNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(antNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(tabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField antNameField;
    private javax.swing.JLabel antNameLabel;
    private javax.swing.JPanel classicClasspathPanel;
    private javax.swing.JPanel classpathPanel;
    private javax.swing.JPanel devicesPanel;
    private javax.swing.JPanel javadocPanel;
    private javax.swing.JPanel sourcesPanel;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables

    public void stateChanged(ChangeEvent e) {
        NbPreferences.forModule(PlatformCustomizerPanel.class).putInt(
                "selectedTabIndex", tabs.getSelectedIndex()); //NOI18N
    }

}
