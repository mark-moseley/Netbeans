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
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.Pumping.Section;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.downloader.services.EmptyQueueListener;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class WorkabilityTest extends WithServerTestCase {
  
  public void testStepByStepWorkability() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final DownloadListener listener = new EmptyQueueListener() {
      int i = 0;
      public void pumpingStateChange(String id) {
        final Pumping pumping = queue.getById(id);
        System.out.println("pumping file " + pumping.outputFile() + " " + pumping.state());
        if (pumping.state() == Pumping.State.FINISHED) {
          assertEquals(pumping.length(), TestDataGenerator.testFileSizes[i++]);
          synchronized (WorkabilityTest.this) {
            WorkabilityTest.this.notify();
          }
        } else if (pumping.state() == Pumping.State.FAILED) {fail();}
      }
      public void pumpingUpdate(String id) {
        //  System.out.print("Update downloading file.." + queue.getById(id).outputFile().getName());
        //    System.out.println("  Size = " + downperc(queue.getById(id)));
      }
      
      private long downperc(Pumping pumping) {
        long size = 0;
        for (Section section : pumping.getSections()) {
          size +=section.offset() - section.getRange().getFirst();
        }
        return /*pumping.length() > 0 ? size * 100 / pumping.length():*/ size;
      }
    };
    queue.addListener(listener);
    assertFalse(queue.isActive());
    queue.invoke();
    assertTrue(queue.isActive());
    int i = 0 ;
    while (i < TestDataGenerator.testUrls.length) {
      synchronized (this) {
        queue.add(TestDataGenerator.testUrls[i], MyTestCase.testOutput);
        try {
          wait();
        } catch (InterruptedException ex) {
          fail();
        }
      }
      i++;
    }
    queue.terminate();
  }
  
  public void testConcurrentlyWorkability() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final DownloadListener listener = new EmptyQueueListener() {
      int i = 0;
      public void pumpingStateChange(String id) {
        final Pumping pumping = queue.getById(id);
        System.out.println("pumping file " + pumping.outputFile() + " " + pumping.state());
        if (pumping.state() == Pumping.State.FINISHED) {
          i++;
          if (i == TestDataGenerator.testUrls.length) {
            synchronized (WorkabilityTest.this) {
              WorkabilityTest.this.notify();
            }
          }
        } else if (pumping.state() == Pumping.State.FAILED) {fail();}
      }
      
      public void pumpingUpdate(String id) {
        //  System.out.print("Update downloading file.." + queue.getById(id).outputFile().getName());
        //    System.out.println("  Size = " + downperc(queue.getById(id)));
      }
      
      private long downperc(Pumping pumping) {
        long size = 0;
        for (Section section : pumping.getSections()) {
          size +=section.offset() - section.getRange().getFirst();
        }
        return /*pumping.length() > 0 ? size * 100 / pumping.length():*/ size;
      }
    };
    queue.addListener(listener);
    queue.invoke();
    int i = 0 ;
    while (i < TestDataGenerator.testUrls.length) {
      queue.add(TestDataGenerator.testUrls[i], MyTestCase.testOutput);
      i++;
    }
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException ex) {
        fail();
      }
    }
    queue.terminate();
  }
}
