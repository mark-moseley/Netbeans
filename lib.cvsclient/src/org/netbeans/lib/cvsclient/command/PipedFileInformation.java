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
package org.netbeans.lib.cvsclient.command;

import java.io.*;

/**
 * Contains intercepted infomation from command standard output.
 * Actula data are held in temporary file.
 *
 */
public class PipedFileInformation extends FileInfoContainer {
    private File file;

    private String repositoryRevision;

    private String repositoryFileName;

    private File tempFile;

    private OutputStream tmpStream;

    public PipedFileInformation(File tempFile) {
        this.tempFile = tempFile;
        //this.tempFile.deleteOnExit();
        try {
            tmpStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        }
        catch (IOException ex) {
            // TODO
        }
    }

    /**
     * Returns the original file. For piped content see {@link #getTempFile()}.
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the original file.
     */
    protected void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the revision of the incoming file.
     */
    public String getRepositoryRevision() {
        return repositoryRevision;
    }

    /**
     * Sets the revision of the incoming file.
     */
    protected void setRepositoryRevision(String repositoryRevision) {
        this.repositoryRevision = repositoryRevision;
    }

    /**
     * Returns the filename in the repository.
     */
    public String getRepositoryFileName() {
        return repositoryFileName;
    }

    /**
     * Sets the repository filename.
     */
    protected void setRepositoryFileName(String repositoryFileName) {
        this.repositoryFileName = repositoryFileName;
    }

    /**
     * Adds the specified line to the temporary file.
     */
    protected void addToTempFile(byte[] bytes) throws IOException {
        if (tmpStream != null) {
            tmpStream.write(bytes);
        }
    }
    /**
     * Adds the specified line to the temporary file.
     */
    public void addToTempFile(byte[] bytes, int len) throws IOException {
        if (tmpStream != null) {
            tmpStream.write(bytes, 0, len);
        }
    }

    protected void closeTempFile() throws IOException {
        if (tmpStream != null) {
            tmpStream.flush();
            tmpStream.close();
        }
    }

    public File getTempFile() {
        return tempFile;
    }

}
