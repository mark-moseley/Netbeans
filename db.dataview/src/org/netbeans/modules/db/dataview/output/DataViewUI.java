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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import org.netbeans.modules.db.dataview.logger.Localizer;

/**
 * DataViewUI hosting display of design-level SQL test output.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewUI extends JPanel {

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
    private String imgPrefix = "/org/netbeans/modules/db/dataview/images/"; // NOI18N
    private DataViewTablePanel dataPanel;
    private final DataView dataView;
    private JButton cancel;
    private DataViewActionHandler actionHandler;
    private static transient final Localizer mLoc = Localizer.get();

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener
        = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            public @Override void mouseEntered(MouseEvent evt) {
                Object src = evt.getSource();

                if (src instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    if (button.isEnabled()) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }
                }
            }

            public @Override void mouseExited(MouseEvent evt) {
                Object src = evt.getSource();
                if (src instanceof AbstractButton)
                {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }

            protected void showPopup(MouseEvent evt) {
            }
        };

    DataViewUI(DataView dataView, boolean nbOutputComponent) {
        this.dataView = dataView;

        //do not show tab view if there is only one tab
        this.putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N

        this.putClientProperty("PersistenceType", "Never"); //NOI18N

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder());
        String nbBundle30 = mLoc.t("RESC030: Data:");
        this.setName(nbBundle30.substring(15) + dataView.getSQLString());

        // Main pannel with toolbars
        JPanel panel = initializeMainPanel(nbOutputComponent);
        this.add(panel, BorderLayout.NORTH);

        actionHandler = new DataViewActionHandler(this, dataView);

        //add resultset data panel
        dataPanel = new DataViewTablePanel(dataView, this, actionHandler);
        this.add(dataPanel, BorderLayout.CENTER);
        dataPanel.revalidate();
        dataPanel.repaint();
    }

    JButton[] getEditButtons() {
        return editButtons;
    }

    void setEditable(boolean editable) {
        dataPanel.setEditable(editable);
    }

    boolean isEditable() {
        return dataPanel.isEditable();
    }

    void setTotalCount(int count) {
        if(count < 0){
            totalRowsLabel.setText("NA");
        } else {
            totalRowsLabel.setText(String.valueOf(count) + dataView.getDataViewPageContext().pageOf());
        }
    }

    boolean isCommitEnabled() {
        return commit.isEnabled();
    }

    DataViewTableUI getDataViewTableUI() {
        return dataPanel.getDataViewTableUI();
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
        dataPanel.createTableModel(rows);
    }

    void syncPageWithTableModel() {
        List<Object[]> newrows = dataPanel.getPageDataFromTable();
        List<Object[]> oldRows = dataView.getDataViewPageContext().getCurrentRows();

        for(String key : dataView.getUpdatedRowContext().getUpdateKeys()){
            int row = Integer.parseInt(key.substring(0, key.indexOf(";"))) - 1;
            newrows.set(row, oldRows.get(row));
        }
        dataView.getDataViewPageContext().setCurrentRows(newrows);
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

    int getPageSize() {
        int pageSize = dataView.getDataViewPageContext().getPageSize();
        int totalCount = dataView.getDataViewPageContext().getTotalRows();
        try {
            int count = Integer.parseInt(refreshField.getText().trim());
            return count < 0 ? pageSize : count;
        } catch (NumberFormatException ex) {
            return totalCount < pageSize ? totalCount : pageSize;
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
                    dataPage.first();
                }
                insert.setEnabled(true);
                if (getUpdatedRowContext().getUpdateKeys().isEmpty()) {
                    commit.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    commit.setEnabled(true);
                    cancel.setEnabled(true);
                }
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
                    actionHandler.commitActionPerformed(false);
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

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof AbstractButton) {
            button.addMouseListener(sharedMouseListener);
        }
        //Focus shouldn't stay in toolbar
        button.setFocusable(false);
    }

    private void initToolbar(JToolBar toolbar, ActionListener outputListener) {

        toolbar.addSeparator(new Dimension(10, 10));

        //add refresh button
        URL url = getClass().getResource(imgPrefix + "refresh.png"); // NOI18N
        refreshButton = new JButton(new ImageIcon(url));
        String nbBundle31 = mLoc.t("RESC017: Refresh Records");
        refreshButton.setToolTipText(nbBundle31.substring(15));
        refreshButton.addActionListener(outputListener);
        processButton(refreshButton);

        toolbar.add(refreshButton);

        // add navigation buttons
        url = getClass().getResource(imgPrefix + "navigate_beginning.png"); // NOI18N
        first = new JButton(new ImageIcon(url));
        String nbBundle32 = mLoc.t("RESC032: First Page");
        first.setToolTipText(nbBundle32.substring(15));
        first.addActionListener(outputListener);
        first.setEnabled(false);
        processButton(first);
        toolbar.add(first);

        url = getClass().getResource(imgPrefix + "navigate_left.png"); // NOI18N
        previous = new JButton(new ImageIcon(url));
        String nbBundle33 = mLoc.t("RESC033: Previous Page");
        previous.setToolTipText(nbBundle33.substring(15));
        previous.addActionListener(outputListener);
        previous.setEnabled(false);
        processButton(previous);
        toolbar.add(previous);

        url = getClass().getResource(imgPrefix + "navigate_right.png"); // NOI18N
        next = new JButton(new ImageIcon(url));
        String nbBundle34 = mLoc.t("RESC034: Next Page");
        next.setToolTipText(nbBundle34.substring(15));
        next.addActionListener(outputListener);
        next.setEnabled(false);
        processButton(next);
        toolbar.add(next);

        url = getClass().getResource(imgPrefix + "navigate_end.png"); // NOI18N
        last = new JButton(new ImageIcon(url));
        String nbBundle35 = mLoc.t("RESC035: Last Page");
        last.setToolTipText(nbBundle35.substring(15));
        last.addActionListener(outputListener);
        last.setEnabled(false);
        toolbar.add(last);
        processButton(last);
        toolbar.addSeparator(new Dimension(10, 10));

        //add limit row label
        String nbBundle36 = mLoc.t("RESC036: Page Size:");
        limitRow = new JLabel(nbBundle36.substring(15));
        limitRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(limitRow);

        //add refresh text field
        refreshField = new JTextField();
        refreshField.setText("" + dataView.getDataViewPageContext().getPageSize()); // NOI18N
        refreshField.setMinimumSize(new Dimension(40, refreshField.getHeight()));
        refreshField.setSize(40, refreshField.getHeight());
        refreshField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent evt) {
                if (refreshField.getText().length() >= 4) {
                    evt.consume();
                }
            }
        });
        refreshField.addActionListener(outputListener);
        toolbar.add(refreshField);
        toolbar.addSeparator(new Dimension(10, 10));

        String nbBundle37 = mLoc.t("RESC037: Total Rows:");
        JLabel totalRowsNameLabel = new JLabel(nbBundle37.substring(15));
        totalRowsNameLabel.getAccessibleContext().setAccessibleName(nbBundle37.substring(15));
        totalRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(totalRowsNameLabel);
        totalRowsLabel = new JLabel();
        toolbar.add(totalRowsLabel);

        char[] fillChars = new char[250];
        Arrays.fill(fillChars, ' ');
        JLabel filler = new JLabel(new String(fillChars));
        filler.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(filler);

    }

    private void initVerticalToolbar(ActionListener outputListener) {

        URL url = getClass().getResource(imgPrefix + "row_add.png"); // NOI18N
        insert = new JButton(new ImageIcon(url));
        String nbBundle38 = mLoc.t("RESC018: Insert Record");
        insert.setToolTipText(nbBundle38.substring(15));
        insert.addActionListener(outputListener);
        insert.setEnabled(false);
        processButton(insert);
        editButtons[0] = insert;

        url = getClass().getResource(imgPrefix + "row_delete.png"); // NOI18N
        deleteRow = new JButton(new ImageIcon(url));
        String nbBundle39 = mLoc.t("RESC019: Delete Selected Record(s)");
        deleteRow.setToolTipText(nbBundle39.substring(15));
        deleteRow.addActionListener(outputListener);
        deleteRow.setEnabled(false);
        processButton(deleteRow);
        editButtons[1] = deleteRow;

        url = getClass().getResource(imgPrefix + "row_commit.png"); // NOI18N
        commit = new JButton(new ImageIcon(url));
        String nbBundle40 = mLoc.t("RESC040: Commit Record(s)");
        commit.setToolTipText(nbBundle40.substring(15));
        commit.addActionListener(outputListener);
        commit.setEnabled(false);
        processButton(commit);
        editButtons[2] = commit;

        url = getClass().getResource(imgPrefix + "cancel_edits.png"); // NOI18N
        cancel = new JButton(new ImageIcon(url));
        String nbBundle41 = mLoc.t("RESC021: Cancel Edits");
        cancel.setToolTipText(nbBundle41.substring(15));
        cancel.addActionListener(outputListener);
        cancel.setEnabled(false);
        processButton(cancel);
        editButtons[3] = cancel;

        //add truncate button
        url = getClass().getResource(imgPrefix + "table_truncate.png"); // NOI18N
        truncateButton = new JButton(new ImageIcon(url));
        String nbBundle42 = mLoc.t("RESC022: Truncate Table");
        truncateButton.setToolTipText(nbBundle42.substring(15));
        truncateButton.addActionListener(outputListener);
        truncateButton.setEnabled(false);
        processButton(truncateButton);
        editButtons[4] = truncateButton;
    }

    private JPanel initializeMainPanel(boolean nbOutputComponent) {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gl);

        ActionListener outputListener = createOutputListener();
        initVerticalToolbar(outputListener);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        if (!nbOutputComponent) {
            JButton[] btns = getEditButtons();
            for (JButton btn : btns) {
                if (btn != null) {
                    toolbar.add(btn);
                }
            }
        }
        initToolbar(toolbar, outputListener);

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        panel.add(toolbar, c);
        this.validate();
        return panel;
    }
}
