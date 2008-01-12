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
package org.netbeans.modules.xml.text;

import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.sync.*;

/**
 * Takes care about specifics of DTD text representation.
 * I.e. no update() is implemented.
 *
 * @author  Petr Kuzel
 * @version
 */
public class DTDTextRepresentation extends TextRepresentation {

    /** Creates new DTDTextRepresentation */
    public DTDTextRepresentation(TextEditorSupport editor, Synchronizator sync) {
        super(editor, sync);
    }

    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {

        if (change instanceof String) {
            String update = (String) change;
            updateText(update);
        }
    }
    
    /** Update tree from selected source.  */
    private void updateText(Object source) {
        //!!! tree is read only
    }
    
    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false; //es.isModified();
    }
        
}
