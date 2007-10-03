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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure.Queue;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureQueueTest extends TestCase {

    public void testInitialElements() {
        Set<String> initial = new HashSet(Arrays.asList("a", "b"));
        Queue<String> queue = new Queue<String>(initial);
        Set<String> dequeued = new HashSet();
        while (!queue.isEmpty()) {
            dequeued.add(queue.poll());
        }
        assertNull("The poll() method should return null at the end of the queue.", queue.poll());
        assertEquals("Should dequeue the initial elements.", initial, dequeued);
    }

    public void testCannotAddEvenDequeuedElements() {
        Set<String> initial = Collections.emptySet();
        Queue<String> queue = new Queue<String>(initial);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        // add some existing elements
        queue.offer("a");
        queue.offer("c");
        assertEquals("Should poll 'a'.", "a", queue.poll());
        assertEquals("Should poll 'b'.", "b", queue.poll());
        // add some already dequeued elements
        queue.offer("b");
        queue.offer("a");
        assertEquals("Should not poll already dequeued elements.", "c", queue.poll());
        assertNull("The queue should be empty.", queue.poll());
    }
}
