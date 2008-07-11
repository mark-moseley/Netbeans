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
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class PHPDocRenderer {

    static String document(CompilationInfo info, ElementHandle element) {
        if (element instanceof PHPDOCTagElement) {
            PHPDOCTagElement pHPDOCTagElement = (PHPDOCTagElement) element;
            return pHPDOCTagElement.getDoc();
        }

        if (element instanceof PredefinedSymbolElement) {
            PredefinedSymbolElement predefinedSymbolElement = (PredefinedSymbolElement) element;
            return predefinedSymbolElement.getDoc();
        }

        if (element instanceof IndexedElement) {
            final IndexedElement indexedElement = (IndexedElement) element;
            StringBuilder description = new StringBuilder();
            final CCDocHtmlFormatter header = new CCDocHtmlFormatter();

            String location = indexedElement.getFile().isPlatform() ? NbBundle.getMessage(PHPCodeCompletion.class, "PHPPlatform")
                    : indexedElement.getFilenameUrl();

            header.appendHtml(String.format("<font size=-1>%s</font>", location));

            final StringBuilder phpDoc = new StringBuilder();

            if (indexedElement.getOffset() > -1) {
                FileObject fo = element.getFileObject();
                SourceModel model = SourceModelFactory.getInstance().getModel(fo);
                try {

                    model.runUserActionTask(new CancellableTask<CompilationInfo>() {

                        public void cancel() {
                        }

                        public void run(CompilationInfo ci) throws Exception {
                            ParserResult presult = ci.getEmbeddedResults(PHPLanguage.PHP_MIME_TYPE).iterator().next();
                            Program program = Utils.getRoot(presult.getInfo());

                            if (program != null) {
                                ASTNode node = Utils.getNodeAtOffset(program, indexedElement.getOffset());
                                header.appendHtml("<p><font size=+1>"); //NOI18N

                                if (node instanceof FunctionDeclaration) {
                                    FunctionDeclaration functionDeclaration = (FunctionDeclaration) node;
                                    String fname = CodeUtils.extractFunctionName(functionDeclaration);
                                    header.name(ElementKind.METHOD, true);
                                    header.appendText(fname);
                                    header.name(ElementKind.METHOD, false);
                                    header.appendHtml("</font>");

                                    header.parameters(true);
                                    header.appendText("("); //NOI18N
                                    int paramCount = functionDeclaration.getFormalParameters().size();

                                    for (int i = 0; i < paramCount; i++) {
                                        FormalParameter param = functionDeclaration.getFormalParameters().get(i);

                                        if (param.getParameterType() != null) {
                                            header.type(true);
                                            header.appendText(param.getParameterType().getName() + " "); //NOI18N
                                            header.type(false);
                                        }

                                        header.appendText(CodeUtils.getParamDisplayName(param));

                                        if (param.getDefaultValue() != null) {
                                            header.type(true);
                                            header.appendText("=");

                                            if (param.getDefaultValue() instanceof Scalar) {
                                                Scalar scalar = (Scalar) param.getDefaultValue();
                                                header.appendText(scalar.getStringValue());
                                            }

                                            header.type(false);
                                        }

                                        if (i + 1 < paramCount) {
                                            header.appendText(", "); //NOI18N
                                        }
                                    }

                                    header.appendText(")");
                                    header.parameters(false);

                                } else {
                                    header.name(indexedElement.getKind(), true);
                                    header.appendText(indexedElement.getDisplayName());
                                    header.name(indexedElement.getKind(), false);
                                }

                                header.appendHtml("</p><br>"); //NOI18N

                                Comment comment = Utils.getCommentForNode(program, node);

                                if (comment instanceof PHPDocBlock) {
                                    StringBuilder params = new StringBuilder();
                                    StringBuilder links = new StringBuilder();
                                    StringBuilder returnValue = new StringBuilder();
                                    StringBuilder others = new StringBuilder();


                                    PHPDocBlock pHPDocBlock = (PHPDocBlock) comment;
                                    phpDoc.append(pHPDocBlock.getDescription());

                                    // list PHPDoc tags
                                    // TODO a better support for PHPDoc tags
                                    phpDoc.append("<br>\n"); //NOI18N

                                    for (PHPDocTag tag : pHPDocBlock.getTags()) {

                                        switch (tag.getKind()) {
                                            case PARAM:
                                                String parts[] = tag.getValue().split("\\s+", 3); //NOI18N
                                                String paramName,
                                                 paramType,
                                                 paramDesc;
                                                paramName = paramType = paramDesc = ""; //NOI18N

                                                if (parts.length > 0) {
                                                    paramName = parts[0];
                                                    if (parts.length > 1) {
                                                        paramType = parts[1];
                                                        if (parts.length > 2) {
                                                            paramDesc = parts[2];
                                                        }
                                                    }
                                                }

                                                String optionalStr = "[optional]"; //NOI18N
                                                if (paramType.endsWith(optionalStr)) {
                                                    paramType = paramType.substring(0, paramType.length() - optionalStr.length());
                                                }

                                                String pline = String.format("<tr><td align=\"right\">%s</td><th  align=\"left\">$%s</th><td>%s</td></tr>\n", //NOI18N
                                                        paramType, paramName, paramDesc);

                                                params.append(pline);
                                                break;
                                            case LINK:
                                                String lline = String.format("<a href=\"%s\">%s</a><br>\n", //NOI18N
                                                        tag.getValue(), tag.getValue());

                                                links.append(lline);
                                                break;
                                            case RETURN:
                                                String rparts[] = tag.getValue().split("\\s+", 2); //NOI18N

                                                if (rparts.length > 0) {
                                                    String type = rparts[0];
                                                    returnValue.append(String.format("<b>%s:</b> %s<br><br>", //NOI18N
                                                            NbBundle.getMessage(PHPCodeCompletion.class, "Type"), type));

                                                    if (rparts.length > 1) {
                                                        String desc = rparts[1];
                                                        returnValue.append(desc);
                                                    }
                                                }

                                                break;
                                            default:
                                                String oline = String.format("<tr><th>%s</th><td>%s</td></tr>\n", //NOI18N
                                                        tag.getKind().toString(), tag.getValue());

                                                others.append(oline);
                                                break;
                                        }
                                    }


                                    if (params.length() > 0) {
                                        phpDoc.append("<h3>"); //NOI18N
                                        phpDoc.append(NbBundle.getMessage(PHPCodeCompletion.class, "Parameters"));
                                        phpDoc.append("</h3>\n<table>\n" + params + "</table>\n"); //NOI18N
                                    }

                                    if (returnValue.length() > 0) {
                                        phpDoc.append("<h3>"); //NOI18N
                                        phpDoc.append(NbBundle.getMessage(PHPCodeCompletion.class, "ReturnValue"));
                                        phpDoc.append("</h3>\n" + returnValue); //NOI18N
                                    }

                                    if (links.length() > 0) {
                                        phpDoc.append("<h3>"); //NOI18N
                                        phpDoc.append(NbBundle.getMessage(PHPCodeCompletion.class, "OnlineDocs"));
                                        phpDoc.append("</h3>\n" + links); //NOI18N
                                    }

                                    if (others.length() > 0) {
                                        phpDoc.append("<table>\n" + others + "</table>\n"); //NOI18N
                                    }
                                }
                            }

                        }
                    }, true);

                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (phpDoc.length() > 0) {
                description.append(phpDoc);
            } else {
                description.append(NbBundle.getMessage(PHPCodeCompletion.class, "PHPDocNotFound"));
            }

            return header.getText() + description.toString();
        }

        return null;
    }
}


