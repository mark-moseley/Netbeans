/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */

package org.apache.tools.ant.module.nodes;

import java.awt.*;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List; // override java.awt.List
import javax.swing.event.*;

import org.w3c.dom.*;

import org.apache.tools.ant.Project;

import org.openide.*;
import org.openide.util.datatransfer.*;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.nodes.Node; // override org.w3c.dom.Node
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.ElementCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.xml.ElementSupport;
import org.apache.tools.ant.module.wizards.properties.*;

/** A node that represents an Ant project.
 */
public class AntProjectNode extends DataNode implements ChangeListener, PropertyChangeListener {

    public AntProjectNode (DataObject obj) {
        super (obj, new AntProjectChildren ((AntProjectCookie) obj.getCookie (AntProjectCookie.class)));
        setIconBase ("org/apache/tools/ant/module/resources/AntIcon"); // NOI18N
        AntProjectCookie cookie = (AntProjectCookie) getCookie (AntProjectCookie.class);
        cookie.addChangeListener (WeakListener.change (this, cookie));
        obj.addPropertyChangeListener (WeakListener.propertyChange (this, obj));
        RequestProcessor.postRequest (new Runnable () {
                public void run () {
                    updateDisplayName ();
                    updateElementCookie ();
                }
            }, 500); // don't even think about squeezing out folder recognizer thread...
        getCookieSet ().add (new ProjectNodeIndex (this));
    }

    private void updateDisplayName () {
        AntProjectCookie cookie = (AntProjectCookie) getCookie (AntProjectCookie.class);
        if (cookie.getFile() == null && cookie.getFileObject() == null) {
            // Script has been invalidated perhaps? Don't continue, we would
            // just get an NPE from the getParseException.
            return;
        }
        Element pel = cookie.getProjectElement ();
        if (pel != null) {
            // #19446: honor file extensions mode
            FileObject fo = getDataObject().getPrimaryFile();
            String simpleName = DataNode.getShowFileExtensions() ? fo.getNameExt() : fo.getName();
            String projectName = pel.getAttribute ("name"); // NOI18N
            if (! projectName.equals("")) { // NOI18N
                // Set the name/display name in the IDE to the name of the project 
                setDisplayName (NbBundle.getMessage (AntProjectNode.class, "LBL_script_display_name", simpleName, projectName));
            } else {
                // No name specified, OK.
                setDisplayName(simpleName);
            }
        }
        Throwable exc = cookie.getParseException ();
        if (exc == null) {
            setShortDescription (getDisplayName ());
            setIconBase ("org/apache/tools/ant/module/resources/AntIcon"); // NOI18N
        } else {
            String m = exc.getLocalizedMessage ();
            if (m == null || m.length () == 0) {
                m = exc.toString ();
                AntModule.err.annotate (exc, ErrorManager.UNKNOWN, "Strange parse error in " + ((DataObject) getCookie (DataObject.class)).getPrimaryFile (), null, null, null); // NOI18N
                AntModule.err.notify (ErrorManager.INFORMATIONAL, exc);
            }
            setShortDescription (m);
            setIconBase ("org/apache/tools/ant/module/resources/AntIconError"); // NOI18N
        }
    }

    private void updateElementCookie () {
        AntProjectCookie main = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element projel = main.getProjectElement ();
        if (projel != null) {
            getCookieSet ().add (new ElementSupport.Introspection (projel, Project.class.getName ()));
        } else {
            ElementCookie cookie = (ElementCookie) getCookie (ElementCookie.class);
            if (cookie != null) {
                getCookieSet ().remove (cookie);
            }
        }
    }

