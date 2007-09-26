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

package org.netbeans.modules.url;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader which recognizes URL files.
 *
 * @author Ian Formanek
 */
public class URLDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-7407252842873642582L;
    /** MIME-type of URL files */
    private static final String URL_MIME_TYPE = "text/url";             //NOI18N
    
    
    /** Creates a new URLDataLoader without the extension. */
    public URLDataLoader() {
        super("org.netbeans.modules.url.URLDataObject");                //NOI18N
    }

    
    /**
     * Initializes this loader. This method is called only once the first time
     * this loader is used (not for each instance).
     */
    protected void initialize () {
        super.initialize();

        ExtensionList ext = new ExtensionList();
        ext.addMimeType(URL_MIME_TYPE);
        ext.addMimeType("text/x-url");                                  //NOI18N
        setExtensions(ext);
    }

    /** */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(URLDataLoader.class,
                                   "PROP_URLLoader_Name");              //NOI18N
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/text/url/Actions/";                             //NOI18N
    }
    
    /**
     * @return  <code>URLDataObject</code> for the specified file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new URLDataObject(primaryFile, this);
    }

}
