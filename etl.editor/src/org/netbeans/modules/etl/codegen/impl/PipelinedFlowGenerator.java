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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.utils.MessageManager;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.engine.ETLEngine;
import com.sun.etl.engine.ETLTask;
import com.sun.etl.engine.ETLTaskNode;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConstants;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Builds ETL Process Flow and delegates to appropriate ETLStrategyBuilder implementation
 * as required
 *
 * @author Ahimanikya Satapathy
 * @author Jonathan Giron
 * @version $Revision$
 */
public class PipelinedFlowGenerator extends BaseFlowGenerator {
    private static final String LOG_CATEGORY = PipelinedFlowGenerator.class.getName();
    private static final MessageManager MSG_MGR = MessageManager.getManager(ETLTaskNode.class);
    protected PipelinedStrategyBuilderImpl pipelinedBuilder;

    public PipelinedFlowGenerator(SQLDefinition sqlD) throws BaseException {
        super(sqlD);
        this.builderModel.setUseInstanceDB(true);
    }

    public void applyConnectionDefinitions() throws BaseException {
        super.applyConnectionDefinitions();
    }

    public void applyConnectionDefinitions(Map name2connectionDefMap, Map otdOid2ConnDefNameMap, Map intDbConfigParams) throws BaseException {
        this.builderModel.setUseInstanceDB(true);
        this.builderModel.setShutdownMonitorDB(true);
        super.applyConnectionDefinitions(name2connectionDefMap, otdOid2ConnDefNameMap, intDbConfigParams);
    }

    public ETLEngine getScript() throws BaseException {
        Logger.print(Logger.DEBUG, LOG_CATEGORY, "In getScript()");
        generateScript();
        return builderModel.getEngine();
    }

    protected void createWarapperTask() throws BaseException {
        final MessageManager dnLabelMgr = MessageManager.getManager(ETLTaskNode.class);

        if (pipelinedBuilder != null) {
            List targetTables = this.builderModel.getSqlDefinition().getTargetTables();
            this.initTask = pipelinedBuilder.buildInitTask(targetTables);
            this.initTask.setDisplayName(dnLabelMgr.getString("LBL_dn_init"));
            this.globalCleanupTask = pipelinedBuilder.buildCleanupTask(targetTables);
            this.globalCleanupTask.setDisplayName(dnLabelMgr.getString("LBL_dn_cleanup"));
        }

        this.threadCollectorWaitNode = this.builderModel.getEngine().createETLTaskNode(ETLEngine.WAIT);

        this.statsUpdateTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.UPDATE_STATS);
        this.statsUpdateTask.setDisplayName(MSG_MGR.getString("LBL_dn_updatestats"));
    }

    protected void generateScript() throws BaseException {
        pipelinedBuilder = getBuilder();
        List dependencies = new ArrayList();
        createWarapperTask();

        // get target table List
        List targetTables = builderModel.getSqlDefinition().getTargetTables();
        if (targetTables == null || targetTables.size() == 0) {
            throw new BaseException("Invalid eTL Collaboration: No target table defined.");
        }

        ETLStrategyBuilderContext context = new ETLStrategyBuilderContext(initTask, globalCleanupTask, this.statsUpdateTask, this.builderModel);

        ETLTaskNode pipelineTask = null;
        // Iterate through the target tables to generate pipeline tasks.
        Iterator it = targetTables.iterator();
        while (it.hasNext()) {
            Logger.print(Logger.DEBUG, LOG_CATEGORY, "Looping through target tables: ");
            TargetTable tt = (TargetTable) it.next();

            context.setTargetTable(tt);
            context.setPredecessorTask(initTask);
            context.setNextTaskOnSucess(threadCollectorWaitNode);
            context.setNextTaskOnException(statsUpdateTask);

            pipelinedBuilder.generateScriptForTable(context);
            pipelineTask = context.getLastPipelinedTask();
            dependencies.add(pipelineTask.getId());
        } // end transformer Loop

        // Create commit node to collect transformer connections and
        // commit/close them.
        ETLTaskNode commitTask = this.builderModel.getEngine().createETLTaskNode(ETLEngine.COMMIT);
        commitTask.setDisplayName(MSG_MGR.getString("LBL_dn_commit"));

        // set dependent list for wait node
        this.threadCollectorWaitNode.setDependsOn(StringUtil.createDelimitedStringFrom(dependencies));

        // Complete task net by linking nodes.
        this.startTask.addNextETLTaskNode(ETLTask.SUCCESS, this.initTask.getId());
        this.initTask.addNextETLTaskNode(ETLTask.EXCEPTION, this.globalCleanupTask.getId());

        //
        // Commit data first, then update statistics.
        //
        this.threadCollectorWaitNode.addNextETLTaskNode(ETLTask.SUCCESS, commitTask.getId());

        commitTask.addNextETLTaskNode(ETLTask.SUCCESS, this.statsUpdateTask.getId());
        commitTask.addNextETLTaskNode(ETLTask.EXCEPTION, this.statsUpdateTask.getId());

        statsUpdateTask.addNextETLTaskNode(ETLTask.SUCCESS, globalCleanupTask.getId());
        statsUpdateTask.addNextETLTaskNode(ETLTask.EXCEPTION, globalCleanupTask.getId());

        globalCleanupTask.addNextETLTaskNode(ETLTask.SUCCESS, endTask.getId());
        globalCleanupTask.addNextETLTaskNode(ETLTask.EXCEPTION, endTask.getId());

        // this to generate the engine xml and save for debugging use
        this.builderModel.getEngine().toXMLString();
    }

    protected ETLStrategyBuilder getTargetTableScriptBuilder() throws BaseException {
        return ETLProcessFlowGeneratorFactory.getPipelinedTargetTableScriptBuilder(builderModel);
    }

    /**
     * Indicates whether the given table must be accessed by the pipeline database via a
     * dblink/remote table combination.
     *
     * @param table SQLDBTable instance to test
     * @return true if <code>table</code> should be accessed via remote table, false
     *         otherwise
     */
    protected boolean requiresRemoteAccess(SQLDBTable table) {
        // If table is not an Axion table, create an external remote table if it doesn't
        // already exist. We handle Axion flatfiles in buildFlatfileSQLParts(), though
        // we will create a log table for target tables, regardless of DB type.
        boolean ret = true;

        if (this.builderModel.isConnectionDefinitionOverridesApplied()) {
            DBConnectionDefinition connDef = table.getParent().getConnectionDefinition();
            ret = SQLUtils.getSupportedDBType(connDef.getDBType()) != DBConstants.AXION;
        }

        return ret;

    }

    private PipelinedStrategyBuilderImpl getBuilder() throws BaseException {
        if (this.builderModel.getSqlDefinition().hasValidationConditions()) {
            return ETLProcessFlowGeneratorFactory.getValidatingTargetTableScriptBuilder(builderModel);
        } else {
            return ETLProcessFlowGeneratorFactory.getPipelinedTargetTableScriptBuilder(builderModel);
        }
    }
}
