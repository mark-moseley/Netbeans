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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Adamek
 */
public abstract class AbstractMethodGenerator {
    
    protected final String ejbClass;
    protected final FileObject ejbClassFileObject;
    protected final org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    
    protected AbstractMethodGenerator(String ejbClass, FileObject ejbClassFileObject) {
        Parameters.notNull("ejbClass", ejbClass);
        Parameters.notNull("ejbClassFileObject", ejbClassFileObject);
        this.ejbClass = ejbClass;
        this.ejbClassFileObject = ejbClassFileObject;
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ejbClassFileObject);
    }
    
    /**
     * Founds business interface if exists and adds method there, adds method into interface <code>className</code> otherwise
     */
    protected void addMethodToInterface(MethodModel methodModel, String className) throws IOException {
        String commonInterface = findCommonInterface(ejbClass, className);
        if (commonInterface == null) { // there is no 'business' interface
            commonInterface = className;
        }
        FileObject fileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, commonInterface);
        addMethod(methodModel, fileObject, commonInterface);
    }
    
    /**
     * Adds method to class
     */
    protected static void addMethod(final MethodModel methodModel, FileObject fileObject, final String className) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(className);
                boolean generateDefaultBody = (typeElement.getKind() != ElementKind.INTERFACE) && !methodModel.getModifiers().contains(Modifier.ABSTRACT);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel, generateDefaultBody);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
    }
    
    protected void saveXml() throws IOException {
        FileObject ddFileObject = ejbModule.getDeploymentDescriptor();
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ddFileObject); // EJB 2.1
        if (ejbJar != null) {
            ejbJar.write(ddFileObject);
        }
    }
    
    /**
     * Returns map of EJB interface class names, where keys are appropriate constants from {@link EntityAndSession}
     */
    protected Map<String, String> getInterfaces() throws IOException {
        return ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {
            public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                Map<String, String> result = new HashMap<String, String>();
                result.put(EntityAndSession.LOCAL, ejb.getLocal());
                result.put(EntityAndSession.LOCAL_HOME, ejb.getLocalHome());
                result.put(EntityAndSession.REMOTE, ejb.getRemote());
                result.put(EntityAndSession.HOME, ejb.getHome());
                return result;
            }
        });
    }
    
    private static String findCommonInterface(final String className1, final String className2) throws IOException {
        //TODO: RETOUCHE
        return null;
    }
    
}
