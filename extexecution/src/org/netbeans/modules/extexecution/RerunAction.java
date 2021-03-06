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
package org.netbeans.modules.extexecution;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.extexecution.api.ExecutionService;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.Utilities;


/**
 * The RerunAction is placed into the I/O window, allowing the user to restart
 * a particular execution context.
 *
 * Based on the equivalent RerunAction in the ant support.
 *
 * @author Tor Norbye, Petr Hejl
 */
public final class RerunAction extends AbstractAction implements FileChangeListener {

    private transient ExecutionService prototype;

    private FileObject fileObject;

    public RerunAction(ExecutionService prototype, FileObject fileObject) {
        this.prototype = prototype;
        setEnabled(false); // initially, until ready
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/extexecution/resources/rerun.png")));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RerunAction.class, "Rerun"));

        this.fileObject = fileObject;

        if (fileObject != null) {
            fileObject.addFileChangeListener(FileUtil.weakFileChangeListener(this, fileObject));
        }
    }

    public void actionPerformed(ActionEvent e) {
        setEnabled(false);

        // Save the file before running it, if any
        if (fileObject != null) {
            // Save the file
            try {
                DataObject dobj = DataObject.find(fileObject);

                if (dobj != null) {
                    SaveCookie saveCookie = dobj.getCookie(SaveCookie.class);

                    if (saveCookie != null) {
                        saveCookie.save();
                    }
                }
            } catch (DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }

        if (prototype != null) {
            Accessor.getDefault().rerun(prototype);
        }
    }

    public void fileDeleted(FileEvent fe) {
        firePropertyChange("enabled", null, false); // NOI18N
    }

    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
    }

    public void fileChanged(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public boolean isEnabled() {
        // #84874: should be disabled in case the original Ant script is now gone.
        return super.isEnabled() && ((fileObject == null) || fileObject.isValid());
    }

    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        private static volatile Accessor accessor;

        public static void setDefault(Accessor accessor) {
            assert Accessor.accessor == null : "Already initialized accessor";

            Accessor.accessor = accessor;
        }

        public static Accessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            // invokes static initializer of ExecutionService.class
            // that will assign value to the DEFAULT field above
            Class c = ExecutionService.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }

            assert accessor != null : "The DEFAULT field must be initialized";
            return accessor;
        }

        public abstract Task rerun(ExecutionService service);

    }
}
