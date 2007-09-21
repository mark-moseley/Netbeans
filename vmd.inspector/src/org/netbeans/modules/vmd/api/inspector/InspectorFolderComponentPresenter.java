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
package org.netbeans.modules.vmd.api.inspector;

import org.netbeans.modules.vmd.api.inspector.common.DesignComponentInspectorFolder;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;


/**
 *
 * @author Karol Harezlak
 */

/**
 * Default implementation of InspectorFolderPresenter. InspectorFolder inside of this presenter is
 * created based on the DesignComponent to whose this preseter is attached to.
 */
public final class InspectorFolderComponentPresenter extends InspectorFolderPresenter {
    
    private DesignComponentInspectorFolder folder;
    private boolean canRename;
    
    /**
     * Creates InspectorFolderComponentPresenter.
     * 
     * @param canRename indicates if folder returns by this presenter can be renamed 
     */ 
    public InspectorFolderComponentPresenter(boolean canRename) {
        this.canRename = canRename;
    }
    
    /**
     * Returns InspectorFolder created based on DesignComponent which this presenter is attached to.
     * 
     * @return components InspectorFolder 
     */ 
    public InspectorFolder getFolder() {
        if (folder == null) {
            folder = new DesignComponentInspectorFolder(canRename, getComponent());
        }
        return folder;
    }
    
    protected void notifyAttached(DesignComponent component) {}
    
    protected void notifyDetached(DesignComponent component) {}
    
    protected DesignEventFilter getEventFilter() {
        return null;
    }
    
    protected void designChanged(DesignEvent event) {}
    
    protected void presenterChanged(PresenterEvent event) {}
    
}
