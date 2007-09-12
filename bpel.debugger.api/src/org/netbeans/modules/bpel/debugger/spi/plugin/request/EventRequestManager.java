/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.bpel.debugger.spi.plugin.request;

import java.util.List;
import org.netbeans.modules.bpel.debugger.spi.plugin.def.Position;
import org.netbeans.modules.bpel.debugger.spi.plugin.exec.ProcessInstance;

/**
 * Manages the creation and deletion of
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest}s.
 * A single implementor of this interface exists for a particuar
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngine}
 * and is accessed through
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngine#getEventRequestManager()}.
 *
 * @author Alexander Zgursky
 */
public interface EventRequestManager {
    
    /**
     * Creates a new disabled
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.ProcessInstanceCreatedRequest}.
     * The new event request is added to the list managed by this
     * EventRequestManager. Use
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest#setEnabled}
     * to activate this event request.
     *
     * @return the created event request
     */
    ProcessInstanceCreatedRequest createProcessInstanceCreatedRequest();
    
    /**
     * Creates a new disabled
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.ProcessInstanceCompletedRequest}.
     * The new event request is added to the list managed by this
     * EventRequestManager. Use
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest#setEnabled}
     * to activate this event request.
     *
     * @return the created event request
     */
    ProcessInstanceCompletedRequest createProcessInstanceCompletedRequest();
    
    /**
     * Creates a new disabled
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.BreakpointReachedRequest}
     * which is set on the given
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.def.Position}.
     * The new breakpoint is added to the list managed by this
     * EventRequestManager. Multiple breakpoints at the same
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.def.Position}
     * are permitted. Use
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest#setEnabled}
     * to activate this event request.
     *
     * @param position the position of the new breakpoint
     *
     * @return the created event request
     */
    BreakpointReachedRequest createBreakpointReachedRequest(Position position);
    
    /**
     * Creates a new disabled
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.StepCompletedRequest}.
     * The new step request is added to the list managed by this
     * EventRequestManager. Use
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest#setEnabled}
     * to activate this event request.
     * <br><br>
     * Only one pending step request is allowed per process instance.
     * <br><br>
     * Note that enabling this request doesn't automatically resume the
     * process instance. The process instance will get a chance to
     * complete the step after a call to
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.exec.ProcessInstance#resume}
     * or
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngine#resumeAllProcessInstances}.
     * <br><br>
     * Note that this event request would cause one or zero
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.StepCompletedEvent}s
     * to be sent. An instance of this request is neither removed from the list
     * nor disabled automatically by the BPEL Debugger Plugin as the
     * corresponding event is sent.
     *
     * @param processInstance the process instance in which to step
     *
     * @return the created event request
     *
     * @throws DuplicateRequestException if there is already a pending
     *  step request for the specified process instance
     */
    StepCompletedRequest createStepCompletedRequest(ProcessInstance processInstance);
    
    /**
     * Return an unmodifiable list of the enabled and disabled event requests.
     * This list is a live view of these requests and thus changes as
     * requests are added and deleted.
     *
     * @return the list of event requests
     */
    List<EventRequest> getAllRequests();
    
    /**
     * Removes an event request. The eventRequest is disabled and removed from
     * the requests managed by this EventRequestManager. Once the eventRequest
     * is deleted, no operations (for example,
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequest#setEnabled})
     * are permitted - attempts to do so will generally cause an
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.InvalidRequestStateException}.
     * No other event requests are effected.
     *
     * @param eventRequest the event request to remove
     */
    void deleteEventRequest(EventRequest eventRequest);
    
    /**
     * Removes a list of event requests.
     *
     * @param eventRequests the event requests to remove
     *
     * @see #deleteEventRequest
     */
    void deleteEventRequests(List<EventRequest> eventRequests);
}
