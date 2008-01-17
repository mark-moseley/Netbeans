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
package org.netbeans.modules.php.model.impl.refs.resolvers;

import java.util.List;

import org.netbeans.modules.php.model.BaseCase;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.resources.ResourceMarker;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class IncludedModelsTest extends BaseCase {

    public void testIncludedModel() throws Exception {
        PhpModel model = getModel(ResourceMarker.MAIN_MODEL);
        model.sync();
        model.readLock();
        try {
            List<PhpModel> models = 
                ModelResolver.ResolverUtility.getIncludedModels(model);
            
            assert models.size() > 0 && models.get( 0 ) == model :
                "Expected to find at least model itself";
            
            assert models.size() == 3 : "Expected to find two imported models";
            
            PhpModel imported = models.get( 1 );
            assert imported != null :"Unexpected null imported model";
            FileObject fileObject = imported.getModelOrigin().getLookup().
                lookup( FileObject.class );
            assert fileObject != null;
            
            assert fileObject.getNameExt().equals( ResourceMarker.INCLUDED_MODEL ):
                "Expected to find source model for "+ResourceMarker.INCLUDED_MODEL+
                    " file , but found for :" +fileObject.getNameExt();
            
            imported = models.get( 2 );
            assert imported != null :"Unexpected null imported model";
            fileObject = imported.getModelOrigin().getLookup().lookup( 
                    FileObject.class );
            assert fileObject != null;
            
            assert fileObject.getNameExt().equals( 
                    ResourceMarker.SECOND_LEVEL_INCLUDED):
                "Expected to find source model for "+
                ResourceMarker.SECOND_LEVEL_INCLUDED+" file , but found for :" 
                +fileObject.getNameExt();
        }
        finally {
            model.readUnlock();
        }
    }
}
