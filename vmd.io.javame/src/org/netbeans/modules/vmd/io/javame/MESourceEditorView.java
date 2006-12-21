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
package org.netbeans.modules.vmd.io.javame;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.awt.UndoRedo;
import org.openide.text.NbDocument;
import org.openide.text.CloneableEditor;
import org.openide.util.HelpCtx;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.IOException;

/**
 * @author David Kaspar
 */
public final class MESourceEditorView implements DataEditorView {

    private static final long serialVersionUID = -1;

    private static final String VIEW_ID = "source"; // NOI18N

    private DataObjectContext context;
    private transient CloneableEditor editor;
    private transient JComponent toolbar;

    MESourceEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        editor = new CloneableEditor (context.getCloneableEditorSupport());
    }

    public DataObjectContext getContext () {
        return context;
    }

    public Kind getKind () {
        return Kind.CODE;
    }

    public String preferredID () {
        return VIEW_ID;
    }

    public String getDisplayName () {
        return "Source";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (MESourceEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return editor;
    }

    public JComponent getToolbarRepresentation () {
        if (toolbar == null) {
            JEditorPane pane = editor.getEditorPane ();
            if (pane != null) {
                Document doc = pane.getDocument ();
                if (doc instanceof NbDocument.CustomToolbar)
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar (pane);
            }
            if (toolbar == null)
                toolbar = new JPanel ();
        }
        return toolbar;
    }

    public UndoRedo getUndoRedo () {
        return editor.getUndoRedo ();
    }

    public void componentOpened () {
    }

    public void componentClosed () {
    }

    public void componentShowing () {
    }

    public void componentHidden () {
    }

    public void componentActivated () {
    }

    public void componentDeactivated () {
    }

    public int getOpenPriority () {
        return getOrder ();
    }

    public int getEditPriority () {
        return - getOrder ();
    }

    public int getOrder () {
        return - 1000;
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (context);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found");
        context = (DataObjectContext) object;
        init ();
    }

}
