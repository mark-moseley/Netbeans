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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.mysql.actions;

import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 * Connects to a server
 * 
 * @author David Van Couvering
 */
public class ConnectServerAction extends CookieAction {
    private static final Class[] COOKIE_CLASSES = 
            new Class[] { DatabaseServer.class };
    
    public ConnectServerAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return Utils.getBundle().getString("LBL_ConnectServerAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectServerAction.class);
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return false;
        }
        
        DatabaseServer server = activatedNodes[0].getCookie(DatabaseServer.class);
        
        return server != null && !server.isConnected();
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        DatabaseServer server = activatedNodes[0].getCookie(DatabaseServer.class);

        // Run this on a separate thread so that we don't hang up the AWT 
        // thread if the database server is not responding
        server.reconnect(false, true); // quiet, async
    }
    
    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }
}
