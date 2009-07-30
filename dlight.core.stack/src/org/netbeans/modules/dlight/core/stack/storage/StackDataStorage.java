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
package org.netbeans.modules.dlight.core.stack.storage;

import java.util.List;
import org.netbeans.modules.dlight.api.stack.ThreadDump;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;

/**
 * @author Alexey Vladykin
 */
public interface StackDataStorage {//extends StackSupport {

    public static final String STACK_DATA_STORAGE_TYPE_ID = "stack"; //NOI18N
    public static final String STACK_METADATA_VIEW_NAME = "DtraceStack"; //NOI18N

    /**
     * Submits new stack (sample) to the storage.
     *
     * @param stack  call stack represented as a list of function names,
     *      leaf function of the stack goes last in the list
     * @param sampleDuration  number of nanoseconds the program spent in this stack
     * @return
     */
    int putStack(List<CharSequence> stack, long sampleDuration);

    List<Long> getPeriodicStacks(long startTime, long endTime, long interval);

    List<FunctionMetric> getMetricsList();

    List<FunctionCallWithMetric> getCallers(FunctionCallWithMetric[] path, boolean aggregate);

    List<FunctionCallWithMetric> getCallees(FunctionCallWithMetric[] path, boolean aggregate);

    List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, int limit);

    List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata, List<Column> metricsColumn, FunctionDatatableDescription functionDescription);

    /**
     * Returns stack trace (stacks for all threads) for the moment of timestamp 
     * (i.e. all returned callstacks will be with timestamp &lt;= than the
     * passed one). Also the state of the thread with id == threadID will be
     * threadState.
     *
     * @param threadID
     * @param timestamp
     * @return
     */
    ThreadDump getThreadDump(long timestamp, int threadID, int threadState);
}
