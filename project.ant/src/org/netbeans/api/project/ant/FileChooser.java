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

package org.netbeans.api.project.ant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.JFileChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.project.ant.FileChooserAccessory;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;


/**
 * Custom file chooser allowing user to choose how a file will be referenced 
 * from a project - via absolute path, or relative path. Make sure you call
 * {@link #getSelectedPaths} instead of {@link #getSelectedFiles} as it returns relative
 * files and performs file copying if necessary.
 * 
 * @author David Konecny
 * @since org.netbeans.modules.project.ant/1 1.19
 */
public final class FileChooser extends JFileChooser {

    private FileChooserAccessory accessory;

    /**
     * Create chooser for given AntProjectHelper. Standard file chooser is shown
     * if project is not sharable.
     * 
     * @param helper ant project helper; cannot be null
     * @param copyAllowed is file copying allowed
     */
    public FileChooser(AntProjectHelper helper, boolean copyAllowed) {
        super();
        LibraryManager lm = ProjectLibraryProvider.getProjectLibraryManager(helper);
        if (lm != null) {
            URL u = lm.getLocation();
            if (u != null) {
                File libBase = new File(URI.create(u.toExternalForm())).getParentFile();
                accessory = new FileChooserAccessory(this, FileUtil.toFile(helper.getProjectDirectory()), 
                    libBase, copyAllowed);
                setAccessory(accessory);
            }
        }
    }

    /**
     * Create chooser for given base folder and shared libraries folder. 
     * 
     * @param baseFolder base folder to which all selected files will be relativized;
     *  can be null in which case regular file chooser is shown
     * @param sharedLibrariesFolder providing shared libraries folder enables option
     *  of copying selected files there; can be null in which case copying option
     *  is disabled
     */
    public FileChooser(File baseFolder, File sharedLibrariesFolder) {
        super();
        if (baseFolder != null) {
            accessory = new FileChooserAccessory(this, baseFolder, sharedLibrariesFolder, sharedLibrariesFolder != null);
            setAccessory(accessory);
        }
    }
    
    /**
     * Returns array of paths selected. The difference from 
     * {@link #getSelectedFiles} is that depends on user's choice the files
     * may be relative and they may have been copied to different location.
     * 
     * @return array of files which may be relative to base folder this chooser
     *  was created for; e.g. project folder in case of AntProjectHelper 
     *  constructor; never null; can be empty array
     * @throws java.io.IOException any IO problem; for example during 
     *  file copying
     */
    public String[] getSelectedPaths() throws IOException {
        if (accessory != null) {
            accessory.copyFilesIfNecessary();
            if (accessory.isRelative()) {
                return accessory.getFiles();
            }
        }
        if (isMultiSelectionEnabled()) {
            File[] sels = getSelectedFiles();
            String[] toRet = new String[sels.length];
            int index = 0;
            for (File fil : sels) {
                toRet[index] = fil.getAbsolutePath();
                index++;
            }
            return toRet;
        } else {
            if (getSelectedFile() != null) {
                return new String[]{ getSelectedFile().getAbsolutePath() };
            } else {
                return new String[0];
            }
        }
    }

    @Override
    public void approveSelection() {
        if (accessory != null && !accessory.canApprove()) {
            return;
        }
        super.approveSelection();
    }



}
