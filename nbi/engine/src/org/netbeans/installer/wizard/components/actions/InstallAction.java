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
package org.netbeans.installer.wizard.components.actions;

import java.util.List;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.panels.DefaultWizardPanel;
import org.netbeans.installer.wizard.components.sequences.*;

public class InstallAction extends CompositeProgressAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final Class CLASS = InstallAction.class;
    
    public static final String DIALOG_TITLE_PROPERTY = DefaultWizardPanel.DIALOG_TITLE_PROPERTY;
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(CLASS, "InstallAction.default.dialog.title");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public InstallAction() {
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public void execute() {
        final ProductRegistry registry = ProductRegistry.getInstance();
        final List<ProductComponent> components = registry.getComponentsToInstall();
        final int percentageChunk = Progress.COMPLETE / components.size();
        final int percentageLeak = Progress.COMPLETE % components.size();
        
        overallProgress = new CompositeProgress();
        overallProgress.setTitle("Installing selected components");
        overallProgress.setPercentage(percentageLeak);
        
        progressPanel.setOverallProgress(overallProgress);
        for (ProductComponent component:components) {
            currentProgress = new Progress();
            currentProgress.setTitle("Installing " + component.getDisplayName());
            progressPanel.setCurrentProgress(currentProgress);
            
            overallProgress.addChild(currentProgress, percentageChunk);
            try {
                component.install(currentProgress);
                
                if (canceled) return;
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (InstallationException e) {
                // adjust the component's status and save this error - it will
                // be reused later at the PostInstallSummary
                component.setStatus(Status.NOT_INSTALLED);
                component.setInstallationError(e);
                
                // since the component failed to install  - we should remove the
                // depending components from our plans to install
                for(ProductComponent dependent : ProductRegistry.getInstance().getDependingComponents(component)) {
                    if (dependent.getStatus()  == Status.TO_BE_INSTALLED) {
                        InstallationException dependentError = new InstallationException("Could not install " + dependent.getDisplayName() + ", since the installation of " + component.getDisplayName() + "failed", e);
                        
                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);
                        
                        components.remove(dependent);
                    }
                }
                
                // finally notify the user of what has happened
                LogManager.log(ErrorLevel.ERROR, e);
            }
        }
    }
    
    public void cancel() {
        if (currentProgress != null) {
            currentProgress.setCanceled(true);
        }
        
        if (overallProgress != null) {
            overallProgress.setCanceled(true);
        }
        
        super.cancel();
    }
    
    public boolean canExecuteForward() {
        return ProductRegistry.getInstance().getComponentsToInstall().size() > 0;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
}