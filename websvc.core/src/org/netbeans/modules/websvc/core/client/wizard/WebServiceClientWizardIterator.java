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

package org.netbeans.modules.websvc.core.client.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements TemplateWizard.Iterator {

    private int index = 0;
    private WizardDescriptor.Panel [] panels;

    private TemplateWizard wiz;
    // !PW FIXME How to handle freeform???
    private Project project;

    /** Entry point specified in layer
     */
    public static WebServiceClientWizardIterator create() {
        return new WebServiceClientWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }

    public void initialize(TemplateWizard wizard) {
        wiz = wizard;
        project = Templates.getProject(wiz);

        index = 0;
        panels = createPanels();

        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = JaxWsUtils.createSteps (beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }

            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
        }
    }

    public void uninitialize(TemplateWizard wizard) {
        wiz = null;
        panels = null;
    }
    
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        ClientCreator creator = CreatorProvider.getClientCreator(project, wiz);
        if (creator!=null) creator.createClient();
                
        return Collections.singleton(dTemplate);
    }

    public String name() {
        return NbBundle.getMessage(WebServiceClientWizardIterator.class, "LBL_WebServiceClient"); // NOI18N
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
       return panels[index];
    }

    public boolean hasNext() {
        return index < panels.length - 1;  
    }

    public void nextPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index++;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void previousPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index--;
    }

    public void addChangeListener(ChangeListener l) {
        // nothing to do yet
    }

    public void removeChangeListener(ChangeListener l) {
        // nothing to do yet
    }
}
