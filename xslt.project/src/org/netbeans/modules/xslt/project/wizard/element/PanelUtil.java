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
package org.netbeans.modules.xslt.project.wizard.element;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ModelSource;

import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;

import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.modules.xml.catalogsupport.util.ProjectVisitor;
import org.netbeans.modules.xml.catalogsupport.util.ProjectUtilities;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.16
 */
final class PanelUtil {

  private PanelUtil() {}

  public static FileObject copyFile(
    FileObject destination,
    String path,
    String file,
    String name,
    String ext)
  {
    if (name == null || destination == null) {
      return null;
    }
    try {
      return FileUtil.copyFile(
        Repository.getDefault().getDefaultFileSystem().findResource(path + file),
        destination,
        name,
        ext);
    }
    catch (IOException ex) {
        ErrorManager.getDefault().notify(ex);
    }
    return null;
  }

  public static boolean isValidFileName(String fileName) {
    boolean isAllowSlash = false;
    boolean isAllowBackslash = false;

    if (File.separatorChar == '\\') {
        isAllowBackslash = true;
        fileName = fileName.replace('/', File.separatorChar);
    } else {
        isAllowSlash = true;
    }
    StringTokenizer dirTokens = new StringTokenizer(fileName, File.separator);
    int numDirs = dirTokens.countTokens();
    String[] dirs = new String[numDirs];
    int i = 0;

    while (dirTokens.hasMoreTokens()) {
        dirs[i] = dirTokens.nextToken();
        i++;
    }
    return
      !(fileName == null 
      || fileName.length() == 0 
      || fileName.indexOf("\\") == 0
      || fileName.indexOf("/") == 0
      || (!isAllowBackslash && fileName.indexOf("\\") >= 0 )
      || (!isAllowSlash && fileName.indexOf("/") >= 0)
      || fileName.indexOf(':') >= 0 
      || !isValidName(dirs));
  }

  public static boolean isValidName(String[] dirs) {
    if (dirs == null || dirs.length == 0) {
        return false;
    }
    boolean isValid = true;
    for (String dir : dirs) {
        isValid = isValidName(dir);
        if (!isValid) {
            break;
        }
    }
    return isValid;
  }
  
  public static boolean isValidName(String fileName) {
    try {
        boolean bValid = true;
        File tempFile = new File(fileName);
        String tempFileName = "00" + fileName;
        File actualTempFile = File.createTempFile(tempFileName, null);

        if (!FileUtil.normalizeFile(tempFile).equals(tempFile.getCanonicalFile())) {
            bValid = false;
        }
        actualTempFile.delete();
        actualTempFile = null;
        tempFile = null;
        return bValid;
    } catch (Exception e) {
        return false;
    }
  }
}
