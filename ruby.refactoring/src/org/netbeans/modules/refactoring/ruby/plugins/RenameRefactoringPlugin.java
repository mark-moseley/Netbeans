/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.refactoring.ruby.plugins;

import org.jruby.nb.ast.ClassVarAsgnNode;
import org.jruby.nb.ast.ClassVarDeclNode;
import org.jruby.nb.ast.ClassVarNode;
import org.jruby.nb.ast.Colon2Node;
import org.jruby.nb.ast.GlobalAsgnNode;
import org.jruby.nb.ast.GlobalVarNode;
import org.jruby.nb.ast.InstAsgnNode;
import org.jruby.nb.ast.InstVarNode;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.SymbolNode;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyStructureAnalyzer.AnalysisResult;
import org.netbeans.modules.ruby.elements.AstElement;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.jruby.nb.ast.ArgumentNode;
import org.jruby.nb.ast.ClassNode;
import org.jruby.nb.ast.DAsgnNode;
import org.jruby.nb.ast.DVarNode;
import org.jruby.nb.ast.LocalAsgnNode;
import org.jruby.nb.ast.LocalVarNode;
import org.jruby.nb.ast.ModuleNode;
import org.jruby.nb.ast.SClassNode;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.ModificationResult;
import org.netbeans.napi.gsfret.source.ModificationResult.Difference;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.WorkingCopy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.refactoring.ruby.DiffElement;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.ruby.RetoucheUtils;
import org.netbeans.modules.refactoring.ruby.RubyElementCtx;
import org.netbeans.modules.ruby.RubyUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.ruby.RubyParseResult;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

