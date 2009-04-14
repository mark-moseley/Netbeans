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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 * ProcDataProvider engine for Solaris.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProviderSolaris implements ProcDataProvider.Engine {

    private static final String PSRINFO = "/usr/sbin/psrinfo"; // NOI18N

    private final ProcDataProvider provider;
    private int cpuCount;

    public ProcDataProviderSolaris(ProcDataProvider provider, ExecutionEnvironment env) {
        this.provider = provider;
        NativeProcessBuilder npb = new NativeProcessBuilder(env, PSRINFO);
        try {
            int onlineCpuCount = 0;
            NativeProcess np = npb.call();
            BufferedReader r = new BufferedReader(new InputStreamReader(np.getInputStream()));
            try {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.contains("on-line")) { // NOI18N
                        ++onlineCpuCount;
                    }
                }
            } finally {
                r.close();
            }
            cpuCount = onlineCpuCount;
        } catch (IOException ex) {
            DLightLogger.instance.severe(ex.getMessage());
            cpuCount = 1;
        }
    }

    public String getCommand(int pid) {
        return "while od -v -t x4 -N 64 /proc/" + pid + "/usage; do sleep 1; done"; // NOI18N
    }

    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return InputProcessors.bridge(new SolarisProcLineProcessor());
    }

    private class SolarisProcLineProcessor implements LineProcessor {

        private double prevTime;
        private double currTime;
        private double prevUsrTime;
        private double currUsrTime;
        private double prevSysTime;
        private double currSysTime;

        @Override
        public void processLine(String line) {
            // Output looks like:
            // 0000000 00000000 00000002 00011964 26bfb1c0
            // 0000020 00011923 26fdb58a 00000000 00000000
            // 0000040 00000081 20cd90ca 0000007e 2b4e8e04
            // 0000060 00000000 14c34ae9 00000000 060272d8
            // 0000100
            StringTokenizer tokenizer = new StringTokenizer(line);
            try {
                String firstToken = tokenizer.nextToken();
                if ("0000000".equals(firstToken)) { // NOI18N
                    prevTime = currTime;
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    long seconds = parseLong(tokenizer.nextToken());
                    long nanos = parseLong(tokenizer.nextToken());
                    currTime = time(seconds, nanos);
                } else if ("0000040".equals(firstToken)) { // NOI18N
                    prevUsrTime = currUsrTime;
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    long usrSeconds = parseLong(tokenizer.nextToken());
                    long usrNanos = parseLong(tokenizer.nextToken());
                    currUsrTime = time(usrSeconds, usrNanos);
                } else if ("0000060".equals(firstToken)) { // NOI18N
                    prevSysTime = currSysTime;
                    long sysSeconds = parseLong(tokenizer.nextToken());
                    long sysNanos = parseLong(tokenizer.nextToken());
                    currSysTime = time(sysSeconds, sysNanos);
                    if (0 < prevTime) {
                        double deltaTime = (currTime - prevTime) * cpuCount;
                        float usrPercent = percent(currUsrTime - prevUsrTime, deltaTime);
                        float sysPercent = percent(currSysTime - prevSysTime, deltaTime);
                        DataRow row = new DataRow(
                                ProcDataProviderConfiguration.CPU_TABLE.getColumnNames(),
                                Arrays.asList(usrPercent, sysPercent));
                        provider.notifyIndicators(row);
                    }
                }
            } catch (NoSuchElementException ex) {
                // silently ignore malformed line
            } catch (NumberFormatException ex) {
                // silently ignore malformed line
            }
        }

        public void reset() {
        }

        public void close() {
        }
    }

    private static long parseLong(String value) {
        return Long.parseLong(value, 16);
    }

    private static double time(long seconds, long nanos) {
        return seconds + nanos / 1e9;
    }

    private static float percent(double value, double total) {
        if (0 < total) {
            if (value <= 0) {
                return 0f;
            }
            if (total <= value) {
                return 100f;
            } else {
                return (float)(100f * value / total);
            }
        } else {
            return 0f;
        }
    }
}
