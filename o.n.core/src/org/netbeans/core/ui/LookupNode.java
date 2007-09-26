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

package org.netbeans.core.ui;

import javax.swing.Action;
import org.openide.actions.FileSystemAction;
import org.openide.actions.MoveDownAction;
import org.openide.actions.MoveUpAction;
import org.openide.actions.NewTemplateAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.TemplateWizard;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/** Node that displays the content of Services directory and let's user 
* customize it.
*
* @author Jaroslav Tulach
*/
public class LookupNode extends DataFolder.FolderNode implements NewTemplateAction.Cookie {
    /** extended attribute that signals that this object should not be visible to the user */
    private static final String EA_HIDDEN = "hidden"; // NOI18N
    private static final String EA_HELPCTX = "helpID"; // NOI18N
    /** This is quite unsafe, but it's the only way how to test that we got uncustomized
     * InstanceDataNode's help (which is really of no use to the user).
     */
    private static final HelpCtx INSTANCE_DEFAULT_HELP = new HelpCtx("org.openide.loaders.InstanceDataObject"); // NOI18N
    /** This is quite unsafe, but it's the only way how to test that we got uncustomized
     * DataFolder's help (which is really of no use to the user).
     */
    private static final HelpCtx FOLDER_DEFAULT_HELP = new HelpCtx("org.openide.loaders.DataFolder"); // NOI18N
    private static final String PREFIX_SETTING_CATEGORIES = "UI"; // NOI18N

    /** Constructs this node with given node to filter.
    */
    public LookupNode (DataFolder folder) {
        folder.super(new Ch(folder));
//        setShortDescription(bundle.getString("CTL_Lookup_hint"));
//        super.setIconBase ("/org/netbeans/modules/url/Lookup"); // NOI18N
        getCookieSet ().add (this);
    }
    
    /** is this node representing a setting ui category? */
    private boolean isUISettingCategoryNode() {
        DataFolder df = (DataFolder) super.getCookie (DataFolder.class);
        if (df != null) {
            String name = df.getPrimaryFile ().getPath();
            return name.startsWith(PREFIX_SETTING_CATEGORIES);
        } else return false;
    }
    
    public HelpCtx getHelpCtx () {
        Object o = getDataObject().getPrimaryFile().getAttribute(EA_HELPCTX);
        if (o != null) {
            return new HelpCtx(o.toString());
        }
        // now try the original DataObject (assume it is a folder-thing)
        HelpCtx ctx = getDataObject().getHelpCtx();
        if (ctx != null &&
            ctx != HelpCtx.DEFAULT_HELP &&
            !FOLDER_DEFAULT_HELP.equals(ctx)) {
            return ctx;
        }
        // try the parent node:
        Node n = getParentNode();
        if (n != null)
            ctx = n.getHelpCtx();
        return ctx;
    }


