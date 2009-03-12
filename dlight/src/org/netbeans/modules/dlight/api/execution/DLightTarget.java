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
package org.netbeans.modules.dlight.api.execution;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * D-Light Target.Target to be d-lighted, it can be anything: starting from shell script to
 *  the whole system.<br>
 * You should implement this interface in case you have your own
 * target type.
 * Default implementation {@link org.netbeans.modules.dlight.api.support.NativeExecutableTarget} can
 * be used.
 */
public abstract class DLightTarget {

    //@GuardedBy("this")
    private final List<DLightTargetListener> listeners;
    private final DLightTargetExecutionService executionService;


    static {
        DLightTargetAccessor.setDefault(new DLightTargetAccessorImpl());
    }

    /**
     * Create new target to be d-lighted, as a parameter service which
     * can start and terminated target should be passed
     * @param executionService service to start and terminate target
     */
    protected DLightTarget(DLightTarget.DLightTargetExecutionService executionService) {
        this.executionService = executionService;
        this.listeners = new ArrayList<DLightTargetListener>();
    }

    private final DLightTargetExecutionService getExecutionService() {
        return executionService;
    }

    /**
     * Adds target listener, all listeners will be notofied about
     * target state change.
     * @param listener add listener
     */
    public final void addTargetListener(DLightTargetListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Remove target listener
     * @param listener listener to remove from the list
     */
    public final void removeTargetListener(DLightTargetListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifyes listeners target state changed in separate thread
     * @param oldState state target was
     * @param newState state  target is
     */
    protected final void notifyListeners(final DLightTarget.State oldState,
            final DLightTarget.State newState) {
        synchronized (this) {
            final CountDownLatch doneFlag = new CountDownLatch(listeners.size());

            // Will do notification in parallel, but wait until all listeners
            // finish processing of event.
            for (final DLightTargetListener l : listeners) {
                DLightExecutorService.submit(new Runnable() {

                    public void run() {
                        try {
                            l.targetStateChanged(DLightTarget.this, oldState, newState);
                        } finally {
                            doneFlag.countDown();
                        }
                    }
                }, "Notifying " + l); // NOI18N
            }

            try {
                doneFlag.await();
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Returns {@link org.netbeans.modules.nativeexecution.api.ExecutionEnvironment} this
     * target will be run at
     * @return {@link org.netbeans.modules.nativeexecution.api.ExecutionEnvironment} to run this target at
     */
    public abstract ExecutionEnvironment getExecEnv();

    /**
     * Returns current target state as {@link org.netbeans.modules.dlight.api.execution.DLightTarget.State}
     * @return target current state
     */
    public abstract DLightTarget.State getState();

    /**
     * States target can be at
     */
    public enum State {

        /**
         * Initial state
         */
        INIT,
        /**
         * Starting state
         */
        STARTING,
        /**
         * Running state
         */
        RUNNING,
        /**
         * Target is done
         */
        DONE,
        /**
         * Target is failed
         */
        FAILED,
        /**
         * Target is Stopped
         */
        STOPPED,
        /**
         * Target is terminated
         */
        TERMINATED,
    }

    /**
     * This service should be implemented to run target along
     * with DLightTarget implementation
     * @param <T> target to execute
     */
    public interface DLightTargetExecutionService<T extends DLightTarget> {

        /**
         * Start target
         * @param target targe to start
         * @param executionEnvProvider  execution enviroment provider
         */
        public void start(T target, ExecutionEnvVariablesProvider executionEnvProvider);

        /**
         * Terminate target
         * @param target target to terminate
         */
        public void terminate(T target);
    }

    /**
     * This provider is supposed to be implemented by the implementator of
     * {@link org.netbeans.modules.dlight.spi.collector.DataCollector} or
     * {@link org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider> if
     * some additional setting up is required before target is stared as an example
     * LD_PRELOAD can be considered
     */
    public interface ExecutionEnvVariablesProvider {

        /**
         * Returns enviroment variables map (name - value) which should
         * be set up before DLightTarget is started
         * @param target  target that is going to start
         * @return enviroment variables map to set up before target is starting
         * @throws ConnectException in case connection to target host is needed,
         *      but the host is not connected yet
         */
        Map<String, String> getExecutionEnv(DLightTarget target) throws ConnectException;
    }

    private static final class DLightTargetAccessorImpl extends DLightTargetAccessor {

        @Override
        public DLightTargetExecutionService getDLightTargetExecution(DLightTarget target) {
            return target.getExecutionService();
        }
    }
}
