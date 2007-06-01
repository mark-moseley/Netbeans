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
package org.netbeans.modules.swingapp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.Toolkit;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.form.FormEditorSupport;
import org.netbeans.modules.form.FormModelEvent;
import org.netbeans.modules.form.FormModelListener;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 * The ActionManager is a singleton which tracks all actions throughout the project.
 * It allows other parts of the SwingApp module search for actions, get action properties,
 * access the list of components bound to actions, and create/update/delete actions.
 *
 * There is one ActionManager singleton per project. This singleton can be obtained
 * by passing any file in the project to the static getActionManager() method.
 *
 * 
 * TODOs:   the internal maps are currently never cleared. They should be cleared when the project
 * they are attached to is closed. Perhaps more often.
 * 
 * We need a way to scan when a new class is created or added to the project. We need to do this without
 * rescaning the *entire* project.
 * 
 * 
 * @author joshua.marinacci@sun.com
 */
public class ActionManager {
    
    private static Map<Project,ActionManager> ams;
    private static Map<ActionManager, Project> reverseams;
    
    private static ActionManager emptyActionManager = new ActionManager(null);
    private static final boolean DEBUG = false;

    public static synchronized ActionManager getActionManager(FileObject fileInProject) {
        if(ams == null) {
            ams = new HashMap<Project,ActionManager>();
            reverseams = new HashMap<ActionManager,Project>();
        }
        Project proj = getProject(fileInProject);
        ActionManager am = ams.get(proj);
        if(am == null && AppFrameworkSupport.isFrameworkEnabledProject(fileInProject)) {
            ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
            FileObject root = cp.findOwnerRoot(fileInProject);
            am = new ActionManager(root);
            ams.put(proj,am); // PENDING never removed, this is memory leak!!!
            reverseams.put(am,proj);
            am = ams.get(proj);
        }
        return am;
    }
    
    public static synchronized ActionManager getActionManager(Project project) {
        Sources srcs = project.getLookup().lookup(Sources.class);
        SourceGroup groups[] = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if(groups != null && groups.length > 0) {
            return ActionManager.getActionManager(groups[0].getRootFolder());
        } else {
            return null;
        }
    }
    
    
    private static void addProject(Project p) {
        ActionManager am = ActionManager.getActionManager(p);
        // do the first scan for actions
        if(am != null) {
            am.rescan();
        }
    }
    private static void removeProject(Project p) {
        if(ams != null) {
            ActionManager am = ams.get(p);
            ams.remove(p);
            reverseams.remove(am);
        }
    }
    
    public static ActionManager getEmptyActionManager() {
        return emptyActionManager;
    }
    
