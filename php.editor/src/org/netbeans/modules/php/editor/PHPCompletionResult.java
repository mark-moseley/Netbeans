/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor;

import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.spi.DefaultCompletionResult;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCompletionResult extends DefaultCompletionResult{
    private CodeCompletionContext completionContext;

    public PHPCompletionResult(CodeCompletionContext completionContext, List<CompletionProposal> list) {
        super(list, false);
        this.completionContext = completionContext;
    }

    @Override
    public void afterInsert(CompletionProposal item) {
        if (item instanceof PHPCompletionItem) {
            PHPCompletionItem phpItem = (PHPCompletionItem) item;
            
            if (phpItem.getElement() instanceof IndexedElement) {
                IndexedElement elem = (IndexedElement) phpItem.getElement();
                
                if (!elem.isResolved()){
                    try {
                        BaseDocument doc = (BaseDocument) completionContext.getInfo().getDocument();
                        
                        doc.atomicLock();
                        
                        try {
                            FileObject currentFolder = completionContext.getInfo().getFileObject().getParent();
                            String includePath = FileUtil.getRelativePath(currentFolder, elem.getFileObject());
                            
                            StringBuilder builder = new StringBuilder();
                            builder.append("\nrequire \""); //NOI18N
                            builder.append(includePath);
                            builder.append("\";\n"); //NOI18N
                            
                            // TODO use the index of previous AST instead
                            int prevLineNumber = Utilities.getLineOffset(doc, completionContext.getCaretOffset());
                            int prevLineEnd = Utilities.getRowStartFromLineOffset(doc, prevLineNumber);
                                                       
                            doc.insertString(prevLineEnd, builder.toString(), null);
                            Utilities.reformatLine(doc, Utilities.getRowStart(doc, prevLineEnd + 1));
                        } catch (BadLocationException e){
                            Exceptions.printStackTrace(e);
                        } finally {
                            doc.atomicUnlock();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
    }
}
