/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.download.connection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.netbeans.installer.sandbox.download.Download;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public class ResourceConnection extends Connection {
    private String name;
    private ClassLoader classLoader;
    
    private InputStream inputStream;
    
    public ResourceConnection(String aName, ClassLoader aClassLoader) throws InitializationException {
        if (aName != null) {
            name = aName;
        } else {
            throw new InitializationException("The supplied resource name cannot be null.");
        }
        
        if (aClassLoader != null) {
            classLoader = aClassLoader;
        } else {
            classLoader = getClass().getClassLoader();
        }
    }
    
    public void open() throws IOException {
        assert inputStream == null;
        
        try {
            inputStream = classLoader.getResource(name).openStream();
        } catch (NullPointerException e) {
            throw new FileNotFoundException();
        }
    }
    
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
    
    public int read(byte[] buffer) throws IOException {
        assert inputStream != null;
        
        return inputStream.read(buffer);
    }
    
    public int available() throws IOException {
        return inputStream.available();
    }
    
    public boolean supportsRanges() {
        return false;
    }
    
    public long getContentLength() {
        return Download.UNDEFINED_LENGTH;
    }
    
    public Date getModificationDate() {
        return new Date();
    }
}
