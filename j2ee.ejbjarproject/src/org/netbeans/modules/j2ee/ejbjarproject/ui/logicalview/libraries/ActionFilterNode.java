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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.libraries;


import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
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
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.openide.nodes.FilterNode.Children;
import org.openide.util.Exceptions;


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
    static ActionFilterNode create (Node original, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, 
                                    String classPathId, String entryId, String includedLibrariesElement) {
        DataObject dobj = original.getLookup().lookup(DataObject.class);
        assert dobj != null;
        FileObject root =  dobj.getPrimaryFile();
        Lookup lkp = new ProxyLookup (new Lookup[] {original.getLookup(), helper == null ?
            Lookups.singleton (new JavadocProvider(root,root)) :
            Lookups.fixed (new Object[] {new Removable (helper, eval, refHelper, classPathId, entryId, includedLibrariesElement),
            new JavadocProvider(root,root)})});
        return new ActionFilterNode (original, helper == null ? MODE_PACKAGE : MODE_ROOT, root, lkp);
    }



    private ActionFilterNode (Node original, int mode, FileObject cpRoot, FileObject resource) {
        this (original, mode, cpRoot,
            new ProxyLookup(new Lookup[] {original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))}));
    }

    private ActionFilterNode (Node original, int mode) {
        super (original, new ActionFilterChildren (original, mode, null));
        this.mode = mode;
    }

    private ActionFilterNode (Node original, int mode, FileObject root, Lookup lkp) {
        super (original, new ActionFilterChildren (original, mode,root),lkp);
        this.mode = mode;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result = initActions();        
        return result;
    }


    @Override
    public Action getPreferredAction() {
        if (mode == MODE_FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && (actions[0] instanceof OpenAction || actions[0] instanceof EditAction)) {
                return actions[0];
            }
        }
        return null;
    }

    private Action[] initActions () {
        if (actionCache == null) {
            List<Action> result = new ArrayList<Action>(2);
            if (mode == MODE_FILE) {
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof OpenAction || superActions[i] instanceof EditAction) {
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
            actionCache = result.toArray(new Action[result.size()]);
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

        @Override
        protected Node[] createNodes(Node n) {
            switch (mode) {
                case MODE_ROOT:
                case MODE_PACKAGE:
                    DataObject dobj = n.getCookie(org.openide.loaders.DataObject.class);
                    if (dobj == null) {
                        assert false : "DataNode without DataObject in Lookup";  //NOI18N
                        return new Node[0];
                    }
                    else if (dobj.getPrimaryFile().isFolder()) {
                        return new Node[] {new ActionFilterNode (n, MODE_PACKAGE,cpRoot,dobj.getPrimaryFile())};
                    }
                    else {
                        return new Node[] {new ActionFilterNode (n, MODE_FILE,cpRoot,dobj.getPrimaryFile())};
                    }
                case MODE_FILE:
                case MODE_FILE_CONTENT:
                    return new Node[] {new ActionFilterNode (n, MODE_FILE_CONTENT)};
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
                URL pageURL;
                if (relativeName.length()==0) {
                    pageURL = ShowJavadocAction.findJavadoc ("overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc ("index.html",urls); //NOI18N
                    }                    
                }
                else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?                    
                    pageURL = ShowJavadocAction.findJavadoc ("package-summary.html",urls); //NOI18N
                }
                else {
                    String javadocFileName = relativeName.substring(0,relativeName.lastIndexOf('.'))+".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc (javadocFileName,urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
            } catch (FileStateInvalidException fsi) {
                Exceptions.printStackTrace(fsi);
            }
        }
    }

   private static class Removable implements RemoveClassPathRootAction.Removable {

       private final UpdateHelper helper;
       private final PropertyEvaluator eval;
       private final ReferenceHelper refHelper;
       private final String classPathId;
       private final String entryId;
       private final String includedLibrariesElement;
       private final ClassPathSupport cs;

       Removable (UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, String classPathId, String entryId, String includedLibrariesElement) {
           this.helper = helper;
           this.eval = eval;
           this.refHelper = refHelper;
           this.classPathId = classPathId;
           this.includedLibrariesElement = includedLibrariesElement;
           this.entryId = entryId;
           
            this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), 
                                            EjbJarProjectProperties.WELL_KNOWN_PATHS, 
                                            EjbJarProjectProperties.LIBRARY_PREFIX, 
                                            EjbJarProjectProperties.LIBRARY_SUFFIX, 
                                            EjbJarProjectProperties.ANT_ARTIFACT_PREFIX );        
       }


       public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

       public Project remove() {
           // Different implementation than j2seproject's one, because
           // we need to remove the library entry from project.xml
           
           // The caller has write access to ProjectManager
           // and ensures the project will be saved.           
           
           boolean removed = false;
           EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
           String raw = props.getProperty (classPathId);
           List<ClassPathSupport.Item> resources = cs.itemsList( raw, includedLibrariesElement );
           for (Iterator<ClassPathSupport.Item> i = resources.iterator(); i.hasNext();) {
               ClassPathSupport.Item item = i.next();
               if (entryId.equals(EjbJarProjectProperties.getAntPropertyName(item.getReference()))) {
                   i.remove();
                   removed = true;
               }
           }
           if (removed) {
               String[] itemRefs = cs.encodeToStrings(resources.iterator(), includedLibrariesElement);
               props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
               props.setProperty(classPathId, itemRefs);
               helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

               //update lib references in private properties
               EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
               ArrayList<ClassPathSupport.Item> l = new ArrayList<ClassPathSupport.Item>();
               l.addAll(resources);
               EjbJarProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
               helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

               return FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
           } else {
               return null;
           }
       }
   }
}
