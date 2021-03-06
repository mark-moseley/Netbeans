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

package org.netbeans.modules.subversion.ui.ignore;

import java.util.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.nodes.Node;
import java.io.File;
import java.lang.String;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Adds/removes files to svn:ignore property.
 * It does not support patterns.
 *
 * @author Maros Sandor
 */
public class IgnoreAction extends ContextAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    protected String getBaseName(Node [] activatedNodes) {
        int actionStatus = getActionStatus(activatedNodes);
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";                                           // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore";                                         // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus);   // NOI18N
        }
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
    
    public int getActionStatus(Node [] nodes) {
        return getActionStatus(SvnUtils.getCurrentContext(nodes).getFiles());
    }

    public int getActionStatus(File [] files) {
        int actionStatus = -1;
        if (files.length == 0) return UNDEFINED; 
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".svn") || files[i].getName().equals("_svn")) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                if (actionStatus == UNIGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = IGNORING;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                if (actionStatus == IGNORING) {
                    actionStatus = UNDEFINED;
                    break;
                }
                actionStatus = UNIGNORING;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }
    
    protected boolean enable(Node[] nodes) {
        return getActionStatus(nodes) != UNDEFINED;
    }

    public void performContextAction(final Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        final int actionStatus = getActionStatus(nodes);
        if (actionStatus != IGNORING && actionStatus != UNIGNORING) {
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
        
        final File files[] = SvnUtils.getCurrentContext(nodes).getRootFiles();                                                

        ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {                
                Map<File, Set<String>> names = splitByParent(files);
                // do not attach onNotify listeners because the ignore command forcefully fires change events on ALL files
                // in the parent directory and NONE of them interests us, see #89516
                SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(false);               
                } catch (SVNClientException e) {
                    SvnClientExceptionHandler.notifyException(e, true, true);
                    return;
                }                
                for (File parent : names.keySet()) {
                    Set<String> patterns = names.get(parent);
                    if(isCanceled()) {
                        return;
                    }
                    try {
                        Set<String> currentPatterns = new HashSet<String>(client.getIgnoredPatterns(parent));
                        if (actionStatus == IGNORING) {
                            ensureVersioned(parent);
                            currentPatterns.addAll(patterns);
                        } else if (actionStatus == UNIGNORING) {
                            currentPatterns.removeAll(patterns);
                        }
                        client.setIgnoredPatterns(parent, new ArrayList<String>(currentPatterns));    
                        
                    } catch (SVNClientException e) {
                        SvnClientExceptionHandler.notifyException(e, true, true);
                    }
                }
                // refresh files manually, we do not suppport wildcards in ignore patterns so this is sufficient
                for (File file : files) {
                    Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
                // refresh also the parents
                for (File parent : names.keySet()) {
                    Subversion.getInstance().getStatusCache().refresh(parent, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        };            
        support.start(createRequestProcessor(nodes));
    }

    private Map<File, Set<String>> splitByParent(File[] files) {
        Map<File, Set<String>> map = new HashMap<File, Set<String>>(2);
        for (File file : files) {
            File parent = file.getParentFile();
            if (parent == null) continue;
            Set<String> names = map.get(parent);
            if (names == null) {
                names = new HashSet<String>(5);
                map.put(parent, names);
            }
            names.add(file.getName());
        }
        return map;
    }    
    
    /**
     * Adds this file and all its parent folders to repository if they are not yet added. 
     * 
     * @param file file to add
     * @throws SVNClientException if something goes wrong in subversion
     */ 
    private static void ensureVersioned(File file) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) != 0) return;
        ensureVersioned(file.getParentFile());
        add(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }

    /**
     * Adds the file to repository with 'svn add', non-recursively.
     * 
     * @param file file to add
     */ 
    private static void add(File file) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        SvnClient client = Subversion.getInstance().getClient(repositoryUrl);               
        client.addFile(file);
    }

    protected boolean asynchronous() {
        return false;
    }

    public static void ignore(File file) throws SVNClientException {
        File parent = file.getParentFile();
        ensureVersioned(parent);
        // technically, this block need not be synchronized but we want to have svn:ignore property set correctly at all times
        synchronized(IgnoreAction.class) {                        
            List<String> patterns = Subversion.getInstance().getClient(true).getIgnoredPatterns(parent);
            if (patterns.contains(file.getName()) == false) {
                patterns.add(file.getName());
                Subversion.getInstance().getClient(true).setIgnoredPatterns(parent, patterns);
            }            
        }
    }
}
