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

/*
 * WSCustomizationView.java
 *
 * Created on February 27, 2006, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz
 */
public class WSCustomizationView extends SectionView{
    private Set<WSDLModel> models;
    private Definitions primaryDefinitions;
    
    static final String ID_PORT_TYPE = "ID_PORT_TYPE";
    static final String ID_PORT_TYPE_OPERATION = "ID_PORT_TYPE_OPERATION";
    static final String ID_PORT_TYPE_OPERATION_FAULT = "ID_PORT_TYPE_OPERATION_FAULT";
    static final String ID_BINDING = "ID_BINDING";
    static final String ID_BINDING_OPERATION = "ID_BINDING_OPERATION";
    static final String ID_SERVICE = "ID_SERVICE";
    static final String ID_PORT= "ID_PORT";
    static final String ID_EXTERNAL_BINDING = "ID_EXTERNAL_BINDING";
    
    /** Creates a new instance of WSCustomizationView */
    public WSCustomizationView(InnerPanelFactory factory, Set<WSDLModel> models ,
            Definitions primaryDefinitions) {
        super(factory);
        this.models = models;
        this.primaryDefinitions = primaryDefinitions;
        populateData();
        //create root node
        Children rootChildren = new Children.Array();
        Node root = new AbstractNode(rootChildren);
        
        //create Definitions node
        Node definitionsNode = new DefinitionsNode(this, primaryDefinitions);
        SectionPanel definitionsPanel = new SectionPanel(this, definitionsNode, primaryDefinitions);
        definitionsPanel.setTitle(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_GLOBAL_CUSTOMIZATION_MNEM"));
        addSection(definitionsPanel, false); //NOI18N
        
        //add the port types section
        Children portTypeChildren = new Children.Array();
        Node[] portTypeNodes = new Node[portTypesList.size()];
        int i = 0;
        for(PortType portType : portTypesList){
            portTypeNodes[i++] = new PortTypeNode(this, portType);
        }
        portTypeChildren.add(portTypeNodes);
        Node portTypeNodeContainer = new SectionContainerNode(portTypeChildren);
        portTypeNodeContainer.setName(ID_PORT_TYPE);
        portTypeNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPES"));
        
        SectionContainer portTypesCont =
                new SectionContainer(this,portTypeNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPES_MNEM"));
        // creating section panels for PortTypes
        SectionPanel[] portTypePanels = new SectionPanel[portTypesList.size()];
        i = 0;
        for(PortType portType : portTypesList){
            portTypePanels[i] = new SectionPanel(this, portTypeNodes[i], portType);
            portTypesCont.addSection(portTypePanels[i], false);
            i++;
        }
        addSection(portTypesCont, false);
        
        //add the port types operation section
        Children opChildren = new Children.Array();
        Node[] operationNodes = new Node[portTypeOperationsList.size()];
        i = 0;
        for(Operation operation : portTypeOperationsList){
            operationNodes[i++] = new PortTypeOperationNode(this, operation);
        }
        opChildren.add(operationNodes);
        Node operationNodeContainer = new SectionContainerNode(opChildren);
        operationNodeContainer.setName(ID_PORT_TYPE_OPERATION);
        operationNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPE_OPERATIONS"));
        
        SectionContainer operationCont =
                new SectionContainer(this,operationNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPE_OPERATIONS_MNEM"));
        // creatings section panels for PortType Operations
        SectionPanel[] operationPanels = new SectionPanel[portTypeOperationsList.size()];
        i = 0;
        for(Operation op : portTypeOperationsList){
            operationPanels[i] = new SectionPanel(this, operationNodes[i], op);
            operationCont.addSection(operationPanels[i], false);
            i++;
        }
        addSection(operationCont, false);
        
        //add the port type fault section
        Children faultChildren = new Children.Array();
        Node[] faultNodes = new Node[portTypeOperationFaultsList.size()];
        i = 0;
        for(Fault fault : portTypeOperationFaultsList){
            faultNodes[i++] = new PortTypeOperationFaultNode(this, fault);
        }
        faultChildren.add(faultNodes);
        Node faultNodeContainer = new SectionContainerNode(faultChildren);
        faultNodeContainer.setName(ID_PORT_TYPE_OPERATION_FAULT);
        faultNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPE_FAULTS"));
        
        SectionContainer faultCont =
                new SectionContainer(this,faultNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTTYPE_FAULTS_MNEM"));
        // creatings section panels for PortType Operation faults
        SectionPanel[] faultPanels = new SectionPanel[portTypeOperationFaultsList.size()];
        i = 0;
        for(Fault fault : portTypeOperationFaultsList){
            faultPanels[i] = new SectionPanel(this, faultNodes[i], fault);
            faultCont.addSection(faultPanels[i], false);
            i++;
        }
        addSection(faultCont, false);
        
        //add binding section
        Children bindingChildren = new Children.Array();
        Node[] bindingNodes = new Node[bindingsList.size()];
        i = 0;
        for(Binding binding : bindingsList){
            bindingNodes[i++] = new BindingNode(this, binding);
        }
        bindingChildren.add(bindingNodes);
        Node bindingNodeContainer = new SectionContainerNode(bindingChildren);
        bindingNodeContainer.setName(ID_BINDING);
        bindingNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_BINDINGS"));
        
        SectionContainer bindingCont =
                new SectionContainer(this,bindingNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_BINDINGS_MNEM"));
        // creatings section panels for Bindings
        SectionPanel[] bindingPanels = new SectionPanel[bindingsList.size()];
        i = 0;
        for(Binding binding : bindingsList){
            bindingPanels[i] = new SectionPanel(this, bindingNodes[i], binding);
            bindingCont.addSection(bindingPanels[i], false);
            i++;
        }
        addSection(bindingCont, false);
        
        //add binding operation section
        Children bindingOpChildren = new Children.Array();
        Node[] bindingOpNodes = new Node[bindingOperationsList.size()];
        i = 0;
        for(BindingOperation bindingOp : bindingOperationsList){
            bindingOpNodes[i++] = new BindingOperationNode(this, bindingOp);
        }
        bindingOpChildren.add(bindingOpNodes);
        Node bindingOpNodeContainer = new SectionContainerNode(bindingOpChildren);
        bindingOpNodeContainer.setName(ID_BINDING_OPERATION);
        bindingOpNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_BINDING_OPERATIONS"));
        
        SectionContainer bindingOpCont =
                new SectionContainer(this,bindingOpNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_BINDING_OPERATIONS_MNEM"));
        // creating section panels for Binding Operations
        SectionPanel[] bindingOpPanels = new SectionPanel[bindingOperationsList.size()];
        i = 0;
        for(BindingOperation bindingOp : bindingOperationsList){
            bindingOpPanels[i] = new SectionPanel(this, bindingOpNodes[i], bindingOp);
            bindingOpCont.addSection(bindingOpPanels[i], false);
            i++;
        }
        addSection(bindingOpCont, false);
        
        Children serviceChildren = new Children.Array();
        Node[] serviceNodes = new Node[servicesList.size()];
        i = 0;
        for(Service service : servicesList){
            serviceNodes[i++] = new ServiceNode(this, service);
        }
        serviceChildren.add(serviceNodes);
        Node serviceNodeContainer = new SectionContainerNode(serviceChildren);
        serviceNodeContainer.setName(ID_SERVICE);
        serviceNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_SERVICES"));
        
        SectionContainer servicesCont =
                new SectionContainer(this,serviceNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_SERVICES_MNEM"));
        // creating section panels for Services
        SectionPanel[] servicePanels = new SectionPanel[servicesList.size()];
        i = 0;
        for(Service service : servicesList){
            servicePanels[i] = new SectionPanel(this, serviceNodes[i], service);
            servicesCont.addSection(servicePanels[i], false);
            i++;
        }
        addSection(servicesCont, false);

        Children portChildren = new Children.Array();
        Node[] portNodes = new Node[portsList.size()];
        i = 0;
        for(Port port : portsList){
            portNodes[i++] = new PortNode(this, port);
        }
        portChildren.add(portNodes);
        Node portNodeContainer = new SectionContainerNode(portChildren);
        portNodeContainer.setName(ID_PORT);
        portNodeContainer.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTS"));
        
        SectionContainer portCont =
                new SectionContainer(this,portNodeContainer,
                NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_PORTS_MNEM"));
        // creating section panels for Ports
        SectionPanel[] portPanels = new SectionPanel[portsList.size()];
        i = 0;
        for(Port port : portsList){
            portPanels[i] = new SectionPanel(this, portNodes[i], port);
            portCont.addSection(portPanels[i], false);
            i++;
        }
        addSection(portCont, false);
        
        ExternalBindingNode externalBindingNode = new ExternalBindingNode(Children.LEAF);
        externalBindingNode.setDisplayName(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_EXTERNAL_BINDING_FILES"));
        SectionPanel externalBindingPanel = new SectionPanel(this, externalBindingNode, new BindingKey());
        externalBindingPanel.setTitle(NbBundle.getMessage(WSCustomizationView.class,
                "TITLE_EXTERNAL_BINDING_FILES_MNEM"));
        addSection(externalBindingPanel, false);
        rootChildren.add(new Node[] {definitionsNode, portTypeNodeContainer,
        operationNodeContainer, faultNodeContainer,
        bindingNodeContainer, bindingOpNodeContainer,
        serviceNodeContainer, portNodeContainer,
        externalBindingNode});
        setRoot(root);
    }
    
    private void populateData(){
        portTypesList = new ArrayList<PortType>();
        portTypeOperationsList = new ArrayList<Operation>();
        portTypeOperationFaultsList = new ArrayList<Fault>();
        bindingsList = new ArrayList<Binding>();
        bindingOperationsList = new ArrayList<BindingOperation>();
        servicesList = new ArrayList<Service>();
        portsList = new ArrayList<Port>();
        
        for(WSDLModel wsdlModel : models){
            Definitions def = wsdlModel.getDefinitions();
            Collection<PortType> portTypes = def.getPortTypes();
            portTypesList.addAll(portTypes);
            for(PortType portType : portTypes){
                Collection<Operation> operations = portType.getOperations();
                portTypeOperationsList.addAll(operations);
                for(Operation operation : operations){
                    Collection<Fault> faults = operation.getFaults();
                    portTypeOperationFaultsList.addAll(faults);
                }
            }
            
            Collection<Binding> bindings = def.getBindings();
            bindingsList.addAll(bindings);
            for(Binding binding : bindings){
                Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                bindingOperationsList.addAll(bindingOperations);
            }
            
            Collection<Service> services = def.getServices();
            servicesList.addAll(services);
            for(Service service : services){
                Collection<Port> ports = service.getPorts();
                portsList.addAll(ports);
            }
        }
    }
    
    private List<PortType> portTypesList;
    private List<Operation> portTypeOperationsList;
    private List<Fault> portTypeOperationFaultsList;
    private List<Binding> bindingsList;
    private List<BindingOperation> bindingOperationsList;
    private List<Service> servicesList;
    private List<Port> portsList;
    
    
    static class ExternalBindingNode extends AbstractNode{
        public ExternalBindingNode(Children children){
            super(children);
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(ID_EXTERNAL_BINDING);
        }
    }
    
    public static class BindingKey extends Object{
        //dummy object that serves as the key for the ExternalBindingPanel
    }
}
