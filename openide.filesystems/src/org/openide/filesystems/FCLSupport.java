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
package org.openide.filesystems;

import java.util.List;
import org.openide.util.Exceptions;


/**
 * Support class for impl. of FileChangeListener
 * @author  rm111737
 */
class FCLSupport {
    enum Op {DATA_CREATED, FOLDER_CREATED, FILE_CHANGED, FILE_DELETED, FILE_RENAMED, ATTR_CHANGED}

    /** listeners */
    ListenerList<FileChangeListener> listeners;

    /* Add new listener to this object.
    * @param l the listener
    */
    synchronized final void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    /* Remove listener from this object.
    * @param l the listener
    */
    synchronized final void removeFileChangeListener(FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove(fcl);
        }
    }

    final void dispatchEvent(FileEvent fe, Op operation) {
        List<FileChangeListener> fcls;

        synchronized (this) {
            if (listeners == null) {
                return;
            }

            fcls = listeners.getAllListeners();
        }

        for (FileChangeListener l : fcls) {
            dispatchEvent(l, fe, operation);
        }
    }

    final static void dispatchEvent(FileChangeListener fcl, FileEvent fe, Op operation) {
        try {
            switch (operation) {
                case DATA_CREATED:
                    fcl.fileDataCreated(fe);
                    break;
                case FOLDER_CREATED:
                    fcl.fileFolderCreated(fe);
                    break;
                case FILE_CHANGED:
                    fcl.fileChanged(fe);
                    break;
                case FILE_DELETED:
                    fcl.fileDeleted(fe);
                    break;
                case FILE_RENAMED:
                    fcl.fileRenamed((FileRenameEvent) fe);
                    break;
                case ATTR_CHANGED:
                    fcl.fileAttributeChanged((FileAttributeEvent) fe);
                    break;
                default:
                    throw new AssertionError(operation);
            }
        } catch (RuntimeException x) {
            Exceptions.printStackTrace(x);
        }
    }

    /** @return true if there is a listener
    */
    synchronized final boolean hasListeners() {
        return listeners != null && listeners.hasListeners();
    }
}
