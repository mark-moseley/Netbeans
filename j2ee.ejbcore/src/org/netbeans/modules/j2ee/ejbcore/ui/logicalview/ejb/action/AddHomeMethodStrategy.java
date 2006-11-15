/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Action that can always be invoked and work procedurally.
 * @author cwebster
 */
public class AddHomeMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddHomeMethodStrategy(String name) {
        super (name);
    }
    public AddHomeMethodStrategy () {
        super(NbBundle.getMessage(AddHomeMethodStrategy.class, "LBL_AddHomeMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(FileObject fileObject, ElementHandle<TypeElement> classHandle) throws IOException {
        final MethodType[] result = new MethodType[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = AbstractMethodController.createMethod(workingCopy, "homeMethod");
                ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(method);
                result[0] = new MethodType.HomeMethodType(methodHandle);
            }
        });
        return result[0];
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodType pType) throws IOException{
        final MethodCustomizer[] result = new MethodCustomizer[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                MethodsNode methodsNode = getMethodsNode();
                TypeElement clazz = SourceUtils.newInstance(workingCopy).getTypeElement();
                EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, clazz);
                boolean local = methodsNode == null ? ejbMethodController.hasLocal() : (methodsNode.isLocal() && ejbMethodController.hasLocal());
                boolean remote = methodsNode == null ? ejbMethodController.hasRemote() : (!methodsNode.isLocal() && ejbMethodController.hasRemote());
                result[0] = MethodCollectorFactory.homeCollector(pType.getMethodElement(), ejbMethodController.hasRemote(), ejbMethodController.hasLocal(), remote, local);
            }
        });
        return result[0];
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_HOME;
    }
}
