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

package org.netbeans.core.ui.warmup;

import java.awt.EventQueue;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 * Warm-up task for initing context menu
 *
 * @author  Tomas Pavek, Peter Zavadsky
 */
public final class ContextMenuWarmUpTask implements Runnable {

    public void run() {
        // For first context menu.
        org.openide.actions.ActionManager.getDefault().getContextActions();
        new javax.swing.JMenuItem();

        // #30676 ToolsAction popup warm up.
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    warmUpToolsPopupMenuItem();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Warms up tools action popup menu item. */
    private static void warmUpToolsPopupMenuItem() {
        SystemAction toolsAction = SystemAction.get(ToolsAction.class);
        if(toolsAction instanceof ContextAwareAction) {
            // Here is important to create proper lookup
            // to warm up Tools sub actions.
            Lookup lookup = new org.openide.util.lookup.ProxyLookup(
                new Lookup[] {
                    // This part of lookup causes warm up of Node (cookie) actions.
                    new AbstractNode(Children.LEAF).getLookup(),
                    // This part of lookup causes warm up of Callback actions.
                    new TopComponent().getLookup()
                }
            );
            
            Action action = ((ContextAwareAction)toolsAction)
                                .createContextAwareInstance(lookup);
            if(action instanceof Presenter.Popup) {
                JMenuItem toolsMenuItem = ((Presenter.Popup)action)
                                                .getPopupPresenter();
                if(toolsMenuItem instanceof Runnable) {
                    // This actually makes the warm up.
                    // See ToolsAction.Popup impl.
                    ((Runnable)toolsMenuItem).run();
                }
            }
        }
    }
}
