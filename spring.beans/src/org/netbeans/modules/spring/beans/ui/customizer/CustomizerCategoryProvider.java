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

package org.netbeans.modules.spring.beans.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class CustomizerCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public Category createCategory(Lookup context) {
        String categoryName = NbBundle.getMessage(CustomizerCategoryProvider.class, "LBL_SpringFramework");
        return Category.create("SpringFramework", categoryName, null); // NOI18N
    }

    public JComponent createComponent(Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        if (project == null) {
            throw new IllegalStateException("The lookup " + context + " does not contain a Project");
        }
        ProjectSpringScopeProvider scopeProvider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
        // The following should pass, since we only register the customizer for
        // projects for which we also extend the lookup with a ProjectSpringScopeProvider.
        assert scopeProvider != null;
        ConfigFileManager manager = scopeProvider.getSpringScope().getConfigFileManager();
        ConfigFileGroupsPanel panel = new ConfigFileGroupsPanel(manager.getConfigFileGroups());
        CategoryListener listener = new CategoryListener(manager, panel);
        category.setOkButtonListener(listener);
        category.setStoreListener(listener);
        return panel;
    }

    private static final class CategoryListener implements ActionListener {

        private final ConfigFileManager manager;
        private final ConfigFileGroupsPanel panel;
        private volatile List<ConfigFileGroup> groups;

        public CategoryListener(ConfigFileManager manager, ConfigFileGroupsPanel panel) {
            this.manager = manager;
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            if (groups == null) {
                // OK button listener called.
                assert SwingUtilities.isEventDispatchThread();
                groups = panel.getConfigFileGroups();
            } else {
                // Store listener called.
                manager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        manager.putConfigFileGroups(groups);
                        // No need to save the project explicitly, the
                        // customizer dialog will.
                    }
                });
            }
        }
    }
}
