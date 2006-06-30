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

public class Arguments extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String ARGUMENT = "Argument";	       // NOI18N

	public Arguments() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Arguments(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("argument", 	// NOI18N
			ARGUMENT, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Argument.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array containing at least one element
	public void setArgument(int index, Argument value) {
		this.setValue(ARGUMENT, index, value);
	}

	//
	public Argument getArgument(int index) {
		return (Argument)this.getValue(ARGUMENT, index);
	}

	// This attribute is an array containing at least one element
	public void setArgument(Argument[] value) {
		this.setValue(ARGUMENT, value);
	}

	//
	public Argument[] getArgument() {
		return (Argument[])this.getValues(ARGUMENT);
	}

	// Return the number of properties
	public int sizeArgument() {
		return this.size(ARGUMENT);
	}

	// Add a new element returning its index in the list
	public int addArgument(org.netbeans.modules.j2ee.sun.validation.constraints.data.Argument value) {
		return this.addValue(ARGUMENT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeArgument(org.netbeans.modules.j2ee.sun.validation.constraints.data.Argument value) {
		return this.removeValue(ARGUMENT, value);
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
		// Validating property argument
		if (sizeArgument() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeArgument() == 0", "argument", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeArgument(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.constraints.data.Argument element = getArgument(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Argument["+this.sizeArgument()+"]");	// NOI18N
		for(int i=0; i<this.sizeArgument(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getArgument(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ARGUMENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Arguments\n");	// NOI18N
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
