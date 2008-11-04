/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.diff;

import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.modules.diff.builtin.ContextualPatch;
import org.openide.filesystems.FileObject;

import java.nio.charset.Charset;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * FEQ for .patch and .diff files generated by the IDE.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileEncodingQueryImplementation.class)
public class DiffFileEncodingQueryImplementation extends FileEncodingQueryImplementation {

    public Charset getEncoding(FileObject file) {
        if (!"text/x-diff".equals(file.getMIMEType())) return null;
        return generatedByIDE(file) ? Charset.forName("UTF-8") : null;
    }

    private boolean generatedByIDE(FileObject file) {
        if (file.getSize() <= ContextualPatch.MAGIC.length()) return false;
        byte [] buffer = new byte[ContextualPatch.MAGIC.length()];
        InputStream is = null;
        try {
            is = file.getInputStream();
            int n = is.read(buffer);
            return n > 0 && ContextualPatch.MAGIC.startsWith(new String(buffer, 0, n, "US-ASCII"));
        } catch (IOException e) {
            Logger.getLogger(DiffFileEncodingQueryImplementation.class.getName()).log(Level.INFO, "FEQ failed", e);
        } finally {
            if (is != null) try { is.close(); } catch (IOException e) {}
        }
        return false;
    }
}
