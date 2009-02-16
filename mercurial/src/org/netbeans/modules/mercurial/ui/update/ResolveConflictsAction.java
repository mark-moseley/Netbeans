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
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;

/**
 * Show basic conflict resolver UI (provided by the diff module).
 *
 * @author Petr Kuzel
 */
public class ResolveConflictsAction extends ContextAction {

    private final VCSContext context;
 
    public ResolveConflictsAction(String name, VCSContext context) {        
        this.context =  context;
        putValue(Action.NAME, name);
    }

    public void performAction(ActionEvent e) {
        resolve(context);
    }

    public static void resolve(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        resolveConflicts(files);

        return;
    }

    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();                
        return cache.containsFileOfStatus(context, FileInformation.STATUS_VERSIONED_CONFLICT, true);
    }

    static void resolveConflicts(File[] files) {
        if (files.length == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    org.openide.util.NbBundle.getMessage(
                        ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        } else {
            for (int i = 0; i<files.length; i++) {
                File file = files[i];
                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                executor.exec();
            }
        }        
    }
    
}
