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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitconf.api;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.*;
import org.netbeans.modules.websvc.wsitconf.ui.service.ServiceTopComponent;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public final class WSITConfigProvider extends Object {

    private static final Logger logger = Logger.getLogger(WSITEditor.class.getName());

    private static WSITConfigProvider instance;
  
    private WSITConfigProvider() { }

    public static synchronized WSITConfigProvider getDefault() {
        if (instance == null) {
            instance = new WSITConfigProvider();
        }
        return instance;
    }

     /**
     * Returns a WSIT configuration editor, then you may call same methods as WS Attributes editor api does,
     * which means WSEditor.createWSEditorComponent(Node node, JaxWsModel jaxWsModel). This call returns JComponent 
     * with WSIT config UI.
     * This method only returns dialog - there are no OK/Cancel buttons provided - thus it's required that a 
     * caller of this method makes sure appropriate actions are taken on wsdlModel and undomanager for Cancel/Save actions
     */
    public final JComponent getWSITServiceConfig(WSDLModel wsdlModel, UndoManager undoManager, Collection<Binding> bindings, Node node) {
        final ServiceTopComponent stc = new ServiceTopComponent(wsdlModel, undoManager, bindings, node);
        return stc;
    }
    
    /**
     * Should be invoked with same parameters as WSEditor calls are invoked. Returns false if WSIT is not supported,
     * is switched off, an error happened, or WSIT security features are switched off.
     * Is here to enable other parties (e.g. AccessManager) to detect if WSIT is configured, because combinations 
     * don't work well together.
     * @param node 
     * @param jaxWsModel 
     * @return 
     */
    public final boolean isWsitSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        
        //is it a client node?
        Client client = node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = node.getLookup().lookup(Service.class);
        
        Project p = null;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        }

        if (p != null) {
            if (Util.isWsitSupported(p)) {
                try {
                    WSDLModel wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, null, false, null);
                    if (wsdlModel != null) {
                        if (client != null) { //it's a client
                            JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                            if (wscs != null) {
                                WSDLModel serviceWsdlModel = WSITModelSupport.getServiceModelForClient(wscs, client);
                                Collection<Binding> bindings = serviceWsdlModel.getDefinitions().getBindings();
                                for (Binding b : bindings) {
                                    if (SecurityPolicyModelHelper.isSecurityEnabled(b)) {
                                        return true;
                                    }
                                }
                            }
                        } else if (service != null) {
                            Collection<Binding> bindings = wsdlModel.getDefinitions().getBindings();
                            for (Binding b : bindings) {
                                if (SecurityPolicyModelHelper.isSecurityEnabled(b)) {
                                    return true;
                                }
                            }
                        }
                    }
                } catch(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
        return false;        
    }

    /**
     * Should be invoked with same parameters as WSEditor calls are invoked. Returns false if WSIT is not supported,
     * is switched off, or an error happened.
     * Is here to enable other parties (e.g. AccessManager) to detect if WSIT is configured, because combinations 
     * don't work well together.
     * @param node 
     * @param jaxWsModel 
     * @return 
     */
    public final boolean isWsitEnabled(Node node, JaxWsModel jaxWsModel) {
        
        Project p = null;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        }

        if (p != null) {
            if (Util.isWsitSupported(p)) {
                try {
                    WSDLModel wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, null, false, null);
                    if (wsdlModel != null) {
                        return true;
                    }
                } catch(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
        return false;        
    }
    
}
