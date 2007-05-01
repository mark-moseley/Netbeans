/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;

import com.sun.sql.framework.utils.StringUtil;

/**
 * Extends base class to provide fixed-width file implementation of
 * FlatfileBootstrapParser.
 * 
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FixedWidthBootstrapParser implements FlatfileBootstrapParser {

    /** Creates a new default instance of FixedWidthBootstrapParser. */
    public FixedWidthBootstrapParser() {
    }

    public boolean acceptable(FlatfileDBTable table) throws FlatfileDBException {
        File dataFile = new File(table.getLocalFilePath(), table.getFileName());
        BufferedReader reader = null;
        boolean fixedWidth = false;

        try {
            reader = new BufferedReader(new FileReader(dataFile));
            int lastRecLength = 0;
            int recLength = 0;
            for (int count = 0; count < 5; count++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                recLength = line.length();
                if (lastRecLength == 0) {
                    lastRecLength = recLength;
                    recLength = 0;
                } else if (lastRecLength == recLength) {
                    fixedWidth = true;
                    lastRecLength = recLength;
                    recLength = 0;
                } else {
                    fixedWidth = false;
                    break;
                }
            }
        } catch (Exception e) {
            throw new FlatfileDBException("Unable to parse data file: ");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return fixedWidth;
    }

    /**
     * @see org.netbeans.modules.mashup.db.bootstrap.FlatfileBootstrapParser#getFlatfileFields
     */
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException {
        if (table == null || table.getProperties() == null || table.getProperties().size() == 0) {
            return Collections.EMPTY_LIST;
        }

        int recordLength = 0;
        int fieldCount = 0;
        int offset = 0;

        int jdbcType = SQLUtils.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == SQLUtils.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }

        try {
            recordLength = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDRECORDLENGTH)).intValue();
            fieldCount = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDFIELDCOUNT)).intValue();
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }

        try {
            offset = Integer.valueOf(table.getProperty(PropertyKeys.HEADERBYTESOFFSET)).intValue();
        } catch (Exception e) {
            offset = 0;
        }

        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();

        try {
            if (!isFirstLineHeader) {
                final int basicLength = recordLength / fieldCount;
                int remainderLength = basicLength;
                assertRecordLength(recordLength, fieldCount);

                // Append remainder, if any, to last field.
                if ((recordLength - (basicLength % fieldCount)) != 0) {
                    remainderLength += recordLength % fieldCount;
                }

                return generateColumnNames(table, fieldCount, jdbcType, basicLength, remainderLength);
            } else {
                return readHeaderFromFirstLine(table, offset, jdbcType, recordLength);
            }
        } catch (ArithmeticException ae) {
            throw new FlatfileDBException(getErrMessage(fieldCount));
        }
    }

    public void makeGuess(FlatfileDBTable table) throws FlatfileDBException {
        File dataFile = new File(table.getLocalFilePath(), table.getFileName());
        int recLength = 0;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(dataFile));
            String line = reader.readLine();
            if (line != null) {
                recLength = line.length();
            }
        } catch (Exception e) {
            throw new FlatfileDBException("Unable to parse data file: ");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        if (recLength > 1) {
            List colList = readHeaderFromFirstLine(table, 0, Types.VARCHAR, recLength);

            table.setProperty(PropertyKeys.WIZARDFIELDCOUNT, new Integer(colList.size()));
            table.setProperty(PropertyKeys.WIZARDRECORDLENGTH, new Integer(recLength));
        }
    }

    private void assertRecordLength(int recordLength, int fieldCount) throws FlatfileDBException {
        if (recordLength <= 0) {
            throw new FlatfileDBException("Record length must be greater than zero.");
        }

        if (fieldCount > recordLength) {
            throw new FlatfileDBException("Field count exceeds record length.");
        }
    }

    private List generateColumnNames(FlatfileDBTable table, int columnCount, int jdbcType, int baseLength, int remainderLength)
            throws FlatfileDBException {
        FlatfileDBColumn[] columns = getColumns(table);
        List colList = new ArrayList(columns.length);
        for (int i = 1; i <= columnCount; i++) {
            FlatfileDBColumn column = null;
            if (columns != null && i <= columns.length) {
                column = columns[i - 1];
            }

            String columnName = "FIELD_" + i;
            int precision = (i != columnCount) ? baseLength : remainderLength;
            if (column == null) {
                column = new FlatfileDBColumnImpl(columnName, jdbcType, precision, 0, true);
            }

            column.setCardinalPosition(i);
            colList.add(column);
        }
        return colList;
    }

    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private String getErrMessage(int fieldCount) {
        StringBuffer errMsg = new StringBuffer(50);
        if (fieldCount == 0) {
            errMsg.append("Field count must be a non-zero integer value.\n");
        } else {
            // Generic message.
            errMsg.append("Invalid length and/or field count values.\nPlease review and correct as necessary.");
        }
        return errMsg.toString();
    }

    private List readHeaderFromFirstLine(FlatfileDBTable table, int offset, int jdbcType, int recordlLength) throws FlatfileDBException {
        String filename = table.getFileName();
        String encoding = table.getEncodingScheme();
        FlatfileDBColumn[] columns = getColumns(table);
        BufferedReader br = null;
        String repFile = null;
        final int maxCharsToRead = 500;
        List colList = new ArrayList(columns.length);

        try {
            repFile = table.getProperty(PropertyKeys.URL);
            repFile = StringUtil.escapeControlChars(repFile);
            File f = new File(repFile);
            InputStream is = null;
            if(f.exists()) {
                is = new FileInputStream(f);    
            } else {
                is = new URL(repFile).openStream();
            }
            
            br = new BufferedReader(new InputStreamReader(is, encoding), maxCharsToRead * 5);

            char[] headerBytes = new char[recordlLength];

            if (br.skip(offset) != offset) {
                throw new FlatfileDBException("No header found");
            }

            if (br.read(headerBytes) != recordlLength) {
                throw new FlatfileDBException("Unable to read header");
            }

            String headerStr = new String(headerBytes);
            String columnNames[] = headerStr.split("\\W+");

            if (columnNames.length == 0) {
                throw new FlatfileDBException("No header found");
            }

            for (int i = 1; i <= columnNames.length; i++) {
                int precision;
                if (i == columnNames.length) { // remainderLength
                    precision = recordlLength - headerStr.indexOf(columnNames[i - 1]);
                } else if (i == 1) { // first column
                    precision = headerStr.indexOf(columnNames[1]);
                } else {
                    precision = headerStr.indexOf(columnNames[i]) - headerStr.indexOf(columnNames[i - 1]);
                }

                FlatfileDBColumn column = null;
                if (columns != null && i <= columns.length) {
                    column = columns[i - 1];
                }

                String columnName = StringUtil.createColumnNameFromFieldName(columnNames[i - 1]);
                if (column == null) {
                    column = new FlatfileDBColumnImpl(columnName, jdbcType, precision, 0, true);

                } else {
                    column.setName(columnName);
                    column.setPrecision(precision);
                }

                column.setCardinalPosition(i);
                colList.add(column);
            }
            return colList;
        } catch (IOException ioe) {
            throw new FlatfileDBException(ioe.getMessage(), ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }

}
