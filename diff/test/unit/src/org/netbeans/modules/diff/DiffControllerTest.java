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
package org.netbeans.modules.diff;

import org.netbeans.junit.NbTestCase;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Maros Sandor
 */
public class DiffControllerTest extends NbTestCase {

    private DiffController controller;

    public DiffControllerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        controller = DiffController.create(new Impl("name1", "title1", "text/plain", "content1\nsame\ndifferent1"), new Impl("name2", "title2", "text/plain", "content2\nsame\ndifferent2"));
    }

    public void testCurrentDifference() throws Exception {
        int dc = controller.getDifferenceCount();
        assertEquals("Wrong number of differences", 2, dc);
    }

    public void testDifferenceIndex() throws Exception {
        int dc = controller.getDifferenceCount();
        int di = controller.getDifferenceIndex();
        assertTrue("Wrong difference index", di == -1 || di >= 0 && di < dc);
    }

    public void testComponent() throws Exception {
        JComponent c = controller.getJComponent();
        assertNotNull("Not a JComponent", c);
    }

    /**
     * Private implementation to be returned by the static methods.
     */
    private static class Impl extends StreamSource {

        private String name;
        private String title;
        private String MIMEType;
        private String buffer;

        Impl(String name, String title, String MIMEType, String str) {
            this.name = name;
            this.title = title;
            this.MIMEType = MIMEType;
            buffer = str;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getMIMEType() {
            return MIMEType;
        }

        public Reader createReader() throws IOException {
            return new StringReader(buffer);
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }


}
