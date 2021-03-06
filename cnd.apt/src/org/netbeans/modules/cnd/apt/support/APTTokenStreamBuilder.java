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

package org.netbeans.modules.cnd.apt.support;

import antlr.TokenStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;

/**
 * Creates token stream for input path
 * 
 * @author Vladimir Voskresensky
 */
public class APTTokenStreamBuilder {
    private APTTokenStreamBuilder() {
    }   
    
//    public static TokenStream buildTokenStream(File file) throws FileNotFoundException {  
//        String path = file.getAbsolutePath();
//        // could be problems with closing this stream
//        InputStream stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);        
//        return buildTokenStream(path, stream);
//    }
    
    public static TokenStream buildTokenStream(String text) {  
        Reader reader = new StringReader(text);
        return  buildTokenStream(text, reader);
    }  
    
    public static TokenStream buildTokenStream(String name, InputStream stream) {  
        APTLexer lexer = new APTLexer(stream);
        lexer.init(name, 0);
        return lexer;
    }    

    public static TokenStream buildLightTokenStream(String name, InputStream stream) {  
        APTLexer lexer = new APTLexer(stream);
        lexer.init(name, 0);
        lexer.setOnlyPreproc(true);
        return lexer;
    }    
    
    public static TokenStream buildTokenStream(String name, Reader in) {  
        APTLexer lexer = new APTLexer(in);
        lexer.init(name, 0);
        return lexer;
    }     
}
