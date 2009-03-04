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
import java.awt.EventQueue;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.eclipse.core.runtime.CoreException;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel showing (and allowing to edit) details of an issue.
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel {
    private static final Color HIGHLIGHT_COLOR = new Color(217, 255, 217);
    private BugzillaIssue issue;
    private CommentsPanel commentsPanel;
    private int resolvedIndex;
    private Map<BugzillaIssue.IssueField,String> initialValues = new HashMap<BugzillaIssue.IssueField,String>();

    public IssuePanel() {
        initComponents();
        reportedField.setBackground(getBackground());
        modifiedField.setBackground(getBackground());
        resolutionField.setBackground(getBackground());
        Font font = headerLabel.getFont();
        headerLabel.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateLabel.setVisible(false);
        duplicateField.setVisible(false);

        // Comments panel
        commentsPanel = new CommentsPanel();
        JScrollPane scrollPane = new JScrollPane(commentsPanel);
        scrollPane.setBorder(null);
        ((GroupLayout)getLayout()).replace(dummyCommentsPanel, scrollPane);
    }

    void reloadFormInAWT(final boolean force) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                reloadForm(force);
            }
        });
    }

    public void setIssue(BugzillaIssue issue) {
        if (this.issue == null) {
            issue.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                        reloadForm(false);
                    }
                }
            });
        }
        this.issue = issue;
        try {
            initCombos();
            reloadForm(true);
        } catch (CoreException cex) {
            cex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private void reloadForm(boolean force) {
        String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
        String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
        headerLabel.setText(headerTxt);
        reloadField(force, productCombo, BugzillaIssue.IssueField.PRODUCT);
        reloadField(force, componentCombo, BugzillaIssue.IssueField.COMPONENT);
        reloadField(force, versionCombo, BugzillaIssue.IssueField.VERSION);
        reloadField(force, platformCombo, BugzillaIssue.IssueField.PLATFORM);
        reloadField(force, resolutionField, BugzillaIssue.IssueField.RESOLUTION); // Must be before statusCombo
        String status = reloadField(force, statusCombo, BugzillaIssue.IssueField.STATUS);
        initStatusCombo(status);
        reloadField(force, resolutionCombo, BugzillaIssue.IssueField.RESOLUTION);
        String initialResolution = initialValues.get(BugzillaIssue.IssueField.RESOLUTION);
        if ("DUPLICATE".equals(initialResolution)) { // NOI18N
            duplicateField.setEditable(false);
            duplicateField.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            duplicateField.setBackground(getBackground());
        } else {
            JTextField field = new JTextField();
            duplicateField.setEditable(true);
            duplicateField.setBorder(field.getBorder());
            duplicateField.setBackground(field.getBackground());
        }
        reloadField(force, priorityCombo, BugzillaIssue.IssueField.PRIORITY);
        reloadField(force, severityCombo, BugzillaIssue.IssueField.SEVERITY);
        reloadField(force, targetMilestoneCombo, BugzillaIssue.IssueField.MILESTONE);
        reloadField(force, urlField, BugzillaIssue.IssueField.URL);
        reloadField(force, keywordsField, BugzillaIssue.IssueField.KEYWORDS);
        format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
        String reportedTxt = MessageFormat.format(format, issue.getFieldValue(BugzillaIssue.IssueField.CREATION), issue.getFieldValue(BugzillaIssue.IssueField.REPORTER));
        reportedField.setText(reportedTxt);
        modifiedField.setText(issue.getFieldValue(BugzillaIssue.IssueField.MODIFICATION));
        reloadField(force, assignedField, BugzillaIssue.IssueField.ASSIGNED_TO);
        reloadField(force, qaContactField, BugzillaIssue.IssueField.QA_CONTACT);
        reloadField(force, ccField, BugzillaIssue.IssueField.CC);
        reloadField(force, dependsField, BugzillaIssue.IssueField.DEPENDS_ON);
        reloadField(force, blocksField, BugzillaIssue.IssueField.BLOCKS);
        commentsPanel.setIssue(issue);
        if (force) {
            addCommentArea.setText(""); // NOI18N
        }
        updateFieldStatuses();
    }

    private String reloadField(boolean force, JComponent component, BugzillaIssue.IssueField field) {
        String currentValue = null;
        if (!force) {
            if (component instanceof JComboBox) {
                currentValue = ((JComboBox)component).getSelectedItem().toString();
            } else if (component instanceof JTextField) {
                currentValue = ((JTextField)component).getText();
            }
        }
        String initialValue = initialValues.get(field);
        String newValue = issue.getFieldValue(field);
        if (force || currentValue.equals(initialValue)) {
            if (component instanceof JComboBox) {
                ((JComboBox)component).setSelectedItem(newValue);
            } else if (component instanceof JTextField) {
                ((JTextField)component).setText(newValue);
            }
            currentValue = newValue;
        } else {
            // PENDING conflict during refresh
        }
        initialValues.put(field, newValue);
        return currentValue;
    }

    private void initCombos() throws CoreException, IOException {
        Bugzilla bugzilla = Bugzilla.getInstance();
        BugzillaRepository repository = issue.getRepository();
        productCombo.setModel(toComboModel(bugzilla.getProducts(repository)));
        // componentCombo, versionCombo, targetMilestoneCombo are filled
        // automatically when productCombo is set/changed
        platformCombo.setModel(toComboModel(bugzilla.getPlatforms(repository)));
        // Do not support MOVED resolution (yet?)
        List<String> resolutions = new LinkedList<String>(bugzilla.getResolutions(repository));
        resolutions.remove("MOVED"); // NOI18N
        resolutionCombo.setModel(toComboModel(resolutions));
        priorityCombo.setModel(toComboModel(bugzilla.getPriorities(repository)));
        severityCombo.setModel(toComboModel(bugzilla.getSeverities(repository)));
        // stausCombo and resolution fields are filled in reloadForm
    }

    private void initStatusCombo(String status) {
        try {
            // Init statusCombo - allowed transitions (heuristics):
            // Open -> Open-Unconfirmed-Reopened+Resolved
            // Resolved -> Reopened+Close
            // Close-Resolved -> Reopened+Resolved+(Close with higher index)
            Bugzilla bugzilla = Bugzilla.getInstance();
            BugzillaRepository repository = issue.getRepository();
            List<String> allStatuses = bugzilla.getStatusValues(repository);
            List<String> openStatuses = bugzilla.getOpenStatusValues(repository);
            List<String> statuses = new LinkedList<String>();
            String unconfirmed = "UNCONFIRMED"; // NOI18N
            String reopened = "REOPENED"; // NOI18N
            String resolved = "RESOLVED"; // NOI18N
            if (openStatuses.contains(status)) {
                statuses.addAll(openStatuses);
                if (!unconfirmed.equals(status)) {
                    statuses.remove(unconfirmed);
                }
                if (!reopened.equals(status)) {
                    statuses.remove(reopened);
                }
                statuses.add(resolved);
            } else {
                if (allStatuses.contains(reopened)) {
                    statuses.add(reopened);
                } else {
                    // Pure guess
                    statuses.addAll(openStatuses);
                    statuses.remove(unconfirmed);
                }
                if (resolved.equals(status)) {
                    List<String> closedStatuses = new LinkedList<String>(allStatuses);
                    closedStatuses.removeAll(openStatuses);
                    statuses.addAll(closedStatuses);
                } else {
                    statuses.add(resolved);
                    for (int i=allStatuses.indexOf(status); i<allStatuses.size(); i++) {
                        String s = allStatuses.get(i);
                        if (!openStatuses.contains(s)) {
                            statuses.add(s);
                        }
                    }
                }
                resolvedIndex = statuses.indexOf(resolved);
            }
            statusCombo.setModel(toComboModel(statuses));
            statusCombo.setSelectedItem(status);
        } catch (CoreException cex) {
            cex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private ComboBoxModel toComboModel(List<String> items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private void updateFieldStatuses() {
        updateFieldStatus(BugzillaIssue.IssueField.PRODUCT, productLabel);
        updateFieldStatus(BugzillaIssue.IssueField.COMPONENT, componentLabel);
        updateFieldStatus(BugzillaIssue.IssueField.VERSION, versionLabel);
        updateFieldStatus(BugzillaIssue.IssueField.PLATFORM, platformLabel);
        updateFieldStatus(BugzillaIssue.IssueField.STATUS, statusLabel);
        updateFieldStatus(BugzillaIssue.IssueField.RESOLUTION, resolutionLabel);
        updateFieldStatus(BugzillaIssue.IssueField.PRIORITY, priorityLabel);
        updateFieldStatus(BugzillaIssue.IssueField.SEVERITY, severityLabel);
        updateFieldStatus(BugzillaIssue.IssueField.MILESTONE, targetMilestoneLabel);
        updateFieldStatus(BugzillaIssue.IssueField.URL, urlLabel);
        updateFieldStatus(BugzillaIssue.IssueField.KEYWORDS, keywordsLabel);
        updateFieldStatus(BugzillaIssue.IssueField.ASSIGNED_TO, assignedLabel);
        updateFieldStatus(BugzillaIssue.IssueField.QA_CONTACT, qaContactLabel);
        updateFieldStatus(BugzillaIssue.IssueField.CC, ccLabel);
        updateFieldStatus(BugzillaIssue.IssueField.DEPENDS_ON, dependsLabel);
        updateFieldStatus(BugzillaIssue.IssueField.BLOCKS, blocksLabel);
    }

    private void updateFieldStatus(BugzillaIssue.IssueField field, JLabel label) {
        boolean highlight = (issue.getFieldStatus(field) != BugzillaIssue.FIELD_STATUS_UPTODATE);
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(HIGHLIGHT_COLOR);
        }
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, JComboBox combo) {
        storeFieldValue(field, combo.getSelectedItem().toString());
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, JTextField textField) {
        storeFieldValue(field, textField.getText());
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, String value) {
        if (!value.equals(initialValues.get(field))) {
            issue.setFieldValue(field, value);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resolutionField = new javax.swing.JTextField();
        productLabel = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        platformLabel = new javax.swing.JLabel();
        productCombo = new javax.swing.JComboBox();
        componentCombo = new javax.swing.JComboBox();
        versionCombo = new javax.swing.JComboBox();
        platformCombo = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        severityLabel = new javax.swing.JLabel();
        severityCombo = new javax.swing.JComboBox();
        targetMilestoneLabel = new javax.swing.JLabel();
        targetMilestoneCombo = new javax.swing.JComboBox();
        urlLabel = new javax.swing.JLabel();
        urlField = new javax.swing.JTextField();
        keywordsLabel = new javax.swing.JLabel();
        keywordsField = new javax.swing.JTextField();
        reportedLabel = new javax.swing.JLabel();
        reportedField = new javax.swing.JTextField();
        modifiedLabel = new javax.swing.JLabel();
        modifiedField = new javax.swing.JTextField();
        assignedLabel = new javax.swing.JLabel();
        assignedField = new javax.swing.JTextField();
        qaContactLabel = new javax.swing.JLabel();
        qaContactField = new javax.swing.JTextField();
        ccLabel = new javax.swing.JLabel();
        ccField = new javax.swing.JTextField();
        dependsLabel = new javax.swing.JLabel();
        dependsField = new javax.swing.JTextField();
        blocksLabel = new javax.swing.JLabel();
        blocksField = new javax.swing.JTextField();
        dummyLabel1 = new javax.swing.JLabel();
        dummyLabel2 = new javax.swing.JLabel();
        addCommentLabel = new javax.swing.JLabel();
        scrollPane1 = new javax.swing.JScrollPane();
        addCommentArea = new javax.swing.JTextArea();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        dummyCommentsPanel = new javax.swing.JPanel();
        attachmentsLabel = new javax.swing.JLabel();
        attachmentsPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        headerLabel = new javax.swing.JLabel();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        duplicateField = new javax.swing.JTextField();
        duplicateLabel = new javax.swing.JLabel();
        keywordsButton = new javax.swing.JButton();

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        productLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text")); // NOI18N

        componentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        versionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.versionLabel.text")); // NOI18N

        platformLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.platformLabel.text")); // NOI18N

        productCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productComboActionPerformed(evt);
            }
        });

        statusLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        resolutionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboActionPerformed(evt);
            }
        });

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        severityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityLabel.text")); // NOI18N

        targetMilestoneLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.targetMilestoneLabel.text")); // NOI18N

        urlLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlLabel.text")); // NOI18N

        urlField.setColumns(15);

        keywordsLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsLabel.text")); // NOI18N

        keywordsField.setColumns(15);

        reportedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text")); // NOI18N

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        modifiedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text")); // NOI18N

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        assignedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignedLabel.text")); // NOI18N

        assignedField.setColumns(15);

        qaContactLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.qaContactLabel.text")); // NOI18N

        qaContactField.setColumns(15);

        ccLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        ccField.setColumns(15);

        dependsLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsLabel.text")); // NOI18N

        dependsField.setColumns(15);

        blocksLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksLabel.text")); // NOI18N

        blocksField.setColumns(15);

        addCommentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text")); // NOI18N

        addCommentArea.setColumns(20);
        addCommentArea.setRows(5);
        scrollPane1.setViewportView(addCommentArea);

        submitButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        attachmentsLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout attachmentsPanelLayout = new org.jdesktop.layout.GroupLayout(attachmentsPanel);
        attachmentsPanel.setLayout(attachmentsPanelLayout);
        attachmentsPanelLayout.setHorizontalGroup(
            attachmentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        attachmentsPanelLayout.setVerticalGroup(
            attachmentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        duplicateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateLabel.text")); // NOI18N

        keywordsButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.text")); // NOI18N
        keywordsButton.setFocusPainted(false);
        keywordsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(dummyCommentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(attachmentsLabel)
                    .add(productLabel)
                    .add(componentLabel)
                    .add(versionLabel)
                    .add(platformLabel)
                    .add(statusLabel)
                    .add(resolutionLabel)
                    .add(priorityLabel)
                    .add(severityLabel)
                    .add(targetMilestoneLabel)
                    .add(dummyLabel1)
                    .add(urlLabel)
                    .add(keywordsLabel)
                    .add(dummyLabel2)
                    .add(addCommentLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(submitButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(keywordsField)
                                .add(0, 0, 0)
                                .add(keywordsButton))
                            .add(urlField)
                            .add(targetMilestoneCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(severityCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, priorityCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(resolutionCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(statusCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(productCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(componentCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(versionCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(platformCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(reportedLabel)
                            .add(modifiedLabel)
                            .add(assignedLabel)
                            .add(qaContactLabel)
                            .add(ccLabel)
                            .add(dependsLabel)
                            .add(blocksLabel)
                            .add(duplicateLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(reportedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(blocksField)
                                .add(dependsField)
                                .add(ccField)
                                .add(assignedField)
                                .add(modifiedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(qaContactField)
                                .add(duplicateField))))
                    .add(scrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .add(attachmentsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(headerLabel)
                .addContainerGap(470, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(430, Short.MAX_VALUE)
                .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, submitButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(headerLabel)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(productLabel)
                            .add(productCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(reportedLabel)
                            .add(reportedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(componentLabel)
                            .add(componentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(modifiedLabel)
                            .add(modifiedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(versionLabel)
                            .add(versionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(platformLabel)
                    .add(platformCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(assignedLabel)
                    .add(assignedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(qaContactLabel)
                    .add(qaContactField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ccLabel)
                    .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusLabel)
                    .add(ccField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resolutionLabel)
                    .add(resolutionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(duplicateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(duplicateLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dependsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dependsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(blocksLabel)
                    .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(priorityLabel)
                    .add(blocksField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(severityLabel)
                    .add(severityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetMilestoneLabel)
                    .add(targetMilestoneCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keywordsLabel)
                    .add(keywordsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(attachmentsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dummyLabel2))
                    .add(attachmentsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addCommentLabel)
                    .add(scrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(submitButton)
                    .add(cancelButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyCommentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 11, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {dummyLabel1, dummyLabel2, targetMilestoneCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

        layout.linkSize(new java.awt.Component[] {assignedLabel, attachmentsLabel, blocksLabel, ccLabel, componentLabel, dependsLabel, keywordsLabel, platformLabel, priorityLabel, productLabel, qaContactLabel, resolutionLabel, severityLabel, statusCombo, statusLabel, targetMilestoneLabel, urlLabel, versionLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        Bugzilla bugzilla = Bugzilla.getInstance();
        BugzillaRepository repository = issue.getRepository();
        String product = productCombo.getSelectedItem().toString();
        try {
            Object component = componentCombo.getSelectedItem();
            Object version = versionCombo.getSelectedItem();
            Object targetMilestone = targetMilestoneCombo.getSelectedItem();
            componentCombo.setModel(toComboModel(bugzilla.getComponents(repository, product)));
            versionCombo.setModel(toComboModel(bugzilla.getVersions(repository, product)));
            targetMilestoneCombo.setModel(toComboModel(bugzilla.getTargetMilestones(repository, product)));
            // Attempt to keep selection
            componentCombo.setSelectedItem(component);
            versionCombo.setSelectedItem(version);
            targetMilestoneCombo.setSelectedItem(targetMilestone);
        } catch (CoreException cex) {
            cex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }//GEN-LAST:event_productComboActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        // Hide/show resolution combo
        String initialStatus = initialValues.get(BugzillaIssue.IssueField.STATUS);
        boolean resolvedInitial = "RESOLVED".equals(initialStatus); // NOI18N
        if (!resolvedInitial) {
            if ("RESOLVED".equals(statusCombo.getSelectedItem())) { // NOI18N
                if (resolutionCombo.getParent() == null) {
                    ((GroupLayout)getLayout()).replace(resolutionField, resolutionCombo);
                }
                resolutionCombo.setSelectedItem("FIXED"); // NOI18N
                resolutionCombo.setVisible(true);
            } else {
                resolutionCombo.setVisible(false);
                duplicateLabel.setVisible(false);
                duplicateField.setVisible(false);
            }
        }
        if (!resolutionField.getText().trim().equals("")) { // NOI18N
            if (statusCombo.getSelectedIndex() >= resolvedIndex) {
                if (resolutionField.getParent() == null) {
                    ((GroupLayout)getLayout()).replace(resolutionCombo, resolutionField);
                }
                resolutionField.setVisible(true);
            } else {
                resolutionField.setVisible(false);
            }
            duplicateLabel.setVisible(false);
            duplicateField.setVisible(false);
        }
    }//GEN-LAST:event_statusComboActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        storeFieldValue(BugzillaIssue.IssueField.PRODUCT, productCombo);
        storeFieldValue(BugzillaIssue.IssueField.COMPONENT, componentCombo);
        storeFieldValue(BugzillaIssue.IssueField.VERSION, versionCombo);
        storeFieldValue(BugzillaIssue.IssueField.PLATFORM, platformCombo);
        storeFieldValue(BugzillaIssue.IssueField.STATUS, statusCombo);
        if (resolutionCombo.isVisible()) {
            storeFieldValue(BugzillaIssue.IssueField.RESOLUTION, resolutionCombo);
        } else if (!resolutionField.isVisible()) {
            storeFieldValue(BugzillaIssue.IssueField.RESOLUTION, ""); // NOI18N
        }
        if (duplicateField.isVisible() && duplicateField.isEditable()) {
            issue.duplicate(duplicateField.getText());
        }
        storeFieldValue(BugzillaIssue.IssueField.PRIORITY, priorityCombo);
        storeFieldValue(BugzillaIssue.IssueField.SEVERITY, severityCombo);
        storeFieldValue(BugzillaIssue.IssueField.MILESTONE, targetMilestoneCombo);
        storeFieldValue(BugzillaIssue.IssueField.URL, urlField);
        storeFieldValue(BugzillaIssue.IssueField.KEYWORDS, keywordsField);
        storeFieldValue(BugzillaIssue.IssueField.ASSIGNED_TO, assignedField);
        storeFieldValue(BugzillaIssue.IssueField.QA_CONTACT, qaContactField);
        storeFieldValue(BugzillaIssue.IssueField.CC, ccField);
        storeFieldValue(BugzillaIssue.IssueField.DEPENDS_ON, dependsField);
        storeFieldValue(BugzillaIssue.IssueField.BLOCKS, blocksField);
        // PENDING attachment modifications
        if (!"".equals(addCommentArea.getText().trim())) { // NOI18N
            issue.addComment(addCommentArea.getText());
        }
        String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
        String submitMessage = MessageFormat.format(submitMessageFormat, issue.getID());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    issue.submit();
                    issue.refresh();
                } catch (CoreException cex) {
                    System.out.println(cex.getStatus().getMessage());
                    cex.printStackTrace();
                } finally {
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getID());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                issue.refresh();
                handle.finish();
                reloadFormInAWT(true);
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
        if (resolutionCombo.getParent() == null) {
            return;
        }
        boolean shown = "DUPLICATE".equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateLabel.setVisible(shown);
        duplicateField.setVisible(shown);
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        String keywords = BugzillaUtil.getKeywords("Specify keywords that apply to the issue.", keywordsField.getText(), issue.getRepository());
        keywordsField.setText(keywords);
    }//GEN-LAST:event_keywordsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JTextField assignedField;
    private javax.swing.JLabel assignedLabel;
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JPanel attachmentsPanel;
    private javax.swing.JTextField blocksField;
    private javax.swing.JLabel blocksLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField ccField;
    private javax.swing.JLabel ccLabel;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JTextField dependsField;
    private javax.swing.JLabel dependsLabel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JLabel dummyLabel1;
    private javax.swing.JLabel dummyLabel2;
    private javax.swing.JTextField duplicateField;
    private javax.swing.JLabel duplicateLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton keywordsButton;
    private javax.swing.JTextField keywordsField;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JTextField modifiedField;
    private javax.swing.JLabel modifiedLabel;
    private javax.swing.JComboBox platformCombo;
    private javax.swing.JLabel platformLabel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JComboBox productCombo;
    private javax.swing.JLabel productLabel;
    private javax.swing.JTextField qaContactField;
    private javax.swing.JLabel qaContactLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private javax.swing.JTextField reportedField;
    private javax.swing.JLabel reportedLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JComboBox severityCombo;
    private javax.swing.JLabel severityLabel;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JComboBox targetMilestoneCombo;
    private javax.swing.JLabel targetMilestoneLabel;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

}
