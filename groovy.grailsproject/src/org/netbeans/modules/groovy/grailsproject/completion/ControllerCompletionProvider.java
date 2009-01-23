/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.groovy.editor.spi.completion.DynamicCompletionProvider.class)
public class ControllerCompletionProvider extends DynamicCompletionProvider {

    private static final Map<MethodSignature, String> METHODS = new HashMap<MethodSignature, String>();

    private static final Map<FieldSignature, String> FIELDS = new HashMap<FieldSignature, String>();

    // FIXME move it to some resource file, check the grails version
    static {
        String[] noParams = new String[] {};

        METHODS.put(new MethodSignature("redirect", new String[] {"java.util.Map"}), "void"); // NOI18N

        METHODS.put(new MethodSignature("chain", new String[] {"java.util.Map"}), "void"); // NOI18N

        METHODS.put(new MethodSignature("render", new String[] {"java.lang.Object"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("render", new String[] {"java.lang.String"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("render", new String[] {"java.util.Map"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("render", new String[] {"groovy.lang.Closure"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("render", new String[] {"java.util.Map", "groovy.lang.Closure"}), "void"); // NOI18N

        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object", "java.util.List"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object", "java.util.List", "java.lang.String"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map", "java.lang.String"}), "void"); // NOI18N
        METHODS.put(new MethodSignature("bindData", new String[] {"java.lang.Object", "java.lang.Object", "java.lang.String"}), "void"); // NOI18N

        METHODS.put(new MethodSignature("withFormat", new String[] {"groovy.lang.Closure"}), "void"); // NOI18N

        FIELDS.put(new FieldSignature("actionName"), "java.lang.String"); // NOI18N
        METHODS.put(new MethodSignature("getActionName", noParams), "java.lang.String"); // NOI18N

        FIELDS.put(new FieldSignature("controllerName"), "java.lang.String"); // NOI18N
        METHODS.put(new MethodSignature("getControllerName", noParams), "java.lang.String"); // NOI18N

        FIELDS.put(new FieldSignature("flash"), "java.util.Map"); // NOI18N
        METHODS.put(new MethodSignature("getFlash", noParams), "java.util.Map"); // NOI18N

        FIELDS.put(new FieldSignature("grailsApplication"), "org.codehaus.groovy.grails.commons.GrailsApplication"); // NOI18N
        METHODS.put(new MethodSignature("getGrailsApplication", noParams), "org.codehaus.groovy.grails.commons.GrailsApplication"); // NOI18N

        FIELDS.put(new FieldSignature("params"), "java.util.Map"); // NOI18N
        METHODS.put(new MethodSignature("getParams", noParams), "java.util.Map"); // NOI18N

        FIELDS.put(new FieldSignature("request"), "javax.servlet.http.HttpServletRequest"); // NOI18N
        METHODS.put(new MethodSignature("getRequest", noParams), "javax.servlet.http.HttpServletRequest"); // NOI18N

        FIELDS.put(new FieldSignature("response"), "javax.servlet.http.HttpServletResponse"); // NOI18N
        METHODS.put(new MethodSignature("getResponse", noParams), "javax.servlet.http.HttpServletResponse"); // NOI18N

        FIELDS.put(new FieldSignature("servletContext"), "javax.servlet.ServletContext"); // NOI18N
        METHODS.put(new MethodSignature("getServletContext", noParams), "javax.servlet.ServletContext"); // NOI18N

        FIELDS.put(new FieldSignature("session"), "javax.servlet.HttpSession"); // NOI18N
        METHODS.put(new MethodSignature("getSession", noParams), "javax.servlet.HttpSession"); // NOI18N
    }

    @Override
    public Map<FieldSignature, String> getFields(DynamicCompletionContext context) {
        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (context.getClassName().equals(context.getSourceClassName()) && project != null
                && project.getLookup().lookup(ControllerCompletionProvider.class) != null) {

            if (isController(context.getSourceFile(), project)) {
                return Collections.unmodifiableMap(FIELDS);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, String> getMethods(DynamicCompletionContext context) {
        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (context.getClassName().equals(context.getSourceClassName()) && project != null
                && project.getLookup().lookup(ControllerCompletionProvider.class) != null) {

            if (isController(context.getSourceFile(), project)) {
                return Collections.unmodifiableMap(METHODS);
            }
        }
        return Collections.emptyMap();
    }

    private boolean isController(FileObject source, Project project) {
        return source.getName().endsWith("Controller") // NOI18N
                    && source.getParent().getName().equals("controllers") // NOI18N
                    && source.getParent().getParent().getName().equals("grails-app") // NOI18N
                    && source.getParent().getParent().getParent().equals(project.getProjectDirectory());
    }

}
