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
 * FaultWidget.java
 *
 * Created on November 6, 2006, 6:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget.ArrowWidget.ParameterType;
import org.openide.util.Lookup;

/**
 * Represents a fault WSDL component.
 *
 * @author radval
 */
public class FaultWidget extends OperationParameterWidget {
    /**
     * Creates a new instance of FaultWidget.
     *
     * @param  scene   Scene in which this Widget will exist.
     * @param  fault   the WSDL component.
     * @param  lookup  the Lookup for this widget.
     * @param  reversed  Should the direction of the arrow be reversed?
     */
    public FaultWidget(Scene scene, Fault fault, Lookup lookup, boolean reversed) {
        super(scene, fault, lookup);
        DirectionCookie dc = lookup.lookup(DirectionCookie.class);
        boolean rightSided = dc == null ? false : dc.isRightSided();
        boolean direction = reversed ? rightSided : !rightSided;
        ArrowWidget widget = new ArrowWidget(scene, direction, ParameterType.FAULT);
        widget.setColor(WidgetConstants.FAULT_ARROW_COLOR);
        if (isImported()) widget.setColor(Color.GRAY.darker());
        addChild(widget);
     }
    
}
