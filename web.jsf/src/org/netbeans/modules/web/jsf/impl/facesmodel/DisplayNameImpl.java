/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class DisplayNameImpl extends JSFConfigComponentImpl implements DisplayName{

    public DisplayNameImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public DisplayNameImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.DISPLAY_NAME));
    }

    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public String getValue() {
        return getText();
    }

    public void setValue(String displayName) {
        setText(DISPLAY_NAME, displayName);
    }

    public String getLang() {
        return getAttribute(FacesAttributes.LANG);
    }

    public void setLang(String lang) {
        setAttribute(LangAttribute.LANG_ATTRIBUTE, FacesAttributes.LANG, lang);
    }

}
