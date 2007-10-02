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
/*
 * Class.java
 *
 * Created on May 3, 2004, 5:52 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelListener;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import java.util.Collections;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * The child nodes for EJB Group, which are grouping nodes:
 *   - Session Beans
 *   - Entity Beans
 *   - Message Driven Beans
 *
 * @author  cao
 */
public class EjbGroupNodeChildren extends Children.Keys implements EjbDataModelListener
{
    private EjbGroup ejbGroup;
    
    public EjbGroupNodeChildren(EjbGroup ejbGroup) 
    {
        this.ejbGroup = ejbGroup;
    }
    
    protected org.openide.nodes.Node[] createNodes( Object key ) 
    {
       // For each key/session bean, we'll create a session bean node
       if( key instanceof EjbInfo ) 
        {
            
            Node node = new SessionBeanNode( ejbGroup, (EjbInfo)key );
            return new Node[] {node};
        } 
        else
            return null;
    }
    
    protected void addNotify() 
    {
        // Set the keys for the children
        
        super.addNotify();
        if( ejbGroup.getSessionBeans() != null && !ejbGroup.getSessionBeans().isEmpty() )
            setKeys( ejbGroup.getSessionBeans() );
        else
            setKeys( Collections.EMPTY_SET );
        
        // Listen on the changes in the EjbDataModel
        EjbDataModel.getInstance().addListener( this );
    }
    
    protected void removeNotify() 
    {
        setKeys( Collections.EMPTY_SET );
        super.removeNotify();
        
        // No need to listen on the data model any more
        EjbDataModel.getInstance().removeListener( this );
    }
    
     public void groupAdded(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        // Handled by EjbRootNodeChildren
    }
    
    public void groupChanged(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        if( modelEvent.getEjbGroup() == ejbGroup )
            setKeys( ejbGroup.getSessionBeans() );
    }
    
    public void groupDeleted(org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModelEvent modelEvent) 
    {
        // Handled by EjbRootNodeChildren
    }
    
    public void groupsDeleted() 
    {
        // Handled by EjbRootNodeChildren
    }    
    
    
}
