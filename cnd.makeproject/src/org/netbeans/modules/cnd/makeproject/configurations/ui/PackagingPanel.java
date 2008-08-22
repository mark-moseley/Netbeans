/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.packaging.InfoElement;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
public class PackagingPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener  {
    PackagingConfiguration packagingConfiguration;
    private PropertyEditorSupport editor;
    private MakeConfiguration conf;
    private PackagingInfoOuterPanel packagingInfoOuterPanel = null;
    private PackagingInfoPanel packagingInfoPanel = null;
    private PackagingFilesOuterPanel packagingFilesOuterPanel = null;
    private PackagingFilesPanel packagingFilesPanel = null;
    
    /** Creates new form PackagingPanel */
    public PackagingPanel(PackagingConfiguration packagingConfiguration, PropertyEditorSupport editor, PropertyEnv env, MakeConfiguration conf) {
        initComponents();
        
        this.packagingConfiguration = packagingConfiguration;
        this.editor = editor;
        this.conf = conf;
        
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
        
        // Add tabs
        packagingInfoOuterPanel = new PackagingInfoOuterPanel(packagingInfoPanel = new PackagingInfoPanel(packagingConfiguration.getHeader().getValue(), packagingConfiguration));
        if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            packagingFilesPanel = new PackagingFilesPanel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        else if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            packagingFilesPanel = new PackagingFiles4Panel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        else {
            packagingFilesPanel = new PackagingFiles4Panel(packagingConfiguration.getFiles().getValue(), conf.getBaseDir());
        }
        packagingFilesOuterPanel = new PackagingFilesOuterPanel(packagingFilesPanel, packagingConfiguration);
        
        tabbedPane.addTab(getString("InfoPanelText"), packagingInfoOuterPanel);
        tabbedPane.addTab(getString("FilePanelText"), packagingFilesOuterPanel);
            
        if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_ZIP || packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            // Add tabs
            tabbedPane.setEnabledAt(0,false);
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setSelectedIndex(1);
        }
        else if (packagingConfiguration.getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            // Add tabs
            tabbedPane.setEnabledAt(0,true);
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setSelectedIndex(0);
        }
        else {
            assert false;
        }
    }

        
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    private Object getPropertyValue() throws IllegalStateException {
        Vector v;
        
        v = packagingInfoPanel.getListData();
        packagingConfiguration.getHeader().setValue(new ArrayList(v));
        
        v = packagingFilesPanel.getListData();
        packagingConfiguration.getFiles().setValue(new ArrayList(v));
        
	return packagingConfiguration;
        
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("Libraries"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        innerPanel = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();

        setPreferredSize(new java.awt.Dimension(1000, 500));
        setLayout(new java.awt.GridBagLayout());

        innerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        innerPanel.add(tabbedPane, gridBagConstraints);
        tabbedPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleName")); // NOI18N
        tabbedPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingPanel.class, "PackagingPanel.tabbedPane.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(innerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel innerPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(PackagingPanel.class);
	}
	return bundle.getString(s);
    }
}
