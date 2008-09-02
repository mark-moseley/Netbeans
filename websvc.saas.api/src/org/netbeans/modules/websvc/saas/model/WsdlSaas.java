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
package org.netbeans.modules.websvc.saas.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author nam
 */
public class WsdlSaas extends Saas implements PropertyChangeListener {

    private WsdlData wsData;
    private List<WsdlSaasPort> ports;

    public WsdlSaas(SaasGroup parentGroup, SaasServices services) {
        super(parentGroup, services);
    }

    public WsdlSaas(SaasGroup parentGroup, String url, String displayName, String packageName) {
        super(parentGroup, url, displayName, packageName);
        getDelegate().setType(NS_WSDL);
    }

    protected void setWsdlData(WsdlData data) {
        wsData = data;
        if (wsData.isReady()) {
            setState(State.READY);
        } else {
            setState(State.UNINITIALIZED);
        }
    }

    public WsdlData getWsdlData() {
        State state = getState();
        if (state == State.RETRIEVED || state == State.READY) {
            return wsData;
        }
        throw new IllegalStateException("Current state: " + state);
    }

    @Override
    protected void refresh() {
        if (getState() == State.INITIALIZING) {
            throw new IllegalStateException(NbBundle.getMessage(WsdlSaas.class, "MSG_CantRefreshWhileInitializing"));
        }
        super.refresh();
        ports = null;
        
        if (wsData == null) {
            wsData = WsdlUtil.findWsdlData(this.getUrl(), null);
            
            if (wsData == null) {
                // If the wsData has never been retrieved and compiled, we simply call
                // toStateReady and return.
                toStateReady(false);
                return;
            } else {
                // If the wsData has been retrieved and compiled but has not
                // yet been initialized, we initialize it now (which is quick)
                // and then refresh it.
                wsData = null;
                toStateReady(true);
            }
        }
        
        WsdlUtil.refreshWsdlData(wsData);
    }

    public String getDefaultServiceName() {
        if (getMethods().size() > 0) {
            return getMethods().get(0).getMethod().getServiceName();
        }
        return ""; //NOI18N

    }

    public String getPackageName() {
        String pname = getDelegate().getSaasMetadata().getCodeGen().getPackageName();
        if (pname == null) {
            pname = "";
        }
        return pname;
    }

    @Override
    public void toStateReady(boolean synchronous) {
        if (getState() == State.REMOVED) {
            return;
        }
        if (wsData == null) {
            String serviceName = getDefaultServiceName();
            wsData = WsdlUtil.getWsdlData(getUrl(), serviceName, synchronous); //NOI18N

            // first-time the call will return null
            if (wsData == null) {
                wsData = WsdlUtil.addWsdlData(getUrl(), getPackageName());
                if (wsData != null && synchronous) {
                    int count = 0;
                    while (!wsData.isReady() && count < 100) {
                        try {
                            Thread.sleep(100);
                            count++;
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            if (wsData != null) {
                wsData.addPropertyChangeListener(WeakListeners.propertyChange(this, wsData));
                if (wsData.isReady()) {
                    setState(State.READY);
                } else {
                    setState(State.INITIALIZING);
                }
            } else {
                setState(State.UNINITIALIZED);
            }
        }
    }

    private List<WSPort> filterNonSoapPorts(List<WSPort> ports) {
        List<WSPort> filterPorts = new java.util.ArrayList<WSPort>(ports.size());

        for (WSPort port : ports) {
            if (port.getAddress() != null) {
                filterPorts.add(port);
            }
        }

        return filterPorts;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        Object newValue = evt.getNewValue();

        // these are transitions out of the temporary state INITIALIZING
        // we are only interested in transition to ready and retrieved states.
        // when compile fail we fallback to retrieved to allow user examine the wsdl

        if (property.equals("resolved") && getState() == State.INITIALIZING) { //NOI18N

            if (Boolean.FALSE.equals(newValue)) {
                setState(State.RETRIEVED);
            } else if (wsData.isReady()) {
                setState(State.READY); // compiled in previous IDE run

            }
        } else if (WsdlData.Status.WSDL_SERVICE_COMPILED.equals(newValue)) {
            setState(State.READY);
        } else if (WsdlData.Status.WSDL_SERVICE_COMPILE_FAILED.equals(newValue)) {
            setState(State.RETRIEVED);
        } else if (WsdlData.Status.WSDL_UNRETRIEVED.equals(newValue)) {
            setState(State.UNINITIALIZED);
        }
    }

    public WSService getWsdlModel() {
        return getWsdlData().getWsdlService();
    }

    public FileObject getLocalWsdlFile() {
        return FileUtil.toFileObject(new File(getWsdlData().getWsdlFile()));
    }

    public List<WsdlSaasPort> getPorts() {
        if (ports == null) {
            ports = new ArrayList<WsdlSaasPort>();
            for (WSPort p : filterNonSoapPorts(getWsdlModel().getPorts())) {
                ports.add(new WsdlSaasPort(this, p));
            }
        }
        return new ArrayList<WsdlSaasPort>(ports);
    }

    @Override
    protected WsdlSaasMethod createSaasMethod(Method method) {
        return new WsdlSaasMethod(this, method);
    }

    @Override
    public FileObject getSaasFolder() {
        if (saasFolder == null) {
            String folderName = WsdlUtil.getServiceDirName(getUrl());
            FileObject websvcHome = SaasServicesModel.getWebServiceHome();
            saasFolder = websvcHome.getFileObject(folderName);
            if (saasFolder == null) {
                try {
                    saasFolder = websvcHome.createFolder(folderName);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return saasFolder;
    }
}
