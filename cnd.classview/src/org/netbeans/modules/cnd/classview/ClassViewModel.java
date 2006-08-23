/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview;

import java.awt.Image;
import java.util.*;
import java.io.*;
import javax.swing.*;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.*;
import org.openide.filesystems.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.model.*;
/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ClassViewModel {
    
    private static final boolean showLibs = Boolean.getBoolean("cnd.classview.sys-includes");
    
    private RequestProcessor requestProcessor;
    private ClassViewUpdater updater;
    
    public ClassViewModel() {
        requestProcessor = new RequestProcessor("Class View Updater", 1);
        updater = new ClassViewUpdater(this);
        requestProcessor.post(updater);
    }
    
    public AbstractNode getRoot() {
        if( root == null ) {
            root = createRoot();
        }
        return root;
    }
    
    protected AbstractNode createRoot() {
        Collection/*<CsmProject>*/ projects = CsmModelAccessor.getModel().projects();
        
        ProjectNode[] nodes = new ProjectNode[projects.size()];
        int pos = 0;
        for( Iterator iter = projects.iterator(); iter.hasNext(); ) {
            CsmProject p = (CsmProject) iter.next();
            nodes[pos++] = new ProjectNode(p);
        }
        Children.Array children = new Children.SortedArray();
        children.add(nodes);
        
        if( showLibs ) {
            addLibNodes(children, projects);
        }
        
        return new AbstractNode(children);
    }
    
    protected void addLibNodes(Children.Array children, Collection/*<CsmProject>*/ projects) {
        Collection/*<CsmProject>*/ libs = gatherLibs(projects);
        if( ! libs.isEmpty() ) {
            ProjectNode[] nodes = new ProjectNode[libs.size()];
            int pos = 0;
            for( Iterator iter = libs.iterator(); iter.hasNext(); ) {
                CsmProject p = (CsmProject) iter.next();
                nodes[pos++] = new ProjectNode(p);
            }
            children.add(nodes);
        }        
    }
    
    protected Collection/*<CsmProject>*/ gatherLibs(Collection/*<CsmProject>*/ projects) {
        Set/*<CsmProject>*/ libs = new HashSet();
        for( Iterator iter = projects.iterator(); iter.hasNext(); ) {
            CsmProject p = (CsmProject) iter.next();
            libs.addAll(p.getLibraries());
        }
        return libs;
    }
    
    public void updateProjects() {
        
        if( root == null ) { // paranoya
            root = createRoot();
            return;
        }
        
        Collection/*<CsmProject>*/ modelProjects = CsmModelAccessor.getModel().projects();
        Collection/*<CsmProject>*/ modelLibraries = gatherLibs(modelProjects);
        
        Collection/*<CsmProject>*/ modelAll = new HashSet();
        modelAll.addAll(modelProjects);
        modelAll.addAll(modelLibraries);
        
        // remove projects that aren't open (i.e. were closed)
        Children.Array children = (Children.Array) root.getChildren();
        List toRemove = new LinkedList();
        Set remaining = new HashSet();
        for( Enumeration en = children.nodes() ; en.hasMoreElements(); ) {
            Object o = en.nextElement();
            if( o instanceof ProjectNode ) {
                CsmProject p = ((ProjectNode) o).getProject();
                if( modelAll.contains(p) ) {
                    remaining.add(p);
                }
                else {
                    toRemove.add(o);
                }
            }
        }
        if( ! toRemove.isEmpty() ) {
            Node[] nodes = (Node[]) toRemove.toArray(new Node[toRemove.size()]);
            for (int i = 0; i < nodes.length; i++) {
                if( nodes[i] instanceof  BaseNode ) {
                    ((BaseNode) nodes[i]).dismiss();
                }
            }
            children.remove(nodes);
        }
        
        List toAdd = new LinkedList();
        for( Iterator iter = modelAll.iterator(); iter.hasNext(); ) {
            CsmProject p = (CsmProject) iter.next();
            if( ! remaining.contains(p) ) {
                toAdd.add(new ProjectNode(p));
            }
        }
        
        if( ! toAdd.isEmpty() ) {
            children.add( (Node[]) toAdd.toArray(new Node[toAdd.size()]) );
        }
    }
    
    public void scheduleUpdate(CsmChangeEvent e) {
	updater.scheduleUpdate(e);
    }
    
    public void dispose() {
        requestProcessor.stop();
    }
    
    public void update(CsmChangeEvent e) {
	update(getRoot(), e);
    }
    
    private void update(Node node, CsmChangeEvent e) {
	if( node instanceof BaseNode ) {
	    ((BaseNode) node).update(e);
	}
        else if( node instanceof ProjectNode ) {
            if( e.getChangedProjects().contains(((ProjectNode) node).getProject()) ) {
                ((ProjectNode) node).update(e);
            }
        }
	else {
	    for( Enumeration children = node.getChildren().nodes(); children.hasMoreElements(); ) {
		update((Node) children.nextElement(), e);
	    }
	}
    }
    
    private void dump(Project[] projects) {
        if( Diagnostic.DEBUG ) {
            Diagnostic.trace("Dumping projects:");
            for( int i = 0; i < projects.length; i++ ) {
                dump(projects[i]);
            }
        }
    }
    
    private void dump(Project p) {
        if( Diagnostic.DEBUG ) {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            Diagnostic.trace("Project " + pi.getName() + " (" + pi.getDisplayName() + ')');
            SourceGroup[] sg = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            Diagnostic.trace("  Source groups are");
            for( int i = 0; i < sg.length; i++ ) {
                Diagnostic.trace("    " + sg[i].getName() + " (" + sg[i].getDisplayName() + ") " + sg[i].getRootFolder().getName());
            }
        }
    }
    
    private AbstractNode root;
    
}
