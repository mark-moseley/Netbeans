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
import antlr.TokenStreamException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTStream;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base Tree walker for APT
 * @author Vladimir Voskresensky
 */
public abstract class APTWalker {
    private final APTMacroMap macros;
    private final APTFile root;
    private boolean walkerInUse = false;
    private boolean stopped = false;
    
    /**
     * Creates a new instance of APTWalker
     */
    public APTWalker(APTFile apt, APTMacroMap macros) {
        assert (apt != null) : "how can we work on null tree?"; // NOI18N
        this.root = apt;
        this.macros = macros;
    }
    
    /** fast visit APT without generating token stream */
    public void visit() {
        if (walkerInUse) {
            throw new IllegalStateException("walker could be used only once"); // NOI18N
        }  
        if (APTTraceFlags.APT_NON_RECURSE_VISIT) {
            nonRecurseVisit();
        } else {
            walkerInUse = true;
            visit(root.getFirstChild());
        }
    }
     
    private void nonRecurseVisit() {
        init(false);
        while(!finished()) {           
            toNextNode();
        }        
    }
    
    public TokenStream getTokenStream() {
        if (walkerInUse) {
            throw new IllegalStateException("walker could be used only once"); // NOI18N
        }
        return new WalkerTokenStream();
    }
    
    private class WalkerTokenStream implements TokenStream, APTTokenStream {
        public WalkerTokenStream() {
            init(true);
        }
        
        public APTToken nextToken() {
            try {
                //do {
                //do {
                //token = nextTokenImpl();
                //token = onToken(token); not used anywhere
                //} while (token == null);
                return nextTokenImpl();
            } catch (TokenStreamException ex) {
                APTUtils.LOG.log(Level.SEVERE, "{0}", new Object[] { ex });
                return APTUtils.EOF_TOKEN;
            }
        }
    }
    
//    /** walk APT and generate correspondent token stream */
//    private Token nextToken() throws TokenStreamException {
////        if (!initedTokenStreamCreating) {
////            init();
////        }
//        Token token = null;
//        do {
//            token = nextTokenImpl();
//            token = onToken(token);
//        } while (token == null);
//        return token;
//    }    
//    
//    private int LA() throws TokenStreamException {
//        if (!initedTokenStreamCreating) {
//            init();
//        }        
//        laToken = (laToken == null) ? nextTokenImpl() : laToken;
//        return laToken.getType();
//    }
    
    ////////////////////////////////////////////////////////////////////////////
    // template methods to override by extensions 

    protected abstract void onInclude(APT apt);

    protected abstract void onIncludeNext(APT apt);
        
    protected abstract void onDefine(APT apt);
    
    protected abstract void onUndef(APT apt);
    
