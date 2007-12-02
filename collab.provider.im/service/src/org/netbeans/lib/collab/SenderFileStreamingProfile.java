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

package org.netbeans.lib.collab;
import java.io.*;
import java.security.DigestInputStream;

/**
 *
 *
 * @since version 0.1
 *
 */
public class SenderFileStreamingProfile extends SenderStreamingProfile {

    private File _file;
    private byte[] _hash;
    private String _desc;

    /**
     * Creates a new instance of SenderFileStreamingProfile
     * @param file The file which is being streamed
     * @param computeHash Computes the MD5 digest of the file.
     * @param description The description of about the file
     * @throws FileNotFoundException when the file passed to it does not exists
     */
    public SenderFileStreamingProfile(java.io.File file, boolean computeHash, String description) 
                                      throws FileNotFoundException
    {
        if (file == null || !file.exists()) throw new FileNotFoundException();
        _file = file;
        _desc = description;
        if (computeHash) {
            _hash = getHash(_file);
        }
    }

    /**
     * Get the file associated with this profile
     * @return The file associated with the profile
     */
    public java.io.File getFile() {
        return _file;
    }
    
    /**
     * Gets the MD5 hash of the file for the streaming
     * @return The MD5 hash
     */
    public byte[] getHash() {
        return _hash;
    }
    
    /**
     * Sets the MD5 hash of the file for the streaming
     * @param hash The MD5 hash
     */
    public void setHash(byte[] hash) {
        _hash = hash;
    }
    
    /**
     * Get the description associated with the stream
     * @return Description
     */
    public String getDescription() {
        return _desc;
    }
    
    private byte[] getHash(File f) {
        try {
            java.security.MessageDigest md5 = 
                java.security.MessageDigest.getInstance("MD5");
            md5.reset();
            byte b[] = new byte[1024];
            DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(f)), md5);
            int len = 0;
            while((len = dis.read(b)) != -1);
            dis.close();
            return md5.digest();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
