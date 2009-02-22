/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPluginsManager;
import org.openide.util.NbBundle;

/**
 * @author David Calavera
 */
public class GrailsPluginsPanel extends javax.swing.JPanel {

    private GrailsProject project;
    private GrailsPluginsManager pluginsManager;
    
    private boolean installedInitialized;
    private boolean installedModified;
    private boolean availablePluginsInitialized;
    private boolean availableModified;
    private List<GrailsPlugin> installedPluginsList = new ArrayList<GrailsPlugin>();
    private List<GrailsPlugin> availablePluginsList = new ArrayList<GrailsPlugin>();

    private final ExecutorService refreshExecutor = Executors.newCachedThreadPool();

    /** Creates new customizer GrailsPluginsPanel */
    public GrailsPluginsPanel(Project project) {
        initComponents();
        this.project = (GrailsProject) project;
        this.pluginsManager = GrailsPluginsManager.getInstance(this.project);
    }

    public void dispose() {
        refreshExecutor.shutdownNow();
    }

    /** Refresh the installed plugin list */
    private void refreshInstalled() {
        assert SwingUtilities.isEventDispatchThread();
        final DefaultListModel model = new DefaultListModel();
        reloadInstalledButton.setEnabled(false);
        installedPluginsList = pluginsManager.loadInstalledPlugins();
        for (GrailsPlugin plugin : installedPluginsList) {
            model.addElement(plugin);
        }        

        installedPlugins.clearSelection();
        installedPlugins.setModel(model);
        installedPlugins.invalidate();
        installedPlugins.repaint();
        reloadInstalledButton.setEnabled(true);         
    }
    
    private void refreshAvailable() {
        assert SwingUtilities.isEventDispatchThread();

        final Runnable runner = new Runnable() {
            public void run() {

                boolean interrupted = false;

                List<GrailsPlugin> plugins;
                try {
                    plugins = pluginsManager.refreshAvailablePlugins();
                } catch (InterruptedException ex) {
                    interrupted = true;
                    plugins = Collections.emptyList();
                }

                try {
                    final List<GrailsPlugin> pluginsToLoad = plugins;

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // FIXME use model impl instead of private list
                            availablePluginsList = new ArrayList<GrailsPlugin>(pluginsToLoad);
                            DefaultListModel model = new DefaultListModel();
                            for (GrailsPlugin plugin : availablePluginsList) {
                                if (!installedPluginsList.contains(plugin)) {
                                    model.addElement(plugin);
                                }
                            }

                            availablePlugins.clearSelection();
                            availablePlugins.setModel(model);
                            availablePlugins.invalidate();
                            availablePlugins.repaint();
                            reloadAvailableButton.setEnabled(true);
                        }
                    });
                } finally {
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        
        reloadAvailableButton.setEnabled(false);
        final DefaultListModel model = new DefaultListModel();
        model.addElement(NbBundle.getMessage(GrailsPluginsPanel.class, "FetchingPlugins"));
        availablePlugins.setModel(model);
            
        refreshExecutor.submit(runner);
    }
    
    private void uninstallPlugins() {
        final Object[] selected = installedPlugins.getSelectedValues();
        
        pluginsManager.uninstallPlugins(toPluginCollection(selected));

        refreshInstalled();
        availableModified = true;
    }
    
    private void installPlugins() {
        installButton.setEnabled(false);
        
        final Object[] selected = availablePlugins.getSelectedValues();
        Collection<GrailsPlugin> selectedColl = toPluginCollection(selected);
        
        if (pluginZipPath.getText() != null && pluginZipPath.getText().trim().length() > 0) {
            GrailsPlugin plugin = pluginsManager.getPluginFromZipFile(pluginZipPath.getText());
            if (plugin != null && !selectedColl.contains(plugin)
                    && !installedPluginsList.contains(plugin)) {
                selectedColl.add(plugin);
            }
        }
        availableModified = pluginsManager.installPlugins(selectedColl);

        refreshInstalled();        

        if (availableModified) {
            pluginsPanel.setSelectedComponent(installedPanel);
            pluginsPanel.invalidate();
            pluginsPanel.repaint();
        }
        installButton.setEnabled(true);
    }
    
