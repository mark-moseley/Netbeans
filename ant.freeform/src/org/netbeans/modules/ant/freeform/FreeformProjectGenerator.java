/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class FreeformProjectGenerator {

    /**
     * Location of ant script as specified in project wizard. This property
     * should be set only when ant script is not in default location, that is not
     * in parent folder of nbproject directory.
     */
    public static final String PROP_ANT_SCRIPT = "ant.script";

    /** Location of original project. This property should be set/used when NB 
     * project metadata are stored in different folder. */
    public static final String PROP_PROJECT_LOCATION = "project.location";
    
    private FreeformProjectGenerator() {}

    /**
     * Creates new Freeform java project at the given folder with the given name,
     * target mappings, source folders, etc.
     * @param location original project folder; cannot be null
     * @param dir freeform project folder; cannot be null
     * @param name name of new project; cannot be null
     * @param antScript Ant script file; can be null what means default Ant script location
     * @param mappings list of TargetMapping instances
     * @param sources list of SourceFolder instances
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static AntProjectHelper createJavaProject(File location, File dir, String name, File antScript, List mappings, List sources, List compUnits) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        FileObject locationFO = FileUtil.toFileObject(location);
        AntProjectHelper h = createProject(locationFO, dirFO, name, antScript, mappings, sources, compUnits, null);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    /**
     * Creates new Freeform web project at the given folder with the given name,
     * target mappings, source folders, etc.
     * @param location original project folder; cannot be null
     * @param dir freeform project folder; cannot be null
     * @param name name of new project; cannot be null
     * @param antScript Ant script file; can be null what means default Ant script location
     * @param mappings list of TargetMapping instances
     * @param sources list of SourceFolder instances
     * @param compUnits list of JavaCompilationUnit instances
     * @param webModules list of WebModule instances
     */
    public static AntProjectHelper createWebProject(File location, File dir, String name, File antScript, List mappings, List sources, List compUnits, List webModules) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        FileObject locationFO = FileUtil.toFileObject(location);
        AntProjectHelper h = createProject(locationFO, dirFO, name, antScript, mappings, sources, compUnits, webModules);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    /**
     * Finds AuxiliaryConfiguration for the given project helper.
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(AntProjectHelper helper) {
        try {
            Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            AuxiliaryConfiguration aux = (AuxiliaryConfiguration)p.getLookup().lookup(AuxiliaryConfiguration.class);
            assert aux != null;
            return aux;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /**
     * Read target mappings from project.
     * @param helper AntProjectHelper instance
     * @return list of TargetMapping instances
     */
    public static List/*<TargetMapping>*/ getTargetMappings(AntProjectHelper helper) {
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
                // XXX: add context here
            }
            tm.targets = targetNames;
            list.add(tm);
        }
        return list;
    }
    
    /**
     * Update target mappings of the project. Project is left modified and 
     * you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances to store
     */
    public static void putTargetMappings(AntProjectHelper helper, List/*<TargetMapping>*/ mappings) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element actionsEl = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl != null) {
            data.removeChild(actionsEl);
        }
        
        Element actions = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-actions"); // NOI18N
        Iterator it = mappings.iterator();
        while (it.hasNext()) {
            TargetMapping tm = (TargetMapping)it.next();
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            action.setAttribute("name", tm.name);
            Iterator it2 = tm.targets.iterator();
            while (it2.hasNext()) {
                String targetName = (String)it2.next();
                Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                target.appendChild(doc.createTextNode(targetName)); // NOI18N
                action.appendChild(target);
            }
            if (tm.script != null) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode(tm.script)); // NOI18N
                action.appendChild(script);
            }
            actions.appendChild(action);
        }
        data.appendChild(actions);
        helper.putPrimaryConfigurationData(data, true);
    }

    /**
     * Update context menu actions. Project is left modified and 
     * you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param mappings list of <TargetMapping> instances for which the context
     *     menu actions will be created
     */
    public static void putContextMenuAction(AntProjectHelper helper, List/*<TargetMapping>*/ mappings) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            data.appendChild(viewEl);
        }
        Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            viewEl.appendChild(contextMenuEl);
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
            if (tm.targets.size() > 0) {
                Element ideAction = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-action"); //NOI18N
                ideAction.setAttribute("name", tm.name);
                contextMenuEl.appendChild(ideAction);
            }
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    /**
     * Read custom context menu actions from project.
     * @param helper AntProjectHelper instance
     * @return list of CustomTarget instances
     */
    public static List/*<CustomTarget>*/ getCustomContextMenuActions(AntProjectHelper helper) {
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
            }
            ct.targets = targetNames;
            list.add(ct);
        }
        return list;
    }
    
    /**
     * Update custom context menu actions of the project. Project is left modified and 
     * you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param list of <CustomTarget> instances to store
     */
    public static void putCustomContextMenuActions(AntProjectHelper helper, List/*<CustomTarget>*/ customTargets) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            data.appendChild(viewEl);
        }
        Element contextMenuEl = Util.findElement(viewEl, "context-menu", FreeformProjectType.NS_GENERAL); // NOI18N
        if (contextMenuEl == null) {
            contextMenuEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "context-menu"); // NOI18N
            viewEl.appendChild(contextMenuEl);
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
            Iterator it2 = ct.targets.iterator();
            while (it2.hasNext()) {
                String targetName = (String)it2.next();
                Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                target.appendChild(doc.createTextNode(targetName)); // NOI18N
                action.appendChild(target);
            }
            contextMenuEl.appendChild(action);
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
    }
    
    /**
     * Structure describing target mapping.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class TargetMapping {
        public String script;
        public List/*<String>*/ targets;
        public String name;
        //public String context;
    }

    private static AntProjectHelper createProject(final FileObject locationFO, final FileObject dirFO, final String name, final File antScript, final List mappings, final List sources, final List compUnits, final List webModules) throws IOException {
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

                    Element nm = doc.createElementNS(FreeformProjectType.NS_GENERAL, "name"); // NOI18N
                    nm.appendChild(doc.createTextNode(name)); // NOI18N
                    data.appendChild(nm);
                    Element props = doc.createElementNS(FreeformProjectType.NS_GENERAL, "properties"); // NOI18N
                    File locationF = FileUtil.toFile(locationFO);
                    File dirF = FileUtil.toFile(dirFO);
                    if (!locationFO.equals(dirFO)) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", PROP_PROJECT_LOCATION);
                        String path;
                        if (CollocationQuery.areCollocated(dirF, locationF)) {
                            path = PropertyUtils.relativizeFile(dirF, locationF); // NOI18N
                        } else {
                            path = locationF.getAbsolutePath();
                        }
                        property.appendChild(doc.createTextNode(path));
                        props.appendChild(property);
                    }
                    String antPath = "build.xml"; // NOI18N
                    if (antScript != null) {
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", PROP_ANT_SCRIPT);
                        antPath = relativizeLocation(locationF, dirF, antScript);
                        property.appendChild(doc.createTextNode(antPath));
                        antPath = "${"+PROP_ANT_SCRIPT+"}";
                        props.appendChild(property);
                    }
                    if (props.getChildNodes().getLength() > 0) {
                        data.appendChild(props);
                    }
                    h[0].putPrimaryConfigurationData(data, true);

                    putTargetMappings(h[0], mappings);
                    
                    List sourceFolders = new ArrayList(sources);
                    if (!locationFO.equals(dirFO)) {
                        SourceFolder gen = new SourceFolder();
                        gen.location = "${"+PROP_PROJECT_LOCATION+"}";
                        // XXX: uniquefy label
                        gen.label = locationFO.getName();
                        sourceFolders.add(gen);
                    }
                    if (sourceFolders.size() > 0) {
                        putSourceFolders(h[0], sourceFolders, null);
                    }
                    if (sources.size() > 0) {
                        putSourceViews(h[0], sources, null);
                    }
                    if (compUnits != null) {
                        putJavaCompilationUnits(h[0], aux, compUnits);
                    }
                    if (webModules != null) {
                        putWebModules (h[0], aux, webModules);
                    }
                    putBuildXMLSourceFile(h[0], antPath);
                    putContextMenuAction(h[0], mappings);
                }
            }
        );

        if (ioe[0] != null) {
            throw ioe[0];
        }
        return h[0];
    }

    private static FileObject createProjectDir (File dir) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF;
        dirFO.getFileSystem().refresh(false);
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir;
        assert dirFO.isFolder() : "Not really a dir: " + dir;
        //assert dirFO.getChildren().length == 0 : "Dir must have been empty: " + dir;
        return dirFO;
    }

    /**
     * Structure describing source folder.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class SourceFolder {
        public String label;
        public String type;
        public String location;
        public String style;
    }

    /**
     * Read source folders from the project.
     * @param helper AntProjectHelper instance
     * @param type type of source folders to be read. Can be null in which case
     *    all types will be read. Useful for reading one type of source folders.
     *    Source folders without type are read only when type == null.
     * @return list of SourceFolder instances; style value will be always null
     */
    public static List/*<SourceFolder>*/ getSourceFolders(AntProjectHelper helper, String type) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Element foldersEl = Util.findElement(data, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersEl == null) {
            return list;
        }
        List/*<Element>*/ sourceFolders = Util.findSubElements(foldersEl);
        Iterator it = sourceFolders.iterator();
        while (it.hasNext()) {
            Element sourceFolderEl = (Element)it.next();
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            Element el = Util.findElement(sourceFolderEl, "label", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.label = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "type", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.type = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "location", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.location = Util.findText(el);
            }
            if (type == null || type.equals(sf.type)) {
                list.add(sf);
            }
        }
        return list;
    }

    /**
     * Update source folders of the project. Project is left modified and you 
     * must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param type type of source folders to update. 
     *    Can be null in which case all types will be overriden.
     *    Useful for overriding just one type of source folders. Source folders
     *    without type are overriden only when type == null.
     * @param sources list of SourceFolder instances
     */
    public static void putSourceFolders(AntProjectHelper helper, List/*<SourceFolder>*/ sources, String type) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "folders"); // NOI18N
            data.appendChild(foldersEl);
        } else {
            List/*<Element>*/ sourceFolders = Util.findSubElements(foldersEl);
            Iterator it = sourceFolders.iterator();
            while (it.hasNext()) {
                Element sourceFolderEl = (Element)it.next();
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                if (type == null) {
                    foldersEl.removeChild(sourceFolderEl);
                } else {
                    Element typeEl = Util.findElement(sourceFolderEl, "type", FreeformProjectType.NS_GENERAL);
                    if (typeEl != null) {
                        String typeElValue = Util.findText(typeEl);
                        if (type.equals(typeElValue)) {
                            foldersEl.removeChild(sourceFolderEl);
                        }
                    }
                }
            }
        }
        Iterator it2 = sources.iterator();
        while (it2.hasNext()) {
            SourceFolder sf = (SourceFolder)it2.next();
            Element sourceFolderEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-folder"); // NOI18N
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.type != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "type"); // NOI18N
                el.appendChild(doc.createTextNode(sf.type)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            foldersEl.appendChild(sourceFolderEl);
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    /**
     * Read source views from the project. At the moment only source-folder
     * elements are read and source-file ones are ignored.
     * @param helper AntProjectHelper instance
     * @param style style of source folders to be read. Can be null in which case
     *    all styles will be read. Useful for reading one style of source folders.
     * @return list of SourceFolder instances; type value will be always null
     */
    public static List getSourceViews(AntProjectHelper helper, String style) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            return list;
        }
        Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl == null) {
            return list;
        }
        Iterator/*<Element>*/ it = Util.findSubElements(itemsEl).iterator();
        while (it.hasNext()) {
            Element sourceFolderEl = (Element)it.next();
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            sf.style = sourceFolderEl.getAttribute("style");
            Element el = Util.findElement(sourceFolderEl, "label", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.label = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "location", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.location = Util.findText(el);
            }
            if (style == null || style.equals(sf.style)) {
                list.add(sf);
            }
        }
        return list;
    }
    
    /**
     * Update source views of the project. 
     * This method should be called always after the putSourceFolders method
     * to keep views and folders in sync.
     * Project is left modified and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param sources list of SourceFolder instances
     * @param style style of source views to update. 
     *    Can be null in which case all styles will be overriden.
     *    Useful for overriding just one style of source view.
     */
    public static void putSourceViews(AntProjectHelper helper, List/*<SourceFolder>*/ sources, String style) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            data.appendChild(viewEl);
        }
        Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "items"); // NOI18N
            viewEl.appendChild(itemsEl);
        }
        List/*<Element>*/ sourceViews = Util.findSubElements(itemsEl);
        Iterator it = sourceViews.iterator();
        while (it.hasNext()) {
            Element sourceViewEl = (Element)it.next();
            if (!sourceViewEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            String sourceStyle = sourceViewEl.getAttribute("style");
            if (style == null || style.equals(sourceStyle)) {
                itemsEl.removeChild(sourceViewEl);
            }
        }
        Iterator it2 = sources.iterator();
        while (it2.hasNext()) {
            SourceFolder sf = (SourceFolder)it2.next();
            Element sourceFolderEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", sf.style);
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            itemsEl.appendChild(sourceFolderEl);
        }
        helper.putPrimaryConfigurationData(data, true);
    }
    
    private static void putBuildXMLSourceFile(AntProjectHelper helper, String antPath) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            data.appendChild(viewEl);
        }
        Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "items"); // NOI18N
            viewEl.appendChild(itemsEl);
        }
        Element fileEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-file"); // NOI18N
        Element el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
        el.appendChild(doc.createTextNode(antPath)); // NOI18N
        fileEl.appendChild(el);
        itemsEl.appendChild(fileEl);
        helper.putPrimaryConfigurationData(data, true);
    }

    /**
     * Read Java compilation units from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of JavaCompilationUnit instances; never null;
     */
    public static List/*<JavaCompilationUnit>*/ getJavaCompilationUnits(
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("java-data", FreeformProjectType.NS_JAVA, true);
        if (data == null) {
            return list;
        }
        List/*<Element>*/ cus = Util.findSubElements(data);
        Iterator it = cus.iterator();
        while (it.hasNext()) {
            Element cuEl = (Element)it.next();
            JavaCompilationUnit cu = new JavaCompilationUnit();
            List outputs = new ArrayList();
            List cps = new ArrayList();
            Iterator it2 = Util.findSubElements(cuEl).iterator();
            while (it2.hasNext()) {
                Element el = (Element)it2.next();
                if (el.getLocalName().equals("package-root")) { // NOI18N
                    cu.packageRoot = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("classpath")) { // NOI18N
                    JavaCompilationUnit.CP cp = new JavaCompilationUnit.CP();
                    cp.classpath = Util.findText(el);
                    cp.mode = el.getAttribute("mode");
                    if (cp.mode != null && cp.classpath != null) {
                        cps.add(cp);
                    }
                    continue;
                }
                if (el.getLocalName().equals("built-to")) { // NOI18N
                    outputs.add(Util.findText(el));
                    continue;
                }
                if (el.getLocalName().equals("source-level")) { // NOI18N
                    cu.sourceLevel = Util.findText(el);
                }
            }
            cu.output = outputs.size() > 0 ? outputs : null;
            cu.classpath = cps.size() > 0 ? cps: null;
            list.add(cu);
        }
        return list;
    }

    /**
     * Update Java compilation units of the project. Project is left modified
     * and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static void putJavaCompilationUnits(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List/*<JavaCompilationUnit>*/ compUnits) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("java-data", FreeformProjectType.NS_JAVA, true);
        if (data == null) {
            data = helper.getPrimaryConfigurationData(true).getOwnerDocument().
                createElementNS(FreeformProjectType.NS_JAVA, "java-data");
        }
        Document doc = data.getOwnerDocument();
        List cus = Util.findSubElements(data); // NOI18N
        Iterator it = cus.iterator();
        while (it.hasNext()) {
            Element cuEl = (Element)it.next();
            data.removeChild(cuEl);
        }
        Iterator it2 = compUnits.iterator();
        while (it2.hasNext()) {
            Element cuEl = doc.createElementNS(FreeformProjectType.NS_JAVA, "compilation-unit"); // NOI18N
            data.appendChild(cuEl);
            JavaCompilationUnit cu = (JavaCompilationUnit)it2.next();
            Element el;
            if (cu.packageRoot != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "package-root"); // NOI18N
                el.appendChild(doc.createTextNode(cu.packageRoot));
                cuEl.appendChild(el);
            }
            if (cu.classpath != null) {
                Iterator it3 = cu.classpath.iterator();
                while (it3.hasNext()) {
                    JavaCompilationUnit.CP cp = (JavaCompilationUnit.CP)it3.next();
                    el = doc.createElementNS(FreeformProjectType.NS_JAVA, "classpath"); // NOI18N
                    el.appendChild(doc.createTextNode(cp.classpath));
                    el.setAttribute("mode", cp.mode);
                    cuEl.appendChild(el);
                }
            }
            if (cu.output != null) {
                Iterator it3 = cu.output.iterator();
                while (it3.hasNext()) {
                    String output = (String)it3.next();
                    el = doc.createElementNS(FreeformProjectType.NS_JAVA, "built-to"); // NOI18N
                    el.appendChild(doc.createTextNode(output));
                    cuEl.appendChild(el);
                }
            }
            if (cu.sourceLevel != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "source-level"); // NOI18N
                el.appendChild(doc.createTextNode(cu.sourceLevel));
                cuEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }

    /**
     * Read web modules from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of WebModule instances
     */
    public static List/*<WebModule>*/ getWebmodules (
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("web-data", FreeformProjectType.NS_WEB, true);
        List/*<Element>*/ wms = Util.findSubElements(data);
        Iterator it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = (Element)it.next();
            WebModule wm = new WebModule();
            Iterator it2 = Util.findSubElements(wmEl).iterator();
            while (it2.hasNext()) {
                Element el = (Element)it2.next();
                if (el.getLocalName().equals("doc-root")) { // NOI18N
                    wm.docRoot = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("classpath")) { // NOI18N
                    wm.classpath = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("context-path")) { // NOI18N
                    wm.contextPath = Util.findText(el);
                    continue;
                }
                if (el.getLocalName().equals("j2ee-spec-level")) { // NOI18N
                    wm.j2eeSpecLevel = Util.findText(el);
                }
            }
            list.add(wm);
        }
        return list;
    }

    /**
     * Update web modules of the project. Project is left modified
     * and you must save it explicitely.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @param webModules list of WebModule instances
     */
    public static void putWebModules(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List/*<WebModule>*/ webModules) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("web-data", FreeformProjectType.NS_WEB, true);
        if (data == null) {
            data = helper.getPrimaryConfigurationData(true).getOwnerDocument().
                createElementNS(FreeformProjectType.NS_WEB, "web-data"); // NOI18N
        }
        Document doc = data.getOwnerDocument();
        List wms = Util.findSubElements(data);
        Iterator it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = (Element)it.next();
            data.removeChild(wmEl);
        }
        Iterator it2 = webModules.iterator();
        while (it2.hasNext()) {
            Element wmEl = doc.createElementNS(FreeformProjectType.NS_WEB, "web-module"); // NOI18N
            data.appendChild(wmEl);
            WebModule wm = (WebModule)it2.next();
            Element el;
            if (wm.docRoot != null) {
                el = doc.createElementNS(FreeformProjectType.NS_WEB, "doc-root"); // NOI18N
                el.appendChild(doc.createTextNode(wm.docRoot));
                wmEl.appendChild(el);
            }
            if (wm.classpath != null) {
                el = doc.createElementNS(FreeformProjectType.NS_WEB, "classpath"); // NOI18N
                el.appendChild(doc.createTextNode(wm.classpath));
                wmEl.appendChild(el);
            }
            if (wm.contextPath != null) {
                el = doc.createElementNS(FreeformProjectType.NS_WEB, "context-path"); // NOI18N
                el.appendChild(doc.createTextNode(wm.contextPath));
                wmEl.appendChild(el);
            }
            if (wm.j2eeSpecLevel != null) {
                el = doc.createElementNS(FreeformProjectType.NS_WEB, "j2ee-spec-level"); // NOI18N
                el.appendChild(doc.createTextNode(wm.j2eeSpecLevel));
                wmEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }
    
    //XXX: The <property-file> elements are ignored at the moment.
    /**
     * Read all <property> elements and return them as Properties instance.
     */
    public static Properties getProperties(AntProjectHelper helper) {
        Properties props = new Properties();
        Element data = helper.getPrimaryConfigurationData(true);
        Element propertiesEl = Util.findElement(data, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (propertiesEl == null) {
            return props;
        }
        List/*<Element>*/ subElms = Util.findSubElements(propertiesEl);
        Iterator it = subElms.iterator();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            if (!el.getLocalName().equals("property")) { // NOI18N
                continue;
            }
            String key = el.getAttribute("name");
            String value = Util.findText(el);
            props.put(key, value);
        }
        return props;
    }

    /**
     * Structure describing compilation unit.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class JavaCompilationUnit {
        public String packageRoot;
        public List/*<CP>*/ classpath;
        public List/*<String>*/ output;
        public String sourceLevel;
        
        public static final class CP {
            public String classpath;
            public String mode;
        }
        
    }
    
    /**
     * Structure describing web module.
     * Data in the struct are in the same format as they are stored in XML.
     */
    public static final class WebModule {
        public String docRoot;
        public String classpath;
        public String contextPath;
        public String j2eeSpecLevel;
    }

    /**
     * Returns Ant script of the freeform project
     * represented by the given AntProjectHelper.
     * @param helper AntProjectHelper of freeform project
     * @param ev evaluator of the freeform project
     * @return Ant script FileObject or null if it cannot be found
     */
    public static FileObject getAntScript(AntProjectHelper helper, PropertyEvaluator ev) {
        String antScript = ev.getProperty(PROP_ANT_SCRIPT);
        if (antScript != null) {
            File f= helper.resolveFile(antScript);
            if (!f.exists()) {
                return null;
            }
            FileObject fo = FileUtil.toFileObject(f);
            return fo;
        } else {
            FileObject fo = helper.getProjectDirectory().getFileObject("build.xml");
            return fo;
        }
    }

    /**
     * Returns location of orignal project base folder. The location can be dirrerent
     * from NetBeans metadata project folder.
     * @param proj freeform project for which the original location will be returned
     * @return location of original project base folder
     */
    public static File getProjectLocation(FreeformProject proj) {
        String loc = proj.evaluator().getProperty(PROP_PROJECT_LOCATION);
        if (loc != null) {
            return proj.helper().resolveFile(loc);
        } else {
            return FileUtil.toFile(proj.getProjectDirectory());
        }
    }

    
    /** 
     * Relativize given file against the original project and if needed use 
     * ${project.location} property as base. If file cannot be relativized
     * the absolute filepath is returned.
     * @param projectBase original project base folder
     * @param freeformBase Freeform project base folder
     * @param location location to relativize
     * @return text suitable for storage in project.xml representing given location
     */
    public static String relativizeLocation(File projectBase, File freeformBase, File location) {
        if (CollocationQuery.areCollocated(projectBase, location)) {
            if (projectBase.equals(freeformBase)) {
                return PropertyUtils.relativizeFile(projectBase, location);
            } else {
                return "${"+PROP_PROJECT_LOCATION+"}/"+PropertyUtils.relativizeFile(projectBase, location); // NOI18N
            }
        } else {
            return location.getAbsolutePath();
        }
    }

    /**
     * Resolve given string value (e.g. "${project.location}/lib/lib1.jar")
     * to a File.
     * @param evaluator evaluator to use for properties resolving
     * @param freeformProjectBase freeform project base folder
     * @val string to be resolved as file
     * @return resolved File or null if file could not be resolved
     */
    public static String resolveFile(PropertyEvaluator evaluator, File freeformProjectBase, String val) {
        String location = evaluator.evaluate(val).replace('/', File.separatorChar).replace('\\', File.separatorChar);
        File f = PropertyUtils.resolveFile(freeformProjectBase, location);
        if (f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }
    
}
