/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

/**
 * Action permitting selection of a configuration for the main project.
 * @author Greg Crawley, Adam Sotona, Jesse Glick
 */
public class ActiveConfigAction extends CallableSystemAction implements ContextAwareAction {

    private static final DefaultComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel();
    private static final Object CUSTOMIZE_ENTRY = new Object();

    private final PropertyChangeListener lst;
    private final JComboBox configListCombo;
    private boolean listeningToCombo = true;

    private Project currentProject;
    private ProjectConfigurationProvider pcp;

    public ActiveConfigAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        configListCombo = new JComboBox();
        configListCombo.setRenderer(new ConfigCellRenderer());
        configListCombo.setToolTipText(org.openide.awt.Actions.cutAmpersand(getName()));
        configurationsListChanged(null);
        configListCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!listeningToCombo) {
                    return;
                }
                Object o = configListCombo.getSelectedItem();
                if (o == CUSTOMIZE_ENTRY) {
                    activeConfigurationChanged(pcp != null ? getActiveConfiguration(pcp) : null);
                    pcp.customize();
                } else if (o != null) {
                    activeConfigurationSelected((ProjectConfiguration) o);
                }
            }
        });
        lst = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ProjectConfigurationProvider.PROP_CONFIGURATIONS.equals(evt.getPropertyName())) {
                    configurationsListChanged(getConfigurations(pcp));
                } else if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
                    activeConfigurationChanged(getActiveConfiguration(pcp));
                }
            }
        };
        activeProjectChanged(OpenProjectList.getDefault().getMainProject());
        OpenProjectList.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (OpenProjectList.PROPERTY_MAIN_PROJECT.equals(evt.getPropertyName())) {
                    activeProjectChanged(OpenProjectList.getDefault().getMainProject());
                }
            }
        });
    }


    private synchronized void configurationsListChanged(Collection<? extends ProjectConfiguration> configs) {
        if (configs == null) {
            configListCombo.setModel(EMPTY_MODEL);
            configListCombo.setEnabled(false);
        } else {
            DefaultComboBoxModel model = new DefaultComboBoxModel(configs.toArray());
            if (pcp.hasCustomizer()) {
                model.addElement(CUSTOMIZE_ENTRY);
            }
            configListCombo.setModel(model);
            configListCombo.setEnabled(true);
        }
        if (pcp != null) {
            activeConfigurationChanged(getActiveConfiguration(pcp));
        }
    }

    private synchronized void activeConfigurationChanged(ProjectConfiguration config) {
        listeningToCombo = false;
        try {
            configListCombo.setSelectedIndex(-1);
            if (config != null) {
                ComboBoxModel m = configListCombo.getModel();
                for (int i = 0; i < m.getSize(); i++) {
                    if (config.equals(m.getElementAt(i))) {
                        configListCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } finally {
            listeningToCombo = true;
        }
    }

    private synchronized void activeConfigurationSelected(ProjectConfiguration cfg) {
        if (pcp != null && cfg != null && !cfg.equals(getActiveConfiguration(pcp))) {
            try {
                setActiveConfiguration(pcp, cfg);
            } catch (IOException x) {
                Logger.getLogger(ActiveConfigAction.class.getName()).log(Level.WARNING, null, x);
            }
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ActiveConfigAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.label");
    }

    public void performAction() {
        assert false;
    }

    public Component getToolbarPresenter() {
        // Do not return combo box directly; looks bad.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // don't interrupt JToolBar background
        panel.setMaximumSize(new Dimension(150, 80));
        panel.setMinimumSize(new Dimension(150, 0));
        panel.setPreferredSize(new Dimension(150, 23));
        // XXX top inset of 2 looks better w/ small toolbar, but 1 seems to look better for large toolbar (the default):
        panel.add(configListCombo, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 6, 1, 5), 0, 0));
        return panel;
    }

    class ConfigMenu extends JMenu implements DynamicMenuContent {

        private final Lookup context;

        public ConfigMenu(Lookup context) {
            this.context = context;
            if (context != null) {
                Mnemonics.setLocalizedText(this, NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.context.label"));
            } else {
                Mnemonics.setLocalizedText(this, ActiveConfigAction.this.getName());
            }
        }

        public JComponent[] getMenuPresenters() {
            removeAll();
            final ProjectConfigurationProvider<?> pcp;
            if (context != null) {
                Collection<? extends Project> projects = context.lookupAll(Project.class);
                if (projects.size() == 1) {
                    pcp = projects.iterator().next().getLookup().lookup(ProjectConfigurationProvider.class);
                } else {
                    // No selection, or multiselection.
                    pcp = null;
                }
            } else {
                pcp = ActiveConfigAction.this.pcp; // global menu item; take from main project
            }
            if (pcp != null) {
                boolean something = false;
                ProjectConfiguration activeConfig = getActiveConfiguration(pcp);
                for (final ProjectConfiguration config : getConfigurations(pcp)) {
                    JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(config.getDisplayName(), config.equals(activeConfig));
                    jmi.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            activeConfigurationSelected(config);
                        }
                    });
                    add(jmi);
                    something = true;
                }
                if (pcp.hasCustomizer()) {
                    if (something) {
                        addSeparator();
                    }
                    something = true;
                    JMenuItem customize = new JMenuItem();
                    Mnemonics.setLocalizedText(customize, NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.customize"));
                    customize.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            pcp.customize();
                        }
                    });
                    add(customize);
                }
                setEnabled(something);
            } else {
                // No configurations supported for this project.
                setEnabled(false);
                // to hide entirely just use: return new JComponent[0];
            }
            return new JComponent[] {this};
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            // Always rebuild submenu.
            // For performance, could try to reuse it if context == null and nothing has changed.
            return getMenuPresenters();
        }

    }

    public JMenuItem getMenuPresenter() {
        return new ConfigMenu(null);
    }

    private static class ConfigCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ProjectConfiguration) {
                return super.getListCellRendererComponent(list, ((ProjectConfiguration) value).getDisplayName(), index, isSelected, cellHasFocus);
            } else if (value == CUSTOMIZE_ENTRY) {
                String label = org.openide.awt.Actions.cutAmpersand(NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.customize"));
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            } else {
                assert value == null;
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            }
        }
    }

    private synchronized void activeProjectChanged(Project p) {
        if (currentProject != p) {
            if (pcp != null) {
                pcp.removePropertyChangeListener(lst);
            }
            currentProject = p;
            if (currentProject != null) {
                pcp = currentProject.getLookup().lookup(ProjectConfigurationProvider.class);
                if (pcp != null) {
                    pcp.addPropertyChangeListener(lst);
                }
            } else {
                pcp = null;
            }
            configurationsListChanged(pcp == null ? null : getConfigurations(pcp));

        }
    }

    public Action createContextAwareInstance(final Lookup actionContext) {
        class A extends AbstractAction implements Presenter.Popup {
            public void actionPerformed(ActionEvent e) {
                assert false;
            }
            public JMenuItem getPopupPresenter() {
                return new ConfigMenu(actionContext);
            }
        }
        return new A();
    }

    private static Collection<? extends ProjectConfiguration> getConfigurations(final ProjectConfigurationProvider<?> pcp) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Collection<? extends ProjectConfiguration>>() {
            public Collection<? extends ProjectConfiguration> run() {
                return pcp.getConfigurations();
            }
        });
    }

    private static ProjectConfiguration getActiveConfiguration(final ProjectConfigurationProvider<?> pcp) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ProjectConfiguration>() {
            public ProjectConfiguration run() {
                return pcp.getActiveConfiguration();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void setActiveConfiguration(ProjectConfigurationProvider<?> pcp, final ProjectConfiguration pc) throws IOException {
        final ProjectConfigurationProvider _pcp = pcp;
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    _pcp.setActiveConfiguration(pc);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

}
