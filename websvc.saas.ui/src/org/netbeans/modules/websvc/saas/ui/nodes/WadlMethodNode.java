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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.xml.bind.JAXBElement;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.ParamStyle;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.TestMethodAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WadlMethodNode extends AbstractNode {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    
    private Method method;
    private Resource[] path;
    private WadlSaas wadlSaas;
    
    public WadlMethodNode(WadlSaas wadlSaas, Resource[] path, Method method) {
        this(wadlSaas, path, method, new InstanceContent());
    }

    public WadlMethodNode(WadlSaas wadlSaas, Resource[] path, Method method, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.wadlSaas = wadlSaas;
        this.path = path;
        this.method = method;
        content.add(method);
        content.add(wadlSaas);
    }

    @Override
    public String getDisplayName() {
        if (method.getId() != null) {
            return method.getId();
        }
        String name = method.getName();
        String displayName = name;
        if (GET.equals(name)) {
            Set<String> medias = SaasUtil.getMediaTypesFromJAXBElement(method.getResponse().getRepresentationOrFault());
            if (medias != null && medias.size() > 0) {
                displayName += medias.toString();
            }
        } else if (PUT.equals(name) || POST.equals(name)) {
            Set<String> medias = SaasUtil.getMediaTypes(method.getRequest().getRepresentation());
            if (medias != null && medias.size() > 0) {
                displayName += medias;
            }
        }
        return displayName;
    }
    
    @Override
    public String getShortDescription() {
        return SaasUtil.getSignature(wadlSaas, path, method);
    }
    
    private static final java.awt.Image ICON =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/method.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            for (Action a : ext.getSaasActions(this.getLookup())) {
                actions.add(a);
            }
        }
        //TODO maybe ???
        actions.add(SystemAction.get(TestMethodAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
}
