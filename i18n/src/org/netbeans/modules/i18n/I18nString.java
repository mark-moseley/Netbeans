/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

import org.openide.loaders.DataObject;
import org.openide.util.MapFormat;


/**
 * This object represent i18n values which will be used by actual
 * i18n-izing of found hard coded string. I.e. resource where will be stored 
 * new key-value pair, actual key-value pair and replace code wich will
 * replace found hard coded string.
 * <p>
 * It also prescribes that each subclass MUST have <b>copy constuctor</b>
 * calling its superclass copy constructor. The copy constructor MUST be  then 
 * called during <b>cloning</b>. All subclasses must also support oposite
 * process <b>becoming</b>
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nString {

    /** 
     * Support for this i18n string istance. 
     * It contains implementation.
     */
    protected I18nSupport support;
    
    /** The key value according the hard coded string will be i18n-ized. */
    protected String key;
    
    /** The "value" value which will be stored to resource. */
    protected String value;
    
    /** Comment for key-value pair stored in resource. */
    protected String comment;
    
    /** Replace format. */
    protected String replaceFormat;

    
    /** 
     * Creates new I18nString. 
     * @param support <code>I18nSupport</code> linked to this instance,
     * has to be non-null 
     */
    protected I18nString(I18nSupport support) {
        if(support == null) throw new NullPointerException();

        this.support = support;
        
        //??? what is this
        replaceFormat = I18nUtil.getOptions().getReplaceJavaCode();
    }

    /**
     * Copy contructor.
     */
    protected  I18nString(I18nString copy) {
        this.key = copy.key;
        this.value = copy.value;
        this.comment = copy.comment;
        this.replaceFormat = copy.replaceFormat;
        this.support = copy.support;
    }
    
    /**
     * Let this instance take its state from passed one.
     * All subclasses must extend it.
     */
    public void become(I18nString copy) {
        this.key = copy.key;
        this.value = copy.value;
        this.comment = copy.comment;
        this.replaceFormat = copy.replaceFormat;
        this.support = copy.support;
    }
    
    /**
     * Cloning must use copy contructors.
     */
    public Object clone() {
        return new I18nString(this);
    }
    
    /** Getter for <code>support</code>. */
    public I18nSupport getSupport() {
        return support;
    }
    
    /** Getter for <code>key</code>. */
    public String getKey() {
        return key;
    }

    /** Setter for <code>key</code>. */
    public void setKey(String key) {
        if(this.key == key || (this.key != null && this.key.equals(key)))
            return;

        this.key = key;
    }
    
    /** Getter for <code>value</code>. */
    public String getValue() {
        return value;
    }

    /** Setter for <code>value</code>. */
    public void setValue(String value) {
        if(this.value == value || (this.value != null && this.value.equals(value)))
            return;

        this.value = value;
    }

    /** Getter for <code>comment</code>. */
    public String getComment() {
        return comment;
    }

    /** Setter for <code>comment</code>. */
    public void setComment(String comment) {
        if(this.comment == comment || (this.comment != null && this.comment.equals(comment)))
            return;

        this.comment = comment;
    }

    /** Getter for replace format property. */
    public String getReplaceFormat() {
        return replaceFormat;
    }
    
    /** Setter for replace format property. */
    public void setReplaceFormat(String replaceFormat) {
        this.replaceFormat = replaceFormat;
    }

    /** 
     * Derive replacing string.
     * @return replacing string or null if this instance is invalid 
     */
    public String getReplaceString() {
        if(getKey() == null || getSupport() == null || getSupport().getResourceHolder().getResource() == null)
            return null;
        
        if(replaceFormat == null)
            replaceFormat = I18nUtil.getOptions().getReplaceJavaCode();

        // Create map.
        
        FileObject fo = getSupport().getResourceHolder().getResource().getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath( fo, ClassPath.SOURCE );
        
        Map map = new HashMap(4);

        map.put("key", getKey()); // NOI18N
        map.put("bundleNameSlashes", cp.getResourceName( fo, '/', false ) ); // NOI18N
        map.put("bundleNameDots", cp.getResourceName( fo, '.', false ) ); // NOI18N

        DataObject sourceDataObject = getSupport().getSourceDataObject();
        map.put("sourceFileName", sourceDataObject == null ? "" : sourceDataObject.getPrimaryFile().getName()); // NOI18N

        return MapFormat.format(replaceFormat, map);
    }
}
