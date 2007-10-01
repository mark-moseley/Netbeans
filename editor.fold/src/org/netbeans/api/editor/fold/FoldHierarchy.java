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

package org.netbeans.api.editor.fold;

import java.util.Collection;
import java.util.Collections;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.fold.ApiPackageAccessor;
import org.netbeans.modules.editor.fold.FoldHierarchyExecution;
import org.netbeans.modules.editor.fold.FoldOperationImpl;

/**
 * Hierarchy of the folds for a single text component represents
 * a model of the code-folding.
 *
 * <br>
 * It is the main entry point into the Code Folding API.
 * <br>
 * Its instance can be obtained by {@link #get(javax.swing.text.JTextComponent)}.
 * <br>
 * The hierarhcy mainly provides access to the root fold
 * by {@link #getRootFold()}
 * and allows to expand/collapse the folds
 * and listen for fold events describing folds structure changes
 * and state changes of any of the folds in the hierarchy.
 *
 * <p>
 * Hierarchy is logically bound to view
 * i.e. {@link javax.swing.text.JTextComponent}
 * instead of the document model because
 * if there would be two views over the same document
 * then a particular fold can be collapsed in one view
 * but uncollapsed in another.
 * <br>
 * It's up to the concrete fold implementations to possibly share
 * some common information even on document model level
 * e.g. java-related folds in multiple views over
 * a single java source document can share
 * the document-level parsing information.
 * <br>
 * On the other hand user-defined folds (e.g. by collapsing caret selection)
 * will only be held for the component in which they were created.
 * 
 * <p>
 * Only one thread at the time can access the code folding hierarchy.
 * Prior working with the hierarchy a document-level lock
 * must be obtained first followed by call to {@link #render(Runnable)}
 * (or {@link #lock()} for advanced uses) which ensure that the hierarchy
 * gets locked exclusively.
 * <br>
 * The document lock can be either readlock
 * e.g. by using {@link javax.swing.text.Document#render(Runnable)}
 * or writelock
 * e.g. when in {@link javax.swing.event.DocumentListener})
 * and must be obtained on component's document
 * i.e. {@link javax.swing.text.JTextComponent#getDocument()}
 * should be used.
 *
 * <p>
 * The whole fold hierarchy related code expects that the document
 * instances of the text component will subclass
 * <code>javax.swing.text.AbstractDocument</code>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchy {

    /**
     * Fold type for the root fold.
     */
    public static final FoldType ROOT_FOLD_TYPE = new FoldType("root-fold"); // NOI18N
    
    private static boolean apiPackageAccessorRegistered;

    static {
        ensureApiAccessorRegistered();
    }
    
    private static void ensureApiAccessorRegistered() {
        if (!apiPackageAccessorRegistered) {
            apiPackageAccessorRegistered = true;
            ApiPackageAccessor.register(new ApiPackageAccessorImpl());
        }
    }
    
    /**
     * Execution carries out most of the fold hierarchy's functionality.
     */
    private FoldHierarchyExecution execution;
    
    /**
     * Get the fold hierarchy for the given component. If the hierarchy
     * does not exist yet it will get created.
     * <br>
     * The hierarchy will exist for the entire lifetime of the component.
     * It is maintained as a client property of it.
     *
     * @return non-null fold hierarchy for the component.
     */
    public static synchronized FoldHierarchy get(JTextComponent component) {
        return FoldHierarchyExecution.getOrCreateFoldHierarchy(component);
    }
    
    /** Only instances created internally are allowed. */
    private FoldHierarchy(FoldHierarchyExecution execution) {
        // Synced under FoldHierarchy.class lock
        this.execution = execution;
    }

    /**
     * Execute the given runnable over the exclusively locked hierarchy.
     * <br>
     * Prior using this method the document must be locked.
     * The document lock can be either readlock
     * e.g. by using {@link javax.swing.text.Document#render(Runnable)}
     * or writelock
     * e.g. when in {@link javax.swing.event.DocumentListener})
     * and must be obtained on component's document
     * i.e. {@link javax.swing.text.JTextComponent#getDocument()}
     * should be used.
     *
     * @param r the runnable to be executed.
     */
    public void render(Runnable r) {
        lock();
        try {
            r.run();
        } finally {
            unlock();
        }
    }
    
    /**
     * Lock the hierarchy for exclusive use. This method must only
     * be used together with {@link #unlock()} in <code>try..finally</code> block.
     * <br>
     * Prior using this method the document must be locked.
     * The document lock can be either readlock
     * e.g. by using {@link javax.swing.text.Document#render(Runnable)}
     * or writelock
     * e.g. when in {@link javax.swing.event.DocumentListener})
     * and must be obtained on component's document
     * i.e. {@link javax.swing.text.JTextComponent#getDocument()}
     * should be used.
     *
     * <p>
     * <font color="red">
     * <b>Note:</b> The clients using this method must ensure that
     * they <b>always</b> use this method in the following pattern:<pre>
     *
     *     lock();
     *     try {
     *         ...
     *     } finally {
     *         unlock();
     *     }
     * </pre>
     * </font>
     *
     * @see #render(Runnable)
     */
    public void lock() {
        execution.lock();
    }

    /**
     * Unlock the hierarchy from exclusive use. This method must only
     * be used together with {@link #lock()} in <code>try..finally</code> block.
     */
    public void unlock() {
        execution.unlock();
    }

    /**
     * Collapse the given fold.
     * <br>
     * Nothing is done if the fold is already collapsed.
     *
     * <p>
     * <b>Note:</b> The hierarchy must be locked prior using of this method.
     *
     * @param f fold to be collapsed.
     */
    public void collapse(Fold f) {
        collapse(Collections.singletonList(f));
    }
    
    /**
     * Collapse all the folds contained in the given collection.
     *
     * <p>
     * <b>Note:</b> The hierarchy must be locked prior using of this method.
     *
     * @param c collection of the {@link Fold}s to be collapsed. The folds
     *  must be present in this hierarchy.
     */
    public void collapse(Collection c) {
        execution.collapse(c);
    }
    
    /**
     * Expand the given fold.
     * <br>
     * Nothing is done if the fold is already expanded.
     *
     * <p>
     * <b>Note:</b> The hierarchy must be locked prior using of this method.
     *
     * @param f fold to be expanded.
     */
    public void expand(Fold f) {
        expand(Collections.singletonList(f));
    }
    
    /**
     * Expand all the folds contained in the given collection.
     *
     * <p>
     * <b>Note:</b> The hierarchy must be locked prior using of this method.
     *
     * @param c collection of the {@link Fold}s to be collapsed. The folds
     *  must be present in this hierarchy.
     */
    public void expand(Collection c) {
        execution.expand(c);
    }
    
    /**
     * Collapse the given fold if it's expanded and expand it if it's
     * collapsed.
     *
     * <p>
     * <b>Note:</b> The hierarchy must be locked prior using of this method.
     *
     * @param f fold which state should be toggled.
     */
    public void toggle(Fold f) {
        if (f.isCollapsed()) {
            expand(f);
        } else { // expanded
            collapse(f);
        }
    }

    /**
     * Get the text component for which this fold hierarchy was created.
     *
     * @return non-null text component for which this fold hierarchy was created.
     */
    public JTextComponent getComponent() {
        return execution.getComponent();
    }

    /**
     * Get the root fold of this hierarchy.
     *
     * @return root fold of this hierarchy.
     *   The root fold covers the whole document and is uncollapsable.
     */
    public Fold getRootFold() {
        return execution.getRootFold();
    }
    
    /**
     * Add listener for changes done in the hierarchy.
     *
     * @param l non-null listener to be added.
     */
    public void addFoldHierarchyListener(FoldHierarchyListener l) {
        execution.addFoldHierarchyListener(l);
    }
    
    /**
     * Remove previously added listener for changes done in the hierarchy.
     *
     * @param l non-null listener to be removed.
     */
    public void removeFoldHierarchyListener(FoldHierarchyListener l) {
        execution.removeFoldHierarchyListener(l);
    }

    /**
     * Get a string description of the hierarchy for debugging purposes.
     * <br>
     * Like all other methods this one can only be used under locking 
     * conditions for the hierarchy.
     */
    public String toString() {
        return execution.toString();
    }

    /**
     * Implementation of the API package accessor allows the implementation
     * to access certain package-private methods from the api classes.
     */
    private static final class ApiPackageAccessorImpl extends ApiPackageAccessor {
        
        public FoldHierarchy createFoldHierarchy(FoldHierarchyExecution execution) {
            return new FoldHierarchy(execution);
        }
        
        public Fold createFold(FoldOperationImpl operation,
        FoldType type, String description, boolean collapsed,
        Document doc, int startOffset, int endOffset,
        int startGuardedLength, int endGuardedLength,
        Object extraInfo)
        throws BadLocationException {
            return new Fold(operation, type, description, collapsed,
                doc, startOffset, endOffset,
                startGuardedLength, endGuardedLength,
                extraInfo
            );
        }
        
        public FoldHierarchyEvent createFoldHierarchyEvent(FoldHierarchy source,
        Fold[] removedFolds, Fold[] addedFolds, FoldStateChange[] foldStateChanges,
        int affectedStartOffset, int affectedEndOffset) {
            return new FoldHierarchyEvent(source, removedFolds, addedFolds,
                foldStateChanges, affectedStartOffset, affectedEndOffset);
        }
        
        public FoldStateChange createFoldStateChange(Fold fold) {
            return new FoldStateChange(fold);
        }
        
        public void foldSetParent(Fold fold, Fold parent) {
            fold.setParent(parent);
        }

        public void foldExtractToChildren(Fold fold, int index, int length, Fold targetFold) {
            fold.extractToChildren(index, length, targetFold);
        }

        public Fold foldReplaceByChildren(Fold fold, int index) {
            return fold.replaceByChildren(index);
        }

        public void foldSetCollapsed(Fold fold, boolean collapsed) {
            fold.setCollapsed(collapsed);
        }
        
        public void foldSetDescription(Fold fold, String description) {
            fold.setDescription(description);
        }
        
        public void foldSetStartOffset(Fold fold, Document doc, int startOffset)
        throws BadLocationException {
            fold.setStartOffset(doc, startOffset);
        }
        
        public void foldSetEndOffset(Fold fold, Document doc, int endOffset)
        throws BadLocationException {
            fold.setEndOffset(doc, endOffset);
        }
        
        public boolean foldIsStartDamaged(Fold fold) {
            return fold.isStartDamaged();
        }

        public boolean foldIsEndDamaged(Fold fold) {
            return fold.isEndDamaged();
        }

        public boolean foldIsExpandNecessary(Fold fold) {
            return fold.isExpandNecessary();
        }

        public void foldInsertUpdate(Fold fold, DocumentEvent evt) {
            fold.insertUpdate(evt);
        }
    
        public void foldRemoveUpdate(Fold fold, DocumentEvent evt) {
            fold.removeUpdate(evt);
        }

        public FoldOperationImpl foldGetOperation(Fold fold) {
            return fold.getOperation();
        }
    
        public int foldGetRawIndex(Fold fold) {
            return fold.getRawIndex();
        }
        
        public void foldSetRawIndex(Fold fold, int rawIndex) {
            fold.setRawIndex(rawIndex);
        }
        
        public void foldUpdateRawIndex(Fold fold, int rawIndexDelta) {
            fold.updateRawIndex(rawIndexDelta);
        }
        
        public Object foldGetExtraInfo(Fold fold) {
            return fold.getExtraInfo();
        }
        
        public void foldStateChangeCollapsedChanged(FoldStateChange fsc) {
            fsc.collapsedChanged();
        }
        
        public void foldStateChangeDescriptionChanged(FoldStateChange fsc) {
            fsc.descriptionChanged();
        }
        
        public void foldStateChangeStartOffsetChanged(FoldStateChange fsc,
        int originalStartOffset) {
            fsc.startOffsetChanged(originalStartOffset);
        }
        
        public void foldStateChangeEndOffsetChanged(FoldStateChange fsc,
        int originalEndOffset) {
            fsc.endOffsetChanged(originalEndOffset);
        }
        
    }

}
