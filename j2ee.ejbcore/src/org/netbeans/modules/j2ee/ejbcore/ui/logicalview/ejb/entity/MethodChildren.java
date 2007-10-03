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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodModel;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.IconVisitor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */

public class MethodChildren extends ComponentMethodModel {

    private ComponentMethodViewStrategy mvs;
    private final EntityMethodController controller;
    private final boolean local;
    private final FileObject ddFile;
    private final Entity entity;
    
    public MethodChildren(JavaSource javaSource, EntityMethodController smc, Entity model, Collection interfaces, boolean local, FileObject ddFile) {
        super(javaSource, smc.getBeanClass(), interfaces, local ? smc.getLocalHome() : smc.getHome());
        controller = smc;
        this.local = local;
        this.ddFile = ddFile;
        this.entity = model;
        mvs = new EntityStrategy();
    }

    protected Collection<String> getInterfaces() {
        if (local) {
            return controller.getLocalInterfaces();
        } else {
            return controller.getRemoteInterfaces();
        }
    }

    public ComponentMethodViewStrategy createViewStrategy() {
        return mvs;
    }

    private class EntityStrategy implements ComponentMethodViewStrategy {
        
        public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO, Collection interfaces) throws IOException {
            String methodName = me.getName();
            if (methodName.startsWith("find") ||     //NOI18N
                methodName.startsWith("ejbSelect")) {   //NOI18N
                controller.deleteQueryMapping(me, ddFile);
            }
            controller.delete(me,local);
        }

        public Image getBadge(MethodModel me, Collection interfaces) {
            return null;
        }

        public Image getIcon(MethodModel me, Collection interfaces) {
            IconVisitor iv = new IconVisitor();
            return Utilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        public void openMethod(final MethodModel me, final String implClass, FileObject implClassFO, Collection interfaces) {
            if (controller.getMethodTypeFromInterface(me).getKind() == MethodType.Kind.FINDER) {
                try {
                    DataObject ddFileDO = DataObject.find(ddFile);
                    Object c = ddFileDO.getCookie(DDEditorNavigator.class);
                    if (c != null) {
                        Query[] queries = entity.getQuery();
                        for (int i = 0; i < queries.length; i++) {
                            String methodName = queries[i].getQueryMethod().getMethodName();
                            if (methodName.equals(me.getName())) {
                                ((DDEditorNavigator) c).showElement(queries[i]);
                            }
                        }
                    }
                } catch (DataObjectNotFoundException donf) {
                    Exceptions.printStackTrace(donf);
                }
            }
            final List<ElementHandle<ExecutableElement>> methodHandle = new ArrayList<ElementHandle<ExecutableElement>>();
            try {
                JavaSource javaSource = JavaSource.forFileObject(implClassFO);
                javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = controller.getElements().getTypeElement(implClass);
                        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                            if (MethodModelSupport.isSameMethod(controller, executableElement, me)) {
                                methodHandle.add(ElementHandle.create(executableElement));
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            if (methodHandle.size() > 0) {
                ElementOpen.open(implClassFO, methodHandle.get(0));
            }
        }

    }

}
