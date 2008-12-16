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

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.DataWrapper;
import org.netbeans.modules.db.explorer.metadata.MetadataUtils.MetadataReadListener;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Schema;

/**
 *
 * @author Rob Englander
 */
public class SchemaNode extends BaseNode {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/schema.png"; // NOI18N
    private static final String FOLDER = "Schema"; //NOI18N

    /**
     * Create an instance of SchemaNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the SchemaNode instance
     */
    public static SchemaNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        SchemaNode node = new SchemaNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String htmlName = null;

    private MetadataElementHandle<Schema> schemaHandle;
    private final DatabaseConnection connection;

    private SchemaNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    protected void initialize() {
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);

        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            Schema schema = getSchema();
            renderNames(schema);
        }
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
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    private void renderNames(Schema schema) {
        if (schema == null) {
            name = "";
        }

        name = schema.getName();
        if (name == null) {
            name = schema.getParent().getName();
        }

        if (schema != null) {
            if (schema.isDefault()) {
                htmlName = "<b>" + name + "</b>"; // NOI18N
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlName;
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }
}
