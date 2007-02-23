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

package org.netbeans.modules.j2ee.ejbcore.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class BusinessMethodGenerator extends AbstractMethodGenerator {
    
    private BusinessMethodGenerator(EntityAndSession ejb, FileObject ejbClassFileObject) {
        super(ejb, ejbClassFileObject, null);
    }
    
    public static BusinessMethodGenerator create(EntityAndSession ejb, FileObject ejbClassFileObject) {
        return new BusinessMethodGenerator(ejb, ejbClassFileObject);
    }
    
    public void generate(MethodModel methodModel, boolean generateLocal, boolean generateRemote) throws IOException {
        
        // local interface
        if (generateLocal && ejb.getLocal() != null) {
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    methodModel.getExceptions(),
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, ejb.getLocal());
        }
        
        // remote interface, add RemoteException if it's not there
        if (generateRemote && ejb.getRemote() != null) {
            List<String> exceptions = new ArrayList<String>(methodModel.getExceptions());
            if (!methodModel.getExceptions().contains("java.rmi.RemoteException")) {
                exceptions.add("java.rmi.RemoteException");
            }
            MethodModel methodModelCopy = MethodModel.create(
                    methodModel.getName(),
                    methodModel.getReturnType(),
                    null,
                    methodModel.getParameters(),
                    exceptions,
                    methodModel.getModifiers()
                    );
            addMethodToInterface(methodModelCopy, ejb.getRemote());
        }
        
        // ejb class, add 'public' modifier
        MethodModel methodModelCopy = MethodModel.create(
                methodModel.getName(),
                methodModel.getReturnType(),
                methodModel.getBody(),
                methodModel.getParameters(),
                methodModel.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
        addMethod(methodModelCopy, ejbClassFileObject, ejb.getEjbClass());
    }
    
}
