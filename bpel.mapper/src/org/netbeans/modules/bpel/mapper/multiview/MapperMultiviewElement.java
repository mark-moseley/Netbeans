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
package org.netbeans.modules.bpel.mapper.multiview;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Enumeration;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.mapper.model.BpelMapperFactory;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.palette.Palette;
import org.netbeans.modules.bpel.mapper.tree.actions.BpelCopyAction;
import org.netbeans.modules.bpel.mapper.tree.actions.BpelCutAction;
import org.netbeans.modules.bpel.mapper.tree.actions.BpelDeleteSelectionGraphAction;
import org.netbeans.modules.bpel.mapper.tree.actions.BpelPasteAction;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class MapperMultiviewElement extends TopComponent
        implements MultiViewElement, Serializable, MapperTcContext, ExplorerManager.Provider
{

//    private static final long serialVersionUID = 1L;
    public static final String MAPPER_PANEL_ID = "mapperPanelId"; // NOI18N
    private transient JPanel myMapperPanel;
    private transient Mapper myMapper;
    private transient JEditorPane myErrorPanel;
    private transient CardLayout myCardLayout;
    private transient JComponent myToolBarPanel;
    private transient MultiViewElementCallback myMultiViewObserver;
    private BPELDataObject myDataObject;
    private DesignContextChangeListener myContextChangeListener;
    private DesignContextController myContextController;
    private Boolean groupVisible;
    private ActivatedNodesMediator myNodesMediator;
    private CookieProxyLookup myCookieProxyLookup;
    private ExplorerManager myExplorerManager;

    // for deexternalization
    public MapperMultiviewElement() {
        super();
    }

    public MapperMultiviewElement(BPELDataObject dObj) {
        myDataObject = dObj;
        initialize();
        initializeUI();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        if ( myToolBarPanel == null ) {
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setFocusable(false);
            toolbar.addSeparator();

            int maxButtonHeight = 0;

            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    maxButtonHeight = Math.max(c.getPreferredSize().height,
                            maxButtonHeight);
                }
            }

            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    Dimension size = c.getMaximumSize();
                    size.height = maxButtonHeight;
                    c.setMaximumSize(size);
                    c.setMinimumSize(c.getPreferredSize());
                    c.setFocusable(false);
                } else if ((c instanceof JTextComponent)
                        || (c instanceof JComboBox))
                {
                    c.setMaximumSize(c.getPreferredSize());
                    c.setMinimumSize(c.getPreferredSize());
                } else {
                    c.setMinimumSize(c.getPreferredSize());
                    c.setFocusable(false);
                }
            }

            myToolBarPanel = toolbar;
        }

        return myToolBarPanel;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }

    // TODO r | m
    public DesignContextController getDesignContextController() {
        return myContextController;
    }

    // TODO r
    public TopComponent getTopComponent() {
        return this;
    }

    public CloseOperationState canCloseElement() {
        //
        // actually if there are any visual changed NOT committed to the model
        // then we may need to flush something here or something
        //
        boolean lastView = isLastView();

        if(!lastView) {
            return CloseOperationState.STATE_OK;
        }

        BPELDataEditorSupport editorSupport = myDataObject.getEditorSupport();
        boolean modified = editorSupport.isModified();

        if(!modified) {
            return CloseOperationState.STATE_OK;
        } else {
            return MultiViewFactory.createUnsafeCloseState(
                    "Data Object Modified", null, null);    // NOI18N
        }
    }

    // TODO m
    @Override
    public void componentActivated() {
//        System.out.println("mapperTC activated "+getClass());
        super.componentActivated();

        boolean clearFocus = true;

        if (myMapper != null) {
            if (!isFocusInside(myMapper)) {
                Component focusable = getFocusableDescendant(myMapper);
                if (focusable != null) {
                    focusable.requestFocusInWindow();
                    clearFocus = false;
                }
            }
        }

        if (clearFocus) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .clearGlobalFocusOwner();
        }

        activateContextNode();
        ExplorerUtils.activateActions(myExplorerManager, true);
    }

    private boolean isFocusInside(Component container) {
        Component focusOwner = KeyboardFocusManager
                .getCurrentKeyboardFocusManager().getFocusOwner();
        for (Component c = focusOwner; c != null; c = c.getParent()) {
            if (c == container) {
                return true;
            }
        }

        return false;
    }

    private Component getFocusableDescendant(Component ancestor) {
        if (ancestor instanceof JTree) {
            return ancestor;
        }

        Component result = null;

        if (ancestor instanceof Container) {
            Container container = (Container) ancestor;
            for (int i = container.getComponentCount() - 1; i >= 0; i--) {
                result = getFocusableDescendant(container.getComponent(i));
                if (result != null) break;
            }
        }

        return result;
    }

    @Override
    public void componentOpened() {
//        System.out.println("mapperTC opened: "+getClass());
        super.componentOpened();
    }

    @Override
    public void componentShowing() {
//        System.out.println("mapperTC showing "+getClass());
        super.componentShowing();
        myContextController.showMapper();
        if (myMapper != null) {
            ActionMap aMap = getActionMap();

            BpelPasteAction paste = SystemAction.get(BpelPasteAction.class);
            paste.initialize(myMapper);
            aMap.put(SystemAction.get(PasteAction.class).getActionMapKey(), paste);

            BpelCopyAction copy = SystemAction.get(BpelCopyAction.class);
            copy.initialize(myMapper);
            aMap.put(SystemAction.get(CopyAction.class).getActionMapKey(), copy);

            BpelCutAction cut = SystemAction.get(BpelCutAction.class);
            cut.initialize(myMapper);
            aMap.put(SystemAction.get(CutAction.class).getActionMapKey(), cut);

            BpelDeleteSelectionGraphAction del = SystemAction.get(BpelDeleteSelectionGraphAction.class);
            del.initialize(myMapper);
            aMap.put(SystemAction.get(DeleteAction.class).getActionMapKey(), del);
        }
    }

    @Override
    public void componentDeactivated() {
//        System.out.println("mapperTC deactivated "+getClass());
        super.componentDeactivated();
        ExplorerUtils.activateActions(myExplorerManager, false);
    }

    @Override
    public void componentHidden() {
//        System.out.println("mapperTC hidden "+getClass());
        super.componentHidden();
        myContextController.hideMapper();
    }

    @Override
    public void componentClosed() {
//        System.out.println("mapperTC closed "+getClass());
        super.componentClosed();
        cleanup();
    }

    private void cleanup() {
        try {
            myExplorerManager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
////        removePropertyChangeListener(myContextChangeListener);

        removePropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES, myNodesMediator);
        removePropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES, myCookieProxyLookup);
        myNodesMediator = null;
        myCookieProxyLookup = null;

        //required to release all references to OM
