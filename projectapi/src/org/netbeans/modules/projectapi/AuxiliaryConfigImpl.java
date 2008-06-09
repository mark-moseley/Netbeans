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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AuxiliaryConfigImpl implements AuxiliaryConfiguration {

    private static final Logger LOG = Logger.getLogger(AuxiliaryConfigImpl.class.getName());
    static final String AUX_CONFIG_ATTR_BASE = AuxiliaryConfiguration.class.getName();
    static final String AUX_CONFIG_FILENAME = ".netbeans.xml"; // NOI18N

    private final Project project;

    public AuxiliaryConfigImpl(Project proj) {
        this.project = proj;
    }

    public Element getConfigurationFragment(final String elementName, final String namespace, final boolean shared) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                AuxiliaryConfiguration delegate = project.getLookup().lookup(AuxiliaryConfiguration.class);
                if (delegate != null) {
                    Element fragment = delegate.getConfigurationFragment(elementName, namespace, shared);
                    if (fragment != null) {
                        if (elementName.equals(fragment.getLocalName()) && namespace.equals(fragment.getNamespaceURI())) {
                            return fragment;
                        } else {
                            LOG.log(Level.INFO, delegate.getClass().getName() + " produced wrong local name or namespace for " + namespace + "#" + elementName + " in " + project);
                        }
                    }
                }
                FileObject dir = project.getProjectDirectory();
                if (shared) {
                    FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
                    if (config != null) {
                        try {
                            InputStream is = config.getInputStream();
                            try {
                                InputSource input = new InputSource(is);
                                input.setSystemId(config.getURL().toString());
                                Element root = XMLUtil.parse(input, false, true, /*XXX*/null, null).getDocumentElement();
                                return findElement(root, elementName, namespace);
                            } finally {
                                is.close();
                            }
                        } catch (Exception x) {
                            LOG.log(Level.INFO, "Cannot parse" + config, x);
                        }
                    }
                } else {
                    String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
                    Object attr = dir.getAttribute(attrName);
                    if (attr instanceof String) {
                        try {
                            Element fragment = XMLUtil.parse(new InputSource(new StringReader((String) attr)), false, true,
                                                             /*XXX #136595: need utility method*/null, null).getDocumentElement();
                            if (elementName.equals(fragment.getLocalName()) && namespace.equals(fragment.getNamespaceURI())) {
                                return fragment;
                            } else {
                                LOG.log(Level.INFO, "Value " + attr + " of " + attrName + " on " + dir + " has the wrong local name or namespace");
                            }
                        } catch (SAXException x) {
                            LOG.log(Level.INFO, "Cannot parse value " + attr + " of " + attrName + " on " + dir + ": " + x.getMessage());
                        } catch (IOException x) {
                            assert false : x;
                        }
                    }
                }
                return null;
            }
        });
    }

    public void putConfigurationFragment(final Element fragment, final boolean shared) throws IllegalArgumentException {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                String elementName = fragment.getLocalName();
                String namespace = fragment.getNamespaceURI();
                if (namespace == null) {
                    throw new IllegalArgumentException();
                }
                AuxiliaryConfiguration delegate = project.getLookup().lookup(AuxiliaryConfiguration.class);
                if (delegate != null) {
                    delegate.putConfigurationFragment(fragment, shared);
                    removeFallbackImpl(elementName, namespace, shared);
                    return null;
                }
                FileObject dir = project.getProjectDirectory();
                try {
                    if (shared) {
                        Document doc;
                        FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
                        if (config != null) {
                            InputStream is = config.getInputStream();
                            try {
                                InputSource input = new InputSource(is);
                                input.setSystemId(config.getURL().toString());
                                doc = XMLUtil.parse(input, false, true, /*XXX*/ null, null);
                            } finally {
                                is.close();
                            }
                        } else {
                            config = dir.createData(AUX_CONFIG_FILENAME);
                            doc = XMLUtil.createDocument("auxiliary-configuration", "http://www.netbeans.org/ns/auxiliary-configuration/1", null, null);
                        }
                        Element root = doc.getDocumentElement();
                        Element oldFragment = findElement(root, elementName, namespace);
                        if (oldFragment != null) {
                            root.removeChild(oldFragment);
                        }
                        Node ref = null;
                        NodeList list = root.getChildNodes();
                        for (int i = 0; i < list.getLength(); i++) {
                            Node node = list.item(i);
                            if (node.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }
                            int comparison = node.getNodeName().compareTo(elementName);
                            if (comparison == 0) {
                                comparison = node.getNamespaceURI().compareTo(namespace);
                            }
                            if (comparison > 0) {
                                ref = node;
                                break;
                            }
                        }
                        root.insertBefore(root.getOwnerDocument().importNode(fragment, true), ref);
                        OutputStream os = config.getOutputStream();
                        try {
                            XMLUtil.write(doc, os, "UTF-8");
                        } finally {
                            os.close();
                        }
                    } else {
                        String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
                        dir.setAttribute(attrName, elementToString(fragment));
                    }
                } catch (Exception x) {
                    LOG.log(Level.WARNING, "Cannot save configuration to " + dir, x);
                }
                return null;
            }
        });
    }

    static String elementToString(Element e) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        DOMImplementationLS ls = (DOMImplementationLS) doc.getImplementation().getFeature("LS", "3.0"); // NOI18N
        assert ls != null : "No DOM 3 LS supported in " + doc.getClass().getName();
        // JAXP bug #6710755: cannot directly serialize fragment in JDK 5, must add to a document.
        doc.appendChild(doc.importNode(e, true));
        LSSerializer serializer = ls.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer.writeToString(doc);
    }

    private boolean removeFallbackImpl(final String elementName, final String namespace, final boolean shared) {
        FileObject dir = project.getProjectDirectory();
        try {
            if (shared) {
                FileObject config = dir.getFileObject(AUX_CONFIG_FILENAME);
                if (config != null) {
                    try {
                        Document doc;
                        InputStream is = config.getInputStream();
                        try {
                            InputSource input = new InputSource(is);
                            input.setSystemId(config.getURL().toString());
                            doc = XMLUtil.parse(input, false, true, /*XXX*/ null, null);
                        } finally {
                            is.close();
                        }
                        Element root = doc.getDocumentElement();
                        Element toRemove = findElement(root, elementName, namespace);
                        if (toRemove != null) {
                            root.removeChild(toRemove);
                            if (root.getElementsByTagName("*").getLength() > 0) {
                                OutputStream os = config.getOutputStream();
                                try {
                                    XMLUtil.write(doc, os, "UTF-8");
                                } finally {
                                    os.close();
                                }
                            } else {
                                config.delete();
                            }
                            return true;
                        }
                    } catch (SAXException x) {
                        LOG.log(Level.INFO, "Cannot parse" + config, x);
                    }
                }
            } else {
                String attrName = AUX_CONFIG_ATTR_BASE + "." + namespace + "#" + elementName;
                if (dir.getAttribute(attrName) != null) {
                    dir.setAttribute(attrName, null);
                    return true;
                }
            }
        } catch (IOException x) {
            LOG.warning("Cannot remove configuration from " + dir);
        }
        return false;
    }

    public boolean removeConfigurationFragment(final String elementName, final String namespace, final boolean shared) throws IllegalArgumentException {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                AuxiliaryConfiguration delegate = project.getLookup().lookup(AuxiliaryConfiguration.class);
                boolean result = false;
                if (delegate != null) {
                    result |= delegate.removeConfigurationFragment(elementName, namespace, shared);
                }
                result |= removeFallbackImpl(elementName, namespace, shared);
                return result;
            }
        });
    }

    private static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        int len = l.getLength();
        for (int i = 0; i < len; i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) l.item(i);
                if (name.equals(el.getLocalName()) && namespace.equals(el.getNamespaceURI())) {
                    if (result == null) {
                        result = el;
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }

}
