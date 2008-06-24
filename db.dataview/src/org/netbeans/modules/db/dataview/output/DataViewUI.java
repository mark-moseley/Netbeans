/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
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
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.output;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

/**
 * DataViewUI hosting display of design-level SQL test output.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewUI {

    private JButton commit;
    private JButton refreshButton;
    private JButton truncateButton;
    private JButton next;
    private JButton last;
    private JButton previous;
    private JButton first;
    private JButton deleteRow;
    private JButton insert;
    private JTextField refreshField;
    private JLabel totalRowsLabel;
    private JLabel limitRow;
    private JButton[] editButtons = new JButton[5];
    private String imgPrefix = "/org/netbeans/modules/db/dataview/images/";
    private DataViewTablePanel dataPanel;
    private final DataView dataView;
    private JButton cancel;
    private DataViewActionHandler actionHandler;

    DataViewUI(DataView dataView, int toolbarType) {
        this.dataView = dataView;

        //do not show tab view if there is only one tab
        dataView.putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N

        dataView.putClientProperty("PersistenceType", "Never"); //NOI18N

        dataView.setLayout(new BorderLayout());
        dataView.setBorder(BorderFactory.createEmptyBorder());
        dataView.setName("Data:" + dataView.getQueryString());
        
        // Main pannel with toolbars
        JPanel panel = initializeMainPanel(toolbarType);
        dataView.add(panel, BorderLayout.NORTH);
        
        actionHandler = new DataViewActionHandler(this, dataView);

        //add resultset data panel
        dataPanel = new DataViewTablePanel(dataView.getDataViewDBTable(), this,actionHandler);
        dataView.add(dataPanel, BorderLayout.CENTER);
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    JButton[] getVerticalToolBar() {
        return editButtons;
    }

    void setEditable(boolean editable) {
        dataPanel.setEditable(editable);
    }

    boolean isEditable() {
        return dataPanel.isEditable();
    }

    void setTotalCount(int count) {
        totalRowsLabel.setText(String.valueOf(count) + dataView.getDataViewPageContext().pageOf());
    }

    boolean isCommitEnabled() {
        return commit.isEnabled();
    }

    DataViewTableUI getResulSetTable() {
        return dataPanel.getResulSetTable();
    }

    UpdatedRowContext getUpdatedRowContext() {
        return dataPanel.getUpdatedRowContext();
    }

    void setCommitEnabled(boolean flag) {
        commit.setEnabled(flag);
    }
    
    void setCancelEnabled(boolean flag) {
        cancel.setEnabled(flag);
    }    

    void setDataRows(List<Object[]> rows) {
        dataPanel.setResultSet(rows);
    }

    void disableButtons() {
        truncateButton.setEnabled(false);
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);

        first.setEnabled(false);
        previous.setEnabled(false);
        next.setEnabled(false);
        last.setEnabled(false);
        deleteRow.setEnabled(false);
        commit.setEnabled(false);
        cancel.setEnabled(false);
        insert.setEnabled(false);
        
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    int getPageSize(int totalCount) {
        try {
            int count = Integer.parseInt(refreshField.getText().trim());
            return count < 0 ? 10 : count;
        } catch (NumberFormatException ex) {
            return totalCount < 10 ? totalCount : 10;
        }
    }

    boolean isDirty() {
        return dataPanel.isDirty();
    }

    void resetToolbar(boolean wasError) {
        refreshButton.setEnabled(true);
        refreshField.setEnabled(true);
        DataViewPageContext dataPage = dataView.getDataViewPageContext();
        if (!wasError) {
            if (dataPage.hasPrevious()) {
                first.setEnabled(true);
                previous.setEnabled(true);
            }

            if (dataPage.hasNext()) {
                next.setEnabled(true);
                last.setEnabled(true);
            }

            if (dataPage.hasOnePageOnly()) {
                first.setEnabled(false);
                previous.setEnabled(false);
            }

            if (dataPage.isLastPage()) {
                next.setEnabled(false);
                last.setEnabled(false);
            }

            // editing controls
            if (!isEditable()) {
                commit.setEnabled(false);
                cancel.setEnabled(false);
                deleteRow.setEnabled(false);
                insert.setEnabled(false);
                truncateButton.setEnabled(false);
                dataPanel.setEditable(false);
            } else {
                if (dataPage.hasRows()) {
                    deleteRow.setEnabled(true);
                    truncateButton.setEnabled(true);
                } else {
                    deleteRow.setEnabled(false);
                    truncateButton.setEnabled(false);
                    dataPage.setCurrentPos(0);
                }
                insert.setEnabled(true);
                commit.setEnabled(false);
                cancel.setEnabled(false);
            }
        } else {
            disableButtons();
        }

        refreshField.setText("" + dataPage.getPageSize());
        if (dataPanel != null) {
            dataPanel.revalidate();
            dataPanel.repaint();
        }
    }

    private ActionListener createOutputListener() {

        ActionListener outputListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src.equals(refreshButton)) {
                    actionHandler.refreshActionPerformed();
                } else if (src.equals(first)) {
                    actionHandler.firstActionPerformed();
                } else if (src.equals(last)) {
                    actionHandler.lastActionPerformed();
                } else if (src.equals(next)) {
                    actionHandler.nextActionPerformed();
                } else if (src.equals(previous)) {
                    actionHandler.previousActionPerformed();
                } else if (src.equals(refreshField)) {
                    actionHandler.setMaxActionPerformed();
                } else if (src.equals(commit)) {
                    actionHandler.commitActionPerformed();
                } else if (src.equals(cancel)) {
                    actionHandler.cancelEditPerformed();
                } else if (src.equals(deleteRow)) {
                    actionHandler.deleteRecordActionPerformed();
                } else if (src.equals(insert)) {
                    actionHandler.insertActionPerformed();
                } else if (src.equals(truncateButton)) {
                    actionHandler.truncateActionPerformed();
                }
            }
        };

        return outputListener;
    }

    private void initToolbar(JToolBar toolbar, ActionListener outputListener) {

        toolbar.addSeparator(new Dimension(10, 10));

        //add refresh button
        URL url = getClass().getResource(imgPrefix + "refresh.png");
        refreshButton = new JButton(new ImageIcon(url));
        String nbBundle2 = "Refresh records";
        refreshButton.setToolTipText(nbBundle2);
        refreshButton.addActionListener(outputListener);

        toolbar.add(refreshButton);

        // add navigation buttons
        url = getClass().getResource(imgPrefix + "navigate_beginning.png");
        first = new JButton(new ImageIcon(url));
        String nbBundle3 = "Go to the first page";
        first.setToolTipText(nbBundle3);
        first.addActionListener(outputListener);
        first.setEnabled(false);
        toolbar.add(first);

        url = getClass().getResource(imgPrefix + "navigate_left.png");
        previous = new JButton(new ImageIcon(url));
        String nbBundle4 = "Go to the previous page";
        previous.setToolTipText(nbBundle4);
        previous.addActionListener(outputListener);
        previous.setEnabled(false);
        toolbar.add(previous);

        url = getClass().getResource(imgPrefix + "navigate_right.png");
        next = new JButton(new ImageIcon(url));
        String nbBundle5 = "Go to the next page";
        next.setToolTipText(nbBundle5);
        next.addActionListener(outputListener);
        next.setEnabled(false);
        toolbar.add(next);

        url = getClass().getResource(imgPrefix + "navigate_end.png");
        last = new JButton(new ImageIcon(url));
        String nbBundle6 = "Go to the last page";
        last.setToolTipText(nbBundle6);
        last.addActionListener(outputListener);
        last.setEnabled(false);
        toolbar.add(last);
        toolbar.addSeparator(new Dimension(10, 10));

        //add limit row label
        String nbBundle7 = "Page size:";
        limitRow = new JLabel(nbBundle7);
        limitRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(limitRow);

        //add refresh text field
        refreshField = new JTextField();
        refreshField.setText("" + dataView.getDataViewPageContext().getRecordToRefresh());
        refreshField.setPreferredSize(new Dimension(30, refreshField.getHeight()));
        refreshField.setSize(30, refreshField.getHeight());
        refreshField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent evt) {
                if (refreshField.getText().length() >= 3) {
                    evt.consume();
                }
            }
        });
        refreshField.addActionListener(outputListener);
        toolbar.add(refreshField);
        toolbar.addSeparator(new Dimension(10, 10));

        String nbBundle8 = "Total Rows:";
        JLabel totalRowsNameLabel = new JLabel(nbBundle8);
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(nbBundle8);
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(totalRowsNameLabel);
        totalRowsLabel = new JLabel();
        toolbar.add(totalRowsLabel);
    }

    private void initVerticalToolbar(ActionListener outputListener) {

        URL url = getClass().getResource(imgPrefix + "row_add.png");
        insert = new JButton(new ImageIcon(url));
        String nbBundle9 = " Insert a record.";
        insert.setToolTipText(nbBundle9);
        insert.addActionListener(outputListener);
        insert.setEnabled(false);
        editButtons[0] = insert;

        url = getClass().getResource(imgPrefix + "row_delete.png");
        deleteRow = new JButton(new ImageIcon(url));
        String nbBundle10 = "Delete selected records.";
        deleteRow.setToolTipText(nbBundle10);
        deleteRow.addActionListener(outputListener);
        deleteRow.setEnabled(false);
        editButtons[1] = deleteRow;
        
        url = getClass().getResource(imgPrefix + "row_commit.png");
        commit = new JButton(new ImageIcon(url));
        String nbBundle31 = "Commit changes done on current page.";
        commit.setToolTipText(nbBundle31);
        commit.addActionListener(outputListener);
        commit.setEnabled(false);
        editButtons[2] = commit;
        
        url = getClass().getResource(imgPrefix + "cancel_edits.png");
        cancel = new JButton(new ImageIcon(url));
        String nbBundle11 = "Cancel changes done on current page.";
        cancel.setToolTipText(nbBundle11);
        cancel.addActionListener(outputListener);
        cancel.setEnabled(false);
        editButtons[3] = cancel;
   
        //add truncate button
        url = getClass().getResource(imgPrefix + "table_truncate.png");
        truncateButton = new JButton(new ImageIcon(url));
        String nbBundle14 = "Truncate table";
        truncateButton.setToolTipText(nbBundle14);
        truncateButton.addActionListener(outputListener);
        truncateButton.setEnabled(false);
        editButtons[4] = truncateButton;
    }

    private JPanel initializeMainPanel(int toolbarType) {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gl);

        ActionListener outputListener = createOutputListener();
        initVerticalToolbar(outputListener);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        if (toolbarType == DataView.HORIZONTAL_TOOLBAR) {
            JButton[] btns = getVerticalToolBar();
            for (JButton btn : btns) {
                if (btn != null) {
                    toolbar.add(btn);
                }
            }
        }
        initToolbar(toolbar, outputListener);

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        panel.add(toolbar, c);
        dataView.validate();

        return panel;
    }
}
