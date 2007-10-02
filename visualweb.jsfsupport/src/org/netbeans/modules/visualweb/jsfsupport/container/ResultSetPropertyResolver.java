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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.sql.*;
import java.util.*;
import javax.faces.*;
import javax.faces.el.*;
import javax.faces.model.*;

//!JOE This *was* a copy of com.sun.jsfcl.data.ResultSetPropertyResolver - but now it contains only
//!JOE design-time behavior, and is no longer similar...  This must stay.

/**
 * This custom Property Resolver handles the special case of resolving ResultSet columns into
 * editable data values.  This also enables binding to a SelectItem array from a ResultSet for
 * filling lists and dropdowns.
 *
 * These expressions are supported:
 *
 * #{...myResultSet.currentRow.COLUMN_NAME}
 * --> binds to the 'COLUMN_NAME' column of the current row of the ResultSet
 *
 * #(...myResultSet.selectItems['COLUMN_NAME'])
 * #(...myResultSet.selectItems['VALUE_COLUMN_NAME,LABEL_COLUMN_NAME'])
 * #(...myResultSet.selectItems['VALUE_COLUMN_NAME,LABEL_COLUMN_NAME,DESC_COLUMN_NAME'])
 * --> binds to an array of SelectItem generated by iterating through the ResultSet
 */
public class ResultSetPropertyResolver extends PropertyResolver {

    public static final String CURRENT_ROW_KEY = "currentRow"; // NOI18N
    public static final String SELECT_ITEMS_KEY = "selectItems"; // NOI18N

    protected PropertyResolver nested;

    public ResultSetPropertyResolver(PropertyResolver nested) {
        this.nested = nested;
    }

    public Object getValue(Object base, Object property) throws EvaluationException,
        PropertyNotFoundException {
//        System.out.println("ResultSetPropertyResolver.get([" + base + "], [" + property + "]) -->");
        if (base instanceof ResultSet) {
            if (CURRENT_ROW_KEY.equals(property)) {
                Object o = new RowData((ResultSet)base);
//                System.out.println("    return [" + o + "]");
                return o;
            }
            if (SELECT_ITEMS_KEY.equals(property)) {
                Object o = new ColumnData((ResultSet)base);
//                System.out.println("    return [" + o + "]");
                return o;
            }
        }
        else if (base instanceof RowData) {
            Object o = ((RowData)base).getData(property.toString());
//            System.out.println("    return [" + o + "]");
            return o;
        }
        else if (base instanceof ColumnData) {
            Object o = ((ColumnData)base).getSelectItems(property.toString());
//            System.out.println("    return [" + o + "]");
            return o;
        }
//        System.out.println("    (deferring to nested)");
        Object o = nested.getValue(base, property);
//        if (o instanceof DesignBean && ((DesignBean)o).getInstance() instanceof ResultSet) {
//            initResultSet((ResultSet)((DesignBean)o).getInstance());
//        }
//        else if (o instanceof ResultSet) {
//            initResultSet((ResultSet)o);
//        }
//        System.out.println("    return [" + o + "]");
        return o;
    }

    public Object getValue(Object base, int index) throws EvaluationException,
        PropertyNotFoundException {
        Object o = nested.getValue(base, index);
//        if (o instanceof ResultSet) {
//            initResultSet((ResultSet)o);
//        }
        return o;
    }

    public void setValue(Object base, Object property, Object value) throws EvaluationException,
        PropertyNotFoundException {
        if (base instanceof RowData) {
            ((RowData) base).setData(property.toString(), value);
            return;
        }
        nested.setValue(base, property, value);
    }

    public void setValue(Object base, int index, Object value) throws EvaluationException,
        PropertyNotFoundException {
        nested.setValue(base, index, value);
    }

    public boolean isReadOnly(Object base, Object property) throws EvaluationException,
        PropertyNotFoundException {
        if (base instanceof ResultSet && (CURRENT_ROW_KEY.equals(property) || SELECT_ITEMS_KEY.equals(property))) {
            return true;
        }
        else if (base instanceof RowData) {
            return false;
        }
        else if (base instanceof ColumnData) {
            return true;
        }
        return nested.isReadOnly(base, property);
    }

