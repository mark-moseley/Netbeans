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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.filesystems.FileObject;

public class ProjectSupport {
    public static boolean saveAllProjects(String extraMessage) {
	boolean ok = true;
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
	    MakeConfigurationDescriptor projectDescriptor = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(openProjects[i]);
	    if (projectDescriptor != null)
		ok = ok && projectDescriptor.save(extraMessage);
	}
	return ok;
    }

    public static Date lastModified(Project project) {
	FileObject projectFile = null;
	try {
	    projectFile = project.getProjectDirectory().getFileObject("nbproject" + File.separator + "Makefile-impl.mk"); // NOI18N
	}
	catch (Exception e) {
	    // happens if project is not a MakeProject
	}
	if (projectFile == null)
	    projectFile = project.getProjectDirectory();
	return projectFile.lastModified();
    }

    public static void executeCustomAction(Project project, CustomProjectActionHandler customProjectActionHandler) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        if (pdp == null)
            return;
        MakeConfigurationDescriptor projectDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        MakeConfiguration conf = (MakeConfiguration)projectDescriptor.getConfs().getActive();

        MakeActionProvider ap = project.getLookup().lookup(MakeActionProvider.class );
        if (ap == null)
            return;

        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class );
        String projectName = info.getDisplayName();

        ap.invokeCustomAction(projectName, projectDescriptor, conf, customProjectActionHandler);
//        ArrayList actionEvents = new ArrayList();
//        ap.addAction(actionEvents, projectName, projectDescriptor, conf, MakeActionProvider.COMMAND_CUSTOM_ACTION, null);
//	ActionEvent ae = new ActionEvent((ProjectActionEvent[])actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]), 0, null);
//        DefaultProjectActionHandler defaultProjectActionHandler = new DefaultProjectActionHandler();
//        defaultProjectActionHandler.setCustomActionHandlerProvider(customProjectActionHandler);
//        defaultProjectActionHandler.actionPerformed(ae);
    }
}
