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

package org.netbeans.modules.db.metadata.model.api;

import java.util.Collection;
import org.netbeans.modules.db.metadata.model.MetadataAccessor;
import org.netbeans.modules.db.metadata.model.MetadataModelImplementation;
import org.netbeans.modules.db.metadata.model.spi.CatalogImplementation;
import org.netbeans.modules.db.metadata.model.spi.ColumnImplementation;
import org.netbeans.modules.db.metadata.model.spi.MetadataImplementation;
import org.netbeans.modules.db.metadata.model.spi.SchemaImplementation;
import org.netbeans.modules.db.metadata.model.spi.TableImplementation;

/**
 *
 * @author Andrei Badea
 */
public class Metadata {

    private final MetadataImplementation impl;

    static {
        MetadataAccessor.setDefault(new MetadataAccessorImpl());
    }

    Metadata(MetadataImplementation impl) {
        this.impl = impl;
    }

    /**
     * @return the default catalog.
     * @throws MetadataException.
     */
    public Catalog getDefaultCatalog() {
        return impl.getDefaultCatalog();
    }

    /**
     * @return the catalogs.
     * @throws MetadataException.
     */
    public Collection<Catalog> getCatalogs() {
        return impl.getCatalogs();
    }

    /**
     * @param name a catalog name.
     * @return a catalog named {@code name} or null.
     * @throws MetadataException.
     */
    public Catalog getCatalog(String name) {
        return impl.getCatalog(name);
    }

    /**
     * @throws MetadataException.
     */
    public void refresh() {
        impl.refresh();
    }

    private static final class MetadataAccessorImpl extends MetadataAccessor {

        @Override
        public MetadataModel createMetadataModel(MetadataModelImplementation impl) {
            return new MetadataModel(impl);
        }

        @Override
        public Metadata createMetadata(MetadataImplementation impl) {
            return new Metadata(impl);
        }

        @Override
        public Catalog createCatalog(CatalogImplementation impl) {
            return new Catalog(impl);
        }

        @Override
        public Schema createSchema(SchemaImplementation impl) {
            return new Schema(impl);
        }

        @Override
        public Table createTable(TableImplementation impl) {
            return new Table(impl);
        }

        @Override
        public Column createColumn(ColumnImplementation impl) {
            return new Column(impl);
        }

        @Override
        public CatalogImplementation getCatalogImpl(Catalog catalog) {
            return catalog.impl;
        }

        @Override
        public SchemaImplementation getSchemaImpl(Schema schema) {
            return schema.impl;
        }

        @Override
        public TableImplementation getTableImpl(Table table) {
            return table.impl;
        }
    }
}
