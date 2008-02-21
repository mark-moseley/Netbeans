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
package org.netbeans.modules.gsfret.editor.fold;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.fpi.gsf.ParserResult;
import org.netbeans.fpi.gsf.StructureScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsfret.editor.semantic.ScanningCancellableTask;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.modules.gsf.GsfEditorOptionsFactory;
import org.netbeans.modules.gsf.GsfOptions;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.openide.loaders.DataObject;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * 
 * Copied from both JavaFoldManager and JavaElementFoldManager
 * 
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class GsfFoldManager implements FoldManager {
    public static final FoldType CODE_BLOCK_FOLD_TYPE = new FoldType("code-block"); // NOI18N
    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = new FoldType("initial-comment"); // NOI18N
    public static final FoldType IMPORTS_FOLD_TYPE = new FoldType("imports"); // NOI18N
    public static final FoldType JAVADOC_FOLD_TYPE = new FoldType("javadoc"); // NOI18N
    
    private static final String IMPORTS_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String JAVADOC_FOLD_DESCRIPTION = "..."; // NOI18N
    
    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N

    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
        = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);
    
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    public static final FoldTemplate IMPORTS_FOLD_TEMPLATE
        = new FoldTemplate(IMPORTS_FOLD_TYPE, IMPORTS_FOLD_DESCRIPTION, 0, 0);

    public static final FoldTemplate JAVADOC_FOLD_TEMPLATE
        = new FoldTemplate(JAVADOC_FOLD_TYPE, JAVADOC_FOLD_DESCRIPTION, 3, 2);

    
    protected static final class FoldTemplate {

        private FoldType type;

        private String description;

        private int startGuardedLength;

        private int endGuardedLength;

        protected FoldTemplate(FoldType type, String description,
        int startGuardedLength, int endGuardedLength) {
            this.type = type;
            this.description = description;
            this.startGuardedLength = startGuardedLength;
            this.endGuardedLength = endGuardedLength;
        }

        public FoldType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public int getStartGuardedLength() {
            return startGuardedLength;
        }

        public int getEndGuardedLength() {
            return endGuardedLength;
        }

    }
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    
    // Folding presets
    private boolean foldImportsPreset;
    private boolean foldInnerClassesPreset;
    private boolean foldJavadocsPreset;
    private boolean foldCodeBlocksPreset;
    private boolean foldInitialCommentsPreset;
    
    /** Creates a new instance of GsfFoldManager */
    public GsfFoldManager() {
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
        
        settingsChange(null);
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od != null) {
            currentFolds = new HashMap<FoldInfo, Fold>();
            task = JavaElementFoldTask.getTask(od.getPrimaryFile());
            task.setGsfFoldManager(GsfFoldManager.this);
        }
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
        currentFolds.remove(operation.getExtraInfo(damagedFold));
        if (importsFold == damagedFold) {
            importsFold = null;//not sure if this is correct...
        }
        if (initialCommentFold == damagedFold) {
            initialCommentFold = null;//not sure if this is correct...
        }
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (task != null) {
            task.setGsfFoldManager(null);
        }
        
        task         = null;
        file         = null;
        currentFolds = null;
        importsFold  = null;
        initialCommentFold = null;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        // Get folding presets
        foldInitialCommentsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
        foldImportsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_IMPORT);
        foldCodeBlocksPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_METHOD);
        foldInnerClassesPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_INNERCLASS);
        foldJavadocsPreset = getSetting(GsfOptions.CODE_FOLDING_COLLAPSE_JAVADOC);
    }
    
    private boolean getSetting(String settingName){
        JTextComponent tc = operation.getHierarchy().getComponent();
        return SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(tc), settingName, false);
    }
    
    static final class JavaElementFoldTask extends ScanningCancellableTask<CompilationInfo> {
        
        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static Map<FileObject, JavaElementFoldTask> file2Task = new WeakHashMap<FileObject, JavaElementFoldTask>();
        
        static JavaElementFoldTask getTask(FileObject file) {
            JavaElementFoldTask task = file2Task.get(file);
            
            if (task == null) {
                file2Task.put(file, task = new JavaElementFoldTask());
            }
            
            return task;
        }
        
        private Reference<GsfFoldManager> manager;
        
        synchronized void setGsfFoldManager(GsfFoldManager manager) {
            this.manager = new WeakReference<GsfFoldManager>(manager);
        }
        
        public void run(final CompilationInfo info) {
            resume();
            
            GsfFoldManager manager;
            
            //the synchronized section should be as limited as possible here
            //in particular, "scan" should not be called in the synchronized section
            //or a deadlock could appear: sy(this)+document read lock against
            //document write lock and this.cancel/sy(this)
            synchronized (this) {
                manager = this.manager != null ? this.manager.get() : null;
            }
            
            if (manager == null) {
                return;
            }
            
            long startTime = System.currentTimeMillis();

            List<FoldInfo> folds = new ArrayList();
            boolean success = gsfFoldScan(manager, info, folds);
            if (!success || isCancelled()) {
                return;
            }
            
            SwingUtilities.invokeLater(manager.new CommitFolds(folds));
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 1",
                    new Object[] {info.getFileObject(), endTime - startTime});
        }
        
        /**
         * Ask the language plugin to scan for folds. 
         * 
         * @return true If folds were found, false if cancelled
         */
        private boolean gsfFoldScan(GsfFoldManager manager, CompilationInfo info, List<FoldInfo> folds) {
            BaseDocument doc = null;
            try {
                doc = (BaseDocument)info.getDocument();
            } catch (IOException ioe) {
                org.openide.ErrorManager.getDefault().notify(ioe);
            }
            if (doc == null) {
                return false;
            }
            
            Set<String> mimeTypes = info.getEmbeddedMimeTypes();
            for (String mimeType : mimeTypes) {
                Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                if (language != null) {
                    scan(manager, info, folds, doc, language);
                }
            }

            if (isCancelled()) {
                return false;
            }

            //check for initial fold:
            boolean success = checkInitialFold(manager, info, folds);
            
            return success;
        }

        private boolean checkInitialFold(GsfFoldManager manager, CompilationInfo info, List<FoldInfo> folds) {
            try {
                TokenHierarchy<?> th = info.getTokenHierarchy();
                TokenSequence<?> ts = th.tokenSequence();
                
                while (ts.moveNext()) {
                    Token<?> token = ts.token();
                    
                    String category = token.id().primaryCategory();
                    if ("comment".equals(category)) { // NOI18N
                        Document doc   = manager.operation.getHierarchy().getComponent().getDocument();
                        int startOffset = ts.offset();
                        int endOffset =  startOffset + token.length();
                        boolean collapsed = manager.foldInitialCommentsPreset;
                        
                        if (manager.initialCommentFold != null) {
                            collapsed = manager.initialCommentFold.isCollapsed();
                        }
                        
                        // Find end - could be a block of single-line statements
                        
                        while (ts.moveNext()) {
                            token = ts.token();
                            category = token.id().primaryCategory();
                            if ("comment".equals(category)) { // NOI18N
                                endOffset =  ts.offset() + token.length();
                            } else if (!"whitespace".equals(category)) { // NOI18N
                                break;
                            }
                        }

                        try {
                            // Start the fold at the END of the line
                            startOffset = org.netbeans.editor.Utilities.getRowEnd((BaseDocument)doc, startOffset);
                            if (startOffset >= endOffset) {
                                return true;
                            }
                        } catch (BadLocationException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                        
                        folds.add(new FoldInfo(doc, startOffset, endOffset, INITIAL_COMMENT_FOLD_TEMPLATE, collapsed));
                        
                        return true;
                    }
                    
                    if (!"whitespace".equals(category)) { // NOI18N
                        break;
                    }
                }
            } catch (BadLocationException e) {
                //the document probably changed, stop
                return false;
            } catch (ConcurrentModificationException e) {
                //from TokenSequence, document probably changed, stop
                return false;
            }
            
            return true;
        }
        
        private void scan(GsfFoldManager manager, CompilationInfo info, List<FoldInfo> folds, BaseDocument doc, Language language) {
            addTree(manager, folds, info, doc, language);
        }
        
        private void addTree(GsfFoldManager manager, List<FoldInfo> result, CompilationInfo info,
           BaseDocument doc, Language language) {
            StructureScanner scanner = language.getStructure();
            if (scanner != null) {
                Map<String,List<OffsetRange>> folds = scanner.folds(info);
                if (isCancelled()) {
                    return;
                }
                List<OffsetRange> ranges = folds.get("codeblocks");
                if (ranges != null) {
                    for (OffsetRange range : ranges) {
                        addFold(range, result, doc, manager.foldCodeBlocksPreset, CODE_BLOCK_FOLD_TEMPLATE);
                    }
                }
                ranges = folds.get("comments");
                if (ranges != null) {
                    for (OffsetRange range : ranges) {
                        addFold(range, result, doc, manager.foldInitialCommentsPreset, JAVADOC_FOLD_TEMPLATE);
                    }
                }
                ranges = folds.get("initial-comment");
                if (ranges != null) {
                    for (OffsetRange range : ranges) {
                        addFold(range, result, doc, manager.foldInitialCommentsPreset, INITIAL_COMMENT_FOLD_TEMPLATE);
                    }
                }
                ranges = folds.get("imports");
                if (ranges != null) {
                    for (OffsetRange range : ranges) {
                        addFold(range, result, doc, manager.foldInitialCommentsPreset, IMPORTS_FOLD_TEMPLATE);
                    }
                }
            }
        }
        
        private void addFold(OffsetRange range, List<FoldInfo> folds, BaseDocument doc, 
                boolean collapseByDefault, FoldTemplate template) {
            if (range != OffsetRange.NONE) {
                int start = range.getStart();
                int end = range.getEnd();
                if (start != (-1) && end != (-1) && end <= doc.getLength()) {
                    try {
                        folds.add(new FoldInfo(doc, start, end, template, collapseByDefault));
                    } catch (BadLocationException ble) {
                        org.openide.ErrorManager.getDefault().notify(ble);
                    }
                }
            }
        }

    }
    
    private class CommitFolds implements Runnable {
        
        private boolean insideRender;
        private List<FoldInfo> infos;
        private long startTime;
        
        public CommitFolds(List<FoldInfo> infos) {
            this.infos = infos;
        }
        
        public void run() {
            Document document = operation.getHierarchy().getComponent().getDocument();
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                document.render(this);
                
                return;
            }
            
            operation.getHierarchy().lock();
            
            try {
                FoldHierarchyTransaction tr = operation.openTransaction();
                
                try {
                    if (currentFolds == null) {
                        return;
                    }
                    
                    Map<FoldInfo, Fold> added   = new TreeMap<FoldInfo, Fold>();
                    List<FoldInfo>      removed = new ArrayList<FoldInfo>(currentFolds.keySet());
                    int documentLength = document.getLength();
                    
                    for (FoldInfo i : infos) {
                        if (removed.remove(i)) {
                            continue ;
                        }
                        
                        int start = i.start.getOffset();
                        int end   = i.end.getOffset();
                        
                        if (end > documentLength) {
                            continue;
                        }
                        
                        if (end > start && (end - start) > (i.template.getStartGuardedLength() + i.template.getEndGuardedLength())) {
                            Fold f    = operation.addToHierarchy(i.template.getType(),
                                                                 i.template.getDescription(),
                                                                 i.collapseByDefault,
                                                                 start,
                                                                 end,
                                                                 i.template.getStartGuardedLength(),
                                                                 i.template.getEndGuardedLength(),
                                                                 i,
                                                                 tr);
                            
                            added.put(i, f);
                            
                            if (i.template == IMPORTS_FOLD_TEMPLATE) {
                                importsFold = f;
                            }
                            if (i.template == INITIAL_COMMENT_FOLD_TEMPLATE) {
                                initialCommentFold = f;
                            }
                        }
                    }
                    
                    for (FoldInfo i : removed) {
                        Fold f = currentFolds.remove(i);
                        
                        operation.removeFromHierarchy(f, tr);
                        
                        if (importsFold == f ) {
                            importsFold = null;
                        }
                        
                        if (initialCommentFold == f) {
                            initialCommentFold = f;
                        }
                    }
                    
                    currentFolds.putAll(added);
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                    tr.commit();
                }
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }
    
    private Map<FoldInfo, Fold> currentFolds;
    private Fold initialCommentFold;
    private Fold importsFold;
    
    protected static final class FoldInfo implements Comparable {
        
        private Position start;
        private Position end;
        private FoldTemplate template;
        private boolean collapseByDefault;
        
        public FoldInfo(Document doc, int start, int end, FoldTemplate template, boolean collapseByDefault) throws BadLocationException {
            this.start = doc.createPosition(start);
            this.end   = doc.createPosition(end);
            this.template = template;
            this.collapseByDefault = collapseByDefault;
        }
        
        @Override
        public int hashCode() {
            return 1;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FoldInfo)) {
                return false;
            }
            
            return compareTo(o) == 0;
        }
        
        public int compareTo(Object o) {
            FoldInfo remote = (FoldInfo) o;
            
            if (start.getOffset() < remote.start.getOffset()) {
                return -1;
            }
            
            if (start.getOffset() > remote.start.getOffset()) {
                return 1;
            }
            
            if (end.getOffset() < remote.end.getOffset()) {
                return -1;
            }
            
            if (end.getOffset() > remote.end.getOffset()) {
                return 1;
            }
            
            return 0;
        }
    }
}
