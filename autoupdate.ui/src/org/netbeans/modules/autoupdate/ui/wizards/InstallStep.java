/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.NetworkProblemPanel;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.actions.BalloonManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private OperationPanel panel;
    private PanelBodyContainer component;
    private InstallUnitWizardModel model = null;
    private WizardDescriptor wd = null;
    private Restarter restarter = null;
    private ProgressHandle systemHandle = null;
    private ProgressHandle spareHandle = null;
    private boolean spareHandleStarted = false;
    private boolean indeterminateProgress = false;
    private int processedUnits = 0;
    private int totalUnits = 0;
    private final Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.wizards.InstallPanel");
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    
    private static final String TEXT_PROPERTY = "text";
    
    private static final String HEAD_DOWNLOAD = "InstallStep_Header_Download_Head";
    private static final String CONTENT_DOWNLOAD = "InstallStep_Header_Download_Content";
    
    private static final String HEAD_VERIFY = "InstallStep_Header_Verify_Head";
    private static final String CONTENT_VERIFY = "InstallStep_Header_Verify_Content";
    
    private static final String HEAD_INSTALL = "InstallStep_Header_Install_Head";
    private static final String CONTENT_INSTALL = "InstallStep_Header_Install_Content";
    
    private static final String HEAD_INSTALL_DONE = "InstallStep_Header_InstallDone_Head";
    private static final String CONTENT_INSTALL_DONE = "InstallStep_Header_InstallDone_Content";
    
    private static final String HEAD_RESTART = "InstallStep_Header_Restart_Head";
    private static final String CONTENT_RESTART = "InstallStep_Header_Restart_Content";
    
    private boolean wasStored = false;
    private boolean runInBg = false;
    
    /** Creates a new instance of OperationDescriptionStep */
    public InstallStep (InstallUnitWizardModel model) {
        this.model = model;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new OperationPanel (true);
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OperationPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            RequestProcessor.Task it = createInstallTask ();
                            PluginManagerUI.registerRunningTask (it);
                            it.waitFinished ();
                            PluginManagerUI.unregisterRunningTask ();
                        } else if (OperationPanel.RUN_IN_BACKGROUND.equals (evt.getPropertyName ())) {
                            setRunInBackground (true);
                        }
                    }
            });
            component = new PanelBodyContainer (getBundle (HEAD_DOWNLOAD), getBundle (CONTENT_DOWNLOAD), panel);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private RequestProcessor.Task createInstallTask () {
        return RequestProcessor.getDefault ().create (new Runnable () {
            public void run () {
                doDownloadAndVerificationAndInstall ();
            }
        });
    }
    
    private void doDownloadAndVerificationAndInstall () {
        Validator v = null;
        // download
        if ((v = handleDownload ()) != null) {
            Installer i = null;
            // verifation
            if ((i = handleValidation (v)) != null) {
                // installation
                Restarter r = null;
                if ((r = handleInstall (i)) != null) {
                    presentInstallNeedsRestart (r);
                } else {
                    presentInstallDone ();
                }
            }
        }
        fireChange ();
    }
    
    private Validator validator;
    
    private Validator handleDownload () {
        validator = null;
        final InstallSupport support = model.getInstallSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        
        boolean finish = false;
        while (! finish) {
            finish = tryPerformDownload ();
        }
        
        return validator;
    }
    
    private boolean runInBackground () {
        return runInBg;
    }
    
    private void setRunInBackground (boolean inBackground) {
        if (inBackground == runInBg) {
            return ;
        }
        runInBg = inBackground;
        if (inBackground) {
            if (getComponent ().getRootPane () != null) {
                getComponent ().getRootPane ().setVisible (false);
            }
            if (model.getPluginManager () != null) {
                model.getPluginManager ().close ();
            }
            if (spareHandle != null && ! spareHandleStarted) {
                indeterminateProgress = true;
                spareHandle.start ();
                spareHandleStarted = true;
            }
        } else {
            assert false : "Cannot set runInBackground to false";
        }
    }
    
    private boolean handleCancel () {
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
            spareHandleStarted = false;
        }
        if (systemHandle != null) {
            systemHandle.finish ();
        }
        try {
            model.doCleanup (true);
        } catch (OperationException x) {
            Logger.getLogger (InstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
        }
        return true;
    }
    
    private boolean tryPerformDownload () {
        validator = null;
        final InstallSupport support = model.getInstallSupport ();
        try {
            ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"));
            JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
            JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
            JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
            if (runInBackground ()) {
                systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                return handleCancel ();
                            }
                        });
                handle = systemHandle;
            } else {
                spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                handleCancel ();
                                return true;
                            }
                        });
                totalUnits = model.getBaseContainer ().listAll ().size ();
                processedUnits = 0;
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }

            handle.setInitialDelay (0);
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);

            validator = support.doDownload (handle, Utilities.isGlobalInstallation());
            if (validator == null) return true;
            if (model.getAdditionallyInstallSupport () != null) {
                handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"));
                ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                validator = model.getAdditionallyInstallSupport ().doDownload (handle, Utilities.isGlobalInstallation());
            }
            if (validator == null) return true;
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
            if (spareHandle != null && spareHandleStarted) {
                spareHandle.finish ();
                spareHandleStarted = false;
            }
        } catch (OperationException ex) {
            assert OperationException.ERROR_TYPE.PROXY.equals (ex.getErrorType ());
            log.log (Level.INFO, ex.getMessage (), ex);
            if (runInBackground ()) {
                handleCancel ();
                notifyNetworkProblem (ex);
            } else {
                JButton tryAgain = new JButton ();
                Mnemonics.setLocalizedText (tryAgain, getBundle ("InstallStep_NetworkProblem_Continue")); // NOI18N
                NetworkProblemPanel problem = new NetworkProblemPanel (
                        getBundle ("InstallStep_NetworkProblem_Text", ex.getLocalizedMessage ()), // NOI18N
                        new JButton [] { tryAgain, model.getCancelButton (wd) });
                Object ret = problem.showNetworkProblemDialog ();
                if (tryAgain.equals(ret)) {
                    // try again
                    return false;
                } else if (DialogDescriptor.CLOSED_OPTION.equals (ret)) {
                    handleCancel ();
                }
            }
        }
        return true;
        
    }
    
    private Installer handleValidation (Validator v) {
        component.setHeadAndContent (getBundle (HEAD_VERIFY), getBundle (CONTENT_VERIFY));
        final InstallSupport support = model.getInstallSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        if (runInBackground ()) {
            systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                handleCancel ();
                                return true;
                            }
                        });
            handle = systemHandle;
        } else {
            spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"),
                    new Cancellable () {
                        public boolean cancel () {
                            handleCancel ();
                            return true;
                        }
                    });
            totalUnits = model.getBaseContainer ().listAll ().size ();
            processedUnits = 0;
            if (indeterminateProgress) {
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }
        }
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
        }
        Installer tmpInst = null;
        
        try {
            tmpInst = support.doValidate (v, handle);
            if (tmpInst == null) return null;
            if (model.getAdditionallyInstallSupport () != null) {
                handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"));
                ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                tmpInst = model.getAdditionallyInstallSupport ().doValidate (v, handle);
            }
            if (tmpInst == null) return null;
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
            NetworkProblemPanel problem = new NetworkProblemPanel (ex.getLocalizedMessage ());
            problem.showNetworkProblemDialog ();
            handleCancel ();
            return null;
        }
        final Installer inst = tmpInst;
        List<UpdateElement> unsigned = new ArrayList<UpdateElement> ();
        List<UpdateElement> untrusted = new ArrayList<UpdateElement> ();
        String certs = "";
        for (UpdateElement el : model.getAllUpdateElements ()) {
            InstallSupport addSupport = model.getAdditionallyInstallSupport ();
            if (! (support.isSigned (inst, el) || (addSupport != null && addSupport.isSigned (inst, el)))) {
                unsigned.add (el);
            } else if (! (support.isTrusted (inst, el) || (addSupport != null && addSupport.isTrusted (inst, el)))) {
                untrusted.add (el);
                String cert = support.getCertificate (inst, el);
                if (cert != null && cert.length () > 0) {
                    certs += getBundle ("ValidationWarningPanel_ShowCertificateFormat", el.getDisplayName (), cert);
                }
            }
        }
        if (untrusted.size () > 0 || unsigned.size () > 0 && ! runInBackground ()) {
            ValidationWarningPanel p = new ValidationWarningPanel (unsigned, untrusted);
            final JButton showCertificate = new JButton ();
            Mnemonics.setLocalizedText (showCertificate, getBundle ("ValidationWarningPanel_ShowCertificateButton"));
            final String certificate = certs;
            showCertificate.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (showCertificate.equals (e.getSource ())) {
                        JTextArea ta = new JTextArea (certificate);
                        ta.setEditable (false);
                        DialogDisplayer.getDefault().notify (new NotifyDescriptor.Message (ta));
                    }
                }
            });
            final JButton canContinue = new JButton ();
            Mnemonics.setLocalizedText (canContinue, getBundle ("ValidationWarningPanel_ContinueButton"));
            final JButton cancel = model.getCancelButton (wd);
            DialogDescriptor dd = new DialogDescriptor (p, getBundle ("ValidationWarningPanel_Title"));
            dd.setOptions (new JButton [] {canContinue, cancel});
            dd.setClosingOptions (new JButton [] {canContinue, cancel});
            dd.setOptionType (NotifyDescriptor.WARNING_MESSAGE);
            if (! untrusted.isEmpty () && certs.length () > 0) {
                dd.setAdditionalOptions (new JButton [] {showCertificate});
            }
            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
            if (! canContinue.equals (dd.getValue ())) {
                if (! cancel.equals (dd.getValue ())) cancel.doClick ();
                return null;
            }
            assert canContinue.equals (dd.getValue ());
        }
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
        return inst;
    }
    
    private Restarter handleInstall (Installer i) {
        component.setHeadAndContent (getBundle (HEAD_INSTALL), getBundle (CONTENT_INSTALL));
        InstallSupport support = model.getInstallSupport();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        model.modifyOptionsForDisabledCancel (wd);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        if (runInBackground ()) {
            systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
            handle = systemHandle;
        } else {
            spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
            totalUnits = model.getBaseContainer ().listAll ().size ();
            processedUnits = 0;
            if (indeterminateProgress) {
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }
        }
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        Restarter r = null;
        
        try {
            r = support.doInstall (i, handle);
            if (model.getAdditionallyInstallSupport () != null) {
                handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
                if (r == null) {
                    ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                    r = model.getAdditionallyInstallSupport ().doInstall (i, handle);
                }
            }
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
        }
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
        }
        return r;
    }
    
    private void presentInstallDone () {
        component.setHeadAndContent (getBundle (HEAD_INSTALL_DONE), getBundle (CONTENT_INSTALL_DONE));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("InstallStep_InstallDone_Text"), InstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false, model.getOperation ()));
        panel.hideRunInBackground ();
    }
    
    private void presentInstallNeedsRestart (Restarter r) {
        component.setHeadAndContent (getBundle (HEAD_RESTART), getBundle (CONTENT_RESTART));
        model.modifyOptionsForDoClose (wd, true);
        restarter = r;
        panel.setRestartButtonsVisible (true);
        panel.setBody (getBundle ("InstallStep_InstallDone_Text"), InstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false, model.getOperation ()));
        panel.hideRunInBackground ();
        if (runInBackground ()) {
            InstallSupport support = model.getInstallSupport ();
            support.doRestartLater (restarter);
            if (model.getAdditionallyInstallSupport () != null) {
                model.getAdditionallyInstallSupport ().doRestartLater (restarter);
            }
            try {
                model.doCleanup (false);
            } catch (OperationException x) {
                log.log (Level.INFO, x.getMessage (), x);
            }
            notifyRestartNeeded (support, r);
        }
    }
    
    private static RestartNeededNotification.UpdatesFlasher flasher;
    
    private void notifyRestartNeeded (final InstallSupport support, final Restarter r) {
        // Some modules found
        final Runnable onMouseClick = new Runnable () {
            public void run () {
                try {
                    support.doRestart (r, spareHandle);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
            }
        };
        flasher = RestartNeededNotification.getFlasher (onMouseClick);
        assert flasher != null : "Updates Flasher cannot be null.";
        flasher.startFlashing ();
        final Runnable showBalloon = new Runnable () {
            public void run () {
                JLabel balloon = new JLabel (getBundle ("InstallSupport_InBackground_RestartNeeded")); // NOI18N
                BalloonManager.show (flasher, balloon, new AbstractAction () {
                    public void actionPerformed (ActionEvent e) {
                        onMouseClick.run ();
                    }
                });
            }
        };
        SwingUtilities.invokeLater (showBalloon);
        flasher.addMouseListener (new MouseAdapter () {
            @Override
            public void mouseEntered (MouseEvent e) {
                showBalloon.run ();
            }
        });
    }

    private static NetworkProblemNotification.UpdatesFlasher nwProblemFlasher;
    
    private void notifyNetworkProblem (final OperationException ex) {
        // Some network problem found
        final Runnable onMouseClick = new Runnable () {
            public void run () {
                NetworkProblemPanel problem = new NetworkProblemPanel (ex.getLocalizedMessage ());
                problem.showNetworkProblemDialog ();
                if (nwProblemFlasher != null) {
                    nwProblemFlasher.disappear ();
                }
                BalloonManager.dismiss ();
            }
        };
        nwProblemFlasher = NetworkProblemNotification.getFlasher (onMouseClick);
        assert nwProblemFlasher != null : "Updates Flasher cannot be null.";
        nwProblemFlasher.startFlashing ();
        final Runnable showBalloon = new Runnable () {
            public void run () {
                JLabel balloon = new JLabel (getBundle ("InstallSupport_InBackground_NetworkError")); // NOI18N
                BalloonManager.show (nwProblemFlasher, balloon, new AbstractAction () {
                    public void actionPerformed (ActionEvent e) {
                        onMouseClick.run ();
                    }
                });
            }
        };
        SwingUtilities.invokeLater (showBalloon);
        nwProblemFlasher.addMouseListener (new MouseAdapter () {
            @Override
            public void mouseEntered (MouseEvent e) {
                showBalloon.run ();
            }
        });
    }

    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        this.wasStored = false;
    }

    public void storeSettings (WizardDescriptor wd) {
        assert ! WizardDescriptor.PREVIOUS_OPTION.equals (wd.getValue ()) : "Cannot invoke Back in this case.";
        if (wasStored) {
            return ;
        }
        this.wasStored = true;
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (InstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        } else if (restarter != null) {
            InstallSupport support = model.getInstallSupport ();
            assert support != null : "OperationSupport cannot be null because OperationContainer " +
                    "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
            if (panel.restartNow ()) {
                try {
                    support.doRestart (restarter, null);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                
            } else {
                support.doRestartLater (restarter);
                if (model.getAdditionallyInstallSupport () != null) {
                    model.getAdditionallyInstallSupport ().doRestartLater (restarter);
                }
                try {
                    model.doCleanup (false);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                return ;
            }
        } else {
            try {
                model.doCleanup (! WizardDescriptor.FINISH_OPTION.equals (wd.getValue ()));
            } catch (OperationException x) {
                log.log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    public boolean isValid() {
        return true;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }

    private String getBundle (String key, Object... params) {
        return NbBundle.getMessage (InstallStep.class, key, params);
    }
}
