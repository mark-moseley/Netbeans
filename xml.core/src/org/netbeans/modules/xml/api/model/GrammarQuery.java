/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.api.model;

import java.util.Enumeration;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A query interface returning possible fenomens as given by document grammar.
 * <p>
 * It provides at specified <code>HintContext</code> following information:
 * <ul>
 *  <li>allowed element names and namespaces
 *  <li>allowed entity names
 *  <li>allowed notation names
 *  <li>allowed attribute names
 *  <li>allowed values of attribute values or element content
 * </ul>
 * This information are returned as <code>Enumeration</code>. Every member of
 * the enumeration represents one possible option. Empty enumeration signals
 * that no hint can be derived from grammar in given context.
 * <p>
 * Every option represents DOM <code>Node</code> that can have children. These children
 * represents mandatory content for given option. Providing them is optional.
 * On the other hand <code>EMPTY</code> elements may not provide any children.
 *
 * <h3>Context Representation</h3>
 * Query context is represented by a read-only DOM1 model Core and XML modules
 * (it may be replaced with DOM2).
 * 
 * <h3>Implementation Note:</h3>
 * <p>
 * DOM1 does describe only non-DTD part of document. 
 * <p>
 * Passed context may represent errorous XML document. The code must
 * take into account that it can get <code>null</code>s even on places
 * where it does not expect it. E.g. <code>getParentNode()</code> as
 * the DOM document can be constructed as a <b>best effort</b> one.
 * <p>
 * Also as the context may originate from a text editor it is better to 
 * rely on <code>getPreviousSibling</code> rather then <code>getNextSibling</code>
 * as user usaully types text from document begining.
 *
 * @author  Petr Kuzel
 * @deprecated Experimantal SPI
 */   
public interface GrammarQuery {
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param virtualElementCtx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    Enumeration queryElements(HintContext virtualElementCtx);

    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms. 
     * @param ownerElementCtx represents owner <code>Element</code> that will host result.
     * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    Enumeration queryAttributes(HintContext ownerElementCtx);

    /**
     * Return options for value at given context.
     * It could be also used for completing of value parts such as Ant or XSLT property names (how to trigger it?).
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param virtualTextCtx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    Enumeration queryValues(HintContext virtualTextCtx);

    /**
     * Return default value for given context.
     * @param virtualNodeCtx position for which default is queried
     * @return default value or <code>null</code>
     */
    GrammarResult queryDefault(HintContext virtualNodeCtx);

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @param prefix prefix filter
     * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    Enumeration queryEntities(String prefix);

    /**
     * Allow to get names of <b>declared notations</b>.
     * @param prefix prefix filter
     * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
     */    
    Enumeration queryNotations(String prefix);

    /**
     * Distinquieshes between empty enumaration types.
     * @return <code>true</code> there is no known result 
     *         <code>false</code> grammar does not allow here a result
     */
    boolean isAllowed(Enumeration en);
    
    
    
    // Candidates for separate interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Allows Grammars to supply customizer for the HintContext
     * @param ctx the hint context node
     * @return a Component which can be used to customize the context node
     */
    java.awt.Component getCustomizer(HintContext ctx);
    
    /**
     * Allows Grammars to supply customizer from the HintContext
     * @param ctx the hint context node
     * @return true if a customizer is available for this context
     */
    boolean hasCustomizer(HintContext ctx);

    /**
     * Allows Grammars to supply properties for the HintContext
     * @param ctx the hint context node
     * @return an array of properties for this context
     */
    org.openide.nodes.Node.Property[] getProperties(HintContext ctx);
}
