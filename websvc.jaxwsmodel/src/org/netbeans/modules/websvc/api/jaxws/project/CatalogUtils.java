/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.api.jaxws.project;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.catalog.Catalog;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogModel;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogModelFactory;
import org.netbeans.modules.websvc.jaxws.catalog.System;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class CatalogUtils {

    public static void copyCatalogEntriesForAllClients(FileObject catalog, FileObject jaxWsCatalog, JaxWsModel jaxWsModel)
        throws IOException {

        CatalogModel sourceModel = getCatalogModel(catalog);
        CatalogModel targetModel = getCatalogModel(jaxWsCatalog);
        Catalog cat1 = sourceModel.getRootComponent();
        Catalog cat2 = targetModel.getRootComponent();
        List<System> systemElements = cat1.getSystems();
        targetModel.startTransaction();
        for (Client client : jaxWsModel.getClients()) {
            String clientName = client.getName();
            for (System systemElement : systemElements) {
                String uri = systemElement.getURIAttr();
                String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
                if (uri != null) {
                    int index = uri.indexOf(prefix);
                    if (index >= 0) {
                        System system = targetModel.getFactory().createSystem();
                        try {
                            system.setSystemIDAttr(new URI(systemElement.getSystemIDAttr()));
                            system.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                        cat2.addSystem(system);
                    }
                }
            }
        }
        targetModel.endTransaction();
    }

    public static void copyCatalogEntriesForClient(FileObject catalog, FileObject jaxWsCatalog, String clientName)
        throws IOException {

        CatalogModel sourceModel = getCatalogModel(catalog);
        CatalogModel targetModel = getCatalogModel(jaxWsCatalog);
        Catalog cat1 = sourceModel.getRootComponent();
        List<System> systemElements = cat1.getSystems();
        if (systemElements.size() > 0) {
            Catalog cat2 = targetModel.getRootComponent();
            targetModel.startTransaction();
            for (System systemElement : systemElements) {
                String uri = systemElement.getURIAttr();
                String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
                if (uri != null) {
                    int index = uri.indexOf(prefix);
                    if (index >= 0) {
                        System system = null;
                        String systemId = systemElement.getSystemIDAttr();
                        if (systemId != null) {
                            for (System s : cat2.getSystems()) {
                                if (systemId.equals(s.getSystemIDAttr())) {
                                    system = s;
                                    break;
                                }
                            }
                        }
                        if (system == null) {
                            system = targetModel.getFactory().createSystem();
                            cat2.addSystem(system);
                        }
                        try {
                            system.setSystemIDAttr(new URI(systemElement.getSystemIDAttr()));
                            system.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }                        
                    }
                }
            }
            targetModel.endTransaction();
        }
    }

    public static void updateCatalogEntriesForClient(FileObject jaxWsCatalog, String clientName)
         throws IOException {

        CatalogModel catalogModel = getCatalogModel(jaxWsCatalog);
        Catalog cat = catalogModel.getRootComponent();
        String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
        catalogModel.startTransaction();
        for (System systemElement : cat.getSystems()) {
            String uri = systemElement.getURIAttr();
            int index = uri.indexOf(prefix);
            if (index >= 0) {
                try {
                    systemElement.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        }
        catalogModel.endTransaction();
    }

    public static CatalogModel getCatalogModel(FileObject thisFileObj)
        throws IOException {
        ModelSource source = createModelSource(thisFileObj, true);
        return CatalogModelFactory.getInstance().getModel(source);
    }

    private static ModelSource createModelSource(final FileObject thisFileObj,
            boolean editable) throws IOException {
        assert thisFileObj != null : "Null file object.";

        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
            final EditorCookie editor = dobj.getCookie(EditorCookie.class);
            if (editor != null) {
                Lookup proxyLookup = Lookups.proxy(
                   new Lookup.Provider() {
                        public Lookup getLookup() {
                            try {
                                return Lookups.fixed(new Object[] {editor.openDocument(), dobj, thisFileObj});
                            } catch (IOException ex) {
                                return Lookups.fixed(new Object[] {dobj, thisFileObj});
                            }
                        }

                    }
                );
                return new ModelSource(proxyLookup, editable);
            }
        } catch (DataObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, // NOI18N
                ex.getMessage(), ex);
        }
        return null;
    }
}
