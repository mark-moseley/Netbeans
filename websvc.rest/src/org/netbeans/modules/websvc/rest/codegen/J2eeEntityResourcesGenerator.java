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

package org.netbeans.modules.websvc.rest.codegen;


import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.support.WebXmlHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author PeterLiu
 */
public class J2eeEntityResourcesGenerator extends EntityResourcesGenerator {
     /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public J2eeEntityResourcesGenerator(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName, String persistenceUnitName) {
        super(model, project, targetFolder, targetPackageName, null, null, persistenceUnitName);
    }

    public J2eeEntityResourcesGenerator(EntityResourceBeanModel model,
            String resourcePackage, String converterPackage) {
        super(model, null, null, null, resourcePackage, converterPackage, null);
    }
    
     /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public J2eeEntityResourcesGenerator(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName,
            String resourcePackage, String converterPackage,
            String persistenceUnitName) {
        super(model, project, targetFolder, targetPackageName, resourcePackage, converterPackage, persistenceUnitName);
        
        injectEntityManager = false;
        addSingletonAnnotation = false;
    }

    protected void configurePersistence() {
        // Add <persistence-context-ref> to web.xml
        WebXmlHelper.addPersistenceContextRef(project, persistenceUnitName);
    }
    
   
}
