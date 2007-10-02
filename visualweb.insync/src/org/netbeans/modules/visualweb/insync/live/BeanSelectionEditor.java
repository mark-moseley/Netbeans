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
package org.netbeans.modules.visualweb.insync.live;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;

/**
 *
 */
public class BeanSelectionEditor extends PropertyEditorSupport {

    final DesignProperty property;
    DesignBean[] liveBeans;
    int selection;

    public BeanSelectionEditor(DesignProperty property) {
        this.property = property;
        assert Trace.trace("insync.live", "BSE.BSE: " + property);

        // lookup current DesignBean in list from current property value
        setValue(property.getValue());
    }

    // load up-to-date list of all sibling beans
    private void loadList() {
        assert Trace.trace("insync.live", "BSE.loadList");
        DesignBean bean = property.getDesignBean();
        DesignContext context = bean.getDesignContext();
        PropertyDescriptor pd = property.getPropertyDescriptor();
        Class type = pd.getPropertyType();
        liveBeans = context.getBeansOfType(type);
        selection = -1;
    }

    public String getAsText() {
        String text = selection >= 0
            ? liveBeans[selection].getInstanceName()
            : "";
        assert Trace.trace("insync.live", "BSE.getAsText: " + text);
        return text;
    }

    public String getJavaInitializationString() {
        String init = selection >= 0
            ? liveBeans[selection].getInstanceName()
            : "";
        assert Trace.trace("insync.live", "BSE.getJavaInitializationString: " + init);
        return init;
    }

    public String[] getTags() {
        String[] tags = new String[liveBeans.length];
        for (int i = 0; i < liveBeans.length; i++)
            tags[i] = liveBeans[i].getInstanceName();
        return tags;
    }

    public Object getValue() {
        Object value = selection >= 0
            ? liveBeans[selection].getInstance()
            : null;
        assert Trace.trace("insync.live", "BSE.getValue: " + value);
        return value;
    }

    public void setAsText(String text)  {
        assert Trace.trace("insync.live", "BSE.setAsText: " + text);
        loadList();
        for (int i = 0; i < liveBeans.length; i++) {
            if (liveBeans[i].getInstanceName().equals(text)) {
                selection = i;
                return;
            }
        }
        //!CQ could throw an invalid arg exception
    }

    public void setValue(Object value) {
        assert Trace.trace("insync.live", "BSE.setValue: " + value);
        loadList();
        for (int i = 0; i < liveBeans.length; i++) {
            if (liveBeans[i].getInstance() == value) {
                selection = i;
                return;
            }
        }
        //!CQ could throw an invalid arg exception
    }

}
