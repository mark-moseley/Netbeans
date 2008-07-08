/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.flow;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;

/**
 *
 * @author avk
 */
public class FlowSVGMenuElementEventSourcePinPresenter extends FlowEventSourcePinPresenter {

    protected DesignComponent getComponentForAttachingPin() {
        return getComponent().getParentComponent();
    }
    protected String getDisplayName() {
        return MidpValueSupport.getHumanReadableString(getComponent().readProperty(
                SVGMenuElementEventSourceCD.PROP_STRING));
    }

    protected String getOrder() {
        return FlowSVGMenuElementPinOrderPresenter.CATEGORY_ID;
    }

    @Override
    protected boolean canRename() {
        return getComponent() != null;
    }

    @Override
    protected String getRenameName() {
        return (String) getComponent().readProperty(
                SVGMenuElementEventSourceCD.PROP_STRING).getPrimitiveValue();
    }

    @Override
    protected void setRenameName(String name) {
        getComponent().writeProperty(SVGMenuElementEventSourceCD.PROP_STRING, 
                MidpTypes.createStringValue(name));
    }

    @Override
    protected DesignEventFilter getEventFilter() {
        return super.getEventFilter().addParentFilter(getComponent(), 1, false);
    }
}
