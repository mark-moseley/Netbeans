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

package org.netbeans.modules.vmd.inspector;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Karol Harezlak
 */
public final class InspectorWrapperTree implements FolderRegistry.Listener/*, ActiveViewSupport.Listener */{
    
    private Collection<InspectorFolder> globalFolders;
    private FolderRegistry registry;
    private WeakReference<DesignDocument> document;
    private InspectorFolderWrapper rootFolderWrapper;
    private WeakSet<DesignComponent> componentsToDelete;
    private WeakSet<DesignComponent> componentsToAdd;
    private WeakSet<DesignComponent> componentsToUndo;
    private WeakSet<InspectorFolderWrapper> foldersToUpdate;
    private WeakSet<DesignComponent> deletedComponentsCash;
    private boolean lock = true;
    
    InspectorWrapperTree(DesignDocument document) {
        foldersToUpdate = new WeakSet<InspectorFolderWrapper>();
        componentsToAdd = new WeakSet<DesignComponent>();
        componentsToDelete = new WeakSet<DesignComponent>();
        componentsToUndo = new WeakSet<DesignComponent>();
        this.document = new WeakReference<DesignDocument>(document);
        rootFolderWrapper = new InspectorFolderWrapper(document, new RootFolder());
        rootFolderWrapper.resolveFolder(document);
    }
    
