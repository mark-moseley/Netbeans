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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.web.ui.component.AddRemove;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import com.sun.rave.web.ui.model.MultipleSelectOptionsList;
import com.sun.rave.web.ui.model.Option;

/** DesignInfo class for components that extend the {@link
 * org.netbeans.modules.visualweb.web.ui.dt.component.AddRemove} component.
 *
 * @author gjmurphy
 */
public class AddRemoveDesignInfo extends SelectorDesignInfo {

    /** Name of the Add Button facet */
    private static final String ADD_BUTTON_FACET = "addButton"; //NOI18N

    /** Name of the Remove Button facet */
    private static final String REMOVE_BUTTON_FACET = "removeButton"; //NOI18N

    public AddRemoveDesignInfo() {
        super(AddRemove.class);
    }


    /** When a new AddRemove-based component is dropped, create a default
     * list of options and bind if to this component's <code>items</code> and
     * <code>selected</code> properties.
     *
     * @param bean <code>DesignBean</code> for the newly created instance
     */
    public Result beanCreatedSetup(DesignBean bean) {
        super.beanCreatedSetup(bean);

        FacesDesignContext context = (FacesDesignContext) bean.getDesignContext();

        DesignProperty availableItemsLabel = bean.getProperty("availableItemsLabel"); // NOI18N
        availableItemsLabel.setValue(DesignMessageUtil.getMessage(AddRemoveDesignInfo.class,"AddRemove.available")); // NOI18N

        DesignProperty selectedItemsLabel = bean.getProperty("selectedItemsLabel"); // NOI18N
        selectedItemsLabel.setValue(DesignMessageUtil.getMessage(AddRemoveDesignInfo.class,"AddRemove.selected"));  // NOI18N


        DesignBean options = context.getBeanByName(getOptionsListName(bean));
        if(options != null) {
            bean.getProperty("selected").setValueSource(context.getBindingExpr(options, ".selectedValue")); //NOI18N
        }
        return Result.SUCCESS;
    }

    protected Class getOptionsListClass() {
        return MultipleSelectOptionsList.class;
    }

}