//        myMapper.closeView();
        myContextController.cleanup();
        myContextController = null;
        myContextChangeListener = null;
        myMapper = null;
        removeAll();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return myDataObject.getEditorSupport().getUndoManager();
    }

    /**
     * Opens or closes the bpel_mapper_tcgroup TopComponentGroup.
     *
     * TODO: Figure out if it necessary to use a group here or using
     * of topComp.open() or topComp.close is enough.
     */
    public void showMapperTcGroup(final boolean show) {
// TODO a & m
////////        // when active TopComponent changes, check if we should open or close
////////        // the BPEL  editor group of windows
////////        WindowManager wm = WindowManager.getDefault();
////////        final TopComponentGroup group = wm.findTopComponentGroup("bpel_designer"); // NOI18N
////////        if (group == null) {
////////            return; // group not found (should not happen)
////////        }
////////        //
////////        boolean mapperSelected = false;
////////        Iterator it = wm.getModes().iterator();
////////        while (it.hasNext()) {
////////            Mode mode = (Mode) it.next();
////////            TopComponent selected = mode.getSelectedTopComponent();
////////            if (selected != null) {
////////            MultiViewHandler mvh = MultiViews.findMultiViewHandler(selected);
////////                if (mvh != null) {
////////                    MultiViewPerspective mvp = mvh.getSelectedPerspective();
////////                    if (mvp != null) {
////////                        String id = mvp.preferredID();
////////                        if (BpelMapperMultiviewElementDesc.PREFERED_ID.equals(id)) {
////////                            mapperSelected = true;
////////                            break;
////////                        }
////////                    }
////////                }
////////            }
////////        }
////////        //
////////        if (mapperSelected && !Boolean.TRUE.equals(groupVisible)) {
////////            group.open();
////////        } else if (!mapperSelected && !Boolean.FALSE.equals(groupVisible)) {
////////            group.close();
////////        }
////////        //
////////        groupVisible = mapperSelected ? Boolean.TRUE : Boolean.FALSE;
    }

    public static String getTitleBase() {
        return NbBundle.getMessage(MapperMultiviewElement.class, "TITLE_MAPPER_WINDOW");  // NOI18N
    }

    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }

    //------------------------------------------------------------------------------
    private boolean isLastView() {
        boolean oneOrLess = true;
        Enumeration en =
                ((CloneableTopComponent)myMultiViewObserver.getTopComponent()
                ).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }

        return oneOrLess;
    }

    protected abstract DesignContextController createDesignContextController();

    protected void initialize() {
        ShowMapperCookie showCookie = new ShowMapperCookie() {
            public void show(MapperModel mapperModel) {
                MapperMultiviewElement.this.setMapperModel(mapperModel);
            }

            public void show(String message) {
                MapperMultiviewElement.this.setMessage(message);
            }
        };

        myExplorerManager = new ExplorerManager();
        Node delegate = myDataObject.getNodeDelegate();
        myNodesMediator = new ActivatedNodesMediator(delegate);
        myNodesMediator.setExplorerManager(this);
        myCookieProxyLookup = new CookieProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {
                        showCookie,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        myDataObject,
                }),
                myDataObject.getLookup(),// this lookup contain objects that are used in OM clients
                Lookups.singleton(this),

                myNodesMediator.getLookup(),
                // The Node delegate Lookup must be the last one in the list
                // for the CookieProxyLookup to work properly.
                delegate.getLookup(),
        }, delegate);

        associateLookup(myCookieProxyLookup);
        addPropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES,
                myNodesMediator);
        addPropertyChangeListener(TopComponent.Registry.PROP_ACTIVATED_NODES,
                myCookieProxyLookup);
        myContextController = createDesignContextController();
