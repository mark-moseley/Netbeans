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
package org.netbeans.modules.php.project;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.php.rt.providers.impl.absent.AbsentServerProvider;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpProjectUtils;
import org.netbeans.modules.php.rt.utils.ServersUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * @author ads
 *
 */
public final class Utils {

    // avoid instantiation
    private Utils() {
    }

    public static SourceGroup[] getSourceGroups(Project phpProject) {
        return PhpProjectUtils.getSourceGroups(phpProject);
    }
    public static FileObject[] getSourceObjects(Project phpProject) {
        return PhpProjectUtils.getSourceObjects(phpProject);
    }

    public static WebServerProvider getProvider(PhpProject project) {
        PropertyEvaluator evaluator = project.getEvaluator();
        String url = evaluator.getProperty(PhpProject.URL);
        String domain = null;
        String baseDir = null;      
        String port = null;        
        if (url != null) {
            try {
                URL u = new URL(url);
                domain = u.getHost();
                baseDir = u.getPath();
                int portNumber = u.getPort();
                port = (portNumber != -1) ? String.valueOf(portNumber) : null;
            } catch(MalformedURLException mex) {
                Exceptions.printStackTrace(mex);
            }
        }
        String docRoot = evaluator.getProperty(PhpProject.COPY_SRC_TARGET);        
        return (domain != null && baseDir != null ) ? WebServerProvider.ServerFactory.getDefaultProvider(domain, baseDir, port, docRoot) : getOriginalProvider(project);
    }

    public static WebServerProvider getOriginalProvider(PhpProject project) {
         String provider = project.getEvaluator().getProperty(PhpProject.PROVIDER_ID);
        if (provider == null) {
            // TODO realize fake provider that will return commands but will 
            // suggest to setup real server
            return new AbsentServerProvider();
            //return null;
        }
        WebServerProvider[] providers = WebServerProvider.ServerFactory.getProviders();
        for (WebServerProvider prov : providers) {
            if (prov.getClass().getCanonicalName().equals(provider)) {
                return prov;
            }
        }
        return null;
    }
    
    
    public static Host findHostById(String hostId) {
        return ServersUtils.findHostById(hostId);
    }
}