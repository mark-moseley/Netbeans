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

package org.netbeans.modules.websvc.jaxrpc.actions;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.websvc.core.InvokeOperationActionProvider;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.dev.wizard.ProjectInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;

public class JaxRpcInvokeOperationProvider implements InvokeOperationActionProvider {
    
    
    public InvokeOperationCookie getInvokeOperationCookie(FileObject targetSource) {
        Project project = FileOwnerQuery.getOwner(targetSource);
        if(supportsJaxrpcOnly(project, targetSource)){
            return new JaxRpcInvokeOperation(project);
        }
        
        return null;
    }
    
    private boolean supportsJaxrpcOnly(Project project, FileObject targetSource){
        ProjectInfo projectInfo = new ProjectInfo(project);
        int projectType = projectInfo.getProjectType();
        if(projectType == ProjectInfo.JSE_PROJECT_TYPE && isJaxWsLibraryOnClasspath(targetSource)) return false;
        if(projectInfo.isJwsdpSupported())return false;
        if(Util.isJavaEE5orHigher(project)) return false;
        if (JaxWsUtils.isEjbJavaEE5orHigher(projectInfo)) return false;
        if (projectType == ProjectInfo.WEB_PROJECT_TYPE && !Util.isJavaEE5orHigher(project) && isJaxWsLibraryOnRuntimeClasspath(targetSource))return false;
        return true;
    }
    
    private boolean isJaxWsLibraryOnRuntimeClasspath(FileObject targetSource){
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.EXECUTE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null  &&
                    classPath.findResource("javax/xml/rpc/Service.class") == null){
                return true;
            }
        }
        return false;
    }
    
    private boolean isJaxWsLibraryOnClasspath(FileObject targetSource) {
        //test on javax.xml.ws.Service.class
        // checking COMPILE classpath
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.COMPILE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        //checking BOOT classpath
        classPath = ClassPath.getClassPath(targetSource,ClassPath.BOOT);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        return false;
    }
    
}
