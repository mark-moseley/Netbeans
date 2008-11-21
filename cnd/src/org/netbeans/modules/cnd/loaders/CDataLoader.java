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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.editor.filecreation.CndHandlableExtensions.class, position=300)
public class CDataLoader extends CndAbstractDataLoaderExt {
    
    private static CDataLoader instance;

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975685L;

    public CDataLoader() {
	super("org.netbeans.modules.cnd.loaders.CDataObject"); // NOI18N
        instance = this;
        //createExtentions(cExtensions);
    }

    public static CDataLoader getInstance(){
        if (instance == null) {
            instance = SharedClassObject.findObject(CDataLoader.class, true);
        }
        return instance;
    }

    /** set the default display name */
    @Override
    protected String defaultDisplayName() {
	return NbBundle.getMessage(CndAbstractDataLoader.class, "PROP_CDataLoader_Name"); // NOI18N
    }

    protected String getMimeType(){
        return MIMENames.C_MIME_TYPE;
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new CDataObject(primaryFile, this);
    }

    // CndHandlableExtensions
    
    public String getDisplayNameForExtensionList() {
	return NbBundle.getMessage(CDataLoader.class, "CDataLoader_Name_ForExtList"); // NOI18N
    }

    public String getSettingsName() {
        return ExtensionsSettings.C_FILE;  
    }
}