    public final Action[] getActions(boolean context) {
        if (isUISettingCategoryNode()) {
            return new Action[0];
        } else {
            return new Action[] {
                SystemAction.get(FileSystemAction.class),
                null,
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(MoveUpAction.class),
                SystemAction.get(MoveDownAction.class),
                SystemAction.get(ReorderAction.class),
                null,
                SystemAction.get(NewTemplateAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };
        }
    }

    /** @return empty property sets. *
    public PropertySet[] getPropertySets () {
        return NO_PROPERTIES;
    }
     */

    public final Node.Cookie getCookie (Class type) {
        if (isUISettingCategoryNode()) return null;
        return super.getCookie (type);
    }

    /** NewTemplateAction.Cookie method implementation to create the desired
     * template wizard for this node.
     */
    public final TemplateWizard getTemplateWizard () {
        TemplateWizard templateWizard = createWizard ();
        
        templateWizard.setTemplatesFolder (findFolder (root (), findName (), true));
        templateWizard.setTargetFolder (findFolder (root (), findName (), false));
        return templateWizard;
    }
    
    /** Allows subclasses to create special wizard.
     */
    protected TemplateWizard createWizard () {
        return new TemplateWizard ();
    }

    /** A method to allow subclasses to create different child for folder.
    * @param folder the folder to create child for
    */
    protected LookupNode createChild (DataFolder folder) {
        return new LookupNode (folder);
    }
    
    /** A method to allow subclasses to create different child for any other node then folder.
    * @param node to create child for
    */
    protected Node createChild (Node node) {
        return node.cloneNode ();
    }

    /** Gets the root from children on system filesystem and in 
    * templates folder.
    */
    protected String root () {
        return "Services"; // NOI18N
    }

    /** Finds a prefix for templates.
    * @return prefix 
    */
    private static String prefTemplates (String root) {
        return "Templates/" + root; // NOI18N
    }

    /** Finds a prefix for objects.
    */
    private static String prefObjects (String root) {
        return root;
    }
    
    /** Finds name of the node by extracting the begin of nodes.
     * @return the string name
     */
    private String findName () {
        DataFolder df = (DataFolder)getCookie (DataFolder.class);
        if (df == null) {
            return "";
        }
        String name = df.getPrimaryFile ().getPath();
        if (name.startsWith (prefObjects (root ()))) {
            name = name.substring (prefObjects (root ()).length ());
        }
        return name;
    }

    /** Locates the right folder for given service name.
     * @param name of the resource
     * @param template folder for templates or for instances?
     * @return the folder
     */
    static DataFolder findFolder (String root, String name, boolean template) {
        try {
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            if (template) {
                name = '/' + prefTemplates (root) + name;
            } else {
                name = '/' + prefObjects (root) + name;
            }
            FileObject fo = fs.findResource (name);
            
            if (fo == null && template) {
                // we do not create template directories, if it is missing
                // we use the root services template directory 
                name = prefTemplates (root);
            }
            
            if (fo == null) {
                // if the directory is missing, create new one
                fo = FileUtil.createFolder (fs.getRoot (), name);
            }
            
            return DataFolder.findFolder (fo);
        } catch (java.io.IOException ex) {
            throw (IllegalStateException) new IllegalStateException(ex.toString()).initCause(ex);
        }
    }

    /** Refreshes the node for given key.
    * @param node the original node
    */
    public final void refreshKey (Node node) {
        ((Ch)getChildren()).refreshKey0(node);
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    protected Sheet createSheet() {
        return new Sheet();
    }
    
    public boolean canRename() {
        return false;
    }

    public Node cloneNode () {
        return new LookupNode((DataFolder)super.getCookie(DataFolder.class));
    }
    
    
    /** Misleading name: need not be a leaf at all. */
    private static final class Leaf extends FilterNode {
        DataObject  data;
        Node parent;
        
        Leaf (Node node, DataObject data, Node parent) {
            super(node, ((data instanceof XMLDataObject) || node.isLeaf()) ? Children.LEAF : new FilterNode.Children(node));
            this.data = data;
            this.parent = parent;
        }
        
        // #17920: Index cookie works only when equality works
        public boolean equals(Object o) {
            return this == o || getOriginal().equals(o) || (o != null && o.equals(getOriginal()));
        }
        public int hashCode() {
            return getOriginal().hashCode();
        }
        
        public HelpCtx getHelpCtx() {
            Object o = data.getPrimaryFile().getAttribute(EA_HELPCTX);
            if (o != null) {
                return new HelpCtx(o.toString());
            }
            // now try the original DataObject (assume it is a folder-thing)
            HelpCtx ctx = getOriginal().getHelpCtx();
            if (ctx != null &&
                ctx != HelpCtx.DEFAULT_HELP &&
                !INSTANCE_DEFAULT_HELP.equals(ctx)) {
                return ctx;
            }
            // try the parent node:
            Node n = getParentNode();
            if (n == null)
                n = parent;
            if (n != null)
                ctx = n.getHelpCtx();
            return ctx;
        }
        
        public Action getPreferredAction() {
            return null;
        }

    }
    

    /** Children for the LookupNode. Creates LookupNodes or
    * LookupItemNodes as filter subnodes...
    */
    private static final class Ch extends FilterNode.Children {
        /** @param or original node to take children from */
        public Ch (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overridden to provide package-private access. */
        void refreshKey0(Node node) {
            refreshKey(node);
        }

        /** Overridden, returns LookupNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return LookupNode filter of the original node
        */
        @Override
        protected Node[] createNodes(Node node) {
            DataObject obj = (DataObject)node.getCookie(DataObject.class);
            //System.err.println("obj="+obj+" node="+node+" hidden="+(obj==null?null:obj.getPrimaryFile ().getAttribute (EA_HIDDEN)));
            
            if (
                obj != null && Boolean.TRUE.equals (obj.getPrimaryFile ().getAttribute (EA_HIDDEN))
            ) {
                return new Node[0];
            }
            
            LookupNode parent = (LookupNode)getNode ();
            
            if (obj != null) {
                if (obj instanceof DataFolder && node.equals (obj.getNodeDelegate ())) {
                    node = parent.createChild ((DataFolder)obj);
                    return new Node[] { node };
                } else if (obj instanceof DataShadow) {
                    DataObject orig = ((DataShadow) obj).getOriginal();
                    FileObject fo = orig.getPrimaryFile();
                    
                    // if folder referenced by shadow is empty do not show it
                    if (fo.isFolder() && !fo.getChildren(false).hasMoreElements()) return null;
                    
                    if (orig instanceof DataFolder) {
                        return new Node[] { 
                            parent.createChild ((DataFolder) orig)
                        };
                    } else {
                        obj = orig;
                        node = orig.getNodeDelegate();
                    }
                }
                node = new Leaf(node, obj, parent);
            }
            
            node = parent.createChild (node);

            return new Node[] { node };
        }

    }

}
