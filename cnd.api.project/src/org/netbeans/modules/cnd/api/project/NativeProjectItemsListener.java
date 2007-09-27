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

package org.netbeans.modules.cnd.api.project;

import java.util.List;

public interface NativeProjectItemsListener {
     /**
      * Called when a file is added to the project.
      * @param fileItem the file item that was added.
      */
     public void fileAdded(NativeFileItem fileItem);

     /**
      * Called when multiple files are added to the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesAdded(List<NativeFileItem> fileItems);
     
     /**
      * Called when a file is removed from the project.
      * @param fileItem the file item that was removed.
      */
     public void fileRemoved(NativeFileItem fileItem);

     /**
      * Called when multiple files are removed from the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesRemoved(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * the file needs to be re-parsed).
      * @param fileItem the file item that has changed.
      */
     public void filePropertiesChanged(NativeFileItem fileItem);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for multiple files.
      * @param fileItems the list of file items that has changed.
      */
     public void filesPropertiesChanged(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for all files in project.
      */
     public void filesPropertiesChanged();

     /**
      * Called when item name is changed.
      * @param oldPath the old file path.
      * @param newFileIetm the new file item.
      */
    void fileRenamed(String oldPath, NativeFileItem newFileIetm);
    
    /**
     * Called when the project is deleted
     * @param nativeProject project that is closed
     */
    void projectDeleted(NativeProject nativeProject);
}
