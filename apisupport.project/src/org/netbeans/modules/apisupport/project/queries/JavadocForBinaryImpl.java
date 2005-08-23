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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Defines Javadoc locations for built modules.
 * @author Jesse Glick
 */
public final class JavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final String NB_ALL_INFIX = "nbbuild" + File.separatorChar + "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    private static final String EXT_INFIX = "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    
    /** Configurable for the unit test, since it is too cumbersome to create fake Javadoc in all the right places. */
    static boolean ignoreNonexistentRoots = true;
    
    private final NbModuleProject project;
    
    public JavadocForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        if (!binaryRoot.equals(Util.urlForJar(project.getModuleJarLocation()))) {
            return null;
        }
        String cnbdashes = project.getCodeNameBase().replace('.', '-');
        try {
            final List/*<URL>*/ candidates = new ArrayList();
            NbPlatform platform = project.getPlatform();
            URL[] roots = platform.getJavadocRoots();
            for (int i = 0; i < roots.length; i++) {
                candidates.add(new URL(roots[i], cnbdashes + "/")); // NOI18N
            }
            File dir;
            NbModuleTypeProvider typeProv = (NbModuleTypeProvider) project.getLookup().lookup(NbModuleTypeProvider.class);
            if (typeProv.getModuleType() == NbModuleTypeProvider.NETBEANS_ORG) {
                dir = project.getNbrootFile(NB_ALL_INFIX + cnbdashes);
            } else {
                dir = new File(FileUtil.toFile(project.getProjectDirectory()), EXT_INFIX + cnbdashes);
            }
            candidates.add(Util.urlForDir(dir));
            if (ignoreNonexistentRoots) {
                Iterator it = candidates.iterator();
                while (it.hasNext()) {
                    URL u = (URL) it.next();
                    if (URLMapper.findFileObject(u) == null) {
                        it.remove();
                    }
                }
            }
            return new JavadocForBinaryQuery.Result() {
                public URL[] getRoots() {
                    return (URL[]) candidates.toArray(new URL[candidates.size()]);
                }
                public void addChangeListener(ChangeListener l) {}
                public void removeChangeListener(ChangeListener l) {}
            };
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
