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

package org.netbeans.modules.websvc.rest.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
public class WebRestSupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of JaxWSLookupProvider */
    public WebRestSupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            
            PropertyChangeListener pcl;
            
            protected void projectOpened() {
                
                final MetadataModel<RestServicesMetadata> wsModel = RestUtils.getRestServicesMetadataModel(prj);
                try {
                    // make sure REST API jar is included in project compile classpath
                    RestUtils.addRestApiJar(prj);
                    
                    wsModel.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                        public Void run(final RestServicesMetadata metadata) {
                            RestServices restServices = metadata.getRoot();
                            pcl = new RestServicesChangeListener(wsModel, prj);
                            restServices.addPropertyChangeListener(pcl);
                            return null;
                        }
                    });
                } catch (java.io.IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
            
            
            protected void projectClosed() {
                final MetadataModel<RestServicesMetadata> wsModel = RestUtils.getRestServicesMetadataModel(prj);
                try {
                    wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                        public Void run(final RestServicesMetadata metadata) {
                            RestServices RestServices = metadata.getRoot();
                            RestServices.removePropertyChangeListener(pcl);
                            return null;
                        }
                    });
                } catch (java.io.IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        };
        
        PrivilegedTemplates templates = new PrivilegedTemplates() {
            public String[] getPrivilegedTemplates() {
                return new String[] {
                    "Templates/WebServices/RESTServicesFromEntities", // NOI18N
                    "Templates/WebServices/RESTServicesFromPatterns",  //NOI18N
                    "Templates/WebServices/RESTClientStubs"    //NOI18N
                };
            }
        };
        
        //ProjectRestServiceNotifier servicesNotifier = new ProjectRestServiceNotifier(prj);
        return Lookups.fixed(new Object[] {openhook, templates});
    }
    
    private class RestServicesChangeListener implements PropertyChangeListener {
        private MetadataModel<RestServicesMetadata> wsModel;
        private Project prj;
        private RestSupport support;
        
        private RequestProcessor.Task updateRestSvcTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                updateRestServices();
            }
        });
        
        RestServicesChangeListener(MetadataModel<RestServicesMetadata> wsModel, Project prj) {
            this.wsModel=wsModel;
            this.prj=prj;
            this.support = prj.getLookup().lookup(RestSupport.class);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            updateRestServices();
        }
        
        private synchronized void updateRestServices() {
            //System.out.println("updating rest services");
            try {
                wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        RestServices root = metadata.getRoot();
                        
                        if (root.sizeRestServiceDescription() > 0) {
                            RestUtils.ensureRestDevelopmentReady(prj);
                        }
                        
                        return null;
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            //System.out.println("done updating rest services");
        }
    }
    
}
