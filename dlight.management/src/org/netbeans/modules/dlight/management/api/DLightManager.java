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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.api.impl.DLightSessionHandlerAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolkitManager;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.management.api.impl.DataProvidersManager;
import org.netbeans.modules.dlight.management.api.impl.VisualizerProvider;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorsComponentProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.spi.impl.IndicatorActionListener;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * D-Light manager 
 */
@ServiceProvider(service=org.netbeans.modules.dlight.api.impl.DLightToolkitManager.class)
public final class DLightManager implements DLightToolkitManager, IndicatorActionListener {

    private static final Logger log = DLightLogger.getLogger(DLightManager.class);
    private final List<DLightSessionListener> sessionListeners = Collections.synchronizedList(new ArrayList<DLightSessionListener>());
    private final List<DLightSession> sessions = new ArrayList<DLightSession>();
    private DLightSession activeSession;

    /**
     *
     */
    public DLightManager() {
        this.addDLightSessionListener(IndicatorsComponentProvider.getInstance().getIndicatorComponentListener());
    }

    public static DLightManager getDefault() {
        return (DLightManager) Lookup.getDefault().lookup(DLightToolkitManager.class);
    }

    public DLightSession createNewSession(DLightTarget target, String configurationName) {
        // TODO: For now just create new session every time we set a target...
        DLightSession session = newSession(target, DLightConfigurationManager.getInstance().getConfigurationByName(configurationName));
        setActiveSession(session);
        return session;
    }

    public DLightSessionHandler createSession(DLightTarget target, String configurationName) {
        return DLightSessionHandlerAccessor.getDefault().create(createNewSession(target, configurationName));
    }

    public DLightSession createNewSession(DLightTarget target) {
        return createNewSession(target, DLightConfigurationManager.getInstance().getSelectedDLightConfiguration().getConfigurationName());
    }

    public void closeSession(DLightSession session) {
        if (session.isRunning()) {
            Object result = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                    loc("DLightManager.disposeRunningContext.Message", session.getDescription()), // NOI18N
                    loc("DLightManager.disposeRunningContext.Title"), NotifyDescriptor.YES_NO_OPTION)); // NOI18N

            if (result == NotifyDescriptor.NO_OPTION) {
                return;
            }

