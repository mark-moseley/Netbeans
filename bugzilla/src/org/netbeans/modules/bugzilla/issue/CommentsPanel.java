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
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    private final static String HL_ATTRIBUTE = "linkact"; // NOI18N
    private BugzillaIssue issue;
    private MouseInputListener listener;

    public CommentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        listener = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    JTextPane pane = (JTextPane)e.getSource();
                    StyledDocument doc = pane.getStyledDocument();
                    Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                    AttributeSet as = elem.getAttributes();
                    StackTraceAction a = (StackTraceAction) as.getAttribute(HL_ATTRIBUTE);
                    if (a != null) {
                        a.openStackTrace(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                    }
                } catch(Exception ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JTextPane pane = (JTextPane)e.getSource();
                StyledDocument doc = pane.getStyledDocument();
                Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                if (StyleConstants.isUnderline(as)) {
                    pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    pane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
    }

    public void setIssue(BugzillaIssue issue) {
        removeAll();
        this.issue = issue;
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.LEADING);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .add(horizontalGroup)
            .addContainerGap());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(verticalGroup));
        addSection(issue.getFieldValue(BugzillaIssue.IssueField.DESCRIPTION), issue.getFieldValue(BugzillaIssue.IssueField.REPORTER), issue.getFieldValue(BugzillaIssue.IssueField.CREATION), horizontalGroup, verticalGroup, true);
        for (BugzillaIssue.Comment comment : issue.getComments()) {
            String when = DateFormat.getDateTimeInstance().format(comment.getWhen());
            addSection(comment.getText(), comment.getWho(), when, horizontalGroup, verticalGroup, false);
        }
        verticalGroup.addContainerGap();
    }

    private void addSection(String text, String author, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        JTextPane textPane = new JTextPane();
        JLabel leftLabel = new JLabel();
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt;
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            leftTxt = MessageFormat.format(leftFormat, issue.getSummary());
        } else {
            leftTxt = bundle.getString("CommentsPanel.leftLabel.text"); // NOI18N
        }
        leftLabel.setText(leftTxt);
        JLabel rightLabel = new JLabel();
        String rightFormat = bundle.getString("CommentsPanel.rightLabel.format"); // NOI18N
        String rightTxt = MessageFormat.format(rightFormat, dateTimeString, author);
        rightLabel.setText(rightTxt);
        setupTextPane(textPane, text);

        // Layout
        GroupLayout layout = (GroupLayout)getLayout();
        horizontalGroup.add(layout.createSequentialGroup()
            .add(leftLabel)
            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(rightLabel))
        .add(textPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.UNRELATED);
        }
        verticalGroup.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(leftLabel)
            .add(rightLabel))
            .addPreferredGap(LayoutStyle.RELATED)
            .add(textPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    private void setupTextPane(JTextPane textPane, String comment) {
        List<StackTraceSupport.StackTracePosition> stacktraces = StackTraceSupport.find(comment);
        if(stacktraces.isEmpty()) {
            textPane.setText(comment);
        } else {
            StyledDocument doc = textPane.getStyledDocument();
            Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            Style hlStyle = doc.addStyle("regularBlue", defStyle); // NOI18N
            StyleConstants.setForeground(hlStyle, Color.BLUE);
            StyleConstants.setUnderline(hlStyle, true);

            int last = 0;
            textPane.setText(""); // NOI18N
            for (StackTraceSupport.StackTracePosition stp : stacktraces) {
                int start = stp.getStartOffset();
                int end = stp.getEndOffset();

                String st = comment.substring(start, end);
                hlStyle.addAttribute(HL_ATTRIBUTE, new StackTraceAction());
                try {
                    doc.insertString(doc.getLength(), comment.substring(last, start), defStyle);
                    doc.insertString(doc.getLength(), st, hlStyle);
                } catch (BadLocationException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
                last = end;
            }
            try {
                doc.insertString(doc.getLength(), comment.substring(last), defStyle);
            } catch (BadLocationException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }
        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), // NOI18N
                BorderFactory.createEmptyBorder(3,3,3,3)));
        textPane.setEditable(false);
        textPane.addMouseListener(listener);
        textPane.addMouseMotionListener(listener);
    }

    private static class StackTraceAction {
        void openStackTrace(String text) {
            StackTraceSupport.findAndOpen(text);
        }
    }

}
