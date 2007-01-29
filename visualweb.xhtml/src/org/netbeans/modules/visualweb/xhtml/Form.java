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
 * <b>Form</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="form">
 *     <ref name="form.attlist"/>
 *     <!-- Don't use Block.model, because this gets redefined by the
 * 	 legacy module. -->
 *     <oneOrMore>
 *       <ref name="Block.class"/>
 *     </oneOrMore>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="form"&gt;
 *     &lt;ref name="form.attlist"/&gt;
 *     &lt;!-- Don't use Block.model, because this gets redefined by the
 * 	 legacy module. --&gt;
 *     &lt;oneOrMore&gt;
 *       &lt;ref name="Block.class"/&gt;
 *     &lt;/oneOrMore&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Form {
    public static final String DIR_LTR = "ltr";
    public static final String DIR_RTL = "rtl";
    public static final String METHOD_GET = "get";
    public static final String METHOD_POST = "post";
    private String acceptCharset_;
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
    private String action_;
    private String method_;
    private String enctype_;
    private String onreset_;
    private String onsubmit_;
    private String target_;
    private String name_;
    /**
     * Creates a <code>Form</code>.
     *
     */
    public Form() {
        action_ = "";
    }
    /**
     * Gets the String property <b>acceptCharset</b>.
     *
     * @return String
     */
    public String getAcceptCharset() {
        return (acceptCharset_);
    }
    /**
     * Sets the String property <b>acceptCharset</b>.
     *
     * @param acceptCharset
     */
    public void setAcceptCharset(String acceptCharset) {
        this.acceptCharset_ = acceptCharset;
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
     * Gets the String property <b>action</b>.
     *
     * @return String
     */
    public String getAction() {
        return (action_);
    }
    /**
     * Sets the String property <b>action</b>.
     *
     * @param action
     */
    public void setAction(String action) {
        this.action_ = action;
    }
    /**
     * Gets the String property <b>method</b>.
     *
     * @return String
     */
    public String getMethod() {
        return (method_);
    }
    /**
     * Sets the String property <b>method</b>.
     *
     * @param method
     */
    public void setMethod(String method) {
        this.method_ = method;
    }
    /**
     * Gets the String property <b>enctype</b>.
     *
     * @return String
     */
    public String getEnctype() {
        return (enctype_);
    }
    /**
     * Sets the String property <b>enctype</b>.
     *
     * @param enctype
     */
    public void setEnctype(String enctype) {
        this.enctype_ = enctype;
    }
    /**
     * Gets the String property <b>onreset</b>.
     *
     * @return String
     */
    public String getOnreset() {
        return (onreset_);
    }
    /**
     * Sets the String property <b>onreset</b>.
     *
     * @param onreset
     */
    public void setOnreset(String onreset) {
        this.onreset_ = onreset;
    }
    /**
     * Gets the String property <b>onsubmit</b>.
     *
     * @return String
     */
    public String getOnsubmit() {
        return (onsubmit_);
    }
    /**
     * Sets the String property <b>onsubmit</b>.
     *
     * @param onsubmit
     */
    public void setOnsubmit(String onsubmit) {
        this.onsubmit_ = onsubmit;
    }
    /**
     * Gets the String property <b>target</b>.
     *
     * @return String
     */
    public String getTarget() {
        return (target_);
    }
    /**
     * Sets the String property <b>target</b>.
     *
     * @param target
     */
    public void setTarget(String target) {
        this.target_ = target;
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
}
