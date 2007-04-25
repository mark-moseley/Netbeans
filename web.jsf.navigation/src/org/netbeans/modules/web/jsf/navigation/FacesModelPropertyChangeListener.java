/*
 * FacesModelPropertyChangeListener.java
 *
 * Created on April 17, 2007, 6:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class FacesModelPropertyChangeListener implements PropertyChangeListener {
    public PageFlowController pfc;
    public PageFlowView view;
    public boolean refactoringIsLikely = false;
    public FacesModelPropertyChangeListener( PageFlowController pfc ){
        this.pfc = pfc;
        view = pfc.getView();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        //        String oldManagedBeanInfo = null;
        //        String newManagedBeanInfo = null;
        //        boolean managedBeanClassModified = false;
        if( ev.getOldValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.removeUserMalFormedFacesConfig();  // Does clear graph take care of this?
                    pfc.setupGraph();
                }
            });
        } else if( ev.getPropertyName().equals("managed-bean-class") ) {
            /* Can I guarentee that Insync will call this and then managed-bean-name? */
            //            managedBeanClassModified = true;
            
        } else if ( ev.getPropertyName().equals("managed-bean-name") ) {
            
            //Use this to notice that refactoring may have happened.
            //            oldManagedBeanInfo = (String) ev.getOldValue();
            //            newManagedBeanInfo = (String) ev.getNewValue();
            
        } else if ( ev.getPropertyName() == "navigation-case") {
            final NavigationCase myNewCase = (NavigationCase)ev.getNewValue();  //Should also check if the old one is null.
            final NavigationCase myOldCase = (NavigationCase)ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    navigationCaseEventHandler(myNewCase, myOldCase);
                }
            });
            
        } else if (ev.getPropertyName() == "navigation-rule" ) {
            //You can actually do nothing.
            final NavigationRule myNewRule = (NavigationRule) ev.getNewValue();
            final NavigationRule myOldRule = (NavigationRule) ev.getOldValue();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    navigationRuleEventHandler(myNewRule, myOldRule);
                }
            });
            
        } else if ( ev.getNewValue() == State.NOT_SYNCED ) {
            // Do nothing.
        } else if (ev.getNewValue() == State.NOT_WELL_FORMED ){
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    view.clearGraph();
                    view.warnUserMalFormedFacesConfig();
                }
            });
        } else if (ev.getPropertyName() == "textContent" ){
            setupGraphInAWTThread();
        } else if ( ev.getPropertyName() == "from-view-id"  || ev.getPropertyName() == "to-view-id"){
            
            final String oldName = FacesModelUtility.getViewIdFiltiered( (String) ev.getOldValue() );
            final String newName = FacesModelUtility.getViewIdFiltiered( (String) ev.getNewValue() );
            
            /* This code is only need if refactor calls rename of file before renaming the faces-config.
            if ( managedBeanClassModified && oldManagedBeanInfo != null && newManagedBeanInfo != null ) {
                if( oldManagedBeanInfo.equals(oldName) && newManagedBeanInfo.equals(newName)){
                    refactoringIsLikely = true;
                }
            }
            managedBeanClassModified = false;
            oldManagedBeanInfo = null;
            newManagedBeanInfo = null;
             */
            
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    replaceFromViewIdToViewIdEventHandler(oldName, newName);
                    //                    replaceFromViewIdToViewIdEventHandler(oldName, newName, refactoringIsLikely);
                }
            });
            refactoringIsLikely = false;
        } else {
            //                System.out.println("Did not catch this event.: " + ev.getPropertyName());
            setupGraphInAWTThread();
        }
    }
    
    
    //    private final void replaceFromViewIdToViewIdEventHandler(String oldName, String newName, boolean possibleRefactor) {
    private final void replaceFromViewIdToViewIdEventHandler(String oldName, String newName) {
        /* Going to have to do this another day. */
        PageFlowNode oldPageNode = pfc.getPageName2Node(oldName);
        PageFlowNode newPageNode = pfc.getPageName2Node(newName);
        boolean isNewPageLinked = false;
        if( newPageNode != null && view.getNodeEdges(newPageNode).size() > 0 ){
            /* This tells me that the new page already exists.*/
            isNewPageLinked = true;
        }
        /* The below code is only necessary if Refactor calls rename on page before it modifies the file.  This is not the case right now so this never really gets executed
        if( possibleRefactor && !isNewPageLinked && oldPageNode != null && newPageNode != null && newPageNode.isDataNode()){
            // This means that we should replace the new node back to the old because refactoring has likely occured
            Node node = newPageNode.getWrappedNode();
            if ( node != null ) {
                oldPageNode.replaceWrappedNode(node);
                view.removeNodeWithEdges(newPageNode);
                pfc.removePageName2Node(newPageNode, true); // Use this instead of replace because I want it to destroy the old node
                pfc.putPageName2Node(newName, oldPageNode);
                view.resetNodeWidget(oldPageNode, true);
                return;
            }
        } */
        //DO I really need this isNewPageLinked?
        if ( oldPageNode != null && !pfc.isPageInFacesConfig(oldName) && !isNewPageLinked ) {
            FileObject fileObj = pfc.getWebFolder().getFileObject(newName);
            if ( fileObj != null && pfc.containsWebFile(fileObj) ){
                try                 {
                    Node delegate = DataObject.find(fileObj).getNodeDelegate();
                    oldPageNode.replaceWrappedNode(delegate);
                    view.resetNodeWidget(oldPageNode, true); /*** JUST PUT TRUE HERE AS A HOLDER */
                    view.validateGraph();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                pfc.changeToAbstractNode(oldPageNode, newName);
            }
        }else if ( oldPageNode == null && !pfc.isPageInFacesConfig(oldName)) {
            //This means that oldPage has already been removed.  Do nothing.
        } else {
            pfc.setupGraph();
        }
    }
    
    private final void navigationCaseEventHandler(NavigationCase myNewCase, NavigationCase myOldCase) {
        
        if( myNewCase != null ){
            NavigationCaseNode node = new NavigationCaseNode(view.getPageFlowController(), myNewCase);
            pfc.putCase2Node(myNewCase, node);//     case2Node.put(myNewCase, node);
            pfc.createEdge(node);
        }
        if ( myOldCase != null ){
            NavigationCaseNode caseNode = pfc.removeCase2Node(myOldCase);
            if( caseNode != null ) {
                view.removeEdge(caseNode);
                
                String toPage = caseNode.getToViewId();
                if( toPage != null ) {
                    PageFlowNode pageNode = pfc.getPageName2Node(toPage);
                    if( pageNode != null && !pfc.isPageInFacesConfig(toPage)){
                        if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                            view.removeNodeWithEdges(pageNode);
                            pfc.removePageName2Node(pageNode,true);
                            view.validateGraph();
                        }
                    }
                }
            }
        }
        view.validateGraph();
    }
    
    private final void navigationRuleEventHandler(NavigationRule myNewRule, NavigationRule myOldRule) {
        //This has side effects in PageFlowNode destroy.
        //Because it does not consistantly work, I can't account for reactions.
        if( myOldRule != null ){
            String fromPage = pfc.removeNavRule2String(myOldRule);
            
            if( fromPage != null ){
                PageFlowNode pageNode = pfc.getPageName2Node(fromPage);
                if( pageNode != null && !pfc.isPageInFacesConfig(fromPage)){
                    if( !pageNode.isDataNode() || PageFlowUtilities.getInstance().getCurrentScope() == PageFlowUtilities.LBL_SCOPE_FACESCONFIG){
                        view.removeNodeWithEdges(pageNode);
                        pfc.removePageName2Node(pageNode, true);
                        view.validateGraph();
                    }
                }
            }
        }
        if( myNewRule != null ){
//            pfc.putNavRule2String(myNewRule, myNewRule.getFromViewId());
            pfc.putNavRule2String(myNewRule, FacesModelUtility.getFromViewIdFiltered(myNewRule));
        }
    }
    
    private final void setupGraphInAWTThread() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                pfc.setupGraph();
            }
        });
    }
}
