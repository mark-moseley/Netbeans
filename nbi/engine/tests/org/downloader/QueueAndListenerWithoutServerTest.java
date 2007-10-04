/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */
public class QueueAndListenerWithoutServerTest extends MyTestCase {
  
  public void testInvokeTerminate() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final ActionsTracer listener = new ActionsTracer(queue);
    queue.invoke();
    assertEquals(1, listener.actions.size());
    assertEquals("invoke", listener.actions.get(0).getFirst());
    queue.terminate();
    assertEquals(2, listener.actions.size());
    assertEquals("terminate", listener.actions.get(1).getFirst());
  }
  
  public void testAddDelete() throws MalformedURLException {
    final PumpingsQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final ActionsTracer listener = new ActionsTracer(queue);
    final Pumping pumping = queue.add(new URL("http://127.0.0.1:8080/testurl.data"));
    assertTrue(queue.toArray().length == 1);
    assertEquals(1, listener.actions.size());
    assertEquals("add", listener.actions.get(0).getFirst());
    assertEquals(pumping.getId(), listener.actions.get(0).getSecond());
    queue.delete(pumping.getId());
    assertEquals(2, listener.actions.size());
    assertEquals("delete", listener.actions.get(1).getFirst());
    assertEquals(pumping.getId(), listener.actions.get(1).getSecond());
    assertTrue(queue.toArray().length == 0);
  }
  
  public void testReset() throws MalformedURLException {
    final PumpingsQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final ActionsTracer listener = new ActionsTracer(queue);
    final Pumping pumping1 = queue.add(new URL("http://127.0.0.1:8080/testurl.data"));
    final Pumping pumping2 = queue.add(new URL("http://127.0.0.1:8080/testurl.data"));
    final Pumping pumping3 = queue.add(new URL("http://127.0.0.1:8080/testurl.data"));
    assertEquals(3, listener.actions.size());
    assertEquals("add", listener.actions.get(0).getFirst());
    assertEquals(pumping1.getId(), listener.actions.get(0).getSecond());
    assertEquals("add", listener.actions.get(1).getFirst());
    assertEquals(pumping2.getId(), listener.actions.get(1).getSecond());
    assertEquals("add", listener.actions.get(2).getFirst());
    assertEquals(pumping3.getId(), listener.actions.get(2).getSecond());
    queue.reset();
    assertEquals(7, listener.actions.size());
    assertEquals("delete", listener.actions.get(3).getFirst());
    assertEquals(pumping3.getId(), listener.actions.get(3).getSecond());
    assertEquals("delete", listener.actions.get(4).getFirst());
    assertEquals(pumping1.getId(), listener.actions.get(4).getSecond());
    assertEquals("delete", listener.actions.get(5).getFirst());
    assertEquals(pumping2.getId(), listener.actions.get(5).getSecond());
    assertEquals("reset", listener.actions.get(6).getFirst());
  }
  
  public void testResetInAction() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final ActionsTracer listener = new ActionsTracer(queue);
    queue.invoke();
    queue.reset();
    queue.terminate();
    assertEquals(5, listener.actions.size());
    assertEquals("invoke", listener.actions.get(0).getFirst());
    assertEquals("terminate", listener.actions.get(1).getFirst());
    assertEquals("reset", listener.actions.get(2).getFirst());
    assertEquals("invoke", listener.actions.get(3).getFirst());
    assertEquals("terminate", listener.actions.get(4).getFirst());
  }
}
