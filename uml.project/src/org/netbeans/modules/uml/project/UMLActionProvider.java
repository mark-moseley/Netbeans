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

package org.netbeans.modules.uml.project;

import java.io.IOException;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

public class UMLActionProvider implements ActionProvider
{
    // Commands available from J2SE project
    private static final String[] supportedActions =
    {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE
    };
    
    private UMLProject mProject = null;
    private UMLProjectHelper mHelper = null;
    
    public UMLActionProvider(UMLProject project, UMLProjectHelper helper)
    {
        mProject = project;
        mHelper = helper;
    }
    
    public String[] getSupportedActions()
    {
        return supportedActions;
    }
    
    public void invokeAction(String command, Lookup context)
        throws IllegalArgumentException
    {
        if (command.equals(COMMAND_DELETE))
        {
            UMLPhysicalViewProvider provider = (UMLPhysicalViewProvider)mProject.
                getLookup().lookup(UMLPhysicalViewProvider.class);
            
            try
            {
                provider.createLogicalView().destroy();
            }
            
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException
    {
        return true;
    }
}