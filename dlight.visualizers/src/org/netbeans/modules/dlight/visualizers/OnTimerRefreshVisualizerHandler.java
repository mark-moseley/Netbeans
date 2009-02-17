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
package org.netbeans.modules.dlight.visualizers;

import java.util.concurrent.Callable;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.management.api.DLightSessionListener;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.netbeans.modules.dlight.util.TimerTaskExecutionService;

/**
 *
 */
class OnTimerRefreshVisualizerHandler
        implements SessionStateListener, DLightSessionListener {

    private static final TimerTaskExecutionService service =
            TimerTaskExecutionService.getInstance();
    private final VisualizerUpdateTask timerTask = new VisualizerUpdateTask();
    private final OnTimerTask task;
    private final int timerFactor;
    private boolean timerIsActive = false;
    private SessionState currentSessionState = null;

    protected OnTimerRefreshVisualizerHandler(OnTimerTask task, int timerFactor) {
        this.task = task;
        this.timerFactor = timerFactor;
        DLightManager.getDefault().addDLightSessionListener(this);
        DLightManager.getDefault().getActiveSession().addSessionStateListener(this);
        currentSessionState = DLightManager.getDefault().getActiveSession().getState();
    }

    protected boolean isSessionRunning() {
        return (currentSessionState == SessionState.RUNNING);
    }

    protected boolean isSessionPaused() {
        return (currentSessionState == SessionState.PAUSED);
    }

    protected boolean isSessionAnalyzed() {
        return (currentSessionState == SessionState.ANALYZE);
    }

    synchronized void startTimer() {
        if (!timerIsActive) {
            service.registerTimerTask(timerTask, timerFactor);
            timerIsActive = true;
        }
    }

    synchronized void stopTimer() {
        if (timerIsActive) {
            service.unregisterTimerTask(timerTask);
            timerIsActive = false;
            task.timerStopped();
        }
    }

    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        currentSessionState = newState;

        if (timerIsActive && (newState == SessionState.PAUSED || newState == SessionState.ANALYZE)) {
            stopTimer();
            return;
        }

        if (!timerIsActive && (newState == SessionState.STARTING || newState == SessionState.RUNNING)) {
            startTimer();
            return;
        }
    }

    public void activeSessionChanged(DLightSession oldSession, DLightSession newSession) {
        if (oldSession != null) {
            oldSession.removeSessionStateListener(this);
            stopTimer();
        }

        if (newSession != null) {
            newSession.addSessionStateListener(this);
            sessionStateChanged(newSession, SessionState.CONFIGURATION, newSession.getState());
        }
    }

    public void sessionAdded(DLightSession newSession) {
    }

    public void sessionRemoved(DLightSession removedSession) {
        if (removedSession != null) {
            removedSession.removeSessionStateListener(this);
            stopTimer();
        }
    }

    private class VisualizerUpdateTask implements Callable<Integer> {

        public Integer call() throws Exception {
            return task.onTimer();
        }
    }
}
