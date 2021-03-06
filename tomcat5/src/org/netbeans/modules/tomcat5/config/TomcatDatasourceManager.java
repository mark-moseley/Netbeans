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

package org.netbeans.modules.tomcat5.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.gen.GlobalNamingResources;
import org.netbeans.modules.tomcat5.config.gen.Parameter;
import org.netbeans.modules.tomcat5.config.gen.ResourceParams;
import org.netbeans.modules.tomcat5.config.gen.Server;

/**
 * DataSourceManager implementation
 *
 * @author sherold
 */
public class TomcatDatasourceManager implements DatasourceManager {
    
    private final TomcatManager tm;
    
    /**
     * Creates a new instance of TomcatDatasourceManager
     */
    public TomcatDatasourceManager(DeploymentManager dm) {
        tm = (TomcatManager) dm;
    }

    /**
     * Get the global datasources defined in the GlobalNamingResources element
     * in the server.xml configuration file.
     */
    public Set<Datasource> getDatasources() {
        HashSet<Datasource> result = new HashSet<Datasource>();
        File serverXml = tm.getTomcatProperties().getServerXml();
        Server server;
        try {
            server = Server.createGraph(serverXml);
        } catch (IOException e) {
            // ok, log it and give up
            Logger.getLogger(TomcatDatasourceManager.class.getName()).log(Level.INFO, null, e);
            return Collections.<Datasource>emptySet();
        } catch (RuntimeException e) {
            // server.xml file is most likely not parseable, log it and give up
            Logger.getLogger(TomcatDatasourceManager.class.getName()).log(Level.INFO, null, e);
            return Collections.<Datasource>emptySet();
        }
        GlobalNamingResources[] globalNamingResources = server.getGlobalNamingResources();
        if (globalNamingResources.length > 0) {
            // only one GlobalNamingResources element is allowed
            GlobalNamingResources globalNR = globalNamingResources[0];
            if (tm.getTomcatVersion() != TomcatVersion.TOMCAT_50) {
                // Tomcat 5.5.x or Tomcat 6.0.x
                int length = globalNR.getResource().length;
                for (int i = 0; i < length; i++) {
                    String type = globalNR.getResourceType(i);
                    if ("javax.sql.DataSource".equals(type)) { // NOI18N
                        String name     = globalNR.getResourceName(i);
                        String username = globalNR.getResourceUsername(i);
                        String url      = globalNR.getResourceUrl(i);
                        String password = globalNR.getResourcePassword(i);
                        String driverClassName = globalNR.getResourceDriverClassName(i);
                        if (name != null && username != null && url != null && driverClassName != null) {
                            // return the datasource only if all the needed params are non-null except the password param
                            result.add(new TomcatDatasource(username, url, password, name, driverClassName));
                        }
                    }
                }
            } else {
                // Tomcat 5.0.x
                int length = globalNR.getResource().length;
                ResourceParams[] resourceParams = globalNR.getResourceParams();
                for (int i = 0; i < length; i++) {
                    String type = globalNR.getResourceType(i);
                    if ("javax.sql.DataSource".equals(type)) { // NOI18N
                        String name = globalNR.getResourceName(i);
                        // find the resource params for the selected resource
                        for (int j = 0; j < resourceParams.length; j++) {
                            if (name.equals(resourceParams[j].getName())) {
                                Parameter[] params = resourceParams[j].getParameter();
                                HashMap paramNameValueMap = new HashMap(params.length);
                                for (Parameter parameter : params) {
                                    paramNameValueMap.put(parameter.getName(), parameter.getValue());
                                }
                                String username = (String) paramNameValueMap.get("username"); // NOI18N
                                String url      = (String) paramNameValueMap.get("url"); // NOI18N
                                String password = (String) paramNameValueMap.get("password"); // NOI18N
                                String driverClassName = (String) paramNameValueMap.get("driverClassName"); // NOI18N
                                if (username != null && url != null && driverClassName != null) {
                                    // return the datasource only if all the needed params are non-null except the password param
                                    result.add(new TomcatDatasource(username, url, password, name, driverClassName));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public void deployDatasources(Set<Datasource> datasources) 
    throws ConfigurationException, DatasourceAlreadyExistsException {
        // nothing needs to be done here
    }
    
}
