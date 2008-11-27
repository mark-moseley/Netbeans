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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.spi.ValueImplementation;

/**
 * This class factors out common methods
 *
 * @author David
 */
public class JDBCValue extends ValueImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCValue.class.getName());

    private final String name;
    private final SQLType type;
    private final int length;
    private final int precision;
    private final short radix;
    private final short scale;
    private final Nullable nullable;

    /**
     * Create a value from a row in getProcedureColumns()
     *
     * @param rs the result set from getProcedureColumns, assumed to be at a valid row
     * @return a newly created JDBCValue instance
     * @throws java.sql.SQLException
     */
    public static JDBCValue createProcedureValue(ResultSet rs) throws SQLException {
        String name = rs.getString("COLUMN_NAME");
        SQLType type = JDBCUtils.getSQLType(rs.getInt("DATA_TYPE"));
        int length = rs.getInt("LENGTH");
        int precision = rs.getInt("PRECISION");
        short scale = rs.getShort("SCALE");
        short radix = rs.getShort("RADIX");
        Nullable nullable = JDBCUtils.getProcedureNullable(rs.getShort("NULLABLE"));

        return new JDBCValue(name, type, length, precision, radix, scale, nullable);
    }

    /**
     * Create a value from a row in DBMD.getColumns()
     *
     * @param rs the result set from getProcedureColumns, assumed to be at a valid row
     * @return a newly created JDBCValue instance
     * @throws java.sql.SQLException
     */
    public static JDBCValue createTableColumnValue(ResultSet rs) throws SQLException {
        String name = rs.getString("COLUMN_NAME");
        SQLType type = JDBCUtils.getSQLType(rs.getInt("DATA_TYPE"));

        int length = 0;
        int precision = 0;

        if (JDBCUtils.isCharType(type)) {
            length = rs.getInt("COLUMN_SIZE");
        } else {
            precision = rs.getInt("COLUMN_SIZE");
        }

        
        short scale = rs.getShort("DECIMAL_DIGITS");
        short radix = rs.getShort("NUM_PREC_RADIX");
        Nullable nullable = JDBCUtils.getColumnNullable(rs.getShort("NULLABLE"));

        return new JDBCValue(name, type, length, precision, radix, scale, nullable);
    }

    public JDBCValue(String name, SQLType type, int length, int precision, short radix, short scale, Nullable nullable) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.precision = precision;
        this.radix = radix;
        this.scale = scale;
        this.nullable = nullable;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public Nullable getNullable() {
        return nullable;
    }

    public int getPrecision() {
        return precision;
    }

    public short getRadix() {
        return radix;
    }

    public short getScale() {
        return scale;
    }

    public SQLType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "name=" + name + ", type=" + type + ", length=" + getLength() + ", precision=" + getPrecision() +
                ", radix=" + getRadix() + ", scale=" + getScale() + ", nullable=" + nullable;
    }

    @Override
    public MetadataElement getParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
