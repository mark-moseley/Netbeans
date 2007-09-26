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

package org.netbeans.modules.palette;

import org.openide.ErrorManager;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Libor Kotouc
 */
class ActiveEditorDropProvider implements InstanceContent.Convertor<String,ActiveEditorDrop> {

    private static ActiveEditorDropProvider instance = new ActiveEditorDropProvider();

    /** Creates a new instance of ActiveEditorDropProvider */
    private ActiveEditorDropProvider() {
    }

    static ActiveEditorDropProvider getInstance() {
        return instance;
    }
    
    public Class<? extends ActiveEditorDrop> type(String obj) {
        //able to convert String instances only
        return ActiveEditorDrop.class;
    }

    public String id(String obj) {
        return obj;
    }

    public String displayName(String obj) {
        return obj;
    }

    public ActiveEditorDrop convert(String obj) {
        return getActiveEditorDrop(obj);
    }
    
    private ActiveEditorDrop getActiveEditorDrop(String instanceName) {

        ActiveEditorDrop drop = null;

        if (instanceName != null && instanceName.trim().length() > 0) {//we should try to instantiate item drop
            try {
                ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                if (loader == null)
                    loader = getClass ().getClassLoader ();
                Class instanceClass = loader.loadClass (instanceName);
                drop = (ActiveEditorDrop)instanceClass.newInstance();
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        return drop;
    }
    
}
