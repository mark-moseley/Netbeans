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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.ant.freeform.Actions;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 * Logical view of a freeform project.
 * @author Jesse Glick
 */
public final class View implements LogicalViewProvider {
    
    private static final String STYLE_TREE = "tree"; // NOI18N
    private static final String STYLE_PACKAGES = "packages"; // NOI18N
    
    private final FreeformProject project;
    
    public View(FreeformProject project) {
        this.project = project;
    }
    
    public Node createLogicalView() {
        return new RootNode(project);
    }
    
    public Node findPath(Node root, Object target) {
        // XXX
        return null;
    }
    
    private static final class RootChildren extends Children.Keys/*<Element>*/ implements AntProjectListener {
        
        private final FreeformProject p;
        
        public RootChildren(FreeformProject p) {
            this.p = p;
        }
        
        protected void addNotify() {
            super.addNotify();
            updateKeys(false);
            p.helper().addAntProjectListener(this);
            // XXX should probably listen to project.evaluator also?
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            p.helper().removeAntProjectListener(this);
            super.removeNotify();
        }
        
        private void updateKeys(boolean fromListener) {
            Element genldata = p.helper().getPrimaryConfigurationData(true);
            Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
            if (viewEl != null) {
                Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
                final List keys = Util.findSubElements(itemsEl);
                if (fromListener) {
                    // #50328 - post setKeys to different thread to prevent deadlocks
                    RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                setKeys(keys);
                            }
                        });
                } else {
                    setKeys(keys);
                }
            } else {
                setKeys(Collections.EMPTY_SET);
            }
        }
        
        protected Node[] createNodes(Object key) {
            Element itemEl = (Element)key;
            Element locationEl = Util.findElement(itemEl, "location", FreeformProjectType.NS_GENERAL); // NOI18N
            String location = Util.findText(locationEl);
            String locationEval = p.evaluator().evaluate(location);
            FileObject file = p.helper().resolveFileObject(locationEval);
            if (file == null) {
                // Not there... skip this node.
                return null;
            }
            String label;
            Element labelEl = Util.findElement(itemEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
            if (labelEl != null) {
                label = Util.findText(labelEl);
            } else {
                label = null;
            }
            DataObject fileDO;
            try {
                fileDO = DataObject.find(file);
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
            if (itemEl.getLocalName().equals("source-folder")) { // NOI18N
                if (!file.isFolder()) {
                    // Just a file. Skip it.
                    return null;
                }
                String style = itemEl.getAttribute("style"); // NOI18N
                if (style.equals(STYLE_TREE)) {
                    return new Node[] {new ViewItemNode((DataFolder) fileDO, location, label)};
                } else {
                    assert style.equals(STYLE_PACKAGES) : style;
                    if (label == null) {
                        // Don't use fileDO.getNodeDelegate().getDisplayName() since we are not listening to changes anyway.
                        label = file.getNameExt();
                    }
                    return new Node[] {PackageView.createPackageView(GenericSources.group(p, file, location, label, null, null))};
                }
            } else {
                assert itemEl.getLocalName().equals("source-file") : itemEl; // NOI18N
                return new Node[] {new ViewItemNode(fileDO.getNodeDelegate(), location, label)};
            }
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            updateKeys(true);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
    }
    
    private static final class RootNode extends AbstractNode {
        
        private final FreeformProject p;
        
        public RootNode(FreeformProject p) {
            super(new RootChildren(p), Lookups.singleton(p));
            this.p = p;
        }
        
        public String getName() {
            return ProjectUtils.getInformation(p).getName();
        }
        
        public String getDisplayName() {
            return ProjectUtils.getInformation(p).getDisplayName();
        }
        
        public Image getIcon(int type) {
            return Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/freeform-project.png", true); // NOI18N
        }
        
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
        
        public Action[] getActions(boolean context) {
            return Actions.createContextMenu(p);
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
    }
    
    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }
    
     private static final class ViewItemNode extends FilterNode {
        
        private final String name;
        
        private final String displayName;
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
       
        public ViewItemNode(Node orig, String name, String displayName) {
            super(orig);
            this.name = name;
            this.displayName = displayName;
        }
        
        public ViewItemNode(DataFolder folder, String name, String displayName) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
            this.name = name;
            this.displayName = displayName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                // #50425: show original name incl. annotations
                return super.getDisplayName();
            }
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
    }
    
}
