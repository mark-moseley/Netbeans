/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspparser;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.core.jsploader.JspParserAccess;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author  pj97932
 */
class TestUtil {
    
    static FileObject getFileInWorkDir(String path, NbTestCase test) throws Exception {
        File f = new File(Manager.getWorkDirPath());
        FileObject workDirFO = FileUtil.fromFile(f)[0];
        StringTokenizer st = new StringTokenizer(path, "/");
        FileObject tempFile = workDirFO;
        while (st.hasMoreTokens()) {
            tempFile = tempFile.getFileObject(st.nextToken());
        }
        return tempFile;
    }
    
     static JspParserAPI.WebModule getWebModule(FileObject fo){
        WebModule wm =  WebModule.getWebModule(fo);
        if (wm == null) {
            return null;
        }
        FileObject wmRoot = wm.getDocumentBase();
        if (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo)) {
            return JspParserAccess.getJspParserWM(WebModule.getWebModule(fo));
        }
        return null;
    }
     
    /*static JspParserAPI.WebModule getWebModule(FileObject wmRoot, FileObject jspFile) throws Exception {
        WebModule wm = createWebModule(new UnpWarWebModuleImplementation(wmRoot));
        return JspParserAccess.getJspParserWM(wm);
    }
    
    private static WebModule createWebModule(WebModuleImplementation impl) throws Exception {
        java.lang.reflect.Constructor c = WebModule.class.getDeclaredConstructor(
            new Class[] {WebModuleImplementation.class});
        c.setAccessible(true);
        return (WebModule)c.newInstance(new Object[] {impl});
    }
    
    static class UnpWarWebModuleImplementation implements WebModuleImplementation {
        
        private FileObject docBase;
        private String contextPath;
        
        public UnpWarWebModuleImplementation(FileObject docBase) {
            this.docBase = docBase;
            contextPath = "";
        }

        public FileObject getDocumentBase () {
            return docBase;
        }
    
        public FileObject getJavaSourcesFolder () {
            return docBase.getFileObject("WEB-INF/classes");
        }
    
        public String getContextPath () {
            return contextPath;
        }
    
        public void setContextPath (String path) {
            this.contextPath = path;
        }
        
        public String getJ2eePlatformVersion (){
            return "";
        }
        
        public ClassPath getJavaSources (){
            return null;
        }
    }*/
    
}
