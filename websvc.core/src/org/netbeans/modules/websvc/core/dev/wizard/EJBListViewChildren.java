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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Provides EJB tree of all open projects. This class is not used for displaying Enterprise Beans node in project view.
 */
public final class EJBListViewChildren extends Children.Keys<String> {

    public static final String KEY_EJBS = "ejbKey"; //NOI18N
        private Project project;

    public EJBListViewChildren(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    protected void addNotify() {
        createNodes();
    }

    private void createNodes() {
        List<String> l = new ArrayList<String>();
        l.add(KEY_EJBS);
        setKeys(l);
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<String>emptySet());
    }

    public Node[] createNodes(String key) {
        Node n = null;
        if (key == KEY_EJBS) {
            EjbJar[] apiEjbJars = EjbJar.getEjbJars(project);
            n = J2eeProjectView.createEjbsView(apiEjbJars[0], project);
        }
        return n == null ? new Node[0] : new Node[] {n};
    }
    
}
