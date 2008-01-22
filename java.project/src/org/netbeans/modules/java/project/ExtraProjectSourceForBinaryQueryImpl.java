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
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public final class ExtraProjectSourceForBinaryQueryImpl extends ProjectOpenedHook implements SourceForBinaryQueryImplementation {

    private static final String REF_START = "file.reference."; //NOI18N
    private static final String SOURCE_START = "source.reference."; //NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private Map<URL,ExtraResult>  cache = new HashMap<URL,ExtraResult>();
    private PropertyChangeListener listener;
    private Map<URL, URI> mappings = new HashMap<URL, URI>();

    public ExtraProjectSourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || evt.getPropertyName().startsWith(SOURCE_START)) {
                    mappings = getExtraSources();
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
     * 
     * returns a result even if only the binary root is found in the project.
     * only returns null when the binary root is missing from project altogether.
     * @param binaryRoot
     * @return
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
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
        mappings = getExtraSources();
        evaluator.addPropertyChangeListener(listener);
    }

    @Override
    protected void projectClosed()   {
        mappings = new HashMap<URL, URI>();
        evaluator.removePropertyChangeListener(listener);
    }
    

    Map<URL, URI> getExtraSources() {
        Map<URL, URI> result = new HashMap<URL, URI>();
        Map<String, String> props = evaluator.getProperties();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (entry.getKey().startsWith(REF_START)) {
                String val = entry.getKey().substring(REF_START.length());
                String sourceKey = SOURCE_START + val;
                String source = props.get(sourceKey);
                File bin = PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), entry.getValue());
                try {
                    URL binURL = bin.toURI().toURL();
                    if (FileUtil.isArchiveFile(binURL)) {
                        binURL = FileUtil.getArchiveRoot(binURL);
                    }
                    if (source != null) {
                        File src = PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), source);
                        result.put(binURL, src.toURI());
                    } else {
                        result.put(binURL, null);
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return result;
    }
    
    private class ExtraResult implements SourceForBinaryQuery.Result {
        private URL binaryroot;
        private ChangeSupport chs = new ChangeSupport(this);
        
        public ExtraResult(URL binary) {
            binaryroot = binary;
        }

        public FileObject[] getRoots() {
            URI source = mappings.get(binaryroot);
            if (source != null) {
                try
                {
                    URL url = source.toURL();
                    if (FileUtil.isArchiveFile(url)) {
                        url = FileUtil.getArchiveRoot(url);
                    }
                    FileObject fo = URLMapper.findFileObject(url);
                    if ( fo != null )
                    {
                        return new FileObject[]{fo};
                    }
                } catch ( MalformedURLException ex )
                {
                    Exceptions.printStackTrace( ex );
                }
            }
            return new FileObject[0];
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
