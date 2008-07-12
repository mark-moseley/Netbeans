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
package org.netbeans.modules.hibernate.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.loaders.reveng.HibernateRevengDataObject;
import org.netbeans.modules.hibernate.reveng.model.HibernateReverseEngineering;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.netbeans.modules.hibernate.wizards.support.Table;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.xml.sax.EntityResolver;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.tool.hbm2x.HibernateMappingExporter;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.hibernate.util.XMLHelper;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.util.CustomClassLoader;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author gowri
 */
public class HibernateRevengWizard implements WizardDescriptor.InstantiatingIterator {

    private static final String PROP_HELPER = "wizard-helper"; //NOI18N

    private int index;
    private Project project;
    private WizardDescriptor wizardDescriptor;
    private HibernateRevengWizardHelper helper;
    private HibernateRevengDbTablesWizardDescriptor dbTablesDescriptor;
    private HibernateRevengCodeGenWizardDescriptor codeGenDescriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private final String DEFAULT_REVENG_FILENAME = "hibernate.reveng"; // NOI18N

    private final String ATTRIBUTE_NAME = "match-schema"; // NOI18N

    private final String MATCH_NAME = "match-name"; // NOI18N

    private final String resourceAttr = "resource"; // NOI18N
    
    private final String classAttr = "class"; // NOI18N

    private XMLHelper xmlHelper;
    private EntityResolver entityResolver;
    private Logger logger = Logger.getLogger(HibernateRevengWizard.class.getName());

    public static HibernateRevengWizard create() {
        return new HibernateRevengWizard();
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project p = Templates.getProject(wizardDescriptor);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups);

