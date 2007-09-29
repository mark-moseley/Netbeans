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

/**
 *
 * @author nn136682
 */
package org.netbeans.modules.xml.schema.model.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.impl.DerivationsImpl.DerivationSet;

public class Util {

    public static <T extends Enum> T parse(Class<T> type, String s) {
        try {
            Method m = type.getMethod("values", new Class[] {});
            T[] values = (T[]) (m.invoke(null, new Object[0]));
            for (T value : values) {
                if (value.toString().equals(s)) {
                    return value;
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        throw new IllegalArgumentException("Invalid String value " + s);
    }
    
    public static <F extends Enum, T extends Enum> Set<T> convertEnumSet(Class<T> toType, Set<F> values) {
        Set<T> result = new DerivationSet<T>();
        for (F v : values) {
            T t = toType.cast(Enum.valueOf(toType, v.name()));
            result.add(t);
        }
        return result;
    }
    
    public static <T extends Enum> Set<T> valuesOf(Class<T> type, String s) {
//        StringTokenizer tokenizer = new StringTokenizer(s, SEP);
        StringTokenizer tokenizer = new StringTokenizer(s); // to escape tabs and new lines as well
        Set<T> result = new DerivationSet<T>();
        if(tokenizer.countTokens()==0) { // to consider blank ("") string
            T value = parse(type, s);
            result.add(value);
        } else {
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                T value = parse(type, token);
                result.add(value);
            }
        }
        return result;
    }
    
    public static final String SEP = " ";
}
