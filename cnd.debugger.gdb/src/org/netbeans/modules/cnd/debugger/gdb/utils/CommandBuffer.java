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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is intended for gathering multiline responses to a single gdb command.
 * 
 * @author gordonp
 */
public class CommandBuffer {
    
    // Static parts
    public static final int STATE_NONE = 0;
    public static final int STATE_TIMEOUT = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_ERROR = 3;
    
    private static Map<Integer, CommandBuffer> map = new HashMap<Integer, CommandBuffer>();
    
    public static CommandBuffer getCommandBuffer(Integer id) {
        return map.get(id);
    }
    
    // Instance parts
    private StringBuilder buf;
    private CommandBufferCallbackProc cbproc;
    private Integer token;
    private String err;
    private int state;
    private Object lock;
    
    public CommandBuffer(int token, CommandBufferCallbackProc cbproc) {
        buf = new StringBuilder();
        this.token = new Integer(token);
        this.cbproc = cbproc;
        state = STATE_NONE;
        err = null;
        lock = new Object();
        map.put(this.token, this);
    }
    
    public CommandBuffer(int token) {
        this(token, null);
    }
    
    public void callback() {
        if (cbproc != null) {
            cbproc.callback(toString());
        }
    }
    
    public String postAndWait() {
        synchronized (lock) {
            try {
                state = STATE_TIMEOUT; // this will change unless we timeout
                lock.wait(2000);
                return toString();
            } catch (InterruptedException ex) {
            }
        }
        
        return null;
    }
    
    public Integer getID() {
        return token;
    }
    
    public void append(String line) {
        buf.append(line);
    }
    
    public void done() {
        synchronized (lock) {
            state = STATE_DONE;
            lock.notify();
        }
    }
    
    public void error(String msg) {
        synchronized (lock) {
            err = msg;
            state = STATE_ERROR;
            lock.notify();
        }
    }
    
    public String getError() {
        if (state == STATE_ERROR && err != null) {
            return err;
        }
        return null;
    }
    
    public void dispose() {
        map.remove(token);
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
