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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Dimension;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.openide.util.Lookup;

/**
 *
 * @author rico
 */
public abstract class OperationWithFaultWidget<T extends Operation> extends OperationWidget<T> {
    private Widget dummyWidget;
    
    /** Creates a new instance of OperationWithFaultWidget */
    public OperationWithFaultWidget(Scene scene, T operation, Lookup lookup) {
        super(scene, operation, lookup);
        dummyWidget = new Widget(scene);
        dummyWidget.setMinimumSize(new Dimension(5, 10));
    }
    
    @Override
    public void setRightSided(boolean rightSided) {
        super.setRightSided(rightSided);
        init();
        refreshFaults(getVerticalWidget());
    }

    protected abstract Widget getVerticalWidget();
    protected abstract void init();
    
    private void refreshFaults(Widget verticalWidget) {
        dummyWidget.removeFromParent();
        for(Fault fault : getWSDLComponent().getFaults()){
            Widget faultWidget = WidgetFactory.getInstance().getOrCreateWidget(getScene(), fault, getLookup(), verticalWidget);
            verticalWidget.addChild(faultWidget); //adjust for dummy widget. add the fault before dummy widget.
        }
        verticalWidget.addChild(dummyWidget);
    }
    
    @Override
    public void childrenAdded() {
        refreshFaults(getVerticalWidget());
    }
    
}
