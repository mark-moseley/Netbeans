/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        if (support == null) {
            throw new NullPointerException();
        }

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
    @Override
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
        if ((this.key == key) || ((this.key != null) && this.key.equals(key))) {
            return;
        }

        this.key = key;
    }
    
    /** Getter for <code>value</code>. */
    public String getValue() {
        return value;
    }

    /** Setter for <code>value</code>. */
    public void setValue(String value) {
        if ((this.value == value) || ((this.value != null) && (this.value.equals(value)))) {
            return;
        }

        this.value = value;
    }

    /** Getter for <code>comment</code>. */
    public String getComment() {
        return comment;
    }

    /** Setter for <code>comment</code>. */
    public void setComment(String comment) {
        if ((this.comment == comment) || ((this.comment != null) && (this.comment.equals(comment)))) {
            return;
        }

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
     * Derive replacing string. The method substitutes parameters into
     * a format string using <code>MapFormat.format</code>. If you
     * override this method, you must not call <code>.format</code>
     * on the return value because values substituted in the previous 
     * round can contain control codes. All replacements
     * must take place simultaneously in a single, the first, call. Thus, if you
     * need to substitute some additional parameters not substituted by
     * default, use 
     * the provided hook {@link #fillFormatMap}.
     * 
     * @return replacing string or null if this instance is invalid 
     */
    public String getReplaceString() {
        if (getKey() == null
                || getSupport() == null
                || getSupport().getResourceHolder().getResource() == null) {
            return null;
        }
        
        if (replaceFormat == null) {
            replaceFormat = I18nUtil.getOptions().getReplaceJavaCode();
        }

        // Create map.
        
        DataObject sourceDataObject = getSupport().getSourceDataObject();

        FileObject fo = getSupport().getResourceHolder().getResource().getPrimaryFile();
        ClassPath cp = Util.getExecClassPath(sourceDataObject.getPrimaryFile(), fo); 
        Map<String,String> map = new HashMap<String,String>(4);

        map.put("key", getKey()); // NOI18N
        map.put("bundleNameSlashes", cp.getResourceName( fo, '/', false ) ); // NOI18N
        map.put("bundleNameDots", cp.getResourceName( fo, '.', false ) ); // NOI18N
        map.put("sourceFileName", sourceDataObject == null ? "" : sourceDataObject.getPrimaryFile().getName()); // NOI18N

        fillFormatMap(map);

        return MapFormat.format(replaceFormat, map);
    }

    /**
     * Hook for filling in additional format key/value pair in
     * subclasses. Within the method, the provided substituion map can
     * be arbitrarilly modified.
     * @param subst Map to be filled in with key/value pairs
     */ 
    protected void fillFormatMap(Map<String,String> subst) {
    }
}
