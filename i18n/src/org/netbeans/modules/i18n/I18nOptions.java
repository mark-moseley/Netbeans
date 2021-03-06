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


import java.io.File;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;

/**
 * Options for i18n module.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptions {
    private static final I18nOptions INSTANCE = new I18nOptions();

    /** Property name for init java code.
     * Format for code which initializes generated resource bundle field in java source. */
    public static final String PROP_INIT_JAVA_CODE = "initJavaCode"; // NOI18N
    
    /** Property name for replacing java code.
     * Format for actual i18n-ized code which replaces found non-i18n-ized hardcoded string. */
    public static final String PROP_REPLACE_JAVA_CODE = "replaceJavaCode"; // NOI18N
    
    /** Property name for regular expression for finding non-i18n strings.
     * Regular expression format which is used for deciding whether found hardcoded string is non-i18n-ized. 
     * If line with found hardcoded string doesn't satisfy the expression it's non-i18n-ized. */
    public static final String PROP_REGULAR_EXPRESSION = "regularExpression"; // NOI18N

    /** Property name for regular expression for finding i18n strings.
     * Regular expression format which is used for deciding whether found hardcoded string is i18n-ized. 
     * If line with found hardcoded string satisfies the expression it's i18n-ized. */
    public static final String PROP_I18N_REGULAR_EXPRESSION = "i18nRegularExpression"; // NOI18N

    /** Property name for replace rseource value. 
     * Indicates wheter values in resources for existing keys has to be replacesed or kept the old ones. */
    public static final String PROP_REPLACE_RESOURCE_VALUE = "replaceResourceValue"; // NOI18N
    
    /** Property name for last used resource data object.
     * Hidden property which serializes last resource data object used by i18n module. */
    public static final String PROP_LAST_RESOURCE2 = "lastResource2"; // NOI18N

    
    /** Provided due exeternaliazation only. 
     * Don't create this object directly use superclass <code>findObject</code> method instead. */
    private I18nOptions() {}

    public static I18nOptions getDefault() {
        return INSTANCE;
    }

    private  static Preferences getPreferences() {
        return NbPreferences.forModule(I18nOptions.class);
    }    
    
    /** Implements superclass abstract method. */
    public String displayName() {
        return I18nUtil.getBundle().getString("LBL_Internationalization");
    }
    
    /** Getter for init java code property. */
    public String getInitJavaCode() {
        return getPreferences().get(PROP_INIT_JAVA_CODE,
                                    I18nUtil.getInitFormatItems().get(0));
    }

    /** Setter for init java code property. */
    public void setInitJavaCode(String initJavaCode) {
        getPreferences().put(PROP_INIT_JAVA_CODE,
                             initJavaCode);
    }    
    
    /** Getter for replace java code property. */
    public String getReplaceJavaCode() {
        // Lazy init.
        return getPreferences().get(PROP_REPLACE_JAVA_CODE,
                                    I18nUtil.getDefaultReplaceFormat(false));
    }

    /** Setter for replace java code property. */
    public void setReplaceJavaCode(String replaceJavaCode) {
        getPreferences().put(PROP_REPLACE_JAVA_CODE, replaceJavaCode);
    }    

    /** Getter for regular expression property. 
     * @see #PROP_REGULAR_EXPRESSION */
    public String getRegularExpression() {
        return getPreferences().get(PROP_REGULAR_EXPRESSION,
                                    I18nUtil.getRegExpItems().get(0));        
    }

    /** Setter for regular expression property. 
     * @see #PROP_REGULAR_EXPRESSION */
    public void setRegularExpression(String regExp) {
        getPreferences().put(PROP_REGULAR_EXPRESSION, regExp);
    }    
    
    /** Getter for i18n regular expression property. 
     * @see #PROP_I18N_REGULAR_EXPRESSION */
    public String getI18nRegularExpression() {
        return getPreferences().get(PROP_I18N_REGULAR_EXPRESSION,
                                    I18nUtil.getI18nRegExpItems().get(0));        
    }

    /** Setter for i18n regular expression property. 
     * @see #PROP_I18N_REGULAR_EXPRESSION */
    public void setI18nRegularExpression(String regExp) {
        getPreferences().put(PROP_I18N_REGULAR_EXPRESSION, regExp);
    }    

    /** Getter for replace resource value property. */
    public boolean isReplaceResourceValue() {
        return getPreferences().getBoolean(PROP_REPLACE_RESOURCE_VALUE,
                                           false);        
    }

    /** Setter for replacve resource value property. */
    public void setReplaceResourceValue(boolean replaceResourceValue) {
        getPreferences().putBoolean(PROP_REPLACE_RESOURCE_VALUE,
                                    replaceResourceValue);
    }
    
    /** Getter for last resource property. */
    public DataObject getLastResource2() {
        String path = getPreferences().get(PROP_LAST_RESOURCE2,null);
        FileObject f = (path != null) ? findFileObject(path) : null;
        if ((f != null) && !f.isFolder() && f.isValid()) {
            try {
                return DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                /* The file was probably deleted or moved. */
                getPreferences().remove(PROP_LAST_RESOURCE2);
            }
        }
        return null;
    }
    
    /** Setter for last resource property. */
    public void setLastResource2(DataObject lastResource) {
        // Make sure it is sane.        
        if(lastResource == null)
            return;
        
        getPreferences().put(PROP_LAST_RESOURCE2,
                             lastResource.getPrimaryFile().getPath());
    }
    
    /** Get context help for this system option.
    * @return context help
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (I18nOptions.class);
    }
    
    private static FileSystem[] getFileSystems() {
        List<FileSystem> retval = new ArrayList<FileSystem>();
        for (File file : File.listRoots()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    retval.add(fo.getFileSystem());
                } catch (FileStateInvalidException ex) {
                        Logger.getLogger(I18nOptions.class.getName())
                              .log(Level.INFO, null, ex);
                }
            }
        }        
        return retval.toArray(new FileSystem[retval.size()]);
    }
    
    private static FileObject findFileObject(String path) {
        for (FileSystem fileSystem : getFileSystems()) {
            FileObject retval = fileSystem.findResource(path);
            if (retval != null) {
                return retval;
            }
        }
        return null;
    }

    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode(I18nOptions.getDefault());
    }             
}
