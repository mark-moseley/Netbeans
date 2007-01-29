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
 * <b>Applet</b> is generated from xhtml.rng by Relaxer.
 * This class is derived from:
 *
 * <!-- for programmer
 * <element name="applet">
 *     <ref name="applet.attlist"/>
 *     <!-- No restrictions on mixed content in TREX. -->
 *     <zeroOrMore>
 *       <ref name="param"/>
 *     </zeroOrMore>
 *     <ref name="Flow.model"/>
 *   </element>-->
 * <!-- for javadoc -->
 * <pre> &lt;element name="applet"&gt;
 *     &lt;ref name="applet.attlist"/&gt;
 *     &lt;!-- No restrictions on mixed content in TREX. --&gt;
 *     &lt;zeroOrMore&gt;
 *       &lt;ref name="param"/&gt;
 *     &lt;/zeroOrMore&gt;
 *     &lt;ref name="Flow.model"/&gt;
 *   &lt;/element&gt;</pre>
 *
 * @version xhtml.rng (Tue Apr 20 01:31:08 PDT 2004)
 * @author  Relaxer 1.1b (http://www.relaxer.org)
 */
public class Applet {
    public static final String ALIGN_TOP = "top";
    public static final String ALIGN_MIDDLE = "middle";
    public static final String ALIGN_BOTTOM = "bottom";
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_RIGHT = "right";
    private String id_;
    private String classValue_;
    private String title_;
    private String style_;
    private String alt_;
    private String archive_;
    private String code_;
    private String codebase_;
    private String object_;
    private String height_;
    private String width_;
    private String name_;
    private String align_;
    private String hspace_;
    private String vspace_;

    /**
     * Creates a <code>Applet</code>.
     *
     */
    public Applet() {
        alt_ = "";
        height_ = "";
        width_ = "";
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
     * Gets the String property <b>archive</b>.
     *
     * @return String
     */
    public String getArchive() {
        return (archive_);
    }
    /**
     * Sets the String property <b>archive</b>.
     *
     * @param archive
     */
    public void setArchive(String archive) {
        this.archive_ = archive;
    }
    /**
     * Gets the String property <b>code</b>.
     *
     * @return String
     */
    public String getCode() {
        return (code_);
    }
    /**
     * Sets the String property <b>code</b>.
     *
     * @param code
     */
    public void setCode(String code) {
        this.code_ = code;
    }
    /**
     * Gets the String property <b>codebase</b>.
     *
     * @return String
     */
    public String getCodebase() {
        return (codebase_);
    }
    /**
     * Sets the String property <b>codebase</b>.
     *
     * @param codebase
     */
    public void setCodebase(String codebase) {
        this.codebase_ = codebase;
    }
    /**
     * Gets the String property <b>object</b>.
     *
     * @return String
     */
    public String getObject() {
        return (object_);
    }
    /**
     * Sets the String property <b>object</b>.
     *
     * @param object
     */
    public void setObject(String object) {
        this.object_ = object;
    }
    /**
     * Gets the String property <b>height</b>.
     *
     * @return String
     */
    public String getHeight() {
        return (height_);
    }
    /**
     * Sets the String property <b>height</b>.
     *
     * @param height
     */
    public void setHeight(String height) {
        this.height_ = height;
    }
    /**
     * Gets the String property <b>width</b>.
     *
     * @return String
     */
    public String getWidth() {
        return (width_);
    }
    /**
     * Sets the String property <b>width</b>.
     *
     * @param width
     */
    public void setWidth(String width) {
        this.width_ = width;
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
    /**
     * Gets the String property <b>hspace</b>.
     *
     * @return String
     */
    public String getHspace() {
        return (hspace_);
    }
    /**
     * Sets the String property <b>hspace</b>.
     *
     * @param hspace
     */
    public void setHspace(String hspace) {
        this.hspace_ = hspace;
    }
    /**
     * Gets the String property <b>vspace</b>.
     *
     * @return String
     */
    public String getVspace() {
        return (vspace_);
    }
    /**
     * Sets the String property <b>vspace</b>.
     *
     * @param vspace
     */
    public void setVspace(String vspace) {
        this.vspace_ = vspace;
    }
}
