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
package org.netbeans.modules.groovy.editor;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.groovy.editor.elements.KeywordElement;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.elements.ElementHandleSupport;
import org.netbeans.modules.groovy.editor.elements.GroovyElement;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.spi.DefaultCompletionResult;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CodeCompleter implements CodeCompletionHandler {

    private static ImageIcon groovyIcon;
    private static ImageIcon javaIcon;
    private int anchor;
    private final Logger LOG = Logger.getLogger(CodeCompleter.class.getName());
    private String jdkJavaDocBase = null;
    private String groovyJavaDocBase = null;
    private String gapiDocBase = null;

    public CodeCompleter() {
        LOG.setLevel(Level.OFF);

        JavaPlatformManager platformMan = JavaPlatformManager.getDefault();
        JavaPlatform platform = platformMan.getDefaultPlatform();
        List<URL> docfolder = platform.getJavadocFolders();

        for (URL url : docfolder) {
            LOG.log(Level.FINEST, "JDK Doc path: {0}", url.toString()); // NOI18N
            jdkJavaDocBase = url.toString();
        }

        GroovySettings groovySettings = new GroovySettings();
        String docroot = groovySettings.getGroovyDoc() + "/";

        groovyJavaDocBase = directoryNameToUrl(docroot + "groovy-jdk/"); // NOI18N
        gapiDocBase = directoryNameToUrl(docroot + "gapi/"); // NOI18N

        LOG.log(Level.FINEST, "GDK Doc path: {0}", groovyJavaDocBase);
        LOG.log(Level.FINEST, "GAPI Doc path: {0}", gapiDocBase);
    }

    String directoryNameToUrl(String dirname) {
        if (dirname == null) {
            return "";
        }

        File dirFile = new File(dirname);

        if (dirFile != null && dirFile.exists() && dirFile.isDirectory()) {
            String fileURL = "";
            if (Utilities.isWindows()) {
                dirname = dirname.replace("\\", "/");
                fileURL = "file:/"; // NOI18N
            } else {
                fileURL = "file://"; // NOI18N
            }
            return fileURL + dirname;
        } else {
            return "";
        }
    }

    private void populateProposal(Class clz, Object method, CompletionRequest request, List<CompletionProposal> proposals, boolean isGDK) {
        if (method != null && (method instanceof MetaMethod)) {
            MetaMethod mm = (MetaMethod) method;

            if (!request.prefix.equals("")) {
                if (mm.getName().startsWith(request.prefix)) {
                    MethodItem item = new MethodItem(clz, mm, anchor, request, isGDK);
                    proposals.add(item);
                }
            } else {
                MethodItem item = new MethodItem(clz, mm, anchor, request, isGDK);
                proposals.add(item);
            }
        }
    }

    private void printASTNodeInformation(ASTNode node) {

        LOG.log(Level.FINEST, "--------------------------------------------------------");

        if (node == null) {
            LOG.log(Level.FINEST, "node == null");
        } else {
            LOG.log(Level.FINEST, "Node.getText()  : " + node.getText());
            LOG.log(Level.FINEST, "Node.toString() : " + node.toString());
            LOG.log(Level.FINEST, "Node.getClass() : " + node.getClass());

            if (node instanceof ModuleNode) {
                LOG.log(Level.FINEST, "ModuleNode.getClasses() : " + ((ModuleNode) node).getClasses());
                LOG.log(Level.FINEST, "SourceUnit.getName() : " + ((ModuleNode) node).getContext().getName());
            }
        }
    }

    private void printMethod(MetaMethod mm) {

        LOG.log(Level.FINEST, "--------------------------------------------------");
        LOG.log(Level.FINEST, "getName()           : " + mm.getName());
        LOG.log(Level.FINEST, "toString()          : " + mm.toString());
        LOG.log(Level.FINEST, "getDescriptor()     : " + mm.getDescriptor());
        LOG.log(Level.FINEST, "getSignature()      : " + mm.getSignature());
        // LOG.log(Level.FINEST, "getParamTypes()     : " + mm.getParameterTypes());
        LOG.log(Level.FINEST, "getDeclaringClass() : " + mm.getDeclaringClass());
    }

//    private boolean startsWith(String theString, String prefix) {
//        if (prefix.length() == 0) {
//            return true;
//        }
//
//        return caseSensitive ? theString.startsWith(prefix)
//            : theString.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase());
//    }

    /**
     * Get the closest ASTNode related to this request. This is used to complete
     * Methods etc later on.
     * @param request
     * @return a valid ASTNode or null
     */
    ASTNode getClosestNode(CompletionRequest request) {

        AstPath path = getPathFromRequest(request);

        if (path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        ASTNode closest;

        if (request.prefix.equals("")) {
            closest = path.leaf();
        } else {
            closest = path.leafParent();
        }

        LOG.log(Level.FINEST, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LOG.log(Level.FINEST, "(leaf): ");
        printASTNodeInformation(closest);
        LOG.log(Level.FINEST, "(parentLeaf): ");
        printASTNodeInformation(path.leafParent());

        // we gotta make sure not to catch the parameterts as closest node
        if (closest instanceof ConstantExpression &&
            path.leafParent() instanceof MethodNode) {
            return path.leafParent();
        }

        return closest;
    }

    /**
     * returns the next enclosing MethodNode for the given request
     * @param request completion request which includes position information
     * @return the next surrouning MethodNode
     */
       private MethodNode getSurroundingMethodNode (CompletionRequest request) {
           AstPath path = getPathFromRequest(request);

           if (path == null) {
               LOG.log(Level.FINEST, "path == null"); // NOI18N
               return null;
           }
           
           for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
                if(current instanceof MethodNode){
                    MethodNode mn = (MethodNode)current;
                    LOG.log(Level.FINEST, "Found Method: {0}", mn.getName()); // NOI18N
                    return mn;
                }
            }
           return null;
       }
       
    /**
     * returns the next enclosing ClassNode for the given request
     * @param request completion request which includes position information
     * @return the next surrouning ClassNode
     */
       private ClassNode getSurroundingClassdNode (CompletionRequest request) {
           AstPath path = getPathFromRequest(request);

           if (path == null) {
               LOG.log(Level.FINEST, "path == null"); // NOI18N
               return null;
           }
           
           for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
                if(current instanceof ClassNode){
                    ClassNode classNode = (ClassNode)current;
                    LOG.log(Level.FINEST, "Found surrounding Class: {0}", classNode.getName()); // NOI18N
                    return classNode;
                }
            }
           return null;
       }
    
    
    
    /**
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     * 
     * @param request
     * @return a freshly created AstPath object for the offset given in the request
     */
    private AstPath getPathFromRequest(CompletionRequest request) {
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(request.info);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317

        if (root == null) {
            LOG.log(Level.FINEST, "root == null"); // NOI18N
            LOG.log(Level.FINEST, "request.info   = {0}", request.info); // NOI18N
            LOG.log(Level.FINEST, "request.prefix = {0}", request.prefix); // NOI18N
            
            return null;
        }

        return new AstPath(root, request.astOffset, request.doc);
    }

    /**
     * Complete Groovy or Java Keywords.
     * 
     * @see GroovyUtils.GROOVY_KEYWORDS or GroovyUtils.JAVA_KEYWORDS
     * @param proposals
     * @param request
     * @return
     */
    private boolean completeKeywords(List<CompletionProposal> proposals, CompletionRequest request) {
        String prefix = request.prefix;

        for (String keyword : GroovyUtils.GROOVY_KEYWORDS) {
            if (keyword.startsWith(prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request, true);
                item.setSymbol(true);
                proposals.add(item);
            }
        }
        
        for (String keyword : GroovyUtils.JAVA_KEYWORDS) {
            if (keyword.startsWith(prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request, false);
                item.setSymbol(true);
                proposals.add(item);
            }
        }
        
        return true;
    }
    
    private boolean completeFields(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeFields"); // NOI18N

        ClassNode surroundingClass = getSurroundingClassdNode(request);

        if (surroundingClass == null) {
            LOG.log(Level.FINEST, "No surrounding class found, bail out ..."); // NOI18N
            return false;
        }

        LOG.log(Level.FINEST, "Surrounding class is : {0}", surroundingClass); // NOI18N

        List<FieldNode> fields = surroundingClass.getFields();

        for (FieldNode field : fields) {
            LOG.log(Level.FINEST, "Field found: {0}", field.getName()); // NOI18N
            // TODO: I take the freedom to filter this: __timeStamp*
            if(field.getName().startsWith("__timeStamp")) { // NOI18N
                continue;
            }
            
            if (request.prefix.length() < 1) {
                proposals.add(new FieldItem(field.getName(), anchor, request, javax.lang.model.element.ElementKind.FIELD, field.getType()));
            } else {
                String fieldName = field.getName();
                if (fieldName.compareTo(request.prefix) != 0 && fieldName.startsWith(request.prefix)) {
                    proposals.add(new FieldItem(field.getName(), anchor, request, javax.lang.model.element.ElementKind.FIELD, field.getType()));
                }
            }
        }
        return false;
    }

    private boolean completeLocalVars(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completeLocalVars"); // NOI18N

        MethodNode scope = getSurroundingMethodNode(request);

        if(scope == null){
            LOG.log(Level.FINEST, "scope == null"); // NOI18N
            return false;
        }

        List<ASTNode> result = new ArrayList<ASTNode>();
        getLocalVars(scope, result);
        
        if(!result.isEmpty()){
            for (ASTNode node : result) {
                String varName = ((Variable)node).getName();
                LOG.log(Level.FINEST, "Node found: {0}", varName); // NOI18N
                
                if(request.prefix.length() < 1) {
                    proposals.add(new LocalVarItem((Variable )node, anchor, request));
                } else {
                    if(varName.compareTo(request.prefix) != 0 && varName.startsWith(request.prefix)){
                        proposals.add(new LocalVarItem((Variable )node, anchor, request));
                    }
                }
                
            }
        }
        
        return true;
    }
    
    private void getLocalVars(ASTNode node, List<ASTNode> result) {
        if (node instanceof Variable) {
            result.add(node);
        }

        List<ASTNode> list = AstUtilities.children(node);
        for (ASTNode child : list) {
            getLocalVars(child, result);
        }
    }

    
    boolean checkForRequestBehindImportStatement(final CompletionRequest request) {

        ASTNode closest = getClosestNode(request);

        if (closest != null && closest instanceof ModuleNode) {
            int rowStart = 0;
            int nonWhite = 0;
            
            try {
                rowStart = org.netbeans.editor.Utilities.getRowStart(request.doc, request.lexOffset);
                nonWhite = org.netbeans.editor.Utilities.getFirstNonWhiteFwd(request.doc, rowStart);

            } catch (BadLocationException ex) {
                LOG.log(Level.FINEST, "Trouble doing getRowStart() or getFirstNonWhiteFwd(): {0}", ex.getMessage());
            }

            Token<? extends GroovyTokenId> importToken = LexUtilities.getToken(request.doc, nonWhite);

            if (importToken != null && importToken.id() == GroovyTokenId.LITERAL_import) {
                LOG.log(Level.FINEST, "Right behind an import statement");
                return true;
            }
        }
        return false;
    }
    
    PackageCompletionRequest getPackageRequest (final CompletionRequest request) {
        int position = request.lexOffset;
        PackageCompletionRequest result = new PackageCompletionRequest();
        
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, position);
        ts.move(position);
        
        // travel back on the token string till the token is neither a
        // DOT nor an IDENTIFIER
        
        while (ts.isValid() && ts.movePrevious() && ts.offset() >= 0) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            // LOG.log(Level.FINEST, "LexerToken(back): {0}", t.text().toString());
            if (!(t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER)) {
                break;
            }
        }
        
        // now we are travelling in the opposite direction to construct
        // the result
        
        
        StringBuffer buf = new StringBuffer();
        Token<? extends GroovyTokenId> lastToken = null;

        while (ts.isValid() && ts.moveNext() && ts.offset() < position) {
            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
            
            // LOG.log(Level.FINEST, "LexerToken(fwd): {0}", t.text().toString());
            if (t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER) {
                buf.append(t.text().toString());
                lastToken = t;
            } else {
                break;
            }
        }

        // construct the return value. These are the combinations:
        // string           basePackage prefix
        // ""               ""          ""
        // "java"           ""          "java"
        // "java."          "java"      ""
        // "java.lan"       "java"      "lan"
        // "java.lang"      "java"      "lang"
        // "java.lang."     "java.lang" ""
        
        result.fullString = buf.toString();
        
        if(buf.length() == 0){
            result.basePackage = "";
            result.prefix = "";
        } else if (lastToken != null && lastToken.id() == GroovyTokenId.DOT) {
            String pkgString = buf.toString();
            result.basePackage = pkgString.substring(0, pkgString.length() - 1);
            result.prefix = "";
        } else if (lastToken != null && lastToken.id() == GroovyTokenId.IDENTIFIER) {
            String pkgString = buf.toString();
            result.prefix = lastToken.text().toString();
            
            result.basePackage = pkgString.substring(0, pkgString.length() - result.prefix.length());
            
            if(result.basePackage.endsWith(".")){
                result.basePackage = result.basePackage.substring(0, result.basePackage.length() - 1);
            }
        }
        
        LOG.log(Level.FINEST, "-- fullString : >{0}<", result.fullString);
        LOG.log(Level.FINEST, "-- basePackage: >{0}<", result.basePackage);
        LOG.log(Level.FINEST, "-- prefix:      >{0}<", result.prefix);
        
        return result;
    }
    
    
    class PackageCompletionRequest {
        String fullString;
        String basePackage;
        String prefix;
    }
    
    
    private ClasspathInfo getClassPathFromDocument(BaseDocument doc){
        
        DataObject dob = NbEditorUtilities.getDataObject(doc);

        if (dob == null) {
            LOG.log(Level.FINEST, "Problem getting DataObject");
            return null;
        }

        FileObject fo = dob.getPrimaryFile();

        if (fo == null) {
            LOG.log(Level.FINEST, "Problem getting FileObject");
            return null;
        }

        ClasspathInfo pathInfo = NbUtilities.getClasspathInfoForFileObject(fo);

        if (pathInfo == null) {
            LOG.log(Level.FINEST, "Problem getting ClasspathInfo");
        }
        
        return pathInfo;
        
    }
    
    /**
     * Here we complete package-names like java.lan to java.lang ...
     * 
     * @param proposals the CompletionPropasal we should populate
     * @param request wrapper object for this specific request ( position etc.)
     * @return true if we found something suitable
     */
    private boolean completePackages(final List<CompletionProposal> proposals, final CompletionRequest request) {
        LOG.log(Level.FINEST, "-> completePackages"); // NOI18N
        
    
        PackageCompletionRequest packageRequest = getPackageRequest(request);
     
        LOG.log(Level.FINEST, "Token fullString = >{0}<", packageRequest.fullString);
        
        ClasspathInfo pathInfo = getClassPathFromDocument(request.doc);

        // try to find suitable packages ...

        Set<String> pkgSet;

        pkgSet = pathInfo.getClassIndex().getPackageNames(packageRequest.fullString, true, EnumSet.allOf(ClassIndex.SearchScope.class));

        for (String singlePackage : pkgSet) {
            LOG.log(Level.FINEST, "PKG set item: {0}", singlePackage);

            if (packageRequest.prefix.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.fullString.length());
            } else if (!packageRequest.basePackage.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.basePackage.length() + 1);
            }

            if (singlePackage.length() > 0) {
                proposals.add(new PackageItem(singlePackage, anchor, request));
            }
        }

        return false;
    }

    /**
     * 
     * @param proposals
     * @param request
     * @return
     */
   private boolean completeTypes(final List<CompletionProposal> proposals, final CompletionRequest request) {

        final PackageCompletionRequest packageRequest = getPackageRequest(request);
     
        LOG.log(Level.FINEST, "completeTypes fullstring = >{0}<", packageRequest.fullString);
        
        ClasspathInfo pathInfo = getClassPathFromDocument(request.doc);
       
        LOG.log(Level.FINEST, "Prefix = >{0}<", packageRequest.basePackage);

        JavaSource javaSource = JavaSource.create(pathInfo);

        if (javaSource != null) {
            LOG.log(Level.FINEST, "JavaSource retrieved!");

            Task<CompilationController> typeSearcher = new Task<CompilationController>() {

                public void run(CompilationController info) throws Exception {
                    Elements elements = info.getElements();

                    if (elements != null) {
                        LOG.log(Level.FINEST, "typeSearcher.run(), elements retrieved");
                        PackageElement packageElement = elements.getPackageElement(packageRequest.basePackage);

                        if (packageElement != null) {
                            List<? extends javax.lang.model.element.Element> typelist = packageElement.getEnclosedElements();

                            for (Element element : typelist) {
                                LOG.log(Level.FINEST, "Found enclosed:  {0}", element);
                                if(packageRequest.prefix.equals("")){
                                    String typeName = element.toString().substring(packageRequest.fullString.length());
                                    proposals.add(new TypeItem(typeName, anchor, request, element.getKind()));
                                } else{
                                    String typeName = NbUtilities.stripPackage(element.toString());
                                    if (typeName.startsWith(packageRequest.prefix)){
                                        proposals.add(new TypeItem(typeName, anchor, request, element.getKind()));
                                    }
                                }
                            }
                        }
                    }
                }
            };

            try {
                javaSource.runUserActionTask(typeSearcher, true);
            } catch (IOException ex) {
                LOG.log(Level.FINEST, "Problem in runUserActionTask :  {0}", ex.getMessage());
                return false;
            }
        }
       
        return true;
   } 
    
    boolean isPackageAlreadyProposed(Set<String> pkgSet, String prefix) {
        for (String singlePackage : pkgSet) {
            if (prefix.startsWith(singlePackage)) {
                return true;
            }
        }
        return false;
    }

    private ClassNode getDeclaringClass(ASTNode closest) {
        ClassNode declClass;

        if (closest != null && closest instanceof AnnotatedNode) {
            LOG.log(Level.FINEST, "closest: AnnotatedNode"); // NOI18N

            // if this AnnotetedNode happens to be a ClassNode then 
            // just cast it. Otherwise try to find declaring class

            if (closest instanceof ClassNode) {
                declClass = (ClassNode) closest;

            } else {
                declClass = ((AnnotatedNode) closest).getDeclaringClass();
            }
        } else if (closest != null && closest instanceof Expression) {
            LOG.log(Level.FINEST, "closest: Expression"); // NOI18N
            declClass = ((Expression) closest).getType();
        } else if (closest != null && closest instanceof ExpressionStatement) {
            LOG.log(Level.FINEST, "closest: ExpressionStatement"); // NOI18N
            Expression expr = ((ExpressionStatement) closest).getExpression();
            if (expr instanceof PropertyExpression) {
                declClass = ((PropertyExpression) expr).getObjectExpression().getType();
            } else {
                return null;
            }
        } else {
            LOG.log(Level.FINEST, "Found nothing to work on"); // NOI18N
            return null;
        }

        return declClass;
    }

    /**
     * Complete the methods invocable on a class.
     * @param proposals the CompletionProposal List we populate (return value)
     * @param request location information used as input
     * @return true if we found something usable
     */
    private boolean completeMethods(List<CompletionProposal> proposals, CompletionRequest request) {

        LOG.log(Level.FINEST, "-> completeMethods"); // NOI18N

        ASTNode closest = getClosestNode(request);

        ClassNode declClass = getDeclaringClass(closest);

        if (declClass == null) {
            LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
            return false;
        }

        Class clz;
        
        try {
            clz = Class.forName(declClass.getName());
        } catch (ClassNotFoundException e) {
            LOG.log(Level.FINEST, "Class.forName() failed: {0}", e.getMessage()); // NOI18N
            return false;
        }

        if (clz != null) {
            MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (Object method : metaClz.getMetaMethods()) {
                    populateProposal(clz, method, request, proposals, true);
                }

                for (Object method : metaClz.getMethods()) {
                    populateProposal(clz, method, request, proposals, false);
                }
            }
        }

        return true;
    }

    public CodeCompletionResult complete(CodeCompletionContext context) {
        CompilationInfo info = context.getInfo();
        int lexOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        HtmlFormatter formatter = context.getFormatter();

        final int astOffset = AstUtilities.getAstOffset(info, lexOffset);

        LOG.log(Level.FINEST, "complete(...), prefix: {0}", prefix); // NOI18N


        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        anchor = lexOffset - prefix.length();

        final Document document = info.getDocument();
        if (document == null) {
            return CodeCompletionResult.NONE;
        }

        // TODO - move to LexUtilities now that this applies to the lexing offset?
//        lexOffset = AstUtilities.boundCaretOffset(info, lexOffset);

        // Discover whether we're in a require statement, and if so, use special completion
        // final TokenHierarchy<Document> th = TokenHierarchy.get(document);
        final BaseDocument doc = (BaseDocument) document;
        // final FileObject fileObject = info.getFileObject();

        doc.readLock(); // Read-lock due to Token hierarchy use

        try {
            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.formatter = formatter;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.doc = doc;
            request.info = info;
            request.prefix = prefix;
            
            // Complete potential import statements if we're invoced from a suitable
            // position (outside method or class, right behind an import statement)
            
            // complete packages
            completePackages(proposals, request);
            
            // complete classes, interfaces and enums
            completeTypes(proposals, request);
            
            if (!checkForRequestBehindImportStatement(request)) {
                // complette keywords
                completeKeywords(proposals, request);

                // complete methods
                completeMethods(proposals, request);


                // complete fields
                completeFields(proposals, request);

                // complete local variables
                completeLocalVars(proposals, request);
            }

            return new DefaultCompletionResult(proposals, false);
        } finally {
            doc.readUnlock();
        }
    //return proposals;
    }

    /**
     * create the signature-string of this method usable as a 
     * Javadoc URL suffix (behind the # ) 
     *    
     * This was needed, since from groovy 1.5.4 to 
     * 1.5.5 the MetaMethod.getSignature() changed from
     * human-readable to Class.getName() output.
     * 
     * To make matters worse, we have some subtle 
     * differences between JDK and GDK MetaMethods
     * 
     * method.getSignature for the JDK gives the return-
     * value right behind the method and encodes like Class.getName():
     *   
     * codePointCount(II)I
     *    
     * GDK-methods look like this:
     * java.lang.String center(java.lang.Number, java.lang.String)
     * 
     * TODO: if groovy folks ever change this (again), we're falling
     * flat on our face.
     * 
     */
    String getMethodSignature(MetaMethod method, boolean forURL, boolean isGDK) {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK) {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1) {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL) {
                methodSignature = methodSignature.replaceAll(", ", ",%20");
            }

            return methodSignature;

        } else {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) {
                return "";
            }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName() 
     */
    String decodeTypes(final String encodedType, boolean forURL) {


        String DELIMITER = ",";

        if (forURL) {
            DELIMITER = DELIMITER + "%20";
        } else {
            DELIMITER = DELIMITER + " ";
        }

        StringBuffer sb = new StringBuffer("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++) {
            char c = encodedType.charAt(i);

            if (c == '[') {
                nextIsAnArray = true;
                continue;
            } else if (c == 'Z') {
                sb.append("boolean");
            } else if (c == 'B') {
                sb.append("byte");
            } else if (c == 'C') {
                sb.append("char");
            } else if (c == 'D') {
                sb.append("double");
            } else if (c == 'F') {
                sb.append("float");
            } else if (c == 'I') {
                sb.append("int");
            } else if (c == 'J') {
                sb.append("long");
            } else if (c == 'S') {
                sb.append("short");
            } else if (c == 'L') { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                if (forURL) {
                    sb.append(typeName);
                } else {
                    sb.append(NbUtilities.stripPackage(typeName));
                }
                
                i = semicolon;
            }

            if (nextIsAnArray) {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1) {
                sb.append(DELIMITER);
            }

        }

        return sb.toString();
    }

    public String document(CompilationInfo info, ElementHandle element) {
        LOG.log(Level.FINEST, "document(), ElementHandle : {0}", element);

        String ERROR = "<h2>" + NbBundle.getMessage(CodeCompleter.class, "CodeCompleter_NoJavaDocFound") + "</h2>";
        String doctext = null;

        if (element instanceof AstMethodElement) {
            AstMethodElement ame = (AstMethodElement) element;

            String base = "";

            if (jdkJavaDocBase != null && ame.isGDK() == false) {
                base = jdkJavaDocBase;
            } else if (groovyJavaDocBase != null && ame.isGDK() == true) {
                base = groovyJavaDocBase;
            } else {
                LOG.log(Level.FINEST, "Neither JDK nor GDK or error locating: {0}", ame.isGDK());
                return ERROR;
            }

            MetaMethod mm = ame.getMethod();

            // enable this to troubleshoot subtle differences in JDK/GDK signatures
            printMethod(mm);

            // figure out who originally defined this method

            String className;

            if (ame.isGDK()) {
                className = mm.getDeclaringClass().getCachedClass().getName();
            } else {

                String declName = null;

                if (mm != null) {
                    CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        Class clz = cc.getCachedClass();
                        if (clz != null) {
                            declName = clz.getName();
                        }
                    }
                }

                if (declName != null) {
                    className = declName;
                } else {
                    className = ame.getClz().getName();
                }
            }

            // create path from fq java package name:
            // java.lang.String -> java/lang/String.html
            String classNamePath = className.replace(".", "/");
            classNamePath = classNamePath + ".html"; // NOI18N

            // if the file can be located in the GAPI folder prefer it
            // over the JDK
            if (!ame.isGDK()) {

                URL url;
                File testFile;

                try {
                    url = new URL(gapiDocBase + classNamePath);
                    testFile = new File(url.toURI());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINEST, "MalformedURLException: {0}", ex);
                    return ERROR;
                } catch (URISyntaxException uriEx) {
                    LOG.log(Level.FINEST, "URISyntaxException: {0}", uriEx);
                    return ERROR;
                }

                if (testFile != null && testFile.exists()) {
                    base = gapiDocBase;
                }
            }

            // create the signature-string of the method
            String sig = getMethodSignature(ame.getMethod(), true, ame.isGDK());
            String printSig = getMethodSignature(ame.getMethod(), false, ame.isGDK());

            String urlName = base + classNamePath + "#" + sig;

            try {
                LOG.log(Level.FINEST, "Trying to load URL = {0}", urlName); // NOI18N
                doctext = HTMLJavadocParser.getJavadocText(
                    new URL(urlName),
                    false,
                    ame.isGDK());
            } catch (MalformedURLException ex) {
                LOG.log(Level.FINEST, "document(), URL trouble: {0}", ex); // NOI18N
                return ERROR;
            }

            // If we could not find a suitable JavaDoc for the method - say so. 
            if (doctext == null) {
                return ERROR;
            }

            doctext = "<h3>" + className + "." + printSig + "</h3><BR>" + doctext;
        }
        return doctext;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '.') {
            return QueryType.COMPLETION;
        }

        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return "";
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class CompletionRequest {

        // private TokenHierarchy<Document> th;
        private CompilationInfo info;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix = "";
        // private NameKind kind;
        // private QueryType queryType;
        // private FileObject fileObject;
        private HtmlFormatter formatter;
    }

    private abstract class GroovyCompletionItem implements CompletionProposal {

        protected CompletionRequest request;
        protected GroovyElement element;
        protected int anchorOffset;
        protected boolean symbol;
        protected boolean smart;

        private GroovyCompletionItem(GroovyElement element, int anchorOffset, CompletionRequest request) {
            this.element = element;
            this.anchorOffset = anchorOffset;
            this.request = request;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }

        public String getName() {
            return element.getName();
        }

        public void setSymbol(boolean symbol) {
            this.symbol = symbol;
        }

        public String getInsertPrefix() {
            if (symbol) {
                return "." + getName();
            } else {
                return getName();
            }
        }

        public String getSortText() {
            return getName();
        }

        public ElementHandle getElement() {
            LOG.log(Level.FINEST, "getElement() request.info : {0}", request.info);
            LOG.log(Level.FINEST, "getElement() element : {0}", element);

            return null;
        }

        public ElementKind getKind() {
            return element.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);

            return formatter.getText();
        }

        public String getRhsHtml() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        void setSmart(boolean smart) {
            this.smart = smart;
        }

        public boolean isSmart() {
            return smart;
        }

        public List<String> getInsertParams() {
            return null;
        }

        public String[] getParamListDelimiters() {
            return new String[]{"(", ")"}; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }
    }

    private class MethodItem extends GroovyCompletionItem {

        private static final String GROOVY_METHOD = "org/netbeans/modules/groovy/editor/resources/groovydoc.png"; //NOI18N
        MetaMethod method;
        HtmlFormatter formatter;
        boolean isGDK;
        AstMethodElement methodElement;

        MethodItem(Class clz, MetaMethod method, int anchorOffset, CompletionRequest request, boolean isGDK) {
            super(null, anchorOffset, request);
            this.method = method;
            this.formatter = request.formatter;
            this.isGDK = isGDK;

            // This is an artificial, new ElementHandle which has no real
            // equivalent in the AST. It's used to match the one passed to super.document()
            methodElement = new AstMethodElement(new ASTNode(), clz, method, isGDK);
        }

        @Override
        public String getName() {
            return method.getName() + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml() {

            ElementKind kind = getKind();
            boolean emphasize = false;

            formatter.reset();
            if (method.isStatic()) {
                emphasize = true;
                formatter.emphasis(true);
            }
            formatter.name(kind, true);

            if (isGDK) {
                formatter.appendText(method.getName());

                // construct signature by removing package names.

                String signature = method.getSignature();
                int start = signature.indexOf("(");
                int end = signature.indexOf(")");

                String sig = signature.substring(start + 1, end);

                StringBuffer buf = new StringBuffer();

                for (String param : sig.split(",")) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(NbUtilities.stripPackage(param));
                }

                String simpleSig = buf.toString();
                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText(getMethodSignature(method, false, isGDK));
            }


            formatter.name(kind, false);

            if (emphasize) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public String getRhsHtml() {
            formatter.reset();

            // no FQN return types but only the classname, please:

            String retType = method.getReturnType().toString();
            retType = NbUtilities.stripPackage(retType);

            formatter.appendHtml(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {

            if (!isGDK) {
                return null;
            }

            if (groovyIcon == null) {
                groovyIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_METHOD));
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {

            // to display the documentation box for each element, the completion-
            // element needs to implement this method. Otherwise document(...)
            // won't even be called at all.

            return methodElement;
        }
    }

    private class KeywordItem extends GroovyCompletionItem {

        private static final String GROOVY_KEYWORD = "org/netbeans/modules/groovy/editor/resources/groovydoc.png"; //NOI18N
        private static final String JAVA_KEYWORD   = "org/netbeans/modules/groovy/editor/resources/duke.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final boolean isGroovy;

        KeywordItem(String keyword, String description, int anchorOffset, CompletionRequest request, boolean isGroovy) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
            this.description = description;
            this.isGroovy = isGroovy;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            
            if (isGroovy) {
                if (groovyIcon == null) {
                    groovyIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_KEYWORD));
                }
                return groovyIcon;
            } else {
                if (javaIcon == null) {
                    javaIcon = new ImageIcon(org.openide.util.Utilities.loadImage(JAVA_KEYWORD));
                }
                return javaIcon;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(keyword));
        }
    }

    /**
     * 
     */
    private class PackageItem extends GroovyCompletionItem {

        private final String keyword;

        PackageItem(String keyword, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.PACKAGE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(keyword));
        }
    }

    /**
     * 
     */
    private class TypeItem extends GroovyCompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;

        TypeItem(String name, int anchorOffset, CompletionRequest request, javax.lang.model.element.ElementKind ek) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(name));
        }
    }

    /**
     * 
     */
    private class FieldItem extends GroovyCompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;
        private final ClassNode type;

        FieldItem(String name, int anchorOffset, CompletionRequest request, javax.lang.model.element.ElementKind ek, ClassNode type) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml() {
            return type.getNameWithoutPackage();
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(request.info, new KeywordElement(name));
        }
    }

    /**
     * 
     */
    private class LocalVarItem extends GroovyCompletionItem {

        private final Variable var;

        LocalVarItem(Variable var, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.var = var;
        }

        @Override
        public String getName() {
            return var.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml() {
            return var.getType().getNameWithoutPackage();
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }
    }
}
