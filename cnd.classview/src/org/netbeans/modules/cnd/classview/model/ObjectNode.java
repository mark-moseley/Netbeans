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

package org.netbeans.modules.cnd.classview.model;
import java.util.Collection;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.PersistentKey;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.actions.MoreDeclarations;

/**
 * @author Vladimir Kvasihn
 */
public abstract class ObjectNode extends BaseNode implements ChangeListener {
    private PersistentKey key;
    
    public ObjectNode(CsmOffsetableDeclaration declaration) {
        this(declaration, Children.LEAF);
    }
    
    public ObjectNode(CsmOffsetableDeclaration declaration, Children children) {
        super(children);
        setObject(declaration);
    }
    
    /** Implements AbstractCsmNode.getData() */
    public CsmObject getCsmObject() {
        return getObject();
    }
    
    public CsmOffsetableDeclaration getObject() {
        return (CsmOffsetableDeclaration) key.getObject();
    }
    
    protected void setObject(CsmOffsetableDeclaration declaration) {
        key = PersistentKey.createKey(declaration);
    }
    
    @Override
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    private Action createOpenAction() {
        CsmOffsetableDeclaration decl = getObject();
        if (decl != null) {
            return new GoToDeclarationAction(decl);
        }
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action action = createOpenAction();
        if (action != null){
            CsmOffsetableDeclaration decl = getObject();
            CharSequence name = decl.getUniqueName();
            CsmProject project = decl.getContainingFile().getProject();
            if (project != null){
                Collection<CsmOffsetableDeclaration> arr = project.findDeclarations(name);
                for(CsmFriend friend : CsmFriendResolver.getDefault().findFriends(decl)){
                    if (CsmKindUtilities.isFriendMethod(friend)) {
                        arr.add(friend);
                    }
                }
                if (CsmKindUtilities.isFunctionDeclaration(decl)) {
                    // add all definitions
                    CsmFunctionDefinition def = ((CsmFunction)decl).getDefinition();
                    if (def != null && def != decl) {
                        arr.addAll(project.findDeclarations(def.getUniqueName()));
                        
                    }
                }
                if (arr.size() > 1){
                    Action more = new MoreDeclarations(arr);
                    return new Action[] { action, more };
                }
            }
            
            return new Action[] { action };
        }
        return new Action[0];
    }
}