    synchronized void buildTree(final Collection<DesignComponent> fullyAffected, final Collection<DesignComponent> createdComponents,final Collection<DesignComponent> affectedComponents) {
        lock = true;
        document.get().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (rootFolderWrapper.getChildren() != null) {
                    if (fullyAffected != null && !fullyAffected.isEmpty())
                        updateFolderToUpdate(fullyAffected, rootFolderWrapper);
                    updateChangedDescriptors(createdComponents, affectedComponents);
                    dive(InspectorFolderPath.createInspectorPath().add(rootFolderWrapper.getFolder()), rootFolderWrapper);
                    updateTreeStructureView();
                    InspectorPanel.getInstance().getUI(document.get()).expandNodes(foldersToUpdate);
                } else {
                    updateChangedDescriptors(markAllComponentsAsToAdd(), null);
                    dive(InspectorFolderPath.createInspectorPath().add(rootFolderWrapper.getFolder()), rootFolderWrapper);
                    updateTreeStructureView();
                    Collection foldersToExpand = rootFolderWrapper.getChildren();
                    if (foldersToExpand != null)
                        InspectorPanel.getInstance().getUI(document.get()).expandNodes(foldersToExpand);
                }
            }
        });
        // cleaning up
        foldersToUpdate.clear();
        componentsToAdd.clear();
        componentsToDelete.clear();
        componentsToUndo.clear();
        lock = false;
    }
    
    private void updateFolderToUpdate(Collection<DesignComponent> fullyAffected, InspectorFolderWrapper parentWrapper) {
        if (parentWrapper.getChildren() == null)
            return;
        for (InspectorFolderWrapper wrapper : parentWrapper.getChildren()) {
            updateFolderToUpdate(fullyAffected, wrapper);
            if (fullyAffected.contains(wrapper.getComponent()))
                foldersToUpdate.add(wrapper);
        }
    }
    
    boolean isLocked() {
        return lock;
    }
    
    //------------------ bulid tree algorithm
    
    private void dive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        if (parentWrapper.getFolder() instanceof RootFolder)
            wrapperChildren = componentsRecursion(path, parentWrapper, document.get().getRootComponent());
        else
            wrapperChildren = componentsChildrenDive(path, parentWrapper, document.get().getRootComponent());
        
        if (wrapperChildren == null)
            wrapperChildren = registryDescriptorsDive(path, parentWrapper);
        else {
            List<InspectorFolderWrapper> registryWrapperChildren = registryDescriptorsDive(path, parentWrapper);
            if (registryWrapperChildren != null)
                wrapperChildren.addAll(registryWrapperChildren);
        }
        
        if (wrapperChildren == null)
            wrapperChildren = parentWrapper.getChildren();
        else {
            foldersToUpdate.add(parentWrapper);
            List<InspectorFolderWrapper> parentWrapperChildren = parentWrapper.getChildren();
            if (parentWrapperChildren != null)
                wrapperChildren.addAll(parentWrapperChildren);
        }
        
        if (wrapperChildren != null) {
            WeakSet<InspectorFolderWrapper> wrappersToDelete = null;
            for (InspectorFolderWrapper folder : wrapperChildren) {
                if (componentsToDelete != null && folder.getFolder().getComponentID() != null) {
                    for (DesignComponent component : componentsToDelete) {
                        if (folder.getFolder().getComponentID().equals(component.getComponentID())) {
                            if (wrappersToDelete == null)
                                wrappersToDelete = new WeakSet<InspectorFolderWrapper>();
                            wrappersToDelete.add(folder);
                            if (deletedComponentsCash == null)
                                deletedComponentsCash = new WeakSet<DesignComponent>();
                            deletedComponentsCash.add(document.get().getComponentByUID(component.getComponentID()));
                            foldersToUpdate.add(parentWrapper);
                        }
                    }
                }
                path.add(folder.getFolder());
                dive(path, folder);
                path.remove(folder.getFolder());
            }
            if (wrappersToDelete != null) {
                wrapperChildren.removeAll(wrappersToDelete);
                for (InspectorFolderWrapper wrapperToDelete : wrappersToDelete) {
                    wrapperToDelete.terminate();
                }
            }
            parentWrapper.setChildren(wrapperChildren);
        }
    }
    
    private List<InspectorFolderWrapper> componentsChildrenDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent parentComponent) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent component : parentComponent.getComponents()) {
            if (componentsToAdd.contains(component) || componentsToUndo.contains(component)) {
                List<InspectorFolderWrapper> tempWrapperChildren = componentsRecursion(path, parentWrapper, component);
                if (wrapperChildren == null)
                    wrapperChildren = tempWrapperChildren;
                else if ( tempWrapperChildren != null)
                    wrapperChildren.addAll(tempWrapperChildren);
            }
            if ((! InspectorRegistry.getDefault().getRegistry().isEmpty()) && InspectorRegistry.getDefault().getRegistry().contains(component)) {
                if (wrapperChildren == null)
                    wrapperChildren = changedComponentsRecursion(path, parentWrapper, component);
                else {
                    List<InspectorFolderWrapper> changedComponentsRecursionChildren = changedComponentsRecursion(path, parentWrapper, component);
                    if (changedComponentsRecursionChildren != null)
                        wrapperChildren.addAll(changedComponentsRecursionChildren);
                }
            }
            if (wrapperChildren == null)
                wrapperChildren = componentsChildrenDive(path, parentWrapper, component);
            else {
                List<InspectorFolderWrapper> componentsChildren = componentsChildrenDive(path, parentWrapper, component);
                if (componentsChildren != null)
                    wrapperChildren.addAll(componentsChildren);
            }
        }
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> componentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent componentToAdd : componentsToAdd) {
            if (componentToAdd.getComponentID() != component.getComponentID() )
                continue;
            for (InspectorFolderPresenter presenter : componentToAdd.getPresenters(InspectorFolderPresenter.class)) {
                if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), componentToAdd)) {
                    if (presenter.getFolder().getComponentID() != null && ( parentWrapper.getChildrenFolders() == null || (!parentWrapper.getChildrenFolders().contains(presenter.getFolder())))) {
                        if (wrapperChildren == null)
                            wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                        InspectorFolderWrapper wrapper = new InspectorFolderWrapper(document.get(), presenter.getFolder());
                        wrapperChildren.add(wrapper);
                        foldersToUpdate.add(parentWrapper);
                    }
                }
            }
        }
        
        for (DesignComponent componentToUndo : componentsToUndo) {
            if (componentToUndo.getComponentID() != component.getComponentID() )
                continue;
            for (InspectorFolderPresenter presenter : componentToUndo.getPresenters(InspectorFolderPresenter.class)) {
                if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), componentToUndo)) {
                    if (presenter.getFolder().getComponentID() != null) {
                        if (wrapperChildren == null)
                            wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                        InspectorFolderWrapper wrapper = new InspectorFolderWrapper(document.get(), presenter.getFolder());
                        wrapperChildren.add(wrapper);
                        foldersToUpdate.add(parentWrapper);
                        deletedComponentsCash.remove(componentToUndo);
                    }
                }
            }
        }
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> changedComponentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
            if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), component)) {
                if (presenter.getFolder().getComponentID() != null) {
                    if (! parentWrapper.removeChild(presenter.getFolder()))
                        continue;
                    if (wrapperChildren == null)
                        wrapperChildren = wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                    InspectorFolderWrapper wrapper = new InspectorFolderWrapper(document.get(), presenter.getFolder());
                    wrapperChildren.add(wrapper);
                    path.add(wrapper.getFolder());
                    rebulidDive(path, wrapper);
                    path.remove(wrapper.getFolder());
                    foldersToUpdate.add(parentWrapper);
                } else
                    throw new IllegalArgumentException("Argument ComponentID is null: component: " + component); //NOI18N
            }
        }
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> registryDescriptorsDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        if (globalFolders == null)
            return null;
        
        for (InspectorFolder folder : globalFolders ){
            if (folder.isInside(path, folder, null)){
                if (parentWrapper.getChildrenFolders() != null && parentWrapper.getChildrenFolders().contains(folder))
                    continue;
                if (wrapperChildren == null)
                    wrapperChildren = wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                InspectorFolderWrapper wrapper = new InspectorFolderWrapper(document.get(), folder);
                wrapperChildren.add(wrapper);
            }
        }
        return wrapperChildren;
    }
    
    //------------------ change tree algorithm
    
    private void rebulidDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        if (parentWrapper.getFolder() instanceof RootFolder)
            wrapperChildren = rebulidComponentsRecursion(path , parentWrapper, document.get().getRootComponent());
        else
            wrapperChildren = rebulidComponentsChildrenDive(path, parentWrapper, document.get().getRootComponent());
        
        if (wrapperChildren == null)
            wrapperChildren = registryDescriptorsDive(path, parentWrapper);
        else {
            List<InspectorFolderWrapper> registryChildren = registryDescriptorsDive(path, parentWrapper);
            if (registryChildren != null)
                wrapperChildren.addAll(registryChildren);
        }
        if (wrapperChildren == null)
            wrapperChildren = parentWrapper.getChildren();
        else {
            List<InspectorFolderWrapper> parentChildren = parentWrapper.getChildren();
            if (parentChildren != null)
                wrapperChildren.addAll(parentChildren);
        }
        if (wrapperChildren != null) {
            for (InspectorFolderWrapper folder : wrapperChildren) {
                path.add(folder.getFolder());
                rebulidDive(path, folder);
                path.remove(folder.getFolder());
            }
            parentWrapper.setChildren(wrapperChildren);
        }
    }
    
    private List<InspectorFolderWrapper> rebulidComponentsChildrenDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent parentComponent) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent component : parentComponent.getComponents()) {
            List<InspectorFolderWrapper> tempWrapperChildren = rebulidComponentsRecursion(path, parentWrapper, component);
            
            if (wrapperChildren == null)
                wrapperChildren = tempWrapperChildren;
            else if ( tempWrapperChildren != null)
                wrapperChildren.addAll(tempWrapperChildren);
            
            if (wrapperChildren == null)
                wrapperChildren = rebulidComponentsChildrenDive(path, parentWrapper, component);
            else {
                List<InspectorFolderWrapper> componentsChildren = rebulidComponentsChildrenDive(path, parentWrapper, component);
                if (componentsChildren != null)
                    wrapperChildren.addAll(componentsChildren);
            }
        }
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> rebulidComponentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> childrenWrapper = null;
        for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
            if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), component)) {
                if (presenter.getFolder().getComponentID() != null && ( parentWrapper.getChildrenFolders() == null || (!parentWrapper.getChildrenFolders().contains(presenter.getFolder())))) {
                    if (childrenWrapper == null)
                        childrenWrapper = childrenWrapper = new ArrayList<InspectorFolderWrapper>();
                    InspectorFolderWrapper wrapper = new InspectorFolderWrapper(document.get(), presenter.getFolder());
                    childrenWrapper.add(wrapper);
                    foldersToUpdate.add(parentWrapper);
                }
            }
        }
        return childrenWrapper;
    }
    
    
    // ---------- Existing tree update
    private void updateChangedDescriptors(final Collection<DesignComponent> createdComponents , final Collection<DesignComponent> affectedComponents) {
        if (createdComponents != null ) {
            for (DesignComponent component : createdComponents) {
                for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
                    componentsToAdd.add(component);
                }
            }
        }
        if (affectedComponents != null ) {
            for (DesignComponent component : affectedComponents) {
                if (deletedComponentsCash != null && deletedComponentsCash.contains(component))
                    componentsToUndo.add(component);
                if (component == null || component.getParentComponent() != null)
                    continue;
                componentsToDelete.add(component);
            }
        }
        
        InspectorRegistry.getDefault().removeAll(componentsToAdd);
        InspectorRegistry.getDefault().removeAll(componentsToDelete);
    }
    
    private void updateTreeStructureView() {
        updateTreeStructureView(rootFolderWrapper);
        rootFolderWrapper.resolveFolder(document.get());
        warmUp(rootFolderWrapper.getNode());
    }
    
    private void updateTreeStructureView(InspectorFolderWrapper parentWrapper) {
        if (parentWrapper.getChildren() != null) {
            for(InspectorFolderWrapper wrapper : parentWrapper.getChildren()) {
                updateTreeStructureView(wrapper);
                if (foldersToUpdate.contains(wrapper))
                    wrapper.resolveFolder(document.get());
            }
        }
        if (parentWrapper.getChildren() == null || parentWrapper.getChildren().isEmpty())
            parentWrapper.resolveFolder(document.get());
    }
    
    private void warmUp(Node node) {
        for (Node child : node.getChildren().getNodes())
            warmUp(child);
    }
    
    private void  updateRegistredFolders() {
        if (registry == null)
            return;
        registry.readAccess(new Runnable() {
            public void run() {
                globalFolders = registry.getFolders();
            }
        });
    }
    
    InspectorFolderWrapper getRootWrapperFolder() {
        return rootFolderWrapper;
    }
    
    public void notifyRegistryContentChange() {
        updateRegistredFolders();
    }
    
    Collection<Node> getSelectedNodes() {
        return getSelectedNodes(rootFolderWrapper);
    }
    
    private Collection<Node> getSelectedNodes(final InspectorFolderWrapper parentFolder) {
        if (parentFolder == null || parentFolder.getChildren() == null)
            return null;
        
        final Collection<Node> selectedNodes = new HashSet<Node>();
        
        if (document.get().getSelectedComponents().isEmpty())
            return selectedNodes;
        for (InspectorFolderWrapper folder : parentFolder.getChildren()) {
            if (folder.getChildren() != null)
                selectedNodes.addAll(getSelectedNodes(folder));
            //TODO Selection based on the ComponentID and Display Name could be not enough it better to create smarter algorithm
            for (DesignComponent component : document.get().getSelectedComponents()) {
                Long  componentID = folder.getFolder().getComponentID();
                if (componentID == null || componentID  != component.getComponentID())
                    continue;
                if (folder.getFolder().getDisplayName().equals(InfoPresenter.getDisplayName(component)) )
                    selectedNodes.add(folder.getNode());
            }
        }
        return selectedNodes;
    }
    
    private Collection<DesignComponent> markAllComponentsAsToAdd(){
        final Collection<DesignComponent> componentsToAdd = new HashSet<DesignComponent>();
        
        componentsToAdd.add(document.get().getRootComponent());
        componentsToAdd.addAll(markAllComponentsAsToAdd(document.get().getRootComponent()));
        
        return componentsToAdd;
    }
    
    private Collection<DesignComponent> markAllComponentsAsToAdd(DesignComponent parentComponent){
        Collection<DesignComponent> componentsToAdd = new HashSet<DesignComponent>();
        
        for (DesignComponent component : parentComponent.getComponents()){
            componentsToAdd.addAll(markAllComponentsAsToAdd(component));
            componentsToAdd.add(component);
        }
        return componentsToAdd;
    }
    
    private class RootFolder implements InspectorFolder {
        
        public TypeID getTypeID() {
            return new TypeID(TypeID.Kind.PRIMITIVE, ""); //NOI18N
        }
        
        public Long getComponentID() {
            return null;
        }
        
        public Image getIcon() {
            return new Image() {
                public void flush() {
                }
                public Graphics getGraphics() {
                    return null;
                }
                public int getHeight(ImageObserver observer) {
                    return 0;
                }
                public Object getProperty(String name, ImageObserver observer) {
                    return null;
                }
                public ImageProducer getSource() {
                    return null;
                }
                public int getWidth(ImageObserver observer) {
                    return 0;
                }
            };
        }
        
        public String getDisplayName() {
            return "ROOT FOLDER - SHOULD BE HIDDEN"; //NOI18N
        }
        
        public Action[] getActions() {
            return null;
        }
        
        public NewType[] getNewTypes() {
            return null;
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            return false;
        }
        
        public String getName() {
            return "ROOT FOLDER - SHOULD BE HIDDEN"; //NOI18N
        }
        
        public InspectorOrderingController[] getOrderingControllers() {
            return null;
        }
        
        public String getHtmlDisplayName() {
            return "ROOT FOLDER - SHOULD BE HIDDEN"; //NOI18N
        }
        
    }
    
}
