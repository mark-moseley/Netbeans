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
package org.netbeans.modules.dlight.management.api;

import org.netbeans.modules.dlight.api.execution.DLightSessionContext;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.api.execution.DLightTarget.State;
import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.SubstitutableTarget;
import org.netbeans.modules.dlight.api.impl.DLightSessionContextAccessor;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorRepairActionProviderAccessor;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * This class represents D-Light Session.
 * 
 */
public final class DLightSession implements DLightTargetListener, DLightSessionInternalReference {

    private static int sessionCount = 0;
    private static final Logger log = DLightLogger.getLogger(DLightSession.class);
    private List<ExecutionContext> contexts = new ArrayList<ExecutionContext>();
    private List<SessionStateListener> sessionStateListeners = null;
    private List<DataStorage> storages = null;
    private List<DataCollector> collectors = null;
    private Map<String, Map<String, Visualizer>> visualizers = null;
    private SessionState state;
    private final int sessionID;
    private String description = null;
    private List<ExecutionContextListener> contextListeners;
    private boolean isActive;
    private final DLightSessionContext sessionContext;
    private final String name;

    public static enum SessionState {

        CONFIGURATION,
        STARTING,
        RUNNING,
        PAUSED,
        ANALYZE,
    }

    public void targetStateChanged(DLightTarget source, State oldState, State newState) {
        switch (newState) {
            case RUNNING:
                targetStarted(source);
                break;
            case FAILED:
                targetFinished(source);
                break;
            case TERMINATED:
                targetFinished(source);
                break;
            case DONE:
                targetFinished(source);
                break;
            case STOPPED:
                targetFinished(source);
                return;
        }
    }

    /**
     * Created new DLightSession instance. This should not be called directly.
     * Instead DLightManager.newSession() should be used.
     *
     */
    DLightSession(String name) {
        this.state = SessionState.CONFIGURATION;
        this.name = name;
        sessionID = sessionCount++;
        sessionContext = DLightSessionContextAccessor.getDefault().newContext();
    }

    public DLightSessionContext getSessionContext() {
        return sessionContext;
    }

    void cleanVisualizers() {
        visualizers.clear();
        visualizers = null;
    }

    List<ExecutionContext> getExecutionContexts() {
        return contexts;
    }

    void addExecutionContext(ExecutionContext context) {
        contexts.add(context);
        context.validateTools();
    }

    void setExecutionContext(ExecutionContext context) {
        clearExecutionContext();
        addExecutionContext(context);
    }

    void clearExecutionContext() {
        assertState(SessionState.CONFIGURATION);

        for (ExecutionContext c : contexts) {
            c.clear();
        }

        contexts.clear();
    }

    public void addSessionStateListener(SessionStateListener listener) {
        if (sessionStateListeners == null) {
            sessionStateListeners = new ArrayList<SessionStateListener>();
        }

        if (!sessionStateListeners.contains(listener)) {
            sessionStateListeners.add(listener);
        }
    }

    public void removeSessionStateListener(SessionStateListener listener) {
        if (sessionStateListeners == null) {
            return;
        }

        sessionStateListeners.remove(listener);
    }

    public SessionState getState() {
        return state;
    }

    public String getDescription() {
        if (description == null) {
            String targets = ""; // NOI18N
            if (contexts.isEmpty()) {
                targets = "no targets"; // NOI18N
            } else {
                for (ExecutionContext context : contexts) {
                    targets += context.getTarget().toString() + "; "; // NOI18N
                }
            }
            description = "Session #" + sessionID + " (" + targets + ")"; // NOI18N
        }
        return description;
    }

    public String getDisplayName() {
        return name == null? getDescription() : name;
    }

    boolean isRunning() {
        return state == SessionState.RUNNING;
    }

    boolean hasVisualizer(String toolName, String visualizerID){
        if (visualizers == null || !visualizers.containsKey(toolName)) {
            return false;
        }
        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        return toolVisualizers.containsKey(visualizerID);
    }

    List<Visualizer> getVisualizers(){
        if (visualizers == null){
            return null;
        }
        List<Visualizer> result = new ArrayList<Visualizer>();
        for (String toolName: visualizers.keySet()){
            Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
            for (String visID : toolVisualizers.keySet()){
                result.add(toolVisualizers.get(visID));
            }
        }
        return result;

    }

    Visualizer getVisualizer(String toolName, String visualizerID){
        if (visualizers == null || !visualizers.containsKey(toolName)) {
            return null;
        }
        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        return toolVisualizers.get(visualizerID);
    }

