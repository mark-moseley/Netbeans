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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates the syntax and semantics of one or more JNLP files.
 * Any other JNLP fragments referred to recursively from these files will be validated as well.
 * JNLP files must specify a document type, normally:
 * &lt;!DOCTYPE jnlp PUBLIC "-//Sun Microsystems, Inc//DTD JNLP Discriptor 1.5//EN" "http://java.sun.com/dtd/JNLP-1.5.dtd">
 * (including the misspelling: see bug #6613630).
 * The codebase specified in the file is used as is if a file: URL;
 * if $$codebase, it is taken to be the immediately containing directory, to match the behavior of JnlpDownloadServlet;
 * if a remote URL, it is also taken to be the immediately containing directory,
 * since otherwise it would be impossible to validate files which were generated with intent to upload to a server.
 * See issue #96630.
 */
public class VerifyJNLP extends Task {

    private List<FileSet> filesets = new ArrayList<FileSet>();
    /**
     * Add one or more JNLP files to validate.
     * Use &lt;fileset file="..."/> if you have just one.
     * Fragments referred to from higher JNLP files are checked automatically and do not need to be specified.
     */
    public void addConfiguredFileset(FileSet fs) {
        filesets.add(fs);
    }

    public @Override void execute() throws BuildException {
        for (FileSet fs : filesets) {
            DirectoryScanner s = fs.getDirectoryScanner(getProject());
            File basedir = s.getBasedir();
            for (String incl : s.getIncludedFiles()) {
                validate(new File(basedir, incl));
            }
        }
    }

    private void validate(File jnlp) throws BuildException {
        log("Validating: " + jnlp);
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(jnlp.toURI().toString()), true, false, new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    fatalError(exception);
                }
                public void error(SAXParseException exception) throws SAXException {
                    fatalError(exception);
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw new SAXException("parse or validation error:\n" +
                            exception.getSystemId() + ":" + exception.getLineNumber() + ": " + exception.getMessage());
                }
            }, new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if ("-//Sun Microsystems, Inc//DTD JNLP Discriptor 1.5//EN".equals(publicId)) {
                        return new InputSource(VerifyJNLP.class.getResource("JNLP-1.5.dtd").toString());
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception x) {
            throw new BuildException(x);
        }
        String codebase = doc.getDocumentElement().getAttribute("codebase");
        URI base;
        if (codebase.equals("$$codebase")) {
            base = jnlp.getParentFile().toURI();
        } else {
            try {
                base = new URI(codebase);
                if (!base.isAbsolute()) {
                    throw new BuildException("JNLP validation error\n" + jnlp + ": non-absolute codebase " + base);
                }
                if (!"file".equals(base.getScheme())) {
                    // Needed for local validation of a tree intended for eventual upload to a server.
                    base = jnlp.getParentFile().toURI();
                }
            } catch (URISyntaxException x) {
                throw new BuildException("JNLP validation error\n" + jnlp + ": invalid codebase '" + codebase + "': " + x, x);
            }
        }
        Certificate[] existingCertificates = null;
        File existingSignedJar = null;
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String href = el.getAttribute("href");
            if (href.length() > 0) {
                URI u;
                try {
                    u = base.resolve(new URI(href));
                } catch (URISyntaxException x) {
                    throw new BuildException("JNLP validation error\n" + jnlp + ": invalid href '" + href + "': " + x, x);
                }
                assert u.isAbsolute() : u + " not absolute as " + href + " resolved against " + base;
                if ("file".equals(u.getScheme())) {
                    File f = new File(u);
                    if (!f.isFile()) {
                        if (el.getTagName().equals("icon")) {
                            // jnlp.xml in harness generates <icon href="${app.icon}"/> optimistically.
                            // Does not seem to be a problem if it is missing.
                            log(jnlp + ": no such file " + f, Project.MSG_WARN);
                        } else {
                            throw new BuildException("JNLP validation error\n" + jnlp + ": no such file " + f);
                        }
                    }
                    if (el.getTagName().equals("extension")) {
                        validate(f);
                    } else if (el.getTagName().equals("jar")) {
                        try {
                            JarFile jf = new JarFile(f, true);
                            // Try to find signers.
                            try {
                                Enumeration<JarEntry> entries = jf.entries();
                                while (entries.hasMoreElements()) {
                                    JarEntry entry = entries.nextElement();
                                    if (entry.getName().startsWith("META-INF/")) {
                                        // At least MANIFEST.MF is not signed in the normal way, it seems.
                                        continue;
                                    }
                                    if (entry.getSize() < 1) {
                                        // Dirs are not signed.
                                        continue;
                                    }
                                    InputStream is = jf.getInputStream(entry);
                                    int read = 0;
                                    while (read != -1) {
                                        read = is.read();
                                    }
                                    Certificate[] certs = entry.getCertificates();
                                    /*
                                    System.err.println("existingSignedJar=" + existingSignedJar + " existingCertificates=" + Arrays.toString(existingCertificates) +
                                                       " f=" + f + " certs=" + Arrays.toString(certs) + " entry.name=" + entry.getName());
                                     */
                                    if (existingSignedJar != null && !Arrays.equals(certs, existingCertificates)) {
                                        throw new BuildException("JNLP validation error\n" + jnlp +
                                                ": different signatures (or signing status) between " + existingSignedJar + " and " + f);
                                    }
                                    existingCertificates = certs;
                                    existingSignedJar = f;
                                    break; // just check one representative file
                                }
                            } finally {
                                jf.close();
                            }
                        } catch (IOException x) {
                            throw new BuildException("JNLP validation error\n" + jnlp + ": error examining signatures in " + f + ": " + x, x);
                        }
                    }
                } else {
                    try {
                        u.toURL().openStream().close();
                    } catch (IOException x) {
                        log(jnlp + ": could not open network URL " + u, Project.MSG_WARN);
                        // Do not halt build; might just be a network connectivity issue.
                    }
                }
            }
        }
    }

}
