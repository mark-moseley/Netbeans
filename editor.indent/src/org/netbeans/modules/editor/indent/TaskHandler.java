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

package org.netbeans.modules.editor.indent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class TaskHandler {
    
    // -J-Dorg.netbeans.modules.editor.indent.TaskHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(TaskHandler.class.getName());
    
    private final boolean indent;
    
    private final Document doc;

    private List<MimeItem> items;

    /**
     * Start position of the currently formatted chunk.
     */
    private Position startPos;

    /**
     * End position of the currently formatted chunk.
     */
    private Position endPos;
    
    private Position caretPos;
    
    private final Set<Object> existingFactories = new HashSet<Object>();
    

    TaskHandler(boolean indent, Document doc) {
        this.indent = indent;
        this.doc = doc;
    }

    public boolean isIndent() {
        return indent;
    }

    public Document document() {
        return doc;
    }
    
    public int caretOffset() {
        return caretPos.getOffset();
    }

    public void setCaretOffset(int offset) throws BadLocationException {
        caretPos = doc.createPosition(offset);
    }
    
    public Position startPos() {
        return startPos;
    }
    
    public Position endPos() {
        return endPos;
    }

    void setGlobalBounds(Position startPos, Position endPos) {
        assert (startPos.getOffset() <= endPos.getOffset())
                : "startPos=" + startPos.getOffset() + " < endPos=" + endPos.getOffset();
        this.startPos = startPos;
        this.endPos = endPos;
    }

    boolean collectTasks() {
        TokenHierarchy<?> th = TokenHierarchy.get(document());
        List<LanguagePath> languagePaths;
        
        Set<LanguagePath> languagePathSet;
        if (th != null && (languagePathSet = th.languagePaths()).size() > 0) {
            // Should contain top-level path and zero or more embedded paths
            languagePaths = new ArrayList<LanguagePath>(languagePathSet);
            
            Collections.sort(languagePaths, LanguagePathSizeComparator.ASCENDING);
            
            for (LanguagePath lp : languagePaths) {
                addItem(MimePath.parse(lp.mimePath()), lp);
            }
            
        } else { // Add a single item corresponding to the document's mime-type
            addItem(MimePath.parse(docMimeType()), null);
        }

        // XXX: HACK TODO PENDING WORKAROUND
        // Temporary Workaround: the HTML formatter clobbers the Ruby formatter's
        // work so make sure the Ruby formatter gets to work last in RHTML files
        //
        // The problem is that both html and ruby formatters have language paths
        // of the same lenght and therefore their ordering is undefined.
        // This will be solved in the infrastructure by segmenting the formatted
        // area by the language paths. And calling each formatter task only
        // with the segments that belong to it.
        if (items != null && "application/x-httpd-eruby".equals(docMimeType())) { //NOI18N
            // Copy list, except for Ruby element, which we then add at the end
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem rubyItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/x-ruby")) { // NOI18N
                    rubyItem = item;
                } else {
                    newItems.add(item);
                }
            }
            if (rubyItem != null) {
                newItems.add(rubyItem);
            }
            items = newItems;
        }
        
        // XXX: HACK TODO PENDING WORKAROUND
        // A hotfix for #116022: the jsp formatter must be called first and the html formatter second
        if (items != null && "text/x-jsp".equals(docMimeType()) || "text/x-tag".equals(docMimeType())) { //NOI18N
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem htmlItem = null;
            MimeItem jspItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/html")) { //NOI18N
                    htmlItem = item;
                } else if (item.mimePath().getPath().endsWith("text/x-jsp") //NOI18N
                        || item.mimePath().getPath().endsWith("text/x-tag")) { //NOI18N
                    jspItem = item;
                } else {
                    newItems.add(item);
                }
            }
            
            if (htmlItem != null) {
                newItems.add(0, htmlItem);
            }
            
            if (jspItem != null) {
                newItems.add(0, jspItem);
            }
            
            items = newItems;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Collected items: "); //NOI18N
            for (MimeItem mi : items) {
                LOG.fine("  Item: " + mi); //NOI18N
            }
            LOG.fine("-----------------"); //NOI18N
        }
        
        return (items != null);
    }
    
    void lock() {
        if (items != null) {
            int i = 0;
            try {
                for (; i < items.size(); i++) {
                    MimeItem item = items.get(i);
                    item.lock();
                }
            } finally {
                if (i < items.size()) { // Locking of i-th item has failed
                    // Unlock the <0,i-1> items that are already locked
                    // Assuming that the unlock() for already locked items will pass
                    while (--i >= 0) {
                        MimeItem item = items.get(i);
                        item.unlock();
                    }
                }
            }
        }
    }

    void unlock() {
        if (items != null) {
            for (MimeItem item : items) {
                item.unlock();
            }
        }
    }

    boolean hasFactories() {
        String mimeType = docMimeType();
        return (mimeType != null && new MimeItem(this, MimePath.get(mimeType), null).hasFactories());
    }

    boolean hasItems() {
        return (items != null);
    }

    void runTasks() throws BadLocationException {
        // Run top-level task and possibly embedded tasks according to the context
        if (items == null) // Do nothing for no items
            return;

        // Start with the doc's mime type's task
        for (MimeItem item : items) {
            item.runTask();
        }
    }

    private boolean addItem(MimePath mimePath, LanguagePath languagePath) {
        MimeItem item = new MimeItem(this, mimePath, languagePath);
        if (item.createTask(existingFactories)) {
            if (items == null) {
                items = new ArrayList<MimeItem>();
            }
            items.add(item);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Adding MimeItem: " + item); //NOI18N
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Collect language paths used within the given token sequence
     * 
     * @param ts non-null token sequence (or subsequence). <code>ts.moveNext()</code>
     * is called first on it.
     * @return collection of language paths present in the given token sequence.
     */
    private Collection<LanguagePath> getActiveEmbeddedPaths(TokenSequence ts) {
        Collection<LanguagePath> lps = new HashSet<LanguagePath>();
        lps.add(ts.languagePath());
        List<TokenSequence<?>> tsStack = null;
        while (true) {
            while (ts.moveNext()) {
                TokenSequence<?> eTS = ts.embedded();
                if (eTS != null) {
                    tsStack.add(ts);
                    ts = eTS;
                    lps.add(ts.languagePath());
                }
            }
            if (tsStack != null && tsStack.size() > 0) {
                ts = tsStack.get(tsStack.size() - 1);
                tsStack.remove(tsStack.size() - 1);
            } else {
                break;
            }
        }
        return lps;
    }

    private String docMimeType() {
        return (String)document().getProperty("mimeType"); //NOI18N
    }
        
    /**
     * Item that services indentation/reformatting for a single mime-path.
     */
    public static final class MimeItem {
        
        private final TaskHandler handler;
        
        private final MimePath mimePath;
        
        private final LanguagePath languagePath;
        
        private IndentTask indentTask;
        
        private ReformatTask reformatTask;
        
        private ExtraLock extraLock;
        
        private Context context;
        
        MimeItem(TaskHandler handler, MimePath mimePath, LanguagePath languagePath) {
            this.handler = handler;
            this.mimePath = mimePath;
            this.languagePath = languagePath;
        }

        public MimePath mimePath() {
            return mimePath;
        }
        
        public LanguagePath languagePath() {
            return languagePath;
        }
        
        public Context context() {
            if (context == null) {
                context = IndentSpiPackageAccessor.get().createContext(this);
            }
            return context;
        }
        
        public TaskHandler handler() {
            return handler;
        }
        
        boolean hasFactories() {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            return handler().isIndent()
                    ? (lookup.lookup(IndentTask.Factory.class) != null)
                    : (lookup.lookup(ReformatTask.Factory.class) != null);
        }
        
        public List<Context.Region> indentRegions() {
            Document doc = handler.document();
            List<Context.Region> indentRegions = new ArrayList<Context.Region>();
            try {
                if (languagePath != null) {
                    int endOffset = handler.endPos().getOffset();
                    if (endOffset >= doc.getLength())
                        endOffset = Integer.MAX_VALUE;
                    int startOffset = handler.startPos().getOffset();
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("indentRegions: startOffset=" + startOffset + ", endOffset=" + endOffset + '\n'); //NOI18N
                    }

                    List<TokenSequence<?>> tsl = TokenHierarchy.get(doc).tokenSequenceList(languagePath,
                            startOffset, endOffset);
                    for (TokenSequence<?> ts : tsl) {
                        ts.moveStart();
                        if (ts.moveNext()) { // At least one token
                            int regionStartOffset = ts.offset();
                            ts.moveEnd(); // At least one token exists
                            ts.movePrevious();
                            int regionEndOffset = ts.offset() + ts.token().length();
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("  Region[" + indentRegions.size() + // NOI18N
                                        "]: startOffset=" + regionStartOffset + ", endOffset=" + regionEndOffset + '\n'); //NOI18N
                            }
                            // Only within global boundaries
                            if (regionStartOffset <= endOffset && regionEndOffset >= startOffset) {
                                regionStartOffset = Math.max(regionStartOffset, startOffset);
                                regionEndOffset = Math.min(regionEndOffset, endOffset);
                                MutablePositionRegion region = new MutablePositionRegion(
                                        doc.createPosition(regionStartOffset),
                                        doc.createPosition(regionEndOffset)
                                );
                                indentRegions.add(IndentSpiPackageAccessor.get().createContextRegion(region));
                            }
                        }
                    }
                } else { // used when no token hierarchy exists
                    MutablePositionRegion wholeDocRegion = new MutablePositionRegion(doc.getStartPosition(),
                            doc.createPosition(doc.getLength()));
                    indentRegions.add(IndentSpiPackageAccessor.get().createContextRegion(wholeDocRegion));
                }
                
                // Filter out guarded regions
                if (indentRegions.size() > 0 && doc instanceof GuardedDocument) {
                    MutablePositionRegion region = IndentSpiPackageAccessor.get().positionRegion(indentRegions.get(0));
                    int regionStartOffset = region.getStartOffset();
                    GuardedDocument gdoc = (GuardedDocument)doc;
//                    int gbStartOffset = guardedBlocks.adjustToBlockEnd(region.getEndOffset());
//                    MarkBlockChain guardedBlocks = gdoc.getGuardedBlockChain();
//                    if (guardedBlocks != null && guardedBlocks.getChain() != null) {
//                        int gbStartOffset = guardedBlocks.adjustToNextBlockStart(indentRegions.getStartOffset());
//                        int regionIndex = 0;
//                        while (regionIndex < indentRegions.size()) { // indentRegions can be mutated dynamically
//                            MutablePositionRegion region = IndentSpiPackageAccessor.get().positionRegion(indentRegions.get(regionIndex));
//                            int gbStartOffset = guardedBlocks.adjustToNextBlockStart(region.getStartOffset());
//                            int gbEndOffset = guardedBlocks.adjustToBlockEnd(region.getEndOffset());
//
//                            while (pos < endPosition.getOffset()) {
//                                int stopPos = endPosition.getOffset();
//                                if (gdoc != null) { // adjust to start of the next guarded block
//                                    stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos);
//                                    if (stopPos == -1 || stopPos > endPosition.getOffset()) {
//                                        stopPos = endPosition.getOffset();
//                                    }
//                                }
//
//                                if (pos < stopPos) {
//                                    int reformattedLen = formatter.reformat(doc, pos, stopPos);
//                                    pos = pos + reformattedLen;
//                                } else {
//                                    pos++; //ensure to make progress
//                                }
//
//                                if (gdoc != null) { // adjust to end of current block
//                                    pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
//                                }
//                            }
//                        }
//                    }
                }
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
                indentRegions = Collections.emptyList();
            }
            return indentRegions;
        }
        
        boolean createTask(Set<Object> existingFactories) {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            if (!handler.isIndent()) { // Attempt reformat task first
                ReformatTask.Factory factory = lookup.lookup(ReformatTask.Factory.class);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("'" + mimePath.getPath() + "' supplied ReformatTask.Factory: " + factory); //NOI18N
                }
                if (factory != null && (reformatTask = factory.createTask(context())) != null
                && !existingFactories.contains(factory)) {
                    extraLock = reformatTask.reformatLock();
                    existingFactories.add(factory);
                    return true;
                }
            }
            
            if (handler.isIndent() || reformatTask == null) { // Possibly fallback to reindent for reformatting
                IndentTask.Factory factory = lookup.lookup(IndentTask.Factory.class);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("'" + mimePath.getPath() + "' supplied IndentTask.Factory: " + factory); //NOI18N
                }
                if (factory != null && (indentTask = factory.createTask(context())) != null
                && !existingFactories.contains(factory)) {
                    extraLock = indentTask.indentLock();
                    existingFactories.add(factory);
                    return true;
                }
            }
            return false;
        }
        
        void lock() {
            if (extraLock != null)
                extraLock.lock();
        }
        
        void runTask() throws BadLocationException {
            if (indentTask != null) {
                indentTask.reindent();
            } else {
                reformatTask.reformat();
            }
        }
        
        void unlock() {
            if (extraLock != null)
                extraLock.unlock();
        }
        
        public @Override String toString() {
            return mimePath + ": " + ((indentTask != null) ? "IT: " + indentTask : "RT: " + reformatTask); //NOI18N
        }
    }
    
    private static final class LanguagePathSizeComparator implements Comparator<LanguagePath> {
        
        static final LanguagePathSizeComparator ASCENDING = new LanguagePathSizeComparator(false);

        private final boolean reverse;
        
        public LanguagePathSizeComparator(boolean reverse) {
            this.reverse = reverse;
        }
        
        public int compare(LanguagePath lp1, LanguagePath lp2) {
            return reverse ? lp2.size() - lp1.size() : lp1.size() - lp2.size();
        }
    } // End of MimePathSizeComparator class
    
}
