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

import java.io.File;
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

/**
 * Finds Javadoc (if it is built) corresponding to binaries in J2SE project.
 * @author David Konecny, Jesse Glick
 */
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public JavadocForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL binaryRoot) {
        class R implements JavadocForBinaryQuery.Result {
            public URL[] getRoots() {
                URL result = getJavadoc(binaryRoot, "build.classes.dir", "dist.javadoc.dir");   //NOI18N
                if (result != null) {
                    return new URL[]{result};
                }
                result = getJavadoc(binaryRoot,"dist.jar", "dist.javadoc.dir"); //NOI18N
                if (result != null) {
                    return new URL[]{result};
                }
                return new URL[0];
            }
            public void addChangeListener(ChangeListener l) {
                // XXX not implemented
            }
            public void removeChangeListener(ChangeListener l) {
                // XXX not implemented
            }
        }
        return new R();
    }


    private URL getJavadoc(URL binaryRoot, String binaryProperty, String javadocProperty) {
        try {
            if (FileUtil.getArchiveFile(binaryRoot) != null) {
                binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            }
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile (outDir);
                URL url = f.toURI().toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) {
                    assert !url.toExternalForm().endsWith("/") : f;
                    url = new URL(url.toExternalForm() + "/");
                }
                if (url.equals(binaryRoot) || 
                        binaryRoot.toExternalForm().startsWith(url.toExternalForm())) {
                    String javadocDir = evaluator.getProperty(javadocProperty);
                    if (javadocDir != null) {
                        f = helper.resolveFile(javadocDir);
                        return f.toURI().toURL();
                    }
                }
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return null;
    }
    
}
