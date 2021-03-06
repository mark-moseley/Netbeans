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

package org.netbeans.api.db.explorer;

import java.net.URL;
import java.util.Arrays;

/**
 * Encapsulates a JDBC driver.
 */
public final class JDBCDriver {

    private URL[] urls;
    private String clazz;
    private String displayName;
    private String name;

    JDBCDriver(String name, String displayName, String clazz, URL[] urls) {
        assert name != null && displayName != null && clazz != null && urls != null;
        this.name = name;
        this.displayName = displayName;
        this.clazz = clazz;
        this.urls = urls;
    }

    /**
     * Creates a new JDBCDriver instance.
     *
     * @param name the programmatic name of the driver; must not be null.
     * @param displayName the display name of the driver (used for example to display the driver in the UI); must not be null.
     * @param clazz the JDBC driver class; must not be null.
     * @param urls the array of the JDBC driver files URLs; must not be null.
     * 
     * @throws NullPointerException if any of the parameters is null.
     */
    public static JDBCDriver create(String name, String displayName, String clazz, URL[] urls) {
        if (name == null || displayName == null || clazz == null || urls == null) {
            throw new NullPointerException();
        }
        return new JDBCDriver(name, displayName, clazz, urls);
    }
    
    /**
     * Returns the array of the JDBC driver files URLs.
     *
     * @return the non-null array of the JDBC driver files URLs.
     */
    public URL[] getURLs() {
        return urls;
    }
    
    /**
     * Returns the JDBC driver class name.
     *
     * @return the JDBC driver class name.
     */
    public String getClassName() {
        return clazz;
    }
    
    /**
     * Returns the display name of the driver (used for example to display the driver in the UI).
     *
     * @return the display name of the driver.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Return the programmatic driver name.
     *
     * @return the programmatic driver name.
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "JDBCDriver[name='" + name + // NOI18N
                "',displayName='" + displayName + // NOI18N
                "',className='" + clazz + // NOI18N
                "',urls=" + Arrays.asList(urls) + "]"; // NOI18N
    }
}
