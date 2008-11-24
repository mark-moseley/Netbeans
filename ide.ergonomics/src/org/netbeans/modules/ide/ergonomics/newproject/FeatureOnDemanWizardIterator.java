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

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.ide.ergonomics.fod.FoDFileSystem;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Make project.
 */
public final class FeatureOnDemanWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {
    public static final String CHOSEN_ELEMENTS_FOR_INSTALL = "chosen-elements-for-install"; // NOI18N
    public static final String CHOSEN_ELEMENTS_FOR_ENABLE = "chosen-elements-for-enable"; // NOI18N
    public static final String APPROVED_ELEMENTS = "approved-elements"; // NOI18N
    public static final String CHOSEN_TEMPLATE = "chosen-template"; // NOI18N
    public static final String DELEGATE_ITERATOR = "delegate-iterator"; // NOI18N
    
    private WizardDescriptor.InstantiatingIterator delegateIterator;
    private Boolean doInstall = null;
    private Boolean doEnable = null;
    private FileObject template;
    private LicenseStep licenseStep = null;
    
    public FeatureOnDemanWizardIterator (FileObject template) {
        this.template = template;
    }
    
    public static WizardDescriptor.InstantiatingIterator newProject (FileObject fo) {
        try {
            WizardDescriptor.InstantiatingIterator it = getRealNewMakeProjectWizardIterator (fo);
            if (it != null) {
                return it;
            }
        } catch (Exception x) {
            // x.printStackTrace ();
        }
        return new FeatureOnDemanWizardIterator (fo);
    }
    
    private static WizardDescriptor.InstantiatingIterator getRealNewMakeProjectWizardIterator (FileObject template) {
        WizardDescriptor.InstantiatingIterator res = null;
        if (FoDFileSystem.getInstance().getDelegateFileSystem (template) != null) {
            return null;
        }
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource (template.getPath ());
        if (fo != null) {
            Object o = fo.getAttribute ("instantiatingIterator");
            if (o == null) {
                o = fo.getAttribute ("templateWizardIterator");
            }
            assert o != null && o instanceof WizardDescriptor.InstantiatingIterator :
                o + " is not null and instanceof WizardDescriptor.InstantiatingIterator";
            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator) o;
            if (! FeatureOnDemanWizardIterator.class.equals (o.getClass ())) {
                return iterator;
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private void compatPanels () {
        if (wiz != null && licenseStep != null) {
            Collection<UpdateElement> approved = (Collection<UpdateElement>) wiz.getProperty (APPROVED_ELEMENTS);
            Collection<UpdateElement> chosen = (Collection<UpdateElement>) wiz.getProperty (CHOSEN_ELEMENTS_FOR_INSTALL);
            boolean allApproved = true;
            for (UpdateElement el : chosen) {
                allApproved &= approved.contains (el);
            }
            if (allApproved) {
                panels.remove (licenseStep);
            }
        }
    }
    
    private void createPanels () {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
            panels.add (new DescriptionStep ());
            panels.add (new ToBeContinuedStep ());
            names = new String [] {
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "DescriptionStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "ToBeContinuedStep_Name")
            
            };
            String[] steps = new String [panels.size ()];
            assert steps.length == names.length : "As same names as steps must be";
            int i = 0;
            for (WizardDescriptor.Panel p : panels) {
                Component c = p.getComponent ();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps [i] = c.getName ();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                }
                i ++;
            }
        }
    }
    
