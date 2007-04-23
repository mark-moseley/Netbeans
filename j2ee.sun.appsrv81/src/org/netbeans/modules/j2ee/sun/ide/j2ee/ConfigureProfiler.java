// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.w3c.dom.Document;

/**
 *
 * @author Ludovic Champenois
 */
public class ConfigureProfiler {
    
    
    private static final String ASENV_INSERTION_POINT_WIN_STRING    = "set AS_JAVA";
    private static final String ASENV_INSERTION_POINT_NOWIN_STRING  = "AS_JAVA";
    
    // removes any existing 'profiler' element and creates new one using provided parameters (if needed)
    static boolean instrumentProfilerInDomain(DeploymentManager dm, String nativeLibraryPath, String[] jvmOptions) {
        DomainEditor dEditor = new DomainEditor(dm);
        
        // Load domain.xml
        Document domainDocument = dEditor.getDomainDocument();
        if (domainDocument == null) {
            return false;
        }
        
        return dEditor.addProfilerElements(domainDocument, nativeLibraryPath, jvmOptions);
    }
    
    // removes any existing 'profiler' element and creates new one using provided parameters (if needed)
    static boolean removeProfilerFromDomain(DeploymentManager dm) {
        DomainEditor dEditor = new DomainEditor(dm);
        
        // Load domain.xml
        Document domainDocument = dEditor.getDomainDocument();
        if (domainDocument == null) {
            return false;
        }
        
        return dEditor.removeProfilerElements(domainDocument);
    }
    
    // replaces the AS_JAVA item in asenv.bat/conf
    static boolean modifyAsEnvScriptFile( DeploymentManager dm, String targetJavaHomePath) {
        
        String ext = (isUnix() ? "conf" : "bat");
        File irf = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
        if (null == irf || !irf.exists()) {
            Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.SEVERE,"installRoot issue");
            return false;
        }
        String installRoot = irf.getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        String asEnvScriptFilePath  = installRoot+"/config/asenv." + ext;
        File asEnvScriptFile = new File(asEnvScriptFilePath);
        if (!asEnvScriptFile.canWrite()) {
            Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.SEVERE,"asenv issue");
            return false;
        }
        String lineBreak = System.getProperty("line.separator");
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            
            String line;
            FileReader fr = new FileReader(asEnvScriptFile);
            br = new BufferedReader(fr);
            StringBuffer buffer = new StringBuffer();
            
            String asJavaString = (isUnix() ? ASENV_INSERTION_POINT_NOWIN_STRING : ASENV_INSERTION_POINT_WIN_STRING);
            
            // copy config file from disk into memory buffer and modify line containing AS_JAVA definition
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith(asJavaString)) {
                    buffer.append(asJavaString);
                    buffer.append('=');
                    buffer.append(targetJavaHomePath);
                } else {
                    buffer.append(line);
                }
                buffer.append(lineBreak);
            }
            //br.close();
            
            // flush modified config file from memory buffer back to disk
            fw = new FileWriter(asEnvScriptFile);
            fw.write(buffer.toString());
            fw.flush();
            //fw.close();
            
            if (isUnix()) {
                Runtime.getRuntime().exec("chmod a+r " + asEnvScriptFile.getAbsolutePath()); //NOI18N
            }
            
            return true;
            
        } catch (RuntimeException re) {
            Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.SEVERE,"",re);
            return false;
        } catch (Exception ex) {
            Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.SEVERE,"",ex);
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.WARNING,"",ioe);
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ioe) {
                    Logger.getLogger(ConfigureProfiler.class.getName()).log(Level.WARNING,"",ioe);
                }
            }
        }
        
    }
    
    static boolean isUnix() {
        return File.separatorChar == '/';
    }    
}
