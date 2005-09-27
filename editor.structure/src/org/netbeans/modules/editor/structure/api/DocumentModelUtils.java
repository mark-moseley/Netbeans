/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.editor.structure.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;


/**
 * An utilitity class containing various methods simplifying work with the DocumentElements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public final class DocumentModelUtils {
    
    /** Returns and element starting on the specified position with specied name and type. 
     * If any of these conditions is not true (there isn't any element on the offset, or 
     * there is an element, but the name or the type doń't match) it returns null.
     *
     * @param startOffset - offset of the searched element
     * @param name - name of the element
     * @param type - type of the element
     *
     * @return the element or null, when there is not such an element.
     */
    public static DocumentElement findElement(DocumentModel model, int startOffset, String name, String type) throws BadLocationException {
        List els = model.getDocumentElements(startOffset);
        Iterator i = els.iterator();
        while(i.hasNext()) {
            DocumentElement de = (DocumentElement)i.next();
            if(de.getName().equals(name) && de.getType().equals(type)) return de;
        }
        return null; //no such element found
    }
    
    /** Returns a list of all document elements which are descendants of the givent DocumentElement. 
     *
     * @return list of document elements descendants or empty list if there isn't any descendant.
     */
    
    public static List/*<DocumentElement>*/ getDescendants(DocumentElement de) {
        ArrayList desc = new ArrayList();
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            desc.add(child);
            desc.addAll(getDescendants(child));
        }
        return desc;
    }
    
    /** Dumps a tree like view of document element's children. 
     *  To see a hierarchical view of the entire model use dumpElementStructure(model.getRootElement());
     */
    public static void dumpElementStructure(DocumentElement de) {
        System.out.println("-------- ELEMENTS STRUCTURE --------");
        dumpElementStructure(de, 0);
    }
    
    private static void dumpElementStructure(DocumentElement de, int level) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < level; i++) {
            sb.append(' ');
        }
        String text = de.toString();
        sb.append(text);
        System.out.println(sb.toString());
        
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            dumpElementStructure((DocumentElement)children.next(), level + 4);
        }
    }
    
    /** Dumps a list of existing elements in the model.
     *  This method is mainly used for testing purposes.*/
    public static void dumpModelElements(DocumentModel model) {
        model.debugElements();
    }
    
}
