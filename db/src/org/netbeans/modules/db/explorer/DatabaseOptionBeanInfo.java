/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer;

import java.beans.*;
import java.awt.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** A BeanInfo for DatabaseOption
*/
public class DatabaseOptionBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors ()
    {
        PropertyDescriptor[] desc = new PropertyDescriptor[1];
        try {
            desc[0] = new PropertyDescriptor("debugMode", DatabaseOption.class); //NOI18N
            desc[0].setDisplayName(NbBundle.getMessage (DatabaseOptionBeanInfo.class, "PROP_DEBUG_MODE")); //NOI18N
            desc[0].setShortDescription(NbBundle.getMessage (DatabaseOptionBeanInfo.class, "HINT_DEBUG_MODE")); //NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return desc;
    }

    @Override
    public Image getIcon(int type)
    {
        if (type == BeanInfo.ICON_COLOR_16x16) {
            return ImageUtilities.loadImage("/org/netbeans/modules/db/resources/optionIcon.gif"); //NOI18N
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            return ImageUtilities.loadImage("/org/netbeans/modules/db/resources/optionIcon32.gif"); //NOI18N
        }

        return super.getIcon(type);
    }
}
