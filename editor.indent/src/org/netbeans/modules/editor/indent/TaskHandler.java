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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;
import org.netbeans.spi.editor.indent.ReformatTask;
import org.openide.util.Lookup;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class TaskHandler {
    
    // -J-Dorg.netbeans.modules.editor.indent.TaskHandler=FINE
    private static final Logger LOG = Logger.getLogger(TaskHandler.class.getName());
    
    private boolean indent;
    
    private Document doc;

    private List<MimeItem> items;

    private Map<MimePath,MimeItem> mime2Item;

    private int maxMimePathSize;

    /**
     * Start position of the currently formatted chunk.
     */
    private Position startPos;

    /**
     * End position of the currently formatted chunk.
     */
    private Position endPos;
    
    private Set<Object> existingFactories = new HashSet<Object>();
    

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
    
    public Position startPos() {
        return startPos;
    }
    
    public Position endPos() {
        return endPos;
    }

    int maxMimePathSize() {
        return maxMimePathSize;
    }

    void setGlobalBounds(Position startPos, Position endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    boolean collectTasks() {
        String mimeType = docMimeType();
        if (mimeType != null) {
            // Get base indent task for the document.
            // Only if it exists also get the ones for possible embedded sections.
            MimePath mimePath = MimePath.get(mimeType);
            if (addItem(mimePath)) {
                // Also add the embedded ones
                TokenHierarchy<?> hi = TokenHierarchy.get(document());
                if (hi != null) {
                    LanguagePath[] languagePaths = sort(hi.languagePaths());
                    // Don't know the range yet :(
                    // TokenSequence<?> ts = hi.tokenSequence().subSequence(startPos.getOffset(), endPos.getOffset());
                    // Collection<LanguagePath> activeEmbeddedPaths = getActiveEmbeddedPaths();
                    for (LanguagePath lp : languagePaths) {
                        mimePath = MimePath.parse(lp.mimePath());
                        // Temporary fix until #108173 gets resolved: take only rightmost mime-type
                        mimePath = MimePath.get(mimePath.getMimeType(mimePath.size() - 1));

                        addItem(mimePath);
                    }
                }
            }
        }

        // HACK TODO PENDING WORKAROUND
        // Temporary Workaround: the HTML formatter clobbers the Ruby formatter's
        // work so make sure the Ruby formatter gets to work last in RHTML files
        if (items != null && "application/x-httpd-eruby".equals(mimeType)) {
            // Copy list, except for Ruby element, which we then add at the end
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem rubyItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().equals("text/x-ruby")) { // NOI18N
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


        return (items != null);
    }
    
    private LanguagePath[] sort(Set<LanguagePath> lps) {
        LanguagePath[] arr = new LanguagePath[lps.size()];
        lps.toArray(arr);
        Arrays.sort(arr, LanguagePathSizeComparator.INSTANCE);
        return arr;
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
        return (mimeType != null && new MimeItem(this, MimePath.get(mimeType)).hasFactories());
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

    private boolean addItem(MimePath mimePath) {
        // Only add if not added yet (doc's mime-type always added as first)
        if (mime2Item != null && mime2Item.containsKey(mimePath))
            return false;
        
        maxMimePathSize = Math.max(maxMimePathSize, mimePath.size());
        MimeItem item = new MimeItem(this, mimePath);
        if (item.createTask(existingFactories)) {
            if (items == null) {
                items = new ArrayList<MimeItem>();
                mime2Item = new HashMap<MimePath,MimeItem>();
            }
            items.add(item);
            mime2Item.put(item.mimePath(), item);
            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder(isIndent() ? "INDENT" : "REFORMAT");
                sb.append(": ");
                sb.append(item);
                LOG.fine(sb.toString());
            }
            return true;
        }
        return false;
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
        return (String)document().getProperty("mimeType");
    }
        
    /**
     * Item that services indentation/reformatting for a single mime-path.
     */
    public static final class MimeItem {
        
        private final TaskHandler handler;
        
        private final MimePath mimePath;
        
        private IndentTask indentTask;
        
        private ReformatTask reformatTask;
        
        private ExtraLock extraLock;
        
        private Context context;
        
        MimeItem(TaskHandler handler, MimePath mimePath) {
            this.handler = handler;
            this.mimePath = mimePath;
        }

        public MimePath mimePath() {
            return mimePath;
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
            return (lookup.lookup(IndentTask.Factory.class) != null)
                || (lookup.lookup(ReformatTask.Factory.class) != null);
        }
        
        boolean createTask(Set<Object> existingFactories) {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            if (!handler.isIndent()) { // Attempt reformat task first
                ReformatTask.Factory factory = lookup.lookup(ReformatTask.Factory.class);
                if (factory != null && (reformatTask = factory.createTask(context())) != null
                && !existingFactories.contains(factory)) {
                    extraLock = reformatTask.reformatLock();
                    existingFactories.add(factory);
                    return true;
                }
            }
            
            if (handler.isIndent() || reformatTask == null) { // Possibly fallback to reindent for reformatting
                IndentTask.Factory factory = lookup.lookup(IndentTask.Factory.class);
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
        
        @Override 
        public String toString() {
            return mimePath + ": " +
                    ((indentTask != null)
                        ? "IT: " + indentTask
                        : "RT: " + reformatTask);
        }

    }
    
    private final class TokenHierarchyL implements TokenHierarchyListener {
        
        boolean modified;
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            modified = true;
        }
        
    }
    
    private static final class LanguagePathSizeComparator implements Comparator<LanguagePath> {
        
        static final LanguagePathSizeComparator INSTANCE = new LanguagePathSizeComparator();

        public int compare(LanguagePath o1, LanguagePath o2) {
            return o1.size() - o2.size();
        }
        
        
    }
    
}
