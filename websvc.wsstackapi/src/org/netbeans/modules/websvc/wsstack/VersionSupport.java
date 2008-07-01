/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.wsstack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;

/**
 *
 * @author mkuchtiak
 */
public  class VersionSupport {
    
    public static WSStackVersion parseVersion(String version) {
        int major = 0;
        int minor = 0;
        int micro = 0;
        int update = 0;
        
        int tokenPosition = 0;
        StringTokenizer versionTokens = new StringTokenizer(version,"."); //NOI18N
        List<Integer> versionNumbers = new ArrayList<Integer>();
        while (versionTokens.hasMoreTokens()) {
            versionNumbers.add(valueOf(versionTokens.nextToken().trim()));
        }
        switch (versionNumbers.size()) {
            case 0 : {
                break;
            }
            case 1 : {
                major = versionNumbers.get(0).intValue();
                break;
            }
            case 2 : {
                major = versionNumbers.get(0).intValue();
                minor = versionNumbers.get(1).intValue();
                break;
            } 
            case 3 : {
                major = versionNumbers.get(0).intValue();
                minor = versionNumbers.get(1).intValue();
                micro = versionNumbers.get(2).intValue();
                break;
            } 
            default: {
                major = versionNumbers.get(0).intValue();
                minor = versionNumbers.get(1).intValue();
                micro = versionNumbers.get(2).intValue();
                update = versionNumbers.get(3).intValue();
            }
        }
        return WSStackVersion.valueOf(major, minor, micro, update);
    }

    private static Integer valueOf(String versionToken) {
        int i = 0;
        StringBuffer buf = new StringBuffer();
        while (i<versionToken.length()) {
            char ch = versionToken.charAt(i);
            if (Character.isDigit(ch)) {
                buf.append(ch);
            } else {
                break;
            }
            ++i;
        }
        return buf.length() > 0 ? Integer.valueOf(buf.toString()) : Integer.valueOf(0);
    }
}
