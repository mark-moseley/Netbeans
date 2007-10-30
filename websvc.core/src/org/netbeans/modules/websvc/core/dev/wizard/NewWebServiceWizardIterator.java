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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.ServiceCreator;

import org.openide.WizardDescriptor;

import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebServiceWizardIterator implements TemplateWizard.Iterator /*, ItemListener*/ {
    
    private Project project;

    /** Create a new wizard iterator. */
    public NewWebServiceWizardIterator() {
    }
        
    public static NewWebServiceWizardIterator create() {
        return new NewWebServiceWizardIterator();
    }

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        
        //new WebServiceCreator(project, wiz).create();
        ServiceCreator creator = CreatorProvider.getServiceCreator(project, wiz);
        if (creator!=null) {
            creator.createService();
        }
                
        return Collections.singleton(dTemplate);
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;
    private transient WizardDescriptor.Panel<WizardDescriptor> bottomPanel;

//convert Java class not implemented for 5.5 release
//    private int serviceType = WizardProperties.FROM_SCRATCH;
    
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        bottomPanel = new WebServiceType(project, wiz);
//convert Java class not implemented for 5.5 release
//        ((WebServiceTypePanel) bottomPanel.getComponent()).addItemListener(this);
        WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0)
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, bottomPanel));
        else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, bottomPanel, true));

        JComponent comp = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(comp, NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_Webservice_Name") );
        Util.hideLabelAndLabelFor(comp, NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));        
        
        panels = new WizardDescriptor.Panel[] {
            firstPanel,
//convert Java class not implemented for 5.5 release
//            new WebServiceMethodChooser()
        };
        
        // Creating steps.
        Object prop = this.wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }
//convert Java class not implemented for 5.5 release
//        ((WebServiceTypePanel) bottomPanel.getComponent()).removeItemListener(this);        
        panels = null;
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_WizardStepsCount"), Integer.valueOf(index + 1).toString(), Integer.valueOf(panels.length).toString()); //NOI18N
    }
    
    public boolean hasNext() {
//convert Java class not implemented for 5.5 release
//        if (index == 0 && serviceType != WizardProperties.CONVERT_JAVA_CLASS)
//            return false;
//        else
            return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

//convert Java class not implemented for 5.5 release
//    public void itemStateChanged(ItemEvent e) {
//        serviceType = ((WebServiceTypePanel) bottomPanel.getComponent()).getServiceType();
//        //refresh wizard buttons
//        wiz.setTitleFormat(wiz.getTitleFormat());
//    }

}
