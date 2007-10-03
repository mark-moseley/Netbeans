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
package org.netbeans.modules.j2ee.websphere6.ui.nodes.actions;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.websphere6.WSDeploymentManager;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.WSManagerNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.awt.HtmlBrowser.URLDisplayer;

/**
 *
 * @author Kirill Sorokin
 */
public class ShowAdminConsoleAction extends CookieAction {
    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1)) {
            return;
        }
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (node instanceof WSManagerNode) {
                try{
                    URL url = new URL(
                            ((WSManagerNode) node).getAdminConsoleURL());
                    
                    URLDisplayer.getDefault().showURL(url);
                } catch (Exception e){
                    return;//nothing much to do
                }
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminConsoleAction.class, "LBL_ShowAdminConsole");
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected Class[] cookieClasses() {
        return new Class[]{};
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1) {
            return false;
        }
        
        boolean running = true;
        
        for (int i = 0; i < nodes.length; i++) {
            Object node = nodes[i].getLookup().lookup(WSManagerNode.class);
            if (!(node instanceof WSManagerNode)) {
                running = false;
                break;
            }

            WSDeploymentManager dm =
                    ((WSManagerNode) node).getDeploymentManager();
            
            // try to get an open socket to the target host/port
            try {
                new Socket(dm.getHost(), new Integer(dm.getPort()).intValue());
                
                running = true;
            } catch (UnknownHostException e) {
                Logger.getLogger("global").log(Level.SEVERE, null, e);
            } catch (IOException e) {
                running = false;
            }
        }
        
        return running;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}