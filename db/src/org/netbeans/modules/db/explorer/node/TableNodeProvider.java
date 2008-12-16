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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.DataWrapper;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.MetadataReadListener;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander
 */
public class TableNodeProvider extends NodeProvider {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            public TableNodeProvider createInstance(Lookup lookup) {
                TableNodeProvider provider = new TableNodeProvider(lookup);
                return provider;
            }
        };
    }

    private final DatabaseConnection connection;
    private MetadataElementHandle<Schema> schemaHandle;

    private TableNodeProvider(Lookup lookup) {
        super(lookup, new TableComparator());
        connection = getLookup().lookup(DatabaseConnection.class);
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    public Schema getSchema() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        DataWrapper<Schema> wrapper = new DataWrapper<Schema>();
        MetadataUtils.readModel(metaDataModel, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Schema schema = schemaHandle.resolve(metaData);
                    wrapper.setObject(schema);
                }
            }
        );

        return wrapper.getObject();
    }

    @Override
    protected void initialize() {
        
        List<Node> newList = new ArrayList<Node>();

        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            Schema schema = getSchema();
            if (schema != null) {
                Collection<Table> tables = schema.getTables();
                for (Table table : tables) {
                    MetadataElementHandle<Table> handle = MetadataElementHandle.create(table);
                    Collection<Node> matches = getNodes(handle);
                    if (matches.size() > 0) {
                        newList.addAll(matches);
                    } else {
                        NodeDataLookup lookup = new NodeDataLookup();
                        lookup.add(connection);
                        lookup.add(handle);

                        newList.add(TableNode.create(lookup, this));
                    }
                }
            }
        }

        setNodes(newList);
    }

    static class TableComparator implements Comparator<Node> {

        public int compare(Node node1, Node node2) {
            return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
        }

    }
}
