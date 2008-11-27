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

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.SQLType;

/**
 *
 * @author David
 */
public final class JDBCUtils {
    private static final Logger LOGGER = Logger.getLogger(JDBCUtils.class.getName());

    private static EnumSet<SQLType> charTypes = EnumSet.of(SQLType.CHAR, SQLType.VARCHAR, SQLType.LONGVARCHAR);
    private static EnumSet<SQLType> dateTypes = EnumSet.of(SQLType.DATE, SQLType.TIME, SQLType.TIMESTAMP);
    private static EnumSet<SQLType> numericTypes = EnumSet.of(SQLType.TINYINT, SQLType.INTEGER, SQLType.BIGINT, SQLType.SMALLINT,
            SQLType.FLOAT, SQLType.DOUBLE, SQLType.REAL, SQLType.NUMERIC, SQLType.DECIMAL);


    /**
     * Get the SQLType for the given java.sql.Type type.
     *
     * @param type the java.sql.Type type specifier
     * @return SQLType.the SQLType for this java.sql.Type, or null if it's not recognized
     */
    public static SQLType getSQLType(int type) {
        switch (type) {
            case Types.BIT: return SQLType.BIT;
            case Types.TINYINT: return SQLType.TINYINT;
            case Types.SMALLINT: return SQLType.SMALLINT;
            case Types.INTEGER: return SQLType.INTEGER;
            case Types.BIGINT: return SQLType.BIGINT;
            case Types.FLOAT: return SQLType.FLOAT;
            case Types.REAL: return SQLType.REAL;
            case Types.DOUBLE: return SQLType.DOUBLE;
            case Types.NUMERIC: return SQLType.NUMERIC;
            case Types.DECIMAL: return SQLType.DECIMAL;
            case Types.CHAR: return SQLType.CHAR;
            case Types.VARCHAR: return SQLType.VARCHAR;
            case Types.LONGVARCHAR: return SQLType.LONGVARCHAR;
            case Types.DATE: return SQLType.DATE;
            case Types.TIME: return SQLType.TIME;
            case Types.TIMESTAMP: return SQLType.TIMESTAMP;
            case Types.BINARY: return SQLType.BINARY;
            case Types.VARBINARY: return SQLType.VARBINARY;
            case Types.LONGVARBINARY: return SQLType.LONGVARBINARY;
            case Types.NULL: return SQLType.NULL;
            case Types.OTHER: return SQLType.OTHER;
            case Types.JAVA_OBJECT: return SQLType.JAVA_OBJECT;
            case Types.DISTINCT: return SQLType.DISTINCT;
            case Types.STRUCT: return SQLType.STRUCT;
            case Types.ARRAY: return SQLType.ARRAY;
            case Types.BLOB: return SQLType.BLOB;
            case Types.CLOB: return SQLType.CLOB;
            case Types.REF: return SQLType.REF;
            case Types.DATALINK: return SQLType.DATALINK;
            case Types.BOOLEAN: return SQLType.BOOLEAN;
            default:
                return null;
        }
    }

    public static boolean isCharType(SQLType type) {
        return charTypes.contains(type);
    }

    public static boolean isDateType(SQLType type) {
        return dateTypes.contains(type);
    }

    public static boolean isNumericType(SQLType type) {
        return numericTypes.contains(type);
    }

    public static Nullable getColumnNullable(int dbmdColumnNullable) {
        switch (dbmdColumnNullable) {
            case DatabaseMetaData.columnNoNulls:
                return Nullable.NOT_NULLABLE;
            case DatabaseMetaData.columnNullable:
                return Nullable.NULLABLE;
            case DatabaseMetaData.columnNullableUnknown:
            default:
                return Nullable.UNKNOWN;
        }
    }

    public static Nullable getProcedureNullable(int dbmdProcedureNullable) {
        switch (dbmdProcedureNullable) {
            case DatabaseMetaData.procedureNoNulls:
                return Nullable.NOT_NULLABLE;
            case DatabaseMetaData.procedureNullable:
                return Nullable.NULLABLE;
            case DatabaseMetaData.procedureNullableUnknown:
            default:
                return Nullable.UNKNOWN;
        }
    }
    
    public static Direction getDirection(short sqlDirection) {
        switch (sqlDirection) {
            case DatabaseMetaData.procedureColumnOut:
                return Direction.OUT;
            case DatabaseMetaData.procedureColumnInOut:
                return Direction.INOUT;
            case DatabaseMetaData.procedureColumnIn:
                return Direction.IN;
            default:
                LOGGER.log(Level.INFO, "Unknown direction value from DatabaseMetadat.getProcedureColumns(): " + sqlDirection);
                return Direction.IN;
        }
    }

}
