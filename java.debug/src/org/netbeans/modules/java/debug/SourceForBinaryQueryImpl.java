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
package org.netbeans.modules.java.debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {
    
    public SourceForBinaryQueryImpl() {
    }
    
    public synchronized Result findSourceRoots(URL binaryRoot) {
        try {
            String binaryRootS = binaryRoot.toExternalForm();
            URL result = null;
            if (binaryRootS.startsWith("jar:file:")) { // NOI18N
                if (binaryRootS.endsWith("/java/source/javacapi/external/javac-api-nb-7.0-b07.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/java/source/javacapi/external/javac-api-nb-7.0-b07.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                } else if (binaryRootS.endsWith("/java/source/javacimpl/external/javac-impl-nb-7.0-b07.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/java/source/javacimpl/external/javac-impl-nb-7.0-b07.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                } else if (binaryRootS.endsWith("/libs.javacapi/external/javac-api-nb-7.0-b07.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/libs.javacapi/external/javac-api-nb-7.0-b07.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                } else if (binaryRootS.endsWith("/libs.javacimpl/external/javac-impl-nb-7.0-b07.jar!/")) { // NOI18N
                    result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/libs.javacimpl/external/javac-impl-nb-7.0-b07.jar!/".length()) + "/retouche/Jsr199/src"); // NOI18N
                }
                final FileObject resultFO = result != null ? URLMapper.findFileObject(result) : null;
                if (resultFO != null) {
                    return new Result() {
                        public FileObject[] getRoots() {
                            return new FileObject[] {resultFO};
                        }
                        public void addChangeListener(ChangeListener l) {}
                        public void removeChangeListener(ChangeListener l) {}
                    };
                }
            }
        } catch (MalformedURLException e) {
            Logger.getLogger("global").log(Level.INFO, null, e); //NOI18N
        }
        return null;
    }

}
