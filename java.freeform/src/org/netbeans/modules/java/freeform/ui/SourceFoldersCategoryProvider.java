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

package org.netbeans.modules.java.freeform.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.LookupProviderImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkleint
 */
public class SourceFoldersCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    /** Creates a new instance of SouceFoldersCustomizerProvider */
    public SourceFoldersCategoryProvider() {
    }
    
    public Category createCategory(Lookup context) {
        AuxiliaryConfiguration aux = context.lookup(AuxiliaryConfiguration.class);
        final ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        Project project = context.lookup(Project.class);
        assert aux != null;
        assert acc != null;
        assert project != null;
        if (LookupProviderImpl.isMyProject(aux)) {
            Category cat = ProjectCustomizer.Category.create("SourceFolders", //NOI18N
                    NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Sources"), null);
            final ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(acc.getHelper(), acc.getEvaluator()), 
                    FileUtil.toFile(project.getProjectDirectory()), acc.getEvaluator(), acc.getHelper());
            InstanceContent ic = context.lookup(InstanceContent.class);
            assert ic != null;
            ic.add(pm);
            cat.setOkButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                   // store changes always because model could be modified in ClasspathPanel or OutputPanel
                    ProjectModel.saveProject(acc.getHelper(), pm);                
                }
            });
            return cat;
        }
        return null;
    }

    public JComponent createComponent(Category category, Lookup context) {
        ProjectModel pm = context.lookup(ProjectModel.class);
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        assert pm != null;
        SourceFoldersPanel panel = new SourceFoldersPanel(false);
        panel.setModel(pm, acc.getHelper());    
        return panel;
        
    }

}
