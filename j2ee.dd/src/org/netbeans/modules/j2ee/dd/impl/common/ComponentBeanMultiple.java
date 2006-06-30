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

/**
 * Superclass that implements DisplayNameInterface and IconInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.common.IconInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface;

public abstract class ComponentBeanMultiple extends DescriptionBeanMultiple implements DisplayNameInterface, IconInterface {
    
    public ComponentBeanMultiple(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    // methods implemented by specific BaseBeans e.g. Servlet
    public void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon icon){}
    public void  setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] icons){}
    public org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int i){return null;}
    public org.netbeans.modules.j2ee.dd.api.common.Icon[] getIcon(){return null;}
    public int sizeIcon(){return 0;}
    public int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon){return 0;}
    public int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon){return 0;}
    
    public abstract void setDisplayName(int index, java.lang.String value);
    public abstract String getDisplayName(int index);
    public abstract void setDisplayName(java.lang.String[] value);
    //public abstract java.lang.String[] getDisplayName();
    public abstract int sizeDisplayName();
    public abstract int addDisplayName(java.lang.String value);
    //public abstract int removeDisplayName(java.lang.String value);
    public abstract void setDisplayNameXmlLang(int index, java.lang.String value);
    public abstract String getDisplayNameXmlLang(int index);
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        if (displayName==null) removeDisplayNameForLocale(locale);
        else {
            int size = sizeDisplayName();
            boolean found=false;
            for (int i=0;i<size;i++) {
                String loc=getDisplayNameXmlLang(i);
                if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                    found=true;
                    setDisplayName(i, displayName);
                    break;
                }
            }
            if (!found) {
                addDisplayName(displayName);
                if (locale!=null) setDisplayNameXmlLang(size, locale.toLowerCase());
            }
        }
    }
    
    public void setDisplayName(String displayName) {
        try {
            setDisplayName(null,displayName);
        } catch (VersionNotSupportedException ex){}
    }
    
    public void setAllDisplayNames(java.util.Map displayNames) throws VersionNotSupportedException {
        removeAllDisplayNames();
        if (displayNames!=null) {
            java.util.Iterator keys = displayNames.keySet().iterator();
            String[] newDisplayName = new String[displayNames.size()]; 
            int i=0;
            while (keys.hasNext()) {
                String key = (String) keys.next();
                addDisplayName((String)displayNames.get(key));
                setDisplayNameXmlLang(i++, key);
            }
        }
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        for (int i=0;i<sizeDisplayName();i++) {
            String loc=getDisplayNameXmlLang(i);
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                return getDisplayName(i);
            }
        }
        return null;
    }
    public String getDefaultDisplayName() {
        try {
            return getDisplayName(null);
        } catch (VersionNotSupportedException ex){return null;}
    }
    public java.util.Map getAllDisplayNames() {
        java.util.Map map =new java.util.HashMap();
        for (int i=0;i<sizeDisplayName();i++) {
            String desc=getDisplayName(i);
            String loc=getDisplayNameXmlLang(i);
            map.put(loc, desc);
        }
        return map;
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        java.util.Map map = new java.util.HashMap();
        for (int i=0;i<sizeDisplayName();i++) {
            String desc=getDisplayName(i);
            String loc=getDisplayNameXmlLang(i);
            if ((locale==null && loc!=null) || (locale!=null && !locale.equalsIgnoreCase(loc)))
                map.put(loc, desc);
        }
        setAllDisplayNames(map);
    }
    
    public void removeDisplayName() {
        try {
            removeDisplayNameForLocale(null);
        } catch (VersionNotSupportedException ex){}
    }
    public void removeAllDisplayNames() {
        setDisplayName(new String[]{});
    }
    
    // setters    
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        setIcon(locale, icon, true);
    }
    public void setSmallIcon(String icon) {
        try {
            setSmallIcon(null,icon);
        } catch (VersionNotSupportedException ex){}
    }
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        setIcon(locale, icon, false);
    }
    public void setLargeIcon(String icon) {
        try {
            setLargeIcon(null,icon);
        } catch (VersionNotSupportedException ex){}
    }
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        org.netbeans.modules.j2ee.dd.api.common.Icon[] newIcons = new org.netbeans.modules.j2ee.dd.api.common.Icon[locales.length];
        for (int i=0;i<locales.length;i++) {
            try {
                newIcons[i] = (org.netbeans.modules.j2ee.dd.api.common.Icon)createBean("Icon"); //NOI18N
                if (smallIcons[i]!=null) newIcons[i].setSmallIcon(smallIcons[i]);
                if (largeIcons[i]!=null) newIcons[i].setLargeIcon(largeIcons[i]);
                if (locales[i]!=null) newIcons[i].setXmlLang(locales[i]);
            } catch (ClassNotFoundException ex){}
        }
        setIcon(newIcons);
    }
    
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon) {
        if (icon==null) removeIcon();
        else {
            org.netbeans.modules.j2ee.dd.api.common.Icon[] oldIcons = getIcon();
            boolean found=false;
            try {
                String locale = icon.getXmlLang();
                for (int i=0;i<oldIcons.length;i++) {
                    String loc=oldIcons[i].getXmlLang();
                        if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                        found=true;
                        setIcon(i, icon);
                    }
                }
            } catch (VersionNotSupportedException ex){}
            if (!found) {
                addIcon(icon);
            }
        }
    }
    
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        return getIcon(locale,true);
    }
    public String getSmallIcon() {
        try {
            return getSmallIcon(null);
        } catch (VersionNotSupportedException ex){return null;}
    }
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        return getIcon(locale,false);
    }
    public String getLargeIcon() {
        try {
            return getLargeIcon(null);
        } catch (VersionNotSupportedException ex){return null;}
    }
    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon[] icons = getIcon();
        for (int i=0;i<icons.length;i++) {
            try {
                String loc=icons[i].getXmlLang();
                if (loc==null) return icons[i];
            } catch (VersionNotSupportedException ex){}
        }
        return null;
    }
    public java.util.Map getAllIcons() {
        java.util.Map map =new java.util.HashMap();
        org.netbeans.modules.j2ee.dd.api.common.Icon[] icons = getIcon();
        for (int i=0;i<icons.length;i++) {
            String[] iconPair = new String[] {icons[i].getSmallIcon(),icons[i].getLargeIcon()};
            String loc=null;
            try {
                loc=icons[i].getXmlLang();
            } catch (VersionNotSupportedException ex){}
            map.put(loc, iconPair);
        }
        return map;
    }
    
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        removeIcon(locale, true);
    }
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        removeIcon(locale, false);
    }
    public void removeIcon(String locale) throws VersionNotSupportedException {
        org.netbeans.modules.j2ee.dd.api.common.Icon[] icons = getIcon();
        for (int i=0;i<icons.length;i++) {
            String loc=icons[i].getXmlLang();
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                removeIcon(icons[i]);                    
            }
        }
    }
    public void removeSmallIcon() {
        try {
            removeSmallIcon(null);
        } catch (VersionNotSupportedException ex){}
    }
    public void removeLargeIcon() {
        try {
            removeLargeIcon(null);
        } catch (VersionNotSupportedException ex){}
    }
    public void removeIcon() {
        org.netbeans.modules.j2ee.dd.api.common.Icon[] icons = getIcon();
        for (int i=0;i<icons.length;i++) {
            try {
                String loc=icons[i].getXmlLang();
                if (loc==null) removeIcon(icons[i]);
            } catch (VersionNotSupportedException ex){}
        }
    }
    public void removeAllIcons() {
        setIcon(new org.netbeans.modules.j2ee.dd.api.common.Icon[]{});
    }
    private void setIcon(String locale, String icon, boolean isSmall) throws VersionNotSupportedException {
        if (icon==null) {
            if (isSmall) removeSmallIcon(locale);
            else removeLargeIcon(locale);
        }
        else {
            org.netbeans.modules.j2ee.dd.api.common.Icon[] oldIcons = getIcon();
            boolean found=false;
            for (int i=0;i<oldIcons.length;i++) {
                String loc=oldIcons[i].getXmlLang();
                if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                    found=true;
                    if (isSmall) oldIcons[i].setSmallIcon(icon);
                    else oldIcons[i].setLargeIcon(icon);
                    break;
                }
            }
            if (!found) {
                try {
                    org.netbeans.modules.j2ee.dd.api.common.Icon newIcon = (org.netbeans.modules.j2ee.dd.api.common.Icon)createBean("Icon"); //NOI18N
                    if (locale!=null) newIcon.setXmlLang(locale.toLowerCase());
                    if (isSmall) newIcon.setSmallIcon(icon);
                    else newIcon.setLargeIcon(icon);
                    addIcon(newIcon);
                } catch (ClassNotFoundException ex){}
            }
        }
    }
    private String getIcon(String locale, boolean isSmall) throws VersionNotSupportedException {
        for (int i=0;i<sizeIcon();i++) {
            org.netbeans.modules.j2ee.dd.api.common.Icon icon = getIcon(i);
            String loc=icon.getXmlLang();
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                if (isSmall) return icon.getSmallIcon();
                else return icon.getLargeIcon();
            }
        }
        return null;
    }
    
    public void removeIcon(String locale, boolean isSmall) throws VersionNotSupportedException  {
        org.netbeans.modules.j2ee.dd.api.common.Icon[] icons = getIcon();
        java.util.List iconList = new java.util.ArrayList();
        for (int i=0;i<icons.length;i++) {
            String loc=icons[i].getXmlLang();
            if ((locale==null && loc==null) || (locale!=null && locale.equalsIgnoreCase(loc))) {
                if (isSmall) {
                    icons[i].setSmallIcon(null);
                    if (icons[i].getLargeIcon()==null) iconList.add(icons[i]);
                } else {
                    icons[i].setLargeIcon(null);
                    if (icons[i].getSmallIcon()==null) iconList.add(icons[i]);                    
                }
            }
        }
        java.util.Iterator it = iconList.iterator();
        while(it.hasNext()) removeIcon((org.netbeans.modules.j2ee.dd.api.common.Icon)it.next());
    }
}
