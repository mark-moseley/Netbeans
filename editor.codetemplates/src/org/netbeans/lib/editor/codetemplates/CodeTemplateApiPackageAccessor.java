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

package org.netbeans.lib.editor.codetemplates;

import java.util.List;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;


/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class CodeTemplateApiPackageAccessor {

    private static CodeTemplateApiPackageAccessor INSTANCE;

    public static CodeTemplateApiPackageAccessor get() {
        if (INSTANCE == null) {
            try {
                Class clazz = Class.forName(CodeTemplateManager.class.getName());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        
        assert INSTANCE != null : "There is no API package accessor available!"; //NOI18N
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(CodeTemplateApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract CodeTemplateManager createCodeTemplateManager(
    CodeTemplateManagerOperation operation);

    public abstract CodeTemplateManagerOperation getOperation(
    CodeTemplateManager manager);

    public abstract CodeTemplateManagerOperation getOperation(
    CodeTemplate codeTemplate);

    public abstract CodeTemplate createCodeTemplate(
        CodeTemplateManagerOperation managerOperation,
        String abbreviation, 
        String description, 
        String parametrizedText,
        List<String> contexts);

    public abstract String getSingleLineText(CodeTemplate codeTemplate);
}
