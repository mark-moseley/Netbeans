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

package org.netbeans.modules.subversion.ui.blame;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;
import org.tigris.subversion.svnclientadapter.*;
import javax.swing.*;
import java.io.File;
import java.util.*;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClientFactory;

/**
 *
 * @author Maros Sandor
 */
public class BlameAction extends ContextAction {
    
    protected String getBaseName(Node [] activatedNodes) {
        if (visible(activatedNodes)) {
            return "CTL_MenuItem_HideAnnotations";  // NOI18N
        } else {
            return "CTL_MenuItem_ShowAnnotations"; // NOI18N
        }
    }

    public boolean enable(Node[] nodes) {
        return super.enable(nodes) && activatedEditorCookie(nodes) != null;
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected void performContextAction(Node[] nodes) {        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }        
        if (visible(nodes)) {
            JEditorPane pane = activatedEditorPane(nodes);
            AnnotationBarManager.hideAnnotationBar(pane);
        } else {
            EditorCookie ec = activatedEditorCookie(nodes);
            if (ec == null) return;
            
            final File file = activatedFile(nodes);

            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes == null) {
                ec.open();
            }
            panes = ec.getOpenedPanes();
            if (panes == null) {
                return;
            }
            final JEditorPane currentPane = panes[0];
            
            final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationSubstitute")); // NOI18N;
            
            long revision = Subversion.getInstance().getStatusCache().getStatus(file).getEntry(file).getRevision().getNumber();                        
            
            SVNUrl repository;
            try {            
                repository = SvnUtils.getRepositoryRootUrl(file);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }                                                     
            
            ab.setSVNClienListener(new SVNClientListener(revision, repository, file, ab));
            
            computeAnnotations(repository, file, ab);                        
        }
    }

    private void computeAnnotations(SVNUrl repository, final File file, final AnnotationBar ab) {
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {                    
                computeAnnotations(file, this, ab);
            }
        };
        support.start(rp, repository, NbBundle.getMessage(BlameAction.class, "MSG_Annotation_Progress")); // NOI18N        
    }
            
    
    private void computeAnnotations(File file, SvnProgressSupport progress, AnnotationBar ab) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(file, progress);
        } catch (SVNClientException ex) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        ISVNAnnotations annotations;
        try {
            annotations = client.annotate(file, new SVNRevision.Number(1), SVNRevision.BASE);
        } catch (SVNClientException e) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            SvnClientExceptionHandler.notifyException(e, true, true);
            return;
        }
        if (progress.isCanceled()) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            return;
        }
        AnnotateLine [] lines = toAnnotateLines(annotations);
        ab.annotationLines(file, Arrays.asList(lines));
        
        // fetch log messages
        ISVNLogMessage [] logs;
        try {
            logs = client.getLogMessages(file, new SVNRevision.Number(1), SVNRevision.BASE, false, false);
        } catch (SVNClientException e) {
            progress.annotate(e);
            return;
        }
        if (progress.isCanceled()) {
            return;
        }
        fillCommitMessages(lines, logs);
    }

    private static void fillCommitMessages(AnnotateLine [] annotations, ISVNLogMessage[] logs) {
        long lowestRevisionNumber = Long.MAX_VALUE;
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            for (int j = 0; j < logs.length; j++) {
                ISVNLogMessage log = logs[j];
                if (log.getRevision().getNumber() < lowestRevisionNumber) {
                    lowestRevisionNumber = log.getRevision().getNumber(); 
                }
                if (annotation.getRevision().equals(log.getRevision().toString())) {
                    annotation.setDate(log.getDate());
                    annotation.setCommitMessage(log.getMessage());
                }
            }
        }
        String lowestRev = Long.toString(lowestRevisionNumber);
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            annotation.setCanBeRolledBack(!annotation.getRevision().equals(lowestRev));
        }
    }

    private static AnnotateLine [] toAnnotateLines(ISVNAnnotations annotations) {
        AnnotateLine [] lines = new AnnotateLine[annotations.numberOfLines()];
        int n = annotations.numberOfLines();
        for (int i = 0; i < n; i++) {
            lines[i] = new AnnotateLine();
            lines[i].setAuthor(annotations.getAuthor(i));
            lines[i].setContent(annotations.getLine(i));
            lines[i].setLineNum(i + 1);
            lines[i].setRevision(Long.toString(annotations.getRevision(i)));
            lines[i].setDate(annotations.getChanged(i));
        }
        return lines;
    }

    /**
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public boolean visible(Node[] nodes) {
        JEditorPane currentPane = activatedEditorPane(nodes);
        return AnnotationBarManager.annotationBarVisible(currentPane);
    }

    /**
     * @return active editor pane or null if selected node
     * does not have any or more nodes selected.
     */
    private JEditorPane activatedEditorPane(Node[] nodes) {
        EditorCookie ec = activatedEditorCookie(nodes);        
        if (ec != null && SwingUtilities.isEventDispatchThread()) {              
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return null;
    }

    private EditorCookie activatedEditorCookie(Node[] nodes) {
        if (nodes == null) {
            nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        }
        if (nodes.length == 1) {
            Node node = nodes[0];
            return (EditorCookie) node.getCookie(EditorCookie.class);
        }
        return null;
    }

    private File activatedFile(Node[] nodes) {
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                return FileUtil.toFile(fo);
            }
        }
        return null;
    }

    private class SVNClientListener implements ISVNNotifyListener {
        
        private final SVNUrl repository;
        private final File file;
        private final AnnotationBar ab;
        
        private long revision = -1;
        
        private File notifiedFile = null;
        
        public SVNClientListener(long revision, SVNUrl repository, final File file, final AnnotationBar ab) {
            this.revision = revision;
            this.repository = repository;
            this.ab = ab;
            this.file = file;
        }                
        
        public void setCommand(int arg0) {            
            // do nothing
        }

        public void logCommandLine(String arg0) {
            // do nothing
        }

        public void logMessage(String arg0) {
            // do nothing
        }

        public void logError(String arg0) {
            // do nothing
        }

        public void logRevision(long newRevision, String path) {
            if(notifiedFile == null) {
                return;
            }
            if(notifiedFile.getAbsolutePath().equals(file.getAbsolutePath()) && revision != newRevision) {
                computeAnnotations(repository, file, ab);
                revision = newRevision;
                notifiedFile = null;
            }
        }

        public void logCompleted(String arg0) {
            // do nothing
        }

        public void onNotify(File file, SVNNodeKind nodeKind) {
            notifiedFile = file;
        }        
    }
    
}
