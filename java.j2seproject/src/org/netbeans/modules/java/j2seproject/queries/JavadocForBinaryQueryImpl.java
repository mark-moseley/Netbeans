/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seproject.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.event.ChangeListener;
import javax.xml.transform.Result;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.WeakListeners;

/**
 * Finds Javadoc (if it is built) corresponding to binaries in J2SE project.
 * @author David Konecny, Jesse Glick
 */
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final String PROP_JAVADOC_DIR = "dist.javadoc.dir";  //NOI18N

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public JavadocForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL binaryRoot) {
        
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener  {
            
            private List listeners;
            private URL[] result;
            
            public R () {
                JavadocForBinaryQueryImpl.this.evaluator.addPropertyChangeListener (WeakListeners.propertyChange(this,JavadocForBinaryQueryImpl.this.evaluator));
            }
            
            public synchronized URL[] getRoots() {
                if (this.result == null) {
                    String javadocDir = evaluator.getProperty(PROP_JAVADOC_DIR);
                    if (javadocDir != null) {
                        File f = helper.resolveFile(javadocDir);
                        try {
                            URL url = f.toURI().toURL();
                            if (!f.exists()) {
                                assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                                url = new URL(url.toExternalForm() + "/"); // NOI18N
                            }
                            this.result = new URL[] {url};
                        } catch (MalformedURLException e) {
                            this.result = new URL[0];
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                    else {
                        this.result = new URL[0];
                    }                
                }
                return this.result;
            }
            public synchronized void addChangeListener(final ChangeListener l) {
                assert l != null;
                if (this.listeners == null) {
                    this.listeners = new ArrayList ();
                }
                this.listeners.add (l);
            }
            public synchronized void removeChangeListener(final ChangeListener l) {
                assert l != null;
                if (this.listeners == null) {
                    return;
                }
                this.listeners.remove (l);
            }
            
            public void propertyChange (final PropertyChangeEvent event) {
                if (PROP_JAVADOC_DIR.equals(event.getPropertyName())) {
                    synchronized (this) {
                        result = null;
                    }
                    this.fireChange ();
                }
            }
            
            private void fireChange () {
                ChangeListener[] _listeners;
                synchronized (this) {
                    if (this.listeners == null) {
                        return;
                    }
                    _listeners = (ChangeListener[]) this.listeners.toArray (new ChangeListener[this.listeners.size()]);
                }
                ChangeEvent event = new ChangeEvent (this);
                for (int i=0; i<_listeners.length; i++) {
                    _listeners[i].stateChanged(event);
                }
            }
        }
        if (isRootOwner(binaryRoot, "build.classes.dir") || isRootOwner (binaryRoot, "dist.jar")) { //NOI18N
            return new R();
        }
        return null;
    }

    private boolean isRootOwner (URL binaryRoot, String binaryProperty) {
        try {
            if (FileUtil.getArchiveFile(binaryRoot) != null) {
                binaryRoot = FileUtil.getArchiveFile(binaryRoot);
                // XXX check whether this is really the root
            }
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile (outDir);
                URL url = f.toURI().toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                return url.equals(binaryRoot) ||
                        binaryRoot.toExternalForm().startsWith(url.toExternalForm());
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return false;
    }

//    private URL getJavadoc(URL binaryRoot, String binaryProperty, String javadocProperty) {
//        try {
//            if (FileUtil.getArchiveFile(binaryRoot) != null) {
//                binaryRoot = FileUtil.getArchiveFile(binaryRoot);
//            }
//            String outDir = evaluator.getProperty(binaryProperty);
//            if (outDir != null) {
//                File f = helper.resolveFile (outDir);
//                URL url = f.toURI().toURL();
//                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) {
//                    assert !url.toExternalForm().endsWith("/") : f;
//                    url = new URL(url.toExternalForm() + "/");
//                }
//                if (url.equals(binaryRoot) ||
//                        binaryRoot.toExternalForm().startsWith(url.toExternalForm())) {
//                    String javadocDir = evaluator.getProperty(javadocProperty);
//                    if (javadocDir != null) {
//                        f = helper.resolveFile(javadocDir);
//                        return f.toURI().toURL();
//                    }
//                }
//            }
//        } catch (MalformedURLException malformedURL) {
//            ErrorManager.getDefault().notify(malformedURL);
//        }
//        return null;
//    }
    
}
