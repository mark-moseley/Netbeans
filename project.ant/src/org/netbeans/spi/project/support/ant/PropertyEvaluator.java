/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeListener;
import java.util.Map;

/**
 * A way of mapping property names to values.
 * <p>
 * This interface defines no independent thread safety, but in typical usage
 * it will be used with the project manager mutex. Changes should be fired
 * synchronously.
 * @author Jesse Glick
 */
/*XXX: public*/ interface PropertyEvaluator {
    
    /**
     * Evaluate a single property.
     * @param prop the name of a property
     * @return its value, or null if it is not defined or its value could not be
     *         retrieved for some reason (e.g. a circular definition)
     */
    String getProperty(String prop);
    
    /**
     * Evaluate a block of text possibly containing property references.
     * The syntax is the same as for Ant: <samp>${foo}</samp> means the value
     * of the property <samp>foo</samp>; <samp>$$</samp> is an escape for
     * <samp>$</samp>; references to undefined properties are left unsubstituted.
     * @param text some text possibly containing one or more property references
     * @return its value, or null if some problem (such a circular definition) made
     *         it impossible to retrieve the values of some properties
     */
    String evaluate(String text);
    
    /**
     * Get a set of all current property definitions at once.
     * This may be more efficient than evaluating individual properties,
     * depending on the implementation.
     * @return an immutable map from property names to values, or null if the
     *         mapping could not be computed (e.g. due to a circular definition)
     */
    Map/*<String,String>*/ getProperties();
    
    /**
     * Add a listener to changes in particular property values.
     * As generally true with property change listeners, the old and new
     * values may both be null in case the true values are not known or not
     * easily computed; and the property name might be null to signal that any
     * property might have changed.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a listener to changes in particular property values.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
