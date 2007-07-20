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
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;

/**
 *
 * @author joelle
 */
public class LayoutUtility<N, E, P> {
    
    final static Logger LOG = Logger.getLogger(LayoutUtility.class.getName());
    
    public static enum LayoutType  {
        GRID_GRAPH,  TREE_GRAPH, FREE_PLACES_NODES
    }
    
    static {
        LOG.setLevel(Level.ALL);
    }
    
    public  LayoutUtility() {
    }
    
    public static <N,E,P> void performLayout( GraphPinScene<N, E, P> graph, LayoutType type){
        switch( type ) {
        case GRID_GRAPH:
            GridGraphLayoutUtility.performLayout(graph);
            
            break;
        case TREE_GRAPH:
            /* Tree Graph Layout Utility was taken from Tree Graph Layout as is incomplete.*/
            TreeGraphLayoutUtility.performLayout(graph);
            break;
        case FREE_PLACES_NODES:
            //TreeGraphLayoutUtility.performLayout(graph); //Putting this in temporarily.
            /* Tree Graph Layout Utility was taken from Tree Graph Layout as is incomplete.*/
            if ( graph instanceof PageFlowScene)
                FreePlaceNodesLayouter.performLayout((PageFlowScene)graph);
            break;
        }
    }
}
