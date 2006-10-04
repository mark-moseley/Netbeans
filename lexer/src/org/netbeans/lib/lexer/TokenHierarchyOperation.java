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

package org.netbeans.lib.lexer;

import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.lib.lexer.batch.CopyTextTokenList;
import org.netbeans.lib.lexer.batch.TextTokenList;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.TokenListUpdater;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.lib.lexer.inc.TokenListChange;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Token hierarchy operation services tasks of its associated token hierarchy.
 * <br/>
 * There is one-to-one relationship between token hierarchy and its operation.
 * <br/>
 * Token hierarchy may be a snapshot of an original 
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyOperation<I> { // "I" stands for input
    
    /**
     * The token hierarchy delegating to this operation.
     * <br>
     * There is one-to-one relationship between token hierarchy and its operation.
     */
    private TokenHierarchy<I> tokenHierarchy;
    
    /**
     * Mutable text input for mutable token hierarchy or null otherwise.
     */
    private MutableTextInput<I> mutableTextInput;
    
    private TokenList tokenList;
    
    /**
     * The hierarchy can be made inactive to release the tokens
     * and the memory that they consume temporarily.
     * <br>
     * By default the hierarchy is active so the tokens
     * will be created lazily for it.
     */
    private boolean active = true;

    /**
     * Primary token hierarchy for snapshot.
     */
    private TokenHierarchyOperation<I> liveTokenHierarchyOperation;
    
    /**
     * References to active snapshots.
     */
    private List<SnapshotRef> snapshotRefs;

    /**
     * Listener list solely for token change listeners.
     */
    private EventListenerList listenerList;
    
    private boolean snapshotReleased;

    /**
     * Constructor for reader as input.
     */
    public TokenHierarchyOperation(Reader inputReader,
    LanguageDescription language, Set<? extends TokenId> skipTokenIds, InputAttributes inputAttributes) {
        this.tokenList = new CopyTextTokenList(inputReader,
                language, skipTokenIds, inputAttributes);
        init();
    }

    /**
     * Constructor for character sequence as input.
     */
    public TokenHierarchyOperation(CharSequence inputText, boolean copyInputText,
    LanguageDescription language, Set<? extends TokenId> skipTokenIds, InputAttributes inputAttributes) {

        this.tokenList = copyInputText
                ? (TokenList)new CopyTextTokenList(inputText,
                        language, skipTokenIds, inputAttributes)
                : (TokenList)new TextTokenList(inputText,
                        language, skipTokenIds, inputAttributes);
        init();
    }

    /**
     * Constructor for mutable input.
     */
    public TokenHierarchyOperation(MutableTextInput<I> mutableTextInput,
    LanguageDescription language) {
        this.mutableTextInput = mutableTextInput;
        this.tokenList = new IncTokenList(mutableTextInput);
        init();
    }

    public TokenHierarchyOperation(TokenHierarchyOperation<I> liveTokenHierarchy) {
        this.liveTokenHierarchyOperation = liveTokenHierarchy;
        this.tokenList = new SnapshotTokenList(this);
        init();
    }

    private void init() {
        assert (tokenHierarchy == null);
        tokenHierarchy = LexerApiPackageAccessor.get().createTokenHierarchy(this);
        if (isMutable()) {
            listenerList = new EventListenerList();
            snapshotRefs = new ArrayList<SnapshotRef>(1);
        }
    }
    
    public TokenHierarchy<I> tokenHierarchy() {
        return tokenHierarchy;
    }
    
    public TokenList tokenList() {
        return tokenList;
    }

    public TokenList checkedTokenList() {
        checkSnapshotNotReleased();
        return tokenList();
    }

    public boolean isMutable() {
        return (mutableTextInput != null);
    }

    public MutableTextInput mutableTextInput() {
        return mutableTextInput;
    }
    
    public Object mutableInputSource() {
        return isMutable()
            ? LexerSpiPackageAccessor.get().inputSource(mutableTextInput)
            : null;
    }
    
    public void setActive(boolean active) {
        assert (isMutable());
        if (this.active != active) {
            this.active = active;
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void rebuild() {
        // TODO implement
    }
    
    public void fireTokenHierarchyChanged(TokenHierarchyEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        int listenersLength = listeners.length;
        for (int i = 1; i < listenersLength; i += 2) {
            ((TokenHierarchyListener)listeners[i]).tokenHierarchyChanged(evt);
        }
    }
    
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        if (isMutable()) {
            listenerList.add(TokenHierarchyListener.class, listener);
        }
    }
    
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        if (isMutable()) {
            listenerList.remove(TokenHierarchyListener.class, listener);
        }
    }

    public void textModified(int offset, int removedLength, CharSequence removedText, int insertedLength) {
        TokenListChange change = new TokenListChange(this, TokenHierarchyEvent.Type.TEXT_MODIFY,
                offset, removedLength, removedText, insertedLength);
        if (active) {
            IncTokenList incTokenList = (IncTokenList)tokenList;
            incTokenList.incrementModCount();
            TokenListUpdater.update(incTokenList, change);
            if (!incTokenList.isFullyLexed())
                incTokenList.refreshLexerInputOperation();
            
            synchronized (snapshotRefs) {
                for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
                    TokenHierarchyOperation op = snapshotRefs.get(i).get();
                    
                    if (op != null) {
                        ((SnapshotTokenList) op.tokenList()).update(change);
                    }
                }
            }
        } else { // not active - no changes to hierarchy
            change.noChange(tokenList);
        }
        fireTokenHierarchyChanged(LexerApiPackageAccessor.get().createTokenChangeEvent(tokenHierarchy, change));
    }
    
    public boolean isSnapshot() {
        return (liveTokenHierarchyOperation != null);
    }

    public TokenHierarchy<I> snapshotOf() {
        return (isSnapshot() ? liveTokenHierarchyOperation.tokenHierarchy() : null);
    }

    private void checkIsSnapshot() {
        if (!isSnapshot()) {
            throw new IllegalStateException("Not a snapshot");
        }
    }

    private void checkSnapshotNotReleased() {
        if (snapshotReleased) {
            throw new IllegalStateException("Snapshot already released"); // NOI18N
        }
    }

    public TokenHierarchy<I> createSnapshot() {
        if (isMutable()) {
            TokenHierarchyOperation<I> snapshot = new TokenHierarchyOperation<I>(this);
            snapshotRefs.add(new SnapshotRef(snapshot));
            return snapshot.tokenHierarchy();
        }
        return null;
    }

    public void snapshotRelease() {
        checkIsSnapshot();
        checkSnapshotNotReleased();

        snapshotReleased = true;
        if (liveTokenHierarchyOperation != null) { // only when "real" snapshot for mutable hierarchies
            // Remove the reference from the snapshots array
            liveTokenHierarchyOperation.removeSnapshot(this);
        }
    }

    public boolean isSnapshotReleased() {
        return snapshotReleased;
    }

    void removeSnapshot(SnapshotRef snapshotRef) {
        synchronized (snapshotRefs) {
            snapshotRefs.remove(snapshotRef);
        }
    }
    
    void removeSnapshot(TokenHierarchyOperation snapshot) {
        synchronized (snapshotRefs) {
            for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
                Reference ref = (Reference)snapshotRefs.get(i);
                if (ref.get() == snapshot) {
                    snapshotRefs.remove(i);
                    break;
                }
            }
        }
    }

    private int snapshotCount() {
        synchronized (snapshotRefs) {
            return snapshotRefs.size();
        }
    }

    public boolean canModifyToken(int index, AbstractToken token) {
        synchronized (snapshotRefs) {
            for (int i = snapshotCount() - 1; i >= 0; i--) {
                TokenHierarchyOperation op = snapshotRefs.get(i).get();
                
                if (op != null && ((SnapshotTokenList) op.tokenList()).canModifyToken(index, token)) {
                    return false;
                }
            }
        }
        return true;
    }

    public TokenHierarchyOperation liveTokenHierarchyOperation() {
        return liveTokenHierarchyOperation;
    }

    public int tokenOffset(AbstractToken token, TokenList tokenList, int rawOffset) {
        if (this.tokenList.getClass() == SnapshotTokenList.class) {
            if (tokenList != null) {
                return ((SnapshotTokenList)this.tokenList).tokenOffset(token, tokenList, rawOffset);
            } else { // passed tokenList is null => token removed from BranchTokenList
                return rawOffset;
            }
        } else { // not a snapshot - regular situation
            return (tokenList != null)
                    ? tokenList.childTokenOffset(rawOffset)
                    : rawOffset;
        }
    }

    public int tokenShiftStartOffset() {
        return isSnapshot() ? ((SnapshotTokenList)tokenList).tokenShiftStartOffset() : -1;
    }

    public int tokenShiftEndOffset() {
        return isSnapshot() ? ((SnapshotTokenList)tokenList).tokenShiftEndOffset() : -1;
    }

    
    private final class SnapshotRef extends WeakReference<TokenHierarchyOperation<I>> implements Runnable {
        
        SnapshotRef(TokenHierarchyOperation<I> snapshot) {
            super(snapshot, org.openide.util.Utilities.activeReferenceQueue());
        }

        public void run() {
            if (liveTokenHierarchyOperation != null) {
                // Remove the reference from the snapshots array
                liveTokenHierarchyOperation.removeSnapshot(this);
            }
        }

    }

}
