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
package org.netbeans.core;

import java.io.File;

import org.netbeans.performance.DataDescriptor;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;

/**
 * Describes data
 */
final class MUCDataDescriptor extends DataDescriptor {

    private int classCount;
    private int fsCount;

    private DataDescriptor dd;

    /** New MFSDataDescriptor */
    public MUCDataDescriptor(int classCount, int fsCount) {
        this.classCount = classCount;
        this.fsCount = fsCount;
    }

    /** Getter for classCount */
    public int getClassCount() {
        return classCount;
    }
    
    /** Getter for xmlfsCount */
    public int getFsCount() {
        return fsCount;
    }
    
    /** Getter for dd */
    public DataDescriptor getDD() {
        return dd;
    }
    
    /** Setter for dd */
    public void setDD(DataDescriptor dd) {
        this.dd = dd;
    }
    
    /** @return hashCode */
    public int hashCode() {
        return getClassName().hashCode(); 
    }
    
    /** @return boolean iff obj equals this */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof MUCDataDescriptor) {
            MUCDataDescriptor dd = (MUCDataDescriptor) obj;
            
            boolean flag = this.dd == dd.dd || this.dd == null || this.dd.equals(dd.dd);
            
            return flag && getClassName().equals(dd.getClassName()) && classCount == dd.classCount && fsCount == dd.fsCount;
        }
        
        return false;
    }
}
