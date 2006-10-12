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

import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class IncludeImpl extends SchemaComponentImpl implements Include{
    
    protected IncludeImpl(SchemaModelImpl model){
	this(model, createNewComponent(SchemaElements.INCLUDE, model));
    }
    
    public IncludeImpl(SchemaModelImpl model, Element el){
	super(model,el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return Include.class;
    }
    
    /**
     * Visitor
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
    
    /**
     *
     */
    public void setSchemaLocation(String uri) {
	setAttribute(SCHEMA_LOCATION_PROPERTY, SchemaAttributes.SCHEMA_LOCATION, uri);
    }
    
    /**
     *
     */
    public String getSchemaLocation() {
	return getAttribute(SchemaAttributes.SCHEMA_LOCATION);
    }
    
    public SchemaModel resolveReferencedModel() throws CatalogModelException {
        ModelSource ms = resolveModel(getSchemaLocation());
        return SchemaModelFactory.getDefault().getModel(ms);
    }
}
