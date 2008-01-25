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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.projects.common.ui.actions;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.ImplicitCatalogValidator;
import org.netbeans.modules.compapp.projects.common.ImplicitCatalogValidator.ResultPrinter;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

/**
 * This class provides the project action interface which can be used
 * to add implicit catalog validation action to the available project
 * actions.
 * User should use the #projectSensitiveAction() to create an instance
 * of this action.
 * 
 * @author chikkala
 */
public final class ValidateImplicitCatalogAction extends CookieAction implements ProjectActionPerformer {

    /**
     * 
     * @param activatedNodes
     */
    protected void performAction(Node[] activatedNodes) {
        Project project = activatedNodes[0].getLookup().lookup(Project.class);
        perform(project);
    }

    /**
     * 
     * @return
     */
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(ValidateImplicitCatalogAction.class, "LBL_ValidateImplicitCatalogAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{Project.class};
    }

    /**
     * 
     */
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    /**
     * 
     * @return
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * 
     * @return
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * 
     * @param project
     * @return
     */
    public boolean enable(Project project) {
        return true;
    }

    /**
     * 
     * @param project
     */
    public void perform(Project project) {
        try {
            ImplicitCatalogValidator validator = ImplicitCatalogValidator.newInstance(project);
            ResultPrinter prn = new ResultPrinter() {

                @Override
                protected void initWriters() {
                    OutputWriter out = IOProvider.getDefault().getStdOut();
                    setOutWriter(out);
                    setErrorWriter(out);
                }
            };
            validator.setResultPrinter(prn);
            validator.validate();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * 
     * @return
     */
    public static Action projectSensitiveAction() {
        ProjectActionPerformer performer = new ValidateImplicitCatalogAction();
        String name = NbBundle.getMessage(ValidateImplicitCatalogAction.class, "LBL_ValidateImplicitCatalogAction");
        return ProjectSensitiveActions.projectSensitiveAction(performer, name, null);
    }
}

