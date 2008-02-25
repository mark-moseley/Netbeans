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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.WSDLEndpointAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.LoadWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.CloneWSDLPortAction;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.properties.PortTypeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortNode;
import org.openide.nodes.Children;

import javax.swing.Action;
/**
 *
 * @author Josh Sandusky
 */
public class WSDLEndpointNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/WSDLEndpointNode.png");    // NOI18N
    
    private static final String CHILD_ID_PROVIDES = "Provides"; // NOI18N
    private static final String CHILD_ID_CONSUMES = "Consumes"; // NOI18N
    private static final String SOAP_BINDING = "soap"; // NOI18N

    
    public WSDLEndpointNode(CasaPort component, CasaNodeFactory factory) {
        super(component, new MyChildren(component, factory), factory);
    }
    
    
    @Override
    public String getName() {
        CasaPort endpoint = (CasaPort) getData();
        if (endpoint != null) {
            return endpoint.getEndpointName();
            //return endpoint.getQName().getLocalPart(); // TMP FIXME
        }
        return super.getName();
    }
    
    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getName();
            CasaPort endpoint = (CasaPort) getData();
            String decoration = null;
            if (endpoint != null) {
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",    // NOI18N
                        endpoint.getEndpointName());
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";    // NOI18N
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }

    // todo: 05/31/07, enable WSIT GUI
    @Override
    protected void addCustomActions(List<Action> actions) {
        CasaPort cp = (CasaPort) this.getData();
        CasaWrapperModel model = (CasaWrapperModel) cp.getModel();
        if (model.isEditable(cp)) {
            // only add this for soap port...
            if (model.getBindingType(cp).equalsIgnoreCase(SOAP_BINDING)) {
                actions.add(new WSDLEndpointAction());
            }
        } else { // non-editable port
            actions.add(SystemAction.get(CloneWSDLPortAction.class));
        }
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaPort casaPort = (CasaPort) getData();
        if (casaPort == null) {
            return;
        }
        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);

        Node.Property portTypeDefinition = new PortTypeProperty(this); 
        identificationProperties.put(portTypeDefinition);

        PropertyUtils.installEndpointNameProperty(
                identificationProperties, this, casaPort,
                JBIAttributes.ENDPOINT_NAME.getName(),
                "endpointName",            // NOI18N
                NbBundle.getMessage(getClass(), "PROP_EndpointName"),       // NOI18N
                NbBundle.getMessage(getClass(), "PROP_EndpointName"));      // NOI18N
        
        Node.Property<String> componentNameSupport = new PropertySupport.ReadOnly<String>(
                "componentName", // NOI18N
                String.class,
                NbBundle.getMessage(getClass(), "PROP_ComponentName"),  // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return ((CasaWrapperModel)casaPort.getModel()).getBindingComponentName(casaPort); 
            }
        };
        identificationProperties.put(componentNameSupport);


        
        // Add all concrete child properties, as a convenience to the user.
        for (Node child : getChildren().getNodes()) {
            if (child instanceof PortNode) {
                addPortChildrenProperties(sheet, child.getChildren(), isEditable());
                break;
            }
        }
    }
    
    private static void addPortChildrenProperties(Sheet sheet, Children children, boolean bEditable) {
        if (children == null) {
            return;
        }
        Node child;
        for (Node origChild : children.getNodes()) {
            child = bEditable ? origChild : new ReadOnlyFilterNode(origChild);
            Sheet.Set portProperties = new Sheet.Set();
            portProperties.setName(child.getDisplayName());
            sheet.put(portProperties);

            PropertySet[] propertySets = child.getPropertySets();
            if (propertySets != null) {
                for (PropertySet propertySet : propertySets) {
                    portProperties.put(propertySet.getProperties());
                }
            }
            addPortChildrenProperties(sheet, child.getChildren(), bEditable);
        }
    }
    
    
    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(CasaComponent component, CasaNodeFactory factory) {
            super(component, factory);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof Port) {
                Node pn = NodesFactory.getInstance().create((Port) key);
                return new Node[] { pn };
            } else if (key instanceof Binding) {
                Node bn = NodesFactory.getInstance().create((Binding) key);
                return new Node[] { bn };
            }
            return null;
        }
        
        public Object getChildKeys(Object data)  {
            List<Object> children = new ArrayList<Object>();
            CasaPort endpoint = (CasaPort) getData();
            if (endpoint != null) {
                
                // todo: 12/15/06 test code...
                Port p = ((CasaWrapperModel) endpoint.getModel()).getLinkedWSDLPort(endpoint);
                if (p != null) {
                    children.add(p);
                    // TMP: the try/catch is added temporarly to ignore an 
                    // illegal state exception thrown when executing the steps
                    // in BUG #13.
                    // I probably need to fire some event when port/binding/service 
                    // is removed from wsdl model or need to change the order 
                    // that things get deleted in the model. 
                    // -Jun 01/26/07
                    try { 
                        Binding b = p.getBinding().get();
                        if (b != null) {
                            children.add(b);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (endpoint.getConsumes() != null) {
                    children.add(CHILD_ID_CONSUMES);
                }
                if (endpoint.getProvides() != null) {
                    children.add(CHILD_ID_PROVIDES);
                }
            }
            return children;
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    public boolean isEditable(String propertyType) {
        CasaPort port = (CasaPort) getData();
        if (port != null) {
            return getModel().isEditable(port, propertyType);
        }
        return false;
    }

    public boolean isEditable() {
        CasaPort port = (CasaPort) getData();
        if (port != null) {
            return getModel().isEditable(port);
        }
        return false;
    }

    public boolean isDeletable() {
        CasaPort port = (CasaPort) getData();
        if (port != null) {
            return getModel().isDeletable(port);
        }
        return false;
    }
}
