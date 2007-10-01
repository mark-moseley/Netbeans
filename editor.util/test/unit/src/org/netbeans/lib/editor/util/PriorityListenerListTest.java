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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.editor.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.io.NbMarshalledObject;


/**
 * Test of PriorityListenerList correctness.
 *
 * @author mmetelka
 */
public class PriorityListenerListTest extends NbTestCase {
    
    private static final boolean debug = false;
    
    private static final int SET_RATIO_2 = 50;
    
    public PriorityListenerListTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testAddAndRemoveListenersOnThreeLevels() {
        int TEST_PRIORITY_1 = 0;
        int TEST_PRIORITY_2 = 3;
        int TEST_PRIORITY_3 = 2;
        
        PriorityListenerListCouple couple = new PriorityListenerListCouple();
        L l1 = new L();
        L l11 = new L();
        L l2 = new L();
        L l21 = new L();
        L l3 = new L();
        couple.add(l1, TEST_PRIORITY_1);
        couple.add(l2, TEST_PRIORITY_2);
        couple.add(l3, TEST_PRIORITY_3);
        couple.add(l21, TEST_PRIORITY_2);
        couple.add(l11, TEST_PRIORITY_1);
        couple.remove(l1, TEST_PRIORITY_1);
        couple.remove(l2, TEST_PRIORITY_1); // should do nothing
        couple.remove(l2, TEST_PRIORITY_2);
        couple.remove(l21, TEST_PRIORITY_2);
        couple.remove(l3, TEST_PRIORITY_3); // should remove the levels 2 and 3
        couple.checkLastPriority(1);
        couple.add(l3, TEST_PRIORITY_3);
    }
    
    public void testNegativePriorities() {
        try {
            PriorityListenerList<EventListener> ll = new PriorityListenerList<EventListener>();
            ll.add(new L(), -1);
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // Invalid priority properly catched
        }

        try {
            PriorityListenerList<EventListener> ll = new PriorityListenerList<EventListener>();
            ll.remove(new L(), -1);
            fail("Should not get here");
        } catch (IndexOutOfBoundsException e) {
            // Invalid priority properly catched
        }
    }
    
    public void testSerialization() throws Exception {
        PriorityListenerList<EventListener> ll = new PriorityListenerList<EventListener>();
        ll.add(new L(), 3);
        ll.add(new L(), 1);
        ll.add(new L(), 1);
        
        NbMarshalledObject mo = new NbMarshalledObject(ll);
        PriorityListenerList sll = (PriorityListenerList)mo.get();
        EventListener[][] lla = ll.getListenersArray();
        EventListener[][] slla = sll.getListenersArray();
        assertEquals(lla.length, slla.length);
        for (int priority = lla.length - 1; priority >= 0; priority--) {
            assertEquals(lla[priority].length, slla[priority].length);
        }
    }
    
    private static final class L implements EventListener, Serializable {
        
        static final long serialVersionUID = 12345L;
        
        private int notified;
        
        public void notifyChange() {
            notified++;
        }
        
        public int getNotified() {
            return notified;
        }
        
    }

    private static final class PriorityListenerListImitation extends HashMap<Integer,List<EventListener>> {

        public synchronized void add(EventListener listener, int priority) {
            assertTrue(priority >= 0);
            // Add to begining so that fired as last (comply with PriorityListenerList)
            getList(priority, true).add(0, listener);
        }
        
        public synchronized void remove(EventListener listener, int priority) {
            assertTrue(priority >= 0);
            List<EventListener> l = getList(priority, false);
            for (int i = l.size() - 1; i >= 0; i--) {
                if (l.get(i) == listener) {
                    l.remove(i);
                    break;
                }
            }
        }
        
        public synchronized List<EventListener> getList(int priority) {
            return getList(priority, false);
        }
        
        public synchronized void checkEquals(PriorityListenerList<EventListener> priorityListenerList) {
            // Check the same listeners are stored in imitation
            EventListener[][] listenersArray = priorityListenerList.getListenersArray();
            for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
                EventListener[] listeners = listenersArray[priority];
                for (int i = listeners.length - 1; i >= 0; i--) {
                    assertTrue(getList(priority).get(i) == listeners[i]);
                }
            }
            
            // Check there are no extra priorities in the imitation
            for (Map.Entry<Integer,List<EventListener>> entry : entrySet()) {
                if (entry.getValue().size() > 0) {
                    assertTrue (entry.getKey() < listenersArray.length);
                }
            }
        }
        
        private List<EventListener> getList(int priority, boolean forceCreation) {
            List<EventListener> l = get(priority);
            if (l == null) {
                if (forceCreation) {
                    l = new ArrayList<EventListener>();
                    put(priority, l);
                } else { // just getting the value
                    l = Collections.emptyList();
                }
            }
            return l;
        }

    }

    private static final class PriorityListenerListCouple {
        
        PriorityListenerList<EventListener> priorityListenerList;
        
        PriorityListenerListImitation imitation;
        
        public PriorityListenerListCouple() {
            priorityListenerList = new PriorityListenerList<EventListener>();
            imitation = new PriorityListenerListImitation();
        }
        
        public void add(EventListener listener, int priority) {
            priorityListenerList.add(listener, priority);
            imitation.add(listener, priority);
            imitation.checkEquals(priorityListenerList);
        }
        
        public void remove(EventListener listener, int priority) {
            priorityListenerList.remove(listener, priority);
            imitation.remove(listener, priority);
            imitation.checkEquals(priorityListenerList);
        }
        
        public void checkLastPriority(int priority) {
            assertTrue(priorityListenerList.getListenersArray().length - 1 == priority);
        }
        
    }
    
}
