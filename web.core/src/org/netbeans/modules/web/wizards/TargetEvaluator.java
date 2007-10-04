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

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

class TargetEvaluator extends Evaluator { 

    private final boolean debug = false; 

    private ArrayList pathItems = null; 
    private DeployData deployData = null; 
    private String errorMessage = null; 
    private String fileName;
    private boolean initialized = false;
    private String className;
    
    TargetEvaluator(FileType fileType, DeployData deployData) {
	super(fileType); 
	if(debug) { 
	    log("::CONSTRUCTOR"); 
	    log("file type is " + getFileType().toString()); 
	} 
	this.deployData = deployData; 
    }

    String getErrorMessage() { 
	if(errorMessage == null) return ""; 
	else return errorMessage; 
    } 

    /**
     * Used to get the deploy data object
     */
    DeployData getDeployData() { 
	return deployData; 
    } 

    /**
     * Used by the various wizard panels to display the classname of
     * the target
     */
    String getClassName() {
        return className;
	/*
	if(pathItems == null || pathItems.isEmpty()) return ""; 
	else { 
	    StringBuffer buf = new StringBuffer();
	    Iterator iterator = pathItems.iterator(); 
	    while(iterator.hasNext()) { 
		buf.append((String)(iterator.next())); 
		if(iterator.hasNext())
		    buf.append("."); //NOI18N
		
	    }
	    return buf.toString(); 
	}
        */
    } 

    /**
     * Used by the various wizard panels to display the classname of
     * the target
     */
    
    //void setClassName(String fileName, FileObject targetFolder) {
    void setClassName(String fileName, String targetFolder) {
        if (targetFolder.length()>0)
            className=targetFolder+"."+fileName;
        else className=fileName;
        this.fileName=fileName;
        /*
	if(debug) log("::setClassName(" + fileName + ")"); //NOI18N
        if (!initialized) {
            initialized=true;
        } else {
            pathItems.remove (pathItems.size()-1);
        }
        pathItems.add(fileName);
        
        try {
            checkFile(pathItems.iterator(),targetFolder);
            this.fileName=fileName;
            if(debug) 
                log("\tNumber of path items: " + pathItems.size()); //NOI18N
            return;
        } catch (IOException ex) {}
        
        setAlternativeName(fileName,targetFolder);
	if(debug) 
	    log("\tNumber of path items: " + pathItems.size()); //NOI18N
        */
    }
    
    /**
     * Used by the DD info panels to generate default names
     */    
    String getFileName() {
        return fileName;
    } 

    /**
     * Used by the servlet wizard when creating the files
     */
    Iterator getPathItems() {
        if(debug) log("::getPathItems()"+pathItems.size()); //NOI18N;
	return pathItems.iterator(); 
    } 
    
    String getTargetPath() { 
	return super.getTargetPath(pathItems.iterator()); 
    }
    
    /**
     * Used by the ObjectNameWizard panel to set the target folder
     * gotten from the system wizard initially. 
     */
    
    void setInitialFolder(DataFolder selectedFolder, Project p) {
	if(selectedFolder == null) { 
	    if(debug) log("\t" + "No target folder!"); //NOI18N
	    return; 
	}
        FileObject targetFolder = selectedFolder.getPrimaryFile();
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
            deployData.setWebApp(DeployData.getWebAppFor(groups [i].getRootFolder ()));
        }
        if (packageName==null) packageName="";
        setInitialPath(packageName);
    }
    
    /** 
     * Used by the system wizard to check whether the input so far is valid
     */
    boolean isValid() { 
        return true;
    } 
    
    

    /**
     * Calculates the package name for a new Servlet/Filter/Listener
     * based on the path to the file system relative to the target
     * directory. If the user selected a directory from the web module
     * file system under WEB-INF/classes, then we strip off the
     * WEB-INF/classes portion from the path name. 
     */ 
    
    private void setInitialPath(String dirPath) { 

	if(debug) log("::setInitialPath()"); 
        
	pathItems = new ArrayList(); 
        
	String path[] = dirPath.split("/"); //NOI18N
	if(path.length > 0) { 
	    for(int i=0; i<path.length; ++i) { 
		if(!path[i].equals("")) {
		    pathItems.add(path[i]); 
		}
	    }
	}
        if(debug) log("::setInitialPath():pathItems.size() "+pathItems.size());
    } 

    private static void log(String s) { 
	System.out.println("TargetEvaluator" + s); 
    }
    
    private void setAlternativeName (String fileName, FileObject targetFolder) {
 	int index = 0; 
	String tempName = fileName; 
	boolean pathOK = false; 
        while(!pathOK) {
	    pathItems.remove(tempName); 
	    tempName = fileName.concat("_").concat(String.valueOf(++index)); 
	    pathItems.add(tempName);
	    try { 
		checkFile(pathItems.iterator(),targetFolder); 
		pathOK = true;
                this.fileName=tempName;
	    }
	    catch(IOException ioex) {
                pathOK = true;
            } 
        }
    }

}
