/*
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle lam
 */
public class PageFlowController {
    private PageFlowView view;
    private JSFConfigModel configModel;
    private Project project;
    private Collection<FileObject> webFiles;
    
    /** Creates a new instance of PageFlowController
     * @param context
     * @param view
     */
    public PageFlowController(JSFConfigEditorContext context, PageFlowView view ) {
        this.view = view;
        FileObject configFile = context.getFacesConfigFile();
        configModel = ConfigurationUtils.getConfigModel(configFile,true);
        project = FileOwnerQuery.getOwner(configFile);
        webFiles = getAllProjectRelevantFilesObjects();
        setupGraph();
        
        configModel.addPropertyChangeListener(new FacesModelPropertyChangeListener(view));
        
        configModel.addComponentListener(new ComponentListener(){
            public void valueChanged(ComponentEvent evt) {
                System.out.println("ValueChanged: " + evt);
            }
            
            public void childrenAdded(ComponentEvent evt) {
                System.out.println("childrenAdded: " + evt);
            }
            
            public void childrenDeleted(ComponentEvent evt) {
                System.out.println("\n\n\n\n\n\nchildrenDeleted: " + evt);
            }
        });
    }
    
    /**
     *
     * @param source
     * @param target
     * @param comp
     * @return
     */
    public NavigationCase createLink(AbstractNode source, AbstractNode target, String comp) {
        
        String sourceName = source.getDisplayName();
        int caseNum = 1;
        
        configModel.startTransaction();
        NavigationCase navCase = configModel.getFactory().createNavigationCase();
        
        navCase.setToViewId(target.getDisplayName());
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = getRuleWithFromViewID(facesConfig, source.getDisplayName());
        
        
        if (navRule == null) {
            navRule = configModel.getFactory().createNavigationRule();
            navRule.setFromViewId(source.getDisplayName());
            facesConfig.addNavigationRule(navRule);
        } else {
            caseNum = getNewCaseNumber(navRule);
        }
        
        navCase.setFromOutcome(CASE_STRING + Integer.toString(caseNum));
        navRule.addNavigationCase(navCase);
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        //        view.createEdge(navRule, navCase);
        return navCase;
        
    }
    
    private final static String CASE_STRING = "case";
    
    private int getNewCaseNumber( NavigationRule navRule ) {
        Collection<String> caseOutcomes = new HashSet<String>();
        List<NavigationCase> navCases = navRule.getNavigationCases();
        for( NavigationCase navCase : navCases ){
            caseOutcomes.add(navCase.getFromOutcome());
        }
        
        int caseNum = 1;
        while( true ){
            if( !caseOutcomes.contains(CASE_STRING + Integer.toString(caseNum)) ){
                return caseNum;
            }
            caseNum++;
        }
    }
    
    /**
     * @return the navigation rule.  This will be null if none was found
     **/
    private NavigationRule getRuleWithFromViewID(FacesConfig facesConfig, String fromViewId ){
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        for( NavigationRule rule : rules ){
            //            System.out.println("\nDo these match?");
            //            System.out.println(rule.getFromViewId() + " == " + fromViewId);
            if( rule.getFromViewId().equals(fromViewId) ){
                //                System.out.println("Match Found.");
                return rule;
            }
        }
        return null;
    }
    
    
    
    
    private Collection<FileObject> getAllProjectRelevantFilesObjects() {
        FileObject parentFolder = project.getProjectDirectory();
        FileObject webFileObject = parentFolder.getFileObject("web");
        Collection<FileObject> webFiles = getProjectJSPFileOjbects(webFileObject);
        System.out.println("Web Files: " + webFiles);
        return webFiles;
        
        //Add a listener to the Filesystem that listens to fileDelete, fileCreated, etc.
        //DataObject.find
        //        DataObject.find(parentFolder)
        
    }
    
    
    private Collection<FileObject> getProjectJSPFileOjbects(FileObject folder ) {
        Collection<FileObject> webFiles = new HashSet<FileObject>();
        FileObject[] childrenFiles = folder.getChildren();
        for( FileObject file : childrenFiles ){
            if( !file.isFolder() ) {
                if( file.getMIMEType().equals("text/x-jsp"))
                    webFiles.add(file);
            } else {
                webFiles.addAll(getProjectJSPFileOjbects(file));
            }
        }
        
        return webFiles;
    }
    
