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


package org.netbeans.core.windows.services;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

import java.awt.*;


// Extracted from core/NbTopManager.
/**
 * Implementation of <code>org.openide.DialogDisplayer</code>.
 *
 * @author  Jesse Glick
 */
public class DialogDisplayerImpl extends DialogDisplayer {
    
    /** Creates a new instance of DialogDisplayerImpl */
    public DialogDisplayerImpl() {
    }

    /** Creates new dialog. */
    public Dialog createDialog (final DialogDescriptor d) {
        return (Dialog)Mutex.EVENT.readAccess (new Mutex.Action () {
            public Object run () {
                // if a modal dialog active use it as parent
                // otherwise use the main window
                if (NbPresenter.currentModalDialog != null) {
                    return new NbDialog(d, NbPresenter.currentModalDialog);
                }
                else {
                    Frame f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                        instanceof Frame ? 
                        (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                        : WindowManager.getDefault().getMainWindow();
                    return new NbDialog(d, f);
                }
            }
        });
    }
    
    /** Notifies user by a dialog.
     * @param descriptor description that contains needed informations
     * @return the option that has been choosen in the notification. */
    public Object notify (final NotifyDescriptor descriptor) {
        return Mutex.EVENT.readAccess (new Mutex.Action () {
                public Object run () {
                    Component focusOwner = null;
                    Component comp = org.openide.windows.TopComponent.getRegistry ().getActivated ();
                    Component win = comp;
                    while ((win != null) && (!(win instanceof Window))) win = win.getParent ();
                    if (win != null) focusOwner = ((Window)win).getFocusOwner ();

                    // if a modal dialog is active use it as parent
                    // otherwise use the main window
                    
                    NbPresenter presenter = null;
                    if (descriptor instanceof DialogDescriptor) {
                        if (NbPresenter.currentModalDialog != null) {
                            presenter = new NbDialog((DialogDescriptor) descriptor, NbPresenter.currentModalDialog);
                        } else {
                            Frame f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                                instanceof Frame ? 
                                (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                                : WindowManager.getDefault().getMainWindow();
                            presenter = new NbDialog((DialogDescriptor) descriptor, f);
                        }
                    } else {
                        if (NbPresenter.currentModalDialog != null) {
                            presenter = new NbPresenter(descriptor, NbPresenter.currentModalDialog, true);
                        } else {
                            Frame f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                                instanceof Frame ? 
                                (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() 
                                : WindowManager.getDefault().getMainWindow();
                            presenter = new NbPresenter(descriptor, f, true);
                        }
                    }

                    //Bugfix #8551
                    presenter.getRootPane().requestDefaultFocus();
                    presenter.setVisible(true);

                    // dialog is gone, restore the focus
                    
                    if (focusOwner != null) {
                        win.requestFocus ();
                        comp.requestFocus ();
                        focusOwner.requestFocus ();
                    }
                    return descriptor.getValue();
                }
            }
        );
    }

}
