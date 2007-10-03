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

package org.netbeans.modules.j2ee.dd.api.web;
/**
 * Generated interface for Servlet element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */

public interface Servlet extends org.netbeans.modules.j2ee.dd.api.common.ComponentInterface {
        /** Setter for servlet-name property.
         * @param value property value
         */
	public void setServletName(java.lang.String value);
        /** Getter for servlet-name property.
         * @return property value 
         */
	public java.lang.String getServletName();
        /** Setter for servlet-class property.
         * @param value property value
         */
	public void setServletClass(java.lang.String value);
        /** Getter for servlet-class property.
         * @return property value 
         */
	public java.lang.String getServletClass();
        /** Setter for jsp-file property.
         * @param value property value
         */
	public void setJspFile(java.lang.String value);
        /** Getter for jsp-file property.
         * @return property value 
         */
	public java.lang.String getJspFile();
        /** Setter for init-param element.
         * @param index position in the array of elements
         * @param valueInterface init-param element (InitParam object)
         */
	public void setInitParam(int index, org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Getter for init-param element.
         * @param index position in the array of elements
         * @return init-param element (InitParam object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int index);
        /** Setter for init-param elements.
         * @param value array of init-param elements (InitParam objects)
         */
	public void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[] value);
        /** Getter for init-param elements.
         * @return array of init-param elements (InitParam objects)
         */
	public org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam();
        /** Returns size of init-param elements.
         * @return number of init-param elements 
         */
	public int sizeInitParam();
        /** Adds init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of new init-param
         */
	public int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Removes init-param element.
         * @param valueInterface init-param element (InitParam object)
         * @return index of the removed init-param
         */
	public int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam valueInterface);
        /** Setter for load-on-startup property.
         * @param value property value
         */
	public void setLoadOnStartup(java.math.BigInteger value);
        /** Getter for load-on-startup property.
         * @return property value 
         */
	public java.math.BigInteger getLoadOnStartup();
        /** Setter for run-as element.
         * @param valueInterface run-as element (RunAs object)
         */
	public void setRunAs(org.netbeans.modules.j2ee.dd.api.common.RunAs valueInterface);
        /** Getter for run-as element.
         * @return run-as element (RunAs object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.RunAs getRunAs();
        /** Setter for security-role-ref element.
         * @param index position in the array of elements
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         */
	public void setSecurityRoleRef(int index, org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);
        /** Getter for security-role-ref  element.
         * @param index position in the array of elements
         * @return security-role-ref element (SecurityRoleRef object)
         */
	public org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef getSecurityRoleRef(int index);
        /** Setter for security-role-ref elements.
         * @param value array of security-role-ref elements (SecurityRoleRef objects)
         */
	public void setSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] value);
        /** Getter for security-role-ref elements.
         * @return array of security-role-ref  elements (SecurityRoleRef objects)
         */
	public org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] getSecurityRoleRef();
        /** Returns size of security-role-ref elements.
         * @return number of security-role-ref elements 
         */
	public int sizeSecurityRoleRef();
        /** Adds security-role-ref element.
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         * @return index of new security-role-ref
         */
	public int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);
        /** Removes security-role-ref element.
         * @param valueInterface security-role-ref element (SecurityRoleRef object)
         * @return index of the removed security-role-ref
         */
	public int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef valueInterface);

}
