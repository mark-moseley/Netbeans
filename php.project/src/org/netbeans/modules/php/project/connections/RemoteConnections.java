/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnections {

    public static enum ConnectionType {
        FTP ("LBL_Ftp"); // NOI18N

        private final String label;

        private ConnectionType(String labelKey) {
            label = NbBundle.getMessage(RemoteConnections.class, labelKey);
        }

        public String getLabel() {
            return label;
        }
    }

    // XXX temporary
    public static final String DEBUG_PROPERTY = "remote.connections"; // NOI18N

    static final Logger LOGGER = Logger.getLogger(RemoteConnections.class.getName());

    private static final String PREFERENCES_PATH = "RemoteConnections"; // NOI18N

    private static final ConnectionType DEFAULT_TYPE = ConnectionType.FTP;
    private static final int DEFAULT_PORT = 21;
    private static final String DEFAULT_INITIAL_DIRECTORY = "/"; // NOI18N
    private static final String DEFAULT_PATH_SEPARATOR = "/"; // NOI18N
    private static final int DEFAULT_TIMEOUT = 30;

    static final String TYPE = "type"; // NOI18N
    static final String HOST = "host"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String USER = "user"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String ANONYMOUS_LOGIN = "anonymousLogin"; // NOI18N
    static final String INITIAL_DIRECTORY = "initialDirectory"; // NOI18N
    static final String PATH_SEPARATOR = "pathSeparator"; // NOI18N
    static final String TIMEOUT = "timeout"; // NOI18N
    static final String PASSIVE_MODE = "passiveMode"; // NOI18N

    static final String[] PROPERTIES = new String[] {
        TYPE,
        HOST,
        PORT,
        USER,
        PASSWORD,
        ANONYMOUS_LOGIN,
        INITIAL_DIRECTORY,
        PATH_SEPARATOR,
        TIMEOUT,
        PASSIVE_MODE,
    };

    private final ConfigManager configManager;
    private final ConfigManager.ConfigProvider configProvider = new DefaultConfigProvider();
    private final ChangeListener defaultChangeListener = new DefaultChangeListener();
    RemoteConnectionsPanel panel = null;
    private DialogDescriptor descriptor = null;

    public static RemoteConnections get() {
        return new RemoteConnections();
    }

    private RemoteConnections() {
        configManager = new ConfigManager(configProvider);
    }

    private void initPanel() {
        if (panel != null) {
            return;
        }
        panel = new RemoteConnectionsPanel();
        // data
        panel.setConfigurations(getConfigurations());

        // listeners
        panel.addChangeListener(defaultChangeListener);
        panel.addAddButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addConfig();
            }
        });
        panel.addRemoveButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeConfig();
            }
        });
        panel.addConfigListListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectCurrentConfig();
            }
        });
    }

    /**
     * Open the UI manager for {@link RemoteConfiguration remote configurations} (optionally,
     * the first configuration is preselected). One can easily add, remove and edit remote configuration.
     * @return <code>true</code> if there are changes in remote configurations.
     */
    public boolean openManager() {
        return openManager(null);
    }

    /**
     * Open the UI manager for {@link RemoteConfiguration remote configurations} with the preselected
     * configuration (if possible). One can easily add, remove and edit remote configuration.
     * @param configName configuration name to be preselected, can be <code>null</code>.
     * @return <code>true</code> if there are changes in remote configurations.
     */
    public boolean openManager(final RemoteConfiguration remoteConfiguration) {
        initPanel();
        String title = NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ManageRemoteConnections");
        descriptor = new DialogDescriptor(panel, title, true, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            // XXX probably not the best solution
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (panel.getConfigurations().isEmpty()) {
                        // no config available => show add config dialog
                        addConfig();
                    } else {
                        // this would need to implement hashCode() and equals() for RemoteConfiguration.... hmm, probably not needed
                        //assert getConfigurations().contains(remoteConfiguration) : "Unknow remote configration: " + remoteConfiguration;
                        if (remoteConfiguration != null) {
                            // select config
                            panel.selectConfiguration(remoteConfiguration.getName());
                        } else {
                            // select the first one
                            panel.selectConfiguration(0);
                        }
                    }
                }
            });
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        boolean changed = descriptor.getValue() == NotifyDescriptor.OK_OPTION;
        if (changed) {
            saveRemoteConnections();
        }
        return changed;
    }

    /**
     * Get the ordered list of {@link RemoteConfiguration remote configurations}. The list is order according to configuration's display
     * name (locale-sensitive string comparison).
     * @return the ordered list of remote configurations.
     * @see RemoteConfiguration
     */
    public List<RemoteConfiguration> getRemoteConfigurations() {
        // get all the configs
        List<Configuration> configs = getConfigurations();

        // convert them to remote connections
        List<RemoteConfiguration> remoteConfigs = new ArrayList<RemoteConfiguration>(configs.size());
        for (Configuration cfg : configs) {
            remoteConfigs.add(new RemoteConfiguration(cfg));
        }
        return Collections.unmodifiableList(remoteConfigs);
    }

    /**
     * Get the {@link RemoteConfiguration remote configuration} for the given name (<b>NOT</b> the display name).
     * @param name the name of the configuration.
     * @return the {@link RemoteConfiguration remote configuration} for the given name or <code>null</code> if not found.
     */
    public RemoteConfiguration remoteConfigurationForName(String name) {
        assert name != null;
        for (RemoteConfiguration remoteConfig : getRemoteConfigurations()) {
            if (remoteConfig.getName().equals(name)) {
                return remoteConfig;
            }
        }
        return null;
    }

    private List<Configuration> getConfigurations() {
        Collection<String> cfgNames = configManager.configurationNames();
        List<Configuration> configs = new ArrayList<Configuration>(cfgNames.size() - 1); // without default config

        for (String name : cfgNames) {
            if (name == null) {
                // default config
                continue;
            }
            Configuration cfg = configManager.configurationFor(name);
            if (cfg == null) {
                // deleted configuration
                continue;
            }
            configs.add(cfg);
        }
        Collections.sort(configs, ConfigManager.getConfigurationComparator());
        return configs;
    }

    void addConfig() {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(NbBundle.getMessage(RemoteConnections.class, "LBL_ConnectionName"),
                NbBundle.getMessage(RemoteConnections.class, "LBL_CreateNewConnection"));

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
            String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N

            String err = null;
            if (name.trim().length() == 0) {
                err = NbBundle.getMessage(RemoteConnections.class, "MSG_EmptyConnectionExists");
            } else if (configManager.exists(config)) {
                err = NbBundle.getMessage(RemoteConnections.class, "MSG_ConnectionExists", config);
            }
            if (err != null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(err, NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            Configuration cfg = configManager.createNew(config, name);
            cfg.putValue(PORT, String.valueOf(DEFAULT_PORT));
            cfg.putValue(INITIAL_DIRECTORY, DEFAULT_INITIAL_DIRECTORY);
            cfg.putValue(PATH_SEPARATOR, DEFAULT_PATH_SEPARATOR);
            cfg.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
            panel.addConfiguration(cfg);
            configManager.markAsCurrentConfiguration(config);
        }
    }

    void removeConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        assert cfg != null;
        configManager.configurationFor(cfg.getName()).delete();
        panel.removeConfiguration(cfg); // this will change the current selection in the list => selectCurrentConfig() is called
    }

    void selectCurrentConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();

        // unregister default listener (validate() would be called soooo many times)
        panel.removeChangeListener(defaultChangeListener);

        // change the state of the fields
        panel.setEnabledFields(cfg != null);

        if (cfg != null) {
            configManager.markAsCurrentConfiguration(cfg.getName());

            panel.setConnectionName(cfg.getDisplayName());
            panel.setType(resolveType(cfg.getValue(TYPE)));
            panel.setHostName(cfg.getValue(HOST));
            panel.setPort(cfg.getValue(PORT));
            panel.setUserName(cfg.getValue(USER));
            panel.setPassword(cfg.getValue(PASSWORD));
            panel.setAnonymousLogin(resolveBoolean(cfg.getValue(ANONYMOUS_LOGIN)));
            panel.setInitialDirectory(cfg.getValue(INITIAL_DIRECTORY));
            panel.setTimeout(cfg.getValue(TIMEOUT));
            panel.setPassiveMode(resolveBoolean(cfg.getValue(PASSIVE_MODE)));
        } else {
            panel.resetFields();
        }
        // register default listener
        panel.addChangeListener(defaultChangeListener);

        if (cfg != null) {
            // validate fields only if there's valid config
            validate();
        }
    }

    void validate() {
        assert panel != null;
        // remember password is dangerous
        // just warning - do it every time
        if (validateRememberPassword()) {
            setWarning(null);
        }

        if (!validateHost()) {
            return;
        }

        if (!validatePort()) {
            return;
        }

        if (!validateUser()) {
            return;
        }

        if (!validatePathSeparator()) {
            return;
        }

        if (!validateTimeout()) {
            return;
        }

        // everything ok
        setError(null);

        // check whether all the configs are errorless
        checkAllTheConfigs();
    }

    private boolean validateHost() {
        if (panel.getHostName().trim().length() == 0) {
            setError("MSG_NoHostName");
            return false;
        }
        return true;
    }

    private boolean validatePort() {
        String err = null;
        try {
            int port = Integer.parseInt(panel.getPort());
            if (port < 1) {
                err = "MSG_PortNotPositive"; // NOI18N
            }
        } catch (NumberFormatException nfe) {
            err = "MSG_PortNotNumeric"; // NOI18N
        }
        setError(err);
        return err == null;
    }

    private boolean validateUser() {
        if (panel.isAnonymousLogin()) {
            return true;
        }
        if (panel.getUserName().trim().length() == 0) {
            setError("MSG_NoUserName");
            return false;
        }
        return true;
    }

    private boolean validatePathSeparator() {
        if (panel.getHostName().trim().length() == 0) {
            setError("MSG_NoPathSeparator");
            return false;
        }
        return true;
    }

    private boolean validateTimeout() {
        String err = null;
        try {
            int timeout = Integer.parseInt(panel.getTimeout());
            if (timeout < 0) {
                err = "MSG_TimeoutNotPositive"; // NOI18N
            }
        } catch (NumberFormatException nfe) {
            err = "MSG_TimeoutNotNumeric"; // NOI18N
        }
        setError(err);
        return err == null;
    }

    private boolean validateRememberPassword() {
        if (panel.getPassword().length() > 0) {
            setWarning("MSG_PasswordRememberDangerous"); // NOI18N
            return false;
        }
        return true;
    }

    private void checkAllTheConfigs() {
        for (Configuration cfg : panel.getConfigurations()) {
            assert cfg != null;
            if (!cfg.isValid()) {
                panel.setError(NbBundle.getMessage(RemoteConnections.class, "MSG_InvalidConfiguration", cfg.getDisplayName()));
                assert descriptor != null;
                descriptor.setValid(false);
                return;
            }
        }
    }

    private void setError(String errorKey) {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        String err = errorKey != null ? NbBundle.getMessage(RemoteConnections.class, errorKey) : null;
        cfg.setErrorMessage(err);
        panel.setError(err);
        assert descriptor != null;
        descriptor.setValid(err == null);
    }

    private void setWarning(String errorKey) {
        assert panel != null;
        panel.setWarning(errorKey != null ? NbBundle.getMessage(RemoteConnections.class, errorKey) : null);
    }

    private void updateActiveConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        if (cfg == null) {
            // no config selected
            return;
        }
        cfg.putValue(TYPE, panel.getType().name());
        cfg.putValue(HOST, panel.getHostName());
        cfg.putValue(PORT, panel.getPort());
        cfg.putValue(USER, panel.getUserName());
        cfg.putValue(PASSWORD, panel.getPassword());
        cfg.putValue(ANONYMOUS_LOGIN, String.valueOf(panel.isAnonymousLogin()));
        cfg.putValue(INITIAL_DIRECTORY, panel.getInitialDirectory());
        cfg.putValue(PATH_SEPARATOR, DEFAULT_PATH_SEPARATOR);
        cfg.putValue(TIMEOUT, panel.getTimeout());
        cfg.putValue(PASSIVE_MODE, String.valueOf(panel.isPassiveMode()));
    }

    private void saveRemoteConnections() {
        Preferences remoteConnections = NbPreferences.forModule(RemoteConnections.class).node(PREFERENCES_PATH);
        for (Map.Entry<String, Map<String, String>> entry : configProvider.getConfigs().entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                // no default config
                continue;
            }
            Map<String, String> cfg = entry.getValue();
            if (cfg == null) {
                // config was deleted
                try {
                    remoteConnections.node(config).removeNode();
                } catch (BackingStoreException bse) {
                    LOGGER.log(Level.INFO, "Error while removing unused remote connection: " + config, bse);
                }
            } else {
                // add/update
                Preferences node = remoteConnections.node(config);
                for (Map.Entry<String, String> cfgEntry : cfg.entrySet()) {
                    node.put(cfgEntry.getKey(), cfgEntry.getValue());
                }
            }
        }
    }

    private ConnectionType resolveType(String type) {
        if (type == null) {
            return DEFAULT_TYPE;
        }
        ConnectionType connectionType = null;
        try {
            connectionType = ConnectionType.valueOf(type);
        } catch (IllegalArgumentException iae) {
            connectionType = DEFAULT_TYPE;
        }
        return connectionType;
    }

    private boolean resolveBoolean(String value) {
        return Boolean.valueOf(value);
    }

    private class DefaultConfigProvider implements ConfigManager.ConfigProvider {
        final Map<String, Map<String, String>> configs;

        public DefaultConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
            readConfigs();
        }

        public String[] getConfigProperties() {
            return PROPERTIES;
        }

        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }

        public String getActiveConfig() {
            return null;
        }

        public void setActiveConfig(String configName) {
        }

        private void readConfigs() {
            Preferences remoteConnections = NbPreferences.forModule(RemoteConnections.class).node(PREFERENCES_PATH);
            try {
                for (String name : remoteConnections.childrenNames()) {
                    Preferences node = remoteConnections.node(name);
                    Map<String, String> value = new TreeMap<String, String>();
                    for (String key : node.keys()) {
                        value.put(key, node.get(key, null));
                    }
                    configs.put(name, value);
                }
            } catch (BackingStoreException bse) {
                LOGGER.log(Level.INFO, "Error while reading existing remote connections", bse);
            }
        }
    }

    private class DefaultChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateActiveConfig();
            validate();
        }
    }
}
