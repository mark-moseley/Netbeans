/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import org.openide.actions.CloseViewAction;
import org.openide.actions.CopyAction;
import org.openide.actions.SaveAction;
import org.openide.util.HelpCtx;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.diff.builtin.visualizer.LinesComponent;

/**
 * This class displays two editor panes with two files and marks the differences
 * by a different color.
 * @author  Martin Entlicher
 */
public class MergePanel extends javax.swing.JPanel {
    
    public static final String ACTION_FIRST_CONFLICT = "firstConflict"; // NOI18N
    public static final String ACTION_LAST_CONFLICT = "lastConflict"; // NOI18N
    public static final String ACTION_PREVIOUS_CONFLICT = "previousConflict"; // NOI18N
    public static final String ACTION_NEXT_CONFLICT = "nextConflict"; // NOI18N
    public static final String ACTION_ACCEPT_RIGHT = "acceptRight"; // NOI18N
    //public static final String ACTION_ACCEPT_RIGHT_AND_NEXT = "acceptRightAndNext"; // NOI18N
    public static final String ACTION_ACCEPT_LEFT = "acceptLeft"; // NOI18N
    //public static final String ACTION_ACCEPT_LEFT_AND_NEXT = "acceptLeftAndNext"; // NOI18N
    
    public static final String PROP_CAN_BE_SAVED = "canBeSaved"; // NOI18N
    public static final String PROP_CAN_NOT_BE_SAVED = "canNotBeSaved"; // NOI18N
    
    // scroll 4 lines vertically
    private static final double VERTICAL_SCROLL_NUM_LINES = 4.0;
    // scroll 4 "lines" horizontally
    private static final double HORIZONTAL_SCROLL_NUM_LINES = 4.0;
    
//    private AbstractDiff diff = null;
    private int totalHeight = 0;
    private int additionalHeight = 0;
    private int totalLines = 0;

    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    private int horizontalScroll3ChangedValue = -1;
    private int verticalScroll1ChangedValue = -1;
    private int verticalScroll3ChangedValue = -1;
    
    private LinesComponent linesComp1;
    private LinesComponent linesComp2;
    private LinesComponent linesComp3;
    
    /**
     * Line numbers in the result document. The indexes are "physical" document line numbers,
     * and values are "logical" document line numbers. If there is a space inserted (a conflict),
     * the corresponding document content is not defined and logical document line numbers
     * do not grow.
     * If the conflict starts from the beginning of the file, the logical line numbers are '0',
     * if the conflict is in the middle of the file, the logical line numbers are euqal to
     * the last logical line before this conflict.
     * The line numbers start from '1'.
     */
    private int[] resultLineNumbers;
    
    private int numConflicts;
    private int numUnresolvedConflicts;
    private int currentConflictPos;
    private List resolvedLeftConflictsLineNumbers = new ArrayList();
    private List resolvedRightConflictsLineNumbers = new ArrayList();

    private ArrayList controlListeners = new ArrayList();
    
    private SystemAction[] systemActions = new SystemAction[] { SaveAction.get(SaveAction.class),
                                                                null,
                                                                CloseMergeViewAction.get(CloseMergeViewAction.class) };

    /**
     * Used for deserialization.
     */
    private boolean diffSetSuccess = true;

    static final long serialVersionUID =3683458237532937983L;
    private static final String PLAIN_TEXT_MIME = "text/plain";

    /** Creates new DiffComponent from AbstractDiff object*/
    public MergePanel() {
//        this.diff = diff;
        initComponents ();
        prevConflictButton.setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/prev.gif")));
        nextConflictButton.setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/next.gif")));
        //prevConflictButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/prev.gif")));
        //nextConflictButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/next.gif")));
        prevConflictButton.setMnemonic (org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.prevButton.mnemonic").charAt (0));
        nextConflictButton.setMnemonic (org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.nextButton.mnemonic").charAt (0));
        //setTitle(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        //setName(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.title"));
        //HelpCtx.setHelpIDString (getRootPane (), DiffComponent.class.getName ());
        initActions();
        diffSplitPane.setResizeWeight(0.5);
        mergeSplitPane.setResizeWeight(0.5);
        putClientProperty("PersistenceType", "Never");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        commandPanel = new javax.swing.JPanel();
        firstConflictButton = new javax.swing.JButton();
        prevConflictButton = new javax.swing.JButton();
        nextConflictButton = new javax.swing.JButton();
        lastConflictButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();
        editorPanel = new javax.swing.JPanel();
        mergeSplitPane = new javax.swing.JSplitPane();
        diffSplitPane = new javax.swing.JSplitPane();
        filePanel1 = new javax.swing.JPanel();
        leftCommandPanel = new javax.swing.JPanel();
        acceptLeftButton = new javax.swing.JButton();
        acceptAndNextLeftButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        fileLabel1 = new javax.swing.JLabel();
        filePanel2 = new javax.swing.JPanel();
        rightCommandPanel = new javax.swing.JPanel();
        acceptRightButton = new javax.swing.JButton();
        acceptAndNextRightButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane2 = new javax.swing.JEditorPane();
        fileLabel2 = new javax.swing.JLabel();
        resultPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        jEditorPane3 = new javax.swing.JEditorPane();
        resultLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        commandPanel.setLayout(new java.awt.GridBagLayout());

        firstConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        firstConflictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstConflictButtonActionPerformed(evt);
            }
        });

        commandPanel.add(firstConflictButton, new java.awt.GridBagConstraints());

