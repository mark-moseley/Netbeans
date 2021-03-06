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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.swing.*;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Handles module events distributed by NetBeans module
 * framework.
 *
 * <p>It's registered and instantiated from module manifest.
 *
 * @author Petr Kuzel
 * @author Maros Sandor
 */
public final class ModuleLifecycleManager extends ModuleInstall implements ErrorHandler, EntityResolver {

    static final String [] vcsGenericModules = {
        "org.netbeans.modules.vcs.advanced", // NOI18N
        "org.netbeans.modules.vcs.profiles.cvsprofiles", // NOI18N
        "org.netbeans.modules.vcs.profiles.vss", // NOI18N
        "org.netbeans.modules.vcs.profiles.pvcs", // NOI18N
        "org.netbeans.modules.vcs.profiles.subversion", // NOI18N
        "org.netbeans.modules.vcs.profiles.teamware" // NOI18N
    };
    
    public void restored() {
        disableOldModules();
    }

    private void disableOldModules() {
        Runnable runnable = new Runnable() {
            public void run() {
                boolean notified = false;
                outter: for (int i = 0; i < vcsGenericModules.length; i++) {
                    FileLock lock = null;
                    OutputStream os = null;
                    try {
                        String newModule = vcsGenericModules[i];
                        String newModuleXML = "Modules/" + newModule.replace('.', '-') + ".xml"; // NOI18N
                        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(newModuleXML);
                        if (fo == null) continue;
                        Document document = readModuleDocument(fo);

                        NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
                        int n = list.getLength();
                        for (int j = 0; j < n; j++) {
                            Element node = (Element) list.item(j);
                            if ("enabled".equals(node.getAttribute("name"))) { // NOI18N
                                Text text = (Text) node.getChildNodes().item(0);
                                String value = text.getNodeValue();
                                if ("true".equals(value)) { // NOI18N
                                    text.setNodeValue("false"); // NOI18N
                                    break;
                                } else {
                                    continue outter;
                                }
                            }
                        }
                        if (!notified) {
                            JOptionPane.showMessageDialog(null, 
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"), 
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"), 
                                                          JOptionPane.WARNING_MESSAGE);
                            notified = true;
                        }
                        lock = fo.lock();
                        os = fo.getOutputStream(lock);
                        
                        XMLUtil.write(document, os, "UTF-8"); // NOI18N
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        if (os != null) try { os.close(); } catch (IOException ex) {}
                        if (lock != null) lock.releaseLock();
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        InputStream is = fo.getInputStream();
        Document document = parser.parse(is);
        is.close();
        return document;
    }

    public void uninstalled() {
        CvsVersioningSystem.getInstance().shutdown();
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
    
    public void error(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void fatalError(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void warning(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }
}
