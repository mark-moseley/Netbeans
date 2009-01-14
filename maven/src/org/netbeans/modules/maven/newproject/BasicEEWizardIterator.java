/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 *@author Dafe Simonek
 */
public class BasicEEWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    private Archetype[] archs;
    private String[] eeLevels;

    private BasicEEWizardIterator(String[] eeLevels, Archetype[] archs) {
        this.archs = archs;
        this.eeLevels = eeLevels;
    }
    
    public static BasicEEWizardIterator createWebAppIterator() {
        return new BasicEEWizardIterator(ArchetypeWizardUtils.EE_LEVELS, ArchetypeWizardUtils.WEB_APP_ARCHS);
    }

    public static BasicEEWizardIterator createEJBIterator() {
        return new BasicEEWizardIterator(ArchetypeWizardUtils.EE_LEVELS, ArchetypeWizardUtils.EJB_ARCHS);
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new BasicWizardPanel(eeLevels, archs, true)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_CreateProjectStep2"),
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }
    
    public Set instantiate(ProgressHandle handle) throws IOException {
        return ArchetypeWizardUtils.instantiate(handle, wiz);
    }
    
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        panels = createPanels();
        this.wiz = wiz;
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(BasicEEWizardIterator.class, "NameFormat"),
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public boolean hasPrevious() {
        return false;
    }
    
    public void nextPanel() {
    }
    
    public void previousPanel() {
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
