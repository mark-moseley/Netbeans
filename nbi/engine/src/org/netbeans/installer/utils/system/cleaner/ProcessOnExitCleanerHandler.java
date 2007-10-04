/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.system.cleaner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Dmitry Lipin
 */


public abstract class ProcessOnExitCleanerHandler extends OnExitCleanerHandler {
    protected List <String> runningCommand;
    private String cleanerFileName ;
    
    protected ProcessOnExitCleanerHandler(String cleanerFileName) {
        this.cleanerFileName = cleanerFileName;
    }
    protected File getCleanerFile() throws IOException{
        String name = cleanerFileName;
        int idx = name.lastIndexOf(".");
        String ext = "";
        if(idx > 0) {
            ext = name.substring(idx);
            name = name.substring(0, idx);
        }
        return File.createTempFile(name, ext, SystemUtils.getTempDirectory());        
    }
    
    protected File getListFile() throws IOException{
        return File.createTempFile(DELETING_FILES_LIST,null, SystemUtils.getTempDirectory());        
    }
    
    protected abstract void writeCleaningFileList(File listFile, List <String> files) throws IOException;
    protected abstract void writeCleaner(File cleanerFile) throws IOException;
    
    public void init(){
        if(fileList.size() > 0) {
            try {
            File listFile = getListFile();
            
            List <String> paths = new ArrayList <String> ();
            for(File f : fileList) {
                paths.add(f.getAbsolutePath());
            }
            Collections.sort(paths, Collections.reverseOrder());
            
            
                writeCleaningFileList(listFile, paths);                
                File cleanerFile = getCleanerFile();
                writeCleaner(cleanerFile);
                SystemUtils.correctFilesPermissions(cleanerFile);
                runningCommand = new ArrayList <String> ();
                runningCommand.add(cleanerFile.getCanonicalPath());
                runningCommand.add(listFile.getCanonicalPath());
            } catch  (IOException e) {
                // do nothing then..
            }
        }
    }
    
    public void run() {
        init();
        if(runningCommand!=null ) {
            try {                
                ProcessBuilder builder= new ProcessBuilder(runningCommand);
                builder.directory(SystemUtils.getUserHomeDirectory());
                builder.start();
                LogManager.log(ErrorLevel.DEBUG, "... cleaning process has been started ");
            } catch (IOException ex) {
                LogManager.log(ex);
            }
        }
    }
}
