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

package org.netbeans.modules.cnd.debugger.gdb;

import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;

/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackFrame {
    private final GdbDebugger debugger;
    private final int lineNumber;
    private final String func;
    private final String file;
    private final String fullname;
    private final int frameNumber;
    private final String address;
    
    private LocalVariable[] cachedLocalVariables = null;
    private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public CallStackFrame(GdbDebugger debugger, String func, String file, String fullname, String lnum, String address, int frameNumber) {
        this.debugger = debugger;
        this.func = func;
        this.file = file;
        this.fullname = fullname;
        this.address = address;
        this.frameNumber = frameNumber;
        int lNumber = -1;
        if (lnum != null) {
            try {
                lNumber = Integer.parseInt(lnum);
            } catch (NumberFormatException ex) {
                lNumber = 1; // shouldn't happen
            }
        } else {
            lNumber = -1;
        }
        this.lineNumber = lNumber;
    }
    
    /**
     *  Get frame number.
     *
     *  @return Frame nunmber in Call Stack ("0" means top)
     */
    public int getFrameNumber() {
        return frameNumber;
    }
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @return line number associated with this this stack frame
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public String getFunctionName() {
        return func;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFileName() {
        return file;
    }
    
    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public String getFullname() {
        return fullname;
    }
    
    /**
     * @return address this stack frame is stopped in
     */
    public String getAddr() {
        return address;
    }
    
    /** Sets this frame current */
    public void makeCurrent() {
        debugger.setCurrentCallStackFrame(this);
    }
    
    /** UNCOMMENT WHEN THIS METHOD IS NEEDED. IT'S ALREADY IMPLEMENTED IN THE IMPL. CLASS.
     * Determine, if this stack frame can be poped off the stack.
     *
     * @return <code>true</code> if this frame can be poped
     *
     * public abstract boolean canPop();
     */
    
    /**
     * Pop stack frames. All frames up to and including the frame
     * are popped off the stack. The frame previous to the parameter
     * frame will become the current frame.
     */
    public void popFrame() {
        debugger.getGdbProxy().exec_finish();
    }
    
    /**
     * Returns local variables.
     * If local variables are not available returns empty array.
     *
     * NOTE: This method should <b>not</b> be called from GdbReaderRP as it can block
     * waiting for type information to be returned on that thread.
     *
     * @return local variables
     */
    public LocalVariable[] getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        assert !(SwingUtilities.isEventDispatchThread()); 

        if (cachedLocalVariables == null) {
            List<GdbVariable> list = debugger.getLocalVariables();
            int n = list.size();

            LocalVariable[] locals = new LocalVariable[n];
            for (int i = 0; i < n; i++) {
                locals[i] = new AbstractVariable(list.get(i));
            }
            cachedLocalVariables = locals;
            return locals;
        } else {
            return cachedLocalVariables;
        }
    }

    @Override
    public int hashCode() {
        // currently default hash code and equals are the optimal ones,
        // because CallStackFrames can not be equal if they are not the same object
        return super.hashCode();
    }
}