    /**
     * Setup The Graph
     * Should only be called by init();
     *
     **/
    public void setupGraph(){
        assert configModel !=null;
        assert project != null;
        assert webFiles != null;
        
        view.clearGraph();
        
        
        FacesConfig facesConfig = configModel.getRootComponent();
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        String currentScope = PageFlowUtilities.getInstance().getCurrentScope();
        Collection<String> pagesInConfig = getFacesConfigPageNames(rules);
        if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_FACESCONFIG)){
            createFacesConfigPageNodes(pagesInConfig);
        } else if (currentScope.equals(PageFlowUtilities.LBL_SCOPE_PROJECT)) {
            createAllProjectPageNodes(pagesInConfig);
        }
        createAllEdges(rules);
        //        view.layoutGraph();
        
        view.validateGraph();
        
    }
    
    private void createAllEdges( List<NavigationRule> rules ){
        for( NavigationRule rule : rules ) {
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                view.createEdge(rule, navCase);
            }
        }
    }
    
    
    private Collection<String> getFacesConfigPageNames(List<NavigationRule>rules) {
        // Get all the pages in the faces config.
        Collection<String> pages = new HashSet<String>();
        for( NavigationRule rule : rules ){
            String pageName = rule.getFromViewId();
            pages.add(pageName);
            List<NavigationCase> navCases = rule.getNavigationCases();
            for( NavigationCase navCase : navCases ){
                String toPage = navCase.getToViewId();
                pages.add(toPage);
            }
        }
        return pages;
    }
    
    private void createAllProjectPageNodes(Collection<String> pagesInConfig) {
        
        
        Collection<String> pages = pagesInConfig;
        
        //Create all pages in the project...
        for( FileObject webFile : webFiles ) {
            String webFileName = webFile.getNameExt();
            pages.remove(webFileName);
            DataNode node = null;
            try {
                node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
            } catch ( DataObjectNotFoundException ex ) {
                ex.printStackTrace();
            } catch( ClassCastException cce ){
                cce.printStackTrace();
            }
            view.createNode(node, null, null);
        }
        
        //Create any pages that don't actually exist but are defined specified by the config file.
        for( String pageName : pages ){
            AbstractNode node = new AbstractNode(Children.LEAF);
            
            node.setName(pageName);
            view.createNode(node, null, null);
        }
    }
    
    private void createFacesConfigPageNodes(Collection<String> pagesInConfig) {
        Collection<String> pages = pagesInConfig;
        
        for( String pageName : pages ) {
            boolean isFound = false;
            for( FileObject webFile : webFiles ) {
                String webFileName = webFile.getNameExt();
                if( webFileName.equals(pageName)) {
                    DataNode node = null;
                    try {
                        node = (DataNode)(DataObject.find(webFile)).getNodeDelegate();
                    } catch ( DataObjectNotFoundException ex ) {
                        ex.printStackTrace();
                    } catch( ClassCastException cce ){
                        cce.printStackTrace();
                    }
                    view.createNode(node, null, null);
                    isFound = true;
                }
            }
            if( !isFound ) {
                AbstractNode node = new AbstractNode(Children.LEAF);
                node.setName(pageName);
                view.createNode(node, null, null);
            }
            isFound = false;
        }
    }
    
    
    private  class FacesModelPropertyChangeListener implements PropertyChangeListener {
        public FacesModelPropertyChangeListener( PageFlowView view ){
            
        }
        
        public void propertyChange(PropertyChangeEvent ev) {
            if ( ev.getPropertyName() == "navigation-case"){
                NavigationCase myNavCase = (NavigationCase)ev.getNewValue();
                view.createEdge((NavigationRule)myNavCase.getParent(), myNavCase);
            } else if (ev.getPropertyName() == "navigation-rule" ) {
                NavigationRule myNavRule = (NavigationRule)ev.getNewValue();            
                //You can actually do nothing.
            } else if ( ev.getNewValue() == State.VALID ) {            
                setupGraph();
            } else if (ev.getNewValue() == State.NOT_WELL_FORMED ){
                view.warnUserMalFormedFacesConfig();
                System.out.println("NOT WELL FORMED!!!");
            }
//            System.out.println("New Value: " + ev.getNewValue());
//            System.out.println("Old Value: " + ev.getOldValue());
//            
//            System.out.println("PropertyName: " + ev.getPropertyName());
//            System.out.println("ID: " + ev.getPropagationId());
//            System.out.println("PropertyChangeListener");
            
        }
    }
    
//    public final class EmptyPageNode extends AbstractNode {
//        public EmptyPageNode(Children children) {
//            super(children);
//        }
//        public rename( String name ) {
//            this.setDisplayName(name);
//            configModel.startTransaction();
//            
//        }
//    }
    
    
}
