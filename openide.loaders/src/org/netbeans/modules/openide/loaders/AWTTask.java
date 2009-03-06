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

package org.netbeans.modules.openide.loaders;

import java.util.logging.Level;

import java.awt.EventQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.openide.util.Mutex;

/** A special task designed to run in AWT thread.
 * It will fire itself immediatelly.
 */
public final class AWTTask extends org.openide.util.Task {
    static final LinkedBlockingQueue<AWTTask> PENDING = new LinkedBlockingQueue<AWTTask>();
    private static final Runnable PROCESSOR = new Processor();

    private boolean executed;

    public AWTTask (Runnable r) {
        super (r);
        PENDING.add(this);
        Mutex.EVENT.readAccess (PROCESSOR);
    }

    @Override
    public void run () {
        if (!executed) {
            try {
                super.run ();
            } catch (ThreadDeath t) {
                throw t;
            } catch (Throwable t) {
                Logger.getLogger("org.openide.awt.Toolbar").log(Level.WARNING, "Error in AWT task", t); // NOI18N
            } finally {
                executed = true;
            }
        }
    }

    @Override
    public void waitFinished () {
        if (EventQueue.isDispatchThread ()) {
            run ();
        } else {
            /*
            if (DataObjectAccessor.DEFAULT.isInstancesThread()) {
                try {
                    super.waitFinished(100);
                } catch (InterruptedException ex) {
                    // ok, go on
                }
            } else {*/
                super.waitFinished ();
            //}
        }
    }

    public static final void flush() {
        PROCESSOR.run();
    }

    private static final class Processor implements Runnable {
        public void run() {
            assert EventQueue.isDispatchThread();
            for(;;) {
                AWTTask t = PENDING.poll();
                if (t == null) {
                    return;
                }
                t.run();
            }
        }
    }

}
