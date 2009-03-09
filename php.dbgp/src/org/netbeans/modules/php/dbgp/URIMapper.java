/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Converts remote (or also local) URI to local project File an vice versa
 * @author Radek Matous
 */
abstract class URIMapper {
    private static final Logger LOGGER = Logger.getLogger(URIMapper.class.getName());
    abstract File toSourceFile(URI remoteURI);
    abstract URI toWebServerURI(File localFile, boolean includeHostPart);

    URI toWebServerURI(File localFile) {
        return toWebServerURI(localFile, true);
    }

    static URIMapper.MultiMapper createMultiMapper(URI webServerURI, FileObject sourceFileObj, FileObject sourceRoot) {
        MultiMapper mergedMapper = new MultiMapper();
        URIMapper defaultMapper = createDefaultMapper(webServerURI, sourceFileObj, sourceRoot);
        if (defaultMapper != null) {
            mergedMapper.addAsFirstMapper(defaultMapper);
        }
        return mergedMapper;
    }
    static URIMapper createDefaultMapper(URI webServerURI, FileObject sourceFileObj, FileObject sourceRoot) {
        File webServerFile = new File(webServerURI);
        File sourceFile = FileUtil.toFile(sourceFileObj);
        String sourcePath = FileUtil.getRelativePath(sourceRoot, sourceFileObj);
        //debugged file must be part of the debugged project (for now)
        if (sourcePath != null) {
            if (sourceFile.isDirectory()) {
                //TODO: not sure about this (should be reviewed)
                sourceFile = new File(sourceFile, webServerFile.getName());
                if (!sourceFile.exists()) {
                    LOGGER.fine("No default path mapping: "+//NOI18N
                            "webServerURI: "+webServerURI.toString() + " sourceFile: " + sourceFile.getAbsolutePath());//NOI18N
                    return null;
                }
            }
            if (sourceFile.equals(webServerFile)) {
                return createOneToOne();
            } else {
                File sourceRootFile = FileUtil.toFile(sourceRoot);
                assert sourceRootFile != null;
                //File[] bases = findBases(webServerFile, sourceFile, sourceRootFile);
                URI[] bases = findBases(webServerURI, sourceFile, sourceRootFile);                
                if (bases != null) {
                    URI webServerBase = bases[0];
                    File sourceBase = new File(bases[1]);
                    assert webServerBase != null;
                    assert sourceBase != null;
                    return new BasesMapper(webServerBase, sourceBase, true);
                }
            }
        }
        //no decision how to map - must exist user's defined mapping
        LOGGER.fine("No default path mapping: " +//NOI18N
                "webServerURI: " + webServerURI.toString() + " sourceFile: " + sourceFile.getAbsolutePath());//NOI18N
        return null;
    }

    static URIMapper createOneToOne() {
        return new URIMapper() {

            @Override
            File toSourceFile(URI remoteURI) {
                return new File(remoteURI);
            }

            @Override
            URI toWebServerURI(File localFile, boolean includeHostPart) {
                return toURI(localFile, includeHostPart);
            }

        };
    }

    static URIMapper createBasedInstance(URI baseRemoteURI, File baseLocalFolder) {
        return createBasedInstance(baseRemoteURI, baseLocalFolder, false);
    }
    static URIMapper createBasedInstance(URI baseRemoteURI, File baseLocalFolder, boolean isDefault) {
        return new BasesMapper(baseRemoteURI, baseLocalFolder, isDefault);
    }

    private static URI[] findBases(URI webServerURI, File sourceFile, File sourceRoot) {
        File baseFile = sourceFile;
        boolean nullRetVal = true;
        List<String> pathFragments = new ArrayList<String>();
        Collections.addAll(pathFragments, webServerURI.getPath().split("/"));//NOI18N
        Collections.reverse(pathFragments);
        for (String path : pathFragments) {
            if (baseFile != null && path.equals(baseFile.getName()) && !baseFile.equals(sourceRoot)) {
                nullRetVal = false;
                if (baseFile.equals(sourceRoot)) {
                    break;
                }
                baseFile = baseFile.getParentFile();
            } else {
                break;
            }
        }
        if (nullRetVal) {
            return null;
        }
        assert baseFile.isDirectory();
        int basePathLen = webServerURI.getPath().length() -
                (sourceFile.getAbsolutePath().length() - baseFile.getAbsolutePath().length());
        String basePath = webServerURI.getPath().substring(0, basePathLen);
        URI baseURI = createURI(webServerURI.getScheme(), webServerURI.getHost(),
               basePath, webServerURI.getFragment(),
                true, true);
        return new URI[]{baseURI, baseFile.toURI()};
    }

