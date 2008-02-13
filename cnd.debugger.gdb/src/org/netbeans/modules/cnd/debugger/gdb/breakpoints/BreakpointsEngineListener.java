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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.openide.util.RequestProcessor;


/**
 * Listens on GdbDebugger.PROP_STATE and DebuggerManager.PROP_BREAKPOINTS, and
 * and creates XXXBreakpointImpl classes for all GdbBreakpoints.
 *
 * @author   Gordon Prieur (Copied from Jan Jancura's JPDA implementation)
 */
public class BreakpointsEngineListener extends LazyActionsManagerListener 
		implements PropertyChangeListener, DebuggerManagerListener {
    
    private GdbDebugger         debugger;
    private Session             session;
    private BreakpointsReader   breakpointsReader;
    private HashMap<Breakpoint, BreakpointImpl> breakpointToImpl = new HashMap<Breakpoint, BreakpointImpl>();

    public BreakpointsEngineListener(ContextProvider lookupProvider) {
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        session = (Session) lookupProvider.lookupFirst(null, Session.class);
        debugger.addPropertyChangeListener(this);
        breakpointsReader = PersistenceManager.findBreakpointsReader();
    }
    
    protected void destroy() {
        debugger.removePropertyChangeListener(GdbDebugger.PROP_STATE, this);
        DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
        removeBreakpointImpls();
    }
    
    public String[] getProperties() {
        return new String[] {"asd"}; // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (pname.equals(GdbDebugger.PROP_STATE)) {
            if (evt.getNewValue() == GdbDebugger.STATE_LOADING) {
                int count = createBreakpointImpls();
                DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
                if (count == 0) { // no breakpoints
                    debugger.setReady();
                }
            }
        } else if (pname.equals(GdbDebugger.PROP_SHARED_LIB_LOADED)) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                   sharedLibLoaded(); 
                }
            });
            
        }
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        createBreakpointImpl(breakpoint);
    }    

    public void breakpointRemoved(Breakpoint breakpoint) {
        removeBreakpointImpl(breakpoint);
    }
    
    private int createBreakpointImpls() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
        int count = 0;
	
	if (k > 0) {
	    for (i = 0; i < k; i++) {
		if (bs[i] instanceof GdbBreakpoint) {
		    createBreakpointImpl(bs[i]);
                    count++;
		}
	    }
	}
        return count;
    }

    private void createBreakpointImpl(Breakpoint b) {
        if (breakpointToImpl.containsKey(b)) {
	    return;
	}
        if (b instanceof LineBreakpoint) {
            breakpointToImpl.put(b, new LineBreakpointImpl((LineBreakpoint) b,
			breakpointsReader, debugger, session));
        } else if (b instanceof FunctionBreakpoint) {
            breakpointToImpl.put(b, new FunctionBreakpointImpl(
			(FunctionBreakpoint) b, breakpointsReader, debugger, session));
        }
    }
    
    private void removeBreakpointImpls() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
	    if (bs[i] instanceof GdbBreakpoint) {
		removeBreakpointImpl(bs [i]);
	    }
	}
    }

    private void removeBreakpointImpl(Breakpoint b) {
        BreakpointImpl impl = breakpointToImpl.get(b);
        if (impl != null) {
            impl.remove();
            breakpointToImpl.remove(b);
	}
    }
    
    private void sharedLibLoaded() {
        for (Breakpoint bp : DebuggerManager.getDebuggerManager().getBreakpoints()) {
            if (bp.getValidity() == Breakpoint.VALIDITY.INVALID) {
                BreakpointImpl impl = breakpointToImpl.get(bp);
                if (impl != null) {
                    impl.setState(BreakpointImpl.BPSTATE_UNVALIDATED);
                    impl.update();
                }
            }
        }
    }
    
    public Breakpoint[] initBreakpoints() {return new Breakpoint[0];}

    // unused methods
    public void initWatches() {}
    public void sessionAdded(Session session) {}
    public void sessionRemoved(Session session) {}
    public void watchAdded(Watch watch) {}
    public void watchRemoved(Watch watch) {}
    public void engineAdded(DebuggerEngine engine) {}
    public void engineRemoved(DebuggerEngine engine) {}
}
