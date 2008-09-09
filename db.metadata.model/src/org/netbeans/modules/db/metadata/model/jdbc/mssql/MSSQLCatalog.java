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

package org.netbeans.modules.db.metadata.model.jdbc.mssql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.MetadataUtilities;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;

/**
 *
 * @author Andrei Badea
 */
public class MSSQLCatalog extends JDBCCatalog {

    private static final Logger LOGGER = Logger.getLogger(MSSQLCatalog.class.getName());

    public MSSQLCatalog(MSSQLMetadata metadata, String name, boolean _default, String defaultSchemaName) {
        super(metadata, name, _default, defaultSchemaName);
    }

    @Override
    public String toString() {
        return "MSSQLCatalog[name='" + getName() + "']"; // NOI18N
    }

    @Override
    protected void createSchemas() {
        Map<String, Schema> newSchemas = new LinkedHashMap<String, Schema>();
        try {
            ResultSet rs = getJDBCMetadata().getDmd().getSchemas();
            try {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    // #141598: the MS SQL 2005 and jTDS drivers return null for the catalog name, and they
                    // only return the schemas in the default catalog.
                    LOGGER.log(Level.FINE, "Read schema ''{0}''", schemaName);
                    if (defaultSchemaName != null && MetadataUtilities.equals(schemaName, defaultSchemaName)) {
                        defaultSchema = createJDBCSchema(defaultSchemaName, true, false).getSchema();
                        newSchemas.put(defaultSchema.getName(), defaultSchema);
                        LOGGER.log(Level.FINE, "Created default schema {0}", defaultSchema);
                    } else {
                        Schema schema = createJDBCSchema(schemaName, false, false).getSchema();
                        newSchemas.put(schemaName, schema);
                        LOGGER.log(Level.FINE, "Created schema {0}", schema);
                    }
                }
            } finally {
                rs.close();
            }
            // Schemas always supported, so no need to try to create a synthetic schema.
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        schemas = Collections.unmodifiableMap(newSchemas);
    }
}
