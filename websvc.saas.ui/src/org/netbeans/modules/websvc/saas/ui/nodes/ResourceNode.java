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
import java.util.Arrays;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.openide.nodes.AbstractNode;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class ResourceNode extends AbstractNode {
    private final WadlSaasResource resource;
    
    public ResourceNode(WadlSaasResource resource) {
        this(resource, new InstanceContent());
    }

    public ResourceNode(WadlSaasResource resource, InstanceContent content) {
        super(new ResourceNodeChildren(resource), new AbstractLookup(content));
        this.resource = resource;
        content.add(resource);
    }

    public Resource getResource() {
        return resource.getResource();
    }
    
    @Override
    public String getDisplayName() {
        return "[" + getResource().getPath() + "]";
    }
    
    @Override
    public String getShortDescription() {
        StringBuffer sb = new StringBuffer();
        sb.append(resource.getSaas().getBaseURL());
        sb.append('/');
        WadlSaasResource r = resource;
        while (r != null) {
            sb.insert(0, '/');
            sb.insert(0, r.getResource().getPath());
            r = r.getParent();
        }
        return sb.toString();
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
}
