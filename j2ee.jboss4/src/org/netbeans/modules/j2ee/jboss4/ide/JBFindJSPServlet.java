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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;

/**
 *
 * @author Libor Kotouc
 */
public class JBFindJSPServlet implements FindJSPServlet {
    
    JBDeploymentManager dm;
    
    public JBFindJSPServlet(JBDeploymentManager manager) {
        dm = manager;
    }

    public File getServletTempDirectory(String moduleContextPath) {
        InstanceProperties ip = dm.getInstanceProperties();
        String domainPath = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
        File servletRoot = new File(domainPath, "work/jboss.web/localhost".replace('/', File.separatorChar)); // NOI18N
        String contextRootPath = getContextRootPath(moduleContextPath);
        File workDir = new File(servletRoot, contextRootPath);
        return workDir;
    }

    private String getContextRootPath(String moduleContextPath) {
        if (moduleContextPath.startsWith("/")) {
            moduleContextPath = moduleContextPath.substring(1);
        }
        if (moduleContextPath.length() == 0) {
            moduleContextPath = "/";
        }
        
        return moduleContextPath.replace('/', '_');
    }
    
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {

        String path = "";
        
        String extension = jspResourcePath.substring(jspResourcePath.lastIndexOf("."));
        if (".jsp".equals(extension)) { // NOI18N
            String pkgName = getServletPackageName(jspResourcePath);
            String pkgPath = pkgName.replace('.', '/');
            String clzName = getServletClassName(jspResourcePath);
            path = pkgPath + '/' + clzName + ".java"; // NOI18N
        }
        
        return path;
    }

    // copied from org.apache.jasper.JspCompilationContext
    public String getServletPackageName(String jspUri) {
        String dPackageName = getDerivedPackageName(jspUri);
        if (dPackageName.length() == 0) {
            return JspNameUtil.JSP_PACKAGE_NAME;
        }
        return JspNameUtil.JSP_PACKAGE_NAME + '.' + getDerivedPackageName(jspUri);
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    private String getDerivedPackageName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        return (iSep > 0) ? JspNameUtil.makeJavaPackage(jspUri.substring(0,iSep)) : "";
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return JspNameUtil.makeJavaIdentifier(jspUri.substring(iSep));
    }
    
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }
    
    
    
}