/**
 * The actual Renaming refactoring work for Ruby. The skeleton (name checks etc.) based
 * on the Java refactoring module by Jan Becicka, Martin Matula, Pavel Flaska and Daniel Prusa.
 * 
 * @author Jan Becicka
 * @author Martin Matula
 * @author Pavel Flaska
 * @author Daniel Prusa
 * @author Tor Norbye
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * @todo Check that the new name doesn't conflict with an existing name
 * @todo Check unknown files!
 * @todo More prechecks
 * @todo When invoking refactoring on a file object, I also rename the file. I should (a) list the
 *   name it's going to change the file to, and (b) definitely "filenamize" it - e.g. for class FooBar the
 *   filename should be foo_bar.
 * @todo If you rename a Model, I should add a corresponding rename_table entry in the migrations...
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
public class RenameRefactoringPlugin extends RubyRefactoringPlugin {
    
    private RubyElementCtx treePathHandle = null;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
//    private boolean doCheckName = true;
    
    private RenameRefactoring refactoring;
    
    /** Creates a new instance of RenameRefactoring */
    public RenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        RubyElementCtx tph = rename.getRefactoringSource().lookup(RubyElementCtx.class);
        if (tph!=null) {
            treePathHandle = tph;
        } else {
            Source source = RetoucheUtils.getSource(rename.getRefactoringSource().lookup(FileObject.class));
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }
                    
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
                        org.jruby.nb.ast.Node root = AstUtilities.getRoot(co);
                        if (root != null) {
                            RubyParseResult rpr = AstUtilities.getParseResult(co);
                            if (rpr != null) {
                                AnalysisResult ar = rpr.getStructure();
                                List<? extends AstElement> els = ar.getElements();
                                if (els.size() > 0) {
                                    // TODO - try to find the outermost or most "relevant" module/class in the file?
                                    // In Java, we look for a class with the name corresponding to the file.
                                    // It's not as simple in Ruby.
                                    AstElement element = els.get(0);
                                    org.jruby.nb.ast.Node node = element.getNode();
                                    treePathHandle = new RubyElementCtx(root, node, element, co.getFileObject(), co);
                                    refactoring.getContext().add(co);
                                }
                            }
                        }
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected Source getRubySource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case CHECKPARAMETERS:    
                if (treePathHandle==null) {
                    return null;
                }
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return RetoucheUtils.createSource(cpInfo, treePathHandle.getFileObject());
            case FASTCHECKPARAMETERS:
                return RetoucheUtils.getSource(treePathHandle.getFileObject());

        }
        throw new IllegalStateException();
    }
    
    protected Problem preCheck(CompilationController info) throws IOException {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    
    protected Problem fastCheckParameters(CompilationController info) throws IOException {
        Problem fastCheckProblem = null;
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
        ElementKind kind = treePathHandle.getKind();
        String newName = refactoring.getNewName();
        String oldName = treePathHandle.getSimpleName();
        if (oldName == null) {
            return new Problem(true, "Cannot determine target name. Please file a bug with detailed information on how to reproduce (preferably including the current source file and the cursor position)");
        }
        
        if (oldName.equals(newName)) {
            boolean nameNotChanged = true;
            //if (kind == ElementKind.CLASS || kind == ElementKind.MODULE) {
            //    if (!((TypeElement) element).getNestingKind().isNested()) {
            //        nameNotChanged = info.getFileObject().getName().equals(element);
            //    }
            //}
            if (nameNotChanged) {
                fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                return fastCheckProblem;
            }
            
        }
        
        // TODO - get a better ruby name picker - and check for invalid Ruby symbol names etc.
        // TODO - call RubyUtils.isValidLocalVariableName if we're renaming a local symbol!
        if (kind == ElementKind.CLASS && !RubyUtils.isValidConstantFQN(newName)) {
            String s = getString("ERR_InvalidClassName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else if (kind == ElementKind.METHOD && !RubyUtils.isValidRubyMethodName(newName)) {
            String s = getString("ERR_InvalidMethodName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else if (!RubyUtils.isValidRubyIdentifier(newName)) {
            String s = getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[] {newName}
            );
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        String msg = getWarningMsg(kind, newName);
        if (msg != null) {
            fastCheckProblem = createProblem(fastCheckProblem, false, msg);
        }
        
        return fastCheckProblem;
    }
    
    protected Problem checkParameters(CompilationController info) throws IOException {
        
        Problem checkProblem = null;
        int steps = 0;
        if (overriddenByMethods != null) {
            steps += overriddenByMethods.size();
        }
        if (overridesMethods != null) {
            steps += overridesMethods.size();
        }
        
        fireProgressListenerStart(RenameRefactoring.PARAMETERS_CHECK, 8 + 3*steps);
        
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
//        Element element = treePathHandle.resolveElement(info);
        
        fireProgressListenerStep();
        fireProgressListenerStep();
        fireProgressListenerStop();
        return checkProblem;
    }
    
    @Override
    public Problem preCheck() {
        if (treePathHandle == null || treePathHandle.getFileObject() == null || !treePathHandle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(RenameRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        }
        
        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        Source source = RetoucheUtils.createSource(cpInfo, treePathHandle.getFileObject());

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
                public void run(CompilationController info) throws Exception {
                    // TODO if getSearchInComments I -should- search all files
                    if (treePathHandle.getKind() == ElementKind.VARIABLE || treePathHandle.getKind() == ElementKind.PARAMETER) {
                        // For local variables, only look in the current file!
                        set.add(info.getFileObject());
                    }  else {
                        set.addAll(RetoucheUtils.getRubyFilesInProject(info.getFileObject()));
                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
    private Set<RubyElementCtx> allMethods;
    
    public Problem prepare(RefactoringElementsBag elements) {
        if (treePathHandle == null) {
            return null;
        }
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            TransformTask transform = new TransformTask(new RenameTransformer(refactoring.getNewName(), allMethods), treePathHandle);
            final Collection<ModificationResult> results = processFiles(a, transform);
            elements.registerTransaction(new RetoucheCommit(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference diff: result.getDifferences(jfo)) {
                        String old = diff.getOldText();
                        if (old!=null) {
                            //TODO: workaround
                            //generator issue?
                            elements.add(refactoring,DiffElement.create(diff, jfo, result));
                        }
                    }
                }
            }
        }
        // see #126733. need to set a correct new name for the file rename plugin
        // that gets invoked after this plugin when the refactoring is invoked on a file.
        if (refactoring.getRefactoringSource().lookup(FileObject.class) != null) {
            String newName = RubyUtils.camelToUnderlinedName(refactoring.getNewName());
            refactoring.setNewName(newName);
        }

        fireProgressListenerStop();
                
        return null;
    }

    private static final String getString(String key) {
        return NbBundle.getMessage(RenameRefactoringPlugin.class, key);
    }

    private String getWarningMsg(ElementKind kind, String newName) {
        String msg = null;
        if (ElementKind.CLASS == kind) {
            for (String each : newName.split("::")) {
                //NOI18N
                msg = RubyUtils.getIdentifierWarning(each, 0);
                if (msg != null) {
                    break;
                }
            }
        } else {
            msg = RubyUtils.getIdentifierWarning(newName, 0);
        }
        return msg;
    }
    
    /**
     *
     * @author Jan Becicka
     */
    public class RenameTransformer extends SearchVisitor {

        private Set<RubyElementCtx> allMethods;
        private String newName;
        private String oldName;
        private CloneableEditorSupport ces;
        private List<Difference> diffs;

        @Override
        public void setWorkingCopy(WorkingCopy workingCopy) {
            // Cached per working copy
            this.ces = null;
            assert diffs == null; // Should have been committed already
            super.setWorkingCopy(workingCopy);
        }
        
        public RenameTransformer(String newName, Set<RubyElementCtx> am) {
            this.newName = newName;
            this.oldName = treePathHandle.getSimpleName();
            this.allMethods = am;
        }
        
        @Override
        public void scan() {
            // TODO - do I need to force state to resolved?
            //compiler.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

            diffs = new ArrayList<Difference>();
            RubyElementCtx searchCtx = treePathHandle;
            Error error = null;
            Node root = AstUtilities.getRoot(workingCopy);
            if (root != null) {
                
                Element element = AstElement.create(workingCopy, root);
                Node node = searchCtx.getNode();
                RubyElementCtx fileCtx = new RubyElementCtx(root, node, element, workingCopy.getFileObject(), workingCopy);
                Node method = null;
                if (node instanceof ArgumentNode) {
                    AstPath path = searchCtx.getPath();
                    assert path.leaf() == node;
                    Node parent = path.leafParent();

                    if (!(parent instanceof MethodDefNode)) {
                        method = AstUtilities.findLocalScope(node, path);
                    }
                } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode || node instanceof DAsgnNode || 
                        node instanceof DVarNode) {
                    // A local variable read or a parameter read, or an assignment to one of these
                    AstPath path = searchCtx.getPath();
                    method = AstUtilities.findLocalScope(node, path);
                }

                if (method != null) {
                    findLocal(searchCtx, fileCtx, method, oldName);
                } else {
                    // Full AST search
                    AstPath path = new AstPath();
                    path.descend(root);
                    find(path, searchCtx, fileCtx, root, oldName, Character.isUpperCase(oldName.charAt(0)));
                    path.ascend();
                }
            } else {
                // See if the document contains references to this symbol and if so, put a warning in
                if (workingCopy.getText().indexOf(oldName) != -1) {
                    // TODO - icon??
                    if (ces == null) {
                        ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
                    }
                    int start = 0;
                    int end = 0;
                    String desc = NbBundle.getMessage(RenameRefactoringPlugin.class, "ParseErrorFile", oldName);
                    List<Error> errors = workingCopy.getErrors();
                    if (errors.size() > 0) {
                        for (Error e : errors) {
                            if (e.getSeverity() == Severity.ERROR) {
                                error = e;
                                break;
                            }
                        }
                        if (error == null) {
                            error = errors.get(0);
                        }
                        
                        String errorMsg = error.getDisplayName();
                        
                        if (errorMsg.length() > 80) {
                            errorMsg = errorMsg.substring(0, 77) + "..."; // NOI18N
                        }

                        desc = desc + "; " + errorMsg;
                        start = error.getStartPosition();
                        start = LexUtilities.getLexerOffset(workingCopy, start);
                        if (start == -1) {
                            start = 0;
                        }
                        end = start;
                    }
                    PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                    PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                    Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, "", "", desc); // NOI18N
                    diffs.add(diff);
                }
            }

            if (error == null && refactoring.isSearchInComments()) {
                Document doc = RetoucheUtils.getDocument(workingCopy, workingCopy.getFileObject());
                if (doc != null) {
                    //force open
                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = th.tokenSequence();

                    ts.move(0);

                    searchTokenSequence(ts);
                }
            }

            // Sort the diffs, if applicable
            if (diffs.size() > 0) {
                Collections.sort(diffs, new Comparator<Difference>() {
                    public int compare(Difference d1, Difference d2) {
                        return d1.getStartPosition().getOffset() - d2.getStartPosition().getOffset();
                    }
                });
                for (Difference diff : diffs) {
                    workingCopy.addDiff(diff);
                }
            }
            diffs = null;
            ces = null;
            
        }
        
        private void searchTokenSequence(TokenSequence<?> ts) {
            if (ts.moveNext()) {
                do {
                    Token<?> token = ts.token();
                    TokenId id = token.id();

                    String primaryCategory = id.primaryCategory();
                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
                        // search this comment
                        CharSequence tokenText = token.text();
                        if (tokenText == null || oldName == null) {
                            continue;
                        }
                        int index = TokenUtilities.indexOf(tokenText, oldName);
                        if (index != -1) {
                            String text = tokenText.toString();
                            // TODO make sure it's its own word. Technically I could
                            // look at identifier chars like "_" here but since they are
                            // used for other purposes in comments, consider letters
                            // and numbers as enough
                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index-1))) &&
                                    (index+oldName.length() >= text.length() || 
                                    !Character.isLetterOrDigit(text.charAt(index+oldName.length())))) {
                                int start = ts.offset() + index;
                                int end = start + oldName.length();
                                if (ces == null) {
                                    ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
                                }
                                PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                                PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                                String desc = getString("ChangeComment");
                                Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldName, newName, desc);
                                diffs.add(diff);
                            }
                        }
                    } else {
                        TokenSequence<?> embedded = ts.embedded();
                        if (embedded != null) {
                            searchTokenSequence(embedded);
                        }                                    
                    }
                } while (ts.moveNext());
            }
        }

        private void rename(Node node, String oldCode, String newCode, String desc) {
            OffsetRange range = AstUtilities.getNameRange(node);
            assert range != OffsetRange.NONE;
            int pos = range.getStart();

            if (desc == null) {
                // TODO - insert "method call", "method definition", "class definition", "symbol", "attribute" etc. and from and too?
                if (node instanceof MethodDefNode) {
                    desc = getString("UpdateMethodDef");
                } else if (AstUtilities.isCall(node)) {
                    desc = getString("UpdateCall");
                } else if (node instanceof SymbolNode) {
                    desc = getString("UpdateSymbol");
                } else if (node instanceof ClassNode || node instanceof SClassNode) {
                    desc = getString("UpdateClassDef");
                } else if (node instanceof ModuleNode) {
                    desc = getString("UpdateModule");
                } else if (node instanceof LocalVarNode || node instanceof LocalAsgnNode || node instanceof DVarNode || node instanceof DAsgnNode) {
                    desc = getString("UpdateLocalvar");
                } else if (node instanceof GlobalVarNode || node instanceof GlobalAsgnNode) {
                    desc = getString("UpdateGlobal");
                } else if (node instanceof InstVarNode || node instanceof InstAsgnNode) {
                    desc = getString("UpdateInstance");
                } else if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode || node instanceof ClassVarAsgnNode) {
                    desc = getString("UpdateClassvar");
                } else {
                    desc = NbBundle.getMessage(RenameRefactoringPlugin.class, "UpdateRef", oldCode);
                }
            }

            if (ces == null) {
                ces = RetoucheUtils.findCloneableEditorSupport(workingCopy);
            }
            
            // Convert from AST to lexer offsets if necessary
            pos = LexUtilities.getLexerOffset(workingCopy, pos);
            if (pos == -1) {
                // Translation failed
                return;
            }
            
            int start = pos;
            int end = pos+oldCode.length();
            // TODO if a SymbolNode, +=1 since the symbolnode includes the ":"
            BaseDocument doc = null;
            try {
                doc = (BaseDocument)ces.openDocument();
                doc.readLock();

                if (start > doc.getLength()) {
                    start = end = doc.getLength();
                }

                if (end > doc.getLength()) {
                    end = doc.getLength();
                }

                // Look in the document and search around a bit to detect the exact method reference
                // (and adjust position accordingly). Thus, if I have off by one errors in the AST (which
                // occasionally happens) the user's source won't get munged
                if (!oldCode.equals(doc.getText(start, end-start))) {
                    // Look back and forwards by 1 at first
                    int lineStart = Utilities.getRowFirstNonWhite(doc, start);
                    int lineEnd = Utilities.getRowLastNonWhite(doc, start)+1; // +1: after last char
                    if (lineStart == -1 || lineEnd == -1) { // We're really on the wrong line!
                        System.out.println("Empty line entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + start + " referenced by node " + 
                                node + " of type " + node.getClass().getName());
                        return;
                    }

                    if (lineStart < 0 || lineEnd-lineStart < 0) {
                        return; // Can't process this one
                    }

                    String line = doc.getText(lineStart, lineEnd-lineStart);
                    if (line.indexOf(oldCode) == -1) {
                        System.out.println("Skipping entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + line + " referenced by node " + 
                                node + " of type " + node.getClass().getName());
                    } else {
                        int lineOffset = start-lineStart;
                        int newOffset = -1;
                        // Search up and down by one
                        for (int distance = 1; distance < line.length(); distance++) {
                            // Ahead first
                            if (lineOffset+distance+oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset+distance, lineOffset+distance+oldCode.length()))) {
                                newOffset = lineOffset+distance;
                                break;
                            }
                            if (lineOffset-distance >= 0 && lineOffset-distance+oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset-distance, lineOffset-distance+oldCode.length()))) {
                                newOffset = lineOffset-distance;
                                break;
                            }
                        }

                        if (newOffset != -1) {
                            start = newOffset+lineStart;
                            end = start+oldCode.length();
                        }
                    }
                }
            } catch (IOException ie) {
                Exceptions.printStackTrace(ie);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } finally {
                if (doc != null) {
                    doc.readUnlock();
                }
            }
            
            if (newCode == null) {
                // Usually it's the new name so allow client code to refer to it as just null
                newCode = refactoring.getNewName(); // XXX isn't this == our field "newName"?
            }

            PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
            PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
            Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldCode, newCode, desc);
            diffs.add(diff);
        }
    
        /** Search for local variables in local scope */
        private void findLocal(RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name) {
            switch (node.nodeId) {
            case ARGUMENTNODE:
                // TODO - check parent and make sure it's not a method of the same name?
                // e.g. if I have "def foo(foo)" and I'm searching for "foo" (the parameter),
                // I don't want to pick up the ArgumentNode under def foo that corresponds to the
                // "foo" method name!
                if (((ArgumentNode)node).getName().equals(name)) {
                    rename(node, name, null, getString("RenameParam"));
                }
                break;
// I don't have alias nodes within a method, do I?                
//            } else if (node instanceof AliasNode) { 
//                AliasNode an = (AliasNode)node;
//                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
//                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
//                }
//                break;
            case LOCALVARNODE:
            case LOCALASGNNODE:
                if (((INameNode)node).getName().equals(name)) {
                    rename(node, name, null, getString("UpdateLocalvar"));
                }
                break;
            case DVARNODE:
            case DASGNNODE:
                 if (((INameNode)node).getName().equals(name)) {
                    // Found a method call match
                    // TODO - make a node on the same line
                    // TODO - check arity - see OccurrencesFinder
                    rename(node, name, null, getString("UpdateDynvar"));
                 }                 
                 break;
            case SYMBOLNODE:
                // XXX Can I have symbols to local variables? Try it!!!
                if (((SymbolNode)node).getName().equals(name)) {
                    rename(node, name, null, getString("UpdateSymbol"));
                }
                break;
            }

            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }
                findLocal(searchCtx, fileCtx, child, name);
            }
        }
        
        /**
         * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
         *   - I've gotta filter fields, methods etc. that are not in the current class
         *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
         * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
         */
        @SuppressWarnings("fallthrough")
        private void find(AstPath path, RubyElementCtx searchCtx, RubyElementCtx fileCtx, Node node, String name, boolean upperCase) {
            /* TODO look for both old and new and attempt to fix
             if (node instanceof AliasNode) {
                AliasNode an = (AliasNode)node;
                if (an.getNewName().equals(name) || an.getOldName().equals(name)) {
                    RubyElementCtx matchCtx = new RubyElementCtx(fileCtx, node);
                    elements.add(refactoring, WhereUsedElement.create(matchCtx));
                }
            } else*/ if (!upperCase) {
                // Local variables - I can be smarter about context searches here!
                
                // Methods, attributes, etc.
                // TODO - be more discriminating on the filetype
                switch (node.nodeId) {
                case DEFNNODE:
                case DEFSNODE: {
                    if (((MethodDefNode)node).getName().equals(name)) {
                                                
                        boolean skip = false;

                        // Check that we're in a class or module we're interested in
                        String fqn = AstUtilities.getFqnName(path);
                        if (fqn == null || fqn.length() == 0) {
                            fqn = RubyIndex.OBJECT;
                        }
                        
                        if (!fqn.equals(searchCtx.getDefClass())) {
                            // XXX THE ABOVE IS NOT RIGHT - I shouldn't
                            // use equals on the class names, I should use the
                            // index and see if one derives fromor includes the other
                            skip = true;
                        }

                        // Check arity
                        if (!skip && AstUtilities.isCall(searchCtx.getNode())) {
                            // The reference is a call and this is a definition; see if
                            // this looks like a match
                            // TODO - enforce that this method is also in the desired
                            // target class!!!
                            if (!AstUtilities.isCallFor(searchCtx.getNode(), searchCtx.getArity(), node)) {
                                skip = true;
                            }
                        } else {
                            // The search handle is a method def, as is this, with the same name.
                            // Now I need to go and see if this is an override (e.g. compatible
                            // arglist...)
                            // XXX TODO
                        }
                        
                        if (!skip) {
                            // Found a method match
                            // TODO - check arity - see OccurrencesFinder
                            node = AstUtilities.getDefNameNode((MethodDefNode)node);
                            rename(node, name, null, getString("UpdateMethodDef"));
                        }
                    }
                    break;
                }
                case FCALLNODE:
                    if (AstUtilities.isAttr(node)) {
                        SymbolNode[] symbols = AstUtilities.getAttrSymbols(node);
                        for (SymbolNode symbol : symbols) {
                            if (symbol.getName().equals(name)) {
                                // TODO - can't replace the whole node here - I need to replace only the text!
                                rename(node, name, null, null);
                            }
                        }
                    }
                    // Fall through for other call checking
                case VCALLNODE:
                case CALLNODE:
                     if (((INameNode)node).getName().equals(name)) {
                         // TODO - if it's a call without a lhs (e.g. Call.LOCAL),
                         // make sure that we're referring to the same method call
                        // Found a method call match
                        // TODO - make a node on the same line
                        // TODO - check arity - see OccurrencesFinder
                        rename(node, name, null, null);
                     }
                     break;
                case SYMBOLNODE:
                    if (((SymbolNode)node).getName().equals(name)) {
                        // TODO do something about the colon?
                        rename(node, name, null, null);
                    }
                    break;
                case GLOBALVARNODE:
                case GLOBALASGNNODE:
                case INSTVARNODE:
                case INSTASGNNODE:
                case CLASSVARNODE:
                case CLASSVARASGNNODE:
                case CLASSVARDECLNODE:
                    if (((INameNode)node).getName().equals(name)) {
                        rename(node, name, null, null);
                    }
                    break;
                }
            } else {
                // Classes, modules, constants, etc.
                switch (node.nodeId) {
                case COLON2NODE: {
                    Colon2Node c2n = (Colon2Node)node;
                    if (c2n.getName().equals(name)) {
                        rename(node, name, null, null);
                    }
                    
                    break;
                }
                case CONSTNODE:
                case CONSTDECLNODE:
                    if (((INameNode)node).getName().equals(name)) {
                        rename(node, name, null, null);
                    }
                    break;
                }
            }
            
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }
                path.descend(child);
                find(path, searchCtx, fileCtx, child, name, upperCase);
                path.ascend();
            }
        }
    
    }
    
}
