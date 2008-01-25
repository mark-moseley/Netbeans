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


/*
 * ActionablePoint.java
 *
 */

package org.netbeans.test.umllib.actions;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author Alexei Mokeev
 */
public class ActionablePoint implements Actionable{
    private Point p = null;
    private ComponentOperator component = null;
    /** Creates a new instance of ActionablePoint */
    public ActionablePoint(ComponentOperator component, Point p) {
        this.p = p;
        this.component = component;
    }
    
    public ActionablePoint(ComponentOperator component, int x, int y) {
        this.p = new Point(x,y);
        this.component = component;
    }
    
    public JPopupMenuOperator getPopup() {
        component.clickForPopup(p.x,p.y);
        return new JPopupMenuOperator();
    }
    
    public void select() {
        component.clickMouse(p.x,p.y,1);
        new Timeout("",50).sleep();
    }
    
    
    public void addToSelection() {
        new Timeout("",10).sleep();
        component.clickMouse(p.x, p.y, 1, InputEvent.BUTTON1_MASK, KeyEvent.CTRL_MASK);
        new Timeout("",50).sleep();
    }
}
