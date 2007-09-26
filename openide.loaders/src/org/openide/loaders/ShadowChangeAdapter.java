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

package org.openide.loaders;

import java.util.EventObject;

import org.openide.util.Lookup;
import org.openide.filesystems.*;

/** Adapter for listening on changes of fileobjects and refreshing data
* shadows/broken links
*
* @author Ales Kemr
*/
class ShadowChangeAdapter extends Object implements OperationListener {

    /** Creates new ShadowChangeAdapter */
    ShadowChangeAdapter() {

        /* listen on loader pool to refresh datashadows after
        * create/delete dataobject
        */
        DataLoaderPool.getDefault().addOperationListener(this);
    }

    /** Checks for BrokenDataShadows */
    static void checkBrokenDataShadows(EventObject ev) {
        BrokenDataShadow.checkValidity(ev);
    }
    
    /** Checks for DataShadows */
    static void checkDataShadows(EventObject ev) {
        DataShadow.checkValidity(ev);
    }
    
    /** Object has been recognized by
     * {@link DataLoaderPool#findDataObject}.
     * This allows listeners
     * to attach additional cookies, etc.
     *
     * @param ev event describing the action
    */
    public void operationPostCreate(OperationEvent ev) {
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully copied.
     * @param ev event describing the action
    */
    public void operationCopy(OperationEvent.Copy ev) {
    }
    
    /** Object has been successfully moved.
     * @param ev event describing the action
    */
    public void operationMove(OperationEvent.Move ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully deleted.
     * @param ev event describing the action
    */
    public void operationDelete(OperationEvent ev) {
        checkDataShadows(ev);
    }
    
    /** Object has been successfully renamed.
     * @param ev event describing the action
    */
    public void operationRename(OperationEvent.Rename ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** A shadow of a data object has been created.
     * @param ev event describing the action
    */
    public void operationCreateShadow(OperationEvent.Copy ev) {
    }
    
    /** New instance of an object has been created.
     * @param ev event describing the action
    */
    public void operationCreateFromTemplate(OperationEvent.Copy ev) {
        checkBrokenDataShadows(ev);
    }
    
}
