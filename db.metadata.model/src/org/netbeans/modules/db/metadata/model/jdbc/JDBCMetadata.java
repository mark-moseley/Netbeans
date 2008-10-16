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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadata extends MetadataImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCMetadata.class.getName());

    private final Connection conn;
    private final String defaultSchemaName;
    private final DatabaseMetaData dmd;

    protected Catalog defaultCatalog;
    protected Map<String, Catalog> catalogs;

    public JDBCMetadata(Connection conn, String defaultSchemaName) {
        LOGGER.log(Level.FINE, "Creating metadata for default schema ''{0}''", defaultSchemaName);
        this.conn = conn;
        this.defaultSchemaName = defaultSchemaName;
        try {
            dmd = conn.getMetaData();
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            try {
                LOGGER.log(Level.FINE, "Retrieved DMD for product ''{0}'' version ''{1}'', driver ''{2}'' version ''{3}''", new Object[] {
                        dmd.getDatabaseProductName(),
                        dmd.getDatabaseProductVersion(),
                        dmd.getDriverName(),
                        dmd.getDriverVersion()
                });
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, "Exception while logging metadata information", e);
            }
        }
    }

    public final Catalog getDefaultCatalog() {
        initCatalogs();
        return defaultCatalog;
    }

    public final Collection<Catalog> getCatalogs() {
        return initCatalogs().values();
    }

    public final Catalog getCatalog(String name) {
        return MetadataUtilities.find(name, initCatalogs());
    }

    public Schema getDefaultSchema() {
        Catalog catalog = getDefaultCatalog();
        if (catalog != null) {
            return ((JDBCCatalog) MetadataAccessor.getDefault().getCatalogImpl(catalog)).getDefaultSchema();
        }
        return null;
    }

    public final void refresh() {
        LOGGER.fine("Refreshing metadata");
        defaultCatalog = null;
        catalogs = null;
    }

    @Override
    public String toString() {
        return "JDBCMetadata"; // NOI18N
    }

    protected JDBCCatalog createJDBCCatalog(String name, boolean _default, String defaultSchemaName) {
        return new JDBCCatalog(this, name, _default, defaultSchemaName);
    }

    protected void createCatalogs() {
        Map<String, Catalog> newCatalogs = new LinkedHashMap<String, Catalog>();
        try {
            String defaultCatalogName = conn.getCatalog();
            LOGGER.log(Level.FINE, "Default catalog is ''{0}''", defaultCatalogName);
            ResultSet rs = dmd.getCatalogs();
            try {
                while (rs.next()) {
                    String catalogName = rs.getString("TABLE_CAT"); // NOI18N
                    LOGGER.log(Level.FINE, "Read catalog ''{0}''", catalogName);
                    if (MetadataUtilities.equals(catalogName, defaultCatalogName)) {
                        defaultCatalog = createJDBCCatalog(catalogName, true, defaultSchemaName).getCatalog();
                        newCatalogs.put(defaultCatalog.getName(), defaultCatalog);
                        LOGGER.log(Level.FINE, "Created default catalog {0}", defaultCatalog);
                    } else {
                        Catalog catalog = createJDBCCatalog(catalogName, false, null).getCatalog();
                        newCatalogs.put(catalogName, catalog);
                        LOGGER.log(Level.FINE, "Created catalog {0}", catalog);
                    }
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        if (defaultCatalog == null) {
            defaultCatalog = createJDBCCatalog(null, true, defaultSchemaName).getCatalog();
            newCatalogs.put(defaultCatalog.getName(), defaultCatalog);
            LOGGER.log(Level.FINE, "Created fallback default catalog {0}", defaultCatalog);
        }
        catalogs = Collections.unmodifiableMap(newCatalogs);
    }

    private Map<String, Catalog> initCatalogs() {
        if (catalogs != null) {
            return catalogs;
        }
        LOGGER.fine("Initializing catalogs");
        createCatalogs();
        return catalogs;
    }

    public final void refreshTable(String tableName) {
        if (defaultCatalog != null) {
            ((JDBCCatalog) MetadataAccessor.getDefault().getCatalogImpl(defaultCatalog)).refreshTable(tableName);
        }
    }

    public final Connection getConnection() {
        return conn;
    }

    public final DatabaseMetaData getDmd() {
        return dmd;
    }
}
