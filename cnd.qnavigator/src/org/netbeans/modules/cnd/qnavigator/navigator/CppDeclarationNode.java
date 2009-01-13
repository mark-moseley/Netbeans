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

package org.netbeans.modules.cnd.qnavigator.navigator;


import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Navigator Tree node.
 */
public class CppDeclarationNode extends AbstractCsmNode implements Comparable<CppDeclarationNode> {
    private Image icon;
    private CsmOffsetable object;
    private CsmFile file;
    private boolean isFriend;
    private CsmFileModel model;
    private String htmlDisplayName = NEEDS_INIT;
    private final byte weight;
    private static final String NEEDS_INIT = new String("");
    
    private CppDeclarationNode(CsmOffsetableDeclaration element, CsmFileModel model, List<IndexOffsetNode> lineNumberIndex) {
        this(element, model, null, lineNumberIndex);
    }

    private CppDeclarationNode(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier classifier, List<IndexOffsetNode> lineNumberIndex) {
        super(new NavigatorChildren(element, model, classifier, lineNumberIndex), Lookups.fixed(element));
        object = element;
        file = element.getContainingFile();
        this.model = model;
        this.weight = getObjectWeight();
    }

    private CppDeclarationNode(Children children, CsmOffsetable element, CsmFileModel model) {
        super(children, Lookups.fixed(element));
        object = element;
        file = element.getContainingFile();
        this.model = model;
        this.weight = getObjectWeight();
    }

    private CppDeclarationNode(Children children, CsmOffsetableDeclaration element, CsmFileModel model, boolean isFriend) {
        this(children, element, model);
        this.isFriend = isFriend;
    }

