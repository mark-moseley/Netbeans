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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers.lists;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.InputEvent;

import org.netbeans.jemmy.QueueTool;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.MultiSelListDriver;

import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * List driver for javax.swing.JList component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class JListMouseDriver extends LightSupportiveDriver implements MultiSelListDriver {
    QueueTool queueTool;
    /**
     * Constructs a JListMouseDriver.
     */
    public JListMouseDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JListOperator"});
	queueTool = new QueueTool();
    }

    public void selectItem(ComponentOperator oper, int index) {
	clickOnItem((JListOperator)oper, index);
    }

    public void selectItems(ComponentOperator oper, int[] indices) {
	clickOnItem((JListOperator)oper, indices[0]);
	for(int i = 1; i < indices.length; i++) {
	    clickOnItem((JListOperator)oper, indices[i], InputEvent.CTRL_MASK);
	}
    }

    /**
     * Clicks on a list item.
     * @param oper an operator to click on.
     * @param index item index.
     */
    protected void clickOnItem(JListOperator oper, int index) {
	clickOnItem(oper, index, 0);
    }

    /**
     * Clicks on a list item.
     * @param oper an operator to click on.
     * @param index item index.
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    protected void clickOnItem(final JListOperator oper, final int index, final int modifiers) {
        if(!queueTool.isDispatchThread()) {
            oper.scrollToItem(index);
        }
        queueTool.invokeSmoothly(new QueueTool.QueueAction("Path selecting") {
                public Object launch() {
                    Rectangle rect = oper.getCellBounds(index, index);
                    DriverManager.getMouseDriver(oper).
                        clickMouse(oper, 
                                   rect.x + rect.width / 2,
                                   rect.y + rect.height / 2,
                                   1, oper.getDefaultMouseButton(), modifiers,
                                   oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
                    return(null);
                }
            });
    }
}
