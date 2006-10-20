/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl.html or http://www.netbeans.org/cddl.txt. When
 * distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable,
 * add the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]" The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.core;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;

import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.bpel.core.multiview.BPELSourceMultiViewElementDesc;
import org.netbeans.modules.bpel.core.multiview.BpelMultiViewSupport;
import org.netbeans.modules.bpel.core.multiview.spi.MultiviewId;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.core.validation.SelectBpelElement;
import org.netbeans.modules.bpel.editors.canvas.Canvas;
import org.netbeans.modules.bpel.editors.canvas.CanvasManagerAccess;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.spi.PrintProviderCookie;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.validation.ValidationOutputWindowController;
import org.netbeans.modules.xml.validation.ui.ValidationAnnotation;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author ads
 */
public class BPELDataEditorSupport extends DataEditorSupport implements
        OpenCookie, EditCookie, EditorCookie.Observable, PrintProviderCookie,
        ShowCookie, ValidateXMLCookie {
    
    public BPELDataEditorSupport( BPELDataObject obj ) {
        super(obj, new BPELEnv(obj));
        setMIMEType(BPELDataLoader.MIME_TYPE);
    }
    
    /**
     * PrintProviderCookie interface {@inheritDoc}
     */
    public PrintProvider getPrintProvider() {
        final Canvas canvas = CanvasManagerAccess.getManager().getCanvas(
                getDataObject());
        
        if (canvas == null) {
            return null;
        }
        final BpelModel model = getBpelModel();
        
        return new PrintProvider.Component() {
            
            public JComponent getComponent() {
                return canvas.getComponent();
            }
            
            public Date getLastModifiedDate() {
                return getDataObject().getPrimaryFile().lastModified();
            }
            
            public String getName() {
                return model.getProcess().getName();
            }
        };
    }

    public QuietUndoManager getUndoManager() {
        return (QuietUndoManager) getUndoRedo();
    }
    
    /**
     * @return Bpel Model for this editor.
     */
    public BpelModel getBpelModel() {
        BPELDataObject dataObject = getEnv().getBpelDataObject();
        ModelSource modelSource = Utilities.getModelSource(dataObject
                .getPrimaryFile(), true);
        return getModelFactory().getModel(modelSource);
    }
    
    /** {@inheritDoc} */
    public void saveDocument() throws IOException {
        super.saveDocument();
        syncModel();
        getDataObject().setModified(false);
    }
    
    /**
     * Sync Bpel model with source.
     */
    public void syncModel() {
        try {
            BpelModel model = getBpelModel();
            if ( model != null ){
                model.sync();
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            // assert false;
        }
    }
    
    /**
     * Public accessor for the <code>initializeCloneableEditor()</code>
     * method.
     *
     * @param editor
     *            cloneable editor.
     */
    @Override
    public void initializeCloneableEditor( CloneableEditor editor ) {
        super.initializeCloneableEditor(editor);
        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        if (!getEnv().getBpelDataObject().isModified()) {
            // Update later to avoid an infinite loop.
            EventQueue.invokeLater(new Runnable() {
                
                public void run() {
                    updateTitles();
                }
            });
        }
    }
    
    @Override
    public Task prepareDocument() {
        Task task = super.prepareDocument();
        // Avoid listening to the same task more than once.
        if ( task == prepareTask ){
            return task;
        }
        prepareTask = task;
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                // The superclass prepareDocument() adds the undo/redo
                // manager as a listener -- we need to remove it since
                // the views will add and remove it as needed.
                QuietUndoManager qum = (QuietUndoManager) getUndoRedo();
                StyledDocument doc = getDocument();
                synchronized ( qum ) {
                    if (!qum.isCompound()) {
                        doc.removeUndoableEditListener(qum);
                    }
                    // Now that the document is ready, pass it to the manager.
                    qum.setDocument((AbstractDocument) doc);
                }
            }
        });
        return task;
    }
    
    @Override
    public Task reloadDocument() {
        Task task = super.reloadDocument();
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        QuietUndoManager undo = getUndoManager();
                        StyledDocument doc = getDocument();
                        // The superclass reloadDocument() adds the undo
                        // manager as an undoable edit listener.
                        synchronized (undo) {
                            if (!undo.isCompound()) {
                                doc.removeUndoableEditListener(undo);
                            }
                        }
                    }
                });
            }
        });
        return task;
    }
    
    /**
     * Adds the undo/redo manager to the bpel model as an undoable
     * edit listener, so it receives the edits onto the queue.
     * 
     * I suppose that those methods are called always in one thread.
     * Currently this is true. They are always called from AWT thread.
     * 
     * @param id Id of mutiview element for which we add undo/redo manager.
     */
    public void addUndoManager( MultiviewId id ) 
    {
        BpelModel model = getBpelModel();
        if (model != null) {
            QuietUndoManager undo = getUndoManager();
            // Ensure the listener is not added twice.
            removeUndoManager();
            model.addUndoableEditListener(undo);
            // Ensure the model is sync'd when undo/redo is invoked,
            // otherwise the edits are added to the queue and eventually
            // cause exceptions.
            undo.setModel(model);
            
            // keep id of multiview 
            setId(id);
        }
    }

    /**
     * Removes the undo/redo manager undoable edit listener from the
     * bpel model, to stop receiving undoable edits.
     * 
     * I suppose that those methods are called always in one thread.
     * Currently this is true. They are always called from AWT thread.
     * 
     * @param id Id of mutiview element for which we remove undo/redo manager.
     */
    public void removeUndoManager( MultiviewId id )
    {
        /*
         *  we prevent deleting undo/redo manager if it was added by multiview
         *  with different id. 
         */ 
        if ( id!= getId()) {
            return;
        }
        
        BpelModel model = getBpelModel();
        if (model != null) {
            QuietUndoManager undo = getUndoManager();
            model.removeUndoableEditListener(undo);
            // Must unset the model when leaving model view.
            undo.setModel(null);
            
            // remove id
            setId( null );
        }
    }
    
    /**
     * This method allows the close behavior of CloneableEditorSupport to be 
     * invoked from the SourceMultiViewElement. The close method of 
     * CloneableEditorSupport at least clears the undo queue and releases
     * the swing document. 
     */ 
    public boolean silentClose() {
        return super.close(false);
    }
    
    /**
     * Implement ShowCookie.
     */
    public void show(final ResultItem resultItem) {
        
        if (!(resultItem.getModel() instanceof BpelModel))
            return;
        
        final BpelEntity bpelEntity = (BpelEntity) resultItem.getComponents();
        
        // Get the edit and line cookies.
        DataObject d = getDataObject();
        final LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
        final EditCookie ec = (EditCookie) d.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Opens the editor or brings it into focus
                // and makes it the activated topcomponent.
                ec.edit();
                
                TopComponent tc = WindowManager.getDefault().getRegistry()
                .getActivated();
                MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
                
                if (mvh == null) {
                    return;
                }
                
                // If model is broken
                // OR if the resultItem.getComponents() is null which
                // means the resultItem was generated when the model was broken.
                // In the above cases switch to the source multiview.
                if(resultItem.getModel().getState().equals(State.NOT_WELL_FORMED) ||
                        resultItem.getComponents() == null) {
                    for(int index1=0 ; index1<mvh.getPerspectives().length ; index1++) {
                        if(mvh.getPerspectives()[index1].preferredID().equals(
                                BPELSourceMultiViewElementDesc.PREFERED_ID))
                            mvh.requestActive(mvh.getPerspectives()[index1]);
                    }
                }
                
                
                // Set annotation or select element in the multiview.
                MultiViewPerspective mvp = mvh.getSelectedPerspective();
                if (mvp.preferredID().equals("orch-designer")) {
                    List<TopComponent> list = getAssociatedTopComponents();
                    for (TopComponent topComponent : list) {
                        // Make sure this is a multiview window, and not just
                        // some
                        // window that has our DataObject (e.g. Projects,Files).
                        MultiViewHandler handler = MultiViews
                                .findMultiViewHandler(topComponent);
                        if (handler != null && topComponent != null) {
                            SelectBpelElement selectElement =
                                    (SelectBpelElement) topComponent.getLookup()
                                    .lookup(SelectBpelElement.class);
                            if (selectElement == null)
                                return;
                            selectElement.select(bpelEntity);
                        }
                    }
                } else if (mvp.preferredID().equals(
                        BPELSourceMultiViewElementDesc.PREFERED_ID)) {
                        
                    // Get the line number.
                    int lineNum;
                    if(resultItem.getComponents() != null) {
                        lineNum = getLineNumber((BpelEntity)resultItem.getComponents());
                    } else {
                        lineNum = resultItem.getLineNumber() - 1;
                    }
                    if (lineNum < 1) {
                        return;
                    }
                    Line l = lc.getLineSet().getCurrent(lineNum);
                    l.show(Line.SHOW_GOTO);
                    annotation.show(l, resultItem.getDescription());
                    
                }
            }
        });
        
        
    }
    
    // Implement Validate XML action.
    public boolean validateXML( CookieObserver cookieObserver ) {
        List<ResultItem> validationResults;
        
        ValidationOutputWindowController validationController =
                new ValidationOutputWindowController();
        validationResults = validationController.validate((Model) ((BPELDataObject) this
                .getDataObject()).getLookup().lookup(Model.class));
        
        // Send the complete/slow validation results to the validation controller
        // so that clients can be notified.
        BPELValidationController controller = (BPELValidationController)((BPELDataObject)
        this.getDataObject()).getLookup().lookup(BPELValidationController.class);
        if(controller != null)
            controller.notifyCompleteValidationResults(validationResults);
        
        
        return true;
    }
    
    protected CloneableEditorSupport.Pane createPane() {
        TopComponent multiview = BpelMultiViewSupport
                .createMultiView((BPELDataObject) getDataObject());
        
        Mode editorMode = WindowManager.getDefault().findMode(EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(multiview);
        }
        
        return (Pane) multiview;
    }
    
    @Override
    protected void notifyClosed() {
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
                undo.setDocument(null);
            }

            BpelModel model = getBpelModel();
            if (model != null) {
                model.removeUndoableEditListener(undo);
            }
            // Must unset the model when no longer listening to it.
            undo.setModel(null);

        }
        super.notifyClosed();
        getUndoManager().discardAllEdits();
        
        // all editors are closed so we don't need to keep this task.
        prepareTask = null;
    }
    
    /*
     * This method is redefined for marking big TopCompenent as modified (
     * asterik (*) needs to be appended to name of bpel file ). Without this
     * overriding file will be marked as modified only when source multiview is
     * edited. Modification in design view will not lead to marking TopComponent
     * as modified. see bug description for #6421669. (non-Javadoc)
     *
     * @see org.openide.text.CloneableEditorSupport#updateTitles()
     */
    @Override
    protected void updateTitles() {
        // This method is invoked by DataEditorSupport.DataNodeListener
        // whenever the DataNode displayName property is changed. It is
        // also called when the CloneableEditorSupport is (un)modified.
        
        // Let the superclass handle the CloneableEditor instances.
        super.updateTitles();
        
        // We need to get the title updated on the MultiViewTopComponent.
        EventQueue.invokeLater(new Runnable() {
            
            public void run() {
                List<TopComponent> list = getAssociatedTopComponents();
                for (TopComponent topComponent : list) {
                    // Make sure this is a multiview window, and not just some
                    // window that has our DataObject (e.g. Projects, Files).
                    MultiViewHandler handler = MultiViews
                            .findMultiViewHandler(topComponent);
                    if (handler != null && topComponent != null) {
                        topComponent.setHtmlDisplayName(messageHtmlName());
                        String name = messageName();
                        topComponent.setDisplayName(name);
                        topComponent.setName(name);
                        topComponent.setToolTipText(messageToolTip());
                    }
                }
            }
        });
    }
    
    protected BPELEnv getEnv() {
        return (BPELEnv) env;
    }
    
    @Override
    protected UndoRedo.Manager createUndoRedoManager() {
        // Override so the superclass will use our proxy undo manager
        // instead of the default, then we can intercept edits.
        return new QuietUndoManager(super.createUndoRedoManager());
        // Note we cannot set the document on the undo manager right
        // now, as CES is probably trying to open the document.
    }
    
    ValidationAnnotation annotation = new ValidationAnnotation();
    
    private void setId( MultiviewId id) {
        myMultiviewId = id;
    }
    
    private MultiviewId getId() {
        return myMultiviewId;
    }
    
    /**
     * Removes the undo/redo manager undoable edit listener from the
     * bpel model, to stop receiving undoable edits.
     */
    private void removeUndoManager( ) {
        BpelModel model = getBpelModel();
        if (model != null) {
            QuietUndoManager undo = getUndoManager();
            model.removeUndoableEditListener(undo);
            // Must unset the model when leaving model view.
            undo.setModel(null);
        }
    }
    
    private List<TopComponent> getAssociatedTopComponents() {
        // Create a list of TopComponents associated with the
        // editor's schema data object, starting with the the
        // active TopComponent. Add all open TopComponents in
        // any mode that are associated with the DataObject.
        // [Note that EDITOR_MODE does not contain editors in
        // split mode.]
        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        DataObject targetDO = getDataObject();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC != null
                && targetDO == (DataObject) activeTC.getLookup().lookup(
                DataObject.class)) {
            associatedTCs.add(activeTC);
        }
        Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            TopComponent tcc = (TopComponent) tc;
            if (targetDO == (DataObject) tcc.getLookup().lookup(
                    DataObject.class)) {
                associatedTCs.add(tcc);
            }
        }
        return associatedTCs;
    }
    
    private int getLineNumber( BpelEntity entity ) {
        int position = entity.findPosition();
        ModelSource modelSource = entity.getBpelModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument) lookup
                .lookup(StyledDocument.class);
        if (document == null) {
            return -1;
        }
        return NbDocument.findLineNumber(document, position);
    }
    
    private static class BPELEnv extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = 835762240381036211L;
        
        public BPELEnv( BPELDataObject obj ) {
            super(obj);
        }
        
        public BPELDataObject getBpelDataObject() {
            return (BPELDataObject) getDataObject();
        }
        
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry()
            .takeLock();
        }
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // /////////////////////// CloseOperationHandler ///////////////////////////
    // /////////////////////////////////////////////////////////////////////////
    
    public static class CloseHandler implements CloseOperationHandler,
            Serializable {
        
        private static final long serialVersionUID = -4621077799099893176L;
        
        private CloseHandler() {
            // CTOR for deser
        }
        
        public CloseHandler( BPELDataObject obj ) {
            myDataObject = obj;
        }
        
        public boolean resolveCloseOperation( CloseOperationState[] elements ) {
            BPELDataEditorSupport support = myDataObject == null ? null
                    : (BPELDataEditorSupport) myDataObject
                    .getCookie(BPELDataEditorSupport.class);
            if (support == null) {
                return true;
            }
            boolean close = support.canClose();
            if (close) {
                if (myDataObject.isValid()) {
//                  In odrer to clear the undo queue of orphaned edits, let's always
//                  reload the document, which discards the edits on the undo queue.
//                  The critical part is that BeforeSaveEdit gets added to the queue.
//                  // In case user discarded edits, need to reload.
//                  if (dataObject.isModified()) {
                        support.reloadDocument().waitFinished();
//                    }
                }
                
                myDataObject.setModified(false); // Issue 85629
            }
            return close;
        }
        
        private BPELDataObject myDataObject;
    }
    
    private BpelModelFactory getModelFactory() {
        BpelModelFactory factory = (BpelModelFactory) Lookup.getDefault()
        .lookup(BpelModelFactory.class);
        return factory;
    }
    
    /** Used for managing the prepareTask listener. */
    private transient Task prepareTask;
    
    private transient MultiviewId myMultiviewId;
    
}
