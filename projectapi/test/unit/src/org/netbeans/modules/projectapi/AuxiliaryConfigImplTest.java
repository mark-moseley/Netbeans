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

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class AuxiliaryConfigImplTest extends NbTestCase {

    public AuxiliaryConfigImplTest(String name) {
        super(name);
    }

    private FileObject prjdir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        prjdir = FileUtil.createMemoryFileSystem().getRoot();
    }

    private class PrjNoAC implements Project {
        public FileObject getProjectDirectory() {
            return prjdir;
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

    public void testFallbackAuxiliaryConfigurationExists() throws Exception {
        Project prj = new PrjNoAC();
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(prj);
        assertNotNull(ac);
        String namespace = "http://nowhere.net/test";
        assertNull(ac.getConfigurationFragment("x", namespace, true));
        ac.putConfigurationFragment(makeElement("x", namespace), true);
        Element e = ac.getConfigurationFragment("x", namespace, true);
        assertNotNull(e);
        assertEquals("x", e.getLocalName());
        assertEquals(namespace, e.getNamespaceURI());
        assertNull(ac.getConfigurationFragment("x", namespace, false));
        ac.removeConfigurationFragment("x", namespace, true);
        assertNull(ac.getConfigurationFragment("x", namespace, true));
        ac.putConfigurationFragment(makeElement("y", namespace), false);
        prj = new PrjNoAC();
        ac = ProjectUtils.getAuxiliaryConfiguration(prj);
        e = ac.getConfigurationFragment("y", namespace, false);
        assertNotNull(e);
        assertEquals("y", e.getLocalName());
        assertEquals(namespace, e.getNamespaceURI());
    }

    public void testSharedStorage() throws Exception {
        Project prj = new PrjNoAC();
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(prj);
        ac.putConfigurationFragment(makeElement("x", "one"), true);
        ac.putConfigurationFragment(makeElement("x", "two"), true);
        ac.putConfigurationFragment(makeElement("y", "two"), true);
        ac.putConfigurationFragment(makeElement("y", "one"), true);
        FileObject netbeansXml = prjdir.getFileObject(AuxiliaryConfigImpl.AUX_CONFIG_FILENAME);
        assertNotNull(netbeansXml);
        assertEquals("<auxiliary-configuration xmlns=\"http://www.netbeans.org/ns/auxiliary-configuration/1\">" +
                "<x xmlns=\"one\"/>" +
                "<x xmlns=\"two\"/>" +
                "<y xmlns=\"one\"/>" +
                "<y xmlns=\"two\"/>" +
                "</auxiliary-configuration>",
                loadXML(netbeansXml));
        Element x = makeElement("x", "two");
        x.appendChild(x.getOwnerDocument().createElementNS("two", "xx"));
        ac.putConfigurationFragment(x, true);
        ac.removeConfigurationFragment("y", "one", true);
        assertEquals("<auxiliary-configuration xmlns=\"http://www.netbeans.org/ns/auxiliary-configuration/1\">" +
                "<x xmlns=\"one\"/>" +
                "<x xmlns=\"two\"><xx/></x>" +
                "<y xmlns=\"two\"/>" +
                "</auxiliary-configuration>",
                loadXML(netbeansXml));
    }

    public void testPrivateStorage() throws Exception {
        Project prj = new PrjNoAC();
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(prj);
        ac.putConfigurationFragment(makeElement("x", "ns"), false);
        String attr = AuxiliaryConfigImpl.AUX_CONFIG_ATTR_BASE + ".ns#x";
        assertEquals("<x xmlns=\"ns\"/>", prjdir.getAttribute(attr));
        Element x = makeElement("x", "ns");
        x.appendChild(x.getOwnerDocument().createElementNS("ns", "xx"));
        ac.putConfigurationFragment(x, false);
        assertEquals("<x xmlns=\"ns\"><xx/></x>", prjdir.getAttribute(attr));
        ac.removeConfigurationFragment("x", "ns", false);
        assertEquals(null, prjdir.getAttribute(attr));
    }

    public void testDefinedAC() throws Exception {
        Project prj1 = new PrjNoAC();
        AuxiliaryConfiguration ac1 = ProjectUtils.getAuxiliaryConfiguration(prj1);
        ac1.putConfigurationFragment(makeElement("x", "ns"), true);
        ac1.putConfigurationFragment(makeElement("y", "ns"), false);
        final List<String> ops = new ArrayList<String>();
        Project prj2 = new Project() {
            public FileObject getProjectDirectory() {
                return prjdir;
            }
            public Lookup getLookup() {
                return Lookups.singleton(new AuxiliaryConfiguration() {
                    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
                        ops.add("gCF " + elementName + " " + namespace + " " + shared);
                        if (elementName.equals("x")) {
                            try {
                                Element x = makeElement("x", "ns");
                                x.appendChild(x.getOwnerDocument().createElementNS("ns", "xx"));
                                return x;
                            } catch (Exception x) {
                                throw new AssertionError(x);
                            }
                        } else {
                            return null;
                        }
                    }
                    public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
                        try {
                            ops.add("pCF " + AuxiliaryConfigImpl.elementToString(fragment) + " " + shared);
                        } catch (ParserConfigurationException x) {
                            throw new AssertionError(x);
                        }
                    }
                    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
                        ops.add("rCF " + elementName + " " + namespace + " " + shared);
                        return true;
                    }
                });
            }
        };
        AuxiliaryConfiguration ac2 = ProjectUtils.getAuxiliaryConfiguration(prj2);
        assertEquals("<x xmlns=\"ns\"><xx/></x>", AuxiliaryConfigImpl.elementToString(ac2.getConfigurationFragment("x", "ns", true)));
        assertEquals("<y xmlns=\"ns\"/>", AuxiliaryConfigImpl.elementToString(ac2.getConfigurationFragment("y", "ns", false)));
        ac2.putConfigurationFragment(makeElement("z", "ns"), true);
        ac2.removeConfigurationFragment("x", "ns", true);
        assertEquals(null, ac1.getConfigurationFragment("x", "ns", true));
        assertEquals(null, ac1.getConfigurationFragment("z", "ns", true));
        assertEquals("[gCF x ns true, gCF y ns false, pCF <z xmlns=\"ns\"/> true, rCF x ns true]", ops.toString());
    }

    private static Element makeElement(String name, String namespace) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        return doc.createElementNS(namespace, name);
    }

    private static String loadXML(FileObject f) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(f.getInputStream()), false, true, null, null);
        return AuxiliaryConfigImpl.elementToString(doc.getDocumentElement()).replaceAll("\n\\s*", "");
    }

}
