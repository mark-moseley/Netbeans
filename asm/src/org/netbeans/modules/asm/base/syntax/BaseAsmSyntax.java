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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.asm.base.syntax;

import java.io.Reader;

import org.netbeans.modules.asm.base.att.ATTIdentResolver;
import org.netbeans.modules.asm.base.att.ATTParser;
import org.netbeans.modules.asm.model.AbstractAsmModel;
import org.netbeans.modules.asm.model.AsmSyntax;
import org.netbeans.modules.asm.model.lang.syntax.AsmHighlightLexer;
import org.netbeans.modules.asm.model.lang.syntax.AsmParser;

public abstract class BaseAsmSyntax implements AsmSyntax {
        
        final IdentResolver resolver;
        final ScannerFactory fact;
        
        public BaseAsmSyntax(AbstractAsmModel model, ScannerFactory fact) {                       
            this.fact = fact;
            resolver = new ATTIdentResolver(model);
        }

        public AsmParser createParser() {            
            ATTParser parser = new ATTParser(fact, resolver);
            patchAttParser(parser);
            
            return parser;
        }

        public AsmHighlightLexer createHighlightLexer(Reader input, Object state) {                                           
            return new AntlrLexer(fact.createScanner(input, state), 
                                  resolver);
        }    
        
        protected abstract void patchAttParser(ATTParser parser);
}
