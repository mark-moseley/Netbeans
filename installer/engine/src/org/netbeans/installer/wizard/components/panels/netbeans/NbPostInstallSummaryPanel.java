/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 * @author Kirill Sorokin
 */
public class NbPostInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbPostInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);
        
        setProperty(MESSAGE_TEXT_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS);
        setProperty(MESSAGE_TEXT_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS);
        setProperty(MESSAGE_TEXT_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS);
        
        setProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL);
        setProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL);
        setProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL);
        setProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY, 
                DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL);
        
        setProperty(NEXT_BUTTON_TEXT_PROPERTY, 
                DEFAULT_NEXT_BUTTON_TEXT);
    }
    
    @Override
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbPostInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbPostInstallSummaryPanelUi extends WizardPanelUi {
        protected NbPostInstallSummaryPanel component;
        
        public NbPostInstallSummaryPanelUi(NbPostInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbPostInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbPostInstallSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected NbPostInstallSummaryPanel component;
        
        private NbiTextPane messagePaneInstall;
        private NbiTextPane messagePaneUninstall;
        private NbiTextPane messagePaneNetBeans;
        
        public NbPostInstallSummaryPanelSwingUi(
                final NbPostInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setVisible(true);
            container.getBackButton().setEnabled(false);
            
            // set up the next (or finish) button
            container.getNextButton().setVisible(true);
            container.getNextButton().setEnabled(true);
            
            container.getNextButton().setText(
                    component.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(false);
        }
        
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            if (registry.getProducts(INSTALLED_SUCCESSFULLY).size() +
                    registry.getProducts(INSTALLED_WITH_WARNINGS).size() +
                    registry.getProducts(FAILED_TO_INSTALL).size() > 0) {
                boolean warningsEncountered =
                        registry.getProducts(INSTALLED_WITH_WARNINGS).size() > 0;
                
                boolean errorsEncountered =
                        registry.getProducts(FAILED_TO_INSTALL).size() > 0;
                
                if (errorsEncountered) {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY));
                    messagePaneInstall.setText(component.getProperty(MESSAGE_TEXT_ERRORS_PROPERTY));
                } else if (warningsEncountered) {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY));
                    messagePaneInstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_WARNINGS_PROPERTY), LogManager.getLogFile()));
                } else {
                    messagePaneInstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY));
                    messagePaneInstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_SUCCESS_PROPERTY), LogManager.getLogFile()));
                }
            } else {
                messagePaneInstall.setVisible(false);
            }
            
            if (registry.getProducts(UNINSTALLED_SUCCESSFULLY).size() +
                    registry.getProducts(UNINSTALLED_WITH_WARNINGS).size() +
                    registry.getProducts(FAILED_TO_UNINSTALL).size() > 0) {
                boolean warningsEncountered =
                        registry.getProducts(UNINSTALLED_WITH_WARNINGS).size() > 0;
                
                boolean errorsEncountered =
                        registry.getProducts(FAILED_TO_UNINSTALL).size() > 0;
                
                if (errorsEncountered) {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(component.getProperty(MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY));
                } else if (warningsEncountered) {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY), LogManager.getLogFile()));
                } else {
                    messagePaneUninstall.setContentType(component.getProperty(MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY));
                    messagePaneUninstall.setText(StringUtils.format(component.getProperty(MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY), LogManager.getLogFile()));
                }
            } else {
                messagePaneUninstall.setVisible(false);
            }
            
            final List<Product> products = new LinkedList<Product>();
            
            products.addAll(registry.getProducts(INSTALLED_SUCCESSFULLY));
            products.addAll(registry.getProducts(INSTALLED_WITH_WARNINGS));
            
            messagePaneNetBeans.setContentType(DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE);
            messagePaneNetBeans.setText("");
            for (Product product: products) {
                if (product.getUid().equals("nb-base")) {
                    if (SystemUtils.isWindows()) {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS);
                    } else if (SystemUtils.isMacOS()) {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX);
                    } else {
                        messagePaneNetBeans.setText(DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX);
                    }
                }
            }
        }
        
        private void initComponents() {
            // messagePaneInstall ///////////////////////////////////////////////////
            messagePaneInstall = new NbiTextPane();
            
            // messagePaneUninstall /////////////////////////////////////////////////
            messagePaneUninstall = new NbiTextPane();
            
            // messagePaneNetBeans ///////////////////////////////////////////////////
            messagePaneNetBeans = new NbiTextPane();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePaneInstall, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneUninstall, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(messagePaneNetBeans, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 11, 11),       // padding
                    0, 0));                           // padx, pady - ???
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_SUCCESS_PROPERTY =
            "message.text.success"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_PROPERTY =
            "message.content.type.success"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_PROPERTY =
            "message.text.warnings"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_PROPERTY =
            "message.content.type.warnings"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_PROPERTY =
            "message.text.errors"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_PROPERTY =
            "message.content.type.errors"; // NOI18N
    public static final String MESSAGE_TEXT_SUCCESS_UNINSTALL_PROPERTY =
            "message.text.success.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL_PROPERTY =
            "message.content.type.success.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_WARNINGS_UNINSTALL_PROPERTY =
            "message.text.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL_PROPERTY =
            "message.content.type.warnings.uninstall"; // NOI18N
    public static final String MESSAGE_TEXT_ERRORS_UNINSTALL_PROPERTY =
            "message.text.errors.uninstall"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL_PROPERTY =
            "message.content.type.errors.uninstall"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_SUCCESS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.success.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_WARNINGS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.warnings.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_TEXT_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.text.errors.uninstall"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE_ERRORS_UNINSTALL =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.content.type.errors.uninstall"); // NOI18N
    
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_WINDOWS = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.windows"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_UNIX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.unix"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_TEXT_MACOSX = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.text.macosx"); // NOI18N
    public static final String DEFAULT_MESSAGE_NETBEANS_CONTENT_TYPE = 
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.message.netbeans.content.type"); // NOI18N
    
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            NbPostInstallSummaryPanel.class,
            "NPoISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.description"); // NOI18N
    
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(NbPostInstallSummaryPanel.class,
            "NPoISP.next.button.text"); // NOI18N
}
