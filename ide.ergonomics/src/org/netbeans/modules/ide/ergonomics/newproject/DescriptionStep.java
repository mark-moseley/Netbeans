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
package org.netbeans.modules.ide.ergonomics.newproject;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.netbeans.modules.ide.ergonomics.fod.ModulesInstaller;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FoDFileSystem;
import org.netbeans.modules.ide.ergonomics.fod.Feature2LayerMapping;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public class DescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {

    private ContentPanel panel;
    private boolean isValid = false;
    private ProgressHandle handle = null;
    private JComponent progressComponent;
    private JLabel mainLabel;
    private Collection<UpdateElement> forInstall = null;
    private Collection<UpdateElement> forEnable = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static FindComponentModules finder = null;
    private FeatureInfo info;
    private WizardDescriptor wd;
    private FileObject fo;

    public Component getComponent () {
        if (panel == null) {
            panel = new ContentPanel (getBundle ("DescriptionPanel_Name"));
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                        public void propertyChange (PropertyChangeEvent evt) {
                            if (ContentPanel.FINDING_MODULES.equals (evt.getPropertyName ())) {
                                doFindingModues.run ();
                            }
                        }
                    });
        }
        return panel;
    }

    public HelpCtx getHelp () {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid () {
        return isValid &&
                forInstall != null && ! forInstall.isEmpty ();
    }

    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }
    
    private Runnable doFindingModues = new Runnable () {
        public void run () {
            if (SwingUtilities.isEventDispatchThread ()) {
                RequestProcessor.getDefault ().post (doFindingModues);
                return;
            }
            RequestProcessor.Task findingTask = getFinder ().getFindingTask ();
            if (findingTask != null && findingTask.isFinished ()) {
                presentModulesForActivation ();
            } else {
                if (findingTask == null) {
                    findingTask = getFinder ().createFindingTask ();
                    findingTask.schedule (10);
                }
                if (findingTask.getDelay () > 0) {
                    findingTask.schedule (10);
                }
                findingTask.addTaskListener (new TaskListener () {
                            public void taskFinished (Task task) {
                                presentModulesForActivation ();
                                return;
                            }
                        });
                presentFindingModules ();
            }
        }
    };

    private void presentModulesForActivation () {
        forInstall = getFinder ().getModulesForInstall ();
        forEnable = getFinder ().getModulesForEnable ();
        if (forInstall != null && ! forInstall.isEmpty ()) {
            presentModulesForInstall ();
        } else if (forEnable != null && ! forEnable.isEmpty ()) {
            presentModulesForEnable ();
        } else {
            presentNone ();
        }
    }

    private void presentModulesForInstall () {
        if (handle != null) {
            handle.finish ();
            panel.replaceComponents ();
            handle = null;
        }
        Collection<UpdateElement> elems = getFinder ().getModulesForInstall ();
        if (elems != null && !elems.isEmpty ()) {
            isValid = true;
            Collection<UpdateElement> visible = getFinder().getVisibleUpdateElements (elems);
            String names = ModulesInstaller.presentUpdateElements (visible);
            panel.replaceComponents(
                new JLabel(getBundle ("DescriptionStep_BrokenModulesFound", visible.size(), names))
            );
            forInstall = elems;
        } else {
            panel.replaceComponents (
                new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
                new JLabel ()
            );
            isValid = false;
        }
        fireChange ();
    }
    
    private void presentNone () {
        panel.replaceComponents (
            new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
            new JLabel ()
        );
        isValid = false;
    }
    
    private void presentModulesForEnable () {
        if (handle != null) {
            handle.finish ();
            panel.replaceComponents ();
            handle = null;
        }
        Collection<UpdateElement> elems = getFinder ().getModulesForEnable ();
        if (elems != null && !elems.isEmpty ()) {
            isValid = true;
            Collection<UpdateElement> visible = getFinder().getVisibleUpdateElements (elems);
            final String names = ModulesInstaller.presentUpdateElements (visible);
            final JPanel[] pan = new JPanel[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        pan[0] = new ConfigurationPanel(names, new Callable<JComponent>() {

                            public JComponent call() throws Exception {
                                FoDFileSystem.getInstance().refresh();
                                waitForDelegateWizard ();
                                return new JLabel(" ");
                            }
                        }, info);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            panel.replaceComponents(pan[0]);
            forEnable = elems;
        } else {
            panel.replaceComponents (
                new JLabel (getBundle ("DescriptionStep_NoMissingModules1")),
                new JLabel ()
            );
            isValid = false;
        }
        fireChange ();
    }
    
    private FindComponentModules getFinder () {
        assert finder != null : "Finder needs to be created first!";
        return finder;
    }

    private void presentFindingModules () {
        handle = ProgressHandleFactory.createHandle (ContentPanel.FINDING_MODULES);
        progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        mainLabel = new JLabel (getBundle ("DescriptionStep_FindingRuns_Wait"));
        JLabel detailLabel = new JLabel (getBundle ("DescriptionStep_FindingRuns"));

        handle.setInitialDelay (0);
        handle.start ();
        panel.replaceComponents (mainLabel, progressComponent, detailLabel);
    }

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (DescriptionStep.class, key, params);
    }

    public void readSettings (WizardDescriptor settings) {
        wd = settings;
        Object o = settings.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject : o + " is not null and instanceof FileObject.";
        FileObject fo = (FileObject) o;
        info = FoDFileSystem.getInstance ().whichProvides(fo);
        finder = new FindComponentModules(info);
    }

    public void storeSettings (WizardDescriptor settings) {
        if (forInstall != null && ! forInstall.isEmpty ()) {
            settings.putProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_INSTALL, forInstall);
            Collection<UpdateElement> notNeedApproveLicense = new HashSet<UpdateElement> ();
            for (UpdateElement el : forInstall) {
                if (UpdateUnitProvider.CATEGORY.STANDARD == el.getSourceCategory ()) {
                    notNeedApproveLicense.add (el);
                }
            }
            if (! notNeedApproveLicense.isEmpty ()) {
                settings.putProperty (FeatureOnDemanWizardIterator.APPROVED_ELEMENTS, notNeedApproveLicense);
            }
            fireChange ();
        }
        if (forEnable != null && ! forEnable.isEmpty ()) {
            settings.putProperty (FeatureOnDemanWizardIterator.CHOSEN_ELEMENTS_FOR_ENABLE, forEnable);
            fireChange ();
        }
    }

    private void waitForDelegateWizard () {
        Object o = wd.getProperty (FeatureOnDemanWizardIterator.CHOSEN_TEMPLATE);
        assert o != null && o instanceof FileObject :
            o + " is not null and instanceof FileObject";
        final String templateResource = ((FileObject) o).getPath ();
        fo = null;
        while (fo == null) {
            RequestProcessor.getDefault ().post (new Runnable () {
               public void run () {
                   fo = Repository.getDefault ().getDefaultFileSystem ().findResource (templateResource);
               }
            }, 100).waitFinished ();
        }
        WizardDescriptor.InstantiatingIterator<?> iterator = readWizard(fo);
        iterator.initialize (wd);
        wd.putProperty (FeatureOnDemanWizardIterator.DELEGATE_ITERATOR, iterator);
        fireChange ();
    }
    
    public static WizardDescriptor.InstantiatingIterator<?> readWizard(FileObject fo) {
        Object o = fo.getAttribute ("instantiatingIterator");
        if (o == null) {
            o = fo.getAttribute ("templateWizardIterator");
        }
        if (o instanceof WizardDescriptor.InstantiatingIterator) {
            // OK
        } else if (o instanceof TemplateWizard.Iterator) {
            final TemplateWizard.Iterator it = (TemplateWizard.Iterator) o;
            o = new WizardDescriptor.InstantiatingIterator<WizardDescriptor>() {
                private TemplateWizard tw;

                public Set instantiate() throws IOException {
                    return it.instantiate(tw);
                }
                public void initialize(WizardDescriptor wizard) {
                    tw = (TemplateWizard)wizard;
                    it.initialize(tw);
                }

                public void uninitialize(WizardDescriptor wizard) {
                    it.uninitialize((TemplateWizard)wizard);
                    tw = null;
                }

                public Panel<WizardDescriptor> current() {
                    return it.current();
                }

                public String name() {
                    return it.name();
                }

                public boolean hasNext() {
                    return it.hasNext();
                }

                public boolean hasPrevious() {
                    return it.hasPrevious();
                }

                public void nextPanel() {
                    it.nextPanel();
                }

                public void previousPanel() {
                    it.previousPanel();
                }

                public void addChangeListener(ChangeListener l) {
                    it.addChangeListener(l);
                }

                public void removeChangeListener(ChangeListener l) {
                    it.removeChangeListener(l);
                }
            };
        }

        assert o != null && o instanceof WizardDescriptor.InstantiatingIterator :
            o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
        return (WizardDescriptor.InstantiatingIterator<?>)o;
    }
}

