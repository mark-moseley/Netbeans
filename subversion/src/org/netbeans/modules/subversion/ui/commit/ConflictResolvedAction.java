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
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.*;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Represnts <tt>svn resolved</tt> command.
 *
 * @author Petr Kuzel
 */
public class ConflictResolvedAction extends ContextAction {
    
    protected String getBaseName(Node[] activatedNodes) {
        return "resolve";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED_CONFLICT;
    }

    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File[] files = ctx.getFiles();

        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {

                SvnClient client = null;
                try {
                    client = Subversion.getInstance().getClient(ctx, this);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }

                if (client == null) {
                    return;
                }
                
                for (int i = 0; i<files.length; i++) {
                    if(isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    try {
                        ConflictResolvedAction.perform(file, client);
                    } catch (SVNClientException ex) {
                        annotate(ex);                        
                    }
                }
            }
        };
        support.start(createRequestProcessor(nodes));        
    }


    /** Marks as resolved or shows error dialog. */
    public static void perform(File file) throws SVNClientException {
        SvnClient client = Subversion.getInstance().getClient(file);
        perform(file, client);        
    }

    private static void perform(File file, SvnClient client) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        client.resolved(file);
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);

        // auxiliary files disappear, synch with FS
        File parent = file.getParentFile();
        if (parent != null) {
            FileObject folder = FileUtil.toFileObject(parent);
            if (folder != null) {
                folder.refresh();
            }
        }        
    }

}
