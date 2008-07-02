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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.project.support.ant.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public final class ExtraProjectJavadocForBinaryQueryImpl extends ProjectOpenedHook implements JavadocForBinaryQueryImplementation {

    private static final String REF_START = "file.reference."; //NOI18N
    private static final String JAVADOC_START = "javadoc.reference."; //NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private Map<URL,ExtraResult>  cache = new HashMap<URL,ExtraResult>();
    private PropertyChangeListener listener;
    private Map<URL, URI> mappings = new HashMap<URL, URI>();
    private final Object MAPPINGS_LOCK = new Object();
    private Project project;
    

    public ExtraProjectJavadocForBinaryQueryImpl(Project prj, AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        project = prj;
        
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || evt.getPropertyName().startsWith(JAVADOC_START)) {
                    checkAndRegisterExtraJavadoc(getExtraSources());
                    Collection<ExtraResult> results = null;
                    synchronized (cache) {
                        results = new ArrayList<ExtraResult>(cache.values());
                    }
                    for (ExtraResult res : results) {
                        res.fire();
                    }
                }
            }
            
        };
        
    }

    /**
     * return null only if the javadoc and also the binary url are missing in project..
     * @param binaryRoot
     * @return
     */
    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        synchronized (cache) {
            ExtraResult res = cache.get(binaryRoot);
            if (res != null) {
                return res;
            }
            if (mappings.containsKey(binaryRoot)) {
                res = new ExtraResult(binaryRoot);
                cache.put (binaryRoot, res);
                return res;
            }
        }
        return null;
    }
    
    @Override
    protected void projectOpened() {
        checkAndRegisterExtraJavadoc(getExtraSources());
        evaluator.addPropertyChangeListener(listener);
    }

    @Override
    protected void projectClosed()
    {
        checkAndRegisterExtraJavadoc(new HashMap<URL, URI>());
        evaluator.removePropertyChangeListener(listener);
    }
    

    private Map<URL, URI> getExtraSources() {
        Map<URL, URI> result = new HashMap<URL, URI>();
        Map<String, String> props = evaluator.getProperties();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (entry.getKey().startsWith(REF_START)) {
                String val = entry.getKey().substring(REF_START.length());
                String sourceKey = JAVADOC_START + val;
                String source = props.get(sourceKey);
                File bin = PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), entry.getValue());
                URL binURL = FileUtil.urlForArchiveOrDir(bin);
                if (source != null && binURL != null) {
                    File src = PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), source);
                    // #138349 - ignore non existing paths or entries with undefined IDE variables
                    if (src.exists()) {
                        result.put(binURL, src.toURI());
                    }
                }
            }
        }
        return result;
    }
    
    private void checkAndRegisterExtraJavadoc(Map<URL, URI> newvalues) {
        Set<URL> removed;
        Set<URL> added;
        synchronized (MAPPINGS_LOCK) {
            removed = new HashSet<URL>(mappings.keySet());
            removed.removeAll(newvalues.keySet());
            added = new HashSet<URL>(newvalues.keySet());
            added.removeAll(mappings.keySet());
            mappings = newvalues;
        }
                //TODO removing/adding the mapping can cause lost javadoc/source for other open projects..
                //the mappings should be probably static, or there should be a way to trigger recalculations 
                //in other ant projects from here
        
        for (URL rem : removed) {
            synchronized (cache) {
                ExtraResult res = cache.remove(rem);
                if (res != null) {
                    res.fire();
                }
            }
            try {
                URL jaradd = FileUtil.getArchiveFile(rem);
                if (jaradd != null) {
                    rem = jaradd;
                }
                FileOwnerQuery.markExternalOwner(rem.toURI(), null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        for (URL add : added) {
            try {
                URL jaradd = FileUtil.getArchiveFile(add);
                if (jaradd != null) {
                    add = jaradd;
                }
                FileOwnerQuery.markExternalOwner(add.toURI(), project, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
        
    
    private class ExtraResult implements JavadocForBinaryQuery.Result {
        private URL binaryroot;
        private ChangeSupport chs = new ChangeSupport(this);
        
        
        public ExtraResult(URL binary) {
            binaryroot = binary;
        }

        public URL[] getRoots() {
            URI source = mappings.get(binaryroot);
            if (source != null) {
                try
                {
                    URL url = source.toURL();
                    if (FileUtil.isArchiveFile(url)) {
                        url = FileUtil.getArchiveRoot(url);
                    }
                    return new URL[] { url };
                } catch ( MalformedURLException ex )
                {
                    Exceptions.printStackTrace( ex );
                }
            }
            return new URL[0];
        }

        public void fire() {
            chs.fireChange();
        }

        public void addChangeListener(ChangeListener l) {
            chs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            chs.removeChangeListener(l);
        }
        
    }
}
