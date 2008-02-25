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

import java.awt.Rectangle;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.HashSet;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.WsitServerConfigAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.WsitClientConfigAction;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaBindingBadges;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


public class CasaBadgeEditAction extends WidgetAction.Adapter {

    private CasaModelGraphScene mScene;
    private Node mEditNode;


    public CasaBadgeEditAction(CasaModelGraphScene scene) {
        mScene = scene;
    }

    private boolean inBadge(WidgetMouseEvent event, CasaBindingBadges.Badge badge, CasaNodeWidgetBinding nodeWidget) {
        Rectangle badgeBounds = nodeWidget.getBadges().getBadgeBoundsForParent(badge, nodeWidget);
        return (badgeBounds.contains(event.getPoint()));
    }

    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        mEditNode = null;

        if (event.getButton () != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }

        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        if (inBadge(event, CasaBindingBadges.Badge.IS_EDITABLE, nodeWidget)) {

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

        } else if (inBadge(event, CasaBindingBadges.Badge.WS_POLICY, nodeWidget)) {

            CasaPort endpoint = (CasaPort) mScene.findObject(widget);
            if (endpoint == null || !mScene.getModel().isEditable(endpoint)) {
                return State.REJECTED;
            }

            mEditNode = mScene.getNodeFactory().createNodeFor(endpoint);
            if (mEditNode == null) {
                return State.REJECTED;
            }

            nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.WS_POLICY, true);

            return State.CONSUMED;

        }
         return State.REJECTED;
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
        if (inBadge(event, CasaBindingBadges.Badge.IS_EDITABLE, nodeWidget)) {

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

        } else if (inBadge(event, CasaBindingBadges.Badge.WS_POLICY, nodeWidget)) {

            nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.WS_POLICY, false);

            // select the port
            CasaPort widgetData = (CasaPort) mScene.findObject(widget);
            Set<CasaComponent> objectsToSelect = new HashSet<CasaComponent>();
            objectsToSelect.add((CasaComponent) widgetData);
            mScene.userSelectionSuggested(objectsToSelect, false);

            // show popup menu
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem serverItem = new JMenuItem();
            JMenuItem clientItem = new JMenuItem();
            Actions.connect(serverItem, (Action) SystemAction.get(WsitServerConfigAction.class), true);
            Actions.connect(clientItem, (Action) SystemAction.get(WsitClientConfigAction.class), true);
            popupMenu.add(serverItem);
            popupMenu.add(clientItem);
            Point point = mScene.convertSceneToView (widget.convertLocalToScene(event.getPoint()));
            popupMenu.show(mScene.getView(), point.x, point.y);
        }

        return State.CONSUMED;
    }
}
