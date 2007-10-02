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
package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.UIComponent;
import com.sun.rave.designtime.*;
import javax.faces.component.html.HtmlMessage;

public class HtmlMessageDesignInfo extends HtmlDesignInfoBase {

    public Class getBeanClass() {
        return HtmlMessage.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        bean.getProperty("for").setValue(""); //NOI18N
        bean.getProperty("showDetail").setValue(Boolean.FALSE); //NOI18N
        bean.getProperty("showSummary").setValue(Boolean.TRUE); //NOI18N
        bean.getProperty("infoClass").setValue("infoMessage"); // NOI18N
        bean.getProperty("warnClass").setValue("warnMessage"); // NOI18N
        bean.getProperty("errorClass").setValue("errorMessage"); // NOI18N
        bean.getProperty("fatalClass").setValue("fatalMessage"); // NOI18N
        return Result.SUCCESS;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return UIComponent.class.isAssignableFrom(sourceClass);
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        if (sourceBean.getInstance() instanceof UIComponent &&
            sourceBean != targetBean) {
            DesignProperty prop = targetBean.getProperty("for"); // NOI18N
            if (prop != null) {
                prop.setValue(sourceBean.getInstanceName());
            }
        }
        return Result.SUCCESS;
    }
}
