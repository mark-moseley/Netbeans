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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.lookup.Lookups;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.BuildException;

import org.openide.filesystems.FileUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sreenivasan Genipudi
 */
public class IDEValidateBPELProject extends Task {

    public void setSourceDirectory(String srcDir) {
      this.mSourceDirectory = srcDir;
    }

    public void setBuildDirectory(String buildDir) {
      this.mBuildDirectory = buildDir;
    }

    public void setRunValidation(String flag) {
      setAllowBuildWithError(flag);
      mAllowBuildWithError = !mAllowBuildWithError;
    }

    public void setAllowBuildWithError(String flag) {
      if (flag != null) {
        if (flag.equals("false")) {
          mAllowBuildWithError = false;
        }
        else if (flag.equals("true")) {
          mAllowBuildWithError = true;
        }
      }
    }

    public void setClasspathRef(Reference ref) {}

    public void setProjectClassPath(String projectClassPath) {
      this.mProjectClassPath = projectClassPath;
    }

    public void setBuildDependentProjectDir(String dependentProjectFilesDir) {
      this.mBuildDependentProjectFilesDirectory = dependentProjectFilesDir;
    }

    public boolean isFoundErrors() {
      return this.isFoundErrors;
    }

    public void execute() throws BuildException {
      if (this.mSourceDirectory == null) {
        throw new BuildException("No directory is set for source files.");
      }

      if (this.mBuildDirectory == null) {
        throw new BuildException("No build directory is set.");
      }

      if (this.mBuildDependentProjectFilesDirectory == null) {
        throw new BuildException("No dependentProjectFiles directory is set.");
      }
      try {
        this.mSourceDir = new File(this.mSourceDirectory);
        CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);

      }
      catch(Exception ex) {
        throw new BuildException("Failed to get File object for project source directory "+ this.mSourceDirectory, ex);
      }
      try {
        this.mBuildDir = new File(this.mBuildDirectory);
      } 
      catch(Exception ex) {
        throw new BuildException("Failed to get File object for project build directory "+ this.mBuildDirectory, ex);
      }
      ArrayList projectDirs = new ArrayList();
      
