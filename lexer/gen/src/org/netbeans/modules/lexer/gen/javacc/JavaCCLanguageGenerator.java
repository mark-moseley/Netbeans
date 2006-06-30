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

package org.netbeans.modules.lexer.gen.javacc;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.lexer.gen.DescriptionReader;
import org.netbeans.modules.lexer.gen.LanguageGenerator;
import org.netbeans.modules.lexer.gen.LanguageData;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;
import org.xml.sax.SAXException;

/**
 * Language class generator for javacc generated lexers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class JavaCCLanguageGenerator extends LanguageGenerator {

    protected void appendClassStart(StringBuffer sb, LanguageData data) {
        LexerGenUtilities.appendSpaces(sb, 4);
        sb.append("/** Maximum lexer state determined from xxxConstants class. */\n");
        LexerGenUtilities.appendSpaces(sb, 4);
        sb.append("static final int MAX_STATE = ");
        JavaCCTokenTypes jcctt = (JavaCCTokenTypes)data.getTokenTypes();
        sb.append(jcctt.getMaxState());
        sb.append(";\n\n");
        
        super.appendClassStart(sb, data);
    }
    
    public String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File xmlLangDescFile)
    throws ClassNotFoundException, SAXException, IOException {

        LanguageData data = new LanguageData();
        data.setLanguageClassName(langClassName);
        data.setLexerClassName(lexerClassName);

        // Apply token constants class info
        if (tokenTypesClassName != null) {
            Class tokenTypesClass = Class.forName(tokenTypesClassName);
            JavaCCTokenTypes tokenTypes = new JavaCCTokenTypes(tokenTypesClass);
            data.registerTokenTypes(tokenTypes);
        }

        // Apply possible xml description
        if (xmlLangDescFile != null) {
            DescriptionReader xmlLangDesc = new DescriptionReader(
                xmlLangDescFile.getAbsolutePath());

            xmlLangDesc.applyTo(data);

        }

        // Update int ids that do not have counterparts in token types
        data.updateUnassignedIntIds();

        return createSource(data);
    }

}