    private void createPanelsForInstall () {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
            panels.add (new DescriptionStep ());
            licenseStep = new LicenseStep ();
            panels.add (licenseStep);
            panels.add (new InstallStep ());
            panels.add (new ToBeContinuedStep ());
            names = new String [] {
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "DescriptionStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "LicenseStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "InstallStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "ToBeContinuedStep_Name")
            
            };
            String[] steps = new String [panels.size ()];
            assert steps.length == names.length : "As same names as steps must be";
            int i = 0;
            for (WizardDescriptor.Panel p : panels) {
                Component c = p.getComponent ();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps [i] = c.getName ();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                }
                i ++;
            }
        }
    }
    
    private void createPanelsForEnable () {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
            panels.add (new DescriptionStep ());
            panels.add (new EnableStep ());
            panels.add (new ToBeContinuedStep ());
            names = new String [] {
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "DescriptionStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "EnableStep_Name"),
                NbBundle.getMessage (FeatureOnDemanWizardIterator.class, "ToBeContinuedStep_Name")
            
            };
            String[] steps = new String [panels.size ()];
            assert steps.length == names.length : "As same names as steps must be";
            int i = 0;
            for (WizardDescriptor.Panel p : panels) {
                Component c = p.getComponent ();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps [i] = c.getName ();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                }
                i ++;
            }
        }
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().instantiate ();
        }
        return null;
    }
    
    
    public Set instantiate (ProgressHandle handle) throws IOException {
        InstantiatingIterator it = getDelegateIterator ();
        if (it != null) {
            if (it instanceof ProgressInstantiatingIterator) {
                return ((ProgressInstantiatingIterator) getDelegateIterator ()).instantiate (handle);
            } else {
                return getDelegateIterator ().instantiate ();
            }
        }
        return null;
    }
    
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels = null;
    private String [] names;

    private WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        wiz.putProperty (CHOSEN_TEMPLATE, template);
        index = 0;
        createPanels (); // NOI18N
    }
    public void uninitialize(WizardDescriptor wiz) {
        if (getDelegateIterator () != null) {
            getDelegateIterator ().uninitialize (wiz);
        }
        this.wiz = null;
        panels = null;
    }
    
    @SuppressWarnings ("unchecked")
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().current ();
        }
        assert panels != null;
        return panels.get (index);
    }

    public String name () {
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().name ();
        }
        return names [index];
    }

    public boolean hasNext () {
        compatPanels ();
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().hasNext ();
        }
        return index < panels.size () - 1;
    }

    public boolean hasPrevious () {
        compatPanels ();
        if (getDelegateIterator () != null) {
            return getDelegateIterator ().hasPrevious ();
        }
        return index > 0 && !(current () instanceof InstallStep);
    }

    public void nextPanel () {
        if (getDelegateIterator () != null) {
            if (getDelegateIterator ().hasNext ()) {
                getDelegateIterator ().nextPanel ();
            }
            return ;
        }
        if (!hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }

    public void previousPanel () {
        if (getDelegateIterator () != null) {
            if (getDelegateIterator ().hasPrevious ()) {
                getDelegateIterator ().previousPanel ();
            }
            return ;
        }
        if (!hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener (ChangeListener l) {
        if (getDelegateIterator () != null) {
            getDelegateIterator ().addChangeListener (l);
        }
    }

    public void removeChangeListener (ChangeListener l) {
        if (getDelegateIterator () != null) {
            getDelegateIterator ().removeChangeListener (l);
        }
    }
    
    private WizardDescriptor.InstantiatingIterator getDelegateIterator () {
        if (wiz != null) {
            if (delegateIterator == null) {
                Object o = wiz.getProperty (DELEGATE_ITERATOR);
                assert o == null || o instanceof WizardDescriptor.InstantiatingIterator :
                    o + " is instanceof WizardDescriptor.InstantiatingIterator or null";
                delegateIterator = (WizardDescriptor.InstantiatingIterator) o;
                if (delegateIterator == null) {
                    if (doInstall == null && doEnable == null) {
                        o = wiz.getProperty (CHOSEN_ELEMENTS_FOR_INSTALL);
                        assert o == null || o instanceof Collection :
                            o + " is instanceof Collection<UpdateElement> or null.";
                        if (o != null && ! ((Collection) o).isEmpty ()) {
                            doInstall = Boolean.TRUE;
                            panels = null;
                            createPanelsForInstall ();
                        }
                    }
                    if (doInstall == null && doEnable == null) {
                        o = wiz.getProperty (CHOSEN_ELEMENTS_FOR_ENABLE);
                        if (o != null && ! ((Collection) o).isEmpty ()) {
                            doEnable = Boolean.TRUE;
                            panels = null;
                            createPanelsForEnable ();
                        }
                    }
                }
            }
        }
        return delegateIterator;
    }

}