    private static class BasesMapper extends URIMapper {

        private static final String FILE_SCHEME = "file";
        private URI baseWebServerURI;
        private URI baseSourceURI;
        private File baseSourceFolder;
        private boolean isDefault;

        BasesMapper(URI baseWebServerURI, File baseSourceFolder, boolean isDefault) {
            this.isDefault = isDefault;
            if (!baseSourceFolder.exists()) {
                throw new IllegalArgumentException();
            }
            if (!baseSourceFolder.isDirectory()) {
                throw new IllegalArgumentException();
            }

            this.baseSourceFolder = baseSourceFolder;
            this.baseWebServerURI = baseWebServerURI;

            assert FILE_SCHEME.equals(baseWebServerURI.getScheme());
            assert baseWebServerURI.getPath().endsWith("/") : baseWebServerURI.getPath();//NOI18N
            assert baseWebServerURI.isAbsolute();
            this.baseSourceURI = baseSourceFolder.toURI();
            assert baseSourceURI.isAbsolute();
        }

        @Override
        File toSourceFile(URI webServerURI) {
            URI relativizedURI = baseWebServerURI.relativize(webServerURI);
            if (!relativizedURI.isAbsolute()) {
                assert FILE_SCHEME.equals(webServerURI.getScheme());
                return new File(baseSourceURI.resolve(relativizedURI));
            }
            return isDefault ? new File(webServerURI) : null;
        }

        @Override
        URI toWebServerURI(File sourceFile, boolean includeHostPart) {
            if (sourceFile.equals(baseSourceFolder)) {
                return baseWebServerURI;
            } else {
                URI relativizedURI = baseSourceURI.relativize(sourceFile.toURI());
                if (!relativizedURI.isAbsolute()) {
                    URI retval = baseWebServerURI.resolve(relativizedURI);
                    retval = createURI(retval.getScheme(), retval.getHost(),
                            retval.getPath(), retval.getFragment(),
                            true, false);
                    return retval;
                }
            }
            return isDefault ? toURI(sourceFile, includeHostPart) : null;
        }
    }

    static class MultiMapper extends URIMapper {
        private LinkedList<URIMapper> mappers = new LinkedList<URIMapper>();

        MultiMapper addAsFirstMapper(URIMapper mapper) {
            mappers.addFirst(mapper);
            return this;
        }

        MultiMapper addAsLastMapper(URIMapper mapper) {
            mappers.addLast(mapper);
            return this;
        }

        @Override
        File toSourceFile(URI remoteURI) {
            for (URIMapper mapperInstance : mappers) {
                File sourceFile = mapperInstance.toSourceFile(remoteURI);
                if (sourceFile != null) {
                    return sourceFile;
                }
            }
            return null;
        }

        @Override
        URI toWebServerURI(File localFile, boolean includeHostPart) {
            for (URIMapper mapperInstance : mappers) {
                URI toWebServerURI = mapperInstance.toWebServerURI(localFile, includeHostPart);
                if (toWebServerURI != null) {
                    return toWebServerURI;
                }
            }
            return null;
        }
    }

    private static URI createURI(String scheme, String host, String path, String fragment,
            boolean includeHostPart, boolean pathEndsWithSlash) {
        if (pathEndsWithSlash && !path.endsWith("/")) {//NOI18N
            path = path + "/"; //NOI18N
        }
        if (host == null && includeHostPart) {
            host = ""; //NOI18N
        }
        try {
            return new URI(scheme, host, path, fragment);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static URI toURI(File webServerBase, boolean includeHostPart) {
        URI webServerBaseURI = webServerBase.toURI();
        return createURI(webServerBaseURI.getScheme(), webServerBaseURI.getHost(),
                webServerBaseURI.getPath(),webServerBaseURI.getFragment(),
                includeHostPart, webServerBase.exists() && webServerBase.isDirectory());
    }
}
