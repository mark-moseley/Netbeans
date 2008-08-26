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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Project provider for single files.
 *
 * This provider create project for FileObject which is not in the model
 * and return CsmFile for it. This 'dummy' project could be removed
 * if corresponding file is included in the model as a part of any usual project.
 * 
 * @author Leonid Mesnik
 */
public abstract class CsmStandaloneFileProvider {
    /** A dummy provider that never returns any results.
     */
    private static final CsmStandaloneFileProvider EMPTY = new Empty();
    
    /** default instance */
    private static CsmStandaloneFileProvider defaultProvider;
    
    
    /** Static method to obtain the provider.
     * @return the provider
     */
    public static synchronized CsmStandaloneFileProvider getDefault() {
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(CsmStandaloneFileProvider.class);
        return defaultProvider == null ? EMPTY : defaultProvider;
    }
    
    /**
     *  This method returns CsmFile for this FileObject. The new project will
     *  be created for this file if it is not in the model.
     * @param file FileObject for which CsmFile should be created
     * @return CsmFile for given file or null if it could not be created in the model
     */

    public abstract CsmFile getCsmFile(FileObject file);

    /**
     * This method notifies provider that the editor tab for given file is closed
     *  and file should be removed from model. The provider remoeves this file from
     *  the model.
     * @param file The file which should be removed from model
     */
    public abstract void notifyClosed(CsmFile file);
    
    //
    // Implementation of the default provider
    //
    private static final class Empty extends CsmStandaloneFileProvider {
        Empty() {
        }

        @Override
        public CsmFile getCsmFile(FileObject file) {
            return null;
        }

        public void notifyClosed(CsmFile file) {
        
        }
    } 
}
