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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.fold;

import java.util.Collections;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/**
 *
 * @author mmetelka
 */
public class SimpleFoldManagerTest extends NbTestCase {
    
    private static final int MAX_FOLD_MEMORY_SIZE = 64;
    
    static final int FOLD_START_OFFSET_1 = 5;
    static final int FOLD_END_OFFSET_1 = 10;
    
    public SimpleFoldManagerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test the creation of several folds.
     */
    public void test() {
        FoldHierarchyTestEnv env = new FoldHierarchyTestEnv(new SimpleFoldManagerFactory());

        FoldHierarchy hierarchy = env.getHierarchy();
        AbstractDocument doc = env.getDocument();
        doc.readLock();
        try {
            hierarchy.lock();
            try {
                
                Fold rootFold = hierarchy.getRootFold();
                int foldCount = rootFold.getFoldCount();
                int expectedFoldCount = 1;
                assertTrue("Incorrect fold count " + foldCount, // NOI18N
                    (foldCount == expectedFoldCount)
                );
                
                Fold fold = rootFold.getFold(0);
                FoldType foldType = fold.getType();
                int foldStartOffset = fold.getStartOffset();
                int foldEndOffset = fold.getEndOffset();
                assertTrue("Incorrect fold type " + foldType, // NOI18N
                    (foldType == AbstractFoldManager.REGULAR_FOLD_TYPE));
                assertTrue("Incorrect fold start offset " + foldStartOffset, // NOI18N
                    (foldStartOffset == FOLD_START_OFFSET_1));
                assertTrue("Incorrect fold end offset " + foldEndOffset, // NOI18N
                    (foldEndOffset == FOLD_END_OFFSET_1));
                
                // Check fold size
                assertSize("Size of the fold " , Collections.singleton(fold), // NOI18N
                    MAX_FOLD_MEMORY_SIZE, new FoldMemoryFilter(fold));
                
            } finally {
                hierarchy.unlock();
            }
        } finally {
            doc.readUnlock();
        }
    }
    
    
    final class SimpleFoldManager extends AbstractFoldManager {
        
        public void initFolds(FoldHierarchyTransaction transaction) {
            try {
                getOperation().addToHierarchy(
                    REGULAR_FOLD_TYPE,
                    "...", // non-null to properly count fold's size (non-null desc gets set) // NOI18N
                    false,
                    FOLD_START_OFFSET_1, FOLD_END_OFFSET_1, 1, 1,
                    null,
                    transaction
                );
            } catch (BadLocationException e) {
                e.printStackTrace();
                fail();
            }
        }
        
    }

    public final class SimpleFoldManagerFactory implements FoldManagerFactory {
        
        public FoldManager createFoldManager() {
            return new SimpleFoldManager();
        }
        
    }

    private final class FoldMemoryFilter implements MemoryFilter {
        
        private Fold fold;
        
        FoldMemoryFilter(Fold fold) {
            this.fold = fold;
        }
        
        public boolean reject(Object o) {
            return (o == fold.getType())
                || (o == fold.getDescription()) // requires non-null description during construction
                || (o == fold.getParent())
                || (o instanceof FoldOperationImpl)
                || (o instanceof Position);
            
            // Will count possible FoldChildren and ExtraInfo
        }

    }

}
