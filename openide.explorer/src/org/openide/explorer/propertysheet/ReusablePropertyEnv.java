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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ReusablePropertyEnv.java
 *
 * Created on February 6, 2003, 6:17 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;

import java.beans.*;


/** A subclass of PropertyEnv that can be reused by the rendering infrastructure.
 *  All methods for attaching listeners are no-ops:  A renderer will only be
 *  momentarily attached to a given property, and property changes will result
 *  the property being rerendered (and the ReusablePropertyEnv being
 *  reconfigured correctly).<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if it is called from off the
 *  AWT thread.
 *  <P>Note, the use of this class may be non-obvious at first - the value of
 *  <code>NODE</code> is set in the rendering loop, by the SheetTable instance,
 *  which knows about the nodes (other classes in the package should only
 *  be interested in the properties they represnt).  The instance is actually
 *  used in <code>PropertyEditorBridgeEditor.setPropertyEditor()</code>, but
 *  must rely on the table to configure it.
 * @author  Tim Boudreau
 */
final class ReusablePropertyEnv extends PropertyEnv {
    private Object NODE = null;
    private ReusablePropertyModel mdl;

    /** Creates a new instance of ReusablePropertyEnv */
    public ReusablePropertyEnv() {
    }

    public ReusablePropertyModel getReusablePropertyModel() {
        return mdl;
    }

    void clear() {
        NODE = null;

        if (mdl != null) {
            mdl.clear();
        }
    }

    void setReusablePropertyModel(ReusablePropertyModel mdl) {
        this.mdl = mdl;
    }

    /** Uses the <code>NODE</code> field to supply the beans - if it is an instance
     *  of ProxyNode (multi-selection), returns the nodes that ProxyNode represents. */
    public Object[] getBeans() {
        if (ReusablePropertyModel.DEBUG) {
            ReusablePropertyModel.checkThread();
        }

        if (getNode() instanceof ProxyNode) {
            return ((ProxyNode) getNode()).getOriginalNodes();
        } else if (getNode() instanceof Object[]) {
            return (Object[]) getNode();
        } else {
            return new Object[] { getNode() };
        }
    }

    public FeatureDescriptor getFeatureDescriptor() {
        return mdl.getProperty();
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    public boolean isEditable() {
        boolean result;

        if (mdl.getProperty() != null) {
            result = mdl.getProperty().canWrite();
        } else {
            result = true;
        }

        return result;
    }

    public void reset() {
        setEditable(true);
        setState(STATE_NEEDS_VALIDATION);
    }

    public Object getNode() {
        return NODE;
    }

    public void setNode(Object NODE) {
        this.NODE = NODE;
    }
}
