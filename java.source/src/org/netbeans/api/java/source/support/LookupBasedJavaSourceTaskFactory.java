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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**A {@link JavaSourceTaskFactorySupport} that registers tasks to all files that are
 * found in the given {@link Lookup}.
 *
 * This factory searches for {@link FileObject}, {@link DataObject} and {@link Node}
 * in the lookup. If {@link Node}(s) are found, its/their lookup is searched for
 * {@link FileObject} and {@link DataObject}.
 *
 * @author Jan Lahoda
 */
public abstract class LookupBasedJavaSourceTaskFactory extends JavaSourceTaskFactory {

    private Result<FileObject> fileObjectResult;
    private Result<DataObject> dataObjectResult;
    private Result<Node> nodeResult;
    
    private List<FileObject> currentFiles;
    private LookupListener listener;
    
    private String[] supportedMimeTypes;

    /**Construct the LookupBasedJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public LookupBasedJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String[]) null);
    }
    
    /**Construct the LookupBasedJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @since 0.21
     */
    public LookupBasedJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority);
        currentFiles = Collections.emptyList();
        listener = new LookupListenerImpl();
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }

    /**Sets a new {@link Lookup} to search.
     *
     * @param lookup new {@link Lookup}
     */
    protected synchronized final void setLookup(Lookup lookup) {
        if (fileObjectResult != null) {
            fileObjectResult.removeLookupListener(listener);
        }
        if (dataObjectResult != null) {
            dataObjectResult.removeLookupListener(listener);
        }
        if (nodeResult != null) {
            nodeResult.removeLookupListener(listener);
        }
        fileObjectResult = lookup.lookupResult(FileObject.class);
        dataObjectResult = lookup.lookupResult(DataObject.class);
        nodeResult = lookup.lookupResult(Node.class);

        fileObjectResult.addLookupListener(listener);
        dataObjectResult.addLookupListener(listener);
        nodeResult.addLookupListener(listener);

        updateCurrentFiles();
        fileObjectsChanged();
    }

    private synchronized void updateCurrentFiles() {
        Set<FileObject> newCurrentFiles = new HashSet<FileObject>();

        newCurrentFiles.addAll(fileObjectResult.allInstances());

        for (DataObject d : dataObjectResult.allInstances()) {
            newCurrentFiles.add(d.getPrimaryFile());
        }

        for (Node n : nodeResult.allInstances()) {
            newCurrentFiles.addAll(n.getLookup().lookupAll(FileObject.class));

            for (DataObject d : n.getLookup().lookupAll(DataObject.class)) {
                newCurrentFiles.add(d.getPrimaryFile());
            }
        }

        currentFiles = OpenedEditors.filterSupportedMIMETypes(new LinkedList<FileObject>(newCurrentFiles), supportedMimeTypes);
        
        lookupContentChanged();
    }
    
    /**@inheritDoc*/
    public synchronized List<FileObject> getFileObjects() {
        return currentFiles;
    }

    /**This method is called when the provided Lookup's content changed.
     * Subclasses may override this method in order to be notified about such change.
     */
    protected void lookupContentChanged() {
    }

    private class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            updateCurrentFiles();
            fileObjectsChanged();
        }
    }

}
