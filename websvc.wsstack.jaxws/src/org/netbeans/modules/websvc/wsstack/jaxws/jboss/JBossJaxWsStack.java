/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.wsstack.jaxws.jboss;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;


/**
 *
 * @author mkuchtiak
 */
public class JBossJaxWsStack implements WSStackImplementation<JaxWs> {
    private static final String JAXWS_TOOLS_JAR = "client/jaxws-tools.jar"; //NOI18N
    
    private File root;
    private String version;
    private JaxWs jaxWs;
    
    public JBossJaxWsStack(File root) {
        this.root = root;
        try {
            version = resolveImplementationVersion();
            if (version == null) {
                // Default Version
                version = "2.1.3"; // NOI18N
            }
        } catch (IOException ex) {
            // Default Version
            version = "2.1.3"; // NOI18N
        };
        jaxWs = new JaxWs(getUriDescriptor());
    }

    public JaxWs get() {
        return jaxWs;
    }

    public WSStackVersion getVersion() {
        return WSStackFactory.createWSStackVersion(version);
    }

    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxWs.Tool.WSIMPORT) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSIMPORT));
        } else if (toolId == JaxWs.Tool.WSGEN) {
            return WSStackFactory.createWSTool(new JaxWsTool(JaxWs.Tool.WSGEN));
        } else {
            return null;
        }
    }

    private JaxWs.UriDescriptor getUriDescriptor() {
        return new JaxWs.UriDescriptor() {

            public String getServiceUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return applicationRoot+"/"+serviceName; //NOI18N
            }

            public String getDescriptorUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?wsdl"; //NOI18N
            }

            public String getTesterPageUri(String applicationRoot, String serviceName, String portName, boolean isEjb) {
                return getServiceUri(applicationRoot, serviceName, portName, isEjb)+"?Tester"; //NOI18N
            }
            
        };
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxWs.Feature.JSR109 || feature == JaxWs.Feature.SERVICE_REF_INJECTION || feature == JaxWs.Feature.TESTER_PAGE || feature == JaxWs.Feature.WSIT) {
            return true;
        } else {
            return false;
        }    
    }
    
    private String resolveImplementationVersion() throws IOException {
        // take webservices-tools.jar file
        File wsToolsJar = new File(root, JAXWS_TOOLS_JAR);
        
        if (wsToolsJar.exists()) {            
            JarFile jarFile = new JarFile(wsToolsJar);
            JarEntry entry = jarFile.getJarEntry("com/sun/tools/ws/version.properties"); //NOI18N
            if (entry != null) {
                InputStream is = jarFile.getInputStream(entry);
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                String ln = null;
                String ver = null;
                while ((ln=r.readLine()) != null) {
                    String line = ln.trim();
                    if (line.startsWith("major-version=")) { //NOI18N
                        ver = line.substring(14);
                    }
                }
                r.close();
                return ver;
            }           
        }
        return null;
    }
    
    private class JaxWsTool implements WSToolImplementation {
        JaxWs.Tool tool;
        JaxWsTool(JaxWs.Tool tool) {
            this.tool = tool;
        }

        public String getName() {
            return tool.getName();
        }

        public URL[] getLibraries() {
            
            File clientRoot = new File(root, "client"); // NOI18N
            File jaxWsAPILib = new File(root, "jboss-jaxws.jar"); // NOI18N
            try {
                // JBoss without jbossws 
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-common-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-logging-spi.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N

                        new File(clientRoot, "jbossws-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-jaxws-ext.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jboss-jaxws.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jboss-saaj.jar").toURI().toURL()    // NOI18N
                    };
                }
                jaxWsAPILib = new File(root, "jbossws-native-jaxws.jar"); // NOI18N
                // JBoss+jbossws-native
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-common-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-logging-spi.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N

                        new File(clientRoot, "jbossws-native-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jbossws-native-jaxws-ext.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jbossws-native-jaxws.jar").toURI().toURL(),    // NOI18N
                        new File(clientRoot, "jbossws-native-saaj.jar").toURI().toURL()    // NOI18N
                    };
                }
                jaxWsAPILib = new File(root, "jaxws-api.jar"); // NOI18N
                // JBoss+jbossws-metro
                if (jaxWsAPILib.exists()) {
                    return new URL[] {
                        new File(clientRoot, "wstx.jar").toURI().toURL(),   // NOI18N
                        new File(clientRoot, "jaxws-tools.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-common-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "jboss-logging-spi.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "stax-api.jar").toURI().toURL(),    // NOI18N

                        new File(clientRoot, "jbossws-metro-client.jar").toURI().toURL(),  // NOI18N
                        new File(clientRoot, "saaj-api.jar").toURI().toURL()    // NOI18N
                    };
                }
            } catch (MalformedURLException ex) {
                return new URL[0];
            }
            return null;
        }
        
    }

}
