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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.RefreshServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.ViewApiDocAction;
import org.netbeans.modules.websvc.saas.ui.actions.ViewWadlAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WadlSaasNode extends AbstractNode {
    private WadlSaas wadlSaas;
    
    public WadlSaasNode(WadlSaas wadlSaas) {
        this(wadlSaas, new InstanceContent());
        this.wadlSaas = wadlSaas;
    }

    public WadlSaasNode(WadlSaas wadlSaas, InstanceContent content) {
        super(new WadlSaasNodeChildren(wadlSaas));
        this.wadlSaas = wadlSaas;
        content.add(wadlSaas);
    }

    @Override
    public String getDisplayName() {
        return wadlSaas.getDisplayName();
    }
    
    @Override
    public String getShortDescription() {
        return wadlSaas.getDescription();
    }
    
    private static final java.awt.Image SERVICE_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/restservice.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return SERVICE_BADGE;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }

    @Override
    public boolean canRename() {
        return super.canRename();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            for (Action a : ext.getSaasActions(this.getLookup())) {
                actions.add(a);
            }
        }
        actions.add(SystemAction.get(ViewApiDocAction.class));
        actions.add(SystemAction.get(ViewWadlAction.class));
        actions.add(SystemAction.get(DeleteServiceAction.class));
        actions.add(SystemAction.get(RefreshServiceAction.class));

        return actions.toArray(new Action[actions.size()]);
    }

}
