/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;

/**
 * Factory for creating {@link JavaFileObject}s used by the {@link JavacParser}.
 * The unit tests may implement this interface to force the {@link JavacParser} to
 * use different implementation of the {@link JavaFileObject}.
 * @see JavacParser
 * @see JavaFileObject
 * @see JavaFileManager
 * @author Tomas Zezula
 */
public interface JavaFileObjectProvider {
    
    /**
     * Creates {@link JavaFileObject} for given file under given root.
     * @param fo for which the {@link JavaFileObject} should be created
     * @param root the owner root of the file
     * @param filter used to read the content
     * @param content of the file object if null, the snapshot from fo is taken
     * @return the {@link JavaFileObject}
     * @throws java.io.IOException on io failure.
     */
    public JavaFileObject createJavaFileObject (FileObject fo, FileObject root, JavaFileFilterImplementation filter, CharSequence content) throws IOException;
       
    /**
     * Forces the provider to refresh the content of the given file,
     * @param javaFileObject to be refreshed
     * @param content of the file object if null, the snapshot from fo is taken
     * @throws java.io.IOException on io failure.
     */
    public void update (JavaFileObject javaFileObject, CharSequence content) throws IOException;
}