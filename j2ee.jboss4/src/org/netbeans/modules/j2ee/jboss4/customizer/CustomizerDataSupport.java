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

package org.netbeans.modules.j2ee.jboss4.customizer;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.openide.ErrorManager;


/**
 * Customizer data support keeps models for all the customizer components, 
 * initializes them, tracks model changes and performs save.
 *
 * @author sherold
 */
public class CustomizerDataSupport {
    
    // models    
    private DefaultComboBoxModel    jvmModel;
    private Document                javaOptsModel;
    private ButtonModel             proxyModel;
    private CustomizerSupport.PathModel sourceModel;
    private CustomizerSupport.PathModel classModel;
    private CustomizerSupport.PathModel javadocModel;
    
    // model dirty flags    
    private boolean jvmModelFlag;
    private boolean javaOptsModelFlag;
    private boolean proxyModelFlag;
    private boolean sourceModelFlag;
    private boolean javadocModelFlag;
    
    private JBProperties properties;
    
    /**
     * Creates a new instance of CustomizerDataSupport 
     */
    public CustomizerDataSupport(JBProperties properties) {
        this.properties = properties;
        init();
    }
    
    /** Initialize the customizer models. */
    private void init() {
        
        // jvmModel
        jvmModel = new DefaultComboBoxModel();
        loadJvmModel();
        jvmModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                jvmModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
            
            public void intervalAdded(ListDataEvent e) {
            }

            public void intervalRemoved(ListDataEvent e) {
            }
        });
        
        // javaOptions
        javaOptsModel = createDocument(properties.getJavaOpts());
        javaOptsModel.addDocumentListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javaOptsModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // proxyModel
        proxyModel = createToggleButtonModel(properties.getProxyEnabled());
        proxyModel.addItemListener(new ModelChangeAdapter() {
            public void modelChanged() {
                proxyModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // classModel
        classModel = new CustomizerSupport.PathModel(properties.getClasses());
        
        // sourceModel
        sourceModel = new CustomizerSupport.PathModel(properties.getSources());
        sourceModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                sourceModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
        
        // javadocModel
        javadocModel = new CustomizerSupport.PathModel(properties.getJavadocs());
        javadocModel.addListDataListener(new ModelChangeAdapter() {
            public void modelChanged() {
                javadocModelFlag = true;
                store(); // This is just temporary until the server manager has OK and Cancel buttons
            }
        });
    }
    
    /** Update the jvm model */
    public void loadJvmModel() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatformAdapter curJvm = (JavaPlatformAdapter)jvmModel.getSelectedItem();
        String curPlatformName = null;
        if (curJvm != null) {
            curPlatformName = curJvm.getName();
        } else {
            curPlatformName = (String)properties.getJavaPlatform().getProperties().get(JBProperties.PLAT_PROP_ANT_NAME);
        }

        jvmModel.removeAllElements();
        
        // feed the combo with sorted platform list
        JavaPlatform[] j2sePlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
        JavaPlatformAdapter[] platformAdapters = new JavaPlatformAdapter[j2sePlatforms.length];
        for (int i = 0; i < platformAdapters.length; i++) {
            platformAdapters[i] = new JavaPlatformAdapter(j2sePlatforms[i]);
        }
        Arrays.sort(platformAdapters);
        for (int i = 0; i < platformAdapters.length; i++) {
            JavaPlatformAdapter platformAdapter = platformAdapters[i];
            jvmModel.addElement(platformAdapter);
            // try to set selected item
            if (curPlatformName != null) {
                if (curPlatformName.equals(platformAdapter.getName())) {
                    jvmModel.setSelectedItem(platformAdapter);
                }
            }   
        }
    }
    
    // model getters ----------------------------------------------------------
        
    public DefaultComboBoxModel getJvmModel() {
        return jvmModel;
    }
    
    public Document getJavaOptsModel() {
        return javaOptsModel;
    }
    
    public ButtonModel getProxyModel() {
        return proxyModel;
    }
    
    public CustomizerSupport.PathModel getClassModel() {
        return classModel;
    }
    
    public CustomizerSupport.PathModel getSourceModel() {
        return sourceModel;
    }
    
    public CustomizerSupport.PathModel getJavadocsModel() {
        return javadocModel;
    }
    
    // private helper methods -------------------------------------------------
    
    /** Save all changes */
    private void store() {
        
        if (jvmModelFlag) {
            JavaPlatformAdapter platformAdapter = (JavaPlatformAdapter)jvmModel.getSelectedItem();
            properties.setJavaPlatform(platformAdapter.getJavaPlatform());
            jvmModelFlag = false;
        }
        
        if (javaOptsModelFlag) {
            properties.setJavaOpts(getText(javaOptsModel));
            javaOptsModelFlag = false;
        }
        
        if (proxyModelFlag) {
            properties.setProxyEnabled(proxyModel.isSelected());
            proxyModelFlag = false;
        }
        
        if (sourceModelFlag) {
            properties.setSources(sourceModel.getData());
            sourceModelFlag = false;
        }
        
        if (javadocModelFlag) {
            properties.setJavadocs(javadocModel.getData());
            javadocModelFlag = false;
        }
    }
    
    /** Create a Document initialized by the specified text parameter, which may be null */
    private Document createDocument(String text) {
        PlainDocument doc = new PlainDocument();
        if (text != null) {
            try {
                doc.insertString(0, text, null);
            } catch(BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return doc;
    }
    
    /** Get the text value from the document */
    private String getText(Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch(BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /** Create a ToggleButtonModel inilialized by the specified selected parameter. */
    private JToggleButton.ToggleButtonModel createToggleButtonModel(boolean selected) {
        JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
        model.setSelected(selected);
        return model;
    }
        
    // private helper class ---------------------------------------------------
    
    /** 
     * Adapter that implements several listeners, which is useful for dirty model
     * monitoring.
     */
    private abstract class ModelChangeAdapter implements ListDataListener, 
            DocumentListener, ItemListener, ChangeListener {
        
        public abstract void modelChanged();
        
        public void contentsChanged(ListDataEvent e) {
            modelChanged();
        }

        public void intervalAdded(ListDataEvent e) {
            modelChanged();
        }

        public void intervalRemoved(ListDataEvent e) {
            modelChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            modelChanged();
        }

        public void itemStateChanged(ItemEvent e) {
            modelChanged();
        }

        public void stateChanged(javax.swing.event.ChangeEvent e) {
            modelChanged();
        }
    }
    
    /** Java platform combo box model helper */
    private static class JavaPlatformAdapter implements Comparable {
        private JavaPlatform platform;
        
        public JavaPlatformAdapter(JavaPlatform platform) {
            this.platform = platform;
        }
        
        public JavaPlatform getJavaPlatform() {
            return platform;
        }
        
        public String getName() {
            return (String)platform.getProperties().get(JBProperties.PLAT_PROP_ANT_NAME);
        }
        
        public String toString() {
            return platform.getDisplayName();
        }
        
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}
