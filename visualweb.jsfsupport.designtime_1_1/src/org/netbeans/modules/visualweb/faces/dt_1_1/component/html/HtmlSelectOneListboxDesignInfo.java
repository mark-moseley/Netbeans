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

import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.html.HtmlSelectOneListbox;

/**
 * DesignInfo for the "drop down"
 * XXX - copy paste some relevant javadoc comments for the main
 * methods which are being over written here, until I learn the
 * stuff well
 */
public class HtmlSelectOneListboxDesignInfo extends HtmlSelectDesignInfoBase {

    public Class getBeanClass() { return HtmlSelectOneListbox.class; }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty sizeProp = bean.getProperty("size"); //NOI18N
        if (sizeProp != null) {
            sizeProp.setValue(new Integer(5));
        }
        return selectOneBeanCreated(bean);
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        modifyVirtualFormsOnBeanDeletedCleanup(bean);
        return selectOneBeanDeleted(bean);
    }

    public Result beanPastedSetup(DesignBean bean) {
        return selectOneBeanPasted(bean);
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return selectOneGetContextItems(bean);
    }

    /**
     * This method is called when an object from a design surface or palette has been dropped 'on' a
     * JavaBean type handled by this DesignInfo (to establish a link). This method will not be
     * called unless the corresponding 'acceptLink' method call returned true. Typically, this
     * results in property settings on potentially both of the DesignBean objects.
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {

        try {
            if (canLinkConverterOrValidatorBeans(targetBean, sourceBean)) {
                linkConverterOrValidatorBeans(targetBean, sourceBean);
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAILURE;
        }
        return selectOneLinkBeans(targetBean, sourceBean);
    }
}
