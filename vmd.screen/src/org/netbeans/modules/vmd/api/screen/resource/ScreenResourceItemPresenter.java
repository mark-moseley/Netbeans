/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.api.screen.resource;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

/**
 * Screen resource presenter
 * @author breh
 */
public abstract class ScreenResourceItemPresenter extends Presenter {

    /**
     * Returns category descriptor. This method cannot return null.
     * @return non-null category descriptor
     */
    public abstract ScreenResourceCategoryDescriptor getCategoryDescriptor();
    
    /**
     * Determines whether this resource is active in the given component (i.e. whether it makes sense to visualize it
     * when a given component is being edited
     * @param component the edited component in the device view
     * @return true, if active for the component
     */
    public abstract boolean isActiveFor (DesignComponent component);
    
     /**
     * Returns name type for InfoPresenter
     * @return NameType - PRIMARY, SECONDARY or TERTIARY 
     */
    public abstract InfoPresenter.NameType getNameType();

    public final DesignComponent getRelatedComponent () {
        return getComponent ();
    }
    
}
