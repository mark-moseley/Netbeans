/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.UnboundTargetAlert;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Element;

/**
 * Action bindings for a freeform project.
 * @author Jesse Glick
 */
public final class Actions implements ActionProvider {

    /**
     * Some routine global actions for which we can supply a display name.
     * These are IDE-specific.
     */
    private static final Set/*<String>*/ COMMON_IDE_GLOBAL_ACTIONS = new HashSet(Arrays.asList(new String[] {
        ActionProvider.COMMAND_DEBUG,
    }));
    /**
     * Similar to {@link #COMMON_IDE_GLOBAL_ACTIONS}, but these are not IDE-specific.
     * We also mark all of these as bound in the project; if the user
     * does not really have a binding, they are prompted for one when
     * the action is "run".
     */
    private static final Set/*<String>*/ COMMON_NON_IDE_GLOBAL_ACTIONS = new HashSet(Arrays.asList(new String[] {
        ActionProvider.COMMAND_BUILD,
        ActionProvider.COMMAND_CLEAN,
        ActionProvider.COMMAND_REBUILD,
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_TEST,
        // XXX JavaProjectConstants.COMMAND_JAVADOC
        "javadoc", // NOI18N
        // XXX WebProjectConstants.COMMAND_REDEPLOY
        // XXX should this really be here? perhaps not, once web part of #46886 is implemented...
        "redeploy", // NOI18N
    }));
    
    private final FreeformProject project;
    
    public Actions(FreeformProject project) {
        this.project = project;
    }
    