    Visualizer putVisualizer(String toolName, String id, Visualizer visualizer) {
        if (visualizers == null) {
            visualizers = new HashMap<String, Map<String, Visualizer>>();
        }

        Map<String, Visualizer> toolVisualizers = visualizers.get(toolName);
        if (toolVisualizers == null){
            toolVisualizers = new HashMap<String, Visualizer>();
            visualizers.put(toolName, toolVisualizers);
        }
        Visualizer oldVis = toolVisualizers.put(id,  visualizer);
        return oldVis;
    
    }

    public void revalidate() {
        for (ExecutionContext c : contexts) {
            c.validateTools();
        }
    }

    synchronized void stop() {
        if (state == SessionState.ANALYZE) {
            return;
        }
        setState(SessionState.ANALYZE);
        for (ExecutionContext c : contexts) {
            final DLightTarget target = c.getTarget();
            target.removeTargetListener(this);
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    DLightTargetAccessor.getDefault().getDLightTargetExecution(target).terminate(target);
                }
            }, "Stop DLight session's target " + target.toString()); // NOI18N
        }
    }

    void start() {
        Runnable sessionRunnable = new Runnable() {

            public void run() {
                DataStorageManager.getInstance().clearActiveStorages();

                if (storages != null) {
                    storages.clear();
                }

                if (collectors != null) {
                    collectors.clear();
                }

                for (ExecutionContext context : contexts) {
                    prepareContext(context);
                }

                setState(SessionState.STARTING);

                // TODO: For now add target listeners to
                // first target.... (from the first context)
                boolean f = false;

                final DLightTargetAccessor targetAccess =
                    DLightTargetAccessor.getDefault();

                for (ExecutionContext context : contexts) {
                    DLightTarget target = context.getTarget();

                    if (!f) {
                        target.addTargetListener(DLightSession.this);
                        f = true;
                    }

                    DLightTarget.ExecutionEnvVariablesProvider envProvider =
                        context.getDLightTargetExecutionEnvProvider();

                    targetAccess.getDLightTargetExecution(target).start(target, envProvider);
                }
            }
        };

        DLightExecutorService.submit(sessionRunnable, "DLight session"); // NOI18N
    }

    private boolean prepareContext(ExecutionContext context) {
        final DLightTarget target = context.getTarget();

        context.validateTools(context.getDLightConfiguration().getConfigurationOptions(false).validateToolsRequiredUserInteraction());

        List<DLightTool> validTools = new ArrayList<DLightTool>();

        for (DLightTool tool : context.getTools()) {
//            if (tool.getValidationStatus().isValid()) {//it is not quite correct, should run anyway
            validTools.add(tool);
        //          }
        }

        if (validTools.isEmpty()) {
            return false;
        }

        DataCollector notAttachableDataCollector = null;

        if (collectors == null) {
            collectors = new ArrayList<DataCollector>();
        }
        if (context.getDLightConfiguration().getConfigurationOptions(false).areCollectorsTurnedOn()) {
            for (DLightTool tool : validTools) {
                List<DataCollector<?>> toolCollectors = context.getDLightConfiguration().getConfigurationOptions(false).getCollectors(tool);
                //TODO: no algorithm here:) should be better
                for (DataCollector c : toolCollectors) {
                    if (!collectors.contains(c)) {
                        if (c.getValidationStatus().isValid()) {//for valid collectors only
                            collectors.add(c);
                        }
                    }
                }
            }
        } else {
            collectors.clear();
        }

        
        //if we have IDP which are collectors add them into the list of collectors
        for (DLightTool tool : validTools) {
            // Try to subscribe every IndicatorDataProvider to every Indicator
            //there can be the situation when IndicatorDataProvider is collector
            //and not attacheble
            List<Indicator> subscribedIndicators = new ArrayList<Indicator>();
            List<IndicatorDataProvider<?>> idps = context.getDLightConfiguration().getConfigurationOptions(false).getIndicatorDataProviders(tool);
            if (idps != null) {
                for (IndicatorDataProvider idp : idps) {
                    if (idp.getValidationStatus().isValid()) {
                        if (idp instanceof DLightTarget.ExecutionEnvVariablesProvider) {
                            context.addDLightTargetExecutionEnviromentProvider((DLightTarget.ExecutionEnvVariablesProvider) idp);
                        }
                        if (idp instanceof DataCollector) {
                            if (!collectors.contains(idp)) {
                                collectors.add((DataCollector) idp);
                            }
                            if (notAttachableDataCollector == null && !((DataCollector) idp).isAttachable()) {
                                notAttachableDataCollector = ((DataCollector) idp);
                            }
                        }
                        List<Indicator<?>> indicators = DLightToolAccessor.getDefault().getIndicators(tool);
                        for (Indicator i : indicators) {
                            target.addTargetListener(i);
                            boolean wasSubscribed = idp.subscribe(i);
                            if (wasSubscribed) {
                                if (!subscribedIndicators.contains(i)){
                                    subscribedIndicators.add(i);
                                }
                                target.addTargetListener(idp);
                                log.info("I have subscribed indicator " + i + " to indicatorDataProvider " + idp);
                            }
                        }
                    }
                }
            }
            List<Indicator<?>> indicators = DLightToolAccessor.getDefault().getIndicators(tool);
            for (Indicator i : indicators){
                if (!subscribedIndicators.contains(i)){
                    IndicatorAccessor.getDefault().setRepairActionProviderFor(i, IndicatorRepairActionProviderAccessor.getDefault().createNew(context.getDLightConfiguration(), tool, target));
                }
            }
        }


        for (DataCollector toolCollector : collectors) {
            DataStorage storage = DataStorageManager.getInstance().getDataStorageFor(toolCollector);
            if (toolCollector instanceof DLightTarget.ExecutionEnvVariablesProvider) {
                context.addDLightTargetExecutionEnviromentProvider((DLightTarget.ExecutionEnvVariablesProvider) toolCollector);
            }
            if (storage != null) {
                if (notAttachableDataCollector == null && !toolCollector.isAttachable()) {
                    notAttachableDataCollector = toolCollector;
                }
                //init storage with the target values
                DLightTarget.Info targetInfo = DLightTargetAccessor.getDefault().getDLightTargetInfo(target);
                Map<String, String> info = targetInfo.getInfo();
                for (String key : info.keySet()) {
                    storage.put(key, info.get(key));
                }
                toolCollector.init(storage, target);
                if (storages == null) {
                    storages = new ArrayList<DataStorage>();
                }
                if (!storages.contains(storage)) {
                    storages.add(storage);
                }
            } else {
                // Cannot find storage for this collector!
                log.severe("Cannot find storage for collector " + toolCollector);
            }

            target.addTargetListener(toolCollector);
        }



        //and now if we have collectors which cannot be attached let's substitute target
        //the question is is it possible in case target is the whole system: WebTierTarget
        //or SystemTarget
        if (notAttachableDataCollector != null && target instanceof SubstitutableTarget) {
            ((SubstitutableTarget) target).substitute(notAttachableDataCollector.getCmd(), notAttachableDataCollector.getArgs());
        }

        return true;

//    activeTasks = new ArrayList<DLightExecutorTask>();

    // TODO: For now: assume that ANY collector can attach to the process!
    // Here we need to start (paused!) the target; subscribe collectors as listeners .... ;


//    if (selectedTools != null) {
//      for (TargetRunnerListener l : tool.getTargetRunnerListeners()) {
//        runner.addTargetRunnerListener(l);
//      }
//    }
////    RequestProcessor.getDefault().post(runner);
//    runner.run();
    }

    List<DataStorage> getStorages() {
        return (storages == null) ? Collections.<DataStorage>emptyList() : storages;
    }

    void close() {
        // Unsubscribe listeners
        if (sessionStateListeners != null) {
            sessionStateListeners.clear();
            sessionStateListeners = null;
        }
    }

    private void targetStarted(DLightTarget target) {
        setState(SessionState.RUNNING);
    }

    private void targetFinished(DLightTarget target) {
        setState(SessionState.ANALYZE);
        target.removeTargetListener(this);
    }

    private void setState(SessionState state) {
        SessionState oldState = this.state;
        this.state = state;

        if (sessionStateListeners != null) {
            for (SessionStateListener l : sessionStateListeners.toArray(new SessionStateListener[0])) {
                l.sessionStateChanged(this, oldState, state);
            }
        }
    }



    


    // Proxy method to contexts
    public void addExecutionContextListener(ExecutionContextListener listener) {
        if (contextListeners == null) {
            contextListeners = new ArrayList<ExecutionContextListener>();
        }

        if (!contextListeners.contains(listener)) {
            contextListeners.add(listener);
        }

        updateContextListeners();
    }

    // Proxy method to contexts
    public void removeExecutionContextListener(ExecutionContextListener listener) {
        if (contextListeners == null) {
            return;
        }

        contextListeners.remove(listener);
        updateContextListeners();
    }

    private void updateContextListeners() {
        for (ExecutionContext c : contexts) {
            c.setListeners(contextListeners);
        }
    }

    public List<DLightTool> getTools() {
        List<DLightTool> result = new ArrayList<DLightTool>();
        for (ExecutionContext c : contexts) {
            result.addAll(c.getTools());
        }
        return result;
    }

    public List<Indicator> getIndicators() {
        List<Indicator> result = new ArrayList<Indicator>();
        for (ExecutionContext c : contexts) {
            result.addAll(c.getIndicators());
        }
        return result;
    }

    private void assertState(SessionState expectedState) {
        if (this.state != expectedState) {
            throw new IllegalStateException("Session is in illegal state " + this.state + "; Must be in " + expectedState);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    void setActive(boolean b) {
        isActive = b;
    }
}
