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

package org.netbeans.modules.ant.freeform.spi;

import java.util.List;

/**
 * Description of the build target to be shown in Target Mappings customizer
 * panel.
 * @see ProjectNature#getExtraTargets
 * @author David Konecny
 */
public final class TargetDescriptor {
    
    private String actionName;
    private List/*<String>*/ defaultTargets;
    private String actionLabel;
    private String accessibleLabel;
    
    /**
     * Constructor.
     * @param actionName IDE action name (see {@link org.netbeans.spi.project.ActionProvider})
     * @param defaultTargets list of regular expressions to match name of the 
     *   Ant target to which this IDE action usually maps
     * @param actionLabel localized label of this action. To be shown in UI csutomizer
     * @param accessibleLabel accessible label. Used togerther with actionLabel
     */
    public TargetDescriptor(String actionName, List/*<String>*/ defaultTargets, String actionLabel, String accessibleLabel) {
        this.actionName = actionName;
        this.defaultTargets = defaultTargets;
        this.actionLabel = actionLabel;
        this.accessibleLabel = accessibleLabel;
    }
    
    /**
     * Name of the IDE action which is mapped to an Ant script.
     */
    public String getIDEActionName() {
        return actionName;
    }
    
    /**
     * List of regular expressions to match name of the target in Ant script 
     * which usually maps to the IDE action. List will be processed in the
     * given order so it is recommended to list the most specific ones first.
     * @return cannot be null; can be empty array
     */
    public List/*<String>*/ getDefaultTargets() {
        return defaultTargets;
    }

    /**
     * Label name under which this IDE action will be presented in the 
     * Target Mapping customizer panel.
     */
    public String getIDEActionLabel() {
        return actionLabel;
    }
    
    /**
     * Accessibility of the getIDEActionLabel().
     */
    public String getAccessibleLabel() {
        return accessibleLabel;
    }
    
}
