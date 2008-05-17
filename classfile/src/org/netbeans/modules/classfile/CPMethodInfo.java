/*
 * CPMethodInfo.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;


/**
 * A class representing the CONSTANT_Methodref constant pool type.
 *
 * @author Thomas Ball
 */
public class CPMethodInfo extends CPFieldMethodInfo {
    CPMethodInfo(ConstantPool pool,int iClass,int iNameAndType) {
        super(pool, iClass, iNameAndType);
    }

    public final String getMethodName() {
	return getFieldName();
    }

    /**
     * Get method name and signature, such as "void setBar(Bar)".
     */
    public final String getFullMethodName() {
        return getFullMethodName(getMethodName(), getDescriptor());
    }
    
    static String getFullMethodName(String name, String signature) {
        StringBuffer sb = new StringBuffer();
        int index = signature.indexOf(')');
        String params = signature.substring(1, index);
        
        if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
            String ret = signature.substring(index + 1);
            ret = CPFieldMethodInfo.getSignature(ret, false);
            if (ret.length() > 0) {
                sb.append(ret);
                sb.append(' ');
            }
        }
        sb.append(name);
        sb.append('(');
        index = 0;
        int paramsLength = params.length();
        while (index < paramsLength) {
            StringBuffer p = new StringBuffer();
            char ch = params.charAt(index++);
            while (ch == '[') {
                p.append(ch);
                ch = params.charAt(index++);
            }
            p.append(ch);
            if (ch == 'L')
                do {
                    ch = params.charAt(index++);
                    p.append(ch);
                } while (ch != ';');
            sb.append(CPFieldMethodInfo.getSignature(p.toString(), false));
            if (index < paramsLength)
                sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }

    public int getTag() {
	return ConstantPool.CONSTANT_MethodRef;
    }
}