    // preproc conditions
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIf(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIfdef(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onIfndef(APT apt);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onElif(APT apt, boolean wasInPrevBranch);
    
    /*
     * @return true if infrastructure can continue visiting children
     *         false if infrastructure should not visit children
     */
    protected abstract boolean onElse(APT apt, boolean wasInPrevBranch);
    
    protected abstract void onEndif(APT apt, boolean wasInBranch);
    
    // callback for stream node
    protected void onStreamNode(APT apt) {
        // do nothing
    }
    
    /**
     * Callback for #error node.
     */
    protected void onErrorNode(APT apt) {
        // do nothing
    }
    
    protected void onOtherNode(APT apt) {
        // do nothing
    }

    protected void onEOF() {
        // do nothing
    }
    
    /**
     * Determines whether the walker should stop or proceed
     * as soon as it encounteres #error directive
     *
     * @return true if the walker should stop on #error directive,
     * otherwise false
     */
    protected boolean stopOnErrorDirective() {
	return true;
    }
    
    // tokens stream generating/callback for token
//    protected abstract Token onToken(Token token);  
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    /** fast recursive walker to be used when token stream is not interested */
    private void visit(APT t) {
        if (t == null || isStopped()) {
            return;
        }
        boolean wasInChild = false;
        for (APT node = t; node != null && !isStopped(); node = node.getNextSibling()) {
            if (node.getType() == APT.Type.CONDITION_CONTAINER) {
                assert(node.getFirstChild() != null);
                visit(node.getFirstChild());
            } else {
                boolean visitChild = onAPT(node, wasInChild);
                if (visitChild && node.getFirstChild() != null) {
                    assert(APTUtils.isStartOrSwitchConditionNode(node.getType()));
                    visit(node.getFirstChild());
                }
                wasInChild |= visitChild;
            }
        }
    } 
    
    protected boolean onAPT(APT node, boolean wasInBranch) {
        boolean visitChild = false;
        switch(node.getType()) {
            case APT.Type.IF:
                visitChild = onIf(node);
                break;
            case APT.Type.IFDEF:
                visitChild = onIfdef(node);
                break;
            case APT.Type.IFNDEF:
                visitChild = onIfndef(node);
                break;
            case APT.Type.ELIF:
                visitChild = onElif(node, wasInBranch);
                break;
            case APT.Type.ELSE:
                visitChild = onElse(node, wasInBranch);
                break;
            case APT.Type.ENDIF:
                onEndif(node, wasInBranch);
                break;
            case APT.Type.DEFINE:
                onDefine(node);
                break;
            case APT.Type.UNDEF:
                onUndef(node);
                break;
            case APT.Type.INCLUDE:
                onInclude(node);
                break;
            case APT.Type.INCLUDE_NEXT:
                onIncludeNext(node);
                break;
            case APT.Type.TOKEN_STREAM:
//                onStreamNode(node);
                break;
            case APT.Type.ERROR:
		onErrorNode(node);
		break;
            case APT.Type.INVALID:
            case APT.Type.LINE:
            case APT.Type.PRAGMA:
            case APT.Type.PREPROC_UNKNOWN:   
                onOtherNode(node);
                break;
            default:
                assert(false) : "unsupported " + APTTraceUtils.getTypeName(node); // NOI18N
        }
        if (APTUtils.LOG.isLoggable(Level.FINE)) {
            APTUtils.LOG.log(Level.FINE, "onAPT: {0}; {1} {2}",  // NOI18N
                    new Object[]    {
                                    node,
                                    (wasInBranch ? "Was before;" : ""), // NOI18N
                                    (visitChild ? "Will visit children" : "") // NOI18N
                                    }
                            );
        }
        return visitChild;
    }
    
    private void pushState() {
        visits.push(new WalkerState(curAPT, curWasInChild));
    }
    
    private boolean popState() {
        if (visits.isEmpty()) {
            return false;
        }
        WalkerState state = visits.pop();
        curAPT = state.lastNode;
        curWasInChild = state.wasInChild;
        return true;
    }
    
    private void init(boolean needStream) {
        curAPT = root.getFirstChild();
        if (needStream) {
            fillTokens();        
        }
        curWasInChild = false;
        pushState();
        walkerInUse = true;
    }    
    
    private APTToken nextTokenImpl() throws TokenStreamException {
        APTToken theRetToken;
        tokenLoop:
        for (;;) {           
            while (!tokens.isEmpty()) {
                TokenStream ts = tokens.peek();
                theRetToken = (APTToken) ts.nextToken();
                if (!APTUtils.isEOF(theRetToken)) {
                    return theRetToken;
                } else {
                    tokens.removeFirst();
                }
            }
            if (finished()) {
                onEOF();
                return APTUtils.EOF_TOKEN;
            } else {        
                toNextNode();
                fillTokens();                 
            }
        }
    }
    
    private void toNextNode() {
        popState();
        if (finished()) {
            return;
        }
        if (curAPT == null) {
            // we are in APT of incomplete file
            APTUtils.LOG.log(Level.SEVERE, "incomplete APT {0}", new Object[] { root });// NOI18N
            do {
                popState();
                curAPT = curAPT.getNextSibling();
            } while (curAPT == null && !finished());
            
            if (curAPT == null) {
                return;
            }
        }
        if (curAPT.getType() == APT.Type.CONDITION_CONTAINER) {
            // new conditional container node
            
            // if wasn't yet in any children
            curWasInChild = false; 
            // push container to have possibility move on it's sibling after ENDIF
            pushState();
            // move to the first child of container
            assert(curAPT.getFirstChild() != null);
            curAPT = curAPT.getFirstChild();           
        } 

        // allow any actions in extension for the current node
        boolean visitChild = onAPT(curAPT, curWasInChild);
        curWasInChild |= visitChild;            
        if (visitChild) {          
            // move on next node to visit
            assert(APTUtils.isStartOrSwitchConditionNode(curAPT.getType()));   
            if (curAPT.getFirstChild() != null) {
                // node has children
                // push to have possibility move on it's sibling after visited children
                pushState();                  
                curAPT = curAPT.getFirstChild();
                curWasInChild = false;
            } else {
                // move on sibling, as cur node has empty children
                curAPT = curAPT.getNextSibling();
            }
        } else {
            if (curAPT.getType() == APT.Type.ENDIF) {
                APT endif = curAPT;
                // end of condition block
                popState();
                if (curAPT.getType() != APT.Type.CONDITION_CONTAINER) {
                    APTUtils.LOG.log(Level.SEVERE, 
                            "#endif directive {0} without starting #if in APT {1}", // NOI18N
                            new Object[] { endif, root });
                }
                curWasInChild = false;
            }
	    else if( curAPT.getType() == APT.Type.ERROR ) {
		if (stopOnErrorDirective()) {
		    stop();
		    return;
		}
	    }
            curAPT = curAPT.getNextSibling();
            while (curAPT == null && !finished()) {
                popState();
                curAPT = curAPT.getNextSibling();
            }
        }
        if (!finished()) {
            pushState();
        }
    }
    
    private void fillTokens() {
        // only token stream nodes contain tokens as TokenStream
        if (curAPT != null && (curAPT.getType() == APT.Type.TOKEN_STREAM)) {
            onStreamNode(curAPT);
            TokenStream ts = ((APTStream)curAPT).getTokenStream();
            tokens.addFirst(ts);
        }
    }
    
    private boolean finished() {
        return (curAPT == null && visits.isEmpty()) || isStopped();
    }
    
    protected final APTMacroMap getMacroMap() {
        return macros;
    }
    
    protected final APT getCurNode() {
        return curAPT;
    }

    public final APTFile getRootFile() {
        return root;
    }

    // fields to be used when generating token stream
    private APT curAPT;
    private boolean curWasInChild;
    private LinkedList<TokenStream> tokens = new LinkedList<TokenStream>();
    private Stack<WalkerState> visits = new Stack<WalkerState>();
    
    private static final class WalkerState {
        APT lastNode;
        boolean wasInChild;
        private WalkerState(APT node, boolean wasInChildState) {
            this.lastNode = node;
            this.wasInChild = wasInChildState;
        }
    }     

    protected final boolean isStopped() {
        return stopped;
    }

    protected final void stop() {
        this.stopped = true;
    }
}
