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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.awt.Dialog;
import java.io.IOException;
import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCreator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 * NewType for importing a WSDL file.
 *
 * @author  Nathan Fiedler
 */
public class ImportWSDLNewType extends NewType {
    /** Component in which to create import. */
    private WSDLComponent component;

    /**
     * Creates a new instance of ImportWSDLNewType.
     *
     * @param  component  the WSDL component in which to create the import.
     */
    public ImportWSDLNewType(WSDLComponent component) {
        this.component = component;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportWSDLNewType.class,
                "LBL_NewType_ImportWSDL");
    }

    @Override
    public void create() throws IOException {
        // Create the new import with empty attributes.
        WSDLModel model = component.getModel();
        Definitions def = model.getDefinitions();
        //Store existing imports, to find which imports got added.
        Collection<Import> oldImports = def.getImports();
        
        model.startTransaction();

        // Create the new import(s).
        // Note this happens during the transaction, which is unforunate
        // but supposedly unavoidable.
        ImportWSDLCreator customizer = new ImportWSDLCreator(def);
        DialogDescriptor descriptor = UIUtilities.getCreatorDialog(
                customizer, NbBundle.getMessage(ImportWSDLNewType.class,
                "LBL_NewType_ImportCustomizer"), true);
        descriptor.setValid(false);
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        
        // Creator will have created the import(s) by now.
        // In either case, end the transaction.
        model.endTransaction();
        
        Import lastAdded = null;
        Collection<Import> newImports = def.getImports();
        for (Import imp : newImports) {
            if (!oldImports.contains(imp)) {
                lastAdded = imp;
            }
        }
        
        ActionHelper.selectNode(lastAdded);
    }
}
