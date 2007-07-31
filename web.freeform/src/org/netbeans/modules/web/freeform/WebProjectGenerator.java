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

package org.netbeans.modules.web.freeform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Reads/writes project.xml.
 * Handling of /1 vs. /2 namespace: either namespace can be read;
 * when writing, attempts to keep existing namespace when possible, but
 * will always write a /2 namespace when it is necessary (for <tt>web-inf</tt> elemennt).
 *
 * @author  Jesse Glick, David Konecny, Pavel Buzek
 */
public class WebProjectGenerator {

//    /** Keep root elements in the order specified by project's XML schema. */
    private static final String[] rootElementsOrder = new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    private static final String[] viewElementsOrder = new String[]{"items", "context-menu"}; // NOI18N

//    // this order is not required by schema, but follow it to minimize randomness a bit
    private static final String[] folderElementsOrder = new String[]{"source-folder", "build-folder"}; // NOI18N
    private static final String[] viewItemElementsOrder = new String[]{"source-folder", "source-file"}; // NOI18N
    
    private WebProjectGenerator() {}

    /**
     * @param sources list of pairs[relative path, display name]
     */
    public static void putWebSourceFolder(AntProjectHelper helper, List<String> sources) {
        putFolder(WebProjectConstants.TYPE_DOC_ROOT, helper, sources);
    }

    /**
     * @param sources list of pairs[relative path, display name]
     */
    // TMYSIK handle this (version 1/2)
    public static void putWebInfFolder(AntProjectHelper helper, List<String> sources) {
        putFolder(WebProjectConstants.TYPE_WEB_INF, helper, sources);
    }
    
    // allowed folder types: WebProjectConstants.TYPE_DOC_ROOT, WebProjectConstants.TYPE_WEB_INF
    private static void putFolder(String folderType, AntProjectHelper helper, List<String> sources) {
        String label = null;
        if (WebProjectConstants.TYPE_DOC_ROOT.equals(folderType)) {
            label = NbBundle.getMessage(WebProjectGenerator.class, "LBL_WebPages");
        } else if (WebProjectConstants.TYPE_WEB_INF.equals(folderType)) {
            label = NbBundle.getMessage(WebProjectGenerator.class, "LBL_WebInf");
        } else {
            assert false : "Unknown folder type: " + folderType;
        }

        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", Util.NAMESPACE); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(Util.NAMESPACE, "folders"); // NOI18N
            Util.appendChildElement(data, foldersEl, rootElementsOrder);
        } else {
            List l = Util.findSubElements(foldersEl);
            for (int i = 0; i < l.size(); i++) {
                Element e = (Element) l.get(i);
                Element te = Util.findElement(e, "type", Util.NAMESPACE);
                if (te != null && Util.findText(te).equals(folderType)) {
                    foldersEl.removeChild(e);
                    break;
                }
            }
        }
        
