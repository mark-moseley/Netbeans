/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.project.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.common.CommonUiSupport;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectModel;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectSupport;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.util.StringTokenizer2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * @author Mike Frisino
 */
public class UMLProjectProperties 
{
    // Special properties of the project, stored in project.xml by project.save
    
    // Properties stored in the PROJECT.PROPERTIES
    public static final String MODELING_MODE = "uml.modeling.mode"; // NOI18N
    public static final String GEN_CODE_SOURCE_FOLDER = "gen.code.source.folder"; // NOI18N
    public static final String UML_PROJECT_ANT_ARTIFACT = "uml.umlproject"; // NOI18N
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT = "uml.javaproject"; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT_ARTIFACTS = "uml.javaproject.artifacts"; // NOI18N
    public static final String REFERENCED_JAVA_PROJECT_SRC = "uml.javaproject.src"; // NOI18N
    public static final String UML_PROJECT_IMPORTS = "uml.imports"; // NOI18N
    public static final String UML_ARTIFACT_PREFIX = "${uml.reference."; // NOI18N
    public static final String CODE_GEN_TEMPLATES = "code.gen.templates"; // NOI18N
    
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    public ComboBoxModel MODELING_MODE_MODEL;
    public String modelingModeValue;
    public ReferencedJavaProjectModel REFERENCED_JAVA_PROJECT_MODEL;
    public JavaSourceRootsModel REFERENCED_JAVA_SOURCE_ROOTS_MODEL;
    public DefaultTableModel UML_PROJECT_IMPORTS_MODEL;
    public String codeGenTemplates;
    
    UMLImportsSupport importsSupport;
    
    // CustomizerRunTest
    
    // Private fields ---------------------------------------------------------
    private UMLProject project;
    private UMLProjectHelper updateHelper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private ReferencedJavaProjectSupport javaRefSupport;
    
    private StoreGroup privateGroup;
    private StoreGroup projectGroup;
    
    public UMLProject getProject()
    {
        return project;
    }
    
    /** Creates a new instance of UMLProjectProperties and initializes them */
    public UMLProjectProperties(
        UMLProject project,
        UMLProjectHelper updateHelper,
        PropertyEvaluator evaluator,
        ReferenceHelper refHelper
        )
    {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.javaRefSupport = new ReferencedJavaProjectSupport(
            evaluator, refHelper, updateHelper.getAntProjectHelper(),
            null, ANT_ARTIFACT_PREFIX );
        
        this.importsSupport =
            new UMLImportsSupport( evaluator, refHelper,
            updateHelper.getAntProjectHelper(),
            UML_ARTIFACT_PREFIX);
        
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        init(); // Load known properties
    }
    
    /** Initializes the visual models
     */
    public void init()
    {
        // Customizer Modeling
        // fetch the stored value
        // SELECTED_MODELING_MODE_MODEL = projectGroup.createStringDocument(
        //  evaluator, MODELING_MODE );
        // construct model with stored value as selected item.
        
        EditableProperties projectProperties = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH );
        
        MODELING_MODE_MODEL = CommonUiSupport.createModelingModeComboBoxModel(
            evaluator.getProperty(MODELING_MODE));
        
        REFERENCED_JAVA_PROJECT_MODEL =
            javaRefSupport.createReferencedJeavaProjectModel(
            REFERENCED_JAVA_PROJECT, evaluator
            .getProperty(REFERENCED_JAVA_PROJECT));
        
        REFERENCED_JAVA_SOURCE_ROOTS_MODEL = javaRefSupport.
            createReferencedJavaSourceRootsModel(
            REFERENCED_JAVA_PROJECT_MODEL,
            (String) projectProperties.get(REFERENCED_JAVA_PROJECT_SRC));
        
        UML_PROJECT_IMPORTS_MODEL = UMLImportsUiSupport.createTableModel(
            importsSupport.itemsIterator(
            (String)projectProperties.get(UML_PROJECT_IMPORTS)));
        
