/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 * This class represents javax.servlet.annotation.WebServlet annotation
 * @author Petr Slechta
 */
public class WebServlet extends PersistentObject implements Refreshable {
    private String name;
    private List<String> urlPatterns = new ArrayList<String>();
    private String clazz;

    public WebServlet(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    public boolean refresh(TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> annByType = getHelper().getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror annotationMirror = annByType.get("javax.servlet.annotation.WebServlet"); // NOI18N
        if (annotationMirror == null) {
            return false;
        }

        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("name", AnnotationParser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        parser.expectStringArray("urlPatterns", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String)arrayMember.getValue();
                    urlPatterns.add(value);
                }
                return null;
            }
        }, null);
        parser.expectStringArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String)arrayMember.getValue();
                    urlPatterns.add(value);
                }
                return null;
            }
        }, null);
        ParseResult parseResult = parser.parse(annotationMirror);
        name = parseResult.get("name", String.class); // NOI18N
        clazz = typeElement.getQualifiedName().toString();
        return true;
    }

    public String getName() {
        return name;
    }

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    public String getServletClass() {
        return clazz;
    }

}
