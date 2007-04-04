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
package org.netbeans.installer.wizard.components.sequences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.wizard.components.panels.netbeans.NbPostInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbPreInstallSummaryPanel;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.CreateBundleAction;
import org.netbeans.installer.wizard.components.actions.CreateNativeLauncherAction;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.PostCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.LicensesPanel;
import org.netbeans.installer.wizard.components.sequences.ProductWizardSequence;

/**
 *
 * @author Kirill Sorokin
 */
public class MainSequence extends WizardSequence {
    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private LicensesPanel licensesPanel;
    private NbPreInstallSummaryPanel nbPreInstallSummaryPanel;
    private UninstallAction uninstallAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private InstallAction installAction;
    private NbPostInstallSummaryPanel nbPostInstallSummaryPanel;
    private PreCreateBundleSummaryPanel preCreateBundleSummaryPanel;
    private CreateBundleAction createBundleAction;
    private CreateNativeLauncherAction createNativeLauncherAction;
    private PostCreateBundleSummaryPanel postCreateBundleSummaryPanel;
    
    private Map<Product, ProductWizardSequence> productSequences;
    
    public MainSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        licensesPanel = new LicensesPanel();
        nbPreInstallSummaryPanel = new NbPreInstallSummaryPanel();
        uninstallAction = new UninstallAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        installAction = new InstallAction();
        nbPostInstallSummaryPanel = new NbPostInstallSummaryPanel();
        preCreateBundleSummaryPanel = new PreCreateBundleSummaryPanel();
        createBundleAction = new CreateBundleAction();
        createNativeLauncherAction = new CreateNativeLauncherAction();
        postCreateBundleSummaryPanel = new PostCreateBundleSummaryPanel();
        
        productSequences = new HashMap<Product, ProductWizardSequence>();
    }
    
    public void executeForward() {
        final Registry      registry    = Registry.getInstance();
        final List<Product> toInstall   = registry.getProductsToInstall();
        final List<Product> toUninstall = registry.getProductsToUninstall();
        
        // remove all current children (if there are any), as the components
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();
        
        // the set of wizard components differs greatly depending on the execution
        // mode - if we're installing, we ask for input, run a wizard sequence for
        // each selected component and then download and install; if we're creating
        // a bundle, we only need to download and package things
        switch (ExecutionMode.getCurrentExecutionMode()) {
        case NORMAL:
            if (toInstall.size() > 0) {
                addChild(downloadConfigurationLogicAction);
                addChild(licensesPanel);
                
                for (Product product: toInstall) {
                    if (!productSequences.containsKey(product)) {
                        productSequences.put(
                                product, 
                                new ProductWizardSequence(product));
                    }
                    
                    addChild(productSequences.get(product));
                }
            }
            
            addChild(nbPreInstallSummaryPanel);
            
            if (toUninstall.size() > 0) {
                addChild(uninstallAction);
            }
            
            if (toInstall.size() > 0) {
                addChild(downloadInstallationDataAction);
                addChild(installAction);
            }
            
            addChild(nbPostInstallSummaryPanel);
            break;
        case CREATE_BUNDLE:
            addChild(preCreateBundleSummaryPanel);
            addChild(downloadConfigurationLogicAction);
            addChild(downloadInstallationDataAction);
            addChild(createBundleAction);
            addChild(createNativeLauncherAction);
            addChild(postCreateBundleSummaryPanel);
            break;
        default:
            // there is no real way to recover from this fancy error, so we
            // inform the user and die
            ErrorManager.notifyCritical(
                    "A terrible and weird error happened - installer's " +
                    "execution mode is not recognized");
        }
        
        super.executeForward();
    }
    
    public boolean canExecuteForward() {
        return true;
    }
}
