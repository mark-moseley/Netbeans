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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.openide.actions.DeleteAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;



/**
 *
 * @author Ritesh Adval
 *
 */
public class OperationInputNode extends OperationParameterNode<Input> {

    
    public OperationInputNode(Input wsdlConstruct) {
        super(wsdlConstruct);
        ICON  =Utilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/input.png");
    }
    
    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(OperationInputNode.class, "LBL_OperationInputNode_TypeDisplayName");
    }
    
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actionsList = new ArrayList<Action>();
        Collections.addAll(actionsList, super.getActions(context));
        actionsList.remove(SystemAction.get(DeleteAction.class));
        return actionsList.toArray(new Action[actionsList.size()]);
        
    }
    
    @Override
    public boolean canDestroy() {
       return false;
    }
    
}
