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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;

/** Use this class for a column if the entry is stored as an attribute in
 *  the parent bean class.
 *
 * @author Peter Williams
 */
public class AttributeEntry extends TableEntry {

    public AttributeEntry(String pn, String c, int w) {
        super(pn, c, w);
    }

    public AttributeEntry(String pn, String c, int w, boolean required) {
        super(pn, c, w, required);
    }

    public AttributeEntry(String ppn, String pn, String c, int w, boolean required) {
        super(ppn, pn, c, w, required);
    }

    public AttributeEntry(String ppn, String pn, String c, int w, boolean required, boolean isName) {
        super(ppn, pn, c, w, required, isName);
    }

    public AttributeEntry(String ppn, String pn, ResourceBundle resBundle,
            String resourceBase, int w, boolean required, boolean isName) {
        super(ppn, pn, resBundle, resourceBase, w, required, isName);
    }

            public Object getEntry(CommonDDBean parent) {
        return parent.getAttributeValue(propertyName);
    }

    public void setEntry(CommonDDBean parent, Object value) {
        String attrValue = null;
        if(value != null) {
            attrValue = value.toString();
        }
        parent.setAttributeValue(propertyName, attrValue);
    }

    public Object getEntry(CommonDDBean parent, int row) {
        return parent.getAttributeValue(parentPropertyName, row, propertyName);
    }

    public void setEntry(CommonDDBean parent, int row, Object value) {
        String attrValue = null;
        if(value != null) {
            attrValue = value.toString();
        }

        parent.setAttributeValue(parentPropertyName, row, propertyName, attrValue);
        // !PW FIXME I think Cliff Draper fixed the bug this was put in for... we'll see.
        // The issue was that attributes that were children of non-property objects and
        // thus were attached to a boolean array, needed to have the boolean set to true
        // in order to be recognized, otherwise, it was as if they did not exist.
        // attributes of real properties (those that have non-attribute children as well)
        // work fine regardless.
//        if(Common.isBoolean(parent.beanProp(parentPropertyName).getType())) {
//            parent.setValue(parentPropertyName, row, Boolean.TRUE);
//        }
    }
}
