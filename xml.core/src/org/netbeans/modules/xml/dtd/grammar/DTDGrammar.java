/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.dtd.grammar;

import java.util.*;
import javax.swing.Icon;

import org.w3c.dom.*;

import org.openide.ErrorManager;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * Rather simple query implemetation based on DTD grammar.
 * It is produced by {@link DTDParser}.
 * Hints given by this grammar do not guarantee that valid XML document is created.
 *
 * @author  Petr Kuzel
 */
public class DTDGrammar implements GrammarQuery {
    
    // element name keyed
    private Map elementDecls, attrDecls;
    
    // Map<elementName:String, model:String || model:ContentModel || null>
    // this map is filled asynchronously as it takes some time
    private Map contentModels;
    
    // Map<elementname + " " + attributename, List<String>>
    private Map attrEnumerations;
    
    // Map<elementname + " " + attributename, String>
    private Map defaultAttributeValues;
    
    private Set entities, notations;

    /** Set&lt;elementName:String> holding all emenets with <code>EMPTY</code> content model.*/
    private Set emptyElements;

    /** Creates new DTDGrammar */
    DTDGrammar(Map elementDecls, Map contentModels, Map attrDecls, Map attrDefs, Map enums, Set entities, Set notations, Set emptyElements) {
        this.elementDecls = elementDecls;
        this.attrDecls = attrDecls;
        this.entities = entities;
        this.notations = notations;
        this.attrEnumerations = enums;
        this.contentModels = contentModels;
        this.defaultAttributeValues = attrDefs;
        this.emptyElements = emptyElements;
    }

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public Enumeration queryEntities(String prefix) {
        if (entities == null) return org.openide.util.Enumerations.empty();
        
        LinkedList list = new LinkedList();
        Iterator it = entities.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                list.add (new MyEntityReference(next));
            }
        }

        // add well-know build-in entity names
        
        if ("lt".startsWith(prefix)) list.add(new MyEntityReference("lt"));
        if ("gt".startsWith(prefix)) list.add(new MyEntityReference("gt"));
        if ("apos".startsWith(prefix)) list.add(new MyEntityReference("apos"));
        if ("quot".startsWith(prefix)) list.add(new MyEntityReference("quot"));
        if ("amp".startsWith(prefix)) list.add(new MyEntityReference("amp"));
        
        return java.util.Collections.enumeration (list);
    }
    
    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms.
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return list of <code>CompletionResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public Enumeration queryAttributes(HintContext ctx) {
        if (attrDecls == null) return org.openide.util.Enumerations.empty();
        
        Element el = null;
        // Support two versions of GrammarQuery contract
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            el = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
            el = (Element) ctx;
        }
        if (el == null) return org.openide.util.Enumerations.empty();
        
        NamedNodeMap existingAttributes = el.getAttributes();        
        
        Set possibleAttributes = (Set) attrDecls.get(el.getTagName());
        if (possibleAttributes == null) return org.openide.util.Enumerations.empty();
        
        String prefix = ctx.getCurrentPrefix();
        
        LinkedList list = new LinkedList ();
        Iterator it = possibleAttributes.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                if (existingAttributes.getNamedItem(next) == null) {
                    list.add (new MyAttr(next));
                }
            }
        }
        
        return Collections.enumeration (list);
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>CompletionResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *        Every list member represents one possibility.
     */
    public Enumeration queryElements(HintContext ctx) {
        if (elementDecls == null) return org.openide.util.Enumerations.empty();;
        
        Node node = ((Node)ctx).getParentNode();        
        Set elements = null;
        
        if (node instanceof Element) {
            Element el = (Element) node;
            if (el == null) return org.openide.util.Enumerations.empty();;

            // lazilly parse content model
            Object model = null;
            String prefs = System.getProperty("netbeans.xml.completion", "default"); // NOI18N
            if ("fast".equals(prefs)) {                                      // NO18N
                model = null;
            } else if ("default".equals(prefs) || "accurate".equals(prefs)) { // NO18N
                model = contentModels.get(el.getTagName());
            } else {
                model = null;
            }
            if (model instanceof String) {
                model = ContentModel.parseContentModel((String)model);
                contentModels.put(el.getTagName(), model);
            }
            if (model instanceof ContentModel) {
                Enumeration en = ((ContentModel)model).whatCanFollow(new PreviousEnumeration(el, ctx));
                if (en == null) return org.openide.util.Enumerations.empty();
                String prefix = ctx.getCurrentPrefix();
                elements = new TreeSet();
                while (en.hasMoreElements()) {
                    String next = (String) en.nextElement();
                    if (next.startsWith(prefix)) {
                        elements.add(next);
                    }
                }
            }
            // simple fallback
            if (elements == null) {
                elements = (Set) elementDecls.get(el.getTagName());
            }
        } else if (node instanceof Document) {
            elements = elementDecls.keySet();  //??? should be one from DOCTYPE if exist
        } else {
            return org.openide.util.Enumerations.empty();
        }
                
        if (elements == null) return org.openide.util.Enumerations.empty();;
        String prefix = ctx.getCurrentPrefix();
        
        LinkedList list = new LinkedList ();
        Iterator it = elements.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                boolean empty = emptyElements.contains(next);
                list.add(new MyElement(next, empty));
            }
        }
        
        return Collections.enumeration (list);
    }
    
    /**
     * Allow to get names of <b>declared notations</b>.
     * @return list of <code>CompletionResult</code>s (NOTATION_NODEs)
     */
    public Enumeration queryNotations(String prefix) {
        if (notations == null) return org.openide.util.Enumerations.empty();;
        
        LinkedList list = new LinkedList ();
        Iterator it = notations.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                list.add (new MyNotation(next));
            }
        }
        
        return Collections.enumeration (list);
    }
       
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>CompletionResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public Enumeration queryValues(HintContext ctx) {
        if (attrEnumerations.isEmpty()) return org.openide.util.Enumerations.empty();
        
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            String attributeName = ctx.getNodeName();
            Element element = ((Attr)ctx).getOwnerElement();
            if (element == null) return org.openide.util.Enumerations.empty();
            
            String elementName = element.getNodeName();
            String key = elementName + " " + attributeName;
            List values = (List) attrEnumerations.get(key);
            if (values == null) return org.openide.util.Enumerations.empty();
            
            String prefix = ctx.getCurrentPrefix();
            LinkedList en = new LinkedList ();
            Iterator it = values.iterator();
            while (it.hasNext()) {
                String next = (String) it.next();
                if (next.startsWith(prefix)) {
                    en.add(new MyText(next, next));
                }
            }
            return Collections.enumeration (en);
        }
        return org.openide.util.Enumerations.empty();
    }

    // return defaults for attribute values (DTD does not declare content defaults)
    public GrammarResult queryDefault(final HintContext ctx) {
        
        Node node = ctx;
        
        if (ctx.getNodeType() == Node.TEXT_NODE) {
            node = ctx.getParentNode();
            if (node == null) return null;
        }
        
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr attr = (Attr) node;
            Element element = attr.getOwnerElement();
            if (element == null) return null;

            String elementName = element.getNodeName();
            String attributeName = attr.getNodeName();
            String key = elementName + " " + attributeName;                 // NOI18N
            String def = (String) defaultAttributeValues.get(key);
            if (def == null) return null;
            return new MyText(def, def);
        }
        
        return null;
    }
    
    // it is not yet implemented
    public boolean isAllowed(Enumeration en) {
        return true;
    }
    
    // customizers section ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public java.awt.Component getCustomizer(HintContext ctx) {
        return null;
    }
    
    public boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
        return null;
    }
    

    /** For debug purposes only. */
    public String toString() {
        return "DTD grammar";
//        return "DTDGrammar:\nelements: " + elementDecls.keySet() + "\nattributes:" + attrDecls.keySet() + "";
    }

    /**
     * Lazy evaluated enumeration of previous <b>element</b> sibling
     * tag names.
     */
    private static class PreviousEnumeration implements Enumeration {
        
        private final Node parent;
        private final Element lastElement;
        private Node next;
        private boolean eoeSeen = false;

        PreviousEnumeration(Node parent, Node pointer) {
            this.parent = parent;
            Node last = pointer.getPreviousSibling();
            while (last != null) {
                if (last.getNodeType() == Node.ELEMENT_NODE) break;
                last = last.getPreviousSibling();
            }
            lastElement = (Element) last;

            // init next

            if (last != null) {
                fetchNext(parent.getFirstChild());
            } else {
                next = null;
            }

        }
        
        public boolean hasMoreElements() {
            return next != null;
        }

        public Object nextElement() {
            if (next == null) throw new NoSuchElementException();
            try {
                return next.getNodeName();
            } finally {
                fetchNext(next.getNextSibling());
            }
        }

        /**
         * Load next field being aware if terminating element
         * @param next candidate
         */
        private void fetchNext(Node candidate) {
            next = candidate;
            if (eoeSeen) {
                next = null;
            } else {
                while (next != null) {
                    if (next.getNodeType() == Node.ELEMENT_NODE) break;
                    next = next.getNextSibling();
                }
                //!!! how to properly implement it
                if (lastElement.equals(next)) eoeSeen = true;
            }
        }
    }
    
    // Result classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    private static abstract class AbstractResultNode extends AbstractNode implements GrammarResult {
        
        public Icon getIcon(int kind) {
            return null;
        }
        
        /**
         * @output provide additional information simplifiing decision
         */
        public String getDescription() {
            return getNodeName() + " desc";
        }
        
        /**
         * @output text representing name of suitable entity
         * //??? is it really needed
         */
        public String getText() {
            return getNodeName();
        }
        
        /**
         * @output name that is presented to user
         */
        public String getDisplayName() {
            return null;
        }

        public boolean isEmptyElement() {
            return false;
        }
    }
    
    private static class MyEntityReference extends AbstractResultNode implements EntityReference {
        
        private String name;
        
        MyEntityReference(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.ENTITY_REFERENCE_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
        
        
    }
    
    private static class MyElement extends AbstractResultNode implements Element {
        
        private String name;

        private boolean empty;

        MyElement(String name, boolean empty) {
            this.name = name;
            this.empty = empty;
        }
        
        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
        
        public String getTagName() {
            return name;
        }

        public boolean isEmptyElement() {
            return empty;
        }
    }

    private static class MyAttr extends AbstractResultNode implements Attr {
        
        private String name;
        
        MyAttr(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
        
        public String getName() {
            return name;                
        }

        public String getValue() {
            return null;  //??? what spec says
        }
        
        
    }

    private static class MyNotation extends AbstractResultNode implements Notation {
        
        private String name;
        
        MyNotation(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.NOTATION_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
                        
    }
    
    private static class MyText extends AbstractResultNode implements Text {
        
        private final String data;
        private final String displayName;

        MyText(String data, String displayName) {
            this.data = data;
            this.displayName = displayName;
        }
        
        public short getNodeType() {
            return Node.TEXT_NODE;
        }

        public String getNodeValue() {
            return getData();
        }
        
        public String getData() throws DOMException {
            return data;
        }

        public int getLength() {
            return data == null ? -1 : data.length();
        }

        public String getDisplayName() {
            return displayName;
        }
    }
        
}
