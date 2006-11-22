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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.nodes;


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.wizards.shortcut.ShortcutWizard;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class AntTargetNode extends AbstractNode implements ChangeListener {
    
    /** main project, not necessarily the one defining this target */
    private final AntProjectCookie project;
    private final TargetLister.Target target;
    
    /**
     * Create a new target node.
     * @param project the <em>main</em> project with this target (may not be the project this is physically part of)
     * @param target a representation of this target
     * @param allTargets all other targets in the main project
     */
    public AntTargetNode(AntProjectCookie project, TargetLister.Target target) {
        super(Children.LEAF);
        this.project = project;
        assert !target.isOverridden() : "Cannot include overridden targets";
        this.target = target;
        target.getScript().addChangeListener(WeakListeners.change(this, target.getScript()));
        setName(target.getQualifiedName());
        setDisplayName(target.getName());
        if (target.isDescribed()) {
            setShortDescription(target.getElement().getAttribute("description")); // NOI18N
            setIconBaseWithExtension("org/apache/tools/ant/module/resources/EmphasizedTargetIcon.gif");
        } else if (target.isDefault()) {
            setIconBaseWithExtension("org/apache/tools/ant/module/resources/EmphasizedTargetIcon.gif");
        } else {
            setIconBaseWithExtension("org/apache/tools/ant/module/resources/TargetIcon.gif");
        }
        getCookieSet().add(new TargetOpenCookie(target));
    }
    
    private static String internalTargetColor = null;
    /** Loosely copied from VcsFileSystem.annotateNameHtml */
    private static synchronized String getInternalTargetColor() {
        if (internalTargetColor == null) {
            if (UIManager.getDefaults().getColor("Tree.selectionBackground").equals(UIManager.getDefaults().getColor("controlShadow"))) { // NOI18N
                internalTargetColor = "Tree.selectionBorderColor"; // NOI18N
            } else {
                internalTargetColor = "controlShadow"; // NOI18N
            }
        }
        return internalTargetColor;
    }
    
    @Override
    public String getHtmlDisplayName() {
        // Use markup to indicate the default target, imported targets, and internal targets.
        boolean imported = target.getScript() != project;
        if (!imported && !target.isDefault() && !target.isInternal()) {
            return null;
        }
        StringBuffer name = new StringBuffer(target.getName());
        if (imported) {
            name.insert(0, "<i>"); // NOI18N
            name.append("</i>"); // NOI18N
        }
        if (target.isDefault()) {
            name.insert(0, "<b>"); // NOI18N
            name.append("</b>"); // NOI18N
        }
        if (target.isInternal()) {
            name.insert(0, "'>"); // NOI18N
            name.insert(0, getInternalTargetColor());
            name.insert(0, "<font color='!"); // NOI18N
            name.append("</font>"); // NOI18N
        }
        return name.toString();
    }
    
    public void stateChanged (ChangeEvent ev) {
        firePropertyChange (null, null, null);
    }

    @Override
    public boolean canDestroy () {
        return false;
    }
    
    @Override
    public boolean canRename () {
        return false;
    }
    
    @Override
    public boolean canCopy () {
        return true;
    }
    
    @Override
    public boolean canCut () {
        return false;
    }
    
    private final Action EXECUTE = new ExecuteAction();
    private final Action CREATE_SHORTCUT = new CreateShortcutAction();

    @Override
    public Action[] getActions(boolean context) {
        if (!target.isInternal()) {
            return new Action[] {
                SystemAction.get(OpenAction.class),
                null,
                EXECUTE,
                CREATE_SHORTCUT,
                null,
                SystemAction.get(PropertiesAction.class),
            };
        } else {
            return new Action[] {
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(PropertiesAction.class),
            };
        }
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    private final class ExecuteAction extends AbstractAction {
        
        ExecuteAction() {
            super(NbBundle.getMessage(AntTargetNode.class, "LBL_execute_target"));
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                TargetExecutor te = new TargetExecutor(project, new String[] {target.getName()});
                te.execute();
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
        
    }
    
    /**
     * Action to invoke the target shortcut wizard.
     * Used to be a "template", but this is more natural.
     * @see "issue #37374"
     */
    private final class CreateShortcutAction extends AbstractAction {
        
        CreateShortcutAction() {
            super(NbBundle.getMessage(AntTargetNode.class, "LBL_create_shortcut"));
        }
        
        public void actionPerformed(ActionEvent e) {
            ShortcutWizard.show(project, target.getElement());
        }
        
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet ();
        Sheet.Set props = sheet.get (Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        String[] attrs = new String[] {"name", "description", "depends"}; // NOI18N
        for (String attr : attrs) {
            org.openide.nodes.Node.Property<?> prop = new AntProperty(target.getElement(), attr);
            prop.setDisplayName (NbBundle.getMessage (AntTargetNode.class, "PROP_target_" + attr));
            prop.setShortDescription (NbBundle.getMessage (AntTargetNode.class, "HINT_target_" + attr));
            props.put (prop);
        }
        /*XXX
        props.put (new BuildSequenceProperty());
         */
        return sheet;
    }
    
    /**
     * Node displaying the sequence of all called targets when executing.
     */
    private final class BuildSequenceProperty extends PropertySupport.ReadOnly<String> {
        
        /** Creates new BuildSequenceProperty.
         */
        public BuildSequenceProperty() {
            super ("buildSequence", // NOI18N
                   String.class,
                   NbBundle.getMessage (AntTargetNode.class, "PROP_target_sequence"),
                   NbBundle.getMessage (AntTargetNode.class, "HINT_target_sequence")
                  );
        }

        /** Computes the dependencies of all called targets and returns an ordered
         * sequence String.
         * @param target the target that gets executed
         * /
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
         * /
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
        
        /** Returns the Element of a target given by its name. * /
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
 
        /** Returns a String of all Elements in the List in reverse order. * /
        protected String getReverseString (List l) {
            StringBuffer sb = new StringBuffer ();
            for (int x= (l.size() - 1); x > -1; x--) {
                sb.append (l.get(x));
                if (x > 0) sb.append (", "); // NOI18N
            }
            return sb.toString ();
        }
        
        /** Returns the value of this property. */
        @Override
        public String getValue () {
            /*XXX
            return computeTargetDependencies(getTarget());
             */
            return "XXX BuildSequenceProperty currently unimplemented";
        }
    }
    
    private static final class TargetOpenCookie implements OpenCookie {
        
        private final TargetLister.Target target;
        
        public TargetOpenCookie(TargetLister.Target target) {
            this.target = target;
        }
        
        public void open() {
            if (target.getScript().getParseException() != null) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            FileObject script = target.getScript().getFileObject();
            assert script != null : "No build script for " + target;
            EditorCookie editor;
            LineCookie lines;
            try {
                DataObject d = DataObject.find(script);
                editor = d.getCookie(EditorCookie.class);
                lines = d.getCookie(LineCookie.class);
                assert editor != null;
                assert lines != null;
            } catch (DataObjectNotFoundException e) {
                throw new AssertionError(e);
            }
            try {
                StyledDocument doc = editor.openDocument();
                InputSource in = AntProjectSupport.createInputSource(script, doc);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                final int[] line = new int[1];
                final String name = target.getName();
                class Handler extends DefaultHandler {
                    private Locator locator;
                    @Override
                    public void setDocumentLocator(Locator l) {
                        locator = l;
                    }
                    @Override
                    public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                        if (line[0] == 0) {
                            if (qname.equals("target") && name.equals(attr.getValue("name"))) { // NOI18N
                                line[0] = locator.getLineNumber();
                            }
                        }
                    }
                }
                parser.parse(in, new Handler());
                if (line[0] < 1) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                lines.getLineSet().getCurrent(line[0] - 1).show(Line.SHOW_GOTO);
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
                return;
            }
        }
        
    }
    
}
