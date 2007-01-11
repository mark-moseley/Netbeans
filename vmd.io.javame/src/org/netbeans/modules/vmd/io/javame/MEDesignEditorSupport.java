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

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.mobility.editor.pub.J2MEDataObject.J2MEEditorSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditor;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import java.io.*;

/**
 * @author David Kaspar
 */
// TODO - save cookie is not added/removed to/from MEDesignEditorSupport based on the saveDocument, notifyModified, notifyUnmodified, notifyClosed
public final class MEDesignEditorSupport extends J2MEEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie {

    private MEDesignDataObject dataObject;
    private CloseOperationHandler closeHandler;
    private TopComponent mvtc;

    private MultiViewDescription[] descriptions;
    private boolean useEditPriority;

    private GuardsEditor guardsEditor;
    private GuardedSectionsProvider sections;

    public MEDesignEditorSupport(MEDesignDataObject dataObject) {
        super(dataObject);
        this.dataObject = dataObject;
        closeHandler = new CloseHandler (dataObject);
    }

    @Override
    public void saveDocument() throws IOException {
        DocumentSerializer documentSerializer = IOSupport.getDocumentSerializer (dataObject);
        documentSerializer.waitDocumentLoaded();
        IOSupport.forceUpdateCode (dataObject);
        documentSerializer.saveDocument();
        super.saveDocument();
    }

    @Override
    public boolean notifyModified() {
        if (!super.notifyModified()) {
            return false;
        }
        updateDisplayName();
        return true;
    }

    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        updateDisplayName();
    }

    @Override
    protected void notifyClosed() {
        mvtc = null;
        super.notifyClosed();
        IOSupport.notifyDataObjectClosed (dataObject);
    }

    public void open() {
        useEditPriority = false;
        super.open();

        TopComponent mvtc = this.mvtc;
        if (mvtc != null) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler (mvtc);
            int index = getIndex ();
            if (index >MIDP: ListCD - removed unused parameters= 0) {
                MultiViewPerspective perspective = handler.getPerspectives ()[index];
                handler.requestActive (perspective);
                handler.requestVisible (perspective);
            }
        }
    }

    public void edit() {
        useEditPriority = true;
        super.open();

        TopComponent mvtc = this.mvtc;
        if (mvtc != null) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler (mvtc);
            int index = getIndex ();
            if (index >= 0) {
                MultiViewPerspective perspective = handler.getPerspectives ()[index];
                handler.requestActive (perspective);
                handler.requestVisible (perspective);
            }
        }
    }

    private int getIndex () {
        MultiViewDescription[] descriptions = this.descriptions;
        if (descriptions == null)
            return -1;
        int bestPriority = Integer.MIN_VALUE;
        int bestIndex = -1;
        int index = 0;
        for (MultiViewDescription description : this.descriptions) {
            DataEditorView dataEditorView = IOSupport.getDataEditorView (description);
            int priority = useEditPriority ? dataEditorView.getEditPriority () : dataEditorView.getOpenPriority ();
            if (priority > bestPriority) {
                bestPriority = priority;
                bestIndex = index;
            }
            index ++;
        }
        return bestIndex;
    }

    public void initializeCloneableEditor (CloneableEditor editor) {
        super.initializeCloneableEditor (editor);
    }

    @Override
    protected Pane createPane() {
        descriptions = IOSupport.createEditorSupportPane (IOSupport.getDataObjectContext (dataObject));
        int index = getIndex ();
        return (CloneableEditorSupport.Pane) MultiViewFactory.createCloneableMultiView (descriptions, index >= 0 ? descriptions[index] : null, closeHandler);
    }

    public void setMVTC(TopComponent mvtc) {
        this.mvtc = mvtc;
        updateDisplayName ();
    }

    protected CloneableTopComponent createCloneableTopComponent() {
        CloneableTopComponent tc = super.createCloneableTopComponent();
        this.mvtc = tc;
        return tc;
    }

    public void updateDisplayName() {
        final TopComponent tc = mvtc;
        if (tc == null)
            return;
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                String displayName = messageName();
                if (! displayName.equals(tc.getDisplayName()))
                    tc.setDisplayName(displayName);
                tc.setToolTipText(dataObject.getPrimaryFile().getPath());
            }
        });
    }

    @Override
    protected void loadFromStreamToKitHook (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (sections == null) {
            guardsEditor = new GuardsEditor (doc);
            String mimeType = ((CloneableEditorSupport.Env) this.env).getMimeType ();
            GuardedSectionsFactory factory = GuardedSectionsFactory.find (mimeType);
            sections = factory.create (guardsEditor);
        } else {
            guardsEditor.setDocument (doc);
        }

        if (sections != null) {
            Reader reader = sections.createGuardedReader (stream, getEncoding ());
            try {
                kit.read (reader, doc, 0);
            } finally {
                reader.close ();
            }
        } else {
            super.loadFromStreamToKitHook (doc, stream, kit);
        }
    }

    @Override
    protected void saveFromKitToStreamHook (StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (sections != null) {
            Writer w = sections.createGuardedWriter (stream, getEncoding ());
            try {
                kit.write (w, doc, 0, doc.getLength ());
            } finally {
                w.close ();
            }
        } else {
            super.saveFromKitToStream (doc, kit, stream);
        }
    }

    private static class CloseHandler implements CloseOperationHandler, Serializable {

        private static final long serialVersionUID = -1;
        private MEDesignDataObject dataObject;

        public CloseHandler (MEDesignDataObject dataObject) {
            this.dataObject = dataObject;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            MEDesignEditorSupport editorSupport = dataObject.getEditorSupport ();
            boolean can = editorSupport.canClose();
            if (can)
                editorSupport.notifyClosed();
            return can;
        }

    }

    private class GuardsEditor implements GuardedEditorSupport {

        private StyledDocument document;

        public GuardsEditor(StyledDocument document) {
            this.document = document;
        }

        public StyledDocument getDocument() {
            return document;
        }

        public void setDocument(StyledDocument document) {
            this.document = document;
        }

    }

}
