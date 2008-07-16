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

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.models.WatchesNodeModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 * The expression evaluator.
 *
 * @author  Martin Entlicher
 */
public class Evaluator extends javax.swing.JPanel {
    
    /** The maximum number of expressions that are kept in the combo box. */
    private static final int MAX_ITEMS_TO_KEEP = 20;
    
    private JPDADebugger debugger;
    private EvaluatorModelListener viewModelListener;
    private Variable result;
    private RequestProcessor.Task evalTask =
            new RequestProcessor("Debugger Evaluator", 1).  // NOI18N
            create(new EvaluateTask());
    private boolean ignoreEvents = false;
    private SessionListener sessionListener;
    private PropertyChangeListener csfListener;
    private JButton watchButton;
    private JButton evaluateButton;
    
    /** Creates new form Evaluator */
    public Evaluator(JPDADebugger debugger) {
        setDebugger(debugger);
        initComponents();
        initCombo();
        initResult();
        //expressionLabel.setLabelFor(expressionComboBox);
        Mnemonics.setLocalizedText(expressionLabel,
                NbBundle.getMessage(Evaluator.class, "Evaluator.Expression"));
        //resultLabel.setLabelFor(resultPanel);
        Mnemonics.setLocalizedText(resultLabel,
                NbBundle.getMessage(Evaluator.class, "Evaluator.Result"));
        sessionListener = new SessionListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_SESSION, sessionListener);
    }
    
    private void setDebugger(JPDADebugger debugger) {
        if (debugger == this.debugger || debugger == null) {
            return;
        }
        this.debugger = debugger;
        this.csfListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
                    // Re-initialize the context of the combo
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            initCombo();
                        }
                    });
                }
            }
        };
        debugger.addPropertyChangeListener(
                WeakListeners.propertyChange(csfListener, debugger));
    }
    
    private void setButtons(JButton watchButton, JButton evaluateButton) {
        this.watchButton = watchButton;
        this.evaluateButton = evaluateButton;
        boolean enabled = ((CompletionedEditor) expressionComboBox.getEditor()).getDocument().getLength() > 0;
        watchButton.setEnabled(enabled);
        evaluateButton.setEnabled(enabled);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        expressionLabel = new javax.swing.JLabel();
        expressionComboBox = new javax.swing.JComboBox();
        resultLabel = new javax.swing.JLabel();
        resultPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        expressionLabel.setLabelFor(expressionComboBox);
        expressionLabel.setText(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.Expression")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 12);
        add(expressionLabel, gridBagConstraints);
        expressionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.ExpressionA11YDescr")); // NOI18N

        expressionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.ExpressionA11YDescr")); // NOI18N
        expressionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                expressionComboBoxItemStateChanged(evt);
            }
        });
        expressionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expressionComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 12, 12);
        add(expressionComboBox, gridBagConstraints);

        resultLabel.setText(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.Result")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 12);
        add(resultLabel, gridBagConstraints);
        resultLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.ResultA11YDescr")); // NOI18N

        resultPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        resultPanel.setLayout(new javax.swing.BoxLayout(resultPanel, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 12, 12);
        add(resultPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.A11YName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(Evaluator.class, "Evaluator.A11YDescr")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void expressionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_expressionComboBoxItemStateChanged
        //System.err.println("itemStateChanged("+evt+")");
        //Thread.dumpStack();
        if (ignoreEvents) return ;
        evaluate();
    }//GEN-LAST:event_expressionComboBoxItemStateChanged

    private void expressionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expressionComboBoxActionPerformed
        //System.out.println("actionPerformed("+evt+")");
        //evaluate();
    }//GEN-LAST:event_expressionComboBoxActionPerformed
    
    private void initCombo() {
        String textInEditor = (String) expressionComboBox.getEditor().getItem();
        final CompletionedEditor ce = new CompletionedEditor(expressionComboBox);
        expressionComboBox.setEditor(ce);
        expressionComboBox.setEditable(true);
        ce.setupContext();
        expressionComboBox.getEditor().setItem(textInEditor);
        //expressionComboBox.revalidate();
        expressionComboBox.repaint();
        
        class ExpressionDocumentListener implements DocumentListener, Runnable {
            public void insertUpdate(DocumentEvent e) {
                updateWatch();
            }
            public void removeUpdate(DocumentEvent e) {
                updateWatch();
            }
            public void changedUpdate(DocumentEvent e) {
                updateWatch();
            }
            private void updateWatch() {
                // Update this LAZILY to prevent from deadlocks!
                SwingUtilities.invokeLater(this);
            }
            public void run() {
                boolean enabled = ce.getDocument().getLength() > 0;
                watchButton.setEnabled(enabled);
                evaluateButton.setEnabled(enabled);
            }
        }
        
        ce.getDocument().addDocumentListener(new ExpressionDocumentListener());
    }
    
    private void initResult() {
        javax.swing.JComponent tree = Models.createView (Models.EMPTY_MODEL);
        Container hackedFCR = (Container) ((Container) ((Container) tree.getComponents()[0]).getComponents()[0]).getComponents()[0];
        hackedFCR = (Container) tree.getComponents()[0];
        try {
            java.lang.reflect.Field treeTableField = hackedFCR.getClass().getSuperclass().getDeclaredField("treeTable");
            treeTableField.setAccessible(true);
            hackedFCR = (Container) treeTableField.get(hackedFCR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        hackedFCR.setFocusCycleRoot(false);
        hackedFCR.setFocusTraversalPolicy(null);
        hackedFCR.setFocusTraversalPolicyProvider(false);
        resultPanel.add (tree, "Center");  //NOI18N
        viewModelListener = new EvaluatorModelListener (
            tree
        );
        Dimension tps = tree.getPreferredSize();
        tps.height = tps.width/2;
        tree.setPreferredSize(tps);
        tree.setName(NbBundle.getMessage(Evaluator.class, "Evaluator.ResultA11YName"));
        tree.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Evaluator.class, "Evaluator.ResultA11YDescr"));
        resultLabel.setLabelFor(tree);
        JTextField referenceTextField = new JTextField();
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        tree.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
    }
    
    private void destroy() {
        viewModelListener.destroy();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox expressionComboBox;
    private javax.swing.JLabel expressionLabel;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JPanel resultPanel;
    // End of variables declaration//GEN-END:variables
    
    /** Get the current expression. */
    public String getExpression() {
        String textInEditor =
                (String) expressionComboBox.getEditor().getItem();
        String exp = (String) expressionComboBox.getSelectedItem();
        if (textInEditor != null && !textInEditor.equals(exp)) {
            try {
                ignoreEvents = true;
                expressionComboBox.setSelectedItem(textInEditor);
                exp = textInEditor;
            } finally {
                ignoreEvents = false;
            }
        }
        return exp;
    }
    
    /**
     * Perform the evaluation (lazily).
     */
    private void evaluate() {
        //System.err.println("Evaluator.evaluate()");
        //Thread.dumpStack();
        evalTask.schedule(10);
    }
    
    private void addExpressionToHistory(String exp) {
        try {
            ignoreEvents = true;
            int ic = expressionComboBox.getItemCount();
            int i;
            for (i = 0; i < ic; i++) {
                String item = (String) expressionComboBox.getItemAt(i);
                if (item.equals(exp)) {
                    expressionComboBox.removeItemAt(i);
                    break;
                }
            }
            if (i >= MAX_ITEMS_TO_KEEP) {
                expressionComboBox.removeItemAt(i-1);
            }
            if (ic > 0) {
                expressionComboBox.insertItemAt(exp, 0);
            } else {
                expressionComboBox.addItem(exp);
            }
            // It's necessary to set back the selected item, because
            // removeItemAt() unexpectedly sets the selected item to
            // some different value.
            expressionComboBox.setSelectedItem(exp);
        } finally {
            ignoreEvents = false;
        }
    }
    
    private void displayResult(Variable var) {
        this.result = var;
        //System.out.println("Updating model with result = "+result);
        viewModelListener.updateModel();
    }
    
    private static Dialog evalDialog;
    
    private static volatile Evaluator currentEvaluator;
    
    public static void open(JPDADebugger debugger) {
        if (evalDialog != null) {
            evalDialog.setVisible(true);
            evalDialog.requestFocus();
            requestFocusForExpression();
            return ;
        }
        final Evaluator evaluatorPanel = new Evaluator(debugger);
        String evalStr = NbBundle.getMessage(Evaluator.class, "Evaluator.Evaluate");
        String watchStr = NbBundle.getMessage(Evaluator.class, "Evaluator.Watch");
        String closeStr = NbBundle.getMessage(Evaluator.class, "Evaluator.Close");
        final JButton evalBtn = new JButton();
        Mnemonics.setLocalizedText(evalBtn, evalStr);
        evalBtn.setToolTipText(NbBundle.getMessage(Evaluator.class, "Evaluator.Evaluate.TLT"));
        final JButton watchBtn = new JButton();
        Mnemonics.setLocalizedText(watchBtn, watchStr);
        watchBtn.setToolTipText(NbBundle.getMessage(Evaluator.class, "Evaluator.Watch.TLT"));
        final JButton closeBtn = new JButton();
        Mnemonics.setLocalizedText(closeBtn, closeStr);
        closeBtn.setToolTipText(NbBundle.getMessage(Evaluator.class, "Evaluator.Close.TLT"));
        evaluatorPanel.setButtons(watchBtn, evalBtn);
        DialogDescriptor dd = new DialogDescriptor(evaluatorPanel,
                NbBundle.getMessage(Evaluator.class, "Evaluator.Title"),
                false, new Object[] { evalBtn, watchBtn, closeBtn },
                evalStr, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(Evaluator.class.getName()), new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Object option = e.getSource();
                        if (evalBtn.equals(option)) {
                            evaluatorPanel.evaluate();
                        } else if (watchBtn.equals(option)) {
                            DebuggerManager.getDebuggerManager ().createWatch (evaluatorPanel.getExpression ());
                    } else if (closeBtn.equals(option)) {
                            close();
                        }
                    }
                });
        evalDialog = DialogDisplayer.getDefault().createDialog(dd);
        evalDialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Evaluator.class, "Evaluator.A11YDescr"));
        evalDialog.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(Evaluator.class, "Evaluator.A11YName"));
        currentEvaluator = evaluatorPanel;
        //traverseComponents(evalDialog, evalDialog, evalDialog.getFocusTraversalPolicy());
        evalDialog.setVisible(true);
        //traverseComponents(evalDialog, evalDialog, evalDialog.getFocusTraversalPolicy());
        requestFocusForExpression();
        currentEvaluator.setNextFocusableComponent(evalBtn);
    }
    
    private static void traverseComponents(Component c, Container fcr, FocusTraversalPolicy ftp) {
        if (c instanceof Container) {
            Container cc = (Container) c;
            System.err.println("\nComponent "+c);
            System.err.println("isFocusable: "+cc.isFocusable()+", isFocusCycleRoot: "+cc.isFocusCycleRoot()+", is PolicySet: "+cc.isFocusTraversalPolicySet()+", is PolicyProvider: "+cc.isFocusTraversalPolicyProvider());
            if (cc.isFocusCycleRoot()) {
                System.err.println("DEFAULT component: "+ftp.getDefaultComponent(fcr)+", first component: "+ftp.getFirstComponent(fcr));
                if (cc != fcr) {
                    cc.setFocusCycleRoot(false);
                    cc.setFocusTraversalPolicyProvider(false);
                    cc.setFocusTraversalPolicy(null);
                }
            }
            if (cc instanceof JComponent) {
                System.err.println("NEXT Focusable: "+((JComponent) cc).getNextFocusableComponent());
                System.err.println("NEXT After:     "+ftp.getComponentAfter(fcr, c));
            }
            Component[] subComponents = cc.getComponents();
            for (Component sc : subComponents) {
                traverseComponents(sc, fcr, ftp);
            }
        }
    }
    
    private static void requestFocusForExpression() {
        Component c = currentEvaluator.expressionComboBox.getEditor().getEditorComponent();
        if (c instanceof JScrollPane) {
            c = ((JScrollPane) c).getViewport().getView();
        }
        c.requestFocusInWindow();
    }
    
    private static void close() {
        //currentEvaluator = null;
        evalDialog.setVisible(false);
        try {
            currentEvaluator.ignoreEvents = true;
            // Clean the input line
            currentEvaluator.expressionComboBox.setSelectedItem(""); // NOI18N
        } finally {
            currentEvaluator.ignoreEvents = false;
        }
        // Clean the model
        currentEvaluator.result = null;
        currentEvaluator.viewModelListener.updateModel();
        /*
        evaluatorPanel.destroy();
        evalDialog.dispose();
        evalDialog = null;
         */
    }
    
    private class SessionListener extends DebuggerManagerAdapter {
        
        private boolean autoClosed = false;
        
        public void propertyChange(PropertyChangeEvent evt) {
            Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (currentSession != null && "Java".equals(currentSession.getCurrentLanguage())) {
                if (autoClosed) {
                    DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
                    if (de == null) return ;
                    JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
                    if (debugger == null) return ;
                    open(debugger);
                    autoClosed = false;
                }
            } else {
                if (evalDialog.isVisible()) {
                    autoClosed = true;
                    close();
                }
            }
        }
        
    }
    
    private class EvaluateTask implements Runnable {
        public void run() {
            String exp = getExpression();
            if (exp == null || "".equals(exp)) {
                //System.out.println("Can not evaluate '"+exp+"'");
                return ;
            }
            //System.out.println("evaluate: '"+exp+"'");
            try {
                Variable var = debugger.evaluate(exp);
                addExpressionToHistory(exp);
                displayResult(var);
            } catch (InvalidExpressionException ieex) {
                String message = ieex.getLocalizedMessage();
                Throwable t = ieex.getTargetException();
                if (t != null && t instanceof org.omg.CORBA.portable.ApplicationException) {
                    java.io.StringWriter s = new java.io.StringWriter();
                    java.io.PrintWriter p = new java.io.PrintWriter(s);
                    t.printStackTrace(p);
                    p.close();
                    message += " \n"+s.toString();
                }
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message));
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        evalDialog.requestFocus();
                        expressionComboBox.getEditor().getEditorComponent().requestFocusInWindow();
                    }
                });
            }
        }
    }
    
    private static final class CompletionedEditor implements ComboBoxEditor {
        
        private JEditorPane editor;
        private java.awt.Component component;
        private Object oldValue;
        private boolean isContextSetUp;
        private boolean canTransferFocus = true;
        
        public CompletionedEditor(javax.swing.JComboBox comboBox) {
            editor = new JEditorPane();
            editor.setBorder(null);
            editor.setKeymap(new FilteredKeymap(editor));
            editor.setFocusCycleRoot(false);
            editor.setFocusTraversalPolicy(null);
            editor.setFocusTraversalPolicyProvider(false);
            component = new JScrollPane(editor,
                                        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            ((JScrollPane)component).setBorder(null);
            editor.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    setupContext();
                }
                public void focusLost(FocusEvent e) {
                }
            });
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    canTransferFocus = false;
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    canTransferFocus = true;
                }
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
            component.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (canTransferFocus) {
                        editor.requestFocusInWindow();
                    }
                }
                public void focusLost(FocusEvent e) {
                }
            });
            JTextField referenceTextField = new JTextField();
            Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
            editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
            tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        }
        
        public void setupContext() {
            if (!isContextSetUp) {
                WatchPanel.setupContext(editor);
                HelpCtx.setHelpIDString(editor, Evaluator.class.getName());
                isContextSetUp = true;
            }
        }
        
        public void addActionListener(java.awt.event.ActionListener actionListener) {
        }

        public void removeActionListener(java.awt.event.ActionListener actionListener) {
        }

        public java.awt.Component getEditorComponent() {
            return component;
        }
        
        javax.swing.text.Document getDocument() {
            return editor.getDocument();
        }

        public Object getItem() {
            Object newValue = editor.getText();
            
            if (oldValue != null && !(oldValue instanceof String))  {
                // The original value is not a string. Should return the value in it's
                // original type.
                if (newValue.equals(oldValue.toString()))  {
                    return oldValue;
                } else {
                    // Must take the value from the editor and get the value and cast it to the new type.
                    Class cls = oldValue.getClass();
                    try {
                        Method method = cls.getMethod("valueOf", new Class[]{String.class});
                        newValue = method.invoke(oldValue, new Object[] { editor.getText()});
                    } catch (Exception ex) {
                        // Fail silently and return the newValue (a String object)
                    }
                }
            }
            return newValue;
        }

        public void setItem(Object obj) {
            if (obj != null)  {
                editor.setText(obj.toString());
                
                oldValue = obj;
            } else {
                editor.setText("");
            }
        }
        
        public void selectAll() {
            editor.selectAll();
            editor.requestFocus();
        }
        
    }
    
    /**
     * Inspired by org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener.
     */
    private static class EvaluatorModelListener extends DebuggerManagerAdapter {
        
        private String          viewType;
        private JComponent      view;
        private List models = new ArrayList(11);
    
    
        public EvaluatorModelListener(JComponent view) {
            this.viewType = "LocalsView"; // NOI18N
            this.view = view;
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            updateModel ();
        }

        public void destroy () {
            DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            Models.setModelsToView (
                view, 
                Models.EMPTY_MODEL
            );
        }

        public void propertyChange (PropertyChangeEvent e) {
            Evaluator eval = currentEvaluator;
            if (eval != null) {
                eval.csfListener = null;
                DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
                if (de == null) return ;
                JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
                eval.setDebugger(debugger);
            }
            updateModel ();
        }
    
        public synchronized void updateModel () {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            DebuggerEngine e = dm.getCurrentEngine ();
            
            List treeModels;
            List treeModelFilters;
            List treeExpansionModels;
            List nodeModels;
            List nodeModelFilters;
            List tableModels;
            List tableModelFilters;
            List nodeActionsProviders;
            List nodeActionsProviderFilters;
            List columnModels;
            List mm;
            ContextProvider cp = e != null ? DebuggerManager.join(e, dm) : dm;
            treeModels =            cp.lookup (viewType, TreeModel.class);
            treeModelFilters =      cp.lookup (viewType, TreeModelFilter.class);
            treeExpansionModels =   cp.lookup (viewType, TreeExpansionModel.class);
            nodeModels =            cp.lookup (viewType, NodeModel.class);
            nodeModelFilters =      cp.lookup (viewType, NodeModelFilter.class);
            tableModels =           cp.lookup (viewType, TableModel.class);
            tableModelFilters =     cp.lookup (viewType, TableModelFilter.class);
            nodeActionsProviders =  cp.lookup (viewType, NodeActionsProvider.class);
            nodeActionsProviderFilters = cp.lookup (viewType, NodeActionsProviderFilter.class);
            columnModels =          cp.lookup (viewType, ColumnModel.class);
            mm =                    cp.lookup (viewType, Model.class);
            
            List treeNodeModelsCompound = new ArrayList(11);
            treeNodeModelsCompound.add(treeModels);
            for (int i = 0; i < 2; i++) {
                treeNodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            treeNodeModelsCompound.add(nodeModels);
            for (int i = 0; i < 7; i++) {
                treeNodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);
            /*
            List nodeModelsCompound = new ArrayList(11);
            nodeModelsCompound.add(new ArrayList()); // An empty tree model will be added
            for (int i = 0; i < 2; i++) {
                nodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            nodeModelsCompound.add(nodeModels);
            for (int i = 0; i < 7; i++) {
                nodeModelsCompound.add(Collections.EMPTY_LIST);
            }
            CompoundModel nodeModel = Models.createCompoundModel(nodeModelsCompound);
             */
            EvaluatorModel eTreeNodeModel = new EvaluatorModel(treeNodeModel, treeNodeModel);
            
            models.clear();
            treeModels.clear();
            treeModels.add(eTreeNodeModel);
            models.add(treeModels);
            models.add(treeModelFilters);
            models.add(treeExpansionModels);
            nodeModels.clear();
            nodeModels.add(eTreeNodeModel);
            models.add(nodeModels);
            models.add(nodeModelFilters);
            models.add(tableModels);
            models.add(tableModelFilters);
            models.add(nodeActionsProviders);
            models.add(nodeActionsProviderFilters);
            models.add(columnModels);
            models.add(mm);
            
            Models.setModelsToView (
                view, 
                Models.createCompoundModel (models)
            );
        }
        
    }
    
    private static class EvaluatorModel implements TreeModel, ExtendedNodeModel {
        
        private CompoundModel treeModel;
        private CompoundModel nodeModel;
        
        public EvaluatorModel(CompoundModel treeModel, CompoundModel nodeModel) {
            this.treeModel = treeModel;
            this.nodeModel = nodeModel;
        }
        
        public void addModelListener(ModelListener l) {
            treeModel.addModelListener(l);
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(parent)) {
                Evaluator eval = currentEvaluator;
                if (eval == null || eval.result == null) {
                    return new Object[] {};
                } else {
                    return new Object[] { eval.result };
                }
            } else {
                return treeModel.getChildren(parent, from, to);
            }
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(node)) {
                return currentEvaluator == null ? 0 : 1;
            } else {
                return treeModel.getChildrenCount(node);
            }
        }

        public Object getRoot() {
            return TreeModel.ROOT;
        }

        public boolean isLeaf(Object node) throws UnknownTypeException {
            if (TreeModel.ROOT.equals(node)) {
                return false;
            } else {
                return treeModel.isLeaf(node);
            }
        }

        public void removeModelListener(ModelListener l) {
            treeModel.removeModelListener(l);
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            Evaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return eval.getExpression();
                }
            }
            return nodeModel.getDisplayName(node);
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported.");
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            Evaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return eval.getExpression();
                }
            }
            return nodeModel.getShortDescription(node);
        }

        public boolean canRename(Object node) throws UnknownTypeException {
            return nodeModel.canRename(node);
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            return nodeModel.canCopy(node);
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            return nodeModel.canCut(node);
        }

        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            return nodeModel.clipboardCopy(node);
        }

        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            return nodeModel.clipboardCut(node);
        }

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return nodeModel.getPasteTypes(node, t);
        }

        public void setName(Object node, String name) throws UnknownTypeException {
            nodeModel.setName(node, name);
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            Evaluator eval = currentEvaluator;
            if (eval != null && eval.result != null) {
                if (node == eval.result) {
                    return WatchesNodeModel.WATCH;
                }
            }
            return nodeModel.getIconBaseWithExtension(node);
        }
    }
        
}
