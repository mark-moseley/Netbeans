/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2.ui;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.netbeans.core.output2.Controller;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import org.netbeans.core.output2.OutputDocument;

/**
 * A basic output pane.  This class implements the non-output window specific
 * gui management for the output window - creating the text component,
 * locking the caret and scrollbar to the bottom of the document to the 
 * bottom, etc.  Could be merged with OutputView, but it's more readable
 * and maintainable to keep the pure gui code separate.  Mainly contains 
 * logic for layout and showing and hiding a toolbar and input area.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputTab extends JComponent implements Accessible {
    private boolean inputVisible = false;
    private AbstractOutputPane outputPane;
    private Action[] actions = new Action[0];  
    private JButton[] buttons = new JButton[0];
    
    private Component toFocus;
    
    public AbstractOutputTab() {
        outputPane = createOutputPane();
        add (outputPane);
        setFocusable(false);
    }
    
    public void setDocument (Document doc) {
        outputPane.setDocument(doc);
        //#114290
        if (doc instanceof OutputDocument) {
            ((OutputDocument)doc).setPane(outputPane);
        }
    }
    
    /* Read accessible context
     * @return - accessible context
     */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                        @Override
                        public AccessibleRole getAccessibleRole() {
                            // is it really a panel?
                            return AccessibleRole.PANEL;
                        }

                        @Override
                        public String getAccessibleName() {
                            if (accessibleName != null) {
                                return accessibleName;
                            }
                            return getName();
                        }
                    };
        }

        return accessibleContext;
    }
    

    /**
     * on mouse click the specialized component is marked, and activation is requested.
     * activation results in request focus on the tab -> the marked component gets focus.
     */
    public void setToFocus(Component foc) {
        toFocus = foc;
    }
    
    @Override
    public void requestFocus() {
    // on mouse click the specialized component is marked, and activation is requested.
    // activation results in request focus on the tab -> the marked component gets focus.
        if (toFocus != null) {
            toFocus.requestFocus();
            toFocus = null;
            return;
        }
        outputPane.requestFocus();
    }
    
    @Override
    public boolean requestFocusInWindow() {
        return getOutputPane().requestFocusInWindow();
    }    

    protected abstract AbstractOutputPane createOutputPane();
    
    protected abstract void inputSent (String txt);
    
    public final AbstractOutputPane getOutputPane() {
        return outputPane;
    }

    public final void setToolbarActions (Action[] a) {
        if (a == null || a.length == 0) {
            actions = new Action[0];
            buttons = new JButton[0];
            return;
        }
        if (a.length > 5) {
            throw new IllegalArgumentException ("No more than 5 actions allowed" //NOI18N
                + "in the output window toolbar"); //NOI18N
        }
        actions = new Action[a.length];
        buttons = new JButton[a.length];
        for (int i=0; i < buttons.length; i++) {
            actions[i] = a[i];
            // mkleint - ignore the WeakAction referencing as it introduces
            // additional non obvious contract to using the the toolbar actions.
//            actions[i] = new WeakAction(a[i]);
            installKeyboardAction (actions[i]);
            buttons[i] = new JButton(actions[i]);
            buttons[i].setBorderPainted(false);
            buttons[i].setOpaque(false);
            buttons[i].setText(null);
            buttons[i].putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            if (a[i].getValue (Action.SMALL_ICON) == null) {
                throw new IllegalStateException ("No icon provided for " + a[i]); //NOI18N
            }
        }
    }


    /**
     * Get the toolbar actions, if any, which have been supplied by the client.
     * Used to add them to the popup menu if they return a non-null name.
     *
     * @return An array of actions
     */
    public Action[] getToolbarActions() {
        return actions;
    }

    /**
     * Install a keyboard action.  This is used in two places - all toolbar actions with
     * accelerator keys and names will also be installed as keyboard actions.  Also, the
     * master controller installs its actions which should be accessible via the keyboard.
     * The actions are actually installed into the text control.
     *
     * @param a An action to install, if its name and accelerator are non-null
     */
    public void installKeyboardAction (Action a) {
        if (!(a instanceof WeakAction)) {
            //It is a Controller.ControllerAction - don't create a memory leak by listening to it
            a = new WeakAction(a);
        }
        KeyStroke accel = null;
        String name;
        Object o = a.getValue (Action.ACCELERATOR_KEY);
        if (o instanceof KeyStroke) {
            accel = (KeyStroke) o;
        }
        name = (String) a.getValue(Action.NAME);
        if (accel != null) {
            if (Controller.LOG) Controller.log ("Installed action " + name + " on " + accel);
            // if the logic here changes, check the popup escaping hack in Controller
            // it temporarily removes the VK_ESCAPE from input maps..
            JComponent c = getOutputPane().textView;
            c.getInputMap().put(accel, name);
            c.getActionMap().put(name, a);
            getInputMap (WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put (accel, name);
            getActionMap().put(name, a);
        }
    }

    public final boolean isInputVisible() {
        return inputVisible;
    }
    
    public final void setInputVisible (boolean val) {
        if (val == isInputVisible()) {
            return;
        }
        inputVisible = val;
        this.outputPane.textView.setEditable(val);
        validate();
        getOutputPane().ensureCaretPosition();
        getOutputPane().requestFocusInWindow();
    }

    protected abstract void inputEof();

    @Override
    public void doLayout() {
        Insets ins = getInsets();
        int left = ins.left;
        int bottom = getHeight() - ins.bottom;
        
        Component main = outputPane;
        
        if (main != null) {
            main.setBounds (left, ins.top, getWidth() - (left + ins.right), 
                bottom - ins.top);
        }
    }

    public abstract void hasSelectionChanged(boolean val);
    
    void notifyInputFocusGained(){
        getOutputPane().lockScroll();
        getOutputPane().ensureCaretPosition();
    }

    JButton[] getToolbarButtons() {
        return buttons;
    }

}
