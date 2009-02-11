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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jec
 */
public class CsmImageLoader implements CsmImageName {
    private static Map<String,ImageIcon> map = new HashMap<String,ImageIcon>();

    
    /** Creates a new instance of CsmImageLoader */
    private CsmImageLoader() {
    }
    
    public static Image getImage(CsmObject o) {
        return getImage(o, Collections.<CsmDeclaration.Kind, CsmDeclaration.Kind>emptyMap());
    }

    /**
     * allow translation of kinds when interested in another icon for object, i.e.
     * can set translation from FUNCTION icon to FUNCTION_DEFINITION icon
     * @param o
     * @param translateKinds
     * @return
     */
    public static Image getImage(CsmObject o, Map<CsmDeclaration.Kind, CsmDeclaration.Kind> translateIcons) {
        String iconPath = getImagePath(o, translateIcons);
        return ImageUtilities.loadImage(iconPath);
    }

    public static Image getFriendFunctionImage(CsmFriend o) {
        String iconPath;
        if (CsmKindUtilities.isFriendClass(o)) {
            iconPath = FRIEND_CLASS;
        } else {
            int modifiers = CsmUtilities.getModifiers(o);
            if ((modifiers & CsmUtilities.OPERATOR) == CsmUtilities.OPERATOR) {
                iconPath = FRIEND_OPERATOR;
            } else {
                iconPath = FRIEND_METHOD;
            }
        }
        return ImageUtilities.loadImage(iconPath);
    }

    public static ImageIcon getProjectIcon(CsmProject prj, boolean opened) {
        String iconPath = getProjectPath(prj.isArtificial(), opened);
        return getCachedImageIcon(iconPath);
    }

    public static Image getProjectImage(boolean library, boolean opened) {
        String iconPath = getProjectPath(library, opened);
        return ImageUtilities.loadImage(iconPath);
    }
    
    private static String getProjectPath(boolean library, boolean opened) {
        String iconPath;
        if (library) {
            iconPath = opened ? LIB_PROJECT_OPENED : LIB_PROJECT;
        } else {
            iconPath = opened ? PROJECT_OPENED : PROJECT;
        }
        return iconPath;
    }
    
    public static ImageIcon getIcon(CsmObject o) {
        String iconPath = getImagePath(o);
        return getCachedImageIcon(iconPath); 
    }
    
    public static ImageIcon getIcon(CsmDeclaration.Kind kind, int modifiers) {
        String iconPath = getImagePath(kind, modifiers);
        return getCachedImageIcon(iconPath);        
    }

    public static ImageIcon getPreprocessorDirectiveIcon() {
        return getCachedImageIcon(MACRO);
    }
    
    public static ImageIcon getTempleteParameterIcon() {
        return getCachedImageIcon(TEMPLATE_PARAMETER);        
    }
    
    public static ImageIcon getIncludeImageIcon(boolean sysIncludeKind, boolean folder) {
        String iconPath;
        if (folder) {
            iconPath = sysIncludeKind ? INCLUDE_SYS_FOLDER : INCLUDE_USR_FOLDER;
        } else {
            iconPath = sysIncludeKind ? INCLUDE_SYSTEM : INCLUDE_USER;
        }
        return getCachedImageIcon(iconPath);        
    }

    public static String getImagePath(CsmObject o) {
        return getImagePath(o, Collections.<CsmDeclaration.Kind, CsmDeclaration.Kind>emptyMap());
    }
    
