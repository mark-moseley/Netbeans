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
/**
 *	This generated bean class Field matches the schema element field
 *
 *	Generated on Thu Sep 25 15:18:26 PDT 2003
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;
import org.netbeans.modules.schema2beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Field extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String FIELDTYPE = "FieldType";	// NOI18N
	static public final String REQUIRED = "Required";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String FIELD_VALUE = "FieldValue";	// NOI18N
	static public final String TAG = "Tag";	// NOI18N

	public Field() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Field(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("field-value", 	// NOI18N
			FIELD_VALUE, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FieldValue.class);
		this.createProperty("tag", 	// NOI18N
			TAG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Tag.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is mandatory
	public void setFieldType(java.lang.String value) {
		setAttributeValue(FIELDTYPE, value);
	}

	//
	public java.lang.String getFieldType() {
		return getAttributeValue(FIELDTYPE);
	}

	// This attribute is mandatory
	public void setRequired(java.lang.String value) {
		setAttributeValue(REQUIRED, value);
	}

	//
	public java.lang.String getRequired() {
		return getAttributeValue(REQUIRED);
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
	public void setFieldValue(FieldValue value) {
		this.setValue(FIELD_VALUE, value);
	}

	//
	public FieldValue getFieldValue() {
		return (FieldValue)this.getValue(FIELD_VALUE);
	}

	// This attribute is optional
	public void setTag(Tag value) {
		this.setValue(TAG, value);
	}

	//
	public Tag getTag() {
		return (Tag)this.getValue(TAG);
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
		// Validating property fieldType
		if (getFieldType() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getFieldType() == null", "fieldType", this);	// NOI18N
		}
		// Validating property required
		if (getRequired() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRequired() == null", "required", this);	// NOI18N
		}
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);	// NOI18N
		}
		// Validating property fieldValue
		if (getFieldValue() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getFieldValue() == null", "fieldValue", this);	// NOI18N
		}
		getFieldValue().validate();
		// Validating property tag
		if (getTag() != null) {
			getTag().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
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
		str.append("FieldValue");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFieldValue();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FIELD_VALUE, 0, str, indent);

		str.append(indent);
		str.append("Tag");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getTag();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(TAG, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Field\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!ELEMENT wizard (name, field-group+)>
<!ELEMENT field-group (name, field+)>
<!ELEMENT field (name, field-value, tag?)>
<!ATTLIST field  field-type                 CDATA     "string"
                 required                   CDATA     "true">
<!ELEMENT field-value (default-field-value, option-value-pair*)>
<!ELEMENT option-value-pair (option-name, conditional-value)>
<!ELEMENT name (#PCDATA)>
<!ELEMENT default-field-value (#PCDATA)>
<!ELEMENT option-name (#PCDATA)>
<!ELEMENT conditional-value (#PCDATA)>
<!ELEMENT tag (tag-item*)>
<!ELEMENT tag-item (#PCDATA)>



*/
