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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads/writes project.xml.
 *
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class FreeformProjectGenerator {

    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N
    
    // this order is not required by schema, but follow it to minimize randomness a bit
    private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    private static final String[] contextMenuElementsOrder = new String[]{"ide-action", "separator", "action"}; // NOI18N
    
    private FreeformProjectGenerator() {}

    public static AntProjectHelper createProject(File location, File dir, String name, File antScript) throws IOException {
        FileObject dirFO = createProjectDir(dir);
        FileObject locationFO = FileUtil.toFileObject(location);
        AntProjectHelper h = createProject(locationFO, dirFO, name, antScript);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    private static AntProjectHelper createProject(final FileObject locationFO, final FileObject dirFO, final String name, final File antScript) throws IOException {
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    Project p;
                    try {
                        h[0] = ProjectGenerator.createProject(dirFO, FreeformProjectType.TYPE);
                        p = ProjectManager.getDefault().findProject(dirFO);
                    } catch (IOException e) {
                        ioe[0] = e;
                        return;
                    }
                    AuxiliaryConfiguration aux = (AuxiliaryConfiguration)p.getLookup().lookup(AuxiliaryConfiguration.class);
                    assert aux != null;

                    Element data = h[0].getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();

                    Node comment = doc.createComment(" " + NbBundle.getMessage(FreeformProjectGenerator.class, "LBL_Manual_Editing_Warning") + " ");
                    data.appendChild(comment);
                    
                    Element nm = doc.createElementNS(FreeformProjectType.NS_GENERAL, "name"); // NOI18N
                    nm.appendChild(doc.createTextNode(name)); // NOI18N
                    data.appendChild(nm);
                    Element props = doc.createElementNS(FreeformProjectType.NS_GENERAL, "properties"); // NOI18N
                    File locationF = FileUtil.toFile(locationFO);
                    File dirF = FileUtil.toFile(dirFO);
                    Map properties = new HashMap();
                    if (!locationFO.equals(dirFO)) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", ProjectConstants.PROP_PROJECT_LOCATION); // NOI18N
                        String path;
                        if (CollocationQuery.areCollocated(dirF, locationF)) {
                            path = PropertyUtils.relativizeFile(dirF, locationF); // NOI18N
                        } else {
                            path = locationF.getAbsolutePath();
                        }
                        property.appendChild(doc.createTextNode(path));
                        props.appendChild(property);
                        properties.put(ProjectConstants.PROP_PROJECT_LOCATION, path);
                    }
                    String antPath = "build.xml"; // NOI18N
                    if (antScript != null) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", ProjectConstants.PROP_ANT_SCRIPT); // NOI18N
                        antPath = Util.relativizeLocation(locationF, dirF, antScript);
                        property.appendChild(doc.createTextNode(antPath));
                        properties.put(ProjectConstants.PROP_ANT_SCRIPT, antPath);
                        antPath = "${"+ProjectConstants.PROP_ANT_SCRIPT+"}"; // NOI18N
                        props.appendChild(property);
                    }
                    PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
                        PropertyUtils.fixedPropertyProvider(properties)});
                    //#56344:Always write a <properties> element to project.xml of a generated freeform
//                  if (props.getChildNodes().getLength() > 0) {
                    data.appendChild(props);
