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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.jaxws.catalog;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.websvc.jaxws.catalog.impl.CatalogModelImpl;

public class CatalogModelFactory extends AbstractModelFactory<CatalogModel> {
    /**
     * Creates a new instance of CatalogModelFactory
     */
    private CatalogModelFactory() {
    }
    
    private static CatalogModelFactory instance = new CatalogModelFactory();
    
    public static CatalogModelFactory getInstance() {
        return instance;
    }
    
    protected CatalogModel createModel(ModelSource source) {
        return new CatalogModelImpl(source);
    }
    
    public static final String CATALOG_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\""
            +" standalone=\"no\"?>"+"\n"+
            "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\"/>";
    
    public CatalogModel getModel(ModelSource source) {
        Document doc = (Document) source.getLookup().lookup(Document.class);
        if( (doc != null) && doc.getLength() <= 5){
            //means the catalog file is empty now
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, CATALOG_TEMPLATE, null);
            } catch (BadLocationException ex) {
                return null;
            }
        }
        
        CatalogModel cm =(CatalogModel) super.getModel(source);
        try {
            cm.sync();
        } catch (IOException ex) {
        }
        return cm;
    }
}
