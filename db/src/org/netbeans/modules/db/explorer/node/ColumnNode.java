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

import java.util.Collection;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.impl.RemoveColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.netbeans.modules.db.explorer.metadata.MetadataReader;
import org.netbeans.modules.db.explorer.metadata.MetadataReader.DataWrapper;
import org.netbeans.modules.db.explorer.metadata.MetadataReader.MetadataReadListener;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class ColumnNode extends BaseNode implements SchemaProvider, ColumnProvider {
    private static final String COLUMN = "org/netbeans/modules/db/resources/column.gif";
    private static final String PRIMARY = "org/netbeans/modules/db/resources/columnPrimary.gif";
    private static final String INDEX = "org/netbeans/modules/db/resources/columnIndex.gif";
    private static final String FOLDER = "Column"; //NOI18N

    /**
     * Create an instance of ColumnNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ColumnNode instance
     */
    public static ColumnNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ColumnNode node = new ColumnNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name;
    private String icon;
    private MetadataModel metaDataModel;
    private MetadataElementHandle<Column> columnHandle;

    private ColumnNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
    }

    @Override
    public synchronized void refresh() {
        setupNames();
        super.refresh();
    }

    protected void initialize() {
        metaDataModel = getLookup().lookup(MetadataModel.class);
        columnHandle = getLookup().lookup(MetadataElementHandle.class);
        setupNames();
    }

    private void setupNames() {
        Column column = getColumn();
        if (column != null) {
            name = column.getName();
            icon = COLUMN;

            Tuple tuple = column.getParent();
            if (tuple instanceof Table) {
                Table table = (Table)tuple;
                PrimaryKey pkey = table.getPrimaryKey();

                boolean found = false;
                if (pkey != null) {
                    Collection<Column> columns = pkey.getColumns();
                    for (Column c : columns) {
                        if (c.getName().equals(column.getName())) {
                            found = true;
                            icon = PRIMARY;
                            break;
                        }
                    }
                }

                if (!found) {
                    Collection<Index> indexes = table.getIndexes();
                    for (Index index : indexes) {
                        Collection<IndexColumn> columns = index.getColumns();
                        for (IndexColumn c : columns) {
                            if (c.getName().equals(column.getName())) {
                                found = true;
                                icon = INDEX;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public Column getColumn() {
        DataWrapper<Column> wrapper = new DataWrapper<Column>();
        MetadataReader.readModel(metaDataModel, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Column column = columnHandle.resolve(metaData);
                    wrapper.setObject(column);
                }
            }
        );

        return wrapper.getObject();
    }

    public Schema getSchema() {
        Column column = getColumn();
        return (Schema)column.getParent().getParent();
    }

    public Tuple getTuple() {
        Column column = getColumn();
        return column.getParent();
    }

    public int getPosition() {
        Column column = getColumn();
        return column.getPosition();
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = getLookup().lookup(DatabaseConnection.class).getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            RemoveColumn command = spec.createCommandRemoveColumn(getTuple().getName());

            String schema = MetadataReader.getSchemaWorkingName(getSchema());

            command.setObjectOwner(schema);
            command.removeColumn(getName());
            command.execute();
        } catch (Exception e) {
        }

        SystemAction.get(RefreshAction.class).performAction(new Node[] { getParentNode() });
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = getLookup().lookup(DatabaseConnection.class).getConnector();
        return connector.supportsCommand(Specification.REMOVE_COLUMN);
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
        return icon;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        return sheet;
    }
}
