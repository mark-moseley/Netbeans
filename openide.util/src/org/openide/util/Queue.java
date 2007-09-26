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

package org.openide.util;

import java.util.LinkedList;

/** Queue of objects. When there is no object in the queue the process
* is suspended till some arrives.
* Implementation appears to be LIFO.
*
* @author Jaroslav Tulach
* @deprecated Use {@link java.util.concurrent.BlockingQueue} instead.
*/
@Deprecated
public class Queue<T> extends Object {
    /** Queue enumeration */
    private LinkedList<T> queue = new LinkedList<T>();

    /** Adds new item.
    * @param o object to add
    */
    public synchronized void put(T o) {
        queue.add(o);
        notify();
    }

    /** Gets an object from the queue. If there is no such object the
    * thread is suspended until some object arrives
    *
    * @return object from the queue
    */
    public synchronized T get() {
        for (;;) {
            if (queue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            } else {
                break;
            }
        }

        return queue.removeFirst();
    }
}
