/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.ErrorManager;


/**
 * Utility class for containing a list of Java sources.
 * @author Richard Michalsky
 */
public class SourceRootsSupport implements SourceRootsProvider {

    private URL[] sourceRoots;
    private SourceRootsProvider delegate;
    private PropertyChangeSupport pcs;
    private List<ModuleList> listsForSources;

    /**
     * Constructs <tt>SourceRootsSupport</tt> object.
     * No property change is fired when setting initial source roots. If a delegate is provided,
     * <tt>getDefaultSourceRoots()<tt> and <i>internal</i> calls to <tt>setSourceRoots()</tt>
     * are passed to delegate. This allows to customize the behavior of SourceRootsSupport.
     *
     * @param sourceRoots Initial source roots.
     * @param pcs Support to use to fire property changes. May be <tt>null</tt>.
     * @param delegate The delegate for routing calls. May be <tt>null</tt>.
     */
    public SourceRootsSupport(URL[] sourceRoots, PropertyChangeSupport pcs, SourceRootsProvider delegate) {
        if (sourceRoots == null)
            throw new NullPointerException("sourceRoots must not be null.");
        this.sourceRoots = sourceRoots;
        this.pcs = pcs;
        this.delegate = delegate;
    }

    private void maybeUpdateDefaultSources() {
        if (sourceRoots.length == 0) {
            URL[] defaults = getDefaultSourceRoots();
            if (defaults != null) {
                sourceRoots = defaults;
                if (pcs != null)
                    pcs.firePropertyChange(SourceRootsProvider.PROP_SOURCE_ROOTS, null, null);
            }
        }
    }

    public URL[] getDefaultSourceRoots() {
        if (delegate == null)
            return null;
        return delegate.getDefaultSourceRoots();
    }

    public void addSourceRoot(URL root) throws IOException {
        maybeUpdateDefaultSources();
        URL[] newSourceRoots = new URL[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[sourceRoots.length] = root;
        setSourceRootsInternal(newSourceRoots);
    }
    
    public File getSourceLocationOfModule(File jar) {
        if (listsForSources == null) {
            List<ModuleList> _listsForSources = new ArrayList<ModuleList>();
            for (URL u : getSourceRoots()) {
                if (!u.getProtocol().equals("file")) { // NOI18N
                    continue;
                }
                File dir = new File(URI.create(u.toExternalForm()));
                if (dir.isDirectory()) {
                    try {
                        if (ModuleList.isNetBeansOrg(dir)) {
                            _listsForSources.add(ModuleList.findOrCreateModuleListFromNetBeansOrgSources(dir));
                        } else {
                            _listsForSources.add(ModuleList.findOrCreateModuleListFromSuiteWithoutBinaries(dir));
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
            listsForSources = _listsForSources;
        }
        for (ModuleList l : listsForSources) {
            for (ModuleEntry entry : l.getAllEntriesSoft()) {
                // XXX should be more strict (e.g. compare also clusters)
                if (!entry.getJarLocation().getName().equals(jar.getName())) {
                    continue;
                }
                File src = entry.getSourceLocation();
                if (src != null && src.isDirectory()) {
                    return src;
                }
            }
            for (ModuleEntry entry : l.getAllEntries()) {
                if (!entry.getJarLocation().getName().equals(jar.getName())) {
                    continue;
                }
                File src = entry.getSourceLocation();
                if (src != null && src.isDirectory()) {
                    return src;
                }
            }
        }
        return null;
    }

    public URL[] getSourceRoots() {
        maybeUpdateDefaultSources();
        return sourceRoots;
    }

    public void removeSourceRoots(URL[] urlsToRemove) throws IOException {
        maybeUpdateDefaultSources();
        Collection<URL> newSources = new ArrayList<URL>(Arrays.asList(sourceRoots));
        newSources.removeAll(Arrays.asList(urlsToRemove));
        URL[] sources = new URL[newSources.size()];
        setSourceRootsInternal(newSources.toArray(sources));
    }

    private void setSourceRootsInternal(URL[] roots) throws IOException {
        if (delegate != null)
            delegate.setSourceRoots(roots);
        else
            setSourceRoots(roots);
    }
    
    public void setSourceRoots(URL[] roots) throws IOException {
        sourceRoots = roots;
        if (pcs != null)
            pcs.firePropertyChange(SourceRootsProvider.PROP_SOURCE_ROOTS, null, null);
        listsForSources = null;
    }

    public void moveSourceRootUp(int indexToUp) throws IOException {
        maybeUpdateDefaultSources();
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToUp - 1] = sourceRoots[indexToUp];
        newSourceRoots[indexToUp] = sourceRoots[indexToUp - 1];
        setSourceRootsInternal(newSourceRoots);
    }

    public void moveSourceRootDown(int indexToDown) throws IOException {
        maybeUpdateDefaultSources();
        if (indexToDown >= (sourceRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToDown + 1] = sourceRoots[indexToDown];
        newSourceRoots[indexToDown] = sourceRoots[indexToDown + 1];
        setSourceRootsInternal(newSourceRoots);
    }
    
}
