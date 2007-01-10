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
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.codegen.JavaCodeGenerator;
import org.netbeans.modules.vmd.api.io.CodeGenerator;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.openide.loaders.DataObject;

import javax.swing.text.StyledDocument;

/**
 * @author David Kaspar
 */
public class MidpCodeGenerator implements CodeGenerator {

    public void validateModelForCodeGeneration (DataObjectContext context, DesignDocument document) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.validateModelForCodeGeneration");
        }
    }

    public void updateModelFromCode (DataObjectContext context, DesignDocument document) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.updateModelFromCode");
            DataObject dataObject = context.getDataObject ();
            StyledDocument styledDocument = IOSupport.getDataObjectInteface (dataObject).getEditorDocument ();
            JavaCodeGenerator.getDefault ().updateUserCodesFromEditor (styledDocument);
        }
    }

    public void updateCodeFromModel (DataObjectContext context, DesignDocument document) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (context.getProjectType ())) {
//            System.out.println ("MidpCodeGenerator.updateCodeFromModel");
            DataObject dataObject = context.getDataObject ();
            StyledDocument styledDocument = IOSupport.getDataObjectInteface (dataObject).getEditorDocument ();
            JavaCodeGenerator.getDefault ().generateCode (styledDocument, document);
        }
    }

}
