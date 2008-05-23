/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.filecreation;

import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author sg155630
 */
public class CndClassWizardIterator extends CCFSrcFileIterator {

    @Override
    public void initialize(TemplateWizard wiz) {
        targetChooserDescriptorPanel = wiz.targetChooser();
    }

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder ();
        DataObject template = wiz.getTemplate ();

	String filename = wiz.getTargetName();

        FileObject bro = FileUtil.findBrother(template.files().iterator().next(), "h");
        DataObject dobjBro = DataObject.find(bro);
        
        Set<DataObject> res = new HashSet<DataObject>();
        res.add(template.createFromTemplate(targetFolder, filename + ".cc"));
        res.add(dobjBro.createFromTemplate(targetFolder, filename + ".h"));
        
	return res;
    }

    @Override
    public String name() {
        return "CndClassWizardIterator"; //NOI8N
    }

}