    public boolean isReadOnly(Object base, int index) throws EvaluationException,
        PropertyNotFoundException {
        return nested.isReadOnly(base, index);
    }

    public Class getType(Object base, Object property) throws EvaluationException,
        PropertyNotFoundException {
        if (base instanceof ResultSet) {
            if (CURRENT_ROW_KEY.equals(property)) {
                return RowData.class;
            }
            if (SELECT_ITEMS_KEY.equals(property)) {
                return ColumnData.class;
            }
        }
        else if (base instanceof RowData) {
            return ((RowData)base).getDataType(property.toString());
        }
        else if (base instanceof ColumnData) {
            return ArrayList.class;
        }
        return nested.getType(base, property);
    }

    public Class getType(Object base, int index) throws EvaluationException,
        PropertyNotFoundException {
        return nested.getType(base, index);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class RowData {

        protected ResultSet resultSet;
        protected ResultSetMetaData metadata;
        protected ArrayList columnNameList;

        public RowData(ResultSet resultSet) {
            this.resultSet = resultSet;
            try {
                metadata = resultSet.getMetaData();
                columnNameList = new ArrayList();
                int cols = metadata.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    columnNameList.add(metadata.getColumnName(i));
                }
            }
            catch (SQLException x) {
                throw new FacesException(x);
            }
        }

        public Class getDataType(String column) throws PropertyNotFoundException {
            if (!columnNameList.contains(column)) {
                throw new PropertyNotFoundException("Invalid column name: " + column);
            }
            try {
                return Class.forName(metadata.getColumnClassName(columnNameList.indexOf(column) + 1));
            }
            catch (Exception x) {
                return null;
            }
        }

        public Object getData(String column) throws PropertyNotFoundException {
            if (!columnNameList.contains(column)) {
                throw new PropertyNotFoundException("Invalid column name: " + column);
            }
            try {
//                if (Beans.isDesignTime()) {
                    return getFakeData(metadata, column);
//                }
//                else {
//                    initResultSet(resultSet);
//                    return resultSet.getObject(column);
//                }
            }
            catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        public Object setData(String column, Object value) throws PropertyNotFoundException {
            if (!columnNameList.contains(column)) {
                throw new PropertyNotFoundException("Invalid column name: " + column);
            }
            try {
                Object previous;
//                if (!Beans.isDesignTime()) {
//                    initResultSet(resultSet);
//                    previous = resultSet.getObject(column);
//                    if ((previous == null) && (value == null)) {
//                        return previous;
//                    }
//                    else if ((previous != null) && (value != null) &&
//                        previous.equals(value)) {
//                        return previous;
//                    }
//                    resultSet.updateObject(column, value);
//                    return previous;
//                }
//                else {
                    previous = getFakeData(metadata, column);
                    return previous;
//                }
            }
            catch (SQLException e) {
                throw new FacesException(e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class ColumnData {
        protected ResultSet resultSet;
        protected ResultSetMetaData metadata;

        public ColumnData(ResultSet resultSet) {
            this.resultSet = resultSet;
            try {
                this.metadata = resultSet.getMetaData();
            }
            catch (SQLException x) {
                throw new FacesException(x);
            }
        }

        public Object getSelectItems(String columns) {
            /*
             * returns a List of Objects or SelectItems
             *
             * (examples based on PERSON database table):
             *
             *  "NAME" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to NAME's values
             *
             *  "PERSONID,NAME" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to PERSONID's values,
             *  and the 'itemLabel' set to NAME's values
             *
             *  "PERSONID,NAME,JOBTITLE" -->
             *  returns a List filled with SelectItem objects,
             *  with the 'itemValue' set to PERSONID's values,
             *  the 'itemLabel' set to NAME's values,
             *  and the 'itemDescription' set to JOBTITLE's values
             *
             * Any cases that are out-of-scope throw IllegalArgumentException
             */
            String itemValueName = null;
            String itemLabelName = null;
            String itemDescriptionName = null;

            //MBOHM fix 5086833
            //could have internal commas, in say, selectItems['employee.employeeid, employee.firstname || \' , \' || employee.lastname']
            List cols = new ArrayList();
            String col;
            boolean quoteOpen = false;
            int currStart = 0;
            for (int i = 0; i < columns.length(); i++) {
            	char c = columns.charAt(i);
            	if (c == '\'') {
            		quoteOpen = !quoteOpen;
            	}
            	else if (c == ',' && !quoteOpen) {
        			col = columns.substring(currStart, i);
        			if (col.length() > 0) {
        				cols.add(col);
        			}
        			currStart = i + 1;
            	}
            }
            //get the remaining stuff after the last period
            if (currStart < columns.length()) {
            	col = columns.substring(currStart);
            	cols.add(col);
            }
            
            //String[] args = columns.split(","); //NOI18N
            String[] args = (String[])cols.toArray(new String[cols.size()]);
            if (args.length < 1) {
                throw new IllegalArgumentException();
            }
            itemValueName = args[0];
            if (args.length > 1) {
                itemLabelName = args[1];
            }
            if (args.length > 2) {
                itemDescriptionName = args[2];
            }

            ArrayList list = new ArrayList();
//            if (!Beans.isDesignTime()) {
//                try {
//                    initResultSet(resultSet);
//                    int resultSetIndexSave = resultSet.getRow();
//                    resultSet.first();
//                    while (!resultSet.isAfterLast()) {
//                        if (itemLabelName == null) {
//                            list.add(new SelectItem(
//                                resultSet.getObject(itemValueName)
//                                ));
//                        }
//                        else if (itemDescriptionName == null) {
//                            list.add(new SelectItem(
//                                resultSet.getObject(itemValueName),
//                                resultSet.getObject(itemLabelName).toString()
//                                ));
//                        }
//                        else {
//                            list.add(new SelectItem(
//                                resultSet.getObject(itemValueName),
//                                resultSet.getObject(itemLabelName).toString(),
//                                resultSet.getObject(itemDescriptionName).toString()
//                                ));
//                        }
//                        resultSet.next();
//                    }
//                    if (resultSetIndexSave > 0) {
//                        resultSet.absolute(resultSetIndexSave);
//                    }
//                    else {
//                        resultSet.first();
//                    }
//                }
//                catch (SQLException x) {
//                    x.printStackTrace();
//                }
//            }
//            else {
                for (int i = 0; i < 3; i++) {
                    try {
                        if (itemLabelName == null) {
                            list.add(new SelectItem(
                                getFakeData(metadata, itemValueName)
                                ));
                        }
                        else if (itemDescriptionName == null) {
                            list.add(new SelectItem(
                                getFakeData(metadata, itemValueName),
                                getFakeData(metadata, itemLabelName).toString()
                                ));
                        }
                        else {
                            list.add(new SelectItem(
                                getFakeData(metadata, itemValueName),
                                getFakeData(metadata, itemLabelName).toString(),
                                getFakeData(metadata, itemDescriptionName).toString()
                                ));
                        }
                    }
                    catch (SQLException x) {
                    }
                }
//            }

            return list;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

//!JOE - this code has been removed, because we never need to execute the rowset at design-time, we
//!JOE   can always use fake data to represent the rows by looking at the meta-data.
//    public static void initResultSet(ResultSet resultSet) {
//        try {
//            if (resultSet.isBeforeFirst()) {
//                try {
//                    resultSet.first();
//                }
//                catch (SQLException x) {
//                }
//            }
//        }
//        catch (SQLException x) {
//            if (resultSet instanceof RowSet) {
//                try {
//                    ((RowSet)resultSet).execute();
//                    resultSet.first();
//                }
//                catch (SQLException x2) {
//                }
//            }
//        }
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static Object getFakeData(ResultSetMetaData rsmd, String colName) throws SQLException {

        int colIndex = -1;
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (rsmd.getColumnName(i).equals(colName)) {
                colIndex = i;
                break;
            }
        }
        switch (rsmd.getColumnType(colIndex)) {
            case Types.ARRAY:
                return new java.sql.Array() {
                    public Object getArray() {
                        return null;
                    }

                    public Object getArray(long index, int count) {
                        return null;
                    }

                    public Object getArray(long index, int count, Map map) {
                        return null;
                    }

                    public Object getArray(Map map) {
                        return null;
                    }

                    public int getBaseType() {
                        return Types.CHAR;
                    }

                    public String getBaseTypeName() {
                        return "CHAR";
                    }

                    public ResultSet getResultSet() {
                        return null;
                    }

                    public ResultSet getResultSet(long index, int count) {
                        return null;
                    }

                    public ResultSet getResultSet(long index, int count, Map map) {
                        return null;
                    }

                    public ResultSet getResultSet(Map map) {
                        return null;
                    }

		    public void free() {
		    }
                }
                ;
            case Types.BIGINT:

                //return new Long(rowIndex);
                return new Long(123);
            case Types.BINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.BIT:
                return new Boolean(true);
            case Types.BLOB:
                return new javax.sql.rowset.serial.SerialBlob(new byte[] {1,2,3,4,5});
            case Types.BOOLEAN:
                return new Boolean(true);
            case Types.CHAR:

                //return new String(colName + rowIndex);
                return new String("abc");
            case Types.CLOB:
                return new javax.sql.rowset.serial.SerialClob("arbitraryClobData".toCharArray());
            case Types.DATALINK:
                try {
                    return new java.net.URL("http://www.sun.com");
                }
                catch (java.net.MalformedURLException e) {
                    return null;
                }
                case Types.DATE:
                    return new java.sql.Date(new java.util.Date().getTime());
            case Types.DECIMAL:
                return new java.math.BigDecimal(java.math.BigInteger.ONE);
            case Types.DISTINCT:
                return null;
            case Types.DOUBLE:

                //return new Double(rowIndex);
                return new Double(123);
            case Types.FLOAT:

                //return new Double(rowIndex);
                return new Double(123);
            case Types.INTEGER:

                //return new Integer(rowIndex);
                return new Integer(123);
            case Types.JAVA_OBJECT:

                //return new String(colName + "_" + rowIndex);
                return new String("abc");
            case Types.LONGVARBINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.LONGVARCHAR:

                //return new String(colName + "_" + rowIndex);
                return new String("abc");
            case Types.NULL:
                return null;
            case Types.NUMERIC:
                return new java.math.BigDecimal(java.math.BigInteger.ONE);
            case Types.OTHER:
                return null;
            case Types.REAL:

                //return new Float(rowIndex);
                return new Float(123);
            case Types.REF:
                return new java.sql.Ref() {
                    private Object data = new String("abc");
                    public String getBaseTypeName() {
                        return "CHAR";
                    }

                    public Object getObject() {
                        return data;
                    }

                    public Object getObject(Map map) {
                        return data;
                    }

                    public void setObject(Object value) {
                        data = value;
                    }
                }
                ;
            case Types.SMALLINT:

                //return new Short((short)rowIndex);
                return new Short((short)123);
            case Types.STRUCT:
                return new java.sql.Struct() {
                    private String[] data = {
                        "abc", "def", "ghi"};
                    public Object[] getAttributes() {
                        return data;
                    }

                    public Object[] getAttributes(Map map) {
                        return data;
                    }

                    public String getSQLTypeName() {
                        return "CHAR";
                    }
                }
                ;
            case Types.TIME:
                return new java.sql.Time(new java.util.Date().getTime());
            case Types.TIMESTAMP:
                return new java.sql.Timestamp(new java.util.Date().getTime());
            case Types.TINYINT:

                //return new Byte((byte)rowIndex);
                return new Byte((byte)123);
            case Types.VARBINARY:
                return new byte[] {
                    1, 2, 3, 4, 5};
            case Types.VARCHAR:

                //return new String(colName + "_" + rowIndex);
                return new String("abc");
        }
        return null;
    }
}
