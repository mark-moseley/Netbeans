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
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import javax.swing.plaf.UIResource;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.DirectoryChooserInnerPanel;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class ProjectPropPanel extends javax.swing.JPanel implements ActionListener {

    private SourceRootChooser sourceRootChooser;
    private Project project;
    private MakeConfigurationDescriptor makeConfigurationDescriptor;
    private String originalEncoding;

    /** Creates new form ProjectPropPanel */
    public ProjectPropPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
        this.project = project;
        makeConfigurationDescriptor = (MakeConfigurationDescriptor) configurationDescriptor;
        initComponents();
        projectTextField.setText(FileUtil.toFile(project.getProjectDirectory()).getPath());
        sourceRootPanel.add(sourceRootChooser = new SourceRootChooser(configurationDescriptor.getBaseDir(), makeConfigurationDescriptor.getSourceRootsAsArray()));

        MakeCustomizerProvider makeCustomizerProvider = (MakeCustomizerProvider) project.getLookup().lookup(MakeCustomizerProvider.class);
        makeCustomizerProvider.addActionListener(this);
        
        this.originalEncoding = getPreferences().get("CND_ENCODING", null);
        if (originalEncoding == null) {
            Charset enc = FileEncodingQuery.getDefaultEncoding();
            originalEncoding = enc.name();
        }
//        if (originalEncoding != null) {
//            try {
//                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
//            } catch (UnsupportedCharsetException e) {
//                //When the encoding is not supported by JVM do not set it as default
//            }
//        }
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(new EncodingModel(this.originalEncoding));
        this.encoding.setRenderer(new EncodingRenderer());
        

        this.encoding.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }            
        });
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(ProjectPropPanel.class);
    }
    
    private void handleEncodingChange () {
            Charset enc = (Charset) encoding.getSelectedItem();
            String encName;
            if (enc != null) {
                encName = enc.name();
            }
            else {
                encName = originalEncoding;
            }
            getPreferences().put("CND_ENCODING", encName);
    }
    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel (String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset (originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + originalEncoding);
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset (String name) {
            super (name, new String[0]);
        }
    
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (sourceRootChooser.isChanged()) {
            Vector list = sourceRootChooser.getListData();
            makeConfigurationDescriptor.setSourceRootsList(new ArrayList(list));
        }
        MakeCustomizerProvider makeCustomizerProvider = (MakeCustomizerProvider) project.getLookup().lookup(MakeCustomizerProvider.class);
        makeCustomizerProvider.removeActionListener(this);
    }

    class SourceRootChooser extends DirectoryChooserInnerPanel {

        public SourceRootChooser(String baseDir, Object[] feed) {
            super(baseDir, feed);
            getCopyButton().setVisible(false);
            getEditButton().setVisible(false);
        }

        @Override
        public String getListLabelText() {
            return getString("ProjectPropPanel.sourceRootLabel.text");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("ProjectPropPanel.sourceRootLabel.mn").charAt(0);
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        sourceRootPanel = new javax.swing.JPanel();
        encodingPanel = new javax.swing.JPanel();
        encodingLabel = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("ProjectPropPanel.projectLabel.mn").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(projectLabel, gridBagConstraints);
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.projectLabel.ad")); // NOI18N

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(projectTextField, gridBagConstraints);

        sourceRootPanel.setBackground(new java.awt.Color(255, 255, 255));
        sourceRootPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(sourceRootPanel, gridBagConstraints);

        encodingLabel.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(ProjectPropPanel.class, "ProjectPropPanel.encodingLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout encodingPanelLayout = new org.jdesktop.layout.GroupLayout(encodingPanel);
        encodingPanel.setLayout(encodingPanelLayout);
        encodingPanelLayout.setHorizontalGroup(
            encodingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(encodingPanelLayout.createSequentialGroup()
                .add(encodingLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(encoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        encodingPanelLayout.setVerticalGroup(
            encodingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(encodingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(encodingLabel)
                .add(encoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(encodingPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel encodingPanel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JPanel sourceRootPanel;
    // End of variables declaration//GEN-END:variables
    private static String getString(String key) {
        return NbBundle.getMessage(ProjectPropPanel.class, key);
    }
}
