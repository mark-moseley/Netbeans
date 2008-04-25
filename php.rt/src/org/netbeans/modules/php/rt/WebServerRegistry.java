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
package org.netbeans.modules.php.rt;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.project.Project;
import org.netbeans.modules.php.dbgp.api.DebuggerFactory;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileObject;


/**
 * Entry point for accessing to information about configured hosts.
 * 
 * Methods {@link #addHost(Host)} and {@link #removeHost(Host)} are 
 * used ONLY for notifications because all Host management is performed
 * in WebServerProvider.
 *   
 * @author ads
 *
 */
public class WebServerRegistry {

    private final static WebServerRegistry INSTANCE = new WebServerRegistry();

    // avoid external instantiation
    private WebServerRegistry() {
        myListener = new CopyOnWriteArrayList<HostListener>();
    }
    
    public void addHost( Host host ) {
        /*
         * There is no common code for addition of new host.
         * Each provider has reponsibility to perform this action 
         * internally via first configuration stage and via wizard
         * ( wizard in supported also by provider ). 
         */
        fireAddHostEvent(host);
    }
    
    public void removeHost( Host host ) {
        fireDeleteHostEvent(host);
    }

    public void upadateHost( Host host ) {
        fireUpdateHostEvent(host);
    }

    public Collection<Host> getHosts(){
        WebServerProvider[] providers = 
            WebServerProvider.ServerFactory.getProviders();
        List<Host> ret = new LinkedList<Host>();
        for (WebServerProvider provider : providers) {
            List<Host> list = provider.getHosts();
            ret.addAll( list );
        }
        return ret;
    }
    
    public static WebServerRegistry getInstance() {
        return INSTANCE;
    }
    
    public void addListener( HostListener listener ) {
        getListeners().add(listener);
    }
    
    public void removeListener( HostListener listener ) {
        getListeners().remove(listener);
    }
    
    private void fireAddHostEvent( Host host ) {
        for ( HostListener listener:getListeners() ) {
            listener.hostAdded(host);
        }
    }
    
    private void fireDeleteHostEvent( Host host ) {
        for ( HostListener listener:getListeners() ) {
            listener.hostRemoved(host);
        }
    }
    
    private void fireUpdateHostEvent( Host host ) {
        for ( HostListener listener:getListeners() ) {
            listener.hostUpdated(host);
        }
    }

    private Collection<HostListener> getListeners(){
        return myListener;
    }
    
    private Collection<HostListener> myListener; 
}
