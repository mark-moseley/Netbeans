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

package org.netbeans.modules.profiler.j2ee.selector.nodes.web.servlet;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.profiler.j2ee.WebProjectUtils;
import org.netbeans.modules.profiler.j2ee.ui.Utils;
import org.netbeans.modules.profiler.selector.api.SelectorChildren;
import org.netbeans.modules.profiler.selector.api.SelectorNode;
import org.netbeans.modules.profiler.selector.api.nodes.ContainerNode;
import org.netbeans.modules.profiler.selector.api.nodes.GreedySelectorChildren;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;


/**
 *
 * @author Jaroslav Bachorik
 */
public class ServletsNode extends ContainerNode {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class Children extends GreedySelectorChildren<ServletsNode> {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Set<ClassIndex.SearchScope> scope = new HashSet<ClassIndex.SearchScope>();

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Children() {
            scope.add(ClassIndex.SearchScope.SOURCE);
            scope.add(ClassIndex.SearchScope.DEPENDENCIES);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected List<SelectorNode> prepareChildren(final ServletsNode parent) {
            final Set<SelectorNode> servlets = new HashSet<SelectorNode>();

            try {
                Project project = parent.getProject();
                final ClasspathInfo cpInfo = ProjectUtilities.getClasspathInfo(project);

                Collection<FileObject> dds = WebProjectUtils.getDeploymentDescriptorFileObjects(project, true);

                for (FileObject dd : dds) {
                    enumerateServlets(cpInfo, parent, dd, servlets);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return new ArrayList<SelectorNode>(servlets);
        }

        private void enumerateServlets(final ClasspathInfo cpInfo, final ServletsNode parent, FileObject dd,
                                       final Collection<SelectorNode> servlets)
                                throws IllegalArgumentException, IOException {
            final WebApp webApp = DDProvider.getDefault().getDDRoot(dd);

            final Map<String, String> servlet2class = new HashMap<String, String>();

            for (Servlet servlet : webApp.getServlet()) {
                servlet2class.put(servlet.getServletName(), servlet.getServletClass());
            }

            JavaSource js = JavaSource.create(cpInfo, new FileObject[0]);
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController controller)
                             throws Exception {
                        for (ServletMapping mapping : webApp.getServletMapping()) {
                            TypeElement type = controller.getElements().getTypeElement(servlet2class.get(mapping.getServletName()));

                            if (type != null) {
                                servlets.add(new ServletNode(cpInfo, type, mapping.getServletName(), mapping.getUrlPattern(),
                                                             parent));
                            }
                        }
                    }
                }, true);
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String SERVLETS_STRING = NbBundle.getMessage(ServletsNode.class, "ServletsNode_ServletsString"); // NOI18N
                                                                                                                          // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ServletsNode */
    public ServletsNode(ContainerNode parent) {
        super(SERVLETS_STRING, Utils.PACKAGE_ICON, parent);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected SelectorChildren getChildren() {
        return new Children();
    }
}
