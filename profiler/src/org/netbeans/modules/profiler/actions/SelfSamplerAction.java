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
package org.netbeans.modules.profiler.actions;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jaroslav Bachorik
 */
public class SelfSamplerAction extends AbstractAction implements AWTEventListener {
    final private static class Singleton {
        static final SelfSamplerAction INSTANCE = new SelfSamplerAction();
    }

    // -----
    // I18N String constants
    private static final String ACTION_NAME_START = "Start sampling IDE"; // NOI18N
    private static final String ACTION_NAME_STOP = "Stop sampling and take snapshot"; // NOI18N
    private static final String ACTION_DESCR = "Will take a snapshot based on the thread dump"; // NOI18N
    private static final String THREAD_NAME = "IDE Self Sampler"; // NOI18N
    private StackTraceSnapshotBuilder builder = new StackTraceSnapshotBuilder();
    private final ThreadFactory threadFactory = new ThreadFactory() {

        public Thread newThread(Runnable r) {
            return new Thread(r, THREAD_NAME);
        }
    };
    private ScheduledExecutorService executor;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private long startTime;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    private SelfSamplerAction() {
        putValue(Action.NAME, ACTION_NAME_START);
        putValue(Action.SHORT_DESCRIPTION, ACTION_DESCR);
        putValue(Action.SMALL_ICON,
            ImageUtilities.loadImageIcon(
                "org/netbeans/modules/profiler/actions/resources/openSnapshot.png" //NOI18N
        , false)
        );
        if (System.getProperty(SelfSamplerAction.class.getName() + ".sniff") != null) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        }
        builder.setIgnoredThreads(Collections.singleton(THREAD_NAME));
    }

    public static final SelfSamplerAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        if (isRunning.compareAndSet(false, true)) {
            putValue(Action.NAME, ACTION_NAME_STOP);
            putValue(Action.SMALL_ICON,
                ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/profiler/actions/resources/modifyProfiling.png" //NOI18N
            , false)
            );
            executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
            startTime = System.currentTimeMillis();
            executor.scheduleAtFixedRate(new Runnable() {

                public void run() {
                    builder.addStacktrace(Thread.getAllStackTraces(), System.nanoTime());
                }
            }, 10, 10, TimeUnit.MILLISECONDS);
        } else if (isRunning.compareAndSet(true, false)) {
            putValue(Action.NAME, ACTION_NAME_START);
            putValue(Action.SMALL_ICON,
                    ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/profiler/actions/resources/openSnapshot.png" //NOI18N
            , false)
            );
            try {
                executor.shutdown();
                executor.awaitTermination(100, TimeUnit.MILLISECONDS);
                CPUResultsSnapshot snapshot = builder.createSnapshot(startTime, System.nanoTime());
                LoadedSnapshot loadedSnapshot = new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);
                ResultsManager.getDefault().openSnapshot(loadedSnapshot);
                builder.reset();

            } catch (CPUResultsSnapshot.NoDataAvailableException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public void eventDispatched(AWTEvent event) {
        KeyEvent kevent = (KeyEvent)event;
        if (kevent.getID() == KeyEvent.KEY_RELEASED && kevent.getKeyCode() == KeyEvent.VK_ALT_GRAPH) { // AltGr
            actionPerformed(new ActionEvent(this, event.getID(), "shortcut"));
            kevent.consume();
        }
    }


}
