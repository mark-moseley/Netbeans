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

package org.netbeans.modules.uml.codegen.java;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

/*
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
*/

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.modules.uml.core.coreapplication.ICodeGenerator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Operation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;


public class JavaCodegen implements ICodeGenerator {

    public final static String COLON_COLON = "::"; // NOI18N
    public final static String JAVA = "java"; // NOI18N
    public final static String JAVA_EXT = ".java"; // NOI18N
    public final static String DOT = "."; // NOI18N
    

    // templates map TBD of config GUI & API integration 
    public static HashMap<String, TemplateDesc[]> stereotypesToTemplates = new HashMap<String, TemplateDesc[]>();

    static {
	// dummy test config
	stereotypesToTemplates.put("webservice", 
				   new TemplateDesc[] {new TemplateDesc("webservice.ftl", "java"),
						       new TemplateDesc("wsdl.ftl", "xml")});
	stereotypesToTemplates.put("st2", new TemplateDesc[] {new TemplateDesc("st2.ftl", "java")});
    }


    public JavaCodegen(){

    }

    
    public void generate(IClassifier classifier, String targetFolderName, boolean backup) {

	try {
	    
	    
	    ClassInfo clinfo = new ClassInfo(classifier);
        
	    // skip inner class/interface/enumeration elements
	    // as they are taken care of by their outer class code gen
	    if (clinfo.getOuterClass() != null)
		return;
        
	    clinfo.setMethodsAndMembers(classifier);
	    clinfo.setExportSourceFolderName(targetFolderName);
	    clinfo.setComment(classifier.getDocumentation());

	    File existingFile = sourceFileExists(targetFolderName, classifier);
        
	    if (existingFile != null) {

		if (backup) {
		    FileObject buFileObj = backupFile(existingFile);		
		    System.out.println("buFileObj = "+ buFileObj);
		    if (buFileObj != null) {
			FileObject efo = FileUtil.toFileObject(existingFile);
			if (efo != null) 
			    efo.delete();
		    }
		}
		
		try {
		    clinfo.updateFilename(existingFile.getCanonicalPath());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    } else {
		// after generation TBD
		existingFile = FileUtil.toFile(
		    clinfo.getExportSourceFolderFileObject());
	    }
        
	    try {
		classifier.addSourceFileNotDuplicate(
		    existingFile.getCanonicalPath());
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }

        
	    // 2 possible places to get templates from - 
	    // registry and teplates subdir of the project 
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
	    FileObject root = fs.getRoot().getFileObject("Templates/UML/CodeGeneration/Java");
	    String projTemplPath = clinfo.getOwningProject().getBaseDirectory()+File.separator+"templates"+File.separator+"java";

	    List<TemplateDesc> templateDescs = templatesToUse(clinfo);
	    Iterator<TemplateDesc> iterDescs = templateDescs.iterator();
	    while(iterDescs.hasNext()) {
		TemplateDesc templDesc = iterDescs.next();	    		
		try {
		    FileObject tfo;
		    File tf = new File(projTemplPath + File.separator + templDesc.templateName);
		    if (tf.exists()) {
			tfo = FileUtil.toFileObject(tf);
		    } else {
			tfo = root.getFileObject(templDesc.templateName);
		    }
		    
		    tfo.setAttribute("javax.script.ScriptEngine", "freemarker");
		    DataObject obj = DataObject.find(tfo);
		    
		    if (clinfo.getExportPackageFileObject() != null) {
			
			DataFolder folder = DataFolder.findFolder(FileUtil.toFileObject(new File(clinfo.getExportSourcePackage())));
			Map parameters = Collections.singletonMap("classInfo", clinfo);
			DataObject n = obj.createFromTemplate(folder, clinfo.getName(), parameters);

		    } else {
			// TBD - couldn't create the package directory for some reason
			;			
		    }
		} catch (Exception e) {
		    e.printStackTrace(System.out);		
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.out);		
	}

    }
    


    private File sourceFileExists(String sourceFolderName, IClassifier classifier)
    {
        File file = null;
        
        String pathName = sourceFolderName + File.separatorChar +
            StringUtilities.replaceAllSubstrings(
                classifier.getFullyQualifiedName(false),
                COLON_COLON, String.valueOf(File.separatorChar)) + JAVA_EXT;
        
	System.out.println("pathName = "+pathName);

        if (pathName != null && pathName.length() > 0)
        {
            file = new File(pathName);
            
            if (!file.exists())
                file = null;
        }
        
        return file;
    }
    

    
    private FileObject backupFile(File file)
    {
        String fileName = file.getName();
        String className = fileName.substring(0, fileName.indexOf('.'));
        
        String[] files = file.getParentFile().list(
            new BackupJavaFilesFilter(fileName));
        
        int nextSeqNum = 0;
        
        for (String curName: files)
        {
            String numStr = curName.substring(
                curName.indexOf(JAVA_EXT)+5); // NOI18N
            
            try
            {
                int seqNum = Integer.parseInt(numStr);

                if (seqNum > nextSeqNum)
                    nextSeqNum = seqNum;
            }

            catch (NumberFormatException ex)
            {
                // silently suppress
            }
        }
        
        nextSeqNum++;
        
        try
        {
            FileObject buFileObj = FileUtil.copyFile(
                FileUtil.toFileObject(file),
                FileUtil.toFileObject(file.getParentFile()),
                className,
                JAVA + nextSeqNum);
            
            return buFileObj;
        }
        
        catch (IOException ex)
        {
            // TODO: conover - provide proper handling
            ex.printStackTrace();
            return null;
        }
    }
    
    
    private static class BackupJavaFilesFilter implements java.io.FilenameFilter
    {
        String searchName;
        
        public BackupJavaFilesFilter(String fileName)
        {
            searchName = fileName;
        }
        
        public boolean accept(File dir, String name)
        {
            return name.contains(searchName);
        }
    }
    

    public List<TemplateDesc> templatesToUse(ClassInfo clinfo) {
	
	List<TemplateDesc> tlist = new ArrayList<TemplateDesc>();

	String stereoName = null;
	IClassifier classElem = clinfo.getClassElement();
	List<Object> stereotypes = classElem.getAppliedStereotypes();
	if ( stereotypes != null && stereotypes.size() > 0 ) {	
	    Iterator iter = stereotypes.iterator();
	    while (iter.hasNext()) {
		IStereotype stereo = (IStereotype)iter.next();
		String name = stereo.getName();	    	    
		TemplateDesc[] templates = stereotypesToTemplates.get(name);
		if (templates != null) {
		    for (int i = 0; i < templates.length; i++) {
			tlist.add(templates[i]);
		    }
		    return tlist;
		}
	    }
	}
	// if nothing found
	TemplateDesc templDef = new TemplateDesc("CompilationUnit.java", "java");
	tlist.add(templDef);
	return tlist;

    }
    

    public static class TemplateDesc {
	public String templateName;
	public String generatedExt;
	
	public TemplateDesc( String name, String extension ) {
	    templateName = name;
	    generatedExt = extension;
	}

    }









}



