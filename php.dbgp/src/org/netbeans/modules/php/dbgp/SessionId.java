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
package org.netbeans.modules.php.dbgp;

import java.io.File;

import java.net.URI;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.api.Pair;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * This class is used for session identifying between IDE and Debugger.
 * Session id is based on file requested for debug.
 * It is used for mapping remote files to local files based on
 * session information.
 *
 *
 * @author ads, Radek Matous
 *
 */
public class SessionId {
    //keep synchronized with org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants
    private static final String SOURCES_TYPE_PHP = "PHPSOURCE"; // NOI18N
    private URIMapper.MultiMapper uriMapper;
    private String id;
    private final FileObject sessionFileObject;

    public SessionId( FileObject fileObject ) {
        id = getSessionPrefix();
        sessionFileObject = fileObject;
    }
    public String getId() {
        return id;
    }
    public Project getProject(){
        return FileOwnerQuery.getOwner( sessionFileObject );
    }

    synchronized void initialize(String uri, List<Pair<String, String>> pathMapping) {
        // XXX needs to be fixed after changes from main-whatever are propagated to web-main
    }

    synchronized void initialize(String uri, Pair<String, String> pathMapping) {
        if (uriMapper == null) {
            Project project = getProject();
            FileObject sourceRoot = project != null ? getSourceRoot() : sessionFileObject.getParent();
            uriMapper = URIMapper.createMultiMapper(URI.create(uri),
                    sessionFileObject, sourceRoot, pathMapping);
        }
        notifyAll();
        SessionProgress s = SessionProgress.forSessionId(this);
        if (s != null) {
            s.notifyConnectionFinished();
        }
    }
    public synchronized boolean isInitialized(boolean waitForInitialization) {
        boolean isInitialized =  uriMapper != null;
        if (!isInitialized && waitForInitialization) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            isInitialized =  uriMapper != null;
        }
        return isInitialized;
    }
    /**
     * Converts file in project directory to URI in document
     * root of web server (local or remote because both local|remote debugging
     * is supported now)
     * @param localFile 
     * @return uri URI in document root of web server
     */
    public String toWebServerURI( FileObject localFile ) {
        if (uriMapper != null) {
            File file = FileUtil.toFile(localFile);
            assert file != null;
            URI uri = uriMapper.toWebServerURI(file);
            if (uri != null) {
                return uri.toString();
            }
        }
        return null;
    }
    public FileObject toSourceFile( String uri ){
        if (uriMapper != null) {
            File localFile = uriMapper.toSourceFile(URI.create(uri));
            localFile = (localFile != null) ? FileUtil.normalizeFile(localFile) : null;
            return (localFile != null) ? FileUtil.toFileObject(localFile) : null;
        }
        return null;
    }
    private FileObject getSourceRoot() {
        final FileObject[] sourceObjects = getSourceObjects(getProject());
        return (sourceObjects != null && sourceObjects.length > 0) ? sourceObjects[0] : null;
    }
    private static FileObject[] getSourceObjects(Project phpProject) {
        SourceGroup[] groups = getSourceGroups(phpProject);
        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }
    private static SourceGroup[] getSourceGroups(Project phpProject) {
        Sources sources = ProjectUtils.getSources(phpProject);
        SourceGroup[] groups = sources.getSourceGroups(SOURCES_TYPE_PHP);//NOI18N
        return groups;
    }
    private String getSessionPrefix() {
        return PhpOptions.getInstance().getDebuggerSessionId();
    }
}
