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
package org.netbeans.modules.etl.codegen.impl;

import java.util.List;

import org.netbeans.modules.etl.codegen.ETLScriptBuilderModel;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.codegen.PatternFinder;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.evaluators.database.DB;
import org.netbeans.modules.sql.framework.evaluators.database.DBFactory;
import org.netbeans.modules.sql.framework.evaluators.database.StatementContext;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.etl.engine.impl.Extractor;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.jdbc.SQLPart;
import com.sun.sql.framework.utils.AttributeMap;
import com.sun.sql.framework.utils.Logger;

/**
 * If all source table are from same DB and statement type is Insert
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */

public class OnePassETLStrategyBuilderImpl extends BaseETLStrategyBuilder {
    private static final String LOG_CATEGORY = OnePassETLStrategyBuilderImpl.class.getName();

    public OnePassETLStrategyBuilderImpl(ETLScriptBuilderModel model) throws BaseException {
        super(model);
    }

    public void createTransformerTask(ETLStrategyBuilderContext context) throws BaseException {
        DBConnectionDefinition targetConnDef = context.getModel().getConnectionDefinition(context.getTargetTable());
        DB targetDB = this.getDBFor(targetConnDef);
        MessageManager msgMgr = MessageManager.getManager(ETLTaskNode.class);
        ETLTaskNode onePassETLNode = context.getModel().getEngine().createETLTaskNode(ETLEngine.EXTRACTOR);
        String displayName = msgMgr.getString("TEMPLATE_dn", msgMgr.getString("LBL_dn_onepass"), context.getTargetTable().getName());
        onePassETLNode.setDisplayName(displayName);
        
        context.getPredecessorTask().addNextETLTaskNode(ETLTask.SUCCESS, onePassETLNode.getId());

        TargetTable tgtTable = context.getTargetTable();        
        StatementContext tgtStmtContext = new StatementContext();
        if ( (this.builderModel.isConnectionDefinitionOverridesApplied()) 
                && (PatternFinder.isInternalDBTable(tgtTable))){
            tgtStmtContext.setUsingUniqueTableName(tgtTable, true);
        }
        createTargetTableIfNotExists(context.getTargetTable(), onePassETLNode, targetConnDef.getName(), targetDB, tgtStmtContext);        
        truncateTargetTableIfExists(context.getTargetTable(), onePassETLNode, targetConnDef.getName(), targetDB.getStatements(), tgtStmtContext);


        List sourceTables = context.getTargetTable().getSourceTableList();
        if (sourceTables != null && !sourceTables.isEmpty()) {
            StatementContext stmtContext = new StatementContext();            
            SourceTable srcTable = (SourceTable) sourceTables.get(0);
            DBConnectionDefinition srcConDef = context.getModel().getConnectionDefinition(srcTable);
            String sourceConnName = getConDefnName(srcConDef);
            int dbType = connFactory.getDatabaseVersion(BaseETLStrategyBuilder.getConnectionPropertiesFrom(srcConDef));

            DB db = DBFactory.getInstance().getDatabase(dbType);

            if ( (this.builderModel.isConnectionDefinitionOverridesApplied()) 
                    && (dbType == DBConstants.AXION)){
                stmtContext.setUsingUniqueTableName(true);
            }        
            
            SQLPart selectPart = db.getStatements().getOnePassSelectStatement(tgtTable, stmtContext);
            stmtContext.setUsingUniqueTableName(false);            
            selectPart.setConnectionPoolName(sourceConnName);
            onePassETLNode.addStatement(selectPart);

            // Insert new execution record for target table, tt, and add query to obtain
            // execution id assigned to new execution record.
            super.addInsertNewExecutionRecordStatement(onePassETLNode, tgtTable);
            super.addSelectExecutionIdForNewExecutionRecordStatement(onePassETLNode, tgtTable);

            DB statsDb = DBFactory.getInstance().getDatabase(DB.AXIONDB);
            stmtContext.setUsingFullyQualifiedTablePrefix(false);
            stmtContext.setUsingUniqueTableName(true);
            String statsTable = statsDb.getUnescapedName(statsDb.getEvaluatorFactory().evaluate(tgtTable, stmtContext));
            onePassETLNode.setTableName(statsTable); //NOI18N
        } else {
            throw new BaseException("Must have source table for One-Pass eTL Strategy.");
        }

        SQLPart insertPart = targetDB.getStatements().getPreparedInsertStatement(tgtTable, tgtStmtContext);
        insertPart.setConnectionPoolName(getTargetConnName());
        onePassETLNode.addStatement(insertPart);

        onePassETLNode.addNextETLTaskNode(ETLTask.SUCCESS, context.getNextTaskOnSuccess().getId());
        onePassETLNode.addNextETLTaskNode(ETLTask.EXCEPTION, context.getNextTaskOnException().getId());

        if (context.getDependentTasksForNextTask().length() > 0) {
            context.getDependentTasksForNextTask().append(",");
        }
        context.getDependentTasksForNextTask().append(onePassETLNode.getId());

        AttributeMap attrMap = new AttributeMap();
        attrMap.put("batchSize", context.getTargetTable().getBatchSize() + ""); //NOI18N

        // Set flag in extractor indicating it is operating in one-pass mode.
        attrMap.put(Extractor.KEY_ISONEPASS, Boolean.TRUE);

        onePassETLNode.setAttributeMap(attrMap);
    }

