/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.products.nb.javaee;

import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    private static final String VISUALWEB_CLUSTER =
            "{visualweb-cluster}"; // NOI18N
    private static final String IDENTITY_CLUSTER =
            "{identity-cluster}"; // NOI18N
    private static final String XML_CLUSTER =
            "{xml-cluster}"; // NOI18N
    private static final String ID =
            "WEBEE"; // NOI18N
    private static final String MOBILITY_END_2_END_KIT =
            "org-netbeans-modules-mobility-end2end-kit";
    
    private static final String NB_JAVAME_UID = "nb-javame";
    
    private static final String MOBILITY_CLUSTER =
            "{mobility-cluster}";
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
                    ENTERPRISE_CLUSTER,
                    VISUALWEB_CLUSTER,
                    IDENTITY_CLUSTER,
            XML_CLUSTER}, ID);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
        final Product nbProduct =
                Registry.getInstance().getProducts(dependencies.get(0)).get(0);

        for (Product product : Registry.getInstance().getInavoidableDependents(nbProduct)) {
            if (product.getUid().equals(NB_JAVAME_UID) && product.getStatus().equals(Status.INSTALLED)) {
                //mobility installed, enable end2end kit                
                NetBeansUtils.setModuleStatus(product.getInstallationLocation(),
                        MOBILITY_CLUSTER,
                        MOBILITY_END_2_END_KIT,
                        true);
                break;
            }
        }
    }
}
