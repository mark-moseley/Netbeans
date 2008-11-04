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

package org.netbeans.modules.cnd.makeproject.runprofiles;

import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomizerNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.configurations.CustomizerNodeProvider.class)
public class RunProfileNodeProvider implements CustomizerNodeProvider {
    
    /**
     * Creates an instance of a customizer node
     */
    private CustomizerNode customizerNode = null;
    
    public CustomizerNode factoryCreate() {
        if (customizerNode == null)
            customizerNode = createProfileNode();
	return customizerNode;
    }
    
    public CustomizerNode createProfileNode() {
            return new RunProfileCustomizerNode(
                "Run", // NOI18N
                getString("RUNNING"),
                null);
    }

    class RunProfileCustomizerNode extends CustomizerNode {
	public RunProfileCustomizerNode(String name, String displayName, CustomizerNode[] children) {
	    super(name, displayName, children);
	}

        @Override
	public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
	    RunProfile runProfile = (RunProfile) configuration.getAuxObject(RunProfile.PROFILE_ID);
            boolean isRemote = false;
            if (configuration instanceof MakeConfiguration) {
                isRemote = !((MakeConfiguration) configuration).getDevelopmentHost().isLocalhost();
            }
	    return runProfile != null ? runProfile.getSheet(isRemote) : null;
	    //return configurationDescriptor.getSheet(project, configuration);
	}
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsRunning"); // NOI18N
        }
    }
    
    /** Look up i18n strings here */
    private ResourceBundle bundle;
    protected String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(RunProfileNodeProvider.class);
	}
	return bundle.getString(s);
    }
}
