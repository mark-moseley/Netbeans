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
 * Microsystems, Inc. All Rights  Reserved.
 *
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.navigation.JSFPageFlowMultiviewDescriptor.PageFlowElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.LayoutUtility;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneData;
import org.netbeans.modules.web.jsf.navigation.graph.SceneSerializer;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class PageFlowView extends TopComponent implements Lookup.Provider, ExplorerManager.Provider {

    private JSFConfigEditorContext context;
    private PageFlowScene scene;
    private JSFConfigModel configModel;
    private PageFlowController pfc;
    private PageFlowElement multiview;
    private PageFlowSceneData sceneData;
    private BlockingQueue<Runnable> runnables = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executor;

    private static final Logger LOG = Logger.getLogger("org.netbeans.web.jsf.navigation");
    private static final int CAPACITY = 1000;

    private JComponent view;


    private static final String ACN_PAGEVIEW_TC = NbBundle.getMessage(PageFlowView.class, "ACN_PageView_TC");
    private static final String ACDS_PAGEVIEW_TC = NbBundle.getMessage(PageFlowView.class, "ACDS_PageView_TC");

    PageFlowView(PageFlowElement multiview, JSFConfigEditorContext context) {
        this.multiview = multiview;
        this.context = context;
        scene = initializeScene();
        pfc = new PageFlowController(context, this);
        sceneData = new PageFlowSceneData(PageFlowUtilities.getInstance(this));

        deserializeNodeLocation(getStorageFile(context.getFacesConfigFile()));
        //runnables = new LinkedBlockingQueue<Runnable>();
        pfc.setupGraphNoSaveData(); /* I don't want to override the loaded locations with empy sceneData */
        LOG.fine("Initializing Page Flow SetupGraph");

        setFocusable(true);

        executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, runnables);
        LOG.finest("Create Executor Thread");

        getAccessibleContext().setAccessibleDescription(ACDS_PAGEVIEW_TC);
        getAccessibleContext().setAccessibleName(ACN_PAGEVIEW_TC);
        
    }

    public void requestMultiViewActive() {
        multiview.getMultiViewCallback().requestActive();
        requestFocus(); //This is a hack because requestActive does not call requestFocus when it is already active (BUT IT SHOULD).
    }

    /**
     *
     * @return PageFlowController
     */
    public PageFlowController getPageFlowController() {
        return pfc;
    }


    /** Weak reference to the lookup. */
    private WeakReference<Lookup> lookupWRef = new WeakReference<Lookup>(null);


    public Lookup getLookup() {
        Lookup lookup = lookupWRef.get();

        if (lookup == null) {
            Lookup superLookup = super.getLookup();

            // XXX Needed in order to close the component automatically by project close.
            /* This is currently done at the MultiViewElement level all though we can easily add it here */
            //            DataObject jspDataObject = webform.getJspDataObject();
            //            DataObject jspDataObject = null;
            //            try {
            //                jspDataObject = DataObject.find(context.getFacesConfigFile());
            //            } catch ( DataObjectNotFoundException donfe) {
            //                donfe.printStackTrace();
            //            }
            /* Temporarily Removing Palette */
            //            PaletteController paletteController = getPaletteController();
            //            if (paletteController == null) {
            lookup = new ProxyLookup(new Lookup[]{superLookup, Lookups.fixed(new Object[]{scene})});
            //            } else {
            //                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] { paletteController})});
            //            }
            lookupWRef = new WeakReference<Lookup>(lookup);
        }

        return lookup;
    }


    public void unregstierListeners() {
        if (pfc != null) {
            pfc.unregisterListeners();
        }
    }

    public void registerListeners() {
        if (pfc != null) {
            pfc.registerListeners();
        }
    }


    /*
     * Initializes the Panel and the graph
     **/
    private PageFlowScene initializeScene() {
        setLayout(new BorderLayout());

        scene = new PageFlowScene(this);

        scene.setAccessibleContext(this.getAccessibleContext());
        view = scene.createView();

        JScrollPane pane = new JScrollPane(view);
        pane.setVisible(true);

        add(pane, BorderLayout.CENTER);

        setDefaultActivatedNode();

        return scene;
    }

    /**
     * Set the default actived node to faces config node.
     */
    public void setDefaultActivatedNode() {
        try {
            Node node = new DefaultDataNode(DataObject.find(context.getFacesConfigFile()));
            setActivatedNodes(new Node[]{node});
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
    }

    private class DefaultDataNode extends FilterNode {

        Node srcFolderNode = null;

        public DefaultDataNode(DataObject dataObject) {
            this(dataObject.getNodeDelegate());
            org.netbeans.api.project.Project p = org.netbeans.api.project.FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            org.openide.filesystems.FileObject projectDirectory = p.getProjectDirectory();
            org.netbeans.api.project.Sources sources = org.netbeans.api.project.ProjectUtils.getSources(p);
            org.netbeans.api.project.SourceGroup[] groups = sources.getSourceGroups(org.netbeans.api.project.Sources.TYPE_GENERIC);
            org.openide.filesystems.FileObject srcFolder;

            try {
                if (groups != null && groups.length > 0) {
                    srcFolder = groups[0].getRootFolder();
                } else {
                    srcFolder = dataObject.getFolder().getPrimaryFile();
                }
                srcFolderNode = org.openide.loaders.DataObject.find(srcFolder).getNodeDelegate();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public DefaultDataNode(Node node) {
            super(node);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            if (type.equals(DataFolder.class)) {
                assert srcFolderNode != null;
                return srcFolderNode.getCookie(type);
            }
            return super.getCookie(type);
        }
    }

    /**
     *
     */
    public void warnUserMalFormedFacesConfig() {
        //        clearGraph();
        scene.createMalFormedWidget();
    }

    public void removeUserMalFormedFacesConfig() {
        scene.removeMalFormedWidget();
    }

    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    /**
     *
     */
    public void clearGraph() {
        //Workaround: Temporarily Wrapping Collection because of  http://www.netbeans.org/issues/show_bug.cgi?id=97496
        Collection<Page> nodes = new HashSet<Page>(scene.getNodes());
        for (Page node : nodes) {
            scene.removeNodeWithEdges(node);
        }
        scene.validate();
    }

    /**
     *
     */
    public void validateGraph() {
        //        scene.layoutScene();
        //        System.out.println("Validating Graph: ");
        //        System.out.println("Nodes: " + scene.getNodes());
        //        System.out.println("Edges: "+ scene.getEdges());
        //        System.out.println("Pins: " + scene.getPins());
        scene.validate();
    }

    public void saveLocations() {
        sceneData.saveCurrentSceneData(scene);
    }

    public void saveLocation(String oldDisplayName, String newDisplayName) {
        sceneData.savePageWithNewName(oldDisplayName, newDisplayName);
    }


//    private static final String ACN_PAGE = NbBundle.getMessage(PageFlowView.class, "ACN_Page");
//    private static final String ACDS_PAGE = NbBundle.getMessage(PageFlowView.class, "ACDS_Page");
//    private static final String ACN_PIN = NbBundle.getMessage(PageFlowView.class, "ACN_Pin");
//    private static final String ACDS_PIN = NbBundle.getMessage(PageFlowView.class, "ACDS_Pin");
//    private static final String ACN_EDGE = NbBundle.getMessage(PageFlowView.class, "ACN_Edge");
//    private static final String ACDS_EDGE = NbBundle.getMessage(PageFlowView.class, "ACDS_Edge");
    /**
     * Creates a PageFlowScene node from a pageNode.  The PageNode will generally be some type of DataObject unless
     * there is no true file to represent it.  In that case a abstractNode should be passed
     * @param pageNode the node that represents a dataobject or empty object
     * @param type
     * @param glyphs
     * @return
     */
    protected VMDNodeWidget createNode(final Page pageNode, String type, List<Image> glyphs) {
        String pageName = pageNode.getDisplayName();


        final VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(pageNode);
        //        widget.setNodeProperties(null /*IMAGE_LIST*/, pageName, type, glyphs);
        widget.setNodeProperties(pageNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16), pageName, type, glyphs);
        widget.setPreferredLocation(sceneData.getPageLocation(pageName));

//        widget.getAccessibleContext().setAccessibleDescription(ACDS_PAGE);
//        widget.getAccessibleContext().setAccessibleDescription(ACN_PAGE);
        scene.addPin(pageNode, new Pin(pageNode));
        runPinSetup(pageNode, widget);
        return widget;
    }





    private void runPinSetup(final Page pageNode, final VMDNodeWidget widget) {
        LOG.entering(PageFlowView.class.getName(), "runPinSetup");
        final LabelWidget loadingWidget = new LabelWidget(scene, "Loading...");
        widget.addChild(loadingWidget);
        runnables.add(new Runnable() {

            public void run() {
                /* This is called in redrawPins and edges setupPinsInNode(pageNode);*/
                /* Need to do updateNodeWidgetActions after setupPinInNode because this is when the model is set. */
                LOG.finest("    PFE: Inside Thread: " + java.util.Calendar.getInstance().getTime());
                if (!pageNode.isDataNode()) {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            widget.removeChild(loadingWidget);
                            scene.validate();
                        }
                    });
                    return;
                }
                final java.util.Collection<org.netbeans.modules.web.jsf.navigation.Pin> newPinNodes = pageNode.getPinNodes();

                LOG.finest("    PFE: Completed Nodes Setup: " + java.util.Calendar.getInstance().getTime());

                try {
                    EventQueue.invokeAndWait(new java.lang.Runnable() {

                        public void run() {
                            LOG.finest("    PFE: Starting Redraw: " + java.util.Calendar.getInstance().getTime());
                            java.util.Collection<org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge> redrawCaseNodes = new java.util.ArrayList<org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge>();
                            java.util.Collection<org.netbeans.modules.web.jsf.navigation.Pin> pinNodes = new java.util.ArrayList<org.netbeans.modules.web.jsf.navigation.Pin>(scene.getPins());

                            widget.removeChild(loadingWidget);
                            for (org.netbeans.modules.web.jsf.navigation.Pin pin : pinNodes) {
                                if (pin.getPage() == pageNode) {
                                    assert pin.getPage().getDisplayName().equals(pageNode.getDisplayName());
                                    java.util.Collection<org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge> caseNodes = scene.findPinEdges(pin, true, false);
                                    redrawCaseNodes.addAll(caseNodes);
                                    if (!pin.isDefault()) {
                                        scene.removePin(pin);
                                    }
                                }
                            }
                            if (newPinNodes.size() > 0) {
                                for (org.netbeans.modules.web.jsf.navigation.Pin pinNode : newPinNodes) {
                                    createPin(pageNode, pinNode);
                                }
                            }
                            for (org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge caseNode : redrawCaseNodes) {
                                setEdgeSourcePin(caseNode, pageNode);
                            }
                            scene.updateNodeWidgetActions(pageNode);
                            scene.validate();
                            LOG.finest("    PFE: Ending Redraw: " + java.util.Calendar.getInstance().getTime());
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        LOG.exiting(PageFlowView.class.getName(), "runPinSetup");
    }

    private void setupPinsInNode(Page pageNode) {
        Collection<Pin> pinNodes = pageNode.getPinNodes();
        for (Pin pinNode : pinNodes) {
            createPin(pageNode, pinNode);
        }
    }


    /**
     * Creates a PageFlowScene pin from a pageNode and pin name String.
     * In general a pin represents a NavigasbleComponent orginally designed for VWP.
     * @param pageNode
     * @param pinNode representing that page item.
     * @return
     */
    protected final VMDPinWidget createPin(Page pageNode, Pin pinNode) {
        VMDPinWidget widget = (VMDPinWidget) scene.addPin(pageNode, pinNode);
        //        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, pin);
        //        if( navComp != null ){
        //            widget.setProperties(navComp, Arrays.asList(navComp.getBufferedIcon()));
        //        }
//        widget.getAccessibleContext().setAccessibleName(ACN_PIN);
//        widget.getAccessibleContext().setAccessibleDescription(ACDS_PIN);
        return widget;
    }

    /**
     * Creates an Edge or Connection in the Graph Scene
     * @param navCaseNode
     * @param fromPageNode
     * @param toPageNode
     */
    protected void createEdge(NavigationCaseEdge navCaseEdge, Page fromPageNode, Page toPageNode) {
        assert fromPageNode.getDisplayName() != null;
        assert toPageNode.getDisplayName() != null;

        VMDConnectionWidget connectionWidget = (VMDConnectionWidget) scene.addEdge(navCaseEdge);
        setEdgeSourcePin(navCaseEdge, fromPageNode);
        setEdgeTargePin(navCaseEdge, toPageNode);

        //connectionWidget.getAccessibleContext().setAccessibleName(ACN_EDGE);
        //connectionWidget.getAccessibleContext().setAccessibleDescription(ACDS_PAGE);
    }


    public void renameEdgeWidget(NavigationCaseEdge edge, String newName, String oldName) {
        scene.renameEdgeWidget(edge, newName, oldName);
    }
    
    public Pin getEdgeSourcePin( NavigationCaseEdge navCase ) {  
        return scene.getEdgeSource(navCase);
    }

    private void setEdgeSourcePin(NavigationCaseEdge navCaseNode, Page fromPageNode) {
        Pin sourcePin = scene.getDefaultPin(fromPageNode);
        Collection<Pin> pinNodes = scene.getPins();
        for (Pin pin : pinNodes) {
            if (pin.getFromOutcome() != null && fromPageNode == pin.getPage() && pin.getFromOutcome().equals(navCaseNode.getFromOuctome())) {
                sourcePin = pin;
                /* Remove any old navigation case nodes coming from this source */
                Collection<NavigationCaseEdge> oldNavCaseNodes = scene.findPinEdges(sourcePin, true, false);
                for (NavigationCaseEdge oldNavCaseNode : oldNavCaseNodes) {
                    scene.setEdgeSource(oldNavCaseNode, scene.getDefaultPin(fromPageNode));
                }
            }
        }

        scene.setEdgeSource(navCaseNode, sourcePin);
    }
    

    private void setEdgeTargePin(NavigationCaseEdge navCaseNode, Page toPageNode) {
        Pin targetPin = scene.getDefaultPin(toPageNode);
        //I need to remove extension so it matches the DataNode's pins.
        scene.setEdgeTarget(navCaseNode, targetPin);
    }


    private static final String PATH_TOOLBAR_FOLDER = "PageFlowEditor/Toolbars"; // NOI18N

    /**
     *
     * @return
     */
    public JComponent getToolbarRepresentation() {

        //        PageFlowUtilities pfu = PageFlowUtilities.getInstance();
        // TODO -- Look at NbEditorToolBar in the editor - it does stuff
        // with the UI to get better Aqua and Linux toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));

        toolbar.addSeparator();
        PageFlowUtilities utilities = PageFlowUtilities.getInstance(this);
        toolbar.add(utilities.createScopeComboBox());

        toolbar.add(utilities.createLayoutButton());

        return toolbar;
    }


    private static final String PATH_PALETTE_FOLDER = "PageFlowEditor/Palette"; // NOI18N

    /**
     * Get's the Palette Controller for the related Palette.
     * @return the Palette Controller.
     */
    public PaletteController getPaletteController() {
        try {
            return PaletteFactory.createPalette(PATH_PALETTE_FOLDER, new PaletteActions() {

                public Action[] getCustomCategoryActions(Lookup lookup) {
                    return new Action[0];
                }

                public Action[] getCustomItemActions(Lookup lookup) {
                    return new Action[0];
                }

                public Action[] getCustomPaletteActions() {
                    return new Action[0];
                }

                public Action[] getImportActions() {
                    return new Action[0];
                }

                public Action getPreferredAction(Lookup lookup) {
                    return null; //TODO
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public ExplorerManager getExplorerManager() {
        return explorer;
    }

    private ExplorerManager explorer;

    @Override
    public void addNotify() {
        super.addNotify();
        explorer = ExplorerManager.find(this);
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    public void requestFocus() {
        super.requestFocus();
        view.requestFocus();
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }


    /**
     * Remove the Edge from the scene.
     * @param node
     */
    public void removeEdge(NavigationCaseEdge node) {
        if (scene.getEdges().contains(node)) {
            scene.removeEdge(node);
        }
    }

    public void removeNodeWithEdges(Page node) {
        //        scene.removeNode(node);
        if (scene.getNodes().contains(node)) {
            /* In some cases the node will already be deleted by a side effect of deleting another node.
             * This is primarily in the FacesConfig view or an abstract Node in the project view.
             */
            scene.removeNodeWithEdges(node);
        }
    }

    public void resetNodeWidget(Page pageNode, boolean contentItemsChanged) {

        if (pageNode == null) {
            throw new RuntimeException("PageFlowEditor: Cannot set node to null");
        }

        //Reset the Node Name
        VMDNodeWidget nodeWidget = (VMDNodeWidget) scene.findWidget(pageNode);

        //Do this because sometimes the node display name is the object display name.
        pageNode.updateNode_HACK();
        //        nodeWidget.setNodeName(node.getDisplayName());
        if (nodeWidget != null) {
            nodeWidget.setNodeProperties(pageNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16), pageNode.getDisplayName(), null, null);
            if (contentItemsChanged) {
                redrawPinsAndEdges(pageNode);
            }
        } else {
            validateGraph();
            System.err.println("PageFlowCreationStack: " + pfc.PageFlowCreationStack);
            System.err.println("PageFlowDestroyStack: " + pfc.PageFlowDestroyStack);
            pfc.PageFlowCreationStack.clear();
            pfc.PageFlowDestroyStack.clear();
            System.err.println("PageNode: " + pageNode);
            //            System.err.println("Node Widget is null in scene for: " + pageNode.getDisplayName());
            System.err.println("Here are the scene nodes: " + scene.getNodes());
            //            Thread.dumpStack();
        }
    }

    private final void redrawPinsAndEdges(Page pageNode) {
        /* Gather the Edges */
        Collection<NavigationCaseEdge> redrawCaseNodes = new ArrayList<NavigationCaseEdge>();
        Collection<Pin> pinNodes = new ArrayList<Pin>(scene.getPins());
        for (Pin pin : pinNodes) {
            if (pin.getPage() == pageNode) {
                assert pin.getPage().getDisplayName().equals(pageNode.getDisplayName());

                Collection<NavigationCaseEdge> caseNodes = scene.findPinEdges(pin, true, false);
                redrawCaseNodes.addAll(caseNodes);
                if (!pin.isDefault()) {
                    scene.removePin(pin);
                }
            }
        }

        if (pageNode.isDataNode()) {
            // This is already done.  pageNode.updateContentModel();
            //This will re-add the pins.
            setupPinsInNode(pageNode);
        }

        for (NavigationCaseEdge caseNode : redrawCaseNodes) {
            setEdgeSourcePin(caseNode, pageNode);
        }
    }

    public Collection<NavigationCaseEdge> getNodeEdges(Page node) {
        Collection<NavigationCaseEdge> navCases = scene.getEdges();
        Collection<NavigationCaseEdge> myNavCases = new HashSet<NavigationCaseEdge>();

        String fromViewId = node.getDisplayName();
        for (NavigationCaseEdge navCaseNode : navCases) {
            String strToViewId = navCaseNode.getToViewId();
            String strFromViewId = navCaseNode.getFromViewId();
            if ((strToViewId != null && strToViewId.equals(fromViewId)) || (strFromViewId != null && strFromViewId.equals(fromViewId))) {
                myNavCases.add(navCaseNode);
            }
        }
        return myNavCases;
    }

    //    private File navDataFile = null;
    public static final FileObject getStorageFile(FileObject configFile) {
        //        FileObject webFolder = getWebFolder(configFile);
        Project p = FileOwnerQuery.getOwner(configFile);
        FileObject projectDirectory = p.getProjectDirectory();
        FileObject nbprojectFolder = projectDirectory.getFileObject("nbproject", null);
        if (nbprojectFolder == null) {
            LOG.warning("Unable to create access the follow folder: " + nbprojectFolder);
            System.err.println("Unable to create access the follow folder:" + nbprojectFolder);
            return null;
        }



        //        FileObject nbprojectFolder = webFolder.getParent().getFileObject("nbproject", null);
        String filename = configFile.getName() + ".NavData";
        FileObject storageFile = nbprojectFolder.getFileObject(filename);
        if (storageFile == null) {
            try {
                storageFile = nbprojectFolder.createData(filename);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return storageFile;
    }

    public static final FileObject getWebFolder(FileObject configFile) {
        WebModule webModule = WebModule.getWebModule(configFile);

        if (webModule == null) {
            LOG.warning("This configuration file is not part of a webModule: " + configFile);
            System.err.println("This configuration file is not part of a webModule: " + configFile);
            return null;
        }
        FileObject webFolder = webModule.getDocumentBase();
        return webFolder;
    }

    public void serializeNodeLocations(FileObject navDataFile) {
        if (navDataFile != null) {
            saveLocations();
            SceneSerializer.serialize(sceneData, navDataFile);
        }
    }

    public void deserializeNodeLocation(FileObject navDataFile) {
        if (navDataFile != null && navDataFile.isValid() && navDataFile.getSize() > 0) {
            SceneSerializer.deserialize(sceneData, navDataFile);
        }
    }

    @Override
    protected String preferredID() {
        return "PageFlowEditor";
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    LayoutUtility.LayoutType lastUsedLayout = LayoutUtility.LayoutType.GRID_GRAPH;

    public void layoutNodes() {
        LayoutUtility.LayoutType useLayout = null;
        if (lastUsedLayout.equals(LayoutUtility.LayoutType.GRID_GRAPH)) {
            useLayout = LayoutUtility.LayoutType.FREE_PLACES_NODES;
        } else {
            useLayout = LayoutUtility.LayoutType.GRID_GRAPH;
        }

        LayoutUtility.performLayout(scene, useLayout);
        lastUsedLayout = useLayout;
    }

    protected void startBackgroundProcess() {
        executor.prestartAllCoreThreads();
    }
}
