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
package org.netbeans.modules.subversion.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.logging.Level;
import javax.net.ssl.SSLKeyException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.util.Cancellable;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 *
 * @author Tomas Stupka 
 */
public class SvnClientInvocationHandler implements InvocationHandler {    
    
    private static Set<String> remoteMethods = new HashSet<String>();    
    static {
        remoteMethods.add("checkout");  // NOI18N
        remoteMethods.add("commit"); // NOI18N
        remoteMethods.add("commitAcrossWC"); // NOI18N
        remoteMethods.add("getList"); // NOI18N
        remoteMethods.add("getDirEntry"); // NOI18N
        remoteMethods.add("copy");  // NOI18N
        remoteMethods.add("remove"); // NOI18N
        remoteMethods.add("doExport"); // NOI18N
        remoteMethods.add("doImport"); // NOI18N
        remoteMethods.add("mkdir"); // NOI18N
        remoteMethods.add("move"); // NOI18N
        remoteMethods.add("update"); // NOI18N
        remoteMethods.add("getLogMessages"); // NOI18N
        remoteMethods.add("getContent"); // NOI18N
        remoteMethods.add("setRevProperty"); // NOI18N
        remoteMethods.add("diff"); // NOI18N
        remoteMethods.add("annotate"); // NOI18N
        remoteMethods.add("getInfo"); // NOI18N
        remoteMethods.add("switchToUrl"); // NOI18N
        remoteMethods.add("merge"); // NOI18N
        remoteMethods.add("lock"); // NOI18N
        remoteMethods.add("unlock"); // NOI18N        
    }       
    
    private static Object semaphor = new Object();        

    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private Cancellable cancellable;
    private SvnProgressSupport support;
    private final int handledExceptions; 
    
   /**
     *
     */
    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, int handledExceptions) {
        
        assert adapter  != null;
        assert desc     != null;
        
        this.adapter = adapter;
        this.desc = desc;
        this.handledExceptions = handledExceptions;
    }

    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
        
        assert adapter  != null;
        assert desc     != null;
        
        this.adapter = adapter;
        this.desc = desc;
        this.support = support;
        this.handledExceptions = handledExceptions;
        this.cancellable = new Cancellable() {
            public boolean cancel() {
                try {
                    SvnClientInvocationHandler.this.adapter.cancelOperation();
                } catch (SVNClientException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {               
        
        String methodName = method.getName();
        assert noRemoteCallinAWT(methodName, args) : "noRemoteCallinAWT(): " + methodName; // NOI18N

        try {      
            Object ret = null;        
            if(parallelizable(method, args)) {
                ret = invokeMethod(method, args);    
            } else {
                synchronized (semaphor) {
                    ret = invokeMethod(method, args);    
                }
            }            
            Subversion.getInstance().getStatusCache().refreshDirtyFileSystems();
            return ret;
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e) ) {
                    return invoke(proxy, method, args);
                } else {
                    // some action canceled by user message 
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER); 
                }                
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException();
                if(t instanceof SVNClientException) {
                    throw t;
                }
                throw ite;
            } catch (SSLKeyException ex) {
                if(ex.getCause() instanceof InvalidKeyException) {
                    InvalidKeyException ike = (InvalidKeyException) ex.getCause();
                    if(ike.getMessage().toLowerCase().equals("illegal key size or default parameters")) { // NOI18N
                        SvnClientExceptionHandler.handleInvalidKeyException(ike);
                    }
                    return null; 
                }
                throw ex;
            } catch (Throwable t) {
                if(t instanceof InterruptedException) {
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                } 
                Throwable c = t.getCause();
                if(c instanceof InterruptedException) {                    
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                } 
                throw t;
            }
        }
    }
    
    protected boolean parallelizable(Method method, Object[] args) {
        return  SwingUtilities.isEventDispatchThread();
    }
    
    protected Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        return handle(proxyMethod, args);    
    }

    protected Object handle(final Method proxyMethod, final Object[] args) 
    throws SecurityException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException 
    {
        Object ret;

        Class[] parameters = proxyMethod.getParameterTypes();
        Class declaringClass = proxyMethod.getDeclaringClass();

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            // Cliet Adapter
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }            
            // save the proxy settings into the svn servers file                
            if(desc != null && desc.getSvnUrl() != null) {
                SvnConfigFiles.getInstance().setProxy(desc.getSvnUrl());      
            }                            
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
            if(support != null) {
                support.setCancellableDelegate(null);
            }
        } else if( Cancellable.class.isAssignableFrom(declaringClass) ) { 
            // Cancellable
            ret = cancellable.getClass().getMethod(proxyMethod.getName(), parameters).invoke(cancellable, args);
        } else if( SvnClientDescriptor.class.isAssignableFrom(declaringClass) ) {            
            // Client Descriptor
            if(desc != null) {
                ret = desc.getClass().getMethod(proxyMethod.getName(), parameters).invoke(desc, args);    
            } else {
                // when there is no descriptor, then why has the method been called
                throw new NoSuchMethodException(proxyMethod.getName());
            }            
        } else {
            // try to take care for hashCode, equals & co. -> fallback to clientadapter
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
        }                
        
        return ret;
    }

    private boolean handleException(SvnClient client, Throwable t) throws Throwable {
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }

        SvnClientExceptionHandler eh = new SvnClientExceptionHandler((SVNClientException) t, adapter, client, handledExceptions);        
        return eh.handleException();        
    }
    
   /**
     * @return false for methods that perform calls over network
     */
    protected boolean noRemoteCallinAWT(String methodName, Object[] args) {
        if(!SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        if (remoteMethods.contains(methodName)) {
            return false;
        } 
        return true;
    }
}

