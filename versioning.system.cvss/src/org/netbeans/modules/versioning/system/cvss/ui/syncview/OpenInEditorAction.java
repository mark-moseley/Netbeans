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

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Opens the file under {@link SyncFileNode} in editor.
 * 
 * @author Maros Sandor
 */
public class OpenInEditorAction extends AbstractAction {

    public OpenInEditorAction() {
        super(NbBundle.getBundle(OpenInEditorAction.class).getString("CTL_Synchronize_Popup_OpenInEditor"));
        setEnabled(isActionEnabled());
    }

    private boolean isActionEnabled() {
        File [] files = Utils.getCurrentContext(null).getFiles();
        for (File file : files) {
            if (file.canRead()) return true;
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        File [] files = Utils.getCurrentContext(null).getFiles();
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    openDataObjectByCookie(DataObject.find(fo));
                } catch (DataObjectNotFoundException ex) {
                    // ignore this error and try to open other files too
                }
            }
        }
    }
    
    private final boolean openDataObjectByCookie(DataObject dataObject) {
        Node.Cookie cookie;
        Class cookieClass;
        if ((((cookie = dataObject.getCookie(cookieClass = EditorCookie.Observable.class)) != null
                            || (cookie = dataObject.getCookie(cookieClass = EditorCookie.class)) != null))
                || (cookie = dataObject.getCookie(cookieClass = OpenCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = EditCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = ViewCookie.class)) != null) {
            return openByCookie(cookie, cookieClass);
        }
        return false;
    }
    
    private boolean openByCookie(Node.Cookie cookie, Class cookieClass) {
        if ((cookieClass == EditorCookie.class)
                || (cookieClass == EditorCookie.Observable.class)) {
            ((EditorCookie) cookie).open();
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == EditCookie.class) {
            ((EditCookie) cookie).edit();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException("Reopen #58766: " + cookieClass); // NOI18N
        }
        return true;
    }
}
