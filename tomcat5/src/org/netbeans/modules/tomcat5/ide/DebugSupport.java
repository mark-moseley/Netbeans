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

package org.netbeans.modules.tomcat5.ide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;

import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;

import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;


/** Debug support addition for Tomcat5
 *
 * @author Martin Grebac
 */
public class DebugSupport {
        
    private static final String JSP_SERVLET_NAME  = "jsp";                          //NOI18N
    private static final String JSP_SERVLET_CLASS = "org.apache.jasper.servlet.JspServlet"; //NOI18N

    private static final String MAPPED_PARAM_NAME =  "mappedfile"; //NOI18N
    private static final String MAPPED_PARAM_VALUE = "true"; //NOI18N

    public static void allowDebugging(TomcatManager tm) throws IOException, SAXException {
        String url = tm.getUri();
        
        // find the web.xml file
        File webXML = getDefaultWebXML(tm);
        if (webXML == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(url));
            return;
        }
        WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        if (webApp == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception(url));
            return;
        }
        boolean needsSave = setMappedProperty(webApp);
        if (needsSave) {
            OutputStream os = new FileOutputStream(webXML);
            try {
                webApp.write(os);
            } finally {
                os.close();
            }
        }
    }
    
    private static File getDefaultWebXML(TomcatManager tm) {
        File cb = tm.getCatalinaBaseDir();
        if (cb == null)
            cb = tm.getCatalinaHomeDir();
        File webXML = new File(cb, "conf" + File.separator + "web.xml");
        if (webXML.exists())
            return webXML;
        return null;
    }
    
    private static boolean setMappedProperty(WebApp webApp) {

        boolean changed=false;
        boolean isServlet=false;
        
        Servlet[] servlets = webApp.getServlet();
        int i;
        for(i=0;i<servlets.length;i++) {
            if ((servlets[i].getServletName().equals(JSP_SERVLET_NAME)) && 
                (servlets[i].getServletClass().equals(JSP_SERVLET_CLASS))) {
                isServlet=true;
                break;
            }
        }
        
        if (!isServlet) {
            try {
                Servlet servlet = (Servlet)webApp.createBean("Servlet"); //NOI18N
                servlet.setServletName(JSP_SERVLET_NAME);
                servlet.setServletClass(JSP_SERVLET_CLASS);
                InitParam initParam = (InitParam)servlet.createBean("InitParam"); //NOI18N
                initParam.setParamName(MAPPED_PARAM_NAME);
                initParam.setParamValue(MAPPED_PARAM_VALUE);
                servlet.addInitParam(initParam);
                webApp.addServlet(servlet);
                changed=true;
            } catch (ClassNotFoundException ex) {}
        } else {
            try {
                boolean isInitparam = false;
                InitParam[] initparams = servlets[i].getInitParam();
                int j;
                for (j=0;j<initparams.length;j++) {
                    if ((initparams[j].getParamName().equals(MAPPED_PARAM_NAME))) {
                        isInitparam=true;
                        break;
                    }
                }
                if (isInitparam) {
                    if (!initparams[j].getParamValue().equals(MAPPED_PARAM_VALUE)) {
                        initparams[j].setParamValue(MAPPED_PARAM_VALUE);
                        changed=true;
                    }
                } else {
                    InitParam initParam = (InitParam)servlets[i].createBean("InitParam"); //NOI18N
                    initParam.setParamName(MAPPED_PARAM_NAME);
                    initParam.setParamValue(MAPPED_PARAM_VALUE);
                    servlets[i].addInitParam(initParam);
                    changed=true;
                }
            } catch (ClassNotFoundException ex) {}
        }

        return changed;
    }

}
