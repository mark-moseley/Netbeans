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

package org.netbeans.modules.apisupport.project.ui;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.actions.EditAction;
import org.openide.actions.FindAction;
import org.openide.loaders.DataObject;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;

// XXX this class is simplified version of the same class in the j2seproject.
// Get rid of it as soon as "some" Libraries Node API is provided.

/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction}. It also adds the {@link ShowJavadocAction}
 * to both file and package nodes.
 */
class ActionFilterNode extends FilterNode {
    
    private static final int MODE_PACKAGE = 2;
    private static final int MODE_FILE = 3;
    private static final int MODE_FILE_CONTENT = 4;
    
    private final int mode;
    private Action[] actionCache;
    
    /**
     * Creates new ActionFilterNode for class path root.
     * @param original the original node
     * @return ActionFilterNode
     */
    static ActionFilterNode create(Node original) {
        DataObject dobj = original.getLookup().lookup(DataObject.class);
        assert dobj != null;
        FileObject root =  dobj.getPrimaryFile();
        Lookup lkp = new ProxyLookup(original.getLookup(), Lookups.singleton(new JavadocProvider(root, root)));
        return new ActionFilterNode(original, MODE_PACKAGE, root, lkp);
    }
    
    private ActionFilterNode(Node original, int mode, FileObject cpRoot, FileObject resource) {
        this(original, mode, cpRoot,
                new ProxyLookup(original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))));
    }
    
    private ActionFilterNode(Node original, int mode) {
        super(original, new ActionFilterChildren(original, mode, null));
        this.mode = mode;
    }
    
    private ActionFilterNode(Node original, int mode, FileObject root, Lookup lkp) {
        super(original, new ActionFilterChildren(original, mode,root),lkp);
        this.mode = mode;
    }
    
    public Action[] getActions(boolean context) {
        Action[] result = initActions();
        return result;
    }
    
    public Action getPreferredAction() {
        if (mode == MODE_FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && (actions[0] instanceof OpenAction || actions[0] instanceof EditAction )) {
                return actions[0];
            }
        }
        return null;
    }
    
    private Action[] initActions() {
        if (actionCache == null) {
            List<Action> result = new ArrayList<Action>(2);
            if (mode == MODE_FILE) {
                for (Action superAction : super.getActions(false)) {
                    if (superAction instanceof OpenAction || superAction instanceof EditAction) {
                        result.add(superAction);
                    }
                }
                result.add(SystemAction.get(ShowJavadocAction.class));
            } else if (mode == MODE_PACKAGE) {
                result.add(SystemAction.get(ShowJavadocAction.class));
                for (Action superAction : super.getActions(false)) {
                    if (superAction instanceof FindAction) {
                        result.add(superAction);
                    }
                }
            }
            actionCache = result.toArray(new Action[result.size()]);
        }
        return actionCache;
    }
    
    private static class ActionFilterChildren extends FilterNode.Children {
        
        private final int mode;
        private final FileObject cpRoot;
        
        ActionFilterChildren(Node original, int mode, FileObject cpRooot) {
            super(original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }
        
        protected Node[] createNodes(Node n) {
            switch (mode) {
                case MODE_PACKAGE:
                    DataObject dobj = n.getCookie(DataObject.class);
                    if (dobj == null) {
                        assert false : "DataNode without DataObject in Lookup";  //NOI18N
                        return new Node[0];
                    } else if (dobj.getPrimaryFile().isFolder()) {
                        return new Node[] {new ActionFilterNode(n, MODE_PACKAGE, cpRoot, dobj.getPrimaryFile())};
                    } else {
                        return new Node[] {new ActionFilterNode(n, MODE_FILE, cpRoot, dobj.getPrimaryFile())};
                    }
                case MODE_FILE:
                case MODE_FILE_CONTENT:
                    return new Node[] {new ActionFilterNode(n, MODE_FILE_CONTENT)};
                default:
                    assert false : "Unknown mode";  //NOI18N
                    return new Node[0];
            }
        }
        
    }
    
    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {
        
        private final FileObject cpRoot;
        private final FileObject resource;
        
        JavadocProvider(FileObject cpRoot, FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
        }
        
        public boolean hasJavadoc() {
            try {
                boolean rNotNull = resource != null;
                int jLength = JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots().length;
                return  rNotNull && jLength > 0;
            } catch (FileStateInvalidException fsi) {
                return false;
            }
        }
        
        public void showJavadoc() {
            try {
                String relativeName = FileUtil.getRelativePath(cpRoot, resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots();
                URL pageURL;
                if (relativeName.length() == 0) {
                    pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc("index.html",urls); //NOI18N
                    }
                } else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?
                    pageURL = ShowJavadocAction.findJavadoc(relativeName + "/package-summary.html", urls); //NOI18N
                } else {
                    String javadocFileName = relativeName.substring(0, relativeName.lastIndexOf('.')) + ".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc(javadocFileName, urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
            } catch (FileStateInvalidException fsi) {
                ErrorManager.getDefault().notify(fsi);
            }
        }
        
    }
    
}
