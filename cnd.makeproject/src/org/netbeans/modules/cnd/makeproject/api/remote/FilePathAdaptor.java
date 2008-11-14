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

package org.netbeans.modules.cnd.makeproject.api.remote;

import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class FilePathAdaptor {

    public static String mapToRemote(String local) {
        FilePathMapper conv = getConverting();
        return conv.mapToRemote(local);
    }

    public static String mapToLocal(String remote) {
        FilePathMapper conv = getConverting();
        return conv.mapToLocal(remote);
    }

    public static String normalize(String path) {
        FilePathMapper conv = getConverting();
        return conv.normalize(path);
    }
    
    public static String naturalize(String path) {
        FilePathMapper conv = getConverting();
        return conv.naturalize(path);
    }

    private static FilePathMapper getConverting() {
        FilePathMapper conv = Lookup.getDefault().lookup(FilePathMapper.class);
        return conv == null ? FilePathMapperDefault.DEFAULT : conv;
    }
    
    private static class FilePathMapperDefault implements FilePathMapper {
        public final static FilePathMapper DEFAULT = new FilePathMapperDefault();
        
        public String mapToRemote(String local) {
            return local;
        }

        public String mapToLocal(String remote) {
            return remote;
        }

        public String normalize(String path) {
            // Always use Unix file separators
            return path.replaceAll("\\\\", "/"); // NOI18N
        }
        
        public String naturalize(String path) {
            if (Utilities.isUnix())
                return path.replaceAll("\\\\", "/"); // NOI18N
            else if (Utilities.isWindows())
                return path.replaceAll("/", "\\\\"); // NOI18N
            else
                return path;
        }
    }
}