    protected Sheet createSheet() {  
        Sheet sheet = super.createSheet();

        // Make sure there is a "Properties" set: // NOI18N
        Sheet.Set props = sheet.get(Sheet.PROPERTIES); // get by name, not display name
        if (props == null)  {
            props = Sheet.createPropertiesSet ();
            sheet.put(props);
        }
        add2Sheet (props);

        Sheet.Set exec = new Sheet.Set ();
        exec.setName ("execution"); // NOI18N
        exec.setDisplayName (NbBundle.getMessage (AntProjectNode.class, "LBL_execution"));
        exec.setShortDescription (NbBundle.getMessage (AntProjectNode.class, "HINT_execution"));
        CompilerSupport csupp = (CompilerSupport) getCookie (CompilerSupport.class);
        if (csupp != null) csupp.addProperties (exec);
        ExecSupport xsupp = (ExecSupport) getCookie (ExecSupport.class);
        if (xsupp != null) xsupp.addProperties (exec);
        exec.remove (ExecSupport.PROP_FILE_PARAMS);
        exec.remove (ExecSupport.PROP_DEBUGGER_TYPE);
        if (csupp != null || xsupp != null) {
            sheet.put (exec);
        }

        return sheet;
    }

    private class ProjectNameProperty extends AntProperty {
        public ProjectNameProperty (String name, AntProjectCookie proj) {
            super (name, proj);
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
    }

    private class TargetEditor extends PropertyEditorSupport {
        public String getAsText () {
            return (String) getValue ();
        }
        public void setAsText (String v) throws IllegalArgumentException {
            setValue (v);
        }
        public String[] getTags () {
            Element proj = ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
            if (proj == null) return new String[] { getAsText () };
            NodeList nl = proj.getElementsByTagName ("target"); // NOI18N
            String[] tags = new String[nl.getLength ()];
            for (int i = 0; i < tags.length; i++) {
                tags[i] = ((Element) nl.item (i)).getAttribute ("name"); // NOI18N
            }
            return tags;
        }
    }

    private class ProjectTargetProperty extends AntProperty {
        public ProjectTargetProperty (String name, AntProjectCookie proj) {
            super (name, proj);
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        public boolean supportsDefaultValue () {
            return false;
        }
        public void setValue (Object value) throws IllegalArgumentException, InvocationTargetException {
            if (value == null || value.equals ("")) {
                IllegalArgumentException iae = new IllegalArgumentException ("no default for " + this.getName ()); // NOI18N
                AntModule.err.annotate (iae, NbBundle.getMessage (AntProjectNode.class, "EXC_no_default_value_for_prop", this.getDisplayName ()));
                throw iae;
            }
            super.setValue (value);
        }
        public PropertyEditor getPropertyEditor () {
            return new TargetEditor ();
        }
    }

    private class ProjectBasedirProperty extends PropertySupport.ReadWrite {
        public ProjectBasedirProperty (String dname, String sdesc) {
            super ("basedir", File.class, dname, sdesc); // NOI18N
            this.setValue ("directories", Boolean.TRUE); // NOI18N
            this.setValue ("files", Boolean.FALSE); // NOI18N
        }
        protected Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        public Object getValue () {
            Element el = getElement ();
            if (el == null) { // #9675
                return null;
            }
            String bd = el.getAttribute("basedir"); // NOI18N
            if (bd.equals("")) return null; // NOI18N
            if (bd.equals(".")) bd = ""; // NOI18N
            return new File(bd);
        }
        public void setValue (Object o) throws IllegalArgumentException, InvocationTargetException {
            Element el = getElement ();
            if (el == null) return;
            if (o == null) {
                try {
                    el.removeAttribute ("basedir"); // NOI18N
                } catch (DOMException dome) {
                    throw new InvocationTargetException (dome);
                }
                return;
            }
            if (! (o instanceof File)) throw new IllegalArgumentException ();
            try {
                String path = ((File)o).getPath();
                if (path.equals("")) path = "."; // NOI18N
                el.setAttribute("basedir", path); // NOI18N
            } catch (DOMException dome) {
                throw new InvocationTargetException (dome);
            }
        }
        public boolean canWrite () {
            return (getElement () != null && ! isScriptReadOnly((AntProjectCookie) getCookie(AntProjectCookie.class)));
        }
        public boolean supportsDefaultValue () {
            return (getElement () != null);
        }
        public void restoreDefaultValue () throws InvocationTargetException {
            setValue (null);
        }
        public PropertyEditor getPropertyEditor() {
            // Before using File editor, set up the base directory...
            AntProjectCookie cookie = (AntProjectCookie)getCookie(AntProjectCookie.class);
            if (cookie != null) {
                Element root = cookie.getProjectElement();
                File buildscript = cookie.getFile();
                if (root != null && buildscript != null) {
                    AntModule.err.log("ProjectBasedirProperty: setting baseDir=" + buildscript.getParentFile());
                    // Controls which directory relative paths are relative to:
                    ProjectBasedirProperty.this.setValue("baseDir", buildscript.getParentFile()); // NOI18N
                }
            }
            return super.getPropertyEditor();
        }
    }

