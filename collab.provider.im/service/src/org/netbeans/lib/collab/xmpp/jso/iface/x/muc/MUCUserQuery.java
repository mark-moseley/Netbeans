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

package org.netbeans.lib.collab.xmpp.jso.iface.x.muc;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.NSI;

/**
 * <p>
 * Interface for representing a "http://jabber.org/protocol/muc#user" query.</p>

 *
 * @author Rahul Shah
 *
 */
public interface MUCUserQuery extends Extension {
    //"Constants"
    /** The namespace URI for <tt>MUCUserQuery</tt> and its known children
     * (http://jabber.org/protocol/muc#user).
     *
     *
     */
    public static final String  NAMESPACE       = "http://jabber.org/protocol/muc#user";
    /** The qualified name for <tt>MUCUserQuery</tt> ({http://jabber.org/protocol/muc#user}query).
     *
     * 
     */
    public static final NSI     NAME            = new NSI("x", NAMESPACE);
    
    //Methods
    /**
     * <p>
     * Retrieves the alt attribute from this <tt>MUCUserQuery</tt>.</p>
     *
     * <p>
     * The value returned by this method may be <tt>null</tt> if there is no
     * <tt>alt</tt> </p>
     *
     *
     * @return The alt attribute.
     */
    public String getAlt();
    
    /**
     * <p>
     * Sets the alt attribute to this
     * <tt>MUCUserQuery</tt>. </p>
     *
     * The value of <tt>alt</tt> cannot be <tt>null</tt> or an
     * <tt>IllegalArgumentException</tt> is thrown.</p>
     *
     * @param value The value of alt attribute.
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setAlt(String value) throws IllegalArgumentException;
    
    /**
     * <p>
     * Retrieves the value of <tt>status</tt> attribute. </p>
     *
     * @return The value of status or -1 if status is not present
     */
    public int getStatus();
    
    /**
     * <p>
     * Sets the value of <tt>status</tt> attribute. </p>
     *
     * @param The value of status
     * @throws IllegalArgumentException If the status is not in (100,201,301,303,307).
     */
    public void setStatus(int n) throws IllegalArgumentException;
    
    /**
     * <p>
     * Retrieves the password from this <tt>MUCUserQuery</tt>.</p>
     *
     * <p>
     * The value returned by this method may be <tt>null</tt> if there is no
     * <tt>Password</tt> </p>
     *
     *
     * @return The muc user password.
     */
    public String getPassword();
    
    /**
     * <p>
     * Sets the password for this
     * <tt>MUCUserQuery</tt>. </p>
     *
     * The value of <tt>password</tt> cannot be <tt>null</tt> or an
     * <tt>IllegalArgumentException</tt> is thrown.</p>
     *
     * @param name The muc user password.
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    public void setPassword(String password) throws IllegalArgumentException;
    
}
