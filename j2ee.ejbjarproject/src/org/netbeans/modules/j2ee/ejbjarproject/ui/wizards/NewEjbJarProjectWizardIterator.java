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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.modules.j2ee.common.project.ui.ProjectLocationWizardPanel;
import org.netbeans.modules.j2ee.common.project.ui.ProjectServerWizardPanel;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewEjbJarProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[] {
        NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
        NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "NewEjbJarProjectWizardIterator.secondpanel"),
    };

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ProjectLocationWizardPanel(J2eeModule.EJB, 
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "TXT_NewWebApp"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName")), // NOI18N
            new ProjectServerWizardPanel(J2eeModule.EJB, 
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "NewEjbJarProjectWizardIterator.secondpanel"),
                    NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "TXT_NewWebApp"),
                    true, false, false, false, false, true),
        };
    }

    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NewEjbJarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(ProjectLocationWizardPanel.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty(ProjectLocationWizardPanel.NAME);
        String serverInstanceID = (String) wiz.getProperty(ProjectServerWizardPanel.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(ProjectServerWizardPanel.J2EE_LEVEL);
        String librariesDefinition =
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SHARED_LIBRARIES));
        String serverLibraryName = (String) wiz.getProperty(ProjectServerWizardPanel.WIZARD_SERVER_LIBRARY);
        
        AntProjectHelper h = EjbJarProjectGenerator.createProject(dirF, name,
                j2eeLevel, serverInstanceID, librariesDefinition, serverLibraryName);
        
        handle.progress(2);
        FileObject dir = FileUtil.toFileObject(dirF);
        
        Project earProject = (Project) wiz.getProperty(ProjectServerWizardPanel.EAR_APPLICATION);
        EjbJarProject createdEjbJarProject = (EjbJarProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdEjbJarProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addEjbJarModule(createdEjbJarProject.getAPIEjbJar());
            }
        }
        
        // remember last used server
    	UserProjectSettings.getDefault().setLastUsedServer(serverInstanceID);
        SharableLibrariesUtils.setLastProjectSharable(librariesDefinition != null);
        
        // downgrade the Java platform or src level to 1.4        
        String platformName = (String)wiz.getProperty(ProjectServerWizardPanel.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(ProjectServerWizardPanel.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            EjbJarProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
        
        handle.progress(NbBundle.getMessage(NewEjbJarProjectWizardIterator.class, "LBL_NewEjbJarProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        
        resultSet.add(dir);
        
        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        Utils.setSteps(panels, STEPS);
    }
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(ProjectLocationWizardPanel.PROJECT_DIR,null);
        this.wiz.putProperty(ProjectLocationWizardPanel.NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle").getString("LBL_WizardStepsCount"), //NOI18N
                (new Integer(index + 1)).toString(), (new Integer(panels.length)).toString());
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
