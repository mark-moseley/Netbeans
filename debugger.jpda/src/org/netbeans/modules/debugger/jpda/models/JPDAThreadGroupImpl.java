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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;

import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * The implementation of JPDAThreadGroup.
 */
public class JPDAThreadGroupImpl implements JPDAThreadGroup {

    private ThreadGroupReference tgr;
    private JPDADebuggerImpl debugger;
    
    public JPDAThreadGroupImpl (ThreadGroupReference tgr, JPDADebuggerImpl debugger) {
        this.tgr = tgr;
        this.debugger = debugger;
    }

    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroup getParentThreadGroup () {
        ThreadGroupReference ptgr = tgr.parent ();
        if (ptgr == null) return null;
        return debugger.getThreadGroup(ptgr);
    }
    
    public JPDAThread[] getThreads () {
        List l = tgr.threads ();
        int i, k = l.size ();
        JPDAThread[] ts = new JPDAThread [k];
        for (i = 0; i < k; i++)
            ts [i] = debugger.getThread((ThreadReference) l.get (i));
        return ts;
    }
    
    public JPDAThreadGroup[] getThreadGroups () {
        List l = tgr.threadGroups ();
        int i, k = l.size ();
        JPDAThreadGroup[] ts = new JPDAThreadGroup [k];
        for (i = 0; i < k; i++)
            ts [i] = debugger.getThreadGroup((ThreadGroupReference) l.get (i));
        return ts;
    }
    
    public String getName () {
        return tgr.name ();
    }
    
    // XXX Add some synchronization so that the threads can not be resumed at any time
    public void resume () {
        List threads = tgr.threads();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            JPDAThreadImpl thread = (JPDAThreadImpl) debugger.getThread((ThreadReference) it.next());
            thread.notifyToBeResumed();
        }
        tgr.resume ();
    }
    
    // XXX Add some synchronization
    public void suspend () {
        tgr.suspend ();
        List threads = tgr.threads();
        for (Iterator it = threads.iterator(); it.hasNext(); ) {
            JPDAThreadImpl thread = (JPDAThreadImpl) debugger.getThread((ThreadReference) it.next());
            thread.notifySuspended();
        }
    }
    
}
