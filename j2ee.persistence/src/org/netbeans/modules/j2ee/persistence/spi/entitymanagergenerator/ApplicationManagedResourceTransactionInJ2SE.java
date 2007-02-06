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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import org.netbeans.modules.j2ee.persistence.action.*;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

/**
 * Generates the code needed for invoking an <code>EntityManager</code> in Java SE 
 * enviroment with an application-managed persistence unit.
 * 
 * @author Erno Mononen
 */
public final class ApplicationManagedResourceTransactionInJ2SE extends EntityManagerGenerationStrategySupport{
    
    public ClassTree generate() {
        
        ModifiersTree methodModifiers = getTreeMaker().Modifiers(
                Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList()
                );
        
        String text = 
                "javax.persistence.EntityManagerFactory emf = javax.persistence.Persistence.createEntityManagerFactory(\"" + getPersistenceUnitName() + "\");\n" +
                "javax.persistence.EntityManager em = emf.createEntityManager();\n" +
                "em.getTransaction().begin();\n" +
                "try {\n" +
                generateCallLines()     +
                "    em.getTransaction().commit();\n" +
                "} catch (Exception e) {\n" +
                "    e.printStackTrace();\n" +
                "    em.getTransaction().rollback();\n" +
                "} finally {\n" +
                "    em.close();\n" +
                "}";
        
        MethodTree newMethod = getTreeMaker().Method(
                methodModifiers, 
                computeMethodName(),
                getTreeMaker().PrimitiveType(TypeKind.VOID), 
                Collections.<TypeParameterTree>emptyList(), 
                getParameterList(),
                Collections.<ExpressionTree>emptyList(), 
                "{ " + text + "}", 
                null
                );
        
        return getTreeMaker().addClassMember(getClassTree(), newMethod);
    }
    
}
