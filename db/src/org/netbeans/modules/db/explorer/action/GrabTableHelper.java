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

package org.netbeans.modules.db.explorer.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Table;

public class GrabTableHelper {

    public void execute(final DatabaseConnector connector,
            final Specification spec,
            final MetadataElementHandle<Table> tableHandle,
            final File file) throws Exception {

        MetadataModel model = connector.getDatabaseConnection().getMetadataModel();

        final Exception[] array = new Exception[1];

        model.runReadAction(
            new Action<Metadata>() {
                public void run(Metadata metaData) {
                    Table table = tableHandle.resolve(metaData);

                    try {
                        CreateTable cmd = spec.createCommandCreateTable(table.getName());

                        Collection<Column> columns = table.getColumns();
                        for (Column column : columns) {
                            cmd.getColumns().add(connector.getColumnSpecification(table, column));
                        }

                        FileOutputStream fstream = new FileOutputStream(file);
                        ObjectOutputStream ostream = new ObjectOutputStream(fstream);
                        cmd.setSpecification(null);
                        ostream.writeObject(cmd);
                        ostream.flush();
                        ostream.close();
                    } catch (Exception e) {
                        array[0] = e;
                    }
                }
            }
        );

        if (array[0] != null) {
            throw array[0];
        }
    }
}
