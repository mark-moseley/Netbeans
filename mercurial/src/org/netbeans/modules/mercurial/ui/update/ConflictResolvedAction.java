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

package org.netbeans.modules.mercurial.ui.update;

import java.io.File;
import java.util.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileInformation;
import javax.swing.AbstractAction;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.RequestProcessor;
import  org.openide.util.NbBundle;

/**
 * Allow files who have Conlfict Status to be manually resolved.
 *
 * @author Petr Kuzel
 */
public class ConflictResolvedAction extends AbstractAction {

    private final VCSContext context;
 
    public ConflictResolvedAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent e) {
        if(!Mercurial.getInstance().isGoodVersionAndNotify()) return;
        resolved(context);
    }

    public static void resolved(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);
        final File root = HgUtils.getRootFile(ctx);
        if (root == null || files == null || files.length == 0) return;

        conflictResolved(root, files);

        return;
    }

    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
        
        if(cache.listFiles(context, FileInformation.STATUS_VERSIONED_CONFLICT).length != 0)
            return true;

        return false;
    }

    public static void conflictResolved(File repository, final File[] files) {
        if (repository == null || files == null || files.length == 0) return;
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {

            public void perform() {
                for (int i = 0; i < files.length; i++) {
                    if (isCanceled()) {
                        return;
                    }
                    File file = files[i];
                    ConflictResolvedAction.perform(file);
                }
            }
        };
        support.start(rp, repository.getAbsolutePath(), NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConlictResolved_Progress")); // NOI18N
    }
    
    private static void perform(File file) {
        if (file == null) return;
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        
        HgCommand.deleteConflictFile(file.getAbsolutePath());
        Mercurial.LOG.log(Level.FINE, "ConflictResolvedAction.perform(): DELETE CONFLICT File: {0}", // NOI18N
                new Object[] {file.getAbsolutePath() + HgCommand.HG_STR_CONFLICT_EXT} );
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
    }
    
    public static void resolved(File file) {
        perform(file);
    }

}
