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

package org.netbeans.modules.db.explorer.metadata;

import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.util.Lookup;

/**
 *
 * @author Rob Englander
 */
public class MetadataReader {

    public static class DataWrapper<C> {
        C object;

        public DataWrapper() {
            object = null;
        }

        public DataWrapper(C obj) {
            object = obj;
        }

        public void setObject(C obj) {
            object = obj;
        }

        public C getObject() {
            return object;
        }
    }

    public interface MetadataReadListener {
        public void run(Metadata metaData, DataWrapper wrapper);
    }

    private MetadataReader() {

    }

    public static String getSchemaWorkingName(Schema schema) {
        String schemaName = schema.getName();
        if (schemaName == null) {
            schemaName = schema.getParent().getName();
        }

        return schemaName;
    }

    public static String getCatalogWorkingName(Schema schema, Catalog catalog) {
        String catName = catalog.getName();

        if (catName == null) {
            catName = schema.getName();
        }

        return catName;
    }

    public static Schema findSchema(Lookup lookup) {
        MetadataModel model = lookup.lookup(MetadataModel.class);
        final MetadataElementHandle handle = lookup.lookup(MetadataElementHandle.class);

        DataWrapper<Schema> wrapper = new DataWrapper<Schema>();
        readModel(model, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Schema schema = (Schema)handle.resolve(metaData);
                    wrapper.setObject(schema);
                }
            }
        );

        return wrapper.getObject();
    }

    public static Catalog findCatalog(Lookup lookup) {
        MetadataModel model = lookup.lookup(MetadataModel.class);
        final MetadataElementHandle handle = lookup.lookup(MetadataElementHandle.class);

        DataWrapper<Catalog> wrapper = new DataWrapper<Catalog>();
        readModel(model, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Catalog catalog = (Catalog)handle.resolve(metaData);
                    wrapper.setObject(catalog);
                }
            }
        );

        return wrapper.getObject();
    }

    public static void readModel(MetadataModel model, DataWrapper wrapper, MetadataReadListener listener) {
        MetadataReader reader = new MetadataReader();
        reader.read(model, wrapper, listener);
    }

    private void read(MetadataModel model, final DataWrapper wrapper, final MetadataReadListener listener) {
        try {
            model.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        listener.run(metaData, wrapper);
                    }
                }
            );
        } catch (MetadataModelException e) {

        }
    }
}