        codeGenTemplates = projectProperties.get(CODE_GEN_TEMPLATES);
    }
    
    public void save()
    {
        try
        {
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction()
            {
                public Object run() throws IOException
                {
                    storeProperties();
                    return null;
                }
            });
            
            // and save the project
            ProjectManager.getDefault().saveProject(project);
            
            // MCF - I am not sure if this bad practice. But I want to
            // avoid having to recreate props in my AssociatedSoruceProvider
            // which will be called a lot.
            // So I am trying to cache these properties in the project.
            project.setUMLProjectProperties(this);
        }
        
        catch (MutexException e)
        {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        
        catch (IOException ex)
        {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void storeProperties() throws IOException
    {
        // Store special properties
        
        // Modify the project dependencies properly
        resolveProjectDependencies();
        
        String[] umlImports = importsSupport.encodeToStrings(
            UMLImportsUiSupport.getIterator(UML_PROJECT_IMPORTS_MODEL));
        
        
        String[] refJavaSrcRoots = javaRefSupport
            .encodeSrcGroupsToStrings(REFERENCED_JAVA_SOURCE_ROOTS_MODEL);
        
        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        EditableProperties privateProperties = updateHelper.getProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        
        // Standard store of the properties
        projectGroup.store(projectProperties);
        privateGroup.store(privateProperties);
        
        projectProperties.setProperty(MODELING_MODE, getProjectMode());
        
//        projectProperties.setProperty(
//            GEN_CODE_SOURCE_FOLDER, getGenCodeSourceFolder());
        
        projectProperties.setProperty(
            REFERENCED_JAVA_PROJECT_SRC, refJavaSrcRoots);
        
        if (codeGenTemplates != null)
        {
            projectProperties.setProperty(CODE_GEN_TEMPLATES, codeGenTemplates);
        }
        
        projectProperties.setProperty(UML_PROJECT_IMPORTS, umlImports);
       
        // Store the property changes into the project
        updateHelper.putProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        
        updateHelper.putProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );
		
        UMLProjectGenerator.fixJavaProjectReferences(
            updateHelper.getAntProjectHelper(),
            REFERENCED_JAVA_PROJECT_MODEL.getProject());
    }
    
    private static String getDocumentText(Document document)
    {
        try
        {
            return document.getText(0, document.getLength());
        }
        
        catch( BadLocationException e )
        {
            return ""; // NOI18N
        }
    }
    
    /** Finds out what are new and removed project dependencies and
     * applyes the info to the project
     */
    private void resolveProjectDependencies()
    {
        
        AntProjectHelper h = updateHelper.getAntProjectHelper();
        
        EditableProperties ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        String oldJavaArtifactRefStr =
            (String)ep.get(this.REFERENCED_JAVA_PROJECT_ARTIFACTS);
        
        if (oldJavaArtifactRefStr != null && oldJavaArtifactRefStr.length() > 0)
        {
            
            String[] oldJavaArtifactRefs = new String[]{oldJavaArtifactRefStr};
            
            for (int i=0; i < oldJavaArtifactRefs.length; i++)
            {
                // Debug.out.println("MCF destroy reference "  // NOI18N
                //	+ oldJavaArtifactRefs[i]);
                refHelper.destroyReference(oldJavaArtifactRefs[i]);
            }
        }
        
        
        // got to get fresh ep, after refHelper modified
        ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        // now add the new ref
        // refHelper.addReference(artifact, uri);
        Project javaSrcProj = REFERENCED_JAVA_PROJECT_MODEL.getProject();
        
        if (javaSrcProj != null )
        {
            String[] refStrs = UMLProjectGenerator
                .addJavaSrcProjRef(h, refHelper, javaSrcProj);
            ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(UMLProjectProperties
                .REFERENCED_JAVA_PROJECT_ARTIFACTS, refStrs);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        
        // Create a set of old and new artifacts.
        Set oldArtifacts = new HashSet();
        Set newArtifacts = new HashSet();
        
        // Create set of removed artifacts and remove them
        Set removed = new HashSet( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set added = new HashSet(newArtifacts);
        added.removeAll(oldArtifacts);

        // 2. now read project.properties and modify rest
        ep = updateHelper.getProperties(
            AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        boolean changed = false;
        String javaSrcProjRefVal = ""; // NOI18N
        
        if (javaSrcProj != null)
        {
            javaSrcProjRefVal =
                UMLProjectGenerator.getJavaSrcProjRefVal(h, javaSrcProj);
            
            changed = true;
        }
        
        ep.setProperty(
            UMLProjectProperties.REFERENCED_JAVA_PROJECT, javaSrcProjRefVal);
        
        File projDir = FileUtil.toFile(
            updateHelper.getAntProjectHelper().getProjectDirectory());
        
        if (changed)
        {
            updateHelper.putProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
    
    // TODO - MCF - I kept this method from copied J2SE code, thinking we might
    // need it or something like it. I have not yet actually used it, and if we
    // do not use it we can delete it.
    /**
     * Tokenize library classpath and try to relativize all the jars.
     * @param property library property name ala "libs.someLib.classpath"
     * @param projectDir project dir for relativization
     * @return relativized library classpath or null if some jar is not collocated
     */
    private String relativizeLibraryClasspath(String property, File projectDir)
    {
        String value = PropertyUtils.getGlobalProperties().getProperty(property);
        // bugfix #42852, check if the classpath
        // property is set, otherwise return null
        if (value == null)
        {
            return null;
        }
        
        String[] paths = PropertyUtils.tokenizePath(value);
        StringBuffer sb = new StringBuffer();
        
        if (sb.length() == 0)
            return null;
        
        else
            return sb.toString();
    }
    
    
    public static String getAntPropertyName( String property )
    {
        if (property != null &&
            property.startsWith( "${") && // NOI18N
            property.endsWith( "}")) // NOI18N
        {
            return property.substring( 2, property.length() - 1 );
        }
        
        else
            return property;
    }
    
    
    public String getProjectMode()
    {
        return evaluator.getProperty(MODELING_MODE);
    }
    
    public String getCurrentProjectMode()
    {
        return evaluator.getProperty(MODELING_MODE);
    }

//    public String getGenCodeSourceFolder()
//    {
//        return evaluator.getProperty(GEN_CODE_SOURCE_FOLDER);
//    }
    
    public String getCodeGenTemplates()
    {
        String templates = evaluator.getProperty(CODE_GEN_TEMPLATES);
        
        if (templates == null)
            templates = "";
        
        return templates;
    }
    
    public final static String TEMPLATE_DELIMITER = "|";
    
    public List<String> getCodeGenTemplatesArray()
    {
        return new ArrayList<String>(Arrays.asList(StringTokenizer2.toArray(
            getCodeGenTemplates(), TEMPLATE_DELIMITER))); // NOI18N
    }

    public void setCodeGenTemplates(List<String> val)
    {
        codeGenTemplates = 
            StringTokenizer2.delimitedString(val.toArray(), TEMPLATE_DELIMITER);
    }
}
