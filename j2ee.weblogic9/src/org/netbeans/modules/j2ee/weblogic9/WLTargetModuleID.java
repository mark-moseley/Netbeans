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
package org.netbeans.modules.j2ee.weblogic9;

import java.util.Vector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
/**
 *
 * @author whd
 */
class WLTargetModuleID implements TargetModuleID{
    private Target target;
    private String jar_name;
    private String context_url;

    Vector childs = new Vector();
    TargetModuleID  parent = null;
    WLTargetModuleID(Target target  ){
        this( target, "");


    }
    WLTargetModuleID(Target target, String jar_name  ){
        this.target = target;
        this.setJARName(jar_name);
        
    }    
    public void setContextURL( String context_url ){
        this.context_url = context_url;
    }
    public void setJARName( String jar_name ){
        this.jar_name = jar_name;
    }
    
    public void setParent( WLTargetModuleID parent){
        this.parent = parent;
        
    }
    
    public void addChild( WLTargetModuleID child) {
        childs.add( child );
        child.setParent( this );
    }
    
    public TargetModuleID[]     getChildTargetModuleID(){
        return (TargetModuleID[])childs.toArray(new TargetModuleID[childs.size()]);
    }
    //Retrieve a list of identifiers of the children of this deployed module.
    public java.lang.String     getModuleID(){
        return jar_name ;
    }
    //         Retrieve the id assigned to represent the deployed module.
    public TargetModuleID     getParentTargetModuleID(){
        
        return parent;
    }
    //Retrieve the identifier of the parent object of this deployed module.
    public Target     getTarget(){
        return target;
    }
    //Retrieve the name of the target server.
    public java.lang.String     getWebURL(){
        return context_url;//"http://" + module_id; //NOI18N
    }
    //If this TargetModulID represents a web module retrieve the URL for it.
    public java.lang.String     toString() {
        return getModuleID() +  hashCode();
    }
}