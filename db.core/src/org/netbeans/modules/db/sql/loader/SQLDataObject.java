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

package org.netbeans.modules.db.sql.loader;

import java.nio.charset.Charset;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class SQLDataObject extends MultiDataObject {

    private Lookup lookup;

    public SQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new SQLEditorSupport(this));
        cookies.assign(FileEncodingQueryImpl.class, new FileEncodingQueryImpl());
    }

    @Override
    protected Node createNodeDelegate() {
        return new SQLNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    public boolean isConsole() {
        try {
            // the "console" files are stored in the SFS
            return "nbfs".equals(getPrimaryFile().getURL().getProtocol()); // NOI18N
        } catch (FileStateInvalidException e) {
            return false;
        }
    }

    void addCookie(Node.Cookie cookie) {
        getCookieSet().add(cookie);
    }

    void removeCookie(Node.Cookie cookie) {
        getCookieSet().remove(cookie);
    }

    private final class FileEncodingQueryImpl extends FileEncodingQueryImplementation {

        public Charset getEncoding(FileObject file) {
            // the "console" files are always in UTF-8
            if (isConsole()) {
                return Charset.forName("UTF-8"); // NOI18N
            }
            return null;
        }
    }
}
