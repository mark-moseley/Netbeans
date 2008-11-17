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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.api.utils;

import java.util.ArrayList;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;

/** Miscellaneous utility classes useful for the C/C++/Fortran module */
public class CppUtils {
    
    public static String reformatWhitespaces(String string)  {
        return reformatWhitespaces(string, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend)  {
        return reformatWhitespaces(string, prepend, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend, String delimiter)  {
        if (string == null || string.length() == 0)
            return string;
        
        boolean firstToken = true;
        ArrayList<String> tokens = tokenizeString(string);
        StringBuilder formattedString = new StringBuilder(string.length());
        for (String token : tokens) {
            if (!firstToken) {
                formattedString.append(delimiter);
                formattedString.append(" "); // NOI18N
            }
            formattedString.append(prepend);
            formattedString.append(token);
            firstToken = false;
        }
        
        return formattedString.toString();
    }
    
    public static ArrayList<String> tokenizeString(String string)  {
        ArrayList<String> list = new ArrayList<String>(0);
        
        if (string == null || string.length() == 0)
            return list;
        StringBuilder token = new StringBuilder();
        boolean inToken = false;
        boolean inQuote = false;
        char quoteChar = '\0';
        for (int i = 0; i <= string.length(); i++) {
            boolean eol = (i == string.length());
            if (eol || inToken) {
                if (!eol && inQuote) {
                    token.append(string.charAt(i));
                    if (string.charAt(i) == quoteChar)
                        inQuote = false;
                } else {
                    if (eol || Character.isWhitespace(string.charAt(i))) {
                        if (token.length() > 0) {
                            list.add(token.toString());
                            }
                        inToken = false;
                        token = new StringBuilder();
                    } else {
                        token.append(string.charAt(i));
                        if (string.charAt(i) == '"' || string.charAt(i) == '`' || string.charAt(i) == '\'') {
                            inQuote = true;
                            quoteChar = string.charAt(i);
                        }
                    }
                }
            } else {
                if (!Character.isWhitespace(string.charAt(i))) {
                    token.append(string.charAt(i));
                    inToken = true;
                }
            }
        }
        if (token.length() > 0)
            list.add(token.toString());
        
        return list;
    }

    /** Use org.netbeans.modules.cnd.api.compilers.CompilerSetManager.getCygwinBase() instead */
    @Deprecated
    public static String getCygwinBase() {
        return CompilerSetManager.getCygwinBase();
    }
    
    /** Use org.netbeans.modules.cnd.api.compilers.CompilerSetManager.getMSysBase() instead */
    @Deprecated
    public static String getMSysBase() {
        return CompilerSetManager.getMSysBase();
    }
}