        prevConflictButton.setToolTipText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.prevButton.toolTipText"));
        prevConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        prevConflictButton.setMaximumSize(new java.awt.Dimension(24, 24));
        prevConflictButton.setMargin(new java.awt.Insets(1, 1, 0, 1));
        prevConflictButton.setMinimumSize(new java.awt.Dimension(24, 24));
        prevConflictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevConflictButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 1);
        commandPanel.add(prevConflictButton, gridBagConstraints);

        nextConflictButton.setToolTipText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.nextButton.toolTipText"));
        nextConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        nextConflictButton.setMaximumSize(new java.awt.Dimension(24, 24));
        nextConflictButton.setMargin(new java.awt.Insets(1, 1, 0, 1));
        nextConflictButton.setMinimumSize(new java.awt.Dimension(24, 24));
        nextConflictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextConflictButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        commandPanel.add(nextConflictButton, gridBagConstraints);

        lastConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        lastConflictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastConflictButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 2);
        commandPanel.add(lastConflictButton, gridBagConstraints);

        statusLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 1);
        commandPanel.add(statusLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(commandPanel, gridBagConstraints);

        editorPanel.setLayout(new java.awt.GridBagLayout());

        editorPanel.setPreferredSize(new java.awt.Dimension(700, 600));
        mergeSplitPane.setDividerSize(4);
        mergeSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        diffSplitPane.setDividerSize(4);
        filePanel1.setLayout(new java.awt.GridBagLayout());

        leftCommandPanel.setLayout(new java.awt.GridBagLayout());

        acceptLeftButton.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptLeftButton.text"));
        acceptLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptLeftButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 1);
        leftCommandPanel.add(acceptLeftButton, gridBagConstraints);

        acceptAndNextLeftButton.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptAndNextLeftButton"));
        acceptAndNextLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptAndNextLeftButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 2);
        leftCommandPanel.add(acceptAndNextLeftButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        filePanel1.add(leftCommandPanel, gridBagConstraints);

        jEditorPane1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jEditorPane1CaretUpdate(evt);
            }
        });

        jScrollPane1.setViewportView(jEditorPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel1.add(jScrollPane1, gridBagConstraints);

        fileLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel1.add(fileLabel1, gridBagConstraints);

        diffSplitPane.setLeftComponent(filePanel1);

        filePanel2.setLayout(new java.awt.GridBagLayout());

        rightCommandPanel.setLayout(new java.awt.GridBagLayout());

        acceptRightButton.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptRightButton.text"));
        acceptRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptRightButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 1);
        rightCommandPanel.add(acceptRightButton, gridBagConstraints);

        acceptAndNextRightButton.setText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptAndNextRightButton"));
        acceptAndNextRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptAndNextRightButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 2);
        rightCommandPanel.add(acceptAndNextRightButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        filePanel2.add(rightCommandPanel, gridBagConstraints);

        jEditorPane2.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jEditorPane2CaretUpdate(evt);
            }
        });

        jScrollPane2.setViewportView(jEditorPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel2.add(jScrollPane2, gridBagConstraints);

        fileLabel2.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel2.add(fileLabel2, gridBagConstraints);

        diffSplitPane.setRightComponent(filePanel2);

        mergeSplitPane.setLeftComponent(diffSplitPane);

        resultPanel.setLayout(new java.awt.GridBagLayout());

        resultScrollPane.setViewportView(jEditorPane3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultPanel.add(resultScrollPane, gridBagConstraints);

        resultLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        resultPanel.add(resultLabel, gridBagConstraints);

        mergeSplitPane.setRightComponent(resultPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editorPanel.add(mergeSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void firstConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_FIRST_CONFLICT);
    }//GEN-LAST:event_firstConflictButtonActionPerformed

    private void prevConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_PREVIOUS_CONFLICT);
    }//GEN-LAST:event_prevConflictButtonActionPerformed

    private void nextConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_nextConflictButtonActionPerformed

    private void lastConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_LAST_CONFLICT);
    }//GEN-LAST:event_lastConflictButtonActionPerformed

    private void acceptRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptRightButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_RIGHT);
    }//GEN-LAST:event_acceptRightButtonActionPerformed

    private void acceptAndNextRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptAndNextRightButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_RIGHT);
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_acceptAndNextRightButtonActionPerformed

    private void acceptAndNextLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptAndNextLeftButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_LEFT);
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_acceptAndNextLeftButtonActionPerformed

    private void acceptLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptLeftButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_LEFT);
    }//GEN-LAST:event_acceptLeftButtonActionPerformed

  private void jEditorPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane1CaretUpdate
// Add your handling code here:
/*      int pos = evt.getDot();
      int line = org.openide.text.NbDocument.findLineNumber((StyledDocument) jEditorPane1.getDocument(), pos);
      StyledDocument linesDoc = (StyledDocument)jEditorPane1.getDocument();
      int numLines = org.openide.text.NbDocument.findLineNumber(linesDoc, linesDoc.getEndPosition().getOffset());
      if (line <= numLines) {
          jEditorPane1.setCaretPosition(org.openide.text.NbDocument.findLineOffset(linesDoc, line));
      }
 */
  }//GEN-LAST:event_jEditorPane1CaretUpdate

  private void jEditorPane2CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane2CaretUpdate
// Add your handling code here:
/*      int pos = evt.getDot();
      int line = org.openide.text.NbDocument.findLineNumber((StyledDocument) jEditorPane2.getDocument(), pos);
      StyledDocument linesDoc = (StyledDocument)jEditorPane2.getDocument();
      int numLines = org.openide.text.NbDocument.findLineNumber(linesDoc, linesDoc.getEndPosition().getOffset());
      if (line <= numLines) {
          jEditorPane2.setCaretPosition(org.openide.text.NbDocument.findLineOffset(linesDoc, line));
      }
 */
  }//GEN-LAST:event_jEditorPane2CaretUpdate

  public void setNumConflicts(int numConflicts) {
      this.numConflicts = numConflicts;
      this.numUnresolvedConflicts = numConflicts;
  }
    
  public void setCurrentLine(int line, int diffLength, int conflictPos,
                             int resultLine) {
      if (line > 0) {
          showLine12(line, diffLength);
          showLine3(resultLine, diffLength);
          if (conflictPos >= 0) this.currentConflictPos = conflictPos;
          updateStatusLine();
          updateAcceptButtons(line);
      }
  }
  
  public void setNeedsSaveState(boolean needsSave) {
      firePropertyChange((needsSave) ? PROP_CAN_BE_SAVED : PROP_CAN_NOT_BE_SAVED, null, null);
  }
  
  public synchronized void addControlActionListener(ActionListener listener) {
      controlListeners.add(listener);
  }
  
  public synchronized void removeControlActionListener(ActionListener listener) {
      controlListeners.remove(listener);
  }
  
  private void updateStatusLine() {
      statusLabel.setText(org.openide.util.NbBundle.getMessage(MergePanel.class,
          "MergePanel.statusLine", Integer.toString(currentConflictPos + 1),
          Integer.toString(numConflicts), Integer.toString(numUnresolvedConflicts)));
  }
  
  private void updateAcceptButtons(int linePos) {
      Integer conflictPos = new Integer(linePos);
      boolean left = resolvedLeftConflictsLineNumbers.contains(conflictPos);
      boolean right = resolvedRightConflictsLineNumbers.contains(conflictPos);
      acceptLeftButton.setEnabled(!left);
      acceptAndNextLeftButton.setEnabled(!left);
      acceptRightButton.setEnabled(!right);
      acceptAndNextRightButton.setEnabled(!right);
  }
  
  private void fireControlActionCommand(String command) {
      ArrayList listeners;
      synchronized (this) {
          listeners = new ArrayList(controlListeners);
      }
      ActionEvent evt = new ActionEvent(this, 0, command);
      for (Iterator it = listeners.iterator(); it.hasNext(); ) {
          ActionListener l = (ActionListener) it.next();
          l.actionPerformed(evt);
      }
  }

    private void jScrollBar1AdjustmentValueChanged (java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBar1AdjustmentValueChanged
        // Add your handling code here:
    }//GEN-LAST:event_jScrollBar1AdjustmentValueChanged

    private void closeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // Add your handling code here:
        exitForm(null);
    }//GEN-LAST:event_closeButtonActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