        Element viewEl = Util.findElement(data, "view", Util.NAMESPACE); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(Util.NAMESPACE, "view"); // NOI18N
            Util.appendChildElement(data, viewEl, rootElementsOrder);
        }
        Element itemsEl = Util.findElement(viewEl, "items", Util.NAMESPACE); // NOI18N
        if (itemsEl == null) {
            itemsEl = doc.createElementNS(Util.NAMESPACE, "items"); // NOI18N
            Util.appendChildElement(viewEl, itemsEl, viewElementsOrder);
        } else {
            List l = Util.findSubElements(itemsEl);
            for (int i = 0; i < l.size(); i++) {
                Element e = (Element) l.get(i);
                if (e.hasAttribute("style")) {
                    if (e.getAttribute("style").equals("tree")) {
                        // #110173
                        Element labelElement = Util.findElement(e, "label", Util.NAMESPACE);
                        if (labelElement != null && label.equals(Util.findText(labelElement))) {
                            itemsEl.removeChild(e);
                            break;
                        }
                    }
                }
            }
        }
        
        Iterator<String> it1 = sources.iterator();
        while (it1.hasNext()) {
            String path = it1.next();
            assert it1.hasNext();
            String dispname = it1.next();
            Element sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            Element el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
            el.appendChild(doc.createTextNode(dispname));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "type"); // NOI18N
            el.appendChild(doc.createTextNode(folderType));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(path));
            sourceFolderEl.appendChild(el);
            Util.appendChildElement(foldersEl, sourceFolderEl, folderElementsOrder);
            
            sourceFolderEl = doc.createElementNS(Util.NAMESPACE, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", "tree"); // NOI18N
            el = doc.createElementNS(Util.NAMESPACE, "label"); // NOI18N
            el.appendChild(doc.createTextNode(label));
            sourceFolderEl.appendChild(el);
            el = doc.createElementNS(Util.NAMESPACE, "location"); // NOI18N
            el.appendChild(doc.createTextNode(path)); // NOI18N
            sourceFolderEl.appendChild(el);
            Node firstNode = itemsEl.getFirstChild();
            if (firstNode != null) {
                if (WebProjectConstants.TYPE_DOC_ROOT.equals(folderType)) {
                    insertWebElement(itemsEl, firstNode, sourceFolderEl);
                } else if (WebProjectConstants.TYPE_WEB_INF.equals(folderType)) {
                    insertWebInfElement(itemsEl, firstNode, sourceFolderEl);
                }
            } else {
                Util.appendChildElement(itemsEl, sourceFolderEl, viewItemElementsOrder);
            }
        }
        Util.putPrimaryConfigurationData(helper, data);
    }
    
    private static void insertWebElement(Element itemsEl, Node firstNode, Element sourceFolderEl) {
        itemsEl.insertBefore(sourceFolderEl, firstNode);
    }
    
    private static void insertWebInfElement(Element itemsEl, Node firstNode, Element sourceFolderEl) {
        Node secondNode = firstNode.getNextSibling();
        if (secondNode != null) {
            itemsEl.insertBefore(sourceFolderEl, secondNode);
        } else {
            Util.appendChildElement(itemsEl, sourceFolderEl, viewItemElementsOrder);
        }
    }
    
    /**
     * Read web modules from the project.
     * @param helper AntProjectHelper instance
     * @param aux AuxiliaryConfiguration instance
     * @return list of WebModule instances
     */
    public static List<WebModule> getWebmodules (
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        //assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        List<WebModule> list = new ArrayList<WebModule>();
        Element data = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true); // NOI18N
        if (data == null) {
            data = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true); // NOI18N
            if (data == null) {
                return list;
            }
        }
        List<Element> wms = Util.findSubElements(data);
        Iterator<Element> it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = it.next();
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
                    continue;
                }
                if (el.getLocalName().equals("web-inf")) { // NOI18N
                    wm.webInf = Util.findText(el);
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
            AuxiliaryConfiguration aux, List<WebModule> webModules) {
        //assert ProjectManager.mutex().isWriteAccess();
        // do we need /2 data?
        boolean need2 = false;
        String namespace;
        // Look for existing /2 data.
        Element data = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true);
        if (data != null) {
            // Fine, use it as is.
            need2 = true;
            namespace = WebProjectNature.NS_WEB_2;
        } else {
            // Or, for existing /1 data.
            
            // check whether we need /2 data.
            for (WebModule webModule : webModules) {
                String expected = webModule.docRoot + "/WEB-INF"; //NOI18N
                String webInf = webModule.webInf;
                if (webInf != null
                        && !webInf.equals(expected)) {
                    need2 = true;
                    break;
                }
            }
            namespace = need2 ? WebProjectNature.NS_WEB_2 : WebProjectNature.NS_WEB_1;
            data = aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
            if (data != null) {
                if (need2) {
                    // Have to upgrade.
                    aux.removeConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true);
                    data = Util.getPrimaryConfigurationData(helper).getOwnerDocument()
                            .createElementNS(WebProjectNature.NS_WEB_2, WebProjectNature.EL_WEB);
                } // else can use it as is
            } else {
                // Create /1 or /2 data acc. to need.
                data = Util.getPrimaryConfigurationData(helper).getOwnerDocument()
                        .createElementNS(namespace, WebProjectNature.EL_WEB);
            }
        }
        
        Document doc = data.getOwnerDocument();
        List<Element> wms = Util.findSubElements(data);
        Iterator<Element> it = wms.iterator();
        while (it.hasNext()) {
            Element wmEl = it.next();
            data.removeChild(wmEl);
        }
        Iterator<WebModule> it2 = webModules.iterator();
        while (it2.hasNext()) {
            Element wmEl = doc.createElementNS(namespace, "web-module"); // NOI18N
            data.appendChild(wmEl);
            WebModule wm = it2.next();
            Element el;
            if (wm.docRoot != null) {
                el = doc.createElementNS(namespace, "doc-root"); // NOI18N
                el.appendChild(doc.createTextNode(wm.docRoot));
                wmEl.appendChild(el);
            }
            if (wm.classpath != null) {
                el = doc.createElementNS(namespace, "classpath"); // NOI18N
                el.appendChild(doc.createTextNode(wm.classpath));
                wmEl.appendChild(el);
            }
            if (wm.contextPath != null) {
                el = doc.createElementNS(namespace, "context-path"); // NOI18N
                el.appendChild(doc.createTextNode(wm.contextPath));
                wmEl.appendChild(el);
            }
            if (wm.j2eeSpecLevel != null) {
                el = doc.createElementNS(namespace, "j2ee-spec-level"); // NOI18N
                el.appendChild(doc.createTextNode(wm.j2eeSpecLevel));
                wmEl.appendChild(el);
            }
            if (need2 && wm.webInf != null) {
                assert namespace.equals(WebProjectNature.NS_WEB_2);
                el = doc.createElementNS(namespace, "web-inf"); // NOI18N
                el.appendChild(doc.createTextNode(wm.webInf));
                wmEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
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
        public String webInf;
    }

}
