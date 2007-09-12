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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.ConnectionState;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.util.NbBundle;

/**
 *
 * @author Josh Sandusky
 */
public class NodeDeleteAction extends NodeAbstractAction {
    
    
    public NodeDeleteAction(CasaNode node) {
        super(NbBundle.getMessage(NodeDeleteAction.class, "NAME_Delete"), node);        // NOI18N
    }
    
    
    public void actionPerformed(ActionEvent e) {
        List<CasaComponent> widgetsToDelete = new ArrayList<CasaComponent>();
        Object data = getData();
        if (data != null && data instanceof CasaComponent) {
            widgetsToDelete.add((CasaComponent)data);
        }
        delete(getModel(), widgetsToDelete);
    }
    
    public static void delete(CasaWrapperModel model, List<CasaComponent> objectsToDelete) {
        DeleteVisitorCasa deleterCasa = new DeleteVisitorCasa(model);
        for (CasaComponent object : objectsToDelete) {
            object.accept(deleterCasa);
        }
    }
    
    
    private static class DeleteVisitorCasa extends CasaComponentVisitor.Default {
        
        private CasaWrapperModel mModel;
        
        public DeleteVisitorCasa(CasaWrapperModel model) {
            mModel = model;
        }
        
        public void visit(CasaConnection connection) {
            // Ensure the connection is not already deleted.
            String state = connection.getState();
            if (
                    connection.isInDocumentModel() && 
                    !ConnectionState.DELETED.getState().equals(state)) {
                mModel.removeConnection(connection);
            }
        }
        
        public void visit(CasaConsumes consumes) {
            // Ensure the endpoint is not already deleted.
            if (consumes.isInDocumentModel()) {
                mModel.removeEndpoint(consumes);
            }
        }
        
        public void visit(CasaProvides provides) {
            // Ensure the endpoint is not already deleted.
            if (provides.isInDocumentModel()) {
                mModel.removeEndpoint(provides);
            }
        }
        
        public void visit(CasaPort port) {
            mModel.removeCasaPort(port);
        }
        
        public void visit(CasaServiceEngineServiceUnit su) {
            mModel.removeServiceEngineServiceUnit(su); 
        }
    }
}
