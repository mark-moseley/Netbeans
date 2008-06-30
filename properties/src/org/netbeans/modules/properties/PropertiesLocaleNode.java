/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.properties;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;

import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.EditAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.NewAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.SaveAsTemplateAction;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** 
 * Node representing a single properties file.
 *
 * @see  PropertiesDataNode
 * @author Ian Formanek
 * @author Marian Petras
 */
public final class PropertiesLocaleNode extends FileEntryNode
                                        implements CookieSet.Factory,
                                                   Node.Cookie {

    /** Icon base for the <code>PropertiesDataNode</code> node. */
    private static final String LOCALE_ICON_BASE = "org/netbeans/modules/properties/propertiesLocale.gif"; // NOI18N

    
    /** Creates a new PropertiesLocaleNode for the given locale-specific file */
    public PropertiesLocaleNode (PropertiesFileEntry fe) {
        super(fe, fe.getChildren());
        setDisplayName(Util.getLocaleLabel(fe));
        
        setIconBaseWithExtension(LOCALE_ICON_BASE);        
        setShortDescription(messageToolTip());

        getCookieSet().add(PropertiesOpen.class, this);
        getCookieSet().add(fe.getDataObject());
    }
            
    /** Implements <code>CookieSet.Factory</code> interface method. */
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T createCookie(Class<T> clazz) {
        if(clazz.isAssignableFrom(PropertiesOpen.class)) {
            return (T) ((PropertiesDataObject) getFileEntry().getDataObject()).getOpenSupport();
        } else {
            return null;
        }
    }
    
    /** Lazily initialize set of node's actions.
     * Overrides superclass method.
     *
     * @return array of actions for this node
     */
    @Override
    protected SystemAction[] createActions () {
        return new SystemAction[] {
            SystemAction.get(EditAction.class),
            SystemAction.get(OpenAction.class),
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(LangRenameAction.class),
            null,
            SystemAction.get(NewAction.class),
            SystemAction.get(SaveAsTemplateAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        return getActions(false)[0];
    }

    /** Gets the name. Note: It gets only the local part of the name  (e.g. "de_DE_EURO").
     * Reason is to allow user change only this part of name by renaming (on Node).
     * Overrides superclass method. 
     *
     * @return locale part of name
     */
    @Override
    public String getName() {
        String localeName = "invalid"; // NOI18N
        if (getFileEntry().getFile().isValid() && !getFileEntry().getFile().isVirtual()) {
            localeName = Util.getLocaleSuffix (getFileEntry());
            if (localeName.length() > 0) {
                if (localeName.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR) {
                    localeName = localeName.substring(1);
                }
            }
        }
        return localeName;
    }
    
    /** Sets the system name. Overrides superclass method.
     *
     * @param name the new name
     */
    @Override
    public void setName (String name) {
        if(!name.startsWith(getFileEntry().getDataObject().getPrimaryFile().getName())) {
            name = Util.assembleName (getFileEntry().getDataObject().getPrimaryFile().getName(), name);
        }
        
        // new name is same as old one, do nothing
        if (name.equals(super.getName())) {
            return;
        }

        super.setName (name);
        setDisplayName(Util.getLocaleLabel(getFileEntry()));
        setShortDescription(messageToolTip());
    }

    /** Gets tooltip message for this node. Helper method. */
    private String messageToolTip () {
        FileObject fo = getFileEntry().getFile();
        return FileUtil.getFileDisplayName(fo);
    }
    
    /** This node can be renamed. Overrides superclass method. */
    @Override
    public boolean canRename() {
        return getFileEntry().isDeleteAllowed ();
    }

    /** Returns all the item in addition to "normal" cookies. Overrides superclass method. */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cls) {
        if (cls.isInstance(getFileEntry())) {
            return (T) getFileEntry();
        }
        if (cls == PropertiesLocaleNode.class) {
            return (T) this;
        }
        return super.getCookie(cls);
    }

    /** List new types that can be created in this node. Overrides superclass method.
     * @return new types
     */
    @Override
    public NewType[] getNewTypes () {
        return new NewType[] {
            new NewType() {

                /** Getter for name property. */
            @Override
                public String getName() {
                    return NbBundle.getBundle(PropertiesLocaleNode.class).getString("LAB_NewPropertyAction");
                }
                
                /** Gets help context. */ 
            @Override
                public HelpCtx getHelpCtx() {
                    return new HelpCtx(Util.HELP_ID_ADDING);
                }

                /** Creates new type. */
                public void create() throws IOException {
                    final PropertyPanel panel = new PropertyPanel();

                    Object selectedOption = DialogDisplayer.getDefault().notify(
                            new DialogDescriptor(
                                    panel,
                                    NbBundle.getMessage(BundleEditPanel.class,
                                                        "CTL_NewPropertyTitle")));  //NOI18N
                    if (selectedOption != NotifyDescriptor.OK_OPTION) {
                        return;
                    }

                    String key = panel.getKey();
                    String value = panel.getValue();
                    String comment = panel.getComment();

                    // add key to all entries
                    if(!((PropertiesFileEntry)getFileEntry()).getHandler().getStructure().addItem(key, value, comment)) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(
                                        NbBundle.getMessage(
                                                PropertiesLocaleNode.class, "MSG_KeyExists",
                                                key,
                                                Util.getLocaleLabel(getFileEntry())),
                                        NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
                
            } // End of annonymous class.
        };
    }

    /** Indicates if this node has a customizer. Overrides superclass method. 
     * @return true */
    @Override
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Gets node customizer. Overrides superclass method. */
    @Override
    public Component getCustomizer() {
        return new LocaleNodeCustomizer((PropertiesFileEntry)getFileEntry());
    }
    
    /** Creates paste types for this node. Overrides superclass method. */
    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        super.createPasteTypes(t, s);
        Element.ItemElem item;
        Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
        // cut
        if (n != null && n.canDestroy ()) {
            item = n.getCookie(Element.ItemElem.class);
            if (item != null) {
                // are we pasting into the same node
                Node n2 = getChildren().findChild(item.getKey());
                if (n == n2) {
                    return;
                }
                s.add(new KeyPasteType(item, n, KeyPasteType.MODE_PASTE_WITH_VALUE));
                s.add(new KeyPasteType(item, n, KeyPasteType.MODE_PASTE_WITHOUT_VALUE));
                return;
            }
        }
        // copy
        else {
            item = NodeTransfer.cookie(t, NodeTransfer.COPY, Element.ItemElem.class);
            if (item != null) {
                s.add(new KeyPasteType(item, null, KeyPasteType.MODE_PASTE_WITH_VALUE));
                s.add(new KeyPasteType(item, null, KeyPasteType.MODE_PASTE_WITHOUT_VALUE));
                return;
            }
        }
    }

    
    /** Paste type for keys. */
    private class KeyPasteType extends PasteType {
        
        /** Transferred item. */
        private Element.ItemElem item;

        /** The node to destroy or null. */
        private Node node;

        /** Paste mode. */
        int mode;

        /** Paste with value mode. */
        public static final int MODE_PASTE_WITH_VALUE = 1;
        
        /** Paste without value mode. */
        public static final int MODE_PASTE_WITHOUT_VALUE = 2;

        
        /** Constructs new <code>KeyPasteType</code> for the specific type of operation paste. */
        public KeyPasteType(Element.ItemElem item, Node node, int mode) {
            this.item = item;
            this.node = node;
            this.mode = mode;
        }

        /** Gets name. 
         * @return human presentable name of this paste type. */
        @Override
        public String getName() {
            String pasteKey = mode == 1 ? "CTL_PasteKeyValue" : "CTL_PasteKeyNoValue";
            return NbBundle.getBundle(PropertiesLocaleNode.class).getString(pasteKey);
        }

        /** Performs the paste action.
         * @return <code>Transferable</code> which should be inserted into the clipboard after
         * paste action. It can be null, which means that clipboard content
         * should stay the same
         */
        public Transferable paste() throws IOException {
            PropertiesStructure ps = ((PropertiesFileEntry)getFileEntry()).getHandler().getStructure();
            String value;
            if (mode == MODE_PASTE_WITH_VALUE) {
                value = item.getValue();
            } else {
                value = "";
            }
            if (ps != null) {
                Element.ItemElem newItem = ps.getItem(item.getKey());
                if (newItem == null) {
                    ps.addItem(item.getKey(), value, item.getComment());
                }
                else {
                    newItem.setValue(value);
                    newItem.setComment(item.getComment());
                }
                if (node != null) {
                    node.destroy();
                }
            }

            return null;
        }
    } // End of inner KeyPasteType class.

}
