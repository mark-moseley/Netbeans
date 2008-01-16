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
package org.netbeans.modules.sql.framework.model.visitors;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.axiondb.AxionException;
import org.axiondb.parser.AxionDateTimeFormatParser;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.PhysicalTable;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.model.impl.ValidationInfoImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.openide.util.NbBundle;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;
import org.netbeans.modules.sql.framework.model.Index;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLValidationVisitor implements SQLVisitor {

    private static transient final Logger mLogger = LogUtil.getLogger(SQLValidationVisitor.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static final HashSet<String> DATE_FORMAT_OPS = new HashSet<String>();
    

    static {
        DATE_FORMAT_OPS.add("isvaliddatetime");
        DATE_FORMAT_OPS.add("chartodate");
        DATE_FORMAT_OPS.add("datetochar");
    }
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final String LOG_CATEGORY = SQLValidationVisitor.class.getName();
    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    /**
     * Indicates whether the given List of ValidationInfo instances contains at least one
     * validation error.
     *
     * @param valInfoList Collection of ValidationInfo instances to be evaluated.
     * @return true if at least one item in <code>valInfoList</code> is an error.
     */
    public boolean hasErrors(Collection valInfoList) {
        boolean ret = false;

        if (valInfoList != null) {
            Iterator itr = valInfoList.iterator();
            ValidationInfo vInfo = null;

            while (itr.hasNext()) {
                vInfo = (ValidationInfo) itr.next();
                if (vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public void visit(SourceTable sourceTable) {
        // validate extraction condition
        SQLCondition extCondition = sourceTable.getExtractionCondition();
        if (extCondition != null) {
            if (!extCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(sourceTable.getQualifiedName(), "ERROR_extraction_condition_invalid");
                ValidationInfo validationInfo = new ValidationInfoImpl(extCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            } else {
                visit(extCondition);
            }
        }

        // FIXME: This condition when reported and user tries to
        // edit it by double clicking then extraction condition content is shown
        // validate data validation condition
        SQLCondition dataValidationCondition = sourceTable.getDataValidationCondition();
        if (dataValidationCondition != null) {
            if (!dataValidationCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(sourceTable.getQualifiedName(), "ERROR_datavalidation_condition_invalid");
                ValidationInfo validationInfo = new ValidationInfoImpl(dataValidationCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            } else {
                visit(dataValidationCondition);
            }
        }
    }

    public void visit(SQLCaseOperator operator) {
        visitExpression(operator, true);
    }

    public void visit(SQLCondition condition) {
        // If there is no condition then do not validate
        if (!condition.isConditionDefined()) {
            return;
        }

        // If the resulting expression does not form a valid condition, then raise an
        // error.
        if (!condition.isValid()) {
            String error = NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_condition_invalid");
            ValidationInfo info = new ValidationInfoImpl(null, error, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(info);
        }

        Collection allObjects = condition.getAllObjects();
        List expObjects = SQLObjectUtil.getAllExpressionObjects(allObjects);
        // Validate all other objects in objectMap
        validate(allObjects, expObjects);
    }

    public void visit(SQLDefinition definition) {
        Iterator it = definition.getTargetTables().iterator();
        boolean oracleTableGroupByUse = false;
        final int execStrategy = definition.getExecutionStrategyCode().intValue();
        if ((execStrategy == SQLDefinition.EXECUTION_STRATEGY_STAGING) && definition.requiresPipelineProcess()) {
            String desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Staging_mode_not_allowed");
            ValidationInfo validationInfo = new ValidationInfoImpl(definition, desc, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(validationInfo);
        }

        List<PhysicalTable> srcTables = new ArrayList<PhysicalTable>();
        List<PhysicalTable> trgtTables = new ArrayList<PhysicalTable>();
        while (it.hasNext()) {
            TargetTable targetTable = (TargetTable) it.next();
            final String identifier = targetTable.getQualifiedName();
            final int stmtType = targetTable.getStatementType();
            String dbType = targetTable.getParent().getConnectionDefinition().getDBType();
            final String jdbcUrl = targetTable.getParent().getConnectionDefinition().getConnectionURL();
            boolean jdbcEway = isJdbcOrUnknownDB(dbType, jdbcUrl);

            boolean oracleTargetTableWithCondition = false;
            boolean oracleSourceTable = false;

            // Check for Unknown DB and UPDATE and MERGE statement usage.
            if (jdbcEway) {
                if ((stmtType == SQLConstants.INSERT_UPDATE_STATEMENT) && ((execStrategy == SQLDefinition.EXECUTION_STRATEGY_BEST_FIT) || (execStrategy == SQLDefinition.EXECUTION_STRATEGY_STAGING))) {
                    String desc = buildErrorMessageWithObjectIdentifier(identifier, "WARN_merge_may_not_be_supported");
                    ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(validationInfo);
                }
            }

            targetTable.visit(this);

            trgtTables.add(PhysicalTable.getPhysicalTable(targetTable));
            try {
                srcTables.addAll(PhysicalTable.getPhysicalTableList(targetTable.getSourceTableList()));
            } catch (BaseException e) {
                String desc = NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_targettable_not_mapped");
                ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }

            if ((targetTable.getJoinCondition() != null) && (targetTable.getJoinCondition().isConditionDefined())) {
                dbType = targetTable.getParent().getConnectionDefinition().getDBType();
                dbType = (dbType != null) ? dbType.toUpperCase() : "";
                if (dbType.indexOf(DBConstants.ORACLE_STR) >= 0) {
                    oracleTargetTableWithCondition = true;
                }
            }

            try {
                Iterator srcItr = targetTable.getSourceTableList().iterator();
                SourceTable srcTbl = null;
                while (srcItr.hasNext()) {
                    srcTbl = (SourceTable) srcItr.next();
                    dbType = srcTbl.getParent().getConnectionDefinition().getDBType();
                    dbType = (dbType != null) ? dbType.toUpperCase() : "";
                    if (dbType.indexOf(DBConstants.ORACLE_STR) >= 0) {
                        oracleSourceTable = true;
                        break;
                    }
                }
            } catch (BaseException ex) {
                // ignore
            }

            if ((targetTable.getSQLGroupBy() != null) && (targetTable.getSQLGroupBy().getColumns().size() > 0)) {
                if (oracleTargetTableWithCondition || oracleSourceTable) {
                    oracleTableGroupByUse = true;
                }
            }
        }

        for (it = trgtTables.iterator(); it.hasNext();) {
            PhysicalTable pt = (PhysicalTable) it.next();
            if (srcTables.contains(pt)) {
                String desc = NbBundle.getMessage(SQLValidationVisitor.class, "WARNING_source_used_as_target", pt.getName());
                ValidationInfo validationInfo = new ValidationInfoImpl(definition, desc, ValidationInfo.VALIDATION_WARNING);
                validationInfoList.add(validationInfo);
            }
        }

        // Flag error when one of the source tables is Oracle, Group By clause
        // is used and Pipeline is required.
        // Since we use Oracle forward only ResultSet to avoid OutOfMemoryError,
        // as its scroll-able result set caches data at the client VM.
        // Pipeline GROUP BY processing needs scroll-able result set.
        if ((definition.requiresPipelineProcess() || (definition.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_PIPELINE)) && (oracleTableGroupByUse)) {
            String desc = NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_can_not_use_ora_grp_by_in_pipeline");
            ValidationInfo validationInfo = new ValidationInfoImpl(definition, desc, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(validationInfo);
        }
    }

    public void visit(SQLFilter filter) {
        visitExpression(filter, true);

        if (!filter.isValid()) {
            String descriptor = NbBundle.getMessage(SQLValidationVisitor.class, "FORMAT_parenthesis_following", filter.getDisplayName(), filter.toString());
            String message = buildErrorMessageWithObjectIdentifier(descriptor, "ERROR_predicate_invalid");
            ValidationInfo expValidationInfo = new ValidationInfoImpl(filter, message, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        }

        // Check if the next SQLFilter clause, if any, has a prefix.
        if (filter.getNextFilter() != null && StringUtil.isNullString(filter.getNextFilter().getPrefix())) {
            String descriptor = NbBundle.getMessage(SQLValidationVisitor.class, "FORMAT_parenthesis_following", filter.getNextFilter().getDisplayName(), filter.toString());
            String message = buildErrorMessageWithObjectIdentifier(descriptor, "ERROR_predicate_missing_prefix");

            ValidationInfo expValidationInfo = new ValidationInfoImpl(filter, message, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        }
    }

    public void visit(SQLGenericOperator operator) {
        if (operator.hasVariableArgs() && operator.getInputObjectMap().size() == 0) {
            String desc = buildErrorMessageWithObjectIdentifier(operator.getDisplayName(), "ERROR_input_not_linked");
            ValidationInfo expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        } else {
            visitExpression(operator, true);
        }
    }

    public void visit(SQLJoinOperator operator) {
        doJoinConditionValidation(operator);
        visitExpression(operator, true);
    }

    public void visit(SQLJoinView joinView) {
        SQLJoinOperator rJoin = joinView.getRootJoin();
        if (rJoin != null) {
            rJoin.visit(this);
        }
    }

    public void visit(SQLPredicate operator) {
        visitExpression(operator, true);
    }

    public void visit(SQLWhen when) {
    }

    private boolean isJdbcOrUnknownDB(String dbType, String jdbcUrl) {
        boolean unknownDB = true;
        if (dbType != null) {
            dbType = dbType.toUpperCase();
            for (int i = 0; i < DBConstants.SUPPORTED_DB_TYPE_STRINGS.length; i++) {
                if ((dbType.indexOf(DBConstants.SUPPORTED_DB_TYPE_STRINGS[i])) > -1) {
                    unknownDB = false;
                    break;
                }
            }

            // Now check whether connecting to known DB thru JDBC eWay
            if ((unknownDB) && (jdbcUrl != null)) {
                jdbcUrl = jdbcUrl.toLowerCase();
                for (int i = 0; i < DBConstants.SUPPORTED_DB_URL_PREFIXES.length; i++) {
                    if ((jdbcUrl.indexOf(DBConstants.SUPPORTED_DB_URL_PREFIXES[i])) > -1) {
                        unknownDB = false;
                        break;
                    }
                }
            }
        }
        return unknownDB;
    }

    public void visit(TargetTable targetTable) {
        final String identifier = targetTable.getQualifiedName();
        final int stmtType = targetTable.getStatementType();

        Collection<String> uniqueIndexColumns = new HashSet<String>();
        List<Index> indexes = targetTable.getIndexes();
        Index index = null;

        if (indexes != null) {
            Iterator<Index> indexItr = indexes.iterator();
            while (indexItr.hasNext()) {
                index = indexItr.next();
                if (index.isUnique()) {
                    uniqueIndexColumns.addAll(index.getColumnNames());
                }
            }
        }

        int nonNullColumns = findColumnErrors(targetTable, identifier, stmtType, uniqueIndexColumns);
        validateGroupBy(targetTable, stmtType);
        validateTargetTableCondition(targetTable, identifier, stmtType);

        // check for column maps for non-delete statements
        if (stmtType != SQLConstants.DELETE_STATEMENT) {
            if (nonNullColumns == 0) {
                String desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_targettable_not_mapped");
                ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }
        }

        // do join view validation
        SQLJoinView joinView = targetTable.getJoinView();
        if (joinView != null) {
            joinView.visit(this);
        }
        validateSourceTableCondition(targetTable);

        // validate truncate before load option
        if (targetTable.isTruncateBeforeLoad()) {
            if (stmtType == SQLConstants.INSERT_STATEMENT) {
                ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, "Target table " + targetTable.getFullyQualifiedName() + " will be truncated before loading, use this option if you want to refresh the data", ValidationInfo.VALIDATION_WARNING);
                validationInfoList.add(validationInfo);
            } else {
                ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, "Can't truncate target table " + targetTable.getFullyQualifiedName() + " for the selected statement type -> " + targetTable.getStrStatementType(), ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }
        }
    }

    private String buildErrorMessageWithObjectIdentifier(String identifier, String errorKey) {
        return buildErrorMessageWithObjectIdentifiers(identifier, errorKey, EMPTY_OBJECT_ARRAY);
    }

    private String buildErrorMessageWithObjectIdentifiers(String identifier, String errorKey, Object[] errorParams) {
        String errorMessage = NbBundle.getMessage(SQLValidationVisitor.class, errorKey, errorParams);
        return NbBundle.getMessage(SQLValidationVisitor.class, "FORMAT_descriptor_message", identifier, errorMessage);
    }

    private void doJoinConditionValidation(SQLJoinOperator operator) {
        // If join condition is not defined then warn user about a Cartesian join being
        // created
        String identifier = "";
        SQLJoinView joinView = (SQLJoinView) operator.getParentObject();
        if (joinView != null) {
            identifier = joinView.getQualifiedName();
        }

        SQLCondition jCondition = operator.getJoinCondition();
        if (!jCondition.isConditionDefined()) {
            String desc = buildErrorMessageWithObjectIdentifier(identifier, "WARNING_join_condition_missing");
            ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
            validationInfoList.add(vInfo);
        } else {
            if (!jCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_join_condition_invalid");
                ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(vInfo);
            }
        }

        // TODO Check whether this is too restrictive - shouldn't user be able to create
        // arbitrary join conditions?
        try {
            // check for at least one = condition between columns of the two joined table.
            // also check whether such columns are primary
            SQLPredicate rootPredicate = jCondition.getRootPredicate();
            if (rootPredicate != null) {
                SQLFilter filter = SQLPredicateVisitor.visit(rootPredicate);
                boolean foundOneEqualOp = false;
                boolean foundOnePKMatch = false;
                String pkMsg = null;

                while (filter != null) {
                    SQLObject leftObj = filter.getSQLObject(SQLFilter.LEFT);
                    SQLObject rightObj = filter.getSQLObject(SQLFilter.RIGHT);
                    String op = filter.getOperator();

                    if (isValidJoinOperator(op)) {
                        if (leftObj.getObjectType() == SQLConstants.COLUMN_REF) {
                            leftObj = ((ColumnRef) leftObj).getColumn();
                        }

                        if (rightObj.getObjectType() == SQLConstants.COLUMN_REF) {
                            rightObj = ((ColumnRef) rightObj).getColumn();
                        }

                        SourceColumn sLeft = getFirstSourceColumnReferencedIn(leftObj);
                        SourceColumn sRight = getFirstSourceColumnReferencedIn(rightObj);

                        if (sLeft != null && sRight != null) {
                            foundOneEqualOp = true;
                            String errorKey = null;
                            Object[] objectNames = EMPTY_OBJECT_ARRAY;

                            if (sLeft.isPrimaryKey() && sRight.isPrimaryKey()) {
                                foundOnePKMatch = true;
                            } else if (sLeft.isPrimaryKey() && !sRight.isPrimaryKey()) {
                                errorKey = "ERROR_join_column_not_pk";
                                objectNames = new Object[]{sRight.toString()};
                            } else if (!sLeft.isPrimaryKey() && sRight.isPrimaryKey()) {
                                errorKey = "ERROR_join_column_not_pk";
                                objectNames = new Object[]{sLeft.toString()};
                            } else {
                                errorKey = "ERROR_join_columns_not_pks";
                                objectNames = new Object[]{sLeft.toString(), sRight.toString()};
                            }

                            if (errorKey != null) {
                                pkMsg = buildErrorMessageWithObjectIdentifiers(identifier, errorKey, objectNames);
                            }
                        }
                    }

                    filter = filter.getNextFilter();
                }

                // If an equal relation is not found between columns of both tables
                // then this is an error in join condition
                if (!foundOneEqualOp) {
                    String desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_join_missing_equal_op");
                    ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(vInfo);
                } else if (!foundOnePKMatch) {
                    String desc = pkMsg;
                    ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(vInfo);
                }
            }
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("PRSR130: Error while validating SQLJoinOperator{0}", operator.getDisplayName()), ex);
        }
    }

    private int findColumnErrors(TargetTable targetTable, final String identifier, int stmtType, Collection uniqueIndexColumns) {
        int nonNullColumns = 0;
        List<ValidationInfo> columnErrors = new ArrayList<ValidationInfo>(5);
        Iterator iter = targetTable.getColumns().values().iterator();
        while (iter.hasNext()) {
            TargetColumn col = (TargetColumn) iter.next();
            String argName = col.getName();
            SQLObject obj = col.getValue();

            switch (stmtType) {
                case SQLConstants.DELETE_STATEMENT:
                    // Columns must not be linked for delete statement.
                    if (obj != null) {
                        String desc = buildErrorMessageWithObjectIdentifiers(identifier, "ERROR_input_link_prohibited_delete", new Object[]{argName});
                        columnErrors.add(new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_ERROR));
                    }
                    break;
                case SQLConstants.UPDATE_STATEMENT:
                    // PK and not null columns don't need to be linked for update
                    // statement.
                    break;
                default:
                    if ((col.isPrimaryKey() || !col.isNullable()) && obj == null) {
                        String desc = buildErrorMessageWithObjectIdentifiers(identifier, "WARNING_input_link_needed_pknotnull", new Object[]{argName});
                        columnErrors.add(new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_WARNING));
                    } else if (uniqueIndexColumns.contains(col.getName()) && (obj == null)) {
                        String desc = buildErrorMessageWithObjectIdentifiers(identifier, "WARNING_input_link_needed_unique", new Object[]{argName});
                        columnErrors.add(new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_WARNING));
                    }

                    break;
            }

            // Validate the column input against the declared datatype for this column.
            if (obj != null) {
                ValidationInfo expValidationInfo = validateInputDataType(targetTable, argName, col.getDisplayName(), obj);
                if (expValidationInfo != null) {
                    columnErrors.add(expValidationInfo);
                }
            }

            if (columnErrors.size() != 0) {
                validationInfoList.addAll(columnErrors);
                columnErrors.clear();
            }

            if (obj != null) {
                nonNullColumns++;
                if (obj instanceof SQLConnectableObject) {
                    SQLConnectableObject inObj = (SQLConnectableObject) obj;
                    inObj.visit(this);
                }
            }
        }
        return nonNullColumns;
    }

    /**
     * Gets the first column, if any, associated with a source table that is referenced as
     * an input within the given SQLObject.
     *
     * @param sqlObj SQLObject whose inputs are to be traversed
     * @return first SourceColumn input encountered, or null if none are encountered
     */
    private SourceColumn getFirstSourceColumnReferencedIn(SQLObject sqlObj) {
        if (sqlObj instanceof SourceColumn) {
            Object parent = ((SourceColumn) sqlObj).getParent();
            return (parent instanceof RuntimeInput) ? null : (SourceColumn) sqlObj;
        } else if (sqlObj instanceof ColumnRef) {
            return getFirstSourceColumnReferencedIn(((ColumnRef) sqlObj).getColumn());
        } else if (sqlObj instanceof SQLGenericOperator) {
            SQLGenericOperator op = (SQLGenericOperator) sqlObj;
            for (Iterator iter = op.getInputObjectMap().values().iterator(); iter.hasNext();) {
                SQLInputObject inputWrapper = (SQLInputObject) iter.next();
                SQLObject wrappedObj = inputWrapper.getSQLObject();
                return getFirstSourceColumnReferencedIn(wrappedObj);
            }
        }

        return null;
    }

    private boolean isObjectMappedToExpression(SQLObject obj, List expObjects) {
        boolean result = false;
        Iterator it = expObjects.iterator();

        while (it.hasNext()) {
            SQLConnectableObject expObj = (SQLConnectableObject) it.next();
            result = SQLObjectUtil.isObjectMappedToExpression(obj, expObj);
            if (result) {
                return result;
            }
        }

        return result;
    }

    /**
     * Indicates whether the given String represents a valid operator for a join
     * predicate.
     *
     * @param op String representing operator to be tested
     * @return true if join operator is valid, false otherwise
     */
    private boolean isValidJoinOperator(String op) {
        // We currently only support equijoins.
        return op != null && op.trim().equalsIgnoreCase("=");
    }

    private void validate(Collection allObjects, List expObjects) {
        Iterator it = allObjects.iterator();
        SQLObject sqlObject = null;
        SQLObject column = null;
        SQLConnectableObject exprObj = null;
        String desc = null;
        while (it.hasNext()) {
            sqlObject = (SQLObject) it.next();

            if (sqlObject.getObjectType() == SQLConstants.COLUMN_REF) {
                column = ((ColumnRef) sqlObject).getColumn();

                if ((column.getObjectType() == SQLConstants.SOURCE_COLUMN) && !(((SQLDBColumn) column).isVisible())) {
                    desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Column_Used_Not_Visible", sqlObject.getDisplayName());
                    ValidationInfo validationInfo = new ValidationInfoImpl(sqlObject.getParentObject(), desc, ValidationInfo.VALIDATION_ERROR);
                    validationInfoList.add(validationInfo);
                }
            }

            if (sqlObject instanceof SQLConnectableObject) {
                exprObj = (SQLConnectableObject) sqlObject;
                SQLValidationVisitor vVisitor = new SQLValidationVisitor();
                vVisitor.visitExpression(exprObj, false);
                List<ValidationInfo> vInfos = vVisitor.getValidationInfoList();
                if (vInfos.size() > 0) {
                    validationInfoList.addAll(vInfos);
                }
            } else if (!isObjectMappedToExpression(sqlObject, expObjects)) {
                desc = NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_not_mapped_to_expression", new Object[]{sqlObject.getDisplayName()});
                ValidationInfo validationInfo = new ValidationInfoImpl(sqlObject, desc, ValidationInfo.VALIDATION_ERROR);

                validationInfoList.add(validationInfo);
            }
        }
    }

    /**
     * @param sqlObj
     * @return
     */
    private ValidationInfoImpl validateDateFormatInput(SQLConnectableObject op, String argName, SQLObject sqlObj) {
        ValidationInfoImpl errorInfo = null;
        int validationType = ValidationInfo.VALIDATION_ERROR;

        if (sqlObj instanceof SQLLiteral) {
            SQLLiteral literal = (SQLLiteral) sqlObj;
            String value = literal.getValue();
            AxionDateTimeFormatParser formatParser = new AxionDateTimeFormatParser();

            try {
                formatParser.parseDateTimeFormatToJava(value);
            } catch (AxionException e) {
                String message = buildErrorMessageWithObjectIdentifiers(op.getDisplayName(), "ERROR_dateformat_invalid", new Object[]{value});
                errorInfo = new ValidationInfoImpl(op, message, validationType);
            }
        }

        return errorInfo;
    }

    private void validateExpressionObjectChildren(SQLConnectableObject exp) {
        // go through children of this expression object
        List childList = exp.getChildSQLObjects();
        Iterator it = childList.iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            SQLConnectableObject exprObj = null;
            if (sqlObject instanceof SQLConnectableObject) {
                exprObj = (SQLConnectableObject) sqlObject;
                visitExpression(exprObj, true);
            }
        }
    }

    private void validateGroupBy(TargetTable targetTable, int stmtType) {
        if (targetTable.getSQLGroupBy() != null && !targetTable.getSQLGroupBy().getColumns().isEmpty()) {
            switch (stmtType) {
                case SQLConstants.INSERT_STATEMENT:
                    break;
                default:
                    ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, "Defined GroupBy/Having clause will not be used :: " + targetTable.getSQLGroupBy().toString(), ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(validationInfo);
            }
        }

        // validate group by columns
        SQLGroupBy groupBy = targetTable.getSQLGroupBy();
        List groupByNodes = groupBy != null ? groupBy.getColumns() : null;
        SQLGroupByValidationVisitor groupByVisitor = new SQLGroupByValidationVisitor(targetTable, groupByNodes);
        groupByVisitor.visit(targetTable.getMappedColumns());
        validationInfoList.addAll(groupByVisitor.getValidationInfoList());

        if (groupBy != null) {
            // validate having condition
            SQLCondition having = groupBy.getHavingCondition();
            if (having != null) {
                if (!having.isValid()) {
                    String desc = buildErrorMessageWithObjectIdentifier(having.getDisplayName(), "ERROR_condition_invalid");
                    ValidationInfo validationInfo = new ValidationInfoImpl(having, desc, ValidationInfo.VALIDATION_ERROR);
                    validationInfoList.add(validationInfo);
                } else {
                    visit(having);
                }

                groupByVisitor.reset();
                groupByVisitor.visit(Collections.singletonList(having.getRootPredicate()));
                validationInfoList.addAll(groupByVisitor.getValidationInfoList());
            }

            // validate: selected target column as group by node/expr should be mapped
            // validate: Selected target column should not be mapped to aggregate function
            for (Iterator iter = groupByNodes.iterator(); iter.hasNext();) {
                SQLObject sqlObj = (SQLObject) iter.next();
                if (sqlObj instanceof TargetColumn) {
                    TargetColumn col = (TargetColumn) sqlObj;
                    if (col.getValue() == null) {
                        ValidationInfoImpl validationInfo = new ValidationInfoImpl(targetTable, "Selected group by target coulmn not mapped: " + col, ValidationInfo.VALIDATION_ERROR);
                        validationInfoList.add(validationInfo);
                    } else if (SQLObjectUtil.isAggregateFunctionMapped(col.getValue())) {
                        ValidationInfoImpl validationInfo = new ValidationInfoImpl(targetTable, "Group By clause can't contain agrregate function: " + col + "->" + col.getValue(), ValidationInfo.VALIDATION_ERROR);
                        validationInfoList.add(validationInfo);
                    }
                }
            }
        }
    }

    /**
     * Validates whether the given SQLInputObject is an appropriate input for the argument
     * as referenced by the given String for the given SQLConnectableObject.
     *
     * @param op SQLConnectableObject whose input as referenced by <code>argName</code>
     *        is to be validated
     * @param argName name of input argument to be validated
     * @param displayName display name of input argument
     * @param obj SQLObject representing input to be validated
     * @return ExpressionValidationInfo instance if an error or warning has been
     *         generated; null otherwise.
     */
    private ValidationInfoImpl validateInputDataType(SQLConnectableObject op, String argName, String displayName, SQLObject sqlObj) {
        final String identifier = op.getDisplayName();

        ValidationInfoImpl expValidationInfo = null;
        String desc = null;
        int validationType = ValidationInfo.VALIDATION_ERROR;

        int compatibility = op.isInputCompatible(argName, sqlObj);
        switch (compatibility) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                desc = buildErrorMessageWithObjectIdentifiers(identifier, "ERROR_datatype_incompatible", new Object[]{displayName});
                break;
            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
            case SQLConstants.TYPE_CHECK_UNKNOWN:
                String datatype = SQLUtils.getStdSqlType(sqlObj.getJdbcType());
                if (datatype == null) {
                    datatype = "unknown";
                }
                desc = buildErrorMessageWithObjectIdentifiers(identifier, "WARNING_datatype_downcast_unknown", new Object[]{datatype, displayName});
                validationType = ValidationInfo.VALIDATION_WARNING;
                break;
            case SQLConstants.TYPE_CHECK_COMPATIBLE:
            case SQLConstants.TYPE_CHECK_SAME:
            default:
                desc = null;
        }

        if (desc != null) {
            expValidationInfo = new ValidationInfoImpl(op, desc, validationType);
        } else if (sqlObj instanceof SQLCastOperator && op instanceof TargetTable) {
            SQLCastOperator castOp = (SQLCastOperator) sqlObj;
            TargetTable target = (TargetTable) op;

            DBColumn col = target.getColumn(argName);
            int colPrecision = (col != null) ? col.getPrecision() : 0;
            int castPrecision = castOp.getPrecision();

            int colType = (col != null) ? col.getJdbcType() : SQLConstants.JDBCSQL_TYPE_UNDEFINED;
            switch (colType) {
                case Types.VARCHAR:
                case Types.CHAR:
                case Types.NUMERIC:
                    if (castPrecision > colPrecision) {
                        String castError = buildErrorMessageWithObjectIdentifiers(identifier, "ERROR_datatype_castexceedsprecision", new Object[]{new Integer(castPrecision), displayName, new Integer(colPrecision)});
                        expValidationInfo = new ValidationInfoImpl(castOp, castError, validationType);
                    }
                    break;
                default:
                    break;
            }
        }

        return expValidationInfo;
    }

    private void validateSourceTableCondition(TargetTable targetTable) {
        try {
            // do source table extraction condition validation
            Iterator sIt = targetTable.getSourceTableList().iterator();
            while (sIt.hasNext()) {
                SourceTable sTable = (SourceTable) sIt.next();
                sTable.visit(this);
            }
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("PRSR132: Could not find source tables for this target table{0}in {1}", targetTable.getName(), SQLValidationVisitor.class.getName()), ex);
        }
    }

    private void validateTargetConditionForTargetColumnUsage(SQLCondition condition) {
        try {
            SQLPredicate predicate = condition.getRootPredicate();

            if (predicate != null) {
                if (!predicate.hasTargetColumn()) {
                    String desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Should_Have_Target_Column");
                    ValidationInfoImpl validationInfo = new ValidationInfoImpl(condition, desc, ValidationInfo.VALIDATION_ERROR);
                    validationInfoList.add(validationInfo);
                }
            }
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("PRSR133: Validating target table condition in{0}", SQLValidationVisitor.class.getName()), ex);
        }
    }

    private void validateTargetTableCondition(TargetTable targetTable, final String identifier, int stmtType) {
        switch (stmtType) {
            case SQLConstants.INSERT_UPDATE_STATEMENT:
            case SQLConstants.UPDATE_STATEMENT:
                SQLCondition cond = targetTable.getJoinCondition();
                SQLPredicate rootP = null;
                if (cond != null) {
                    rootP = cond.getRootPredicate();
                }

                if (rootP == null) {
                    String desc = null;

                    if (stmtType == SQLConstants.INSERT_UPDATE_STATEMENT) {
                        desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_mergecondition_missing");
                    } else {
                        try {
                            // If this is a static update (no associated source tables), a
                            // condition is not required.
                            if (targetTable.getSourceTableList().size() == 0) {
                                break;
                            }
                            desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_updatecondition_missing");
                        } catch (BaseException ignore) {
                            break;
                        }
                    }

                    if (desc != null) {
                        ValidationInfoImpl validationInfo = new ValidationInfoImpl(targetTable, desc, ValidationInfo.VALIDATION_ERROR);
                        validationInfoList.add(validationInfo);
                    }
                }
                break;
            default:
                break;
        }

        // do target table condition validation
        SQLCondition joinCondition = targetTable.getJoinCondition();
        if (joinCondition != null) {
            if (!joinCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_condition_invalid");
                ValidationInfoImpl validationInfo = new ValidationInfoImpl(joinCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            } else {
                validateTargetConditionForTargetColumnUsage(joinCondition);
            }
        }

        // do target table filter condition validation
        SQLCondition filterCondition = targetTable.getFilterCondition();
        if (filterCondition != null) {
            if (!filterCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(identifier, "ERROR_condition_invalid");
                ValidationInfoImpl validationInfo = new ValidationInfoImpl(filterCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }
        }
    }

    private void visitExpression(SQLConnectableObject operator, boolean recurse) {
        final String identifier = operator.getDisplayName();

        Iterator iter = operator.getInputObjectMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String argName = (String) entry.getKey();
            SQLInputObject obj = (SQLInputObject) entry.getValue();
            ValidationInfo expValidationInfo = null;

            if (obj == null) {
                String desc = buildErrorMessageWithObjectIdentifiers(identifier, "ERROR_object_not_linked", new Object[]{obj.getDisplayName()});
                expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            }

            SQLObject sqlObj = obj.getSQLObject();
            if (sqlObj == null) {
                String desc = buildErrorMessageWithObjectIdentifiers(identifier, "ERROR_object_not_linked", new Object[]{obj.getDisplayName()});
                expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            } else {
                expValidationInfo = (sqlObj instanceof SQLCustomOperatorImpl) ? null : validateInputDataType(operator, argName, obj.getDisplayName(), sqlObj);
            }

            if (expValidationInfo != null) {
                validationInfoList.add(expValidationInfo);
            }

            if (sqlObj != null && operator.isInputStatic(argName) && DATE_FORMAT_OPS.contains(identifier.toLowerCase())) {
                if ("format".equals(argName.toLowerCase()) || "right".equals(argName.toLowerCase())) {
                    expValidationInfo = validateDateFormatInput(operator, argName, sqlObj);
                    if (expValidationInfo != null) {
                        validationInfoList.add(expValidationInfo);
                    }
                }
            }

            if (recurse && sqlObj != null && sqlObj instanceof SQLConnectableObject) {
                SQLConnectableObject inObj = (SQLConnectableObject) sqlObj;
                inObj.visit(this);
            }
        }

        // validate children
        validateExpressionObjectChildren(operator);
    }
}
