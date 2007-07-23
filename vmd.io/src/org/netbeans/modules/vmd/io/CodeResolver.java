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
package org.netbeans.modules.vmd.io;

import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.modules.vmd.api.io.*;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.HashMap;

/**
 * @author David Kaspar
 */
public class CodeResolver implements DesignDocumentAwareness {

    private static final Lookup.Result<CodeGenerator> result = Lookup.getDefault ().lookupResult (CodeGenerator.class);

    private DataObjectContext context;
    private DocumentSerializer serializer;
    private volatile DesignDocument document;
    private volatile long documentState = Long.MIN_VALUE;
    private volatile DataEditorView.Kind viewKind;

    public CodeResolver (DataObjectContext context, DocumentSerializer serializer) {
        this.context = context;
        this.serializer = serializer;
        this.serializer.addDesignDocumentAwareness (this);
    }

    public void resetModelModifiedStatus (DesignDocument document) {
        if (this.document == null  &&  document != null) {
            this.document = document;
            this.documentState = document.getListenerManager ().getDocumentState ();
        }
    }

    public void setDesignDocument (final DesignDocument designDocument) {
        DataEditorView activeView = ActiveViewSupport.getDefault ().getActiveView ();
        if (activeView == null)
            return;
        if (activeView.getContext () != context)
            return;
        update (designDocument, activeView.getKind ());
    }

    public void viewActivated (DataEditorView view) {
        update (serializer.getDocument (), view.getKind ());
    }

    public void forceUpdateCode () {
        update (serializer.getDocument (), null);
    }

    private void update (final DesignDocument document, final DataEditorView.Kind kind) {
        if (document == null)
            return;
        if (kind != null  &&  kind.equals (DataEditorView.Kind.NONE))
            return;

        synchronized (this) {
            if (kind != null  &&  kind.equals (viewKind))
                return;

            boolean modelModified = CodeResolver.this.document != document || CodeResolver.this.documentState != document.getListenerManager ().getDocumentState ();
            boolean editorSupportModified = IOSupport.getCloneableEditorSupport (context.getDataObject ()).isModified ();
            boolean switchedFromModelToCode = kind != null && kind.equals (DataEditorView.Kind.CODE) && viewKind != null && viewKind.equals (DataEditorView.Kind.MODEL);
            final boolean switchedFromCodeToModel = kind != null && kind.equals (DataEditorView.Kind.MODEL) && viewKind != null && viewKind.equals (DataEditorView.Kind.CODE);
            final boolean regenerateSourceCode = editorSupportModified  &&  modelModified  &&  (switchedFromModelToCode  ||  kind == null);

            if (! IOSupport.isDocumentUpdatingEnabled (context.getDataObject ())) {
                if (regenerateSourceCode)
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            String lckFile = ".LCK" + context.getDataObject ().getPrimaryFile ().getNameExt () + "~"; // NOI18N
                            DialogDisplayer.getDefault ().notifyLater (new NotifyDescriptor.Message (
                                    NbBundle.getMessage (CodeResolver.class, "CodeResolver.locked", lckFile) // NOI18N
                            ));
                        }
                    });
                if (kind != null)
                    CodeResolver.this.viewKind = kind;
                return;
            }

            JEditorPane pane = null;
            FoldHierarchy foldHierarchy = null;
            HashMap<String, Boolean> foldStates = null;

            if (regenerateSourceCode) {
                pane = findEditorPane ();
                if (pane != null) {
                    foldHierarchy = FoldHierarchy.get (pane);
                    foldStates = new HashMap<String, Boolean> ();
                }
            }

            if (pane != null) {
                storeFoldStates (foldHierarchy.getRootFold (), foldStates);
            }

            final long eventID = document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    if (regenerateSourceCode)
                        for (CodeGenerator generator : result.allInstances ())
                            generator.validateModelForCodeGeneration (context, document);

                    if (switchedFromCodeToModel)
                        for (CodeGenerator generator : result.allInstances ())
                            generator.updateModelFromCode (context, document);
                }
            });

            CodeResolver.this.document = document;
            if (regenerateSourceCode)
                CodeResolver.this.documentState = eventID;
            if (kind != null)
                CodeResolver.this.viewKind = kind;

            if (regenerateSourceCode)
                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        for (CodeGenerator generator : result.allInstances ())
                            generator.updateCodeFromModel (context, document);
                    }
                });

            if (pane != null)
                loadFoldStates (foldHierarchy, foldHierarchy.getRootFold (), foldStates);
        }
    }

    private void storeFoldStates (Fold fold, HashMap<String, Boolean> foldStates) {
        String description = fold.getDescription ();
        if ("custom-fold".equals (fold.getType ().toString ())  &&  description != null) // NOI18N
            foldStates.put (description, fold.isCollapsed ());
        for (int a = 0; a < fold.getFoldCount (); a ++)
            storeFoldStates (fold.getFold (a), foldStates);
    }

    private void loadFoldStates (FoldHierarchy foldHierarchy, Fold fold, HashMap<String, Boolean> foldStates) {
        String description = fold.getDescription ();
        if ("custom-fold".equals (fold.getType ().toString ())  &&  description != null) { // NOI18N
            Boolean state = foldStates.get (description);
            if (state != null  &&  state != fold.isCollapsed ()) {
                if (state)
                    foldHierarchy.collapse (fold);
                else
                    foldHierarchy.expand (fold);
            }
        }
        for (int a = 0; a < fold.getFoldCount (); a ++)
            loadFoldStates (foldHierarchy, fold.getFold (a), foldStates);
    }

    private JEditorPane findEditorPane () {
        if (! SwingUtilities.isEventDispatchThread ()) {
//            Debug.warning ("Fold states cannot be restored since the code is invoke outside of AWT-thread"); // NOI18N
            return null;
        }
        JEditorPane[] panes = context.getCloneableEditorSupport ().getOpenedPanes ();
        if (panes == null  ||  panes.length < 1) {
//            Debug.warning ("No editor pane found for", context); // NOI18N
            return null;
        }
//        else if (panes.length > 1)
//            Debug.warning ("Multiple editor panes found for", context, "taking first one"); // NOI18N
        return panes[0];
    }

    public void notifyDataObjectClosed () {
        serializer.removeDesignDocumentAwareness (this);
        serializer = null;
        context = null;
    }


}
