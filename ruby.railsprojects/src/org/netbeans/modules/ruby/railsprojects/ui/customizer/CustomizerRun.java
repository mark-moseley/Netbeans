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

package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    
    private RailsProject project;
    private String originalEncoding;
    
    private JTextField[] data;
    private JLabel[] dataLabels;
    private String[] keys;
    private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> configs;
    RailsProjectProperties uiProperties;
    private ItemListener platformListener;
    
    public CustomizerRun( RailsProjectProperties uiProperties ) {
        this.uiProperties = uiProperties;
        initComponents();

        this.project = uiProperties.getProject();
        
        configs = uiProperties.RUN_CONFIGS;
        
        data = new JTextField[] {
            portField,
            rakeTextField
        };
        dataLabels = new JLabel[] {
            portLabel,
            rakeLabel
        };
        keys = new String[] {
            RailsProjectProperties.RAILS_PORT,
            RailsProjectProperties.RAKE_ARGS
            //RailsProjectProperties.RAILS_ENV,
        };
        assert data.length == keys.length;
        
        configChanged(uiProperties.activeConfig);
        
        configCombo.setRenderer(new DefaultListCellRenderer() {
            public @Override Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                String config = (String) value;
                String label;
                if (config == null) {
                    // uninitialized?
                    label = null;
                } else if (config.length() > 0) {
                    Map<String,String> m = configs.get(config);
                    label = m != null ? m.get("$label") : /* temporary? */ null;
                    if (label == null) {
                        label = config;
                    }
                } else {
                    label = NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.default");
                }
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        
        for (int i = 0; i < data.length; i++) {
            final JTextField field = data[i];
            final String prop = keys[i];
            final JLabel label = dataLabels[i];
            field.getDocument().addDocumentListener(new DocumentListener() {
                Font basefont = label.getFont();
                Font boldfont = basefont.deriveFont(Font.BOLD);
                {
                    updateFont();
                }
                public void insertUpdate(DocumentEvent e) {
                    changed();
                }
                public void removeUpdate(DocumentEvent e) {
                    changed();
                }
                public void changedUpdate(DocumentEvent e) {}
                void changed() {
                    String config = (String) configCombo.getSelectedItem();
                    if (config.length() == 0) {
                        config = null;
                    }
                    String v = field.getText();
                    if (v != null && config != null && v.equals(configs.get(null).get(prop))) {
                        // default value, do not store as such
                        v = null;
                    }
                    configs.get(config).put(prop, v);
                    updateFont();
                }
                void updateFont() {
                    String v = field.getText();
                    String config = (String) configCombo.getSelectedItem();
                    if (config.length() == 0) {
                        config = null;
                    }
                    String def = configs.get(null).get(prop);
                    label.setFont(config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? boldfont : basefont);
                }
            });
        }

        this.originalEncoding = this.uiProperties.getProject().evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
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
        platforms.setSelectedItem(uiProperties.getPlatform());
    }
    
    public @Override void addNotify() {
        super.addNotify();
        platformListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    uiProperties.setPlatform(((RubyPlatform) platforms.getSelectedItem()));
                }
            }
        };
        platforms.addItemListener(platformListener);
    }
    
    public @Override void removeNotify() {
        platforms.removeItemListener(platformListener);
        super.removeNotify();
    }
    
    private String[] getEnvironmentNames() {
        return new String[] {
            NbBundle.getMessage(CustomizerRun.class, "Development"),
            NbBundle.getMessage(CustomizerRun.class, "Testing"),
            NbBundle.getMessage(CustomizerRun.class, "Production")
        };
    }
        
    private void handleEncodingChange() {
        Charset enc = (Charset)encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        this.uiProperties.putAdditionalProperty(RailsProjectProperties.SOURCE_ENCODING, encName);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerRun.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configSep = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        encodingLabel = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();
        rakeLabel = new javax.swing.JLabel();
        rakeTextField = new javax.swing.JTextField();
        rakeHelpLabel = new javax.swing.JLabel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        rubyPlatformLabel = new javax.swing.JLabel();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        manageButton = new javax.swing.JButton();

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N

        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });

        encodingLabel.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "TXT_Encoding")); // NOI18N

        rakeLabel.setLabelFor(rakeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rakeHelpLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RakeArgsEx")); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rakeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, encodingLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, portLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(encoding, 0, 479, Short.MAX_VALUE)
                    .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .add(rakeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)))
            .add(mainPanelLayout.createSequentialGroup()
                .add(120, 120, 120)
                .add(rakeHelpLabel)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingLabel)
                    .add(encoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rakeLabel)
                    .add(rakeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rakeHelpLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        portField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldArgs")); // NOI18N
        encoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_Encoding")); // NOI18N
        rakeTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_RakeArguments")); // NOI18N

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel")); // NOI18N

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDelete")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rubyPlatformLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyPlatformLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "RubyHomeBrowse")); // NOI18N
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(configLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configCombo, 0, 356, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configNew)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configDel))
            .add(layout.createSequentialGroup()
                .add(rubyPlatformLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(platforms, 0, 355, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(manageButton)
                .add(68, 68, 68))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, configSep, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyPlatformLabel)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(manageButton))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(configDel)
                    .add(configNew)
                    .add(configLabel)
                    .add(configCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(14, 14, 14)
                .add(configSep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        configCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_Configuration")); // NOI18N
        configNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_NewConfiguration")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_portFieldActionPerformed

    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        uiProperties.activeConfig = null;
    }//GEN-LAST:event_configDelActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.prompt"),
                NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.title"));
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
        if (configs.get(config) != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.duplicate", config),
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        Map<String,String> m = new HashMap<String,String>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.put(config, m);
        configChanged(config);
        uiProperties.activeConfig = config;
    }//GEN-LAST:event_configNewActionPerformed

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        configChanged(config);
        uiProperties.activeConfig = config;
    }//GEN-LAST:event_configComboActionPerformed

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        RubyPlatformCustomizer.manage(platforms);
    }//GEN-LAST:event_manageButtonActionPerformed

    private void configChanged(String activeConfig) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
            Collator coll = Collator.getInstance();
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }
            private String label(String c) {
                Map<String,String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String,Map<String,String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            model.addElement(c);
        }
        configCombo.setModel(model);
        configCombo.setSelectedItem(activeConfig != null ? activeConfig : "");
        Map<String,String> m = configs.get(activeConfig);
        Map<String,String> def = configs.get(null);
        if (m != null) {
            for (int i = 0; i < data.length; i++) {
                String v = m.get(keys[i]);
                if (v == null) {
                    // display default value
                    v = def.get(keys[i]);
                }
                data[i].setText(v);
            }
        } // else ??
        configDel.setEnabled(activeConfig != null);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JSeparator configSep;
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton manageButton;
    private javax.swing.JComboBox platforms;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JLabel rakeHelpLabel;
    private javax.swing.JLabel rakeLabel;
    private javax.swing.JTextField rakeTextField;
    private javax.swing.JLabel rubyPlatformLabel;
    // End of variables declaration//GEN-END:variables
    
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

        UnknownCharset(String name) {
            super(name, new String[0]);
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
}
