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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.io.IOException;
import org.netbeans.modules.xml.xam.Model;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;

/**
 * Represents a collection of external references, or a single file.
 *
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class ExternalReferenceDataNode extends FilterNode
        implements ExternalReferenceNode {
    /** Name of the 'selected' property. */
    public static final String PROP_SELECTED = "selected";
    /** Name of the 'prefix' property. */
    public static final String PROP_PREFIX = "prefix";
    /** Controls the appearance of this node. */
    private ExternalReferenceDecorator decorator;
    /** Set of PropertySets. */
    private Sheet sheet;
    /** True if selected, false otherwise. */
    private boolean selected;
    /** The namespace prefix, if specified. */
    private String prefix;

    /**
     * Creates a new instance of ExternalReferenceDataNode.
     *
     * @param  original   the delegate Node.
     * @param  decorator  the external reference decorator.
     */
    public ExternalReferenceDataNode(Node original,
            ExternalReferenceDecorator decorator) {
        super(original, new Children(original, decorator));
        this.decorator = decorator;
    }

    public boolean canRename() {
        // Disable rename as it serves no purpose here and makes the
        // single-click-select-toggle difficult to use.
        return false;
    }

    /**
     * Indicates if this node allows setting it selected.
     *
     * @return  true if this node can be selected, false otherwise.
     */
    public boolean canSelect() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        return dobj != null && !dobj.getPrimaryFile().isFolder() &&
                decorator.validate(this) == null;
    }

    /**
     * Creates a node property of the given key (same as the column keys)
     * and specific getter/setter methods on the given object.
     *
     * @param  key     property name (same as matching column).
     * @param  type    Class of the property (e.g. String.class).
     * @param  inst    object on which to reflect.
     * @param  getter  name of getter method for property value.
     * @param  setter  name of setter method for property value (may be null).
     * @return  new property.
     */
    private Node.Property createProperty(String key, Class type, Object inst,
            String getter, String setter) {
        Property prop = null;
        try {
            prop = new Reflection(inst, type, getter, setter);
            prop.setName(key);
            prop.setDisplayName(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_ExternalReferenceCreator_Column_Name_" + key));
            prop.setShortDescription(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_ExternalReferenceCreator_Column_Desc_" + key));
        }  catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
        }
        return prop;
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, String.class, this,
                "getHtmlDisplayName", null));
        if (canSelect()) {
            set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
                    "isSelected", "setSelected"));
            Node.Property prop = createProperty(PROP_PREFIX, String.class,
                    this, "getPrefix", "setPrefix");
            // Suppress the [...] button because it is not needed.
            prop.setValue("suppressCustomEditor", Boolean.TRUE);
            set.put(prop);
        } else {
            // Do not include this property so the checkbox is not shown.
            //set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
            //        "isSelected", null));
            Node.Property prop = createProperty(PROP_PREFIX, String.class,
                    this, "getPrefix", null);
            // Suppress the [...] button because it is not needed.
            prop.setValue("suppressCustomEditor", Boolean.TRUE);
            set.put(prop);
        }
        return sheet;
    }

    protected final synchronized Sheet getSheet() {
        if (sheet != null) {
            return sheet;
        }
        sheet = createSheet();
        firePropertySetsChange(null, null);
        return sheet;
    }

    public PropertySet[] getPropertySets() {
        Sheet s = getSheet();
        return s.toArray();
    }

    public String getHtmlDisplayName() {
        String name = getOriginal().getHtmlDisplayName();
        if (decorator != null) {
            if (name == null) {
                name = getDisplayName();
            }
            name = decorator.getHtmlDisplayName(name, this);
        }
        return name;
    }

    public String getNamespace() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    Model model = cookie.getModel();
                    return decorator.getNamespace(model);
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public Model getModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            if (cookie != null) {
                try {
                    return cookie.getModel();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
        return null;
    }

    public String getPrefix() {
        if (prefix == null) {
            prefix = decorator.generatePrefix(this);
        }
        return prefix;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean hasModel() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
            ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
            // Don't check for a model, as it may not be well-formed, and
            // this method is not checking for that, just that we should
            // have a model in the normal case.
            return cookie != null;
        }
        return false;
    }

    public void setDisplayName(String s) {
        super.disableDelegation(DELEGATE_GET_DISPLAY_NAME|DELEGATE_SET_DISPLAY_NAME);
        super.setDisplayName(s);
    }

    /**
     * Set the namespace prefix for this node.
     *
     * @param  prefix  new namespace prefix.
     */
    public void setPrefix(String prefix) {
        String old = this.prefix;
        this.prefix = prefix;
        firePropertyChange(PROP_PREFIX, old, prefix);
    }

    /**
     * Mark this node as selected.
     *
     * @param  selected  true to select, false to unselect.
     */
    public void setSelected(boolean selected) {
        if (!canSelect()) {
            throw new IllegalStateException("node cannot be selected");
        }
        boolean old = this.selected;
        this.selected = selected;
        firePropertyChange(PROP_SELECTED, old, selected);
    }

    private static class Children extends FilterNode.Children {
        /** Controls the appearance of child nodes. */
        private ExternalReferenceDecorator decorator;

        public Children(Node original, ExternalReferenceDecorator decorator) {
            super(original);
            this.decorator = decorator;
        }

        protected Node[] createNodes(Node n) {
            DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                FileObject fobj = dobj.getPrimaryFile();
                if (fobj.isFolder() && fobj.getNameExt().equals("nbproject") &&
                        fobj.getFileObject("project.xml") != null) {
                    // It is the NetBeans project folder, ignore it.
                    return new Node[0];
                }
                ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
                String fname = fobj.getNameExt();
                String ext = decorator.getDocumentType().toString();
                if (fobj.isFolder() || cookie != null && fname.endsWith(ext)) {
                    return super.createNodes(n);
                }
            }
            return new Node[0];
        }

        protected Node copyNode(Node node) {
            return decorator.createExternalReferenceNode(node);
        }
    }
}