/*        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, lineNumbersVisible);
        } catch (Throwable exc) {
            // editor module not found
        }
        //System.out.println("exitForm() called.");
        //diff.closing();
        //close();
        //dispose ();
        for(Iterator it = closeListeners.iterator(); it.hasNext(); ) {
            ((TopComponentCloseListener) it.next()).closing();
        }
 */
    }//GEN-LAST:event_exitForm

    public void setSystemActions(SystemAction[] actions) {
        this.systemActions = actions;
    }
    
    public SystemAction[] getSystemActions() {
        return systemActions;
    }
    
    private void initActions() {
        jEditorPane1.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane1);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane1);
            }
        });
        jEditorPane2.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane2);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane2);
            }
        });
        jEditorPane3.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane3);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane3);
            }
        });
    }
    
    private Hashtable kitActions;
            /** Listener for copy action enabling  */
    private PropertyChangeListener copyL;
    private PropertyChangeListener copyP;
    
    private Action getAction (String s, JEditorPane editor) {
        if (kitActions == null) {
            kitActions = new Hashtable();
        }
        Hashtable actions = (Hashtable) kitActions.get(editor);
        if (actions == null) {
            EditorKit kit = editor.getEditorKit();
            if (kit == null) {
                return null;
            }
            
            Action[] a = kit.getActions ();
            actions = new Hashtable (a.length);
            int k = a.length;
            for (int i = 0; i < k; i++)
                actions.put (a[i].getValue (Action.NAME), a[i]);
            kitActions.put(editor, actions);
        }
        return (Action) actions.get (s);
    }
    
    private void editorActivated(final JEditorPane editor) {
        //System.out.println("editor("+editor+") activated.");
        final Action copy = getAction (DefaultEditorKit.copyAction, editor);
        if (copy != null) {
            final CallbackSystemAction sysCopy
            = ((CallbackSystemAction) SystemAction.get (CopyAction.class));
            final ActionPerformer perf = new ActionPerformer () {
                public void performAction (SystemAction action) {
                    copy.actionPerformed (new ActionEvent (editor, 0, "")); // NOI18N
                }
            };
            sysCopy.setActionPerformer(copy.isEnabled() ? perf : null);
            PropertyChangeListener copyListener;
            copy.addPropertyChangeListener(copyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                        if (((Boolean)evt.getNewValue()).booleanValue()) {
                            sysCopy.setActionPerformer(perf);
                        } else if (sysCopy.getActionPerformer() == perf) {
                            sysCopy.setActionPerformer(null);
                        }
                    }
                }
            });
            if (editor.equals(jEditorPane1)) copyL = copyListener;
            else copyP = copyListener;
        }
    }
    
    private void editorDeactivated(JEditorPane editor) {
        //System.out.println("editorDeactivated ("+editor+")");
        Action copy = getAction (DefaultEditorKit.copyAction, editor);
        PropertyChangeListener copyListener;
        if (editor.equals(jEditorPane1)) copyListener = copyL;
        else copyListener = copyP;
        if (copy != null) {
            copy.removePropertyChangeListener(copyListener);
        }
    }
    
    private void addWindowListener(java.awt.event.WindowListener listener) {
        java.awt.Component ancestor = getTopLevelAncestor();
        if (ancestor instanceof java.awt.Window) {
            ((java.awt.Window) ancestor).addWindowListener(listener);
        }
    }


    public void open() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                diffSplitPane.setDividerLocation(0.5);
                mergeSplitPane.setDividerLocation(0.5);
                openPostProcess();
            }
        });
    }

    protected void openPostProcess() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initGlobalSizes();
                //showLine(1, 0);
                addChangeListeners();
