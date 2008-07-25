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

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Buzek
 */
public class PersistenceClientIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;

    static final String[] UTIL_CLASS_NAMES = {"JsfCrudELResolver", "JsfUtil", "PagingInfo"};
    static final String UTIL_FOLDER_NAME = "util"; //NOI18N
    
    public Set instantiate(TemplateWizard wizard) throws IOException
    {
        final List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final String jsfFolder = (String) wizard.getProperty(WizardProperties.JSF_FOLDER);
        final Project project = Templates.getProject(wizard);
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final FileObject jpaControllerPackageFileObject = (FileObject)wizard.getProperty(WizardProperties.JPA_CLASSES_PACKAGE_FILE_OBJECT);
        final String jpaControllerPkg = (String) wizard.getProperty(WizardProperties.JPA_CLASSES_PACKAGE);
        final String controllerPkg = (String) wizard.getProperty(WizardProperties.JSF_CLASSES_PACKAGE);
        Boolean ajaxifyBoolean = (Boolean) wizard.getProperty(WizardProperties.AJAXIFY_JSF_CRUD);
        final boolean ajaxify = ajaxifyBoolean == null ? false : ajaxifyBoolean.booleanValue();
        
        PersistenceUnit persistenceUnit = 
                (PersistenceUnit) wizard.getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.PERSISTENCE_UNIT);

        if (persistenceUnit != null){
            try {
                ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
            }
            catch (InvalidPersistenceXmlException e) {
                throw new IOException(e.toString());
            }
        }
        
        final JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
        
        final String title = NbBundle.getMessage(PersistenceClientIterator.class, "TITLE_Progress_Jsf_Pages"); //NOI18N
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle = 
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        final ProgressPanel progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(handle);
        
        final Runnable r = new Runnable() {

            public void run() {
                try {
                    handle.start();
                    int jpaProgressStepCount = JpaControllerIterator.getProgressStepCount(entities.size());
                    int progressStepCount = jpaProgressStepCount + getProgressStepCount(ajaxify);
                    progressStepCount += (JSFClientGenerator.PROGRESS_STEP_COUNT * entities.size());
                    progressContributor.start(progressStepCount);
                    JpaControllerIterator.generateJpaControllers(progressContributor, progressPanel, entities, project, jpaControllerPkg, jpaControllerPackageFileObject, embeddedPkSupport, false);
                    generateJsfControllers(progressContributor, progressPanel, targetFolder, controllerPkg, jpaControllerPkg, entities, ajaxify, project, jsfFolder, jpaControllerPackageFileObject, embeddedPkSupport, jpaProgressStepCount);
                    progressContributor.progress(progressStepCount);
                } catch (IOException ioe) {
                    Logger.getLogger(PersistenceClientIterator.class.getName()).log(Level.INFO, null, ioe);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } finally {
                    progressContributor.finish();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressPanel.close();
                        }
                    });
                    handle.finish();
                }
            }
        };
        
        // Ugly hack ensuring the progress dialog opens after the wizard closes. Needed because:
        // 1) the wizard is not closed in the AWT event in which instantiate() is called.
        //    Instead it is closed in an event scheduled by SwingUtilities.invokeLater().
        // 2) when a modal dialog is created its owner is set to the foremost modal
        //    dialog already displayed (if any). Because of #1 the wizard will be
        //    closed when the progress dialog is already open, and since the wizard
        //    is the owner of the progress dialog, the progress dialog is closed too.
        // The order of the events in the event queue:
        // -  this event
        // -  the first invocation event of our runnable
        // -  the invocation event which closes the wizard
        // -  the second invocation event of our runnable
        
        SwingUtilities.invokeLater(new Runnable() {
            private boolean first = true;
            public void run() {
                if (!first) {
                    RequestProcessor.getDefault().post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });
        
        return Collections.singleton(DataFolder.findFolder(targetFolder));
    }
    
    private static int getProgressStepCount(boolean ajaxify) {
        int count = UTIL_CLASS_NAMES.length + 2;
        if (ajaxify) {
            count++;
        }
        return count;
    }
    
    private static void generateJsfControllers(ProgressContributor progressContributor, final ProgressPanel progressPanel, FileObject targetFolder, String controllerPkg, String jpaControllerPkg, List<String> entities, boolean ajaxify, Project project, String jsfFolder, FileObject jpaControllerPackageFileObject, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, int progressIndex) throws IOException {
        String progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Util_Pre"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);     
        
        //copy util classes
        FileObject utilFolder = targetFolder.getFileObject(UTIL_FOLDER_NAME);
        if (utilFolder == null) {
            utilFolder = FileUtil.createFolder(targetFolder, UTIL_FOLDER_NAME);
        }
        String utilPackage = controllerPkg == null || controllerPkg.length() == 0 ? UTIL_FOLDER_NAME : controllerPkg + "." + UTIL_FOLDER_NAME;
        for (int i = 0; i < UTIL_CLASS_NAMES.length; i++){
            if (utilFolder.getFileObject(UTIL_CLASS_NAMES[i], "java") == null) {
                progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", UTIL_CLASS_NAMES[i] + ".java"); //NOI18N
                progressContributor.progress(progressMsg, progressIndex++);
                progressPanel.setText(progressMsg);
                String content = JpaControllerUtil.readResource(PersistenceClientIterator.class.getClassLoader().getResourceAsStream(JSFClientGenerator.RESOURCE_FOLDER + UTIL_CLASS_NAMES[i] + ".java.txt"), "UTF-8"); //NOI18N
                content = content.replaceAll("__PACKAGE__", utilPackage);
                FileObject target = FileUtil.createData(utilFolder, UTIL_CLASS_NAMES[i] + ".java");//NOI18N
                String projectEncoding = JpaControllerUtil.getProjectEncodingAsString(project, target);
                JpaControllerUtil.createFile(target, content, projectEncoding);  //NOI18N
            }
            else {
                progressContributor.progress(progressIndex++);
            }
        }
        
        progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Controller_Converter_Pre"); //NOI18N"Preparing to generate JSF controllers and converters";
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);  
        
        int[] nameAttemptIndices = new int[entities.size()];
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        FileObject[] converterFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleControllerNameBase = simpleClassName + "Controller"; //NOI18N
            String simpleControllerName = simpleControllerNameBase;
            while (targetFolder.getFileObject(simpleControllerName, "java") != null && nameAttemptIndices[i] < 1000) {
                simpleControllerName = simpleControllerNameBase + ++nameAttemptIndices[i];
            }
            String simpleConverterName = simpleClassName + "Converter" + (nameAttemptIndices[i] == 0 ? "" : nameAttemptIndices[i]);
            int converterNameAttemptIndex = 1;
            while (targetFolder.getFileObject(simpleConverterName, "java") != null && converterNameAttemptIndex < 1000) {
                simpleConverterName += "_" + converterNameAttemptIndex++;
            }
            controllerFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleControllerName, null);
            converterFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleConverterName, null);
        }
        
        if (ajaxify) {
            progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Add_Ajax_Lib"); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg); 
            Library[] libraries = { LibraryManager.getDefault().getLibrary("jsf-extensions") };
            ProjectClassPathModifier.addLibraries(libraries, getSourceRoot(project), ClassPath.COMPILE);
        }
        
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            if (nameAttemptIndices[i] > 0) {
                firstLower += nameAttemptIndices[i];
            }
            if (jsfFolder.endsWith("/")) {
                jsfFolder = jsfFolder.substring(0, jsfFolder.length() - 1);
            }
            if (jsfFolder.startsWith("/")) {
                jsfFolder = jsfFolder.substring(1);
            }
            String controller = ((controllerPkg == null || controllerPkg.length() == 0) ? "" : controllerPkg + ".") + controllerFileObjects[i].getName();
            String simpleJpaControllerName = simpleClassName + "JpaController"; //NOI18N
            FileObject jpaControllerFileObject = jpaControllerPackageFileObject.getFileObject(simpleJpaControllerName, "java");
            JSFClientGenerator.generateJSFPages(progressContributor, progressPanel, project, entityClass, jsfFolder, firstLower, controllerPkg, controller, targetFolder, controllerFileObjects[i], embeddedPkSupport, entities, ajaxify, jpaControllerPkg, jpaControllerFileObject, converterFileObjects[i], progressIndex);
            progressIndex += JSFClientGenerator.PROGRESS_STEP_COUNT;
        }
    }

    /**
     * Convenience method to obtain the source root folder.
     * @param project the Project object
     * @return the FileObject of the source root folder
     */
    private static FileObject getSourceRoot(Project project) {
        if (project == null) {
            return null;
        }

        // Search the ${src.dir} Source Package Folder first, use the first source group if failed.
        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] grp = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grp.length; i++) {
            if ("${src.dir}".equals(grp[i].getName())) { // NOI18N
                return grp[i].getRootFolder();
            }
        }
        if (grp.length != 0) {
            return grp[0].getRootFolder();
        }

        return null;
    }

    public void initialize(TemplateWizard wizard) {
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        WizardDescriptor.Panel secondPanel = new ValidationPanel(
                new PersistenceClientEntitySelection(NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
                        new HelpCtx("framework_jsf_fromentity"), wizard)); // NOI18N
        WizardDescriptor.Panel thirdPanel = new PersistenceClientSetupPanel(project, wizard);
//        WizardDescriptor.Panel javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel);
//        panels = new WizardDescriptor.Panel[] { javaPanel };
        panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel };
        String names[] = new String[] {
            NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
            NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSFPagesAndClasses")
        };
        wizard.putProperty("NewFileWizard_Title", 
            NbBundle.getMessage(PersistenceClientIterator.class, "Templates/Persistence/JsfFromDB"));
        Wizards.mergeSteps(wizard, panels, names);
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
    
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage (PersistenceClientIterator.class, "LBL_WizardTitle_FromEntity");
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    /** 
     * A panel which checks that the target project has a valid server set
     * otherwise it delegates to the real panel.
     */
    private class ValidationPanel extends DelegatingWizardDescriptorPanel {

        private ValidationPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        public boolean isValid() {
            Project project = getProject();
            WizardDescriptor wizardDescriptor = getWizardDescriptor();
            
            // check that this project has a valid target server
            if (!org.netbeans.modules.j2ee.common.Util.isValidServerInstance(project)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PersistenceClientIterator.class, "ERR_MissingServer")); // NOI18N
                return false;
            }

            return super.isValid();
        }
    }
}
