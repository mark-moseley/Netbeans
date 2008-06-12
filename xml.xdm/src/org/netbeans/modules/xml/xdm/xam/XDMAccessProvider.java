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
package org.netbeans.modules.xml.xdm.xam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.openide.loaders.DataObject;

/**
 *
 * @author Nam Nguyen
 */
public class XDMAccessProvider implements DocumentModelAccessProvider {
    
    /** Creates a new instance of XDMAccessProvider */
    public XDMAccessProvider() {
    }

    public DocumentModelAccess createModelAccess(AbstractDocumentModel model) {
        return new XDMAccess(model);
    }
    
    public Document loadSwingDocument(InputStream in) throws IOException, BadLocationException {
        Document sd = new BaseDocument(BaseKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sd.insertString(sd.getLength(), line+System.getProperty("line.separator"), null); // NOI18N
            }
        } finally {
            br.close();
        }
        return sd;
    }

    public Object getModelSourceKey(ModelSource source) {
        Object key = source.getLookup().lookup(DataObject.class);
        //Fix for IZ 112329: For referenced schemas in runtime catalog, there will be no DO,
        //hence we must return the Document as the key as an alternative.
        if(key != null)
            return key;
        return source.getLookup().lookup(Document.class);
    }
}