    private Collection<GrailsPlugin> toPluginCollection(Object[] selected) {
        Collection<GrailsPlugin> coll = new ArrayList<GrailsPlugin>();
        if (selected != null && selected.length > 0) {                  
            for (Object obj : selected) {
                coll.add((GrailsPlugin) obj);
            }
        }
        return coll;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pluginsPanel = new javax.swing.JTabbedPane();
        installedPanel = new javax.swing.JPanel();
        reloadInstalledButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        installedPlugins = new javax.swing.JList();
        uninstallButton = new javax.swing.JButton();
        newPluginPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availablePlugins = new javax.swing.JList();
        installButton = new javax.swing.JButton();
        reloadAvailableButton = new javax.swing.JButton();
        pluginLocationLabel = new javax.swing.JLabel();
        pluginZipPath = new javax.swing.JTextField();
        pluginBrowseButton = new javax.swing.JButton();

        installedPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                installedPanelComponentShown(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reloadInstalledButton, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadInstalledButton.text")); // NOI18N
        reloadInstalledButton.setActionCommand(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadInstalledButton.text")); // NOI18N
        reloadInstalledButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadInstalledButtonActionPerformed(evt);
            }
        });

        installedPlugins.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return installedPluginsList.size(); }
            public Object getElementAt(int arg0) { return installedPluginsList.get(arg0); }
        });
        installedPlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                installedPluginsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(installedPlugins);
        installedPlugins.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.installedPlugins.accessibleName")); // NOI18N
        installedPlugins.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.installedPlugins.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(uninstallButton, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.uninstallButton.text")); // NOI18N
        uninstallButton.setEnabled(false);
        uninstallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout installedPanelLayout = new org.jdesktop.layout.GroupLayout(installedPanel);
        installedPanel.setLayout(installedPanelLayout);
        installedPanelLayout.setHorizontalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(reloadInstalledButton)
                    .add(uninstallButton)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE))
                .addContainerGap())
        );
        installedPanelLayout.setVerticalGroup(
            installedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, installedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(reloadInstalledButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(uninstallButton)
                .addContainerGap())
        );

        reloadInstalledButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadInstalledButton.accessibleName")); // NOI18N
        reloadInstalledButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadInstalledButton.accessibleDescription")); // NOI18N

        pluginsPanel.addTab(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.installed"), installedPanel); // NOI18N

        newPluginPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                newPluginPanelComponentShown(evt);
            }
        });

        availablePlugins.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return availablePluginsList.size(); }
            public Object getElementAt(int arg0) { return availablePluginsList.get(arg0); }
        });
        availablePlugins.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                availablePluginsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(availablePlugins);
        availablePlugins.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.availablePlugins.accessibleName")); // NOI18N
        availablePlugins.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.availablePlugins.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(installButton, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.installButton.text")); // NOI18N
        installButton.setEnabled(false);
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reloadAvailableButton, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadAvailableButton.text")); // NOI18N
        reloadAvailableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadAvailableButtonActionPerformed(evt);
            }
        });

        pluginLocationLabel.setLabelFor(pluginZipPath);
        org.openide.awt.Mnemonics.setLocalizedText(pluginLocationLabel, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginLocationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pluginBrowseButton, org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginBrowseButton.text")); // NOI18N
        pluginBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pluginBrowseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout newPluginPanelLayout = new org.jdesktop.layout.GroupLayout(newPluginPanel);
        newPluginPanel.setLayout(newPluginPanelLayout);
        newPluginPanelLayout.setHorizontalGroup(
            newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(newPluginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                    .add(newPluginPanelLayout.createSequentialGroup()
                        .add(installButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pluginLocationLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pluginZipPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pluginBrowseButton))
                    .add(reloadAvailableButton))
                .addContainerGap())
        );
        newPluginPanelLayout.setVerticalGroup(
            newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, newPluginPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(reloadAvailableButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(newPluginPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(installButton)
                    .add(pluginBrowseButton)
                    .add(pluginLocationLabel)
                    .add(pluginZipPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        reloadAvailableButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadAvailableButton.accessibleName")); // NOI18N
        reloadAvailableButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.reloadAvailableButton.accessibleDescription")); // NOI18N
        pluginZipPath.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginZipPath.accessibleName")); // NOI18N
        pluginZipPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginZipPath.accessibleDescription")); // NOI18N
        pluginBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginBrowseButton.accessibleName")); // NOI18N
        pluginBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginBrowseButton.accessibleDescription")); // NOI18N

        pluginsPanel.addTab(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.newPlugins"), newPluginPanel); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(pluginsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                .addContainerGap())
        );

        pluginsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginsPanel.accessibleName")); // NOI18N
        pluginsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.pluginsPanel.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GrailsPluginsPanel.class, "GrailsPluginPanel.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void uninstallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uninstallButtonActionPerformed
    uninstallPlugins();                                               
}                                               

private void reloadAvailableButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
    refreshAvailable();                                                     
}//GEN-HEADEREND:event_uninstallButtonActionPerformed
//GEN-LAST:event_uninstallButtonActionPerformed
private void reloadInstalledButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
    refreshInstalled();                                                     
}                                                          
                                                     
private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
    installPlugins();                                             
}                                                          
                                                     
