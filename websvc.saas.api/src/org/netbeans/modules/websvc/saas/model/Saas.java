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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.List;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices.Header;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class Saas {
    public static final String PROP_PARENT_GROUP = "parentGroup";
    public static final String PROP_STATE = "saasState";

    public static enum State { 
        UNINITIALIZED, 
        RETRIEVED, 
        READY 
    }
    
    public static final String NS_SAAS = "http://xml.netbeans.org/websvc/saas/services/1.0";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_WADL = "http://research.sun.com/wadl/2006/10";
    //private static final String CUSTOM = "custom";
    
    protected final SaasServices delegate;
    private SaasGroup parentGroup;
    private List<SaasMethod> saasMethods;
    
    private State state = State.UNINITIALIZED;
    protected FileObject saasFolder; // userdir folder to store customization and consumer artifacts
    private FileObject moduleJar; // NBM this saas was loaded from
    private boolean userDefined = true;

    public Saas(SaasGroup parentGroup, SaasServices services) {
        this.delegate = services;
        this.parentGroup = parentGroup;
    }
    
    public Saas(String url, String displayName, String description) {
        delegate = new SaasServices();
        delegate.setUrl(url);
        delegate.setDisplayName(url);
        delegate.setDescription(description);
    }

    public SaasServices getDelegate() {
        return delegate;
    }

    public SaasGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(SaasGroup parentGroup) {
        if (this.parentGroup == parentGroup) {
            return;
        }
        this.parentGroup = parentGroup;
        SaasMetadata data = delegate.getSaasMetadata();
        if (data == null) {
            data = new SaasMetadata();
            delegate.setSaasMetadata(data);
            data.setGroup(parentGroup.getPathFromRoot());
        }
    }

    public void save() throws IOException {
        
        //TODO
    }

    public boolean isUserDefined() {
        return userDefined;
    }
    
    public void setUserDefined(boolean v) {
        if (userDefined) {
            userDefined = v;
        }
    }
    
    public String getUrl() {
        return delegate.getUrl();
    }
    
    public State getState() {
        return state;
    }

    protected synchronized void setState(State v) {
        State old = state;
        state = v;
        SaasServicesModel.getInstance().fireChange(PROP_STATE, this, old, state);
    }
    
    /**
     * Asynchronous call to transition Saas to READY state; mainly for UI usage
     * Sub-class need to completely override as needed, without calling super().
     */
    public void toStateReady() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                setState(State.READY);
            }
        });
    }
    
    public FileObject getModuleJar() {
        return moduleJar;
    }

    protected void setModuleJar(FileObject moduleJar) {
        this.moduleJar = moduleJar;
    }
        
    public SaasMetadata getSaasMetadata() {
        return delegate.getSaasMetadata();
    }

    public List<SaasMethod> getMethods() {
        if (saasMethods == null) {
            saasMethods = new ArrayList<SaasMethod>();
            if (delegate.getMethods() != null && delegate.getMethods().getMethod() != null) {
                for (Method m : delegate.getMethods().getMethod()) {
                    saasMethods.add(createSaasMethod(m));
                }
            }
        }
        return Collections.unmodifiableList(saasMethods);
    }

    protected SaasMethod createSaasMethod(Method method) {
        return new SaasMethod(this, method);
    }
    
    public Header getHeader() {
        return delegate.getHeader();
    }

    public String getDisplayName() {
        return (String) delegate.getDisplayName();
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public String getApiDoc() {
        return delegate.getApiDoc();
    }
    
    public FileObject getSaasFolder() {
        if (saasFolder == null) {
            saasFolder = getParentGroup().getGroupFolder().getFileObject(getDisplayName(), null);
            if (saasFolder == null) {
                try {
                    saasFolder = getParentGroup().getGroupFolder().createFolder(getDisplayName());
                } catch(Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return saasFolder;
    }
    
    /**
     * Get the URL class loader for the module defining this SaaS.
     * @return URLClassLoader instance; or null if this SaaS does not come from an NBM
     */
    /*protected URLClassLoader getModuleLoader() {
        if (loader == null) {
            try {
                loader = new URLClassLoader(new URL[] { new URL(moduleJar.getPath()) });
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return loader;
    }*/
}