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
package com.sun.rave.web.ui.taglib;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;
import com.sun.rave.web.ui.el.ConstantMethodBinding;

/**
 * <p>Auto-generated component tag class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public class ThemeLinksTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.ThemeLinks";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.ThemeLinks";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        javaScript = null;
        styleSheet = null;
        styleSheetInline = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (javaScript != null) {
            if (isValueReference(javaScript)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(javaScript);
                _component.setValueBinding("javaScript", _vb);
            } else {
                _component.getAttributes().put("javaScript", Boolean.valueOf(javaScript));
            }
        }
        if (styleSheet != null) {
            if (isValueReference(styleSheet)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleSheet);
                _component.setValueBinding("styleSheet", _vb);
            } else {
                _component.getAttributes().put("styleSheet", Boolean.valueOf(styleSheet));
            }
        }
        if (styleSheetInline != null) {
            if (isValueReference(styleSheetInline)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleSheetInline);
                _component.setValueBinding("styleSheetInline", _vb);
            } else {
                _component.getAttributes().put("styleSheetInline", Boolean.valueOf(styleSheetInline));
            }
        }
    }

    // javaScript
    private String javaScript = null;
    public void setJavaScript(String javaScript) {
        this.javaScript = javaScript;
    }

    // styleSheet
    private String styleSheet = null;
    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }

    // styleSheetInline
    private String styleSheetInline = null;
    public void setStyleSheetInline(String styleSheetInline) {
        this.styleSheetInline = styleSheetInline;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}
