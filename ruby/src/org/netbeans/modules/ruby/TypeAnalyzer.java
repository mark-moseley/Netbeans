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
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.ast.CallNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.DVarNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.openide.filesystems.FileObject;


/**
 * Perform type analysis on a given AST tree, attempting to provide a type
 * associated with each variable, field etc.
 *
 * @todo Track boolean types for simple operators; e.g.
 *    cc_no_width = letter == '[' && !width
 *    etc.  The operators here let me conclude cc_no_width is of type boolean!
 * @todo Handle find* method in Rails to indicate object types
 * @todo A reference to "foo." in a method is an alias to "@foo" if the method
 *    has not been defined explicitly. Attributes are especially clear, but an
 *    index lookup from type analyzer may be too expensive.
 * @todo The structure analyzer already tracks field declarations for the current class;
 *    I should use that to track down the types
 * @todo Use some statistical results to improve this; .to_s => String, .to_f => float,
 *   etc.
 * @todo In     create_table :posts do |t|
 *   I need to realize the type of "t" is ActiveRecord::ConnectionAdapters::TableDefinition from schema_definitions.rb
 * @todo Methods whose names end with "?" probably return TrueClass or FalseClass
 *   so I can handle those expressions without actual return value lookup
 *
 * @author Tor Norbye
 */
public class TypeAnalyzer {
    static final String PARAM_HINT_ARG = "#:arg:"; // NOI18N
    static final String PARAM_HINT_RETURN = "#:return:=>"; // NOI18N

    private RubyIndex index;
    /** Map from variable or field(etc) name to type. */
    private Map<String, String> types;
    private final int astOffset;
    private final int lexOffset;
    private final Node root;
    /** Node we are looking for;  */
    private Node target;
    private final BaseDocument doc;
    private final FileObject fileObject;

    /** Creates a new instance of TypeAnalyzer for a given position.
     * The {@link #analyze} method will do the rest. */
    public TypeAnalyzer(RubyIndex index, Node root, Node target, int astOffset, int lexOffset, BaseDocument doc, FileObject fileObject) {
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

        if (target == null && node.getPosition().getStartOffset() > astOffset) {
            return null;
        }

        // Algorithm: walk AST and look for assignments and such.
        // Attempt to compute the type of each expression and
        switch (node.nodeId) {
        case NodeTypes.LOCALASGNNODE:
        case NodeTypes.INSTASGNNODE:
        case NodeTypes.GLOBALASGNNODE:
        case NodeTypes.CLASSVARASGNNODE:
        case NodeTypes.CLASSVARDECLNODE:
        case NodeTypes.DASGNNODE: {
            String symbol = ((INameNode)node).getName();
            String type = expressionType(node);

            if (type != null) {
                types.put(symbol, type);
            } else {
                // A more complicated expresion of some sort - we're no longer
                // sure of the type
                types.remove(symbol);
            }
        }
//        case NodeTypes.ITERNODE: {
//            // A block. See if I know the LHS expression types, and if so
//            // I can propagate the type into the block variables.
//        }
//        case NodeTypes.CALLNODE: {
//            // Look for known calls whose return types we can guess
//            String name = ((INameNode)node).getName();
//            if (name.startsWith("find")) {
//            }
//        }
//            
        }

        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            analyze(child);
        }
        
