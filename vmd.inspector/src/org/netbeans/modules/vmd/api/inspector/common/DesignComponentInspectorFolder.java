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
package org.netbeans.modules.vmd.api.inspector.common;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;

/**
 *
 * @author Karol Harezlak
 */

 /**
 * This class suits as a descriptor for the Mobility Visual Designer Navigator node.
 * InspectorFolder in this class is created based on DesignComponent passed in the class constructor.
 * This class creates visual representation of DesignComponent in the Mobility Visual Designer Navigator.
 */
 public class DesignComponentInspectorFolder extends InspectorFolder {
        
        private String displayName;
        private Image icon;
        private boolean canRename;
        private String name;
        private List<InspectorOrderingController> ocs;
        private DesignComponent component;
        
        /**
         * Creates  DesignComponentInspectorFolder
         * @param canRename - indicates if name of the InspectorFolder can be changed
         * @param component - InspectorFolder is created based on this parameter 
         */ 
        public DesignComponentInspectorFolder(boolean canRename, DesignComponent component) {
            assert (component != null);
            this.component = component;
            this.canRename = canRename;
        }
        
        public TypeID getTypeID() {
            return getComponent().getType();
        }
        
        public Long getComponentID() {
            return getComponent().getComponentID();
        }
        
        public Image getIcon() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    InfoPresenter presenter =  getComponent().getPresenter(InfoPresenter.class);
                    if (presenter == null)
                        throw new IllegalStateException("No InfoPresenter for this component"); //NOI18N
                    icon = presenter.getIcon(InfoPresenter.IconType.COLOR_16x16);
                }
            });
            return icon;
        }
        
        public String getDisplayName() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    if (getComponent().getParentComponent() != null)
                        displayName = InfoPresenter.getDisplayName(getComponent());
                    else if (getComponent() == getComponent().getDocument().getRootComponent())
                        displayName = InfoPresenter.getDisplayName(getComponent());
                }
            });
            return displayName;
        }
        
        public String getHtmlDisplayName() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    if (getComponent().getParentComponent() != null)
                        displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                    else if (getComponent() == getComponent().getDocument().getRootComponent())
                        displayName = InfoPresenter.getHtmlDisplayName(getComponent());
                }
            });
            return displayName;
        }
        
        public boolean isInside(final InspectorFolderPath path, final InspectorFolder folder, final  DesignComponent component) {
            for (InspectorPositionPresenter presenter : getComponent().getPresenters(InspectorPositionPresenter.class)){
                for (InspectorPositionController pc : presenter.getFolderPositionControllers()) {
                    if (pc != null && pc.isInside(path, folder, getComponent()))
                        return true;
                }
            }
            return false;
        }
        
        public Action[] getActions() {
            return ActionsSupport.createActionsArray(getComponent());
        }
        
        public boolean canRename() {
            return canRename;
        }
        
        public String getName() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    InfoPresenter presenter = getComponent().getPresenter(InfoPresenter.class);
                    if (presenter != null) {
                        if (presenter.isEditable())
                            name = presenter.getEditableName();
                    } else
                        Debug.warning("No info presenter for component :"  + getComponent()); //NOI18N
                }
            });
            return name;
        }
        
        public InspectorOrderingController[] getOrderingControllers() {
            getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    Collection<? extends InspectorOrderingPresenter> presenters = getComponent().getPresenters(InspectorOrderingPresenter.class);
                    if (presenters == null || presenters.isEmpty() ) {
                        ocs = null;
                        return;
                    }
                    ocs = new ArrayList<InspectorOrderingController>();
                    for (InspectorOrderingPresenter presenter : presenters){
                        ocs.addAll(Arrays.asList(presenter.getFolderOrderingControllers()));
                    }
                }
            });
            if (ocs == null)
                return null;
            return ocs.toArray(new InspectorOrderingController[ocs.size()]);
        }

        protected DesignEventFilter getEventFilter() {
            return null;
        }
        
        protected DesignComponent getComponent() {
            return component;
        }
        
    }
