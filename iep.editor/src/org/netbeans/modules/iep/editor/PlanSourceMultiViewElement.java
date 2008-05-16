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

package org.netbeans.modules.iep.editor;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jeri Lockhart
 */

public class PlanSourceMultiViewElement extends CloneableEditor implements MultiViewElement {
    
    private static final long serialVersionUID = 4403502726950453345L;
    
    transient private  JComponent toolbar;
    transient private  MultiViewElementCallback multiViewObserver;
    private PlanDataObject mObj;
    
    
    // Do NOT remove. Only for externalization //
    public PlanSourceMultiViewElement() {
        super();
    }
    
    // Creates new editor //
    public PlanSourceMultiViewElement(PlanDataObject obj) {
        super(obj.getPlanEditorSupport());
        this.mObj = obj;


        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in WSDLMultiViewFactory,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the tree view appear.
        // This initialization fixes CR 6380287 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        mObj.getPlanEditorSupport().initializeCloneableEditor(this);
        initialize();
    }

    /**
     * create lookup, caretlistener, timer
     */
    private void initialize()
    {
        
        ShowCookie showCookie = new ShowCookie()
        {
            
            public void show(ResultItem resultItem) {
                if(isActiveTC()) {
                    Component component = resultItem.getComponents();
                    if (component.getModel() == null) return; //may have been deleted.
                    
                    UIUtilities.annotateSourceView(mObj, (DocumentComponent) component, 
                            resultItem.getDescription(), true);
                    if(component instanceof IEPComponent) {
                        int position = ((IEPComponent)component).findPosition();
                        getEditorPane().setCaretPosition(position);
                    } else {
                        int line = resultItem.getLineNumber();
                        try {
                            int position = NbDocument.findLineOffset(
                                    (StyledDocument)getEditorPane().getDocument(),line);
                            getEditorPane().setCaretPosition(position);
                        } catch (IndexOutOfBoundsException iob) {
                            // nothing
                        }
                    }
                }
            }
        };

//      create and associate lookup
        Node delegate = mObj.getNodeDelegate();
        SourceCookieProxyLookup lookup = new SourceCookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so editor actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                mObj,
                // The Show Cookie in lookup to show schema component
                showCookie,
            }),
        },delegate);
        associateLookup(lookup);
        addPropertyChangeListener("activatedNodes", lookup);
        
        caretListener = new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                timerSelNodes.restart();
            }
        };

        timerSelNodes = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isActiveTC() || getEditorPane() == null) {
                    return;
                }
                //selectElementsAtOffset();
            }
        });
        timerSelNodes.setRepeats(false);
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (toolbar == null) {
                toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
            }
            return toolbar;
        }
        return null;
    }
    
    
    @Override
    public UndoRedo getUndoRedo() {
        return mObj.getPlanEditorSupport().getUndoManager();
    }
    
    public JComponent getVisualRepresentation() {
        return this; 
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    @Override
    public void requestVisible() {
        if (multiViewObserver != null)
            multiViewObserver.requestVisible();
        else
            super.requestVisible();
    }
    
    @Override
    public void requestActive() {
        if (multiViewObserver != null)
            multiViewObserver.requestActive();
        else
            super.requestActive();
    }
    
    @Override
    protected String preferredID() {
        
        return "WSDLSourceMultiViewElementTC";  //  NOI18N
    }
    
    
   /* @Override
    public UndoRedo getUndoRedo() {
        return mObj.getWSDLEditorSupport().getUndoManager();
    }*/

    /**
     * The close last method should be called only for the last clone. 
     * If there are still existing clones this method must return false. The
     * implementation from the FormEditor always returns true but this is 
     * not the expected behavior. The intention is to close the editor support
     * once the last editor has been closed, using the silent close to avoid
     * displaying a new dialog which is already being displayed via the 
     * close handler. 
     */ 
    @Override
    protected boolean closeLast() {
    PlanEditorSupport support = mObj.getPlanEditorSupport();
    JEditorPane[] editors = support.getOpenedPanes();
    if (editors == null || editors.length == 0) {
        return support.silentClose();
    }
    return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!PlanEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    @Override
    public void componentActivated() {
        JEditorPane p = getEditorPane();
        if (p != null) {
            p.addCaretListener(caretListener);
        }
        if(timerSelNodes!=null) {
            timerSelNodes.restart();
        }
        super.componentActivated();            
        PlanEditorSupport editor = mObj.getPlanEditorSupport();
        editor.addUndoManagerToDocument();
    }
    @Override
    public void componentDeactivated() {
        // Note: componentDeactivated() is called when the entire
        // MultiViewTopComponent is deactivated, _not_ when switching
        // between the multiview elements.
        JEditorPane p = getEditorPane();
        if (p != null) {
            p.removeCaretListener(caretListener);
        }
        synchronized (this) {
            if (selectionTask != null) {
                selectionTask.cancel();
                selectionTask = null;
            }
        }
        if(timerSelNodes!=null) {
            timerSelNodes.stop();
        }
        super.componentDeactivated();
        PlanEditorSupport editor = mObj.getPlanEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    @Override
    public void componentOpened() {
    super.componentOpened();
    }
    
    
    /*
     * In other non-multiview editors, the text editor is the one that is docked into the editor mode.
     * and super.canClose() returns true and schedules the clean up of editor kit, 
     * which in turn calls uninstallUI in NbEditorUI class.
     * 
     * In our case, we need to explicitly call setEditorKit(null), so that uninstallUI gets called.
     * So our editor gets removed from different caches and propertychangesupports.
     * 
     * (non-Javadoc)
     * @see org.openide.text.CloneableEditor#componentClosed()
     */
    @Override
    public void componentClosed() {
        super.componentClosed();
        multiViewObserver = null;
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        PlanEditorSupport editor = mObj.getPlanEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        PlanEditorSupport editor = mObj.getPlanEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mObj);
    }

    @Override
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof PlanDataObject) {
            mObj = (PlanDataObject) firstObject;
            initialize();
        }
    }

    // node support
    /** Root node of schema model */
    private Node rootNode;
    /** current selection*/
    private Node selectedNode;
    /** listens to selected node destroyed event */
    private NodeAdapter nl;
    /** Timer which countdowns the "update selected element node" time. */
    private Timer timerSelNodes;
    /** Listener on caret movements */
    private CaretListener caretListener;
    /* task */
    private transient RequestProcessor.Task selectionTask = null;
    /** Selects element at the caret position. */
    

    

    protected boolean isActiveTC() {
        if (multiViewObserver != null)
            return getRegistry().getActivated() == multiViewObserver.getTopComponent();

        return false;
    }
}
