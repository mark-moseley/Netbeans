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

package org.netbeans.modules.profiler.selector.java.impl;

import org.netbeans.modules.profiler.selector.java.project.nodes.ProjectNode;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.selector.spi.SelectionTreeBuilder;
import org.netbeans.modules.profiler.selector.spi.nodes.SelectorNode;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(service=SelectionTreeBuilder.class,
    projectTypes={@ProjectType(id="org-netbeans-modules-java-j2seproject"),
                    @ProjectType(id="org-netbeans-modules-j2ee-earproject"),
                    @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
                    @ProjectType(id="org-netbeans-modules-web-project"),
                    @ProjectType(id="org-netbeans-modules-apisupport-project"),
                    @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
                    @ProjectType(id="org-netbeans-modules-ant-freeform", position=1200),
                    @ProjectType(id="org-netbeans-modules-maven")
    }
)
public class PackageSelectionTreeViewBuilder extends ProjectSelectionTreeBuilder {

    public PackageSelectionTreeViewBuilder(Project project) {
        this(project, false);
    }

    public PackageSelectionTreeViewBuilder(Project project, boolean isPreferred) {
        super(new Type("package-view", "Package View"), isPreferred, project);
    }

    @Override
    public List<? extends SelectorNode> buildSelectionTree() {
        return Collections.singletonList(new ProjectNode(project));
    }

    @Override
    public int estimatedNodeCount() {
        return 1;
    }

}
