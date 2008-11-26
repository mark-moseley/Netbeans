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
package org.netbeans.modules.cnd.folding;

import antlr.TokenStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.*;

/**
 * implementation of walker used for folding
 * Responsible for creating folds:
 * - sequental #include directives
 * - #if* #endif
 * - block comments
 * - sequental line comments
 * @author Vladimir Voskresensky
 */
/*package*/ class APTFoldingWalker extends APTWalker {

    private static final int IFDEF_FOLD = CppFoldRecord.IFDEF_FOLD;
    private static final int INCLUDES_FOLD = CppFoldRecord.INCLUDES_FOLD;
    private APTFoldingCommentFilter filter = null;
    private List<CppFoldRecord> includeFolds = new ArrayList<CppFoldRecord>();
    private List<CppFoldRecord> ifdefFolds = new ArrayList<CppFoldRecord>();

    public APTFoldingWalker(APTFile apt) {
        super(apt, null);
    }

    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        return lang.getFilteredStream(getTokenStream());
    }

    @Override
    public TokenStream getTokenStream() {
        // get original
        // remove comments and hanlde includes
        filter = new APTFoldingCommentFilter(super.getTokenStream());
        return filter;
    }

    public List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> filterFolds = filter.getFolders();
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(filterFolds.size() + includeFolds.size() + ifdefFolds.size());
        out.addAll(filterFolds);
        out.addAll(includeFolds);
        out.addAll(ifdefFolds);
        return out;
    }

    protected void onInclude(APT apt) {
        include(apt);
    }

    protected void onIncludeNext(APT apt) {
        include(apt);
    }

    protected boolean onIf(APT apt) {
        return onStartPreprocNode(apt);
    }

    protected boolean onIfdef(APT apt) {
        return onStartPreprocNode(apt);
    }

    protected boolean onIfndef(APT apt) {
        return onStartPreprocNode(apt);
    }

    protected void onDefine(APT apt) {
        onOtherPreprocNode(apt);
    }

    protected void onUndef(APT apt) {
        onOtherPreprocNode(apt);
    }

    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        onOtherPreprocNode(apt);
        return true;
    }

    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        onOtherPreprocNode(apt);
        return true;
    }

    protected void onEndif(APT apt, boolean wasInBranch) {
        createEndifFold(apt);
    }

    @Override
    protected void onErrorNode(APT apt) {
        onOtherPreprocNode(apt);
    }

    @Override
    protected void onOtherNode(APT apt) {
        onOtherPreprocNode(apt);
    }

    @Override
    protected void onStreamNode(APT apt) {
        addIncludesIfNeeded();
    }

    @Override
    protected void onEOF() {
        addIncludesIfNeeded();
    }
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    private Stack<APT> ppStartDirectives = new Stack<APT>();

    private boolean onStartPreprocNode(APT apt) {
        filter.onPreprocNode(apt);
        addIncludesIfNeeded();
        ppStartDirectives.push(apt);
        return true;
    }

    private void createEndifFold(APT end) {
        filter.onPreprocNode(end);
        addIncludesIfNeeded();
        // there could be errors with unbalanced directives => check 
        if (!ppStartDirectives.empty()) {
            APT start = ppStartDirectives.pop();
            // we want fold after full "#if A" directive
            int startFold = start.getEndOffset();
            int endFold = end.getEndOffset();
            if (APTFoldingUtils.isStandalone()) {
                ifdefFolds.add(new CppFoldRecord(IFDEF_FOLD, start.getToken().getLine(), startFold, (end.getToken()).getEndLine(), endFold));
            } else {
                ifdefFolds.add(new CppFoldRecord(IFDEF_FOLD, startFold, endFold));
            }
        }
    }

    private void include(APT apt) {
        filter.onPreprocNode(apt);
        if (firstInclude == null) {
            firstInclude = apt;
        }
        lastInclude = apt;
    }

    private void addIncludesIfNeeded() {
        if (lastInclude != firstInclude) {
            assert (lastInclude != null);
            assert (firstInclude != null);
            // we want fold after #include string
            int start = (firstInclude.getToken()).getEndOffset();
            int end = lastInclude.getEndOffset();
            if (start < end) {
                if (APTFoldingUtils.isStandalone()) {
                    includeFolds.add(new CppFoldRecord(INCLUDES_FOLD, (firstInclude.getToken()).getLine(), start, (lastInclude.getToken()).getEndLine(), end));
                } else {
                    includeFolds.add(new CppFoldRecord(INCLUDES_FOLD, start, end));
                }
            }
        }
        lastInclude = null;
        firstInclude = null;
    }

    private void onOtherPreprocNode(APT apt) {
        filter.onPreprocNode(apt);
        addIncludesIfNeeded();
    }

    /** 
     * overrides APTWalker.stopOnErrorDirective 
     * We should be able to make folds after #error as well
     */
    @Override
    protected boolean stopOnErrorDirective() {
        return false;
    }
    private APT firstInclude = null;
    private APT lastInclude = null;
}
