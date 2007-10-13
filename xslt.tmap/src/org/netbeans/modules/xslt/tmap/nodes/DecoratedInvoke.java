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

package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedInvoke extends DecoratedTMapComponentAbstract<Invoke>{

    public DecoratedInvoke(Invoke orig) {
        super(orig);
    }

    @Override
    public String getHtmlDisplayName() {
        Invoke ref = getOriginal();
        String pltName = null;
        String roleName = null;
        String opName = null;
        if (ref != null) {
            pltName = Util.getReferenceLocalName(ref.getPartnerLinkType());
            roleName = Util.getReferenceLocalName(ref.getRole());
            opName = Util.getReferenceLocalName(ref.getOperation());
        }
        String addon = null;
        if (pltName != null) {
            addon = TMapComponentNode.WHITE_SPACE+pltName; // NOI18N
        }
        
        if (roleName != null) {
            addon = (addon == null ? TMapComponentNode.EMPTY_STRING : addon+TMapComponentNode.WHITE_SPACE) + roleName; // NOI18N
        }
        
        if (opName != null) {
            addon = (addon == null ? TMapComponentNode.EMPTY_STRING : addon+TMapComponentNode.WHITE_SPACE) + opName; // NOI18N
        }

        return Util.getGrayString(super.getHtmlDisplayName(), addon);
    }

    @Override
    public String getTooltip() {
        Invoke ref = getOriginal();
        StringBuffer attributesTooltip = new StringBuffer();
        if (ref != null) {
            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getPartnerLinkType()
                    , Invoke.PARTNER_LINK_TYPE));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getRole()
                    , Invoke.ROLE_NAME));

            attributesTooltip.append(
                    Util.getLocalizedAttribute(ref.getOperation()
                    , Invoke.OPERATION_NAME));
        }
        return  NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", super.getName(), 
                attributesTooltip.toString());      
    }
}
