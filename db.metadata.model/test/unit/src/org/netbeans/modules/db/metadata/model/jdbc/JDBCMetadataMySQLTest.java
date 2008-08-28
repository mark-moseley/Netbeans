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
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class JDBCMetadataMySQLTest extends MetadataTestBase {

    private JDBCMetadata metadata;
    private String defaultCatalogName;

    public JDBCMetadataMySQLTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (! isMySQL()) {
            return;
        }

        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE foo (" +
                "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                "FOO_NAME VARCHAR(16))");
        stmt.executeUpdate("CREATE TABLE bar (" +
                "`i+d` INT NOT NULL PRIMARY KEY, " +
                "foo_id INT NOT NULL, " +
                "bar_name  VARCHAR(16), " +
                "FOREIGN KEY (foo_id) REFERENCES foo(id))");
        metadata = new JDBCMetadata(conn, null);
        defaultCatalogName = getSchema();
    }

    public void testRunReadAction() throws Exception {
        if (! isMySQL()) {
            return;
        }
        Collection<Catalog> catalogs = metadata.getCatalogs();
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals(defaultCatalogName, defaultCatalog.getName());
        assertTrue(catalogs.contains(defaultCatalog));

        Schema schema = defaultCatalog.getDefaultSchema();
        assertTrue(defaultCatalog.getSchemas().contains(schema));
        assertTrue(schema.isSynthetic());
        assertTrue(schema.isDefault());

        Collection<Table> tables = schema.getTables();
        assertNames(new HashSet<String>(Arrays.asList("foo", "bar")), tables);
        Table barTable = schema.getTable("bar");
        assertTrue(tables.contains(barTable));

        Collection<Column> columns = barTable.getColumns();
        assertNames(Arrays.asList("i+d", "foo_id", "bar_name"), columns);
        Column iPlusDColumn = barTable.getColumn("i+d");
        assertTrue(columns.contains(iPlusDColumn));
    }
}
