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

package org.netbeans.lib.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.TokenHierarchyUpdate;
import org.netbeans.lib.lexer.inc.TokenListChange;

/**
 * Token hierarchy operation services tasks of its associated token hierarchy.
 * <br/>
 * There is one-to-one relationship between token hierarchy and its operation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyOperation<I, T extends TokenId> { // "I" stands for input

    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyOperation.level=FINE
    static final Logger LOG = Logger.getLogger(TokenHierarchyOperation.class.getName());
    
    // -J-Dorg.netbeans.spi.lexer.MutableTextInput.level=FINE
    private static final Logger LOG_LOCK = Logger.getLogger(MutableTextInput.class.getName()); // Logger for read/write-lock

    /**
     * Input source of this token hierarchy.
     */
    private final I inputSource;
    
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

    /**
     * Root token list of this hierarchy. It is created in constructor and never changed
     * during the whole lifetime of the token hierarchy.
     */
    private final TokenList<T> rootTokenList;
    
    /**
     * The hierarchy can be made inactive to release the tokens
     * and the memory that they consume temporarily.
     * <br>
     * By default the hierarchy is active for immutable inputs and unitialized
     * for mutable inputs (will become active upon first ask for TH.tokenSequence()
     * when MTI.language() will provide a valid language).
     */
    private Activity activity;

    /**
     * Primary token hierarchy for snapshot.
     */
//    private TokenHierarchyOperation<I,T> liveTokenHierarchyOperation;
    
    /**
     * References to active snapshots.
     */
//    private List<SnapshotRef> snapshotRefs;

    /**
     * Listener list solely for token change listeners.
     */
    private EventListenerList listenerList;
    
