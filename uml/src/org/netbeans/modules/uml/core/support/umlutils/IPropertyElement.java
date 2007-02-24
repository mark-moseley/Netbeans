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

package org.netbeans.modules.uml.core.support.umlutils;
import java.util.HashMap;
import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
public interface IPropertyElement
{
	/**
	 * Name of property element
	*/
	public String getName();

	/**
	 * Name of property element
	*/
	public void setName( String value );

	/**
	 * Value of the property element
	*/
	public String getValue();

	/**
	 * Value of the property element
	*/
	public void setValue( String value );

	/**
	 * Child properties of this property, get IPropertyElement[]
	*/
	public Vector<IPropertyElement> getSubElements();
	public HashMap<String, IPropertyElement> getHashedSubElements();

	/**
	 * Child properties of this property
	*/
	public void setSubElements( Vector<IPropertyElement> value );

	/**
	 * Add a new property element to the property
	*/
	public void addSubElement( IPropertyElement element );

	/**
	 * Gets a specific sub property by index
	*/
	public IPropertyElement getSubElement( int index, IPropertyElement element );
	public IPropertyElement getSubElement( String name, IPropertyElement element );

	/**
	 * Gets this elements corresponding definition
	*/
	public IPropertyDefinition getPropertyDefinition();

	/**
	 * Gets this elements corresponding definition
	*/
	public void setPropertyDefinition( IPropertyDefinition value );

	/**
	 * Gets this elements corresponding element
	*/
	public Object getElement();

	/**
	 * Gets this elements corresponding element
	*/
	public void setElement( Object value );

	/**
	 * Parent element of this property element
	*/
	public IPropertyElement getParent();

	/**
	 * Parent element of this property element
	*/
	public void setParent( IPropertyElement value );

	/**
	 * Determines whether or not this element has been modified
	*/
	public boolean getModified();

	/**
	 * Determines whether or not this element has been modified
	*/
	public void setModified( boolean value );

	/**
	 * Gets whether or not this element needs to be built immediately
	*/
	public boolean getOnDemand();

	/**
	 * Gets whether or not this element needs to be built immediately
	*/
	public void setOnDemand( boolean value );

	/**
	 * Tells a property element to store itself
	*/
	public boolean save();

	/**
	 * Original value of the property element
	*/
	public String getOrigValue();

	/**
	 * Original value of the property element
	*/
	public void setOrigValue( String value );

	/**
	 * Tells a property element to remove itself
	*/
	public void remove();

	/**
	 * Returns the path to this element by getting each of the parent names and building a | delimited string
	*/
	public String getPath();

	/**
	 * Translated value of the property element
	*/
	public String getTranslatedValue();

	/**
	 * The PropertyElementManager is a helper object for saving/restoring this Element in the proper language format.
	*/
	public IPropertyElementManager getPropertyElementManager();

	/**
	 * The PropertyElementManager is a helper object for saving/restoring this Element in the proper language format.
	*/
	public void setPropertyElementManager( IPropertyElementManager value );

        public IProject getProject();

	public String toString();
}
