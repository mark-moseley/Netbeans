/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.tools.impl;

import org.netbeans.modules.dlight.tools.*;
import java.io.File;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.ExecutionEnvVariablesProvider;
import org.netbeans.modules.dlight.api.execution.DLightTarget.State;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * @author Alexey Vladykin
 */
public class LLDataCollector
        extends IndicatorDataProvider<LLDataCollectorConfiguration>
        implements DataCollector<LLDataCollectorConfiguration>,
        ExecutionEnvVariablesProvider {

    private EnumSet<LLDataCollectorConfiguration.CollectedData> collectedData;
    private DLightTarget target;
    private String ldPreload;
    private File agentLibraryLocal;
    private String agentLibraryRemote;
    private File monitorExecutableLocal;
    private String monitorExecutableRemote;
    private ValidationStatus validationStatus;
    private List<ValidationListener> validationListeners;

    public LLDataCollector(LLDataCollectorConfiguration configuration) {
        collectedData = EnumSet.of(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
        validationStatus = ValidationStatus.initialStatus();
        validationListeners = new ArrayList<ValidationListener>();
    }

    public void addConfiguration(LLDataCollectorConfiguration configuration) {
        collectedData.add(LLDataCollectorConfigurationAccessor.getDefault().getCollectedData(configuration));
    }

    public String getName() {
        return "LLTool"; // NOI18N
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> tables = new ArrayList<DataTableMetadata>();
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
            tables.add(LLDataCollectorConfiguration.CPU_TABLE);
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
            tables.add(LLDataCollectorConfiguration.MEM_TABLE);
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
            tables.add(LLDataCollectorConfiguration.SYNC_TABLE);
        }
        return tables;
    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return Arrays.<DataStorageType>asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public void init(DataStorage storage, DLightTarget target) {
        this.target = target;

        ExecutionEnvironment env = target.getExecEnv();

        if (env.isLocal()) {
            agentLibraryRemote = agentLibraryLocal.getAbsolutePath();
            monitorExecutableRemote = monitorExecutableLocal.getAbsolutePath();
        } else {
            try {
                agentLibraryRemote = "/tmp/" + agentLibraryLocal.getName(); // NOI18N
                CommonTasksSupport.uploadFile(
                        agentLibraryLocal.getAbsolutePath(), env,
                        agentLibraryRemote, 644, null).get();
                monitorExecutableRemote = "/tmp/" + monitorExecutableLocal.getName(); // NOI18N
                CommonTasksSupport.uploadFile(
                        monitorExecutableLocal.getAbsolutePath(), env,
                        monitorExecutableRemote, 755, null).get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean isAttachable() {
        return true;
    }

    public String getCmd() {
        // should not be called
        return null;
    }

    public String[] getArgs() {
        // should not be called
        return null;
    }

    public Map<String, String> getExecutionEnv() {
        return Collections.singletonMap(ldPreload, agentLibraryRemote);
    }

    public void targetStateChanged(DLightTarget source, State oldState, State newState) {
        switch (newState) {
            case RUNNING:
                startMonitor();
                break;
        }
    }

    private void startMonitor() {
        AttachableTarget at = (AttachableTarget) target;
        NativeProcessBuilder npb = new NativeProcessBuilder(target.getExecEnv(), monitorExecutableRemote);
        StringBuilder flags = new StringBuilder("-"); // NOI18N
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.CPU)) {
            flags.append('c'); // NOI18N
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.MEM)) {
            flags.append('m'); // NOI18N
        }
        if (collectedData.contains(LLDataCollectorConfiguration.CollectedData.SYNC)) {
            flags.append("s"); // NOI18N
        }
        npb = npb.setArguments(flags.toString(), String.valueOf(at.getPID()));

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.bridge(new MonitorOutputProcessor());
            }
        });

        ExecutionService service = ExecutionService.newService(npb, descr, "monitor"); // NOI18N
        service.run();
    }

    private class MonitorOutputProcessor implements LineProcessor {

        private float syncPrev;

        @Override
        public void processLine(String line) {
            DataRow row = null;
            if (line.startsWith("cpu:")) { // NOI18N
                String[] times = line.substring(5).split("\t"); // NOI18N
                row = new DataRow(Arrays.asList("utime", "stime"), Arrays.asList(Float.valueOf(times[0]), Float.valueOf(times[1]))); // NOI18N
            } else if (line.startsWith("mem:")) { // NOI18N
                row = new DataRow(Arrays.asList("total"), Arrays.asList(line.substring(5))); // NOI18N
            } else if (line.startsWith("sync:")) { // NOI18N
                String[] fields = line.substring(6).split("\t"); // NOI18N
                float syncCurr = Float.parseFloat(fields[0]);
                int threads = Integer.parseInt(fields[1]);
                row = new DataRow(Arrays.asList("sync"), Arrays.asList(Float.valueOf((syncCurr - syncPrev) * 100 / threads))); // NOI18N
                syncPrev = syncCurr;
            }
            if (row != null) {
                notifyIndicators(Collections.singletonList(row));
            }
        }

        public void reset() {
        }

        public void close() {
        }
    }

