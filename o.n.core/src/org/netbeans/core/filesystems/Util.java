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

package org.netbeans.core.filesystems;

import org.openide.util.Utilities;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
class Util {
    /** Forbid creating new Util */
    private Util() {
    }

    static String[] addString(String[] array, String val) {
        if (array == null) {
            return new String[] {val};
        } else {
            String[] n = new String[array.length + 1];
            System.arraycopy(array, 0, n, 0, array.length);
            n[array.length] = val;
            return n;
        }
    }

    static int indexOf(Object[] where, Object what) {                    
        if (where == null) return -1;
        for (int i = 0; i<where.length; i++) {
            if (where[i].equals(what)) return i;
        }        
        return -1;
    }

    static int indexOf(String[] where, String what, boolean caseInsensitiv) {                  
        boolean isEqual;        
        
        for (int i = 0; where != null && i < where.length; i++) {            
            if (caseInsensitiv)
                isEqual = where[i].equalsIgnoreCase (what);
            else  
                isEqual = where[i].equals(what);
            
            if (isEqual)  return i;
        }                
        return -1;
    }
        
    static boolean contains(Object[] where, Object what) {
        return indexOf(where, what) != -1;
    }
    
    static boolean contains(String[] where, String what, boolean caseInsensitiv) {                    
        return indexOf(where, what, caseInsensitiv) != -1;
    }    
}
