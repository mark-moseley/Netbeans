/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on May 19, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.etl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.etl.ui.view.ETLEditorTopView;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.SQLToolBar;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLCollaborationView;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ETLEditorViewMultiViewElement extends CloneableTopComponent
    implements MultiViewElement,  Lookup.Provider {
    
    /**
     * 
     */
    private static final long serialVersionUID = -655912409997381426L;

    private ETLDataObject mObj = null;
    
    private ETLCollaborationTopComponent mTC;
    
    private ETLEditorSupport mEditorSupport = null;
    
    private transient InstanceContent nodesHack;
    
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
        
    private static JComponent ToolBar;
    
    public ETLEditorViewMultiViewElement() {
            super();
    }
    
    public ETLEditorViewMultiViewElement(ETLDataObject dObj) {
        super();
        this.mObj = dObj;
        try {
            initialize();
        } catch (Exception ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                    NbBundle.getMessage(ETLEditorViewMultiViewElement.class,
                    "ERROR_IN_INITITALIZATION_OF_ETL_EDITOR"));
        }
    }
    
    private void initialize() throws Exception {        
        try {
            mEditorSupport = mObj.getETLEditorSupport(); 
            setLayout(new BorderLayout());
            mTC = mObj.getETLEditorTC();
            add(BorderLayout.CENTER, mTC);
            initializeLookup();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    private void initializeLookup() {
        associateLookup(createAssociateLookup());
        
        addPropertyChangeListener(new PropertyChangeListener() {
            /**
             * TODO: may not be needed at some point when parenting
             * MultiViewTopComponent delegates properly to its peer's
             * activatedNodes.
             *
             * see http://www.netbeans.org/issues/show_bug.cgi?id=67257
             *
             * note: TopComponent.setActivatedNodes is final
             */
            public void propertyChange(PropertyChangeEvent event) {
                if(event.getPropertyName().equals("activatedNodes")) {
                    nodesHack.set(Arrays.asList(getActivatedNodes()),null);
                }
            }
        });        
        setActivatedNodes(new Node[] {getETLDataObject().getNodeDelegate()});
    }
    
    private Lookup createAssociateLookup() {
        
        //
        // see http://www.netbeans.org/issues/show_bug.cgi?id=67257
        //
        nodesHack = new InstanceContent();
        return new ProxyLookup(new Lookup[] {
            //
            // other than nodesHack what else do we need in the associated
            // lookup?  I think that XmlNavigator needs DataObject
            //
            getETLDataObject().getLookup(), // this lookup contain objects that are used in OM clients
            Lookups.singleton(this),
            new AbstractLookup(nodesHack),           
        });
    }    
    
    private ETLDataObject getETLDataObject() {
        return mObj;
    }
   
    
    /**
     * Overwrite when you want to change default persistence type. Default
     * persistence type is PERSISTENCE_ALWAYS.
     * Return value should be constant over a given TC's lifetime.
     * @return one of P_X constants
     * @since 4.20
     */
    @Override
    public int getPersistenceType() {
            return PERSISTENCE_ONLY_OPENED;//was PERSISTENCE_NEVER
    }
    
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        ETLEditorSupport editor = mObj.getETLEditorSupport();
        editor.setTopComponent(callback.getTopComponent());
    }
    
    public CloseOperationState canCloseElement() {
        if ((mObj != null) && (mObj.isModified())) {
            return MultiViewFactory.createUnsafeCloseState("Data object modified", null, null);
        }
        return CloseOperationState.STATE_OK;
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();        
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();        
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();   
        DataObjectProvider.activeDataObject = mObj;
    }
    
    @Override
    public void componentActivated() {        
        super.componentActivated();    
        getETLDataObject().getETLEditorSupport().syncModel();
        getETLDataObject().createNodeDelegate();   
        DataObjectProvider.activeDataObject = mObj;
    }
    
    @Override
    public void componentDeactivated() {
        SaveCookie cookie = (SaveCookie) mObj.getCookie(SaveCookie.class);
        if(cookie != null) {
            getETLDataObject().getETLEditorSupport().synchDocument();
        }        
        super.componentDeactivated();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();        
        ETLEditorSupport editor = mObj.getETLEditorSupport();
        UndoRedo.Manager undoRedo = editor.getUndoManager();
        Document document = editor.getDocument();
        if(document != null) {
            document.removeUndoableEditListener(undoRedo);
        }
        checkModelState();        
        DataObjectProvider.activeDataObject = mObj;
    }
    
    
    
    private void checkModelState() {
        ETLEditorSupport editor = getETLDataObject().getETLEditorSupport();
        ETLDataObject dObj = (ETLDataObject) editor.getDataObject();
        ETLDefinition model = null; 
        String errorMessage = null;
        
        try {
            model = dObj.getETLDefinition();
            if( model != null) {
                recreateUI(model);
                return;
            }
        } catch (Exception ex) {
            errorMessage = ex.getMessage();            
        }
        
        //if it comes here, either the schema is not well-formed or invalid
        if( model == null ) {
            if(errorMessage == null)
                errorMessage = NbBundle.getMessage(
                        ETLEditorViewMultiViewElement.class,
                        "MSG_NotWellformedEtl");
        }
        
        emptyUI(errorMessage);
    }
    
    
    private void emptyUI(String errorMessage) {
        this.removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg :
            Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
    }
    
    private void recreateUI(ETLDefinition model) {
        this.removeAll();
        // Add the schema category pane as our primary interface.
        add(mTC, BorderLayout.CENTER);
    }
    
    public javax.swing.JComponent getToolbarRepresentation() {
        if(ToolBar == null) {
            ToolBar = createToolbar();
        }        
        return ToolBar;
    }

    private JToolBar createToolbar() {
        IGraphView graphView = this.mTC.getGraphView();
        ETLCollaborationModel collabModel = getETLDataObject().getModel();
        ETLEditorTopView etlTopView = new ETLEditorTopView(collabModel,this.mTC);        
        etlTopView.enableToolBarActions(true);        
        SQLCollaborationView collabView = etlTopView.getCollaborationView();        
        IToolBar toolBar = new SQLToolBar(collabView.getIOperatorManager());        
        if(toolBar != null) {
            toolBar.setGraphView(graphView);
            toolBar.setActions(etlTopView.getToolBarActions());
            toolBar.initializeToolBar();                        
        }
        return (JToolBar) toolBar;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }   
    
    public static JToolBar getToolBar() {
        return (JToolBar) ToolBar;
    }
    
    public static void setToolBar(IToolBar tBar) {
        ToolBar = (JToolBar)tBar;
    }
    
    public CloneableTopComponent getComponent() {
        return this;
    }
        
    /** Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
     @Override
    public UndoRedo getUndoRedo () {
         return new TreeEditorUndoRedo();
     }
     
     public void cleanUp() {
        
     }

     class TreeEditorUndoRedo implements UndoRedo {
        
        public void addChangeListener(ChangeListener l) {
            
        }
        
        public boolean canRedo() {
            return mEditorSupport.getUndoManager().canRedo();
        }
        
        public boolean canUndo() {
            return mEditorSupport.getUndoManager().canUndo();
        }
        
        public String getRedoPresentationName() {
            if(mEditorSupport != null) {
                return mEditorSupport.getUndoManager().getRedoPresentationName();
            }
            return "Redo_ETLTreeEditor";
        }
        
        public String getUndoPresentationName() {
            if(mEditorSupport != null) {
                return mEditorSupport.getUndoManager().getUndoPresentationName();
            }
            
            return "Undo_ETLTreeEditor";
        }
        
        public void redo() throws CannotRedoException {
            mEditorSupport.getUndoManager().redo();
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void undo() throws CannotUndoException {
            mEditorSupport.getUndoManager().undo();
        }
     }
}
