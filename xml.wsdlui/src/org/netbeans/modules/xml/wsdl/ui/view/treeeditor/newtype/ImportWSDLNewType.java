/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.io.IOException;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCustomizer;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
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
        model.startTransaction();
        Import imp = model.getFactory().createImport();

        // Customize the new import.
        // Note this happens during the transaction, which is unforunate
        // but supposedly unavoidable.
        ImportWSDLCustomizer customizer = new ImportWSDLCustomizer(imp);
        DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(
                customizer, NbBundle.getMessage(ImportWSDLNewType.class,
                "LBL_NewType_ImportCustomizer"), true);
        descriptor.setValid(false);
        Object result = DialogDisplayer.getDefault().notify(descriptor);

        // If okay, add the import to the model.
        if (result == DialogDescriptor.OK_OPTION) {
            def.addImport(imp);
            //Check whether namespace was added. Temporary fix till import dialog 
            //mandates the prefix.
            if (Utility.getNamespacePrefix(imp.getNamespace(), model) == null) {
                //create a prefix for this namespace
                String prefix = NameGenerator.getInstance().generateNamespacePrefix(null, model);
                ((AbstractDocumentComponent) def).addPrefix(prefix, imp.getNamespace());
            }
        }
        // In either case, end the transaction.
            model.endTransaction();
    }
}
