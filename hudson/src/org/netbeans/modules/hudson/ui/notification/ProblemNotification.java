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

package org.netbeans.modules.hudson.ui.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.CharConversionException;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.ui.actions.ShowBuildConsole;
import org.netbeans.modules.hudson.ui.actions.ShowFailures;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;

/**
 * Build failed or was unstable.
 */
class ProblemNotification implements ActionListener {

    private final HudsonJob job;
    private final int build;
    private final boolean failed;
    private final boolean running;
    private Notification notification;

    ProblemNotification(HudsonJob job, int build, boolean failed, boolean running) {
        this.job = job;
        this.build = build;
        this.failed = failed;
        this.running = running;
    }

    private String getTitle() {
        try {
            return XMLUtil.toElementContent(job.getDisplayName()) + " #" + build +
                    (failed ? " <em>failed</em>" : " is <em>unstable</em>"); // XXX I18N
        } catch (CharConversionException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }

    private String getDescription() {
        return failed ? "The build failed." : "Some tests failed."; // XXX I18N
    }

    public void actionPerformed(ActionEvent e) {
        for (HudsonJobBuild b : job.getBuilds()) {
            if (b.getNumber() == build) {
                Action delegate = failed ? new ShowBuildConsole(b) : new ShowFailures(b);
                delegate.actionPerformed(e);
                break;
            }
        }
    }

    private Priority getPriority() {
        return failed ? Priority.HIGH : Priority.NORMAL;
    }

    private Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/hudson/ui/resources/" + // NOI18N
                (failed ? "red" : "yellow") + (running ? "_run" : "") + ".png", true); // NOI18N
    }

    void add() {
        notification = NotificationDisplayer.getDefault().notify(getTitle(), getIcon(), getDescription(), this, getPriority());
    }

    void remove() {
        if (notification != null) {
            notification.clear();
            notification = null;
        }
    }

    public @Override boolean equals(Object obj) {
        if (!(obj instanceof ProblemNotification)) {
            return false;
        }
        ProblemNotification other = (ProblemNotification) obj;
        return job.getName().equals(other.job.getName()) && build == other.build;
    }

    public @Override int hashCode() {
        return job.getName().hashCode() ^ build;
    }

    public @Override String toString() {
        return "ProblemNotification[" + job.getName() + "#" + build + "]";
    }

}