            session.stop();
            session.close();
            List<Indicator> indicators = session.getIndicators();
            for (Indicator ind : indicators) {
                IndicatorAccessor.getDefault().removeIndicatorActionListener(ind, this);
            }
        }

        List<Visualizer> visualizers = session.getVisualizers();
        if (visualizers != null) {
            for (Visualizer v : visualizers) {
                VisualizerContainer vc = (VisualizerContainer) SwingUtilities.getAncestorOfClass(VisualizerContainer.class, v.getComponent());
                // TODO: It could be so, that Visualizer is already closed - in this case vc will be null
                //       Should visualizer be removed from a session on it's closure?
                if (vc != null) {
                    vc.removeVisualizer(v);
                }
            }
        }

        sessions.remove(session);
        notifySessionRemoved(session);

        if (sessions.isEmpty()) {
            setActiveSession(null);
        } else {
            setActiveSession(sessions.get(0));
        }
    }

    public DLightSession getActiveSession() {
        return activeSession;
    }

    public List<DLightSession> getSessionsList() {
        return sessions;
    }

    public DLightSession setActiveSession(DLightSession newActiveSession) {
        DLightSession oldActiveSession = activeSession;
        if (newActiveSession != oldActiveSession) {

            activeSession = newActiveSession;

            if (oldActiveSession != null) {
                oldActiveSession.setActive(false);
            }

            if (newActiveSession != null) {
                newActiveSession.setActive(true);
            }

            notifySessionActivated(oldActiveSession, newActiveSession);
        }

        return activeSession;
    }

    /**
     * Sets session as active and starts it
     * @param dlightSession
     */
    public void startSession(DLightSession dlightSession) {
        setActiveSession(dlightSession);
        dlightSession.start();
    }

    public void stopActiveSession() {
        activeSession.stop();
    }

    public void stopSession(DLightSession session) {
        session.stop();
    }

    public void addDLightSessionListener(DLightSessionListener listener) {
        if (listener == null){
            return;
        }
        if (!sessionListeners.contains(listener)) {
            sessionListeners.add(listener);

            for (DLightSession s : sessions) {
                listener.sessionAdded(s);
            }

            listener.activeSessionChanged(null, activeSession);
        }
    }

    public void removeDLightSessionListener(DLightSessionListener listener) {
        sessionListeners.remove(listener);
    }

    private DLightSession newSession(DLightTarget target, DLightConfiguration configuration) {
        DLightSession session = new DLightSession();
        session.setExecutionContext(new ExecutionContext(target, configuration.getToolsSet()));
        sessions.add(session);
        List<Indicator> indicators = session.getIndicators();
        for (Indicator ind : indicators) {
            IndicatorAccessor.getDefault().addIndicatorActionListener(ind, this);
        }
        notifySessionAdded(session);
//        AK: Listeners are already notified...
//        //should add existinhg listeners
//        for (DLightSessionListener sessionListener : sessionListeners){
//            sessionListener.sessionAdded(session);
//        }
        return session;
    }

    private Visualizer openVisualizer(String toolName, final VisualizerConfiguration configuration) {
        Visualizer visualizer = null;

        /*
         * Two conditions should be met in order to create and open visualizer:
         *
         * 1. registered factory that can create Visualizer with such a visualizerID
         *    should exist;
         *
         * 2. there should be a DataProvider that:
         *    a) hand can 'talk' to one of *already configured!* DataStorage (i.e. registered DataStorage
         *    should support the same DataStorageScheme as provider does)
         *
         *    b) can serve Visualizer's needs (i.e. implements StackDataProvider for CallersCaleesVisualizer) and
         *
         * Scope: ExecutionContext
         *
         */

        Collection<? extends VisualizerProvider> allVisualizerFactories = Lookup.getDefault().lookupAll(VisualizerProvider.class);
        Collection<? extends DataStorage> activeDataStorages = activeSession.getStorages();

//        for (VisualizerFactory vf : allVisualizerFactories) {
//            if (vf.canCreate(visualizerID)) {
        // This is a candidate to be used for Visualizer creation
        // Check for a second condition:

        DataModelScheme dataModel = configuration.getSupportedDataScheme();

        for (DataStorage storage : activeDataStorages) {
            if (!storage.hasData(configuration.getMetadata())) {
                continue;
            }
            Collection<DataStorageType> dataStorageTypes = storage.getStorageTypes();
            for (DataStorageType dss : dataStorageTypes) {
                // As DataStorage is already specialized, there is always only one
                // returned DataSchema
                DataProvider dataProvider = DataProvidersManager.getInstance().getDataProviderFor(dss, dataModel);

                if (dataProvider == null) {
                    // no providers for this storage can be found nor created
                    continue;
                } else {
                    // Found! Can craete visualizer with this id for this dataProvider
                    visualizer = VisualizerProvider.getInstance().createVisualizer(configuration, dataProvider);
                    if (visualizer instanceof SessionStateListener) {
                        activeSession.addSessionStateListener((SessionStateListener) visualizer);
                        ((SessionStateListener) visualizer).sessionStateChanged(activeSession, null, activeSession.getState());

                    }
                    //  visualizer = Visualiz.newVisualizerInstance(visualizerID, activeSession, dataProvider, configuration);
                    dataProvider.attachTo(storage);
                    break;
                }
            }
        }
        //there is one more changes to find VisualizerDataProvider without any storage attached

        if (visualizer == null) {
            VisualizerDataProvider dataProvider = DataProvidersManager.getInstance().getDataProviderFor(dataModel);

            if (dataProvider == null) {
                // no providers for this storage can be found nor created
                log.info("Unable to find factory to create Visualizer with ID == " + configuration.getID());
                return null;
            } else {
                // Found! Can craete visualizer with this id for this dataProvider
                visualizer = VisualizerProvider.getInstance().createVisualizer(configuration, dataProvider);
                if (visualizer instanceof SessionStateListener) {
                    activeSession.addSessionStateListener((SessionStateListener) visualizer);
                    ((SessionStateListener) visualizer).sessionStateChanged(activeSession, null, activeSession.getState());

                }
            }

        }
        if (visualizer == null) {
            log.info("Unable to find factory to create Visualizer with ID == " + configuration.getID());
            return null;

        }
        VisualizerContainer container = visualizer.getDefaultContainer();
        container.addVisualizer(toolName, visualizer);
        container.showup();

        activeSession.addVisualizer(visualizer);

        return visualizer;
    }

    private void notifySessionRemoved(DLightSession session) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.sessionRemoved(session);
            }
        }
    }

    private void notifySessionAdded(DLightSession session) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.sessionAdded(session);
            }
        }
    }

    private void notifySessionActivated(DLightSession oldActiveSession, DLightSession newActiveSession) {
        synchronized (sessionListeners) {
            for (DLightSessionListener l : sessionListeners) {
                l.activeSessionChanged(oldActiveSession, newActiveSession);
            }
        }
    }

    private void resetContext(ExecutionContext context) {
        // TODO: implement
    }

    private static String loc(String key) {
        return NbBundle.getMessage(DLightManager.class, key);
    }

    private static String loc(String key, Object param1) {
        return NbBundle.getMessage(DLightManager.class, key, param1);
    }

    public void startSession(DLightSessionHandler handler) {
        DLightSessionInternalReference reference = DLightSessionHandlerAccessor.getDefault().getSessionReferenceImpl(handler);
        if (!(reference instanceof DLightSession)) {
            throw new IllegalArgumentException("Illegal Argument, reference you are trying to use " +
                    "to start D-Light session is invalid");//NOI18N
        }

        startSession((DLightSession) reference);
    }

    public void stopSession(DLightSessionHandler handler) {
        DLightSessionInternalReference reference = DLightSessionHandlerAccessor.getDefault().getSessionReferenceImpl(handler);

        if (!(reference instanceof DLightSession)) {
            throw new IllegalArgumentException("Illegal Argument, reference you are trying to use " +
                    "to stop D-Light session is invalid");//NOI18N
        }

        stopSession((DLightSession) reference);
    }

    public void revalidateSessions() {
        for (DLightSession session : sessions) {
            session.revalidate();
        }
    }

    public void mouseClickedOnIndicator(Indicator source) {
        openVisualizer(IndicatorAccessor.getDefault().getToolName(source),
                IndicatorAccessor.getDefault().getVisualizerConfiguration(source));
    }
}
