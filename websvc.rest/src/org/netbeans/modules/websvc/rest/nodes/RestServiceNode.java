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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.rest.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.nodes.AbstractNode;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class RestServiceNode extends AbstractNode{
    private Project project;
    private String serviceName;
    private String uriTemplate;
    private String className;
   
    private MetadataModel<RestServicesMetadata> model;
    
    public  RestServiceNode(Project project, MetadataModel<RestServicesMetadata> model,
            RestServiceDescription desc) {
        this(project, model, desc, new InstanceContent());
    }
    
    private RestServiceNode(Project project, MetadataModel<RestServicesMetadata> model,
            RestServiceDescription desc, InstanceContent content) {
        super(new RestServiceChildren(project, model, desc.getName()), new AbstractLookup(content));
        this.project = project;
        this.serviceName = desc.getName();
        this.uriTemplate = desc.getUriTemplate();
        this.className = desc.getClassName();
        
        content.add(this);
        content.add(OpenCookieFactory.create(project, className));
    }
    
    public String getDisplayName() {
        if (uriTemplate.length() > 0) {
            return serviceName + " [" + uriTemplate + "]";      //NOI18N
        } else {
            return serviceName;
        }
    }
    
    public String getShortDescription() {
        return "";
    }
    
    public static String getKey(RestServiceDescription desc) {
        return desc.getName() + ":" + desc.getUriTemplate();        //NOI18N
    }
    
    private static final java.awt.Image SERVICE_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/rest/nodes/resources/restservice.png" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        return SERVICE_BADGE;
    }
    
    void changeIcon() {
        fireIconChange();
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            null,
            //SystemAction.get(DeleteAction.class),
            //null,
            SystemAction.get(PropertiesAction.class),
        };
    }
}
