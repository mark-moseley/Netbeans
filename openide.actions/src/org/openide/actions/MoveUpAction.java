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
package org.openide.actions;

import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.Arrays;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** Move an item up in a list.
*
* @see Index
* @author Ian Formanek, Jan Jancura, Dafe Simonek
*/
public final class MoveUpAction extends NodeAction {
    /** the key to listener to reorder of selected nodes */
    private static final String PROP_ORDER_LISTENER = "sellistener"; // NOI18N
    private static Logger err = Logger.getLogger("org.openide.actions.MoveUpAction"); // NOI18N

    /** Holds index cookie on which we are listening */
    private Reference curIndexCookie;

    /* Initilizes the set of properties.
    */
    protected void initialize() {
        err.fine("initialize");

        super.initialize();

        // initializes the listener
        OrderingListener sl = new OrderingListener();
        putProperty(PROP_ORDER_LISTENER, sl);
    }

    /** Getter for curIndexCookie */
    private Index getCurIndexCookie() {
        return ((curIndexCookie == null) ? null : (Index) curIndexCookie.get());
    }

    protected void performAction(Node[] activatedNodes) {
        // we need to check activatedNodes, because there's no
        // guarantee that they not changed between enable() and
        // performAction calls
        Index cookie = getIndexCookie(activatedNodes);

        if (cookie == null) {
            return;
        }

        int nodeIndex = cookie.indexOf(activatedNodes[0]);

        if (nodeIndex > 0) {
            cookie.moveUp(nodeIndex);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        err.fine(
            "enable; activatedNodes=" + ((activatedNodes == null) ? null : Arrays.asList(activatedNodes))
        );

        // remove old listener, if any
        Index idx = getCurIndexCookie();

        if (idx != null) {
            idx.removeChangeListener((ChangeListener) getProperty(PROP_ORDER_LISTENER));
        }

        Index cookie = getIndexCookie(activatedNodes);

        if (err != null) {
            err.fine("enable; cookie=" + cookie);
        }

        if (cookie == null) {
            return false;
        }

        // now start listening to reordering changes
        cookie.addChangeListener((OrderingListener) getProperty(PROP_ORDER_LISTENER));
        curIndexCookie = new WeakReference(cookie);

        int index = cookie.indexOf(activatedNodes[0]);

        if (err != null) {
            err.fine("enable; index=" + index);

            if (index == -1) {
                Node parent = activatedNodes[0].getParentNode();
                err.fine(
                    "enable; parent=" + parent + "; parent.children=" + Arrays.asList(parent.getChildren().getNodes())
                );
            }
        }

        return index > 0;
    }

    public String getName() {
        return NbBundle.getMessage(MoveUpAction.class, "MoveUp");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveUpAction.class);
    }

    /** Helper method. Returns index cookie or null, if some
    * conditions weren't satisfied */
    private Index getIndexCookie(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return null;
        }

        Node parent = activatedNodes[0].getParentNode();

        if (parent == null) {
            return null;
        }

        return (Index) parent.getCookie(Index.class);
    }

    /** Listens to the ordering changes and enables/disables the
    * action if appropriate */
    private final class OrderingListener implements ChangeListener {
        OrderingListener() {
        }

        public void stateChanged(ChangeEvent e) {
            Node[] activatedNodes = getActivatedNodes();

            err.fine(
                "stateChanged; activatedNodes=" + ((activatedNodes == null) ? null : Arrays.asList(activatedNodes))
            );

            Index cookie = getIndexCookie(activatedNodes);

            if (err != null) {
                err.fine("stateChanged; cookie=" + cookie);
            }

            if (cookie == null) {
                setEnabled(false);
            } else {
                int index = cookie.indexOf(activatedNodes[0]);

                if (err != null) {
                    err.fine("stateChanged; index=" + index);
                }

                setEnabled(index > 0);
            }
        }
    }
}
