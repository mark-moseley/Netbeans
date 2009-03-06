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
package org.netbeans.modules.java.source.classpath;


import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=125)
public class CacheSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private String FILE_PROTOCOL = "file";  //NOI18N
    
    /** Creates a new instance of CacheSourceForBinaryQueryImpl */
    public CacheSourceForBinaryQueryImpl() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (!FILE_PROTOCOL.equals (binaryRoot.getProtocol())) {
            return null;
        }
        URL sourceURL = null;//Index.getSourceRootForClassFolder(binaryRoot);
        SourceForBinaryQuery.Result result = null;
        if (sourceURL != null) {            
            for ( SourceForBinaryQueryImplementation impl :Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation.class)) {
                if (impl != this) {
                    result = impl.findSourceRoots(sourceURL);
                    if (result != null) {
                        break;
                    }
                }
            }
            result = new R (sourceURL, result);
            }
        return result;
    }
    
    private static class R implements SourceForBinaryQuery.Result {
        
        private final FileObject sourceRoot;
        private final SourceForBinaryQuery.Result delegate;
        
        public R (final URL sourceRootURL, final SourceForBinaryQuery.Result delegate) {
            assert sourceRootURL != null;
            this.sourceRoot = URLMapper.findFileObject(sourceRootURL);
            this.delegate = delegate;
        }

        public void removeChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public void addChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public FileObject[] getRoots() {
            FileObject[] result;
            //Is here SFBQ.Result for root?
            if (delegate != null) {                
                //Yes - either [root*] or [] - nothing or unknown
                result = this.delegate.getRoots();
                if (result.length == 0) {
                    //nothing or unkown
                    if (this.sourceRoot != null && GlobalPathRegistry.getDefault().getSourceRoots().contains(this.sourceRoot)) {
                        //nothing
                        result = new FileObject[] {this.sourceRoot};
                    }                
                    else {
                        //unknown
                        result = new FileObject[0];
                    }
                }
            }else {            
                //No - unknown file - treat it like a source root
                if (this.sourceRoot == null) {
                    result = new FileObject[0];
                }
                else {
                    result = new FileObject[] {this.sourceRoot};
                }
            }
            return result;
        }                
    }            
}
