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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaBindingBadges;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;



public class CasaBadgeEditAction extends WidgetAction.Adapter {
    
    private CasaModelGraphScene mScene;
    private Node mEditNode;
    
    
    public CasaBadgeEditAction(CasaModelGraphScene scene) {
        mScene = scene;
    }
    
    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        mEditNode = null;
        
        if (event.getButton () != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }
        
        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        Rectangle badgeBounds = nodeWidget.getBadges().getBadgeBoundsForParent(
                CasaBindingBadges.Badge.IS_EDITABLE, 
                nodeWidget);
        if (!badgeBounds.contains(event.getPoint())) {
            return State.REJECTED;
        }
        
        CasaPort endpoint = (CasaPort) mScene.findObject(widget);
        if (endpoint == null || !mScene.getModel().isEditable(endpoint)) {
            return State.REJECTED;
        }
        
        mEditNode = mScene.getNodeFactory().createNodeFor(endpoint);
        if (mEditNode == null) {
            return State.REJECTED;
        }
        
        nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.IS_EDITABLE, true);
        
        return State.CONSUMED;
    }

    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State dragExit(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked(widget, this);
    }
    
    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State mouseExited(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked(widget, this);
    }
    
    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked (widget, this);
    }
    
    protected boolean isLocked() {
        return mEditNode != null;
    }
    
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        
        final PropertySheet propertySheetPanel = new PropertySheet();
        final Node editNodeRef = mEditNode;
        mEditNode = null;
        
        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.IS_EDITABLE, false);
        
        propertySheetPanel.setNodes(new Node[] { editNodeRef });

        final Object[] options = new Object[] {Constants.CLOSE};
        final DialogDescriptor descriptor = new DialogDescriptor(
                propertySheetPanel,
                NbBundle.getMessage(getClass(), "STR_PROPERTIES", editNodeRef.getDisplayName()),
                true,
                options,
                null, 
                DialogDescriptor.DEFAULT_ALIGN, 
                null,
                null); 
        descriptor.setClosingOptions(options);
                
        
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        
        // The dialog is modal, allow the action chain to continue while
        // we open the dialog later.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dlg.setVisible(true);
            }
        });
        
        return State.CONSUMED;
    }
}
