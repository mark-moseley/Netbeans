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

package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JSpinner;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public final class FormattingCustomizerPanel extends javax.swing.JPanel implements ActionListener {
    
    // ------------------------------------------------------------------------
    // ProjectCustomizer.CompositeCategoryProvider implementation
    // ------------------------------------------------------------------------

    /**
     * Creates an instance of the 'Formatting' category in the project properties dialog.
     * This method is meant to be used from XML layers by modules that wish to add
     * the 'Formatting' category to their project type's properties dialog.
     *
     * <p>The method recognizes 'allowedMimeTypes' XML layer attribute, which should
     * contain the comma separated list of mime types, which formatting settings
     * customizers should be made available for the project. If the attribute is
     * not specified all registered customizers are shown. If the attribute specifies
     * an empty list only the 'All Languages' customizer is shown.
     *
     * @param attrs The map of <code>FileObject</code> attributes
     *
     * @return A new 'Formatting' category provider.
     * @since 1.10
     */
    public static ProjectCustomizer.CompositeCategoryProvider createCategoryProvider(Map attrs) {
        return new Factory((String)attrs.get("allowedMimeTypes")); //NOI18N
    }

    public static class Factory implements ProjectCustomizer.CompositeCategoryProvider {
 
        private static final String CATEGORY_FORMATTING = "Formatting"; // NOI18N
        private final String allowedMimeTypes;

        public Factory() {
            this(null);
        }

        public Factory(String allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
        }

        public ProjectCustomizer.Category createCategory(Lookup context) {
            return context.lookup(Project.class) == null ? null : ProjectCustomizer.Category.create(
                    CATEGORY_FORMATTING, 
                    NbBundle.getMessage(Factory.class, "LBL_CategoryFormatting"), //NOI18N
                    null);
        }

        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            FormattingCustomizerPanel customizerPanel = new FormattingCustomizerPanel(context, allowedMimeTypes);
            category.setStoreListener(customizerPanel);
            return customizerPanel;
        }
    } // End of Factory class
    
    // ------------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------------

    // this is called when OK button is clicked to store the controlled preferences
    public void actionPerformed(ActionEvent e) {
        if (DEFAULT_PROFILE.equals(pf.getPreferences("").parent().get(USED_PROFILE, DEFAULT_PROFILE))) { //NOI18N
            // no per-project formatting settings
            Preferences p = ProjectUtils.getPreferences(pf.getProject(), IndentUtils.class, true);
            try {
                removeAllKidsAndKeys(p);
            } catch (BackingStoreException bse) {
                LOG.log(Level.WARNING, null, bse);
            }
        } else {
            pf.applyChanges();

            // Find mimeTypes that do not have a customizer
            Set<String> mimeTypes = new HashSet<String>(EditorSettings.getDefault().getAllMimeTypes());
            mimeTypes.removeAll(selector.getMimeTypes());

            // and make sure that they do NOT override basic settings from All Languages
            Preferences p = ProjectUtils.getPreferences(pf.getProject(), IndentUtils.class, true);
            for(String mimeType : mimeTypes) {
                try {
                    p.node(mimeType).removeNode();
                } catch (BackingStoreException bse) {
                    LOG.log(Level.WARNING, null, bse);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group = new javax.swing.ButtonGroup();
        globalButton = new javax.swing.JRadioButton();
        editGlobalButton = new javax.swing.JButton();
        projectButton = new javax.swing.JRadioButton();
        loadButton = new javax.swing.JButton();
        customizerPanel = new javax.swing.JPanel();

        group.add(globalButton);
        org.openide.awt.Mnemonics.setLocalizedText(globalButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Global")); // NOI18N
        globalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editGlobalButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_EditGlobal")); // NOI18N
        editGlobalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGlobalButtonActionPerformed(evt);
            }
        });

        group.add(projectButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Project")); // NOI18N
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(loadButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_ForamttingCustomizer_Load")); // NOI18N
        loadButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        customizerPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .add(globalButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(editGlobalButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(loadButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(customizerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(globalButton)
                    .add(editGlobalButton))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectButton)
                    .add(loadButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(customizerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
        );

        globalButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.globalButton.AccessibleContext.accessibleDescription")); // NOI18N
        editGlobalButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.editGlobalButton.AccessibleContext.accessibleDescription")); // NOI18N
        projectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.projectButton.AccessibleContext.accessibleDescription")); // NOI18N
        loadButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.loadButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void globalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalButtonActionPerformed

    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_use_global_settings_confirmation"), //NOI18N
            NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_use_global_settings_confirmation_title"), //NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION
    );

    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
        pf.getPreferences("").parent().put(USED_PROFILE, DEFAULT_PROFILE); //NOI18N
        loadButton.setEnabled(false);
        setEnabled(panel, false);
    }

}//GEN-LAST:event_globalButtonActionPerformed

private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed

    pf.getPreferences("").parent().put(USED_PROFILE, PROJECT_PROFILE); //NOI18N
    loadButton.setEnabled(true);
    setEnabled(panel, true);

    if (copyOnFork) {
        copyOnFork = false;

        // copy global settings
        EditorSettingsStorage<String, TypedValue> storage = EditorSettingsStorage.<String, TypedValue>get("Preferences"); //NOI18N
        for(String mimeType : selector.getMimeTypes()) {
            Map<String, TypedValue> mimePathLocalPrefs;
            try {
                mimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
                continue;
            }

            Preferences projectPrefs = pf.getPreferences(mimeType);
            
            // XXX: we should somehow be able to determine __all__ the formatting settings
            // for each mime type, but there can be different sets of settings for different
            // mime types (eg. all java, ruby and C++ have different formatting settings).
            // The only way is to stash all formatting settings under one common Preferences node
            // as it is in projects. The problem is that MimeLookup's Preferences implementation
            // does not support subnodes.
            // So, we at least copy the basic setting
            boolean copied = false;
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.EXPAND_TABS);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.INDENT_SHIFT_WIDTH);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.SPACES_PER_TAB);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.TAB_SIZE);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.TEXT_LIMIT_WIDTH);

            if (mimeType.length() > 0 && copied) {
                projectPrefs.putBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, true);
            }
        }
    }
}//GEN-LAST:event_projectButtonActionPerformed

