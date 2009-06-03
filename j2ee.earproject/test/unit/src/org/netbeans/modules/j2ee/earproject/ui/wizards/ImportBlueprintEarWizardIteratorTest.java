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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Profile;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectTest;
import org.netbeans.modules.j2ee.earproject.ModuleType;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 * @author Martin Krauskopf
 */
public class ImportBlueprintEarWizardIteratorTest extends NbTestCase {
    
    private static final String CUSTOM_CONTEXT_ROOT = "/my-context-root";
    
    private String name;
    private Profile j2eeProfile;
    private String warName;
    private String jarName;
    private String carName;
    private String mainClass;
    private String platformName;
    private String sourceLevel;
    
    private String serverInstanceID;
    private File prjDirF;
    
    public ImportBlueprintEarWizardIteratorTest(String testName) {
        super(testName);
        setDefaultValues();
    }
    
    private void setDefaultValues() {
        name = "Test EnterpriseApplication";
        j2eeProfile = Profile.JAVA_EE_5;
        warName = "testEA-war";
        jarName = "testEA-ejb";
        carName = "testEA-app-client";
        mainClass = "testEA.app.client.Main";
        platformName = null;
        sourceLevel = "1.5";
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        setDefaultValues();
        clearWorkDir();
        TestUtil.initLookup(this);
        serverInstanceID = TestUtil.registerSunAppServer(
                this, new Object[] { new SilentDialogDisplayer(), new SimplePlatformProvider() });
        assertTrue("wrong dialog displayer", DialogDisplayer.getDefault() instanceof SilentDialogDisplayer);
        // default project dir
        prjDirF = new File(getWorkDir(), "testEA");
    }
    
    public void testTestableInstantiateBasics() throws Exception {
        j2eeProfile = Profile.JAVA_EE_5;
        generateJ2EEApplication(false);
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeProfile, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null, null, null);
        
