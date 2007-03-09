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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/**
 * An Action implementation useful for key bindings that are
 * shared across a number of different text components.  Because
 * the action is shared, it must have a way of getting it's
 * target to act upon.  This class provides support to try and
 * find a text component to operate on.  The preferred way of
 * getting the component to act upon is through the ActionEvent
 * that is received.  If the Object returned by getSource can
 * be narrowed to a text component, it will be used.  If the
 * action event is null or can't be narrowed, the last focused
 * text component is tried.  This is determined by being
 * used in conjunction with a JTextController which
 * arranges to share that information with a TextAction.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author  Timothy Prinzing
 * @version 1.27 01/23/03
 */
public abstract class TextAction extends AbstractAction {
    /**
     * Creates a new JTextAction object.
     *
     * @param name the name of the action
     */
    public TextAction(String name) {
        super(name);
    }

    /**
     * Determines the component to use for the action.
     * This if fetched from the source of the ActionEvent
     * if it's not null and can be narrowed.  Otherwise,
     * the last focused component is used.
     *
     * @param e the ActionEvent
     * @return the component
     */
    protected final DesignerPaneBase getTextComponent(ActionEvent e) {
        if (e != null) {
            Object o = e.getSource();

            if (o instanceof DesignerPaneBase) {
                return (DesignerPaneBase)o;
            }
        }

        return getFocusedComponent();
    }

    /**
     * Takes one list of
     * commands and augments it with another list
     * of commands.  The second list is considered
     * to be higher priority than the first list
     * and commands with the same name will both lists
     * will only have the dominate command found in the
     * second list in the returned list.
     *
     * @param list1 the first list, may be empty but not null
     * @param list2 the second list, may be empty but not null
     * @return the augmented list
     */
    public static final Action[] augmentList(Action[] list1, Action[] list2) {
        Hashtable<String, Action> h = new Hashtable<String, Action>();

        for (int i = 0; i < list1.length; i++) {
            Action a = list1[i];
            String value = (String)a.getValue(Action.NAME);
            h.put(((value != null) ? value : ""), a);
        }

        for (int i = 0; i < list2.length; i++) {
            Action a = list2[i];
            String value = (String)a.getValue(Action.NAME);
            h.put(((value != null) ? value : ""), a);
        }

        Action[] actions = new Action[h.size()];
        int index = 0;

        for (Enumeration<Action> e = h.elements(); e.hasMoreElements();) {
            actions[index++] = e.nextElement();
        }

        return actions;
    }

    /**
     * Fetches the text component that currently has focus.
     * This allows actions to be shared across text components
     * which is useful for key-bindings where a large set of
     * actions are defined, but generally used the same way
     * across many different components.
     *
     * @return the component
     */
    protected final DesignerPaneBase getFocusedComponent() {
        return DesignerPaneBase.getFocusedComponent();
    }
}
