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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <h3>HTML Elements and Layout</h3>
 * 
 * <p> The <code>PropertySheetSection</code> component was written to be used
 *     within the <code>PropertySheet</code> component.  It allows you to group
 *     <code>Property<code> components together in sections within the
 *     <code>PropertySheet</code> component.</p>
 * 
 * <p> When you use this component to create a grouping of <code>Property</code>
 *     components, you may provide a description for the
 *     <code>PropertySheetSection</code>.  This is done via the
 *     <code>label</code> attribute.  Set this attribute to the desired value,
 *     which, of course, may be a ValueBinding expression or a literal String.</p>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 * <p> None.</p>
 * 
 * <h3>Example:</h3>
 * 
 * <p> For an example, please see the documentation for the <code>propertySheet</code> Tag.</p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class PropertySheetSectionBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>PropertySheetSectionBase</code>.</p>
     */
    public PropertySheetSectionBase() {
        super();
        setRendererType("com.sun.rave.web.ui.PropertySheetSection");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.PropertySheetSection";
    }

    // label
    private String label = null;

    /**
 * <p>	This attribute allows you to provide a label or title for the section
 * 	this <code>PropertySheetSection</code> defines for the<code>PropertySheet</code> component.  The value may be a literal
 * 	String, or it may be a ValueBinding expression (useful for
 * 	localization).</p>
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueBinding _vb = getValueBinding("label");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>	This attribute allows you to provide a label or title for the section
 * 	this <code>PropertySheetSection</code> defines for the<code>PropertySheet</code> component.  The value may be a literal
 * 	String, or it may be a ValueBinding expression (useful for
 * 	localization).</p>
     * @see #getLabel()
     */
    public void setLabel(String label) {
        this.label = label;
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.label = (String) _values[1];
        this.style = (String) _values[2];
        this.styleClass = (String) _values[3];
        this.visible = ((Boolean) _values[4]).booleanValue();
        this.visible_set = ((Boolean) _values[5]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[6];
        _values[0] = super.saveState(_context);
        _values[1] = this.label;
        _values[2] = this.style;
        _values[3] = this.styleClass;
        _values[4] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