        FileObject fo = FileUtil.toFileObject(importedDir);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(fo);
        EditableProperties props = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("j2ee.platform was set to 1.5", J2eeModule.JAVA_EE_5, props.getProperty("j2ee.platform")); // #76874
    }
    
    public void testTestableInstantiateWitoutDD() throws Exception {
        j2eeProfile = Profile.J2EE_14;
        FileObject prjDirFO = generateJ2EEApplication(true);
        
        // and Enterprise Application's deployment descriptor
        prjDirFO.getFileObject("src/conf/application.xml").delete();
        
        Map<FileObject, ModuleType> userModules = new HashMap<FileObject, ModuleType>();
        userModules.put(prjDirFO.getFileObject(warName), ModuleType.WEB);
        userModules.put(prjDirFO.getFileObject(jarName), ModuleType.EJB);
        userModules.put(prjDirFO.getFileObject(carName), ModuleType.CLIENT);
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeProfile, importedDir, prjDirF, serverInstanceID, name, userModules, null, null, null);
        
        FileObject importedDirFO = FileUtil.toFileObject(importedDir);
        FileObject ddFO = prjDirFO.getFileObject("src/conf/application.xml");
        assertNotNull("deployment descriptor was created", ddFO);
        EarProjectTest.validate(ddFO);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(importedDirFO);
        EarProjectTest.openProject(project);
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        assertSame("three modules", 3, app.getModule().length);
    }
    
    public void testTestableInstantiateWithWebAndEJBAndAC() throws Exception {
        j2eeProfile = Profile.J2EE_14;
        FileObject prjDirFO = generateJ2EEApplication(true);
        
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeProfile, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null, null, null);
        
        assertNotNull("have a backup copy of application.xml", prjDirFO.getFileObject("src/conf/original_application.xml"));
        assertNotNull("have a backup copy of manifest", prjDirFO.getFileObject("src/conf/original_MANIFEST.MF"));
        FileObject importedDirFO = FileUtil.toFileObject(importedDir);
        EarProject project = (EarProject) ProjectManager.getDefault().findProject(importedDirFO);
        EarProjectTest.openProject(project);
        
        FileObject ddFO = project.getAppModule().getDeploymentDescriptor();
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        EarProjectTest.validate(ddFO);
        assertSame("three modules", 3, app.getModule().length);
        NewEarProjectWizardIteratorTest.doTestThatEJBWasAddedToWebAndAC( // #66546 and #74123
                importedDirFO.getFileObject("testEA-war"),
                importedDirFO.getFileObject("testEA-app-client"));
    }
    
    // temporarily(?) turned off
    public void off_testWebContextRootIsSet() throws Exception {
        this.j2eeProfile = Profile.J2EE_14;
        generateJ2EEApplicationWithWeb();
        
        File importedDir = new File(getWorkDir(), "testEA-imported");
        ImportBlueprintEarWizardIterator.testableInstantiate(platformName, sourceLevel,
                j2eeProfile, importedDir, prjDirF, serverInstanceID, name,
                Collections.<FileObject, ModuleType>emptyMap(), null, null, null);
        
        String importedContextRoot = null;
        FileObject ddFO = FileUtil.toFileObject(prjDirF).getFileObject("src/conf/application.xml");
        assertNotNull(ddFO);
        EarProjectTest.validate(ddFO);
        Application app = DDProvider.getDefault().getDDRoot(ddFO);
        assertNotNull(app);
        for (Module module : app.getModule()) {
            Web web = module.getWeb();
            if (web != null) {
                importedContextRoot = web.getContextRoot();
                break;
            }
        }
        
        assertNotNull("context-root set", importedContextRoot);
        assertEquals("context-root successfully imported", CUSTOM_CONTEXT_ROOT, importedContextRoot);
    }
    
    private FileObject generateJ2EEApplication() throws Exception {
        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(
                prjDirF, name, j2eeProfile, serverInstanceID,
                warName, jarName, carName, mainClass, platformName, sourceLevel);
        
        // Workaround. Set the context root which should be set automatically.
        // Do not know how to do it. Probably by getting somehow "Sun J2EE DD GUI"
        // loader into the game.
        FileObject ddFO = FileUtil.toFileObject(prjDirF).getFileObject("src/conf/application.xml");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        EarProject earProject = project.getLookup().lookup(EarProject.class);
        Application app = earProject.getAppModule().getApplication();
        for (Module module : app.getModule()) {
            Web web = module.getWeb();
            if (web != null) {
                web.setContextRoot("/my-context-root");
                if (EarProjectUtil.isDDWritable(earProject)) {
                    app.write(ddFO);
                }
                break;
            }
        }
        
        // clean-up NB specific metadata
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        prjDirFO.getFileObject("nbproject").delete();
        if (warName != null) {
            prjDirFO.getFileObject("testEA-war/nbproject").delete();
        }
        if (jarName != null) {
            prjDirFO.getFileObject("testEA-ejb/nbproject").delete();
        }
        if (carName != null) {
            prjDirFO.getFileObject("testEA-app-client/nbproject").delete();
        }
        return prjDirFO;
    }
    
    private FileObject generateJ2EEApplication(boolean withSubModules) throws Exception {
        if (!withSubModules) {
            this.warName = null;
            this.jarName = null;
            this.carName = null;
            this.mainClass = null;
        }
        return generateJ2EEApplication();
    }
    
    private FileObject generateJ2EEApplicationWithWeb() throws Exception {
        this.jarName = null;
        this.carName = null;
        this.mainClass = null;
        return generateJ2EEApplication();
    }
    
    // This could be probably removed as soon as #66988 is fixed since the
    // dialog will not be displayed any more.
    private static final class SilentDialogDisplayer extends DialogDisplayer {
        
        public Object notify(NotifyDescriptor descriptor) {
            return null;
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            return null;
        }
        
    }
    
    private static class SimplePlatformProvider implements JavaPlatformProvider {
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public JavaPlatform[] getInstalledPlatforms() {
            return new JavaPlatform[] {
                getDefaultPlatform()
            };
        }
        
        public JavaPlatform getDefaultPlatform() {
            return new TestDefaultPlatform();
        }
        
    }
    
    private static class TestDefaultPlatform extends JavaPlatform {
        
        public FileObject findTool(String toolName) {
            return null;
        }
        
        public String getDisplayName() {
            return "Default Platform";
        }
        
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }
        
        @SuppressWarnings("unchecked")
        public Collection getInstallFolders() {
            return null;
        }
        
        public ClassPath getStandardLibraries() {
            return null;
        }
        
        public String getVendor() {
            return null;
        }
        
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("1.5"));
        }
        
        public ClassPath getSourceFolders() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public List getJavadocFolders() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public Map getProperties() {
            return Collections.singletonMap("platform.ant.name","default_platform");
        }
        
    }
    
}
