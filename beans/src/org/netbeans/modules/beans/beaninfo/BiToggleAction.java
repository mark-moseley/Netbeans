/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
* Toggles selection.
*
* @author   Petr Hrebejk
*/
public class BiToggleAction extends NodeAction  {

    static final long serialVersionUID =3773842179168178798L;
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 1391479985940417455L;

    //private static final Class[] cookieClasses = new Class[] { BiFeatureNode.class };



    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (GenerateBeanInfoAction.class).getString ("CTL_TOGGLE_MenuItem");
    }


    /*
     public Class[] cookieClasses() {
       return cookieClasses;
     }
     
    */
    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return null;
        //return "/org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
    }

    /*
    public int mode () {
      return CookieAction.MODE_ALL;
}
    */

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (BiToggleAction.class);
    }

    protected boolean enable( Node[] activatedNodes ) {
        activatedNodes = BiPanel.getSelectedNodes();

        if ( activatedNodes.length < 1 )
            return false;

        for(int i = 0; i <activatedNodes.length; i++ ) {
            if( activatedNodes[i].getCookie( BiFeatureNode.class ) == null )
                return false;
            BiFeature biFeature = ((BiFeatureNode)activatedNodes[i].getCookie( BiFeatureNode.class )).getBiFeature();
            BiAnalyser biAnalyser = ((BiFeatureNode)activatedNodes[i].getCookie( BiFeatureNode.class )).getBiAnalyser();
            if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() )
                return false;
            if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() )
                return false;
            if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() )
                return false;
        }
        return true;
    }


    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {

        nodes = BiPanel.getSelectedNodes();

        if ( nodes.length < 1 )
            return;

        for(int i = 0; i < nodes.length; i++ ) {
            if( nodes[i].getCookie( BiFeatureNode.class ) != null )
                ((BiFeatureNode)nodes[i].getCookie( BiFeatureNode.class )).toggleSelection();
        }

    }

}
