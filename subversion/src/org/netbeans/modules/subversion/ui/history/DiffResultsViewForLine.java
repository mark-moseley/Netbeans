/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.ui.history;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * DiffResultsView not showing differences but rather fixed line numbers.
 * Currently used by bugtracking to display revisions of a file and to fix the view on a given line number.
 * 
 * @author Ondra Vrabec
 */
final class DiffResultsViewForLine extends DiffResultsView {
    private int lineNumber;
    
    public DiffResultsViewForLine(final SearchHistoryPanel parent, final List<RepositoryRevision> results, final int lineNumber) {
        super(parent, results);
        this.lineNumber = Math.max(lineNumber - 1, 0);
        setButtonLabels();
    }

    @Override
    protected void showRevisionDiff(RepositoryRevision.Event rev, boolean showLastDifference) {
        if (rev.getFile() == null) return;
        long revision2 = rev.getLogInfoHeader().getLog().getRevision().getNumber();
        showDiff(rev, null, Long.toString(revision2), showLastDifference);
    }

    @Override
    protected SvnProgressSupport createShowDiffTask(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
        if (revision1 == null) {
            return new ShowDiffTask(header, revision1, revision2, showLastDifference);
        } else {
            return super.createShowDiffTask(header, revision1, revision2, showLastDifference);
        }
    }

    @Override
    void onNextButton() {
        if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
        setDiffIndex(currentIndex, false);
    }

    @Override
    void onPrevButton() {
        if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
        setDiffIndex(currentIndex, true);
    }

    private void setButtonLabels() {
        parent.bNext.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSN_NextRevision")); // NOI18N
        parent.bNext.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_NextRevision")); // NOI18N
        parent.bNext.setToolTipText(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_NextRevision")); // NOI18N
        parent.bPrev.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSN_PrevRevision")); // NOI18N
        parent.bPrev.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_PrevRevision")); // NOI18N
        parent.bPrev.setToolTipText(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_PrevRevision")); // NOI18N
    }

    private class ShowDiffTask extends SvnProgressSupport {
        private final RepositoryRevision.Event header;
        private final String revision2;

        public ShowDiffTask(RepositoryRevision.Event header, String revision1, String revision2, boolean showLastDifference) {
            this.header = header;
            this.revision2 = revision2;
        }

        @Override
        protected void perform() {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); // NOI18N
            SVNUrl repotUrl = header.getLogInfoHeader().getRepositoryRootUrl();
            SVNUrl fileUrl = repotUrl.appendPath(header.getChangedPath().getPath());
            // through peg revision always except from 'deleting the file', since the file does not exist in the newver revision
            final DiffStreamSource leftSource = new DiffStreamSource(header.getFile(), repotUrl, fileUrl, revision2, revision2);
            final LocalFileDiffStreamSource rightSource = new LocalFileDiffStreamSource(header.getFile(), true);
            this.setCancellableDelegate(new Cancellable() {
                public boolean cancel() {
                    leftSource.cancel();
                    return true;
                }
            });
            // it's enqueued at ClientRuntime queue and does not return until previous request handled
            leftSource.getMIMEType();  // triggers s1.init()
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }
            rightSource.getMIMEType();  // triggers s2.init()
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }

            if (currentTask != this) return;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        if (isCanceled()) {
                            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                            return;
                        }
                        final DiffController view = DiffController.create(leftSource, rightSource);
                        int leftMaxLineNumber = getLastLineIndex(leftSource);
                        int rightMaxLineNumber = getLastLineIndex(rightSource);
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getJComponent());
                            if (leftMaxLineNumber != -1) {
                                setLocation(Math.min(leftMaxLineNumber, lineNumber), false);
                            }
                            if (rightMaxLineNumber != -1) {
                                setLocation(Math.min(rightMaxLineNumber, lineNumber), true);
                            }
                            parent.refreshComponents(false);
                        }
                    } catch (IOException e) {
                        Subversion.LOG.log(Level.INFO, null, e);
                    }
                }
            });
        }
    }

    private int getLastLineIndex (final StreamSource ss) {
        String mimeType = ss.getMIMEType();
        if (mimeType == null || !mimeType.startsWith("text/")) {
            Subversion.LOG.log(Level.INFO, "Wrong mime type");
            return 0;
        }
        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        if (kit == null) {
            Subversion.LOG.log(Level.WARNING, "No editor kit available");
            return 0;
        }
        Document sdoc = getSourceDocument(ss);
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        StyledDocument styledDoc;
        if ((doc instanceof StyledDocument)) {
            styledDoc = (StyledDocument) doc;
        } else {
            styledDoc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
        }
        if (sdoc == null) {
            Reader r = null;
            try {
                r = ss.createReader();
                if (r != null) {
                    try {
                        kit.read(r, styledDoc, 0);
                    } catch (javax.swing.text.BadLocationException e) {
                        throw new IOException("Can not locate the beginning of the document."); // NOI18N
                    } finally {
                        r.close();
                    }
                }
            } catch (IOException ex) {
                Subversion.LOG.log(Level.INFO, null, ex);
            } finally {
                try {
                    r.close();
                } catch (IOException ex) {
                    Subversion.LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return org.openide.text.NbDocument.findLineNumber(styledDoc, styledDoc.getEndPosition().getOffset());
    }

    private Document getSourceDocument(StreamSource ss) {
        Document sdoc = null;
        FileObject fo = ss.getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                if (dao.getPrimaryFile() == fo) {
                    EditorCookie ec = dao.getCookie(EditorCookie.class);
                    if (ec != null) {
                        sdoc = ec.openDocument();
                    }
                }
            } catch (Exception e) {
                // fallback to other means of obtaining the source
            }
        } else {
            sdoc = ss.getLookup().lookup(Document.class);
        }
        return sdoc;
    }

    private void setLocation (final int lineNumber, final boolean showLineInLocal) {
        if (currentDiff == null) {
            return;
        }
        if (showLineInLocal) {
            currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.LineNumber, lineNumber);
        } else {
            currentDiff.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, lineNumber);
        }
    }

    private static class LocalFileDiffStreamSource extends StreamSource {

        private final FileObject    fileObject;
        private final boolean       isRight;
        private File file;
        private String mimeType;

        public LocalFileDiffStreamSource (File file, boolean isRight) {
            this.file = FileUtil.normalizeFile(file);
            this.fileObject = FileUtil.toFileObject(this.file);
            this.isRight = isRight;
        }

        @Override
        public boolean isEditable() {
            return isRight && fileObject != null && fileObject.canWrite();
        }

        @Override
        public Lookup getLookup() {
            if (fileObject != null) {
                return Lookups.fixed(fileObject);
            } else {
                return Lookups.fixed();
            }
        }

        public String getName() {
            return file.getName();
        }

        public String getTitle() {
            return fileObject != null ? FileUtil.getFileDisplayName(fileObject) : file.getAbsolutePath();
        }

        public String getMIMEType() {
            return mimeType = fileObject != null && fileObject.isValid() ? SvnUtils.getMimeType(file) : null;
        }

        public Reader createReader() throws IOException {
            if (mimeType == null || !mimeType.startsWith("text/")) {
                return null;
            } else {
                return Utils.createReader(file);
            }
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
