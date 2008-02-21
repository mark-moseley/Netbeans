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
package org.netbeans.modules.javascript.editing;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.modules.javascript.editing.lexer.JsCommentLexer;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;


/**
 * Perform type analysis on a given AST tree, attempting to provide a type
 * associated with each variable, field etc.
 *
 * @todo Track boolean types for simple operators; e.g.
 *    cc_no_width = letter == '[' && !width
 *    etc.  The operators here let me conclude cc_no_width is of type boolean!
 * @todo Use some statistical results to improve this; .to_s => String, .to_f => float,
 *   etc.
 * @todo I see some conventions in the JavaScript source for annotating returtn
 *   types and parameter types; see this from dojo.js for example:
 * <pre>
 *       dojo.trim = function(/*String*^/ str){
 *        return str.replace(/^\s\s*, '').replace(/\s\s*$/, ''); // String
 *  Watch out for this non-type:
 *     return o; /*anything*^/
 *  Look out for question marks too:
 *    /*int?*^/ delay, /*Boolean?*^/
 *  and nontypes like "int".
 *  (In all of the above, *^/ should really be the comment terminator.)
 * 
 * @todo prototype.js adds the $() function which extends objects passed through it as elements,
 *   so treat these as elements!
 * @todo When you see Element.extend, also resolve the result as an Element
        var my_div = document.createElement('div');

        Element.extend(my_div);
        my_div.addClassName('pending').hide();
 * 
 * </pre>
 * 
 * @todo Handle type unions (of the form Foo|Bar|Baz) - queries need to look at all.
 * 
 * 
 *
 * @author Tor Norbye
 */
public class JsTypeAnalyzer {
    private JsIndex index;
    /** Map from variable or field(etc) name to type. */
    private Map<String, String> types;
    private final int astOffset;
    private final int lexOffset;
    private final Node root;
    /** Node we are looking for;  */
    private Node target;
    private final BaseDocument doc;
    private final FileObject fileObject;
    private final CompilationInfo info;

    /** Creates a new instance of JsTypeAnalyzer for a given position.
     * The {@link #analyze} method will do the rest. */
    public JsTypeAnalyzer(CompilationInfo info, JsIndex index, Node root, Node target, int astOffset, int lexOffset, BaseDocument doc, FileObject fileObject) {
        this.info = info;
        this.index = index;
        this.root = root;
        this.target = target;
        this.astOffset = astOffset;
        this.lexOffset = lexOffset;
        this.doc = doc;
        this.fileObject = fileObject;
    }

    /**
     * Analyze the given code block down to the given offset. The {@link #getType}
     * method can then be used to read out the symbol type if any at that point.
     * Returns the type of the current expression, if known.
     */
    private String analyze(Node node) {
        // Avoid including definitions appearing later in the
        // context than the caret. (This only works for local variable
        // analysis; for fields it could be complicated by code earlier
        // than the caret calling code later than the caret which initializes
        // the fild...
        if (node == target) {
            target = null;
        }

        if (target == null && node.getSourceStart() > astOffset) {
            return null;
        }

        // Algorithm: walk AST and look for assignments and such.
        // Attempt to compute the type of each expression and
        switch (node.getType()) {
        case Token.VAR: {
            Node first = AstUtilities.getFirstChild(node);
            if (first != null) {
                Node rhs = AstUtilities.getFirstChild(first);
                if (rhs != null) {
                    String name = first.getString();
                    String type = expressionType(rhs);
                    setType(type, name);
                }
            }
            break;
        }
        
        case Token.SETNAME: {
            String name = AstUtilities.getFirstChild(node).getString();
            Node expr = AstUtilities.getSecondChild(node);
            if (expr != null) {
                String type = expressionType(expr);
                setType(type, name);
            }
            break;
        }
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            analyze(child);
        }
        
        return null;
    }

    /** Called on AsgnNodes to compute RHS */
    private String expressionType(Node node) {
        switch (node.getType()) {
        case Token.NUMBER:
            return "Number";
        case Token.STRING:
            return "String";
        case Token.REGEXP:
            return "RegExp";
        case Token.TRUE:
        case Token.FALSE:
            return "Boolean";
        case Token.ARRAYLIT:
            return "Array";
        case Token.FUNCTION:
            return "Function";
        case Token.NEW: {
            Node first = AstUtilities.getFirstChild(node);
            if (first.getType() == Token.NAME) {
                return first.getString();
            } else {
                return expressionType(first);
            }
        }
        case Token.NAME: {
            String name = node.getString();
            return types.get(name);
        }
        case Token.GETPROP: {
            Node first = AstUtilities.getFirstChild(node);
            String secondStr = AstUtilities.getSecondChild(node).getString();
            if (first.getType() == Token.NAME) {
               return first.getString()+"."+secondStr; // NOI18N
            } else {
                String lhsType = expressionType(first);
                if (lhsType != null) {
                    return lhsType+"."+secondStr; // NOI18N
                } else {
                    return null;
                }
            }
        }
        default:
            return null;
        }
    }

    public String getType(String symbol) {
        if (types == null) {
            types = new HashMap<String, String>();

//            if (fileObject != null) {
//                initFileTypeVars();
//            }
//            
            if (doc != null) {
                initTypeAssertions();
            }

            analyze(root);
        }

        String type = types.get(symbol);
        
        // We keep track of the types contained within Arrays
        // internally (and probably hashes as well, TODO)
        // such that we can do the right thing when you operate
        // on an Array. However, clients should only see the "raw" (and real)
        // type.
        if (type != null && type.startsWith("Array<")) { // NOI18N
            return "Array"; // NOI18N
        }
        
        return type;
    }
    
    private void setType(String type, String name) {

        if (type != null) {
            types.put(name, type);
        } else {
            // A more complicated expresion of some sort - we're no longer
            // sure of the type
            types.remove(name);
        }
    }
    
    /** Look at type assertions in the document and initialize name context */
    private void initTypeAssertions() {
        if (root.getType() == Token.FUNCTION) {
            // Look for parameter hints 
            TokenSequence<? extends JsCommentTokenId> ts = AstUtilities.getCommentFor(info, doc, (FunctionNode)root);
            
            if (ts != null) {
                Map<String, String> typeMap = JsCommentLexer.findFunctionTypes(ts);
                for (Map.Entry<String,String> entry : typeMap.entrySet()) {
                    // TODO - filter out @return name here. Doesn't cause any damage though.
                    types.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
