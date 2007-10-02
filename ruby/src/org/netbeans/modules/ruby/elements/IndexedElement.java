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
package org.netbeans.modules.ruby.elements;

import java.io.IOException;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.filesystems.FileObject;


/**
 * A program element coming from the persistent index.
 *
 * @author Tor Norbye
 */
public abstract class IndexedElement extends RubyElement {
    protected String fileUrl;
    protected final String clz;
    protected final String fqn;
    protected final RubyIndex index;
    protected final String require;
    protected final Set<Modifier> modifiers;
    protected final String attributes;
    private int docLength = -1;
    private Document document;
    private FileObject fileObject;

    protected IndexedElement(RubyIndex index, String fileUrl, String fqn,
        String clz, String require, Set<Modifier> modifiers, String attributes) {
        this.index = index;
        this.fileUrl = fileUrl;
        this.fqn = fqn;
        this.require = require;
        this.modifiers = modifiers;
        this.attributes = attributes;
        // XXX Why do methods need to know their clz (since they already have fqn)
        this.clz = clz;
    }

    public abstract String getSignature();

    public final String getFileUrl() {
        return fileUrl;
    }

    public final String getRequire() {
        return require;
    }

    public final String getFqn() {
        return fqn;
    }

    @Override
    public String toString() {
        return getSignature() + ":" + getFileUrl();
    }

    public final String getClz() {
        return clz;
    }

    public RubyIndex getIndex() {
        return index;
    }

    public String getIn() {
        return getClz();
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = AstUtilities.getBaseDocument(fileObject, true);
        }

        return document;
    }

    public ParserFile getFile() {
        boolean platform = false; // XXX FIND OUT WHAT IT IS!

        return new DefaultParserFile(getFileObject(), null, platform);
    }

    public FileObject getFileObject() {
        if ((fileObject == null) && (fileUrl != null)) {
            fileObject = RubyIndex.getFileObject(fileUrl);

            if (fileObject == null) {
                // Don't try again
                fileUrl = null;
            }
        }

        return fileObject;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /** Return the length of the documentation for this class, in characters */
    public int getDocumentationLength() {
        if (docLength == -1) {
            docLength = 0;

            if (attributes != null) {
                int index = attributes.indexOf('d');

                if (index != -1) {
                    index = attributes.indexOf('(', index + 1);

                    if (index != -1) {
                        docLength = Integer.parseInt(attributes.substring(index + 1,
                                    attributes.indexOf(')', index + 1)));
                    } else {
                        // Unknown length - just use 1 to indicate positive document length
                        docLength = 1;
                    }
                }
            }
        }

        return docLength;
    }
}
