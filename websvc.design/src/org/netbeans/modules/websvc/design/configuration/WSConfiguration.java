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

package org.netbeans.modules.websvc.design.configuration;

import java.awt.Image;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Ajit Bhate
 */
public interface WSConfiguration {
    
    public static String PROPERTY="value";

    public static String PROPERTY_ENABLE="enabled";
    
    /**
     * Returns the user interface component for this WSConfiguration.
     *
     * @return  the user interface component.
     */
    java.awt.Component getComponent();

    /**
     * Returns the user-oriented description of this WSConfiguration, for use in
     * tooltips in the usre interface.
     *
     * @return  the human-readable description of this WSConfiguration.
     */
    String getDescription();

    /**
     * Returns the display icon of this WSConfiguration.
     *
     * @return  icon for this WSConfiguration.
     */
    Image getIcon();

    /**
     * Returns the display name of this WSConfiguration.
     *
     * @return  title for this WSConfiguration.
     */
    String getDisplayName();
    
    /**
     *  Called to apply changes made by the user 
     */ 
    void set();
    
    /**
     *  Called to cancel changes made by the user
     */
    void unset();
    
    
    /**
     * Used to determine if a functionality is active.
     */ 
    boolean isSet();

    /**
     * Used to determine if a functionality is enabled.
     */ 
    boolean isEnabled();

    /**
     * Allows to register for changes on the client.
     */ 
    public void registerListener(PropertyChangeListener listener);

    /**
     * Required to unregister the listeners when not needed.
     */ 
    public void unregisterListener(PropertyChangeListener listener);
        
}
