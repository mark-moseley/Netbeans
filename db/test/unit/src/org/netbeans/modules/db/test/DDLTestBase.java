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

package org.netbeans.modules.db.test;

import org.netbeans.modules.db.test.DBTestBase;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.CreateView;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.TableColumn;

/**
 *
 * @author David
 */
public class DDLTestBase extends DBTestBase {
    private static Logger LOGGER = Logger.getLogger(DDLTestBase.class.getName());

    protected static SpecificationFactory specfactory;
    protected Specification spec;
    protected DriverSpecification drvSpec;

    static {
        try {
            specfactory = new SpecificationFactory();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }


    public DDLTestBase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        try {
            spec = (Specification)specfactory.createSpecification(conn);

            drvSpec = specfactory.createDriverSpecification(
                    spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals(
                    "jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - copied from mainline code
                drvSpec.setMetaData(conn.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());

            drvSpec.setCatalog(conn.getCatalog());
            drvSpec.setSchema(SCHEMA);
        } catch ( SQLException e ) {
            SQLException original = e;
            while ( e != null ) {
                LOGGER.log(Level.SEVERE, null, e);
                e = e.getNextException();
            }

            throw original;
        }
    }
    protected void createBasicTable(String tablename, String pkeyName)
            throws Exception {
        dropTable(tablename);
        CreateTable cmd = spec.createCommandCreateTable(tablename);
        cmd.setObjectOwner(SCHEMA);

        // primary key
        TableColumn col = cmd.createPrimaryKeyColumn(pkeyName);
        col.setColumnType(Types.INTEGER);
        col.setNullAllowed(false);

        cmd.execute();
    }

    protected void createView(String viewName, String query) throws Exception {
        CreateView cmd = spec.createCommandCreateView(viewName);
        cmd.setQuery(query);
        cmd.setObjectOwner(SCHEMA);
        cmd.execute();

        assertFalse(cmd.wasException());
    }

    protected void createSimpleIndex(String tablename,
            String indexname, String colname) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
        xcmd.setIndexName(indexname);

        // *not* unique
        xcmd.setIndexType(new String());

        xcmd.setObjectOwner(SCHEMA);
        xcmd.specifyColumn(fixIdentifier(colname));

        xcmd.execute();
    }

    /**
     * Adds a basic column.  Non-unique, allows nulls.
     */
    protected void addBasicColumn(String tablename, String colname,
            int type, int size) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        AddColumn cmd = spec.createCommandAddColumn(tablename);
        cmd.setObjectOwner(SCHEMA);
        TableColumn col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(type);
        col.setColumnSize(size);
        col.setNullAllowed(true);

        cmd.execute();
        if ( cmd.wasException() ) {
            throw new Exception("Unable to add column");
        }
    }
}
