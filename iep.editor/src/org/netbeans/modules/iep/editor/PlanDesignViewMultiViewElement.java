/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.iep.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PlanDesignViewMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider {
    private static final long serialVersionUID = -655912409997381426L;
    private static final String ACTIVATED_NODES = "activatedNodes";//NOI18N
    private ExplorerManager manager;
    private PlanDataObject mObj;
    private transient MultiViewElementCallback multiViewObserver;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient JToolBar mToolbar;

    public PlanDesignViewMultiViewElement() {
        super();
    }

    public PlanDesignViewMultiViewElement(PlanDataObject dObj) {
        super();
        this.mObj = dObj;
        initialize();
    }

    private void initialize() {
        manager = new ExplorerManager();
        // Install our own actions.
        CallbackSystemAction globalFindAction = SystemAction.get(FindAction.class);
        Object mapKey = globalFindAction.getActionMapKey();
        Action action = new WSDLFindAction();
        ActionMap map = getActionMap();
        map.put(mapKey, action);
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));

        // Define the keyboard shortcuts for the actions.
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = (KeyStroke) globalFindAction.getValue(Action.ACCELERATOR_KEY);
        if (key == null) {
            key = KeyStroke.getKeyStroke("control F");
        }
        keys.put(key, mapKey);

        
        Node delegate = mObj.getNodeDelegate();
        
        setLayout(new BorderLayout());
    }

    
    private void cleanup() {
        try {
            manager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
        
       if (mToolbar != null) mToolbar.removeAll();
        mToolbar = null;
        removeAll();
    }
    public ExplorerManager getExplorerManager() {
    	return manager;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewObserver = callback;
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
/*
    @Override
    public UndoRedo getUndoRedo() {
    return mObj.getWSDLEditorSupport().getUndoManager();
    }
*/
    @Override
    public void componentHidden() {
        super.componentHidden();
        
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        cleanup();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        initUI();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(manager, true);
        //mObj.getWSDLEditorSupport().syncModel();
        updateGroupVisibility();
    }
    
    @Override
    public void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
        super.componentDeactivated();
        updateGroupVisibility();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        initUI();

        
    }

    @Override
    public void requestActive() {
        super.requestActive();
       
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PlanDesignViewMultiViewDesc.class);
    }

    private static Boolean groupVisible = null;
    
    private void updateGroupVisibility() {
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("wsdl_ui"); // NOI18N
        if (group == null) {
            return; // group not found (should not happen)
        }
        //
        boolean isWSDLViewSelected = false;
        Iterator it = wm.getModes().iterator();
        while (it.hasNext()) {
            Mode mode = (Mode) it.next();
            TopComponent selected = mode.getSelectedTopComponent();
            if (selected != null) {
            MultiViewHandler mvh = MultiViews.findMultiViewHandler(selected);
                if (mvh != null) {
                    MultiViewPerspective mvp = mvh.getSelectedPerspective();
                    if (mvp != null) {
                        String id = mvp.preferredID();
                        if (PlanDesignViewMultiViewDesc.PREFERRED_ID.equals(id)) {
                            isWSDLViewSelected = true;
                            break;
                        }
                    }
                }
            }
        }
        //
        if (isWSDLViewSelected && !Boolean.TRUE.equals(groupVisible)) {
            group.open();
        } else if (!isWSDLViewSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
        }
        //
        groupVisible = isWSDLViewSelected ? Boolean.TRUE : Boolean.FALSE;
        
    }

    @Override
    protected String preferredID() {
        return "WSDLTreeViewMultiViewElementTC";  //  NOI18N
    }

    /**
     * Construct the user interface.
     */
    private void initUI() {
        PlanEditorSupport editor = mObj.getPlanEditorSupport();
        //WSDLModel wsdlModel = null;
        String errorMessage = null;
        /*wsdlModel = editor.getModel();
        if (wsdlModel != null &&
        		wsdlModel.getState() == WSDLModel.State.VALID) {
        	// Construct the standard editor interface.
        	return;
        }

        // If it comes here, either the model is not well-formed or invalid.
        if (wsdlModel == null ||
        		wsdlModel.getState() == WSDLModel.State.NOT_WELL_FORMED) {
        	errorMessage = NbBundle.getMessage(
        			WSDLTreeViewMultiViewElement.class,
        			"MSG_NotWellformedWsdl");
        }*/

        // Clear the interface and show the error message.
        removeAll();
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

    public javax.swing.JComponent getToolbarRepresentation() {
        if (mToolbar == null) {
        	/*WSDLModel model = mObj.getWSDLEditorSupport().getModel();
        	if (model != null && model.getState() == WSDLModel.State.VALID) {
        		mToolbar = new JToolBar();
        		mToolbar.setFloatable(false);
        		// vlv: search
        		mToolbar.addSeparator();
        		SearchManager searchManager = SearchManagerAccess.getManager();

        		if (searchManager != null) {
        			mToolbar.add(searchManager.getSearchAction());
        		}
        		mToolbar.addSeparator();
        		mToolbar.add(new ValidateAction(model));
        	}*/
        }
        return mToolbar;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }   

    /**
     * Find action for WSDL editor.
     *
     * @author  Nathan Fiedler
     */
    private class WSDLFindAction extends AbstractAction {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of WSDLFindAction.
         */
        public WSDLFindAction() {
        }

        public void actionPerformed(ActionEvent event) {
            PlanDesignViewMultiViewElement parent =
                    PlanDesignViewMultiViewElement.this;
            
        }
    }
}
