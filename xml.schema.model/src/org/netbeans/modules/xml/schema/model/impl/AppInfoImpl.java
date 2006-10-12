/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class AppInfoImpl extends SchemaComponentImpl implements AppInfo {

    public AppInfoImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.APPINFO, model));
    }

    public AppInfoImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

    public void setURI(String uri) {
        setAttribute(SOURCE_PROPERTY, SchemaAttributes.SOURCE, uri);
    }

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public String getURI() {
        return getAttribute(SchemaAttributes.SOURCE);
    }

    public Element getAppInfoElement() {
        return Element.class.cast(getPeer().cloneNode(true));
    }

    public void setAppInfoElement(Element content) {
        super.updatePeer(CONTENT_PROPERTY, content);
    }

    public Class<? extends SchemaComponent> getComponentType() {
        return AppInfo.class;
    }

    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_PROPERTY, text);
    }

    public String getContentFragment() {
        return super.getXmlFragment();
    }
}