//                  }
                    h[0].putPrimaryConfigurationData(data, true);
                    putBuildXMLSourceFile(h[0], antPath);
                }
            }
        );

        if (ioe[0] != null) {
            throw ioe[0];
        }
        return h[0];
    }

    private static FileObject createProjectDir(File dir) throws IOException {
        FileObject dirFO;
        if(!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem (dir);
            dir.mkdirs();
            refreshFileSystem (dir);
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;                        
    }


    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

    /**
     * Read target mappings from project.
     * @param helper AntProjectHelper instance
     * @return list of TargetMapping instances
     */
    public static List/*<TargetMapping>*/ getTargetMappings(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl == null) {
            return list;
        }
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            TargetMapping tm = new TargetMapping();
            tm.name = actionEl.getAttribute("name"); // NOI18N
            List/*<Element>*/ subElems = Util.findSubElements(actionEl);
            List/*<String>*/ targetNames = new ArrayList(subElems.size());
            EditableProperties props = new EditableProperties(false);
            Iterator it2 = subElems.iterator();
            while (it2.hasNext()) {
                Element subEl = (Element)it2.next();
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(Util.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    tm.script = Util.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("context")) { // NOI18N
                    TargetMapping.Context ctx = new TargetMapping.Context();
                    Iterator it3 = Util.findSubElements(subEl).iterator();
                    while (it3.hasNext()) {
                        Element contextSubEl = (Element)it3.next();
                        if (contextSubEl.getLocalName().equals("property")) { // NOI18N
                            ctx.property = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("format")) { // NOI18N
                            ctx.format = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("folder")) { // NOI18N
                            ctx.folder = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("pattern")) { // NOI18N
                            ctx.pattern = Util.findText(contextSubEl);
                            continue;
                        }
                        if (contextSubEl.getLocalName().equals("arity")) { // NOI18N
                            Element sepFilesEl = Util.findElement(contextSubEl, "separated-files", FreeformProjectType.NS_GENERAL); // NOI18N
                            if (sepFilesEl != null) {
                                ctx.separator = Util.findText(sepFilesEl);
                            }
                            continue;
                        }
                    }
                    tm.context = ctx;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            tm.targets = targetNames;
            if (props.keySet().size() > 0) {
                tm.properties = props;
            }
            list.add(tm);
        }
        return list;
    }
    
    private static void readProperty(Element propertyElement, EditableProperties props) {
        String key = propertyElement.getAttribute("name"); // NOI18N
        String value = Util.findText(propertyElement);
        props.setProperty(key, value);
    }

    /**
     * Update target mappings of the project. Project is left modified and 
     * you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances to store
     */
    public static void putTargetMappings(AntProjectHelper helper, List/*<TargetMapping>*/ mappings) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element actions = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actions != null) {
            data.removeChild(actions);
        }
        
        actions = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-actions"); // NOI18N
        Iterator it = mappings.iterator();
        while (it.hasNext()) {
            TargetMapping tm = (TargetMapping)it.next();
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            action.setAttribute("name", tm.name); // NOI18N
            if (tm.script != null) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode(tm.script));
                action.appendChild(script);
            }
            if (tm.targets != null) {
                Iterator it2 = tm.targets.iterator();
                while (it2.hasNext()) {
                    String targetName = (String)it2.next();
                    Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                    target.appendChild(doc.createTextNode(targetName));
                    action.appendChild(target);
                }
            }
            if (tm.properties != null) {
                writeProperties(tm.properties, doc, action);
            }
            if (tm.context != null) {
                Element context = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context"); //NOI18N
                TargetMapping.Context ctx = tm.context;
                assert ctx.property != null;
                Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); //NOI18N
                property.appendChild(doc.createTextNode(ctx.property));
                context.appendChild(property);
                assert ctx.folder != null;
                Element folder = doc.createElementNS(FreeformProjectType.NS_GENERAL, "folder"); //NOI18N
                folder.appendChild(doc.createTextNode(ctx.folder));
                context.appendChild(folder);
                if (ctx.pattern != null) {
                    Element pattern = doc.createElementNS(FreeformProjectType.NS_GENERAL, "pattern"); //NOI18N
                    pattern.appendChild(doc.createTextNode(ctx.pattern));
                    context.appendChild(pattern);
                }
                assert ctx.format != null;
                Element format = doc.createElementNS(FreeformProjectType.NS_GENERAL, "format"); //NOI18N
                format.appendChild(doc.createTextNode(ctx.format));
                context.appendChild(format);
                Element arity = doc.createElementNS(FreeformProjectType.NS_GENERAL, "arity"); // NOI18N
                if (ctx.separator != null) {
                    Element sepFilesEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "separated-files"); // NOI18N
                    sepFilesEl.appendChild(doc.createTextNode(ctx.separator));
                    arity.appendChild(sepFilesEl);
                } else {
                    arity.appendChild(doc.createElementNS(FreeformProjectType.NS_GENERAL, "one-file-only")); // NOI18N
                }
                context.appendChild(arity);
                action.appendChild(context);
            }
            actions.appendChild(action);
        }
        Util.appendChildElement(data, actions, rootElementsOrder);
        helper.putPrimaryConfigurationData(data, true);
    }
    
    private static void writeProperties(EditableProperties props, Document doc, Element element) {
        Iterator it2 = props.keySet().iterator();
        while (it2.hasNext()) {
            String key = (String)it2.next();
            String value = props.getProperty(key);
            Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); //NOI18N
            property.setAttribute("name", key); // NOI18N
            property.appendChild(doc.createTextNode(value));
            element.appendChild(property);
        }
    }
    
    /**
     * Update context menu actions. Project is left modified and 
     * you must save it explicitely. This method stores all IDE actions
     * before the custom actions what means that user's customization by hand
     * (e.g. order of items) is lost.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances for which the context
     *     menu actions will be created
     */
    public static void putContextMenuAction(AntProjectHelper helper, List/*<TargetMapping>*/ mappings) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            Util.appendChildElement(viewEl, contextMenuEl, viewElementsOrder);
        }
        List/*<Element>*/ contextMenuElements = Util.findSubElements(contextMenuEl);
        Iterator it = contextMenuElements.iterator();
        while (it.hasNext()) {
            Element ideActionEl = (Element)it.next();
            if (!ideActionEl.getLocalName().equals("ide-action")) { // NOI18N
                continue;
            }
            contextMenuEl.removeChild(ideActionEl);
        }
        it = mappings.iterator();
        while (it.hasNext()) {
            TargetMapping tm = (TargetMapping)it.next();
            if (tm.context != null) {
                // ignore context sensitive actions
                continue;
            }
            Element ideAction = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-action"); //NOI18N
            ideAction.setAttribute("name", tm.name); // NOI18N
            Util.appendChildElement(contextMenuEl, ideAction, contextMenuElementsOrder);
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    /**
     * Read custom context menu actions from project.
     * @param helper AntProjectHelper instance
     * @return list of CustomTarget instances
     */
    public static List/*<CustomTarget>*/ getCustomContextMenuActions(AntProjectHelper helper) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        ArrayList list = new ArrayList();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element viewEl = Util.findElement(genldata, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            return list;
        }
        Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            return list;
        }
        List/*<Element>*/ actions = Util.findSubElements(contextMenuEl);
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (!actionEl.getLocalName().equals("action")) { // NOI18N
                continue;
            }
            CustomTarget ct = new CustomTarget();
            List/*<Element>*/ subElems = Util.findSubElements(actionEl);
            List/*<String>*/ targetNames = new ArrayList(subElems.size());
            EditableProperties props = new EditableProperties(false);
            Iterator it2 = subElems.iterator();
            while (it2.hasNext()) {
                Element subEl = (Element)it2.next();
                if (subEl.getLocalName().equals("target")) { // NOI18N
                    targetNames.add(Util.findText(subEl));
                    continue;
                }
                if (subEl.getLocalName().equals("script")) { // NOI18N
                    ct.script = Util.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("label")) { // NOI18N
                    ct.label = Util.findText(subEl);
                    continue;
                }
                if (subEl.getLocalName().equals("property")) { // NOI18N
                    readProperty(subEl, props);
                    continue;
                }
            }
            ct.targets = targetNames;
            if (props.keySet().size() > 0) {
                ct.properties = props;
            }
            list.add(ct);
        }
        return list;
    }
    
    /**
     * Update custom context menu actions of the project. Project is left modified and 
     * you must save it explicitely. This method stores all custom actions 
     * after the IDE actions what means that user's customization by hand 
     * (e.g. order of items) is lost.
     * @param helper AntProjectHelper instance
     * @param list of <CustomTarget> instances to store
     */
    public static void putCustomContextMenuActions(AntProjectHelper helper, List/*<CustomTarget>*/ customTargets) {
        //assert ProjectManager.mutex().isWriteAccess();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            Util.appendChildElement(viewEl, contextMenuEl, viewElementsOrder);
        }
        List/*<Element>*/ contextMenuElements = Util.findSubElements(contextMenuEl);
        Iterator it = contextMenuElements.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            if (!actionEl.getLocalName().equals("action")) { // NOI18N
                continue;
            }
            contextMenuEl.removeChild(actionEl);
        }
        it = customTargets.iterator();
        while (it.hasNext()) {
            CustomTarget ct = (CustomTarget)it.next();
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            if (ct.script != null) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode(ct.script)); // NOI18N
                action.appendChild(script);
            }
            Element label = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); //NOI18N
            label.appendChild(doc.createTextNode(ct.label)); // NOI18N
            action.appendChild(label);
            if (ct.targets != null) {
                Iterator it2 = ct.targets.iterator();
                while (it2.hasNext()) {
                    String targetName = (String)it2.next();
                    Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                    target.appendChild(doc.createTextNode(targetName)); // NOI18N
                    action.appendChild(target);
                }
            }
            if (ct.properties != null) {
                writeProperties(ct.properties, doc, action);
            }
            Util.appendChildElement(contextMenuEl, action, contextMenuElementsOrder);
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    /**
     * Structure describing custom target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class CustomTarget {
        public List/*<String>*/ targets;
        public String label;
        public String script;
        public EditableProperties properties;
    }
    
    /**
     * Structure describing target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class TargetMapping {
        public String script;
        public List/*<String>*/ targets;
        public String name;
        public EditableProperties properties;
        public Context context; // may be null
        
        public static final class Context {
            public String property;
            public String format;
            public String folder;
            public String pattern; // may be null
            public String separator; // may be null
        }
    }

    private static void putBuildXMLSourceFile(AntProjectHelper helper, String antPath) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "items"); // NOI18N
            Util.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        }
        Element fileEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-file"); // NOI18N
        Element el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
        el.appendChild(doc.createTextNode(antPath)); // NOI18N
        fileEl.appendChild(el);
        Util.appendChildElement(itemsEl, fileEl, viewItemElementsOrder);
        helper.putPrimaryConfigurationData(data, true);
    }

    /**
     * Returns Ant script of the freeform project
     * represented by the given AntProjectHelper.
     * @param helper AntProjectHelper of freeform project
     * @param ev evaluator of the freeform project
     * @return Ant script FileObject or null if it cannot be found
     */
    public static FileObject getAntScript(AntProjectHelper helper, PropertyEvaluator ev) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        String antScript = ev.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
        if (antScript != null) {
            File f= helper.resolveFile(antScript);
            if (!f.exists()) {
                return null;
            }
            FileObject fo = FileUtil.toFileObject(f);
            return fo;
        } else {
            FileObject fo = helper.getProjectDirectory().getFileObject("build.xml"); // NOI18N
            return fo;
        }
    }

}
