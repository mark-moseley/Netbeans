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

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataFactory;

/**
 *
 * @author Andrei Badea
 */
public class JDBCCatalog implements CatalogImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCCatalog.class.getName());

    protected final JDBCMetadata metadata;
    protected final String name;
    protected final boolean _default;
    protected final String defaultSchemaName;

    protected Schema defaultSchema;
    protected Map<String, Schema> schemas;

    public JDBCCatalog(JDBCMetadata metadata, String name, boolean _default, String defaultSchemaName) {
        this.metadata = metadata;
        this.name = name;
        this._default = _default;
        this.defaultSchemaName = defaultSchemaName;
    }

    public final String getName() {
        return name;
    }

    public final boolean isDefault() {
        return _default;
    }

    public final Schema getDefaultSchema() {
        initSchemas();
        return defaultSchema;
    }

    public final Collection<Schema> getSchemas() {
        return initSchemas().values();
    }

    public final Schema getSchema(String name) {
        return MetadataUtilities.find(name, initSchemas());
    }

    @Override
    public String toString() {
        return "JDBCCatalog[name='" + name + "',default=" + _default + "]"; // NOI18N
    }

    protected JDBCSchema createSchema(String name, boolean _default, boolean synthetic) {
        return new JDBCSchema(this, name, _default, synthetic);
    }

    protected void createSchemas() {
        Map<String, Schema> newSchemas = new LinkedHashMap<String, Schema>();
        try {
            ResultSet rs = metadata.getDmd().getSchemas();
            int columnCount = rs.getMetaData().getColumnCount();
            if (columnCount < 2) {
                LOGGER.fine("DatabaseMetaData.getSchemas() not JDBC 3.0-compliant");
            }
            try {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    // #140376: Oracle JDBC driver doesn't return a TABLE_CATALOG column
                    // in DatabaseMetaData.getSchemas().
                    String catalogName = columnCount > 1 ? rs.getString("TABLE_CATALOG") : name; // NOI18N
                    LOGGER.log(Level.FINE, "Read schema {0} in catalog {1}", new Object[] { schemaName, catalogName });
                    if (MetadataUtilities.equals(catalogName, name)) {
                        if (defaultSchemaName != null && MetadataUtilities.equals(schemaName, defaultSchemaName)) {
                            defaultSchema = MetadataFactory.createSchema(createSchema(defaultSchemaName, true, false));
                            newSchemas.put(defaultSchema.getName(), defaultSchema);
                            LOGGER.log(Level.FINE, "Created default schema {0}", defaultSchema);
                        } else {
                            Schema schema = MetadataFactory.createSchema(createSchema(schemaName, false, false));
                            newSchemas.put(schemaName, schema);
                            LOGGER.log(Level.FINE, "Created schema {0}", schema);
                        }
                    }
                }
            } finally {
                rs.close();
            }
            if (newSchemas.isEmpty() && !metadata.getDmd().supportsSchemasInTableDefinitions()) {
                defaultSchema = MetadataFactory.createSchema(createSchema(null, true, true));
                newSchemas.put(defaultSchema.getName(), defaultSchema);
                LOGGER.log(Level.FINE, "Created fallback default schema {0}", defaultSchema);
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        schemas = Collections.unmodifiableMap(newSchemas);
    }

    private Map<String, Schema> initSchemas() {
        if (schemas != null) {
            return schemas;
        }
        LOGGER.log(Level.FINE, "Initializing schemas in {0}", this);
        createSchemas();
        return schemas;
    }

    public final void refreshTable(String tableName) {
        if (defaultSchema != null) {
            ((JDBCSchema) MetadataAccessor.getDefault().getSchemaImpl(defaultSchema)).refreshTable(tableName);
        }
    }

    public final JDBCMetadata getMetadata() {
        return metadata;
    }

    public final String getDefaultSchemaName() {
        return defaultSchemaName;
    }
}
