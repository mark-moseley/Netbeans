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

package org.netbeans.modules.web.jsf;

import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */

public class JSFInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public JSFInjectionTargetQueryImplementation() {
    }
    
    /** 
     * For method return true if:
     *      1) The web module follows 2.5 servlet specification or higher
     *      2) The jc is defined as manage bean in a jsp configuration file.
     */
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        Parameters.notNull("controller", controller);
        Parameters.notNull("typeElement", typeElement);
        
        // Find the web module, where the class is
        WebModule webModule  = WebModule.getWebModule(controller.getFileObject());
        // Is the web modile 2.5 servlet spec or higher?
        if (webModule != null && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_13_LEVEL)
                && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_14_LEVEL)){
            // Get deployment desctriptor from the web module
            FileObject ddFileObject = webModule.getDeploymentDescriptor();
            if (ddFileObject != null){
                // Get all jsf configurations files
                FileObject[] jsfConfigs = ConfigurationUtils.getFacesConfigFiles(webModule);
                for (FileObject jsfConfigFO : jsfConfigs) {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(jsfConfigFO, true);
                    if (model != null) {
                        // Get manage beans from the configuration file
                        List<ManagedBean> beans = model.getRootComponent().getManagedBeans();
                        for (ManagedBean managedBean : beans) {
                            if (typeElement.getQualifiedName().contentEquals(managedBean.getManagedBeanClass())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
