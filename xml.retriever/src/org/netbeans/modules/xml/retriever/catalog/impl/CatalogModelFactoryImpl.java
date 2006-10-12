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
package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author girix
 */
public class CatalogModelFactoryImpl extends CatalogWriteModelFactory{
    private static Logger logger = Utilities.getLogger();
    
    
    private static WeakHashMap <Project, CatalogWriteModel> projcat = new WeakHashMap<Project, CatalogWriteModel>();
    
    public CatalogWriteModel getCatalogWriteModelForProject(FileObject anyFileObjectExistingInAProject) throws CatalogModelException {
        logger.entering("CatalogModelFactoryImpl","getCatalogModelForProject");
        Project project = FileOwnerQuery.getOwner(anyFileObjectExistingInAProject);
        assert(project != null);
        CatalogWriteModel result = null;
        try {
            result = new CatalogWriteModelImpl(project);
        } catch (IOException ex) {
            throw new CatalogModelException(ex);
        }
        return result;
    }
    
    
    private static WeakHashMap <Project, CatalogModel> proj2cm = new WeakHashMap<Project, CatalogModel>();
    
    public CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException {
        if(modelSource == null)
            throw new IllegalArgumentException("modelSource arg is null.");
        CatalogModel catalogModel = (CatalogModel) modelSource.getLookup().lookup(CatalogModel.class);
        if(catalogModel == null){
            FileObject fo = (FileObject) modelSource.getLookup().lookup(FileObject.class);
            if(fo == null)
                throw new IllegalArgumentException("ModelSource must have FileObject in its lookup");
            return getCatalogModel(fo);
        }
        return catalogModel;
    }
    
    public CatalogModel getCatalogModel(FileObject fo) throws CatalogModelException{
        CatalogModel catalogModel = null;
        Project project = FileOwnerQuery.getOwner(fo);
        if(project != null){
            catalogModel = proj2cm.get(fo);
            if(catalogModel != null)
                return catalogModel;
            try {
                catalogModel = new CatalogModelImpl(project);
            } catch (IOException ex) {
                throw new CatalogModelException(ex);
            }
            return catalogModel;
        }
        catalogModel = new CatalogModelImpl();
        return catalogModel;
    }
    
    public LSResourceResolver getLSResourceResolver() {
        return new LSResourceResolverImpl();
    }
    
    public CatalogWriteModel getCatalogWriteModelForCatalogFile(FileObject fileObjectOfCatalogFile) throws CatalogModelException {
        try{
            return new CatalogWriteModelImpl(fileObjectOfCatalogFile);
        } catch (IOException ex) {
            throw new CatalogModelException(ex);
        }
    }
}