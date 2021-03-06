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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * filter for GNU C language
 * @author Vladimir Voskresensky
 */
public class APTGnuCFilter extends APTStdCFilter {
    
    /** Creates a new instance of APTGnuCFilter */
    public APTGnuCFilter() {
        initialize();
    }
    
    private void initialize() {
        // GNU C extensions 
        filter("__alignof__", APTTokenTypes.LITERAL_alignof); // NOI18N
        filter("__asm", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("__asm__", APTTokenTypes.LITERAL_asm); // NOI18N
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        filter("__complex__", APTTokenTypes.LITERAL___complex); // NOI18N
        filter("__const", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__const__", APTTokenTypes.LITERAL_const); // NOI18N
        filter("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        filter("__inline", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("__inline__", APTTokenTypes.LITERAL_inline); // NOI18N
        filter("__real__", APTTokenTypes.LITERAL___real); // NOI18N
        filter("__signed", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__signed__", APTTokenTypes.LITERAL_signed); // NOI18N
        filter("__typeof", APTTokenTypes.LITERAL_typeof); // NOI18N
        filter("__typeof__", APTTokenTypes.LITERAL_typeof); // NOI18N
        filter("__volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        filter("__volatile__", APTTokenTypes.LITERAL_volatile); // NOI18N        
    }
}