    private void add2Sheet (Sheet.Set props) {
        ResourceBundle bundle = NbBundle.getBundle (AntProjectNode.class);
        AntProjectCookie proj = (AntProjectCookie) getCookie (AntProjectCookie.class);
        
        // Create the required properties (XML attributes) of the Ant project
        Node.Property prop = new ProjectNameProperty ("name", proj); // NOI18N
        // Cannot reuse 'name' because it conflicts with the DataObject.PROP_NAME:
        prop.setName ("projectName"); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_projectName"));
        prop.setShortDescription (bundle.getString ("HINT_projectName"));
        props.put (prop);
        prop = new ProjectTargetProperty ("default", proj); // NOI18N
        prop.setDisplayName (bundle.getString ("PROP_default"));
        prop.setShortDescription (bundle.getString ("HINT_default"));
        props.put (prop);
        prop = new ProjectBasedirProperty (bundle.getString ("PROP_basedir"), bundle.getString ("HINT_basedir"));
        props.put (prop);
        // id prop unnecessary, since project name functions as an ID
        props.put (new ProjectBuildSequenceProperty(proj));
        ProjectPropertiesFileProperty ppfp = new ProjectPropertiesFileProperty ();
        props.put (ppfp);
        PropertiesChooserProperty pcp = new PropertiesChooserProperty (ppfp);
        props.put (pcp);
    }

    public void propertyChange (PropertyChangeEvent evt) {
        String prop = evt.getPropertyName ();
        if (prop == null || prop.equals (DataObject.PROP_NAME)) {
            updateDisplayName ();
        }
    }

