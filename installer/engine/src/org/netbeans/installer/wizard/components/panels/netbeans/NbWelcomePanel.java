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

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.AndFilter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class NbWelcomePanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Registry bundledRegistry;
    private Registry defaultRegistry;
    
    private boolean registriesFiltered;
    private static BundleType type;
    public NbWelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY,
                DEFAULT_TEXT_PANE_CONTENT_TYPE);
        type = BundleType.getType(
                System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));
        
        setProperty(WELCOME_TEXT_HEADER_PROPERTY,
                (type.isJDKBundle() ?
                    DEFAULT_WELCOME_TEXT_HEADER_JDK :
                    (type.equals(BundleType.JAVA_TOOLS) ? 
                           DEFAULT_WELCOME_TEXT_HEADER_JTB : 
                           (type.equals(BundleType.MYSQL) ? 
                                  DEFAULT_WELCOME_TEXT_HEADER_MYSQL : 
                                       DEFAULT_WELCOME_TEXT_HEADER ))));
        
        setProperty(WELCOME_TEXT_DETAILS_PROPERTY,
                ResourceUtils.getString(NbWelcomePanel.class,
                WELCOME_TEXT_HEADER_APPENDING_PROPERTY + "." + type ));
        
        
        setProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_OPENTAG_PROPERTY,
                DEFAULT_WELCOME_TEXT_OPENTAG);
        setProperty(WELCOME_TEXT_FOOTER_PROPERTY,
                DEFAULT_WELCOME_TEXT_FOOTER);
        setProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY,
                DEFAULT_CUSTOMIZE_BUTTON_TEXT);
        setProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY,
                DEFAULT_INSTALLATION_SIZE_LABEL_TEXT);
        
        setProperty(CUSTOMIZE_TITLE_PROPERTY,
                DEFAULT_CUSTOMIZE_TITLE);
        
        setProperty(MESSAGE_PROPERTY,
                DEFAULT_MESSAGE);
        setProperty(MESSAGE_INSTALL_PROPERTY,
                DEFAULT_MESSAGE_INSTALL);
        setProperty(MESSAGE_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_UNINSTALL);
        setProperty(COMPONENT_DESCRIPTION_TEXT_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_TEXT);
        setProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT);
        setProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD);
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        setProperty(OK_BUTTON_TEXT_PROPERTY,
                DEFAULT_OK_BUTTON_TEXT);
        setProperty(CANCEL_BUTTON_TEXT_PROPERTY,
                DEFAULT_CANCEL_BUTTON_TEXT);
        setProperty(DEFAULT_COMPONENT_DESCRIPTION_PROPERTY,
                DEFAULT_DEFAULT_COMPONENT_DESCRIPTION);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY);
        setProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY,
                DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT);
        setProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY,
                DEFAULT_ERROR_EVERYTHING_IS_INSTALLED);
        
        // initialize the registries used on the panel - see the initialize() and
        // canExecute() method
        try {
            defaultRegistry = Registry.getInstance();
            bundledRegistry = new Registry();
            
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
            if (bundledRegistryUri != null) {
                bundledRegistry.loadProductRegistry(bundledRegistryUri);
            } else {
                bundledRegistry.loadProductRegistry(
                        Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError("Cannot load bundled registry", e);
        }
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbWelcomePanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        if (registriesFiltered) {
            return;
        }
        
        // we need to apply additional filters to the components tree - filter out
        // the components which are not present in the bundled registry; if the
        // bundled registry contains only one element - registry root, this means
        // that we're running without any bundle, hence not filtering is required;
        // additionally, we should not be suggesting to install tomcat by default,
        // thus we should correct it's initial status
        Product bundledJdkProductSkip = null;
        if (bundledRegistry.getNodes().size() > 1) {
            for (Product product: defaultRegistry.getProducts()) {
                if (bundledRegistry.getProduct(
                        product.getUid(),
                        product.getVersion()) == null) {
                    product.setVisible(false);
                    
                    if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }
                } else if (product.getUid().equals("tomcat") &&
                        (product.getStatus() == Status.TO_BE_INSTALLED)) {
                    BundleType tp = BundleType.getType(System.getProperty(
                            WELCOME_PAGE_TYPE_PROPERTY));
                    boolean stateFileUsed = System.getProperty(
                            Registry.SOURCE_STATE_FILE_PATH_PROPERTY) != null;
                    boolean sysPropInstallLocation = System.getProperty(product.getUid() + 
				StringUtils.DOT + 
				Product.INSTALLATION_LOCATION_PROPERTY) == null;
                    if(!stateFileUsed && sysPropInstallLocation &&
                            (tp.equals(BundleType.CUSTOMIZE) || tp.equals(BundleType.CUSTOMIZE_JDK))) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }
                } else if(type.isJDKBundle() && product.getUid().equals("jdk")) { // current checking product in global registry is jdk
                    if(product.getStatus() == Status.TO_BE_INSTALLED && 
                        ExecutionMode.getCurrentExecutionMode() == ExecutionMode.NORMAL) {
                        // check if jdk is already installed                        
                        if(JavaUtils.findJDKHome(product.getVersion())!=null) {
                            product.setStatus(Status.NOT_INSTALLED);
                            product.setVisible(false);
                            setProperty(JDK_INSTALLED_TEXT_PROPERTY,
                                    StringUtils.format(DEFAULT_JDK_INSTALLED_TEXT, product.getDisplayName()));
                            bundledJdkProductSkip = product;                            
                        } else {
                            // do not allow installation under non-admin user on windows
                            try {
                                if(SystemUtils.isWindows() && !SystemUtils.isCurrentUserAdmin()) {
                                    product.setStatus(Status.NOT_INSTALLED);
                                    product.setVisible(false);
                                    setProperty(JDK_UNSUFFICIENT_PERMISSIONS_PROPERTY,
                                            DEFAULT_JDK_UNSUFFICIENT_PERMISSIONS_TEXT);
                                    bundledJdkProductSkip = product;
                                }
                            } catch (NativeException e){
                                LogManager.log(e);
                            }
                        }
                    } else if(product.getStatus() == Status.INSTALLED && //installed
                            bundledRegistry.getProduct(
                            product.getUid(),product.getVersion())!=null){ //and it is the same product
                        setProperty(JDK_INSTALLED_TEXT_PROPERTY,
                                StringUtils.format(DEFAULT_JDK_INSTALLED_TEXT,
                                product.getDisplayName()));
                        bundledJdkProductSkip = product;
                    }
                }
            }
        }
        final List <Product> toInstall = defaultRegistry.getProductsToInstall();
        if (bundledJdkProductSkip != null) {            
            if (toInstall.isEmpty()) {
                if (getProperty(JDK_INSTALLED_TEXT_PROPERTY) != null) {
                    setProperty(JDK_EVERYTHING_INSTALLED_TEXT_PROPERTY,
                            StringUtils.format(DEFAULT_JDK_EVERYTHING_INSTALLED_TEXT,
                            bundledJdkProductSkip.getDisplayName()));
                } else if (getProperty(JDK_UNSUFFICIENT_PERMISSIONS_PROPERTY) != null) {
                    setProperty(JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY,
                            StringUtils.format(DEFAULT_JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT,
                            bundledJdkProductSkip.getDisplayName()));
                }
            } 
        }
        if(toInstall.size()==1 && toInstall.get(0).getUid().equals("jdk")) { // install only JDK
                setProperty(JDK_NETBEANS_INSTALLED_TEXT_PROPERTY, 
                        DEFAULT_JDK_NETBEANS_INSTALLED_TEXT);
        }
        registriesFiltered = true;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return bundledRegistry.getNodes().size() > 1;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbWelcomePanelUi extends ErrorMessagePanelUi {
        protected NbWelcomePanel component;
        
        public NbWelcomePanelUi(NbWelcomePanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbWelcomePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbWelcomePanelSwingUi extends ErrorMessagePanelSwingUi {
        protected NbWelcomePanel panel;
        
        private NbiTextPane textPane;
        private NbiTextPane detailsTextPane;
        private NbiLabel detailsWarningIconLabel;
        private NbiTextPane textScrollPane;
        private NbiScrollPane scrollPane;
        private NbiButton customizeButton;
        private NbiLabel installationSizeLabel;
        
        private NbCustomizeSelectionDialog customizeDialog;
        private NbiPanel leftImagePanel;
        
        private List<RegistryNode> registryNodes;
        
        private boolean everythingIsInstalled;
        
        ValidatingThread validatingThread;
        
        public NbWelcomePanelSwingUi(
                final NbWelcomePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.panel = component;
            
            registryNodes = new LinkedList<RegistryNode>();
            populateList(
                    registryNodes,
                    Registry.getInstance().getRegistryRoot());
            
            initComponents();
        }
        
        @Override
        public String getTitle() {
            return null; // the welcome page does not have a title
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            container.getBackButton().setVisible(false);
        }
      
        @Override
        protected void initialize() {            
            StringBuilder welcomeText = new StringBuilder();
            String header = StringUtils.format(panel.getProperty(WELCOME_TEXT_HEADER_PROPERTY));
            if(type.isJDKBundle()) {
                for(Product product : panel.getBundledRegistry().getProducts("jdk")) {
                    header = StringUtils.format(header, product.getDisplayName());
                    break;
                }
            }
            
            welcomeText.append(header);
            welcomeText.append(panel.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            textPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textPane.setText(welcomeText);
            StringBuilder detailsText = new StringBuilder(
                    panel.getProperty(WELCOME_TEXT_OPENTAG_PROPERTY));
            boolean warningIcon = false;
            if(panel.getProperty(JDK_EVERYTHING_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(JDK_EVERYTHING_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(JDK_NETBEANS_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(JDK_NETBEANS_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(JDK_INSTALLED_TEXT_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(JDK_INSTALLED_TEXT_PROPERTY));
                warningIcon = true;
            } else if(panel.getProperty(JDK_UNSUFFICIENT_PERMISSIONS_PROPERTY)!=null) {
                detailsText.append(panel.getProperty(JDK_UNSUFFICIENT_PERMISSIONS_PROPERTY));
                warningIcon = true;
            } else {
                detailsText.append(panel.getProperty(WELCOME_TEXT_DETAILS_PROPERTY));
            }
            detailsTextPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            
            detailsTextPane.setText(detailsText.toString());
            if(warningIcon) {
                detailsWarningIconLabel.setIcon(new ImageIcon(
                        getClass().getClassLoader().getResource(WARNING_ICON)));
            } else {
                detailsWarningIconLabel.setVisible(false);                
            }
            

            everythingIsInstalled = true;
            welcomeText = new StringBuilder(
                    panel.getProperty(WELCOME_TEXT_OPENTAG_PROPERTY));
            for (RegistryNode node: registryNodes) {
                if (node instanceof Product) {
                    final Product product = (Product) node;
                    
                    if (product.getStatus() == Status.INSTALLED) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.CUSTOMIZE_JDK)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    } else if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.CUSTOMIZE_JDK)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                        everythingIsInstalled = false;
                    } else if ((product.getStatus() == Status.NOT_INSTALLED)) {
                        everythingIsInstalled = false;
                    } else {
                        continue;
                    }
                } else if (node instanceof Group) {
                    final RegistryFilter filter = new AndFilter(
                            new ProductFilter(true),
                            new OrFilter(
                            new ProductFilter(Status.TO_BE_INSTALLED),
                            new ProductFilter(Status.INSTALLED)));
                    
                    if (node.hasChildren(filter)) {
                        if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.CUSTOMIZE_JDK)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    }
                }
            }                      
            
            welcomeText.append(panel.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            
            textScrollPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textScrollPane.setText(welcomeText);
            textScrollPane.setCaretPosition(0);              
            customizeButton.setText(
                    panel.getProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY));                                                           
            
            scrollPane.getViewport().setMinimumSize(
                    textScrollPane.getPreferredScrollableViewportSize());
            updateSizes();                
            super.initialize();             
        }
        
        private void updateSizes() {
            long installationSize = 0;
            for (Product product: Registry.getInstance().getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
            }
            
            if (installationSize == 0) {
                installationSizeLabel.setText(StringUtils.EMPTY_STRING);
            } else {
                installationSizeLabel.setText(StringUtils.format(
                        panel.getProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY),
                        StringUtils.formatSize(installationSize)));
            }
        }
        
        
        @Override
        protected String validateInput() {
            if (everythingIsInstalled) {
                customizeButton.setEnabled(false);
                installationSizeLabel.setVisible(false);
                
                return panel.getProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY);
            } else {
                customizeButton.setEnabled(true);
                installationSizeLabel.setVisible(true);
            }
            
            final List<Product> products =
                    Registry.getInstance().getProductsToInstall();
            
            if (products.size() == 0) {
                return panel.getProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY);
            }
            
            String template = panel.getProperty(
                    ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY);
            for (Product product: products) {
                if (product.getRegistryType() == RegistryType.REMOTE) {
                    template = panel.getProperty(
                            ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY);
                    break;
                }
            }
            
            try {                
                if(!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {                     
                    final long availableSize = SystemUtils.getFreeSpace(
                            Installer.getInstance().getLocalDirectory());
                    
                    long requiredSize = 0;
                    for (Product product: products) {
                        requiredSize += product.getDownloadSize();
                    }
                    requiredSize += REQUIRED_SPACE_ADDITION;
                    
                    if (availableSize < requiredSize) {
                        return StringUtils.format(
                                template,
                                Installer.getInstance().getLocalDirectory(),
                                StringUtils.formatSize(requiredSize - availableSize));
                    }
                }
            } catch (NativeException e) {
                ErrorManager.notifyError(
                        "Cannot check the free disk space",
                        e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();
            
            // textScrollPane /////////////////////////////////////////////////////////////
            textScrollPane = new NbiTextPane();
            textScrollPane.setOpaque(true);
            textScrollPane.setBackground(Color.WHITE);
            
            detailsTextPane = new NbiTextPane();
            detailsTextPane.setOpaque(true);
            detailsTextPane.setBackground(Color.WHITE);
            
            // scrollPane ////////////////////////////////////////////////////
            scrollPane = new NbiScrollPane(textScrollPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setViewportBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            scrollPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            
            // customizeButton //////////////////////////////////////////////////////
            customizeButton = new NbiButton();
            customizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    customizeButtonPressed();
                }
            });
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            
            
            leftImagePanel = new NbiPanel();
            int width = 0;
            int height = 0;
            final String topLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY));
            final String bottomLeftImage = SystemUtils.resolveString(
                    System.getProperty(
                    WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY));
            
            if(topLeftImage!=null) {
                leftImagePanel.setBackgroundImage(topLeftImage,ANCHOR_TOP_LEFT);
                width   = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_TOP_LEFT).getIconHeight();
            }
            if(bottomLeftImage!=null) {
                leftImagePanel.setBackgroundImage(bottomLeftImage,ANCHOR_BOTTOM_LEFT);
                width   = leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_BOTTOM_LEFT).getIconWidth();
                height += leftImagePanel.getBackgroundImage(NbiPanel.ANCHOR_BOTTOM_LEFT).getIconHeight();
            }
             
            leftImagePanel.setPreferredSize(new Dimension(width,height));
            leftImagePanel.setMaximumSize(new Dimension(width,height));
            leftImagePanel.setMinimumSize(new Dimension(width,0));
            leftImagePanel.setSize(new Dimension(width,height));
            
            leftImagePanel.setOpaque(false);
            // this /////////////////////////////////////////////////////////////////
            int dy = 0;
            add(leftImagePanel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 100,                           // width, height
                    0.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,     // anchor
                    GridBagConstraints.VERTICAL,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            add(textPane, new GridBagConstraints(
                    1, dy++,                             // x, y
                    4, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,        // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(10, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
            detailsWarningIconLabel = new NbiLabel();
            add(detailsWarningIconLabel, new GridBagConstraints(
                    1, dy,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.NORTHWEST,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(2, 11, 0, 0),        // padding
                    0, 0));                           // padx, pady - ???
            add(detailsTextPane, new GridBagConstraints(
                    2, dy++,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,        // anchor
                    GridBagConstraints.HORIZONTAL,          // fill
                    new Insets(2, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            NbiTextPane separatorPane =  new NbiTextPane();
            BundleType type = BundleType.getType(
                    System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));
            if(!type.equals(BundleType.JAVAEE) && !type.equals(BundleType.JAVAEE_JDK) && 
                    !type.equals(BundleType.JAVA_TOOLS) && !type.equals(BundleType.MYSQL)) {
                add(scrollPane, new GridBagConstraints(
                        1, dy++,                           // x, y
                        4, 1,                              // width, height
                        1.0, 10.0,                         // weight-x, weight-y
                        GridBagConstraints.LINE_START,     // anchor
                        GridBagConstraints.BOTH,           // fill
                        new Insets(0, 11, 0, 11),            // padding
                        0, 0));                            // padx, pady - ???
            }else {
                for (RegistryNode node: registryNodes) {
                    if (node instanceof Product) {
                        final Product product = (Product) node;
                        if(product.getUid().equals("glassfish") ||
                                product.getUid().equals("glassfish-mod") ||
                                product.getUid().equals("tomcat") ||
				product.getUid().equals("mysql")) {
                            final NbiCheckBox chBox;
                            
                            if (product.getStatus() == Status.INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText( "<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(true);
                                chBox.setEnabled(false);
                            } else if (product.getStatus() == Status.TO_BE_INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText("<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(true);
                                chBox.setEnabled(true);
                            } else if (product.getStatus() == Status.NOT_INSTALLED) {
                                chBox = new NbiCheckBox();
                                chBox.setText("<html>" +
                                        StringUtils.format(
                                        panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                        node.getDisplayName()));
                                chBox.setSelected(false);
                                chBox.setEnabled(true);
                            } else {
                                chBox = null;
                            }
                            if(chBox != null) {
                                chBox.setOpaque(false);
                                
                                //chBox.setPreferredSize(new Dimension(chBox.getPreferredSize().width,
                                //        chBox.getPreferredSize().height-2));
                                chBox.setBorder(new EmptyBorder(0,0,0,0));
                                add(chBox,new GridBagConstraints(
                                        1, dy++,                             // x, y
                                        4, 1,                             // width, height
                                        1.0, 0.0,                         // weight-x, weight-y
                                        GridBagConstraints.LINE_START,        // anchor
                                        GridBagConstraints.HORIZONTAL,          // fill
                                        new Insets(0, 11, 0, 0),        // padding
                                        0, 0));
                                chBox.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        if(chBox.isSelected()) {
                                            product.setStatus(Status.TO_BE_INSTALLED);
                                        } else {
                                            product.setStatus(Status.NOT_INSTALLED);
                                        }
                                        updateErrorMessage();
                                        updateSizes();
                                    }
                                });
                                
                            }
                        }
                    }
                }
                add(separatorPane , new GridBagConstraints(
                        1, dy++,                             // x, y
                        4, 1,                                // width, height
                        1.0, 2.0,                            // weight-x, weight-y
                        GridBagConstraints.LINE_START,       // anchor
                        GridBagConstraints.BOTH,             // fill
                        new Insets(0, 0, 0, 0),              // padding
                        0, 0));                              // padx, pady - ???
            }
            add(customizeButton, new GridBagConstraints(
                    1, dy,                            // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(10, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            separatorPane =  new NbiTextPane();
            add(separatorPane , new GridBagConstraints(
                    3, dy,                            // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???
            
            add(installationSizeLabel, new GridBagConstraints(
                    4, dy,                            // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.EAST,          // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(10, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            
            // move error label after the left welcome image
            Component errorLabel = getComponent(0);
            getLayout().removeLayoutComponent(errorLabel);
            add(errorLabel, new GridBagConstraints(
                    1, 99,                             // x, y
                    99, 1,                             // width, height
                    1.0, 0.0,                          // weight-x, weight-y
                    GridBagConstraints.CENTER,         // anchor
                    GridBagConstraints.HORIZONTAL,     // fill
                    new Insets(4, 11, 4, 0),          // padding
                    0, 0));                            // ??? (padx, pady)
            
            // platform-specific tweak //////////////////////////////////////////////
            if (SystemUtils.isMacOS()) {
                customizeButton.setOpaque(false);
            }
            
            if(type.equals(BundleType.CUSTOMIZE) || type.equals(BundleType.CUSTOMIZE_JDK) ||
                    type.equals(BundleType.JAVA_TOOLS)) {
                customizeButton.setVisible(true);
            } else {
                customizeButton.setVisible(false);
            }  
        }
        
        private void customizeButtonPressed() {
            if (customizeDialog == null) {
                final Runnable callback = new Runnable() {
                    public void run() {
                        initialize();
                    }
                };
                
                customizeDialog = new NbCustomizeSelectionDialog(
                        panel,
                        callback,
                        registryNodes);
            }

            customizeDialog.setVisible(true);
            customizeDialog.requestFocus();
        }
        
        private void populateList(
                final List<RegistryNode> list,
                final RegistryNode parent) {
            final List<RegistryNode> groups = new LinkedList<RegistryNode>();
            
            for (RegistryNode node: parent.getChildren()) {
                if (!node.isVisible()) {
                    continue;
                }
                
                if (node instanceof Product) {
                    if (!SystemUtils.getCurrentPlatform().isCompatibleWith(
                            ((Product) node).getPlatforms())) {
                        continue;
                    }
                    
                    list.add(node);
                }
                
                if (node instanceof Group) {
                    if (node.hasChildren(new ProductFilter(true))) {
                        groups.add(node);
                    }
                }
            }
            
            for (RegistryNode node: groups) {
                list.add(node);
                if(!node.getUid().equals("nb-ide-group")) {
                    populateList(list, node);
                }
            }
        }
    }
    
    public Registry getBundledRegistry() {
        return bundledRegistry;
    }    
    
    
    public enum BundleType {
        JAVASE("javase"),
        JAVAEE("javaee"),
        JAVAME("javame"),
        RUBY("ruby"),
        CND("cnd"),
        PHP("php"),
        CUSTOMIZE("customize"),
        JAVASE_JDK("javase.jdk"),
        JAVAEE_JDK("javaee.jdk"),
        JAVAME_JDK("javame.jdk"),
        RUBY_JDK("ruby.jdk"),
        CND_JDK("cnd.jdk"),
        PHP_JDK("php.jdk"),
        CUSTOMIZE_JDK("customize.jdk"),
        JAVA_TOOLS("java.tools"),
	MYSQL("mysql");
        
        private String name;
        private BundleType(String s) {
            this.name = s;
        }
        public static BundleType getType(String s) {
            if(s!=null) {
                for(BundleType type : BundleType.values())
                    if(type.toString().equals(s)) {
                    return type;
                    }
            }
            return CUSTOMIZE;
        }
        @Override
        public String toString() {
            return name;
        }
        public boolean isJDKBundle() {
            return name.endsWith(".jdk");
        }
        public String getNetBeansBundleId() {
            if(isJDKBundle()) {
                return "NBJDK";
            } else if(this.equals(JAVA_TOOLS)) {
                return "NBEETOOLS";
            } else if(this.equals(MYSQL)) {
                return "NBMYSQL";
            } else {
                return "NB";
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.description"); // NOI18N
    
    public static final String TEXT_PANE_CONTENT_TYPE_PROPERTY =
            "text.pane.content.type"; // NOI18N
    public static final String WELCOME_TEXT_HEADER_PROPERTY =
            "welcome.text.header"; // NOI18N
    public static final String WELCOME_TEXT_DETAILS_PROPERTY =
            "welcome.text.details"; // NOI18N
    public static final String WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY =
            "welcome.text.group.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.not.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_OPENTAG_PROPERTY =
            "welcome.text.opentag"; // NOI18N
    public static final String WELCOME_TEXT_FOOTER_PROPERTY =
            "welcome.text.footer"; // NOI18N
    public static final String CUSTOMIZE_BUTTON_TEXT_PROPERTY =
            "customize.button.text"; // NOI18N
    public static final String INSTALLATION_SIZE_LABEL_TEXT_PROPERTY =
            "installation.size.label.text"; // NOI18N
    
    public static final String JDK_INSTALLED_TEXT_PROPERTY =
            "jdk.already.installed.text";//NOI18N
    public static final String JDK_EVERYTHING_INSTALLED_TEXT_PROPERTY =
            "jdk.everything.installed.text";//NOI18N
    public static final String JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT_PROPERTY = 
            "jdk.everything.installed.unsufficient.permissions.text";//NOI18N
    public static final String JDK_NETBEANS_INSTALLED_TEXT_PROPERTY = 
            "jdk.netbeans.installed.text";//NOI18N
    public static final String JDK_UNSUFFICIENT_PERMISSIONS_PROPERTY =
            "jdk.unsufficient.permissions";//NOI18N
    
    public static final String DEFAULT_TEXT_PANE_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.text.pane.content.type"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_JDK =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.jdk"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_JTB =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.jtb"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER_MYSQL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header.nbgfmysql"); // NOI18N


    public static final String WELCOME_TEXT_HEADER_APPENDING_PROPERTY =
            "NWP.welcome.text.header"; // NOI18N
    
    public static final String WELCOME_PAGE_TYPE_PROPERTY =
            "NWP.welcome.page.type";
    
    public static final String DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.group.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.not.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_OPENTAG =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.opentag");
    public static final String DEFAULT_WELCOME_TEXT_FOOTER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.footer"); // NOI18N
    public static final String DEFAULT_CUSTOMIZE_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.button.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.installation.size.label.text"); // NOI18N
    
    public static final String DEFAULT_JDK_INSTALLED_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.jdk.installed.text"); // NOI18N
    public static final String DEFAULT_JDK_EVERYTHING_INSTALLED_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.jdk.everything.installed.text"); // NOI18N
    public static final String DEFAULT_JDK_EVERYTHING_INSTALLED_UNSUFFICIENT_PERMISSIONS_TEXT = 
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.jdk.everything.installed.admin.warning.text"); // NOI18N
    public static final String DEFAULT_JDK_NETBEANS_INSTALLED_TEXT = 
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.jdk.netbeans.installed.text");//NOI18N
    public static final String DEFAULT_JDK_UNSUFFICIENT_PERMISSIONS_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.admin.warning.text");//NOI18N
    
    
    public static final String CUSTOMIZE_TITLE_PROPERTY =
            "customize.title"; // NOI18N
    public static final String MESSAGE_PROPERTY =
            "message"; // NOI18N
    public static final String MESSAGE_INSTALL_PROPERTY =
            "message.install"; // NOI18N
    public static final String MESSAGE_UNINSTALL_PROPERTY =
            "message.uninstall"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_TEXT_PROPERTY =
            "component.description.text"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "component.description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY =
            "sizes.label.text.no.download"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    public static final String OK_BUTTON_TEXT_PROPERTY =
            "ok.button.text"; // NOI18N
    public static final String CANCEL_BUTTON_TEXT_PROPERTY =
            "cancel.button.text"; // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_PROPERTY =
            "default.component.description";
    public static final String WELCOME_PAGE_LEFT_TOP_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.top.image";//NOI18N
    public static final String WELCOME_PAGE_LEFT_BOTTOM_IMAGE_PROPERTY =
            "nbi.wizard.ui.swing.welcome.left.bottom.image";//NOI18N
    public static final String DEFAULT_CUSTOMIZE_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.title"); // NOI18N
    public static final String DEFAULT_MESSAGE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.both"); // NOI18N
    public static final String DEFAULT_MESSAGE_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.install"); // NOI18N
    public static final String DEFAULT_MESSAGE_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.uninstall"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.download.size"); // NOI18N
    public static final String DEFAULT_OK_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.ok.button.text"); // NOI18N
    public static final String DEFAULT_CANCEL_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.cancel.button.text"); // NOI18N
    public static final String DEFAULT_DEFAULT_COMPONENT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.component.description"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes.both"; // NOI18N
    public static final String ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY =
            "error.no.changes.install"; // NOI18N
    public static final String ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY =
            "error.no.changes.uninstall"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY =
            "error.not.enough.space.to.download"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY =
            "error.not.enough.space.to.extract"; // NOI18N
    public static final String ERROR_EVERYTHING_IS_INSTALLED_PROPERTY =
            "error.everything.is.installed"; // NOI18N
    
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.download"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.extract"); // NOI18N
    public static final String DEFAULT_ERROR_EVERYTHING_IS_INSTALLED =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.everything.is.installed"); // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
