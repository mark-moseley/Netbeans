/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;

/**
 *
 * @author jawed
 */
public class DataViewDBTableTest extends NbTestCase {
    
    Collection<DBTable> tables;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DataViewDBTableTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewDBTableTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn=null;
    }

    public static DBTable getTable(DataView dv, int index){
        return dv.getDataViewDBTable().geTable(index);
    }
    /**
     * Test of geTable method, of class DataViewDBTable.
     */
    public void testDataViewDBTableMethods() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        DBTable result = instance.geTable(0);
        assertNotNull(result);
        assertEquals(11, instance.getColumnCount());
    }

    /**
     * Test of geTableCount method, of class DataViewDBTable.
     */
    public void testGeTableCount() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        int expResult = 1;
        int result = instance.geTableCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasOneTable method, of class DataViewDBTable.
     */
    public void testHasOneTable() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        boolean expResult = true;
        boolean result = instance.hasOneTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFullyQualifiedName method, of class DataViewDBTable.
     */
    public void testGetFullyQualifiedName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        String expResult = "SIMPLETABLE";
        String result = instance.getFullyQualifiedName(0);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnType method, of class DataViewDBTable.
     */
    public void testGetColumnType() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        int expResult = 12;
        int result = instance.getColumnType(2);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnName method, of class DataViewDBTable.
     */
    public void testGetColumnName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        String expResult = "DATEC";
        String result = instance.getColumnName(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of getQualifiedName method, of class DataViewDBTable.
     */
    public void testGetQualifiedName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        String expResult = "TINYINTC";
        String result = instance.getQualifiedName(0);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnCount method, of class DataViewDBTable.
     */
    public void testGetColumnCount() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        int expResult = 11;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumns method, of class DataViewDBTable.
     */
    public void testGetColumns() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        Map result = instance.getColumns();
        assertEquals(11, result.size());
    }

    /**
     * Test of getColumnToolTips method, of class DataViewDBTable.
     */
/*    public void testGetColumnToolTips() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        String expResult ="<html> <table border=0 cellspacing=0 cellpadding=0 ><tr> <td>&nbsp;Name</td> " +
                "<td> &nbsp; : &nbsp; <b>TINYINTC</b> </td> </tr><tr> <td>&nbsp;Type</td> <td> &nbsp; : &nbsp; " +
                "<b>INTEGER</b> </td> </tr><tr> <td>&nbsp;Precision</td> <td> &nbsp; : &nbsp; <b>10</b> </td>" +
                " </tr><tr> <td>&nbsp;PK</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr></table> </html>";
        String[] result = instance.getColumnToolTips();
        assertEquals(expResult, result[0]);
    }*/

}
