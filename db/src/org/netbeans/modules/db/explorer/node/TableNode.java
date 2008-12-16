/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.db.explorer.node;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.DataWrapper;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.MetadataReadListener;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Rob Englander
 */
public class TableNode extends BaseNode implements SchemaProvider {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/table.gif";
    private static final String FOLDER = "Table"; //NOI18N

    /**
     * Create an instance of TableNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the TableNode instance
     */
    public static TableNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        TableNode node = new TableNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private MetadataElementHandle<Table> tableHandle;
    private final DatabaseConnection connection;

    private TableNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    protected void initialize() {
        tableHandle = getLookup().lookup(MetadataElementHandle.class);

        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            Table table = getTable();
            name = table.getName();
        }
    }

    public Table getTable() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        DataWrapper<Table> wrapper = new DataWrapper<Table>();
        MetadataUtils.readModel(metaDataModel, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Table table = tableHandle.resolve(metaData);
                    wrapper.setObject(table);
                }
            }
        );

        return wrapper.getObject();
    }

    public Schema getSchema() {
        Table table = getTable();
        return table.getParent();
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            AbstractCommand command = spec.createCommandDropTable(getName());
            command.setObjectOwner(MetadataUtils.getSchemaWorkingName(getSchema()));
            command.execute();
        } catch (Exception e) {
        }

        SystemAction.get(RefreshAction.class).performAction(new Node[] { getParentNode() });
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = connection.getConnector();
        return connector.supportsCommand(Specification.DROP_TABLE);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return bundle().getString("ND_Table"); //NOI18N
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.TABLE_FLAVOR) {
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createTableData(connection.getDatabaseConnection(),
                        connection.findJDBCDriver(), getName());
            }
        });
        return result;
    }
}
