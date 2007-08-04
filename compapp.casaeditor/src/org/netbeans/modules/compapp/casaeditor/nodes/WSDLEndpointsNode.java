/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddWSDLPortsAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Josh Sandusky
 */
public class WSDLEndpointsNode extends CasaNode {

    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/WSDLEndpointsNode.png");   // NOI18N

    public WSDLEndpointsNode(List<CasaPort> data, CasaNodeFactory factory) {
        super(data, new MyChildren(data, factory), factory);
    }


    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_WSDLEndpoints");        // NOI18N
    }

    @Override
    protected void addCustomActions(List<Action> actions) {
        actions.add(SystemAction.get(AddWSDLPortsAction.class));
    }

    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(Object data, CasaNodeFactory factory) {
            super(data, factory);
        }
        protected Node[] createNodes(Object key) {
            assert key instanceof CasaComponent;
            if (key instanceof CasaPort) {
                return new Node[] { mNodeFactory.createNodeFor((CasaPort) key) };
            }
            return null;
        }
    }

    public Image getIcon(int type) {
        return ICON;
    }

    public Image getOpenedIcon(int type) {
        return ICON;
    }
}
