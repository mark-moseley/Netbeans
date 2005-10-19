/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.netbeans.spi.options.OptionsCategory.PanelController;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;


public class OptionsWindowAction extends AbstractAction {

    /** Link to dialog, if its opened. */
    private Dialog              dialog;
    /** weak link to options dialog DialogDescriptor. */
    private WeakReference       optionsDialogDescriptor = 
                                    new WeakReference (null);
    private ErrorManager        log = ErrorManager.getDefault ().getInstance
                                    (OptionsWindowAction.class.getName ());
    
    public OptionsWindowAction () {
        putValue (
            Action.NAME, 
            loc ("CTL_Options_Window_Action")
        );
    }

    public void actionPerformed (ActionEvent evt) {     
        if (dialog != null) {
            // dialog already opened
            dialog.setVisible (true);
            dialog.toFront ();
            log.log ("Front Options Dialog"); //NOI18N
            return;
        }
        
        DialogDescriptor descriptor = (DialogDescriptor) 
            optionsDialogDescriptor.get ();
        
        OptionsPanel optionsPanel = null;
        if (descriptor == null) {
            // create new DialogDescriptor for options dialog
            JButton bClassic = (JButton) loc (new JButton (), "CTL_Classic");//NOI18N
            JButton bOK = (JButton) loc (new JButton (), "CTL_OK");//NOI18N

            optionsPanel = new OptionsPanel ();
            descriptor = new DialogDescriptor (
                optionsPanel,
                "Options",
                false,
                Utilities.getOperatingSystem () == Utilities.OS_MAC ?
                    new Object[] {
                        DialogDescriptor.CANCEL_OPTION,
                        bOK
                    } :
                    new Object[] {
                        bOK,
                        DialogDescriptor.CANCEL_OPTION
                    },
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null
            );
            descriptor.setAdditionalOptions (new Object[] {bClassic});
            descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            OptionsPanelListener listener = new OptionsPanelListener 
                (descriptor, optionsPanel, bOK, bClassic);
            descriptor.setButtonListener (listener);
            optionsPanel.addPropertyChangeListener (listener);
            optionsDialogDescriptor = new WeakReference (descriptor);
            log.log ("Create new Options Dialog"); //NOI18N
        } else {
            optionsPanel = (OptionsPanel) descriptor.getMessage ();
            optionsPanel.update ();
            log.log ("Reopen Options Dialog"); //NOI18N
        }
        
        dialog = DialogDisplayer.getDefault ().createDialog (descriptor);
        dialog.setVisible (true);
        dialog.addWindowListener (new MyWindowListener (optionsPanel));
        descriptor = null;
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (OptionsWindowAction.class, key);
    }
    
    private static Component loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
        return c;
    }
    
    private class OptionsPanelListener implements PropertyChangeListener,
    ActionListener {
        private DialogDescriptor    descriptor;
        private OptionsPanel        optionsPanel;
        private JButton             bOK;
        private JButton             bClassic;
        
        
        OptionsPanelListener (
            DialogDescriptor    descriptor, 
            OptionsPanel        optionsPanel,
            JButton             bOK,
            JButton             bClassic
        ) {
            this.descriptor = descriptor;
            this.optionsPanel = optionsPanel;
            this.bOK = bOK;
            this.bClassic = bClassic;
        }
        
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals (
                "buran" + PanelController.PROP_HELP_CTX)               //NOI18N
            )
                descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            else
            if (ev.getPropertyName ().equals (
                "buran" + PanelController.PROP_VALID)                  //NOI18N
            )
                bOK.setEnabled (optionsPanel.dataValid ());
        }
        
        public void actionPerformed (ActionEvent e) {
            if (dialog == null) 
                return; //WORKARROUND for some bug in NbPresenter
                // listener is called twice ...
            if (e.getSource () == bOK) {
                log.log ("Options Dialog - Ok pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                d.dispose ();
                RequestProcessor.getDefault ().post (new Runnable () {
                   public void run () {
                        optionsPanel.save ();
                   } 
                });
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION) {
                log.log ("Options Dialog - Cancel pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                d.dispose ();
                RequestProcessor.getDefault ().post (new Runnable () {
                   public void run () {
                        optionsPanel.cancel ();
                   } 
                });
            } else
            if (e.getSource () == bClassic) {
                log.log ("Options Dialog - Classic pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                if (optionsPanel.isChanged ()) {
                    Confirmation descriptor = new Confirmation (
                        loc ("CTL_Some_values_changed"), 
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE
                    );
                    if (DialogDisplayer.getDefault ().notify (descriptor) ==
                        NotifyDescriptor.OK_OPTION
                    ) {
                        d.dispose ();
                        RequestProcessor.getDefault ().post (new Runnable () {
                           public void run () {
                                optionsPanel.save ();
                           } 
                        });
                    } else {
                        d.dispose ();
                        RequestProcessor.getDefault ().post (new Runnable () {
                           public void run () {
                                optionsPanel.cancel ();
                           } 
                        });
                    }
                } else {
                    d.dispose ();
                    RequestProcessor.getDefault ().post (new Runnable () {
                       public void run () {
                            optionsPanel.cancel ();
                       } 
                    });
                }
                try {
                    ClassLoader cl = (ClassLoader) Lookup.getDefault ().
                        lookup (ClassLoader.class);
                    Class clz = cl.loadClass 
                        ("org.netbeans.core.actions.OptionsAction");
                    CallableSystemAction a = (CallableSystemAction) 
                        SystemAction.findObject (clz, true);
                    a.putValue ("additionalActionName", loc ("CTL_Modern"));
                    a.putValue (
                        "additionalActionListener", 
                        new OpenOptionsListener ()
                    );
                    a.performAction ();
                } catch (Exception ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            } // classic
        }
    }
    
    private class MyWindowListener implements WindowListener {
        
        private OptionsPanel optionsPanel;
        
        
        MyWindowListener (OptionsPanel optionsPanel) {
            this.optionsPanel = optionsPanel;
        }
        
        public void windowClosing (WindowEvent e) {
            if (dialog == null) return;
            log.log ("Options Dialog - windowClosed "); //NOI18N
            RequestProcessor.getDefault ().post (new Runnable () {
               public void run () {
                    optionsPanel.cancel ();
               } 
            });
            dialog = null;
        }

        public void windowClosed (WindowEvent e) {}
        public void windowDeactivated (WindowEvent e) {}
        public void windowOpened (WindowEvent e) {}
        public void windowIconified (WindowEvent e) {}
        public void windowDeiconified (WindowEvent e) {}
        public void windowActivated (WindowEvent e) {}
    }
    
    class OpenOptionsListener implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    log.log ("Options Dialog - Back to modern."); //NOI18N
                    OptionsWindowAction.this.actionPerformed 
                        (new ActionEvent (this, 0, "Open"));
                }
            });
        }
    }
}

