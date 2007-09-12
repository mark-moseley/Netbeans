/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    protected UpdateItemImpl impl(UpdateItem item) {
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

    protected UpdateUnitProviderImpl impl (UpdateUnitProvider provider) {
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
