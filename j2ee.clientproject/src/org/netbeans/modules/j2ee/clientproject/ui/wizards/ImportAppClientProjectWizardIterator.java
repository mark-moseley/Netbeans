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

package org.netbeans.modules.j2ee.clientproject.ui.wizards;

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
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.ui.FoldersListSettings;
import org.netbeans.modules.j2ee.common.project.ui.PanelSharability;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Application Client project for an existing one.
 * @author Pavel Buzek
 */
public class ImportAppClientProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
//    private boolean imp = true;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[]{
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_IW_ImportTitle"), //NOI18N
        NbBundle.getMessage(NewAppClientProjectWizardIterator.class, "PanelShareabilityVisual.label"),
        NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LAB_ConfigureSourceRoots") //NOI18N
    };
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportLocation(),
            new PanelSharability(WizardProperties.PROJECT_DIR, WizardProperties.SERVER_INSTANCE_ID, true),
            new PanelSourceFolders.Panel()
        };
    }

    public Set<FileObject> instantiate() throws IOException/*, IllegalStateException*/ {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty(WizardProperties.NAME);
//        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        File[] sourceFolders = (File[]) wiz.getProperty(WizardProperties.JAVA_ROOT);
        File[] testFolders = (File[]) wiz.getProperty(WizardProperties.TEST_ROOT);
        File configFilesFolder = (File) wiz.getProperty(WizardProperties.CONFIG_FILES_FOLDER);
        File libName = (File) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        String librariesDefinition =
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(PanelSharability.WIZARD_SHARED_LIBRARIES));
        String serverLibraryName = (String) wiz.getProperty(PanelSharability.WIZARD_SERVER_LIBRARY);
        
        AntProjectHelper h = AppClientProjectGenerator.importProject(dirF, name,
                sourceFolders, testFolders, configFilesFolder, libName, j2eeLevel,
                serverInstanceID, librariesDefinition, serverLibraryName);
        
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject (dirF);

        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        AppClientProject createdAppClientProject = (AppClientProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdAppClientProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addCarModule(createdAppClientProject.getAPICar());
            }
        }
        
        resultSet.add (dir);

        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
        
        // remember last used server
        FoldersListSettings.getDefault().setLastUsedServer(serverInstanceID);
        
        // downgrade the Java platform or src level to 1.4
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
                
        handle.progress(NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_NewAppClientProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        
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
        this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz.putProperty (WizardProperties.SOURCE_ROOT, null);
        this.wiz.putProperty(WizardProperties.JAVA_ROOT, null);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, null);
        this.wiz.putProperty(WizardProperties.CONFIG_FILES_FOLDER, null);
        this.wiz.putProperty(WizardProperties.LIB_FOLDER, null);
        this.wiz.putProperty(WizardProperties.SERVER_INSTANCE_ID, null);
        this.wiz.putProperty(WizardProperties.J2EE_LEVEL, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(ImportAppClientProjectWizardIterator.class, "LBL_WizardStepsCount"),
                index + 1, panels.length);
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

