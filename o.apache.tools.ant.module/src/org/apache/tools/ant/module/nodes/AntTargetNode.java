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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.w3c.dom.*;

import org.apache.tools.ant.Target;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.util.datatransfer.*;
import org.openide.util.actions.SystemAction;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.ElementCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.xml.ElementSupport;
import org.apache.tools.ant.module.wizards.properties.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/** A node representing an Ant build target.
 */
public class AntTargetNode extends ElementNode {

    public AntTargetNode (final AntProjectCookie project, final Element targetElem) {
        super (targetElem, new AntTargetChildren (targetElem));
        /*
        AntTargetCookie targetCookie = new AntTargetSupport (project, targetElem);
        getCookieSet().add(targetCookie);
         */
        if (project.getFile () != null) {
            getCookieSet ().add (new ExecCookie () {
                    public void start () {
                        try {
                            TargetExecutor te = new TargetExecutor(project, new String[] {targetElem.getAttribute("name")}); // NOI18N
                            te.setSwitchWorkspace(true);
                            te.execute();
                        } catch (IOException ioe) {
                            TopManager.getDefault ().notifyException (ioe);
                        }
                    }
                });
        }
    }

    protected ElementCookie createElementCookie () {
        return new ElementSupport.Introspection (el, Target.class.getName ());
    }

    protected void initDisplay () {
        String targetName = el.getAttribute ("name"); // NOI18N
        setNameSuper (targetName);
        String desc = el.getAttribute ("description"); // NOI18N
        if (desc.length () > 0) {
            setShortDescription (desc);
            setIconBase ("org/apache/tools/ant/module/resources/EmphasizedTargetIcon");
        } else {
            setShortDescription (getDisplayName ());
            setIconBase ("org/apache/tools/ant/module/resources/TargetIcon");
        }
    }

