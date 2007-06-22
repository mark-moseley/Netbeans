/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.jaxb.util;

import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.xml.jaxb.cfg.schema.Bindings;
import org.netbeans.modules.xml.jaxb.cfg.schema.Catalog;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOption;
import org.netbeans.modules.xml.jaxb.cfg.schema.XjcOptions;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizBindingCfgPanel;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * @author lgao
 * @author $Author$
 */
public class ProjectHelper {

    public static final int PROJECT_TYPE_J2SE = 0;
    public static final int PROJECT_TYPE_EJB = 1;
    public static final int PROJECT_TYPE_WEB = 2;
    private static final String JAXB_LIB_NAME = "jaxb20"; // No I18N
    private static final String XML_BINDING_CONFIG_FILE_NAME = "xml_binding_cfg.xml"; // No I18N
    private static final String XML_BINDING_BUILD_FILE_NAME = "xml_binding_build.xml"; // No I18N
    private static final String FILE_OBJECT_SEPARATOR = "/" ; // NOI18N
    private static final String NBPROJECT_DIR = "nbproject"; // No I18N
    private static final String XSL_RESOURCE = "org/netbeans/modules/xml/jaxb/resources/JAXBBuild.xsl"; // No I18N
    private static final String BUILD_GEN_JAXB_DIR = "build/generated/addons/jaxb"; // No I18N
    private static final String NON_JAVA_SE_CONFIG_DIR = "conf/xml-resources/jaxb"; // No I18N
    private static final String JAVA_SE_CONFIG_DIR = "xml-resources/jaxb"; // No I18N
    private static final String PLATFORM_ACTIVE = "platform.active"; // No I18N
    private static final String DEFAULT_PLATFORM = "default_platform"; // No I18N
    private static final String RUN_JVM_ARGS_KEY = "run.jvmargs"; // No I18N
    private static final String PROP_ENDORSED = "jaxbwiz.endorsed.dirs"; // No I18N
    private static final String RUN_JVM_ARGS_VAL_PREFIX = "-Djava.endorsed.dirs=${" + PROP_ENDORSED + "}"; // No I18N
    private static final SpecificationVersion JDK_1_6 = new SpecificationVersion("1.6"); // No I18N

    // Make sure nobody instantiates this class.
    private ProjectHelper() {
    }

    private static void log(Level level, String msg, Exception ex) {
        Logger.getLogger(ProjectHelper.class.getName()).log(level, msg, ex);
    }

    public static void refreshBuildScript(Project prj) {
        try {
            Source xmlSource = new StreamSource(getXMLBindingConfigFile(prj));
            Source xslSource = new StreamSource(ProjectHelper.class.getClassLoader().getResourceAsStream(XSL_RESOURCE));
            Result result = new StreamResult(getXMLBindingBuildFile(prj));
            TransformerFactory fact = TransformerFactory.newInstance();
            fact.setAttribute("indent-number", 4); //NOI18N
            Transformer xformer = fact.newTransformer(xslSource);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            xformer.transform(xmlSource, result);
        } catch (Exception ex) {
            log(Level.INFO, "refreshBuildScript()", ex); //NO18N
        }
    }

