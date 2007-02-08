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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyledDocument;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 */
public class JWSProjectProperties {
    
    public static final String JNLP_ENABLED      = "jnlp.enabled";
    //public static final String JNLP_DESC_SHORT   = "jnlp.desc.short";
    public static final String JNLP_ICON         = "jnlp.icon";
    public static final String JNLP_OFFLINE      = "jnlp.offline-allowed";
    public static final String JNLP_CBASE_TYPE   = "jnlp.codebase.type";
    public static final String JNLP_CBASE_USER   = "jnlp.codebase.user";
    public static final String JNLP_CBASE_URL    = "jnlp.codebase.url";
    
    public static final String JNLP_SPEC         = "jnlp.spec";
    public static final String JNLP_INIT_HEAP    = "jnlp.initial-heap-size";
    public static final String JNLP_MAX_HEAP     = "jnlp.max-heap-size";
    
    public static final String JNLP_SIGN_ENABLED = "jnlp.signing.enabled";
    
    public static final String CB_TYPE_LOCAL = "local";
    public static final String CB_TYPE_WEB = "web";
    public static final String CB_TYPE_USER = "user";
    
    public static final String CB_URL_WEB = "$$codebase";
    
    // special value to persist Ant script handling
    public static final String CB_URL_WEB_PROP_VALUE = "$$$$codebase";
    
    private StoreGroup jnlpPropGroup = new StoreGroup();
    
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project j2seProject;
    
    // Models 
    JToggleButton.ToggleButtonModel enabledModel;
    JToggleButton.ToggleButtonModel allowOfflineModel;
    ComboBoxModel codebaseModel;
    
    // and Documents
    Document iconDocument;
    Document codebaseURLDocument;
    
    /** Creates a new instance of JWSProjectProperties */
    public JWSProjectProperties(Lookup context) {
        
        j2seProject = (Project) context.lookup(Project.class);
        if (j2seProject != null) {
            j2sePropEval = (J2SEPropertyEvaluator) j2seProject.getLookup().lookup(J2SEPropertyEvaluator.class);
        } else {
            // XXX
        }
        
        evaluator = j2sePropEval.evaluator();
        
        enabledModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_ENABLED);
        allowOfflineModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_OFFLINE);
        iconDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_ICON);
        
        codebaseModel = new CodebaseComboBoxModel();
        codebaseURLDocument = createCBTextFieldDocument();
        
    }
    
    private void storeRest(EditableProperties editableProps) {
        String selItem = ((CodebaseComboBoxModel) codebaseModel).getSelectedCodebaseItem();
        String propName = null;
        String propValue = null;
        if (CB_TYPE_USER.equals(selItem)) {
            propName = JNLP_CBASE_USER;
            try {
                propValue = codebaseURLDocument.getText(0, codebaseURLDocument.getLength());
            } catch (BadLocationException ex) {
                // do not store anything
                // XXX log the exc
                return;
            }
        } else if (CB_TYPE_LOCAL.equals(selItem)) {
            propName = JNLP_CBASE_URL;
            propValue = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(selItem))  {
            propName = JNLP_CBASE_URL;
            propValue = CB_URL_WEB_PROP_VALUE;
        }
        if (propName == null || propValue == null) {
            return;
        } else {
            editableProps.setProperty(JNLP_CBASE_TYPE, selItem);
            editableProps.setProperty(propName, propValue);
        }
    }
    
    public void store() throws IOException {
        
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = j2seProject.getProjectDirectory().getFileObject("nbproject/project.properties");
        
        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    jnlpPropGroup.store(ep);
                    storeRest(ep);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        } 
        
    }
    
    private Document createCBTextFieldDocument() {
        Document doc = new PlainDocument();
        String valueURL = evaluator.getProperty(JNLP_CBASE_USER);
        String valueType = evaluator.getProperty(JNLP_CBASE_TYPE);
        String docString = "";
        if (CB_TYPE_LOCAL.equals(valueType)) {
            docString = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(valueType)) {
            docString = CB_URL_WEB;
        } else if (CB_TYPE_USER.equals(valueType)) {
            docString = getCodebaseLocation();
        }
        try {
            doc.insertString(0, docString, null);
        } catch (BadLocationException ex) {
            // do nothing, just return PlainDocument
            // XXX log the exc
        }
        return doc;
    }
    /*
    private StyledDocument createDescTextAreaDocument() {
        StyledDocument doc = new DefaultStyledDocument();
        String docString = "";
        docString = evaluator.getProperty(JNLP_DESC);
        try {
            doc.insertString(0, docString, null);
        } catch (BadLocationException ex) {
            // do nothing, just return DefaultStyledDocument
        }
        return doc;
    }
    */
    public String getCodebaseLocation() {
        return evaluator.getProperty(JNLP_CBASE_USER);
    }
        
    public String getProjectDistDir() {
        File distDir = new File(FileUtil.toFile(j2seProject.getProjectDirectory()), evaluator.getProperty("dist.dir"));
        return distDir.toURI().toString();
    }
    
    // only should return JNLP properties
    public String getProperty(String propName) {
        return evaluator.getProperty(propName);
    }
    
    // ----------
    
    public class CodebaseComboBoxModel extends DefaultComboBoxModel {
        
        String localLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_Local");
        String webLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_Web");
        String userLabel = NbBundle.getBundle(JWSProjectProperties.class).getString("LBL_CB_Combo_User");
        Object visItems[] = new Object[] { localLabel, webLabel, userLabel };
        String cbItems[] = new String[] { CB_TYPE_LOCAL, CB_TYPE_WEB, CB_TYPE_USER };
        
        public CodebaseComboBoxModel() {
            super();
            addElement(visItems[0]);
            addElement(visItems[1]);
            addElement(visItems[2]);
            String propValue = evaluator.getProperty(JNLP_CBASE_TYPE);
            if (cbItems[2].equals(propValue)) {
                setSelectedItem(visItems[2]);
            } else if (cbItems[1].equals(propValue)) {
                setSelectedItem(visItems[1]);
            } else {
                setSelectedItem(visItems[0]);
            }
        }
        
        public String getSelectedCodebaseItem() {
            return cbItems[getIndexOf(getSelectedItem())];
        }
        
    }
    
}
