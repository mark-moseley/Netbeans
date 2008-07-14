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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class JsAnalyzer implements StructureScanner {
    public static final String NETBEANS_IMPORT_FILE = "__netbeans_import__"; // NOI18N
    private static final String DOT_CALL = ".call"; // NOI18N
    
    public List<? extends StructureItem> scan(CompilationInfo info, HtmlFormatter formatter) {
        JsParseResult result = AstUtilities.getParseResult(info);
        AnalysisResult ar = result.getStructure();

        List<?extends AstElement> elements = ar.getElements();
        List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        Map<String,List<AstElement>> classes = new HashMap<String,List<AstElement>>();
        List<AstElement> outside = new ArrayList<AstElement>();
        List<String> classNames = new ArrayList<String>(); // Preserves source order for the map
        for (AstElement e : elements) {
            String in = e.getIn();
            if (e.getKind() == ElementKind.CLASS) {
                if (in != null && in.length() > 0) {
                    in = in + "." + e.getName();
                } else {
                    in = e.getName();
                }
            } else if (e.getKind() == ElementKind.CONSTRUCTOR && Character.isUpperCase(e.getName().charAt(0))) {
                if (e.getIn() != null && e.getIn().length() > 0) {
                    in = e.getIn() + "." + e.getName();
                } else {
                    in = e.getName();
                }
            }

            if (in != null && in.length() > 0) {
                List<AstElement> list = classes.get(in);
                if (list == null) {
                    list = new ArrayList<AstElement>();
                    classes.put(in, list);
                    classNames.add(in);
                }
                list.add(e);
            } else {
                outside.add(e);
            }
        }
        
        for (AstElement e : outside) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                String in = e.getName();
                List<AstElement> list = classes.get(in);
                if (list == null) {
                    list = new ArrayList<AstElement>();
                    classes.put(in, list);
                    classNames.add(in);
                }
                list.add(e);
            } else {
                JsAnalyzer.JsStructureItem item = new JsStructureItem(e, info, formatter);
                itemList.add(item);
            }
        }
        
        for (String clz : classNames) {
            List<AstElement> list = classes.get(clz);
            assert list != null;

            AstElement first = list.get(0);
            JsFakeStructureItem currentClass = new JsFakeStructureItem(clz, ElementKind.CLASS,
                    first, info, formatter);
            itemList.add(currentClass);
            
            int firstAstOffset = first.getNode().getSourceStart();
            int lastAstOffset = first.getNode().getSourceEnd();
            
            for (AstElement e : list) {
                if (e.getKind() != ElementKind.CLASS) {
                    JsAnalyzer.JsStructureItem item = new JsStructureItem(e, info, formatter);
                    currentClass.addChild(item);
                } else {
                    currentClass.element = e;
                }
                Node n = e.getNode();
                if (n.getSourceStart() < firstAstOffset) {
                    firstAstOffset = n.getSourceStart();
                }
                if (n.getSourceEnd() > lastAstOffset) {
                    lastAstOffset = n.getSourceEnd();
                }
            }
            
            currentClass.begin = LexUtilities.getLexerOffset(info, firstAstOffset);
            currentClass.end = LexUtilities.getLexerOffset(info, lastAstOffset);
        }
        
        return itemList;
    }

    public Map<String, List<OffsetRange>> folds(CompilationInfo info) {
        JsParseResult result = AstUtilities.getParseResult(info);
        TranslatedSource source = result.getTranslatedSource();
        AnalysisResult ar = result.getStructure();

        List<?extends AstElement> elements = ar.getElements();
        //List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        Map<String,List<OffsetRange>> folds = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> codeblocks = new ArrayList<OffsetRange>();
        folds.put("codeblocks", codeblocks); // NOI18N

        try {
            BaseDocument doc = (BaseDocument)info.getDocument();
            if (doc == null) {
                return Collections.emptyMap();
            }

            for (AstElement element : elements) {
                ElementKind kind = element.getKind();
                switch (kind) {
                case METHOD:
                case CONSTRUCTOR:
                case CLASS:
                case MODULE:
                    Node node = element.getNode();
                    OffsetRange range = AstUtilities.getRange(node);
                    
                    if (source != null) {
                        int lexStart = source.getLexicalOffset(range.getStart());
                        int lexEnd = source.getLexicalOffset(range.getEnd());
                        if (lexStart < lexEnd) {
                            //recalculate the range if we parsed the virtual source
                            range = new OffsetRange(lexStart,lexEnd);
                        }
                    }

                    if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR ||
                        // Only make nested classes/modules foldable, similar to what the java editor is doing
                        (range.getStart() > Utilities.getRowStart(doc, range.getStart()))) {

                        int start = range.getStart();
                        // Start the fold at the END of the line
                        start = org.netbeans.editor.Utilities.getRowEnd(doc, start);
                        int end = range.getEnd();
                        if (start != (-1) && end != (-1) && start < end && end <= doc.getLength()) {
                            range = new OffsetRange(start, end);
                            codeblocks.add(range);
                        }
                    }
                    break;
                }

                assert element.getChildren().size() == 0;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return folds;
    }
    
    static AnalysisResult analyze(JsParseResult result, CompilationInfo info) {
        AnalysisResult analysisResult = new AnalysisResult(info);
        BaseDocument doc = LexUtilities.getDocument(info, true);
        if (doc != null) {
            try {
                doc.readLock(); // Read-lock due to token hierarchy use

                ParseTreeWalker walker = new ParseTreeWalker(analysisResult);
                Node root = result.getRootNode();
                if (root != null) {
                    walker.walk(root);
                }
                analysisResult.postProcess(result);
            } finally {
                doc.readUnlock();
            }
        }
        
        return analysisResult;
    }

    public Configuration getConfiguration() {
        return null;
    }

    public static class AnalysisResult implements ParseTreeVisitor {
        private List<AstElement> elements = new ArrayList<AstElement>();
        private List<String> imports;
        private CompilationInfo info;
        private Map<String,String> classExtends;
        private Set<String> fields;
        private Node inConstructor;
        private Node currentFunction;
        /** Namespace map */
        private Map<String,String> classToFqn;

        private AnalysisResult(CompilationInfo info) {
            this.info = info;
        }

        void addNameSpace(String name, String namespace) {
            if (classToFqn == null) {
                classToFqn = new HashMap<String,String>();
            }
            classToFqn.put(name, namespace);
        }
        
        String getNameSpace(String name) {
            if (classToFqn != null) {
                return classToFqn.get(name);
            }
            
            return null;
        }

        String getExtends(String name) {
            if (classExtends != null) {
                return classExtends.get(name);
            }
            return null;
        }
        
        Map<String,String> getExtendsMap() {
            return classExtends;
        }
        
        void addSuperClass(String className, String superName) {
            if (classExtends == null) {
                classExtends = new HashMap<String, String>();
            }
            classExtends.put(className.toString(), superName.toString());
        }
        
        public boolean visit(Node node) {
            switch (node.getType()) {
            case Token.CALL: {
                if (node.hasChildren()) {
                    // Handle imports
                    Node child = node.getFirstChild();
                    if (child.getType() == Token.NAME) {
                        String s = child.getString();
                        if (s.equals(NETBEANS_IMPORT_FILE)) {
                            processImports(child.getNext());
                        }
                    } else if (child.getType() == Token.GETPROP) {
                        // Handle YAHOO.extend(YAHOO.widget.Calendar2up, YAHOO.widget.CalendarGroup);
                        // This is CALL, GETPROP, NAME=YAHOO+STRING=extend
                        Node nameNode = child.getFirstChild();
                        if (nameNode != null && nameNode.getType() == Token.NAME &&
                                "YAHOO".equals(nameNode.getString())) { // NOI18N
                            Node stringNode = nameNode.getNext();
                            if (stringNode != null && stringNode.getType() == Token.STRING &&
                                    "extend".equals(stringNode.getString())) { // NOI18N
                                Node first = child.getNext();
                                if (first != null) {
                                    Node second = first.getNext();
                                    StringBuilder className = new StringBuilder();
                                    StringBuilder superName = new StringBuilder();
                                    if (AstUtilities.addName(className, first) && AstUtilities.addName(superName, second)) {
                                        addSuperClass(className.toString(), superName.toString());
                                    }
                                }
                            }
                        }
                    }
                }
                
                break;
            }
            case Token.OBJECTLIT: {
                // Foo.Bar = { foo : function() }
                String[] classes = AstUtilities.getObjectLitFqn(node);
                String className = classes[0];
                String superName = classes[1];
                if (className != null) {
                    if (className.endsWith(AstUtilities.DOT_PROTOTYPE)) {
                        className = className.substring(0, className.length()-AstUtilities.DOT_PROTOTYPE.length());

                        // Make a constructor function for this type of object
                        String name = className;
                        String in = null;
                        int dot = className.lastIndexOf('.');
                        if (dot != -1) {
                            in = className.substring(0, dot);
                            name = className.substring(dot+1);
                        }
                        AstElement js = AstElement.createElement(info, node, name, in, this);
                        if (js != null) {
                            checkDocumentation(js);
                            js.setKind(ElementKind.CONSTRUCTOR);
                            elements.add(js);
                        }
                    }

                    if (superName != null) {
                        addSuperClass(className, superName.toString());
                    }

                    // TODO - only do this for capitalized names??
                    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                        if (child.getType() == Token.OBJLITNAME) {
                            Node f = AstUtilities.getLabelledNode(child);
                            if (f != null) {
                                if (f.getType() == Token.FUNCTION && ((FunctionNode)f).getFunctionName().length() > 0) {
                                    // This is a function whose name we already know
                                    // Unusual syntax but yuiloader.html for example has it:
                                    //    var YAHOO_config = {
                                    //        listener: function g_mycallback(info) {
                                    //            g_modules.push(info.name);
                                    //        }
                                    //    };
                                    // Here the function is named both listener: and g_mycallback.
                                    break;
                                }
                                String funcName = child.getString();
                                AstElement js = AstElement.createElement(info, f, funcName, className, this);
                                if (js != null) {
                                    checkDocumentation(js);
                                    if (f.getType() != Token.FUNCTION) {
                                        js.setKind(ElementKind.PROPERTY);
                                        // Try to initialize the type, if possible
                                        String type = AstUtilities.getExpressionType(f);
                                        if (type != null) {
                                            js.setType(type);
                                        }
                                    } else if (js.getKind() == ElementKind.CONSTRUCTOR) {
                                        inConstructor = f;
                                        String in = js.getIn();
                                        if (in != null && in.length() > 0) {
                                            js.setType(in);
                                        } else {
                                            js.setType(js.getName());
                                        }
                                    }
                                    elements.add(js);
                                }
                            }
                        }
                    }
                    
                    // TODO - now that I've processed the OBJLIT - can I just
                    // skip processing its middle? But what about the constructor?
                }
                
                break;
            }
            
            case Token.FUNCTION: {
                FunctionNode func = (FunctionNode) node;
                currentFunction = func;
                boolean[] isInstanceHolder = new boolean[1];
                String name = AstUtilities.getFunctionFqn(node, isInstanceHolder);
                    
                if (name != null && name.length() > 0) {
                    String in = "";
                    boolean isInstance = isInstanceHolder[0];
                    String fqn = name;
                    int lastDotIndex = name.lastIndexOf('.');
                    if (lastDotIndex != -1) {
                        in = name.substring(0, lastDotIndex);

                        // Don't look at things like "window.onload=function(){}" -- we only
                        // care about "Window.onload=function(){}" etc
                        //if (!Character.isUpperCase(in.charAt(0))) {
                        //    break;
                        //}
                        //... that didn't work well... Dojo is defined in that way (dojo.foo=function())
                        //Just hack this for now
                        if (in.equals("window") || in.equals("document")) { // NOI18N
                            break;
                        } else if (!Character.isUpperCase(in.charAt(0))) {
                            isInstance = true;
                        }
                        
                        name = name.substring(lastDotIndex+1);
                        if (in.endsWith(AstUtilities.DOT_PROTOTYPE)) {
                            in = in.substring(0, in.length()-AstUtilities.DOT_PROTOTYPE.length()); // NOI18N
                        }
                    }
                    
                    AstElement js = AstElement.createElement(info, func, name, in, this);
                    if (js != null) {
                        checkDocumentation(js);
                        if (!isInstance) {
                            js.markStatic();
                        }
                        elements.add(js);
                    }
                    
                    if (Character.isUpperCase(name.charAt(0))) {
                        js.setType(fqn);
                        inConstructor = func;
                    }
                } else {
                    // Some other dynamic function, like this:
                    //   this.timer = setInterval(function() { self.drawEffect(); }, this.interval);
                    // Skip these
                }

                break;
            }
            
            case Token.THIS: {
                // Handle (1) inheritance, (2) properties
                // (1) inheritance:
                //   From the constructor function Spry.Data.XMLDataSet, we have
                //    Spry.Data.DataSet.call(this);
                //   This means that Spry.Data.XMLDataSet "extends" Spry.Data.DataSet.
                // (2) instance properties:
                //    this.name = whatever();
                //   Here we have a property named "name" on this class.
                Node parentNode = node.getParentNode();
                if (inConstructor != null && parentNode.getType() == Token.SETPROP) {
                    Node setProp = parentNode;
                    if (setProp.getParentNode() != null && 
                            setProp.getParentNode().getType() == Token.EXPR_VOID) {
                        // Only bother with this in constructors...
                        Node func = parentNode;
                        for (; func != null; func = func.getParentNode()) {
                            if (func.getType() == Token.FUNCTION) {
                                break;
                            }
                        }
                        if (func != inConstructor) {
                            break;
                        }
                        
                        Node propertyNode = node.getNext();
                        if (propertyNode != null && propertyNode.getType() == Token.STRING) {
                            String property = propertyNode.getString();
                            Node rhs = propertyNode.getNext();
                            if (rhs != null && rhs.getType() == Token.FUNCTION) {
                                // Functions are handled separately - when we see a function we walk outwards
                                // and compute the name, handling "this" appropriately
                                break;
                            }
                            if (Character.isLowerCase(property.charAt(0))) {
                                //String[] method = new String[1];
                                String fqn = AstUtilities.getFqn(node, null, null);
                                //boolean validMethod = method[0] == null || Character.isUpperCase(method[0].charAt(0)) ||
                                //        method[0].startsWith("init");
                                if (fqn != null/* && validMethod*/) {
                                    String propFqn = fqn + "." + property;
                                    if (fields == null || !fields.contains(propFqn)) {
                                        int clzIndex = fqn.lastIndexOf('.')+1;
                                        if (clzIndex < fqn.length() && Character.isUpperCase(fqn.charAt(clzIndex))) {
                                            // Looks like a class
                                            String name = property;
                                            String in = fqn;

                                            if (fields == null) {
                                                fields = new HashSet<String>();
                                            }
                                            fields.add(propFqn);
                                            AstElement js = AstElement.createElement(info, propertyNode, name, in, this); 
                                            if (js != null) {
                                                checkDocumentation(js);
                                                js.setKind(ElementKind.PROPERTY);
                                                if (rhs != null) {
                                                    String type = AstUtilities.getExpressionType(rhs);
                                                    if (type != null) {
                                                        js.setType(type);
                                                    }
                                                }
                                                elements.add(js);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (inConstructor != null && parentNode.getType() == Token.CALL) {
                    // Handle inheritance:
                    //   From the constructor function Spry.Data.XMLDataSet, we have
                    //    Spry.Data.DataSet.call(this);
                    // This ends up as a
                    //   CALL
                    //     GETPROP
                    //       NAME, or GETPROP (possibly nested for Foo.Bar.Baz)
                    //       STRING=call
                    //     THIS
                    Node getProp = parentNode.getFirstChild();
                    if (getProp.getType() == Token.GETPROP) {
                        StringBuilder sb = new StringBuilder();
                        if (AstUtilities.addName(sb, getProp)) {
                            String callExp = sb.toString();
                            if (callExp.endsWith(DOT_CALL)) { // NOI18N
                                String className = AstUtilities.getFqn(parentNode, null, null);
                                if (className != null) {
                                    String superClass = callExp.substring(0, callExp.length()-DOT_CALL.length()); // NOI18N
                                    addSuperClass(className, superClass.toString());
                                }
                            }
                        }
                    }
                }
                break;
            }
            
            case Token.RETURN: {
                // See if we can figure out the return type of this function
                if (currentFunction != null) {
                    if (currentFunction.nodeType == Node.UNKNOWN_TYPE) {
                        break;
                    }
                    Node child = node.getFirstChild();
                    String type; // NOI18N
                    if (child == null) {
                        type = "void"; // NOI18N
                    } else {
                        type = AstUtilities.getExpressionType(child);
                    }
                    if (type == Node.UNKNOWN_TYPE) {
                        currentFunction.nodeType = Node.UNKNOWN_TYPE;
                    } else if (currentFunction.nodeType == null) {
                        currentFunction.nodeType = type;
                    } else if (type != null) {
                        if (currentFunction.nodeType.indexOf(type) == -1) {
                            currentFunction.nodeType = currentFunction.nodeType + "|" + type; // NOI18N
                        }
                    }
                }
            }
            }
            
            return false;
        }
        
        public boolean unvisit(Node node) {
            if (node.getType() == Token.FUNCTION) {
                if (currentFunction != null && currentFunction.nodeType == null) {
                    // Except for stubs...
                    if (info != null && !info.getFileObject().getNameExt().startsWith("stub_")) { // NOI18N
                        currentFunction.nodeType = "void"; // NOI18N
                    }
                }
                currentFunction = null;
                
                Node n = node.getParentNode();
                for (; n != null; n = n.getParentNode()) {
                    if (n.getType() == Token.FUNCTION) {
                        currentFunction = n;
                        break;
                    }
                }
            }

            return false;
        }
        
        /** 
         * Elements can have their names and namespaces updated by documentation;
         * not just on this element but by a general name space map. E.g.
         * we can have one documentation saying that "Anim" is in YAHOO.util
         * and then multiple references to just Anim; these should all map to
         * YAHOO.util.Anim.
         */
        private void checkDocumentation(AstElement element) {
            // Namespace lookup
            if (element.in != null && element.in.length() > 0) {
                String namespace = getNameSpace(element.in);
                if (namespace != null) {
                    element.in = namespace + "." + element.in;
                }
            } else if (Character.isUpperCase(element.name.charAt(0))) {
                String namespace = getNameSpace(element.name);
                if (namespace != null) {
                    element.in = namespace;
                }
            }
            
            Map<String, String> typeMap = element.getDocProps();
            if (typeMap != null) {
// I can't look at @class since for Jsdoc (such as in Woodstock) the @class is just
// a marker and it may not actually specify the class - it could just be the first
// word of the class description!                
//                String clz = typeMap.get("@class"); // NOI18N
//                if (clz != null) {
//                    int dot = clz.lastIndexOf('.');
//                    if (dot != -1) {
//                        element.in = clz.substring(0, dot);
//                        element.name = clz.substring(dot+1);
//                    } else {
//                        element.name = clz;
//                    }
                    String s = typeMap.get("@extends"); // NOI18N
                    if (s != null) {
                        addSuperClass(element.name, s);
                    }
//                }
                String namespace = typeMap.get("@namespace"); // NOI18N
                if (namespace != null) {
                    addNameSpace(element.name, namespace);
                    element.in = namespace;
                }
            }
        }
        
        private void postProcess(JsParseResult result) {
            if (result.getRootNode() != null) {
                VariableVisitor visitor = result.getVariableVisitor();
                Collection<Node> globalVars = visitor.getGlobalVars(true);
                if (globalVars.size() > 0) {
                    Set<String> globals = new HashSet<String>();
                    for (Node node : globalVars) {
                        String name = node.getString();
                        if (!globals.contains(name)) {
                            globals.add(name);
                            GlobalAstElement global = new GlobalAstElement(info, node);
                            node.element = global;
                            elements.add(global);
                        }
                    }
                }
            }
        }

        private void processImports(Node node) {
            if (imports == null) {
                imports = new ArrayList<String>();
            }
            while (node != null) {
                assert node.getType() == Token.STRING;
                String path = node.getString();
                if (path.indexOf(",") != -1) {
                    String[] paths = path.split(",");
                    for (String s : paths) {
                        if (s.startsWith("'") || s.startsWith("\"")) {
                            imports.add(s.substring(1, s.length()-1));
                        } else {
                            imports.add(s);
                        }
                    }
                } else {
                    imports.add(path);
                }
                node = node.getNext();
            }
        }
        
        public List<String> getImports() {
            if (imports == null) {
                return Collections.emptyList();
            }
            
            return imports;
        }
        
        public List<?extends AstElement> getElements() {
            return elements;
        }
    }
    
    /** Fake up classes to wrap something like
     * Spry.Effect.Animator.prototype.stop = function()
     *  This creates a fake class "Spry", containing "Effect", containing
     *  "Animator", and so on.
     */
    class JsFakeStructureItem implements StructureItem {
        private String name;
        private AstElement element;
        private ElementKind kind;
        private CompilationInfo info;
        private HtmlFormatter formatter;
        List<StructureItem> children = new ArrayList<StructureItem>();
        int begin;
        int end;

        JsFakeStructureItem(String name, ElementKind kind, AstElement node, CompilationInfo info, HtmlFormatter formatter) {
            this.name = name;
            this.kind = kind;
            this.element = node;
            this.info = info;
            this.formatter = formatter;
        }
        
        private void addChild(StructureItem child) {
            children.add(child);
        }

        public String getName() {
            return name;
        }

        public String getSortText() {
            return getName();
        }

        public String getHtml() {
            formatter.reset();
            formatter.appendText(name);

            return formatter.getText();
        }

        public ElementHandle getElementHandle() {
            return element;
        }

        public ElementKind getKind() {
            return kind;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public boolean isLeaf() {
            return children.size() == 0;
        }

        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        public long getPosition() {
            return begin;
        }

        public long getEndPosition() {
            return end;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof JsFakeStructureItem)) {
                return false;
            }

            JsFakeStructureItem d = (JsFakeStructureItem)o;

            if (kind != d.kind) {
                return false;
            }

            if (!getName().equals(d.getName())) {
                return false;
            }

            if (isLeaf() != d.isLeaf()) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }

        public ImageIcon getCustomIcon() {
            return null;
        }
    }

    private class JsStructureItem implements StructureItem {
        private AstElement element;
        private ElementKind kind;
        private CompilationInfo info;
        private HtmlFormatter formatter;
        private String name;

        private JsStructureItem(AstElement node, CompilationInfo info, HtmlFormatter formatter) {
            this.element = node;
            this.info = info;
            this.formatter = formatter;

            kind = node.getKind();
        }
        
        void setKind(ElementKind kind) {
            this.kind = kind;
        }
        
        void setName(String name) {
            this.name = name;
        }

        public String getName() {
            if (name == null) {
                name = element.getName();
            }
            
            return name;
        }
        
        public String getSortText() {
            return getName();
        }

        public String getHtml() {
            formatter.reset();
            boolean strike = element.getModifiers().contains(Modifier.DEPRECATED);
            if (strike) {
                formatter.deprecated(true);
            }

            formatter.appendText(getName());
            
            if (strike) {
                formatter.deprecated(false);
            }
            
            if (element instanceof FunctionAstElement) {
                // Append parameters
                FunctionAstElement jn = (FunctionAstElement)element;

                Collection<String> parameters = jn.getParameters();

                if ((parameters != null) && (parameters.size() > 0)) {
                    formatter.appendHtml("(");
                    formatter.parameters(true);

                    for (Iterator<String> it = parameters.iterator(); it.hasNext();) {
                        String ve = it.next();
                        // TODO - if I know types, list the type here instead. For now, just use the parameter name instead
                        formatter.appendText(ve);

                        if (it.hasNext()) {
                            formatter.appendHtml(", ");
                        }
                    }

                    formatter.parameters(false);
                    formatter.appendHtml(")");
                }
            }

            if (element.getType() != null && element.getType() != Node.UNKNOWN_TYPE) {
                formatter.appendHtml(" : ");
                formatter.appendText(JsUtils.normalizeTypeString(element.getType()));
            }

            return formatter.getText();
        }

        public ElementHandle getElementHandle() {
            return element;
        }

        public ElementKind getKind() {
            return kind;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        public boolean isLeaf() {
            switch (kind) {
            case ATTRIBUTE:
            case CONSTANT:
            case CONSTRUCTOR:
            case METHOD:
            case FIELD:
            case KEYWORD:
            case VARIABLE:
            case OTHER:
            case GLOBAL:
            case PACKAGE:
            case PROPERTY:
                return true;

            case MODULE:
            case CLASS:
                return false;

            default:
                throw new RuntimeException("Unhandled kind: " + kind);
            }
        }

        public List<?extends StructureItem> getNestedItems() {
            List<AstElement> nested = element.getChildren();

            if ((nested != null) && (nested.size() > 0)) {
                List<JsStructureItem> children = new ArrayList<JsStructureItem>(nested.size());

                for (Element co : nested) {
                    children.add(new JsStructureItem((AstElement)co, info, formatter));
                }

                return children;
            } else {
                return Collections.emptyList();
            }
        }

        public long getPosition() {
            return LexUtilities.getLexerOffset(info, element.getNode().getSourceStart());
        }

        public long getEndPosition() {
            return LexUtilities.getLexerOffset(info, element.getNode().getSourceEnd());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (!(o instanceof JsStructureItem)) {
                return false;
            }

            JsStructureItem d = (JsStructureItem)o;

            if (kind != d.kind) {
                return false;
            }

            if (!getName().equals(d.getName())) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = (29 * hash) + ((this.getName() != null) ? this.getName().hashCode() : 0);
            hash = (29 * hash) + ((this.kind != null) ? this.kind.hashCode() : 0);

            return hash;
        }

        @Override
        public String toString() {
            return getName();
        }

        public ImageIcon getCustomIcon() {
            return null;
        }
    }
}
