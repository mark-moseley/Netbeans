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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Image;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

class AnnotatedNode extends AbstractNode implements Runnable, FileStatusListener {
    
    private Set files;
    private Set fileSystemListeners;
    private RequestProcessor.Task task;
    private volatile boolean iconChange;
    private volatile boolean nameChange;
    private boolean forceAnnotation;
    
    private FileStatusListener fsl = null;;
    private FileSystem fs = null;
    
    protected AnnotatedNode(Children children) {
        super(children, null);
    }
    
    protected AnnotatedNode(Children children, Lookup lookup) {
        super(children, lookup);
    }
    
    protected final void setFiles(final Set files) {
        if (fs != null && fsl != null)
            fs.removeFileStatusListener(fsl);
        
        this.files = files;
        if (files == null) {
            return;
        }
        if (files.size() == 0) {
            return;
        }
        FileObject fo = (FileObject) files.iterator().next();
        if (fo == null) {
            // See IZ 125880
            return;
        }
        try {
            fs = fo.getFileSystem();
            fsl = FileUtil.weakFileStatusListener(this, fs);
            fs.addFileStatusListener(fsl);
        } catch (FileStateInvalidException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Cannot get " + fo + " filesystem, ignoring...");  // NOI18N
            err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    protected final Set/*<FileObject>*/ getFiles() {
        return files;
    }
    
    protected void setForceAnnotation(boolean forceAnnotation) {
        this.forceAnnotation = forceAnnotation;
    }
    
    protected final Image annotateIcon(final Image img, final int type) {
        Image annotatedImg = img;
        if (files != null && !files.isEmpty()) {
            Iterator it = files.iterator();
            try {
                FileObject fo = (FileObject) it.next();
                if (fo.isValid()){
                    annotatedImg = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedImg;
    }
    
    protected final String annotateName(final String name) {
        String annotatedName = name;
        if (files != null && !files.isEmpty()) {
            Iterator it = files.iterator();
            try {
                FileObject fo = (FileObject) it.next();
                if (fo.isValid()){
                    annotatedName = fo.getFileSystem().getStatus().annotateName(name, files);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedName;
    }
    
    public final void annotationChanged(FileStatusEvent event) {
        if (files == null)
            return;
        if (task == null) {
            task = RequestProcessor.getDefault().create(this);
        }
        boolean changed = false;
        if (forceAnnotation || ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange()))) {
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                if (event.hasChanged(fo)) {
                    iconChange |= event.isIconChange();
                    nameChange |= event.isNameChange();
                    changed = true;
                }
            }
        }
        
        if (changed) {
            task.schedule(50); // batch by 50 ms
        }
    }
    
    public final void run() {
        if (forceAnnotation || iconChange) {
            fireIconChange();
            fireOpenedIconChange();
            iconChange = false;
        }
        if (forceAnnotation || nameChange) {
            fireDisplayNameChange(null, null);
            nameChange = false;
        }
    }
    
}
