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
package org.netbeans.modules.xslt.tmap.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.DecoratedTMapComponent;
import org.netbeans.modules.xslt.tmap.nodes.TMapComponentNode;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Vitaly Bychkov
 */
public abstract class TMapAbstractNodeAction extends NodeAction {

    public TMapAbstractNodeAction() {
        myName = getBundleName();
    }

    protected abstract String getBundleName();
    
    public abstract ActionType getType();
    
    protected abstract void performAction(TMapComponent[] tmapComponents);
    
   protected boolean enable(TMapComponent[] tmapComponents) {
        if (tmapComponents == null) {
            return false;
        }
        
        if (tmapComponents.length != 1) {
            return false;
        }
        if (tmapComponents[0] == null) {
            return false;
        }
                
        TMapModel tmapModel = tmapComponents[0].getModel();
        
        if (tmapModel == null) {
            return false;
        }
        
        boolean readonly = !XAMUtils.isWritable(tmapModel);
        
        if (readonly && isChangeAction()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public void performAction(Node[] nodes) {
        final TMapComponent[] tmapComponents = getComponents(nodes);
        
        if (!enable(tmapComponents)) {
            return;
        }
        TMapModel model = getModel(nodes[0]);
        if (model == null) {
            return;
        }
        try {
            model.invoke(new Callable<Object>() {
                public Object call() {
                    performAction(tmapComponents);
                    return null; 
                }
            });
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    
    public boolean enable(final Node[] nodes) {
        if (nodes == null || nodes.length < 1) {
            return false;
        }
        for (Node node : nodes) {
            if (!(node instanceof TMapComponentNode)) {
                return false;
            }
        }
        
        TMapModel model = getModel(nodes[0]);
        // model == null in case dead element
        if (model == null) {
            return false;
        }
        boolean isEnable = false;
        
//        if (model.isIntransaction()) {
            return enable(getComponents(nodes));
//        }
//        try {
//            model.startTransaction();
//            
//            return model.invoke(new Callable<Boolean>() {
//                public Boolean call() throws Exception {
//                    return new Boolean(enable(getComponents(nodes)));
//                }
//            });
//        } catch (Exception ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
//        
//        return false;
    }
    
    public String getName() {
        return myName;
    }
    
    
    public boolean isChangeAction() {
        return true;
    }
    
    
    public TMapModel getModel(Node node) {
        TMapComponent ref = null;
        TMapModel tmapModel = node.getLookup().lookup(TMapModel.class);
        if (tmapModel == null && node instanceof TMapComponentNode) {
            DecoratedTMapComponent decoratedRef = ((TMapComponentNode)node).getReference();
            ref = decoratedRef == null ? null : decoratedRef.getOriginal();
        }
        return getModel(ref);
    }
    
    public TMapModel getModel(TMapComponent tmapComponent) {
        return tmapComponent == null ? null : tmapComponent.getModel();
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected static final TMapComponent[] getComponents(Node[] nodes) {
        List<TMapComponent> components = new ArrayList<TMapComponent>();
        
        for (Node node : nodes) {
            if (node instanceof TMapComponentNode) {
                DecoratedTMapComponent decoratedRef = 
                        ((TMapComponentNode)node).getReference();
                TMapComponent ref = decoratedRef != null 
                        ? decoratedRef.getOriginal() : null;
                if (ref != null) {
                    components.add(ref);
                }
            }
        }
        
        return components !=null && components.size() > 0
                ? components.toArray(new TMapComponent[components.size()]) 
                : null;
    }
    
    private String myName;

}
