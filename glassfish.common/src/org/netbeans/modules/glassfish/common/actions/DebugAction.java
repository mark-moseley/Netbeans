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
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Debug server action starts the server in the debug mode.
 *
 * @author Peter Williams
 */
public class DebugAction extends NodeAction {
    
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
//        Map<String, String> ip = commonSupport.getInstanceProperties();
//        String title = NbBundle.getMessage(DebugAction.class, "LBL_Debugging", ip.get(GlassfishModule.DISPLAY_NAME_ATTR));
        commonSupport.setEnvironmentProperty(GlassfishModule.JVM_MODE, GlassfishModule.DEBUG_MODE, true);
        commonSupport.startServer(null);
    }

//    private static void performActionImpl(final ServerInstance si) {
//        if (si != null) {
//            RequestProcessor.getDefault().post(new Runnable() {
//                public void run() {
//                    String title = NbBundle.getMessage(DebugAction.class, "LBL_Debugging", si.getDisplayName());
//                    ProgressUI progressUI = new ProgressUI(title, false);
//                    try {
//                        progressUI.start();
//                        si.startDebug(progressUI);
//                    } catch (ServerException ex) {
//                        String msg = ex.getLocalizedMessage();
//                        NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
//                        DialogDisplayer.getDefault().notify(desc);
//                    } finally {
//                        progressUI.finish();
//                    }
//                }
//            });
//        }
//    }

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
    
    private static boolean enableImpl(GlassfishModule commonSupport) {
        return commonSupport.getServerState() == ServerState.STOPPED;
    }

    @Override
    protected boolean asynchronous() { 
        return false; 
    }
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "CTL_DebugAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractOutputAction {
        
        private static final String ICON = 
                "org/netbeans/modules/glassfish/common/resources/debug.png"; // NOI18N
        
        public OutputAction(final GlassfishModule commonSupport) {
            super(commonSupport, NbBundle.getMessage(DebugAction.class, "LBL_DebugOutput"), // NOI18N
                    NbBundle.getMessage(DebugAction.class, "LBL_DebugOutputDesc"), // NOI18N
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