// validation stuff ////////////////////////////////////////////////////////////

    public Future<ValidationStatus> validate(final DLightTarget objectToValidate) {
        Callable<ValidationStatus> validationTask = new Callable<ValidationStatus>() {

            public ValidationStatus call() throws Exception {
                if (validationStatus.isValid()) {
                    return validationStatus;
                }

                ValidationStatus oldStatus = validationStatus;
                ValidationStatus newStatus = doValidation(objectToValidate);

                notifyStatusChanged(oldStatus, newStatus);

                validationStatus = newStatus;
                return newStatus;
            }
        };
        return DLightExecutorService.service.submit(validationTask);
    }

    public void invalidate() {
        validationStatus = ValidationStatus.initialStatus();
        ldPreload = null;
        agentLibraryLocal = null;
        monitorExecutableLocal = null;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    private ValidationStatus doValidation(final DLightTarget target) {
        DLightLogger.assertNonUiThread();

        ExecutionEnvironment env = target.getExecEnv();

        try {
            String osnameRemote = HostInfoUtils.getOS(env);
            if ("Mac_OS_X".equals(osnameRemote)) { // NOI18N
                ldPreload = "DYLD_INSERT_LIBRARIES"; // NOI18N
            } else {
                ldPreload = "LD_PRELOAD"; // NOI18N
            }
        } catch (ConnectException ex) {
            ldPreload = null;
        }
        if (ldPreload == null) {
            ConnectionManager mgr = ConnectionManager.getInstance();

            Runnable doOnConnect = new Runnable() {

                public void run() {
                    DLightManager.getDefault().revalidateSessions();
                }
            };

            AsynchronousAction connectAction = mgr.getConnectToAction(env, doOnConnect);

            return ValidationStatus.unknownStatus(
                    "ValidationStatus.HostNotConnected",
                    connectAction);
        }

        MacroExpander mef = MacroExpanderFactory.getExpander(env);

        String agentLibraryLocalPath = null;
        try {
            agentLibraryLocalPath = mef.expandPredefinedMacros(NativeToolsUtil.getSharedLibrary("prof_agent")); // NOI18N
            agentLibraryLocal = InstalledFileLocator.getDefault().locate(agentLibraryLocalPath, null, false);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (agentLibraryLocal == null || !agentLibraryLocal.exists()) {
            return ValidationStatus.invalidStatus(agentLibraryLocalPath);
        }

        String monitorExecutableLocalPath = null;
        try {
            monitorExecutableLocalPath = mef.expandPredefinedMacros(NativeToolsUtil.getExecutable("prof_monitor")); // NOI18N
            monitorExecutableLocal = InstalledFileLocator.getDefault().locate(monitorExecutableLocalPath, null, false);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (monitorExecutableLocal == null || !monitorExecutableLocal.exists()) {
            return ValidationStatus.invalidStatus(monitorExecutableLocalPath);
        }

        return ValidationStatus.validStatus();
    }

    private void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (!oldStatus.equals(newStatus)) {
            for (ValidationListener vl : validationListeners) {
                vl.validationStateChanged(this, oldStatus, newStatus);
            }
        }
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }
}
