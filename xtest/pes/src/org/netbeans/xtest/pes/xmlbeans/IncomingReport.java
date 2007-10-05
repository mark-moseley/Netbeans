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
 * IncomingReport.java
 *
 * Created on May 27, 2002, 9:50 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pe.*;

import java.io.*;

/**
 *
 * @author  breh
 */
public class IncomingReport extends ManagedReport {

    /** Creates a new instance of IncomingReport */
    public IncomingReport() {
    }


    // attributes
    public String xmlat_reportRoot;
    public boolean xmlat_replace;

    // getters/setters
    public String getReportRoot() {
        return xmlat_reportRoot;
    }
    
    public File getReportDir() throws IOException {
        if (getReportRoot()!=null) {
            File root = new File (getReportRoot());
            if (root.isDirectory()) {
                return root; 
            } else {
                throw new IOException("reportRoot is not valid directory");
            }
        } else {
            throw new IOException("reportRoot is not specified, cannot return reportDir");
        }
    }
    
    public void setReportRoot(String reportRoot) {
        xmlat_reportRoot = reportRoot;
    }
    
    public boolean isReplace() {
        return xmlat_replace;
    }
    
    public void setReplace(boolean replace) {
        xmlat_replace = replace;
    }
    
    // bussiness methods
    // is report valid ?
    private boolean valid;
    private String invalidMessage = "";
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /** Sets a validity and describing message. */
    public void setValid(boolean valid, String message) {
        this.valid = valid;
        this.invalidMessage = message;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getInvalidMessage() {
        return this.invalidMessage;
    }
    
    // is this report used only for reconfiguration
    private boolean reconfiguration=false;
    
    public void setReconfiguration(boolean reconfiguration) {
        this.reconfiguration = reconfiguration;
    }
    
    public boolean isReconfiguration() {
        return reconfiguration;
    }
    
    // name of the archive where report is packed
    private File archiveFile;
    
    public void setArchiveFile(File archive) {
        archiveFile = archive;
    }
    
    public File getArchiveFile() {
        return archiveFile;
    }
    

    // check validity of the report
    // this means, report has all xml and html files generated, including index.html
    public boolean areReportFilesValid() {
        try {
            File reportRootDir = this.getReportDir();            
            if (!areReportFilesValid(reportRootDir)) return false;
        } catch (IOException ioe) {
            // a problem - not valid
            return false;
        }
        return true;
    }


    public void readIncomingReport(IncomingReport ir) {
        readManagedReport(ir);
        this.xmlat_reportRoot = ir.xmlat_reportRoot;
        this.xmlat_replace = ir.xmlat_replace;
    }
    
   public static IncomingReport loadIncomingReportFromFile(File reportFile) throws IOException {
       try {
           XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
           if (!(xmlBean instanceof IncomingReport)) {
               throw new IOException("Loaded file "+reportFile+" does not contain IncomingReport");
           }
           return (IncomingReport)xmlBean;
       } catch (ClassNotFoundException cnfe) {
           IOException ioe = new IOException("Loaded file "+reportFile+" does not contain IncomingReport, caused by ClassNotFoundException :"+cnfe.getMessage());
           ioe.initCause(cnfe);
           throw ioe;
       }
    }

    
    
}