            panels = new WizardDescriptor.Panel[]{
                        targetChooser,
                        dbTablesDescriptor,
                        codeGenDescriptor
                    };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components

                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                    // Sets steps names for a panel

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step

                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N

                }
            }
        }
        return panels;
    }

    static HibernateRevengWizardHelper getHelper(WizardDescriptor wizardDescriptor) {
        return (HibernateRevengWizardHelper) wizardDescriptor.getProperty(PROP_HELPER);
    }

    public String name() {
        return NbBundle.getMessage(HibernateRevengWizard.class, "LBL_RevEngWizardTitle"); // NOI18N

    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizardDescriptor.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N

        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    private boolean foundRevengFileInProject(List<FileObject> revengFiles, String revengFileName) {
        for (FileObject fo : revengFiles) {
            if (fo.getName().equals(revengFileName)) {
                return true;
            }
        }
        return false;
    }

    public Set instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        String targetName = Templates.getTargetName(wizardDescriptor);
        FileObject templateFileObject = Templates.getTemplate(wizardDescriptor);
        DataObject templateDataObject = DataObject.find(templateFileObject);


        DataObject newOne = templateDataObject.createFromTemplate(targetDataFolder, targetName);
        try {
            HibernateRevengDataObject hro = (HibernateRevengDataObject) newOne;
            HibernateReverseEngineering hre = hro.getHibernateReverseEngineering();
            ArrayList<Table> list = (ArrayList<Table>) helper.getSelectedTables().getTables();
            for (int i = 0; i < list.size(); i++) {
                int index = hre.addTableFilter(true);
                hre.setAttributeValue(hre.TABLE_FILTER, index, ATTRIBUTE_NAME, helper.getSchemaName());
                hre.setAttributeValue(hre.TABLE_FILTER, index, MATCH_NAME, list.get(i).getName());

            }
            hro.addReveng();
            hro.save();
            if (list.size() > 0) {
                generateClasses(hro.getPrimaryFile());
                updateConfiguration();
            }
            return Collections.singleton(hro.getPrimaryFile());
        } catch (Exception e) {
            return Collections.EMPTY_SET;
        }

    }

    public final void initialize(WizardDescriptor wiz) {
        wizardDescriptor = wiz;
        project = Templates.getProject(wiz);
        helper = new HibernateRevengWizardHelper(project);

        wiz.putProperty(PROP_HELPER, helper);
        dbTablesDescriptor = new HibernateRevengDbTablesWizardDescriptor(project);
        codeGenDescriptor = new HibernateRevengCodeGenWizardDescriptor(project, wizardDescriptor);

        if (Templates.getTargetFolder(wiz) == null) {
            HibernateFileLocationProvider provider = project != null ? project.getLookup().lookup(HibernateFileLocationProvider.class) : null;
            FileObject location = provider != null ? provider.getLocation() : null;
            if (location != null) {
                Templates.setTargetFolder(wiz, location);
            }
        }

        // Set the targetName here. Default name for new files should be in the form : 'hibernate<i>.reveng.xml 
        // and not like : hibernate.reveng<i>.xml.
        if (wiz instanceof TemplateWizard) {
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
            List<FileObject> revengFiles = hibernateEnv.getAllHibernateReverseEnggFileObjects();
            String targetName = DEFAULT_REVENG_FILENAME;
            if (!revengFiles.isEmpty() && foundRevengFileInProject(revengFiles, DEFAULT_REVENG_FILENAME)) {
                int revengFilesCount = revengFiles.size();
                targetName = "hibernate" + (revengFilesCount++) + ".reveng";
                while (foundRevengFileInProject(revengFiles, targetName)) {
                    targetName = "hibernate" + (revengFilesCount++) + ".reveng";
                }
            }
            ((TemplateWizard) wiz).setTargetName(targetName);
        }

        String wizardBundleKey = "Templates/Hibernate/HibernateReveng";  // NOI18N

        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(HibernateRevengWizard.class, wizardBundleKey)); // NOI18N        

    }

    // Generates POJOs and hibernate mapping files based on a .reveng.xml file
    public void generateClasses(FileObject revengFile) throws IOException {
        JDBCMetaDataConfiguration cfg = null;
        ReverseEngineeringSettings settings = null;
        ClassLoader oldClassLoader = null;

        File confFile = FileUtil.toFile(helper.getConfigurationFile());
        File outputDir = FileUtil.toFile(helper.getLocation().getRootFolder());

        try {

            // Setup classloader.
            logger.info("Setting up classloader");
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            CustomClassLoader ccl = new CustomClassLoader(env.getProjectClassPath(revengFile).toArray(new URL[]{}),
                    getClass().getClassLoader());
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ccl);

            // Configuring the reverse engineering strategy
            try {

                cfg = new JDBCMetaDataConfiguration();
                OverrideRepository or = new OverrideRepository();
                Configuration c = cfg.configure(confFile);
                or.addFile(FileUtil.toFile(revengFile));
                DefaultReverseEngineeringStrategy strategy = new DefaultReverseEngineeringStrategy();
                settings = new ReverseEngineeringSettings(strategy);
                settings.setDefaultPackageName(helper.getPackageName());
                strategy.setSettings(settings);
                cfg.setReverseEngineeringStrategy(or.getReverseEngineeringStrategy(strategy));
                cfg.readFromJDBC();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            // Generating POJOs            
            try {
                if (helper.getDomainGen()) {
                    POJOExporter exporter = new POJOExporter(cfg, outputDir);
                    exporter.getProperties().setProperty("jdk", new Boolean(helper.getJavaSyntax()).toString());
                    exporter.getProperties().setProperty("ejb3", new Boolean(helper.getEjbAnnotation()).toString());
                    exporter.start();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            // Generating Mappings
            try {
                if (helper.getHbmGen()) {
                    HibernateMappingExporter exporter = new HibernateMappingExporter(cfg, outputDir);
                    exporter.start();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    // Update mapping entries in the selected configuration file 
    public void updateConfiguration() {
        try {
            DataObject confDataObject = DataObject.find(helper.getConfigurationFile());
            HibernateCfgDataObject hco = (HibernateCfgDataObject) confDataObject;
            SessionFactory sf = hco.getHibernateConfiguration().getSessionFactory();
            FileObject pkg = SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName(), false);
            if (pkg != null && pkg.isFolder()) {
                // bugfix: 137052
                pkg.getFileSystem().refresh(true);
                
                Enumeration<? extends FileObject> enumeration = pkg.getChildren(true);
                
                // Generate cfg.xml with annotated pojos
                if (helper.getDomainGen() && helper.getEjbAnnotation() && !helper.getHbmGen()) {
                    while (enumeration.hasMoreElements()) {
                        FileObject fo = enumeration.nextElement();
                        if (fo.getNameExt() != null && fo.getMIMEType().equals("text/x-java")) { // NOI18N
                            int mappingIndex = sf.addMapping(true);
                            String javaFileName = HibernateUtil.getRelativeSourcePath(fo, Util.getSourceRoot(project));                                                        
                            String fileName = javaFileName.replaceAll("/", ".").substring(0, javaFileName.indexOf(".java", 0)); // NOI18N
                            sf.setAttributeValue(SessionFactory.MAPPING, mappingIndex, classAttr, fileName);
                            hco.modelUpdatedFromUI();
                            hco.save();
                        }
                    }
                } else {
                
                    // Generate cfg.xml with hbm files
                    while (enumeration.hasMoreElements()) {
                        FileObject fo = enumeration.nextElement();
                        if (fo.getNameExt() != null && fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                            int mappingIndex = sf.addMapping(true);
                            sf.setAttributeValue(SessionFactory.MAPPING, mappingIndex, resourceAttr, HibernateUtil.getRelativeSourcePath(fo, Util.getSourceRoot(project)));
                            hco.modelUpdatedFromUI();
                            hco.save();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
}