      if (this.mProjectClassPath != null && !this.mProjectClassPath.trim().equals("") && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
        StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
        
        while (st.hasMoreTokens()) {
          String spath = st.nextToken();

          try {
            File sFile =  new File(mSourceDir.getParentFile().getCanonicalPath() + File.separator + spath);
            File srcFolder = new File(sFile.getParentFile().getParentFile().getCanonicalFile(), "src");
            projectDirs.add(srcFolder);
          }
          catch (Exception e) {
            throw new BuildException("Failed to create File object for dependent project path "+ spath);
          }
        }
      }
      myBPELFiles = new ArrayList<BPELFile>();
      processBpelFilesFolderInBuildDir(this.mBuildDir);
      this.mDependentProjectDirs = projectDirs;
      ArrayList sourceDirs = new ArrayList();
      sourceDirs.add(this.mSourceDir);
      processSourceDirs(sourceDirs);
    }

    private void processBpelFilesFolderInBuildDir(File folder) {
      File files[] = folder.listFiles(new Util.BpelFileFilter());

      for(int i =0; i < files.length; i++) {
        File file = files[i];

        if (file.isFile()) {
          processBpelFilesInBuildDir(file);
        }
        else {
          processBpelFilesFolderInBuildDir(file);
        }
      }
    }

    private void processBpelFilesInBuildDir(File bpelFile) {
      String relativePath = RelativePath.getRelativePath(this.mBuildDir, bpelFile);
      this.mBpelFileNamesToFileInBuildDir.put(relativePath, bpelFile);
    }

    private void processSourceDirs(List sourceDirs) {
      Iterator it = sourceDirs.iterator();

      while(it.hasNext()) {
        File sourceDir = (File) it.next();
        processSourceDir(sourceDir);
      }
   }

    private void processSourceDir(File sourceDir) {
      processFileObject(sourceDir);
    }

    private void processFileObject(File file) {
      if (file.isDirectory()) {
        processFolder(file);
      }
    }

    private void processFolder(File fileDir) {
      File[] bpelFiles = fileDir.listFiles(new Util.BpelFileFilter());
      processBpelFiles(bpelFiles);
    }

    private void processFile(File file) {
      String fileName = file.getName();
      String fileExtension = null;
      int dotIndex = fileName.lastIndexOf('.');

      if (dotIndex != -1) {
        fileExtension = fileName.substring(dotIndex +1);
      }

      if (fileExtension != null) {
        if (fileExtension.equalsIgnoreCase("bpel")) {
          processBpelFile(file);
        }
      }
    }

    private void processBpelFiles(File[]bpelFiles) {
      for (int i = 0; i < bpelFiles.length; i++) {
        if (bpelFiles[i].isFile()) {
          processBpelFile(bpelFiles[i]);
        } 
        else {
          processFolder(bpelFiles[i]);
        }
      }
    }

    // vlv # 100036
    private void processBpelFile(File file) throws BuildException {
      BpelModel model = null;

      try {
        model = IDEBPELCatalogModel.getDefault().getBPELModel(file);
      }
      catch (Exception e) {
        throw new RuntimeException("Error while trying to get BPEL Model", e);
      }
      Process process = model.getProcess();
      
      if (process != null) {
          String qName = process.getName() + ", " + process.getTargetNamespace(); // NOI18N
          BPELFile current = new BPELFile(file, mSourceDir, qName);

          for (BPELFile bpel : myBPELFiles) {
            if (bpel.getQName().equals(qName)) {
              if ( !mAllowBuildWithError) { // # 106342
                throw new BuildException(
                  " \n" +
                  "BPEL files " + bpel.getName() + " and " + current.getName() + "\n" +
                  "have the same bpel process name and targetname space:\n" +
                  qName + " \n \n"
                );
              }
            }
          }
          myBPELFiles.add(current);
      }

      if (isBpelFileModified(file)) {
        loadAndValidateExistingBusinessProcess(file);
      }
    }

    private boolean isBpelFileModified(File bpelFile) {
      boolean modified = true;
      String relativePath = RelativePath.getRelativePath(this.mSourceDir, bpelFile);
      File bpelFileInBuildDir = (File) this.mBpelFileNamesToFileInBuildDir.get(relativePath);

      if (bpelFileInBuildDir != null) {
        if (bpelFileInBuildDir.lastModified() == bpelFile.lastModified()) {
          modified = false;
        }
      }
      return modified;
    }
    
    private void validateBPEL(File bpel) throws BuildException {
        org.netbeans.modules.xml.xam.Model model = null;

        try {
          model = IDEBPELCatalogModel.getDefault().getBPELModel(bpel);
        }
        catch (Exception ex) {
          throw new RuntimeException("Error while trying to create BPEL Model", e);
        }
        Validation validation = new Validation();
        validation.validate((org.netbeans.modules.xml.xam.Model)model, ValidationType.COMPLETE);
        Collection col = validation.getValidationResult();
        boolean isError = false;
        StringBuffer buffer = new StringBuffer();

        for (Iterator itr = col.iterator(); itr.hasNext();) {
          ResultItem resultItem = (ResultItem) itr.next();
          logValidationErrors(bpel, resultItem);

          if (resultItem.getType() == Validator.ResultType.ERROR) {
            String description = resultItem.getDescription();
            buffer.append("\nError: " + description);
            isError = true;
          }
        }
        if (isError) {
          throw new BuildException("\nFound validation errors:\n" + buffer);
        }
    }

    private void logValidationErrors(File bpelFile, ResultItem resultItem) {
      int lineNumber = 0;
      int columnNumber = 0;
      String errorDescription = resultItem.getDescription();
      String msgType = resultItem.getType().name();
      Component component = resultItem.getComponents();
      FileObject fileObj = null;
      File file = null;

      if (component != null) {
        lineNumber = ModelUtil.getLineNumber(component);
        columnNumber = ModelUtil.getColumnNumber(component);
        fileObj = (FileObject) component.getModel().getModelSource().getLookup().lookup(FileObject.class);
  
        if (fileObj != null) {
          file = FileUtil.toFile(fileObj);
        }
        showError(file,columnNumber, lineNumber,errorDescription,msgType );
      }
      else {
        columnNumber = resultItem.getColumnNumber();
        lineNumber = resultItem.getLineNumber();
        fileObj = (FileObject)resultItem.getModel().getModelSource().getLookup().lookup(FileObject.class);

        if (fileObj != null) {
          file = FileUtil.toFile(fileObj);
        }                
        showError(file,columnNumber, lineNumber,errorDescription ,msgType);
      }
    }

    private void showError(File file, int columnNumber, int lineNumber, String errorDescription, String msgType) {
        StringBuffer lineNumStr = new StringBuffer(5);
        StringBuffer columnNumStr = new StringBuffer(5);

        if (lineNumber != -1) {
          lineNumStr.append(":");
          lineNumStr.append(lineNumber);
          lineNumStr.append(":");
        }
        if (columnNumber != -1) {
          columnNumStr.append(" column:");
          columnNumStr.append(columnNumber);
          columnNumStr.append(" ");
        }
        msgType = msgType + ": ";
        StringBuffer msg = new StringBuffer(100);

        if (file != null) {
          msg.append(file.getPath());
        }
        msg.append(lineNumStr);
        msg.append(columnNumStr);
        msg.append(msgType);
        msg.append(errorDescription);
    }

    private void loadAndValidateExistingBusinessProcess(File bpelFile) throws BuildException {
      try {
        validateBPEL(bpelFile);
      }
      catch (Throwable ex) {
        if ( !mAllowBuildWithError) {
          StringWriter writer = new StringWriter();
          PrintWriter pWriter = new PrintWriter(writer);
          throw new BuildException(ex);
        }
      }
    }

    // ----------------------------
    private static class BPELFile {
      public BPELFile(File file, File project, String qName) {
        myFile = file;
        myProject = project;
        myQName = qName;
      }

      public String getQName() {
        return myQName;
      }

      public String getName() {
        String file = myFile.toString();
        String path = myProject.toString();

        if (file.startsWith(path)) {
          return file.substring(path.length() + 1);
        }
        return file;
      }

      private File myFile;
      private File myProject;
      private String myQName;
    }

    private String mSourceDirectory;
    private String mProjectClassPath;
    private String mBuildDirectory;
    private String mBuildDependentProjectFilesDirectory;
    private List mDependentProjectDirs;
    private File mSourceDir;
    private File mBuildDir;
    private Map mBpelFileNamesToFileInBuildDir = new HashMap();
    private boolean isFoundErrors = false;
    private boolean mAllowBuildWithError = false;
    private Logger logger = Logger.getLogger(IDEValidateBPELProject.class.getName());
    private List<BPELFile> myBPELFiles;
}
