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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Permits use of Mercurial to create and view Hudson projects.
 */
@ServiceProvider(service=HudsonSCM.class, position=200)
public class HudsonMercurialSCM implements HudsonSCM {

    static final Logger LOG = Logger.getLogger(HudsonMercurialSCM.class.getName());

    public Configuration forFolder(File folder) {
        // XXX could also permit projects as subdirs of Hg repos (lacking SPI currently)
        final URI source = getDefaultPull(folder.toURI());
        if (source == null) {
            return null;
        }
        if (!source.isAbsolute() || "file".equals(source.getScheme())) {
            LOG.log(Level.FINE, "{0} is a local file location", source);
            return null;
        }
        return new Configuration() {
            public void configure(Document doc) {
                Element root = doc.getDocumentElement();
                Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm"));
                configXmlSCM.setAttribute("class", "hudson.plugins.mercurial.MercurialSCM");
                configXmlSCM.appendChild(doc.createElement("source")).appendChild(doc.createTextNode(source.toString()));
                configXmlSCM.appendChild(doc.createElement("modules")).appendChild(doc.createTextNode(""));
                configXmlSCM.appendChild(doc.createElement("clean")).appendChild(doc.createTextNode("true"));
                Helper.addTrigger(doc);
            }
        };
    }

    public String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot) {
        // XXX find repo at or above localRoot, assume workspacePath is repo-relative
        // XXX check whether job's repo matches that of repo, by looking at e.g. head of 00changelog.i
        return null; // XXX
    }

    public List<? extends HudsonJobChangeItem> parseChangeSet(HudsonJob job, Element changeSet) {
        if (!"hg".equals(Helper.xpath("kind", changeSet))) {
            return null;
        }
        final URI repo = getDefaultPull(URI.create(job.getUrl() + "ws/"));
        if (repo == null) {
            LOG.log(Level.FINE, "No known repo location for {0}", job);
            return null;
        }
        if (!"http".equals(repo.getScheme()) && !"https".equals(repo.getScheme())) {
            LOG.log(Level.FINE, "Need hgweb to show changes from {0}", repo);
            return null;
        }
        class HgItem implements HudsonJobChangeItem {
            final Element itemXML;
            HgItem(Element xml) {
                this.itemXML = xml;
            }
            public String getUser() {
                return Helper.xpath("author/fullName", itemXML);
            }
            public String getMessage() {
                return Helper.xpath("msg", itemXML);
            }
            public Collection<? extends HudsonJobChangeFile> getFiles() {
                if ("true".equals(Helper.xpath("merge", itemXML))) {
                    return Collections.emptySet();
                }
                final String node = Helper.xpath("node", itemXML);
                class HgFile implements HudsonJobChangeFile {
                    final String path;
                    final EditType editType;
                    HgFile(String path, EditType editType) {
                        this.path = path;
                        this.editType = editType;
                    }
                    public String getName() {
                        return path;
                    }
                    public EditType getEditType() {
                        return editType;
                    }
                    public OutputListener hyperlink() {
                        return new MercurialHyperlink(repo, node, this);
                    }
                }
                List<HgFile> files = new ArrayList<HgFile>();
                NodeList nl = itemXML.getElementsByTagName("addedPath");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Helper.xpath("text()", (Element) nl.item(i)), EditType.add));
                }
                nl = itemXML.getElementsByTagName("modifiedPath");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Helper.xpath("text()", (Element) nl.item(i)), EditType.edit));
                }
                nl = itemXML.getElementsByTagName("deletedPath");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new HgFile(Helper.xpath("text()", (Element) nl.item(i)), EditType.delete));
                }
                return files;
            }
        }
        List<HgItem> items = new ArrayList<HgItem>();
        NodeList nl = changeSet.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new HgItem((Element) nl.item(i)));
        }
        return items;
    }

    /**
     * Try to find the default pull location for a possible Hg repository.
     * @param repository the repository location (checkout root)
     * @return its pull location as an absolute URI ending in a slash,
     *         or null in case it could not be determined
     */
    static URI getDefaultPull(URI repository) {
        assert repository.toString().endsWith("/");
        URI hgrc = repository.resolve(".hg/hgrc");
        String defaultPull = null;
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(HudsonMercurialSCM.class.getClassLoader()); // #141364
        try {
            Ini ini = new Ini(hgrc.toURL());
            Ini.Section section = ini.get("paths");
            if (section != null) {
                defaultPull = section.get("default-pull");
                if (defaultPull == null) {
                    defaultPull = section.get("default");
                }
            }
        } catch (FileNotFoundException x) {
            LOG.log(Level.FINE, "{0} is not an Hg repo", repository);
            return null;
        } catch (Exception x) {
            LOG.log(Level.WARNING, "Could not parse " + hgrc, x);
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(l);
        }
        if (defaultPull == null) {
            LOG.log(Level.FINE, "{0} does not specify paths.default or default-pull", hgrc);
            return null;
        }
        if (!defaultPull.endsWith("/")) {
            defaultPull += "/";
        }
        if (defaultPull.startsWith("/") || defaultPull.startsWith("\\")) {
            LOG.log(Level.FINE, "{0} looks like a local file location", defaultPull);
            return new File(defaultPull).toURI();
        } else {
            String defaultPullNoPassword = defaultPull.replaceFirst("//[^/]+(:[^/]+)?@", "//");
            try {
                return repository.resolve(new URI(defaultPullNoPassword));
            } catch (URISyntaxException x) {
                LOG.log(Level.FINE, "{0} is not a valid URI", defaultPullNoPassword);
                return null;
            }
        }
    }

}
