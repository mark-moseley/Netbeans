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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Abstract definition of watch. Each watch is created for
 * one String which contains the name of variable or some expression.
 *
 * @author   Jan Jancura
 */
public final class Watch {

    /** Name of the property for the watched expression. */
    public static final String PROP_EXPRESSION = "expression"; // NOI18N
    /** Name of the property for the value of the watched expression. This constant is not used at all. */
    public static final String PROP_VALUE = "value"; // NOI18N

    private String          expression;
    private PropertyChangeSupport pcs;
    
    
    Watch (String expr) {
        this.expression = expr;
        pcs = new PropertyChangeSupport (this);
    }
    
    /**
     * Return expression this watch is created for.
     *
     * @return expression this watch is created for
     */
    public String getExpression () {
        return expression;
    }

    /** 
     * Set the expression to watch.
     *
     * @param expression expression to watch
     */
    public void setExpression (String expression) {
        String old = this.expression;
        this.expression = expression;
        pcs.firePropertyChange (PROP_EXPRESSION, old, expression);
    }
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        dm.removeWatch (this);
    }

    /**
     * Add a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Remove a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
}

