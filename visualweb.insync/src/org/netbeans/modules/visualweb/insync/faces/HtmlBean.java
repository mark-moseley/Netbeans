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
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.BeanInfo;

import org.w3c.dom.Element;

import com.sun.rave.designtime.DesignBean;

/**
 * Concrete bean that is persisted entirely in markup source and not Java.
 */
public class HtmlBean extends MarkupBean {

    // XXX This is very suspicious and error prone, to use the strings to compare classes.
//    public static final String PACKAGE = "org.netbeans.modules.visualweb.xhtml.";
    public static final String PACKAGE = org.netbeans.modules.visualweb.xhtml.Html.class.getPackage().getName() + "."; // NOI18N
    public static final int PACKAGE_LEN = PACKAGE.length();

    public static final String JSP_PAGE_CLASSBASE = PACKAGE + "Jsp_";
    public static final int JSP_PAGE_CLASSBASE_LEN = JSP_PAGE_CLASSBASE.length();

    public static final String JSF_CORE_CLASSBASE = PACKAGE + "F_";
    public static final int JSF_CORE_CLASSBASE_LEN = JSF_CORE_CLASSBASE.length();

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a bean bound to existing field & accessor methods, and page element
     */
    HtmlBean(FacesPageUnit unit, BeanInfo beanInfo, String tag, Element element) {
        super(unit, beanInfo, tag, element);
    }

    /**
     * Construct a new bean, creating the underlying field and accessor methods and using given page
     * element
     */
    HtmlBean(FacesPageUnit unit, BeanInfo beanInfo, String tag, MarkupBean parent, Element element) {
        super(unit, beanInfo, tag, parent, element);
    }

    /**
     * Return true if the BeanInto describes one of our beans
     */
    public static final boolean isHtmlBean(BeanInfo bi) {
        return bi.getBeanDescriptor().getBeanClass().getName().startsWith(PACKAGE);
    }

    /**
     * Return the classname for a bean described by a source element iff it is our bean
     */
    public static final String getBeanClassname(Element e) {
        String ns = e.getNamespaceURI();
        String tag = e.getLocalName();
        String cname = classtailFromTag(tag);

        if (ns != null) {
            if (ns.equals(FacesPageUnit.URI_JSP_PAGE))
                return PACKAGE + "Jsp_" + cname;  // JSP tag, use fake bean
            if (ns.equals(FacesPageUnit.URI_JSF_CORE))
                return PACKAGE + "F_" + cname;  // Faces core, use fake bean
        }
        else
            return PACKAGE + cname;  // HTML, use fake bean

        return null;  // not an HTML or other fake bean
    }

    /**
     * @param tag
     * @return
     */
    static String classtailFromTag(String tag) {
        StringBuffer sb = new StringBuffer(tag);
        int len = sb.length();
        boolean first = true;
        for (int i = 0; i < len; i++) {
            char ch = sb.charAt(i);
            if (ch == '.') {
                sb.setCharAt(i, '_');
                first = true;
            }
            else if (first && Character.isJavaIdentifierStart(ch)) {
                sb.setCharAt(i, Character.toUpperCase(ch));
                first = false;
            }
        }
        return sb.toString();
    }

    /**
     * @param cname
     * @return
     */
    static String tagFromClasstail(String cname) {
        StringBuffer sb = new StringBuffer(cname);
        int len = sb.length();
        boolean first = true;
        for (int i = 0; i < len; i++) {
            char ch = sb.charAt(i);
            if (ch == '_') {
                sb.setCharAt(i, '.');
                first = true;
            }
            else if (first && Character.isJavaIdentifierStart(ch)) {
                sb.setCharAt(i, Character.toLowerCase(ch));
                first = false;
            }
        }
        return sb.toString();
    }

    /**
     * Return the taglib URI for a bean described by a BeanInfo iff it is our bean
     */
    public static final String getBeanTaglibUri(BeanInfo bi) {
        String cname = bi.getBeanDescriptor().getBeanClass().getName();
        if (cname.startsWith(JSP_PAGE_CLASSBASE))
            return FacesPageUnit.URI_JSP_PAGE;
        if (cname.startsWith(JSF_CORE_CLASSBASE))
            return FacesPageUnit.URI_JSF_CORE;
        return null;  // html
    }

    /**
     * Return the taglib prefix for a bean described by a BeanInfo iff it is our bean
     */
    public static final String getBeanTaglibPrefix(BeanInfo bi) {
        String cname = bi.getBeanDescriptor().getBeanClass().getName();
        if (cname.startsWith(JSP_PAGE_CLASSBASE))
            return "jsp";
        if (cname.startsWith(JSF_CORE_CLASSBASE))
            return "f";
        return null;  // html
    }

    /**
     * Return the tag for a bean described by a BeanInfo iff it is our bean
     */
    public static final String getBeanTagName(BeanInfo bi) {
        String cname = bi.getBeanDescriptor().getBeanClass().getName();
        if (cname.startsWith(JSP_PAGE_CLASSBASE))
            return tagFromClasstail(cname.substring(JSP_PAGE_CLASSBASE_LEN));
        if (cname.startsWith(JSF_CORE_CLASSBASE))
            return tagFromClasstail(cname.substring(JSF_CORE_CLASSBASE_LEN));
        return tagFromClasstail(cname.substring(PACKAGE_LEN));
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#canSetName()
     */
    public boolean canSetName() {
        return false;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#setName(java.lang.String, boolean, com.sun.rave.designtime.DesignBean)
     */
    public String setName(String name, boolean autoNumber, DesignBean liveBean) {
        return null;
    }
}
