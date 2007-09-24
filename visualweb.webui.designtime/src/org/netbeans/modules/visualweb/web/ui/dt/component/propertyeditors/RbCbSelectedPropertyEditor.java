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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import java.beans.PropertyEditorSupport;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;

/**
 * A property editor for the <code>selected</code> property of radioButton and
 * checkbox components.
 *
 * @author gjmurphy
 */
public class RbCbSelectedPropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {

    static final int UNSET = 0;
    static final int TRUE = 1;
    static final int FALSE = 2;

    DesignProperty designProperty;
    String tags[] = new String[3];

    /**
     * Creates a new instance of RbCbSelectedPropertyEditor
     */
    public RbCbSelectedPropertyEditor() {
        tags[UNSET] = "";
        tags[TRUE] = Boolean.TRUE.toString();
        tags[FALSE] = Boolean.FALSE.toString();
    }

    /**
     * Set the design property for this editor.
     */
    public void setDesignProperty (DesignProperty designProperty) {
        this.designProperty = designProperty;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(tags[UNSET])) {
            this.setValue(null);
        } else if (text.equals(tags[FALSE])) {
            if (Boolean.TRUE.equals(getSelectedValue()))
                this.setValue(Boolean.FALSE);
            else
                this.setValue(null);
        } else {
            this.setValue(getSelectedValue());
        }
    }

    public String getAsText() {
        Object value = this.getValue();
        Object selectedValue = getSelectedValue();
        if (value == null)
            return tags[UNSET];
        if (value.equals(selectedValue)) {
            if (selectedValue instanceof String)
                return tags[TRUE] + " (" + getSelectedValue().toString() + ")";
            else
                return tags[TRUE];
        } else if (value instanceof String && Boolean.valueOf((String) value).equals(selectedValue)) {
            return tags[TRUE];
        }
        return tags[FALSE];
    }

    public String[] getTags() {
        return tags;
    }

    private Object getSelectedValue() {
        return designProperty.getDesignBean().getProperty("selectedValue").getValue(); //NOI18N
    }

}
