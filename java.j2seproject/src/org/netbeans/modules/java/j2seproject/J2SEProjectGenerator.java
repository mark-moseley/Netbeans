/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a J2SEProject from scratch according to some initial configuration.
 */
public class J2SEProjectGenerator {
    
    private J2SEProjectGenerator() {}
    
    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String mainClass, String manifestFile) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        // if manifestFile is null => it's TYPE_LIB
        AntProjectHelper h = createProject(dirFO, name, "src", "test", mainClass, manifestFile, manifestFile == null); //NOI18N
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
        dirFO.createFolder("test"); // NOI18N
        if ( mainClass != null ) {
            createMainClass( mainClass, srcFolder );
        }
        return h;
    }

    public static AntProjectHelper createProject(final File dir, final String name,
                                                  final File sourceFolder, final File testFolder) throws IOException {
        assert sourceFolder != null : "Source folder must be given";   //NOI18N
        final FileObject dirFO = createProjectDir (dir);
        // this constructor creates only java application type
        final AntProjectHelper h = createProject(dirFO, name, null, null, null, null, false);
        final J2SEProject p = (J2SEProject) ProjectManager.getDefault().findProject(dirFO);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
            public Object run() throws Exception {
                String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
                EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.put("src.dir",srcReference);          //NOI18N
                String testLoc;
                if (testFolder == null) {
                    testLoc = NbBundle.getMessage (J2SEProjectGenerator.class,"TXT_DefaultTestFolderName");
                    File f = new File (dir,testLoc);    //NOI18N
                    f.mkdirs();
                }
                else {
                    if (!testFolder.exists()) {
                        testFolder.mkdirs();
                    }
                    testLoc = refHelper.createForeignFileReference(testFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
                }                
                props.put("test.src.dir",testLoc);    //NOI18N
                h.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                ProjectManager.getDefault().saveProject (p);
                return null;
            }
        });
        } catch (MutexException me ) {
            ErrorManager.getDefault().notify (me);
        }
        return h;
    }

    private static AntProjectHelper createProject(FileObject dirFO, String name,
                                                  String srcRoot, String testRoot, String mainClass, String manifestFile, boolean isLibrary) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty("javac.classpath", new String[0]); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });
        ep.setProperty("application.args", ""); // NOI18N
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (!isLibrary) {
            ep.setProperty("main.class", mainClass == null ? "" : mainClass); // NOI18N
        }
        ep.setProperty("javac.source", "${default.javac.source}"); // NOI18N
        ep.setProperty("javac.target", "${default.javac.target}"); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}", // NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            "${run.test.classpath}", // NOI18N
        });
        ep.setProperty("src.dir", srcRoot == null ? "" : srcRoot); // NOI18N
        if (testRoot != null) {
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", "default_platform"); // NOI18N

        ep.setProperty("run.jvmargs", ""); // NOI18N
        ep.setComment("run.jvmargs", new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(J2SEProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty("application.args", ""); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVAC_DEBUG, "true");  // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        return h;
    }

    private static FileObject createProjectDir (File dir) throws IOException {
        FileObject dirFO;
        if(!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem (dir);
            if (!dir.mkdirs()) {
                throw new IOException ("Can not create project folder.");   //NOI18N
            }
            refreshFileSystem (dir);
        }        
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }

    private static void createMainClass( String mainClassName, FileObject srcFolder ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        }
        else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        
        if ( mName.length() == 0 ) {
            return;
        }
        
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Classes/Main.java" ); // NOI18N

        if ( mainTemplate == null ) {
            return; // Don't know the template
        }
                
        DataObject mt = DataObject.find( mainTemplate );
        
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );        
        mt.createFromTemplate( pDf, mName );
        
    }


    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }
}


