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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.InputStream;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // Disable old importer module if present.
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(
                        "Modules/org-netbeans-modules-projectimport.xml"); // NOI18N
                if (fo == null) {
                    return;
                }
                try {
                    Document doc;
                    InputStream is = fo.getInputStream();
                    try {
                        doc = XMLUtil.parse(new InputSource(is), false, true, /* XXX #136595 */null, null);
                    } finally {
                        is.close();
                    }
                    NodeList params = doc.getDocumentElement().getElementsByTagName("param"); // NOI18N
                    for (int i = 0; i < params.getLength(); i++) {
                        Element param = (Element) params.item(i);
                        if (param.getAttribute("name").equals("enabled")) { // NOI18N
                            Text text = (Text) param.getChildNodes().item(0); // XXX #136595
                            if (text.getNodeValue().equals("true")) { // NOI18N
                                text.setNodeValue("false"); // NOI18N
                                OutputStream os = fo.getOutputStream();
                                try {
                                    XMLUtil.write(doc, os, "UTF-8");
                                } finally {
                                    os.close();
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception x) {
                    Exceptions.printStackTrace(x);
                }
            }
        });
    }

}
