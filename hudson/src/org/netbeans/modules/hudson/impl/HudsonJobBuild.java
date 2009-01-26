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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import static org.netbeans.modules.hudson.constants.HudsonJobBuildConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonJobChangeFileConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonJobChangeItemConstants.*;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;

/**
 * Describes Hudson Job's Build
 *
 * @author Michal Mocnak
 */
public class HudsonJobBuild {
    
    public enum Result {
        SUCCESS, FAILURE
    }
    
    private final HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    private List<HudsonJobChangeItem> changes = new ArrayList<HudsonJobChangeItem>();
    
    public void putProperty(String name, Object o) {
        properties.putProperty(name, o);
    }
    
    public boolean isBuilding() {
        Boolean building = properties.getProperty(JOB_BUILD_BUILDING, Boolean.class);
        return building != null ? building : false;
    }
    
    public int getDuration() {
        return (int) (properties.getProperty(JOB_BUILD_DURATION, java.lang.Long.class) / 60000);
    }
    
    public Date getDate() {
        return new Date(properties.getProperty(JOB_BUILD_TIMESTAMP, Long.class));
    }
    
    public Result getResult() {
        return properties.getProperty(JOB_BUILD_RESULT, Result.class);
    }
    
    public Collection<HudsonJobChangeItem> getChanges() {
        return changes;
    }
    
    public void addChangeItem(HudsonJobChangeItem item) {
        changes.add(item);
    }
    
    public static class HudsonJobChangeItem {
        
        private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
        
        private List<HudsonJobChangeFile> files = new ArrayList<HudsonJobChangeFile>();
        
        public void putProperty(String name, Object o) {
            properties.putProperty(name, o);
        }
        
        public String getUser() {
            return properties.getProperty(JOB_CHANGE_ITEM_USER, String.class);
        }
        
        public String getMsg() {
            return properties.getProperty(JOB_CHANGE_ITEM_MESSAGE, String.class);
        }
        
        public Collection<HudsonJobChangeFile> getFiles() {
            return files;
        }
        
        public void addFile(HudsonJobChangeFile file) {
            files.add(file);
        }
    }
    
    public static class HudsonJobChangeFile {
        
        public enum EditType {
            add, edit, delete
        }
        
        private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
        
        public void putProperty(String name, Object o) {
            properties.putProperty(name, o);
        }
        
        public String getName() {
            return properties.getProperty(JOB_CHANGE_FILE_NAME, String.class);
        }
        
        public EditType getEditType() {
            return properties.getProperty(JOB_CHANGE_FILE_EDIT_TYPE, EditType.class);
        }
        
        public String getRevision() {
            return properties.getProperty(JOB_CHANGE_FILE_REVISION, String.class);
        }
        
        public String getPrevRevision() {
            return properties.getProperty(JOB_CHANGE_FILE_PREVIOUS_REVISION, String.class);
        }
    }
}