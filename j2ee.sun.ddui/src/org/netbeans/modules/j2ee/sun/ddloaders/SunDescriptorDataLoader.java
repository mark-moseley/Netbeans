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
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;


/** Recognizes deployment descriptors of sun descriptor files.
 *
 * @author Peter Williams
 */
public class SunDescriptorDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    
    private static final String [] SUPPORTED_MIME_TYPES = {
        "text/x-dd-sun-web+xml",            // NOI18N
        "text/x-dd-sun-ejb-jar+xml",        // NOI18N
        "text/x-dd-sun-application+xml",    // NOI18N
        "text/x-dd-sun-app-client+xml"      // NOI18N
    };
    
    public SunDescriptorDataLoader() {
        this("org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject");  // NOI18N
    }

    public SunDescriptorDataLoader(String name) {
        super(name);
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage (SunDescriptorDataLoader.class, "LBL_LoaderName"); // NOI18N
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-sun-dd/Actions/"; // NOI18N
    }
    
    @Override
    protected void initialize() {
         super.initialize();
         for (int i = 0; i < SUPPORTED_MIME_TYPES.length; i++) {
             getExtensions().addMimeType(SUPPORTED_MIME_TYPES[i]);
         }
     }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject result = null;
        
        if(!fo.isFolder() && DDType.getDDType(fo.getNameExt()) != null) {
            result = fo;
        }
        
        return result;
    }
    
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new SunDescriptorDataObject(primaryFile, this);
    }

}
