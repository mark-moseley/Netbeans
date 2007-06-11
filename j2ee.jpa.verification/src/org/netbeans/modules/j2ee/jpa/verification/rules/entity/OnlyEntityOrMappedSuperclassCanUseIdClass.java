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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.Arrays;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * 
 * @author Tomasz.Slota@Sun.COM
 */
public class OnlyEntityOrMappedSuperclassCanUseIdClass extends JPAClassRule {
    
    /** Creates a new instance of NonFinalClass */
    public OnlyEntityOrMappedSuperclassCanUseIdClass() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        
        if (Utilities.hasAnnotation(subject, JPAAnnotations.ID_CLASS)){
            return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(QueriesProperlyDefined.class, "MSG_OnlyEntityOrMappedSuperclassCanUseIdClass"))};
        }
        
        return null;
    }
    
    @Override protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext)ctx;
        
        return !(jpaCtx.isEntity() || jpaCtx.isMappedSuperClass());
    }
}
