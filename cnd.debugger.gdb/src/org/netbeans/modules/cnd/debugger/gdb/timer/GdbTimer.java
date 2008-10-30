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

package org.netbeans.modules.cnd.debugger.gdb.timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for performance testing. This file is not intended to display any information to
 * users. So all strings should be marked NOI18N.
 *
 * @author gordon
 */
public abstract class GdbTimer {
    
    public enum TimerType {Dummy, Default, Custom};
    
    private static boolean enabled = Boolean.getBoolean("org.netbeans.modules.cnd.gdb.timer.GdbTimer");
    
    private static GdbTimer dummy = new GdbDummyTimer();
    
    private static GdbTimer default_instance;
    
    private static Map<String, GdbTimer> map = new HashMap();
    
    /** GdbTimer factory */
    public static GdbTimer getTimer(String name) {
        if (Boolean.getBoolean("org.netbeans.modules.cnd.gdb.timer.GdbTimer." + name)) {
            GdbTimer timer = map.get(name);
            
            if (timer == null) {
                timer = new GdbTimerImpl();
                map.put(name, timer);
            }
            return timer;
        } else {
            return dummy;
        }
    }
    
    public static void release(String name) {
        map.remove(name);
    }
        
    
    /** Start the timer running */
    public abstract void start(String msg);
    
    /**
     * Start the timer running. This form of the command if for situations where start will be
     * called mulitple times but we want to ignore subsequent calls. For instance, if we're
     * timing multiple steps we start on the 1st one and ignore the next count start calls.
     *
     * @param msg The message to use as a label
     * @param count Number of starts to allow before throwing IllegalStateException
     */
    public abstract void start(String msg, int count);
    
    public abstract void reset();
    
    /** Mark an intermediate time */
    public abstract void mark(String msg);
    
    /** Stop the timer and mark the time */
    public abstract void stop(String msg);
    
    /** Restart the timer and mark the time */
    public abstract void restart(String msg);
    
    /** Signals timing is done and this timer can be reused */
    public abstract void free();
    
    /** Log the timer information */
    public abstract void report(String msg);
    
    /** Return the skip count */
    public abstract int getSkipCount();
}
