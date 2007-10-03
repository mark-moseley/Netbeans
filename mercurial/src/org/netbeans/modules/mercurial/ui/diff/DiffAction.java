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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Diff action for mercurial: 
 * hg diff - diff repository (or selected files)
 * 
 * @author John Rice
 */
public class DiffAction extends AbstractAction {
    
    private final VCSContext context;

    public DiffAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void actionPerformed(ActionEvent e) {
        String contextName = Utils.getContextDisplayName(context);
        diff(context, Setup.DIFFTYPE_LOCAL, contextName);
    }
    
    public boolean isEnabled() {
        return HgUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE).length > 0;
    } 

    public static void diff(VCSContext ctx, int type, String contextName) {

        DiffMainPanel panel = new DiffMainPanel(ctx, type, contextName); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); // NOI18N
        tc.open();
        tc.requestActive();
    }

    public static void diff(File file, String rev1, String rev2) {
        DiffMainPanel panel = new DiffMainPanel(file, rev1, rev2); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
}