private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
    JFileChooser chooser = ProjectChooser.projectChooser();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();
        FileObject fo = FileUtil.toFileObject(f);
        if (fo != null) {
            Object ret;
            
            try {
                final Project prjFrom = ProjectManager.getDefault().findProject(fo);
                if (prjFrom == pf.getProject()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Forbidden_From_The_Same_Project"), //NOI18N
                        NotifyDescriptor.Message.PLAIN_MESSAGE));

                    return;
                }

                ret = ProjectManager.mutex().readAccess(new ExceptionAction<Object>() {
                    public Object run() throws Exception {
                        Preferences fromPrjPrefs = ProjectUtils.getPreferences(prjFrom, IndentUtils.class, true);

                        if (!fromPrjPrefs.nodeExists(CODE_STYLE_PROFILE) ||
                            fromPrjPrefs.node(CODE_STYLE_PROFILE).get(USED_PROFILE, null) == null
                        ) {
                            return NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_No_CodeStyle_Info_To_Import"); //NOI18N
                        }

                        ProjectPreferencesFactory newPrefsFactory = new ProjectPreferencesFactory(pf.getProject());
                        Preferences toPrjPrefs = newPrefsFactory.projectPrefs;

                        removeAllKidsAndKeys(toPrjPrefs);
                        deepCopy(fromPrjPrefs, toPrjPrefs);

                        // XXX: detect somehow if the basic options are overriden in fromPrjPrefs
                        // and set the flag accordingly in toPrjPrefs
                        
                        //dump(fromPrjPrefs, "fromPrjPrefs");
                        //dump(toPrjPrefs, "toPrjPrefs");

                        return newPrefsFactory;
                    }
                });

            } catch (Exception e) {
                LOG.log(Level.INFO, null, e);
                ret = e;
            }

            if (ret instanceof ProjectPreferencesFactory) {
                String selectedMimeType = selector.getSelectedMimeType();
                PreferencesCustomizer c = selector.getSelectedCustomizer();
                String selectedCustomizerId = c != null ? c.getId() : null;

                pf.destroy();
                pf = (ProjectPreferencesFactory) ret;
                selector = new CustomizerSelector(pf, false, allowedMimeTypes);
                panel.setSelector(selector);

                if (selectedMimeType != null) {
                    selector.setSelectedMimeType(selectedMimeType);
                }
                if (selectedCustomizerId != null) {
                    selector.setSelectedCustomizer(selectedCustomizerId);
                }

                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Successful"), //NOI18N
                    NotifyDescriptor.Message.PLAIN_MESSAGE));

            } else if (ret instanceof Exception) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Failed"), //NOI18N
                    NotifyDescriptor.Message.WARNING_MESSAGE));
                
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    (String) ret,
                    NotifyDescriptor.Message.PLAIN_MESSAGE));
            }
        }
    }
}//GEN-LAST:event_loadButtonActionPerformed

    private void dump(Preferences prefs, String prefsId) throws BackingStoreException {
        for(String key : prefs.keys()) {
            System.out.println(prefsId + ", " + prefs.absolutePath() + "/" + key + "=" + prefs.get(key, null));
        }
        for(String child : prefs.childrenNames()) {
            dump(prefs.node(child), prefsId);
        }
    }

