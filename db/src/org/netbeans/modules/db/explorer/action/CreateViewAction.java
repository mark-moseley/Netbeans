/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * "Portions Copyrighted [year] [schemaName of copyright owner]"
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.AddViewDialog;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class CreateViewAction extends BaseAction {

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }

        boolean enabled = false;
        DatabaseConnection dbconn = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

        if (dbconn != null) {
            Connection conn = dbconn.getConnection();
            try {
                if (conn != null) {
                    enabled = !conn.isClosed();
                }
            } catch (SQLException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return enabled;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateViewAction.class);
    }

    public void performAction (Node[] activatedNodes) {
        final BaseNode node = activatedNodes[0].getLookup().lookup(BaseNode.class);
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    perform(node);
                }
            }
        );
    }

    private void perform(final BaseNode node) {
        DatabaseConnection connection = node.getLookup().lookup(DatabaseConnection.class);

        String schemaName = findSchemaWorkingName(node.getLookup());

        try {
            boolean viewsSupported = connection.getConnector().getDriverSpecification(schemaName).areViewsSupported();
            if (!viewsSupported) {
                String message = NbBundle.getMessage (CreateViewAction.class, "MSG_ViewsAreNotSupported", // NOI18N
                        connection.getConnection().getMetaData().getDatabaseProductName().trim());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }

            Specification spec = connection.getConnector().getDatabaseSpecification();

            boolean viewAdded = AddViewDialog.showDialogAndCreate(spec, schemaName);
            if (viewAdded) {
                SystemAction.get(RefreshAction.class).performAction(new Node[]{node});
            }
        } catch(Exception exc) {
            DbUtilities.reportError(NbBundle.getMessage (CreateViewAction.class, "ERR_UnableToCreateView"), exc.getMessage()); // NOI18N
        }
     }

    @Override
    public String getName() {
        return NbBundle.getMessage (CreateViewAction.class, "AddView"); // NOI18N
    }
}
