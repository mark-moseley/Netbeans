/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.openide.ErrorManager;

import org.openide.filesystems.*;

/** Data related to compilation attached to one JSP page.
 *  Basically a copy of the data retrieved from the compilation plugin.
 *  This data will change during the compilation process.
 *  This class is also responsible for translating File-view of compiled data to
 *  FileObject-view, including the creation of the necessary filesystems.
 *
 * @author  Petr Jiricka
 * @version 
 */
public class CompileData {

    private JspDataObject jspPage;
    private FileObject docRoot;
    //private ServerInstance serverInstance;
    private String servletEncoding;
    
    private final static boolean debug = false;
    
    private File servletJavaRoot;
    private String servletResourceName;
     

    /** Creates new CompileData */
    public CompileData(JspDataObject jspPage) {
        this.jspPage = jspPage;
        this.docRoot = WebModule.getWebModule (jspPage.getPrimaryFile()).getDocumentBase ();
        String jspResourcePath = JspCompileUtil.findRelativeContextPath(docRoot, jspPage.getPrimaryFile());
        JSPServletFinder finder = JSPServletFinder.findJSPServletFinder (docRoot);
        servletJavaRoot = finder.getServletTempDirectory();
        servletResourceName = finder.getServletResourcePath(jspResourcePath);
        servletEncoding = finder.getServletEncoding(jspResourcePath);
    }
    
    public FileObject getServletJavaRoot() {
        if ((servletJavaRoot != null) && servletJavaRoot.exists()) {
            return FileUtil.toFileObject(servletJavaRoot);
        }
        else {
            return null;
        }
    }
    
/*    public FileObject getServletJavaRoot() {
        // PENDING - this is incorrect!!!
        return WebModule.getWebModule (jspPage.getPrimaryFile ()).getJavaSourcesFolder ();
    }*/
    
    public String getServletResourceName() {
        return servletResourceName;
    }
    
    private File getServletFile() {
        if (servletJavaRoot == null) {
            return null;
        }
        URI rootURI = servletJavaRoot.toURI();
        URI servletURI = rootURI.resolve(servletResourceName);
        return new File(servletURI);
    }
    
    public FileObject getServletFileObject() {
        FileObject root = getServletJavaRoot();
        if (root == null) {
            return null;
        }
        File servlet = getServletFile();
        if ((servlet == null) || !servlet.exists()) {
            return null;
        }
        
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(servlet));
        if (fo != null) {
            return fo;
        }
        try {
            FileSystem rootFs = root.getFileSystem();
            root.getFileSystem().refresh(false);
            return root.getFileObject(getServletResourceName());
        }
        catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        /*FileObject fo[] = FileUtil.fromFile(servlet);
        // get a fileobject from the same FS as the root
        try {
            FileSystem rootFs = root.getFileSystem();
            for (int i = 0; i < fo.length; i++) {
                if (fo[i].getFileSystem() == rootFs) {
                    return fo[i];
                }
            }
            // not found, needs refresh
            root.getFileSystem().refresh(false);
            return root.getFileObject(getServletResourceName());
              //JspCompileUtil.findRelativeResource(root, getServletResourceName());
        }
        catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
         */
        return null;
    }
    
    
    /** Returns encoding for the servlet generated from the JSP. */
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--COMPILE DATA--"); // NOI18N
        sb.append("\n"); // NOI18N
//        sb.append("server          : " + serverInstance); // NOI18N
//        sb.append("\n"); // NOI18N
        sb.append("JSP page        : " + jspPage.getPrimaryFile().getPackageNameExt('/','.')); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletJavaRoot : " + servletJavaRoot + ", exists= " +  // NOI18N
            ((servletJavaRoot == null) ? "false" : "" + servletJavaRoot.exists())); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletResource : " + servletResourceName + ", fileobject exists= " +  // NOI18N
            (getServletFileObject() != null)); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletFile : " + getServletFile().getAbsolutePath() + ", exists= " +  // NOI18N
            getServletFile().exists()); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("--end COMPILE DATA--"); // NOI18N
        return sb.toString();
    }
    
}
