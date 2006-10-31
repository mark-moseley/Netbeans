/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

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
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"),  // NOI18N
                                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"),  // NOI18N
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
        Subversion.getInstance().shutdown();
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
