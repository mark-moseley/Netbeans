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

package org.netbeans.modules.hudson.maven;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Enables developers to run and debug Hudson plugins.
 */
@ServiceProvider(service=MavenActionsProvider.class, position=57)
public class HpiActionGoalProvider implements MavenActionsProvider {

    private static final HashSet<String> ACTIONS = new HashSet<String>(Arrays.asList(ActionProvider.COMMAND_RUN, ActionProvider.COMMAND_DEBUG));

    private static Map<Project,Boolean> IS_HPI = new WeakHashMap<Project,Boolean>();

    private static synchronized boolean isHPI(Project p) {
        Boolean b = IS_HPI.get(p);
        if (b == null) {
            b = "hpi".equals(p.getLookup().lookup(NbMavenProject.class).getPackagingType()); // NOI18N
            IS_HPI.put(p, b);
        }
        return b;
    }

    private static final AbstractMavenActionsProvider delegate = new AbstractMavenActionsProvider() {
        protected InputStream getActionDefinitionStream() {
            return HpiActionGoalProvider.class.getResourceAsStream("action-mappings.xml"); // NOI18N
        }
    };

    public RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup) {
        if (isActionEnable(actionName, project, null)) {
            return delegate.createConfigForDefaultAction(actionName, project, lookup);
        } else {
            return null;
        }
    }

    public NetbeansActionMapping getMappingForAction(String actionName, Project project) {
        if (isActionEnable(actionName, project, null)) {
            return delegate.getMappingForAction(actionName, project);
        } else {
            return null;
        }
    }

    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        return ACTIONS.contains(action) && isHPI(project);
    }

    public Set<String> getSupportedDefaultActions() {
        return ACTIONS;
    }

}
