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
import java.util.Set;
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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.navigation.JSFPageFlowMultiviewDescriptor.PageFlowElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.LayoutUtility;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneData;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.graph.SceneSerializer;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
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
 * PageFlowView is the TopComponent that setups the controller and the view.  It also does
 * the necessary setting of activated nodes, focus setting, etc.
 * @author Joelle Lam
 */
public class PageFlowView extends TopComponent implements Lookup.Provider {

    private JSFConfigEditorContext context;
    private PageFlowScene scene;
    private PageFlowController pfc;
    private PageFlowSceneData sceneData;
    private BlockingQueue<Runnable> runnables = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executor;
    private static final Logger LOG = Logger.getLogger("org.netbeans.web.jsf.navigation");
    private static final String ACN_PAGEVIEW_TC = NbBundle.getMessage(PageFlowView.class, "ACN_PageView_TC");
    private static final String ACDS_PAGEVIEW_TC = NbBundle.getMessage(PageFlowView.class, "ACDS_PageView_TC");

    PageFlowView(PageFlowElement multiview, JSFConfigEditorContext context) {
        setMultiview(multiview);
        this.context = context;
        initializeScene(); /* setScene is called inside this method */
        pfc = new PageFlowController(context, this);
        sceneData = new PageFlowSceneData(PageFlowToolbarUtilities.getInstance(this));

        deserializeNodeLocation(getStorageFile(context.getFacesConfigFile()));
        pfc.setupGraphNoSaveData(); /* I don't want to override the loaded locations with empy sceneData */
        LOG.fine("Initializing Page Flow SetupGraph");

        setFocusable(true);

        executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, runnables);
        LOG.finest("Create Executor Thread");

