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
package org.netbeans.dlight.core.stack.storage;

import java.util.List;
import org.netbeans.dlight.core.stack.model.FunctionCall;
import org.netbeans.dlight.core.stack.model.FunctionMetric;

/**
 * @author Alexey Vladykin
 */
public interface StackDataStorage {//extends StackSupport {

  public static final String STACK_DATA_STORAGE_TYPE_ID = "stack";
  public static final String STACK_METADATA_VIEW_NAME = "DtraceStack";

   /**
     * Submits new stack to the storage and returns its id.
     *
     * @param stack  call stack represented as a list of function names.
     * @param cpuId  CPU executing given stack
     * @param threadId  thread executing given stack
     * @param timestamp  nanosecond time when the stack was seen
     * @return stack id which uniquely identifies the sequence of function calls
     */
  int getStackId(List<CharSequence> stack, int cpuId, int threadId, long timestamp);

  /**
   * Submits new stack (sample) to the storage.
   *
   * @param stack  call stack represented as a list of function names,
   *      leaf function of the stack goes last in the list
   * @param cpuId  CPU executing given stack
   * @param threadId  thread executing given stack
   * @param sampleTimestamp  nanosecond time when the stack was seen
   * @param sampleDuration  number of nanoseconds the program spent in this stack
   */
  void putStack(List<CharSequence> stack, int cpuId, int threadId, long sampleTimestamp, long sampleDuration);

  List<Long> getPeriodicStacks(long startTime, long endTime, long interval);

  List<FunctionMetric> getMetricsList();

  List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate);

  List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate);

  List<FunctionCall> getHotSpotFunctions(FunctionMetric metric, int limit);
}
