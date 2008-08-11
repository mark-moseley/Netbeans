/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.spi.autoupdate.UpdateItem;

/** Trampline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
final class TrampolineAPI extends Trampoline {
    
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        UpdateUnit unit = new UpdateUnit (impl);
        impl.setUpdateUnit (unit);
        return unit;
    }
    
    protected UpdateUnitImpl impl(UpdateUnit unit) {
        return unit.impl;
    }
    
    protected UpdateElement createUpdateElement(UpdateElementImpl impl) {
        UpdateElement element = new UpdateElement (impl);
        impl.setUpdateElement (element);
        return element;
    }

    protected UpdateElementImpl impl (UpdateElement element) {
        return element.impl;
    }

    public UpdateItemImpl impl(UpdateItem item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateItem createUpdateItem (UpdateItemImpl impl) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    protected OperationContainerImpl impl(OperationContainer container) {
        return container.impl;
    }

    protected UpdateUnitProvider createUpdateUnitProvider (UpdateUnitProviderImpl impl) {
        return new UpdateUnitProvider (impl);
    }

    public UpdateUnitProviderImpl impl (UpdateUnitProvider provider) {
        return provider.impl;
    }
    
    protected OperationInfoImpl impl (OperationInfo info) {
        return info.impl;
    }

    @SuppressWarnings ("unchecked")
    protected OperationContainer.OperationInfo createOperationInfo (OperationInfoImpl impl) {
        return new OperationContainer.OperationInfo (impl);
    }

    protected File findCluster (String clusterName, AutoupdateClusterCreator creator) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    protected File[] registerCluster (String clusterName, File cluster, AutoupdateClusterCreator creator) throws IOException {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

}
