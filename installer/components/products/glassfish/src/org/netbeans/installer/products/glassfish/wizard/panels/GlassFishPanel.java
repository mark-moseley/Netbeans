/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.products.glassfish.wizard.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiComboBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiPasswordField;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationValidator;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxEditor;
import org.netbeans.installer.wizard.components.panels.ApplicationLocationPanel.LocationsComboBoxModel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelSwingUi;
import org.netbeans.installer.wizard.components.panels.DestinationPanel.DestinationPanelUi;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi.ValidatingDocumentListener;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import static java.lang.Integer.parseInt;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiFileChooser;

/**
 *
 * @author Kirill Sorokin
 */
public class GlassFishPanel extends DestinationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JdkLocationPanel jdkLocationPanel;
    
    public GlassFishPanel() {
        jdkLocationPanel = new JdkLocationPanel();
        
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        
        setProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_JDK_LOCATION_LABEL_TEXT);
        setProperty(BROWSE_BUTTON_TEXT_PROPERTY,
                DEFAULT_BROWSE_BUTTON_TEXT);
        
        setProperty(USERNAME_LABEL_TEXT_PROPERTY,
                DEFAULT_USERNAME_LABEL_TEXT);
        setProperty(PASSWORD_LABEL_TEXT_PROPERTY,
                DEFAULT_PASSWORD_LABEL_TEXT);
        setProperty(REPEAT_PASSWORD_LABEL_TEXT_PROPERTY,
                DEFAULT_REPEAT_PASSWORD_LABEL_TEXT);
        setProperty(DEFAULTS_LABEL_TEXT_PROPERTY,
                DEFAULT_DEFAULTS_LABEL_TEXT);
        setProperty(HTTP_LABEL_TEXT_PROPERTY,
                DEFAULT_HTTP_LABEL_TEXT);
        setProperty(HTTPS_LABEL_TEXT_PROPERTY,
                DEFAULT_HTTPS_LABEL_TEXT);
        setProperty(ADMIN_LABEL_TEXT_PROPERTY,
                DEFAULT_ADMIN_LABEL_TEXT);
        
        setProperty(ERROR_USERNAME_NULL_PROPERTY,
                DEFAULT_ERROR_USERNAME_NULL);
        setProperty(ERROR_USERNAME_NOT_ALNUM_PROPERTY,
                DEFAULT_ERROR_USERNAME_NOT_ALNUM);
        setProperty(ERROR_PASSWORD_NULL_PROPERTY,
                DEFAULT_ERROR_PASSWORD_NULL);
        setProperty(ERROR_PASSWORD_TOO_SHORT_PROPERTY,
                DEFAULT_ERROR_PASSWORD_TOO_SHORT);
        setProperty(ERROR_PASSWORD_SPACES_PROPERTY,
                DEFAULT_ERROR_PASSWORD_SPACES);
        setProperty(ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY,
                DEFAULT_ERROR_PASSWORDS_DO_NOT_MATCH);
        setProperty(ERROR_ALL_PORTS_OCCUPIED_PROPERTY,
                DEFAULT_ERROR_ALL_PORTS_OCCUPIED);
        setProperty(ERROR_HTTP_NULL_PROPERTY,
                DEFAULT_ERROR_HTTP_NULL);
        setProperty(ERROR_HTTPS_NULL_PROPERTY,
                DEFAULT_ERROR_HTTPS_NULL);
        setProperty(ERROR_ADMIN_NULL_PROPERTY,
                DEFAULT_ERROR_ADMIN_NULL);
        setProperty(ERROR_HTTP_NOT_INTEGER_PROPERTY,
                DEFAULT_ERROR_HTTP_NOT_INTEGER);
        setProperty(ERROR_HTTPS_NOT_INTEGER_PROPERTY,
                DEFAULT_ERROR_HTTPS_NOT_INTEGER);
        setProperty(ERROR_ADMIN_NOT_INTEGER_PROPERTY,
                DEFAULT_ERROR_ADMIN_NOT_INTEGER);
        setProperty(ERROR_HTTP_NOT_IN_RANGE_PROPERTY,
                DEFAULT_ERROR_HTTP_NOT_IN_RANGE);
        setProperty(ERROR_HTTPS_NOT_IN_RANGE_PROPERTY,
                DEFAULT_ERROR_HTTPS_NOT_IN_RANGE);
        setProperty(ERROR_ADMIN_NOT_IN_RANGE_PROPERTY,
                DEFAULT_ERROR_ADMIN_NOT_IN_RANGE);
        setProperty(ERROR_HTTP_OCCUPIED_PROPERTY,
                DEFAULT_ERROR_HTTP_OCCUPIED);
        setProperty(ERROR_HTTPS_OCCUPIED_PROPERTY,
                DEFAULT_ERROR_HTTPS_OCCUPIED);
        setProperty(ERROR_ADMIN_OCCUPIED_PROPERTY,
                DEFAULT_ERROR_ADMIN_OCCUPIED);
        setProperty(ERROR_HTTP_EQUALS_HTTPS_PROPERTY,
                DEFAULT_ERROR_HTTP_EQUALS_HTTPS);
        setProperty(ERROR_HTTP_EQUALS_ADMIN_PROPERTY,
                DEFAULT_ERROR_HTTP_EQUALS_ADMIN);
        setProperty(ERROR_HTTPS_EQUALS_ADMIN_PROPERTY,
                DEFAULT_ERROR_HTTPS_EQUALS_ADMIN);
        
        setProperty(WARNING_PORT_IN_USE_PROPERTY,
                DEFAULT_WARNING_PORT_IN_USE);
        setProperty(WARNING_ASADMIN_FILES_EXIST_PROPERTY,
                DEFAULT_WARNING_ASADMIN_FILES_EXIST);
        
        setProperty(DEFAULT_USERNAME_PROPERTY,
                DEFAULT_DEFAULT_USERNAME);
        setProperty(DEFAULT_PASSWORD_PROPERTY,
                DEFAULT_DEFAULT_PASSWORD);
        setProperty(DEFAULT_HTTP_PORT_PROPERTY,
                DEFAULT_DEFAULT_HTTP_PORT);
        setProperty(DEFAULT_HTTPS_PORT_PROPERTY,
                DEFAULT_DEFAULT_HTTPS_PORT);
        setProperty(DEFAULT_ADMIN_PORT_PROPERTY,
                DEFAULT_DEFAULT_ADMIN_PORT);
        
        setProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MINIMUM_JDK_VERSION);
        setProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MAXIMUM_JDK_VERSION);
        setProperty(JdkLocationPanel.VENDOR_JDK_ALLOWED_PROPERTY,
                SystemUtils.isMacOS() ? 
                    DEFAULT_VENDOR_JDK_ALLOWED_MACOSX : 
                    DEFAULT_VENDOR_JDK_ALLOWED);        
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new GlassFishPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        jdkLocationPanel.setWizard(getWizard());
        
        jdkLocationPanel.setProperty(
                JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY,
                getProperty(JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
        jdkLocationPanel.setProperty(
                JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY,
                getProperty(JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));
        jdkLocationPanel.setProperty(
                JdkLocationPanel.VENDOR_JDK_ALLOWED_PROPERTY,
                getProperty(JdkLocationPanel.VENDOR_JDK_ALLOWED_PROPERTY));
        
        if (getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY) != null) {
            jdkLocationPanel.setProperty(
                    JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY,
                    getProperty(JdkLocationPanel.PREFERRED_JDK_VERSION_PROPERTY));
        }
        
        jdkLocationPanel.initialize();
    }
    
    public JdkLocationPanel getJdkLocationPanel() {
        return jdkLocationPanel;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class GlassFishPanelUi extends DestinationPanelUi {
        protected GlassFishPanel component;
        
        public GlassFishPanelUi(GlassFishPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new GlassFishPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class GlassFishPanelSwingUi extends DestinationPanelSwingUi {
        protected GlassFishPanel panel;
        
        private NbiPanel containerPanel;
        
        private NbiLabel jdkLocationLabel;
        private NbiComboBox jdkLocationComboBox;
        private NbiButton browseButton;
        private NbiLabel statusLabel;
        
        private NbiTextField jdkLocationField;
        
        private NbiDirectoryChooser fileChooser;
        
        private NbiLabel usernameLabel;
        private NbiTextField usernameField;
        
        private NbiLabel passwordLabel;
        private NbiPasswordField passwordField;
        
        private NbiLabel defaultsLabel;
        
        private NbiLabel repeatPasswordLabel;
        private NbiPasswordField repeatPasswordField;
        
        private NbiLabel httpPortLabel;
        private NbiTextField httpPortField;
        
        private NbiLabel httpsPortLabel;
        private NbiTextField httpsPortField;
        
        private NbiLabel adminPortLabel;
        private NbiTextField adminPortField;
        
        private boolean allPortsOccupied;
        
        public GlassFishPanelSwingUi(
                final GlassFishPanel panel,
                final SwingContainer container) {
            super(panel, container);
            
            this.panel = panel;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            jdkLocationLabel.setText(
                    panel.getProperty(JDK_LOCATION_LABEL_TEXT_PROPERTY));
            
            final JdkLocationPanel jdkLocationPanel = panel.getJdkLocationPanel();
            
            if (jdkLocationPanel.getLocations().size() == 0) {
                final Version minVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MINIMUM_JDK_VERSION_PROPERTY));
                final Version maxVersion = Version.getVersion(jdkLocationPanel.getProperty(
                        JdkLocationPanel.MAXIMUM_JDK_VERSION_PROPERTY));
                
                statusLabel.setText(StringUtils.format(
                        jdkLocationPanel.getProperty(JdkLocationPanel.ERROR_NOTHING_FOUND_PROPERTY),
                        minVersion.toJdkStyle(),
                        minVersion.toJdkStyle()));
            } else {
                statusLabel.clearText();
                statusLabel.setVisible(false);
            }

            final List<File> jdkLocations = jdkLocationPanel.getLocations();                        
            final List<String> jdkLabels = jdkLocationPanel.getLabels();
            
            final LocationsComboBoxModel model = new LocationsComboBoxModel(
                    jdkLocations,
                    jdkLabels);            
            
            ((LocationsComboBoxEditor) jdkLocationComboBox.getEditor()).setModel(
                    model);
            jdkLocationComboBox.setModel(
                    model);
            
            final File selectedLocation = jdkLocationPanel.getSelectedLocation();
            final int index = jdkLocations.indexOf(selectedLocation);
            String selectedItem;
            if(index != -1) {
                  selectedItem = jdkLabels.get(index);  
            } else {
                  selectedItem = selectedLocation.toString();
            }  
            model.setSelectedItem(selectedItem);                                                       
            browseButton.setText(
                    panel.getProperty(BROWSE_BUTTON_TEXT_PROPERTY));
            
            final String defaultUsername =
                    panel.getProperty(DEFAULT_USERNAME_PROPERTY);
            final String defaultPassword =
                    panel.getProperty(DEFAULT_PASSWORD_PROPERTY);
            
            final int defaultHttpPort = SystemUtils.getAvailablePort(
                    parseInt(
                    panel.getProperty(DEFAULT_HTTP_PORT_PROPERTY)));
            final int defaultHttpsPort = SystemUtils.getAvailablePort(
                    parseInt(panel.getProperty(DEFAULT_HTTPS_PORT_PROPERTY)),
                    defaultHttpPort);
            final int defaultAdminPort = SystemUtils.getAvailablePort(
                    parseInt(panel.getProperty(DEFAULT_ADMIN_PORT_PROPERTY)),
                    defaultHttpPort,
                    defaultHttpsPort);
            
            usernameLabel.setText(
                    panel.getProperty(USERNAME_LABEL_TEXT_PROPERTY));
            passwordLabel.setText(
                    panel.getProperty(PASSWORD_LABEL_TEXT_PROPERTY));
            repeatPasswordLabel.setText(
                    panel.getProperty(REPEAT_PASSWORD_LABEL_TEXT_PROPERTY));
            httpPortLabel.setText(
                    panel.getProperty(HTTP_LABEL_TEXT_PROPERTY));
            httpsPortLabel.setText(
                    panel.getProperty(HTTPS_LABEL_TEXT_PROPERTY));
            adminPortLabel.setText(
                    panel.getProperty(ADMIN_LABEL_TEXT_PROPERTY));
            
            String username = panel.getWizard().getProperty(
                    USERNAME_PROPERTY);
            if (username == null) {
                username = defaultUsername;
            }
            usernameField.setText(username);
            
            String password = panel.getWizard().getProperty(
                    PASSWORD_PROPERTY);
            if (password == null) {
                password = defaultPassword;
            }
            passwordField.setText(password);
            repeatPasswordField.setText(password);
            
            defaultsLabel.setText(StringUtils.format(
                    panel.getProperty(DEFAULTS_LABEL_TEXT_PROPERTY),
                    defaultUsername,
                    defaultPassword));
            
            String httpPort = panel.getWizard().getProperty(
                    HTTP_PORT_PROPERTY);
            if (httpPort == null) {
                if (defaultHttpPort != -1) {
                    httpPort = Integer.toString(defaultHttpPort);
                    allPortsOccupied = false;
                } else {
                    httpPort = StringUtils.EMPTY_STRING;
                    allPortsOccupied = true;
                }
            }
            httpPortField.setText(httpPort);
            
            String httpsPort = panel.getWizard().getProperty(
                    HTTPS_PORT_PROPERTY);
            if (httpsPort == null) {
                if (defaultHttpsPort != -1) {
                    httpsPort = Integer.toString(defaultHttpsPort);
                    allPortsOccupied = false;
                } else {
                    httpsPort = StringUtils.EMPTY_STRING;
                    allPortsOccupied = true;
                }
            }
            httpsPortField.setText(httpsPort);
            
            String adminPort = panel.getWizard().getProperty(
                    ADMIN_PORT_PROPERTY);
            if (adminPort == null) {
                if (defaultAdminPort != -1) {
                    adminPort = Integer.toString(defaultAdminPort);
                    allPortsOccupied = false;
                } else {
                    adminPort = StringUtils.EMPTY_STRING;
                    allPortsOccupied = true;
                }
            }
            adminPortField.setText(adminPort);
                        
            super.initialize();
        }
        
        @Override
        protected void saveInput() {
            super.saveInput();
            
            panel.getJdkLocationPanel().setLocation(
                    new File(jdkLocationField.getText()));
            
            panel.getWizard().setProperty(
                    USERNAME_PROPERTY,
                    usernameField.getText());
            panel.getWizard().setProperty(
                    PASSWORD_PROPERTY,
                    new String(passwordField.getPassword()));
            
            panel.getWizard().setProperty(
                    HTTP_PORT_PROPERTY,
                    httpPortField.getText());
            panel.getWizard().setProperty(
                    HTTPS_PORT_PROPERTY,
                    httpsPortField.getText());
            panel.getWizard().setProperty(
                    ADMIN_PORT_PROPERTY,
                    adminPortField.getText());
        }
        
        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();
            
            if (errorMessage == null) {
                errorMessage = panel.getJdkLocationPanel().validateLocation(
                        jdkLocationField.getText());
            }
            
            if (errorMessage != null) {
                return errorMessage;
            }
            
            final String username = usernameField.getText();
            final String password = new String(passwordField.getPassword());
            final String password2 = new String(repeatPasswordField.getPassword());
            final String httpPort = httpPortField.getText().trim();
            final String httpsPort = httpsPortField.getText().trim();
            final String adminPort = adminPortField.getText().trim();
            
            if ((username == null) || username.trim().equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_USERNAME_NULL_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!username.matches("[0-9a-zA-Z]+")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_USERNAME_NOT_ALNUM_PROPERTY),
                        username,
                        password,
                        password2);
            }
            
            if ((password == null) || password.trim().equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_NULL_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (password.length() < 8) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_TOO_SHORT_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!password.equals(password2)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY),
                        username,
                        password,
                        password2);
            }
            if (!password.trim().equals(password)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_PASSWORD_SPACES_PROPERTY),
                        username,
                        password,
                        password2);
            }
            
            if ((httpPort.equals("") || httpsPort.equals("") || adminPort.equals("")) && allPortsOccupied) {
                return panel.getProperty(ERROR_ALL_PORTS_OCCUPIED_PROPERTY);
            }
            
            if ((httpPort == null) || httpPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NULL_PROPERTY),
                        httpPort);
            }
            if (!httpPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NOT_INTEGER_PROPERTY),
                        httpPort);
            }
            int port = new Integer(httpPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_NOT_IN_RANGE_PROPERTY),
                        httpPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_OCCUPIED_PROPERTY),
                        httpPort);
            }
            
            if ((httpsPort == null) || httpsPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NULL_PROPERTY),
                        httpsPort);
            }
            if (!httpsPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NOT_INTEGER_PROPERTY),
                        httpsPort);
            }
            port = new Integer(httpsPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_NOT_IN_RANGE_PROPERTY),
                        httpsPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_OCCUPIED_PROPERTY),
                        httpsPort);
            }
            
            if ((adminPort == null) || adminPort.equals("")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NULL_PROPERTY),
                        adminPort);
            }
            if (!adminPort.matches("(0|[1-9][0-9]*)")) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NOT_INTEGER_PROPERTY),
                        adminPort);
            }
            port = new Integer(adminPort);
            if ((port < 0) || (port > 65535)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_NOT_IN_RANGE_PROPERTY),
                        adminPort);
            }
            if (!SystemUtils.isPortAvailable(port)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_ADMIN_OCCUPIED_PROPERTY),
                        adminPort);
            }
            
            if (httpPort.equals(httpsPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_EQUALS_HTTPS_PROPERTY),
                        httpPort,
                        httpsPort);
            }
            if (httpPort.equals(adminPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTP_EQUALS_ADMIN_PROPERTY),
                        httpPort,
                        adminPort);
            }
            if (httpsPort.equals(adminPort)) {
                return StringUtils.format(
                        panel.getProperty(ERROR_HTTPS_EQUALS_ADMIN_PROPERTY),
                        httpsPort, adminPort);
            }
            
            return null;
        }
        
        @Override
        protected String getWarningMessage() {
            // check whether the selected ports are already in use by any other
            // installed application server (SJSAS or GlassFish)
            final RegistryFilter filter = new OrFilter(
                    new ProductFilter("glassfish", SystemUtils.getCurrentPlatform()),
                    new ProductFilter("sjsas", SystemUtils.getCurrentPlatform()));
            final List<Product> products =
                    Registry.getInstance().queryProducts(filter);
            
            final int httpPort = Integer.parseInt(httpPortField.getText().trim());
            final int httpsPort = Integer.parseInt(httpsPortField.getText().trim());
            final int adminPort = Integer.parseInt(adminPortField.getText().trim());
            
            try {
                for (Product product: products) {
                    if (product.getStatus() == Status.INSTALLED) {
                        final File location = product.getInstallationLocation();
                        
                        for (String domainName: GlassFishUtils.getDomainNames(location)) {
                            int port = GlassFishUtils.getHttpPort(location, domainName);
                            if ((port == httpPort) ||
                                    (port == httpsPort) ||
                                    (port == adminPort)) {
                                return StringUtils.format(
                                        panel.getProperty(WARNING_PORT_IN_USE_PROPERTY),
                                        StringUtils.EMPTY_STRING + port,
                                        product);
                            }
                            
                            port = GlassFishUtils.getHttpsPort(location, domainName);
                            if ((port == httpPort) ||
                                    (port == httpsPort) ||
                                    (port == adminPort)) {
                                return StringUtils.format(
                                        panel.getProperty(WARNING_PORT_IN_USE_PROPERTY),
                                        StringUtils.EMPTY_STRING + port,
                                        product);
                            }
                            
                            port = GlassFishUtils.getAdminPort(location, domainName);
                            if ((port == httpPort) ||
                                    (port == httpsPort) ||
                                    (port == adminPort)) {
                                return StringUtils.format(
                                        panel.getProperty(WARNING_PORT_IN_USE_PROPERTY),
                                        StringUtils.EMPTY_STRING + port,
                                        product);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                ErrorManager.notifyDebug("Failed to get the port value.", e);
            } catch (XMLException e) {
                ErrorManager.notifyDebug("Failed to get the port value.", e);
            }
            
            // check whether the .asadminpass and .asadmintruststore file exist 
            // in the user's home directory
            final File asadminpass = new File(
                    SystemUtils.getUserHomeDirectory(), 
                    ".asadminpass");;
            final File asadmintruststore = new File(
                    SystemUtils.getUserHomeDirectory(), 
                    ".asadmintruststore");
            if (asadminpass.exists() || asadmintruststore.exists()) {
                return panel.getProperty(WARNING_ASADMIN_FILES_EXIST_PROPERTY);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // containerPanel ///////////////////////////////////////////////////////
            containerPanel = new NbiPanel();
            
            // selectedLocationField ////////////////////////////////////////////////
            jdkLocationField = new NbiTextField();
            jdkLocationField.getDocument().addDocumentListener(
                    new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
                
                public void removeUpdate(DocumentEvent e) {
                    //updateErrorMessage();
                }
                
                public void changedUpdate(DocumentEvent e) {
                    updateErrorMessage();
                }
            });
            
            // jdkLocationComboBox //////////////////////////////////////////////////
            final LocationValidator validator = new LocationValidator() {
                public void validate(String location) {
                    jdkLocationField.setText(location);
                }
            };
            
            jdkLocationComboBox = new NbiComboBox();
            jdkLocationComboBox.setEditable(true);
            jdkLocationComboBox.setEditor(new LocationsComboBoxEditor(validator));
            jdkLocationComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    final ComboBoxModel model = jdkLocationComboBox.getModel();
                    
                    if (model instanceof LocationsComboBoxModel) {
                        jdkLocationField.setText(
                                ((LocationsComboBoxModel) model).getLocation());
                    }
                }
            });
            
            // jdkLocationLabel /////////////////////////////////////////////////////
            jdkLocationLabel = new NbiLabel();
            jdkLocationLabel.setLabelFor(jdkLocationComboBox);
            
            // browseButton /////////////////////////////////////////////////////////
            browseButton = new NbiButton();
            browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    browseButtonPressed();
                }
            });
            
            // statusLabel //////////////////////////////////////////////////////////
            statusLabel = new NbiLabel();
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();
                        
            final Dimension longFieldSize = new Dimension(
                    200,
                    new NbiTextField().getPreferredSize().height);
            final Dimension shortFieldSize = new Dimension(
                    80,
                    longFieldSize.height);
            
            // usernameField ////////////////////////////////////////////////////////
            usernameField = new NbiTextField();
            usernameField.setPreferredSize(longFieldSize);
            usernameField.setMinimumSize(longFieldSize);
            usernameField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // usernameLabel ////////////////////////////////////////////////////////
            usernameLabel = new NbiLabel();
            usernameLabel.setLabelFor(usernameField);
            
            // passwordField ////////////////////////////////////////////////////////
            passwordField = new NbiPasswordField();
            passwordField.setPreferredSize(longFieldSize);
            passwordField.setMinimumSize(longFieldSize);
            passwordField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // passwordLabel ////////////////////////////////////////////////////////
            passwordLabel = new NbiLabel();
            passwordLabel.setLabelFor(passwordField);
            
            // repeatPasswordField //////////////////////////////////////////////////
            repeatPasswordField = new NbiPasswordField();
            repeatPasswordField.setPreferredSize(longFieldSize);
            repeatPasswordField.setMinimumSize(longFieldSize);
            repeatPasswordField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // repeatPasswordLabel //////////////////////////////////////////////////
            repeatPasswordLabel = new NbiLabel();
            repeatPasswordLabel.setLabelFor(repeatPasswordField);
            
            // httpPortField ////////////////////////////////////////////////////////
            httpPortField = new NbiTextField();
            httpPortField.setPreferredSize(shortFieldSize);
            httpPortField.setMinimumSize(shortFieldSize);
            httpPortField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // httpPortLabel ////////////////////////////////////////////////////////
            httpPortLabel = new NbiLabel();
            httpPortLabel.setLabelFor(httpPortField);
            
            // httpsPortField ///////////////////////////////////////////////////////
            httpsPortField = new NbiTextField();
            httpsPortField.setPreferredSize(shortFieldSize);
            httpsPortField.setMinimumSize(shortFieldSize);
            httpsPortField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // httpsPortLabel ///////////////////////////////////////////////////////
            httpsPortLabel = new NbiLabel();
            httpsPortLabel.setLabelFor(httpsPortField);
            
            // adminPortField ///////////////////////////////////////////////////////
            adminPortField = new NbiTextField();
            adminPortField.setPreferredSize(shortFieldSize);
            adminPortField.setMinimumSize(shortFieldSize);
            adminPortField.getDocument().addDocumentListener(
                    new ValidatingDocumentListener(this));
            
            // adminPortLabel ///////////////////////////////////////////////////////
            adminPortLabel = new NbiLabel();
            adminPortLabel.setLabelFor(adminPortField);
            
            // defaultsLabel ////////////////////////////////////////////////////////
            defaultsLabel = new NbiLabel();
            
            // this /////////////////////////////////////////////////////////////////
            add(jdkLocationLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(jdkLocationComboBox, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(browseButton, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 4, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            add(statusLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(containerPanel, new GridBagConstraints(
                    0, 5,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            // containerPanel ///////////////////////////////////////////////////////
            containerPanel.add(usernameLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(usernameField, new GridBagConstraints(
                    1, 0,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(passwordLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(passwordField, new GridBagConstraints(
                    1, 1,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(defaultsLabel, new GridBagConstraints(
                    3, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 0, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(repeatPasswordLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(repeatPasswordField, new GridBagConstraints(
                    1, 2,                             // x, y
                    2, 1,                             // width, height
                    0.5, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(httpPortLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(httpPortField, new GridBagConstraints(
                    1, 3,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 3,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(httpsPortLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(httpsPortField, new GridBagConstraints(
                    1, 4,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 4,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            containerPanel.add(adminPortLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(adminPortField, new GridBagConstraints(
                    1, 5,                             // x, y
                    1, 1,                             // width, height
                    0.1, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    2, 5,                             // x, y
                    1, 1,                             // width, height
                    0.4, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            containerPanel.add(new NbiPanel(), new GridBagConstraints(
                    3, 5,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(jdkLocationField.getText()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jdkLocationComboBox.getModel().setSelectedItem(
                        fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.description"); // NOI18N
    
    public static final String USERNAME_PROPERTY =
            "username"; // NOI18N
    public static final String PASSWORD_PROPERTY =
            "password"; // NOI18N
    public static final String HTTP_PORT_PROPERTY =
            "http.port"; // NOI18N
    public static final String HTTPS_PORT_PROPERTY =
            "https.port"; // NOI18N
    public static final String ADMIN_PORT_PROPERTY =
            "admin.port"; // NOI18N
    
    public static final String JDK_LOCATION_LABEL_TEXT_PROPERTY =
            "jdk.location.label.text"; // NOI18N
    public static final String BROWSE_BUTTON_TEXT_PROPERTY =
            "browse.button.text"; // NOI18N
    public static final String USERNAME_LABEL_TEXT_PROPERTY =
            "username.label.text"; // NOI18N
    public static final String PASSWORD_LABEL_TEXT_PROPERTY =
            "password.label.text"; // NOI18N
    public static final String REPEAT_PASSWORD_LABEL_TEXT_PROPERTY =
            "repeat.password.label.text"; // NOI18N
    public static final String HTTP_LABEL_TEXT_PROPERTY =
            "http.label.text"; // NOI18N
    public static final String HTTPS_LABEL_TEXT_PROPERTY =
            "https.label.text"; // NOI18N
    public static final String ADMIN_LABEL_TEXT_PROPERTY =
            "admin.label.text"; // NOI18N
    public static final String DEFAULTS_LABEL_TEXT_PROPERTY =
            "defaults.label.text"; // NOI18N
    
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.destination.button.text"); // NOI18N
    
    public static final String DEFAULT_JDK_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.jdk.location.label.text"); // NOI18N
    public static final String DEFAULT_BROWSE_BUTTON_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.browse.button.text"); // NOI18N
    public static final String DEFAULT_USERNAME_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.username.label.text"); // NOI18N
    public static final String DEFAULT_PASSWORD_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.password.label.text"); // NOI18N
    public static final String DEFAULT_REPEAT_PASSWORD_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.repeat.password.label.text"); // NOI18N
    public static final String DEFAULT_HTTP_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.http.label.text"); // NOI18N
    public static final String DEFAULT_HTTPS_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.https.label.text"); // NOI18N
    public static final String DEFAULT_ADMIN_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.admin.label.text"); // NOI18N
    public static final String DEFAULT_DEFAULTS_LABEL_TEXT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.defaults.label.text"); // NOI18N
    
    public static final String DEFAULT_USERNAME_PROPERTY =
            "default.username"; // NOI18N
    public static final String DEFAULT_PASSWORD_PROPERTY =
            "default.password"; // NOI18N
    public static final String DEFAULT_HTTP_PORT_PROPERTY =
            "default.http.port"; // NOI18N
    public static final String DEFAULT_HTTPS_PORT_PROPERTY =
            "default.https.port"; // NOI18N
    public static final String DEFAULT_ADMIN_PORT_PROPERTY =
            "default.admin.port"; // NOI18N
    
    public static final String DEFAULT_DEFAULT_USERNAME =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.default.username"); // NOI18N
    public static final String DEFAULT_DEFAULT_PASSWORD =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.default.password"); // NOI18N
    public static final String DEFAULT_DEFAULT_HTTP_PORT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.default.http.port"); // NOI18N
    public static final String DEFAULT_DEFAULT_HTTPS_PORT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.default.https.port"); // NOI18N
    public static final String DEFAULT_DEFAULT_ADMIN_PORT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.default.admin.port"); // NOI18N
    
    public static final String ERROR_USERNAME_NULL_PROPERTY =
            "error.username.null"; // NOI18N
    public static final String ERROR_USERNAME_NOT_ALNUM_PROPERTY =
            "error.username.not.alnum"; // NOI18N
    public static final String ERROR_PASSWORD_NULL_PROPERTY =
            "error.password.null"; // NOI18N
    public static final String ERROR_PASSWORD_TOO_SHORT_PROPERTY =
            "error.password.too.short"; // NOI18N
    public static final String ERROR_PASSWORD_SPACES_PROPERTY =
            "error.password.spaces"; // NOI18N
    public static final String ERROR_PASSWORDS_DO_NOT_MATCH_PROPERTY =
            "error.passwords.do.not.match"; // NOI18N
    public static final String ERROR_ALL_PORTS_OCCUPIED_PROPERTY =
            "error.all.ports.occupied"; // NOI18N
    public static final String ERROR_HTTP_NULL_PROPERTY =
            "error.http.null"; // NOI18N
    public static final String ERROR_HTTPS_NULL_PROPERTY =
            "error.https.null"; // NOI18N
    public static final String ERROR_ADMIN_NULL_PROPERTY =
            "error.admin.null"; // NOI18N
    public static final String ERROR_HTTP_NOT_INTEGER_PROPERTY =
            "error.http.not.integer"; // NOI18N
    public static final String ERROR_HTTPS_NOT_INTEGER_PROPERTY =
            "error.https.not.integer"; // NOI18N
    public static final String ERROR_ADMIN_NOT_INTEGER_PROPERTY =
            "error.admin.not.integer"; // NOI18N
    public static final String ERROR_HTTP_NOT_IN_RANGE_PROPERTY =
            "error.http.not.in.range"; // NOI18N
    public static final String ERROR_HTTPS_NOT_IN_RANGE_PROPERTY =
            "error.https.not.in.range"; // NOI18N
    public static final String ERROR_ADMIN_NOT_IN_RANGE_PROPERTY =
            "error.admin.not.in.range"; // NOI18N
    public static final String ERROR_HTTP_OCCUPIED_PROPERTY =
            "error.http.occupied"; // NOI18N
    public static final String ERROR_HTTPS_OCCUPIED_PROPERTY =
            "error.https.occupied"; // NOI18N
    public static final String ERROR_ADMIN_OCCUPIED_PROPERTY =
            "error.admin.occupied"; // NOI18N
    public static final String ERROR_HTTP_EQUALS_HTTPS_PROPERTY =
            "error.http.equals.https"; // NOI18N
    public static final String ERROR_HTTP_EQUALS_ADMIN_PROPERTY =
            "error.http.equals.admin"; // NOI18N
    public static final String ERROR_HTTPS_EQUALS_ADMIN_PROPERTY =
            "error.https.equals.admin"; // NOI18N
    
    public static final String WARNING_PORT_IN_USE_PROPERTY =
            "warning.port.in.use"; // NOI18N
    public static final String WARNING_ASADMIN_FILES_EXIST_PROPERTY =
            "GFP.warning.asadmin.files.exist"; // NOI18N
    
    public static final String DEFAULT_ERROR_USERNAME_NULL =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.username.null"); // NOI18N
    public static final String DEFAULT_ERROR_USERNAME_NOT_ALNUM =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.username.not.alnum"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_NULL =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.password.null"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_TOO_SHORT =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.password.too.short"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORD_SPACES =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.password.spaces"); // NOI18N
    public static final String DEFAULT_ERROR_PASSWORDS_DO_NOT_MATCH =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.passwords.do.not.match"); // NOI18N
    public static final String DEFAULT_ERROR_ALL_PORTS_OCCUPIED =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.all.ports.occupied"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_NULL =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.null"); // NOI18N
    public static final String DEFAULT_ERROR_HTTPS_NULL =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.https.null"); // NOI18N
    public static final String DEFAULT_ERROR_ADMIN_NULL =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.admin.null"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_NOT_INTEGER =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.not.integer"); // NOI18N
    public static final String DEFAULT_ERROR_HTTPS_NOT_INTEGER =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.https.not.integer"); // NOI18N
    public static final String DEFAULT_ERROR_ADMIN_NOT_INTEGER =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.admin.not.integer"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_NOT_IN_RANGE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.not.in.range"); // NOI18N
    public static final String DEFAULT_ERROR_HTTPS_NOT_IN_RANGE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.https.not.in.range"); // NOI18N
    public static final String DEFAULT_ERROR_ADMIN_NOT_IN_RANGE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.admin.not.in.range"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_OCCUPIED =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.occupied"); // NOI18N
    public static final String DEFAULT_ERROR_HTTPS_OCCUPIED =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.https.occupied"); // NOI18N
    public static final String DEFAULT_ERROR_ADMIN_OCCUPIED =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.admin.occupied"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_EQUALS_HTTPS =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.equals.https"); // NOI18N
    public static final String DEFAULT_ERROR_HTTP_EQUALS_ADMIN =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.http.equals.admin"); // NOI18N
    public static final String DEFAULT_ERROR_HTTPS_EQUALS_ADMIN =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.error.https.equals.admin"); // NOI18N
            
    public static final String DEFAULT_WARNING_PORT_IN_USE =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.warning.port.in.use"); // NOI18N
    public static final String DEFAULT_WARNING_ASADMIN_FILES_EXIST = 
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.warning.asadmin.files.exist"); // NOI18N
            
    public static final String DEFAULT_MINIMUM_JDK_VERSION =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.minimum.jdk.version"); // NOI18N
    public static final String DEFAULT_MAXIMUM_JDK_VERSION =
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.maximum.jdk.version"); // NOI18N
    public static final String DEFAULT_VENDOR_JDK_ALLOWED = 
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.vendor.jdk.allowed"); // NOI18N
    public static final String DEFAULT_VENDOR_JDK_ALLOWED_MACOSX = 
            ResourceUtils.getString(GlassFishPanel.class,
            "GFP.vendor.jdk.allowed.macosx"); // NOI18N
}
