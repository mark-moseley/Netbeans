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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/** Syntax utils class.
 * 
 * @author  mf100882
 */
public class SyntaxUtils {
    
    /** Dumps token context names for given {@link org.netbeans.editor.TokenContextPath} */
    public static void dumpTokenContextPath(TokenContextPath tcp, PrintStream out) {
        TokenContext[] tcs = tcp.getContexts();
        for(int i = 0; i < tcs.length; i++ ) {
            String tcClassName = tcs[i].getClass().getName();
            tcClassName = tcClassName.substring(tcClassName.lastIndexOf(".") + 1);
            out.print(tcClassName + ( (i < (tcs.length - 1)) ? ", " : ""));
        }
    }
    
    /** converts \n to <NL> \t to <TAB> etc... */
    public static String normalize(String s, String[][] translationTable) {
        StringBuffer normalized = new StringBuffer();
        for(int i = 0; i < s.length(); i++) {
            String ch = s.substring(i,i+1);
            for(int j = 0; j < normalizeTable.length; j++) {
                if(ch.equals(normalizeTable[j][0])) ch = normalizeTable[j][1];
            }
            normalized.append(ch);
        }
        return normalized.toString();
    }
    
    /** the some as {@ling normalize(String s, String[][] translationTable)} 
     * but uses default translation table. */
    public static String normalize(String s) {
        return normalize(s, normalizeTable);
    }
    
    public static final String[][] normalizeTable = {{"\n", "<NL>"},
                                                      {"\t", "<TAB>"}};
    
}
