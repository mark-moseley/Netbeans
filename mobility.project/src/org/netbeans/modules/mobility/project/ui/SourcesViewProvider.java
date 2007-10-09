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

/*
 * SourcesViewProvider.java
 *
 * Created on 21 April 2006, 15:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.J2MEPhysicalViewProvider.ChildLookup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Lukas Waldmann
 */
final class SourcesViewProvider extends ChildLookup {
    public Node[] createNodes(final J2MEProject project) {
        Node n = null;
        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
        final FileObject root = helper.resolveFileObject (helper.getStandardPropertyEvaluator ().getProperty ("src.dir")); // NOI18N
        DataObject dao = null;
        try {
            dao = root == null ? null : DataObject.find (root);
            final Sources src = ProjectUtils.getSources(project);
            if (src != null) {
                final SourceGroup sg[] = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (sg != null && sg.length == 1)
                    n = PackageView.createPackageView(sg[0]);
            }
        } catch (Exception e) {}
        if (dao == null || n == null) {
            setLookups(new Lookup[] {Lookups.singleton(project)});
        } else {
            setLookups(new Lookup[] {Lookups.fixed(new Object[] {project, dao}), n.getLookup()});
        }
        return new Node[] {n == null ? Node.EMPTY : new FilterNode(n)};
    }
}
