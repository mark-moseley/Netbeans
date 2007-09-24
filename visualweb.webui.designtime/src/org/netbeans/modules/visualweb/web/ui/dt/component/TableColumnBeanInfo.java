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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import com.sun.rave.designtime.Constants;
import com.sun.rave.web.ui.component.*;

/**
 * BeanInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.TableColumn} component.
 *
 * @author Winston Prakash
 */
public class TableColumnBeanInfo extends TableColumnBeanInfoBase {
    public TableColumnBeanInfo(){
        BeanDescriptor beanDescriptor = super.getBeanDescriptor();
        // Suppose to show up in a right-click "Add >" menu item.
        beanDescriptor.setValue(Constants.BeanDescriptor.PREFERRED_CHILD_TYPES, new String[] {
            //TableColumn.class.getName(),
            StaticText.class.getName(),
            TextField.class.getName(),
            TextArea.class.getName(),
            Button.class.getName(),
            Label.class.getName(),
            Hyperlink.class.getName(),
            ImageHyperlink.class.getName(),
            DropDown.class.getName(),
            Checkbox.class.getName(),
            RadioButton.class.getName(),
            ImageComponent.class.getName(),
            PanelGroup.class.getName(),
            Message.class.getName()
        });
        //Doesn't work well yet
        //beanDescriptor.setValue(
        //    Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES,
        //    //new String[] { "*headerText://a" }); // NOI18N
        //    new String[] { "*headerText" }); // NOI18N

        PropertyDescriptor[] descriptors = this.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals("actionListener")) //NOI18N
                descriptors[i].setHidden(true);
        }
    }
}