    protected SystemAction[] createActions () {
        return new SystemAction[] {
            SystemAction.get (ExecuteAction.class),
            null,
            SystemAction.get (OpenLocalExplorerAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (MoveUpAction.class),
            SystemAction.get (MoveDownAction.class),
            SystemAction.get (ReorderAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            SystemAction.get (RenameAction.class),
            null,
            SystemAction.get (NewAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    public SystemAction getDefaultAction () {
        return SystemAction.get (ExecuteAction.class);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
    }

    protected void addProperties (Sheet.Set props) {
        String[] attrs = new String[] { "name", "description", "if", "unless", /*"id"*/ }; // NOI18N
        AntProjectCookie proj = (AntProjectCookie) getCookie(AntProjectCookie.class);
        for (int i = 0; i < attrs.length; i++) {
            org.openide.nodes.Node.Property prop = new AntProperty (el, attrs[i], proj);
            prop.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_" + attrs[i]));
            prop.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_" + attrs[i]));
            props.put (prop);
        }
        props.put (new DependsProperty (proj));
        props.put (new BuildSequenceProperty(el));
        TargetPropertiesFileProperty tpfp = new TargetPropertiesFileProperty ();
        props.put (tpfp); 
        PropertiesChooserProperty pcp = new PropertiesChooserProperty (tpfp);
        props.put (pcp);
    }

    private class TargetPropertiesFileProperty extends PropertiesFileProperty {
        TargetPropertiesFileProperty () {
            super ( NbBundle.getMessage (AntTargetNode.class, "PROP_target_properties"),
                    NbBundle.getMessage (AntTargetNode.class, "HINT_target_properties")
                  );
        }
        /** Get the Element of this TargetNode. */
        public Element getElement () {
            return el;
        }
        /** Get the AntProjectCookie. */
        public AntProjectCookie getAntProjectCookie () {
            return (AntProjectCookie) getCookie (AntProjectCookie.class);
        }
        protected void firePropertiesFilePropertyChange() {
            AntTargetNode.this.firePropertySetsChange(null, null);
        }
    }
    
    private class DependsProperty extends AntProperty {
        public DependsProperty (AntProjectCookie proj) {
            super (el, "depends", proj); // NOI18N
            this.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_depends"));
            this.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_depends"));
        }
        private Set/*<String>*/ getAvailable () {
            Element proj = (Element) el.getParentNode ();
            if (proj == null) return Collections.EMPTY_SET;
            String me = el.getAttribute ("name"); // NOI18N
            NodeList nl = proj.getChildNodes();
            Set s = new HashSet();
            for (int i = 0; i < nl.getLength (); i++) {
                if (nl.item (i) instanceof Element) {
                    Element target = (Element) nl.item (i);
                    if (target.getTagName().equals("target") && ! me.equals(target.getAttribute("name"))) { // NOI18N
                        s.add (target.getAttribute ("name")); // NOI18N
                    }
                }
            }
            AntModule.err.log ("AntTargetNode.DependsProperty.available=" + s);
            return s;
        }
        public void setValue (Object o) throws IllegalArgumentException, InvocationTargetException {
            if (! (o instanceof String)) throw new IllegalArgumentException ();
            Set av = getAvailable ();
            StringTokenizer tok = new StringTokenizer ((String) o, ", "); // NOI18N
            while (tok.hasMoreTokens ()) {
                String target = tok.nextToken ();
                if (! av.contains (target)) {
                    IllegalArgumentException iae = new IllegalArgumentException ("no such target: " + target); // NOI18N
                    //TopManager.getDefault ().getErrorManager ().annotate (iae, NbBundle.getMessage (AntTargetNode.class, "EXC_target_not_exist", target));
                    AntModule.err.annotate (iae, NbBundle.getMessage (AntTargetNode.class, "EXC_target_not_exist", target));
                    throw iae;
                }
            }
            super.setValue (o);
        }
        public Object getValue () {
            return super.getValue ();
        }
        public PropertyEditor getPropertyEditor () {
            return new DependsEditor ();
        }
        /** Note: treats list of dependencies as an _unordered set_.
         * Ant does not currently officially specify that the order
         * of items in a depends clause means anything, so this GUI
         * faithfully provides no interface to reorder them.
         * Cf. Peter Donald's message "RE: Order of Depends" to
         * ant-dev on Feb 21 2001.
         */
        private class DependsEditor extends PropertyEditorSupport {
            public String getAsText () {
                return (String) this.getValue ();
            }
            public void setAsText (String v) {
                this.setValue (v);
            }
            public boolean supportsCustomEditor () {
                return true;
            }
            public Component getCustomEditor () {
                return new JScrollPane(new DependsPanel ());
            }
            private class DependsPanel extends Box implements ActionListener {
                private final Set on = new HashSet (); // Set<String>
                public DependsPanel () {
                    super (BoxLayout.Y_AXIS);
                    String depends = (String) DependsEditor.this.getValue ();
                    StringTokenizer tok = new StringTokenizer (depends, ", "); // NOI18N
                    Set available = getAvailable ();
                    Set bogus = new HashSet (); // Set<String>
                    while (tok.hasMoreTokens ()) {
                        String dep = tok.nextToken ();
                        if (available.contains (dep)) {
                            on.add (dep);
                        } else {
                            bogus.add (dep);
                        }
                    }
                    if (! bogus.isEmpty ()) {
                        // #12681: if there are bad dependencies, just skip them.
                        List bogusList = new ArrayList (bogus); // List<String>
                        Collections.sort (bogusList);
                        StringBuffer bogusListString = new StringBuffer (100);
                        Iterator it = bogusList.iterator ();
                        bogusListString.append ((String) it.next ());
                        while (it.hasNext ()) {
                            bogusListString.append (' '); // NOI18N
                            bogusListString.append ((String) it.next ());
                        }
                        add (new JLabel (NbBundle.getMessage (AntTargetNode.class,
                            "MSG_suppressing_bad_deps", bogusListString.toString ()))); // NOI18N
                    }
                    List availableList = new ArrayList (available); // List<String>
                    Collections.sort (availableList);
                    Iterator it = availableList.iterator ();
                    while (it.hasNext ()) {
                        String target = (String) it.next ();
                        AbstractButton check = new JCheckBox (target, on.contains (target));
                        check.addActionListener (this);
                        add (check);
                    }
                    add (createGlue ());
                }
                public void actionPerformed (ActionEvent ev) {
                    JCheckBox box = (JCheckBox) ev.getSource ();
                    String target = box.getText ();
                    if (box.isSelected ()) {
                        on.add (target);
                    } else {
                        on.remove (target);
                    }
                    StringBuffer buf = new StringBuffer ();
                    List onList = new ArrayList (on); // List<String>
                    Collections.sort (onList);
                    Iterator it = onList.iterator ();
                    while (it.hasNext ()) {
                        target = (String) it.next ();
                        if (buf.length () > 0) buf.append (','); // NOI18N
                        buf.append (target);
                    }
                    DependsEditor.this.setValue (buf.toString ());
                }
            }
        }
    }

    public NewType[] getNewTypes () {
        if (! AntProjectNode.isScriptReadOnly ((AntProjectCookie) getCookie (AntProjectCookie.class))) {
            List names = new ArrayList ();
            names.addAll (IntrospectedInfo.getDefaults ().getDefs ("task").keySet ()); // NOI18N
            names.addAll (AntSettings.getDefault ().getCustomDefs ().getDefs ("task").keySet ()); // NOI18N
            Collections.sort (names);
            List newtypes = new ArrayList();
            newtypes.add(new TaskNewType(names));
            List types = new ArrayList();
            types.addAll (IntrospectedInfo.getDefaults ().getDefs ("type").keySet ()); // NOI18N
            types.addAll (AntSettings.getDefault ().getCustomDefs ().getDefs ("type").keySet ()); // NOI18N
            Collections.sort(types);
            Iterator it = types.iterator();
            while (it.hasNext()) {
                newtypes.add(new TypeNewType((String)it.next()));
            }
            return (NewType[])newtypes.toArray(new NewType[newtypes.size()]);
        } else {
            return new NewType[0];
        }
    }
    
    private final class TypeNewType extends NewType {
        private final String name;
        public TypeNewType(String name) {
            this.name = name;
        }
        public String getName () {
            return name;
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.apache.tools.ant.module.node-manip");
        }
        public void create () throws IOException {
            try {
                Element el2 = el.getOwnerDocument ().createElement (name);
                el2.setAttribute ("id", NbBundle.getMessage (AntProjectNode.class, "MSG_id_changeme"));
                ElementNode.appendWithIndent (el, el2);
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

    private class TaskNewType extends NewType {
        private List names;
        public TaskNewType (List names) {
            this.names = names;
        }
        public String getName () {
            return NbBundle.getMessage (AntTargetNode.class, "LBL_task_new_type");
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.apache.tools.ant.module.node-manip");
        }
        public void create () throws IOException {
            // Ask the user which to choose.
            JPanel pane = new JPanel ();
            pane.setLayout (new GridBagLayout ());
            GridBagConstraints gridBagConstraints;

            // #20657 - the content of the panel was redesigned. The issue
            // contain the .form file
            JLabel jLabel1 = new javax.swing.JLabel(NbBundle.getMessage (AntTargetNode.class, "LBL_choose_task"));
            final JButton help = new javax.swing.JButton();
            final JComboBox combo = new javax.swing.JComboBox(names.toArray ());

            jLabel1.setFont(new java.awt.Font("Arial", 0, 11));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 6);
            pane.add(jLabel1, gridBagConstraints);


            if (AntTaskNode.helpFor("property", "task") != null) { // NOI18N
                // We have help available. (<property> is well-known.)
                ActionListener helplistener = new ActionListener () {
                        public void actionPerformed (ActionEvent ignore) {
                            help.setText (NbBundle.getMessage (AntTargetNode.class, "LBL_help_on_task", combo.getSelectedItem ()));
                            help.setEnabled(AntTaskNode.helpFor((String)combo.getSelectedItem(), "task") != null); // NOI18N
                        }
                    };
                helplistener.actionPerformed (null);
                combo.addActionListener (helplistener);
                help.addActionListener (new ActionListener () {
                        public void actionPerformed (ActionEvent ev) {
                            TopManager.getDefault().showHelp(
                                AntTaskNode.helpFor((String)combo.getSelectedItem(), "task")); // NOI18N
                        }
                    });
            }
            
            help.setFont(new java.awt.Font("Arial", 0, 11));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.insets = new java.awt.Insets(12, 0, 11, 11);
            pane.add(help, gridBagConstraints);

            
            combo.setFont(new java.awt.Font("Arial", 0, 12));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(12, 6, 11, 6);
            pane.add(combo, gridBagConstraints);
    
            
            DialogDescriptor dlg = new DialogDescriptor (pane, NbBundle.getMessage (AntTargetNode.class, "TITLE_select_task"));
            dlg.setHelpCtx (getHelpCtx ());
            dlg.setModal (true);
            TopManager.getDefault ().createDialog (dlg).show ();
            if (dlg.getValue () != NotifyDescriptor.OK_OPTION) return;
            String name = (String) combo.getSelectedItem ();
            try {
                Element el2 = el.getOwnerDocument ().createElement (name);
                ElementNode.appendWithIndent (el, el2);
            } catch (DOMException dome) {
                IOException ioe = new IOException ();
                AntModule.err.annotate (ioe, dome);
                throw ioe;
            }
        }
    }

    protected boolean canPasteElement (Element el2) {
        String name = el2.getNodeName ();
        if (IntrospectedInfo.getDefaults ().getDefs ("task").containsKey (name)) { // NOI18N
            return true;
        }
        if (IntrospectedInfo.getDefaults ().getDefs ("type").containsKey (name)) { // NOI18N
            return true;
        }
        if (AntSettings.getDefault ().getCustomDefs ().getDefs ("task").containsKey (name)) { // NOI18N
            return true;
        }
        if (AntSettings.getDefault ().getCustomDefs ().getDefs ("type").containsKey (name)) { // NOI18N
            return true;
        }
        return false;
    }

    /**
     * Node displaying the sequence of all called targets when executing.
     */
    public static class BuildSequenceProperty extends org.openide.nodes.PropertySupport.ReadOnly {
        
        /** Target Element of the build. */
        protected org.w3c.dom.Element el;
                
        /** Creates new BuildSequenceProperty.
         * @param el Element representing the target.
         * @param name the name of the target.
         * @param proj AntProjectCookie of the Ant file
         */
        public BuildSequenceProperty (Element el) {
            super ("buildSequence", // NOI18N
                   String.class,
                   NbBundle.getMessage (AntTargetNode.class, "PROP_target_sequence"),
                   NbBundle.getMessage (AntTargetNode.class, "HINT_target_sequence")
                  );
            this.el = el;
        }

        /** Returns the target of this BuildSequenceProperty. */
        protected Element getTarget() {
            return el;
        }
        
        /** Computes the dependencies of all called targets and returns an ordered
         * sequence String.
         * @param target the target that gets executed
         */
        protected String computeTargetDependencies(org.w3c.dom.Element target) {
            if (target == null) {
                return "";
            }
            
            // get ProjectElement
            Element proj = (Element) target.getParentNode ();
            if (proj == null) {
                // just return current target name
                return target.getAttribute ("name"); // NOI18N
            } else {
                // List with all called targets. the last called target is the first
                // in the list
                List callingList = new LinkedList(); 
                // add this target.
                callingList = addTarget (callingList, target, 0, proj);
                if (callingList != null) {
                    return getReverseString (callingList);
                } else {
                    return NbBundle.getMessage (AntProjectNode.class, "MSG_target_sequence_illegaldepends");
                }
            }
        }

        /** Adds a target to the List. Calls depends-on targets recursively.
         * @param runningList List containing the ordered targets.
         * @param target the target that should be added
         * @param pos position where this target should be inserted
         * @projectElement the Element of the Ant project.
         *
         * @return list with all targets or null if a target was not found.
         */
        protected List addTarget(List runningList, Element target, int pos, Element projectElement) {
            String targetName = target.getAttribute ("name"); // NOI18N
            if (targetName == null) return runningList;
            
            // search target, skip it if found
            Iterator it = runningList.iterator();
            while (it.hasNext()) {
                if (targetName.equals (it.next())) {
                    return runningList;
                }
            }
            //add target at the given position...
            runningList.add(pos, targetName);
            
            // check dependenciesList
            String dependsString = target.getAttribute ("depends"); // NOI18N
            if (dependsString == null) return runningList;
            
            // add each target of the dependencies List
            StringTokenizer st = new StringTokenizer(dependsString, ", "); // NOI18N
            while (st.hasMoreTokens() && runningList != null) {
                Element dependsTarget = getTargetElement(st.nextToken(), projectElement);
                if (dependsTarget != null) {
                    runningList = addTarget(runningList, dependsTarget, (pos + 1), projectElement);
                } else {
                    // target is missing, we return null to indicate that something is wrong
                    return null;
                }
            }
            
            return runningList;
        }
        
        /** Returns the Element of a target given by its name. */
        protected Element getTargetElement(String targetName, Element projectElement) {
            NodeList nl = projectElement.getChildNodes();
            for (int i = 0; i < nl.getLength (); i++) {
                if (nl.item (i) instanceof Element) {
                    Element el = (Element) nl.item (i);
                    if (el.getTagName().equals("target") && el.getAttribute("name").equals(targetName)) { // NOI18N
                        return el;
                    }
                }
            }
            return null;
        }
 
        /** Returns a String of all Elements in the List in reverse order. */
        protected String getReverseString (List l) {
            StringBuffer sb = new StringBuffer ();
            for (int x= (l.size() - 1); x > -1; x--) {
                sb.append (l.get(x));
                if (x > 0) sb.append (", "); // NOI18N
            }
            return sb.toString ();
        }
        
        /** Returns the value of this property. */
        public Object getValue () {
            return computeTargetDependencies(getTarget());
        }
    }
}
