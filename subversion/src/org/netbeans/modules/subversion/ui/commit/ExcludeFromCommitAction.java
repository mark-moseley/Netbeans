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

package org.netbeans.modules.subversion.ui.commit;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.openide.nodes.*;

/**
 *
 * @author Petr Kuzel
 */
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_LOCAL_CHANGE;
    }

    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "popup_commit_exclude"; // NOI18N
        case INCLUDING:
            return "popup_commit_include"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(Node[] nodes) {
        SvnModuleConfig config = SvnModuleConfig.getDefault();
        File [] files = getContext(nodes).getFiles();
        int status = UNDEFINED;
        for (int i = 0; i < files.length; i++) {
            if (config.isExcludedFromCommit(files[i].getAbsolutePath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    public void performContextAction(final Node[] nodes) {
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                SvnModuleConfig config = SvnModuleConfig.getDefault();
                int status = getActionStatus(nodes);                
                List<File> files = new ArrayList<File>();
                for (Node node : nodes) {                    
                    File aFile = node.getLookup().lookup(File.class);
                    if (aFile != null) {
                        files.add(aFile);                        
                    }
                }
                List<String> paths = new ArrayList<String>(files.size());
                for (File file : files) {
                    paths.add(file.getAbsolutePath());
                }
                if (isCanceled()) return;
                if (status == EXCLUDING) {
                    config.addExclusionPaths(paths);
                } else if (status == INCLUDING) {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(createRequestProcessor(nodes));
    }

}
