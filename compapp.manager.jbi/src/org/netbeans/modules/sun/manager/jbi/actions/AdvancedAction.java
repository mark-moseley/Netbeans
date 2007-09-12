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
package org.netbeans.modules.sun.manager.jbi.actions;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Advanced action for a JBI Component.
 * 
 * @author jqian
 */
public class AdvancedAction extends NodeAction implements Presenter.Popup {
    
    public String getName() {
        return NbBundle.getMessage(AdvancedAction.class, "LBL_Advanced");  // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(getName());
        
        Action forceShutdownAction = SystemAction.get(ShutdownAction.Force.class);
        // The following hack doesn't seem to be needed any more.
        //((ShutdownAction)forceShutdownAction).clearEnabledState(); // TMP FIX for #108106
        forceShutdownAction.setEnabled(forceShutdownAction.isEnabled());
        
        // Instead of adding Action directly into JMenu, use Actions.connect instead. #~98576
        // menu.add(forceShutdownAction);
        JMenuItem forceShutdownMenuItem = new JMenuItem();
        Actions.connect(forceShutdownMenuItem, forceShutdownAction, true);
        menu.add(forceShutdownMenuItem);

        Action forceUninstallAction = SystemAction.get(UninstallAction.Force.class);
        forceUninstallAction.setEnabled(forceUninstallAction.isEnabled());
        
        // Instead of adding Action directly into JMenu, use Actions.connect instead. #~98576
        // menu.add(forceUninstallAction);
        JMenuItem forceUninstallMenuItem = new JMenuItem();
        Actions.connect(forceUninstallMenuItem, forceUninstallAction, true);
        menu.add(forceUninstallMenuItem);
            
        return menu;
    }

    protected void performAction(Node[] activatedNodes) {
        ;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}
