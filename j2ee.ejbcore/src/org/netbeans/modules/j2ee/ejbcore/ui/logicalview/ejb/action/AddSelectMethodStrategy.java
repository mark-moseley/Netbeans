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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.action.SelectMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodStrategy.class, "LBL_AddSelectMethodAction"));
    }
    
    public AddSelectMethodStrategy(String name) {
        super(name);
    }
    
    @Override
    public MethodModel getPrototypeMethod() {
        Set<Modifier> modifiers = new HashSet<Modifier>();
        modifiers.add(Modifier.PUBLIC);
        modifiers.add(Modifier.ABSTRACT);
        return MethodModel.create(
                "ejbSelectBy",
                "int",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList("javax.ejb.FinderException"),
                modifiers
                );
    }
    
    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException {
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        return MethodCustomizerFactory.selectMethod(
                getTitle(),
                methodModel,
                ClasspathInfo.create(fileObject),
                ejbMethodController.createDefaultQL(methodModel),
                _RetoucheUtil.getMethods(fileObject, className)
                );
    }
    
//    @SuppressWarnings("deprecation") //NOI18N
//    protected void okButtonPressed(final MethodCustomizer methodCustomizer, final MethodType methodType, 
//            final FileObject fileObject, String classHandle) throws java.io.IOException {
//        ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
//        try {
//            handle.start(100);
//            String className = _RetoucheUtil.getMainClassName(fileObject);
//            EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
//            MethodModel method = methodType.getMethodElement();
//            EntityMethodController entityMethodController = (EntityMethodController) ejbMethodController;
//            entityMethodController.addSelectMethod(method, methodCustomizer.getEjbQL(), getEjbModule(fileObject).getDeploymentDescriptor());
//            handle.progress(99);
//        } finally {
//            handle.finish();
//        }
//    }
    
    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.SELECT;
    }

    protected void generateMethod(MethodModel method, boolean isOneReturn,
                                  boolean publishToLocal,
                                  boolean publishToRemote, String ejbql,
                                  FileObject ejbClassFO, String ejbClass) throws IOException {
        SelectMethodGenerator generator = SelectMethodGenerator.create(ejbClass, ejbClassFO);
        generator.generate(method, publishToLocal, publishToRemote, isOneReturn, ejbql);
    }

    public boolean supportsEjb(FileObject fileObject,final String className) {

        boolean isCMP = false;
        
        EjbJar ejbModule = getEjbModule(fileObject);
        if (ejbModule != null) {
            MetadataModel<EjbJarMetadata> metadataModel = ejbModule.getMetadataModel();
            try {
                isCMP = metadataModel.runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                    public Boolean run(EjbJarMetadata metadata) {
                        Ejb ejb = metadata.findByEjbClass(className);
                        if (ejb instanceof Entity) {
                            Entity entity = (Entity) ejb;
                            return Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType());
                        }
                        return false;
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        return isCMP;

    }
}
