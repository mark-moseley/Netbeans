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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModuleFactory;
import org.netbeans.spi.glassfish.ServerUtilities;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Williams
 */
public class JavaEEServerModuleFactory implements GlassfishModuleFactory {

    private static JavaEEServerModuleFactory singleton = new JavaEEServerModuleFactory();
    
    private JavaEEServerModuleFactory() {
    }
    
    public static GlassfishModuleFactory getDefault() {
        return singleton;
    }
    
    public boolean isModuleSupported(String glassfishHome, Properties asenvProps) {

        // Do some moderate sanity checking to see if this v3 build looks ok.
        File jar = ServerUtilities.getJarName(glassfishHome, "glassfish-10.0");


        if (jar==null) {
            return false;
        }
        if (jar.exists()) {
            return true;
        }

        return false;
    }

    public Object createModule(Lookup instanceLookup) {
        // When creating JavaEE support, also ensure this instance is added to j2eeserver
        InstanceProperties ip = null;
        GlassfishModule commonModule = instanceLookup.lookup(GlassfishModule.class);
        if(commonModule != null) {
            try {
                Map<String, String> props = commonModule.getInstanceProperties();
                String url = props.get(InstanceProperties.URL_ATTR);
                ip = InstanceProperties.getInstanceProperties(url);
                if(ip == null) {
                    String username = props.get(InstanceProperties.USERNAME_ATTR);
                    String password = props.get(InstanceProperties.PASSWORD_ATTR);
                    String displayName = props.get(InstanceProperties.DISPLAY_NAME_ATTR);
                    ip = InstanceProperties.createInstancePropertiesWithoutUI(
                            url, username, password, displayName, props);
                    
                    if(ip == null) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, 
                                "Unable to create/locate J2EE InstanceProperties for " + url);
                    }
                }
                
                final String installRoot = commonModule.getInstanceProperties().get(
                        GlassfishModule.HOME_FOLDER_ATTR);
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ensureEclipseLinkSupport(installRoot);
                        ensureCometSupport(installRoot);
                    }
                });
            } catch(InstanceCreationException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, null, ex);
            }
        }

        return (ip != null) ? new JavaEEServerModule(instanceLookup, ip) : null;
    }
    
    private static final String CLASS_LIBRARY_TYPE = "j2se"; // NOI18N
    private static final String CLASSPATH_VOLUME = "classpath"; // NOI18N
    private static final String SOURCE_VOLUME = "src"; // NOI18N
    private static final String JAVADOC_VOLUME = "javadoc"; // NOI18N
    
    private static final String ECLIPSE_LINK_LIB = "EclipseLink-GlassFish-V3"; // NOI18N
    private static final String EL_CORE_LIB_PREFIX = "eclipselink-wrapper-10.0"; // NOI18N

    private static final String PERSISTENCE_API_LIB_PREFIX = "javax.javaee-10.0"; // NOI18N
    private static final String PERSISTENCE_JAVADOC = "javaee5-doc-api.zip"; // NOI18N
    
    public boolean ensureEclipseLinkSupport(String installRoot) {
        LibraryManager lmgr = LibraryManager.getDefault();
        Library eclipseLinkLib = lmgr.getLibrary(ECLIPSE_LINK_LIB);
        
        // Verify that existing library is still valid.
        if(eclipseLinkLib != null) {
            List<URL> libraryList = eclipseLinkLib.getContent(CLASSPATH_VOLUME);
            for(URL libUrl: libraryList) {
                String libPath = libUrl.getFile();
                if(!new File(libPath).exists()) {
                    Logger.getLogger("glassfish-javaee").log(Level.FINE, 
                            "libPath does not exists.  Updating " + ECLIPSE_LINK_LIB);
                    try {
                        lmgr.removeLibrary(eclipseLinkLib);
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        // Already removed somehow, ignore.
                    }
                    eclipseLinkLib = null;
                    break;
                }
            }
        }
        
        if(eclipseLinkLib == null) {
            try {
                // classpath, src, javadoc -- library volumes
                List<URL> libraryList = new ArrayList<URL>();
                File f = ServerUtilities.getJarName(installRoot, EL_CORE_LIB_PREFIX);
                if ((f!=null)&&(f.exists())){
                    libraryList.add(f.toURI().toURL());
                }
                f = ServerUtilities.getJarName(installRoot, PERSISTENCE_API_LIB_PREFIX);
                if ((f!=null)&&(f.exists())){
                    libraryList.add(f.toURI().toURL());
                }

                File j2eeDoc = InstalledFileLocator.getDefault().locate(
                        "docs/" + PERSISTENCE_JAVADOC, null, false); // NOI18N
                List<URL> docList = new ArrayList<URL>();
                docList.add(j2eeDoc.toURI().toURL());

                Map<String, List<URL>> contents = new HashMap<String, List<URL>>();
                contents.put(CLASSPATH_VOLUME, libraryList);
                contents.put(JAVADOC_VOLUME, docList);
                
                eclipseLinkLib = lmgr.createLibrary(CLASS_LIBRARY_TYPE, ECLIPSE_LINK_LIB, contents);
                Logger.getLogger("glassfish-javaee").log(Level.FINE, "Created library " + ECLIPSE_LINK_LIB);
            } catch (IOException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch (IllegalArgumentException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                eclipseLinkLib = lmgr.getLibrary(ECLIPSE_LINK_LIB);
                if(eclipseLinkLib == null) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return eclipseLinkLib != null;
    }
 
    private static final String COMET_LIB = "Comet-GlassFish-V3"; // NOI18N
    private static final String COMET_JAR_LIB_PREFIX = "grizzly-module"; // NOI18N
    private static final String GRIZZLY_OPTIONAL_JAR_LIB_PREFIX = "grizzly-optional"; // NOI18N
    
    public boolean ensureCometSupport(String installRoot) {
        LibraryManager lmgr = LibraryManager.getDefault();
        Library cometLib = lmgr.getLibrary(COMET_LIB);
        
        // Verify that existing library is still valid.
        if(cometLib != null) {
            List<URL> libraryList = cometLib.getContent(CLASSPATH_VOLUME);
            for(URL libUrl: libraryList) {
                String libPath = libUrl.getFile();
                if(!new File(libPath).exists()) {
                    Logger.getLogger("glassfish-javaee").log(Level.FINE, 
                            "libPath does not exists.  Updating " + COMET_LIB);
                    try {
                        lmgr.removeLibrary(cometLib);
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        // Already removed somehow, ignore.
                    }
                    cometLib = null;
                    break;
                }
            }
        }
        
        if(cometLib == null) {
            try {
                // classpath, src,  -- library volumes
                List<URL> libraryList = new ArrayList<URL>();
                File f = ServerUtilities.getJarName(installRoot, GRIZZLY_OPTIONAL_JAR_LIB_PREFIX);
                if ((f != null) && (f.exists())) {
                    libraryList.add(f.toURI().toURL());
                } else {
                    f = ServerUtilities.getJarName(installRoot, COMET_JAR_LIB_PREFIX);
                    if ((f != null) && (f.exists())) {
                        libraryList.add(f.toURI().toURL());
                    }
                }

//                File j2eeDoc = InstalledFileLocator.getDefault().locate(
//                        "docs/" + PERSISTENCE_JAVADOC, null, false); // NOI18N
//                List<URL> docList = new ArrayList<URL>();
//                docList.add(j2eeDoc.toURI().toURL());

                Map<String, List<URL>> contents = new HashMap<String, List<URL>>();
                contents.put(CLASSPATH_VOLUME, libraryList);
//                contents.put(JAVADOC_VOLUME, docList);
                
                cometLib = lmgr.createLibrary(CLASS_LIBRARY_TYPE, COMET_LIB, contents);
                Logger.getLogger("glassfish-javaee").log(Level.FINE, "Created library " + COMET_LIB);
            } catch (IOException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch (IllegalArgumentException ex) {
                // Someone must have created the library in a parallel thread, try again otherwise fail.
                cometLib = lmgr.getLibrary(COMET_LIB);
                if(cometLib == null) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
        }
        return cometLib != null;
    }

}
