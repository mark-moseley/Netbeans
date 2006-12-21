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

package org.netbeans.modules.vmd.midp.inspector.folders;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */
abstract class CategoryFolder implements InspectorFolder {

    public Long getComponentID() {
        return null;
    }

    public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
        if (path.getPath().size() == 2) //TODO It is not enought, it should check type of parent Folder
            return true;
        
        return false;
    }
    
    public Action[] getActions() {
        return null;
    }

    public boolean canRename() {
        return false;
    }

}
