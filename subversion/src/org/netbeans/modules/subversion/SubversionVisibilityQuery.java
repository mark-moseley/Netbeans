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

package org.netbeans.modules.subversion;

import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

import java.io.*;
import java.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.openide.ErrorManager;

/**
 * Hides folders that have 'Localy removed' status.
 * 
 * @author Maros Sandor
 */
public class SubversionVisibilityQuery implements VisibilityQueryImplementation, VersioningListener {

    private List<ChangeListener>  listeners = new ArrayList<ChangeListener>();
    private FileStatusCache       cache;

    public SubversionVisibilityQuery() {
        cache = Subversion.getInstance().getStatusCache();
        cache.addVersioningListener(this);
    }

    public boolean isVisible(FileObject fileObject) {
        if (fileObject.isData()) return true;
        File file = FileUtil.toFile(fileObject);
        if(file == null) return true;
        try {
            return cache.getStatus(file).getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            return true;
        }        
    }

    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.remove(l);
        listeners = newList;
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            File file = (File) event.getParams()[0];
            if (file != null && file.isDirectory()) {
                FileInformation old = (FileInformation) event.getParams()[1];
                FileInformation cur = (FileInformation) event.getParams()[2];
                if (old != null && old.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY || cur.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                    fireVisibilityChanged();
                }
            }
        }
    }

    static boolean isHiddenFolder(FileInformation info, File file) {
        return file.isDirectory() && info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY;
    }
    
    private void fireVisibilityChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);          
        }          
    }
}
