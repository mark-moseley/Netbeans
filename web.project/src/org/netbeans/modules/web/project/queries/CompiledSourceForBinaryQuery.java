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
package org.netbeans.modules.web.project.queries;

import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Finds sources corresponding to binaries in a J2SE project.
 * @author Jesse Glick, Tomas Zezula
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private AntProjectHelper helper;

    public CompiledSourceForBinaryQuery (AntProjectHelper helper) {
        this.helper = helper;
    }

    public FileObject[] findSourceRoot(URL binaryRoot) {

        FileObject result = getSources(binaryRoot, "build.classes.dir","src.dir");   //NOI18N
        if (result != null)
            return new FileObject[] {result};
        result = getSources (binaryRoot,"dist.jar","src.dir");                             //NOI18N
        if (result != null)
            return new FileObject[] {result};
        return new FileObject[0];
    }


    private FileObject getSources (URL binaryRoot, String binaryProperty, String sourceProperty) {
        try {
            if (binaryRoot.getProtocol().equals("jar")) {  // NOI18N
                // We are interested in the JAR file itself.
                // Note that this impl therefore accepts *both* file:/tmp/foo.jar
                // and jar:file:/tmp/foo.jar!/ as equivalent (like URLClassLoader).
                String surl = binaryRoot.toExternalForm();
                if (surl.endsWith("!/")) { // NOI18N
                    binaryRoot = new URL(surl.substring(4, surl.length() - 2));
                } else if (surl.lastIndexOf("!/") == -1) { // NOI18N
                    // Legal??
                    binaryRoot = new URL(surl.substring(4));
                } else {
                    // Some specific path, e.g. jar:file:/tmp/foo.jar!/foo/,
                    // which we do not support.
                }
            }
            String outDir = helper.getStandardPropertyEvaluator ().getProperty (binaryProperty);
            if (outDir != null) {
                URL url = helper.resolveFile (outDir).toURI().toURL();
                if (url.equals (binaryRoot)) {
                    String srcDir = helper.getStandardPropertyEvaluator ().getProperty (sourceProperty);
                    if (srcDir != null) {
                        return helper.resolveFileObject(srcDir);
                    }
                }
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return null;
    }
    
}
