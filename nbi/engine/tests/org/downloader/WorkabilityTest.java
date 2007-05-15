/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
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
