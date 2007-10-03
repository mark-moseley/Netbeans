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

import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for SecurityConstraint element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface SecurityConstraint extends CommonDDBean, DisplayNameInterface, CreateCapability, FindCapability {
        /** Setter for web-resource-collection element.
         * @param index position in the array of elements
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         */
	public void setWebResourceCollection(int index, org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Getter for web-resource-collection element.
         * @param index position in the array of elements
         * @return web-resource-collection element (WebResourceCollection object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection getWebResourceCollection(int index);
        /** Setter for web-resource-collection elements.
         * @param value array of web-resource-collection elements (WebResourceCollection objects)
         */
	public void setWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[] value);
        /** Getter for web-resource-collection elements.
         * @return array of web-resource-collection elements (WebResourceCollection objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[] getWebResourceCollection();
        /** Returns size of web-resource-collection elements.
         * @return number of web-resource-collection elements 
         */
	public int sizeWebResourceCollection();
        /** Adds web-resource-collection element.
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         * @return index of new web-resource-collection
         */
	public int addWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Removes web-resource-collection element.
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         * @return index of the removed web-resource-collection
         */
	public int removeWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Setter for auth-constraint element.
         * @param valueInterface auth-constraint element (AuthConstraint object)
         */
	public void setAuthConstraint(org.netbeans.modules.j2ee.dd.api.web.AuthConstraint valueInterface);
        /** Getter for auth-constraint element.
         * @return auth-constraint element (AuthConstraintobject)
         */
	public org.netbeans.modules.j2ee.dd.api.web.AuthConstraint getAuthConstraint();
        /** Setter for user-data-constraint element.
         * @param valueInterface user-data-constraint element (UserDataConstraint object)
         */
	public void setUserDataConstraint(org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint valueInterface);
        /** Getter for user-data-constraint element.
         * @return user-data-constraint element (UserDataConstraint object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint getUserDataConstraint();

}