        getAccessibleContext().setAccessibleDescription(ACDS_PAGEVIEW_TC);
        getAccessibleContext().setAccessibleName(ACN_PAGEVIEW_TC);
    }

    public void requestMultiViewActive() {
        getMultiview().getMultiViewCallback().requestActive();
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

    @Override
    public Lookup getLookup() {
        Lookup lookup = lookupWRef.get();
        DataObject jsfConfigDataObject = null;

        if (lookup == null) {
            Lookup superLookup = super.getLookup();
            try {
                //Necessary for close project work propertly.
                jsfConfigDataObject = DataObject.find(context.getFacesConfigFile());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (jsfConfigDataObject != null) {
                lookup = new ProxyLookup(new Lookup[]{superLookup, Lookups.fixed(new Object[]{getScene(), jsfConfigDataObject})});
            } else {
                /* Temporarily Removing Palette */
                //            PaletteController paletteController = getPaletteController();
                //            if (paletteController == null) {
                lookup = new ProxyLookup(new Lookup[]{superLookup, Lookups.fixed(new Object[]{getScene()})});
            //            } else {
                //                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] { paletteController})});
                //            }
            }
            lookupWRef = new WeakReference<Lookup>(lookup);
        }

        return lookup;
    }

    /**
     * Unregister all the listeners.  See "registerListeners()".
     **/
    public void unregstierListeners() {
        if (pfc != null) {
            pfc.unregisterListeners();
        }
    }

    /**
     * Regsiter all the Page Flow Controller Listeners. Ie FileSystem, FacesModel, etc
     **/
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

        setScene(new PageFlowScene(this));

        getScene().setAccessibleContext(this.getAccessibleContext());

        JScrollPane pane = new JScrollPane(getScene().createView());
        pane.setVisible(true);

        add(pane, BorderLayout.CENTER);

        setDefaultActivatedNode();

        return getScene();
    }

    public void destroyScene() {
        clearGraph();
        getScene().destoryPageFlowScene();
        setScene(null);
        sceneData = null;
        runnables.clear();
        context = null;
        pfc.destroy();
        pfc = null;
    }

    /**
     * Set the default actived node to faces config node.  The default activated
     * node is always teh faces config file.
     */
    public void setDefaultActivatedNode() {
        try {
            Node node = new DefaultDataNode(DataObject.find(context.getFacesConfigFile()));
            setActivatedNodes(new Node[]{node});
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            /* Trying to track down #112243 */
            LOG.fine("WARNING: Unable to find the following DataObject: " + context.getFacesConfigFile());
            setActivatedNodes(new Node[]{});
        }
    }

    
    public int sceneAssgn = 0;
    public PageFlowScene getScene() {
        return scene;
    }
    

    public void setScene(PageFlowScene scene) {
        sceneAssgn++;
        this.scene = scene;
    }

/* In order to prevent modifications of tab names when a page was selected, I needed to
     * create a DefaultDataNode (or filterNode).  Basically this is used to take look like
     * a DataNode but not be one exactly.
     **/
    private class DefaultDataNode extends FilterNode {

        Node srcFolderNode = null;

        public DefaultDataNode(DataObject dataObject) {
            this(dataObject.getNodeDelegate());
            Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            /* Let's only worry about this if it is actually part of a project.*/
            if (p != null) {
                //FileObject projectDirectory = p.getProjectDirectory();
                Sources sources = ProjectUtils.getSources(p);
                SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                FileObject srcFolder;

                try {
                    if (groups != null && groups.length > 0) {
                        srcFolder = groups[0].getRootFolder();
                    } else {
                        srcFolder = dataObject.getFolder().getPrimaryFile();
                    }
                    srcFolderNode = DataObject.find(srcFolder).getNodeDelegate();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
     * This call draws a warning in the scene saying that the faces-config can not be parsed.
     */
    public void warnUserMalFormedFacesConfig() {
        //        clearGraph();
        getScene().createMalFormedWidget();
    }

    /*
     * Once the faces-config can be parsed again remove the warning from the scene.
     * See "warnUserMalFormedFacesConfig()
     **/
    public void removeUserMalFormedFacesConfig() {
        getScene().removeMalFormedWidget();
    }

    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    /**
     * Remove all the nodes from the graph.  I especially use this when redrawing
     * the page in a new scope or if the faces-config file can no longer be parsed.
     **/
    public void clearGraph() {
        //Workaround: Temporarily Wrapping Collection because of  http://www.netbeans.org/issues/show_bug.cgi?id=97496
        Collection<Page> pages = new HashSet<Page>(getScene().getNodes());
        for (Page page : pages) {
            getScene().removeNodeWithEdges(page);
            page.destroy2();
        }
        getScene().validate();
    }

    

    /**
     * Validating the graph is necessary to push a series of modifications to view.
     * Please see the graph library API for details.
     **/
    public void validateGraph() {
        //        scene.layoutScene();
        //        System.out.println("Validating Graph: ");
        //        System.out.println("Nodes: " + scene.getNodes());
        //        System.out.println("Edges: "+ scene.getEdges());
        //        System.out.println("Pins: " + scene.getPins());
        getScene().validate();
    }

    /*
     * Save the locations for all the pages currently in the scene.
     */
    public void saveLocations() {
        sceneData.saveCurrentSceneData(getScene());
    }

    /*
     * Save the location of just the given page.
     * This is necessary because I save locations by page name.  If the page name
     * is updated, we must save the location under the new page name.
     */
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


        final VMDNodeWidget widget = (VMDNodeWidget) getScene().addNode(pageNode);
        //        widget.setNodeProperties(null /*IMAGE_LIST*/, pageName, type, glyphs);
        widget.setNodeProperties(pageNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16), pageName, type, glyphs);
        PageFlowSceneData.PageData data = sceneData.getPageData(pageName);
        if (data != null) {
            widget.setPreferredLocation(data.getPoint());
            widget.setMinimized(data.isMinimized());
        }

        //        widget.getAccessibleContext().setAccessibleDescription(ACDS_PAGE);
//        widget.getAccessibleContext().setAccessibleDescription(ACN_PAGE);
        getScene().addPin(pageNode, new Pin(pageNode));
        runPinSetup(pageNode, widget);
        selectPageFlowSceneElement(pageNode);
        return widget;
    }

    private void selectPageFlowSceneElement(PageFlowSceneElement element) {
        Set<PageFlowSceneElement> selectedSet = new HashSet<PageFlowSceneElement>(1);
        selectedSet.add(element);
        getScene().setSelectedObjects(selectedSet);
    }

    /* This method was put into place to gather all the pin data for a given page.
     * Although it may seem non-sensical for a non-jsp page, a vwp-page may need
     * more processing time to determine it's compoenents.  To prevent further delay
     * in the drawing of the Page Flow Editor, I have added an executor in which
     * it will run and get the conent items in the background.
     * For the background process to start startBackgroundPinAddingProcess() must be called.
     * If you no longer need the page to complete loading, you can call the clear equivalent.
     **/
    private void runPinSetup(Page page, VMDNodeWidget widget) {
        final WeakReference<Page> pageRef = new WeakReference<Page>(page);
        final WeakReference<VMDNodeWidget> widgetRef = new WeakReference<VMDNodeWidget>(widget);
        LOG.entering(PageFlowView.class.getName(), "runPinSetup");
        final LabelWidget loadingWidget = new LabelWidget(getScene(), "Loading...");
        widget.addChild(loadingWidget);
        runnables.add(new Runnable() {

                    public void run() {
                        /* This is called in redrawPins and edges setupPinsInNode(pageNode);*/
                        /* Need to do updateNodeWidgetActions after setupPinInNode because this is when the model is set. */
                        LOG.finest("    PFE: Inside Thread: " + java.util.Calendar.getInstance().getTime());
                        //Page page = pageRef.get();
                        if( pageRef.get() == null || widgetRef.get() == null ){
                            LOG.finest("    PFE: runPinSetup will not completed because the page is now null.  It may have been removed to reset graph.");
                            return;
                        }
                        if (!pageRef.get().isDataNode()) {
                            EventQueue.invokeLater(new Runnable() {

                                        public void run() {
                                            if (getScene() != null) {
                                                widgetRef.get().removeChild(loadingWidget);
                                                getScene().validate();
                                            }
                                        }
                                    });
                            return;
                        }
                        final java.util.Collection<org.netbeans.modules.web.jsf.navigation.Pin> newPinNodes = pageRef.get().getPinNodes();

                        LOG.finest("    PFE: Completed Nodes Setup: " + java.util.Calendar.getInstance().getTime());

                        try {
                            EventQueue.invokeAndWait(new java.lang.Runnable() {

                                        public void run() {

                                            LOG.finest("    PFE: Starting Redraw: " + java.util.Calendar.getInstance().getTime());
                                            Collection<NavigationCaseEdge> redrawCaseNodes = new ArrayList<NavigationCaseEdge>();
                                            Collection<Pin> pinNodes = new ArrayList<Pin>(getScene().getPins());

                                            widgetRef.get().removeChild(loadingWidget);
                                            for (Pin pin : pinNodes) {
                                                if (pin.getPage() == pageRef.get()) {
                                                    assert pin.getPage().getDisplayName().equals(pageRef.get().getDisplayName());
                                                    java.util.Collection<NavigationCaseEdge> caseNodes = getScene().findPinEdges(pin, true, false);
                                                    redrawCaseNodes.addAll(caseNodes);
                                                    if (!pin.isDefault()) {
                                                        getScene().removePin(pin);
                                                    }
                                                }
                                            }
                                            if (newPinNodes.size() > 0) {
                                                for (org.netbeans.modules.web.jsf.navigation.Pin pinNode : newPinNodes) {
                                                    createPin(pageRef.get(), pinNode);
                                                }
                                            }
                                            for (org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge caseNode : redrawCaseNodes) {
                                                setEdgeSourcePin(caseNode, pageRef.get());
                                            }
                                            getScene().updateNodeWidgetActions(pageRef.get());
                                            getScene().validate();
                                            LOG.finest("    PFE: Ending Redraw: " + java.util.Calendar.getInstance().getTime());
                                        }
                                    });
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                    if (getScene() == null )  {
                                /* It is okay suppress this exception because it is expected if the scene is deleted (closed)
                         * before the page has finished loading. 
                         */
                                LOG.finer("Scene is has been closed before page has finished loading.:" + ex);
                            } else {
                                Exceptions.printStackTrace(ex);
                            }
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
        VMDPinWidget widget = null;

        /* Make sure scene still has this page. */
        if (pageNode != null && getScene().isNode(pageNode)) {
            widget = (VMDPinWidget) getScene().addPin(pageNode, pinNode);
        }
        //        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, pin);
        //        if( navComp != null ){
        //            widget.setProperties(navComp, Arrays.asList(navComp.getBufferedIcon()));
        //        }
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

        VMDConnectionWidget connectionWidget = (VMDConnectionWidget) getScene().addEdge(navCaseEdge);
        setEdgeSourcePin(navCaseEdge, fromPageNode);
        setEdgeTargePin(navCaseEdge, toPageNode);

        selectPageFlowSceneElement(navCaseEdge);
    //connectionWidget.getAccessibleContext().setAccessibleName(ACN_EDGE);
        //connectionWidget.getAccessibleContext().setAccessibleDescription(ACDS_PAGE);
    }

    public void renameEdgeWidget(NavigationCaseEdge edge, String newName, String oldName) {
        getScene().renameEdgeWidget(edge, newName, oldName);
    }

    /*
     * Figure out what the source of the case is.  This is necessary if we are renaming
     * a case.  It must also modify the case string for a non-default pin.
     * @return source pin
     */
    public Pin getEdgeSourcePin(NavigationCaseEdge navCase) {
        return getScene().getEdgeSource(navCase);
    }

    /*
     * Sets the source or "from" pin.  This can either be a pages default pin (in otherwords
     * it is simply navigable from that page) or from another pin (ie button).
     */
    private void setEdgeSourcePin(NavigationCaseEdge navCaseNode, Page fromPageNode) {
        Pin sourcePin = getScene().getDefaultPin(fromPageNode);
        Collection<Pin> pinNodes = getScene().getPins();
        for (Pin pin : pinNodes) {
            if (pin.getFromOutcome() != null && fromPageNode == pin.getPage() && pin.getFromOutcome().equals(navCaseNode.getFromOuctome())) {
                sourcePin = pin;
                /* Remove any old navigation case nodes coming from this source */
                Collection<NavigationCaseEdge> oldNavCaseNodes = getScene().findPinEdges(sourcePin, true, false);
                for (NavigationCaseEdge oldNavCaseNode : oldNavCaseNodes) {
                    getScene().setEdgeSource(oldNavCaseNode, getScene().getDefaultPin(fromPageNode));
                }
            }
        }

        getScene().setEdgeSource(navCaseNode, sourcePin);
    }

    /*
     * Set the target or the "to page" for a given pin
     **/
    private void setEdgeTargePin(NavigationCaseEdge navCaseNode, Page toPageNode) {
        Pin targetPin = getScene().getDefaultPin(toPageNode);
        //I need to remove extension so it matches the DataNode's pins.
        getScene().setEdgeTarget(navCaseNode, targetPin);
    }
    private static final String PATH_TOOLBAR_FOLDER = "PageFlowEditor/Toolbars"; // NOI18N

    /**
     * Gives the JSFPageMultiviewViewDescriptor (MultiView Component)the needed
     * toolbar.
     * @return the JComponent of the Toolbar
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
        PageFlowToolbarUtilities utilities = PageFlowToolbarUtilities.getInstance(this);
        toolbar.add(utilities.createScopeComboBox());
        toolbar.addSeparator();
        toolbar.add(utilities.createLayoutButton());

        return toolbar;
    }
    private static final String PATH_PALETTE_FOLDER = "PageFlowEditor/Palette"; // NOI18N

    /**
     * Get's the Palette Controller for the related Palette. This allows for the addition
     * of a palette.
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


    @SuppressWarnings(value = "deprecation")
    @Override
    public void requestFocus() {
        super.requestFocus();
        getScene().getView().requestFocus();
    }

    @SuppressWarnings(value = "deprecation")
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return getScene().getView().requestFocusInWindow();
    }

    /**
     * Remove the Edge from the scene.
     * @param node
     */
    public void removeEdge(NavigationCaseEdge node) {
        if (getScene().getEdges().contains(node)) {
            getScene().removeEdge(node);
        }
    }

    /*
     * Removes a node from a scene with it's edges.  This is useful when a page 
     * is deleted from the faces-config file.
     */
    public void removeNodeWithEdges(Page node) {
        //        scene.removeNode(node);
        if (getScene().getNodes().contains(node)) {
            /* In some cases the node will already be deleted by a side effect of deleting another node.
             * This is primarily in the FacesConfig view or an abstract Node in the project view.
             */
            getScene().removeNodeWithEdges(node);
        }
    }

    /*
     * Reset a Node Widget basically redraws a page gathering the current information
     * for a given page.  This is useful when a page has been renamed.  A flag is
     * also passed if it is suspected the page content items have been modified.
     * If this is suspected, it will then call redrawPinsAndEdges.
     **/
    public void resetNodeWidget(Page pageNode, boolean contentItemsChanged) {

        if (pageNode == null) {
            throw new RuntimeException("PageFlowEditor: Cannot set node to null");
        }

        //Reset the Node Name
        VMDNodeWidget nodeWidget = (VMDNodeWidget) getScene().findWidget(pageNode);

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
            System.err.println("Here are the scene nodes: " + getScene().getNodes());
        //            Thread.dumpStack();
        }
    }

    /*
     * If a page is updated it will general call this method to redraw both pins
     * and edges.  Although it is obvious why we would update pins if a pin a page
     * is modified ( a pin may have been added,removed,etc), it we also need to
     * redraw the edges.
     * Redrawing pin edges is necessary when the case name has been modified.
     * Generally this method is only called by "resetNodeWidget()".
     */
    private final void redrawPinsAndEdges(Page pageNode) {
        /* Gather the Edges */
        Collection<NavigationCaseEdge> redrawCaseNodes = new ArrayList<NavigationCaseEdge>();
        Collection<Pin> pinNodes = new ArrayList<Pin>(getScene().getPins());
        for (Pin pin : pinNodes) {
            if (pin.getPage() == pageNode) {
                assert pin.getPage().getDisplayName().equals(pageNode.getDisplayName());

                Collection<NavigationCaseEdge> caseNodes = getScene().findPinEdges(pin, true, false);
                redrawCaseNodes.addAll(caseNodes);
                if (!pin.isDefault()) {
                    getScene().removePin(pin);
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

    /*
     * Get all the edges for a given node.
     * @returns the collection of edge objects.
     **/
    public Collection<NavigationCaseEdge> getNodeEdges(Page node) {
        Collection<NavigationCaseEdge> navCases = getScene().getEdges();
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

    /*
     * Solve for the file in which we should store serialization information.
     **/
    public static final FileObject getStorageFile(FileObject configFile) {
        //        FileObject webFolder = getWebFolder(configFile);
        Project p = FileOwnerQuery.getOwner(configFile);
        if (p == null) {
            LOG.warning("File does not exist inside a project.  Can't solve getStorageFile().");
            System.err.println("File does not exist inside a project.  Can't solve getStorageFile().");
            return null;
        }
        FileObject projectDirectory = p.getProjectDirectory();
        FileObject nbprojectFolder = projectDirectory.getFileObject("nbproject", null);
        if (nbprojectFolder == null) {
            LOG.warning("Unable to create access the follow folder: " + nbprojectFolder);
            System.err.println("Unable to create access the follow folder:" + nbprojectFolder);
            return null;
        }

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

    /*
     * Figures out the current web folder
     * @return the project web folder.
     */
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

    /* Use to keep the node locations for the next time the Page Flow Editor is
     * opened.
     */
    public void serializeNodeLocations(FileObject navDataFile) {
        if (navDataFile != null) {
            saveLocations();
            SceneSerializer.serialize(sceneData, navDataFile);
        }
    }

    /* Takes the storage file and grabs the various locations and the last used
     * scope. It then sets the node information.
     */
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
    /* Keep the last layout used so you can toggle through them with the layout button. */
    LayoutUtility.LayoutType lastUsedLayout = LayoutUtility.LayoutType.GRID_GRAPH;

    /*
     * Method use to reset the layout of the various pages
     */
    public void layoutNodes() {
        LayoutUtility.LayoutType useLayout = null;


        switch (lastUsedLayout) {
            case GRID_GRAPH:
                useLayout = LayoutUtility.LayoutType.FREE_PLACES_NODES;
                break;
            case FREE_PLACES_NODES:
                //                useLayout = LayoutUtility.LayoutType.TREE_GRAPH;
//                break;
//            case TREE_GRAPH:
                useLayout = LayoutUtility.LayoutType.GRID_GRAPH;
                break;
        }

        LayoutUtility.performLayout(getScene(),useLayout);
        lastUsedLayout = useLayout;
    }

    /*
     * Start the background process for loading on the inner page (pin)
     * information.
     */
    protected void startBackgroundPinAddingProcess() {
        executor.prestartAllCoreThreads();
    }

    /*
     * Prevent any Runners that have not completed from running.  This is important
     * when the scope is being changed and we no longer want the pages to continue
     * loading.
     */
    protected void clearBackgroundPinAddingProcess() {
        executor.purge();
    }
    private WeakReference<PageFlowElement> multiviewRef;

    public PageFlowElement getMultiview() {
        PageFlowElement multiview = null;
        if (multiviewRef != null) {
            multiview = multiviewRef.get();
        }
        return multiview;
    }

    public void setMultiview(PageFlowElement multiview) {
        this.multiviewRef = new WeakReference<PageFlowElement>(multiview);
    }

    /* To be used for unit test purposes only. */
    static class PFVTestAccessor {
        static PageFlowScene getPageFlowScene(PageFlowView view) throws InterruptedException {
            if ( view.getScene() == null ){
                Thread.sleep(3000);
            }
            return view.getScene();
        }
    }

    
    
    
}