/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lexer.gen;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.modules.lexer.gen.handcoded.HandcodedLanguageGenerator;
import org.xml.sax.SAXException;

/**
 * Generate language source from information contained
 * in xxxConstants class generated by JavaCC and
 * possibly the language description xml file if it's present.
 *
 * @author Miloslav Metelka
 */
public class HandcodedGenerateLanguageSource extends GenerateLanguageSource {
    
    protected String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File langDescFile)
    throws SAXException, IOException {
        
        HandcodedLanguageGenerator generator = new HandcodedLanguageGenerator();
        return generator.generate(langClassName, lexerClassName, null, langDescFile);
    }
            
}
