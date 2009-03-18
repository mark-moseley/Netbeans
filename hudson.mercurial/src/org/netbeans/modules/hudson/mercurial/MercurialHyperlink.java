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

package org.netbeans.modules.hudson.mercurial;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.spi.HudsonSCM.Helper;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Creates a hyperlink to a Mercurial change.
 * Assumes hgweb/hgwebdir.cgi.
 */
class MercurialHyperlink implements OutputListener {

    private static final Logger LOG = Logger.getLogger(MercurialHyperlink.class.getName());

    private final URI repo;
    private final String node;
    private final HudsonJobChangeFile file;

    MercurialHyperlink(URI repo, String node, HudsonJobChangeFile file) {
        this.repo = repo;
        this.node = node;
        this.file = file;
    }

    public void outputLineAction(OutputEvent ev) {
        Helper.noteWillShowDiff(file.getName());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    final StreamSource before = makeSource(false);
                    final StreamSource after = makeSource(true);
                    Helper.showDiff(before, after, file.getName());
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        });
    }

    public void outputLineSelected(OutputEvent ev) {
        // XXX could focus diff window if open
    }

    public void outputLineCleared(OutputEvent ev) {}

    private StreamSource makeSource(boolean after) throws IOException {
        Reader r;
        String rev;
        if (file.getEditType() == (after ? EditType.delete : EditType.add)) {
            r = new StringReader("");
            rev = null;
        } else {
            rev = after ? node : findParent(repo, node);
            InputStream is = repo.resolve("raw-file/" + rev + "/" + file.getName()).toURL().openStream();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileUtil.copy(is, baos);
                r = new StringReader(baos.toString());
            } finally {
                is.close();
            }
        }
        String mimeType = "text/plain"; // XXX use FileUtil.getMIMETypeExtensions
        String name = file.getName();
        String title = name.replaceFirst(".+/", "") + " @" + (rev != null ? rev.substring(0, 12) : "null");
        return StreamSource.createSource(name, title, mimeType, r);
    }

    private static final Map<String,String> parents = new HashMap<String,String>();
    private static final Pattern PARENT_COMMENT = Pattern.compile("# Parent ([0-9a-f]{40})");
    private static synchronized String findParent(URI repo, String node) throws IOException {
        String parent = parents.get(node);
        if (parent == null) {
            URL rawrev = repo.resolve("raw-rev/" + node).toURL();
            try {
                InputStream is = rawrev.openStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
                    String line;
                    while ((line = r.readLine()) != null) {
                        Matcher m = PARENT_COMMENT.matcher(line);
                        if (m.matches()) {
                            parent = m.group(1);
                            break;
                        }
                    }
                } finally {
                    is.close();
                }
                if (parent == null) {
                    throw new IOException("No parent rev spec found");
                }
            } catch (IOException x) {
                throw (IOException) new IOException("Could not parse " + rawrev + ": "+ x).initCause(x);
            }
            parents.put(node, parent);
        }
        return parent;
    }

}
