/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.faces;

import javax.faces.context.FacesContext;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.markup.MarkupDesignContext;

/**
 * The FacesDesignContext is an extension to the DesignContext interface (and MarkupDesignContext
 * interface) that adds JSF-specific functionality.  This adds methods for creating new facets and
 * handling value reference expressions at design-time.  A FacesDesignContext can be accessed by
 * calling the DesignBean.getDesignContext() method and testing the returned DesignContext for
 * 'instanceof' FacesDesignContext.  If the file being designed is a JSF-specific backing file
 * (eg Page1.jsp + Page1.java), the DesignContext will be an instanceof FacesDesignContext.
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignContext
 * @see DesignBean#getDesignContext()
 */
public interface FacesDesignContext extends MarkupDesignContext {

    /**
     * Returns the reference name to use in EL expressions to refer to this FacesDesignContext.
     *
     * @return The String to use as a reference name (in JSF EL)
     */
    public String getReferenceName();

    /**
     * Checks if a facet child of type 'facet' using a component of type 'type' on the faces
     * bean 'parent' can be created.  This method should be called as a test before a call to
     * createFacet.
     *
     * @param facetName The desired facet name to create
     * @param type The desired component type to create as a facet
     * @param parent The desired parent component to create a facet in
     * @return <code>true</code> if the creation of this facet would be successful, or
     *         <code>false</code> if not
     * @see createFacet(String, String, DesignBean)
     */
    public boolean canCreateFacet(String facetName, String type, DesignBean parent);

    /**
     * Creates a facet child of type 'facet' using a component of type 'type' on the faces
     * bean 'parent'.  Before this method is called, a test call should be made to canCreateFacet.
     *
     * @param facetName The desired facet name to create
     * @param type The desired component type to create as a facet
     * @param parent The desired parent component to create a facet in
     * @return The resulting DesignBean representing the new facet
     * @see canCreateFacet(String, String, DesignBean)
     */
    public DesignBean createFacet(String facetName, String type, DesignBean parent);

    /**
     * Returns whether a binding expression can be created to point to 'toBean'
     *
     * @param toBean the 'target' bean to create a value expression to point to
     * @return <code>true</code> if a binding expression can be created to point to 'toBean',
     *         <code>false</code> if not
     */
    public boolean isValidBindingTarget(DesignBean toBean);

    /**
     * Returns a valid reference expression "#{...}" value to point to 'toBean'
     *
     * @param toBean the 'target' bean to create a value expression to point to
     * @return A valid reference expression pointing to 'toBean'
     */
    public String getBindingExpr(DesignBean toBean);

    /**
     * Returns a valid binding expression "#{...}" value to point to 'toBean' plus the sub-
     * expression inside of toBean.  For example, toBean might be button2 on Page1, and the
     * desired expression might be to get the value of button2.  The call would then be
     * getReferenceExpr(button2, "value") --> "#{Page1.button2.value}"
     *
     * @param toBean the 'target' bean to create a value expression to point to
     * @param subExpr An optional sub-expression
     * @return A valid reference expression pointing to 'toBean' plus the specified sub-expression
     */
    public String getBindingExpr(DesignBean toBean, String subExpr);

    /**
     * Returns the live instance Object (not DesignBean) that the reference expression resolves to
     * (at the moment called)
     *
     * @param expr The EL reference expression to evaluate
     * @return the live instance Object (not DesignBean) that the reference expression resolves to
     */
    public Object resolveBindingExpr(String expr);

    /**
     * Returns a result that is resolved to the last DesignBean that can be found, plus the
     * remaining un-resolvable string (at least doesn't resolve to a DesignBean)
     *
     * @param expr The EL reference expression to evaluate
     * @return A ResolveResult, which includes the deepest DesignBean that could be resolved, plus
     *         the remainder of the EL expression that could not be resolved to a DesignBean
     */
    public ResolveResult resolveBindingExprToBean(String expr);

    /**
     * Returns the FacesContext that can be used by component designers to retrieve design-time
     * Faces information.
     *
     * @return The 'runtime' FacesContext running at design-time
     */
    public FacesContext getFacesContext();
}
