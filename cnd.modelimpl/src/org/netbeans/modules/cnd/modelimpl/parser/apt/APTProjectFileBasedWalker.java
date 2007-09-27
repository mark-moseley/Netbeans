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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibraryManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.filesystems.FileUtil;

/**
 * base walker to visit project files based APTs
 * @author Vladimir Voskresensky
 */
public abstract class APTProjectFileBasedWalker extends APTAbstractWalker {
    private final FileImpl file;
    private final ProjectBase startProject;
    private int mode;
    
    public APTProjectFileBasedWalker(ProjectBase startProject, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler);
        this.mode = ProjectBase.GATHERING_MACROS;
        this.file = file;
        this.startProject = startProject;
        assert startProject != null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    
    protected void include(ResolvedPath resolvedPath, APTInclude apt) {
        FileImpl included = null;
        if (resolvedPath != null) {
            String path = resolvedPath.getPath();
            if (path.indexOf("..") > 0) { // NOI18N
                path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            }
            if (getIncludeHandler().pushInclude(path, apt.getToken().getLine(), resolvedPath.getIndex())) {
                ProjectBase startProject = this.getStartProject();
                if (startProject != null) {
                    ProjectBase inclFileOwner = LibraryManager.getInsatnce().resolveFileProjectOnInclude(startProject, getFile(), resolvedPath);
                    try {
                        included = includeAction(inclFileOwner, path, mode, apt);
                    } catch (FileNotFoundException ex) {
                        APTUtils.LOG.log(Level.SEVERE, "file {0} not found", new Object[] {path});// NOI18N
                    } catch (IOException ex) {
                        APTUtils.LOG.log(Level.SEVERE, "error on including {0}:\n{1}", new Object[] {path, ex});
                    }
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "file {0} without project!!!", new Object[] {file});// NOI18N
                    getIncludeHandler().popInclude();
                }
            }
        }
	postInclude(apt, included);
    }
    
    abstract protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, int mode, APTInclude apt) throws IOException;

    protected void postInclude(APTInclude apt, FileImpl included) {
    }
    
    protected FileImpl getFile() {
        return this.file;
    }

    protected ProjectBase getStartProject() {
	return this.file.getProjectImpl();
    }
    
    protected void setMode(int mode) {
        this.mode = mode;
    }
    
}
