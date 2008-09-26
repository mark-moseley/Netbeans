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

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.openide.nodes.*;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Resfresh action refreshes the server state.
 *
 * @author  nn136682
 */
public class RefreshAction extends NodeAction {
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "LBL_Refresh");
    }
    
    protected void performAction(Node[] nodes) {
        performActionImpl(nodes);
    }
    
    protected boolean enable(Node[] nodes) {
        return enableImpl(nodes);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() { 
        return false; 
    }
    
    // private helper methods -------------------------------------------------

    private static void performActionImpl(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            performActionImpl(si);
        }
    }

    private static void performActionImpl(final ServerInstance si) {
        if (si != null) {
            si.refresh();
        }
    }

    private static boolean enableImpl(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            if (!enableImpl(si)) {
                return false;
            }
        }
        return true;
    }

    private static boolean enableImpl(final ServerInstance si) {
        if (si == null || si.getServerState() == ServerInstance.STATE_WAITING) {
            return false;
        }
        return true;
    }

    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ServerInstance.StateListener {
        
        private static final String ICON = 
                "org/netbeans/modules/j2ee/deployment/impl/ui/resources/refresh.png"; // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private final ServerInstance instance;
        
        public OutputAction(ServerInstance instance) {
            super(NbBundle.getMessage(DebugAction.class, "LBL_RefreshOutput"),
                  new ImageIcon(ImageUtilities.loadImage(ICON)));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DebugAction.class, "LBL_RefreshOutputDesc"));
            this.instance = instance;
            
            // start listening to changes
            instance.addStateListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            performActionImpl(instance);
        }

        public boolean isEnabled() {
            return enableImpl(instance);
        }
        
        // ServerInstance.StateListener implementation --------------------------
        
        public void stateChanged(final int oldState, final int newState) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    firePropertyChange(
                        PROP_ENABLED, 
                        null,
                        isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
}
