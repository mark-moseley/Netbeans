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

package org.netbeans.modules.languages.ext;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.SyntaxContext;

/**
 *
 * @author Jan Jancura
 */
public class NBS {
    
    private static final String DOC = "org/netbeans/modules/languages/resources/NBS-Library.xml";
    
    public static List<CompletionItem> completion (Context context) {
        if (context instanceof SyntaxContext) {
            SyntaxContext sContext = (SyntaxContext) context;
            ASTPath path = sContext.getASTPath ();
            String c = "root";
            ListIterator<ASTItem> it = path.listIterator (path.size () - 1);
            boolean properties = false;
            while (it.hasPrevious ()) {
                ASTItem item =  it.previous ();
                if (item instanceof ASTToken) continue;
                ASTNode node = (ASTNode) item;
                if (node.getNT ().equals ("regularExpression"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("selector"))
                    return Collections.<CompletionItem>emptyList ();
                else
                if (node.getNT ().equals ("value"))
                    properties = true;
                else
                if (node.getNT ().equals ("command")) {
                    String p = node.getTokenTypeIdentifier ("keyword");
                    if (p != null && properties) {
                        c = p;
                        break;
                    }
                }
            }
            System.out.println(c);
            return getLibrary ().getCompletionItems (c);
        }
        return Collections.<CompletionItem>emptyList ();
    }
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (DOC);
        return library;
    }
}
