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

package org.netbeans.modules.sun.manager.jbi.nodes;

import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceAssemblyStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceUnitStatus;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 *
 */
public class JBIContainerChildFactory {
    
    private AppserverJBIMgmtController controller;
    
    /**
     * Public constructor for factory used to create the children of a given
     * NodeType.
     *
     * @param controller The AppserverMgmtController used as an interface to
     *        the AMX API necessary for determining the existence of certain
     *        components such as resources, apps, etc. on the server.
     */
    public JBIContainerChildFactory(AppserverJBIMgmtController controller) {
        this.controller = controller;
    }
    
    
    /**
     * Creates the children for a given NodeType.
     *
     * @param type The NodeType typs for a particular node.
     * @return The Children object containing a Node[] array of children.
     */
    public Children getChildren(Node node, NodeType type) {
        Children children = new Children.Array();
        children.add(getChildrenObject(node, type));        
        return children;
    }
    
    /**
     * 
     * @param node
     * @param type
     * @return
     */
    public Node[] getChildrenObject(Node node, NodeType type) {
        Node[] children = new Node[] {};
        if (NodeType.JBI.equals(type)) {
            children = createJBIChildren();
        } else if (NodeType.SERVICE_ENGINES.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentType.SERVICE_ENGINE);
        } else if (NodeType.BINDING_COMPONENTS.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentType.BINDING_COMPONENT);
        } else if (NodeType.SHARED_LIBRARIES.equals(type)) {
            children = 
                createJBIComponentContainerChildren(
                        JBIComponentType.SHARED_LIBRARY);
        } else if (NodeType.SERVICE_ASSEMBLIES.equals(type)) {
            children = createServiceAssembliesChildren();
        } else if (NodeType.SERVICE_ASSEMBLY.equals(type)) {
            children = 
                createServiceAssemblyChildren(
                        (JBIServiceAssemblyNode) node);
        }
        return children;
    }
    
    
    /**
     * 
     * @return
     */
    private Node[] createJBIChildren() {
        return new Node[] {
                new JBIComponentContainerNode.ServiceEngines(controller), 
                new JBIComponentContainerNode.BindingComponents(controller), 
                new JBIComponentContainerNode.SharedLibraries(controller),
                new JBIServiceAssembliesNode(controller)
        };
    }
    
    /**
     * 
     * @param componentType
     * @return
     */
    private Node[] createJBIComponentContainerChildren(JBIComponentType componentType) {
        AdministrationService adminService = 
                controller.getJBIAdministrationService();
        List<JBIComponentStatus> compList = 
                adminService.getJBIComponentStatusList(componentType);
        
        Node[] nodes = new Node[compList.size()];
        
        int index = 0;
        for (JBIComponentStatus comp : compList) {
            String name = comp.getName();
            String description = comp.getDescription();
            
            Node newNode;
            if (componentType.equals(JBIComponentType.SERVICE_ENGINE)) {
                newNode = 
                    new JBIComponentNode.ServiceEngine(
                            controller, name, description);
            } else if (componentType.equals(JBIComponentType.BINDING_COMPONENT)) {
                newNode = 
                    new JBIComponentNode.BindingComponent(
                            controller, name, description);
            } else {
                newNode = 
                    new JBIComponentNode.SharedLibrary(
                            controller, name, description);
            }
            nodes[index++] = newNode;
        }
        
        return nodes;
    }  
    
    /**
     * 
     * @return
     */
    private Node[] createServiceAssembliesChildren() {
        AdministrationService adminService =
                controller.getJBIAdministrationService();
        List<JBIServiceAssemblyStatus> assemblyList =
                adminService.getServiceAssemblyStatusList();
        
        Node[] nodes = new Node[assemblyList.size()];
        
        int index = 0;
        for (JBIServiceAssemblyStatus assembly : assemblyList) { 
            String name = assembly.getServiceAssemblyName();
            String description = assembly.getServiceAssemblyDescription();
            nodes[index++] = 
                new JBIServiceAssemblyNode(controller, name, description);
        }
        
        return nodes;
    }
    
    /**
     * 
     * @param node
     * @return
     */
    private Node[] createServiceAssemblyChildren(JBIServiceAssemblyNode node) {
        
        JBIServiceAssemblyStatus assembly = node.getAssembly();
        List unitList = assembly.getJbiServiceUnitStatusList();
        
        Node[] nodes = new Node[unitList.size()];
        
        int index = 0;        
        for (Iterator iter = unitList.iterator(); iter.hasNext();) {
            JBIServiceUnitStatus unit = (JBIServiceUnitStatus) iter.next();
            String unitName = unit.getServiceUnitName();
            String unitDescription = unit.getServiceUnitDescription();
            nodes[index++] = new JBIServiceUnitNode(
                    controller, unitName, unitName, unitDescription);
        }
        
        return nodes;
    }
}
