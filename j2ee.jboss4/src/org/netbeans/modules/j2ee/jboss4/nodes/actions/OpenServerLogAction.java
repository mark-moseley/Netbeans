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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.JBLogWriter;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.nodes.JBManagerNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;

/**
 *
 * @author Libor Kotouc
 */
public class OpenServerLogAction extends NodeAction {
    
    public OpenServerLogAction() {
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            Object node = activatedNode.getLookup().lookup(JBManagerNode.class);
            
            if (!(node instanceof JBManagerNode)) {
                continue;
            }
            
            JBDeploymentManager dm = ((JBManagerNode)node).getDeploymentManager();
            InputOutput io = UISupport.getServerIO(dm.getUrl());
            if (io != null) {
                io.select();
            }
            
            String instanceName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
            JBLogWriter logWriter = JBLogWriter.getInstance(instanceName);
            if (logWriter == null) {
                logWriter = JBLogWriter.createInstance(io, instanceName);
                String serverDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                String logFileName = serverDir + File.separator + "log" + File.separator + "server.log" ; // NOI18N
                File logFile = new File(logFileName);
                if (logFile.exists()) {
                    logWriter.start(logFile);
                }
            }
            else {
                logWriter.refresh();
            }
        }        
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(OpenServerLogAction.class, "LBL_OpenServerLogAction");
    }

    public boolean asynchronous() {
        return false;
    }
    
}
