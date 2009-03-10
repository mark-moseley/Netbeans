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
package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.modules.profiler.heapwalk.OQLController;
import org.netbeans.modules.profiler.heapwalk.oql.OQLEngine;
import org.netbeans.modules.profiler.heapwalk.oql.ui.OQLEditor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 * @author Jaroslav Bachorik
 */
public class OQLControllerUI extends JPanel implements HelpCtx.Provider {


    // --- Presenter -------------------------------------------------------------

    private static class Presenter extends JToggleButton implements HelpCtx.Provider {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static ImageIcon ICON_INFO = ImageUtilities.loadImageIcon("org/netbeans/modules/profiler/heapwalk/ui/resources/oql.png", false); // NOI18N


        //~ Constructors ---------------------------------------------------------------------------------------------------------
        public Presenter(final QueryUI queryUI) {
            super();
            setText(CONTROLLER_NAME);
            setToolTipText(CONTROLLER_DESCR);
            setIcon(ICON_INFO);
            setMargin(new java.awt.Insets(getMargin().top, getMargin().top, getMargin().bottom, getMargin().top));
            
            addKeyListener(new KeyAdapter() {
                public void keyTyped(final KeyEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            queryUI.requestFocus();
                            queryUI.addToQuery(e.getKeyChar());
                        }
                    });
                }
            });
        }

        public HelpCtx getHelpCtx() {
            return HELP_CTX;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String CONTROLLER_NAME = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_ControllerName"); // NOI18N
    private static final String CONTROLLER_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_ControllerDescr"); // NOI18N
    private static final String QUERY_RESULTS_CAPTION = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_QueryResultsCaption"); // NOI18N
    private static final String QUERY_EDITOR_CAPTION = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_QueryEditorCaption"); // NOI18N
    private static final String SAVED_QUERIES_CAPTION = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_SavedQueriesCaption"); // NOI18N
    private static final String EXECUTING_QUERY_MSG = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_ExecutingQueryMsg"); // NOI18N
    private static final String EXECUTE_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_ExecuteButtonText"); // NOI18N
    private static final String EXECUTE_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_ExecuteButtonAccessDescr"); // NOI18N
    private static final String CANCEL_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_CancelButtonText"); // NOI18N
    private static final String CANCEL_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_CancelButtonAccessDescr"); // NOI18N
    private static final String SAVE_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_SaveButtonText"); // NOI18N
    private static final String SAVE_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_SaveButtonAccessDescr"); // NOI18N
    private static final String PROPERTIES_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_PropertiesButtonText"); // NOI18N
    private static final String PROPERTIES_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_PropertiesButtonAccessDescr"); // NOI18N
    private static final String DELETE_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_DeleteButtonText"); // NOI18N
    private static final String DELETE_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_DeleteButtonAccessDescr"); // NOI18N
    private static final String OPEN_BUTTON_TEXT = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_OpenButtonText"); // NOI18N
    private static final String OPEN_BUTTON_ACCESS_DESCR = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_OpenButtonAccessDescr"); // NOI18N
    private static final String LOADING_QUERIES_MSG = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_LoadingQueriesMsg"); // NOI18N
    private static final String NO_SAVED_QUERIES_MSG = NbBundle.getMessage(
            OQLControllerUI.class, "OQLControllerUI_NoSavedQueriesMsg"); // NOI18N
    // -----

    private static final String HELP_CTX_KEY = "OQLControllerUI.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private AbstractButton presenter;
    private OQLController oqlController;


    public OQLControllerUI(OQLController controller) {
        this.oqlController = controller;
        initComponents();
    }

    // --- Public interface ------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = new Presenter((QueryUI)oqlController.getQueryController().getPanel());
        }

        return presenter;
    }

    private void initComponents() {
        JSplitPane querySplitter = new JExtendedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        oqlController.getQueryController().getPanel(),
                                        oqlController.getSavedController().getPanel());
        tweakSplitPaneUI(querySplitter);
        querySplitter.setResizeWeight(1d);

        JSplitPane mainSplitter = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT,
                                        oqlController.getResultsController().getPanel(),
                                        querySplitter);
        tweakSplitPaneUI(mainSplitter);
        mainSplitter.setResizeWeight(1d);

        setLayout(new BorderLayout());
        add(mainSplitter, BorderLayout.CENTER);
    }

    private static void tweakSplitPaneUI(JSplitPane splitPane) {
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        if (!(splitPane.getUI() instanceof BasicSplitPaneUI)) {
            return;
        }

        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();

        if (divider != null) {
            divider.setBorder(null);
        }
    }


    public static class ResultsUI extends JTitledPanel {

        private OQLController.ResultsController resultsController;
        private HTMLTextArea resultsArea;

        private static ImageIcon ICON = ImageUtilities.loadImageIcon(
                "org/netbeans/modules/profiler/heapwalk/ui/resources/properties.png", false); // NOI18N

        public ResultsUI(OQLController.ResultsController resultsController) {
            super(QUERY_RESULTS_CAPTION, ICON, true);
            this.resultsController = resultsController;
            initComponents();
        }


        public void setResult(String result) {
            resultsArea.setText(result);
            try { resultsArea.setCaretPosition(0); } catch (Exception e) {}
            setVisible(true);
        }


        private void initComponents() {
            setLayout(new BorderLayout());

            resultsArea = new HTMLTextArea() {
                protected void showURL(URL url) {
                    resultsController.showURL(url);
                }
            };

            JScrollPane resultsAreaScroll = new JScrollPane(resultsArea,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            resultsAreaScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                            UIUtils.getProfilerResultsBackground()));
            resultsAreaScroll.setViewportBorder(BorderFactory.createEmptyBorder());
            resultsAreaScroll.getVerticalScrollBar().setUnitIncrement(10);
            resultsAreaScroll.getHorizontalScrollBar().setUnitIncrement(10);

            JPanel contentsPanel = new JPanel();
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getTitleBorderColor()));
            contentsPanel.setLayout(new BorderLayout());
            contentsPanel.setOpaque(true);
            contentsPanel.setBackground(resultsArea.getBackground());
            contentsPanel.add(resultsAreaScroll, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);
        }

    }

    public static class QueryUI extends JTitledPanel implements PropertyChangeListener {

        private OQLController.QueryController queryController;
        private OQLEditor editor;
        private boolean queryValid = true;
        
        private JButton runButton;
        private JButton saveButton;
        private JButton cancelButton;
        private JLabel progressLabel;
        private JProgressBar progressBar;
        private JPanel controlPanel;
        private JPanel progressPanel;
        private JPanel contentsPanel;


        private static ImageIcon ICON = ImageUtilities.loadImageIcon(
                "org/netbeans/modules/profiler/heapwalk/ui/resources/rules.png", false); // NOI18N

        public QueryUI(OQLController.QueryController queryController, OQLEngine engine) {
            super(QUERY_EDITOR_CAPTION, ICON, true);

            this.queryController = queryController;

            initComponents(engine);
//            queryValid = editor.isValidScript();
            updateButtons();
        }


        public void setQuery(String query) {
            setVisible(true);
            editor.setScript(query);
            editor.requestFocus();
        }

        public void queryStarted(final BoundedRangeModel model) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressLabel.setText(EXECUTING_QUERY_MSG); // NOI18N
                    progressBar.setModel(model);
                    progressBar.setMaximumSize(new Dimension(progressBar.getMaximumSize().width,
                                                             progressBar.getPreferredSize().height));
                    contentsPanel.remove(controlPanel);
                    contentsPanel.add(progressPanel, BorderLayout.SOUTH);
                    progressPanel.invalidate();
                    contentsPanel.revalidate();
                    contentsPanel.repaint();
                    updateButtons();
                }
            });
        }

        public void queryFinished() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateButtons();
                    contentsPanel.remove(progressPanel);
                    contentsPanel.add(controlPanel, BorderLayout.SOUTH);
                    controlPanel.invalidate();
                    contentsPanel.revalidate();
                    contentsPanel.repaint();
                }
            });
        }

        private void addToQuery(char ch) {
            setVisible(true);
            String chs = new String(new char[] { ch });
            editor.setScript(editor.getScript() + chs);
        }

        public void requestFocus() {
            editor.requestFocus();
        }


        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(OQLEditor.VALIDITY_PROPERTY)) {
//                queryValid = ((Boolean)evt.getNewValue());
                updateButtons();
            }
        }


        private void updateButtons() {
            if (queryController.getOQLController().isQueryRunning()) {
                runButton.setEnabled(false);
            } else {
                runButton.setEnabled(queryValid);
            }
            saveButton.setEnabled(queryValid);
        }

        private void executeQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    requestFocus();
                    queryController.getOQLController().executeQuery(editor.getScript());
                }
            });
        }

        private void saveQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    queryController.getOQLController().getSavedController().
                                                    saveQuery(editor.getScript());
                    editor.requestFocus();
                }
            });
        }

        private void cancelQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    queryController.getOQLController().cancelQuery();
                }
            });
        }


        private void initComponents(OQLEngine engine) {
            editor = new OQLEditor(engine);
            editor.setBackground(UIUtils.getProfilerResultsBackground());
            editor.addPropertyChangeListener(OQLEditor.VALIDITY_PROPERTY, this);

            JScrollPane editorScroll = new JScrollPane(editor,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            editorScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 0, 5,
                                        UIUtils.getProfilerResultsBackground()));
            editorScroll.setViewportBorder(BorderFactory.createEmptyBorder());
            editorScroll.getVerticalScrollBar().setUnitIncrement(10);
            editorScroll.getHorizontalScrollBar().setUnitIncrement(10);

            runButton = new JButton() {
                protected void fireActionPerformed(ActionEvent e) { executeQuery(); }
            };
            Mnemonics.setLocalizedText(runButton, EXECUTE_BUTTON_TEXT);
            runButton.getAccessibleContext().setAccessibleDescription(EXECUTE_BUTTON_ACCESS_DESCR);
            saveButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { saveQuery(); }
            };
            Mnemonics.setLocalizedText(saveButton, SAVE_BUTTON_TEXT);
            saveButton.getAccessibleContext().setAccessibleDescription(SAVE_BUTTON_ACCESS_DESCR);
            
            controlPanel = new JPanel(new BorderLayout(5, 5));
            controlPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
            controlPanel.setOpaque(false);
            controlPanel.add(saveButton, BorderLayout.WEST);
            controlPanel.add(runButton, BorderLayout.EAST);
            
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
            progressLabel = new JLabel();
            progressLabel.setLabelFor(progressBar);
            cancelButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { cancelQuery(); }
            };
            Mnemonics.setLocalizedText(cancelButton, CANCEL_BUTTON_TEXT);
            cancelButton.getAccessibleContext().setAccessibleDescription(CANCEL_BUTTON_ACCESS_DESCR);

            progressPanel = new JPanel(new GridBagLayout());
            progressPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
            progressPanel.setOpaque(false);
            GridBagConstraints c;

            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 0, 10);
            progressPanel.add(progressLabel, c);

            c = new GridBagConstraints();
            c.weightx = 1;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 0, 10);
            progressPanel.add(progressBar, c);

            c = new GridBagConstraints();
            c.weighty = 1;
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 0, 0, 0);
            progressPanel.add(cancelButton, c);

            contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getTitleBorderColor()));
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.add(editorScroll, BorderLayout.CENTER);
            contentsPanel.add(controlPanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);
            
            getInputMap(QueryUI.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL_ACTION"); // NOI18N
            getActionMap().put("CANCEL_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    cancelQuery();
                }
            });

            editorScroll.setPreferredSize(new Dimension(1, 200));
        }

    }

    public static class SavedUI extends JTitledPanel {

        private OQLController.SavedController savedController;

        private JList savedList;
        private JButton openButton;
        private JButton editButton;
        private JButton deleteButton;
        private JTextArea descriptionArea;
        private JPanel contentsPanel;
        private JPanel loadingMsgPanel;
        private JPanel noQueriesMsgPanel;
        private JScrollPane savedListScroll;

        private DefaultListModel listModel;

        private boolean queriesLoaded = false;


        private static ImageIcon ICON = ImageUtilities.loadImageIcon(
                "org/netbeans/modules/profiler/heapwalk/ui/resources/savedOQL.png", false); // NOI18N

        public SavedUI(OQLController.SavedController savedController) {
            super(SAVED_QUERIES_CAPTION, ICON, true);

            this.savedController = savedController;

            listModel = new DefaultListModel();

            initComponents();
            refreshQueries();
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    OQLController.SavedController.loadData(listModel);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            queriesLoaded = true;
                            refreshQueries();
                        }
                    });
                }
            });
        }


        public void saveQuery(final String query) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (OQLQueryCustomizer.saveQuery(query, listModel)) {
                        setVisible(true);
                        refreshQueries();
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                OQLController.SavedController.saveData(listModel);
                            }
                        });
                    }
                }
            });
        }


        private void openQuery() {
            OQLController.Query q = (OQLController.Query)savedList.getSelectedValue();
            if (q != null)
                savedController.getOQLController().getQueryController().setQuery(q.getScript());
        }

        private void editQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    OQLController.Query q = (OQLController.Query)savedList.getSelectedValue();
                    if (q != null) {
                        if (OQLQueryCustomizer.editQuery(q, listModel, savedList)) {
                            refreshDescription();
                            RequestProcessor.getDefault().post(new Runnable() {
                                public void run() {
                                    OQLController.SavedController.saveData(listModel);
                                }
                            });
                        }
                        editButton.requestFocus();
                    }
                }
            });
        }

        private void deleteQuery() {
            OQLController.Query q = (OQLController.Query)savedList.getSelectedValue();
            if (q != null) {
                int selectedIndex = listModel.indexOf(q);
                if (selectedIndex > 0)
                    savedList.setSelectedIndex(selectedIndex - 1);
                listModel.removeElement(q);
                refreshQueries();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        OQLController.SavedController.saveData(listModel);
                    }
                });
            }
        }


        private void refreshQueries() {
            Component currentContents =
                    ((BorderLayout)contentsPanel.getLayout()).
                    getLayoutComponent(BorderLayout.CENTER);

            if (queriesLoaded) {
                if (listModel.isEmpty()) {
                    if (currentContents != noQueriesMsgPanel) {
                        if (currentContents != null) contentsPanel.remove(currentContents);
                        contentsPanel.add(noQueriesMsgPanel, BorderLayout.CENTER);
                        noQueriesMsgPanel.invalidate();
                        contentsPanel.revalidate();
                        contentsPanel.repaint();
                    }
                } else {
                    if (currentContents != savedListScroll) {
                        if (currentContents != null) contentsPanel.remove(currentContents);
                        contentsPanel.add(savedListScroll, BorderLayout.CENTER);
                        savedListScroll.invalidate();
                        contentsPanel.revalidate();
                        contentsPanel.repaint();
                    }
                }
            } else {
                contentsPanel.add(loadingMsgPanel, BorderLayout.CENTER);
                loadingMsgPanel.invalidate();
                contentsPanel.revalidate();
                contentsPanel.repaint();
            }
        }

        private void refreshButtons() {
            boolean selected = savedList.getSelectedValue() != null;
            openButton.setEnabled(selected);
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        }

        private void refreshDescription() {
            OQLController.Query q = (OQLController.Query)savedList.getSelectedValue();
            if (q != null && q.getDescription() != null) {
                descriptionArea.setText(q.getDescription());
                descriptionArea.setVisible(true);
            } else {
                descriptionArea.setVisible(false);
            }
        }


        private void initComponents() {
            setLayout(new BorderLayout());

            savedList = new JList(listModel);
            savedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            savedList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    refreshButtons();
                    refreshDescription();
                }
            });
            
            savedListScroll = new JScrollPane(savedList,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            savedListScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                            UIUtils.getProfilerResultsBackground()));
            savedListScroll.setViewportBorder(BorderFactory.createEmptyBorder());
            
            loadingMsgPanel = new JPanel(new BorderLayout());
            loadingMsgPanel.setOpaque(false);
            JLabel loadingMsgLabel = new JLabel(LOADING_QUERIES_MSG, JLabel.CENTER);
            loadingMsgLabel.setEnabled(false);
            loadingMsgPanel.add(loadingMsgLabel, BorderLayout.CENTER);

            noQueriesMsgPanel = new JPanel(new BorderLayout());
            noQueriesMsgPanel.setOpaque(false);
            JLabel noQueriesMsgLabel = new JLabel(NO_SAVED_QUERIES_MSG, JLabel.CENTER);
            noQueriesMsgLabel.setEnabled(false);
            noQueriesMsgPanel.add(noQueriesMsgLabel, BorderLayout.CENTER);
            
            openButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { openQuery(); }
            };
            Mnemonics.setLocalizedText(openButton, OPEN_BUTTON_TEXT);
            openButton.getAccessibleContext().
                            setAccessibleDescription(OPEN_BUTTON_ACCESS_DESCR);
            editButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { editQuery(); }
            };
            Mnemonics.setLocalizedText(editButton, PROPERTIES_BUTTON_TEXT);
            editButton.getAccessibleContext().
                            setAccessibleDescription(PROPERTIES_BUTTON_ACCESS_DESCR);
            deleteButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { deleteQuery(); }
            };
            Mnemonics.setLocalizedText(deleteButton, DELETE_BUTTON_TEXT);
            deleteButton.getAccessibleContext().
                            setAccessibleDescription(DELETE_BUTTON_ACCESS_DESCR);

            JPanel editContainer = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            editContainer.setOpaque(false);
            editContainer.add(editButton);
            editContainer.add(deleteButton);

            JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
            controlPanel.setBorder(BorderFactory.createMatteBorder(5, 0, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
            controlPanel.setOpaque(false);
            controlPanel.add(editContainer, BorderLayout.WEST);
            controlPanel.add(openButton, BorderLayout.EAST);

            descriptionArea = new JTextArea();
            descriptionArea.setEnabled(false);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setDisabledTextColor(UIManager.getColor("ToolTip.foreground")); // NOI18N
            descriptionArea.setBackground(UIManager.getColor("ToolTip.background")); // NOI18N
            descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 5,
                                        UIUtils.getProfilerResultsBackground()),
                    BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIManager.getColor("ToolTip.background")))); // NOI18N

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            bottomPanel.add(descriptionArea, BorderLayout.CENTER);
            bottomPanel.add(controlPanel, BorderLayout.SOUTH);

            contentsPanel = new JPanel();
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, getTitleBorderColor()));
            contentsPanel.setLayout(new BorderLayout());
            contentsPanel.setOpaque(true);
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.add(bottomPanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);

            refreshButtons();
            refreshDescription();

            savedList.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OPEN_QUERY_ACTION"); // NOI18N
            savedList.getActionMap().put("OPEN_QUERY_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    openQuery();
                }
            });
            savedList.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_QUERY_ACTION"); // NOI18N
            savedList.getActionMap().put("DELETE_QUERY_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    deleteQuery();
                }
            });
            savedList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                        openQuery();
                }
            });

            savedListScroll.setPreferredSize(new Dimension(
                    openButton.getPreferredSize().width +
                    editButton.getPreferredSize().width +
                    deleteButton.getPreferredSize().width + 75, 200));
        }

    }

}
