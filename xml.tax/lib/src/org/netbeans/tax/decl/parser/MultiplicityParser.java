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
package org.netbeans.tax.decl.parser;

import java.io.*;

import org.netbeans.tax.decl.*;

/** Utility parser parsing multiplisity marks. */
public class MultiplicityParser {
    
    /** Parse model content.
     * @param model string without starting delimiter.
     */
    public String parseMultiplicity (ParserReader model) {
        
        int ch = model.peek ();
        switch (ch) {
            case '?': case '+': case '*':
                
                try {
                    model.read (); //use peeked character
                } catch (IOException ex) {
                    ex.printStackTrace ();
                }
                
                return new String ( new char[] {(char) ch});
                
            default:
                return ""; // NOI18N
        }
    }
    
}