private void installedPluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
    uninstallButton.setEnabled(installedPlugins.getSelectedIndices() != null//GEN-HEADEREND:event_installButtonActionPerformed
            && installedPlugins.getSelectedIndices().length > 0);//GEN-LAST:event_installButtonActionPerformed
}//GEN-FIRST:event_installedPluginsValueChanged

private void availablePluginsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-HEADEREND:event_installedPluginsValueChanged
    installButton.setEnabled(availablePlugins.getSelectedIndices() != null//GEN-LAST:event_installedPluginsValueChanged
            && availablePlugins.getSelectedIndices().length > 0);
    //waiting the plugin list from the server
    if (availablePlugins.getSelectedIndices().length == 1 && 
            availablePlugins.getSelectedValue().equals(
            NbBundle.getMessage(GrailsPluginsPanel.class, "FetchingPlugins"))) {                                                  
        installButton.setEnabled(false);                                             
    }
}                                             

private void installedPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_installedPanelComponentShown
    assert SwingUtilities.isEventDispatchThread();                                             

    if (!installedInitialized) {
        installedInitialized = true;
        installedPlugins.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        installedModified = true;
    } else installedModified = false;//GEN-HEADEREND:event_installedPanelComponentShown
//GEN-LAST:event_installedPanelComponentShown
    if (installedModified) {
        refreshInstalled();
    }
}                                             

private void newPluginPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_newPluginPanelComponentShown
    assert SwingUtilities.isEventDispatchThread();                                             

    if (!availablePluginsInitialized) {
        availablePluginsInitialized = true;
        availablePlugins.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        availableModified = true;
    }

    if (availableModified) {//GEN-HEADEREND:event_newPluginPanelComponentShown
        refreshAvailable();//GEN-LAST:event_newPluginPanelComponentShown
        availableModified = false;
    }
}                                             

private void pluginBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pluginBrowseButtonActionPerformed
    final File current = new File(pluginZipPath.getText());                                                  
    final JFileChooser chooser = new JFileChooser(current);

    final FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getPath().toLowerCase().endsWith(".zip");//GEN-HEADEREND:event_pluginBrowseButtonActionPerformed
        }//GEN-LAST:event_pluginBrowseButtonActionPerformed

        @Override
        public String getDescription() {
            return "*.zip";
        }
    };
    
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setFileFilter(fileFilter);            
    chooser.setDialogTitle(NbBundle.getMessage(GrailsPluginsPanel.class, "TITLE_BrowsePluginLocation"));
    chooser.setApproveButtonText(NbBundle.getMessage(GrailsPluginsPanel.class, "LBL_BrowsePluginLocation_OK_Button"));

    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        pluginZipPath.setText(chooser.getSelectedFile().getAbsolutePath());                                                  
        installButton.setEnabled(true);
    }
}                                                  


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList availablePlugins;
    private javax.swing.JButton installButton;
    private javax.swing.JPanel installedPanel;
    private javax.swing.JList installedPlugins;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel newPluginPanel;
    private javax.swing.JButton pluginBrowseButton;
    private javax.swing.JLabel pluginLocationLabel;
    private javax.swing.JTextField pluginZipPath;
    private javax.swing.JTabbedPane pluginsPanel;
    private javax.swing.JButton reloadAvailableButton;
    private javax.swing.JButton reloadInstalledButton;
    private javax.swing.JButton uninstallButton;
    // End of variables declaration//GEN-END:variables

}