private void editGlobalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGlobalButtonActionPerformed
    OptionsDisplayer.getDefault().open(GLOBAL_OPTIONS_CATEGORY);
}//GEN-LAST:event_editGlobalButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton editGlobalButton;
    private javax.swing.JRadioButton globalButton;
    private javax.swing.ButtonGroup group;
    private javax.swing.JButton loadButton;
    private javax.swing.JRadioButton projectButton;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FormattingCustomizerPanel.class.getName());
    
    private static final String GLOBAL_OPTIONS_CATEGORY = "Editor/Formating"; //NOI18N
    private static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N
    private static final String USED_PROFILE = "usedProfile"; // NOI18N

    private final String allowedMimeTypes;
    private ProjectPreferencesFactory pf;
    private CustomizerSelector selector;
    private final FormattingPanel panel;
    private boolean copyOnFork;
    
    /** Creates new form CodeStyleCustomizerPanel */
    private FormattingCustomizerPanel(Lookup context, String allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
        this.pf = new ProjectPreferencesFactory(context.lookup(Project.class));
        this.selector = new CustomizerSelector(pf, false, allowedMimeTypes);
        this.panel = new FormattingPanel();
        this.panel.setSelector(selector);

        initComponents();
        customizerPanel.add(panel, BorderLayout.CENTER);
        
        Preferences prefs = pf.getPreferences("").parent(); //NOI18N
        this.copyOnFork = prefs.get(USED_PROFILE, null) == null;
        String profile = prefs.get(USED_PROFILE, DEFAULT_PROFILE);
        if (DEFAULT_PROFILE.equals(profile)) {
            globalButton.setSelected(true);
            loadButton.setEnabled(false);
            setEnabled(panel, false);
//            globalButton.doClick();
        } else {
//            projectButton.doClick();
            projectButton.setSelected(true);
            loadButton.setEnabled(true);
            setEnabled(panel, true);
        }
    }
    
    private void setEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container && !(component instanceof JSpinner)) {
            for (Component c : ((Container)component).getComponents()) {
                setEnabled(c, enabled);
            }
        }
    }

    private static boolean copyValueIfExists(Map<String, TypedValue> src, Preferences trg, String key) {
        TypedValue value = src.get(key);
        if (value != null) {
            // since project Preferences do not support javaType we can just use simple put
            trg.put(key, value.getValue());
            return true;
        } else {
            return false;
        }
    }

    private static void removeAllKidsAndKeys(Preferences prefs) throws BackingStoreException {
        for(String kid : prefs.childrenNames()) {
            prefs.node(kid).removeNode();
        }
        for(String key : prefs.keys()) {
            prefs.remove(key);
        }
    }

    private static void deepCopy(Preferences from, Preferences to) throws BackingStoreException {
        for(String kid : from.childrenNames()) {
            Preferences fromKid = from.node(kid);
            Preferences toKid = to.node(kid);
            deepCopy(fromKid, toKid);
        }
        for(String key : from.keys()) {
            String value = from.get(key, null);
            assert value != null : "Preferences should never have value == null."; //NOI18N

            Class type = guessType(value);
            if (Integer.class == type) {
                to.putInt(key, from.getInt(key, -1));
            } else if (Long.class == type) {
                to.putLong(key, from.getLong(key, -1L));
            } else if (Float.class == type) {
                to.putFloat(key, from.getFloat(key, -1f));
            } else if (Double.class == type) {
                to.putDouble(key, from.getDouble(key, -1D));
            } else if (Boolean.class == type) {
                to.putBoolean(key, from.getBoolean(key, false));
            } else if (String.class == type) {
                to.put(key, value);
            } else /* byte [] */ {
                to.putByteArray(key, from.getByteArray(key, new byte [0]));
            }
        }
    }

    // XXX: this is here only to supprt deprecated Settings.class, when we are sure,
    // that no code uses Settings.class we will be able to remove this
    private static Class guessType(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { //NOI18N
            return Boolean.class;
        }

        try {
            Integer.parseInt(value);
            return Integer.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Long.parseLong(value);
            return Long.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Float.parseFloat(value);
            return Float.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Double.parseDouble(value);
            return Double.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        // XXX: ignoring byte []
        return String.class;
    }
    
    private static final class ProjectPreferencesFactory implements CustomizerSelector.PreferencesFactory {

        public ProjectPreferencesFactory(Project project) {
            this.project = project;
            Preferences p = ProjectUtils.getPreferences(project, IndentUtils.class, true);
            projectPrefs = ProxyPreferences.getProxyPreferences(this, p);
        }

        public Project getProject() {
            return project;
        }

        public void destroy() {
            accessedMimeTypes.clear();
            projectPrefs.destroy();
            projectPrefs = null;
        }

        // --------------------------------------------------------------------
        // CustomizerSelector.PreferencesFactory implementation
        // --------------------------------------------------------------------

        public synchronized Preferences getPreferences(String mimeType) {
            assert projectPrefs != null;
            accessedMimeTypes.add(mimeType);
            return projectPrefs.node(mimeType).node(CODE_STYLE_PROFILE).node(PROJECT_PROFILE);
        }

        public synchronized void applyChanges() {
            for(String mimeType : accessedMimeTypes) {
                if (mimeType.length() == 0) {
                    continue;
                }

                ProxyPreferences pp = (ProxyPreferences) getPreferences(mimeType);
                if (null != pp.get(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null)) {
                    // tabs-and-indents has been used and the basic options might have been changed
                    pp.silence();
                    if (!pp.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false)) {
                        // remove the basic settings if a language is not overriding the 'all languages' values
                        pp.remove(SimpleValueNames.EXPAND_TABS);
                        pp.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
                        pp.remove(SimpleValueNames.SPACES_PER_TAB);
                        pp.remove(SimpleValueNames.TAB_SIZE);
                        pp.remove(SimpleValueNames.TEXT_LIMIT_WIDTH);
                    }
                    pp.remove(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS);
                }
            }

            // flush the root prefs
            projectPrefs.silence();
            try {
                LOG.fine("Flushing root pp"); //NOI18N
                projectPrefs.flush();
            } catch (BackingStoreException ex) {
                LOG.log(Level.WARNING, "Can't flush project codestyle root preferences", ex); //NOI18N
            }

            destroy();
        }

        public boolean isKeyOverridenForMimeType(String key, String mimeType) {
            Preferences p = ProjectUtils.getPreferences(project, IndentUtils.class, true);
            p = p.node(mimeType).node(CODE_STYLE_PROFILE).node(PROJECT_PROFILE);
            return p.get(key, null) != null;
        }

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private final Project project;
        private final Set<String> accessedMimeTypes = new HashSet<String>();
        private ProxyPreferences projectPrefs;

    } // End of ProjectPreferencesFactory class
}
