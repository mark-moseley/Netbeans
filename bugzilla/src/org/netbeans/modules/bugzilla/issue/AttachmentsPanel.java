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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import org.eclipse.core.runtime.CoreException;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class AttachmentsPanel extends JPanel {
    private static final Color BG_COLOR = new Color(220, 220, 220);
    private BugzillaIssue issue;
    private List<AttachmentPanel> newAttachments;
    private JLabel noneLabel;
    private LinkButton createNewButton;
    private JLabel dummyLabel = new JLabel();

    public AttachmentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        noneLabel = new JLabel(bundle.getString("AttachmentsPanel.noneLabel.text")); // NOI18N
        createNewButton = new LinkButton(new CreateNewAction());
    }

    public void setIssue(BugzillaIssue issue) {
        this.issue = issue;
        newAttachments = new LinkedList<AttachmentPanel>();
        removeAll();

        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.LEADING);
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        GroupLayout.SequentialGroup newVerticalGroup = layout.createSequentialGroup();

        BugzillaIssue.Attachment[] attachments = issue.getAttachments();
        boolean noAttachments = (attachments.length == 0);
        horizontalGroup.add(layout.createSequentialGroup()
            .add(noneLabel)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(noAttachments ? createNewButton : dummyLabel)
            .add(0, 0, Short.MAX_VALUE));
        verticalGroup.add(layout.createParallelGroup(GroupLayout.BASELINE)
            .add(noneLabel)
            .add(noAttachments ? createNewButton : dummyLabel));
        dummyLabel.setVisible(false);
        noneLabel.setVisible(noAttachments);
        updateCreateNewButton(noAttachments);
        if (noAttachments) {
            // noneLabel + createNewButton
            verticalGroup.add(newVerticalGroup);
        } else {
            JLabel descriptionLabel = new JLabel(bundle.getString("AttachmentsPanel.table.description")); // NOI18N
            JLabel filenameLabel = new JLabel(bundle.getString("AttachmentsPanel.table.filename")); // NOI18N
            JLabel dateLabel =  new JLabel(bundle.getString("AttachmentsPanel.table.date")); // NOI18N
            JLabel authorLabel = new JLabel(bundle.getString("AttachmentsPanel.table.author")); // NOI18N
            makeBold(descriptionLabel);
            makeBold(filenameLabel);
            makeBold(dateLabel);
            makeBold(authorLabel);
            GroupLayout.ParallelGroup descriptionGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup filenameGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup dateGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup authorGroup = layout.createParallelGroup();
            int descriptionWidth = Math.max(descriptionLabel.getPreferredSize().width, 150);
            descriptionGroup.add(descriptionLabel, GroupLayout.PREFERRED_SIZE, descriptionWidth, GroupLayout.PREFERRED_SIZE);
            filenameGroup.add(filenameLabel);
            dateGroup.add(dateLabel);
            authorGroup.add(authorLabel);
            JPanel panel = createHighlightPanel();
            GroupLayout.ParallelGroup horizontalSubgroup = layout.createParallelGroup(GroupLayout.LEADING, false);
            horizontalGroup.add(horizontalSubgroup
                .add(panel, 0, 0, Short.MAX_VALUE)
                .add(layout.createSequentialGroup()
                    .add(descriptionGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(filenameGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(dateGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(authorGroup)));
            verticalGroup.add(layout.createParallelGroup(GroupLayout.LEADING, false)
                .add(panel, 0, 0, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(descriptionLabel)
                    .add(filenameLabel)
                    .add(dateLabel)
                    .add(authorLabel)));
            for (BugzillaIssue.Attachment attachment : attachments) {
                JPopupMenu menu = menuFor(attachment);
                String description = attachment.getDesc();
                String filename = attachment.getFilename();
                Date date = attachment.getDate();
                String author = attachment.getAuthor();
                descriptionLabel = new JLabel(description);
                LinkButton filenameButton = new LinkButton();
                filenameButton.setAction(new DefaultAttachmentAction(attachment));
                filenameButton.setText(filename);
                dateLabel = new JLabel(DateFormat.getDateInstance().format(date));
                authorLabel = new JLabel(author);
                descriptionLabel.setComponentPopupMenu(menu);
                filenameButton.setComponentPopupMenu(menu);
                dateLabel.setComponentPopupMenu(menu);
                authorLabel.setComponentPopupMenu(menu);
                descriptionGroup.add(descriptionLabel);
                filenameGroup.add(filenameButton);
                dateGroup.add(dateLabel);
                authorGroup.add(authorLabel);
                panel = createHighlightPanel();
                panel.addMouseListener(new MouseAdapter() {}); // Workaround for bug 6272233
                panel.setComponentPopupMenu(menu);
                horizontalSubgroup.add(panel, 0, 0, Short.MAX_VALUE);
                verticalGroup
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(panel, 0, 0, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(GroupLayout.BASELINE)
                            .add(descriptionLabel)
                            .add(filenameButton)
                            .add(dateLabel)
                            .add(authorLabel)));
            }
            verticalGroup.add(newVerticalGroup);
        }
        horizontalGroup.add(layout.createSequentialGroup()
                .add(noAttachments ? dummyLabel : createNewButton)
                .add(0, 0, Short.MAX_VALUE));
        verticalGroup.addPreferredGap(LayoutStyle.RELATED);
        verticalGroup.add(noAttachments ? dummyLabel : createNewButton);

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        ((CreateNewAction)createNewButton.getAction()).setLayoutGroups(horizontalGroup, newVerticalGroup);
        setLayout(layout);
    }

    private JPopupMenu menuFor(Attachment attachment) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new DefaultAttachmentAction(attachment));
        menu.add(new SaveAttachmentAction(attachment));
        return menu;
    }

    private void updateCreateNewButton(boolean noAttachments) {
        String createNewButtonText = NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.createNewButton.text"); // NOI18N
        createNewButton.setText(noAttachments ? ('('+createNewButtonText+')') : createNewButtonText);
    }

    private void makeBold(JLabel label) {
        Font font = label.getFont().deriveFont(Font.BOLD);
        label.setFont(font);
    }

    private JPanel createHighlightPanel() {
        JPanel panel = new JPanel();
        // PENDING what color (e.g. what key from UIDefaults) should I use?
        panel.setBackground(BG_COLOR);
        add(panel);
        return panel;
    }

    private PropertyChangeListener deletedListener;
    PropertyChangeListener getDeletedListener() {
        if (deletedListener == null) {
            deletedListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    for (AttachmentPanel panel : newAttachments) {
                        if (!panel.isDeleted()) {
                            return;
                        }
                    }
                    // The last attachment deleted
                    noneLabel.setVisible(true);
                    switchHelper();
                    updateCreateNewButton(true);
                }
            };
        }
        return deletedListener;
    }

    private void switchHelper() {
        JLabel temp = new JLabel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyLabel, temp);
        layout.replace(createNewButton, dummyLabel);
        layout.replace(temp, createNewButton);
    }

    List<AttachmentInfo> getNewAttachments() {
        List<AttachmentInfo> infos = new LinkedList<AttachmentInfo>();
        for (AttachmentPanel attachment : newAttachments) {
            if (!attachment.isDeleted()) {
                AttachmentInfo info = new AttachmentInfo();
                info.file = attachment.getFile();
                info.description = attachment.getDescription();
                info.contentType = attachment.getContentType();
                info.isPatch = attachment.isPatch();
                infos.add(info);
            }
        }
        return infos;
    }

    class AttachmentInfo {
        File file;
        String description;
        String contentType;
        boolean isPatch;
    }

    class CreateNewAction extends AbstractAction {
        private GroupLayout.ParallelGroup horizontalGroup;
        private GroupLayout.SequentialGroup verticalGroup;

        void setLayoutGroups(GroupLayout.ParallelGroup horizontalGroup,
                GroupLayout.SequentialGroup verticalGroup) {
            this.horizontalGroup = horizontalGroup;
            this.verticalGroup = verticalGroup;
        }

        public void actionPerformed(ActionEvent e) {
            AttachmentPanel attachment = new AttachmentPanel();
            attachment.setBackground(BG_COLOR);
            horizontalGroup.add(attachment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            verticalGroup.addPreferredGap(LayoutStyle.RELATED);
            verticalGroup.add(attachment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            if (noneLabel.isVisible()) {
                noneLabel.setVisible(false);
                switchHelper();
                updateCreateNewButton(false);
            }
            if (issue.getAttachments().length == 0) {
                attachment.addPropertyChangeListener(getDeletedListener());
            }
            newAttachments.add(attachment);
            revalidate();
        }

    }

    static class DefaultAttachmentAction extends AbstractAction {
        private Attachment attachment;

        public DefaultAttachmentAction(Attachment attachment) {
            this.attachment = attachment;
            putValue(Action.NAME, NbBundle.getMessage(DefaultAttachmentAction.class, "AttachmentsPanel.DefaultAttachmentAction.name")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        String filename = attachment.getFilename();
                        int index = filename.lastIndexOf('.'); // NOI18N
                        String prefix = (index == -1) ? filename : filename.substring(0, index);
                        String suffix = (index == -1) ? null : filename.substring(index);
                        File file = File.createTempFile(prefix, suffix);
                        attachment.getAttachementData(new FileOutputStream(file));
                        String contentType = attachment.getContentType();
                        if ("image/png".equals(contentType) // NOI18N
                                || "image/gif".equals(contentType) // NOI18N
                                || "image/jpeg".equals(contentType)) { // NOI18N
                            HtmlBrowser.URLDisplayer.getDefault().showURL(file.toURL());
                        } else {
                            FileObject fob = FileUtil.toFileObject(file);
                            DataObject dob = DataObject.find(fob);
                            OpenCookie open = dob.getCookie(OpenCookie.class);
                            if (open != null) {
                                open.open();
                            } else {
                                // PENDING
                            }
                        }
                    } catch (DataObjectNotFoundException dnfex) {
                        dnfex.printStackTrace();
                    } catch (CoreException cex) {
                        cex.printStackTrace();
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    }
                }
            });
        }

    }

    static class SaveAttachmentAction extends AbstractAction {
        private Attachment attachment;

        public SaveAttachmentAction(Attachment attachment) {
            this.attachment = attachment;
            putValue(Action.NAME, NbBundle.getMessage(SaveAttachmentAction.class, "AttachmentsPanel.SaveAttachmentAction.name")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            final File file = new FileChooserBuilder(AttachmentsPanel.class)
                    .setFilesOnly(true).showSaveDialog();
            if (file != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            attachment.getAttachementData(new FileOutputStream(file));
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        } catch (CoreException cex) {
                            cex.printStackTrace();
                        }
                    }
                });
            }
        }

    }

}
