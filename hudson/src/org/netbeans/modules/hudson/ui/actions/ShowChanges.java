/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Displays changelog for a build.
 */
public class ShowChanges extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowChanges.class.getName());

    private final HudsonJob job;
    private final int buildNumber;

    public ShowChanges(HudsonJob job, int buildNumber) {
        super("Show Changes"); // XXX I18N
        this.job = job;
        this.buildNumber = buildNumber;
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(job.getName() + " #" + buildNumber + " changes").post(this); // NOI18N
    }

    public void run() {
        HudsonInstanceImpl instance = job.getLookup().lookup(HudsonInstanceImpl.class);
        if (instance == null) {
            return;
        }
        String name = job.getDisplayName() + " #" + buildNumber + " changes"; // XXX I18N
        InputOutput io = IOProvider.getDefault().getIO(name, new Action[0]);
        io.select();
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        Collection<? extends HudsonJobChangeItem> changes = instance.getConnector().getJobBuild(job, buildNumber).getChanges();
        if (changes.isEmpty()) {
            out.println("No changes."); // XXX I18N
        } else {
            for (HudsonJobChangeItem item : changes) {
                out.println(item.getUser() + ": " + item.getMessage());
                for (HudsonJobChangeFile file : item.getFiles()) {
                    // XXX hyperlink to diff viewer
                    switch (file.getEditType()) {
                    case edit:
                        out.print('±');
                        break;
                    case add:
                        out.print('+');
                        break;
                    case delete:
                        out.print('-');
                    }
                    out.print(' ');
                    out.println(file.getName());
                }
            }
        }
        out.close();
        err.close();
    }

}
