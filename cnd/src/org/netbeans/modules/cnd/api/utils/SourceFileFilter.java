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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import org.openide.loaders.ExtensionList;

public abstract class SourceFileFilter extends javax.swing.filechooser.FileFilter {

    public SourceFileFilter() {
        super();
    }

    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory())
                return true;
            int index = f.getName().lastIndexOf('.');
            if (index >= 0) {
                // Match suffix
                String suffix = f.getName().substring(index+1);
                if (amongSuffixes(suffix, getSuffixes()))
                    return true;
            }
            else {
                // Match entire name
                if (amongSuffixes(f.getName(), getSuffixes()))
                    return true;
            }
        }
        return false;
    }
    
    public abstract String[] getSuffixes();
    
    public String getSuffixesAsString() {
        String ret = ""; // NOI18N
        String space = ""; // NOI18N
        for (int i = 0; i < getSuffixes().length; i++) {
            ret = ret + space + "." + getSuffixes()[i]; // NOI18N
            space = " "; // NOI18N
        }
        return ret;
    }
                    
    private boolean amongSuffixes(String suffix, String[] suffixes) {
	for (int i = 0; i < suffixes.length; i++) {
            if (IpeUtils.isSystemCaseInsensitive()) {
                if (suffixes[i].equalsIgnoreCase(suffix))
                    return true;
            } else {
                if (suffixes[i].equals(suffix))
                    return true;
            }
	}
	return false;
    }
    
    protected String[] getSuffixList(ExtensionList elist) {
        Enumeration<String> en = elist.extensions();
        ArrayList<String> list = new ArrayList<String>();
        
        while (en.hasMoreElements()) {
            list.add(en.nextElement());
        }
        return list.toArray(new String[list.size()]);
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}
