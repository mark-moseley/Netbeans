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


package org.netbeans.modules.asm.base.syntax;

import java.util.logging.Logger;
import java.util.logging.Level;

import antlr.TokenStreamException;

import org.netbeans.modules.asm.model.lang.syntax.AsmBaseTokenId;
import org.netbeans.modules.asm.model.lang.syntax.AsmHighlightLexer;
import org.netbeans.modules.asm.model.lang.syntax.AsmTokenId;
import org.netbeans.modules.asm.model.lang.syntax.AsmToken;

public class AntlrLexer implements AsmHighlightLexer {
    
    private AntlrScanner scanner;
    private IdentResolver resolver;
    
    private int length;
    
    public AntlrLexer(AntlrScanner scanner, IdentResolver resolver) {        
        this.scanner = scanner;
        this.resolver = resolver;
        
        length = 0;
    }
    
    public AsmTokenId nextToken() {
     
        int start = scanner.getOffset();                    
        AsmTokenId tokId = AsmBaseTokenId.ASM_EMPTY;
        
        try {           
            AntlrToken antlrTok = (AntlrToken) scanner.nextToken();
            
            AsmToken asmTok = antlrTok.createAsmToken(resolver);
            tokId = asmTok.getId();
               
            length = antlrTok.getEndOffset() -
                     antlrTok.getStartOffset();
                        
            // FAKE STARTED 
            if ((tokId == AsmBaseTokenId.ASM_UNKWN_ID || 
                 tokId == AsmBaseTokenId.ASM_LABEL) &&
                 !asmTok.getText().startsWith(".") &&
                 !asmTok.getText().startsWith("_")) {
                
                tokId = AsmBaseTokenId.ASM_INSTRUCTION;                
            }
            // FAKE ENDED 

            return tokId;

        } catch (TokenStreamException ex) {            
            if (scanner.getPartState() == AntlrScanner.PartState.IN_COMMENT ||
                scanner.getPartState() == AntlrScanner.PartState.IN_STRING) {
                length = scanner.getOffset() - start;
                
            } else {
                Logger.getLogger(this.getClass().getName()).
                    log(Level.SEVERE, "Antlr highlight lexer crashed");

                length = 0;
            }            
        }
        
        return tokId;
    }
  

    public int getLastLength() {     
        return length;
    }
    
    public Object getState() {
        return scanner.getIntState();
    }
}