    public static Set<Project> getKnownProjects() {
        if(ams == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(ams.keySet());
    }
    
    
    /**
     * Removes all closed projects by looking through the list of open projects
     * and removing any extras. Returns true of there were any projects to be removed
     * @param openProjects an array of currently open projects
     * @return true if any projects were removed, else false
     */
    public static boolean clearClosedProjects(Project[] openProjects) {
        boolean updated = false;
        Set<Project> known = getKnownProjects();
        known = new HashSet(known);
        Set<Project> newSet = new HashSet<Project>();
        for(Project p : openProjects) {
            if(!known.contains(p)) {
                newSet.add(p);
            }
            known.remove(p);
        }
        if(known.size() > 0) {
            for(Project p : known) {
                removeProject(p);
            }
            updated = true;
        }
        if(newSet.size() > 0) {
            for(Project p : newSet) {
                addProject(p);
            }
            updated = true;
        }
        
        return updated;
    }
    
    
    // a map of all actions by classname
    private Map<String,List<ProxyAction>> actions;
    // a list of all actions
    private List<ProxyAction> actionList;
    
    // maps actions (by id) to rad components with action properties set to that action
    private Map<String,List<RADComponent>> boundComponents =
            new HashMap<String,List<RADComponent>>();
    // the property change listeners for monitoring changes to the list of actions
    private List<PropertyChangeListener> pcls;
    // the listener for changes to individual actions (their own properties)
    private List<ActionChangedListener> acls;
    // the root object of this ActionManager's project
    private FileObject root;
    
    public Project getProject() {
        return reverseams.get(this);
    }
    
    public FileObject getApplicationClassFile() {
        String appClassName = AppFrameworkSupport.getApplicationClassName(getRoot());
        return getFileForClass(appClassName);
    }
    
    /** Creates a new instance of ActionManager */
    private ActionManager(FileObject root) {
        this.root = root;
        actionList = new ArrayList<ProxyAction>();
        pcls = new ArrayList<PropertyChangeListener>();
        acls = new ArrayList<ActionChangedListener>();
        actions = new HashMap<String, List<ProxyAction>>();
    }
    
    public FileObject getRoot() {
        return root;
    }
    
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.pcls.add(pcl);
    }
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.pcls.remove(pcl);
    }
    
    public void addActionChangedListener(ActionChangedListener acl) {
        this.acls.add(acl);
    }
    public void removeActionChangedListener(ActionChangedListener acl) {
        this.acls.remove(acl);
    }
    
    public static interface ActionChangedListener {
        public void actionChanged(ProxyAction action);
    }
    
    
    
    /** Rescan the entire project.  This could be slow. Should be optimized
     *  in the future so that we don't need to do the full scan very often
     */
    public void rescan() {
        actions = new HashMap<String,List<ProxyAction>>();
        scanFolderForActions(getRoot(), actions);
        actionList.clear();
        for(String appClsName : actions.keySet()) {
            actionList.addAll(actions.get(appClsName));
        }
        fireStructureChanged();
    }
    
    
    
    
    
    public List<ProxyAction> getAllActions() {
        return actionList;
    }
    
    public Collection<String> getAllClasses() {
        return actions.keySet();
    }
    
    public List<ProxyAction> getActions(String defClass, boolean rescan) {
        if (rescan) {
            getActionsFromFile(getFileForClass(defClass), actions);
        }
        List<ProxyAction> list = actions.get(defClass);
        return list != null ? list : Collections.<ProxyAction>emptyList();
    }
    
    public static List<ProxyAction> getActions(FileObject sourceFile, boolean rescan) {
        ActionManager am = getActionManager(sourceFile);
        if (rescan) {
            getActionsFromFile(sourceFile, am.actions);
        }
        return am.getActions(AppFrameworkSupport.getClassNameForFile(sourceFile), false);
    }
    
    
    

    void jumpToActionSource(ProxyAction action) {
        FileObject sourceFile = getFileForClass(action.getClassname());
        try {
            Integer result = (Integer) new ActionMethodTask(sourceFile, action.getId()) {
                Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    return (int) controller.getTrees().getSourcePositions().getStartPosition(
                            controller.getCompilationUnit(), methodTree);
                }
            }.execute();

            int position = result.intValue();
            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie editorCookie = (EditorCookie) dobj.getCookie(EditorCookie.class);
            // make sure the document is opened
            if(editorCookie.getDocument() == null) {
                editorCookie.openDocument();
            }
            // make sure the editor window is open
            editorCookie.open();
            
            Line lineObj = null;
            if (editorCookie != null) {
                Line.Set lineSet = editorCookie.getLineSet();
                int line = editorCookie.getDocument().getParagraphElement(0).getParentElement().getElementIndex(position);
                lineObj = lineSet.getCurrent(line);
            }
            if (lineObj == null) {
                System.out.println("the line is still null"); //log
                Toolkit.getDefaultToolkit().beep();
            } else {
                lineObj.show(Line.SHOW_GOTO);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    boolean isExistingMethod(String className, String methodName) {
        FileObject sourceFile = getFileForClass(className);
        try {
            Object result = new ActionMethodTask(sourceFile, methodName) {
                Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    return true;
                }
            }.execute();
            return Boolean.TRUE.equals(result);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
    }

    /**
     * @return true if the method was successfuly created or already existed
     */
    boolean createActionMethod(final ProxyAction action) {
        if (isExistingMethod(action.getClassname(), action.getId())) {
            return true;
        }

        try {
            FileObject sourceFile = getFileForClass(action.getClassname());
            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            if(ec.getDocument() == null) {
                ec.openDocument();
            }
            //ec.open(); //josh: we fail if the document isn't opened yet. is there a better way to do this?
            Document doc = ec.getDocument();
            int pos;
            if (ec instanceof FormEditorSupport) {
                // in form's source add before the variables section
                doc = ec.getDocument();
                pos = ((FormEditorSupport)ec).getVariablesSection().getStartPosition().getOffset();
            } else {
                // in general java source add at the end of the class
                Integer result = (Integer) new ClassTask(sourceFile) {
                    Object run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                        return (int) controller.getTrees().getSourcePositions().getEndPosition(
                                controller.getCompilationUnit(), classTree);
                    }
                }.execute();
                javax.swing.text.Element docRoot = doc.getDefaultRootElement();
                pos = docRoot.getElement(docRoot.getElementIndex(result.intValue()))
                        .getStartOffset();
            }
            
            StringBuilder buf = new StringBuilder();
            String indent = "    "; // NOI18N
            String taskName = action.isTaskEnabled() ? taskNameForAction(action) : null;
            buf.append(indent);
            buf.append(getAnnotationCode(action));
            buf.append("\n"); // NOI18N
            buf.append(indent);
            buf.append("public "); // NOI18N
            buf.append(taskName != null ? "application.Task " : "void "); // NOI18N
            buf.append(action.getId());
            buf.append("() {\n"); // NOI18N
            buf.append(indent).append(indent);
            if (taskName != null) {
                buf.append("return new ");
                buf.append(taskName);
                buf.append("();\n");
                taskName = getNonExistingTaskName(action.getClassname(), taskName);
            } else {
                buf.append("// put your action code here\n"); // NOI18N
            }
            buf.append(indent);
            buf.append("}\n\n"); // NOI18N

            if (taskName != null) {
                buf.append(getTaskClassImplCode(taskName, null));
                buf.append("\n"); // NOI18N
            }
            doc.insertString(pos, buf.toString(), null);
            return true;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }

        // [would be better to use the java source infrastructure, but it is too buggy...]
/*        try {
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    // get the class tree
                    ClassTree classTree = null;
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                classTree = classT;
                                break;
                            }
                        }
                    }
                    if (classTree == null) {
                        return;
                    }
                    // create new method tree
                    TreeMaker make = workingCopy.getTreeMaker();
                    AnnotationTree annotation = make.Annotation(
                            make.QualIdent(workingCopy.getElements().getTypeElement("application.Action")), // NOI18N
                                           Collections.<ExpressionTree>emptyList());
                    // TODO annotation attributes
                    ModifiersTree methodModifiers = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>singletonList(annotation));
                    Tree returnType = action.isTaskEnabled()
                            ? make.QualIdent(workingCopy.getElements().getTypeElement("application.Task")) // NOI18N
                            : make.PrimitiveType(TypeKind.VOID);
                    MethodTree methodTree = make.Method(
                            methodModifiers,
                            action.getId(),
                            returnType,
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.<VariableTree>emptyList(),
                            Collections.<ExpressionTree>emptyList(),
                            make.Block(Collections.<StatementTree>emptyList(), false), // "// put your action code here", // NOI18N
                            null);
                    // add the method tree to the class tree
                    ClassTree modifiedClassTree = make.addClassMember(classTree, methodTree);
                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            });
            result.commit();
            return true;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return false; */
    }

    private static String taskNameForAction(ProxyAction action) {
        String actionName = action.getId();
        return actionName.substring(0, 1).toUpperCase() + actionName.substring(1) + "Task"; // NOI18N
    }

    private String getNonExistingTaskName(String className, final String taskName) {
        FileObject sourceFile = getFileForClass(className);
        try {
            Object result = new ClassTask(sourceFile) {
                Object run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                    for (TypeElement el: ElementFilter.typesIn(classElement.getEnclosedElements())) {
                        if (el.getSimpleName().toString().equals(taskName)) {
                            return null;
                        }
                    }
                    // TODO check if found type is really a Task, find free name
                    return taskName;
                }
            }.execute();
            return (String) result;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return taskName;
        }
    }

    private static final String TASK_CLASS_TEMPLATE =
            "    private class MyTask extends application.Task<Object, Void> {\n" // NOI18N
          + "        MyTask() {\n" // NOI18N
          + "            // Runs on the EDT.  Copy GUI state that\n" // NOI18N
          + "            // doInBackground() depends on from parameters\n" // NOI18N
          + "            // to MyTask fields, here.\n" // NOI18N
          + "__CTOR_CODE__" // NOI18N
          + "        }\n" // NOI18N
          + "        @Override protected Object doInBackground() {\n" // NOI18N
          + "            // Your Task's code here.  This method runs\n" // NOI18N
          + "            // on a background thread, so don't reference\n" // NOI18N
          + "            // the Swing GUI from here.\n" // NOI18N
          + "            return null;  // return your result\n" // NOI18N
          + "        }\n" // NOI18N
          + "        @Override protected void succeeded(Object result) {\n" // NOI18N
          + "            // Runs on the EDT.  Update the GUI based on\n" // NOI18N
          + "            // the result computed by doInBackground().\n" // NOI18N
          + "        }\n" // NOI18N
          + "    }\n"; // NOI18N

    private static String getTaskClassImplCode(String taskName, String ctorCode) {
        if (ctorCode == null) {
            ctorCode = "";
        }
        if (ctorCode.length() > 0 && !ctorCode.endsWith("\n")) { // NOI18N
            ctorCode = ctorCode + "\n"; // NOI18N
        }
        if (ctorCode.length() > 0) { // provisional indentation, PENDING...
            StringBuilder buf = new StringBuilder();
            String indent = "            "; // NOI18N
            int index = 0;
            boolean lineStart = true;
            for (int i=0; i < ctorCode.length(); i++) {
                char c = ctorCode.charAt(i);
                if (c == '\n') {
                    if (lineStart) {
                        buf.append("\n"); // NOI18N
                    } else {
                        buf.append(ctorCode.substring(index, i+1));
                    }
                    lineStart = true;
                    index = i + 1;
                } else if (c > ' ' && lineStart) {
                    buf.append(indent);
                    lineStart = false;
                    index = i;
                }
            }
            ctorCode = buf.toString();
        }
        return TASK_CLASS_TEMPLATE.replace("__CTOR_CODE__", ctorCode) // NOI18N
                .replace("MyTask", taskName); // NOI18N
    }

    public List<RADComponent> getBoundComponents(ProxyAction act) {
        if(!boundComponents.containsKey(getKey(act))) {
            return new ArrayList<RADComponent>();
        }
        return boundComponents.get(getKey(act));
    }
    
    
    public void removeAllBoundComponents(FormModel model) {
        for(String key : boundComponents.keySet()) {
            List<RADComponent> comps = boundComponents.get(key);
            
            Iterator<RADComponent> it = comps.iterator();
            while(it.hasNext()) {
                RADComponent comp = it.next();
                if(comp != null && comp.getFormModel() == model) {
                    it.remove();
                }
            }
        }
    }
    
    
    
    
    
    
    public void addNewAction(ProxyAction act) {
        List<ProxyAction> list = actions.get(act.getClassname());
        if( list == null) {
            list = new ArrayList<ProxyAction>();
            actions.put(act.getClassname(), list);
        }
        list.add(act);
        actionList.add(act);
        fireStructureChanged();
    }
    
    private void safeRemove(List<ProxyAction> actions, ProxyAction act) {
        ProxyAction found = null;
        for(ProxyAction a : actionList) {
            if(actionsMatch(a, act)) {
                found = a;
            }
        }
        if(found != null) {
            actions.remove(found);
        }
    }
    
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
    
    public void updateAction(ProxyAction action) {
        List<ProxyAction> actions = getActions(action.getClassname(), false);
        boolean replaced = false;
        for(ProxyAction a : actions) {
            if(a.getId().equals(action.getId())) {
                //actions.remove(a);
                // do a replace instead of a remove
                int n = actions.indexOf(a);
                if(n >= 0) {
                    actions.remove(n);
                    actions.add(n, action);
                }
                // do a special search remove because remove(a) isn't working
                //safeRemove(actionList,a);
                for(int i=0; i<actionList.size(); i++) {
                    ProxyAction target = actionList.get(i);
                    if(actionsMatch(action, target)) {
                        actionList.remove(target);
                        actionList.add(i,action);
                    }
                }
                replaced = true;
                break;
            }
        }
        
        if(!replaced) {
            actions.add(action);
            actionList.add(action);
        }
        updateActionMethod(action, getFileForClass(action.getClassname()));
        // this will update the global action table
        //fireStructureChanged();
        fireActionChanged(action); //josh: is this enough of an update?

        // update any form components which use this action, if they are open.
        List<RADComponent> boundList = getBoundComponents(action);
        for(RADComponent comp : boundList) {
            if(comp != null) {
                RADProperty prop = comp.getBeanProperty("action");//NOI18N
                if(prop != null) {
                    try {
                        //set to null then to the proxy to force an update
                        prop.setValue(null);
                        prop.setValue(action);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void updateActionMethod(final ProxyAction action, final FileObject sourceFile) {
        try {
            int[] positions = getAnnotationPositions(action, sourceFile);
            if (positions == null) {
                return;
            }

            // update the method signature and body
            final String taskName = taskNameForAction(action);
            final String newTaskName = getNonExistingTaskName(action.getClassname(), taskName);
            final String[] oldBodyText = new String[1];
            JavaSource js = JavaSource.forFileObject(sourceFile);
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                Trees trees = workingCopy.getTrees();
                                TreePath classTPath = trees.getPath(cut, classT);
                                TypeElement classEl = (TypeElement) trees.getElement(classTPath);
                                for (ExecutableElement el : ElementFilter.methodsIn(classEl.getEnclosedElements())) {
                                    if (el.getSimpleName().toString().equals(action.getId())
                                            && el.getModifiers().contains(Modifier.PUBLIC)) {
                                        if (isAsyncActionMethod(el) != action.isTaskEnabled()) {
                                            MethodTree method = trees.getTree(el);
                                            MethodTree modified;
                                            TreeMaker make = workingCopy.getTreeMaker();
                                            BlockTree body = method.getBody();
                                            SourcePositions sp = trees.getSourcePositions();
                                            int start = (int) sp.getStartPosition(cut, body);
                                            int end = (int) sp.getEndPosition(cut, body);
                                            String bodyText = getMethodBodyWithoutBraces(workingCopy.getText().substring(start, end));
                                            if (action.isTaskEnabled()) { // switch to Task
                                                String genTaskName;
                                                if (newTaskName != null) {
                                                    genTaskName = newTaskName;
                                                    oldBodyText[0] = bodyText;
                                                    bodyText = ""; // NOI18N
                                                } else {
                                                    genTaskName = taskName;
                                                    bodyText = getCommentedBodyText(bodyText);
                                                }

                                                modified = make.Method(
                                                        method.getModifiers(),
                                                        method.getName(),
                                                        make.QualIdent(workingCopy.getElements().getTypeElement("application.Task")), // NOI18N
                                                        method.getTypeParameters(),
                                                        method.getParameters(),
                                                        method.getThrows(),
                                                        "{\nreturn new " + genTaskName + "();\n" + bodyText + "}", // NOI18N
                                                        null);
                                            } else { // switch to void
                                                modified = make.Method(
                                                        method.getModifiers(),
                                                        method.getName(),
                                                        make.PrimitiveType(TypeKind.VOID),
                                                        method.getTypeParameters(),
                                                        method.getParameters(),
                                                        method.getThrows(),
                                                        "{\n" + getCommentedBodyText(bodyText) + "}", // NOI18N
                                                        null);
                                            }
                                            workingCopy.rewrite(method, modified);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
            result.commit();

            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            // make sure it's open before we access it
            if(ec.getDocument() == null) {
                ec.openDocument();
            }
            Document doc = ec.getDocument();

            if (newTaskName != null && action.isTaskEnabled()) { // generate Task impl class
                Integer methodEndPosition = (Integer) new ActionMethodTask(sourceFile, action.getId()) {
                    Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                        return (int) controller.getTrees().getSourcePositions().getEndPosition(
                                controller.getCompilationUnit(), methodTree);
                    }
                }.execute();
                javax.swing.text.Element docRoot = doc.getDefaultRootElement();
                int pos = docRoot.getElement(docRoot.getElementIndex(methodEndPosition.intValue()) + 1)
                        .getStartOffset();
                doc.insertString(pos,
                                 "\n" + getTaskClassImplCode(newTaskName, oldBodyText[0]), // NOI18N
                                 null);
            }

            // update the annotation attributes
            // (the task class was generated below, so the annotation position is not affected)
            int startPos = positions[0];
            int endPos = positions[1];
            doc.remove(startPos, endPos-startPos);
            doc.insertString(startPos, getAnnotationCode(action), null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        // [would be better to use the java source infrastructure, but it is too buggy...]
/*        JavaSource js = JavaSource.forFileObject(sourceFile);
        try {
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                TreePath classTPath = workingCopy.getTrees().getPath(cut, classT);
                                TypeElement classEl = (TypeElement) workingCopy.getTrees().getElement(classTPath);
                                for (ExecutableElement el : ElementFilter.methodsIn(classEl.getEnclosedElements())) {
                                    if (el.getSimpleName().toString().equals(action.getId())
                                            && el.getModifiers().contains(Modifier.PUBLIC)) {
                                        TreeMaker make = workingCopy.getTreeMaker();
                                        ModifiersTree modifiers = workingCopy.getTrees().getTree(el)
                                                .getModifiers();
                                        for (AnnotationTree at : modifiers.getAnnotations()) {
                                            TypeElement annEl = (TypeElement) workingCopy.getTrees().getElement(
                                                    workingCopy.getTrees().getPath(cut, at.getAnnotationType()));
                                            if (annEl.getQualifiedName().toString().equals("application.Action")) { // NOI18N
                                                // [try to update the annotation attr values, or remove and re-add the annotation completely?]
                                                ModifiersTree newModifiers = make.removeModifiersAnnotation(modifiers, at);
//                                                AnnotationTree newAnnT = at;
//                                                List<ExpressionTree> annAttrs = (List<ExpressionTree>) at.getArguments();
//                                                int index = 0;
//                                                while (index < annAttrs.size()) {
//                                                    ExpressionTree attr = annAttrs.get(index);
//                                                    if (attr.getKind() == Tree.Kind.ASSIGNMENT) {
//                                                        AssignmentTree assTree = (AssignmentTree) attr;
//                                                        Tree attrVar = assTree.getVariable();
//                                                        if (attrVar.getKind() == Tree.Kind.IDENTIFIER) {
//                                                            IdentifierTree attrVarIdent = (IdentifierTree) attrVar;
//                                                            String attrName = attrVarIdent.getName().toString();
//                                                            if (action.isAnnotationAttributeUnset(attrName)
//                                                                    || action.isAnnotationAttributeSet(attrName)) {
//                                                                newAnnT = make.removeAnnotationAttrValue(newAnnT, attr);
//                                                                annAttrs = (List<ExpressionTree>) newAnnT.getArguments();
//                                                                index--;
//                                                            }
//                                                        }
//                                                    }
//                                                    index++;
//                                                }
                                                List<AssignmentTree> annAttrs = new LinkedList<AssignmentTree>();
                                                for (String attrName : ProxyAction.getAnnotationAttributeNames()) {
                                                    if (action.isAnnotationAttributeSet(attrName)) {
                                                        Object value = action.getAnnotationAttributeValue(attrName);
                                                        ExpressionTree expTree;
                                                        if (value instanceof String) {
                                                            expTree = make.Literal(value);
                                                        } else if (value instanceof ProxyAction.BlockingType) {
                                                            expTree = make.MemberSelect(
                                                                    make.MemberSelect(make.QualIdent(workingCopy.getElements().getTypeElement("application.Action")), // NOI18N
                                                                    "Block"), // NOI18N
                                                                    value.toString());
                                                        } else {
                                                            continue;
                                                        }
                                                        ExpressionTree identTree = make.Identifier(attrName);
                                                        AssignmentTree attrTree = make.Assignment(identTree, expTree);
                                                        annAttrs.add(attrTree);
//                                                        newAnnT = make.addAnnotationAttrValue(newAnnT, attrTree);
                                                    }
                                                }
                                                AnnotationTree newAnnT = make.Annotation(make.QualIdent(workingCopy.getElements().getTypeElement("application.Action")), annAttrs); // NOI18N
                                                newModifiers = make.addModifiersAnnotation(newModifiers, newAnnT);
//                                                if (newAnnT != at) {
//                                                    workingCopy.rewrite(at, newAnnT);
//                                                }
                                                workingCopy.rewrite(modifiers, newModifiers);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            result.commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } */
    }

    private static String getCommentedBodyText(String bodyText) {
        bodyText = getMethodBodyWithoutBraces(bodyText);
        StringBuilder buf = new StringBuilder();
        int lineStart = 0;
        for (int i=0; i < bodyText.length(); i++) {
            char c = bodyText.charAt(i);
            if (c == '\n' || i+1 == bodyText.length()) {
                buf.append("// "); // NOI18N
                buf.append(bodyText.substring(lineStart, i+1));
                lineStart = i + 1;
            }
        }
        return buf.toString();
    }

    private static String getMethodBodyWithoutBraces(String bodyText) {
        int first = -1;
        int last = -1;
        for (int i=0; i < bodyText.length(); i++) {
            char c = bodyText.charAt(i);
            if (c > ' ' && (c != '{' || first >= 0)) {
                break;
            } else if (c == '\n' && first >= 0) {
                first = i + 1;
                break;
            } else if (c == '{') {
                first = i + 1;
            }
        }
        for (int i=bodyText.length()-1; i >= 0 ; i--) {
            char c = bodyText.charAt(i);
            if (c > ' ' && (c != '}' || last >= 0)) {
                break;
            } else if (c == '\n' && first >= 0) {
                last = i;
                break;
            } else if (c == '}') {
                last = i;
            }
        }
        return bodyText.substring(first >= 0 ? first : 0, last >= 0 ? last : bodyText.length());
    }

    private static int[] getAnnotationPositions(ProxyAction action, FileObject sourceFile) throws IOException {
        return (int[]) new ActionMethodTask(sourceFile, action.getId()) {
            Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                CompilationUnitTree cut = controller.getCompilationUnit();
                Trees trees = controller.getTrees();
                ModifiersTree modifiers = methodTree.getModifiers();
                for (AnnotationTree at : modifiers.getAnnotations()) {
                    TypeElement annEl = (TypeElement) trees.getElement(
                            trees.getPath(cut, at.getAnnotationType()));
                    if (annEl.getQualifiedName().toString().equals("application.Action")) { // NOI18N
                        SourcePositions positions = trees.getSourcePositions();
                        return new int[] {
                                (int) positions.getStartPosition(cut, at),
                                (int) positions.getEndPosition(cut, at)
                        };
                    }
                }
                return null;
            }
        }.execute();
    }

    private static String getAnnotationCode(ProxyAction action) {
        StringBuilder buf = new StringBuilder();
        buf.append("@application.Action"); // NOI18N
        boolean anyAttr = false;
        for (String attrName : ProxyAction.getAnnotationAttributeNames()) {
            if (action.isAnnotationAttributeSet(attrName)) {
                Object value = action.getAnnotationAttributeValue(attrName);
                if (!anyAttr) {
                    buf.append("("); // NOI18N
                    anyAttr = true;
                } else {
                    buf.append(", "); // NOI18N
                }
                buf.append(attrName);
                buf.append("="); // NOI18N
                if (value instanceof ProxyAction.BlockingType) {
                    buf.append("application.Action.Block."); // NOI18N
                    buf.append(value);
                } else {
                    buf.append("\""); // NOI18N
                    buf.append(value);
                    buf.append("\""); // NOI18N
                }
            }
        }
        if (anyAttr) {
            buf.append(")"); // NOI18N
        }
        return buf.toString();
    }
    
    public void deleteAction(ProxyAction action) {
        String defClass = action.getClassname();
        FileObject file = getFileForClass(defClass);
        DesignResourceMap map = ResourceUtils.getDesignResourceMap(file);
        //String actionKey = action.getId()+".Action";
        //ResourceValueImpl res = map.getResourceValue(actionKey+".text",String.class);
        
        // delete the resources
        Collection<String> col = map.collectKeys(action.getId()+"\\..*",true); //NOI18N
        for(String s : col) {
            ResourceValueImpl res = map.getResourceValue(s,String.class);
            if(res != null) {
                map.removeResourceValue(res);
            }
        }
        
        // remove from main map
        Iterator<ProxyAction> it = getActions(defClass, false).iterator();
        while (it.hasNext()) {
            ProxyAction a = it.next();
            if (a.getId().equals(action.getId())) {
                it.remove();
                actionList.remove(a);
                break;
            }
        }
        
        // delete actions from the form
        // only works if the action is stored in the form it's used.
        // must search all forms in the future
        if(hasFormFile(file)) {
            FileObject formfile = getFormFile(file);
            if(formfile.canRead()) {
                try {
                    FormModel mod = getFormModel(formfile);
                    deleteAction(action, mod);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        // remove the annotation
        deleteActionAnnotation(action, file);
        
        // comment out the action code.
        // josh: we can't comment out code yet.
        // josh: We also don't wany to delete the method either. leave alone for now.
        //AppFrameworkSupport.deleteMethod(classDef,action);
        
    }
    
    private static void deleteActionAnnotation(ProxyAction action, FileObject sourceFile) {
        try {
            int[] positions = getAnnotationPositions(action, sourceFile);
            if (positions == null) {
                return;
            }
            DataObject dobj = DataObject.find(sourceFile);
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec == null) {
                return;
            }
            Document doc = ec.getDocument();
            int startPos = positions[0];
            int endPos = positions[1];
            String annotationText = doc.getText(startPos, endPos-startPos);
            javax.swing.text.Element docRoot = doc.getDefaultRootElement();
            javax.swing.text.Element line = docRoot.getElement(docRoot.getElementIndex(startPos));
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset();
            if (doc.getText(lineStart, lineEnd-lineStart).trim().equals(annotationText)) {
                // annotation is on separate line - remove the whole line
                startPos = lineStart;
                endPos = lineEnd;
            }
            doc.remove(startPos, endPos-startPos);
/*            JavaSource js = JavaSource.forFileObject(sourceFile);
            ModificationResult result = js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    for (Tree t: cut.getTypeDecls()) {
                        if (t.getKind() == Tree.Kind.CLASS) {
                            ClassTree classT = (ClassTree) t;
                            if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                                TreePath classTPath = workingCopy.getTrees().getPath(cut, classT);
                                TypeElement classEl = (TypeElement) workingCopy.getTrees().getElement(classTPath);
                                for (ExecutableElement el : ElementFilter.methodsIn(classEl.getEnclosedElements())) {
                                    if (el.getSimpleName().toString().equals(action.getId())
                                            && el.getModifiers().contains(Modifier.PUBLIC)) {
                                        ModifiersTree modifiers = workingCopy.getTrees().getTree(el)
                                                .getModifiers();
                                        for (AnnotationTree at : modifiers.getAnnotations()) {
                                            TypeElement annEl = (TypeElement) workingCopy.getTrees().getElement(
                                                    workingCopy.getTrees().getPath(cut, at.getAnnotationType()));
                                            if (annEl.getQualifiedName().toString().equals("application.Action")) { // NOI18N
                                                TreeMaker make = workingCopy.getTreeMaker();
                                                ModifiersTree newModif = make.removeModifiersAnnotation(modifiers, at);
                                                workingCopy.rewrite(modifiers, newModif);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            result.commit(); */
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    /** attach a RAD component to the specified action. This will
     * trigger an update to any listeners. */
    public void addRADComponent(ProxyAction act, RADComponent comp) {
        if(comp == null) return;
        if(!boundComponents.containsKey(getKey(act))) {
            boundComponents.put(getKey(act),new ArrayList<RADComponent>());
        }
        if(boundComponents.get(getKey(act)).contains(comp)) {
            return;
        }
        boundComponents.get(getKey(act)).add(comp);
        fireActionChanged(act);
    }
    
    /** un-attach a RAD component from the specified action. This will trigger
     * an update to any listeners. */
    void removeRADComponent(ProxyAction act, RADComponent radComponent) {
        if(boundComponents.containsKey(getKey(act))) {
            boundComponents.get(getKey(act)).remove(radComponent);
        }
        fireActionChanged(act);
    }
    
    
    
    
    
    
    private static Project getProject(final FileObject fileInProject) {
        Project project = FileOwnerQuery.getOwner(fileInProject);
        return project;
    }
    
    private static void scanFolderForActions(FileObject folder,
            Map<String, List<ProxyAction>> classNameToActions) {
        for (FileObject fo : folder.getChildren()) {
            if (fo.isFolder()) { // dive into subfolders after scanning files
                scanFolderForActions(fo, classNameToActions);
            } else if (fo.getExt().equalsIgnoreCase("java")) { // NOI18N
                getActionsFromFile(fo, classNameToActions);
            }
        }
    }
    
    private static void getActionsFromFile(FileObject fo,
            Map<String, List<ProxyAction>> classNameToActions) {
        try {
            List<ProxyAction> result = (List<ProxyAction>) new ClassTask(fo) {
                Object run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
                    List<ProxyAction> list = null;
                    for (ExecutableElement el : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                        if (el.getModifiers().contains(Modifier.PUBLIC)) {
                            application.Action ann = el.getAnnotation(application.Action.class);
                            if (ann != null) {
                                ProxyAction action = new ProxyAction(classElement.getQualifiedName().toString(),
                                        el.getSimpleName().toString());
                                initActionFromSource(action, el, ann);
                                action.setResourceMap(ResourceUtils.getDesignResourceMap(sourceFile));
                                action.loadFromResourceMap();
                                if (list == null) {
                                    list = new ArrayList<ProxyAction>();
                                }
                                list.add(action);
                            }
                        }
                    }
                    return list;
                }
            }.execute();
            
            if (result != null && !result.isEmpty()) {
                String className = result.get(0).getClassname();
                classNameToActions.put(className, result);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    static void initActionFromSource(final ProxyAction action, FileObject sourceFile) {
        try {
            new ActionMethodTask(sourceFile, action.getId()) {
                Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement) {
                    application.Action ann = methodElement.getAnnotation(application.Action.class);
                    if (ann != null) {
                        initActionFromSource(action, methodElement, ann);
                    }
                    return null;
                }
            }.execute();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    static void initActionFromSource(ProxyAction action, ExecutableElement methodElement, application.Action annotation) {
        boolean returnsTask = isAsyncActionMethod(methodElement);
        action.setTaskEnabled(returnsTask);
        action.setEnabledName(annotation.enabledProperty());
        action.setSelectedName(annotation.selectedProperty());
        action.setBlockingType(ProxyAction.BlockingType.valueOf(annotation.block().toString()));
        // TBD 'name' attr
    }

    private static boolean isAsyncActionMethod(ExecutableElement methodElement) {
        TypeMirror retType = methodElement.getReturnType();
        return (retType.getKind() != TypeKind.VOID);
        // [TODO we need a precise way to determine that a Task or its subclass is returned]
        //        boolean returnsTask = false;
        //        if (retType.getKind() == TypeKind.DECLARED) {
        //            Element retEl = ((DeclaredType)retType).asElement();
        //            if (retEl.getKind() == ElementKind.CLASS
        //                    && "application.Task".equals(((TypeElement)retEl).getQualifiedName())) { // NOI18N
        //                returnsTask = true; // [does not cover if Task implementation is used as return type]
        //            }
        //        }
    }

    private void deleteAction(final ProxyAction action, final FormModel mod) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        // remove the entry from the form file
        List<RADComponent> comps = mod.getComponentList();
        for(RADComponent comp : comps) {
            RADProperty prop = comp.getBeanProperty("action");//NOI18N
            if(prop != null) {
                ProxyAction pact = (ProxyAction) prop.getValue();
                if(actionsMatch(pact,action)) {
                    prop.setValue(null);
                }
            }
        }
    }
    
    FileObject getFileForClass(String className) {
        ClassPath cp = ClassPath.getClassPath(getRoot(), ClassPath.SOURCE);
        return cp.findResource(className.replace('.', '/') + ".java"); // NOI18N
    }
    
    private FormModel getFormModel(final FileObject formfile) throws DataObjectNotFoundException {
        FormDataObject obj = (FormDataObject) FormDataObject.find(formfile);
        if(!obj.getFormEditor().isOpened()) {
            obj.getFormEditor().loadForm();
        }
        FormModel mod = obj.getFormEditor().getFormModel();
        return mod;
    }
    
    private boolean hasFormFile(FileObject file) {
        if(file.existsExt("form")) {//NOI18N
            return true;
        } else {
            return false;
        }
    }
    
    private FileObject getFormFile(FileObject javaFile) {
        return javaFile.getParent().getFileObject(javaFile.getName()+".form");
    }
    
    public boolean actionsMatch(ProxyAction pact, ProxyAction action) {
        if(pact == null || action == null) {
            return false;
        }
        if(pact.getId().equals(action.getId())) {
            if(pact.getClassname().equals(action.getClassname())) {
                return true;
            }
        }
        return false;
    }
    
    
    
    private String getKey(ProxyAction act) {
        if(act == null) { return "null"; }
        String s = new String(act.getId() + ":"+act.getClassname()).intern();//NOI18N
        return s;
    }
    
    
    private void fireStructureChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PropertyChangeEvent pce = new PropertyChangeEvent(ActionManager.this,"allActions",null,actions);//NOI18N
                for(PropertyChangeListener pcl : pcls) {
                    pcl.propertyChange(pce);
                }
            }
        });
    }
    
    // hack: make it change just the action
    private void fireActionChanged(final ProxyAction act) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for(ActionChangedListener acl : acls) {
                    acl.actionChanged(act);
                }
            }
        });
    }
    
    private void dumpContents() {
        System.out.println("actionList:");//log
        for(ProxyAction a : this.actionList) {
            System.out.println("action = " + a + " " + a.hashCode());//log
        }
    }
    
    // -----
    // helper classes for java source analysis tasks
    
    /**
     * Task for analysing structure of class of give source file.
     */
    abstract static class ClassTask implements CancellableTask<CompilationController> {
        FileObject sourceFile;
        
        private Object result;
        
        ClassTask(FileObject sourceFile) {
            this.sourceFile = sourceFile;
        }
        
        Object execute() throws IOException {
            JavaSource.forFileObject(sourceFile).runUserActionTask(this, true);
            return result;
        }
        
        abstract Object run(CompilationController controller, ClassTree classTree, TypeElement classElement);
        
        // CancellableTask
        public void cancel() {
        }
        
        // CancellableTask
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
                if (t.getKind() == Tree.Kind.CLASS) {
                    ClassTree classT = (ClassTree) t;
                    if (sourceFile.getName().equals(classT.getSimpleName().toString())) {
                        TreePath classTPath = controller.getTrees().getPath(controller.getCompilationUnit(), classT);
                        TypeElement classEl = (TypeElement) controller.getTrees().getElement(classTPath);
                        result = run(controller, classT, classEl);
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Task for analysing an action method of given source file and method name.
     */
    abstract static class ActionMethodTask extends ClassTask {
        String methodName;
        
        ActionMethodTask(FileObject sourceFile, String methodName) {
            super(sourceFile);
            this.methodName = methodName;
        }
        
        Object run(CompilationController controller, ClassTree classTree, TypeElement classElement) {
            for (ExecutableElement el : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                if (el.getSimpleName().toString().equals(methodName)
                        && el.getModifiers().contains(Modifier.PUBLIC)) {
                    MethodTree mTree = controller.getTrees().getTree(el);
                    return run(controller, mTree, el);
                }
            }
            return null;
        }
        
        abstract Object run(CompilationController controller, MethodTree methodTree, ExecutableElement methodElement);
    }
    
    
    private static Set<FormModel> registeredForms = new HashSet<FormModel>();
    
    public static void registerFormModel(final FormModel formModel, final FileObject sourceFile) {
        if(formModel == null) return;
        if(sourceFile == null) return;
        
        if(registeredForms.contains(formModel)) {
            return;
        }
        
        formModel.addFormModelListener(new FormModelListener() {
            public void formChanged(FormModelEvent[] events) {
                if(events != null) {
                    for(FormModelEvent e : events) {
                        if(e.getChangeType() == e.FORM_TO_BE_CLOSED) {
                            ActionManager am = ActionManager.getActionManager(sourceFile);
                            if(am != null) {
                                am.removeAllBoundComponents(e.getFormModel());
                            }
                            final FormModelListener ths = this;
                            final FormModel mod = e.getFormModel();
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    mod.removeFormModelListener(ths);
                                    registeredForms.remove(mod);
                                }
                            });
                        }
                    }
                }
            }
        });
        registeredForms.add(formModel);
    }
}
