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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 *	This generated bean class Element matches the schema element element
 *
 */

package org.netbeans.modules.j2ee.sun.validation.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Element extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	               // NOI18N
	static public final String CHECK = "Check";	               // NOI18N

	public Element() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Element(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("check", 	// NOI18N
			CHECK, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Check.class);
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

	// This attribute is an array, possibly empty
	public void setCheck(int index, Check value) {
		this.setValue(CHECK, index, value);
	}

	//
	public Check getCheck(int index) {
		return (Check)this.getValue(CHECK, index);
	}

	// This attribute is an array, possibly empty
	public void setCheck(Check[] value) {
		this.setValue(CHECK, value);
	}

	//
	public Check[] getCheck() {
		return (Check[])this.getValues(CHECK);
	}

	// Return the number of properties
	public int sizeCheck() {
		return this.size(CHECK);
	}

	// Add a new element returning its index in the list
	public int addCheck(org.netbeans.modules.j2ee.sun.validation.data.Check value) {
		return this.addValue(CHECK, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCheck(org.netbeans.modules.j2ee.sun.validation.data.Check value) {
		return this.removeValue(CHECK, value);
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
		// Validating property check
		for (int _index = 0; _index < sizeCheck(); ++_index) {
			org.netbeans.modules.j2ee.sun.validation.data.Check element = getCheck(_index);
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
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Check["+this.sizeCheck()+"]");	// NOI18N
		for(int i=0; i<this.sizeCheck(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCheck(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CHECK, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Element\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for for validation xml.
  validation.xml is used to specify Constraints to be applied to
  elements.
 
  $Revision$
-->


<!--
This is the root element
-->
<!ELEMENT validation  (element*) >
<!ATTLIST validation
        validate    CDATA        (true | false)     "true">


<!--
This element represents, the set of Constraints to be applied to
the given element.
-->
<!ELEMENT element (name, check*)>


<!--
This element represents, a particular Constraint.
Note : Information about this Constraint must be provided through
corresponding <check-info> object in constraints.xml Sub element
<name> should match with <name> of corresponding <check-info>
element defined in constraints.xml.
-->
<!ELEMENT check (name, parameters?)>


<!--
This element represent, Constraint parameters.
Number of sub elements, <parameter> should match with the number
of <argument> sub elements, of corresponding <arguments> element
in constraints.xml
-->
<!ELEMENT parameters (parameter+)>


<!--
This element represents, a Constraint parameter.
Sub elements <name> should match with the <name> sub element of
corresponding <argument> element in constraints.xml
<value> could be one or more. In case of an variable array
argument, multiple <value> elements will be used.
Example : InConstraint
-->
<!ELEMENT parameter (name, value+)>


<!--
Used in elements : <element>, <check> and <parameter>
In <element> , it represents the name(xpath - complete absolute
name of an element(leaf).
In <check> , it represents name of a Constraint. This is the
linking element for <check> element in validation.xml and
<check-info> element in constraints.xml.
In <parameter>, it represents name of parameter. This is the
linking element for <parameter> element in validation.xml and
<argument> element in constraints.xml.
-->
<!ELEMENT name (#PCDATA)>


<!--
This element represents the value of a parameter.
-->
<!ELEMENT value (#PCDATA)>

*/
