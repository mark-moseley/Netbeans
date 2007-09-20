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

package org.netbeans.modules.vmd.api.inspector;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;

/**
 * @author Karol Harezlak
 */

/**
 * This class suits as a descriptor for the folder which is visuals in the Visual Designer Navigator.
 */
public abstract class InspectorFolder implements  InspectorPositionController {
    
    /**
     * Returns TypeID of the component connected with this folder descriptor.
     * @return components TypeID
     */
    public abstract TypeID getTypeID();
    
    /**
     * Returns ComponentsID of the component connected with this folder descriptor.
     * @return Long components ID
     */
    public abstract Long getComponentID();
    
    /**
     * Returns image icon which represents folder in the Visual Designer Navigator.
     * @return folder's image icon
     */
    public abstract Image getIcon();
    
    /**
     * Returns display name of the folder.
     * @return folder's display name
     */
    public abstract String getDisplayName();
    
    /**
     * Returns HTML display name of the folder.
     * @return folder's HTML display name
     */
    public abstract String getHtmlDisplayName();
    
    /**
     * Returns name of the folder.
     * @return folder's name
     */
    public abstract String getName();
    
    /**
     * Returns array of actions available for this folder. 
     * @return array of folder's actions
     */
    public abstract Action[] getActions();
    
    /**
     * Indicates if folder can be rename.
     * @return returns boolean value. Boolean.TRUE folder can be rename. Boolean.FALSE folder can't be rename. 
     */
    public abstract boolean canRename();
    
    /**
     * Returns array of InspectorOrderingControllers. 
     * @return returns array of the InspectorOrderingControllers
     */
    public abstract InspectorOrderingController[] getOrderingControllers();
    
     /**
     * Creates object with implemented interface AcceptSuggestion. Created object 
     * is not restricted in any way. Created object can have any type of functionality
     * that helps with folder dragging and dropping. AcceptSuggestion interface 
     * is used as a marker and has no methods.
     * @return default implementation returns null
     */
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        return null;
    }
    
}
