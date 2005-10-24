/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Implementation of ClassPathImplementation which represents the Web Pages folder.
 *
 * @author Andrei Badea
 */
final class JspSourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List resources;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    private ProjectDirectoryListener projectDirListener;

    /**
     * Construct the implementation.
     */
    public JspSourcePathImplementation(AntProjectHelper helper, PropertyEvaluator eval) {
        assert helper != null;
        assert eval != null;
        this.helper = helper;
        this.evaluator = eval;
        eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        FileObject projectDir = helper.getProjectDirectory();
        projectDirListener = new ProjectDirectoryListener();
        projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(projectDirListener, projectDir));
    }

    public List /*<PathResourceImplementation>*/ getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return resources;
            }
        }
        PathResourceImplementation webDocbaseDirRes = null;
        String webDocbaseDir = evaluator.getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
        if (webDocbaseDir != null) {
            FileObject webDocbaseDirFO = helper.resolveFileObject(webDocbaseDir);
            if (webDocbaseDirFO != null) {
                try {
                    webDocbaseDirRes = ClassPathSupport.createResource(webDocbaseDirFO.getURL());
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        synchronized (this) {
            if (this.resources == null) {
                List result = null;
                if (webDocbaseDirRes != null) {
                    this.resources = Collections.singletonList(webDocbaseDirRes);
                } else {
                    this.resources = Collections.EMPTY_LIST;
                }
            }
        }
        return this.resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (WebProjectProperties.WEB_DOCBASE_DIR.equals(evt.getPropertyName())) {
            fireChange();
        }
    }
    
    private void fireChange() {
        synchronized (this) {
            this.resources = null;
        }
        this.support.firePropertyChange (PROP_RESOURCES,null,null);
    }
    
    private final class ProjectDirectoryListener implements FileChangeListener {

        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                fireChange();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                fireChange();
            }
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            if (isWatchedFile(getFileName(fe)) || isWatchedFile(getOldFileName(fe))) {
                fireChange();
            }
        }

        private boolean isWatchedFile(String fileName) {
            String webDir = evaluator.getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
            return fileName.equals(webDir);
        }

        private String getFileName(FileEvent fe) {
            return fe.getFile().getNameExt();
        }

        private String getOldFileName(FileRenameEvent fe) {
            String result = fe.getName();
            if (fe.getExt() != "") { // NOI18N
                result = result + "." + fe.getExt(); // NOI18N
            }
            return result;
        }
    }
}