/*                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes(); // do that again to be sure that components are initialized.
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                syncFont(); // Components have to be fully initialized before font syncing
                                addChangeListeners();
                            }
                        });
                    }
                });
 */
            }
        });
    }

    /*
    public void removeNotify() {
        System.out.println("removeNotify() called");
        exitForm(null);
        super.removeNotify();
    }
     */
    private void initGlobalSizes() {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getDocument();
        int numLines1 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getEndPosition().getOffset());
        int numLines2 = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getEndPosition().getOffset());
        int numLines = Math.max(numLines1, numLines2);
        if (numLines < 1) numLines = 1;
        this.totalLines = numLines;
        //        int totHeight = editorPanel1.getSize().height;
        int totHeight = jEditorPane1.getSize().height;
        //        int value = editorPanel2.getSize().height;
        int value = jEditorPane2.getSize().height;
        if (value > totHeight) totHeight = value;
        this.totalHeight = totHeight;
    }

    private void showLine12(int line, int diffLength) {
        //System.out.println("showLine("+line+", "+diffLength+")");
        this.linesComp1.setActiveLine(line);
        this.linesComp2.setActiveLine(line);
        linesComp1.repaint();
        linesComp2.repaint();
        int padding = 5;
        if (line <= 5) padding = line/2;
        int off1, off2;
        int ypos;
        int viewHeight = jViewport1.getExtentSize().height;
        java.awt.Point p1, p2;
        initGlobalSizes(); // The window might be resized in the mean time.
        p1 = jViewport1.getViewPosition();
        p2 = jViewport2.getViewPosition();
        ypos = (totalHeight*(line - padding - 1))/(totalLines + 1);
        int viewSize = jViewport1.getViewRect().y;
        if (ypos < p1.y || ypos + ((diffLength + padding)*totalHeight)/totalLines > p1.y + viewHeight) {
            //System.out.println("resetting posision=" + ypos);
            p1.y = ypos;
            p2.y = ypos;
            setViewPosition(p1, p2);
        }
        off1 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane1.getDocument(), line);
        off2 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane2.getDocument(), line);
        jEditorPane1.setCaretPosition(off1);
        jEditorPane2.setCaretPosition(off2);
        //D.deb("off1 = "+off1+", off2 = "+off2+", totalHeight = "+totalHeight+", totalLines = "+totalLines+", ypos = "+ypos);
        //System.out.println("off1 = "+off1+", off2 = "+off2+", totalHeight = "+totalHeight+", totalLines = "+totalLines+", ypos = "+ypos);
    }
    
    private void showLine3(int line, int diffLength) {
        linesComp3.setActiveLine(line);
        linesComp3.repaint();
    }
    
    private void setViewPosition(java.awt.Point p1, java.awt.Point p2) {
        jViewport1.setViewPosition(p1);
        jViewport1.repaint(jViewport1.getViewRect());
        jViewport2.setViewPosition(p2);
        jViewport2.repaint(jViewport2.getViewRect());
    }
    
    private void joinScrollBars() {
        final JScrollBar scrollBarH1 = jScrollPane1.getHorizontalScrollBar();
        final JScrollBar scrollBarV1 = jScrollPane1.getVerticalScrollBar();
        final JScrollBar scrollBarH2 = jScrollPane2.getHorizontalScrollBar();
        final JScrollBar scrollBarV2 = jScrollPane2.getVerticalScrollBar();
        final JScrollBar scrollBarH3 = resultScrollPane.getHorizontalScrollBar();
        final JScrollBar scrollBarV3 = resultScrollPane.getVerticalScrollBar();
        scrollBarV1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV1.getValue();
                int oldValue = scrollBarV2.getValue();
                if (oldValue != value) {
                    scrollBarV2.setValue(value);
//                    System.out.println("setting v2=" + value);
//                    Thread.dumpStack();
                }
                // TODO use a better algorithm to adjust scrollbars, if there are large changes, this will not work optimally.
                if (value == verticalScroll1ChangedValue) return ;
                int max1 = scrollBarV1.getMaximum();
                int max2 = scrollBarV3.getMaximum();
                int ext1 = scrollBarV1.getModel().getExtent();
                int ext2 = scrollBarV3.getModel().getExtent();
                if (max1 == ext1) verticalScroll3ChangedValue = 0;
                else verticalScroll3ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                verticalScroll1ChangedValue = -1;
                scrollBarV3.setValue(verticalScroll3ChangedValue);
            }
        });
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollBarV2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV2.getValue();
                int oldValue = scrollBarV1.getValue();
                if (oldValue != value) {
                    scrollBarV1.setValue(value);
//                    System.out.println("setting v1 to=" + value);
                }
            }
        });
        /* don't not let the result source vertical scrolling to influence the diff panels.
        scrollBarV3.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV3.getValue();
                if (value == verticalScroll3ChangedValue) return ;
                int max1 = scrollBarV3.getMaximum();
                int max2 = scrollBarV1.getMaximum();
                int ext1 = scrollBarV3.getModel().getExtent();
                int ext2 = scrollBarV1.getModel().getExtent();
                if (max1 == ext1) verticalScroll1ChangedValue = 0;
                else verticalScroll1ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                verticalScroll3ChangedValue = -1;
                scrollBarV1.setValue(verticalScroll1ChangedValue);
            }
        });
         */
        scrollBarH1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH1.getValue();
                //                System.out.println("stateChangedH1:value = "+value+", horizontalScroll1ChangedValue = "+horizontalScroll1ChangedValue);
                if (value == horizontalScroll1ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max1 == ext1) horizontalScroll2ChangedValue = 0;
                else horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                horizontalScroll1ChangedValue = -1;
                //                System.out.println("H1 value = "+value+" => H2 value = "+horizontalScroll2ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        scrollBarH2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH2.getValue();
                //                System.out.println("stateChangedH2:value = "+value+", horizontalScroll2ChangedValue = "+horizontalScroll2ChangedValue);
                if (value == horizontalScroll2ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int max3 = scrollBarH3.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                int ext3 = scrollBarH3.getModel().getExtent();
                if (max2 == ext2) {
                    horizontalScroll1ChangedValue = 0;
                    horizontalScroll3ChangedValue = 0;
                } else {
                    horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max2 - ext2);
                    horizontalScroll3ChangedValue = (value*(max3 - ext3))/(max2 - ext2);
                }
                horizontalScroll2ChangedValue = -1;
                //                System.out.println("H2 value = "+value+" => H1 value = "+horizontalScroll1ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
                scrollBarH3.setValue(horizontalScroll3ChangedValue);
            }
        });
        scrollBarH3.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH3.getValue();
                //                System.out.println("stateChangedH1:value = "+value+", horizontalScroll1ChangedValue = "+horizontalScroll1ChangedValue);
                if (value == horizontalScroll3ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int max3 = scrollBarH3.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                int ext3 = scrollBarH3.getModel().getExtent();
                if (max3 == ext3) {
                    horizontalScroll1ChangedValue = 0;
                    horizontalScroll2ChangedValue = 0;
                } else {
                    horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max3 - ext3);
                    horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max3 - ext3);
                }
                horizontalScroll3ChangedValue = -1;
                //                System.out.println("H1 value = "+value+" => H2 value = "+horizontalScroll2ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        diffSplitPane.setDividerLocation(0.5);
        mergeSplitPane.setDividerLocation(0.5);
    }
    
    private String strCharacters(char c, int num) {
        StringBuffer s = new StringBuffer();
        while(num-- > 0) {
            s.append(c);
        }
        return s.toString();
    }
    
    private void customizeEditor(JEditorPane editor) {
        EditorKit kit = editor.getEditorKit();
        /*
        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, Boolean.FALSE);
        } catch (Throwable exc) {
            // editor module not found
        }
         */
        StyledDocument doc;
        Document document = editor.getDocument();
/*        StyledDocument docLines = new DefaultStyledDocument();
        textLines.setStyledDocument(docLines);
 */
        try {
            doc = (StyledDocument) editor.getDocument();
        } catch(ClassCastException e) {
            doc = new DefaultStyledDocument();
            try {
                doc.insertString(0, document.getText(0, document.getLength()), null);
            } catch (BadLocationException ble) {
                // leaving the document empty
            }
            editor.setDocument(doc);
        }
        //int lastOffset = doc.getEndPosition().getOffset();
        //int numLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        //int numLength = Integer.toString(numLines).length();
        //        textLines.setForeground(numForegroundColor);
        //        textLines.setBackground(numBackgroundColor);
        /*
        for (int line = 0; line <= numLines; line++) {
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            String lineStr = Integer.toString(line+1);
            if (lineStr.length() < numLength) lineStr = strCharacters(' ', numLength - lineStr.length()) + lineStr;
            //lineStr = " "+lineStr+" "; // NOI18N
            try {
                if (line < numLines) lineStr += "\n"; // NOI18N
                docLines.insertString(docLines.getLength(), lineStr, null);
            } catch (BadLocationException e) {
                E.deb("Internal ERROR: "+e.getMessage()); // NOI18N
            }
        }
         */
        //        joinScrollBars();
    }
    
    private void setScrollBarsIncrements() {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        int lineHeight = jEditorPane1.getSize().height/org.openide.text.NbDocument.findLineNumber(doc, doc.getEndPosition().getOffset());
        jScrollPane1.getVerticalScrollBar().setUnitIncrement((int) (VERTICAL_SCROLL_NUM_LINES*lineHeight));
        jScrollPane2.getVerticalScrollBar().setUnitIncrement((int) (VERTICAL_SCROLL_NUM_LINES*lineHeight));
        jScrollPane1.getHorizontalScrollBar().setUnitIncrement((int) (HORIZONTAL_SCROLL_NUM_LINES*lineHeight));
        jScrollPane2.getHorizontalScrollBar().setUnitIncrement((int) (HORIZONTAL_SCROLL_NUM_LINES*lineHeight));
    }
    
    private void addChangeListeners() {
        jEditorPane1.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("1:evt = "+evt+", Property NAME = "+evt.getPropertyName());
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp1.changedAll();
                    }
                });
            }
        });
        jEditorPane2.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("2:evt = "+evt+", Property NAME = "+evt.getPropertyName());
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp2.changedAll();
                    }
                });
            }
        });
        jEditorPane3.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("2:evt = "+evt+", Property NAME = "+evt.getPropertyName());
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp3.changedAll();
                    }
                });
            }
        });
    }
    
    public void setSource1(Reader r) throws IOException {
        //D.deb("setFile("+url+")"); // NOI18N
        //System.out.println("setFile1("+url+")");
        EditorKit kit = jEditorPane1.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane1.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        }
        kit.install(jEditorPane1);
        jEditorPane1.setDocument(doc);
        //jEditorPane1.setPage(url);
        jEditorPane1.setEditable(false);
        customizeEditor(jEditorPane1);
        linesComp1 = new LinesComponent(jEditorPane1);
        jScrollPane1.setRowHeaderView(linesComp1);
        jViewport1 = jScrollPane1.getViewport();
    }
    
    public void setSource2(Reader r) throws IOException {
        //D.deb("setFile("+url+")"); // NOI18N
        EditorKit kit = jEditorPane2.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane2.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        }
        kit.install(jEditorPane2);
        jEditorPane2.setDocument(doc);
        //jEditorPane2.setPage(url);
        jEditorPane2.setEditable(false);
        
        customizeEditor(jEditorPane2);
        linesComp2 = new LinesComponent(jEditorPane2);
        jScrollPane2.setRowHeaderView(linesComp2);
        jViewport2 = jScrollPane2.getViewport();
        // add scrollbar listeners..
        joinScrollBars();
    }
    
    public void setResultSource(Reader r) throws IOException {
        EditorKit kit = jEditorPane3.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane3.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        }
        kit.install(jEditorPane3);
        jEditorPane3.setDocument(doc);
        //jEditorPane2.setPage(url);
        jEditorPane3.setEditable(false);
        customizeEditor(jEditorPane3);
        linesComp3 = new LinesComponent(jEditorPane3);
        resultScrollPane.setRowHeaderView(linesComp3);
        resultLineNumbers = new int[1];
        assureResultLineNumbersLength(
            org.openide.text.NbDocument.findLineNumber((StyledDocument) doc,
                                                       doc.getEndPosition().getOffset()) + 1);
        for (int i = 0; i < resultLineNumbers.length; i++) resultLineNumbers[i] = i;
    }
    
    private static final int EXTRA_CAPACITY = 50;
    private void assureResultLineNumbersLength(int length) {
        if (length > resultLineNumbers.length) {
            int[] newrln = new int[length + EXTRA_CAPACITY];
            System.arraycopy(resultLineNumbers, 0, newrln, 0, resultLineNumbers.length);
            resultLineNumbers = newrln;
        }
    }
    
    /**
     * Copy a part of first document into the result document.
     * @param line1 The starting line in the first source
     * @param line2 The ending line in the first source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     */
    public void copySource1ToResult(int line1, int line2, int line3) {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            copy(doc1, line1, line2, doc2, line3);
        } catch (BadLocationException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
    }
    
    /**
     * Copy a part of second document into the result document.
     * @param line1 The starting line in the second source
     * @param line2 The ending line in the second source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     */
    public void copySource2ToResult(int line1, int line2, int line3) {
        StyledDocument doc1 = (StyledDocument) jEditorPane2.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            copy(doc1, line1, line2, doc2, line3);
        } catch (BadLocationException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
    }
    
    /** Copies a part of one document into another. */
    private void copy(StyledDocument doc1, int line1, int line2, StyledDocument doc2, int line3) throws BadLocationException {
        int offset1 = org.openide.text.NbDocument.findLineOffset(doc1, line1 - 1);
        int offset2 = (line2 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line2)
                                   : (doc1.getLength() - 1);
        if (offset1 >= offset2) return ;
        int offset3 = org.openide.text.NbDocument.findLineOffset(doc2, line3);
        int length = offset2 - offset1;
        if (line2 < 0) length++;
        String text = doc1.getText(offset1, length);
        //System.out.println("copy: offset1 = "+offset1+", offset2 = "+offset2);
        //System.out.println(">> copy text: at "+offset3+" <<\n"+text+">>  <<");
        doc2.insertString(offset3, text, null);
        // Adjust the line numbers
        if (line2 < 0) line2 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getLength());
        int numLines = line2 - line1 + 1;
        //System.out.println("copy("+line1+", "+line2+", "+line3+"): resultLineNumbers.length = "+resultLineNumbers.length);
        assureResultLineNumbersLength(line3 + numLines);
        if (resultLineNumbers[line3] == 0 && line3 > 0) resultLineNumbers[line3] = resultLineNumbers[line3 - 1] + 1;
        int resultLine = resultLineNumbers[line3];
        //System.out.println("resultLine = rln["+line3+"] = "+resultLine);
        //System.out.println("insertNumbers("+line3+", "+resultLine+", "+numLines+")");
        linesComp3.insertNumbers(line3 - 1, resultLine, numLines);
        linesComp3.changedAll();
        for (int i = 0; i < numLines; i++) resultLineNumbers[line3 + i] = resultLine + i;
    }
    
    /**
     * Replace a part of result with a part of the first source.
     * @param line1 The starting line in the first source
     * @param line2 The ending line in the first source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     * @param line4 The ending line in the result
     */
    public void replaceSource1InResult(int line1, int line2, int line3, int line4) {
        //System.out.println("replaceSource1InResult("+line1+", "+line2+", "+line3+", "+line4+")");
        Integer conflictLine = new Integer((line1 > 0) ? line1 : 1);
        // If trying to resolve the conflict twice simply return .
        if (resolvedLeftConflictsLineNumbers.contains(conflictLine)) return ;
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            replace(doc1, line1, line2, doc2, line3, line4);
        } catch (BadLocationException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
        if (resolvedRightConflictsLineNumbers.contains(conflictLine)) {
            resolvedRightConflictsLineNumbers.remove(conflictLine);
        } else {
            // We've resolved the conflict.
            numUnresolvedConflicts--;
            updateStatusLine();
        }
        resolvedLeftConflictsLineNumbers.add(conflictLine);
        updateAcceptButtons(line1);
    }
    
    /**
     * Replace a part of result with a part of the second source.
     * @param line1 The starting line in the second source
     * @param line2 The ending line in the second source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     * @param line4 The ending line in the result
     */
    public void replaceSource2InResult(int line1, int line2, int line3, int line4) {
        //System.out.println("replaceSource2InResult("+line1+", "+line2+", "+line3+", "+line4+")");
        Integer conflictLine = new Integer((line1 > 0) ? line1 : 1);
        // If trying to resolve the conflict twice simply return .
        if (resolvedRightConflictsLineNumbers.contains(conflictLine)) return ;
        StyledDocument doc1 = (StyledDocument) jEditorPane2.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            replace(doc1, line1, line2, doc2, line3, line4);
        } catch (BadLocationException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
        if (resolvedLeftConflictsLineNumbers.contains(conflictLine)) {
            resolvedLeftConflictsLineNumbers.remove(conflictLine);
        } else {
            // We've resolved the conflict.
            numUnresolvedConflicts--;
            updateStatusLine();
        }
        resolvedRightConflictsLineNumbers.add(conflictLine);
        updateAcceptButtons(line1);
    }
    
    private void replace(StyledDocument doc1, int line1, int line2,
                         StyledDocument doc2, int line3, int line4) throws BadLocationException {
        //dumpResultLineNumbers();
        //System.out.println("replace("+line1+", "+line2+", "+line3+", "+line4+")");
        int offset1 = (line1 > 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line1 - 1)
                                  : 0;
        int offset2 = (line2 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line2)
                                   : (doc1.getLength() - 1);
        int offset3 = (line3 > 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line3 - 1)
                                  : 0;
        int offset4 = (line4 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line4)
                                   : (doc2.getLength() - 1);
        //System.out.println("replace: offsets = "+offset1+", "+offset2+", "+offset3+", "+offset4);
        int length = offset2 - offset1;
        if (line2 < 0) length++;
        String text = doc1.getText(offset1, length);
        doc2.remove(offset3, offset4 - offset3);
        doc2.insertString(offset3, text, null);
        // Adjust the line numbers
        assureResultLineNumbersLength(line4);
        //int lineDiff;
        int physicalLineDiff = line2 - line1 - (line4 - line3);
        if (physicalLineDiff > 0) {
            System.arraycopy(resultLineNumbers, line4 + 1,
                             resultLineNumbers, line4 + physicalLineDiff + 1,
                             resultLineNumbers.length - line4 - physicalLineDiff - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        int lineDiff = (resultLineNumbers[line3] <= resultLineNumbers[line3 - 1])
                       ? (line2 - line1 + 1)
                       : (line2 - line1 - (line4 - line3));
        //if (resultLineNumbers[line3] <= resultLineNumbers[line3 - 1]) {
            // There are no line numbers defined.
            //lineDiff = line2 - line1 + 1;
        int n = resultLineNumbers[line3 - 1];
        for (int i = line3; i <= line4 + physicalLineDiff; i++) {
            resultLineNumbers[i] = ++n;
        }
            /*
            for (int i = line4 + lineDiff + 1; i < resultLineNumbers.length; i++) {
                if (resultLineNumbers[i] != 0) resultLineNumbers[i] += lineDiff;
                else break;
            }
             */
        //lineDiff = line2 - line1 + 1;
        //System.out.println("insertNumbers("+line3+", "+resultLineNumbers[line3]+", "+(line2 - line1 + 1)+")");
        linesComp3.insertNumbers(line3 - 1, resultLineNumbers[line3], line2 - line1 + 1);
        linesComp3.changedAll();
        //dumpResultLineNumbers();
        //} else {
        //    lineDiff = line2 - line1 - (line4 - line3);
        //}
        if (physicalLineDiff < 0) {
            System.arraycopy(resultLineNumbers, line4 + 1,
            resultLineNumbers, line4 + physicalLineDiff + 1,
            resultLineNumbers.length - line4 - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        adjustLineNumbers(line4 + physicalLineDiff + 1, lineDiff);
    }
    
    /*
    private void dumpResultLineNumbers() {
        System.out.print("resultLineNum[] = ");
        boolean was = false;
        for (int i = 0; i < resultLineNumbers.length; i++) {
            if (resultLineNumbers[i] == 0 && was) break;
            if (resultLineNumbers[i] != 0) was = true;
            System.out.print(resultLineNumbers[i]+", ");
        }
        System.out.println("");
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException iex) {}
    }
     */
    
    private void adjustLineNumbers(int startLine, int shift) {
        //System.out.println("adjustLineNumbers("+startLine+", "+shift+")");
        int end = resultLineNumbers.length;
        while (end > 0 && resultLineNumbers[end - 1] == 0) end--;
        int startSetLine = -1;
        int endSetLine = -1;
        //resultLineNumbers[startLine] += shift;
        for (int i = startLine; i < end; i++) {
            resultLineNumbers[i] += shift;
            if (resultLineNumbers[i] <= resultLineNumbers[i - 1]) {
                if (startSetLine > 0) {
                    //System.out.println("insertNumbers("+startSetLine+", "+resultLineNumbers[startSetLine]+", "+(i - startSetLine)+")");
                    linesComp3.insertNumbers(startSetLine - 1, resultLineNumbers[startSetLine], i - startSetLine);
                    linesComp3.changedAll();
                    //dumpResultLineNumbers();
                    startSetLine = -1;
                }
                if (endSetLine < 0) {
                    endSetLine = i;
                }
            } else {
                if (endSetLine > 0) {
                    //System.out.println("removeNumbers("+endSetLine+", "+(i - endSetLine)+")");
                    linesComp3.removeNumbers(endSetLine - 1, i - endSetLine);
                    linesComp3.changedAll();
                    //dumpResultLineNumbers();
                    endSetLine = -1;
                }
                if (startSetLine < 0) {
                    startSetLine = i;
                }
            }
        }
        if (startSetLine > 0) {
            //System.out.println("insertNumbers("+startSetLine+", "+resultLineNumbers[startSetLine]+", "+(end - startSetLine)+" (END))");
            linesComp3.insertNumbers(startSetLine - 1, resultLineNumbers[startSetLine], end - startSetLine);
            linesComp3.shrink(end - 1);
            linesComp3.changedAll();
            //dumpResultLineNumbers();
        }
        if (endSetLine > 0) {
            //System.out.println("removeNumbers("+endSetLine+", "+(end - endSetLine)+" (END))");
            linesComp3.removeNumbers(endSetLine - 1, end - endSetLine);
            linesComp3.shrink(end - 1);
            linesComp3.changedAll();
            //dumpResultLineNumbers();
        }
    }
    
    public void setSource1Title(String title) {
        fileLabel1.setText(title);
    }
    
    public void setSource2Title(String title) {
        fileLabel2.setText(title);
    }
    
    public void setResultSourceTitle(String title) {
        resultLabel.setText(title);
    }
    
    public void setStatusLabel(String status) {
        statusLabel.setText(status);
    }
    
    public void setMimeType1(String mime) {
        jEditorPane1.setContentType(mime);
        EditorKit kit = JEditorPane.createEditorKitForContentType(mime);
        if (kit == null) {
            kit = JEditorPane.createEditorKitForContentType(PLAIN_TEXT_MIME);
        }
        jEditorPane1.setEditorKit(kit);
        //Document doc = jEditorPane1.getDocument();
        //if (!(doc instanceof StyledDocument)) jEditorPane1.setDocument(new DefaultStyledDocument());
    }
    
    public void setMimeType2(String mime) {
        jEditorPane2.setContentType(mime);
        EditorKit kit = JEditorPane.createEditorKitForContentType(mime);
        if (kit == null) {
            kit = JEditorPane.createEditorKitForContentType(PLAIN_TEXT_MIME);
        }
        jEditorPane2.setEditorKit(kit);
        //Document doc = jEditorPane2.getDocument();
        //if (!(doc instanceof StyledDocument)) jEditorPane2.setDocument(new DefaultStyledDocument());
    }
    
    public void setMimeType3(String mime) {
        jEditorPane3.setContentType(mime);
        EditorKit kit = JEditorPane.createEditorKitForContentType(mime);
        if (kit == null) {
            kit = JEditorPane.createEditorKitForContentType(PLAIN_TEXT_MIME);
        }
        jEditorPane3.setEditorKit(kit);
    }
    
    /*
    public void setDocument1(Document doc) {
        if (doc != null) {
            jEditorPane1.setDocument(doc);
        }
    }
     */
    
    public void setResultDocument(Document doc) {
        if (doc != null) {
            jEditorPane3.setDocument(doc);
            jEditorPane3.setEditable(false);
            linesComp3 = new LinesComponent(jEditorPane3);
            resultScrollPane.setRowHeaderView(linesComp3);
        }
    }
    
    /*
     * Whether all conflicts are resolved and the panel can be closed.
     * @return <code>true</code> when the panel can be closed, <code>false</code> otherwise.
     *
    public boolean canClose() {
        return true;
    }
     */
    
    /**
     * Write the result content into the given writer. Skip all unresolved conflicts.
     * @param w The writer to write the result into.
     * @throws IOException When the writing process fails.
     */
    public void writeResult(Writer w) throws IOException {
        //System.out.println("writeResult()");
        /*
        try {
            jEditorPane3.getEditorKit().write(w, jEditorPane3.getDocument(),
                                              0, jEditorPane3.getDocument().getLength());
        } catch (BadLocationException blex) {
            throw new IOException(blex.getLocalizedMessage());
        }
         */
        int end = resultLineNumbers.length;
        while (end > 0 && resultLineNumbers[end - 1] == 0) end--;
        int startSetLine = -1;
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        try {
            for (int i = 1; i <= end; i++) {
                if (resultLineNumbers[i] <= resultLineNumbers[i - 1]) {
                    if (startSetLine > 0) {
                        //System.out.println("write("+startSetLine+", "+i+")");
                        int offsetStart = org.openide.text.NbDocument.findLineOffset(doc, startSetLine - 1);
                        int offsetEnd = org.openide.text.NbDocument.findLineOffset(doc, i - 1);
                        //System.out.println("  Have text(<l="+(startSetLine-1)+",off="+offsetStart+";l="+(i-1)+",off="+offsetEnd+">), length = "+doc.getLength());
                        try {
                            //System.out.println("'"+doc.getText(offsetStart, offsetEnd - offsetStart)+"'");
                            w.write(doc.getText(offsetStart, offsetEnd - offsetStart));
                        } catch (BadLocationException blex) {
                            throw new IOException(blex.getLocalizedMessage());
                        }
                        //dumpResultLineNumbers();
                        startSetLine = -1;
                    }
                } else {
                    if (startSetLine < 0) {
                        startSetLine = i;
                    }
                }
            }
            if (startSetLine > 0) {
                //System.out.println("write("+startSetLine+", "+end+" (END))");
                int offsetStart = org.openide.text.NbDocument.findLineOffset(doc, startSetLine - 1);
                int offsetEnd = doc.getLength();
                try {
                    w.write(doc.getText(offsetStart, offsetEnd - offsetStart));
                } catch (BadLocationException blex) {
                    throw new IOException(blex.getLocalizedMessage());
                }
                //dumpResultLineNumbers();
            }
        } finally {
            w.close();
        }
    }
    
    private void setHighlight(StyledDocument doc, int line1, int line2, java.awt.Color color) {
        //System.out.println("setHighlight(): <"+line1+", "+line2+">, color = "+color); // NOI18N
        //Style s = doc.addStyle("diff-style("+color+"):1500", null); // NOI18N
        //      SimpleAttributeSet attrSet = new SimpleAttributeSet();
        //      attrSet.addAttribute(StyleConstants.ColorConstants.Background, java.awt.Color.green);
        //s.addAttribute(StyleConstants.ColorConstants.Background, color);
        Style s = doc.getStyle("diff-style("+color+"):1500");
        //if (s == null) s = doc.getLogicalStyle(offset);
        if (s == null) {
            //System.out.println("setHighlight(): logical style is NULL"); // NOI18N
            s = doc.addStyle("diff-style("+color+"):1500", null); // NOI18N
            s.addAttribute(StyleConstants.ColorConstants.Background, color);
        }
        for(int line = line1-1; line < line2; line++) {
            if (line < 0) continue;
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            //System.out.println("setHighlight(): I got offset = "+offset); // NOI18N
            if (offset >= 0) {
                /*
                Style ls = doc.getLogicalStyle(offset);
                //if (ls != null) ls.addAttributes(s.copyAttributes());
                if (ls == null) {
                    ls = s;//new javax.swing.text.StyleContext.NamedStyle("diff-style("+color+"):1500", null);
                } else {
                    //ls.addAttributes(s.copyAttributes());
                }
                ls.removeAttribute(StyleConstants.ColorConstants.Background);
                ls.addAttribute(StyleConstants.ColorConstants.Background, color);
                 */
                doc.setLogicalStyle(offset, s);
                //doc.setParagraphAttributes(offset, 1, s, false);
            }
        }
        //doc.setParagraphAttributes(offset, 100, s, true);
    }
    
    private void unhighlight(StyledDocument doc) {
        int endOffset = doc.getEndPosition().getOffset();
        int endLine = org.openide.text.NbDocument.findLineNumber(doc, endOffset);
        Style s = doc.addStyle("diff-style(white):1500", null); // NOI18N
        s.addAttribute(StyleConstants.ColorConstants.Background, java.awt.Color.white);
        for(int line = 0; line <= endLine; line++) {
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            doc.setLogicalStyle(offset, s);
        }
    }
    
    public void unhighlightAll() {
        unhighlight((StyledDocument) jEditorPane1.getDocument());
        unhighlight((StyledDocument) jEditorPane2.getDocument());
    }
    
    public void highlightRegion1(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        setHighlight(doc, line1, line2, color);
    }
    
    public void highlightRegion2(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        setHighlight(doc, line1, line2, color);
    }
    
    public void highlightRegion3(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        setHighlight(doc, line1, line2, color);
    }
    
    private void addEmptyLines(StyledDocument doc, int line, int numLines) {
        int lastOffset = doc.getEndPosition().getOffset();
        int totLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        //int totLines = doc.getDefaultRootElement().getElementIndex(lastOffset);
        int offset = lastOffset;
        if (line <= totLines) {
            offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            //offset = doc.getDefaultRootElement().getElement(line).getStartOffset();
        }
        //int endOffset = doc.getEndPosition().getOffset();
        //if (offset > endOffset) offset = endOffset;
        String insStr = strCharacters('\n', numLines);
        //System.out.println("addEmptyLines = '"+insStr+"'");
        try {
            doc.insertString(offset, insStr, null);
        } catch (BadLocationException e) {
            org.openide.TopManager.getDefault().notifyException(e);
        }
        //initScrollBars();
    }
    
    public void addEmptyLines1(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        //System.out.println("addEmptyLines1: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line, numLines);
        linesComp1.addEmptyLines(line, numLines);
    }
    
    public void addEmptyLines2(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        //System.out.println("addEmptyLines2: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line, numLines);
        linesComp2.addEmptyLines(line, numLines);
    }
    
    public void addEmptyLines3(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        //System.out.println("addEmptyLines3: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line, numLines);
        linesComp3.addEmptyLines(line, numLines);
        assureResultLineNumbersLength(line + numLines);
        if (resultLineNumbers[line] == 0 && line > 0) resultLineNumbers[line] = resultLineNumbers[line - 1];
        int resultLine = resultLineNumbers[line];
        for (int i = 1; i < numLines; i++) resultLineNumbers[line + i] = resultLine;
    }
    
    
    private javax.swing.JViewport jViewport1;
    private javax.swing.JViewport jViewport2;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptRightButton;
    private javax.swing.JLabel fileLabel2;
    private javax.swing.JLabel fileLabel1;
    private javax.swing.JButton firstConflictButton;
    private javax.swing.JPanel rightCommandPanel;
    private javax.swing.JButton prevConflictButton;
    private javax.swing.JPanel filePanel2;
    private javax.swing.JPanel filePanel1;
    private javax.swing.JSplitPane diffSplitPane;
    private javax.swing.JPanel leftCommandPanel;
    private javax.swing.JButton lastConflictButton;
    private javax.swing.JEditorPane jEditorPane3;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JEditorPane jEditorPane2;
    private javax.swing.JButton nextConflictButton;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JButton acceptLeftButton;
    private javax.swing.JSplitPane mergeSplitPane;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JButton acceptAndNextRightButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton acceptAndNextLeftButton;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel commandPanel;
    private javax.swing.JLabel resultLabel;
    // End of variables declaration//GEN-END:variables

}
