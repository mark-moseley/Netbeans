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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * URLMapper for the nbinst URL protocol.
 * The mapper handles only the translation from URL into FileObjects.
 * The opposite conversion is not needed, it is handled by the default URLMapper.
 * The format of the nbinst URL is nbinst://host/path.
 * The host part is optional, if presents it specifies the name of the supplying module.
 * The path is mandatory and specifies the relative path from the ${netbeans.home}, ${netbeans.user}
 * or ${netbeans.dirs}.
 * @author  Tomas Zezula
 */
public class NbinstURLMapper extends URLMapper {
    
    public static final String PROTOCOL = "nbinst";     //NOI18N
    
    /** Creates a new instance of NbInstURLMapper */
    public NbinstURLMapper() {
    }

    /**
     * Returns FileObjects for given URL
     * @param url the URL for which the FileObjects should be find.
     * @return FileObject[], returns null in case of unknown protocol.
     */
    public FileObject[] getFileObjects(URL url) {         
        return (PROTOCOL.equals(url.getProtocol())) ? decodeURL (url) : null;
    }

    /**
     * Returns null, the translation into URL is doen by default URLMapper
     */
    public URL getURL(FileObject fo, int type) {
        return null;
    }

    /**
     * Resolves the nbinst URL into the array of the FileObjects.
     * @param url to be resolved
     * @return FileObject[], returns null if unknown url protocol.
     */
    static FileObject[] decodeURL (URL url) {
        assert url != null;
        try {
            URI uri = new URI (url.toExternalForm());
            String protocol = uri.getScheme();
            if (PROTOCOL.equals(protocol)) {
                String module = uri.getHost();
                String path = uri.getPath();
                if (path.length()>0) {
                    try {
                        File file = InstalledFileLocator.getDefault().locate(path.substring(1),module,false);
                        if (file != null) {
                            return new FileObject[] {URLMapper.findFileObject(file.toURI().toURL())};
                        }
                    }
                    catch (MalformedURLException mue) {
                        Exceptions.printStackTrace(mue);
                    }
                }
            }
        } catch (URISyntaxException use) {
            Exceptions.printStackTrace(use);
        }
        return null;
    }

}
