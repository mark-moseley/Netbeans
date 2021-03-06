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

package org.netbeans.modules.versioning.system.cvss;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Allows to synchronize tasks (performed in multiple
 * ClientRuntime threads) in ExecutorSupport group.
 *
 * <p>Example usage:
 * <pre>
 * ExecutorGroup group = ...;
 * group.addExecutor(...);
 * group.addExecutor(...);
 * group.addExecutor(...);
 *
 * // once executed waits until above executors finish
 * group.addBarrier(Runnable action);
 *
 * // then continue
 * group.addExecutor(...);
 * group.addExecutor(...);
 *
 * // dispatch into execution queues
 * group.execute();
 * </pre>
 *
 * @author Petr Kuzel
 */
final class ExecutorGroupBar implements ExecutorGroup.Groupable {

    private final Runnable action;
    private final ExecutorSupport[] bar;
    private ExecutorGroup group;

    /**
     * Creates barrier, with optional action
     * @param executorsBar collection of ExecutorSupports to wait for.
     *        Actually all other Groupables are relaxed, silently ignored.  
     */
    public ExecutorGroupBar(Collection executorsBar, Runnable action) {
        this.action = action;

        // ExecutorSupport.wait(...); works only for ExecutorSupports
        List filtered = new ArrayList(executorsBar.size());
        Iterator it = executorsBar.iterator();
        while (it.hasNext()) {
            ExecutorGroup.Groupable groupable = (ExecutorGroup.Groupable) it.next();
            if (groupable instanceof ExecutorSupport) {
                filtered.add(groupable);
            }
        }

        bar = (ExecutorSupport[]) filtered.toArray(new ExecutorSupport[filtered.size()]);
    }

    public void joinGroup(ExecutorGroup group) {
        this.group = group;        
    }

    /**
     * This one is blocking. It returns when all bar
     * executors and action finish (successfuly or fail).
     */
    public void execute() {
        group.enqueued(null, this);
        ExecutorSupport.wait(bar);
        if (action != null) {
            action.run();
        }
        group.finished(null, this);
    }
}
