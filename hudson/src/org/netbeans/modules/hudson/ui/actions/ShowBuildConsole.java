/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Displays the console from a Hudson build in the Output Window.
 */
public class ShowBuildConsole extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowBuildConsole.class.getName());

    private final HudsonJob job;
    private final int buildNumber;

    public ShowBuildConsole(HudsonJob job, int buildNumber) {
        this.job = job;
        this.buildNumber = buildNumber;
        putValue(NAME, "Show Console"); // XXX I18N
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(job.getName() + " #" + buildNumber + " console").post(this); // NOI18N
    }

    public void run() {
        HudsonInstanceImpl instance = job.getLookup().lookup(HudsonInstanceImpl.class);
        if (instance == null) {
            return;
        }
        String name = job.getDisplayName() + " #" + buildNumber;
        LOG.log(Level.FINE, "{0} started", name);
        InputOutput io = IOProvider.getDefault().getIO(name, new Action[] {/* XXX abort build button? */});
        io.select();
        /* If any metadata is needed, e.g. whether it is running, could use:
        HudsonJobBuild build = instance.getConnector().getJobBuild(job, buildNumber);
        if (build == null) {
            return;
        }
         */
        int start = 0;
        String url = job.getUrl() + buildNumber + "/progressiveLog?start="; // NOI18N
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();
        try {
            while (true) {
                LOG.log(Level.FINE, "{0} polling", name);
                if (out.checkError() || err.checkError() || io.isClosed()) {
                    LOG.log(Level.FINE, "{0} stopped", name);
                    break;
                }
                URLConnection conn = new URL(url + start).openConnection();
                conn.setRequestProperty("Accept-Encoding", "gzip"); // NOI18N
                conn.connect();
                boolean moreData = Boolean.parseBoolean(conn.getHeaderField("X-More-Data"));
                LOG.log(Level.FINE, "{0} retrieving text from {1}", new Object[] {name, start});
                start = conn.getHeaderFieldInt("X-Text-Size", start);
                InputStream is = conn.getInputStream();
                try {
                    InputStream isToUse = is;
                    if ("gzip".equals(conn.getContentEncoding())) {
                        LOG.log(Level.FINE, "{0} using GZIP", name);
                        isToUse = new GZIPInputStream(is);
                    }
                    // XXX safer to check content type on connection, but in fact Stapler sets it to UTF-8
                    BufferedReader r = new BufferedReader(new InputStreamReader(isToUse, "UTF-8"));
                    String line;
                    while ((line = r.readLine()) != null) {
                        // XXX send errors to err, create hyperlinks, ...
                        out.println(line);
                    }
                } finally {
                    is.close();
                }
                if (!moreData) {
                    LOG.log(Level.FINE, "{0} EOF", name);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {
                    LOG.log(Level.FINE, "{0} interrupted", name);
                    break;
                }
            }
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
        }
        out.close();
        err.close();
    }

}
