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
package org.netbeans.modules.cnd.editor;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.cnd.editor.parser.CppFile;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.editor.parser.CppMetaModel;
import org.netbeans.modules.cnd.editor.parser.ParsingEvent;
import org.netbeans.modules.cnd.editor.parser.ParsingListener;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *  Fold maintainer/manager for C and C++ (not yet supporting Fortran).
 *  This code is derived from the NetBeans 4.1 versions of the NbJavaFoldManager 
 *  in the java/editor module.
 */
final class CppFoldManager extends CppFoldManagerBase
        implements Runnable, ParsingListener {

    private FoldOperation operation;
    /** Fold info for code blocks (functions, classes, comments, #ifdef/endif and compound statements) */
    private List<BlockFoldInfo> blockFoldInfos = Collections.emptyList();

    // Folding presets
    private boolean foldIncludesPreset;
    private boolean foldCommentPreset;
    private boolean foldCodeBlocksPreset;
    private boolean foldInitialCommentsPreset;
    private boolean listeningOnParsing;
    private static RequestProcessor cppFoldsRP;
    private static final Logger log = Logger.getLogger(CppFoldManager.class.getName());

    private CppFoldManager() {	// suppress standard creation
    }

    // Helper methods for awhile...
    private static synchronized RequestProcessor getCppFoldsRP() {
        if (cppFoldsRP == null) {
            cppFoldsRP = new RequestProcessor("CPP-Folds", 1); // NOI18N
        }
        return cppFoldsRP;
    }

    /**
     *  Get the filename associated with this FileManager. Used (currently) only for debugging.
     *  @returns A String representing the absolute path of file
     */
    private String getFilename() {
        FoldHierarchy h = (operation != null) ? operation.getHierarchy() : null;
        javax.swing.text.JTextComponent comp = (h != null) ? h.getComponent() : null;
        Document doc = (comp != null) ? comp.getDocument() : null;
        DataObject dob = (doc != null) ? NbEditorUtilities.getDataObject(doc) : null;
        String path = (dob != null) ? FileUtil.getFileDisplayName(dob.getPrimaryFile()) : null;
        return path;
    }

    private String getShortName() {
        String longname = (String) getDocument().getProperty(Document.TitleProperty);
        int slash = longname.lastIndexOf(File.separatorChar);

        if (slash != -1) {
            return longname.substring(slash + 1);
        } else {
            return longname;
        }
    }

    private BlockFoldInfo findBlockFoldInfo(BlockFoldInfo info) {
        if (info == null) {
            return null;
        }
        // Peformance optimization: stop search if we passed reqired start position
        for (BlockFoldInfo orig : blockFoldInfos) {
            if (orig.equals(info)) {
                return orig;
            } else if (orig.after(info)) {
                return null;
            }
        }
        return null;
    }

    private FoldOperation getOperation() {
        return operation;
    }

    private void removeFoldNotify(Fold fold) {
        log.log(Level.FINE, "CppFoldManager.removeFoldNotify"); // NOI18N
    }

    synchronized private void updateFolds() {
        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "CFM.updateFolds: Processing " + getShortName() + " [" +
                    Thread.currentThread().getName() + "]"); // NOI18N
        }
        final UpdateFoldsRequest request = collectFoldUpdates();

        // do not schedule null requests
        if (request == null) {
            return;
        }

        //assert Thread.currentThread().getName().equals("CPP-Folds");
        Runnable hierarchyUpdate = new Runnable() {

            public void run() {
                if (!getOperation().isReleased()) {
                    Document doc = getDocument();
                    if (!(doc instanceof AbstractDocument)) {
                        return; // can happen (e.g. after component close)
                    }
                    if (log.isLoggable(Level.FINE)){
                        log.log(Level.FINE, "CFM.updateFolds$X1.run: Processing " + getShortName() + " [" +
                                Thread.currentThread().getName() + "]"); // NOI18N
                    }

                    AbstractDocument adoc = (AbstractDocument) doc;
                    adoc.readLock();
                    try {
                        FoldHierarchy hierarchy = getOperation().getHierarchy();
                        hierarchy.lock();
                        try {
                            FoldHierarchyTransaction t = getOperation().openTransaction();
                            try {
                                if (log.isLoggable(Level.FINE)){
                                    log.log(Level.FINE, "CFM.updateFolds$X1.run: Calling " + // NOI18N
                                            "processUpdateFoldRequest for " + getShortName() + " [" + // NOI18N
                                            Thread.currentThread().getName() + "]"); // NOI18N
                                }
//                                System.out.println("=========== " + getShortName() + " ===========");
//                                System.out.println(hierarchy.toString());
                                processUpdateFoldRequest(request, t);
//                                System.out.println("------------ VV -----------");
//                                System.out.println(hierarchy.toString());
                            } finally {
                                t.commit();
                            }
                        } finally {
                            hierarchy.unlock();
                        }
                    } finally {
                        adoc.readUnlock();
                    }
                }
            }
        };
        // Do fold updates in AWT
        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "CFM.updateFolds: Starting update for " + getShortName() + " on AWT thread"); // NOI18N
        }
        SwingUtilities.invokeLater(hierarchyUpdate);
    }

    /** Collect all updates into an update request */
    private UpdateFoldsRequest collectFoldUpdates() {
        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "CFM.collectFoldUpdates: Processing " + getShortName() +
                    " [" + Thread.currentThread().getName() + "]"); // NOI18N
        }
        Document doc = getDocument();

        if (getOperation().isReleased() || !(doc instanceof AbstractDocument)) {
            if (log.isLoggable(Level.FINE)){
               log.log(Level.FINE, "CFM.collectFoldUpdates: No doc found for " + getShortName()); // NOI18N
            }
            return null;
        }

        CppFile cpf = CppMetaModel.getDefault().
                get(doc.getProperty(Document.TitleProperty).toString());
        if (cpf == null) {
            return null;
        }
        cpf.waitScanFinished(CppFile.FOLD_PARSING);

        if (cpf.isParsingFailed()) {
            return null;
        }

        UpdateFoldsRequest request = new UpdateFoldsRequest();

        AbstractDocument adoc = (AbstractDocument) doc;
        adoc.readLock();

        try {
            // initial comment fold
            request.addBlockFoldInfo(cpf.getInitialCommentFold());

            // The Includes sections
            for (CppFoldRecord rec : cpf.getIncludesFolds()) {
                request.addBlockFoldInfo(rec);
            }

            // Functions/methods
            for (CppFoldRecord rec : cpf.getBlockFolds()) {
                request.addBlockFoldInfo(rec);
            }
        } finally {
            adoc.readUnlock();
        }
        return request;
    }

    /** Process the fold updates in the request */
    private void processUpdateFoldRequest(UpdateFoldsRequest request, FoldHierarchyTransaction transaction) {
        if (request != null && request.isValid()) {
            if (log.isLoggable(Level.FINE)){
                log.log(Level.FINE, "CFM.processUpdateFoldRequest: Processing " + getShortName() +
                        " [" + Thread.currentThread().getName() + "]"); // NOI18N
            }

            List<BlockFoldInfo> infoList = request.getBlockFoldInfos();
            for (BlockFoldInfo info : infoList) {
                // look for existing fold
                BlockFoldInfo orig = findBlockFoldInfo(info);

                if (orig == null || info.isUpdateRequired(orig)) {
                    //remove old
                    if (orig != null) {
                        orig.removeFromHierarchy(transaction);
                        blockFoldInfos.remove(orig);
                    }

                    // Add the new fold
                    try {
                        info.addToHierarchy(transaction);
                    } catch (BadLocationException e) {
                        // it is OK to skip such exceptions
                    }
                } else {
                    blockFoldInfos.remove(orig);
                    info.fold = orig.fold;
                }
            }

            // remove obsolete items 
            // (this is required because some blocks does not have controlled borders)
            for (BlockFoldInfo orig : blockFoldInfos) {
                orig.removeFromHierarchy(transaction);
            }
            blockFoldInfos = infoList;
        }
    }

    Document getDocument() {
        return getOperation().getHierarchy().getComponent().getDocument();
    }

    DataObject getDataObject() {
        Document doc = getDocument();
        return (doc != null) ? NbEditorUtilities.getDataObject(doc) : null;
    }

    // Implement Runnable
    public void run() {
        try {
            if ((new File(getFilename())).exists()) {
                if (log.isLoggable(Level.FINE)){
                    log.log(Level.FINE, "CFM.run: Processing " + getShortName() +
                            " [" + Thread.currentThread().getName() + "]"); // NOI18N
                }
                if (!listeningOnParsing) {
                    if (log.isLoggable(Level.FINE)){
                        log.log(Level.FINE, "CFM.run: Processing " + getShortName() +
                                " [" + Thread.currentThread().getName() + "]"); // NOI18N
                    }
                    listeningOnParsing = true;
                    if (log.isLoggable(Level.FINE)){
                        log.log(Level.FINE, "CFM.run: Starting WeakParsingListener [" +
                                Thread.currentThread().getName() + "]"); // NOI18N
                    }
                    new WeakParsingListener(this).startListening();
                }
                if (log.isLoggable(Level.FINE)){
                    log.log(Level.FINE, "CFM.run: Calling updateFolds [" +
                            Thread.currentThread().getName() + "]"); // NOI18N
                }
                updateFolds();
            }
        } catch (ThreadDeath e) {
            throw e;
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(t);
        }
    }


    // Implementing FoldManager...
    /** Initialize this manager */
    public void init(FoldOperation operation) {
        this.operation = operation;
        EditorKit kit = org.netbeans.editor.Utilities.getKit(operation.getHierarchy().getComponent());
        if (kit instanceof NbEditorKit) {
            String contentType = ((NbEditorKit) kit).getContentType();
            if (contentType != null) {
                Preferences prefs = MimeLookup.getLookup(contentType).lookup(Preferences.class);
                if (prefs != null) {
                    foldInitialCommentsPreset = prefs.getBoolean(CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, false);
                    foldIncludesPreset = prefs.getBoolean(CODE_FOLDING_COLLAPSE_IMPORT, false);
                    foldCodeBlocksPreset = prefs.getBoolean(CODE_FOLDING_COLLAPSE_METHOD, false);
                    foldCommentPreset = prefs.getBoolean(CODE_FOLDING_COLLAPSE_JAVADOC, false);
                }
            }
        }
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        if (getFilename() != null && getFilename().length() > 0) {
            if (log.isLoggable(Level.FINE)){
                log.log(Level.FINE, "CFM.initFolds: Posting for " + getShortName() +
                        " on Cpp Folds RP [" + Thread.currentThread().getName() + "]"); // NOI18N
            }
            getCppFoldsRP().post(this, 1000, Thread.MIN_PRIORITY);
        }
    }

    private void scheduleParsing(Document doc) {
        // we parse only documents assigned to files on disk        
        // TODO: why above?
        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "TitleProperty: " + doc.getProperty(Document.TitleProperty));
        }
        if (doc.getProperty(Document.TitleProperty) != null) {
            CppMetaModel.getDefault().scheduleParsing(doc);
        }
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        if (log.isLoggable(Level.FINE)){
            log.log(Level.FINE, "FoldManager.insertUpdate: " + evt.getDocument().toString());
        }
        scheduleParsing(evt.getDocument());
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        log.log(Level.FINE, "FoldManager.removeUpdate");
        scheduleParsing(evt.getDocument());
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        log.log(Level.FINE, "FoldManager.changeUpdate");
//        scheduleParsing(evt.getDocument());
    }

    void removeFoldInfo(Fold fold) {
        // TODO: can do binary search here because blockFoldInfos is sorted by the real start pos
        for (Iterator<BlockFoldInfo> iter = blockFoldInfos.iterator(); iter.hasNext();) {
            if (iter.next().fold == fold) {
                iter.remove();
                break;
            }
        }
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeFoldNotify(emptyFold);
        removeFoldInfo(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
        removeFoldNotify(damagedFold);
        removeFoldInfo(damagedFold);
    }

    public void expandNotify(Fold expandedFold) {
    }

    public void release() {
    }

    // Implementing ParsingListener
    public void objectParsed(ParsingEvent evt) {
        DataObject dob = (DataObject) evt.getSource();
        String path = getFilename();

        if (dob != null) {
            FileObject primaryFile = dob.getPrimaryFile();

            if (primaryFile != null) {
                String pfile = FileUtil.getFileDisplayName(primaryFile);
                if (pfile.equals(path)) {
                    if (log.isLoggable(Level.FINE)){
                        log.log(Level.FINE, "CFM.objectParsed: Calling updateFolds for " + getShortName()); // NOI18N
                    }
                    updateFolds();
                } else {
                    log.log(Level.FINE, "CFM.objectParsed: Skipping updateFolds"); // NOI18N
                }
            }
        }
    }

    // Worker classes...
    /** Gather update information in this class */
    private final class UpdateFoldsRequest {

        private final Document creationTimeDoc;
        /** List of the code block folds (methods, functions, compound statements etc.) */
        private final List<BlockFoldInfo> blockFoldInfos = new LinkedList<BlockFoldInfo>();

        UpdateFoldsRequest() {
            creationTimeDoc = getDocument();
        }

        boolean isValid() {
            // Check whether request creation time document
            // is still in use by the fold hierarchy
            return (creationTimeDoc != null && creationTimeDoc == getDocument());
        }

        List<BlockFoldInfo> getBlockFoldInfos() {
            return blockFoldInfos;
        }

        void addBlockFoldInfo(CppFoldRecord foldRecord) {
            if (foldRecord != null) {
                try {
                    blockFoldInfos.add(new BlockFoldInfo(foldRecord));
                } catch (BadLocationException ex) {
                    if (log.isLoggable(Level.FINE)){
                        log.log(Level.FINE, "CFM.addBlockFoldInfo: Got BadLocationException\n    " + // NOI18N
                                ex.getMessage());
                    }
                }
            }
        }
    }

    private final class BlockFoldInfo {

        private Fold fold = null;
        private final FoldTemplate template;
        private final int type;
        private final boolean collapse;
        private final int startOffset;
        private final int endOffset;

        public BlockFoldInfo(CppFoldRecord fi) throws BadLocationException {
            this.startOffset = fi.getStartOffset();
            this.endOffset = fi.getEndOffset();
            this.type = fi.getType();
            switch (fi.getType()) {
                case CppFoldRecord.INITIAL_COMMENT_FOLD:
                    template = INITIAL_COMMENT_FOLD_TEMPLATE;
                    collapse = foldInitialCommentsPreset;
                    break;
                case CppFoldRecord.INCLUDES_FOLD:
                    template = INCLUDES_FOLD_TEMPLATE;
                    collapse = foldIncludesPreset;
                    break;
                case CppFoldRecord.FUNCTION_FOLD:
                case CppFoldRecord.CONSTRUCTOR_FOLD:
                case CppFoldRecord.DESTRUCTOR_FOLD:
                case CppFoldRecord.CLASS_FOLD:
                case CppFoldRecord.NAMESPACE_FOLD:
                    template = CODE_BLOCK_FOLD_TEMPLATE;
                    collapse = foldCodeBlocksPreset;
                    break;
                case CppFoldRecord.BLOCK_COMMENT_FOLD:
                    template = COMMENT_FOLD_TEMPLATE;
                    collapse = foldCommentPreset;
                    break;
                case CppFoldRecord.COMMENTS_FOLD:
                    template = LINE_COMMENT_FOLD_TEMPLATE;
                    collapse = false;
                    break;
                case CppFoldRecord.IFDEF_FOLD:
                    template = IFDEF_FOLD_TEMPLATE;
                    collapse = false;
                    break;
                default:
                    assert (false) : "unsupported block type " + fi; // NOI18N
                    collapse = false;
                    template = null;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BlockFoldInfo)) {
                return false;
            }
            final BlockFoldInfo other = (BlockFoldInfo) obj;
            return (this.template == other.template) && (this.getRealStartOffset() == other.getRealStartOffset());
        }

        public boolean after(BlockFoldInfo other) {
            return this.getRealStartOffset() > other.getRealEndOffset();
        }

        public boolean isUpdateRequired(BlockFoldInfo orig) {
            assert this.equals(orig) : "only equal orig can be here";
            return (orig.fold == null) || (this.getRealEndOffset() != orig.getRealEndOffset());
        }

        private int getRealStartOffset() {
            if (fold != null) {
                return fold.getStartOffset();
            }
            return startOffset;
        }

        private int getRealEndOffset() {
            if (fold != null) {
                return fold.getEndOffset();
            }
            return endOffset;
        }

        public void addToHierarchy(FoldHierarchyTransaction transaction)
                throws BadLocationException {
            if (FoldOperation.isBoundsValid(startOffset, endOffset,
                    template.getStartGuardedLength(), template.getEndGuardedLength())) {
                if (log.isLoggable(Level.FINE)){
                    log.log(Level.FINE, "CFM.BlockFoldInfo.updateHierarchy: Creating fold at (" + // NOI18N
                            startOffset + ", " + endOffset + ")"); // NOI18N
                }
                fold = getOperation().addToHierarchy(
                        template.getType(), template.getDescription(), collapse,
                        startOffset, endOffset,
                        template.getStartGuardedLength(), template.getEndGuardedLength(),
                        this,
                        transaction);
            }
        }

        public void removeFromHierarchy(FoldHierarchyTransaction transaction) {
            if (fold != null) {
                FoldOperation fo = getOperation();
                if (fo.isAddedOrBlocked(fold)) {
                    fo.removeFromHierarchy(fold, transaction);
                }
            }
        }

        @Override
        public String toString() {
            return "BlockFoldInfo:" + template.getType() + " at[" + getRealStartOffset() + "," + getRealEndOffset() + "]";  // NOI18N
        }
    }

    private static final class WeakParsingListener implements ParsingListener {

        private WeakReference<ParsingListener> ref;

        WeakParsingListener(ParsingListener listener) {
            ref = new WeakReference<ParsingListener>(listener);
        }

        public void startListening() {
            CppMetaModel.getDefault().addParsingListener(this);
        }

        public void objectParsed(ParsingEvent evt) {
            ParsingListener listener = ref.get();
            if (listener != null) {
                listener.objectParsed(evt);
            } else {
                CppMetaModel.getDefault().removeParsingListener(this);
            }
        }
    }

    /**
     *  The factory class to create the CppFoldManager. It gets installed via
     *  an entry in the layer file.
     */
    public static final class Factory implements FoldManagerFactory {

        public Factory() {
        }

        public FoldManager createFoldManager() {
            return new CppFoldManager();
        }
    }
}
