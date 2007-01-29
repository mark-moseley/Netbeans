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
package org.netbeans.modules.visualweb.xhtml;

/**
 * <b>ITrChoice</b> is generated from xhtml.rng by Relaxer.
 * Concrete classes of the interface are Th and Td.
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public interface ITrChoice {

    /**
     * @param value
     */
    void setContent(String value);

    /**
     * @param value
     */
    void setContent(org.w3c.dom.Node value);

    /**
     * @param value
     */
    void addContent(org.w3c.dom.Node value);

    /**
     * @return String
     */
    String getId();

    /**
     * @param id
     */
    void setId(String id);

    /**
     * @return String[]
     */
    String[] getClassValue();

    /**
     * @param classValue
     */
    void setClassValue(String[] classValue);

    /**
     * @param classValue
     */
    void setClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String classValue);

    /**
     * @param classValue
     */
    void addClassValue(String[] classValue);

    /**
     * @return int
     */
    int sizeClassValue();

    /**
     * @param index
     * @return String
     */
    String getClassValue(int index);

    /**
     * @param index
     * @param classValue
     */
    void setClassValue(int index, String classValue);

    /**
     * @param index
     * @param classValue
     */
    void addClassValue(int index, String classValue);

    /**
     * @param index
     */
    void removeClassValue(int index);

    /**
     * @param classValue
     */
    void removeClassValue(String classValue);

    /**
     */
    void clearClassValue();

    /**
     * @return String
     */
    String getTitle();

    /**
     * @param title
     */
    void setTitle(String title);

    /**
     * @return String
     */
    String getStyle();

    /**
     * @param style
     */
    void setStyle(String style);

    /**
     * @return java.util.Locale
     */
    java.util.Locale getXmlLang();

    /**
     * @param xmlLang
     */
    void setXmlLang(java.util.Locale xmlLang);

    /**
     * @return java.util.Locale
     */
    java.util.Locale getLang();

    /**
     * @param lang
     */
    void setLang(java.util.Locale lang);

    /**
     * @return String
     */
    String getDir();

    /**
     * @param dir
     */
    void setDir(String dir);

    /**
     * @return String
     */
    String getOnclick();

    /**
     * @param onclick
     */
    void setOnclick(String onclick);

    /**
     * @return String
     */
    String getOndblclick();

    /**
     * @param ondblclick
     */
    void setOndblclick(String ondblclick);

    /**
     * @return String
     */
    String getOnmousedown();

    /**
     * @param onmousedown
     */
    void setOnmousedown(String onmousedown);

    /**
     * @return String
     */
    String getOnmouseup();

    /**
     * @param onmouseup
     */
    void setOnmouseup(String onmouseup);

    /**
     * @return String
     */
    String getOnmouseover();

    /**
     * @param onmouseover
     */
    void setOnmouseover(String onmouseover);

    /**
     * @return String
     */
    String getOnmousemove();

    /**
     * @param onmousemove
     */
    void setOnmousemove(String onmousemove);

    /**
     * @return String
     */
    String getOnmouseout();

    /**
     * @param onmouseout
     */
    void setOnmouseout(String onmouseout);

    /**
     * @return String
     */
    String getOnkeypress();

    /**
     * @param onkeypress
     */
    void setOnkeypress(String onkeypress);

    /**
     * @return String
     */
    String getOnkeydown();

    /**
     * @param onkeydown
     */
    void setOnkeydown(String onkeydown);

    /**
     * @return String
     */
    String getOnkeyup();

    /**
     * @param onkeyup
     */
    void setOnkeyup(String onkeyup);

    /**
     * @return String
     */
    String getAbbr();

    /**
     * @param abbr
     */
    void setAbbr(String abbr);

    /**
     * @return String
     */
    String getAxis();

    /**
     * @param axis
     */
    void setAxis(String axis);

    /**
     * @return String[]
     */
    String[] getHeaders();

    /**
     * @param headers
     */
    void setHeaders(String[] headers);

    /**
     * @param headers
     */
    void setHeaders(String headers);

    /**
     * @param headers
     */
    void addHeaders(String headers);

    /**
     * @param headers
     */
    void addHeaders(String[] headers);

    /**
     * @return int
     */
    int sizeHeaders();

    /**
     * @param index
     * @return String
     */
    String getHeaders(int index);

    /**
     * @param index
     * @param headers
     */
    void setHeaders(int index, String headers);

    /**
     * @param index
     * @param headers
     */
    void addHeaders(int index, String headers);

    /**
     * @param index
     */
    void removeHeaders(int index);

    /**
     * @param headers
     */
    void removeHeaders(String headers);

    /**
     */
    void clearHeaders();

    /**
     * @return String
     */
    String getScope();

    /**
     * @param scope
     */
    void setScope(String scope);

    /**
     * @return String
     */
    String getRowspan();

    /**
     * @param rowspan
     */
    void setRowspan(String rowspan);

    /**
     * @return String
     */
    String getColspan();

    /**
     * @param colspan
     */
    void setColspan(String colspan);

    /**
     * @return String
     */
    String getAlign();

    /**
     * @param align
     */
    void setAlign(String align);

    /**
     * @return String
     */
    String getChar();

    /**
     * @param charValue
     */
    void setChar(String charValue);

    /**
     * @return String
     */
    String getCharoff();

    /**
     * @param charoff
     */
    void setCharoff(String charoff);

    /**
     * @return String
     */
    String getValign();

    /**
     * @param valign
     */
    void setValign(String valign);

    /**
     * @return String
     */
    String getNowrap();

    /**
     * @param nowrap
     */
    void setNowrap(String nowrap);

    /**
     * @return String
     */
    String getBgcolor();

    /**
     * @param bgcolor
     */
    void setBgcolor(String bgcolor);

    /**
     * @return String
     */
    String getWidth();

    /**
     * @param width
     */
    void setWidth(String width);

    /**
     * @return String
     */
    String getHeight();

    /**
     * @param height
     */
    void setHeight(String height);

    /**
     * @return IFlowModelMixed[]
     */
    IFlowModelMixed[] getContent();

    /**
     * @param content
     */
    void setContent(IFlowModelMixed[] content);

    /**
     * @param content
     */
    void setContent(IFlowModelMixed content);

    /**
     * @param content
     */
    void addContent(IFlowModelMixed content);

    /**
     * @param content
     */
    void addContent(IFlowModelMixed[] content);

    /**
     * @return int
     */
    int sizeContent();

    /**
     * @param index
     * @return IFlowModelMixed
     */
    IFlowModelMixed getContent(int index);

    /**
     * @param index
     * @param content
     */
    void setContent(int index, IFlowModelMixed content);

    /**
     * @param index
     * @param content
     */
    void addContent(int index, IFlowModelMixed content);

    /**
     * @param index
     */
    void removeContent(int index);

    /**
     * @param content
     */
    void removeContent(IFlowModelMixed content);

    /**
     */
    void clearContent();
}
