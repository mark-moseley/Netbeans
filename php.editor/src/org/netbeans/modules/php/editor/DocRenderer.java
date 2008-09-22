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
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
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
import org.netbeans.modules.php.editor.parser.astnodes.ClassConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class DocRenderer {

    private static final String TD_STYLE = "style=\"text-aling:left; border-width: 1px;padding: 1px;border-style: solid;border-color: gray;padding:3px\" ";  //NOI18N
    private static final String TABLE_STYLE= "style=\"border-style:solid; border-color: black; border-width: 1px; width: 100%; border-collapse: collapse;\""; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());

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
            return documentIndexedElement(info, (IndexedElement) element);
        }

        return null;
    }

    private static String documentIndexedElement(CompilationInfo info, IndexedElement indexedElement) {

        StringBuilder description = new StringBuilder();
        final CCDocHtmlFormatter header = new CCDocHtmlFormatter();

        String location = null;
        
        if (indexedElement.getFile().isPlatform()){
            location = NbBundle.getMessage(PHPCodeCompletion.class, "PHPPlatform");
        } else {
            FileObject fobj = indexedElement.getFile().getFileObject();

            if (fobj != null) {
                Project project = FileOwnerQuery.getOwner(fobj);

                if (project != null) {
                    // find the appropriate source root
                    Sources sources = ProjectUtils.getSources(project);
                    // TODO the PHPSOURCE constatnt has to be published in the project api
                    SourceGroup[] groups = sources.getSourceGroups("PHPSOURCE");       //NOI18N
                    for (int i = 0; i < groups.length; i++) {
                        if (groups[i].contains(fobj)) {
                            location = FileUtil.getRelativePath(groups[i].getRootFolder(), fobj);
                            break;
                        }
                    }
                    if (location == null) {
                        // just to be sure, that the relative location was resolved
                        location = fobj.getPath();
                    }
                } else {
                    location = indexedElement.getFilenameUrl();
                }
            }
        }

        
        if (location != null) {
            header.appendHtml(String.format("<div align=\"right\"><font size=-1>%s</font></div>", location));  //NOI18N
        }

        final StringBuilder phpDoc = new StringBuilder();

        if (indexedElement.getOffset() > -1) {
            FileObject fo = indexedElement.getFileObject();
            
            if (fo == null){
                return null;
            }
           
            SourceModel model = SourceModelFactory.getInstance().getModel(fo);
            try {
                model.runUserActionTask(new PHPDocExtractor(
                        header, phpDoc, indexedElement), true);

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

    private static class PHPDocExtractor implements CancellableTask<CompilationInfo> {

        private CCDocHtmlFormatter header;
        private StringBuilder phpDoc;
        private IndexedElement indexedElement;

        public PHPDocExtractor(CCDocHtmlFormatter header, StringBuilder phpDoc, IndexedElement indexedElement) {
            this.header = header;
            this.phpDoc = phpDoc;
            this.indexedElement = indexedElement;
        }

        public void cancel() {
        }

        private void doFunctionDeclaration(FunctionDeclaration functionDeclaration) {
            String fname = CodeUtils.extractFunctionName(functionDeclaration);
            header.appendHtml("<font size=\"+1\">"); //NOI18N
            header.name(ElementKind.METHOD, true);
            header.appendText(fname);
            header.name(ElementKind.METHOD, false);
            header.appendHtml("</font>"); //NOI18N

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
        }
       
        private void extractPHPDoc(PHPDocBlock pHPDocBlock) {
            StringBuilder params = new StringBuilder();
            StringBuilder links = new StringBuilder();
            StringBuilder returnValue = new StringBuilder();
            StringBuilder others = new StringBuilder();

            phpDoc.append(pHPDocBlock.getDescription());

            // list PHPDoc tags
            phpDoc.append("<br />\n"); //NOI18N

            for (PHPDocTag tag : pHPDocBlock.getTags()) {

                switch (tag.getKind()) {
                    case PARAM:
                        PHPDocParamTagData tagData = new PHPDocParamTagData(tag.getValue());
                        String pline = String.format("<tr><td valign=\"top\" %s>%s</td><td valign=\"top\" %s><b>%s</b></td><td valign=\"top\" %s>%s</td></tr>\n", //NOI18N
                                TD_STYLE, tagData.type, TD_STYLE, tagData.name, TD_STYLE, tagData.description);

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
                phpDoc.append("<div style=\"padding-top:3px;\"><b>"); //NOI18N
                phpDoc.append(NbBundle.getMessage(PHPCodeCompletion.class, "Parameters"));
                phpDoc.append("</b></div>\n<table cellspacing=0 " + TABLE_STYLE + ">\n" + params + "</table>\n"); //NOI18N
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
        
        private String getElementValue(Program program, ASTNode node){
            if (node instanceof Identifier) {

                if ("define".equals(((Identifier) node).getName())) {  //NOI18N
                    FunctionInvocation invocation = (FunctionInvocation) Utils.getNodeAtOffset(
                            program, indexedElement.getOffset(), FunctionInvocation.class);

                    if (invocation != null) {
                        assert invocation.getStartOffset() == node.getStartOffset();

                        if (invocation.getParameters().size() > 1) {
                            Expression valExpr = invocation.getParameters().get(1);

                            if (valExpr instanceof Scalar) {
                                Scalar val = (Scalar) valExpr;
                                return val.getStringValue();
                            }
                        }
                    }
                }
            } else if (node instanceof ClassConstantDeclaration) {
                ClassConstantDeclaration classConstantDeclaration = (ClassConstantDeclaration) node;
                int constIdx = -1;

                for (int i = 0; i < classConstantDeclaration.getNames().size(); i++) {
                    Identifier id = classConstantDeclaration.getNames().get(i);

                    if (id.getName().equals(indexedElement.getName())) {
                        constIdx = i;
                        break;
                    }
                }

                if (constIdx >= 0) {
                    Expression valExpr = classConstantDeclaration.getInitializers().get(constIdx);

                    if (valExpr instanceof Scalar) {
                        Scalar val = (Scalar) valExpr;
                        return val.getStringValue();
                    }
                }
            }
            
            return null;
        }

        public void run(CompilationInfo ci) throws Exception {
            ParserResult presult = ci.getEmbeddedResults(PHPLanguage.PHP_MIME_TYPE).iterator().next();
            Program program = Utils.getRoot(presult.getInfo());

            if (program != null) {
                ASTNode node = Utils.getNodeAtOffset(program, indexedElement.getOffset());
                //header.appendHtml("<br/>"); //NOI18N

                if (node instanceof FunctionDeclaration) {
                    doFunctionDeclaration((FunctionDeclaration) node);
                } else {
                    header.name(indexedElement.getKind(), true);
                    header.appendText(indexedElement.getDisplayName());
                    header.name(indexedElement.getKind(), false);
                    
                    String valueStr = getElementValue(program, node);
                    
                    if (valueStr != null){
                        header.appendText(" = " + valueStr);
                    }
                }

                header.appendHtml("<br/><br/>"); //NOI18N
                Comment comment = Utils.getCommentForNode(program, node);

                if (comment instanceof PHPDocBlock) {
                    extractPHPDoc((PHPDocBlock) comment);
                }
            }

        }
    }
}