//    private boolean snapshotReleased;
    
    private Set<LanguagePath> languagePaths;
    
    private Set<Language<?>> exploredLanguages;

    /**
     * Mapping of language path to token list lists.
     * <br/>
     * If a token list list is contained then all its parents
     * with the shorter language path are also mandatorily maintained.
     */
    private Map<LanguagePath,TokenListList<?>> path2tokenListList;
    
    private Set<Language<?>> rootChildrenLanguages;
    
    private int maxTokenListListPathSize;
    

    /**
     * Constructor for reader as input.
     */
    public TokenHierarchyOperation(Reader inputReader,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        if (inputReader == null)
            throw new IllegalArgumentException("inputReader cannot be null"); // NOI18N
        if (language == null)
            throw new IllegalArgumentException("language cannot be null");

        @SuppressWarnings("unchecked")
        I input = (I)inputReader;
        this.inputSource = input;

        // Instead of using an original CopyTextTokenList that allowed to skip
        // individual characters of all flyweight tokens do just a copy of all chars
        // from the Reader. TBD - do a lazy reading instead of pre-reading.
        char[] chars = new char[LexerUtilsConstants.READER_TEXT_BUFFER_SIZE];
        int offset = 0;
        try {
            while (true) {
                int readLen = inputReader.read(chars, offset, chars.length - offset);
                if (readLen == -1) // End of stream
                    break;
                offset += readLen;
                if (offset == chars.length) { // Full buffer
                    chars = ArrayUtilities.charArray(chars); // Double array size
                }
            }
        } catch (IOException e) {
            // Ignored silently - there should be a wrapping reader catching and properly handling
            // this IOException.
        } finally {
            // Attempt to close the Reader
            try {
                inputReader.close();
            } catch (IOException e) {
                // Ignored silently - there should be a wrapping reader catching and properly handling
                // this IOException.
            }
        }
        String inputText = new String(chars, 0, offset); // Copy of reader's whole text

        this.rootTokenList = new BatchTokenList<T>(this, inputText,
                language, skipTokenIds, inputAttributes);
        init();
        activity = Activity.ACTIVE;
    }

    /**
     * Constructor for character sequence as input.
     */
    public TokenHierarchyOperation(CharSequence inputText, boolean copyInputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        if (inputText == null)
            throw new IllegalArgumentException("inputText cannot be null"); // NOI18N
        if (language == null)
            throw new IllegalArgumentException("language cannot be null");

        @SuppressWarnings("unchecked")
        I input = (I)inputText;
        this.inputSource = input;
        if (copyInputText) {
            // Instead of using an original CopyTextTokenList (that allowed to skip
            // individual characters of all flyweight tokens) do just a copy of the full text
            // and use regular BatchTokenList.
            inputText = inputText.toString();
        }
        this.rootTokenList = new BatchTokenList<T>(this, inputText,
                        language, skipTokenIds, inputAttributes);
        init();
        activity = Activity.ACTIVE;
    }

    /**
     * Constructor for mutable input.
     */
    public TokenHierarchyOperation(MutableTextInput<I> mutableTextInput) {
        this.inputSource = LexerSpiPackageAccessor.get().inputSource(mutableTextInput);
        this.mutableTextInput = mutableTextInput;
        this.rootTokenList = new IncTokenList<T>(this);
        init();
        activity = Activity.NOT_INITED;
    }

    private void init() {
        assert (tokenHierarchy == null);
        tokenHierarchy = LexerApiPackageAccessor.get().createTokenHierarchy(this);
        // Create listener list even for non-mutable hierarchies as there may be
        // custom embeddings created that need to be notified
        listenerList = new EventListenerList();
        rootChildrenLanguages = Collections.emptySet();
    }
    
    public TokenHierarchy<I> tokenHierarchy() {
        return tokenHierarchy;
    }
    
    public TokenList<T> rootTokenList() {
        return rootTokenList;
    }

    public int modCount() {
        return rootTokenList.modCount();
    }

    public boolean isMutable() {
        return (mutableTextInput != null);
    }

    public MutableTextInput mutableTextInput() {
        return mutableTextInput;
    }
    
    public I inputSource() {
        return inputSource;
    }
    
    public CharSequence text() {
        if (mutableTextInput != null) {
            return LexerSpiPackageAccessor.get().text(mutableTextInput);
        }
        return null;
    }
    
    public void setActive(boolean active) {
        ensureWriteLocked();
        synchronized (rootTokenList) {
            setActiveImpl(active);
        }
    }

    public void setActiveImpl(boolean active) {
        assert (isMutable()) : "Activity changes only allowed for mutable input sources";
        // Check whether the state has changed
        Activity newActivity = active ? Activity.ACTIVE : Activity.INACTIVE;
        if (activity != newActivity) {
            IncTokenList<T> incTokenList = (IncTokenList<T>)rootTokenList;
            boolean doFire = (listenerList.getListenerCount() > 0);
            TokenListChange<T> change;
            if (active) { // Wishing to be active
                if (incTokenList.updateLanguagePath()) {
                    incTokenList.reinit(); // Initialize lazy lexing
                    change = doFire ? new TokenListChange<T>(incTokenList) : null;
                } else { // No valid top language => no change in activity
                    return;
                }
            } else { // Wishing to be inactive
                change = TokenListChange.createRebuildChange(incTokenList);
                incTokenList.replaceTokens(change, 0);
                incTokenList.setLanguagePath(null);
                incTokenList.reinit();
            }

            if (activity != Activity.NOT_INITED) { // Increase modCount if not doing init
                incTokenList.incrementModCount();
            }
            activity = newActivity;
            if (doFire) { // Only if there are already listeners
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Firing ACTIVITY change to " + listenerList.getListenerCount() + " listeners: " + activity); // NOI18N
                }
                TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                        this, TokenHierarchyEventType.ACTIVITY, 0, 0, "", 0);
                CharSequence text = LexerSpiPackageAccessor.get().text(mutableTextInput);
                eventInfo.setMaxAffectedEndOffset(text.length());
                if (activity == Activity.INACTIVE) { // Notify the tokens being removed
                    eventInfo.setTokenChangeInfo(change.tokenChangeInfo());
                    invalidatePath2TokenListList();
                }
                fireTokenHierarchyChanged(eventInfo);
            }
        }
    }

    private void invalidatePath2TokenListList() {
        path2tokenListList = null; // Drop all token list lists
        rootChildrenLanguages = Collections.emptySet();
        maxTokenListListPathSize = 0;
    }

    /**
     * Check whether the hierarchy is active doing initialization (an attempt to activate the hierarchy)
     * if it's not active yet (and it was not set to be inactive).
     *
     * @return true if the hierarchy is currently active or false otherwise.
     */
    public boolean isActive() {
        ensureReadLocked();
        synchronized (rootTokenList) {
            return isActiveImpl();
        }
    }
    
    public boolean isActiveImpl() {
        // Activate if possible
        if (activity == Activity.NOT_INITED) {
            setActiveImpl(true); // Attempt to activate
        }
        return isActiveNoInit();
    }
    
    public boolean isActiveNoInit() { // BTW used by tests to check if hierarchy is active or not
        return (activity == Activity.ACTIVE);
    }
    
    public void ensureReadLocked() {
        if (isMutable() && LOG_LOCK.isLoggable(Level.FINE) &&
                !LexerSpiPackageAccessor.get().isReadLocked(mutableTextInput)
        ) { // Not read-locked
            LOG_LOCK.log(Level.INFO, "!!WARNING!! Missing READ-LOCK of input source "
                    + LexerSpiPackageAccessor.get().inputSource(mutableTextInput),
                    new Exception());
        }
    }
    
    public void ensureWriteLocked() {
        if (isMutable() && LOG_LOCK.isLoggable(Level.FINE) &&
                !LexerSpiPackageAccessor.get().isWriteLocked(mutableTextInput)
        ) { // Not write-locked
            LOG_LOCK.log(Level.INFO, "!!WARNING!! Missing WRITE-LOCK of input source "
                    + LexerSpiPackageAccessor.get().inputSource(mutableTextInput),
                    new Exception());
        }
    }
    
    public TokenSequence<T> tokenSequence() {
        return tokenSequence(null);
    }
    
    public TokenSequence<T> tokenSequence(Language<?> language) {
        ensureReadLocked();
        synchronized (rootTokenList) {
            return (isActiveImpl() && (language == null || rootTokenList.languagePath().topLanguage() == language))
                    ? LexerApiPackageAccessor.get().createTokenSequence(rootTokenList)
                    : null;
        }
    }
    
    public List<TokenSequence<?>> tokenSequenceList(
    LanguagePath languagePath, int startOffset, int endOffset) {
        if (languagePath == null)
            throw new IllegalArgumentException("languagePath cannot be null"); // NOI18N
        ensureReadLocked();
        synchronized (rootTokenList) {
            return isActiveImpl()
                ? new TokenSequenceList(rootTokenList, languagePath, startOffset, endOffset)
                : null;
        }
    }

    /**
     * Get the token list list for the given language path.
     * <br/>
     * If the list needs to be created or it was non-mandatory.
     */
    public <ET extends TokenId> TokenListList<ET> tokenListList(LanguagePath languagePath) {
        assert isActiveNoInit() : "Token hierarchy expected to be active.";
        @SuppressWarnings("unchecked")
        TokenListList<ET> tll = (TokenListList<ET>) path2tokenListList().get(languagePath);
        if (tll == null) {
            tll = new TokenListList<ET>(rootTokenList, languagePath);
            path2tokenListList.put(languagePath, tll);
            maxTokenListListPathSize = Math.max(languagePath.size(), maxTokenListListPathSize);
            // Also create parent token list lists if they don't exist yet
            Language<?> innerLanguage = languagePath.innerLanguage();
            if (languagePath.size() >= 3) { // Top-level token list list not maintained
                tokenListList(languagePath.parent()).notifyChildAdded(innerLanguage);
            } else {
                assert (languagePath.size() == 2);
                assert (languagePath.parent() == rootTokenList.languagePath());
                if (rootChildrenLanguages.size() == 0)
                    rootChildrenLanguages = new HashSet<Language<?>>();
                boolean added = rootChildrenLanguages.add(innerLanguage);
                assert (added) : "Language " + innerLanguage + " already contained: " + rootChildrenLanguages; // NOI18N
            }
        }
        return tll;
    }
    
    /**
     * Get existing token list list or null if the TLL does not exist yet.
     */
    public <ET extends TokenId> TokenListList<ET> existingTokenListList(LanguagePath languagePath) {
        synchronized (rootTokenList()) {
            @SuppressWarnings("unchecked")
            TokenListList<ET> tll = (path2tokenListList != null) 
                    ? (TokenListList<ET>) path2tokenListList.get(languagePath)
                    : null;
            return tll;
        }
    }

    public Set<Language<?>> rootChildrenLanguages() {
        return rootChildrenLanguages;
    }

    private Map<LanguagePath,TokenListList<?>> path2tokenListList() {
        if (path2tokenListList == null) {
            path2tokenListList = new HashMap<LanguagePath,TokenListList<?>>(4, 0.5f);
        }
        return path2tokenListList;
    }
    
    public int maxTokenListListPathSize() {
        return maxTokenListListPathSize;
    }

    public void rebuild() {
        ensureWriteLocked();
        synchronized (rootTokenList) {
            if (isActiveNoInit()) {
                IncTokenList<T> incTokenList = (IncTokenList<T>)rootTokenList;
                incTokenList.incrementModCount();
                CharSequence text = LexerSpiPackageAccessor.get().text(mutableTextInput);
                TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                    this, TokenHierarchyEventType.REBUILD, 0, 0, "", 0);
                TokenListChange<T> change = TokenListChange.createRebuildChange(incTokenList);
                incTokenList.replaceTokens(change, 0);
                incTokenList.reinit(); // Will relex tokens lazily

                eventInfo.setTokenChangeInfo(change.tokenChangeInfo());
                eventInfo.setMaxAffectedEndOffset(text.length());

                invalidatePath2TokenListList();
                fireTokenHierarchyChanged(eventInfo);
            } // not active - no changes fired
        }
    }
    
    public void fireTokenHierarchyChanged(TokenHierarchyEventInfo eventInfo) {
        TokenHierarchyEvent evt = LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo);
        Object[] listeners = listenerList.getListenerList();
        int listenersLength = listeners.length;
        for (int i = 1; i < listenersLength; i += 2) {
            ((TokenHierarchyListener)listeners[i]).tokenHierarchyChanged(evt);
        }
    }
    
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.add(TokenHierarchyListener.class, listener);
    }
    
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.remove(TokenHierarchyListener.class, listener);
    }
    
    public void textModified(int offset, int removedLength, CharSequence removedText, int insertedLength) {
        ensureWriteLocked();
        // Attempt to activate the hierarchy in case there are active listeners
        boolean active = isActiveNoInit();
        if (!active && listenerList.getListenerCount() > 0) {
            active = isActive(); // Attempt to activate the hierarchy
        }
        if (active) {
            TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                    this, TokenHierarchyEventType.MODIFICATION,
                    offset, removedLength, removedText, insertedLength);
            new TokenHierarchyUpdate(eventInfo).update();
            fireTokenHierarchyChanged(eventInfo);
        }
    }

    public Set<LanguagePath> languagePaths() {
        ensureReadLocked();
        Set<LanguagePath> lps;
        synchronized (rootTokenList) {
            lps = languagePaths;
            if (lps == null) {
                if (!isActiveImpl())
                    return Collections.emptySet();
                LanguagePath lp = rootTokenList.languagePath();
                Language<?> lang = lp.topLanguage();
                LanguageOperation<?> langOp = LexerApiPackageAccessor.get().languageOperation(lang);
                @SuppressWarnings("unchecked")
                Set<LanguagePath> clps = (Set<LanguagePath>)
                        ((HashSet<LanguagePath>)langOp.languagePaths()).clone();
                lps = clps;

                @SuppressWarnings("unchecked")
                Set<Language<?>> cel = (Set<Language<?>>)
                        ((HashSet<Language<?>>)langOp.exploredLanguages()).clone();
                exploredLanguages = cel;
                languagePaths = lps;
            }
        }
        return lps;
    }
    
    public void addLanguagePath(LanguagePath lp) {
        Set<LanguagePath> elps = languagePaths(); // init if not inited yet
        if (!elps.contains(lp)) {
            // Add the new language path
            Set<LanguagePath> lps = new HashSet<LanguagePath>();
            LanguageOperation.findLanguagePaths(elps, lps, exploredLanguages, lp);
            elps.addAll(lps);
            // Fire the token hierarchy change event
        }
    }
            
