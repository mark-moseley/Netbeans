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

package org.netbeans.modules.db.explorer.node;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.db.test.DDLTestBase;
import org.openide.nodes.Node;

/**
 *
 * @author Rob Englander
 */
public class ColumnNodeTest extends DDLTestBase {

    public ColumnNodeTest(String testName) {
        super(testName);
    }

    public void testClipboardCopy() throws Exception {
        String tablename = "testtable";
        String pkName = "id";
        createBasicTable(tablename, pkName);

        TableNode tableNode = getTableNode(tablename);
        assertNotNull(tableNode);

        ColumnNode columnNode = getColumnNode(tableNode);
        assertNotNull(columnNode);
        assertTrue(columnNode.canCopy());

        Transferable transferable = (Transferable)columnNode.clipboardCopy();
        Set mimeTypes = new HashSet();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            mimeTypes.add(flavors[i].getMimeType());
        }
        assertTrue(mimeTypes.contains("application/x-java-netbeans-dbexplorer-column; class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Column"));
        assertTrue(mimeTypes.contains("application/x-java-openide-nodednd; mask=1; class=org.openide.nodes.Node"));

        dropTable(tablename);
    }

    private ColumnNode getColumnNode(TableNode tnode) {
        Collection<? extends Node> children = tnode.getChildNodesSync();
        ColumnNode node = null;

        for (Node child : children) {
            if (child instanceof ColumnNode) {
                node = (ColumnNode)child;
                break;
            }
        }

        return node;
    }
}
