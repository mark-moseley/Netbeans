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

/*
 * ProgressObject.java
 *
 * Created on May 30, 2001, 11:30 AM
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.*;
import javax.swing.*;

import org.openide.*;
import org.openide.util.*;
import org.openide.windows.TopComponent;

import javax.enterprise.deploy.spi.status.*;

/**
 *
 * @author Pete Eakle
 * @author Joe Warzecha 
 * @author George Finklang
 * @author Jeri Lockhart
 */
public class ProgressUI extends JPanel {

    private boolean changeFontSize;
    private boolean wasCancelled; 
    private String  dlgTitle;
    private boolean modal;
    private static boolean autoClose = true;
    private WindowAdapter windowListener;

    // Use frame instead of dialog if non-modal, to get window controls (minimize, maximize)
    private Dialog dialog;
    private JFrame frame;
    
    /** Creates new form ProgressObject */
    public ProgressUI() {
	this (true);
    }

    public ProgressUI(boolean modal) {
        initComponents ();
	changeFontSize = true;
	wasCancelled = false;        
	this.modal = modal;
    }

    public ProgressUI(Component parent) {
	this (true);
    }

    public ProgressUI(Component parent, boolean modal) {
	this (modal);
    }

    public void addNotify () {
	super.addNotify ();
	if (changeFontSize) {
	    Font f = taskTitle.getFont ();
	    taskTitle.setFont (new Font (f.getName (), Font.BOLD,
					 f.getSize () + 2));
	    changeFontSize = false;
	    validate ();
	}
	msgText.setText (" ");						//NOI18N
	errorText.setText (" ");					//NOI18N
        autoCloseCheck.setSelected(autoClose);
        autoCloseCheck.setMnemonic(NbBundle.getMessage(ProgressUI.class, "LBL_Close_When_Finished_Mnemonic").charAt(0));
    }

    /**
     *  Use this to set an explicit title on the progress window before calling startTask()
     *
     *  @param s A title for the window.  If you do not specify an explicit title, the default
     *           title "Progress Monitor" will be used.
     *
     */
    public void setTitle (String s) {
	dlgTitle = s;
        if (frame == null) return;
        
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                    frame.setTitle(dlgTitle);
            }
        });
    }
    

    /**
     * must call this next, after instantiation
     */
    public void startTask (final String msg, final int max) {
	if (wasCancelled) {
	    return;
	}
        if (dlgTitle == null) {
            dlgTitle = NbBundle.getMessage (ProgressUI.class, "LBL_Progress");
        }
        start(msg, max);
    }
    
    private void start(final String msg, final int max) {
        final String title = NbBundle.getMessage (ProgressUI.class, "LBL_Progress");
        if (this.modal) {
            if (dialog != null) {
                clearUI(msg, max);
                return;
            }

            ActionListener listener = new ActionListener () {
                public void actionPerformed (ActionEvent evt) {
                    Object o = evt.getSource ();
                    if (o == NotifyDescriptor.CANCEL_OPTION) {
                        wasCancelled = true;
                    }
                }
            };
            

            
            final DialogDescriptor dd = 
                            new DialogDescriptor (this, dlgTitle , modal, listener) {
                public int getOptionsAlign () {
                    return -1;
                }
            };
            Object [] options = new Object [] { NotifyDescriptor.CLOSED_OPTION };
            dd.setOptions (options);
            dd.setClosingOptions (options);
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    myMonitor.setMaximum (max);
                    myMonitor.setValue (0);
                    taskTitle.setText (msg);
                    dialog = TopManager.getDefault ().createDialog (dd);
                    dialog.setVisible (true);
                }
            });
        }
        else {  // Non-modal -- use JFrame   
            if (frame != null) {
                clearUI(msg, max);
                return;
            }      
            final JPanel thisPanel = this;
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    myMonitor.setMaximum (max);
                    myMonitor.setValue (0);
                    taskTitle.setText (msg);
                    frame = new JFrame(dlgTitle );
                    frame.getContentPane().add(thisPanel);
                    frame.setBounds(Utilities.findCenterBounds(frame.getSize()));
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
                    frame.pack();
                    frame.setVisible(true);
                }
            });
            
           
        }
    }
    
    private void clearUI(final String msg, final int max) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                myMonitor.setMaximum (max);
                myMonitor.setValue (0);
                errorText.setText (" ");				//NOI18N
                msgText.setText (" ");				//NOI18N
                taskTitle.setText (msg);
            }
        });
    }
 
    public void addError (final String msg) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
	  	errorText.setText (msg);
	    }
	});
    }	

    public void addMessage (final String msg) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
	  	msgText.setText (msg);
	    }
	});
    }	

    public void recordWork (final int value) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
	  	myMonitor.setValue (value);
//                System.out.println("ProgressUI: recordWork() value = " + myMonitor.getValue()+ " , max = " + myMonitor.getMaximum());
                if ((myMonitor.getValue() >= myMonitor.getMaximum()) && autoClose) {
                    finished();
                }
            }
	});
    }	
  
    public boolean checkCancelled () {
	return wasCancelled;
    }
 
    public boolean isCompleted() {
        return true;
    }
    
    public void finished () {
        if (!autoClose) {
            return;
        }
        if (this.modal) {
            if (dialog == null) {
                return;
            }
        }
        else {
            if (frame == null) {
                return;
            }
        } 
        disposeUI();
    }
    
    private void disposeUI() {            
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    if (modal) {
                        dialog.hide();
                        dialog.dispose ();
                        dialog = null;
                    }
                    else {
                        frame.hide();
                        frame.dispose ();
                        frame = null;
                    }
                }
            });
    }

    //BEGIN_NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        taskTitle = new javax.swing.JLabel();
        msgText = new javax.swing.JLabel();
        myMonitor = new javax.swing.JProgressBar();
        errorText = new javax.swing.JLabel();
        autoCloseCheck = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        taskTitle.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(taskTitle, gridBagConstraints);

        msgText.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 24, 6, 24);
        add(msgText, gridBagConstraints);

        myMonitor.setMinimumSize(new java.awt.Dimension(400, 30));
        myMonitor.setPreferredSize(new java.awt.Dimension(400, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(myMonitor, gridBagConstraints);

        errorText.setForeground(java.awt.Color.red);
        errorText.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(errorText, gridBagConstraints);

        autoCloseCheck.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/deployment/impl/ui/Bundle").getString("LBL_Close_When_Finished"));
        autoCloseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoCloseCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(autoCloseCheck, gridBagConstraints);

    }//GEN-END:initComponents

    private void autoCloseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoCloseCheckActionPerformed
        // Add your handling code here:
        autoClose = autoCloseCheck.isSelected();
    }//GEN-LAST:event_autoCloseCheckActionPerformed
    //END_NOI18N


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoCloseCheck;
    private javax.swing.JLabel taskTitle;
    private javax.swing.JProgressBar myMonitor;
    private javax.swing.JLabel errorText;
    private javax.swing.JLabel msgText;
    // End of variables declaration//GEN-END:variables

    
}
