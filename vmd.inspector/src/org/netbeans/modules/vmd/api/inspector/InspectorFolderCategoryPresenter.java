/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.vmd.api.inspector;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddAction;

/**
 *
 * @author Karol Harezlak
 */

/**
 * Implementation of InspectorFolderPresenter. This class attached to the
 * DesignComponent creates category in the tree structure of Mobility Visiula Designer Navigator.
 * Category doesn't represent particular component and could be used (for example)
 * to keep different types of folders separated in different categories like Commands, Items etc.
*/ 

public final class InspectorFolderCategoryPresenter extends InspectorFolderPresenter {

    private CategoryFolder folder;
    private TypeID typeID;
    private Image icon;
    private TypeID[] filtersTypeID;
    private InspectorOrderingController[] orderingControllers;
    private String displayName;
    private TypeID parentTypeID;
    
    /**
     * Creates InspectorFolderCategoryPresenter object.
     * <p>Note: It's not possible to change name of the category folder created by this presenter.</p>
     * 
     * @param displayName category folder display name
     * @param typeID TypeID connected with this category folder
     * @param icon image icon, visual representation of the category folder
     * @param filtersTypeID array of allowed TypeIDs underneath of this category
     * @param parentTypeID parent components TypeId of the component to which this presenter is attached to 
     * @param orderingControllers array of InspectorOrderingControllers available for this presenter
     */ 
    public InspectorFolderCategoryPresenter(String displayName, TypeID typeID, Image icon, TypeID[] filtersTypeID, TypeID parentTypeID, InspectorOrderingController... orderingControllers) {
        this.displayName = displayName;
        this.typeID = typeID;
        this.icon = icon;
        this.filtersTypeID = filtersTypeID;
        this.orderingControllers = orderingControllers;
        this.parentTypeID = parentTypeID;
    }
    
    /**
     * Returns category InspectorFolder.
     * @return category folder
     */ 
    public InspectorFolder getFolder() {
        if (folder == null) {
            folder = new CategoryFolder(displayName, typeID, icon, filtersTypeID, orderingControllers);
        }
        return folder;
    }

    protected void notifyAttached(DesignComponent component) {
    }

    protected void notifyDetached(DesignComponent component) {
    }

    protected DesignEventFilter getEventFilter() {
        return null;
    }

    protected void designChanged(DesignEvent event) {
    }

    protected void presenterChanged(PresenterEvent event) {
    }

    private class CategoryFolder extends InspectorFolder {

        private Image icon;
        private String displayName;
        private InspectorOrderingController[] orderingControllers;
        private TypeID typeID;
        private AddAction[] addAction;
        private TypeID[] filtersTypeID;

        public CategoryFolder(String displayName, TypeID typeID, Image icon, TypeID[] filtersTypeID, InspectorOrderingController[] orderingControllers) {

            if (typeID == null) {
                throw new IllegalArgumentException("TypeID cant be null InspectorFolderPresenter: " + getComponent()); //NOI18N
            }
            this.displayName = displayName;
            this.icon = icon;
            this.orderingControllers = orderingControllers;
            this.typeID = typeID;
            this.filtersTypeID = filtersTypeID;
        }

        public TypeID getTypeID() {
            return typeID;
        }

        public Long getComponentID() {
            return getComponent().getComponentID();
        }

        public Image getIcon() {
            return icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getName() {
            return displayName;
        }

        public Action[] getActions() {
            if (addAction == null) {
                addAction = new AddAction[]{AddAction.getInstance(filtersTypeID)};
            }
            addAction[0].setComponent(getComponent());
            return addAction;
        }

        public boolean canRename() {
            return false;
        }

        public InspectorOrderingController[] getOrderingControllers() {
            return orderingControllers;
        }

        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            if (parentTypeID != null && component.getParentComponent().getType().equals(parentTypeID)) {
                return false;
            }
            if (getComponent().getType().equals(path.getLastElement().getTypeID()) && path.getLastElement().getComponentID().equals(getComponentID())) {
                return true;
            }
            return false;
        }

        public String getHtmlDisplayName() {
            return displayName;
        }
    }
}
