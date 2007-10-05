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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.editor.hints.rules;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.modules.websvc.editor.hints.fixes.RemoveAnnotationArgument;

/**
 *
 * @author Ajit.Bhate@Sun.com
 */
public class InvalidExcludeAttribute extends Rule<ExecutableElement> implements WebServiceAnnotations {

    public InvalidExcludeAttribute() {
    }

    protected ErrorDescription[] apply(ExecutableElement subject, ProblemContext ctx) {
        AnnotationMirror methodAnn = Utilities.findAnnotation(subject, ANNOTATION_WEBMETHOD);
        AnnotationValue val = Utilities.getAnnotationAttrValue
                (methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
        if (val != null && val.getValue() == Boolean.TRUE) {
            String label = NbBundle.getMessage(InvalidExcludeAttribute.class, 
                    "MSG_WebMethod_ExcludeNotAllowed");
            Fix fix = new RemoveAnnotationArgument(ctx.getFileObject(), 
                    subject, methodAnn, ANNOTATION_ATTRIBUTE_EXCLUDE);
            AnnotationTree annotationTree = (AnnotationTree) ctx.getCompilationInfo().
                        getTrees().getTree(subject, methodAnn);
            Tree problemTree = Utilities.getAnnotationArgumentTree(annotationTree,
                    ANNOTATION_ATTRIBUTE_EXCLUDE);
            ctx.setElementToAnnotate(problemTree);
            ErrorDescription problem = createProblem(subject, ctx, label, fix);
            ctx.setElementToAnnotate(null);
            return new ErrorDescription[]{problem};
        }
        return null;
    }

    protected boolean isApplicable(ExecutableElement subject, ProblemContext ctx) {
        return Utilities.hasAnnotation(subject, ANNOTATION_WEBMETHOD);
    }
}
