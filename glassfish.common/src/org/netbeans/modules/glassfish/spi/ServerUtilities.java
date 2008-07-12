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

package org.netbeans.modules.glassfish.spi;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
import org.netbeans.modules.glassfish.common.wizards.ServerWizardIterator;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;


/**
 * General helper methods for accessing GlassFish server objects.
 *
 * @author Peter Williams
 * @author Ludovic Champenois
 */
public final class ServerUtilities {

    public static final int ACTION_TIMEOUT = 10000;
    public static final TimeUnit ACTION_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    public static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N
    public static final String GFV3_PREFIX_JAR_NAME = "glassfish-10.0"; // NOI18N"
    
    
    private ServerUtilities() {
    }
    
    /**
     * Returns the ServerInstance object for the server with the specified URI.
     * 
     * @param uri uri identifying the server instance.
     * 
     * @return ServerInstance object for this server instance.
     */
    public static ServerInstance getServerInstance(String uri) {
        return GlassfishInstanceProvider.getDefault().getInstance(uri);
    }

    /**
     * Returns the lookup object for a server instance when the caller only has
     * the public handle available via common server API.
     *
     * @param ServerInstance object for this server instance.
     *
     * @return Lookup object maintained by backing instance implementation
     */
    public static Lookup getLookupFor(ServerInstance instance) {
        return GlassfishInstanceProvider.getDefault().getLookupFor(instance);
    }

    /**
     * Returns the facade implementation for the specified server, if that server
     * supports the facade class passed in.
     * 
     * @param uri uri identifying the server instance.
     * @param serverFacadeClass class definition of the server facade we're
     *   looking for.
     * 
     * @return facade implementation for specified server or null if either the
     *   server does not exist or the server does not implement this facade.
     */
    public static <T> T getInstanceByCapability(String uri, Class <T> serverFacadeClass) {
        return GlassfishInstanceProvider.getDefault().
                getInstanceByCapability(uri, serverFacadeClass);
    }
    
    /**
     * Returns a list of the server instances that support the facade class 
     * specified (e.g. all servers that support <code>RubyInstance</code>).
     * 
     * @param serverFacadeClass class definition of the server facade we're
     *   looking for.
     * 
     * @return list of servers that support the interface specified or an empty
     *   list if no servers support that interface.
     */
    public static <T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        return GlassfishInstanceProvider.getDefault().
                getInstancesByCapability(serverFacadeClass);
    }
    
    /**
     * Returns the ServerInstanceImplementation instance for the server with the
     * specified URI.  Use when you need to avoid calling through the ServerInstance
     * facade wrapper.  Otherwise, you should call <code>getServerInstance()</code> instead.
     * 
     * @param uri uri identifying the server instance.
     * 
     * @return ServerInstanceImplementation object for this server instance.
     */
    public static ServerInstanceImplementation getInternalServerInstance(String uri) {
        return GlassfishInstanceProvider.getDefault().getInternalInstance(uri);
    }
    
    /**
     * Returns an instance of the AddServerWizard for this server plugin.
     * 
     * @return instance of the AddServerWizard for this server.
     */
    public static InstantiatingIterator getAddInstanceIterator() {
        return new ServerWizardIterator();
    }
    
    /**
     * Returns the ServerInstanceProvider for this server plugin so we don't 
     * have to look it up via common server SPI.
     * 
     * @return the GlassFish V3 impl for ServerInstanceProvider.
     */
    public static ServerInstanceProvider getServerProvider() {
        return GlassfishInstanceProvider.getDefault();
    }
    
     /**
     * Returns the fqn jar name with the correct version 
     * If jarNamePrefix is ""glassfish-10.0" the the return value
     * will be INSTALL/modules/glassfish-10.0-SNAPSHOT.jar"
     * 
     * @return the File with full path of the jar or null
     */
   public static File getJarName(String glassfishInstallRoot, String jarNamePrefix) {
        File modulesDir = new File(glassfishInstallRoot + File.separatorChar + GFV3_MODULES_DIR_NAME);
        int subindex = jarNamePrefix.lastIndexOf("/");
        if(subindex != -1) {
            String subdir = jarNamePrefix.substring(0, subindex);
            jarNamePrefix = jarNamePrefix.substring(subindex+1);
            modulesDir = new File(modulesDir, subdir);
        }
        File candidates[] = modulesDir.listFiles(new VersionFilter(jarNamePrefix));
        
        if(candidates != null && candidates.length > 0) {
            return candidates[0]; // the first one
        } else {
            return null;
        }
    }
   
    private static class VersionFilter implements FileFilter {
       
        private String nameprefix;
        
        public VersionFilter(String nameprefix) {
            this.nameprefix = nameprefix;
        }
        
        public boolean accept(File file) {
            return file.getName().startsWith(nameprefix);
        }
        
    }
    
    /**
     * Get the url for a file, including proper protocol for archive files (jars).
     * 
     * @param file File to create URL from.
     * 
     * @return url URL for file with proper protocol specifier.
     * 
     * @throws java.net.MalformedURLException
     */
    public static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }    
   
}
