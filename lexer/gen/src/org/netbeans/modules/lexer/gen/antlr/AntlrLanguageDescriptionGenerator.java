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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.gen.antlr;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.lexer.gen.LanguageData;
import org.netbeans.modules.lexer.gen.TokenTypes;
import org.netbeans.spi.lexer.util.LexerUtilities;

/**
 * Generates initial skeleton of the xml file
 * (describing the particular language) from the class
 * (or interface) that contains integer constants.
 * Such file is typically generated by the lexer
 * generator tools.
 * <BR>This tool is useful when the grammar file
 * of the particular language already
 * exists because it creates the  initial version
 * of the language xml description that can be
 * further customized.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class AntlrLanguageDescriptionGenerator {

    public String generate(String tokenTypesClassName) throws ClassNotFoundException {
        LanguageData data = new LanguageData();

        // Apply token types class info
        Class tokenTypesClass = Class.forName(tokenTypesClassName);
        TokenTypes tokenTypes = new TokenTypes(tokenTypesClass);
        data.registerTokenTypes(tokenTypes);

        return data.createDescription();
    }

}

