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
package org.netbeans.modules.dlight.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.tools.LLDataCollectorConfiguration;
import org.netbeans.modules.dlight.spi.util.MangledNameType;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public final class SyncToolConfigurationProvider implements DLightToolConfigurationProvider {

    private static final String TOOL_NAME = loc("SyncTool.ToolName"); // NOI18N    
    private static final Column timestampColumn =
        new Column("timestamp", Long.class, loc("SyncTool.ColumnName.timestamp"), null); // NOI18N
    private static final Column waiterColumn =
        new Column("waiter", Integer.class, loc("SyncTool.ColumnName.waiter"), null); // NOI18N
    private static final Column mutexColumn =
        new Column("mutex", Long.class, loc("SyncTool.ColumnName.mutex"), null); // NOI18N
    private static final Column blockerColumn =
        new Column("blocker", Integer.class, loc("SyncTool.ColumnName.blocker"), null); // NOI18N
    private static final Column timeColumn =
        new Column("time", Long.class, loc("SyncTool.ColumnName.time"), null); // NOI18N
    private static final Column stackColumn =
        new Column("stackid", Integer.class, loc("SyncTool.ColumnName.stackid"), null); // NOI18N
    private static final Column locksColumn =
        new Column("locks", Float.class, loc("SyncTool.ColumnName.locks"), null); // NOI18N
    private static final DataTableMetadata rawTableMetadata;


    static {
        List<Column> rawColumns = Arrays.asList(
            timestampColumn,
            waiterColumn,
            mutexColumn,
            blockerColumn,
            timeColumn,
            stackColumn);

        rawTableMetadata = new DataTableMetadata("sync", rawColumns);
    }

    public SyncToolConfigurationProvider() {
    }

    public DLightToolConfiguration create() {
        DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(TOOL_NAME);
        List<DataCollectorConfiguration> dcConfigurations = initDataCollectorConfigurations();
        for (DataCollectorConfiguration dc : dcConfigurations) {
            toolConfiguration.addDataCollectorConfiguration(dc);
        }
        List<IndicatorDataProviderConfiguration> idpcs = initIndicatorDataProviderConfigurations();
        for (IndicatorDataProviderConfiguration idpc : idpcs) {
            toolConfiguration.addIndicatorDataProviderConfiguration(idpc);
        }
        IndicatorConfiguration ic = initIndicatorConfiguration();
        toolConfiguration.addIndicatorConfiguration(ic);

        return toolConfiguration;
    }

    private List<DataCollectorConfiguration> initDataCollectorConfigurations() {
        List<DataCollectorConfiguration> result = new ArrayList<DataCollectorConfiguration>();

        String scriptFile = Util.copyResource(getClass(),
            Util.getBasePath(getClass()) + "/resources/sync.d"); // NOI18N

        DTDCConfiguration dataCollectorConfiguration =
            new DTDCConfiguration(scriptFile, Arrays.asList(rawTableMetadata));

        dataCollectorConfiguration.setStackSupportEnabled(true);
        dataCollectorConfiguration.setIndicatorFiringFactor(1);
        result.add(new MultipleDTDCConfiguration(
            dataCollectorConfiguration, "sync:")); // NOI18N
        result.add(new SunStudioDCConfiguration(CollectedInfo.SYNCHRONIZARION));
        result.add(new LLDataCollectorConfiguration(LLDataCollectorConfiguration.CollectedData.SYNC));
        return result;
    }

    private IndicatorConfiguration initIndicatorConfiguration() {
        //VisualizerConfiguration vc = null;

        IndicatorMetadata indicatorMetadata = null;
        List<Column> indicatorColumns = new ArrayList<Column>();
        indicatorColumns.add(locksColumn);
        indicatorColumns.add(SunStudioDCConfiguration.c_ulockSummary);
        indicatorColumns.addAll(LLDataCollectorConfiguration.SYNC_TABLE.getColumns());
        indicatorMetadata = new IndicatorMetadata(indicatorColumns);
        SyncIndicatorConfiguration indicatorConfiguration =
            new SyncIndicatorConfiguration(indicatorMetadata);

        indicatorConfiguration.addVisualizerConfiguration(getDetails(rawTableMetadata));

        // Then configure what happens when user clicks on the indicator...
        // configure metadata for the detailed view ...
        DataTableMetadata detailedViewTableMetadata =
            SunStudioDCConfiguration.getSyncTableMetadata(
            SunStudioDCConfiguration.c_name,
            SunStudioDCConfiguration.c_iSync,
            SunStudioDCConfiguration.c_iSyncn);

        indicatorConfiguration.addVisualizerConfiguration(new AdvancedTableViewVisualizerConfiguration(detailedViewTableMetadata,
            SunStudioDCConfiguration.c_name.getColumnName()));


        return indicatorConfiguration;
    }

    private List<IndicatorDataProviderConfiguration> initIndicatorDataProviderConfigurations() {

        List<IndicatorDataProviderConfiguration> lockIndicatorDataProviders = new ArrayList<IndicatorDataProviderConfiguration>();
        final DataTableMetadata indicatorTableMetadata = new DataTableMetadata("locks", Arrays.asList(locksColumn));
        List<DataTableMetadata> indicatorTablesMetadata = Arrays.asList(indicatorTableMetadata);
        CLIODCConfiguration lockConf = new CLIODCConfiguration(
            "/bin/prstat", "-mv -p @PID -c 1", // NOI18N
            new SyncCLIOParser(locksColumn), indicatorTablesMetadata);
        lockConf.setName("prstat");
        lockIndicatorDataProviders.add(lockConf);

        lockIndicatorDataProviders.add(new SunStudioDCConfiguration(CollectedInfo.SYNCSUMMARY));

        lockIndicatorDataProviders.add(new LLDataCollectorConfiguration(
            LLDataCollectorConfiguration.CollectedData.SYNC));

        return lockIndicatorDataProviders;
    }

    private static class SyncCLIOParser implements CLIOParser {

        private final List<String> colnames;

        public SyncCLIOParser(Column locksColumn) {
            colnames = Arrays.asList(locksColumn.getColumnName());
        }

        public DataRow process(String line) {
            DLightLogger.instance.fine("SyncCLIOParser: " + line); //NOI18N

            /*----- Below is an example of output: -------------------------
            PID USERNAME USR SYS TRP TFL DFL LCK SLP LAT VCX ICX SCL SIG PROCESS/NLWP
            2732 vk155633 0.0 0.0 0.0 0.0 0.0 0.0 100 0.0   0   0   0   0 dlight_simpl/1
            Total: 1 processes, 1 lwps, load averages: 0.30, 0.27, 0.34
            ---------------------------------------------------------------- */

            if (line == null || line.length() == 0) {
                return null;
            }

            line = line.trim();

            if (!Character.isDigit(line.charAt(0))) {
                return null;
            }

            String l = line.trim();
            l = l.replaceAll(",", ".");
            String[] tokens = l.split("[ \t]+");

            if (tokens.length < 8) {
                return null;
            }

            try {
                float locks = Float.parseFloat(tokens[7]);
                return new DataRow(colnames, Arrays.asList(locks));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private VisualizerConfiguration getDetails(DataTableMetadata rawTableMetadata) {
        DataTableMetadata viewTableMetadata = null;
        List<Column> viewColumns = Arrays.asList(
            new Column("func_name", MangledNameType.class, "Function", null),
            new Column("time", Long.class, "Time, ms", null),
            new Column("count", Long.class, "Count", null));
        String sql = "SELECT func.func_name as func_name, SUM(sync.time/1000000) as time, COUNT(*) as count" +
            " FROM sync, node AS node, func" +
            " WHERE  sync.stackid = node.node_id and node.func_id = func.func_id" +
            " GROUP BY node.func_id, func.func_name";

        viewTableMetadata = new DataTableMetadata("sync", viewColumns, sql, Arrays.asList(rawTableMetadata));
        AdvancedTableViewVisualizerConfiguration tableVisualizerConfiguration =
            new AdvancedTableViewVisualizerConfiguration(viewTableMetadata, "func_name");

        tableVisualizerConfiguration.setEmptyAnalyzeMessage(
            loc("DetailedView.EmptyAnalyzeMessage")); // NOI18N

        tableVisualizerConfiguration.setEmptyRunningMessage(
            loc("DetailedView.EmptyRunningMessage")); // NOI18N

        tableVisualizerConfiguration.setDefaultActionProvider();

        return tableVisualizerConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SyncToolConfigurationProvider.class, key, params);
    }
}