    private static String getProperty(Project prj, String filePath, String name) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        String str = null;
        String value = ep.getProperty(name);
        if (value != null) {
            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
            str = pe.evaluate(value);
        }
        return str;
    }

    private static void saveProperty(Project prj, String filePath, String name, String value) {
        AntProjectHelper aph = getAntProjectHelper(prj);
        EditableProperties ep = aph.getProperties(filePath);
        if (value != null) {
            ep.put(name, value);
            aph.putProperties(filePath, ep);
        }
    }

    private static String getProjectProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    private static String getPrivateProperty(Project prj, String prop) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop);
    }

    private static void savePrivateProperty(Project prj, String prop, String value) {
        saveProperty(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH, prop, value);
    }

    private static void saveProjectProperty(Project prj, String prop, String value) {
        saveProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, prop, value);
    }

    public static String getProjectSourceDir(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, "src.dir"); // No I18N
    }

    public static File getSourceDirectoryFile(Project prj) {
        return new File(getProjectSourceDir(prj));
    }

    public static String getProjectBuildDir(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, "build.dir"); // No I18N
    }

    public static String getProjectSourceRoot(Project prj) {
        return getProperty(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH, "source.root"); // No I18N
    }

    public static File getProjectDirectory(Project prj) {
        return FileUtil.toFile(prj.getProjectDirectory());
    }

    public static String getProjectRelativePath(Project prj, String absPath) {
        String relPath = null;
        if (absPath != null) {
            String projectDirectory = getProjectDirectory(prj).getAbsolutePath();

            if (absPath.toLowerCase().indexOf(projectDirectory.toLowerCase()) != -1) {
                relPath = absPath.substring(projectDirectory.length() + 1);
            } else {
                relPath = absPath;
            }
        }

        return relPath;
    }

    private static void addJAXB20Library(Project prj) {
        SourceGroup[] sgs = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath compileClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
        ClassPath bootClassPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.BOOT);
        ClassPath classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});
        FileObject jaxbClass = classPath.findResource("javax/xml/bind/JAXBContent.class"); // NOI18N
        if (jaxbClass == null) {
            // Add JAXB jars if not in the classpath
            Library jaxbLib = LibraryManager.getDefault().getLibrary(JAXB_LIB_NAME); //NOI18N
            Sources srcs = ProjectUtils.getSources(prj);
            if (srcs != null) {
                SourceGroup[] srg = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if ((srg != null) && (srg.length > 0)) {
                    try {
                        ProjectClassPathModifier.addLibraries(new Library[]{jaxbLib}, srg[0].getRootFolder(), ClassPath.COMPILE);
                    } catch (IOException ex) {
                        log(Level.WARNING, "addJAXB20Library()", ex);
                        ErrorManager.getDefault().log(ex.getMessage());
                    }
                }
            }
        }
    }

    //    private static String getBuildClassDir(Project prj){
    //        String ret = "build/classes" ; // No I18N
    //        int pType = getProjectType(prj);
    //        if (pType == PROJECT_TYPE_EJB){
    //            ret = "build/jar"; // No I18N
    //        }
    //
    //        if (pType == PROJECT_TYPE_WEB){
    //            ret = "build/web/WEB-INF/classes"; // No I18N
    //        }
    //        return ret;
    //    }
    
    private static void addLibraries(Project prj) {
        addJAXB20Library(prj);
        //setClasspath(prj);
    }

    public static int getProjectType(Project prj) {
        String prjClzName = prj.getClass().getName();
        int prjType = PROJECT_TYPE_J2SE;
        if (prjClzName.indexOf("EjbJarProject") != -1) {
            // No I18N
prjType = PROJECT_TYPE_EJB;
        } else if (prjClzName.indexOf("WebProject") != -1) {
            // No I18N
prjType = PROJECT_TYPE_WEB;
        }

        return prjType;
    }

    public static Schemas getXMLBindingSchemas(Project prj) {
        Schemas scs = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            String projName = fo.getName();
            File projDir = FileUtil.toFile(fo);
            File configFile = null;
            try {
                configFile = new File(projDir, NBPROJECT_DIR + File.separator + XML_BINDING_CONFIG_FILE_NAME);
                if (configFile.exists()) {
                    scs = Schemas.read(configFile);
                } else {
                    scs = new Schemas();
                    Lookup lookup = prj.getLookup();
                    if (lookup != null) {
                        ProjectInformation pi = lookup.lookup(ProjectInformation.class);
                        if (pi != null) {
                            projName = pi.getName();
                        }
                    }
                    scs.setProjectName(projName);
                    scs.setDestdir(BUILD_GEN_JAXB_DIR);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return scs;
    }

    private static File getXMLBindingBuildFile(Project prj) {
        File buildFile = null;

        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);

            try {
                buildFile = new File(projDir, NBPROJECT_DIR + File.separator + XML_BINDING_BUILD_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFile;
    }

    private static File getXMLBindingConfigFile(Project prj) {
        File configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);

            try {
                configFile = new File(projDir, NBPROJECT_DIR + File.separator + XML_BINDING_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    private static void saveXMLBindingSchemas(Project prj, Schemas scs) {
        try {
            File configFile = getXMLBindingConfigFile(prj);
            if (configFile != null) {
                scs.write(configFile);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static XjcOptions populateXjcOptions(WizardDescriptor wiz) {
        XjcOptions xjcOpts = new XjcOptions();
        Map<String, Boolean> options = (Map<String, Boolean>) wiz.getProperty(
                JAXBWizBindingCfgPanel.XJC_OPTIONS);
        if (options != null) {
            Set<String> keys = options.keySet();
            Iterator<String> itr = keys.iterator();
            String key = null;
            Boolean value;
            XjcOption xjcOption = null;

            while (itr.hasNext()) {
                key = itr.next();
                value = options.get(key);
                xjcOption = new XjcOption();
                xjcOption.setName(key);
                xjcOption.setValue(value.toString());
                xjcOpts.addXjcOption(xjcOption);
            }
        }
        return xjcOpts;
    }

    public static FileObject retrieveResource(FileObject targetFolder, URI source) {
        Retriever retriever = Retriever.getDefault();
        FileObject result = null;
        try {
            result = retriever.retrieveResource(targetFolder, source);
        } catch (UnknownHostException ex) {
            // XXX TODO Handle exception.
        } catch (URISyntaxException ex) {
            // XXX TODO Handle exception.
        } catch (IOException ex) {
            // XXX TODO Handle exception.
        }

        if (result == null) {
            // XXX TODO Handle or log exception.
            // Map map = retriever.getRetrievedResourceExceptionMap();
        }
        return result;
    }

    private static Schema populateSchema(WizardDescriptor wiz, FileObject projFO, File projSchemasDir) throws IOException {
        Schema schema = new Schema();
        Catalog catalog = new Catalog();
        Bindings bindings = new Bindings();
        SchemaSources sss = new SchemaSources();
        XjcOptions xo = new XjcOptions();

        schema.setName((String) wiz.getProperty(JAXBWizBindingCfgPanel.SCHEMA_NAME));
        schema.setPackage((String) wiz.getProperty(JAXBWizBindingCfgPanel.PACKAGE_NAME));
        schema.setCatalog(catalog);
        schema.setBindings(bindings);
        schema.setSchemaSources(sss);
        schema.setType((String) wiz.getProperty(JAXBWizBindingCfgPanel.SCHEMA_TYPE));
        schema.setXjcOptions(populateXjcOptions(wiz));

        List<String> xsdFileList = (List<String>) wiz.getProperty(JAXBWizBindingCfgPanel.XSD_FILE_LIST );
        SchemaSource ss = null;

        File schemaDir = new File(projSchemasDir, schema.getName());
        if (!schemaDir.exists()) {
            schemaDir.mkdirs();
        }

        FileObject schemaDirFO = FileUtil.toFileObject(schemaDir);
        File srcFile = null;
        File targetFile = null;
        FileObject newFileFO = null;
        String url = null;
        URL remoteSchema = null;
        boolean srcLocTypeUrl = JAXBWizBindingCfgPanel.SRC_LOC_TYPE_URL.equals((String) wiz.getProperty(JAXBWizBindingCfgPanel.SOURCE_LOCATION_TYPE));

        for (int i = 0; i < xsdFileList.size(); i++) {
            if (srcLocTypeUrl) {
                // URL
                url = (String) xsdFileList.get(i);
                remoteSchema = new URL(url);
                try {
                    newFileFO = retrieveResource(schemaDirFO, remoteSchema.toURI());
                } catch (URISyntaxException ex) {
                    throw new IOException(ex.getMessage());
                }
                ss = new SchemaSource();
                ss.setOrigLocation(url);
                ss.setLocation(FileUtil.getRelativePath(projFO, newFileFO));
                ss.setOrigLocationType(JAXBWizBindingCfgPanel.SRC_LOC_TYPE_URL);
                sss.addSchemaSource(ss);
            } else {
                // Local file
                srcFile = new File((String) xsdFileList.get(i));
                targetFile = new File(schemaDir, srcFile.getName());
                if (targetFile.exists()) {
                    targetFile.delete();
                }

                newFileFO = retrieveResource(schemaDirFO, srcFile.toURI());

                ss = new SchemaSource();
                ss.setOrigLocation(xsdFileList.get(i));
                ss.setLocation(FileUtil.getRelativePath(projFO, newFileFO));
                sss.addSchemaSource(ss);
            }
        }

        return schema;
    }

    public static AntProjectHelper getAntProjectHelper(Project project) {
        try {
            Method getAntProjectHelperMethod = project.getClass().getMethod("getAntProjectHelper");
            if (getAntProjectHelperMethod != null) {
                AntProjectHelper helper = (AntProjectHelper) getAntProjectHelperMethod.invoke(
                        project );

                return helper;
            }
        } catch (NoSuchMethodException nme) {
            Exceptions.printStackTrace(nme);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public static void addModelListner(Project prj, FileChangeAdapter listner) {
        FileObject fo = getFOForBindingConfigFile(prj);
        if (fo != null) {
            fo.addFileChangeListener(listner);
        } else {
            fo = getFOForNBProjectDir(prj);
            if (fo != null) {
                fo.addFileChangeListener(listner);
            }
        }
    }

    public static void removeModelListner(Project prj, FileChangeAdapter listner) {
        FileObject fo = getFOForBindingConfigFile(prj);
        if (fo != null) {
            fo.removeFileChangeListener(listner);
        }

        fo = getFOForNBProjectDir(prj);
        if (fo != null) {
            fo.removeFileChangeListener(listner);
        }
    }

    public static FileObject getFOForProjectBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            buildFileFo = fo.getFileObject("build.xml"); // No I18N
        }
        return buildFileFo;
    }

    public static FileObject getFOForNBProjectDir(Project prj) {
        return prj.getProjectDirectory().getFileObject(NBPROJECT_DIR);
    }

    public static FileObject getFOForBindingConfigFile(Project prj) {
        FileObject configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();

            try {
                configFile = fo.getFileObject(NBPROJECT_DIR 
                        + FILE_OBJECT_SEPARATOR + XML_BINDING_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    public static FileObject getFOForBindingBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            try {
                buildFileFo = fo.getFileObject(NBPROJECT_DIR + 
                        FILE_OBJECT_SEPARATOR + XML_BINDING_BUILD_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return buildFileFo;
    }

    private static void createDirs(FileObject rootDir, String relDir) {
        File fileRootDir = FileUtil.toFile(rootDir);
        File fileRelDirs = new File(fileRootDir, relDir);
        if (!fileRelDirs.exists()) {
            fileRelDirs.mkdirs();
        }
    }

    public static FileObject getFOProjectSchemaDir(Project project) {
        String srcDirStr = null;
        FileObject foSchemaDir = null;
        FileObject foProjDir = project.getProjectDirectory();
        if ((getProjectType(project) == PROJECT_TYPE_EJB) || (getProjectType(project) == PROJECT_TYPE_WEB)) {
            srcDirStr = getProjectSourceRoot(project);
            FileObject srcDir = foProjDir.getFileObject(srcDirStr);
            createDirs(srcDir, NON_JAVA_SE_CONFIG_DIR);
            foSchemaDir = srcDir.getFileObject(NON_JAVA_SE_CONFIG_DIR); //NOI18N
        } else {
            FileObject srcDir = foProjDir;
            createDirs(srcDir, JAVA_SE_CONFIG_DIR);
            foSchemaDir = srcDir.getFileObject(JAVA_SE_CONFIG_DIR); //NOI18N
        }
        return foSchemaDir;
    }

    private static String getEndorsedDirs(Project prj) {
        // XXX TODO:Find a better portable way to do this.
        String ret = "\"${netbeans.home}/../java1/modules/ext/jaxws21/api" + File.pathSeparator + "${netbeans.home}/../java1/modules/ext/jaxws21\""; //NOI18N
//        Library jaxbLib = LibraryManager.getDefault().getLibrary(JAXB_LIB_NAME);
//        List<URL> classPaths = jaxbLib.getContent("classpath"); // No I18N
//        Iterator<URL> itr = classPaths.iterator();
//        while (itr.hasNext()){
//            URL url = itr.next();
//            System.out.println("URL:" + url); // URL protocol is jar:nbinst:
//        }
        return ret;
    }

    private static void addEndorsedDir(Project prj) {
        if (isJDK6(prj)) {
            String endorsedDirs = getProjectProperty(prj, PROP_ENDORSED);

            if ((endorsedDirs == null) || ("".equals(endorsedDirs.trim()))) {
                endorsedDirs = getEndorsedDirs(prj);

                saveProjectProperty(prj, PROP_ENDORSED, endorsedDirs);
                saveProjectProperty(prj, RUN_JVM_ARGS_KEY, RUN_JVM_ARGS_VAL_PREFIX);
                try {
                    ProjectManager.getDefault().saveProject(prj);
                } catch (IOException ex) {
                    log(Level.SEVERE, "While saving project properties.", ex);
                }
            }
        }
    }

    public static void addSchema(Project project, WizardDescriptor wiz) {
        FileObject projectSchemaDir = getFOProjectSchemaDir(project);

        try {
            Schema schema = populateSchema(wiz, project.getProjectDirectory(), FileUtil.toFile(projectSchemaDir));
            Schemas scs = getXMLBindingSchemas(project);
            scs.addSchema(schema);
            saveXMLBindingSchemas(project, scs);
            refreshBuildScript(project);
            addEndorsedDir(project);
            // Register our build XML file, if not already.
            // http://wiki.netbeans.org/wiki/view/BuildScriptExtensibility
            // http://www.netbeans.org/issues/show_bug.cgi?id=93509
            AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
            if (ext != null && ext.getExtension("jaxb") == null) {
                //No I18N
                FileObject jaxbBuildXml = getFOForBindingBuildFile(project);
                AntBuildExtender.Extension jaxbBuild = ext.addExtension("jaxb", jaxbBuildXml); // No I18N
                // XXX TODO Uncomment once all the supported project
                // allow dependency on "-pre-compile.
                jaxbBuild.addDependency("-pre-pre-compile", "jaxb-code-generation"); //No I18N
                //jaxbBuild.addDependency("jar", "jaxb-code-generation");//No I18N
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    public static void removeSchema(Project project, Schema schema) {
        try {
            Schemas scs = getXMLBindingSchemas(project);
            scs.removeSchema(schema);
            saveXMLBindingSchemas(project, scs);
            refreshBuildScript(project);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    public static void compileXSDs(final Project project) {
        compileXSDs(project, null, false);
    }

    private static void compileXSDs(final Project prj, final String pkgName) {
        compileXSDs(prj, pkgName, true);
    }

    public static void compileXSDs(final Project project, final String pkgName, final boolean addLibs) {
        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle("JAXB Wizard Progress");
        progressHandle.start();

        Runnable run = new Runnable() {

            public void run() {
                try {
                    FileObject buildXml = getFOForProjectBuildFile(project);
                    //FileObject buildXml = getFOForBindingBuildFile(project);
                    String[] args = new String[]{"jaxb-code-generation"}; //No I18N
                    if (buildXml != null) {
                        ExecutorTask task = ActionUtils.runTarget(buildXml, args, null);
                        task.waitFinished();
                        if (task.result() != 0) {
                            String mes = "Error while compiling Schemas";
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }

                    if (addLibs) {
                        addLibraries(project);
                    }
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                    progressHandle.finish();
                }
            }
        };

        RequestProcessor.getDefault().post(run);
    }

    public static boolean isJDK6(final Project prj) {
        boolean ret = false;
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        if (jpm != null) {
            String platForm = getProjectProperty(prj, PLATFORM_ACTIVE);
            if (DEFAULT_PLATFORM.equals(platForm)) {

                JavaPlatform dflt = jpm.getDefaultPlatform();
                if (dflt != null) {
                    if (JDK_1_6.compareTo(dflt.getSpecification().getVersion()) <= 0) {
                        ret = true;
                    }
                }
            } else {
                JavaPlatform[] jp = jpm.getInstalledPlatforms();
                if (jp != null) {
                    for (JavaPlatform jpi : jp) {
                        if (jpi.getProperties().get("platform.ant.name").equals(platForm)) {
                            //
                            SpecificationVersion sv = jpi.getSpecification().getVersion();
                            if (JDK_1_6.compareTo(sv) <= 0) {
                                ret = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }
}