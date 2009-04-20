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

package org.netbeans.modules.websvc.jaxrpc.wsstack.glassfish;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxrpc.JaxRpc;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/**
 *
 * @author mkuchtiak
 */
public class GlassFishV3JaxRpcStack implements WSStackImplementation<JaxRpc> {
    private static final String[] METRO_LIBRARIES =
        new String[] {"webservices", "javax.activation"}; //NOI18N
    private static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N
    
    private String gfRootStr;
    private JaxRpc jaxRpc;
    
    public GlassFishV3JaxRpcStack(String gfRootStr) {
        this.gfRootStr = gfRootStr;
        jaxRpc = new JaxRpc();
    }

    public JaxRpc get() {
        return jaxRpc;
    }
    
    public WSStackVersion getVersion() {
        return WSStackVersion.valueOf(1, 1, 3, 0);
    }

    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRpc.Tool.WCOMPILE && isMetroInstalled()) {
            return WSStackFactory.createWSTool(new JaxRpcTool(JaxRpc.Tool.WCOMPILE));
        } else {
            return null;
        }
    }
    
    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxRpc.Feature.JSR109_OLD && isMetroInstalled()) {
            return true;
        }
        return false;   
    }
    
    protected class JaxRpcTool implements WSToolImplementation {
        JaxRpc.Tool tool;
        JaxRpcTool(JaxRpc.Tool tool) {
            this.tool = tool;
        }

        public String getName() {
            return tool.getName();
        }

        public URL[] getLibraries() {
            List<URL> cPath = new ArrayList<URL>();
            if (isMetroInstalled()) {
                for (String entry : METRO_LIBRARIES) {
                    File f = getJarName(gfRootStr, entry);
                    if ((f != null) && (f.exists())) {
                        try {
                            cPath.add(f.toURI().toURL());
                        } catch (MalformedURLException ex) {

                        }
                    }
                }
            }
            return cPath.toArray(new URL[cPath.size()]);
        }
      
    }
    
    protected boolean isMetroInstalled() {
        File f = getJarName(gfRootStr, METRO_LIBRARIES[0]);
        return f!=null && f.exists();
    }
    
    protected static class VersionFilter implements FileFilter {
       
        private String nameprefix;
        
        public VersionFilter(String nameprefix) {
            this.nameprefix = nameprefix;
        }
        
        public boolean accept(File file) {
            return file.getName().startsWith(nameprefix);
        }
        
    }
    
    protected File getJarName(String glassfishInstallRoot, String jarNamePrefix) {
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

}
