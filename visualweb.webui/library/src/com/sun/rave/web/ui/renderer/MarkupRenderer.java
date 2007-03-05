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
package com.sun.rave.web.ui.renderer;


import com.sun.rave.web.ui.component.Markup;
import com.sun.rave.web.ui.util.RenderingUtilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * <p>This class is responsible for rendering any type of XML tag markup. </p>
 */
public class MarkupRenderer extends AbstractRenderer {

    // -------------------------------------------------------- Static Variables

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    // no pass throughs.

    // -------------------------------------------------------- Renderer Methods
    public boolean getRendersChildren() {
        return true;
    }
    
    
    /**
     * <p>Render the start of an Link (Link) tag.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * start should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        //intentionally empty
        Markup markup = (Markup) component;
        
        String tagName = markup.getTag();
        
        if (tagName == null)
            return;  //TODO: write out log message
        
        if (!markup.isSingleton()) {
            // Markup is a singleton
            writeInsides(markup, context, writer);
        }
    }
    
    /**
     * <p>Render the attributes for the Markup. </p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * attributes should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        Markup markup = (Markup) component;
        
    }
    
    public void encodeChildren(FacesContext context, UIComponent component)
    throws IOException {
        Markup markup = (Markup) component;
        
        if (!markup.isSingleton()) {
            super.encodeChildren(context, component);
        }
        
    }
    
    /**
     * <p>Write out the Markup.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * end should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        // End the appropriate element
        
        Markup markup = (Markup) component;
        
        String tagName = markup.getTag();
        
        if (tagName == null)
            return;  //TODO: write out log message
        
        if (markup.isSingleton()) {
            // Markup is a singleton
            writeInsides(markup, context, writer);
        }
            writer.endElement(tagName);
     }
    
    // --------------------------------------------------------- Private Methods
    
    private void writeInsides(Markup markup,
            FacesContext context, ResponseWriter writer) throws IOException {
        String tagName = markup.getTag();
        writer.startElement(tagName, markup);
        writeId(markup, context, writer);
        writeStyles(markup, context, writer);
        writeExtraAttributes(markup, context, writer);
    }
    
    
    private void writeId(Markup markup,
            FacesContext context, ResponseWriter writer) throws IOException {
        
        String id=markup.getClientId(context);
        if (id != null) {
            writer.writeAttribute("id", id, null); //NO18N
        }
    }
    
    private void writeStyles(Markup markup,
            FacesContext context, ResponseWriter writer) throws IOException {
        
        String style = markup.getStyle();
        String styleClass = markup.getStyleClass();
        if (style != null) {
            writer.writeAttribute("style", style, null);  //NO18N
        }
        
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass"); //NO18N
        }
    }
    
    private void writeExtraAttributes(Markup markup,
            FacesContext context, ResponseWriter writer) throws IOException {
        
        String extra = markup.getExtraAttributes();
        if (extra != null) {
            RenderingUtilities.renderExtraHtmlAttributes(writer, extra);
        }
        
        Map attributes = markup.getAttributes();

        Set keys = attributes.keySet();
        
        Iterator listOfKeys = keys.iterator();
        
        while (listOfKeys.hasNext()) {
            String key = (String) listOfKeys.next();
            Object value = attributes.get(key);
            
            // Only take strings (their is a private arraylist that I need to
            // avoid
            
            if (value != null && value instanceof String) {
                writer.writeAttribute(key, value, null);
            }
        }
    }
}
