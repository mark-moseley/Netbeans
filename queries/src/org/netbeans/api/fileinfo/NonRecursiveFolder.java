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

package org.netbeans.api.fileinfo;

import org.openide.filesystems.FileObject;

/**
 * Marker interface for representation of a folder without subfolders.
 * When an implementation of this interface is contained in the
 * lookup of a node, actions on that node should not process the subfolders
 * of this folder.
 * It is permitted for a Node to have both the {@link org.openide.loaders.DataFolder}
 * and NonRecursiveFolder in its lookup. In this case the {@link NonRecursiveFolder#getFolder} has to
 * return the same {@link FileObject} as the {@link org.openide.loaders.DataFolder#getPrimaryFile} method.
 * Any action which checks for the NonRecursiveFolder at all must prefer it to
 * the DataFolder since it is considered to be more specific information.
 *
 * @author  Martin Entlicher
 * @since 1.4
 */
public interface NonRecursiveFolder {
    
    /**
     * Get the folder file object, which represents the non-recursive folder.
     * Only direct children should be processed, no sub-folders.
     * @return The file object that represents non-recursive folder.
     */
    FileObject getFolder();
    
}