    private byte getObjectWeight(){
        if(CsmKindUtilities.isNamespaceDefinition(object)) {
            return 0*0+2;
        } else if(CsmKindUtilities.isNamespaceAlias(object)) {
            return 0*0+0;
        } else if(CsmKindUtilities.isUsing(object)) {
            return 0*0+1;
        } else if(CsmKindUtilities.isClass(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isFriendClass(object)) {
            return 1*10+0;
        } else if(CsmKindUtilities.isClassForwardDeclaration(object)) {
            return 1*10+0;
        } else if(CsmKindUtilities.isEnum(object)) {
            return 1*10+1;
        } else if(CsmKindUtilities.isTypedef(object)) {
            return 1*10+2;
        } else if(CsmKindUtilities.isVariableDeclaration(object)) {
            return 2*10+0;
        } else if(CsmKindUtilities.isVariableDefinition(object)) {
            return 2*10+1;
        } else if(CsmKindUtilities.isFunctionDeclaration(object)) {
            return 3*10+0;
        } else if(CsmKindUtilities.isFunctionDefinition(object)) {
            return 3*10+1;
        } else if(CsmKindUtilities.isMacro(object)) {
            return 4*10+0;
        } else if(CsmKindUtilities.isInclude(object)) {
            return 5*10+0;
        }
        return 9*10+0;
    }


    public CsmObject getCsmObject() {
        if (CsmKindUtilities.isCsmObject(object)) {
            return (CsmObject) object;
        }
        return null;
    }

    int getOffset() {
        return object.getStartOffset();
    }

    void resetNode(CppDeclarationNode node){
        object = node.object;
        file = object.getContainingFile();
        fireIconChange();
    }
    
    public int compareTo(CppDeclarationNode o) {
        int res = 0;
        switch(model.getFilter().getSortMode()) {
            case Name:
                if (model.getFilter().isGroupByKind()) {
                    res = weight/10 - o.weight/10;
                    if (res == 0) {
                        res = getDisplayName().compareTo(o.getDisplayName());
                        if (res == 0) {
                            res = weight - o.weight;
                        }
                    }
                } else {
                    res = getDisplayName().compareTo(o.getDisplayName());
                    if (res == 0) {
                        if (res == 0) {
                            res = weight - o.weight;
                        }
                    }
                }
                break;
            case Offset:
                if (model.getFilter().isGroupByKind()) {
                    res = weight/10 - o.weight/10;
                }
                break;
        }
        if (res == 0) {
            res = object.getStartOffset() - o.object.getStartOffset();
        }
        return res;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }


    @Override
    public String getHtmlDisplayName() {
        if( htmlDisplayName == NEEDS_INIT ) {
            htmlDisplayName = createHtmlDisplayName();
        }
        return htmlDisplayName;
    }
    
    private String createHtmlDisplayName() {
        if (CsmKindUtilities.isFunctionDefinition(getCsmObject())) {
            // the try-catch is just a FIXUP for #118212 NPE when opening file from boost...
            try {
                CsmFunction function = ((CsmFunctionDefinition)object).getDeclaration();
                if (function != null && !function.equals(object) &&  CsmKindUtilities.isClassMember(function)){
                    CsmClass cls = ((CsmMember)function).getContainingClass();
                    if (cls != null && cls.getName().length()>0) {
                        String name = cls.getName().toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
                        String displayName = getDisplayName().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
                        String in = NbBundle.getMessage(getClass(), "LBL_inClass"); //NOI18N
                        return displayName+"<font color='!controlShadow'>  " + in + " " + name; // NOI18N
                    }
                }
            } catch( AssertionError ex ) {
                // FIXUP for #118212 NPE when opening file from boost...
                ex.printStackTrace();
            } catch( Exception ex ) {
                // FIXUP for #118212 NPE when opening file from boost...
                ex.printStackTrace();
            }
        } else if (CsmKindUtilities.isVariableDefinition(getCsmObject())) {
            try {
                CsmVariable variable = ((CsmVariableDefinition)object).getDeclaration();
                if (variable != null && !variable.equals(object) &&  CsmKindUtilities.isClassMember(variable)){
                    CsmClass cls = ((CsmMember)variable).getContainingClass();
                    if (cls != null && cls.getName().length()>0) {
                        String name = cls.getName().toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
                        String displayName = getDisplayName().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
                        String in = NbBundle.getMessage(getClass(), "LBL_inClass"); //NOI18N
                        return displayName+"<font color='!controlShadow'>  " + in + " " + name; // NOI18N
                    }
                }
            } catch( AssertionError ex ) {
                ex.printStackTrace();
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    public Image getIcon(int param) {
        if (icon != null){
            return icon;
        }
        if (file != null && !file.isValid()){
            CsmOffsetable obj = object;
            object = null;
            Image aIcon = super.getIcon(param);
            object = obj;
            return aIcon;
        }
        if (isFriend) {
            CsmFriend csmObj = (CsmFriend)object;
            return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getFriendFunctionImage(csmObj);
        } else {
            return super.getIcon(param);
        }
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    @Override
    public Action getPreferredAction() {
        if (CsmKindUtilities.isOffsetable(object)){
            return new GoToDeclarationAction(object);
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (action != null){
            List<Action> list = new ArrayList<Action>();
            list.add(action);
            list.add(RefactoringActionsFactory.whereUsedAction());
            CsmObject obj = this.getCsmObject();
            if (CsmKindUtilities.isField(obj)) {
                list.add(CsmRefactoringActionsFactory.encapsulateFieldsAction());
            } else if (CsmKindUtilities.isFunction(obj)) {
                list.add(CsmRefactoringActionsFactory.changeParametersAction());
            }
            list.add(null);
            for (Action a : model.getActions()){
                list.add(a);
            }
            return list.toArray(new Action[list.size()]);
        }
        return model.getActions();
    }

    public static CppDeclarationNode nodeFactory(CsmObject element, CsmFileModel model, boolean isFriend, List<IndexOffsetNode> lineNumberIndex){
        if (!model.getFilter().isApplicable((CsmOffsetable)element)){
            return null;
        }
        CppDeclarationNode node = null;
        if (CsmKindUtilities.isTypedef(element)){
            CsmTypedef def = (CsmTypedef) element;
            if (def.isTypeUnnamed()) {
                CsmClassifier cls = def.getType().getClassifier();
                if (cls != null && cls.getName().length()==0 &&
                   (cls instanceof CsmCompoundClassifier)) {
                    node = new CppDeclarationNode((CsmOffsetableDeclaration)element, model, (CsmCompoundClassifier) cls, lineNumberIndex);
                    node.setName(((CsmDeclaration)element).getName().toString());
                    return node;
                }
            }
            node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
            node.setName(((CsmDeclaration)element).getName().toString());
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if (CsmKindUtilities.isClassifier(element)){
            String name = ((CsmClassifier)element).getName().toString();
            if (name.length()==0 && (element instanceof CsmCompoundClassifier)) {
                Collection list = ((CsmCompoundClassifier)element).getEnclosingTypedefs();
                if (list.size() > 0) {
                    return null;
                }
            }
            node = new CppDeclarationNode((CsmOffsetableDeclaration)element, model,lineNumberIndex);
            if (CsmKindUtilities.isClass(element)) {
                CsmClass cls = (CsmClass)element;
                node.setName(CsmKindUtilities.isTemplate(cls) ? ((CsmTemplate)cls).getDisplayName().toString() : cls.getName().toString());
            } else {
                node.setName(((CsmClassifier)element).getName().toString());
            }
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isNamespaceDefinition(element)){
            node = new CppDeclarationNode((CsmNamespaceDefinition)element, model, lineNumberIndex);
            node.setName(((CsmNamespaceDefinition)element).getName().toString());
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isDeclaration(element)){
            if(CsmKindUtilities.isFunction(element)){
                node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
                node.setName(CsmUtilities.getSignature((CsmFunction)element, true));
            } else {
                String name = ((CsmDeclaration)element).getName().toString();
                if (name.length() == 0 && CsmKindUtilities.isVariable(element)){
                    return node;
                }
                node = new CppDeclarationNode(Children.LEAF,(CsmOffsetableDeclaration)element,model,isFriend);
                node.setName(name);
            }
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isEnumerator(element)){
            node = new CppDeclarationNode(Children.LEAF,(CsmEnumerator)element,model);
            node.setName(((CsmEnumerator)element).getName().toString());
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(CsmKindUtilities.isMacro(element)){
            node = new CppDeclarationNode(Children.LEAF,(CsmMacro)element,model);
            node.setName(((CsmMacro)element).getName().toString());
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        } else if(element instanceof CsmInclude){
            node = new CppDeclarationNode(Children.LEAF,(CsmInclude)element,model);
            node.setName(((CsmInclude)element).getIncludeName().toString());
            model.addOffset(node, (CsmOffsetable)element, lineNumberIndex);
            return node;
        }
        return node;
    }
}
