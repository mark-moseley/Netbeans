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

package org.netbeans.modules.javadoc.search;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import javax.swing.SwingUtilities;

/**
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class SearchDocAction extends CallableSystemAction {

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName () {
        return NbBundle.getBundle (SearchDocAction.class).getString ("CTL_SEARCH_MenuItem");
    }

    /** The action's icon location.
     * @return the action's icon location
     */
    protected String iconResource () {
        return "org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SearchDocAction.class);
    }

    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     * This default implementation calls the assigned actionPerformer if it
     * is not null otherwise the action is ignored.
     */
    public void performAction () {

        final IndexSearch indexSearch = IndexSearch.getDefault();

        String toFind = GetJavaWord.getCurrentJavaWord();

        if (toFind != null)
            indexSearch.setTextToFind( toFind );

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                indexSearch.open ();
            }
        });
    }
}
