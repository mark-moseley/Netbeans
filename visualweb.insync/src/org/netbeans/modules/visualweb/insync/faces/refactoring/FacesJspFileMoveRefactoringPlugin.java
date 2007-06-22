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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author
 */
public class FacesJspFileMoveRefactoringPlugin extends FacesRefactoringPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(FacesJspFileMoveRefactoringPlugin.class.getName());
	
    private List<FileObject> filesToMove = new ArrayList<FileObject>();
    private Map<FileObject, String> folderPostfix = new HashMap<FileObject, String>();

    /**
     * 
     * @param refactoring 
     */
    public FacesJspFileMoveRefactoringPlugin(MoveRefactoring refactoring) {
        super(refactoring);
        setup(refactoring.getRefactoringSource().lookupAll(FileObject.class), "", true);
    }
    
    /**
     * 
     * @param refactoring 
     */
    public FacesJspFileMoveRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
        setup(Collections.singleton(refactoring.getRefactoringSource().lookup(FileObject.class)), "", true);
    }

    private MoveRefactoring getMoveRefactoring() {
        return (MoveRefactoring) getRefactoring();
    }
    
    private RenameRefactoring getRenameRefactoring() {
    	return (RenameRefactoring) getRefactoring();
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (FacesRefactoringUtils.isVisualWebJspFile(fo)) {
            	folderPostfix.put(fo, postfix);
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
            	folderPostfix.put(fo, postfix);
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addSlash = !"".equals(postfix);
                Collection col = new ArrayList();
                for (FileObject fo2: fo.getChildren()) {
                    if (!fo2.isFolder() || (fo2.isFolder() && recursively)) 
                        col.add(fo2);
                }
                setup(col, postfix +(addSlash?"/":"") +fo.getName(), recursively); // NOI18N
            }
        }
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
    	if (getRefactoring() instanceof RenameRefactoring) {    		
    		FileObject fileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);	
            String newName = getRenameRefactoring().getNewName();
            if (fileObject != null) {
            	if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
            		if (fileObject.isFolder()) {
            			if (FacesRefactoringUtils.isSpecialFolderName(fileObject.getNameExt())) {
            				return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotRenameSpecialFolders")); // NOI18N
            			}
            			if (newName == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                        } else {
                            newName = newName.trim();
                            if (newName.length() == 0) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameCannotBeEmpty")); // NOI18N
                            }
                            if (newName.equals(fileObject.getName())) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameSameAsOldName")); // TODO I18N
                            }
                            if (!Utilities.isJavaIdentifier(newName)) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameIsNotAValidJavaIdentifier")); // TODO I18N
                            }
                        }
                        FileObject siblingWithNewName = fileObject.getParent().getFileObject(newName, fileObject.getExt());
                        if (siblingWithNewName != null && siblingWithNewName.isValid()) {
                            return new Problem(false, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameFolderAlreadyExists")); // NOI18N
                        }                       
            		}
            	}
            }
    	}
    	if (getRefactoring() instanceof MoveRefactoring) {
    		FileObject fileObject = getMoveRefactoring().getRefactoringSource().lookup(FileObject.class);
    		URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
	        if (fileObject != null) {
                if (targetURL == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                } 
                if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
	                if (fileObject.isFolder()) {
	                	if (FacesRefactoringUtils.isSpecialFolderName(fileObject.getNameExt())) {
	                		return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotMoveSpecialFolders")); // NOI18N
	                	}
	                } else {	                    
	                    if (FacesRefactoringUtils.isVisualWebJspFile(fileObject)) {
	                        Project project = FileOwnerQuery.getOwner(fileObject);
	                        
	                        FileObject targetFileObject = URLMapper.findFileObject(targetURL);
	                        if (targetFileObject == null) {
	                            File file = null;
	                            try {
	                                file = new File(targetURL.toURI());
	                            } catch (URISyntaxException e) {
	                                Exceptions.printStackTrace(e);
	                            }
	                            if (file == null) {
	                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
	                            }
	                            targetFileObject = FileUtil.toFileObject(file);
	                            if (targetFileObject == null) {
	                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
	                            }
	                        }
	                        Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
	                        if (targetFileObjectProject == null) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
	                        }
	                                               
	                        if (!project.equals(targetFileObjectProject)) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
	                        }
	                        
	                        if (!targetFileObject.isFolder()) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
	                        }
	                        
	                        if (targetFileObject.equals(fileObject.getParent())) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotMoveToSameLocation")); // NOI18N
	                        }
	                        
	                        FileObject targetFileObjectWithSameName = targetFileObject.getFileObject(fileObject.getName(), fileObject.getExt());
	                        if (targetFileObjectWithSameName != null && targetFileObjectWithSameName.isValid()) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetJspFileAlreadyExists")); // NOI18N
	                        }
	                        
	                        Project fileObjectProject = FileOwnerQuery.getOwner(fileObject);
	                        if (fileObjectProject.equals(targetFileObjectProject)) {
	//                            if (FacesRefactoringUtils.isSpecialFolderName(targetFileObject.getNameExt()) {
	//                                return new Problem(false, NbBundle.getMessage(FacesRenameRefactoringPlugin.class, "WRN_TargetLocationIsASpecialFolder")); // NOI18N
	//                            }
	                        } else {
	                            boolean isTargetProjectAVisualWebProject = JsfProjectUtils.isJsfProject(targetFileObjectProject);
	                            if (isTargetProjectAVisualWebProject) {
	                                
	                            }
	                            boolean isStartPage = JsfProjectUtils.isStartPage(fileObject);
	                            if (isStartPage) {
	                                return new Problem(false, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "WRN_MovingProjectStartPage")); // NOI18N
	                            }
	                        }
	                    }
	                }
	            }
	        }
    	}
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession refactoringSession = refactoringElements.getSession();
        if (getRefactoring() instanceof MoveRefactoring) {        	
	        for (FileObject refactoringSourcefileObject : filesToMove) {
	            if (FacesRefactoringUtils.isFileUnderDocumentRoot(refactoringSourcefileObject)) {
	                if (FacesRefactoringUtils.isVisualWebJspFile(refactoringSourcefileObject)) {
                		URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
                        
                        if (targetURL == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                        }
                        
                        FileObject targetFileObject = URLMapper.findFileObject(targetURL);
                        if (targetFileObject == null) {
                            File file = null;
                            try {
                                file = new File(targetURL.toURI());
                            } catch (URISyntaxException e) {
                                Exceptions.printStackTrace(e);
                            }
                            if (file == null) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                            }
                            targetFileObject = FileUtil.toFileObject(file);
                            if (targetFileObject == null) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                            }
                        }
                	
	                    Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
	                    if (project == null) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_SourceIsNotInAnOpenProject")); // NOI18N
	                    }
	                    
	                    Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
	                    if (targetFileObjectProject == null) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
	                    }
	                    
	                    if (!project.equals(targetFileObjectProject)) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
	                    }
	                    
	                    if (!targetFileObject.isFolder()) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
	                    }
	                    
	                    // Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
	                    if (!isDelegatedRefactoring(getRefactoring())) {
	                        FileObject javaFileObject = FacesModel.getJavaForJsp(refactoringSourcefileObject);
	                        if (javaFileObject == null) {
	                            // TODO
	                        }
	                        
	                        FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
	                        if (targetDocumentRoot == null) {
	                            // TODO
	                        }
	                        
	                        // Compute relative path of target folder to document root.
	                        String targetRelativePath = FileUtil.getRelativePath(targetDocumentRoot, targetFileObject); // NOI18N
	                        
	                        // Delegate to Java refactoring to rename the backing bean
	                        MoveRefactoring javaMoveRefactoring = new MoveRefactoring(Lookups.singleton(javaFileObject));
	                        
	                        // new folder
	                        
	                        FileObject targetPageBeanRoot = JsfProjectUtils.getPageBeanRoot(targetFileObjectProject);
	                        if (targetPageBeanRoot == null) {
	                            // TODO
	                        }
	                        
	                        String targetJavaPackage = 
	                        	JsfProjectUtils.getProjectProperty(targetFileObjectProject, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE) +
	                        	"." +
	                        	targetRelativePath.replace('/', '.');
	                        FileObject targetJavaFolder = targetPageBeanRoot.getFileObject(targetRelativePath);
	                        if (targetJavaFolder == null) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaPackage));
	                        }
	                        
	                        URL url = URLMapper.findURL(targetJavaFolder, URLMapper.EXTERNAL);
	                        try {
	                            javaMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
	                        } catch (MalformedURLException ex) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaPackage));
	                        }
	                        
	                        // Set ClasspathInfo
	                        ClasspathInfo classpathInfo = FacesRefactoringUtils.getClasspathInfoFor(javaFileObject);                        
	                        javaMoveRefactoring.getContext().add(classpathInfo);
	                        
	                        // Indicate delegation
	                        javaMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
	                        Problem problem = javaMoveRefactoring.prepare(refactoringSession);
	                        if (problem != null) {
	                            return problem;
	                        }
	                    }
	                    
	                    FileObject webFolderFileobject = JsfProjectUtils.getDocumentRoot(project);
	                    FileObject parentObject = refactoringSourcefileObject.getParent();
	                    String parentRelativePath = FileUtil.getRelativePath(webFolderFileobject, parentObject);
	                    String oldRelativePagePath = parentRelativePath +
	                                          (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          refactoringSourcefileObject.getNameExt();
	                    
	                    FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
	                    if (targetDocumentRoot == null) {
	                        // TODO
	                    }
	                    
	                    String targetRelativePath = FileUtil.getRelativePath(targetDocumentRoot, targetFileObject);
	                    String newRelativePagePath = targetRelativePath +
	                                          (targetRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          refactoringSourcefileObject.getNameExt();
	                    
	                    // Add a refactoring element to set the start page
	                    if (JsfProjectUtils.isStartPage(refactoringSourcefileObject)) {
	                        refactoringElements.addFileChange(getRefactoring(), new SetProjectStartPageRefactoringElement(project, oldRelativePagePath, newRelativePagePath));
	                    }
	                    
	                    // Handle navigation view ids
	                    WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
	                    if (webModule != null){
	                        // find all jsf configuration files in the web module
	                        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
	                        
	                        if (configs != null){
	                            List <FacesRefactoringUtils.OccurrenceItem> items = FacesRefactoringUtils.getAllFromViewIdOccurrences(webModule, "/" + oldRelativePagePath, "/" + newRelativePagePath);
	                            for (FacesRefactoringUtils.OccurrenceItem item : items) {
	                                refactoringElements.add(getRefactoring(), new JSFConfigRenameFromViewIdElement(item));
	                            }
	                            items = FacesRefactoringUtils.getAllToViewOccurrences(webModule, "/" + oldRelativePagePath, "/" + newRelativePagePath);
	                            for (FacesRefactoringUtils.OccurrenceItem item : items) {
	                                refactoringElements.add(getRefactoring(), new JSFConfigRenameToViewIdElement(item));
	                            }
	                        }
	                    }
	                }
	            }
	        }
        } else if (getRefactoring() instanceof RenameRefactoring) {
        	FileObject folderFileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        	
        	// Only folder rename comes here
        	assert folderFileObject.isFolder();
        	
        	Project project = FileOwnerQuery.getOwner(folderFileObject);
            if (project == null) {
                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_SourceIsNotInAnOpenProject")); // NOI18N
            }
        	
    		// compute the new target and make sure it exists
        	String targetFolderName = getRenameRefactoring().getNewName();
        	            	
        	// Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
            if (!isDelegatedRefactoring(getRefactoring())) {

            }
    	}

        return null;
    }
}