    private static String getImagePath(CsmObject o, Map<CsmDeclaration.Kind, CsmDeclaration.Kind> translateIcons) {
        CsmDeclaration.Kind kind = CsmDeclaration.Kind.BUILT_IN;
        int modifiers = CsmUtilities.getModifiers(o);
        if (CsmKindUtilities.isEnumerator(o)) {
            kind = CsmDeclaration.Kind.ENUM;
            modifiers |= CsmUtilities.ENUMERATOR;
        } else if (CsmKindUtilities.isUsingDirective(o)) {
            return USING;
        } else if (CsmKindUtilities.isUsingDeclaration(o)) {
            return USING_DECLARATION;
        } else if (CsmKindUtilities.isClassForwardDeclaration(o)) {
            CsmClass cls = ((CsmClassForwardDeclaration)o).getCsmClass();
            if (cls != null && cls.getKind() == CsmDeclaration.Kind.CLASS) {
                return CLASS_FORWARD;
            }
            return STRUCT_FORWARD;
        } else if (CsmKindUtilities.isDeclaration(o)) {
            kind = ((CsmDeclaration)o).getKind();
        } else if (CsmKindUtilities.isNamespace(o)) {
            // FIXUP: consider namespace same as namespace definition
            // because namespace is not declaration
            kind = CsmDeclaration.Kind.NAMESPACE_DEFINITION;
        } else if (CsmKindUtilities.isNamespaceAlias(o)) {
            kind = CsmDeclaration.Kind.NAMESPACE_ALIAS;
        } else if (CsmKindUtilities.isMacro(o)) {
            return MACRO;
        } else if (CsmKindUtilities.isInclude(o)) {
            if (((CsmInclude)o).isSystem()){
                return INCLUDE_SYSTEM;
            } else {
                return INCLUDE_USER;
            }
        } else if (CsmKindUtilities.isProject(o)) {
            return getProjectPath(((CsmProject)o).isArtificial(), false);
        }
        if (translateIcons.get(kind) != null) {
            kind = translateIcons.get(kind);
        }
        return getImagePath(kind, modifiers);
    }
    
    static String getImagePath(CsmDeclaration.Kind kind, int modifiers) {

        String iconPath = DEFAULT;
        if (kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
            iconPath = NAMESPACE;
        } else if (kind == CsmDeclaration.Kind.NAMESPACE_ALIAS) {
            iconPath = NAMESPACE_ALIAS;
        } else if (kind == CsmDeclaration.Kind.ENUM) { 
            if ((modifiers & CsmUtilities.ENUMERATOR) == 0)
                iconPath = ENUMERATION;
            else
                iconPath = ENUMERATOR;
        } else if (kind == CsmDeclaration.Kind.MACRO){
            iconPath = MACRO;
        } else if (kind == CsmDeclaration.Kind.CLASS) {
            iconPath = CLASS;
        } else if (kind == CsmDeclaration.Kind.STRUCT) {
            iconPath = STRUCT;
        } else if (kind == CsmDeclaration.Kind.UNION) {
            iconPath = UNION; 
        } else if (kind == CsmDeclaration.Kind.TYPEDEF) {
            iconPath = TYPEDEF;
        } else if (kind == CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION) {
            iconPath = FRIEND_CLASS;
        } else if (kind == CsmDeclaration.Kind.VARIABLE || kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ) {
            boolean isLocal = (modifiers & CsmUtilities.LOCAL) != 0;
            boolean isFileLocal = (modifiers & CsmUtilities.FILE_LOCAL) != 0;
            boolean isGlobal = !(isLocal | isFileLocal);
            boolean isField = (modifiers & CsmUtilities.MEMBER) != 0;
            boolean isStatic = (modifiers & CsmUtilities.STATIC) != 0;
            boolean isConst = (modifiers & CsmUtilities.CONST_MEMBER_BIT) != 0;
            boolean isExtern = (modifiers & CsmUtilities.EXTERN) != 0;
            if (isGlobal) {
                iconPath = VARIABLE_GLOBAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_GLOBAL;
                    } else {
                        iconPath = VARIABLE_ST_GLOBAL;
                    }
                } else {
                    if (isConst) {
                        if (isExtern) {
                            iconPath = VARIABLE_EX_GLOBAL;
                        } else {
                            iconPath = VARIABLE_CONST_GLOBAL;
                        }
                    } else {
                        if (isExtern) {
                            iconPath = VARIABLE_EX_GLOBAL;
                        } else {
                            iconPath = VARIABLE_GLOBAL;
                        }
                    }
                }
            }