//    public boolean isSnapshot() {
//        return (liveTokenHierarchyOperation != null);
//    }
//
//    public TokenHierarchy<I> snapshotOf() {
//        return (isSnapshot() ? liveTokenHierarchyOperation.tokenHierarchy() : null);
//    }
//
//    private void checkIsSnapshot() {
//        if (!isSnapshot()) {
//            throw new IllegalStateException("Not a snapshot");    
//        }
//    }
//
//    private void checkSnapshotNotReleased() {
//        if (snapshotReleased) {
//            throw new IllegalStateException("Snapshot already released"); // NOI18N
//        }
//    }
//
//    public TokenHierarchy<I> createSnapshot() {
//        if (isMutable()) {
//            TokenHierarchyOperation<I,T> snapshot = new TokenHierarchyOperation<I,T>(this);
//            snapshotRefs.add(new SnapshotRef(snapshot));
//            return snapshot.tokenHierarchy();
//        }
//        return null;
//    }
//
//    public void snapshotRelease() {
//        checkIsSnapshot();
//        checkSnapshotNotReleased();
//
//        snapshotReleased = true;
//        if (liveTokenHierarchyOperation != null) { // only when "real" snapshot for mutable hierarchies
//            // Remove the reference from the snapshots array
//            liveTokenHierarchyOperation.removeSnapshot(this);
//        }
//    }
//
//    public boolean isSnapshotReleased() {
//        return snapshotReleased;
//    }
//
//    void removeSnapshot(SnapshotRef snapshotRef) {
//        synchronized (snapshotRefs) {
//            snapshotRefs.remove(snapshotRef);
//        }
//    }
//    
//    void removeSnapshot(TokenHierarchyOperation<I,T> snapshot) {
//        synchronized (snapshotRefs) {
//            for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
//                Reference ref = (Reference)snapshotRefs.get(i);
//                if (ref.get() == snapshot) {
//                    snapshotRefs.remove(i);
//                    break;
//                }
//            }
//        }
//    }
//
//    private int snapshotCount() {
//        synchronized (snapshotRefs) {
//            return snapshotRefs.size();
//        }
//    }
//
//    public boolean canModifyToken(int index, AbstractToken token) {
//        synchronized (snapshotRefs) {
//            for (int i = snapshotCount() - 1; i >= 0; i--) {
//                TokenHierarchyOperation op = snapshotRefs.get(i).get();
//                
//                if (op != null && ((SnapshotTokenList) op.rootTokenList()).canModifyToken(index, token)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    public TokenHierarchyOperation<I,T> liveTokenHierarchyOperation() {
//        return liveTokenHierarchyOperation;
//    }
//
//    public <TT extends TokenId> int tokenOffset(AbstractToken<TT> token, TokenList<TT> tokenList, int rawOffset) {
//        if (this.rootTokenList.getClass() == SnapshotTokenList.class) {
//            if (tokenList != null) {
//                @SuppressWarnings("unchecked")
//                SnapshotTokenList<TT> tlUC = (SnapshotTokenList<TT>)this.rootTokenList;
//                return tlUC.tokenOffset(token, tokenList, rawOffset);
//            } else { // passed tokenList is null => token removed from EmbeddedTokenList
//                return rawOffset;
//            }
//        } else { // not a snapshot - regular situation
//            return (tokenList != null)
//                    ? tokenList.childTokenOffset(rawOffset)
//                    : rawOffset;
//        }
//    }
//
//    public int tokenShiftStartOffset() {
//        return isSnapshot() ? ((SnapshotTokenList)rootTokenList).tokenShiftStartOffset() : -1;
//    }
//
//    public int tokenShiftEndOffset() {
//        return isSnapshot() ? ((SnapshotTokenList)rootTokenList).tokenShiftEndOffset() : -1;
//    }
//    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TOKEN HIERARCHY"); // NOI18N
        if (inputSource() != null) {
            sb.append(" for " + inputSource());
        }
        if (!isActive()) {
            sb.append(" is NOT ACTIVE.");
            return sb.toString();
        }

        sb.append(":\n"); // NOI18N
        LexerUtilsConstants.appendTokenList(sb, rootTokenList);
        if (path2tokenListList != null && path2tokenListList.size() > 0) {
            sb.append(path2tokenListList.size());
            sb.append(" TokenListList(s) maintained:\n"); // NOI18N
            for (TokenListList tll : path2tokenListList.values()) {
                sb.append(tll).append('\n');
            }
        }
        return sb.toString();
    }
    
    /**
     * Check consistency of the whole token hierarchy.
     * @return string describing the problem or null if the hierarchy is consistent.
     */
    public String checkConsistency() {
        // Check root token list first
        String error = LexerUtilsConstants.checkConsistencyTokenList(rootTokenList(), true);
        // Check token-list lists
        if (error == null && path2tokenListList != null) {
            for (TokenListList<?> tll : path2tokenListList.values()) {
                // Check token-list list consistency
                error = tll.checkConsistency();
                if (error != null)
                    return error;
                // Check each individual token list in token-list list
                for (TokenList<?> tl : tll) {
                    error = LexerUtilsConstants.checkConsistencyTokenList(tl, false);
                    if (error != null) {
                        return error;
                    }
                }
            }
        }
        return error;
    }
    
//    private final class SnapshotRef extends WeakReference<TokenHierarchyOperation<I,T>> implements Runnable {
//        
//        SnapshotRef(TokenHierarchyOperation<I,T> snapshot) {
//            super(snapshot, org.openide.util.Utilities.activeReferenceQueue());
//        }
//
//        public void run() {
//            if (liveTokenHierarchyOperation != null) {
//                // Remove the reference from the snapshots array
//                liveTokenHierarchyOperation.removeSnapshot(this);
//            }
//        }
//
//    }

    static enum Activity {
        
        NOT_INITED, // Initial state for mutable inputs
        INACTIVE, // Explicitly set to inactive
        ACTIVE; // Default for immutable hierarchies; mutable THs once MTI.language() is non-null
        
    }

}
