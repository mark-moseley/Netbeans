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
package org.netbeans.modules.visualweb.dataconnectivity.naming;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import org.openide.ErrorManager;

/**
 * Creator's naming context that is created per project
 *
 * @author John Kline, John Baker
 */
class DesignTimeContext implements Context {   
    private Reference<Project>  projectRef;    
    public static final String  ROOT_CTX_TAG = "rootContext"; // NOI18N
    public static final String  CTX_TAG      = "context"; // NOI18N
    public static final String  OBJ_TAG      = "object"; // NOI18N
    public static final String  ARG_TAG      = "arg"; // NOI18N
    public static final String  NAME_ATTR    = "name"; // NOI18N
    public static final String  CLASS_ATTR   = "class"; // NOI18N
    public static final String  VALUE_ATTR   = "value"; // NOI18N
    private static DesignTimeContext thisInstance;
    private Map                 bindings;
    private static Hashtable    env;
    private boolean             update = false;

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.naming.Bundle", // NOI18N
        Locale.getDefault());
    
    // entry for subcontexts in a context's TreeMap (map)
    private class Subcontext {
        private String            subcontextName;
        private DesignTimeContext subcontext;
            Subcontext(String subcontextName, DesignTimeContext subcontext) {
            this.subcontextName = subcontextName;
            this.subcontext     = subcontext;
        }
    }

    /** Creates a new instance of DesignTimeDatasourceContext */
    private DesignTimeContext(Project p,  Hashtable env) {
        projectRef = new WeakReference<Project>(p);         
        thisInstance  = this;                                                                    
        thisInstance.env    = new Hashtable(env); 
    }
    
    private static class DesignTimeContextHolder {
        static final DesignTimeContext setDesignTimeContext(Project prj, Hashtable environment) {
            return new DesignTimeContext(prj, environment);
        }
    }
        
    public static DesignTimeContext getDesignTimeContext() {
        return thisInstance;
    }
    
    public static DesignTimeContext createDesignTimeContext(Project prj, Hashtable environment) {
        DesignTimeContext dtCtx = null;
        
        if (thisInstance != null) {
            if (prj != null) {
                if (!((Project)thisInstance.projectRef.get()).equals(prj)) {
                    dtCtx = DesignTimeContextHolder.setDesignTimeContext(prj, environment);
                } else {
                    dtCtx = getDesignTimeContext();
                    if (dtCtx == null) {
                        dtCtx = DesignTimeContextHolder.setDesignTimeContext(prj, environment);
                    }
                }
            }
        } else if (prj != null) {
            dtCtx = DesignTimeContextHolder.setDesignTimeContext(prj, environment);
        }
        
        return dtCtx;
    }             

    public Object lookup(Name name) throws NamingException {
        Log.getLogger().entering(getClass().getName(), "lookup", name); //NOI18N
        
        // Update datasources for current project            
        if (name.toString().contains("java:comp/env/jdbc")  || !update) {            
            return updateBindings(projectRef, name) ;
        }
                           
        return null;
    }

    public Object lookup(String name) throws NamingException {
        return lookup(new CompositeName(name));
    }
    
    public void bind(Name arg0, Object arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void bind(String arg0, Object arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void rebind(Name arg0, Object arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void rebind(String arg0, Object arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void unbind(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void unbind(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void rename(Name arg0, Name arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void rename(String arg0, String arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamingEnumeration<NameClassPair> list(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamingEnumeration<NameClassPair> list(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
          Log.getLogger().entering(getClass().getName(), "listBindings", name); //NOI18N
        if (name.size() == 0) {
            Vector v = new Vector();
            for (Iterator i = bindings.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                Object obj = bindings.get(key);
                if (obj instanceof Subcontext) {
                    obj = ((Subcontext)obj).subcontext;
                }
                v.add(new Binding(key, obj, true));
            }
            return new DesignTimeNamingEnumeration(v.elements());
        } else {
            Object obj = lookup(name);
            if (!(obj instanceof Context)) {
                throw new NameNotFoundException(name.toString());
                } else {
                return ((Context)obj).listBindings(new CompositeName());
            }
        }
          
          
    }
    
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return listBindings(new CompositeName(name));
    }
    
    public void destroySubcontext(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void destroySubcontext(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Context createSubcontext(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Context createSubcontext(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object lookupLink(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object lookupLink(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NameParser getNameParser(Name arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public NameParser getNameParser(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Name composeName(Name arg0, Name arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String composeName(String arg0, String arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object addToEnvironment(String arg0, Object arg1) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Object removeFromEnvironment(String arg0) throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void close() throws NamingException {
        // not used by visual web
    }
    
    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

    // Store the datasource info in the Project
    private Object updateBindings(Reference projectReference, Name name) {        
        DesignTimeDataSourceHelper dsHelper = null;        
        Object obj = null;
        update = true;
        
        try {
            dsHelper = new DesignTimeDataSourceHelper();                        
            
            if (dsHelper.datasourcesInProject((Project)projectReference.get())) {
                bindings = dsHelper.updateDataSource((Project)projectReference.get());   
                confirmConnections(bindings, dsHelper);
                return bindings.get(name.toString());
            }            
            
        } catch (NamingException ne) {
            ErrorManager.getDefault().notify(ne);
        }
        return obj;
    }       
    
    /**
     * Make sure connections are available to inform the user if they aren't
     */
    private void confirmConnections(Map bindings, DesignTimeDataSourceHelper dsHelper) {
        // Check if valid connection has been registered.  If not then check if the
        // project is a legacy project and post an alert for migration
        DataSourceInfo dsInfo = null;
        for (int i = 0; i < bindings.size(); i++) {
            dsInfo = (DataSourceInfo) bindings.get(i);

            // check if driverclass is derby and if it hasn't been started then try to start derby
        }
    }
    
}
