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

package org.netbeans.modules.glassfish.common.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;


/**
 * Restart action stops the server and then starts it again in the mode it 
 * was running in before (normal or debug).
 *
 * @author sherold
 */
public class RestartAction extends NodeAction {
    
    public static final int RESTART_DELAY = 5000;
    
    public String getName() {
        return NbBundle.getMessage(RestartAction.class, "CTL_RestartAction");
    }
    
    protected void performAction(Node[] activatedNodes) {
        for(Node node : activatedNodes) {
            GlassfishModule commonSupport = 
                    node.getLookup().lookup(GlassfishModule.class);
            if(commonSupport != null) {
                performActionImpl(commonSupport);
            }
        }
    }
    
    private static void performActionImpl(final GlassfishModule commonSupport) {
//        String title = NbBundle.getMessage(RestartAction.class, "LBL_Restarting", si.getDisplayName());
        final Future<OperationState> stopTask = commonSupport.stopServer(null);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    OperationState stopResult = stopTask.get();
                    if(stopResult == OperationState.COMPLETED) {
                        Thread.sleep(RESTART_DELAY);
                        commonSupport.startServer(null);
                    }
                } catch(InterruptedException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                } catch(ExecutionException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        });
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if(activatedNodes != null && activatedNodes.length > 0) {
            for(Node node : activatedNodes) {
                GlassfishModule commonSupport = node.getLookup().lookup(GlassfishModule.class);
                if(commonSupport != null) {
                    result = enableImpl(commonSupport);
                } else {
                    // No server instance found for this node.
                    result = false;
                }
                if(!result) {
                    break;
                }
            }
        }
        return result;
    }
    
    private static boolean enableImpl(final GlassfishModule commonSupport) {
        // !PW FIXME remote servers?
//        if(!commonSupport.canStartServer()) {
//            return false;
//        }
        
        ServerState state = commonSupport.getServerState();
        if(state != ServerState.RUNNING
                // !PW FIXME support other states - debugging, profiling, etc.
//            && state != ServerInstance.STATE_DEBUGGING
//            && state != ServerInstance.STATE_PROFILING
//            && state != ServerInstance.STATE_PROFILER_BLOCKING) {
                ) {
            return false;
        }
        return true;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() { 
        return false; 
    }
    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractOutputAction {
        
        private static final String ICON = 
                "org/netbeans/modules/glassfish/common/resources/restart.png"; // NOI18N
        
        public OutputAction(final GlassfishModule commonSupport) {
            super(commonSupport, NbBundle.getMessage(RefreshAction.class, "LBL_RestartOutput"), // NOI18N
                    NbBundle.getMessage(RefreshAction.class, "LBL_RestartOutputDesc"), // NOI18N
                    ICON);
        }
        
        public void actionPerformed(ActionEvent e) {
            performActionImpl(commonSupport);
        }

        @Override
        public boolean isEnabled() {
            return enableImpl(commonSupport);
        }
        
    }
}
