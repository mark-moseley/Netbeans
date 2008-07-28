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

package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Properties;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author ads
 */
public class PersistenceManager extends  DebuggerManagerAdapter {
    public static final Logger LOGGER = Logger.getLogger(PersistenceManager.class.getName());
    private static final String DEBUGGER = "debugger";      // NOI18N
    private static final String PHP_DBGP = "PHP-DBGP";      // NOI18N

    @Override
    public Breakpoint[] initBreakpoints() {
        Properties p = Properties.getDefault().getProperties(DEBUGGER).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray( PHP_DBGP ,new Breakpoint [0]);
        List<Breakpoint> validBreakpoints = new ArrayList<Breakpoint>();
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint != null) {
                breakpoint.addPropertyChangeListener(this);
                validBreakpoints.add(breakpoint);
            } else {
                LOGGER.warning("null stored in the array obtained from \"" + PHP_DBGP + "\" property"); // TODO: why?
            }
        }
        return validBreakpoints.toArray(new Breakpoint[validBreakpoints.size()]);
    }

    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        Properties properties = Properties.getDefault().getProperties(DEBUGGER).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
        properties.setArray(PHP_DBGP, getBreakpoints());
        breakpoint.addPropertyChangeListener(this);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        Properties properties = Properties.getDefault().getProperties(DEBUGGER).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
        properties.setArray( PHP_DBGP,getBreakpoints());
        breakpoint.removePropertyChangeListener(this);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        /*
         * Breakpoint could be disabled/enabled.
         * This notification are got in the case changing this property.
         */
        if (evt.getSource() instanceof Breakpoint) {
            Properties.getDefault().getProperties(DEBUGGER).
                getProperties(DebuggerManager.PROP_BREAKPOINTS).setArray(
                PHP_DBGP,getBreakpoints());
        }
    }

    private Breakpoint[] getBreakpoints() {
        Breakpoint[] bpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        List<Breakpoint> result = new ArrayList<Breakpoint>();
        for ( Breakpoint breakpoint : bpoints ) {
            // Don't store hidden breakpoints
            if ( breakpoint instanceof AbstractBreakpoint) {
                result.add( breakpoint );
            }
        }
        return result.toArray( new Breakpoint [result.size()] );
    }
}