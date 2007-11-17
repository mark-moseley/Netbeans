/*****************************************************************************
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
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Thomas Singer.
 * Portions created by Thomas Singer are Copyright (C) 2001.
 * All Rights Reserved.
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
 *
 * Contributor(s): Thomas Singer.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.util;

import java.util.*;

/**
 * @author  Thomas Singer
 * @version Sep 26, 2001
 */
public class BundleUtilities {

    /**
     * Returns the package name of the specified class.
     * An empty String is returned, if the class is in the default package.
     */
    public static String getPackageName(Class clazz) {
        String fullClassName = clazz.getName();
        int lastDotIndex = fullClassName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return ""; // NOI18N
        }
        return fullClassName.substring(0, lastDotIndex);
    }

    /**
     * Returns the resourcename for the resource' shortName relative to the classInSamePackage.
     */
    public static String getResourceName(Class classInSamePackage, String shortName) {
        String packageName = getPackageName(classInSamePackage);
        String resourceName = packageName.replace('.', '/') + '/' + shortName;
        return resourceName;
    }

    /**
     * Returns the resource bundle for the specified resource' shortName relative to classInSamePackage.
     */
    public static ResourceBundle getResourceBundle(Class classInSamePackage, String shortName) {
        String resourceName = getResourceName(classInSamePackage, shortName);
        return ResourceBundle.getBundle(resourceName);
    }
}