    /**
     * Before calling apply appropriate applyConnections
     */
    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException {
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Looping through target tables: ");

        populateInitTask(context.getInitTask(), context.getGlobalCleanUpTask(), context.getTargetTable());

        // Create cleanup task for this execution thread.
        ETLTaskNode threadCleanupTask = context.getNextTaskOnException();

        // Create commit node to collect transformer connections and commit/close
        // them.
        ETLTaskNode commitTask = context.getNextTaskOnSuccess();
        
        checkTargetConnectionDefinition(context);

        createTransformerTask(context);
        
        // Add statements to create execution summary table if it does not exist, and
        // update the associated execution record upon successful execution
        addCreateIfNotExistsSummaryTableStatement(context.getInitTask());
        addUpdateExecutionRecordPreparedStatement(context.getStatsUpdateTask(), context.getTargetTable());

        // --------------------------------------------------------------------
        commitTask.addNextETLTaskNode(ETLTask.SUCCESS, threadCleanupTask.getId());
        commitTask.addNextETLTaskNode(ETLTask.EXCEPTION, threadCleanupTask.getId());
    }

    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException {
        super.checkTargetConnectionDefinition(context);
        StringBuilder buffer = new StringBuilder();

        TargetTable tgtTable = context.getTargetTable();
        List sourceTables = tgtTable.getSourceTableList();

        // generate one-pass select part.
        if (sourceTables != null && !sourceTables.isEmpty()) {
            SourceTable srcTable = (SourceTable) sourceTables.get(0);
            DBConnectionDefinition srcConDefn = this.builderModel.getConnectionDefinition(srcTable);

            String modelName = srcTable.getParent().getModelName();
            buffer.append(MSG_MGR.getString("DISPLAY_SELECT", srcTable.getName(), modelName, srcConDefn.getDBType())).append("\n");

            int dbType = connFactory.getDatabaseVersion(BaseETLStrategyBuilder.getConnectionPropertiesFrom(srcConDefn));
            DB db = DBFactory.getInstance().getDatabase(dbType);
            StatementContext stmtContext = new StatementContext();
            SQLPart selectPart = db.getStatements().getOnePassSelectStatement(tgtTable, stmtContext);

            buffer.append(selectPart.getSQL());
            buffer.append("\n\n");
        }

        // generate one-pass insert/load part.
        StatementContext stmtContext = new StatementContext();
        SQLPart insertPart = getStatementsForTargetTableDB(context).getPreparedInsertStatement(tgtTable, stmtContext);

        if (insertPart != null) {
            buffer.append(getCommentForTransformer(tgtTable)).append("\n");
            buffer.append(insertPart.getSQL());
            buffer.append("\n\n");
        }
        return buffer.toString();
    }
}
