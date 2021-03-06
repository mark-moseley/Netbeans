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
package org.netbeans.modules.bpel.nodes.actions;

import java.util.concurrent.Callable;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.editors.multiview.DesignerMultiViewElementDesc;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 21 April 2006
 */
public class GoToDiagrammAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    public static final String GOTODIAGRAMM_KEYSTROKE = 
            NbBundle.getMessage(GoToDiagrammAction.class,"ACT_GoToDiagrammAction");// NOI18N
    
    public GoToDiagrammAction() {
        super();
//        putValue(GoToDiagrammAction.ACCELERATOR_KEY,
//                KeyStroke.getKeyStroke(GOTODIAGRAMM_KEYSTROKE));
    }
    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_GoToDiagrammAction"); // NOI18N
    }
    
    
    public ActionType getType() {
        return ActionType.GO_TO_DIAGRAMM;
    }

    //TODO m
    public boolean enable(final Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }
        boolean isEnable = false;

        DataNode dataNode = null;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof BpelNode) {
                isEnable = true;
            }
            if (nodes[i] instanceof DataNode) {
                dataNode = (DataNode)nodes[i];
            }
        }
        
        // temporary hack, tc doesn't have nested mv tc activated nodes
        if (dataNode != null) {
            TopComponent activatedTc = WindowManager.getDefault().getRegistry().getActivated();
            Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
            BpelEntity[] entities = getBpelEntities(activatedNodes);
            isEnable = entities != null && entities.length > 0;
        }
        return isEnable;
    }

    private boolean isDataNode(Node[] nodes) {
        boolean isDataNode = true;
        DataNode dataNode = null;
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof BpelNode) {
                isDataNode = false;
            }
            if (nodes[i] instanceof DataNode) {
                dataNode = (DataNode)nodes[i];
            }
        }
        isDataNode = isDataNode && dataNode != null;
        return isDataNode;
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }

        if (isDataNode(nodes)) {
            nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        } 
        
        BpelEntity[] entities = getBpelEntities(nodes);
        if (entities != null && entities.length > 0) {
            performAction(entities);
        }
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        Util.goToDesign(bpelEntities[0]);
    }

    public boolean isChangeAction() {
        return false;
    }
}