    public void stateChanged (ChangeEvent ev) {
        updateDisplayName ();
        updateElementCookie ();
        firePropertyChange (null, null, null);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected void createPasteTypes (Transferable t, List l) {
        AntProjectCookie proj = (AntProjectCookie) getCookie (AntProjectCookie.class);
        Element pel = proj.getProjectElement ();
        if (pel != null && ! isScriptReadOnly (proj)) {
            ElementCookie cookie = (ElementCookie) NodeTransfer.cookie (t, NodeTransfer.COPY, ElementCookie.class);
            if (cookie != null && canPasteElement (cookie.getElement ())) {
                l.add (new ElementNode.ElementPaste (pel, cookie.getElement (), false));
            }
            cookie = (ElementCookie) NodeTransfer.cookie (t, NodeTransfer.MOVE, ElementCookie.class);
            if (cookie != null && canPasteElement (cookie.getElement ())) {
                l.add (new ElementNode.ElementPaste (pel, cookie.getElement (), true));
            }
        }
    }

    private boolean canPasteElement (Element el) {
        String type = el.getNodeName ();
        return type.equals ("target") || // NOI18N
               type.equals ("property") || // NOI18N
               type.equals ("taskdef") || // NOI18N
               IntrospectedInfo.getDefaults ().getDefs ("type").containsKey (type) || // NOI18N
               AntSettings.getDefault ().getCustomDefs ().getDefs ("type").containsKey (type); // NOI18N
    }

    public NewType[] getNewTypes () {
        if (! isScriptReadOnly ((AntProjectCookie) getCookie(AntProjectCookie.class))) {
            List names = new ArrayList ();
            names.addAll (IntrospectedInfo.getDefaults ().getDefs ("type").keySet ()); // NOI18N
            names.addAll (AntSettings.getDefault ().getCustomDefs ().getDefs ("type").keySet ()); // NOI18N
            Collections.sort (names);
            names.add (0, "target"); // NOI18N
            names.add (1, "property"); // NOI18N
            names.add (2, "taskdef"); // NOI18N
            NewType[] types = new NewType[names.size ()];
            for (int i = 0; i < types.length; i++) {
                types[i] = new ProjectNewType ((String) names.get (i));
            }
            return types;
        } else {
            return new NewType[0];
        }
    }

    private class ProjectNewType extends NewType {
        private String name;
        public ProjectNewType (String name) {
            this.name = name;
        }
        public String getName () {
            return name;
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.apache.tools.ant.module.node-manip");
        }
        public void create () throws IOException {
            Element el = ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
            if (el == null) throw new IOException ();
            try {
                Element el2 = el.getOwnerDocument ().createElement (name);
                if (name.equals ("target")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_target_name_changeme"));
                } else if (name.equals ("property")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_property_name_changeme"));
                    el2.setAttribute ("value", NbBundle.getMessage (AntProjectNode.class, "MSG_property_value_changeme"));
                } else if (name.equals ("taskdef")) { // NOI18N
                    el2.setAttribute ("name", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_name_changeme"));
                    el2.setAttribute ("classname", NbBundle.getMessage (AntProjectNode.class, "MSG_taskdef_classname_changeme"));
                } else {
                    el2.setAttribute ("id", NbBundle.getMessage (AntProjectNode.class, "MSG_id_changeme"));
                }
                ElementNode.appendWithIndent (el, el2);
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

    
    
    /** Returns true if the Antscript represented by the passed cookie is read-only. */
    public static boolean isScriptReadOnly(AntProjectCookie cookie) {
        if (cookie != null) {
            if (cookie.getFileObject() != null) {
                return cookie.getFileObject().isReadOnly();
            } else if (cookie.getFile() != null) {
                return ! cookie.getFile().canWrite();
            }
        }
        return true;
    }
    
    /** Property displaying the build sequence of the whole project. */
    public static class ProjectBuildSequenceProperty extends AntTargetNode.BuildSequenceProperty {
        
        /** ProjectCookie. */
        protected AntProjectCookie proj;
        
        /** Creates new ProjectBuildSequenceProperty.
         * @param elem the project Element.
         */
        public ProjectBuildSequenceProperty(AntProjectCookie proj) {
            super (proj.getProjectElement ());
            this.proj = proj;
        }
        
        /** Override getTarget of superclass to find default target. */
        public Element getTarget() {
            el = proj.getProjectElement (); // to be sure that the Element is up to date.
            if (el != null && el.getAttribute("default") != null) { // NOI18N
                return getTargetElement(el.getAttribute("default"), el); // NOI18N
            }
            return null;
        }
        
        /** Returns special String in case of missing default target. */
        public Object getValue() {
            if (proj.getProjectElement () == null) {
                return NbBundle.getMessage (AntProjectNode.class, "LBL_property_invalid_no_element");
            }
            Element el = getTarget();
            if (el == null) {
                return NbBundle.getMessage (AntProjectNode.class, "MSG_defaulttarget_missing");
            }
            return super.getValue();
        }
    }
    
    /** Index Cookie for ProjectNode. Enables ReorderAction. */
    public static class ProjectNodeIndex extends ElementNode.ElementNodeIndex {
        
        /** Creates new ProjectNodeIndex. */
        public ProjectNodeIndex(org.openide.nodes.Node indexNode) {
            super (indexNode);
        }

        /** Get the parent Node of the Elements that can be moved.*/
        protected org.w3c.dom.Node getParentNode() {
            return ((AntProjectCookie) indexNode.getCookie (AntProjectCookie.class)).getProjectElement ();
        }
    }
    
    /** Displays the Properties for the project stored in a .properties file. */
    class ProjectPropertiesFileProperty extends PropertiesFileProperty {
        ProjectPropertiesFileProperty () {
            super ( NbBundle.getMessage (AntProjectNode.class, "PROP_project_properties"),
                    NbBundle.getMessage (AntProjectNode.class, "HINT_project_properties")
                  );
        }
        /** Get the Project Element. */
        public Element getElement () {
            return ((AntProjectCookie) getCookie (AntProjectCookie.class)).getProjectElement ();
        }
        /** Get the AntProjectCookie. */
        public AntProjectCookie getAntProjectCookie () {
            return (AntProjectCookie) getCookie (AntProjectCookie.class);
        }        
        protected void firePropertiesFilePropertyChange() {
            AntProjectNode.this.firePropertySetsChange(null, null);
        }
    }
}
