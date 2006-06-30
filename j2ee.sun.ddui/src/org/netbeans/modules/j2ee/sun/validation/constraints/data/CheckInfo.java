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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.validation.constraints.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class CheckInfo extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	// NOI18N
	static public final String CLASSNAME = "Classname";	// NOI18N
	static public final String ARGUMENTS = "Arguments";	// NOI18N

	public CheckInfo() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public CheckInfo(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("classname", 	// NOI18N
			CLASSNAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("arguments", 	// NOI18N
			ARGUMENTS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Arguments.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is mandatory
	public void setClassname(String value) {
		this.setValue(CLASSNAME, value);
	}

	//
	public String getClassname() {
		return (String)this.getValue(CLASSNAME);
	}

	// This attribute is optional
	public void setArguments(Arguments value) {
		this.setValue(ARGUMENTS, value);
	}

	//
	public Arguments getArguments() {
		return (Arguments)this.getValue(ARGUMENTS);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);	// NOI18N
		}
		// Validating property classname
		if (getClassname() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getClassname() == null", "classname", this);	// NOI18N
		}
		// Validating property arguments
		if (getArguments() != null) {
			getArguments().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Classname");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getClassname();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CLASSNAME, 0, str, indent);

		str.append(indent);
		str.append("Arguments");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getArguments();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ARGUMENTS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("CheckInfo\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for for constraints xml.
  constraints.xml is used to specify provide information of the
  Constraints to the Validation framework.
 
  $Revision$
-->


<!--
This is the root element.
-->
<!ELEMENT constraints (check-info*)>


<!--
This represents an information, about a particular Constraint.
Provides information of a Constraint represented by corresponding
<check> element in validation.xml.
Sub element <name> is used to link this element with the
corresponding <check> element in validation.xml.
-->
<!ELEMENT check-info (name, classname, arguments?)>


<!--
This element represents information of a Constraint class arguments.
Number of sub elements, <argument> should match with the number
of <parameter> sub elements, of corresponding <arguments> element
in validation.xml
-->
<!ELEMENT arguments (argument+)>


<!--
This element represents information of a single Constraint class
argument.
Sub elements <name> should match with the <name> sub element of
corresponding <parameter> element in constraints.xml
-->
<!ELEMENT argument (name, type?)>


<!--
Used in two elements <check-info> and <argument>
In <check-info>, it represents a Constraint name and is the linking
element between <check> element in validation.xml and <check-info>
element in constraints.xml.
In <argument>, it represents argument name and is the linking element
between <parameter> element in validation.xml and <argument> element
in constraints.xml.
-->
<!ELEMENT name (#PCDATA)>


<!--
This element represents Constraint class name.
Constraint class should provide the constructor with no arguments.
Constraint class should also provide the set* methods for all the
required arguments.
Constraint class is always created using default constructor and
then the arguments are set using set* methods.
-->
<!ELEMENT classname (#PCDATA)>


<!--
This element represents the type of an argument.
If not specified, it defaults to java.lang.String
-->
<!ELEMENT type (#PCDATA)>

*/
