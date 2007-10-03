/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.i18n.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;
import java.util.Map;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;


/**
 * Wizard descriptor of i18n wizard and i18n test wizard.
 *
 * @author  Peter Zavadsky
 * @see org.openide.WizardDescriptor
 */
final class I18nWizardDescriptor extends WizardDescriptor {

    /** Preferred size for panels in i18n wizard. */
    public static final Dimension PREFERRED_DIMENSION = new Dimension(500, 300);
    
    /** Hack. In super it's private. */
    private final WizardDescriptor.Iterator<Settings> panels;

    /** Hack. In super it's private. */
    private final Settings settings;
    
    /** Next button. */
    private final JButton nextButton = new JButton();
    /** Previous button. */
    private final JButton previousButton = new JButton();
    /** Finish button. */
    private final JButton finishButton = new JButton();
    /** Cancel button. */
    private final JButton cancelButton = new JButton();

    /** Hack. Listener on root pane. In case not our button was set as default
     * (the one from superclass) our one is set as default. */
    private PropertyChangeListener rootListener;

    /** Creates new I18nWizardDescriptor */
    private I18nWizardDescriptor(WizardDescriptor.Iterator<Settings> panels, Settings settings) {
        super(panels, settings);
        
        Listener listener = new Listener();

        // Button init.
        Mnemonics.setLocalizedText(
                nextButton,
                NbBundle.getMessage(I18nWizardDescriptor.class, "CTL_Next"));
        nextButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(I18nWizardDescriptor.class, "ACSD_NEXT"));
        Mnemonics.setLocalizedText(
                previousButton,
                NbBundle.getMessage(I18nWizardDescriptor.class, "CTL_Previous"));
        previousButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(I18nWizardDescriptor.class, "ACSD_PREVIOUS"));
        Mnemonics.setLocalizedText(
                finishButton, 
                NbBundle.getMessage(I18nWizardDescriptor.class, "CTL_Finish"));
        finishButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(I18nWizardDescriptor.class, "ACSD_FINISH"));
        Mnemonics.setLocalizedText(
                cancelButton, 
                NbBundle.getMessage(I18nWizardDescriptor.class, "CTL_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(I18nWizardDescriptor.class, "ACSD_CANCEL"));
        
        nextButton.addActionListener(listener);
        previousButton.addActionListener(listener);
        finishButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        
        setOptions(new Object[] { previousButton, nextButton, finishButton, cancelButton });
        setClosingOptions(new Object[] { cancelButton });

        this.panels = panels;
        this.settings = settings;
    }

    /** Creates I18N wizard descriptor.
     * @return <code>I18nWizardDescriptor</code> instance. */
    static WizardDescriptor createI18nWizardDescriptor(WizardDescriptor.Iterator<Settings> panels, Settings settings) {
        return new I18nWizardDescriptor(panels, settings);
    }
    
    /** Overrides superclass method. */
    @Override
    protected synchronized void updateState() {
        // Do superclass typical job.
        super.updateState();

        // And do the same for our buttons.        
        WizardDescriptor.Panel current = panels.current();
        
        boolean next = panels.hasNext ();
        boolean prev = panels.hasPrevious ();
        boolean valid = current.isValid();

        nextButton.setEnabled(next && valid);
        previousButton.setEnabled(prev);
        finishButton.setEnabled(
                valid && (!next
                          || ((current instanceof FinishablePanel)
                              && ((FinishablePanel) current).isFinishPanel())));

        if (next) {
            setValue(nextButton);
        } else {
            setValue(finishButton);
        }

        setHelpCtx(current.getHelp());
        
        updateDefaultButton();
    }
           
    /** Updates default button. */
    private void updateDefaultButton() {
        JRootPane root = getRootPane();

        if(root == null)
            return;
        
        final WizardDescriptor.Panel panel = panels.current();
        if ((panel instanceof WizardDescriptor.FinishablePanel)
                && ((WizardDescriptor.FinishablePanel) panel).isFinishPanel()) {
            root.setDefaultButton(finishButton);
        } else {
            root.setDefaultButton(nextButton);
        }
    }

    
    /** Gets root pane. It's retrieved from current panel if possible. 
     * @return root pane or null of not available */
    private JRootPane getRootPane() {
        JRootPane rootPane = null;
        
        Component comp = panels.current().getComponent();
        if (comp instanceof JComponent) {
            rootPane = ((JComponent) comp).getRootPane();
        }
        
        if (rootPane != null && rootListener == null)
            // Set listener on root for cases some needless button
            // would like to become default one (the ones from superclass).
            rootPane.addPropertyChangeListener(WeakListeners.propertyChange(
                rootListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("defaultButton".equals(evt.getPropertyName())) { // NOI18N
                            Object newValue = evt.getNewValue();
                            if ((newValue != nextButton) && (newValue != finishButton)) {
                                RequestProcessor.getDefault().post(new Runnable() {
                                    public void run() {
                                        updateDefaultButton();
                                    }
                                });
                            }
                        }
                    }
                },
                rootPane
            ));
        
        return rootPane;
    }
    
    /** Listener to changes in the iterator and panels. 
     * Hack, it's private in super. */
    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource () == nextButton) {
                
                final WizardDescriptor.Panel current = panels.current();
                
                if (current instanceof ProgressMonitor) {
                    // Do the search job first.
                    RequestProcessor.getDefault().post(new ProgressThread((ProgressMonitor) current) {
                        public void handleAction() {
                            handleNextButton();
                        }
                    });
                } else { 
                    handleNextButton();
                }
            } else if (evt.getSource () == previousButton) {
                panels.previousPanel ();
                updateState ();
            } else if (evt.getSource () == finishButton) {
                final WizardDescriptor.Panel<Settings> current = panels.current();
                
                current.storeSettings(settings);                
                setValue(OK_OPTION);

                if (current instanceof ProgressMonitor) {
                    // Do the search job first.
                    RequestProcessor.getDefault().post(new ProgressThread((ProgressMonitor) current) {
                        public void handleAction() {
                            Dialog dialog = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, current.getComponent());
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    });
                }
            } else if (evt.getSource () == cancelButton) {
                panels.current().storeSettings(settings);
                setValue(CANCEL_OPTION);
            }
        }
        
        /** Helper method. It's actually next button event handler. */
        private void handleNextButton() {

            // #40531 workaround
            Runnable performer = new Runnable() {
                public void run() {
                    panels.nextPanel();
                    
                    try {
                        updateState ();
                    } catch(IllegalStateException ise) {
                        panels.previousPanel();
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ise.getMessage()));
                        updateState();
                    }
                    
                    // focus the current panel
                    WizardDescriptor.Panel current = panels.current();
                    current.getComponent().requestFocus();
                }
            };
            org.openide.util.Mutex.EVENT.writeAccess(performer);
        }
    } // End of inner class Listener;

    
    /** Class used to handle <code>ProgressMonitor</code> inseparate thread. */
    private abstract class ProgressThread implements Runnable {
        
        /** <code>ProgressMonitor</code> to handle. */
        private ProgressMonitor progressMonitor;
        
        
        /** Constructor. */
        public ProgressThread(ProgressMonitor progressMonitor) {
            this.progressMonitor = progressMonitor;
        }
        
        
        /** Implements <code>Runnable</code> interface. */
        public void run() {
            try {
                previousButton.setEnabled(false);
                nextButton.setEnabled(false);
                finishButton.setEnabled(false);
                cancelButton.setEnabled(false);

                progressMonitor.doLongTimeChanges();

                handleAction();
            } finally {
                progressMonitor.reset();
                cancelButton.setEnabled(true);
            }
        }
        
        /** Method which provides additional handling. */
        public abstract void handleAction();
    } // End of inner ProgressThread.

    /** Interface which indicates for descriptor that the panel is provides long time changes and shows
     * progress monitoring. */    
    public interface ProgressMonitor {
        
        /** Provides long time changes. */
        public void doLongTimeChanges();
        
        /** Reset after finish of the changes. Call after previous method. */
        public void reset();
    } // End of interface.
    
    
    /**
     * Kind of abstract "adapter" implementing <code>WizardDescriptor.Panel</code>
     * interface. Used by i18n wizard.
     *
     * @see org.openide.WizardDescriptor.Panel
     */
    public static abstract class Panel
            implements WizardDescriptor.Panel<I18nWizardDescriptor.Settings> {

        /** Reference to panel. */
        private Component component;

        /** Keeps only one listener. It's fine since WizardDescriptor registers always the same listener. */
        private ChangeListener changeListener;


        /** initialized in read settings **/  
        private I18nWizardDescriptor.Settings settings = null;

        /** Gets component to display. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return this instance */
        public synchronized final Component getComponent() {
            if (component == null) {
                component = createComponent();
            }

            return component;
        }

        /** Creates component. */
        protected abstract Component createComponent();

        /** Indicates if panel is valid. Implements <code>WizardDescriptor.Panel</code> interface method. 
         * @return true */
        public boolean isValid() {
            return true;
        }

        /** Reads settings at the start when the panel comes to play. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	  this.settings = settings;
        }

        /** Stores settings at the end of panel show. Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void addChangeListener(ChangeListener listener) {
            changeListener = listener;
        }

        /** Implements <code>WizardDescriptor.Panel</code> interface method. */
        public void removeChangeListener(ChangeListener listener) {
            if ((changeListener != null) && (changeListener == listener)) {
                changeListener = null;
            }
        }

        /** Fires state changed event. Helper method. */
        public final void fireStateChanged() {
            if (changeListener != null) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }

        public Project getProject() {
	  return settings.project;
	}

        public Map<DataObject,SourceData> getMap() {
	  return settings.map;
	}
 
	                	        
    } // End of nested class Panel.

  public static class Settings {
    public Settings(Map<DataObject,SourceData> map, Project project) {
      this.map = map;
      this.project = project;
    }
    public Map<DataObject,SourceData> map;
    public Project project;
  }
    
}


