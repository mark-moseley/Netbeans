/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.analyzer.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.text.Line;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class Nodes {
    
    public static Node constructSemiLogicalView(Map<FileObject, List<ErrorDescription>> errors, List<FixDescription> fixesOut) {
        Map<Project, Map<FileObject, List<ErrorDescription>>> projects = new HashMap<Project, Map<FileObject, List<ErrorDescription>>>();
        
        for (FileObject file : errors.keySet()) {
            Project project = FileOwnerQuery.getOwner(file);
            
            if (project == null) {
                Logger.getLogger(Nodes.class.getName()).log(Level.WARNING, "Cannot find project for: {0}", FileUtil.getFileDisplayName(file));
            }
            
            Map<FileObject, List<ErrorDescription>> projectErrors = projects.get(project);
            
            if (projectErrors == null) {
                projects.put(project, projectErrors = new HashMap<FileObject, List<ErrorDescription>>());
            }
            
            projectErrors.put(file, errors.get(file));
        }
        
        projects.remove(null);
        
        List<Node> nodes = new LinkedList<Node>();
        Map<ErrorDescription, List<FixDescription>> errors2Fixes = new HashMap<ErrorDescription, List<FixDescription>>();
        
        for (Project p : projects.keySet()) {
            nodes.add(constructSemiLogicalView(p, projects.get(p), errors2Fixes));
        }
        
        for (List<FixDescription> descs : errors2Fixes.values()) {
            fixesOut.addAll(descs);
        }
//        Children.Array subNodes = new Children.Array();
//        
//        subNodes.add(nodes.toArray(new Node[0]));
        
        return new AbstractNode(new DirectChildren(nodes));
    }
    
    private static Node constructSemiLogicalView(Project p, Map<FileObject, List<ErrorDescription>> errors, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        Node view;
        
        if (lvp != null) {
            view = lvp.createLogicalView();
        } else {
            try {
                view = DataObject.find(p.getProjectDirectory()).getNodeDelegate();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return new AbstractNode(Children.LEAF);
            }
        }
        
        Map<Node, List<ErrorDescription>> fileNodes = new HashMap<Node, List<ErrorDescription>>();
        
        for (FileObject file : errors.keySet()) {
            List<ErrorDescription> eds = errors.get(file);
            
            fileNodes.put(locateChild(view, lvp, file), eds);
            
            for (ErrorDescription e : eds) {
                List<FixDescription> desc = new LinkedList<FixDescription>();

                for (Fix f : e.getFixes().getFixes()) {
                    desc.add(new FixDescription(e, f));
                }
                errors2Fixes.put(e, desc);
            }
        }
        
        errors = Collections.unmodifiableMap(new HashMap<FileObject, List<ErrorDescription>>(errors));
        
        return new Wrapper(view, fileNodes, errors2Fixes);
    }
    
    private static Node locateChild(Node parent, LogicalViewProvider lvp, FileObject file) {
        if (lvp != null) {
            return lvp.findPath(parent, file);
        }
        
        throw new UnsupportedOperationException("Not done yet");
    }

    private static class Wrapper extends FilterNode {

        public Wrapper(Node orig, Map<Node, List<ErrorDescription>> fileNodes, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
            super(orig, new WrapperChildren(orig, fileNodes, errors2Fixes), lookupForNode(orig, fileNodes, errors2Fixes));
        }
        
        public Wrapper(Node orig, List<ErrorDescription> errors, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
            super(orig, new ErrorDescriptionChildren(errors, errors2Fixes), lookupForNode(orig, errors, errors2Fixes));
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
        
    }
    
    private static Lookup lookupForNode(Node n, Map<Node, List<ErrorDescription>> fileNodes, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
      List<FixDescription> fixes = new LinkedList<FixDescription>();
        
        for (Map.Entry<Node, List<ErrorDescription>> e : fileNodes.entrySet()) {
            if (isParent(n, e.getKey())) {
                for (ErrorDescription ed : e.getValue()) {
                    for (FixDescription f : errors2Fixes.get(ed)) {
                        fixes.add(f);
                    }
                }
            }
        }
        
        return Lookups.fixed((FixDescription[]) fixes.toArray(new FixDescription[0]));
    }
    
    private static Lookup lookupForNode(Node n, List<ErrorDescription> errors, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
        List<FixDescription> fixes = new LinkedList<FixDescription>();

        for (ErrorDescription ed : errors) {
            for (FixDescription f : errors2Fixes.get(ed)) {
                fixes.add(f);
            }
        }

        return Lookups.fixed((FixDescription[]) fixes.toArray(new FixDescription[0]));
    }
    
    private static boolean isParent(Node parent, Node child) {
        if (NodeOp.isSon(parent, child)) {
            return true;
        }

        Node p = child.getParentNode();

        if (p == null) {
            return false;
        }

        return isParent(parent, p);
    }
        
    private static class WrapperChildren extends Children.Keys<Node> {

        private final Node orig;
        private final java.util.Map<Node, List<ErrorDescription>> fileNodes;
        private final java.util.Map<ErrorDescription, List<FixDescription>> errors2Fixes;

        public WrapperChildren(Node orig, java.util.Map<Node, List<ErrorDescription>> fileNodes, java.util.Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
            this.orig = orig;
            this.fileNodes = fileNodes;
            this.errors2Fixes = errors2Fixes;
            
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            doSetKeys();
        }

        private void doSetKeys() {
            Node[] nodes = orig.getChildren().getNodes(true);
            List<Node> toSet = new LinkedList<Node>();
            
            OUTER: for (Node n : nodes) {
                for (Node c : fileNodes.keySet()) {
                    if (n == c || isParent(n, c)) {
                        toSet.add(n);
                        continue OUTER;
                    }
                }
            }
            
            setKeys(toSet);
        }
        
        @Override
        protected Node[] createNodes(Node key) {
            if (fileNodes.containsKey(key)) {
                return new Node[] {new Wrapper(key, fileNodes.get(key), errors2Fixes)};
            }
            return new Node[] {new Wrapper(key, fileNodes, errors2Fixes)};
        }
        
    }
    
    private static final class DirectChildren extends Children.Keys<Node> {

        public DirectChildren(Collection<Node> nodes) {
            setKeys(nodes);
        }
        
        @Override
        protected Node[] createNodes(Node key) {
            return new Node[] {key};
        }
    }
    
    private static final class ErrorDescriptionChildren extends Children.Keys<ErrorDescription> {

        private java.util.Map<ErrorDescription, List<FixDescription>> errors2Fixes;

        public ErrorDescriptionChildren(List<ErrorDescription> ed, java.util.Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
            setKeys(ed);
            this.errors2Fixes = errors2Fixes;
        }
        
        @Override
        protected Node[] createNodes(ErrorDescription key) {
            List<Node> fixes = new LinkedList<Node>();
            
            for (FixDescription fd : errors2Fixes.get(key)) {
                fixes.add(new FixNode(key, fd));
            }
            
            return fixes.toArray(new Node[0]);
//            return new Node[] {new ErrorDescriptionNode(key, errors2Fixes)};
        }
        
    }
    
    private static final class ErrorDescriptionNode extends AbstractNode {
        private ErrorDescription ed;

        public ErrorDescriptionNode(ErrorDescription ed, Map<ErrorDescription, List<FixDescription>> errors2Fixes) {
            super(new FixChildren(ed, errors2Fixes.get(ed)));
            this.ed = ed;
            int line = -1;
            try {
                line = ed.getRange().getBegin().getLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            setDisplayName((line != (-1) ? (line + ":") : "") + ed.getDescription());
            
            setIconBaseWithExtension("org/netbeans/modules/java/hints/analyzer/ui/warning-glyph.gif");
        }

    }

    private static final class FixChildren extends Children.Keys<FixDescription> {

        private ErrorDescription ed;
        
        public FixChildren(ErrorDescription ed, List<FixDescription> eds) {
            this.ed = ed;
            setKeys(eds);
        }
        
        @Override
        protected Node[] createNodes(FixDescription key) {
            return new Node[] {new FixNode(ed, key)};
        }
        
    }

    private static final class FixNode extends AbstractNode implements ChangeListener {
        private FixDescription fix;

        public FixNode(ErrorDescription ed, FixDescription fix) {
            super(Children.LEAF, Lookups.fixed(new OpenCookieImpl(ed), fix));
            this.fix = fix;

            int line = -1;
            try {
                line = ed.getRange().getBegin().getLine();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            setDisplayName((line != (-1) ? (line + ":") : "") + fix.getText() + "(" + ed.getDescription() + ")");
            setIconBaseWithExtension("org/netbeans/modules/java/hints/analyzer/ui/suggestion.png");
            
            fix.addChangeListener(this);
        }

        @Override
        public String getHtmlDisplayName() {
//            System.err.println("fix.isFixed()=" + fix.isFixed());
            if (fix.isFixed()) {
                return "<html><s>" + getDisplayName();
            } else {
                return "<html>" + getDisplayName();
            }
        }

        public void stateChanged(ChangeEvent e) {
//            System.err.println("state changed");
            fireDisplayNameChange(null, null);
        }

        @Override
        public Action getPreferredAction() {
            return OpenAction.get(OpenAction.class);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                OpenAction.get(OpenAction.class),
            };
        }
        
    }
    
    private static final class OpenCookieImpl implements OpenCookie {

        private ErrorDescription ed;

        public OpenCookieImpl(ErrorDescription ed) {
            this.ed = ed;
        }
        
        public void open() {
            try {
                PositionRef pos = ed.getRange().getBegin();
                int line = pos.getLine();
                int column = pos.getColumn();
                Line l = pos.getCloneableEditorSupport().getLineSet().getCurrent(line);

                l.show(Line.SHOW_GOTO, column);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
