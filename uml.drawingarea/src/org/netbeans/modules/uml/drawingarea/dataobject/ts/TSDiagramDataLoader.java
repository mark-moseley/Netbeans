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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.drawingarea.dataobject.ts;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class TSDiagramDataLoader extends UniFileLoader
{
    public static final String ETLD_EXTENSION = "etld"; // NOI18N
    public static final String ETLP_EXTENSION = "etlp"; // NOI18N
    public static final String REQUIRED_MIME = "text/x-tsdiagram";
    private static final long serialVersionUID = 1L;

    public TSDiagramDataLoader()
    {
        super("org.netbeans.modules.uml.drawingarea.dataobject.ts.TSDiagramDataObject");
    }

    @Override
    protected String defaultDisplayName()
    {
        return NbBundle.getMessage(
            TSDiagramDataLoader.class, "LBL_TSDiagram_loader_name");
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) 
        throws DataObjectExistsException, IOException
    {
        return new TSDiagramDataObject(FileUtil.findBrother(
            primaryFile, ETLD_EXTENSION), primaryFile, this);
    }
    
    @Override
    protected MultiDataObject.Entry createPrimaryEntry(
        MultiDataObject obj, FileObject primaryFile)
    {
        return new FileEntry(obj, primaryFile);
    }

    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
            FileObject secondaryFile)
    {
        assert ETLD_EXTENSION.equals(secondaryFile.getExt());
        FileEntry diagramEntry = new FileEntry(obj, secondaryFile);
        return diagramEntry;
    }
    
    @Override
    protected String actionsContext()
    {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo)
    {
        // never recognize folders.
        if (fo.isFolder()) return null;
        String ext = fo.getExt();
        if (ext.equals(ETLD_EXTENSION))
            return FileUtil.findBrother(fo, ETLD_EXTENSION);
        
        FileObject etlpFile = findDiagramPrimaryFile(fo);
        return etlpFile != null && FileUtil.findBrother(
            etlpFile, ETLD_EXTENSION) != null ? etlpFile : null;
    }
    
    private FileObject findDiagramPrimaryFile(FileObject fo)
    {
        if (fo.getExt().equals(ETLP_EXTENSION))
            return fo;
        return null;
    }

}
