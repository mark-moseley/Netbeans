/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.jaxws;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportFactory;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.netbeans.modules.websvc.project.spi.LookupMergerSupport;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Lookup Provider for WS Support in JavaEE project types
 *
 * @author mkuchtiak
 */

@LookupProvider.Registration(projectType="org-netbeans-modules-maven")
public class MavenJaxWsLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        JAXWSLightSupportImpl spiJAXWSSupport = new MavenJAXWSSupportImpl(prj);
        final JAXWSLightSupport jaxWsSupport = JAXWSLightSupportFactory.createJAXWSSupport(spiJAXWSSupport);

        JAXWSLightSupportProvider jaxWsSupportProvider = new MavenJaxWsSupportProvider(prj, jaxWsSupport);
        
        WebServiceDataProvider jaxWsServiceDataProvider = new MavenJaxWsServicesProvider(prj, jaxWsSupport);
        LookupMerger<WebServiceDataProvider> wsDataProviderMerger =
                LookupMergerSupport.createWebServiceDataProviderMerger();

        final FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {                    
                    WSUtils.detectWsdlClients(prj, jaxWsSupport, wsdlFolder);
                }

            });
            
        }

        return Lookups.fixed(jaxWsSupportProvider, jaxWsServiceDataProvider, wsDataProviderMerger);
    }
}
