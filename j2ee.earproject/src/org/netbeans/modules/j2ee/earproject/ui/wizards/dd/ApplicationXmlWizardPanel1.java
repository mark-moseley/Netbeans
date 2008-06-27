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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.earproject.ui.wizards.dd;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Adopted from {@link org.netbeans.modules.j2ee.ejbcore.ejb.wizard.dd.EjbJarXmlWizardPanel1}.
 * @author Martin Adamek
 */
public class ApplicationXmlWizardPanel1 implements WizardDescriptor.Panel {

    // generated by apisupport wizard

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    private final ApplicationXmlVisualPanel1 component = new ApplicationXmlVisualPanel1();
    private WizardDescriptor wizardDescriptor;
    private EarProject project;

    public ApplicationXmlWizardPanel1() {
        component.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireChangeEvent();
            }
        });
    }

    FileObject getSelectedLocation() {
        return component.getSelectedLocation();
    }

    EarProject getProject() {
        return project;
    }

    public Component getComponent() {
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        if (component.getSelectedLocation() == null
                || component.getCreatedFile() == null
                || component.getCreatedFile().canRead()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(ApplicationXmlWizardPanel1.class,"ERR_FileExistsOrNoValidLocation")); //NOI18N
            return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", " "); //NOI18N
        return true;
    }

    public final void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public final void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> listenersIterator;
        synchronized (listeners) {
            listenersIterator = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent changeEvent = new ChangeEvent(this);
        while (listenersIterator.hasNext()) {
            listenersIterator.next().stateChanged(changeEvent);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = (EarProject)Templates.getProject(wizardDescriptor);
            component.setProject(project);
        }

        wizardDescriptor.putProperty ("NewFileWizard_Title", // NOI18N
            NbBundle.getMessage(ApplicationXmlWizardPanel1.class, "TITLE_StdDdFile"));
        
    }

    public void storeSettings(Object settings) {}

}

