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

package org.netbeans.modules.cnd.makeproject.api.actions;

import java.io.File;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.utils.AllFileFilter;
import org.netbeans.modules.cnd.api.utils.AllSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.CCSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.CSourceFileFilter;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.FortranSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.HeaderSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.ResourceFileFilter;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class AddExistingItemAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes)  {
	if (activatedNodes.length != 1)
	    return false;
        Object o = activatedNodes[0].getValue("Folder"); // NOI18N
        if (!(o instanceof Folder))
            return false;
	Folder folder = (Folder)o;
	if (folder == null)
	    return false;
	if (!folder.isProjectFiles())
	    return false;
	return true;
    }

    public String getName() {
	return getString("CTL_AddExistingItemAction"); // NOI18N
    }

    public void performAction(Node[] activatedNodes) {
	boolean notifySources = false;
	Node n = activatedNodes[0];
	Project project = (Project)n.getValue("Project"); // NOI18N
	assert project != null;
	Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	assert folder != null;

	ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	ConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();

	String seed = null;
	if (FileChooser.getCurrectChooserFile() != null) {
	    seed = FileChooser.getCurrectChooserFile().getPath();
	}
	if (seed == null) {
	    seed = projectDescriptor.getBaseDir();
	}
	FileChooser fileChooser = new FileChooser(getString("SelectItem"), getString("Select"), FileChooser.FILES_ONLY, null, seed, false);
	PathPanel pathPanel = new PathPanel();
	fileChooser.setAccessory(pathPanel);
	fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(CSourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(CCSourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(HeaderSourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(FortranSourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(ResourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(AllSourceFileFilter.getInstance());
        fileChooser.addChoosableFileFilter(AllFileFilter.getInstance());
        fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
	int ret = fileChooser.showOpenDialog(null); // FIXUP
	if (ret == FileChooser.CANCEL_OPTION)
	    return;

	File[] files = fileChooser.getSelectedFiles();
	Item[] items = new Item[files.length];
	for (int i = 0; i < files.length; i++) {
	    String itemPath;
	    if (pathPanel.getMode() == PathPanel.REL_OR_ABS)
		itemPath = IpeUtils.toAbsoluteOrRelativePath(projectDescriptor.getBaseDir(), files[i].getPath());
	    else if (pathPanel.getMode() == PathPanel.REL)
		itemPath = IpeUtils.toRelativePath(projectDescriptor.getBaseDir(), files[i].getPath());
	    else
		itemPath = files[i].getPath();
	    itemPath = FilePathAdaptor.mapToRemote(itemPath);
	    itemPath = FilePathAdaptor.normalize(itemPath);
	    if (((MakeConfigurationDescriptor)projectDescriptor).findProjectItemByPath(itemPath) != null) {
		String errormsg = getString("AlreadyInProjectError", itemPath); // NOI18N
		DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                return;
	    }
	    else {
		folder.addItemAction(items[i] = new Item(itemPath));
		if (IpeUtils.isPathAbsolute(itemPath))
		    notifySources = true;
	    }
	}
	MakeLogicalViewProvider.setVisible(project, items);

	if (notifySources)
	    ((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();
    }

    public HelpCtx getHelpCtx() {
	return null;
    }

    protected boolean asynchronous() {
	return false;
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(BatchBuildAction.class);
	}
	return bundle.getString(s);
    }
    private static String getString(String s, String arg) {
        return NbBundle.getMessage(BatchBuildAction.class, s, arg);
    }
}
