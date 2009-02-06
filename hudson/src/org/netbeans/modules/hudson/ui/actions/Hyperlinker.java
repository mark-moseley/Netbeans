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

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.netbeans.modules.hudson.spi.HudsonLogger.HudsonLogSession;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Manages warning/error/stack trace hyperlinking in the Output Window.
 */
class Hyperlinker {

    private static final Logger LOG = Logger.getLogger(Hyperlinker.class.getName());

    private final HudsonLogSession[] sessions;

    public Hyperlinker(HudsonJob job) {
        List<HudsonLogSession> _sessions = new ArrayList<HudsonLogSession>();
        for (HudsonLogger logger : Lookup.getDefault().lookupAll(HudsonLogger.class)) {
            _sessions.add(logger.createSession(job));
        }
        sessions = _sessions.toArray(new HudsonLogSession[_sessions.size()]);
    }

    public void handleLine(String line, OutputWriter stream) {
        for (HudsonLogSession session : sessions) {
            if (session.handle(line, stream)) {
                break;
            }
        }
        // PlainLogger is last and always handles it
    }

    @ServiceProvider(service=HudsonLogger.class)
    public static final class PlainLogger implements HudsonLogger {
        public HudsonLogSession createSession(final HudsonJob job) {
            return new HudsonLogSession() {
                // XXX support Windows build servers (using backslashes)
                /** Looks for errors mentioning workspace files. Prefix captures Maven's [WARNING], Ant's [javac], etc. */
                private final Pattern hyperlinkable = Pattern.compile("(?:\\[.+\\] )?/.+/jobs/\\Q" + job.getName() +
                        "\\E/workspace/([^:]+):(?:([0-9]+):(?:([0-9]+):)?)? (?:warning: )?(.+)");
                public boolean handle(String line, OutputWriter stream) {
                    Matcher m = hyperlinkable.matcher(line);
                    if (m.matches()) {
                        final String path = m.group(1);
                        final int row = m.group(2) != null ? Integer.parseInt(m.group(2)) - 1 : -1;
                        final int col = m.group(3) != null ? Integer.parseInt(m.group(3)) - 1 : -1;
                        final String message = m.group(4);
                        try {
                            stream.println(line, new Hyperlink(job, path, message, row, col));
                        } catch (IOException x) {
                            LOG.log(Level.INFO, null, x);
                            stream.println(line);
                        }
                    } else {
                        stream.println(line);
                    }
                    return true;
                }
            };
        }
    }

    private static class Hyperlink implements OutputListener {

        private final HudsonJob job;
        private final String path;
        private final String message;
        private final int row;
        private final int col;

        public Hyperlink(HudsonJob job, String path, String message, int row, int col) {
            this.job = job;
            this.path = path;
            this.message = message;
            this.row = row;
            this.col = col;
        }

        public void outputLineAction(OutputEvent ev) {
            acted(true);
        }

        public void outputLineSelected(OutputEvent ev) {
            acted(false);
        }

        private void acted(final boolean force) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    FileObject f = job.getRemoteWorkspace().findResource(path);
                    if (f == null) {
                        if (force) {
                            StatusDisplayer.getDefault().setStatusText("No file " + path + " found in remote workspace."); // XXX I18N
                            Toolkit.getDefaultToolkit().beep();
                        }
                        return;
                    }
                    // XXX could be useful to select this file in the workspace node
                    StatusDisplayer.getDefault().setStatusText(message);
                    HudsonLogger.Helper.openAt(f, row, col, force);
                }
            });
        }

        public void outputLineCleared(OutputEvent ev) {}
        
    }

}
