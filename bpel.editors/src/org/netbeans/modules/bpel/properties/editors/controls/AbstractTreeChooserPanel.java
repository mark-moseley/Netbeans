/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.controls.valid.DefaultValidator;
import org.netbeans.modules.bpel.properties.editors.controls.valid.ValidationExtension;
import org.netbeans.modules.bpel.properties.editors.controls.valid.Validator;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This class is intended to be used as a base class for different
 * Node Chooser dialogs.
 *
 * @author nk160297
 */
public class AbstractTreeChooserPanel extends JPanel
        implements ChooserLifeCycle, EditorLifeCycle,
        ExplorerManager.Provider, Validator.Provider, 
        HelpCtx.Provider, Lookup.Provider {
    
    static final long serialVersionUID = 1L;
    
    private Lookup myLookup;
    private ExplorerManager myExplorerManager = new ExplorerManager();
    
    private PropertyChangeListener selectionChangeListener;
    protected Validator myValidator;
    
    /**
     * Creates a not initialized instance.
     */
    public AbstractTreeChooserPanel() {
    }
    
    public AbstractTreeChooserPanel(Lookup lookup) {
        setLookup(lookup);
    }
    
    public void createContent() {
        selectionChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.
                        equals(evt.getPropertyName())) {
                    AbstractTreeChooserPanel.this.getValidator().revalidate(true);
                }
            }
        };
        getExplorerManager().addPropertyChangeListener(selectionChangeListener);
    }
    
    public boolean initControls() {
        getExplorerManager().setRootContext(Node.EMPTY);
        getExplorerManager().setRootContext(constructRootNode());
        getValidator().revalidate(true);
        //
        return false;
    }
    
    protected Node constructRootNode() {
        return Node.EMPTY;
    }
    
    public Lookup getLookup() {
        return myLookup;
    }
    
    public void setLookup(Lookup lookup) {
        ArrayList lookupExtensions = new ArrayList();
        //
        // Check if the lookup contains the correct nodes' factory
        NodeFactory nodeFactory = (NodeFactory)lookup.
                lookup(NodeFactory.class);
        if (nodeFactory == null ||
                !nodeFactory.getClass().equals(PropertyNodeFactory.class)) {
            lookupExtensions.add(PropertyNodeFactory.getInstance());
        }
        //
        if (lookupExtensions.isEmpty()) {
            myLookup = lookup;
        } else {
            myLookup = new ExtendedLookup(lookup, lookupExtensions);
        }
    }
    
    public synchronized ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }
    
    public void setSelectedValue(Object newValue) {
        if (newValue == null) {
            ExplorerManager exManager = getExplorerManager();
            if (exManager != null) {
                try {
                    exManager.setSelectedNodes(new Node[] {});
                } catch (PropertyVetoException ex) {
                    // Nothing to do because an exception is allowed
                }
            }
        } else {
            assert newValue instanceof Node;
            //
            ExplorerManager exManager = getExplorerManager();
            if (exManager != null) {
                try {
                    exManager.setSelectedNodes(new Node[] {(Node)newValue});
                } catch (PropertyVetoException ex) {
                    // Nothing to do because an exception is allowed
                }
            }
        }
    }
    
    public Object getSelectedValue() {
        return getSelectedNode();
    }
    
    protected Node getSelectedNode() {
        ExplorerManager exManager = getExplorerManager();
        if (exManager != null) {
            Node[] result = exManager.getSelectedNodes();
            if (result != null && result.length > 0) {
                return result[0]; // return the first element if there is any
            }
        }
        return null;
    }
    
    /**
     * Creates the new JPanel and put the tree veiw inside.
     * The Panel implements the Lookup.Provider which provids
     * special lookup is intended to trace tree selection.
     * It's necessary for correct handling of popup menu actions.
     * The lookup will be used as a context for actions.
     */
    protected JPanel createTreeWrapper(TreeView tree) {
        TreeWrapperPanel wrapperPanel = new TreeWrapperPanel();
        wrapperPanel.add(tree, BorderLayout.CENTER);
        return wrapperPanel;
    }
    
    public synchronized Validator getValidator() {
        if (myValidator == null) {
            myValidator = createValidator();
        }
        return myValidator;
    }
    
    protected Validator createValidator() {
        return new DefaultChooserValidator();
    }
    
    public boolean unsubscribeListeners() {
        return true;
    }
    
    public boolean subscribeListeners() {
        return true;
    }
    
    public boolean afterClose() {
        myLookup = null;
        return  true;
    }
    
    public boolean applyNewValues() throws Exception {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }
    
    protected class DefaultChooserValidator extends DefaultValidator {
        
        public DefaultChooserValidator() {
            super(AbstractTreeChooserPanel.this);
        }
        
        protected String getIncorrectNodeSelectionReasonKey() {
            return "ERR_INCORRECT_TREE_SELECTION"; // NOI18N
        }
        
        public boolean doFastValidation() {
            boolean result = true;
            //
            Node selectedNode = getSelectedNode();
            if (selectedNode == null) {
                addReasonKey(getIncorrectNodeSelectionReasonKey());
                return false;
            }
            NodesTreeParams treeParams =
                    (NodesTreeParams)getLookup().lookup(NodesTreeParams.class);
            if (treeParams == null) {
                return true;
            }
            //
            Class selectedNodeClass = selectedNode.getClass();
            boolean isTargetNode = false;
            Class<? extends Node>[] nodeTypesArr = treeParams.getTargetNodeClasses();
            for (Class<? extends Node> nodeType : nodeTypesArr) {
                if (nodeType.isAssignableFrom(selectedNodeClass)) {
                    isTargetNode = true;
                    break;
                }
            }
            //
            if (!isTargetNode) {
                addReasonKey(getIncorrectNodeSelectionReasonKey());
                result = false;
            }
            //
            // Process extended validator if it is specified. 
            ValidationExtension extension = (ValidationExtension)getLookup().
                    lookup(ValidationExtension.class);
            if (extension != null) {
                Validator extValidator = extension.getExtensionValidator();
                if (extValidator != null) {
                    if (extValidator.doFastValidation() != true) {
                        result = false;
                        addReasons(extValidator.getReasons());
                    }
                }
            }
            //
            return result;
        }

        public boolean doDetailedValidation() {
            boolean result = true;
            //
            if (super.doDetailedValidation() != true) {
                result = false;
            }
            //
            // Process extended validator if it is specified. 
            ValidationExtension extension = (ValidationExtension)getLookup().
                    lookup(ValidationExtension.class);
            if (extension != null) {
                Validator extValidator = extension.getExtensionValidator();
                if (extValidator != null) {
                    if (extValidator.doDetailedValidation() != true) {
                        result = false;
                        addReasons(extValidator.getReasons());
                    }
                }
            }
            //
            return result;
        }
        
    }
    
    /**
     * Provides the special lookup which is intended to be used by popup actions.
     * The tree view has to be placed inside this panel.
     */
    protected class TreeWrapperPanel extends JPanel implements Lookup.Provider {
        private Lookup lookup;
        public TreeWrapperPanel() {
            super();
            setLayout(new BorderLayout());
            lookup = ExplorerUtils.createLookup(
                    getExplorerManager(), new ActionMap());
            setFocusable(false);
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        
    }
    
}
