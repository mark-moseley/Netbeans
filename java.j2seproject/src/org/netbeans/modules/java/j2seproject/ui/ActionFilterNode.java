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
package org.netbeans.modules.java.j2seproject.ui;


import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import org.openide.ErrorManager;
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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.java.j2seproject.UpdateHelper;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;


/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction} It also adds the {@link ShowJavadocAction}
 * to both file and package nodes. It also adds {@link RemoveClassPathRootAction} to
 * class path roots.
 */
class ActionFilterNode extends FilterNode {

    private static final int MODE_ROOT = 1;
    private static final int MODE_PACKAGE = 2;
    private static final int MODE_FILE = 3;
    private static final int MODE_FILE_CONTENT = 4;

    private final int mode;
    private Action[] actionCache;

    /**
     * Creates new ActionFilterNode for class path root
     * @param original the original node
     * @param helper used for implementing {@link RemoveClassPathRootAction.Removable} or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @param classPathId ant property name of classpath to which these classpath root belongs or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @param entryId ant property name of this classpath root or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @return ActionFilterNode
     */
    static ActionFilterNode create (Node original, UpdateHelper helper, String classPathId, String entryId) {
        DataObject dobj = (DataObject) original.getLookup().lookup(DataObject.class);
        assert dobj != null;
        FileObject root =  dobj.getPrimaryFile();
        Lookup lkp = new ProxyLookup (new Lookup[] {original.getLookup(), helper == null ?
            Lookups.singleton (new JavadocProvider(root,root)) :
            Lookups.fixed (new Object[] {new Removable (helper, classPathId, entryId),
            new JavadocProvider(root,root)})});
        return new ActionFilterNode (original, helper == null ? MODE_PACKAGE : MODE_ROOT, root, lkp);
    }



    private ActionFilterNode (Node original, int mode, FileObject cpRoot, FileObject resource) {
        this (original, mode, cpRoot,
            cpRoot == null ? original.getLookup() :
            new ProxyLookup(new Lookup[] {original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))}));
    }

    private ActionFilterNode (Node original, int mode, FileObject root, Lookup lkp) {
        super (original, new ActionFilterChildren (original, mode,root),lkp);
        this.mode = mode;
    }

    public Action[] getActions(boolean context) {
        Action[] result;
        if (!context) {
            result = initActions();
        }
        else {
            result = new Action[0];
        }
        return result;
    }


    public Action getPreferredAction() {
        if (mode == MODE_FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && (actions[0] instanceof OpenAction)) {
                return actions[0];
            }
        }
        return null;
    }

    private Action[] initActions () {
        if (actionCache == null) {
            List result = new ArrayList(2);
            if (mode == MODE_FILE) {
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof OpenAction) {
                        result.add (superActions[i]);
                    }
                }
                result.add (SystemAction.get(ShowJavadocAction.class));
            }
            else if (mode == MODE_PACKAGE || mode == MODE_ROOT) {
                result.add (SystemAction.get(ShowJavadocAction.class));
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add (superActions[i]);
                    }
                }                
                if (mode == MODE_ROOT) {
                    result.add (SystemAction.get(RemoveClassPathRootAction.class));
                }
            }            
            actionCache = (Action[]) result.toArray(new Action[result.size()]);
        }
        return actionCache;
    }

    private static class ActionFilterChildren extends Children {

        private final int mode;
        private final FileObject cpRoot;

        ActionFilterChildren (Node original, int mode, FileObject cpRooot) {
            super (original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }

        protected Node[] createNodes(Object key) {
            Node n = (Node) key;
            switch (mode) {
                case MODE_ROOT:
                case MODE_PACKAGE:
                    DataObject dobj = (DataObject) n.getCookie(org.openide.loaders.DataObject.class);
                    if (dobj == null) {
                        assert false : "DataNode without DataObject in Lookup";  //NOI18N
                        return new Node[0];
                    }
                    else if (dobj.getPrimaryFile().isFolder()) {
                        return new Node[] {new ActionFilterNode ((Node)key, MODE_PACKAGE,cpRoot,dobj.getPrimaryFile())};
                    }
                    else {
                        return new Node[] {new ActionFilterNode ((Node)key, MODE_FILE,cpRoot,dobj.getPrimaryFile())};
                    }
                case MODE_FILE:
                case MODE_FILE_CONTENT:
                    return new Node[] {new ActionFilterNode ((Node)key, MODE_FILE_CONTENT,null,(FileObject)null)};
                default:
                    assert false : "Unknown mode";  //NOI18N
                    return new Node[0];
            }
        }
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final FileObject cpRoot;
        private final FileObject resource;

        JavadocProvider (FileObject cpRoot, FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
        }

        public boolean hasJavadoc() {
            try {
                return resource != null && JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots().length>0;
            } catch (FileStateInvalidException fsi) {
                return false;
            }
        }

        public void showJavadoc() {
            try {
                String relativeName = FileUtil.getRelativePath(cpRoot,resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots();
                String javadocFileName;
                if (relativeName.length()==0) {
                    javadocFileName = "/index.html";    //NOI18N
                }
                else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?
                    javadocFileName = relativeName + "/package-summary.html";  //NOI18N
                }
                else {
                    javadocFileName = relativeName.substring(0,relativeName.lastIndexOf('.'))+".html"; //NOI18Ns
                }
                ShowJavadocAction.showJavaDoc(
                    ShowJavadocAction.findJavadoc (javadocFileName,urls),relativeName.replace('/','.'));  //NOI18N
            } catch (FileStateInvalidException fsi) {
                ErrorManager.getDefault().notify (fsi);
            }
        }
    }

   private static class Removable implements RemoveClassPathRootAction.Removable {

       private final UpdateHelper helper;
       private final String classPathId;
       private final String entryId;

       Removable (UpdateHelper helper, String classPathId, String entryId) {
           this.helper = helper;
           this.classPathId = classPathId;
           this.entryId = entryId;
       }


       public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

       public void remove() {
           ProjectManager.mutex().writeAccess ( new Runnable () {
               public void run() {
                   EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                   String cp = props.getProperty (classPathId);
                   if (cp != null) {
                       String[] entries = PropertyUtils.tokenizePath(cp);
                       StringBuffer result = new StringBuffer();
                       for (int i=0; i<entries.length; i++) {
                           if (!entryId.equals(J2SEProjectProperties.getAntPropertyName(entries[i]))) {
                               if (result.length()>0) {
                                   result.append(File.pathSeparatorChar);
                               }
                               result.append (entries[i]);
                           }
                       }
                       props.put (classPathId, result.toString());
                       helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                   }
               }
           });
       }
   }
}
