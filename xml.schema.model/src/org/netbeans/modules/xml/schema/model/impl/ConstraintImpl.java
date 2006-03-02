/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConstraintImpl.java
 *
 * Created on October 21, 2005, 8:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Selector;
import org.w3c.dom.Element;/**
 *
 * @author Vidhya Narayanan
 */
public abstract class ConstraintImpl extends NamedImpl 
	implements Constraint {
    
    /**
     * Creates a new instance of ConstraintImpl 
     */
    public ConstraintImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEY,model));
    }
    
    /**
     * Creates a new instance of ConstraintImpl
     */
    public ConstraintImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    /**
     *
     */
    public void setSelector(Selector s) {
        List<Class<? extends SchemaComponent>> classes = new ArrayList<Class<? extends SchemaComponent>>();
        classes.add(Annotation.class);
        setChild(Selector.class, SELECTOR_PROPERTY, s, classes);
    }
    
    /**
     *
     */
    public Selector getSelector() {
        Collection<Selector> elements = getChildren(Selector.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public Collection<Field> getFields() {
        return getChildren(Field.class);
    }
    
    /**
     *
     */
    public void deleteField(Field field) {
        removeChild(FIELD_PROPERTY, field);
    }
    
    /**
     *
     */
    public void addField(Field field) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        list.add(Selector.class);
        addAfter(FIELD_PROPERTY, field, list);
    }
}
