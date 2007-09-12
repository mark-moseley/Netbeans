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
package org.netbeans.modules.compapp.casaeditor.api;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.casaeditor.palette.DefaultPluginDropHandler;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePluginWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * Integrates Composite Application Project Type Plugins into
 * the CASA palette.
 * 
 * @author jsandusky
 */
public class InternalProjectTypePalettePlugin 
implements CasaPalettePlugin {

    private InternalProjectTypePlugin mProjectTypePlugin;
    
    
    public InternalProjectTypePalettePlugin(InternalProjectTypePlugin projectTypePlugin) {
        mProjectTypePlugin = projectTypePlugin;
    }

    
    public CasaPaletteItemID[] getItemIDs() {
        CasaPaletteItemID itemID = new CasaPaletteItemID(
                this,
                mProjectTypePlugin.getCategoryName(),
                mProjectTypePlugin.getPluginName(),
                mProjectTypePlugin.getIconFileBase());
        return new CasaPaletteItemID[] { itemID };
    }

    public REGION getDropRegion(CasaPaletteItemID itemID) {
        return CasaPalettePlugin.REGION.JBI_MODULES;
    }

    public void handleDrop(PluginDropHandler dropHandler, CasaPaletteItemID itemID) throws IOException {
        InternalProjectTypePluginWizardIterator wizardIterator = mProjectTypePlugin.getWizardIterator();
        WizardDescriptor descriptor = new WizardDescriptor(wizardIterator);
        DefaultPluginDropHandler defaultHandler = (DefaultPluginDropHandler) dropHandler;
        Project project = null;
        
        // Set up project name and location
        File projectsRoot = defaultHandler.getInternalProjectTypePluginLocation();
        String projectName = getProjectCount(
                projectsRoot,
                mProjectTypePlugin.getCategoryName() + "_" +
                mProjectTypePlugin.getPluginName());
        File projectFolder = new File(projectsRoot, projectName);
        descriptor.putProperty(WizardPropertiesTemp.PROJECT_DIR, projectFolder);
        descriptor.putProperty(WizardPropertiesTemp.NAME, projectName);
        descriptor.putProperty(WizardPropertiesTemp.J2EE_LEVEL, "1.4");
        descriptor.putProperty(WizardPropertiesTemp.SET_AS_MAIN, new Boolean(false));
        
        if (wizardIterator.hasContent()) {
            descriptor.setModal(true);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        } else {
            wizardIterator.instantiate();
        }
        
        project = wizardIterator.getProject();
        if (project != null) {
            dropHandler.addInternalJBIModule(project);
        }
    }
    
    private String getProjectCount(File parentFolder, String baseName) {
        File file = null;
        int baseCount = 0;
        do {
            baseCount++;
            file = new File(parentFolder, baseName + baseCount);
        } while (file.exists());
        return file.getName();
    }

}