//        initListeneres();
   }

    protected void initializeUI() {
        // activate cur node
        activateContextNode();
        // create empty mapper;
        myMapperPanel = new JPanel();
        myMapperPanel.setLayout(new BorderLayout());
        myMapper = BpelMapperFactory.createMapper(null);
        myMapperPanel.add(myMapper, BorderLayout.CENTER);
        myMapperPanel.add(new Palette(myMapper).createMenuBar(), BorderLayout.NORTH);

        myErrorPanel = new MessagePanel(myMapper);
        myErrorPanel.setText("<b>It is Error Panel !!!</b>"); // NOI18N

        myCardLayout = new CardLayout();
        setLayout(myCardLayout);
        add(myMapperPanel, MAPPER_PANEL_ID);
        add(myErrorPanel, MessagePanel.MESSAGE_PANEL_ID);
    }

    private void activateContextNode() {
        BpelDesignContext context = myContextController != null
                ? myContextController.getContext() : null;
        Node aNode = null;
        if (DesignContextUtil.isValidContext(context)) {
            aNode = context == null ? null : context.getActivatedNode();
        }

        Node[] tcANodes = getActivatedNodes();
        Node tcANode = null;

        if (tcANodes != null && tcANodes.length > 0
                && DesignContextUtil.isValidNode(tcANodes[0])) {
            tcANode = tcANodes[0];
        }

        if (tcANode == null
                || !tcANode.equals(aNode)) {
            if (aNode != null) {
                setActivatedNodes(new Node[] {aNode});
            } else {
                setActivatedNodes(new Node[0]);
            }
        }
    }

    // TODO m
//    protected void initListeneres( ) {
//
////         Check if the BPEL mapper is subscribed to changes of activated node
////         and subscribe if it does not.
//        if (myContextChangeListener == null) {
////            myContextChangeListener = new DesignContextChangeListener(
////                    myContextController);
////            myContextController.setContext(DesignContextChangeListener.getActivatedContext());
//        } else {
////            TopComponent.getRegistry().
////                    removePropertyChangeListener(myContextChangeListener);
//        }
//
//        //add TopComponent Active Node changes listener :
////        TopComponent.getRegistry().
////                addPropertyChangeListener(myContextChangeListener);
//
//    }

