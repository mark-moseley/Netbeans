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

package org.netbeans.modules.cnd.refactoring.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
/**
 *  org.netbeans.modules.refactoring.java.plugins.RetoucheCommit
 * @author Jan Becicka
 */
public class RefactoringCommit implements Transaction {
    ArrayList<BackupFacility.Handle> ids = new ArrayList<BackupFacility.Handle>();
    private boolean commited = false;
    Collection<ModificationResult> results;
    private Set<File> newFiles = new HashSet<File>();
    
    public RefactoringCommit(Collection<ModificationResult> results) {
        this.results = results;
    }
    
    public void commit() {
        try {
            if (commited) {
                for (BackupFacility.Handle id:ids) {
                    try {
                        id.restore();
                    } catch (IOException ex) {
                        throw (RuntimeException) new RuntimeException().initCause(ex);
                    }
                }
                // need to force update in modified files
            } else {
                commited = true;
                for (ModificationResult result:results) {
                    ids.add(BackupFacility.getDefault().backup(result.getModifiedFileObjects()));
                    newFiles.addAll(result.getNewFiles());
                    result.commit();
                }
            }
            
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
    }
    
    private boolean newFilesStored = false;
    public void rollback() {
        for (BackupFacility.Handle id:ids) {
            try {
                id.restore();
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
        }
        // need to force update in modified and new files
        if (newFiles!=null) {
            for (File f:newFiles) {
                try {
                    FileObject fo = FileUtil.toFileObject(f);
                    if (!newFilesStored) {
                        ids.add(BackupFacility.getDefault().backup(fo));
                        newFilesStored = true;
                    }
                    fo.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
