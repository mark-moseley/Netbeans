/*
 * JSFTopComponent.java
 *
 * Created on February 8, 2007, 12:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.navigation.graph.NavigationGraphScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class PageFlowView  extends TopComponent{
    private JSFConfigEditorContext context;
    private PageFlowScene scene;
    private JSFConfigModel configModel;
    
    PageFlowView(JSFConfigEditorContext context){
        this.context = context;
        init();
    }
    
    /*
     * Initializes the Panel and the graph 
     **/
    private void init(){
        setLayout(new BorderLayout());
        configModel = ConfigurationUtils.getConfigModel(context.getFacesConfigFile(),true);
        
        scene = new PageFlowScene();
        add(scene.createView());
        
        try{
            Node node = DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
            setActivatedNodes(new Node[] { node });
        } catch (Exception e){}
        
        setupGraph();
        
    }
    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    private static final Image IMAGE_LIST = null; // NOI18N
    
    /* 
     * Setup The Graph
     * Should only be called by init();
     **/
    private void setupGraph(){
        assert configModel!=null;
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();        
        createAllPageNodes(rules);        
        createAllEdges(rules);       
        
        if(  scene instanceof PageFlowScene ) {
            ((PageFlowScene)scene).layoutScene();
        }
    }

    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                createEdge(scene, rule, navCase);
            }
        }
    }
    
    private void createAllPageNodes(List<NavigationRule> rules) {
        Collection<String> pages = new HashSet<String>();
        for( NavigationRule rule : rules ){
            String page = rule.getFromViewId();
            pages.add(page);
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                String toPage = navCase.getToViewId();
                pages.add(toPage);
            }
        }
        for( String page : pages ) {
            createNode((GraphPinScene)scene, IMAGE_LIST, page, null, null);
        }
    }

    
    
    private VMDNodeWidget createNode(GraphPinScene graphScene, Image image, String page, String type, List<Image> glyphs) {
        VMDNodeWidget widget = (VMDNodeWidget) graphScene.addNode(page);
        widget.setNodeProperties(image, page, type, glyphs);
        graphScene.addPin(page, page +"pin");
        
        return widget;
    }
    
    private VMDPinWidget createPin(GraphPinScene graphScene, String page, String navComp) {
        //        Pin pin = new Pin(page, navComp);
        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, navComp);
        //        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, pin);
        //        if( navComp != null ){
        //            widget.setProperties(navComp, Arrays.asList(navComp.getBufferedIcon()));
        //        }
        return widget;
    }
    
    private void createEdge(GraphPinScene graphScene, NavigationRule rule, NavigationCase navCase) {
        
        String toPage = navCase.getToViewId();
        String caseName = navCase.getFromOutcome();
        String action = navCase.getFromAction();
        String fromPage = rule.getFromViewId();
        
        ConnectionWidget widget = (ConnectionWidget)graphScene.addEdge(navCase);
        
        LabelWidget label = new LabelWidget(graphScene, caseName);
        label.setOpaque(true);
        widget.addChild(label);
        widget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
        
        //        graphScene.setEdgeSource(navCase, label);
        graphScene.setEdgeSource(navCase, fromPage+"pin");
        graphScene.setEdgeTarget(navCase, toPage+"pin");
        
//        Collection<String> pins = graphScene.getPins();
//        String targetPin = null;
//        String sourcePin = null;
//        for (String pin : pins ){
//            if (pin.equals(toPage)) {
//                sourcePin = pin;
//                if( targetPin != null ) {
//                    break;
//                } else {
//                    continue;
//                }
//            } else if (pin.equals(fromPage)) {
//                targetPin = fromPage;
//                if( sourcePin != null ) {
//                    break;
//                } else {
//                    continue;
//                }
//            }
//        }
//        
//        graphScene.setEdgeTarget(navCase, targetPin);
//        graphScene.setEdgeSource(navCase, sourcePin);
        
        
    }
    
    
    //    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    //        out.writeObject(context);
    //    }
    //
    //    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    //        Object object = in.readObject();
    //        if (! (object instanceof JSFConfigEditorContext))
    //            throw new ClassNotFoundException("JSFConfigEditorContext expected but not found");
    //        context = (JSFConfigEditorContext) object;
    //    }
    
    //    public int getPersistenceType() {
    //        return TopComponent.PERSISTENCE_NEVER;
    //    }
    
    
}