        return null;
    }

    /** Called on AsgnNodes to compute RHS */
    private String expressionType(Node node) {
        // If it's a simple assignment, e.g. "= 5" it will have a single
        // child node
        // If it's a method call, it's slightly more complicated:
        //   x = String.new("Whatever")
        // gives me a LocalAsgnNode with a Call node child with name "new",
        // and a ConstNode receiver (could be a composite too)
        @SuppressWarnings("unchecked")
        List<Node> list = node.childNodes();

        if (list.size() != 1) {
            return null;
        }

        Node child = list.get(0);

        switch (child.nodeId) {
        case NodeTypes.CALLNODE: {
            // Look for known calls whose return types we can guess
            CallNode call = (CallNode)child;
            String name = call.getName();
            // If you call Foo.new I'm going to assume the type of the expression if "Foo"
            if ("new".equals(name)) { // NOI18N

                Node receiver = call.getReceiverNode();

                if (receiver instanceof Colon2Node) {
                    return AstUtilities.getFqn((Colon2Node)receiver);
                } else if (receiver instanceof INameNode) {
                    // TODO - compute fqn (packages etc.)
                    return ((INameNode)receiver).getName();
                }
            } else if (name.startsWith("find")) {
                // -Possibly- ActiveRecord finders, very important
                Node receiver = call.getReceiverNode();

                String receiverName = null;
                if (receiver instanceof Colon2Node) {
                    receiverName = AstUtilities.getFqn((Colon2Node)receiver);
                } else if (receiver instanceof INameNode) {
                    // TODO - compute fqn (packages etc.)
                    receiverName = ((INameNode)receiver).getName();
                }
                
                if (receiverName != null && index != null) {
                    IndexedClass superClass = index.getSuperclass(receiverName);
                    if (superClass != null && "ActiveRecord::Base".equals(superClass.getFqn())) { // NOI18N
                        // Looks like a find method on active record
                        // The big question is whether this is going to return
                        // the type itself (receivedName) or an array of it;
                        // that depends on the args (for find(:all) it's asn array,
                        // find(:first) it's an item, and for find(1,2,3) it's an
                        // array etc. 
                        // There are other find signatures which define other semantics
                        return pickFinderType(call, name, receiverName);
                    }
                }
            }
            
            // TODO - build up a return type database for lots of methods
            // whose returntypes we can guess, and look up dynamically
            // (probably backed by a cache since I'm frequently checkin the
            // same methods over and over
            
            break;
        }
        case NodeTypes.LOCALVARNODE:
            return types.get(((LocalVarNode)child).getName());
        case NodeTypes.DVARNODE:
            return types.get(((DVarNode)child).getName());
        case NodeTypes.INSTVARNODE:
            return types.get(((InstVarNode)child).getName());
        case NodeTypes.GLOBALVARNODE:
            return types.get(((GlobalVarNode)child).getName());
        case NodeTypes.CLASSVARNODE:
            return types.get(((ClassVarNode)child).getName());
        case NodeTypes.ARRAYNODE:
        case NodeTypes.ZARRAYNODE:
            return "Array"; // NOI18N
        case NodeTypes.STRNODE:
        case NodeTypes.DSTRNODE:
        case NodeTypes.XSTRNODE:
        case NodeTypes.DXSTRNODE:
            return "String"; // NOI18N
        case NodeTypes.FIXNUMNODE:
            return "Fixnum"; // NOI18N
        case NodeTypes.BIGNUMNODE:
            return "Bignum"; // NOI18N
        case NodeTypes.HASHNODE:
            return "Hash"; // NOI18N
        case NodeTypes.REGEXPNODE:
        case NodeTypes.DREGEXPNODE:
            return "Regexp"; // NOI18N
        case NodeTypes.SYMBOLNODE:
        case NodeTypes.DSYMBOLNODE:
            return "Symbol"; // NOI18N
        case NodeTypes.FLOATNODE:
            return "Float"; // NOI18N
        case NodeTypes.NILNODE:
            return "NilClass"; // NOI18N
        case NodeTypes.TRUENODE:
            return "TrueClass"; // NOI18N
        case NodeTypes.FALSENODE:
            return "FalseClass"; // NOI18N
            //} else if (child instanceof RangeNode) {
            //    return "Range"; // NOI18N
        }

        return null;
    }

    public String getType(String symbol) {
        if (types == null) {
            types = new HashMap<String, String>();

            if (fileObject != null) {
                initFileTypeVars();
            }
            
            if (doc != null) {
                initTypeAssertions();
            }

            analyze(root);
        }

        String type = types.get(symbol);
        
        // Special cases
        if (type == null) {
            // Handle migrations. This needs better flow analysis of block
            // variables but do quickfix for 6.0 which will work in most 
            // migrations files.
            if ("t".equals(symbol) && root.nodeId == NodeTypes.DEFSNODE) {
                String n = ((INameNode)root).getName();
                if ("up".equals(n) || ("down".equals(n))) {
                    return "ActiveRecord::ConnectionAdapters::TableDefinition";
                }
            }
        }

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
    
    
    private static final String[] RAILS_CONTROLLER_VARS = new String[] {
        // This is a bit of a trick. I really know the types of the
        // builtin fields here - @action_name, @assigns, @cookies,.
        // However, this usage is deprecated; people should be using
        // the corresponding accessor methods. Since I don't yet correctly
        // do type analysis of attribute to method mappings (because that would
        // require consulting the index to make sure the given method has not
        // been overridden), I'll just simulate this by pretending that there
        // are -local- variables of the given name corresponding to the return
        // value of these methods.
        "action_name", "String", // NOI18N
        
        "assigns", "Hash", // NOI18N
        
        "cookies", "ActionController::CookieJar", // NOI18N
        
        "flash", "ActionController::Flash::FlashHash", // NOI18N
        
        "headers", "Hash", // NOI18N
        
        "params", "Hash", // NOI18N
        
        "request", "ActionController::CgiRequest", // NOI18N
        
        "session", "CGI::Session", // NOI18N
        
        "url", "ActionController::UrlRewriter", // NOI18N
    };
    
    /** Look at the file type and see if we know about some known variables */
    private void initFileTypeVars() {
        assert fileObject != null;
        
        String ext = fileObject.getExt();
        if (ext.equals("rb")) {
            String name = fileObject.getName();
            if (name.endsWith("_controller")) { // NOI18N
                // request, params, etc.
                for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2) {
                    String var = RAILS_CONTROLLER_VARS[i];
                    String type = RAILS_CONTROLLER_VARS[i+1];
                    types.put(var, type);
                }
            }
            // test files
            //if (name.endsWith("_controller_test")) {
            // For test files in Rails, get testing context (#105043). In particular, actionpack's
            // ActionController::Assertions needs to be pulled in. This happens in action_controller/assertions.rb.
        } else if (ext.equals("rhtml") || ext.equals("erb")) { // NOI18N
            //Insert fields etc. as documented in actionpack's lib/action_view/base.rb (#105095)
            
            // Insert request, params, etc.
            for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2) {
                String var = RAILS_CONTROLLER_VARS[i];
                String type = RAILS_CONTROLLER_VARS[i+1];
                types.put(var, type);
            }
        } else if (ext.equals("rjs")) { // #105088
            types.put("page", "ActionView::Helpers::PrototypeHelper::JavaScriptGenerator::GeneratorMethods"); // NOI18N
        } else if (ext.equals("builder") || ext.equals("rxml")) { // NOI18N
            types.put("xml", "Builder::XmlMarkup"); // NOI18N
            /*
             */
        }
    }

    /** Look at type assertions in the document and initialize name context */
    private void initTypeAssertions() {
        if (root instanceof MethodDefNode) {
            // Look for parameter hints
            List<String> rdoc = AstUtilities.gatherDocumentation(null, doc, root);

            if ((rdoc != null) && (rdoc.size() > 0)) {
                for (String line : rdoc) {
                    if (line.startsWith(PARAM_HINT_ARG)) {
                        StringBuilder sb = new StringBuilder();
                        String name = null;
                        int max = line.length();
                        int i = PARAM_HINT_ARG.length();

                        for (; i < max; i++) {
                            char c = line.charAt(i);

                            if (c == ' ') {
                                continue;
                            } else if (c == '=') {
                                break;
                            } else {
                                sb.append(c);
                            }
                        }

                        if ((i == max) || (line.charAt(i) != '=')) {
                            continue;
                        }

                        i++;

                        if (sb.length() > 0) {
                            name = sb.toString();
                            sb.setLength(0);
                        } else {
                            continue;
                        }

                        if ((i == max) || (line.charAt(i) != '>')) {
                            continue;
                        }

                        i++;

                        for (; i < max; i++) {
                            char c = line.charAt(i);

                            if (c == ' ') {
                                continue;
                            }

                            if (!Character.isJavaIdentifierPart(c)) {
                                break;
                            } else {
                                sb.append(c);
                            }
                        }

                        if (sb.length() > 0) {
                            String type = sb.toString();
                            types.put(name, type);
                        }
                    }

                    //if (line.startsWith(":return:=>")) {
                    //    // I don't really need the return type yet
                    //}
                }
            }
        }
    }

    /** Look up the right return type for the given finder call */
    private String pickFinderType(CallNode call, String method, String model) {
        // Dynamic finders
        boolean multiple;
        if (method.startsWith("find_all")) { // NOI18N
            multiple = true;
        } else if (method.startsWith("find_by_") || method.equals("find_first")) { // NOI18N
            multiple = false;
        } else if (method.equals("find")) { // NOI18N
            // Finder method that does both - gotta inspect it
            List<Node> nodes = new ArrayList<Node>();
            AstUtilities.addNodesByType(call, new int[] {NodeTypes.SYMBOLNODE}, nodes);
            boolean foundAll = false;
            for (Node n : nodes) {
                SymbolNode symbol = (SymbolNode)n;
                if ("all".equals(symbol.getName())) { // NOI18N
                    foundAll = true;
                    break;
                }
            }
            multiple = foundAll;
        } else {
            // Not sure - probably some other locally defined finder method;
            // just default to the model name
            multiple = false;
        }
        
        if (multiple) {
            return "Array<" + model + ">";
        } else {
            return model;
        }
    }
}
