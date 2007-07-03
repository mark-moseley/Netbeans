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

package org.netbeans.api.gsfpath.queries;

import java.net.URL;
import java.util.Arrays;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.gsfpath.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * A query to find Javadoc root for the given classpath root.
 * @author David Konecny, Jesse Glick
 * @since org.netbeans.api.gsfpath/1 1.4
 */
public class JavadocForBinaryQuery {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavadocForBinaryQuery.class.getName());
    
    private static final Lookup.Result<? extends JavadocForBinaryQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(JavadocForBinaryQueryImplementation.class);

    private JavadocForBinaryQuery () {
    }

    /**
     * Find Javadoc information for a classpath root containing Java classes.
     * <p>
     * These methods calls findJavadoc method on the JavadocForBinaryQueryImplementation 
     * instances registered in the lookup until null result is returned for given binaryRoot. The
     * non null result is returned.
     * </p>
     * @param binary URL of a classpath root
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findJavadoc(URL binary) {
        if (FileUtil.isArchiveFile(binary)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+binary); // NOI18N
        }
        boolean log = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        if (log) ERR.log("JFBQ.findJavadoc: " + binary);
        for  (JavadocForBinaryQueryImplementation impl : implementations.allInstances()) {
            Result r = impl.findJavadoc(binary);
            if (r != null) {
                if (log) ERR.log("  got result " + Arrays.asList(r.getRoots()) + " from " + impl);
                return r;
            } else {
                if (log) ERR.log("  got no result from " + impl);
            }
        }
        if (log) ERR.log("  got no results from any impl");
        return EMPTY_RESULT;        
    }

    /**
     * Result of finding Javadoc, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the Javadoc roots.
         * Each root should contain the main <code>index.html</code>, so that
         * for a class <samp>pkg.Class</samp> the generated documentation would
         * have a path <samp>pkg/Class.html</samp> relative to one of the roots.
         * @return array of roots of Javadoc documentation (may be empty but not null)
         */
        URL[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
    private static final Result EMPTY_RESULT = new EmptyResult();
    private static final class EmptyResult implements Result {
        private static final URL[] NO_ROOTS = new URL[0];
        EmptyResult() {}
        public URL[] getRoots() {
            return NO_ROOTS;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }    
    
}
