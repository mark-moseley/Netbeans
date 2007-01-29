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
 * <b>Input</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="input">
 *     <ref name="input.attlist"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="input"&gt;
 *     &lt;ref name="input.attlist"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:09 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Input {
    public static final String DISABLED_DISABLED = "disabled";
    public static final String READONLY_READONLY = "readonly";
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_BUTTON = "button";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PASSWORD = "password";
    public static final String TYPE_CHECKBOX = "checkbox";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_SUBMIT = "submit";
    public static final String TYPE_RESET = "reset";
    public static final String TYPE_HIDDEN = "hidden";
    public static final String CHECKED_CHECKED = "checked";
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_ALL = "all";
    public static final String ALIGN_RIGHT = "right";
    public static final String ALIGN_NONE = "none";
    private String disabled_;
    private String readonly_;
    private String alt_;
    private String tabindex_;
    private String accept_;
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String xmlLang_;
    private String lang_;
    private String dir_;
    private String onclick_;
    private String ondblclick_;
    private String onmousedown_;
    private String onmouseup_;
    private String onmouseover_;
    private String onmousemove_;
    private String onmouseout_;
    private String onkeypress_;
    private String onkeydown_;
    private String onkeyup_;
    private String type_;
    private String name_;
    private String value_;
    private String checked_;
    private String size_;
    private String maxlength_;
    private String src_;
    private String accesskey_;
    private String onblur_;
    private String onchange_;
    private String onfocus_;
    private String onselect_;
    private String align_;
    /**
     * Creates a <code>Input</code>.
     *
     */
    public Input() {
    }
    /**
     * Gets the String property <b>disabled</b>.
     *
     * @return String
     */
    public String getDisabled() {
        return (disabled_);
    }
    /**
     * Sets the String property <b>disabled</b>.
     *
     * @param disabled
     */
    public void setDisabled(String disabled) {
        this.disabled_ = disabled;
    }
    /**
     * Gets the String property <b>readonly</b>.
     *
     * @return String
     */
    public String getReadonly() {
        return (readonly_);
    }
    /**
     * Sets the String property <b>readonly</b>.
     *
     * @param readonly
     */
    public void setReadonly(String readonly) {
        this.readonly_ = readonly;
    }
    /**
     * Gets the String property <b>alt</b>.
     *
     * @return String
     */
    public String getAlt() {
        return (alt_);
    }
    /**
     * Sets the String property <b>alt</b>.
     *
     * @param alt
     */
    public void setAlt(String alt) {
        this.alt_ = alt;
    }
    /**
     * Gets the String property <b>tabindex</b>.
     *
     * @return String
     */
    public String getTabindex() {
        return (tabindex_);
    }
    /**
     * Sets the String property <b>tabindex</b>.
     *
     * @param tabindex
     */
    public void setTabindex(String tabindex) {
        this.tabindex_ = tabindex;
    }
    /**
     * Gets the String property <b>accept</b>.
     *
     * @return String
     */
    public String getAccept() {
        return (accept_);
    }
    /**
     * Sets the String property <b>accept</b>.
     *
     * @param accept
     */
    public void setAccept(String accept) {
        this.accept_ = accept;
    }
    /**
     * Gets the String property <b>id</b>.
     *
     * @return String
     */
    public String getId() {
        return (id_);
    }
    /**
     * Sets the String property <b>id</b>.
     *
     * @param id
     */
    public void setId(String id) {
        this.id_ = id;
    }
    /**
     * Gets the String property <b>classValue</b>.
     *
     * @return String
     */
    public String getClassValue() {
        return classValue_;
    }
    /**
     * Sets the String property <b>classValue</b>.
     *
     * @param classValue
     */
    public void setClassValue(String classValue) {
        this.classValue_ = classValue;
    }
    /**
     * Gets the String property <b>title</b>.
     *
     * @return String
     */
    public String getTitle() {
        return (title_);
    }
    /**
     * Sets the String property <b>title</b>.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title_ = title;
    }
    /**
     * Gets the String property <b>style</b>.
     *
     * @return String
     */
    public String getStyle() {
        return (style_);
    }
    /**
     * Sets the String property <b>style</b>.
     *
     * @param style
     */
    public void setStyle(String style) {
        this.style_ = style;
    }
    /**
     * Gets the java.util.Locale property <b>xmlLang</b>.
     *
     * @return java.util.Locale
     */
    public String getXmlLang() {
        return (xmlLang_);
    }
    /**
     * Sets the java.util.Locale property <b>xmlLang</b>.
     *
     * @param xmlLang
     */
    public void setXmlLang(String xmlLang) {
        this.xmlLang_ = xmlLang;
    }
    /**
     * Gets the java.util.Locale property <b>lang</b>.
     *
     * @return java.util.Locale
     */
    public String getLang() {
        return (lang_);
    }
    /**
     * Sets the java.util.Locale property <b>lang</b>.
     *
     * @param lang
     */
    public void setLang(String lang) {
        this.lang_ = lang;
    }
    /**
     * Gets the String property <b>dir</b>.
     *
     * @return String
     */
    public String getDir() {
        return (dir_);
    }
    /**
     * Sets the String property <b>dir</b>.
     *
     * @param dir
     */
    public void setDir(String dir) {
        this.dir_ = dir;
    }
    /**
     * Gets the String property <b>onclick</b>.
     *
     * @return String
     */
    public String getOnclick() {
        return (onclick_);
    }
    /**
     * Sets the String property <b>onclick</b>.
     *
     * @param onclick
     */
    public void setOnclick(String onclick) {
        this.onclick_ = onclick;
    }
    /**
     * Gets the String property <b>ondblclick</b>.
     *
     * @return String
     */
    public String getOndblclick() {
        return (ondblclick_);
    }
    /**
     * Sets the String property <b>ondblclick</b>.
     *
     * @param ondblclick
     */
    public void setOndblclick(String ondblclick) {
        this.ondblclick_ = ondblclick;
    }
    /**
     * Gets the String property <b>onmousedown</b>.
     *
     * @return String
     */
    public String getOnmousedown() {
        return (onmousedown_);
    }
    /**
     * Sets the String property <b>onmousedown</b>.
     *
     * @param onmousedown
     */
    public void setOnmousedown(String onmousedown) {
        this.onmousedown_ = onmousedown;
    }
    /**
     * Gets the String property <b>onmouseup</b>.
     *
     * @return String
     */
    public String getOnmouseup() {
        return (onmouseup_);
    }
    /**
     * Sets the String property <b>onmouseup</b>.
     *
     * @param onmouseup
     */
    public void setOnmouseup(String onmouseup) {
        this.onmouseup_ = onmouseup;
    }
    /**
     * Gets the String property <b>onmouseover</b>.
     *
     * @return String
     */
    public String getOnmouseover() {
        return (onmouseover_);
    }
    /**
     * Sets the String property <b>onmouseover</b>.
     *
     * @param onmouseover
     */
    public void setOnmouseover(String onmouseover) {
        this.onmouseover_ = onmouseover;
    }
    /**
     * Gets the String property <b>onmousemove</b>.
     *
     * @return String
     */
    public String getOnmousemove() {
        return (onmousemove_);
    }
    /**
     * Sets the String property <b>onmousemove</b>.
     *
     * @param onmousemove
     */
    public void setOnmousemove(String onmousemove) {
        this.onmousemove_ = onmousemove;
    }
    /**
     * Gets the String property <b>onmouseout</b>.
     *
     * @return String
     */
    public String getOnmouseout() {
        return (onmouseout_);
    }
    /**
     * Sets the String property <b>onmouseout</b>.
     *
     * @param onmouseout
     */
    public void setOnmouseout(String onmouseout) {
        this.onmouseout_ = onmouseout;
    }
    /**
     * Gets the String property <b>onkeypress</b>.
     *
     * @return String
     */
    public String getOnkeypress() {
        return (onkeypress_);
    }
    /**
     * Sets the String property <b>onkeypress</b>.
     *
     * @param onkeypress
     */
    public void setOnkeypress(String onkeypress) {
        this.onkeypress_ = onkeypress;
    }
    /**
     * Gets the String property <b>onkeydown</b>.
     *
     * @return String
     */
    public String getOnkeydown() {
        return (onkeydown_);
    }
    /**
     * Sets the String property <b>onkeydown</b>.
     *
     * @param onkeydown
     */
    public void setOnkeydown(String onkeydown) {
        this.onkeydown_ = onkeydown;
    }
    /**
     * Gets the String property <b>onkeyup</b>.
     *
     * @return String
     */
    public String getOnkeyup() {
        return (onkeyup_);
    }
    /**
     * Sets the String property <b>onkeyup</b>.
     *
     * @param onkeyup
     */
    public void setOnkeyup(String onkeyup) {
        this.onkeyup_ = onkeyup;
    }
    /**
     * Gets the String property <b>type</b>.
     *
     * @return String
     */
    public String getType() {
        return (type_);
    }
    /**
     * Sets the String property <b>type</b>.
     *
     * @param type
     */
    public void setType(String type) {
        this.type_ = type;
    }
    /**
     * Gets the String property <b>name</b>.
     *
     * @return String
     */
    public String getName() {
        return (name_);
    }
    /**
     * Sets the String property <b>name</b>.
     *
     * @param name
     */
    public void setName(String name) {
        this.name_ = name;
    }
    /**
     * Gets the String property <b>value</b>.
     *
     * @return String
     */
    public String getValue() {
        return (value_);
    }
    /**
     * Sets the String property <b>value</b>.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value_ = value;
    }
    /**
     * Gets the String property <b>checked</b>.
     *
     * @return String
     */
    public String getChecked() {
        return (checked_);
    }
    /**
     * Sets the String property <b>checked</b>.
     *
     * @param checked
     */
    public void setChecked(String checked) {
        this.checked_ = checked;
    }
    /**
     * Gets the String property <b>size</b>.
     *
     * @return String
     */
    public String getSize() {
        return (size_);
    }
    /**
     * Sets the String property <b>size</b>.
     *
     * @param sizeValue
     */
    public void setSize(String sizeValue) {
        this.size_ = sizeValue;
    }
    /**
     * Gets the String property <b>maxlength</b>.
     *
     * @return String
     */
    public String getMaxlength() {
        return (maxlength_);
    }
    /**
     * Sets the String property <b>maxlength</b>.
     *
     * @param maxlength
     */
    public void setMaxlength(String maxlength) {
        this.maxlength_ = maxlength;
    }
    /**
     * Gets the String property <b>src</b>.
     *
     * @return String
     */
    public String getSrc() {
        return (src_);
    }
    /**
     * Sets the String property <b>src</b>.
     *
     * @param src
     */
    public void setSrc(String src) {
        this.src_ = src;
    }
    /**
     * Gets the String property <b>accesskey</b>.
     *
     * @return String
     */
    public String getAccesskey() {
        return (accesskey_);
    }
    /**
     * Sets the String property <b>accesskey</b>.
     *
     * @param accesskey
     */
    public void setAccesskey(String accesskey) {
        this.accesskey_ = accesskey;
    }
    /**
     * Gets the String property <b>onblur</b>.
     *
     * @return String
     */
    public String getOnblur() {
        return (onblur_);
    }
    /**
     * Sets the String property <b>onblur</b>.
     *
     * @param onblur
     */
    public void setOnblur(String onblur) {
        this.onblur_ = onblur;
    }
    /**
     * Gets the String property <b>onchange</b>.
     *
     * @return String
     */
    public String getOnchange() {
        return (onchange_);
    }
    /**
     * Sets the String property <b>onchange</b>.
     *
     * @param onchange
     */
    public void setOnchange(String onchange) {
        this.onchange_ = onchange;
    }
    /**
     * Gets the String property <b>onfocus</b>.
     *
     * @return String
     */
    public String getOnfocus() {
        return (onfocus_);
    }
    /**
     * Sets the String property <b>onfocus</b>.
     *
     * @param onfocus
     */
    public void setOnfocus(String onfocus) {
        this.onfocus_ = onfocus;
    }
    /**
     * Gets the String property <b>onselect</b>.
     *
     * @return String
     */
    public String getOnselect() {
        return (onselect_);
    }
    /**
     * Sets the String property <b>onselect</b>.
     *
     * @param onselect
     */
    public void setOnselect(String onselect) {
        this.onselect_ = onselect;
    }
    /**
     * Gets the String property <b>align</b>.
     *
     * @return String
     */
    public String getAlign() {
        return (align_);
    }
    /**
     * Sets the String property <b>align</b>.
     *
     * @param align
     */
    public void setAlign(String align) {
        this.align_ = align;
    }
}
