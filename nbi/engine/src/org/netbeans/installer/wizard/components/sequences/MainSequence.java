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

package org.netbeans.installer.wizard.components.sequences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.CreateBundleAction;
import org.netbeans.installer.wizard.components.actions.CreateMacOSAppLauncherAction;
import org.netbeans.installer.wizard.components.actions.CreateNativeLauncherAction;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.PostCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.LicensesPanel;
import org.netbeans.installer.wizard.components.panels.PostInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreInstallSummaryPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class MainSequence extends WizardSequence {
    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private LicensesPanel licensesPanel;
    private PreInstallSummaryPanel preInstallSummaryPanel;
    private UninstallAction uninstallAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private InstallAction installAction;
    private PostInstallSummaryPanel postInstallSummaryPanel;
    private PreCreateBundleSummaryPanel preCreateBundleSummaryPanel;
    private CreateBundleAction createBundleAction;
    private CreateNativeLauncherAction createNativeLauncherAction;
    private CreateMacOSAppLauncherAction createAppLauncherAction ;
    private PostCreateBundleSummaryPanel postCreateBundleSummaryPanel;
    
    private Map<Product, ProductWizardSequence> productSequences;
    
    public MainSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        licensesPanel = new LicensesPanel();
        preInstallSummaryPanel = new PreInstallSummaryPanel();
        uninstallAction = new UninstallAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        installAction = new InstallAction();
        postInstallSummaryPanel = new PostInstallSummaryPanel();
        preCreateBundleSummaryPanel = new PreCreateBundleSummaryPanel();
        createBundleAction = new CreateBundleAction();
        createNativeLauncherAction = new CreateNativeLauncherAction();
        createAppLauncherAction = new CreateMacOSAppLauncherAction();
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
                
                addChild(preInstallSummaryPanel);
                
                if (toUninstall.size() > 0) {
                    addChild(uninstallAction);
                }
                
                if (toInstall.size() > 0) {
                    addChild(downloadInstallationDataAction);
                    addChild(installAction);
                }
                
                addChild(postInstallSummaryPanel);
                break;
            case CREATE_BUNDLE:
                addChild(preCreateBundleSummaryPanel);
                addChild(downloadConfigurationLogicAction);
                addChild(downloadInstallationDataAction);
                addChild(createBundleAction);
                
                if(registry.getTargetPlatform().isCompatibleWith(Platform.MACOSX)) {
                    addChild(createAppLauncherAction);
                } else {
                    addChild(createNativeLauncherAction);
                }
                addChild(postCreateBundleSummaryPanel);
                break;
            default:
                // there is no real way to recover from this fancy error, so we
                // inform the user and die
                ErrorManager.notifyCritical(
                        ResourceUtils.getString(MainSequence.class, ERROR_UKNOWN_MODE_KEY));
        }
        
        super.executeForward();
    }
    
    public boolean canExecuteForward() {
        return true;
    }
    private static final String ERROR_UKNOWN_MODE_KEY =
            "MS.error.mode"; //NOI18N
}