            if (isLocal) {
                iconPath = VARIABLE_LOCAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_LOCAL;
                    } else {
                        iconPath = VARIABLE_ST_LOCAL;
                    }
                } else {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_LOCAL;
                    } else {
                        iconPath = VARIABLE_LOCAL;
                    }
                }
            }

            if (isFileLocal) {
                iconPath = VARIABLE_LOCAL;
                if (isStatic) {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_ST_FILE_LOCAL;
                    } else {
                        iconPath = VARIABLE_ST_FILE_LOCAL;
                    }
                } else {
                    if (isConst) {
                        iconPath = VARIABLE_CONST_FILE_LOCAL;
                    } else {
                        iconPath = VARIABLE_FILE_LOCAL;
                    }
                }
            }

            if (isField) {
                int level = CsmUtilities.getLevel(modifiers);
                iconPath = FIELD_PUBLIC;
                if (isStatic){
                    //static field
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PRIVATE : FIELD_ST_PRIVATE;
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PROTECTED : FIELD_ST_PROTECTED;
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            iconPath = isConst ? FIELD_ST_CONST_PUBLIC : FIELD_ST_PUBLIC;
                            break;
                    }
                }else{
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PRIVATE : FIELD_PRIVATE;
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PROTECTED : FIELD_PROTECTED;
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            iconPath = isConst ? FIELD_CONST_PUBLIC : FIELD_PUBLIC;
                            break;
                    }
                }
          }
        } else if (kind == CsmDeclaration.Kind.FUNCTION || kind == CsmDeclaration.Kind.FUNCTION_DEFINITION) {
            boolean isMethod = (modifiers & CsmUtilities.MEMBER) != 0;
            boolean isGlobal = !(isMethod);
            boolean isConstructor = (modifiers & CsmUtilities.CONSTRUCTOR) != 0;
            boolean isDestructor = (modifiers & CsmUtilities.DESTRUCTOR) != 0;
            boolean isOperator =  (modifiers & CsmUtilities.OPERATOR) != 0;
            boolean isStatic = (modifiers & CsmUtilities.STATIC) != 0;
            int level = CsmUtilities.getLevel(modifiers);
            if (isGlobal) {
                if (isOperator) {
                    iconPath = OPERATOR_GLOBAL;
                } else {
                    if (kind == CsmDeclaration.Kind.FUNCTION) {
                        iconPath = FUNCTION_DECLARATION_GLOBAL;
                    } else {
                        iconPath = FUNCTION_GLOBAL;
                    }
                }
                if (isStatic) {
                    if (isOperator) {
                        iconPath = OPERATOR_ST_GLOBAL;
                    } else {
                        iconPath = FUNCTION_ST_GLOBAL;
                    }
                }
            }
            if (isMethod) {
                if (isOperator) {
                    iconPath = OPERATOR_PUBLIC;
                } else {
                    iconPath = METHOD_PUBLIC;
                }
                if (isStatic){
                    //static method
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PRIVATE;
                            } else {
                                iconPath = METHOD_ST_PRIVATE;
                            }
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PROTECTED;
                            } else {
                                iconPath = METHOD_ST_PROTECTED;
                            }
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_ST_PUBLIC;
                            } else {
                                iconPath = METHOD_ST_PUBLIC;
                            }
                            break;
                    }
                }else{
                    switch (level) {
                        case CsmUtilities.PRIVATE_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PRIVATE;
                            } else {
                                iconPath = METHOD_PRIVATE;
                            }
                            break;
                        case CsmUtilities.PROTECTED_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PROTECTED;
                            } else {
                                iconPath = METHOD_PROTECTED;
                            }
                            break;
                        case CsmUtilities.PUBLIC_LEVEL:
                            if (isOperator) {
                                iconPath = OPERATOR_PUBLIC;
                            } else {
                                iconPath = METHOD_PUBLIC;
                            }
                            break;
                    }
                }
            }
            if (isConstructor) {
                iconPath = CONSTRUCTOR_PUBLIC;
                switch (level) {
                    case CsmUtilities.PRIVATE_LEVEL:
                        iconPath = CONSTRUCTOR_PRIVATE;
                        break;
                    case CsmUtilities.PROTECTED_LEVEL:
                        iconPath = CONSTRUCTOR_PROTECTED;
                        break;
                    case CsmUtilities.PUBLIC_LEVEL:
                        iconPath = CONSTRUCTOR_PUBLIC;
                        break;
                }
            }
            if (isDestructor) {
                iconPath = DESTRUCTOR_PUBLIC;
                switch (level) {
                    case CsmUtilities.PRIVATE_LEVEL:
                        iconPath = DESTRUCTOR_PRIVATE;
                        break;
                    case CsmUtilities.PROTECTED_LEVEL:
                        iconPath = DESTRUCTOR_PROTECTED;
                        break;
                    case CsmUtilities.PUBLIC_LEVEL:
                        iconPath = DESTRUCTOR_PUBLIC;
                        break;
                }
            }
        }
        return iconPath;
    }    

    private static ImageIcon getCachedImageIcon(String iconPath) {
        ImageIcon icon = map.get(iconPath);
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon(iconPath, false);
            map.put(iconPath, icon);
        }
        return icon;
    }    
}
