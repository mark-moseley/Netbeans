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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.insync;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 * Repository whose getDefaultFileSystem returns a writeable FS containing
 * the layers of j2seplatform and visual jsf web project. It is put in the 
 * default lookup, thus it is returned by Repository.getDefault().
 *
 */
public class RepositoryImpl extends Repository {
    
    public RepositoryImpl() {
        super(createDefFs());
    }
    
    private static FileSystem createDefFs() {
        try {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem layerJ2sePlatform = new XMLFileSystem(
                    RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/java/j2seplatform/resources/layer.xml"));
            FileSystem layerVisualWebProject = new XMLFileSystem(
                    RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/visualweb/project/jsf/resources/layer.xml"));
            return new MultiFileSystem(new FileSystem[] {writeFs, layerJ2sePlatform, layerVisualWebProject});
        } catch (SAXException e) {
            AssertionError ae = new AssertionError(e.getMessage());
            ae.initCause(e);
            throw ae;
        }
    }
}
