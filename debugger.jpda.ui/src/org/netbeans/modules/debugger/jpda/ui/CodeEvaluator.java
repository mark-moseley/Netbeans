/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.HistoryPanel.Item;
import org.netbeans.modules.debugger.jpda.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.Models;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Daniel Prusa
 */
public class CodeEvaluator extends TopComponent implements HelpCtx.Provider,
    DocumentListener, KeyListener, PropertyChangeListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "evaluator"; //NOI18N
    private static final String PROP_RESULT_CHANGED = "resultChanged"; // NOI18N

    private static CodeEvaluator defaultInstance = null;
    private static PropertyChangeSupport pcs = new PropertyChangeSupport(new Integer(0)); // TODO

    private JEditorPane codePane;
    private HistoryPanel historyPanel;
    private Reference<JPDADebugger> debuggerRef = new WeakReference(null);
    private DbgManagerListener dbgManagerListener;
    private TopComponent resultView;
    private Set<String> editItemsSet = new HashSet<String>();
    private ArrayList<String> editItemsList = new ArrayList<String>();
    private JPopupMenu editItemsMenu;
    private JButton dropDownButton;

    private Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VariablesViewButtons.PREFERENCES_NAME);

    private Variable result;
    private RequestProcessor.Task evalTask =
            new RequestProcessor("Debugger Evaluator", 1).  // NOI18N
            create(new EvaluateTask());


    /** Creates new form CodeEvaluator */
    public CodeEvaluator() {
        initComponents();
        codePane = new JEditorPane();
        historyPanel = new HistoryPanel();

        historyToggleButton.setMargin(new Insets(2, 3, 2, 3));
        historyToggleButton.setFocusable(false);
        rightPanel.setPreferredSize(new Dimension(evaluateButton.getPreferredSize().width + 6, 0));

        dropDownButton = createDropDownButton();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
        rightPanel.add(dropDownButton, gridBagConstraints);

        final Document[] documentPtr = new Document[] { null };
        ActionListener contextUpdated = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (codePane.getDocument() != documentPtr[0]) {
                    codePane.getDocument().addDocumentListener(CodeEvaluator.this);
                }
            }
        };
        WatchPanel.setupContext(codePane, contextUpdated);
        editorScrollPane.setViewportView(codePane);
        codePane.getDocument().addDocumentListener(this);
        codePane.addKeyListener(this);
        documentPtr[0] = codePane.getDocument();
        dbgManagerListener = new DbgManagerListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_SESSION,
                dbgManagerListener
        );
        checkDebuggerState();
        defaultInstance = this;
    }

    private JButton createDropDownButton() {
        editItemsMenu = new JPopupMenu();
        Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/jpda/resources/drop_down_arrow.png", false);
        final JButton button = new JButton(icon);
        button.setToolTipText(NbBundle.getMessage(CodeEvaluator.class, "CTL_Expressions_Dropdown_tooltip"));
        button.setEnabled(false);
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        button.setPreferredSize(size);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (editItemsMenu.isShowing()) {
                    editItemsMenu.setVisible(false);
                } else {
                    editItemsMenu.show(button, 0, button.getHeight());
                }
            } // actionPerformed
        });
        return button;
    }

    public void recomputeDropDownItems() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editItemsMenu.removeAll();
                for (String str : editItemsList) {
                    StringTokenizer tok = new StringTokenizer(str, "\n"); // NOI18N
                    String dispName = "";
                    while (dispName.trim().length() == 0 && tok.hasMoreTokens()) {
                        dispName = tok.nextToken();
                    }
                    JMenuItem item = new JMenuItem(dispName, null);
                    item.addActionListener(new MenuItemListener(str));
                    editItemsMenu.add(item);
                }
                dropDownButton.setEnabled(!editItemsList.isEmpty());
            }
        });
    }

    public static synchronized CodeEvaluator getInstance() {
        CodeEvaluator instance = (CodeEvaluator) WindowManager.getDefault().findTopComponent(ID);
        if (instance == null) {
            instance = new CodeEvaluator();
        }
        return instance;
    }

    public static ArrayList<Item> getHistory() {
        return defaultInstance != null ? defaultInstance.historyPanel.getHistoryItems() : new ArrayList<Item>();
    }

    public static Variable getResult() {
        return defaultInstance != null ? defaultInstance.result : null;
    }

    public static String getExpressionText() {
        return defaultInstance != null ? defaultInstance.getExpression() : ""; // NOI18N
    }

    public static synchronized void addResultListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static synchronized void removeResultListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private static void fireResultChange() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                synchronized(CodeEvaluator.class) {
                    pcs.firePropertyChange(PROP_RESULT_CHANGED, null, null);
                }
            }
        });
    }

    private synchronized void checkDebuggerState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
                JPDADebugger debugger = null;
                if (de != null) {
                    debugger = de.lookupFirst(null, JPDADebugger.class);
                }
                JPDADebugger lastDebugger = debuggerRef.get();
                if (lastDebugger != null && debugger != lastDebugger) {
                    lastDebugger.removePropertyChangeListener(
                            JPDADebugger.PROP_CURRENT_THREAD,
                            CodeEvaluator.this);
                    debuggerRef = new WeakReference(null);
                    displayResult(null);
                }
                if (debugger != null) {
                    debuggerRef = new WeakReference(debugger);
                    debugger.addPropertyChangeListener(
                            JPDADebugger.PROP_CURRENT_THREAD,
                            CodeEvaluator.this);
                } else {
                    historyPanel.clearHistory();
                }
                computeEvaluationButtonState();
            }
        });
    }

    private void computeEvaluationButtonState() {
        JPDADebugger debugger = debuggerRef.get();
        boolean isEnabled = debugger != null && debugger.getCurrentThread() != null &&
                codePane.getDocument().getLength() > 0 &&
                editorScrollPane.getViewport().getView() == codePane;
        evaluateButton.setEnabled(isEnabled);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                dbgManagerListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        historyToggleButton = new javax.swing.JToggleButton();
        editorScrollPane = new javax.swing.JScrollPane();
        separatorPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        evaluateButton = new javax.swing.JButton();
        emptyPanel = new javax.swing.JPanel();

        historyToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/eval_history.png"))); // NOI18N
        historyToggleButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "CodeEvaluator.historyToggleButton.text")); // NOI18N
        historyToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "HINT_Show_History")); // NOI18N
        historyToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historyToggleButtonActionPerformed(evt);
            }
        });

        setLayout(new java.awt.GridBagLayout());

        editorScrollPane.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorScrollPane, gridBagConstraints);

        separatorPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        separatorPanel.setMaximumSize(new java.awt.Dimension(1, 32767));
        separatorPanel.setMinimumSize(new java.awt.Dimension(1, 10));
        separatorPanel.setPreferredSize(new java.awt.Dimension(1, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(separatorPanel, gridBagConstraints);

        rightPanel.setPreferredSize(new java.awt.Dimension(48, 0));
        rightPanel.setLayout(new java.awt.GridBagLayout());

        evaluateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/evaluate.gif"))); // NOI18N
        evaluateButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "CodeEvaluator.evaluateButton.text")); // NOI18N
        evaluateButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluator.class, "HINT_Evaluate_Button")); // NOI18N
        evaluateButton.setEnabled(false);
        evaluateButton.setPreferredSize(new java.awt.Dimension(40, 20));
        evaluateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        rightPanel.add(evaluateButton, gridBagConstraints);

        emptyPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        rightPanel.add(emptyPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(rightPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void evaluateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateButtonActionPerformed
        evaluate();
    }//GEN-LAST:event_evaluateButtonActionPerformed

    private void historyToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historyToggleButtonActionPerformed
        boolean toggled = historyToggleButton.isSelected();
        if (toggled) {
            // show history
            editorScrollPane.setViewportView(historyPanel);
        } else {
            // show editor pane
            editorScrollPane.setViewportView(codePane);
        }
        computeEvaluationButtonState();
    }//GEN-LAST:event_historyToggleButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editorScrollPane;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JButton evaluateButton;
    private javax.swing.JToggleButton historyToggleButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel separatorPanel;
    // End of variables declaration//GEN-END:variables

    public static void openEvaluator() {
        CodeEvaluator evaluator = getInstance();
        evaluator.open ();
        evaluator.codePane.selectAll();
        evaluator.requestActive ();
    }

    @Override
    public boolean requestFocusInWindow() {
        codePane.requestFocusInWindow(); // [TODO}
        return super.requestFocusInWindow();
    }

    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage (CodeEvaluator.class, "CTL_Code_Evaluator_name"); // NOI18N
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (CodeEvaluator.class, "CTL_Code_Evaluator_tooltip"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("EvaluateCode"); // NOI18N
    }

    // ..........................................................................

    public String getExpression() {
        return codePane.getText();
    }

    public void evaluate() {
        evalTask.schedule(10);
    }

    private void displayResult(Variable var) {
        this.result = var;
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(var.getValue()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//                if (result == null && resultView == null) { // [TODO]
//                    return ; // Ignore when nothing to display and nothing is initialized.
//                }
                if (preferences.getBoolean("show_evaluator_result", false)) {
                    TopComponent view = WindowManager.getDefault().findTopComponent("localsView"); // NOI18N [TODO]
                    view.open();
                    view.requestActive();
                } else {
                    if (resultView == null) {
                        resultView = getResultViewInstance();
                    }
                    if (result != null) {
                        resultView.open();
                        resultView.requestActive();
                    }
                }
                getInstance().requestActive();
                fireResultChange();
            }
        });
    }

    private void addResultToHistory(final String expr, Variable result) {
        String type = result.getType();
        String value = result.getValue();
        String toString = ""; // NOI18N
        if (result instanceof ObjectVariable) {
            try {
                toString = ((ObjectVariable) result).getToStringValue ();
            } catch (InvalidExpressionException ex) {
            } finally {
            }
        } else {
            toString = value;
        }
        historyPanel.addItem(expr, type, value, toString);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (editItemsSet.contains(expr)) {
                    editItemsList.remove(expr);
                    editItemsList.add(0, expr);
                } else {
                    editItemsList.add(0, expr);
                    editItemsSet.add(expr);
                    if (editItemsList.size() > 20) { // [TODO] constant
                        String removed = editItemsList.remove(editItemsList.size() - 1);
                        editItemsSet.remove(removed);
                    }
                }
                recomputeDropDownItems();
            }
        });
    }

    // KeyListener implementation ..........................................

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
            e.consume();
            if (debuggerRef.get() != null) {
                evaluate();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
            close();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    // DocumentListener implementation ..........................................

    public void insertUpdate(DocumentEvent e) {
        updateWatch();
    }

    // DocumentListener
    public void removeUpdate(DocumentEvent e) {
        updateWatch();
    }

    // DocumentListener
    public void changedUpdate(DocumentEvent e) {
        updateWatch();
    }

    private void updateWatch() {
        // Update this LAZILY to prevent from deadlocks!
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                computeEvaluationButtonState();
            }
        });
    }

    // PropertyChangeListener on current thread .................................

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (this) {
                    JPDADebugger debugger = debuggerRef.get();
                    if (debugger != null) {
                        computeEvaluationButtonState();
                    }
                }
            }
        });
    }

    // ..........................................................................

    public static synchronized TopComponent getResultView() {
        return new ResultView();
    }

    private synchronized TopComponent getResultViewInstance() {
        /** unique ID of <code>TopComponent</code> (singleton) */
        TopComponent instance = WindowManager.getDefault().findTopComponent("resultsView"); // NOI18N [TODO]
        if (instance == null) {
            instance = new ResultView();
        }
        //initResult(instance); // [TODO]
        return instance;
    }

    private void initResult(ResultView view) {
        javax.swing.JComponent tree = Models.createView (Models.EMPTY_MODEL);
        view.add (tree, BorderLayout.CENTER);
        Dimension tps = tree.getPreferredSize();
        tps.height = tps.width/2;
        tree.setPreferredSize(tps);
        tree.setName(NbBundle.getMessage(CodeEvaluator.class, "Evaluator.ResultA11YName"));
        tree.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CodeEvaluator.class, "Evaluator.ResultA11YDescr"));
        // view.setLabelFor(tree);
        JTextField referenceTextField = new JTextField();
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
    }

    // ResultView ...............................................................

    private static class ResultView extends TopComponent implements HelpCtx.Provider {

        private static final String ID = "evaluator_result"; //NOI18N

        ResultView() {
            setLayout(new BorderLayout());
        }

        @Override
        protected String preferredID() {
            return this.getClass().getName();
        }

        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ALWAYS;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage (CodeEvaluator.class, "CTL_Evaluator_Result_name"); // NOI18N
        }

        @Override
        public String getToolTipText() {
            return NbBundle.getMessage (CodeEvaluator.class, "CTL_Evaluator_Result_tooltip"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx("EvaluationResult"); // NOI18N
        }
    }

    // EvaluateTask .............................................................

    private class EvaluateTask implements Runnable {
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            try {
                JPDADebugger debugger = debuggerRef.get();
                if (debugger != null) {
                    Variable var = debugger.evaluate(exp);
                    addResultToHistory(exp, var);
                    displayResult(var);
                }
            } catch (InvalidExpressionException ieex) {
                String message = ieex.getLocalizedMessage();
                Throwable t = ieex.getTargetException();
                if (t != null && t instanceof org.omg.CORBA.portable.ApplicationException) {
                    java.io.StringWriter s = new java.io.StringWriter();
                    java.io.PrintWriter p = new java.io.PrintWriter(s);
                    t.printStackTrace(p);
                    p.close();
                    message += " \n" + s.toString();
                }
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //evalDialog.requestFocus();
                        codePane.requestFocusInWindow();
                    }
                });
            }
        }
    }

    private static class DbgManagerListener extends DebuggerManagerAdapter {

        private Reference<CodeEvaluator> codeEvaluatorRef;

        public DbgManagerListener(CodeEvaluator evaluator) {
            codeEvaluatorRef = new WeakReference<CodeEvaluator>(evaluator);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            CodeEvaluator evaluator = (CodeEvaluator) codeEvaluatorRef.get();
            if (evaluator != null) {
                evaluator.checkDebuggerState();
            }
        }

    }

    private class MenuItemListener implements ActionListener {

        private String str;

        MenuItemListener(String str) {
            this.str = str;
        }

        public void actionPerformed(ActionEvent e) {
            codePane.setText(str);
        }

    }

}
