/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.web.core.syntax.spi;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Iterator;

import javax.servlet.jsp.tagext.TagLibraryInfo;

/** Holds data relevant to the JSP coloring for one JSP page. The main purposes 
 * of this class are
 * to report which prefixes are tag library prefixes in the page, and allows 
 * listening on the change of the prefixes, at which point the page needs to be 
 * recolored.
 *
 * @author Petr Jiricka
 */
public final class JSPColoringData extends PropertyChangeSupport {
    
    /** An property whose change is fired every time the tag library 
    *  information changes in such a way that recoloring of the document is required. 
    */
    public static final String PROP_COLORING_CHANGE = "coloringChange"; // NOI18N
    
    /** Taglib id -> TagLibraryInfo */
    private Map taglibs;
    
    /** Prefix -> Taglib id */
    private Map prefixMapper;
    
    private boolean elIgnored = false;
    
    /** Creates a new instance of JSPColoringData. */
    public JSPColoringData(Object sourceBean) {
        super(sourceBean);
    }
    
    public String toString() {
        return "JSPColoringData, taglibMap:\n" +
          (prefixMapper == null ?
            "null" :
            mapToString(prefixMapper, "  ")
          );
    }
    
    private static String mapToString(Map m, String indent) {
        StringBuffer sb = new StringBuffer();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            sb.append(indent).append(key).append(" -> ").append(m.get(key)).append("\n");
        }
        return sb.toString();
    }

    /** Returns true if the given tag library prefix is known in this page.
     */
    public boolean isTagLibRegistered(String prefix) {
        if ((taglibs == null) || (prefixMapper == null)) {
            return false;
        }
        return prefixMapper.containsKey(prefix);
    }
    
    /** Returns true if the EL is ignored in this page.
     */
    public boolean isELIgnored() {
        return elIgnored;
    }
    
    /*public boolean isBodyIntepretedByTag(String prefix, String tagName) {
    }*/
        
    /** Incorporates new parse data from the parser, possibly firing a change about coloring.
     * @param newTaglibs the new map of (uri -> TagLibraryInfo)
     * @param newPrefixMapper the new map of (prefix, uri)
     * @param parseSuccessful wherher parsing was successful. If false, then the new information is partial only
     */
    public void applyParsedData(Map newTaglibs, Map newPrefixMapper, boolean newELIgnored, boolean parseSuccessful) {
        // check whether coloring has not changed
        boolean coloringSame = equalsColoringInformation(taglibs, prefixMapper, newTaglibs, newPrefixMapper);
        
        // check and apply EL data
        if (parseSuccessful) {
            coloringSame = coloringSame && (elIgnored == newELIgnored);
            elIgnored = newELIgnored;
        }
        
        // appy taglib data
        if (parseSuccessful || (taglibs == null) || (prefixMapper == null)) {
            // overwrite
            taglibs = newTaglibs;
            prefixMapper = newPrefixMapper;
        }
        else {
            // merge
            Iterator it = newPrefixMapper.keySet().iterator();
            while (it.hasNext()) {
                Object prefix = it.next();
                Object uri = newPrefixMapper.get(prefix);
                Object uriOld = prefixMapper.get(prefix);
                if ((uriOld == null) || !uri.equals(uriOld)) {
                    Object newTaglib = newTaglibs.get(uri);
                    if (newTaglib != null) {
                        // change - merge it
                        prefixMapper.put(prefix, uri);
                        taglibs.put(uri, newTaglib);
                    }
                }
            }
        }
        // possibly fire the change
        if (!coloringSame) {
            firePropertyChange(PROP_COLORING_CHANGE, null, null);
        }
    }

    private static boolean equalsColoringInformation(Map taglibs1, Map prefixMapper1, Map taglibs2, Map prefixMapper2) {
        if ((taglibs1 == null) != (taglibs2 == null)) {
            return false;
        }
        if ((prefixMapper1 == null) != (prefixMapper2 == null)) {
            return false;
        }
        if (prefixMapper1.size() != prefixMapper2.size()) {
            return false;
        }
        else {
            Iterator it = prefixMapper1.keySet().iterator();
            while (it.hasNext()) {
                Object prefix = it.next();
                Object key1 = prefixMapper1.get(prefix);
                Object key2 = prefixMapper2.get(prefix);
                if ((key1 == null) || (key2 == null)) {
                    return false;
                }
                TagLibraryInfo tli1 = (TagLibraryInfo)taglibs1.get(key1);
                TagLibraryInfo tli2 = (TagLibraryInfo)taglibs2.get(key2);
                if ((tli1 == null) || (tli2 == null)) {
                    return false;
                }
                if (!equalsColoringInformation(tli1, tli2)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean equalsColoringInformation(TagLibraryInfo tli1, TagLibraryInfo tli2) {
        /** PENDING
         * should be going through all tags and checking whether the value 
         * returned by tagInfo.getBodyContent() has not changed.
         */
        return true;
    }
    
}
