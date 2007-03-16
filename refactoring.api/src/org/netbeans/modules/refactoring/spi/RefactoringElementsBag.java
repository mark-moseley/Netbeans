/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.spi;

import java.io.IOException;
import java.util.*;
import javax.swing.text.Position;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.api.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Container holding RefactoringElements
 * @author Jan Becicka
 */
public final class RefactoringElementsBag {
    ArrayList<Transaction> commits;
    ArrayList<RefactoringElementImplementation> fileChanges;

    static {
        SPIAccessor.DEFAULT = new AccessorImpl();
    }
    
    private final List<RefactoringElementImplementation> delegate;
    private final RefactoringSession session;
    private Collection<FileObject> readOnlyFiles = new HashSet();
    
    /**
     * Creates an instance of RefactoringElementsBag
     */
    RefactoringElementsBag(RefactoringSession session, List<RefactoringElementImplementation> delegate) {
        this.session = session;
        this.delegate = delegate;
        this.commits = new ArrayList();
        this.fileChanges =  new ArrayList();
    }
    
    /**
     * Adds RefactoringElementImplementation to this bag.
     * If RefactoringElementImplementation is in read-only file - status of this element is 
     * changes to RefactoringElement.READ_ONLY
     * If RefactoringElementImplementation is in guarded block, all registered GuardedBlockHandler
     * are asked, if they can replace given RefactoringElementImplementation by it's own 
     * RefactoringElementImplementation. If there is no suitable replacement found, 
     * given element is added and it's status is set to RefactringElement.GUARDED
     * 
     * @param refactoring refactoring, which adds this RefactoringElementImplementation
     * @param el element to add
     * @return instance of Problem or null
     */
    public Problem add(AbstractRefactoring refactoring, RefactoringElementImplementation el) {
        Problem p = null;
        if (isReadOnly(el)) {
            FileObject file = el.getParentFile();
            readOnlyFiles.add(file);
            el.setEnabled(false);
            el.setStatus(el.READ_ONLY);
            delegate.add(el);
        } else if (isGuarded(el)) {
            ArrayList<RefactoringElementImplementation> proposedChanges = new ArrayList();
            ArrayList<Transaction> transactions = new ArrayList();
            for (GuardedBlockHandler gbHandler: APIAccessor.DEFAULT.getGBHandlers(refactoring)) {
                el.setEnabled(false);
                p = APIAccessor.DEFAULT.chainProblems(gbHandler.handleChange(el, proposedChanges, transactions),  p);
                
                if (p != null && p.isFatal())
                    return p;
                
                delegate.addAll(proposedChanges);
                
                for (Transaction transaction:transactions) {
                    registerTransaction(transaction);
                }
                
                if (!proposedChanges.isEmpty() || !transactions.isEmpty())
                    return p;
                
            }
            el.setEnabled(false);
            el.setStatus(el.GUARDED);
            delegate.add(el);
        } else {
            delegate.add(el);
        }
        return p;
    }
    
    /**
     * Adds all RefactringElements from given Collection using #add method
     * @param refactoring refactoring, which adds this RefactoringElement
     * @param elements Collection of RefactoringElements
     * @return instance of Problem or null
     */
    public Problem addAll(AbstractRefactoring refactoring, Collection<RefactoringElementImplementation> elements) {
	Problem p = null;
	for (RefactoringElementImplementation rei:elements) {
	    p = APIAccessor.DEFAULT.chainProblems(p, add(refactoring, rei));
            if (p!=null && p.isFatal())
                return p;
	}
        return p;
    }
    
    
    /**
     * 
     * @return RefactoringSession associated with this RefactoringElementsBag
     */
    public RefactoringSession getSession() {
        return session;
    }
    
    Collection<FileObject> getReadOnlyFiles() {
        return readOnlyFiles;
    }
    
    /**
     * commits are called after all changes are performed
     * @param commit Transaction to commit
     * @see Transaction
     * @see BackupFacilty
     */
    public void registerTransaction(Transaction commit) {
        if (APIAccessor.DEFAULT.isCommit(session))
            if (!commits.contains(commit))
                commits.add(commit);
    }
    
    
    /**
     * fileChanges are performed after all element changes
     * @param changes changes to be performed
     * @see Transaction
     * @see BackupFacilty
     */
    public Problem addFileChange(AbstractRefactoring refactoring, RefactoringElementImplementation el) {
        if (APIAccessor.DEFAULT.isCommit(session))
            fileChanges.add(el);
        return null;
    }    
    
    private boolean isReadOnly(RefactoringElementImplementation rei) {
        return !rei.getParentFile().canWrite();
    }
    
    /**
     * TODO: GuardedQuery is still missing
     * this solution has performance issues.
     */ 
    private boolean isGuarded(RefactoringElementImplementation el) {
        if (el.getPosition()==null)
            return false;
        try {
            DataObject dob = DataObject.find(el.getParentFile());
            EditorCookie e = dob.getCookie(EditorCookie.class);
            if (e!=null) {
                GuardedSectionManager manager = GuardedSectionManager.getInstance(e.openDocument());
                if (manager != null) {
                    Position elementStart = el.getPosition().getBegin().getPosition();
                    Position elementEnd = el.getPosition().getEnd().getPosition();
                    for(GuardedSection section:manager.getGuardedSections()) {
                        if (section.contains(elementStart, false) ||
                                section.contains(elementEnd, false)) {
                            return true;
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                    ex.getMessage(), ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                    ex.getMessage(), ex);
        }
        return false;
    }
}
