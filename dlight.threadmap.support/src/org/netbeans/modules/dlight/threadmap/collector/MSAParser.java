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
package org.netbeans.modules.dlight.threadmap.collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState.MSAState;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.dtrace.collector.support.DtraceParser;
import org.netbeans.modules.dlight.threadmap.storage.ThreadInfoImpl;
import org.netbeans.modules.dlight.threadmap.storage.ThreadMapDataStorage;
import org.netbeans.modules.dlight.threadmap.storage.ThreadStateImpl;

public final class MSAParser extends DtraceParser {

    private static final List<String> COLUMN_NAMES = Arrays.asList(
            MSAState.RunningUser.toString(),
            MSAState.RunningSystemCall.toString(),
            MSAState.RunningOther.toString(),
            MSAState.SleepingUserTextPageFault.toString(),
            MSAState.SleepingUserDataPageFault.toString(),
            MSAState.SleepingKernelPageFault.toString(),
            MSAState.SleepingUserLock.toString(),
            MSAState.SleepingOther.toString(),
            MSAState.WaitingCPU.toString(),
            MSAState.ThreadStopped.toString());
    private static final TimeUnit dtraceTimeUnits = TimeUnit.NANOSECONDS;
    // Thread ID to states map
    private final HashMap<Integer, int[]> accumulatedData = new HashMap<Integer, int[]>();
    private final ThreadMapDataStorage storage;
    private final long deltaTime;
    long lastTimestamp = 0;

    public MSAParser(TimeDuration frequency, DataTableMetadata metadata) {
        super(metadata);

        storage = ThreadMapDataStorage.getInstance();
        storage.init(frequency, metadata);
        deltaTime = frequency.getValueIn(dtraceTimeUnits);
    }

    @Override
    public DataRow process(String line) {
        if (line == null) {
            return null;
        }

        String[] chunks = line.split(" +"); // NOI18N

        int cpuID = Integer.parseInt(chunks[0]);
        int threadID = Integer.parseInt(chunks[1]);
        long timestamp = Long.parseLong(chunks[2]);

        ThreadInfoImpl threadInfo = storage.getThreadInfo(threadID);

        if (threadInfo == null) {
            threadInfo = new ThreadInfoImpl(threadID, "Thread " + threadID, timestamp); // NOI18N
            storage.addThreadInfo(threadInfo);
        }

        if (!accumulatedData.containsKey(threadID)) {
            accumulatedData.put(threadID, new int[20]);
        }

        int[] threadStates = accumulatedData.get(threadID);

        int total = 0;
        //                     r_u    r_s  r_o  utf  udf   kf  wcpu st ulock sleep sobjs...
        // 0 1 2                3      4    5    6    7    8    9   10   11   12   13   14   15  16    17   18   19
        // 2 2 540636781605515  999    0    0    0    0    0    0    0    0    0    0    0    0    0    0    0    0
        // 2 1 540636800605980    0    0    0    0    0    0    0    0    0    0 1000    0    0    0    0    0    0
        // 2 3 540636802604857    0    0    0    0    0    0    0    0    0    0    0    0 1000    0    0    0    0

        for (int i = 3; i < chunks.length; i++) {
            int state = Integer.parseInt(chunks[i]);

            if (i == 13 && state > 0) {
                threadStates[11] += state;
            } else if (i > 13 && state > 0) {
                threadStates[12] += state;
            } else {
                threadStates[i] += state;
            }

            total += state;
        }

        if (total == 0) {
            threadInfo.setFinishTime(timestamp);
        } else {
            threadStates[0]++;
        }

        if ((timestamp - lastTimestamp) > deltaTime) {
            lastTimestamp = timestamp;

//            System.out.println("Adding info about " + accumulatedData.size() + " threads");

            int aggregatedStates[] = new int[10];
            int sum = 0;
            for (Map.Entry<Integer, int[]> entry : accumulatedData.entrySet()) {
                int[] states = entry.getValue();
                ThreadStateImpl state = new ThreadStateImpl(timestamp, states);
                storage.addThreadState(storage.getThreadInfo(entry.getKey()), state);
                for (int i = 4; i < 14; ++i) {
                    aggregatedStates[i - 4] += states[i];
                    sum += states[i];
                }
            }

            accumulatedData.clear();

            List<Object> values = new ArrayList<Object>();
            for (int i = 0; i < aggregatedStates.length; ++i) {
                values.add(100.0 * aggregatedStates[i] / sum);
            }
            return new DataRow(COLUMN_NAMES, values);
        } else {
            return null;
        }
    }
}