    public String[] getSupportedActions() {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            return new String[0];
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        // Use a set, not a list, since when using context you can define one action several times:
        Set/*<String>*/ names = new LinkedHashSet(actions.size());
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            names.add(actionEl.getAttribute("name")); // NOI18N
        }
        // #46886: also always enable all common global actions, in case they should be selected:
        names.addAll(COMMON_NON_IDE_GLOBAL_ACTIONS);
        return (String[])names.toArray(new String[names.size()]);
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        boolean foundAction = false;
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                // XXX perhaps check also existence of script
                Element contextEl = Util.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
                if (contextEl != null) {
                    // Check whether the context contains files all in this folder,
                    // matching the pattern if any, and matching the arity (single/multiple).
                    Map/*<String,FileObject>*/ selection = findSelection(contextEl, context, project);
                    if (selection.size() == 1) {
                        // Definitely enabled.
                        return true;
                    } else if (!selection.isEmpty()) {
                        // Multiple selection; check arity.
                        Element arityEl = Util.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                        assert arityEl != null : "No <arity> in <context> for " + command;
                        if (Util.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL) != null) { // NOI18N
                            // Supports multiple selection, take it.
                            return true;
                        }
                    }
                } else {
                    // Not context-sensitive.
                    return true;
                }
            }
        }
        if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
            // #46886: these are always enabled if they are not specifically bound.
            return true;
        }
        if (foundAction) {
            // Was at least one context-aware variant but did not match.
            return false;
        } else {
            throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
        }
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            throw new IllegalArgumentException("No commands supported"); // NOI18N
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        boolean foundAction = false;
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                foundAction = true;
                runConfiguredAction(project, actionEl, context);
            }
        }
        if (!foundAction) {
            if (COMMON_NON_IDE_GLOBAL_ACTIONS.contains(command)) {
                // #46886: try to bind it.
                if (addGlobalBinding(command)) {
                    // If bound, run it immediately.
                    invokeAction(command, context);
                }
            } else {
                throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
            }
        }
    }
    
    /**
     * Find a file selection in a lookup context based on a project.xml <context> declaration.
     * If all DataObject's (or FileObject's) in the lookup match the folder named in the declaration,
     * and match any optional pattern declaration, then they are returned as a map from relative
     * path to actual file object. Otherwise an empty map is returned.
     */
    private static Map/*<String,FileObject>*/ findSelection(Element contextEl, Lookup context, FreeformProject project) {
        Collection/*<FileObject>*/ files = context.lookup(new Lookup.Template(FileObject.class)).allInstances();
        if (files.isEmpty()) {
            // Try again with DataObject's.
            Collection/*<DataObject>*/ filesDO = context.lookup(new Lookup.Template(DataObject.class)).allInstances();
            if (filesDO.isEmpty()) {
                 return Collections.EMPTY_MAP;
            }
            files = new ArrayList(filesDO.size());
            Iterator it = filesDO.iterator();
            while (it.hasNext()) {
                files.add(((DataObject) it.next()).getPrimaryFile());
            }
        }
        Element folderEl = Util.findElement(contextEl, "folder", FreeformProjectType.NS_GENERAL); // NOI18N
        assert folderEl != null : "Must have <folder> in <context>";
        String rawtext = Util.findText(folderEl);
        assert rawtext != null : "Must have text contents in <folder>";
        String evaltext = project.evaluator().evaluate(rawtext);
        if (evaltext == null) {
            return Collections.EMPTY_MAP;
        }
        FileObject folder = project.helper().resolveFileObject(evaltext);
        if (folder == null) {
            return Collections.EMPTY_MAP;
        }
        Pattern pattern = null;
        Element patternEl = Util.findElement(contextEl, "pattern", FreeformProjectType.NS_GENERAL); // NOI18N
        if (patternEl != null) {
            String text = Util.findText(patternEl);
            assert text != null : "Must have text contents in <pattern>";
            try {
                pattern = Pattern.compile(text);
            } catch (PatternSyntaxException e) {
                org.netbeans.modules.ant.freeform.Util.err.annotate(e, ErrorManager.UNKNOWN, "From <pattern> in " + FileUtil.getFileDisplayName(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH)), null, null, null); // NOI18N
                org.netbeans.modules.ant.freeform.Util.err.notify(e);
                return Collections.EMPTY_MAP;
            }
        }
        Map/*<String,FileObject>*/ result = new HashMap();
        Iterator it = files.iterator();
        while (it.hasNext()) {
            FileObject file = (FileObject) it.next();
            String path = FileUtil.getRelativePath(folder, file);
            if (path == null) {
                return Collections.EMPTY_MAP;
            }
            if (pattern != null && !pattern.matcher(path).find()) {
                return Collections.EMPTY_MAP;
            }
            result.put(path, file);
        }
        return result;
    }
    
    /**
     * Run a project action as described by subelements <script> and <target>.
     */
    private static void runConfiguredAction(FreeformProject project, Element actionEl, Lookup context) {
        String script;
        Element scriptEl = Util.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
        if (scriptEl != null) {
            script = Util.findText(scriptEl);
        } else {
            script = "build.xml"; // NOI18N
        }
        String scriptLocation = project.evaluator().evaluate(script);
        FileObject scriptFile = project.helper().resolveFileObject(scriptLocation);
        if (scriptFile == null) {
            return;
        }
        List/*<Element>*/ targets = Util.findSubElements(actionEl);
        List/*<String>*/ targetNames = new ArrayList(targets.size());
        Iterator it2 = targets.iterator();
        while (it2.hasNext()) {
            Element targetEl = (Element)it2.next();
            if (!targetEl.getLocalName().equals("target")) { // NOI18N
                continue;
            }
            targetNames.add(Util.findText(targetEl));
        }
        String[] targetNameArray;
        if (!targetNames.isEmpty()) {
            targetNameArray = (String[])targetNames.toArray(new String[targetNames.size()]);
        } else {
            // Run default target.
            targetNameArray = null;
        }
        Properties props = new Properties();
        Element contextEl = Util.findElement(actionEl, "context", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextEl != null) {
            Map/*<String,FileObject>*/ selection = findSelection(contextEl, context, project);
            if (selection.isEmpty()) {
                return;
            }
            String separator = null;
            if (selection.size() > 1) {
                // Find the right separator.
                Element arityEl = Util.findElement(contextEl, "arity", FreeformProjectType.NS_GENERAL); // NOI18N
                assert arityEl != null : "No <arity> in <context> for " + actionEl.getAttribute("name");
                Element sepFilesEl = Util.findElement(arityEl, "separated-files", FreeformProjectType.NS_GENERAL); // NOI18N
                if (sepFilesEl == null) {
                    // Only handles single files -> skip it.
                    return;
                }
                separator = Util.findText(sepFilesEl);
            }
            Element formatEl = Util.findElement(contextEl, "format", FreeformProjectType.NS_GENERAL); // NOI18N
            assert formatEl != null : "No <format> in <context> for " + actionEl.getAttribute("name");
            String format = Util.findText(formatEl);
            StringBuffer buf = new StringBuffer();
            Iterator it = selection.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if (format.equals("absolute-path")) { // NOI18N
                    File f = FileUtil.toFile((FileObject) entry.getValue());
                    if (f == null) {
                        // Not a disk file??
                        return;
                    }
                    buf.append(f.getAbsolutePath());
                } else if (format.equals("relative-path")) { // NOI18N
                    buf.append((String) entry.getKey());
                } else if (format.equals("absolute-path-noext")) { // NOI18N
                    File f = FileUtil.toFile((FileObject) entry.getValue());
                    if (f == null) {
                        // Not a disk file??
                        return;
                    }
                    String path = f.getAbsolutePath();
                    int dot = path.lastIndexOf('.');
                    if (dot > path.lastIndexOf('/')) {
                        path = path.substring(0, dot);
                    }
                    buf.append(path);
                } else if (format.equals("relative-path-noext")) { // NOI18N
                    String path = (String) entry.getKey();
                    int dot = path.lastIndexOf('.');
                    if (dot > path.lastIndexOf('/')) {
                        path = path.substring(0, dot);
                    }
                    buf.append(path);
                } else {
                    assert format.equals("java-name") : format;
                    String path = (String) entry.getKey();
                    int dot = path.lastIndexOf('.');
                    String dotless;
                    if (dot == -1 || dot < path.lastIndexOf('/')) {
                        dotless = path;
                    } else {
                        dotless = path.substring(0, dot);
                    }
                    String javaname = dotless.replace('/', '.');
                    buf.append(javaname);
                }
                if (it.hasNext()) {
                    assert separator != null;
                    buf.append(separator);
                }
            }
            Element propEl = Util.findElement(contextEl, "property", FreeformProjectType.NS_GENERAL); // NOI18N
            assert propEl != null : "No <property> in <context> for " + actionEl.getAttribute("name");
            String prop = Util.findText(propEl);
            assert prop != null : "Must have text contents in <property>";
            props.setProperty(prop, buf.toString());
        }
        it2 = targets.iterator();
        while (it2.hasNext()) {
            Element propEl = (Element)it2.next();
            if (!propEl.getLocalName().equals("property")) { // NOI18N
                continue;
            }
            String rawtext = Util.findText(propEl);
            if (rawtext == null) {
                // Legal to have e.g. <property name="intentionally-left-blank"/>
                rawtext = ""; // NOI18N
            }
            String evaltext = project.evaluator().evaluate(rawtext); // might be null
            if (evaltext != null) {
                props.setProperty(propEl.getAttribute("name"), evaltext); // NOI18N
            }
        }
        TARGET_RUNNER.runTarget(scriptFile, targetNameArray, props);
    }
    
    public static Action[] createContextMenu(FreeformProject p) {
        List/*<Action>*/ actions = new ArrayList();
        actions.add(CommonProjectActions.newFileAction());
        // Requested actions.
        Element genldata = p.helper().getPrimaryConfigurationData(true);
        Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl != null) {
            Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
            if (contextMenuEl != null) {
                actions.add(null);
                List/*<Element>*/ actionEls = Util.findSubElements(contextMenuEl);
                Iterator it = actionEls.iterator();
                while (it.hasNext()) {
                    Element actionEl = (Element)it.next();
                    if (actionEl.getLocalName().equals("ide-action")) { // NOI18N
                        String cmd = actionEl.getAttribute("name");
                        String displayName;
                        if (COMMON_IDE_GLOBAL_ACTIONS.contains(cmd) || COMMON_NON_IDE_GLOBAL_ACTIONS.contains(cmd)) {
                            displayName = NbBundle.getMessage(Actions.class, "CMD_" + cmd);
                        } else {
                            // OK, fall back to raw name.
                            displayName = cmd;
                        }
                        actions.add(ProjectSensitiveActions.projectCommandAction(cmd, displayName, null));
                    } else if (actionEl.getLocalName().equals("separator")) { // NOI18N
                        actions.add(null);
                    } else {
                        assert actionEl.getLocalName().equals("action") : actionEl;
                        actions.add(new CustomAction(p, actionEl));
                    }
                }
            }
        }
        // Back to generic actions.
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        actions.add(null);
        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    private static final class CustomAction extends AbstractAction {

        private final FreeformProject p;
        private final Element actionEl;
        
        public CustomAction(FreeformProject p, Element actionEl) {
            this.p = p;
            this.actionEl = actionEl;
        }
        
        public void actionPerformed(ActionEvent e) {
            runConfiguredAction(p, actionEl, Lookup.EMPTY);
        }
        
        public boolean isEnabled() {
            String script;
            Element scriptEl = Util.findElement(actionEl, "script", FreeformProjectType.NS_GENERAL); // NOI18N
            if (scriptEl != null) {
                script = Util.findText(scriptEl);
            } else {
                script = "build.xml"; // NOI18N
            }
            String scriptLocation = p.evaluator().evaluate(script);
            return p.helper().resolveFileObject(scriptLocation) != null;
        }
        
        public Object getValue(String key) {
            if (key.equals(Action.NAME)) {
                Element labelEl = Util.findElement(actionEl, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                return Util.findText(labelEl);
            } else {
                return super.getValue(key);
            }
        }
        
    }
    
    // Overridable for unit tests only:
    static TargetRunner TARGET_RUNNER = new TargetRunner();
    
    static class TargetRunner {
        public TargetRunner() {}
        public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props) {
            try {
                ActionUtils.runTarget(scriptFile, targetNameArray, props);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * Prompt the user to make a binding for a common global command.
     * Available targets are shown. If one is selected, it is bound
     * (and also added to the context menu of the project), as if the user
     * had picked it in {@link TargetMappingPanel}.
     * @param command the command name as in {@link ActionProvider}
     * @return true if a binding was successfully created, false if it was cancelled
     * @see "#46886"
     */
    private boolean addGlobalBinding(String command) {
        try {
            return new UnboundTargetAlert(project, command).accepted();
        } catch (IOException e) {
            // Problem generating bindings - so skip it.
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
}
