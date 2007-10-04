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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.core.syntax.deprecated;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.java.JavaTokenContext;

/**
 * Fake token contexts for JSP scriptlets, declarations and expressions
 *
 * @author Marek Fukala
 * @version 1.00
 * @deprecated Use JSP lexer instead
 */

public class JspJavaFakeTokenContext {

    public static class JavaScriptletTokenContext extends TokenContext {

        // Context instance declaration
        public static final JavaScriptletTokenContext context = new JavaScriptletTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaScriptletTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    public static class JavaDeclarationTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaDeclarationTokenContext context = new JavaDeclarationTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaDeclarationTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    public static class JavaExpressionTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaExpressionTokenContext context = new JavaExpressionTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaExpressionTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            
        }
        
    }
    
    
}

