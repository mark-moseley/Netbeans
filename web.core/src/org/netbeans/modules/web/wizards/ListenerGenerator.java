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
package org.netbeans.modules.web.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;

/**
 * Generator for servlet listener class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on March, 2004
 */
public class ListenerGenerator {

    boolean isContext;
    boolean isContextAttr;
    boolean isSession;
    boolean isSessionAttr;
    boolean isRequest;
    boolean isRequestAttr;

    private JavaSource clazz;
    private GenerationUtils gu;

    /** Creates a new instance of ListenerGenerator */
    public ListenerGenerator(boolean isContext, boolean isContextAttr, boolean isSession, boolean isSessionAttr, boolean isRequest, boolean isRequestAttr) {
        this.isContext = isContext;
        this.isContextAttr = isContextAttr;
        this.isSession = isSession;
        this.isSessionAttr = isSessionAttr;
        this.isRequest = isRequest;
        this.isRequestAttr = isRequestAttr;
    }

    public void generate(JavaSource clazz) throws IOException {
        this.clazz = clazz;

        AbstractTask task = new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();


                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        gu = GenerationUtils.newInstance(workingCopy);
                        Element e = workingCopy.getTrees().getElement(new TreePath(new TreePath(workingCopy.getCompilationUnit()), typeDecl));
                        if (e != null && e.getKind().isClass()) {
                            TypeElement te = (TypeElement) e;
                            workingCopy.rewrite(gu.getClassTree(), generateInterfaces(workingCopy, te, gu));
                        }
                    }
                }
            }
        };
        ModificationResult result = clazz.runModificationTask(task);
        result.commit();


//        if (isContext) addContextListenerMethods();
//        if (isContextAttr) addContextAttrListenerMethods();
//        if (isSession) addSessionListenerMethods();
//        if (isSessionAttr) addSessionAttrListenerMethods();
//        if (isRequest) addRequestListenerMethods();
//        if (isRequestAttr) addRequestAttrListenerMethods();
    }

    private ClassTree generateInterfaces(WorkingCopy wc, TypeElement te, GenerationUtils gu) {
        ClassTree newClassTree = gu.getClassTree();

        List<String> ifList = new ArrayList<String>();
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        
        if (isContext) {
            ifList.add("javax.servlet.ServletContextListener");
        }
        if (isContextAttr) {
            ifList.add("javax.servlet.ServletContextAttributeListener");
        }
        if (isSession) {
            ifList.add("javax.servlet.http.HttpSessionListener");
        }
        if (isSessionAttr) {
            ifList.add("javax.servlet.http.HttpSessionAttributeListener");
        }
        if (isRequest) {
            ifList.add("javax.servlet.ServletRequestListener");
        }
        if (isRequestAttr) {
            ifList.add("javax.servlet.ServletRequestAttributeListener");
        }
        for (String ifName : ifList) {
            newClassTree = gu.addImplementsClause(newClassTree, ifName);
            TypeElement typeElement = wc.getElements().getTypeElement(ifName);
            methods.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
        }

        for (MethodTree t : GeneratorUtilities.get(wc).createAbstractMethodImplementations(te, methods)) {
            newClassTree = GeneratorUtilities.get(wc).insertClassMember(newClassTree, t);
        }

        return newClassTree;
    }
}