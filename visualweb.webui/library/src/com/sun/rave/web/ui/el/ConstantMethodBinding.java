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
package com.sun.rave.web.ui.el;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/**
 * <p>Implementation of <code>MethodBinding</code> that always returns
 * the value specified to our constructor.</p>
 */
public class ConstantMethodBinding extends MethodBinding
  implements StateHolder {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Zero arguments constructor for restoring state.</p>
     */
    public ConstantMethodBinding() {
        this(null);
    }


    /**
     * <p>Create a new <code>MethodBinding</code> that always returns
     * the specified value.</p>
     */
    public ConstantMethodBinding(String value) {
        this.value = value;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The value to be returned when this method binding is
     * evaluated.</p>
     */
    private String value = null;


    // --------------------------------------------------- MethodBinding Methods


    /**
     * <p>Return the appropriate constant value.</p>
     *
     * @param context <code>FacesContext</code> for this request
     * @param params Method parameters to pass in
     */
    public Object invoke(FacesContext context, Object params[]) {
        return value;
    }


    /**
     * <p>Return the expected return type class.</p>
     *
     * @param context <code>FacesContext</code> for this request
     */
    public Class getType(FacesContext context) {
        return String.class;
    }


    /**
     * <p>Return the expression string for this method binding.</p>
     */
    public String getExpressionString() {
        return this.value;
    }


    // ----------------------------------------------------- StateHolder Methods


    private boolean transientFlag = false;


    public boolean isTransient() {
        return this.transientFlag;
    }


    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }


    public void restoreState(FacesContext context, Object state) {
        this.value = (String) state;
    }


    public Object saveState(FacesContext context) {
        return this.value;
    }


}
