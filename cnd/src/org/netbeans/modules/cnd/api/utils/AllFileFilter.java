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

import java.util.Vector;
import org.openide.util.NbBundle;

public class AllFileFilter extends SourceFileFilter {

    private static AllFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllFileFilter getInstance() {
        if (instance == null)
            instance = new AllFileFilter();
        return instance;
    }

    public String getDescription() {
        return NbBundle.getMessage(SourceFileFilter.class, "FILECHOOSER_All_FILEFILTER", getSuffixesAsString()); // NOI18N
    }
    
    
    public String[] getSuffixes() {
        if (suffixes == null)
            suffixes = getAllSuffixes();
        return suffixes;
    }
    
    public String getSuffixesAsString() {
        String suf = AllSourceFileFilter.getInstance().getSuffixesAsString();
        suf += ResourceFileFilter.getInstance().getSuffixesAsString();
        return suf;
    }
    
    public static String[] getAllSuffixes() {
        Vector suffixes = new Vector();
        addSuffices(suffixes, AllSourceFileFilter.getInstance().getSuffixes());
        addSuffices(suffixes, ResourceFileFilter.getInstance().getSuffixes());
        return (String[])suffixes.toArray(new String[suffixes.size()]);
    }
    
    private static void addSuffices(Vector suffixes, String[] suffixes2) {
        for (int i = 0; i < suffixes2.length; i++)
            suffixes.add(suffixes2[i]);
    }
}
