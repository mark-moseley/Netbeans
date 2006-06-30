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

package org.netbeans.modules.editor.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.openide.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** MIME Option XML file for Abbreviations settings.
 *  Abbreviations settings are loaded and saved in XML format
 *  according to EditorAbbreviations-1_0.dtd.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class AbbrevsMIMEOptionFile extends MIMEOptionFile{
    
    /** Elements */
    public static final String TAG_ROOT = "abbrevs"; //NOI18N
    public static final String TAG_ABBREV = "abbrev"; //NOI18N
    
    /** Attributes */
    public static final String ATTR_KEY = "key"; //NOI18N
    public static final String ATTR_ACTION = "action"; //NOI18N
    public static final String ATTR_REMOVE = "remove"; //NOI18N
    public static final String ATTR_XML_SPACE = "xml:space"; //NOI18N    
    public static final String VALUE_XML_SPACE = "preserve"; //NOI18N
    
    /** File name of this MIMEOptionFile */
    static final String FILENAME = "abbreviations"; //NOI18N
    
    public AbbrevsMIMEOptionFile(BaseOptions base, Object proc) {
        super(base, proc);
    }
    
    /** Loads settings from XML file.
     * @param propagate if true - propagates the loaded settings to Editor UI */
    protected void loadSettings(boolean propagate){
        synchronized (Settings.class) {
            Document doc = dom;
            Element rootElement = doc.getDocumentElement();

            if (!TAG_ROOT.equals(rootElement.getTagName())) {
                // Wrong root element
                return;
            }

            // gets current abbreviations map
            Map abbrevsMap = (Map)Settings.getValue(base.getKitClass(), SettingsNames.ABBREV_MAP);
            Map mapa = (abbrevsMap==null) ? new HashMap() : new HashMap(abbrevsMap);
            properties.clear();

            NodeList abbr = rootElement.getElementsByTagName(TAG_ABBREV);
            int len = abbr.getLength();
            for (int i=0; i < len; i++){
                Node node = abbr.item(i);
                Element FCElement = (Element)node;

                if (FCElement == null){
                    continue;
                }

                String key    = FCElement.getAttribute(ATTR_KEY);
                String delete    = FCElement.getAttribute(ATTR_REMOVE);
                String expanded  = "";

                if (! Boolean.valueOf(delete).booleanValue()){
                    NodeList textList = FCElement.getChildNodes();
                    if (textList.getLength() > 0) {
                        Node subNode = textList.item(0);
                        if (subNode instanceof Text) {
                            Text textNode = (Text) subNode;
                            expanded = textNode.getData();
                        }
                    }
                }

                properties.put(key, expanded);
            }

            if (properties.size()>0){
                // create updated map
                mapa.putAll(properties);

                // remove all deleted values
                for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
                    String key = (String)i.next();
                    if(((String)properties.get(key)).length() == 0){
                        mapa.remove(key);
                    }
                }
                // setAbbrevMap without saving to XML
                if (propagate){
                    base.setAbbrevMap(mapa, false);
                }
            }
            if (propagate) setLoaded(true);
        }
    }
    
    /** Save settings to XML file
     *  @param changedProp the Map of settings to save */
    protected void updateSettings(Map changedProp){
        synchronized (Settings.class) {
            // put changed properties to local map
            properties.putAll(changedProp);

            // now we can save local map to XML file
            Document doc = XMLUtil.createDocument(TAG_ROOT, null, processor.getPublicID(), processor.getSystemID());
            Element rootElem = doc.getDocumentElement();

            ArrayList removed = new ArrayList();

            Map defaultAbbrevs = base.getDefaultAbbrevMap();
            // if default abbreviations don't exist for appropriate kit, set them empty
            if (defaultAbbrevs == null) defaultAbbrevs = new HashMap();

            // save XML
            for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                if (properties.get(key) instanceof String){

                    String action = (String) properties.get(key);
                    if (action.length()==0){
                        // null value => DETETE: if property is in default set, mark it as deleted else delete it completely
                        if (!defaultAbbrevs.containsKey(key)) {
                            removed.add(key);
                            continue;
                        }
                    } else{
                        // if key and value is already in settings default, no need to store
                        // this in diff XML file
                        if (defaultAbbrevs.containsKey(key)){
                            String defValue = (String) defaultAbbrevs.get(key);
                            if (defValue.equals(action)){
                                removed.add(key);
                                continue;
                            }
                        }
                    }

                    Element abbrevElem = doc.createElement(TAG_ABBREV);
                    abbrevElem.setAttribute(ATTR_KEY, key);
                    if (action.length()==0){
                        abbrevElem.setAttribute(ATTR_REMOVE, Boolean.TRUE.toString());
                    }else{
                        abbrevElem.setAttribute(ATTR_XML_SPACE, VALUE_XML_SPACE);                    
                        abbrevElem.appendChild(doc.createTextNode(action));
                    }

                    rootElem.appendChild(abbrevElem);
                }
            }

            for (int i=0; i<removed.size(); i++){
                properties.remove(removed.get(i));
            }

            doc.getDocumentElement().normalize();

            saveSettings(doc);
        }
    }
    
}
