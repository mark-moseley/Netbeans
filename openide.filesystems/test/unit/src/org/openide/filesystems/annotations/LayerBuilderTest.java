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

package org.openide.filesystems.annotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

public class LayerBuilderTest extends NbTestCase {

    public LayerBuilderTest(String n) {
        super(n);
    }

    private Document doc;
    private LayerBuilder b;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        doc = XMLUtil.createDocument("filesystem", null, null, null);
        b = new LayerBuilder(doc);
        assertEquals("<filesystem/>", dump());
    }

    private String dump() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return baos.toString("UTF-8").
                replace('"', '\'').
                replaceFirst("^<\\?xml version='1\\.0' encoding='UTF-8'\\?>\r?\n", "").
                replaceAll("\r?\n *", "");
    }

    public void testBasicFiles() throws Exception {
        b.file("Menu/File/x.instance").stringvalue("instanceClass", "some.X").write().
                file("Menu/Edit/y.instance").stringvalue("instanceClass", "some.Y").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='some.X'/></file></folder>" +
                "<folder name='Edit'><file name='y.instance'><attr name='instanceClass' stringvalue='some.Y'/></file></folder>" +
                "</folder></filesystem>", dump());
    }

    public void testContent() throws Exception {
        b.file("a.txt").contents("some text here...").write().
                file("b.xml").url("/resources/b.xml").write();
        assertEquals("<filesystem><file name='a.txt'><![CDATA[some text here...]]></file>" +
                "<file name='b.xml' url='/resources/b.xml'/></filesystem>", dump());
    }

    public void testOverwriting() throws Exception {
        b.file("Menu/File/x.instance").stringvalue("instanceClass", "some.X").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'><attr name='instanceClass' stringvalue='some.X'/></file></folder>" +
                "</folder></filesystem>", dump());
        b.file("Menu/File/x.instance").write();
        assertEquals("<filesystem><folder name='Menu'>" +
                "<folder name='File'><file name='x.instance'/></folder>" +
                "</folder></filesystem>", dump());
    }

    public void testShadows() throws Exception {
        LayerBuilder.File orig = b.file("Actions/System/some-Action.instance");
        orig.write();
        b.shadowFile(orig.getPath(), "Menu/File", null).write();
        b.shadowFile(orig.getPath(), "Shortcuts", "C-F6").write();
        assertEquals("<filesystem>" +
                "<folder name='Actions'><folder name='System'><file name='some-Action.instance'/></folder></folder>" +
                "<folder name='Menu'><folder name='File'><file name='some-Action.shadow'>" +
                "<attr name='originalFile' stringvalue='Actions/System/some-Action.instance'/></file></folder></folder>" +
                "<folder name='Shortcuts'><file name='C-F6.shadow'>" +
                "<attr name='originalFile' stringvalue='Actions/System/some-Action.instance'/></file></folder>" +
                "</filesystem>", dump());
    }

    public void testSerialValue() throws Exception {
        b.file("x").serialvalue("a", new byte[] {0, 10, 100, (byte) 200}).write();
        assertEquals("<filesystem><file name='x'><attr name='a' serialvalue='000A64C8'/></file></filesystem>", dump());
    }

    public void testURIs() throws Exception {
        LayerBuilder.File f = b.file("x").urlvalue("a", "../rel").urlvalue("b", "/abs").urlvalue("c", "nbresloc:/proto");
        try {
            f.urlvalue("bogus", ":not:a:URI");
            fail();
        } catch (IllegalArgumentException x) {/* right */}
        try {
            f.urlvalue("bogus", "something:opaque");
            fail();
        } catch (IllegalArgumentException x) {/* right */}
        f.write();
        assertEquals("<filesystem><file name='x'>" +
                "<attr name='a' urlvalue='../rel'/>" +
                "<attr name='b' urlvalue='/abs'/>" +
                "<attr name='c' urlvalue='nbresloc:/proto'/>" +
                "</file></filesystem>", dump());
    }

}
