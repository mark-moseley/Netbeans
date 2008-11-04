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

package org.netbeans.modules.websvc.core;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek, Milan Kuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation.class)
public class WSInjectiontargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    /** Creates a new instance of WSInjectiontargetQueryImplementation */
    public WSInjectiontargetQueryImplementation() {
    }
    
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to WSInjectionTargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        FileObject fo = controller.getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        
        if (Util.isJavaEE5orHigher(project) && !isTomcatTargetServer(project) && !(ElementKind.INTERFACE==typeElement.getKind())) {
            
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            boolean found = false;

            for (AnnotationMirror m : annotations) {
                Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                if (qualifiedName.contentEquals("javax.jws.WebService")) { //NOI18N
                    found = true;
                    break;
                }
                if (qualifiedName.contentEquals("javax.jws.WebServiceProvider")) { //NOI18N
                    found = true;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
    private boolean isTomcatTargetServer(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            if (j2eeModuleProvider.getServerID().startsWith("Tomcat")) return true; //NOI18N
        }
        return false;
    }
}
