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
package org.netbeans.modules.j2ee.sun.ide.runtime.nodes;

import java.util.List;
import java.util.Arrays;

import javax.swing.Action;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtLeafNode;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.j2ee.sun.ide.controllers.AppClientModuleController;
import org.netbeans.modules.j2ee.sun.ide.controllers.DeployedItemsController;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.ide.runtime.actions.UndeployAction;
import org.netbeans.modules.j2ee.sun.bridge.apis.Undeployable;
import org.openide.actions.PropertiesAction;




/**
 */
public class AppClientModuleNode extends AppserverMgmtLeafNode 
        implements Undeployable {
        
    private static String NODE_TYPE = NodeTypes.APP_CLIENT_MODULE;
    private String appClientName;
    private boolean isEmbedded;
            

    
    
    /**
     *
     *
     */
    public AppClientModuleNode(AppClientModuleController controller) {
        super(controller,NODE_TYPE);
        this.appClientName = controller.getName();
        setDisplayName(appClientName);
    }
    
    
    /**
     *
     *
     */
    public AppClientModuleNode(final AppClientModuleController controller,
            final boolean isEmbedded) {
        super(controller,NODE_TYPE);
        this.appClientName = controller.getName();
        this.isEmbedded = isEmbedded;
        setDisplayName(appClientName);

    }
    
    public AppClientModuleNode(final String name) {
        super(NODE_TYPE);
        this.isEmbedded = true;
        setDisplayName(name);
    }
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on an Applications node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        if(!isEmbedded) {
            return new SystemAction[] {
                SystemAction.get(UndeployAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        } else {
            return new SystemAction[] {
                SystemAction.get(PropertiesAction.class)
            };
        }
    }
    
    
    /**
     * Return the SheetProperties to be displayed for this Application Client
     * Module.
     *
     * @return A java.util.Map containing all app client module properties.
     */
    protected java.util.Map getSheetProperties() {
       return getController().getProperties(getPropertiesToIgnore()); 
    }
      
    
    /**
     * Sets the property as an attribute to the underlying AMX mbeans. It 
     * usually will delegate to the controller object which is responsible for
     * finding the correct AMX mbean objectname in order to execute a 
     * JMX setAttribute.
     *
     * @param attrName The name of the property to be set.
     * @param value The value retrieved from the property sheet to be set in the
     *        backend.
     * @returns the updated Attribute accessed from the Sheet
     */
    public javax.management.Attribute setSheetProperty(String attrName, Object value) {
        return getController().setProperty(attrName, value);
    }
    
    
    /**
     *
     *
     */
    public void undeploy() {
        ((DeployedItemsController)getController()).undeploy();
    }
    
    protected List getPropertiesToIgnore() {
        return Arrays.asList(NodeTypes.APP_CLIENT_MODULES_NODE);        
    }
}
