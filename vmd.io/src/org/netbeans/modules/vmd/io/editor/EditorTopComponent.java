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
package org.netbeans.modules.vmd.io.editor;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class EditorTopComponent extends TopComponent {

    private JComponent view;

    public EditorTopComponent (DataObjectContext context, Lookup lookup, JComponent view) {
        this.view = view;
        setLayout (new BorderLayout ());
        setFocusable (true);
        add (view, BorderLayout.CENTER);
        Node node = context.getDataObject ().getNodeDelegate ();
        setActivatedNodes (new Node[] { node });
        lookup = new ProxyLookup (lookup, node.getLookup (), Lookups.singleton (getActionMap ()));
        associateLookup (lookup);
    }

    public void requestFocus () {
        super.requestFocus ();
        view.requestFocus ();
    }

    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        return view.requestFocusInWindow ();
    }

}
