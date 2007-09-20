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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.util;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * A helper class for generating equals, hashCode and toString methods for entity classes.
 */
//XXX all methods are extracted from JavaPersistenceGenerator, consider generalizing 
// them and putting to GenerationUtils.
public class EntityMethodGenerator {

    private final WorkingCopy copy;
    private final GenerationUtils genUtils;
    
    public EntityMethodGenerator(WorkingCopy copy, GenerationUtils genUtils) {
        this.copy = copy;
        this.genUtils = genUtils;
    }

    public MethodTree createHashCodeMethod(List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(20 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("int hash = 0;"); // NOI18N
        for (VariableTree field : fields) {
            body.append(createHashCodeLineForField(field));
        }
        body.append("return hash;"); // NOI18N
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "hashCode",
                make.PrimitiveType(TypeKind.INT), Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }

    private String createHashCodeLineForField(VariableTree field) {
        Name fieldName = field.getName();
        Tree fieldType = field.getType();
        if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
            if (((PrimitiveTypeTree) fieldType).getPrimitiveTypeKind() == TypeKind.BOOLEAN) {
                return "hash += (" + fieldName + " ? 1 : 0"; // NOI18N
            }
            return "hash += (int)" + fieldName + ";"; // NOI18N
        }
        return "hash += (" + fieldName + " != null ? " + fieldName + ".hashCode() : 0);"; // NOI18N
    }

    public  MethodTree createEqualsMethod(String simpleClassName, List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(50 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("// TODO: Warning - this method won't work in the case the id fields are not set\n"); // NOI18N
        body.append("if (!(object instanceof "); // NOI18N
        body.append(simpleClassName + ")) {return false;}"); // NOI18N
        body.append(simpleClassName + " other = (" + simpleClassName + ")object;"); // NOI18N
        for (VariableTree field : fields) {
            body.append(createEqualsLineForField(field));
        }
        body.append("return true;"); // NOI18N
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "equals",
                make.PrimitiveType(TypeKind.BOOLEAN), Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(genUtils.createVariable("object", "java.lang.Object")),
                Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }

    private String createEqualsLineForField(VariableTree field) {
        Name fieldName = field.getName();
        Tree fieldType = field.getType();
        if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
            return "if (this." + fieldName + " != other." + fieldName + ") return false;"; // NOI18N
        }
        return "if ((this." + fieldName + " == null && other." + fieldName + " != null) || " + "(this." + fieldName +
                " != null && !this." + fieldName + ".equals(other." + fieldName + ")) return false;"; // NOI18N
    }

    public MethodTree createToStringMethod(String fqn, List<VariableTree> fields) {
        StringBuilder body = new StringBuilder(30 + fields.size() * 30);
        body.append("{"); // NOI18N
        body.append("return \"" + fqn + "["); // NOI18N
        for (Iterator<VariableTree> i = fields.iterator(); i.hasNext();) {
            String fieldName = i.next().getName().toString();
            body.append(fieldName + "=\" + " + fieldName + " + \""); //NOI18N
            body.append(i.hasNext() ? ", " : "]\";"); //NOI18N
        }
        body.append("}"); // NOI18N
        TreeMaker make = copy.getTreeMaker();
        // XXX Javadoc
        return make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC),
                Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), "toString",
                genUtils.createType("java.lang.String"), Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body.toString(), null);
    }
}