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

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import javax.servlet.http.Cookie;

public class CookieIn extends BaseBean {

    static Vector comparators = new Vector();


    public CookieIn() {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookieIn(Cookie cookie) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 5));
	this.setAttributeValue("name", cookie.getName());//NOI18N
	this.setAttributeValue("value", cookie.getValue());//NOI18N
    }

    public CookieIn(String name, String value) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 5));
	this.setAttributeValue("name", name);//NOI18N
	this.setAttributeValue("value", value);//NOI18N
    }

    public CookieIn(int options) {
	super(comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options)
    {

    }

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

        
    /**
       * Get the value of name.
       * @return Value of name.
       */
    public String getName() {return this.getAttributeValue("name");} //NOI18N
    
    /**
       * Set the value of Name.
       * @param v  Value to assign to name.
       */
    public void setName(String  v) {this.setAttributeValue("name", v);} //NOI18N
    

    /**
       * Get the value of value.
       * @return Value of value.
       */
    public String getValue() {return this.getAttributeValue("value");} //NOI18N
    
    /**
       * Set the value of value.
       * @param v  Value to assign to value.
       */
    public void setValue(String  v)  {this.setAttributeValue("value", v);} //NOI18N
    
    //
    static public void addComparator(BeanComparator c)
    {
	CookieIn.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c)
    {
	CookieIn.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
	String s;
	BaseBean n;
    }

    public String dumpBeanNode()
    {
	StringBuffer str = new StringBuffer();
	str.append("CookieIn\n");//NOI18N
	this.dump(str, "\n  ");//NOI18N
	return str.toString();
    }
}

