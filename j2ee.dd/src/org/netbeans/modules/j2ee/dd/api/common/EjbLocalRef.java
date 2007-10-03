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

package org.netbeans.modules.j2ee.dd.api.common;

//
// This interface has all of the bean info accessor methods.

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

/**
 * Generated interface for EjbLocalRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface EjbLocalRef extends CommonDDBean, DescriptionInterface {

        public static final String EJB_REF_NAME = "EjbRefName";	// NOI18N
	public static final String EJB_REF_TYPE = "EjbRefType";	// NOI18N
	public static final String LOCAL_HOME = "LocalHome";	// NOI18N
	public static final String LOCAL = "Local";	// NOI18N
	public static final String EJB_LINK = "EjbLink";	// NOI18N
        public static final String EJB_REF_TYPE_ENTITY = "Entity"; // NOI18N
        public static final String EJB_REF_TYPE_SESSION = "Session"; // NOI18N
        /** Setter for ejb-ref-name property.
         * @param value property value
         */
	public void setEjbRefName(String value);
        /** Getter for ejb-ref-name property.
         * @return property value 
         */
	public String getEjbRefName();
        /** Setter for ejb-ref-type property.
         * @param value property value 
         */
	public void setEjbRefType(String value);
        /** Getter for ejb-ref-type property.
         * @return property value 
         */
	public String getEjbRefType();
        /** Setter for local-home property.
         * @param value property value
         */
	public void setLocalHome(String value);
        /** Getter for local-home property.
         * @return property value 
         */
	public String getLocalHome();
        /** Setter for local property.
         * @param value property value
         */
	public void setLocal(String value);
        /** Getter for local property.
         * @return property value 
         */
	public String getLocal();
        /** Setter for ejb-link property.
         * @param value property value
         */
	public void setEjbLink(String value);
        /** Getter for ejb-link property.
         * @return property value 
         */
	public String getEjbLink();

        // Java EE 5
        
        void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException;
	InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException;
	int sizeInjectionTarget() throws VersionNotSupportedException;
	void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException;
	InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException;
	int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
	int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
	InjectionTarget newInjectionTarget() throws VersionNotSupportedException;

}

