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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.datatransfer.Transferable;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction; 
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.AddGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.AddServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.RenameGroupAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities; 
import org.openide.util.datatransfer.PasteType;

/**
 * Node representing Group of Web Services
 * @author nam
 */
public class SaasGroupNode extends AbstractNode {
    private final SaasGroup group;

    public SaasGroupNode(SaasGroup group) {
        super(new SaasGroupNodeChildren(group));
        this.group = group;
        setName(group.getName());
    }
    
    @Override
    public boolean canRename() {
        return group.isUserDefined();
    }

    private Image getUserDirFolderImage(int type) {
        FileObject folder = FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); //NOI18N
        if (folder != null) {
            DataFolder df = DataFolder.findFolder(folder);
            if (df != null) {
                return df.getNodeDelegate().getIcon(type);
            }
        }
        return null;
    }

    @Override
    public Image getIcon(int type){
        Image standardFolderImage = getUserDirFolderImage(type);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return Utilities.loadImage("org/netbeans/modules/websvc/saas/resources/folder-closed.png");
    }
    
    @Override
    public Image getOpenedIcon(int type){
        Image standardFolderImage = getUserDirFolderImage(type);
        if (standardFolderImage != null) {
            return standardFolderImage;
        }
        return Utilities.loadImage("org/netbeans/modules/websvc/saas/resources/folder-open.png");
    }
    
    @Override
    public void setName(String name){
        if (group.isUserDefined()) {
            super.setName(name);
            group.setName(name);
        }
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            for (Action a : ext.getSaasActions(this.getLookup())) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(AddServiceAction.class));
        actions.add(SystemAction.get(AddGroupAction.class));
        actions.add(SystemAction.get(DeleteGroupAction.class));
        actions.add(SystemAction.get(RenameGroupAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    @Override
    public boolean canDestroy() {
        return group.isUserDefined();
    }
    
    @Override
    public void destroy() throws IOException{
        SaasServicesModel.getInstance().removeGroup(group);
        super.destroy();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("websvcGroupNode");
    }

    @Override
    protected void createPasteTypes(final Transferable t, List<PasteType> s) {
        //TODO review original
    }
    
    
}
