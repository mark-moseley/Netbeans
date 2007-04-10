/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.wizard.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiProgressBar;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressListener;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardAction extends WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private WizardUi wizardUi;
    
    private boolean finished;
    private boolean canceled;
    
    protected WizardAction() {
        finished = false;
        canceled = false;
    }
    
    public final void executeForward() {
        new NbiThread() {
            @Override
            public void run() {
                finished = false;
                execute();
                finished = true;
                
                if (!canceled) {
                    getWizard().next();
                }
            }
        }.start();
    }
    
    public final void executeBackward() {
        // does nothing
    }
    
    @Override
    public final boolean canExecuteBackward() {
        return false;
    }
    
    public WizardActionUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WizardActionUi(this);
        }
        
        return (WizardActionUi) wizardUi;
    }
    
    public void initialize() {
        // does nothing
    }
    
    public abstract void execute();
    
    public boolean isCancellable() {
        return true;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void cancel() {
        canceled = true;
        
        while (!finished) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                ErrorManager.notifyDebug(RESOURCE_INTERRUPTED_EXCEPTION, e);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WizardActionUi extends WizardComponentUi
            implements ProgressListener {
        protected WizardAction action;
        protected Progress progress;
        
        public WizardActionUi(final WizardAction component) {
            super(component);
            
            
            this.action = component;
        }
        
        @Override
        public SwingUi getSwingUi(final SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardActionSwingUi(action, container);
            }
            
            return super.getSwingUi(container);
        }
        
        public void setProgress(final Progress progress) {
            if (this.progress != null) {
                this.progress.removeProgressListener(this);
            }
            
            this.progress = progress;
            this.progress.addProgressListener(this);
        }
        
        public void progressUpdated(final Progress progress) {
            if (swingUi != null) {
                ((WizardActionSwingUi) swingUi).progressUpdated(progress);
            }
        }
    }
    
    public static class WizardActionSwingUi extends WizardComponentSwingUi {
        private WizardAction action;
        
        private NbiLabel titleLabel;
        private NbiLabel detailLabel;
        private NbiProgressBar progressBar;
        
        public WizardActionSwingUi(
                final WizardAction component,
                final SwingContainer container) {
            super(component, container);
            
            this.action = component;
            
            initComponents();
        }
        
        @Override
        public void initializeContainer() {
            super.initializeContainer();
            
            // set up the help button
            container.getHelpButton().setEnabled(false);
            container.getHelpButton().setVisible(false);
            
            // set up the back button
            container.getBackButton().setEnabled(false);
            container.getBackButton().setVisible(true);
            
            // set up the next (or finish) button
            container.getNextButton().setEnabled(false);
            container.getNextButton().setVisible(true);
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(action.isCancellable());
        }
        
        @Override
        public void evaluateCancelButtonClick() {
            if (action.isCancellable()) {
                final String cancelDialogTitle = ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCEL_DIALOG_TITLE);
                final String canceldialogText = ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCEL_DIALOG_TEXT);
                
                if (!UiUtils.showYesNoDialog(cancelDialogTitle, canceldialogText)) {
                    return;
                }
                
                container.getCancelButton().setEnabled(false);
                titleLabel.setText(ResourceUtils.getString(
                        WizardAction.class,
                        RESOURCE_CANCELING_PROGRESS_TITLE));
                
                new NbiThread() {
                    public void run() {
                        ((WizardAction) action).cancel();
                        action.getWizard().getFinishHandler().cancel();
                    }
                }.start();
            }
        }
        
        @Override
        public NbiButton getDefaultEnterButton() {
            return container.getCancelButton();
        }
        
        public void progressUpdated(final Progress progress) {
            if (progress != null) {
                if (titleLabel != null) {
                    titleLabel.setText(progress.getTitle());
                }
                
                if (detailLabel != null) {
                    detailLabel.setText(progress.getDetail());
                }
                
                if (progressBar != null) {
                    progressBar.setValue(progress.getPercentage());
                }
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // titleLabel ///////////////////////////////////////////////////////////
            titleLabel = new NbiLabel();
            titleLabel.setFocusable(true);
            
            // progressBar //////////////////////////////////////////////////////////
            progressBar = new NbiProgressBar();
            
            // detailLabel //////////////////////////////////////////////////////////
            detailLabel = new NbiLabel(true);
            detailLabel.setFocusable(true);
            
            // this /////////////////////////////////////////////////////////////////
            add(titleLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.SOUTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // ??? (padx, pady)
            add(progressBar, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.NORTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // ??? (padx, pady)
            add(detailLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // ??? (padx, pady)
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String RESOURCE_CANCEL_DIALOG_TITLE = 
            "WAC.cancel.dialog.title";
    private static final String RESOURCE_CANCEL_DIALOG_TEXT = 
            "WA.cancel.dialog.text";
    private static final String RESOURCE_INTERRUPTED_EXCEPTION = 
            "WA.error.interrupted.exception"; // NOI18N
    private static final String RESOURCE_CANCELING_PROGRESS_TITLE =
            "WA.canceling.progress.title"; // NOI18N
}
