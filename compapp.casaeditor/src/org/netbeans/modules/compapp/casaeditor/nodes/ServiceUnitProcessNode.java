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
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.GoToSourceAction;

/**
 *
 * @author jqian
 */
public class ServiceUnitProcessNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ServiceUnitNode.png");     // NOI18N
    
    private static final String CHILD_ID_PROVIDES_LIST = "ProvidesList";        // NOI18N
    private static final String CHILD_ID_CONSUMES_LIST = "ConsumesList";        // NOI18N
    
    private String processName;
    private String filePath;
    
    public ServiceUnitProcessNode(CasaServiceEngineServiceUnit component, 
            String processName, String filePath, CasaNodeFactory factory) {
        super(component, new MyChildren(component, processName, factory), factory);
        this.processName = processName;
        this.filePath = filePath;
    }
    
    public String getProcessName() {
        return processName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    @Override
    protected void addCustomActions(List<Action> actions) {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null && su.isInternal()) {
            actions.add(SystemAction.get(GoToSourceAction.class));
        }
    }

    @Override
    public String getName() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return NbBundle.getMessage(getClass(), "LBL_Process");      // NOI18N
        }
        return super.getName();
    }

    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getName();
            CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
            String decoration = null;
            if (casaSU != null) {
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",        // NOI18N
                        processName);
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";        // NOI18N
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }

    @Override
    protected void setupPropertySheet(Sheet sheet) {
        final CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
        if (casaSU == null) {
            return;
        }        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);
               
        Node.Property<String> descriptionSupport = new PropertySupport.ReadOnly<String>(
                "processName", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ProcessName"),    // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return getProcessName();
            }
        };
        identificationProperties.put(descriptionSupport);   
        
        Node.Property<String> filePathSupport = new PropertySupport.ReadOnly<String>(
                "filePath", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_FilePath"),    // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return getFilePath();
            }
        };
        identificationProperties.put(filePathSupport);        
    }

    
    private static class MyChildren extends CasaNodeChildren {
        
        private String processName;
    
        public MyChildren(CasaComponent component, String processName, CasaNodeFactory factory) {
            super(component, factory);            
            this.processName = processName;
        }
        protected Node[] createNodes(Object key) {
            assert key instanceof String;
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                CasaWrapperModel model = mNodeFactory.getCasaModel();
                if (model != null) {
                    String keyName = (String) key;
                    if (keyName.equals(CHILD_ID_CONSUMES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_consumesList(getConsumeEndpointRefs()) };
                    } else if (keyName.equals(CHILD_ID_PROVIDES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_providesList(getProvideEndpointRefs()) };
                    } 
                }
            }
            return null;
        }
        @Override
        public Object getChildKeys(Object data)  {
            List<String> children = new ArrayList<String>();           
            children.add(CHILD_ID_CONSUMES_LIST);
            children.add(CHILD_ID_PROVIDES_LIST);
            return children;
        }
        private List<CasaConsumes> getConsumeEndpointRefs() {
            List<CasaConsumes> ret = new ArrayList<CasaConsumes>();
            
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                for (CasaConsumes endpointRef : serviceUnit.getConsumes()) {
                    CasaEndpoint endpoint = endpointRef.getEndpoint().get();
                    String pName = endpoint.getProcessName();
                    if (pName != null && pName.equals(processName)) {
                        ret.add(endpointRef);
                    }
                }
            }
            return ret;
        }
        private List<CasaProvides> getProvideEndpointRefs() {
            List<CasaProvides> ret = new ArrayList<CasaProvides>();
            
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                for (CasaProvides endpointRef : serviceUnit.getProvides()) {
                    CasaEndpoint endpoint = endpointRef.getEndpoint().get();
                    String pName = endpoint.getProcessName();
                    if (pName != null && pName.equals(processName)) {
                        ret.add(endpointRef);
                    }
                }
            }
            return ret;
        }
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public boolean isEditable(String propertyType) {
//        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
//        if (su != null) {
//            return getModel().isEditable(su, propertyType);
//        }
        return false;
    }
    
    @Override
    public boolean isDeletable() {
//        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
//        if (su != null) {
//            return getModel().isDeletable(su);
//        }
        return false;
    }
}
