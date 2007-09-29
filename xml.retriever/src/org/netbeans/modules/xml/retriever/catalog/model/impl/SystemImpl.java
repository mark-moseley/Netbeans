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

/*
 * SystemImpl.java
 *
 * Created on December 6, 2006, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogAttributes;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.System;
import org.w3c.dom.Element;

/**
 *
 * @author girix
 */
public class SystemImpl extends CatalogComponentImpl implements
        org.netbeans.modules.xml.retriever.catalog.model.System{
    
    public SystemImpl(CatalogModelImpl model, Element e) {
        super(model, e);
    }
    
    public SystemImpl(CatalogModelImpl model) {
        this(model, createElementNS(model, CatalogQNames.SYSTEM));
    }
    
    public void accept(CatalogVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getSystemIDAttr() {
        return getAttribute(CatalogAttributes.systemId);
    }
    
    public String getURIAttr() {
        return getAttribute(CatalogAttributes.uri);
    }
    
    public String getXprojectCatalogFileLocationAttr() {
        return getAttribute(CatalogAttributes.xprojectCatalogFileLocation);
    }
    
    public String getReferencingFileAttr() {
        return getAttribute(CatalogAttributes.referencingFile);
    }
    
    public void setSystemIDAttr(URI uri) {
        super.setAttribute(SYSTEMID_ATTR_PROP, CatalogAttributes.systemId,
                uri.toString());
    }
    
    public void setURIAttr(URI uri) {
        super.setAttribute(URI_ATTR_PROP, CatalogAttributes.uri,
                uri.toString());
    }
    
    public void setXprojectCatalogFileLocationAttr(URI uri) {
        super.setAttribute(XPROJECTREF_ATTR_PROP, 
                CatalogAttributes.xprojectCatalogFileLocation, uri.toString());
    }
    
    public void setReferencingFileAttr(URI uri) {
        super.setAttribute(REFFILE_ATTR_PROP, CatalogAttributes.referencingFile,
                uri.toString());
    }

}
