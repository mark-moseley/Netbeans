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
package org.netbeans.installer.wizard.components.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.wizard.InstallationLogDialog;

/**
 *
 * @author Kirill Sorokin
 */
public class PostCreateBundleSummaryPanel extends TextPanel {
    private NbiTextPane messagePane;
    
    private NbiLabel    successfullyBundledComponentsLabel;
    private NbiTextPane successfullyBundledComponentsPane;
    private NbiLabel    componentsFailedToBundleLabel;
    private NbiTextPane componentsFailedToBundlePane;
    
    private NbiButton   viewLogButton;
    private NbiButton   sendLogButton;
    
    private NbiPanel    spacer;
    
    private InstallationLogDialog     logDialog;
    
    public PostCreateBundleSummaryPanel() {
        setProperty(MESSAGE_SUCCESS_TEXT_PROPERTY, DEFAULT_MESSAGE_SUCCESS_TEXT);
        setProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE);
        setProperty(MESSAGE_ERRORS_TEXT_PROPERTY, DEFAULT_MESSAGE_ERRORS_TEXT);
        setProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE);
        setProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_VIEW_LOG_BUTTON_TEXT);
        setProperty(SEND_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_SEND_LOG_BUTTON_TEXT);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public void initialize() {
        getBackButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        
        ProductRegistry registry = ProductRegistry.getInstance();
        
        if (registry.wereErrorsEncountered()) {
            messagePane.setContentType(getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
        } else {
            messagePane.setContentType(getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
        }
        
        List<ProductComponent> components;
        
        components = registry.getComponentsInstalledSuccessfullyDuringThisSession();
        if (components.size() > 0) {
            successfullyBundledComponentsLabel.setVisible(true);
            successfullyBundledComponentsPane.setVisible(true);
            
            successfullyBundledComponentsLabel.setText(getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY));
            successfullyBundledComponentsPane.setContentType(getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
            successfullyBundledComponentsPane.setText(StringUtils.formatMessage(getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            successfullyBundledComponentsLabel.setVisible(false);
            successfullyBundledComponentsPane.setVisible(false);
        }
        
        components = registry.getComponentsFailedToInstallDuringThisSession();
        if (components.size() > 0) {
            componentsFailedToBundleLabel.setVisible(true);
            componentsFailedToBundlePane.setVisible(true);
            
            componentsFailedToBundleLabel.setText(getProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY));
            componentsFailedToBundlePane.setContentType(getProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY));
            componentsFailedToBundlePane.setText(StringUtils.formatMessage(getProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsFailedToBundleLabel.setVisible(false);
            componentsFailedToBundlePane.setVisible(false);
        }
        
        viewLogButton.setText(getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY));
        sendLogButton.setText(getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY));
    }
    
    public void initComponents() {
        messagePane = new NbiTextPane();
        
        successfullyBundledComponentsLabel = new NbiLabel();
        
        successfullyBundledComponentsPane = new NbiTextPane();
        successfullyBundledComponentsPane.setOpaque(false);
        successfullyBundledComponentsPane.setEditable(false);
        successfullyBundledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsFailedToBundleLabel = new NbiLabel();
        
        componentsFailedToBundlePane = new NbiTextPane();
        componentsFailedToBundlePane.setOpaque(false);
        componentsFailedToBundlePane.setEditable(false);
        componentsFailedToBundlePane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        viewLogButton = new NbiButton();
        viewLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewLogButtonClicked();
            }
        });
        
        sendLogButton = new NbiButton();
        sendLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendLogButtonClicked();
            }
        });
        sendLogButton.setEnabled(false);
        
        spacer = new NbiPanel();
        
        add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(successfullyBundledComponentsLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(successfullyBundledComponentsPane, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(componentsFailedToBundleLabel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsFailedToBundlePane, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(viewLogButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 11, 11, 0), 0, 0));
        add(sendLogButton, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 6, 11, 11), 0, 0));
    }
    
    private void viewLogButtonClicked() {
        if (LogManager.getLogFile() != null) {
            if (logDialog == null) {
                logDialog = new InstallationLogDialog(getWizard().getFrame());
            }
            logDialog.setVisible(true);
            logDialog.loadLogFile();
        } else {
            ErrorManager.notify(ErrorLevel.ERROR, "Log file is not available.");
        }
    }
    
    private void sendLogButtonClicked() {
    }
    
    public static final String MESSAGE_SUCCESS_TEXT_PROPERTY = "message.success.text";
    public static final String MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY = "message.success.content.type";
    public static final String MESSAGE_ERRORS_TEXT_PROPERTY = "message.errors.text";
    public static final String MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY = "message.errors.content.type";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY = "successfully.bundled.components.label.text";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY = "successfully.bundled.components.text";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY = "successfully.bundled.components.content.type";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY = "components.failed.to.bundle.label.text";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY = "components.failed.to.bundle.text";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY = "components.failed.to.bundle.content.type";
    public static final String VIEW_LOG_BUTTON_TEXT_PROPERTY = "view.log.button.text";
    public static final String SEND_LOG_BUTTON_TEXT_PROPERTY = "send.log.button.text";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY = "components.list.separator";
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.message.success.text");
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.message.success.content.type");
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.message.errors.text");
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.message.errors.content.type");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.successfully.bundled.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.successfully.bundled.components.text");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.successfully.bundled.components.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.components.failed.to.bundle.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.components.failed.to.bundle.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.components.failed.to.bundle.content.type");
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.view.log.button.text");
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.send.log.button.text");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.components.list.separator");
    
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PostCreateBundleSummaryPanel.default.dialog.title");
}