//    private void showContextMapper() {
//            Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
//            if (nodes != null && nodes.length > 0 && nodes[0] instanceof InstanceRef) {
//                Object entity = ((InstanceRef) nodes[0]).getReference();
//                if (entity instanceof BpelEntity) {
//                    BpelEntity bpelEntity = (BpelEntity)entity;
//                    setMapperModel(
////                            BpelMapperModelFactory.getInstance().constructModel(
////                            bpelEntity, nodes[0].getLookup()));
//                            BpelMapperModelFactory.getInstance().constructModel(
//                            myContextController.getContext()));
//                }
//
//            }
//    }

    // TODO r
    public void setMapper(final Mapper newMapper) {
        assert true : "mapper shouldn't be changed in mapper tc";
////        if (!EventQueue.isDispatchThread()) {
////            SwingUtilities.invokeLater(new Runnable() {
////                public void run() {
////                    MapperMultiviewElement.this.setMapperInAwt(newMapper);
////                }
////            });
////        } else {
////            setMapperInAwt(newMapper);
////        }
    }

    private void setMapperInAwt(Mapper newMapper) {
        assert EventQueue.isDispatchThread();

        //removeAll();

        if (newMapper != null) {
            myCardLayout.show(this, MAPPER_PANEL_ID);
        }
        revalidate();
        repaint();
    }

    private void showMapper() {
        assert EventQueue.isDispatchThread();
        if (myMapper == null) {
            return;
        }

        myCardLayout.show(this, MAPPER_PANEL_ID);

        revalidate();
        repaint();
    }

    // TODO r
    public Mapper getMapper() {
        return myMapper;
    }

    private void setMessage(final String message) {
        if (!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MapperMultiviewElement.this.setMessageInAwt(message);
//                    updateTitle();
                }
            });
        } else {
            setMessageInAwt(message);
//            updateContextTitle();
        }
    }

    private void setMessageInAwt(String message) {
        assert EventQueue.isDispatchThread();

        if (message != null) {
            myErrorPanel.setText(message);
        } else {
            myErrorPanel.setText(NbBundle.getMessage(MapperMultiviewElement.class, "LBL_CantShowMapper")); // NOI18N
        }
        myCardLayout.show(this, MessagePanel.MESSAGE_PANEL_ID);
    }

    // TODO m
    public void setMapperModel(final MapperModel mModel) {

        if (!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MapperMultiviewElement.this.setMapperModelInAwt(mModel);
//                    updateContextTitle();
                }
            });
        } else {
            setMapperModelInAwt(mModel);
//            updateContextTitle();
        }
    }

    private void setMapperModelInAwt(MapperModel mModel) {
        assert EventQueue.isDispatchThread();

        if (myMapper != null) {
            myMapper.setModel(mModel);

            //
//            setMapper(myMapper);
            showMapper();
        }
//
//        else {
//            Mapper newMapper = createMapper(mModel);
//            setMapper(newMapper);
//        }
    }

    protected abstract Mapper createMapper(MapperModel mModel);

//    private void updateContextTitle() {
//        assert EventQueue.isDispatchThread();
//
//        String activeNodeDisplayName = null;
//        BpelDesignContext currentDesignContext = myContextController.getContext();
//        if (currentDesignContext != null) {
//            Node node = currentDesignContext.getActivatedNode();
//            if (node != null) {
//                activeNodeDisplayName = node.getDisplayName();
//            }
//        }
//        if (activeNodeDisplayName != null && myContextTitle != null) {
//            myContextTitle.setText("Context Entity: "+activeNodeDisplayName);
//              //setDisplayName(getTitleBase() + " - " + activeNodeDisplayName);  // NOI18N
//
//        }
//    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }

    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    @Override
    public void readExternal( ObjectInput in ) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);

        myDataObject = (BPELDataObject) in.readObject();

        initialize();
//        initializeLookup();
        //
        // FIX ME
        //
        initializeUI();
    }
}
