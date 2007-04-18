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


package org.netbeans.modules.compapp.test.ui;

import java.beans.PropertyEditor;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * PropertyFileWrapper.java
 *
 * Created on February 13, 2006, 5:10 PM
 *
 * @author Bing Lu
 */
public class PropertyFileWrapper {
    private static final java.util.logging.Logger mLogger = 
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.PropertyFileWrapper"); // NOI18N
    
    private List mPropertySpecList;
    private FileObject mFileObject;
    private EditableProperties mProperties;
    private Sheet mSheet;
    
    /** Creates a new instance of PropertyFileWrapper */
    public PropertyFileWrapper(FileObject fileObject, List propertySpecList) {
        mFileObject = fileObject;
        mPropertySpecList = propertySpecList;
        mProperties = new EditableProperties(true);
    }
    
    public void loadProperties() {
        InputStream is = null;
        try {
            mProperties.clear();
            is = mFileObject.getInputStream();
            mProperties.load(is);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, 
                    NbBundle.getMessage(PropertyFileWrapper.class,
                                        "LBL_Fail_to_load_properties_from_file", // NOI18N
                                         mFileObject.getNameExt()),
                    e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                } 
            } catch (Exception e) {
            }
        }
    }
    
    public Sheet getSheet() {
        if (mSheet != null) {
            return mSheet;
        }
        loadProperties();
        Sheet sheet = new Sheet();
        Sheet.Set ss = null;
        HashMap catTable = new HashMap();
        for (int i = 0, I = mPropertySpecList.size(); i < I; i++) {
            PropertySpec spec = (PropertySpec)mPropertySpecList.get(i);
            String categoryName = spec.getCategoryName();
            if (catTable.containsKey(categoryName)) {
                ss = (Sheet.Set)catTable.get(categoryName);
            } else {
                ss = new Sheet.Set();
                ss.setName(categoryName);
                ss.setDisplayName(spec.getCategoryDisplayName());
                catTable.put(categoryName, ss);
                sheet.put(ss);
            }
            ss.put(new PropertyWrapper(spec));
        }
        mSheet = sheet;
        return sheet;
    }
    
    class PropertyWrapper extends PropertySupport {
        PropertySpec mSpec;
        PropertyEditor mEditor;
        
        public PropertyWrapper(PropertySpec spec) {
            super(spec.getName(), 
                   spec.getType(), 
                   spec.getDisplayName(), 
                   spec.getShortDescription(), 
                   spec.canRead(),
                   spec.canWrite());
            mSpec = spec;
            if (mSpec.hasEditorType()) {
                mEditor = mSpec.getPropertyEditor();
//                mEditor.setValue(getValue());
            } else {
                mEditor = super.getPropertyEditor();
            }
        }
        
        public Object getValue() {
            String s = mProperties.getProperty(mSpec.getName());
            if (s == null) {
                return mSpec.getDefaultValue();
            }
            Class type = mSpec.getType();
            if (s != null && type != String.class) {
                if (type == Boolean.class) {
                    return new Boolean(s);
                } 
                if (type == Integer.class) {
                    return new Integer(s);
                }
                if (type == Double.class) {
                    return new Double(s);
                }
                if (type == Long.class) {
                    return new Long(s);
                } 
            }
            return s;
        }
        
        public void setValue(Object value) {
            try {
                mProperties.put(mSpec.getName(), value.toString());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mProperties.store(baos);
                final byte[] data = baos.toByteArray();
                FileLock lock = mFileObject.lock();
                OutputStream os = mFileObject.getOutputStream(lock);
                try {
                    os.write(data);
                } finally {
                    os.close();
                    lock.releaseLock();
                }                
            } catch (Exception e) {
                mLogger.log(Level.SEVERE, 
                        NbBundle.getMessage(PropertyFileWrapper.class,
                                            "LBL_Fail_to_set_property", // NOI18N
                                             mSpec.getCategoryName() + "." + mSpec.getName()), // NOI18N
                        e);
            }
        }
        
        public PropertyEditor getPropertyEditor() {
            return mEditor;
        }
    }
    
}
