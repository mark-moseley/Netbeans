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

package org.netbeans.modules.cnd.discovery.wizard.checkedtree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander Simon
 */
public class Root implements AbstractRoot {
    private Map<String,AbstractRoot> children = new HashMap<String,AbstractRoot>();
    private String name;
    private String folder;
    private List<String> files;
    
    public Root(String name, String folder){
        this.name = name;
        this.folder = folder;
    }
    
    public Collection<AbstractRoot> getChildren(){
        return children.values();
    }
    
    public String getName(){
        return name;
    }

    public String getFolder(){
        return FileUtil.normalizeFile(new File(folder)).getAbsolutePath();
    }
    
    private Root getChild(String child){
        return (Root)children.get(child);
    }
    
    public List<String> getFiles(){
        return files;
    }
    
    public void setFiles(List<String> files){
        if (this.files == null) {
            this.files = files;
        }
    }
    
    public Root addChild(String child){
        Root current = this;
        StringTokenizer st = new StringTokenizer(child,"/\\"); // NOI18N
        StringBuilder path = new StringBuilder();
        while(st.hasMoreTokens()){
            String segment = st.nextToken();
            if (path.length()>0){
                path.append('/');
            } else {
                if(!(segment.length()>1 && segment.charAt(1)==':')){
                    path.append('/');
                }
            }
            path.append(segment);
            if (st.hasMoreTokens()) {
                Root found = current.getChild(segment);
                if (found == null) {
                    found = new Root(segment, path.toString());
                    current.children.put(segment, found);
                }
                current = found;
            } else {
                List<String> fileList = current.getFiles();
                if (fileList == null){
                    fileList = new ArrayList<String>();
                    current.setFiles(fileList);
                }
                fileList.add(child);
            }
        }
        return current;
    }
}
