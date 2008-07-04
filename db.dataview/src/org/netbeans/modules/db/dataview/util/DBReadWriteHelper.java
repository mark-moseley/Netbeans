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
package org.netbeans.modules.db.dataview.util;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.db.dataview.logger.Localizer;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DBReadWriteHelper {

    private static Logger mLogger = Logger.getLogger(DBReadWriteHelper.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    @SuppressWarnings(value = "fallthrough") // NOI18N

    public static Object readResultSet(ResultSet rs, int colType, int index) throws SQLException {
        switch (colType) {
            case Types.BIT:
            case Types.BOOLEAN: {
                boolean bdata = rs.getBoolean(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Boolean(bdata);
                }
            }
            case Types.TIME: {
                Time tdata = rs.getTime(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tdata;
                }
            }
            case Types.DATE: {
                Date ddata = rs.getDate(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return ddata;
                }
            }
            case Types.TIMESTAMP:
            case -100: // -100 = Oracle timestamp
            {
                Timestamp tsdata = rs.getTimestamp(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tsdata;
                }
            }
            case Types.BIGINT: {
                long ldata = rs.getLong(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Long(ldata);
                }
            }
            case Types.DOUBLE: {
                double fdata = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Double(fdata);
                }
            }

            case Types.FLOAT:
            case Types.REAL: {
                float rdata = rs.getFloat(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Float(rdata);
                }
            }
            case Types.DECIMAL:
            case Types.NUMERIC: {
                BigDecimal bddata = rs.getBigDecimal(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return bddata;
                }
            }
            case Types.INTEGER: {
                int idata = rs.getInt(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Integer(idata);
                }
            }
            case Types.SMALLINT: {
                short sidata = rs.getShort(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Short(sidata);
                }
            }
            case Types.TINYINT: {
                byte tidata = rs.getByte(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new Byte(tidata);
                }
            }
            // JDBC/ODBC bridge JDK1.4 brings back -9 for nvarchar columns in
            // MS SQL Server tables.
            // -8 is ROWID in Oracle.
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:
            case -8: {
                String sdata = rs.getString(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return sdata;
                }
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
                byte[] bdata = rs.getBytes(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    Byte[] internal = new Byte[bdata.length];
                    for (int i = 0; i < bdata.length; i++) {
                        internal[i] = new Byte(bdata[i]);
                    }
                    return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.BINARY, true);
                }
            }
            case Types.BLOB: {
                // We always get the BLOB, even when we are not reading the contents.
                // Since the BLOB is just a pointer to the BLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the blob).
                Blob blob = rs.getBlob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // BLOB exists, so try to read the data from it
                byte[] blobData = null;
                if (blob != null) {
                    blobData = blob.getBytes(1, 255);
                }
                Byte[] internal = new Byte[blobData.length];
                for (int i = 0; i < blobData.length; i++) {
                    internal[i] = new Byte(blobData[i]);
                }
                return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.HEX, false);
            }
            case Types.CLOB: {
                // We always get the CLOB, even when we are not reading the contents.
                // Since the CLOB is just a pointer to the CLOB data rather than the
                // data itself, this operation should not take much time (as opposed
                // to getting all of the data in the clob).
                Clob clob = rs.getClob(index);

                if (rs.wasNull()) {
                    return null;
                }
                // CLOB exists, so try to read the data from it
                if (clob != null) {
                    return clob.getSubString(1, 255);
                }
            }
            case Types.OTHER:
            default:
                return rs.getObject(index);
        }
    }

    public static void setAttributeValue(PreparedStatement ps, int index, int jdbcType, Object valueObj) throws DBException {
        Number numberObj = null;

        try {

            if (valueObj == null) {
                ps.setNull(index, jdbcType);
                return;
            }

            switch (jdbcType) {

                case Types.DOUBLE:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.DECIMAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : new BigDecimal(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.BIGINT:
                case Types.NUMERIC:
                    BigDecimal bigDec = new BigDecimal(valueObj.toString());
                    ps.setBigDecimal(index, bigDec);
                    break;

                case Types.FLOAT:
                case Types.REAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                    ps.setFloat(index, numberObj.floatValue());
                    break;

                case Types.INTEGER:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                    ps.setInt(index, numberObj.intValue());
                    break;

                case Types.SMALLINT:
                case Types.TINYINT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Short.valueOf(valueObj.toString());
                    ps.setShort(index, numberObj.shortValue());
                    break;

                case Types.TIMESTAMP:
                    ps.setTimestamp(index, (Timestamp) new TimestampType().convert(valueObj));
                    break;

                case Types.DATE:
                    ps.setDate(index, (Date) new DateType().convert(valueObj));
                    break;

                case Types.TIME:
                    ps.setTime(index, (Time) new TimeType().convert(valueObj));
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                    ps.setBytes(index, valueObj.toString().getBytes());
                    break;

                case Types.LONGVARBINARY:
                case Types.BLOB:
                    byte[] byteval = valueObj.toString().getBytes();
                    ps.setBinaryStream(index, new ByteArrayInputStream(byteval), byteval.length);
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                    ps.setString(index, valueObj.toString());
                    break;

                case Types.CLOB:
                case Types.LONGVARCHAR:
                    String charVal = valueObj.toString();
                    ps.setCharacterStream(index, new StringReader(charVal), charVal.length());
                    break;

                default:
                    ps.setObject(index, valueObj, jdbcType);
            }
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("LOGR002: Invalid Data for {0} type --", jdbcType), e);
            throw new DBException("Invalid Data for " + jdbcType + " type ", e);
        }
    }

    public static Object validate(Object valueObj, DBColumn col) throws DBException {
        int colType = col.getJdbcType();
        if (valueObj == null) {
            return null;
        }
        try {
            switch (colType) {
                case Types.BIT:
                case Types.BOOLEAN:
                    return (valueObj instanceof Boolean) ? valueObj : new Boolean(valueObj.toString());

                case Types.TIMESTAMP:
                    return valueObj instanceof Timestamp ? valueObj : new TimestampType().convert(valueObj);

                case Types.DATE:
                    return valueObj instanceof Date ? valueObj : new DateType().convert(valueObj);

                case Types.TIME:
                    return valueObj instanceof Time ? valueObj : new TimeType().convert(valueObj);

                case Types.BIGINT:
                    return valueObj instanceof Long ? valueObj : new Long(valueObj.toString());

                case Types.DOUBLE:
                    return valueObj instanceof Double ? valueObj : new Double(valueObj.toString());

                case Types.FLOAT:
                case Types.REAL:
                    return valueObj instanceof Float ? valueObj : new Float(valueObj.toString());

                case Types.DECIMAL:
                case Types.NUMERIC:
                    return valueObj instanceof BigDecimal ? valueObj : new BigDecimal(valueObj.toString());

                case Types.INTEGER:
                    return valueObj instanceof Integer ? valueObj : new Integer(valueObj.toString());

                case Types.SMALLINT:
                    return valueObj instanceof Short ? valueObj : new Short(valueObj.toString());

                case Types.TINYINT:
                    return valueObj instanceof Byte ? valueObj : new Byte(valueObj.toString());

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case -9:  //NVARCHAR
                case -8:  //ROWID
                case -15: //NCHAR    
                    if (valueObj.toString().length() > col.getPrecision()) {
                        String colName = col.getQualifiedName();
                        String errMsg = "Too large data \'" + valueObj + "\' for column " + colName;
                        throw new DBException(errMsg);
                    }
                    return valueObj;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.CLOB:
                case Types.BLOB:
                    char[] bytes = valueObj.toString().toCharArray();
                    Byte[] internal = new Byte[bytes.length];
                    for (int i = 0; i < bytes.length; i++) {
                        internal[i] = new Byte((byte) bytes[i]);
                    }
                    return BinaryToStringConverter.convertToString(internal, BinaryToStringConverter.BINARY, true);

                case Types.OTHER:
                default:
                    return valueObj;
            }
        } catch (Exception e) {
            String type = DataViewUtils.getStdSqlType(colType);
            String colName = col.getQualifiedName();
            String errMsg = "Please enter valid data for " + colName + " of " + type + " type";
            mLogger.severe(mLoc.x("LOGR004: Invalid Data for {0} type -- {1}", type, e.getMessage()));
            throw new DBException(errMsg);
        }
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }
}
