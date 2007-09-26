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

package org.netbeans.modules.pdf;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/** Loader for PDF files (Portable Document Format).
 * Permits simple viewing of them.
 * @author Jesse Glick
 */
public class PDFDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    private static final long serialVersionUID = -4354042385752587850L;
    /** MIME-type of PDF files */
    private static final String PDF_MIME_TYPE = "application/pdf";      //NOI18N

    
    /** Creates loader. */
    public PDFDataLoader() {
        super("org.netbeans.modules.pdf.PDFDataObject"); // NOI18N
    }

    
    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize () {
        super.initialize();

        ExtensionList extensions = new ExtensionList ();
        extensions.addMimeType(PDF_MIME_TYPE);
        extensions.addMimeType("application/x-pdf");                    //NOI18N
        extensions.addMimeType("application/vnd.pdf");                  //NOI18N
        extensions.addMimeType("application/acrobat");                  //NOI18N
        extensions.addMimeType("text/pdf");                             //NOI18N
        extensions.addMimeType("text/x-pdf");                           //NOI18N
        setExtensions (extensions);
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage (PDFDataLoader.class, "LBL_loaderName");
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/application/pdf/Actions/";                      //NOI18N
    }
    
    /** Creates multi data objcte for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new PDFDataObject (primaryFile, this);
    }

}
