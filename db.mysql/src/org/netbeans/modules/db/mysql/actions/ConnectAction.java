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

import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.DatabaseServer;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 * Connect to a database
 * 
 * @author David Van Couvering
 */
public class ConnectAction extends CookieAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectAction.class.getName());
    private static final Class[] COOKIE_CLASSES = new Class[] {
        Database.class
    };

    public ConnectAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return Utils.getBundle().getString("LBL_ConnectAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectAction.class);
    }
    
    @Override
    public boolean enable(Node[] activatedNodes) {
        return true;
    }


    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return;
        }
        Database model = activatedNodes[0].getCookie(Database.class);
        DatabaseServer server = model.getServer();
        
        String dbname = model.getDbName();

        List<DatabaseConnection> conns =
                DatabaseUtils.findDatabaseConnections(
                    server.getURL(dbname));

        if ( conns.size() == 0 ) {
            ConnectionManager.getDefault().
                showAddConnectionDialogFromEventThread(
                    DatabaseUtils.getJDBCDriver(),
                    server.getURL(dbname),
                    server.getUser(),
                    null);
        } else {
            ConnectionManager.getDefault().showConnectionDialog(conns.get(0));
        }

        // Refresh in case the state of the server changed... (e.g. the connection was lost)
        server.refreshDatabaseList();
    }
}