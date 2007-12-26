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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * utility methods for sorting Csm elements
 *
 * @author Vladimir Voskresensky
 */
@SuppressWarnings("unchecked")
public class CsmSortUtilities {
    
    /* ------------------- COMPARATORS --------------------------- */
    public static final Comparator CLASS_NAME_COMPARATOR = new DefaultClassNameComparator();
    public static final Comparator INSENSITIVE_CLASS_NAME_COMPARATOR = new InsensitiveClassNameComparator();
    public static final Comparator NATURAL_MEMBER_NAME_COMPARATOR = new NaturalMemberNameComparator(true);
    public static final Comparator INSENSITIVE_NATURAL_MEMBER_NAME_COMPARATOR = new NaturalMemberNameComparator();
    public static final Comparator NATURAL_NAMESPACE_MEMBER_COMPARATOR = new NsNaturalMemberNameComparator(true);
    public static final Comparator INSENSITIVE_NATURAL_NAMESPACE_MEMBER_COMPARATOR = new NsNaturalMemberNameComparator(false);
    
    /** Creates a new instance of CsmSortUtilities */
    private CsmSortUtilities() {
    }
    
    /// match names
        
    public static boolean matchName(CharSequence name, CharSequence strPrefix) {
	return matchName(name, strPrefix, false);
    }
    
    public static boolean matchName(CharSequence name, CharSequence strPrefix, boolean match) {
	return matchName(name, strPrefix, match, false);
    }
    
    public static boolean matchName(CharSequence name_, CharSequence strPrefix_, boolean match, boolean caseSensitive) {
        // mached element is not empty name
        if (name_.length() > 0) {
            String name = name_.toString();
            String strPrefix = strPrefix_.toString();
            if (!caseSensitive) {
                name = name.toLowerCase();
                strPrefix = strPrefix.toLowerCase();
            }
            if (strPrefix.length() == 0 || name.startsWith(strPrefix)) {
                return match ? (name.compareTo(strPrefix) == 0) : true;
            }
        }
	return false;
    }   
    
    public static List<CsmNamedElement> filterList(List<? extends CsmDeclaration> list, CharSequence strPrefix, boolean match, boolean caseSensitive) {
	List<CsmNamedElement> res = new ArrayList<CsmNamedElement>();
	Iterator it = list.iterator();
	while (it.hasNext()) {
	    Object elem = it.next();
	    if (CsmKindUtilities.isNamedElement(elem) && 
                    matchName(((CsmNamedElement)elem).getName(), strPrefix, match, caseSensitive)) {
		res.add((CsmNamedElement)elem);
	    }
	}
	return res;
    }
    
    /// sorting
    
    public static List sortClasses(List classes, boolean sensitive) {
        if (sensitive) {
            Collections.sort(classes, CLASS_NAME_COMPARATOR);
        } else {
            Collections.sort(classes, INSENSITIVE_CLASS_NAME_COMPARATOR);
        }
        return classes;
    }

    public static List sortMembers(List members, boolean sensitive) {
        return sortMembers(members, true, sensitive);
    }
    
    public static List sortMembers(List members, boolean natural, boolean sensitive) {
        if (sensitive) {
            Collections.sort(members, NATURAL_MEMBER_NAME_COMPARATOR);
        } else {
            Collections.sort(members, INSENSITIVE_NATURAL_MEMBER_NAME_COMPARATOR);
        }
        return members;
    }
    
    public static List sortNamespaceMembers(List members, boolean sensitive) {
        if (sensitive) {
            Collections.sort(members, NATURAL_NAMESPACE_MEMBER_COMPARATOR);
        } else {
            Collections.sort(members, INSENSITIVE_NATURAL_NAMESPACE_MEMBER_COMPARATOR);
        }
        return members;
    }
    
    //======================Comparators=================================
    
    public static final class DefaultClassNameComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if (CsmKindUtilities.isCsmObject(o1) && CsmKindUtilities.isCsmObject(o2)) {
                if (CsmKindUtilities.isClass((CsmObject)o1) && CsmKindUtilities.isClass((CsmObject)o2)){
                    return CharSequenceKey.Comparator.compare(((CsmClass)o1).getName(),((CsmClass)o2).getName());
                }
                if (CsmKindUtilities.isNamespace((CsmObject)o1) && CsmKindUtilities.isNamespace((CsmObject)o2)){
                    return CharSequenceKey.Comparator.compare(((CsmNamespace)o1).getName(),((CsmNamespace)o2).getName());
                }
            }
            
            return 0;
        }
        
    }
    
    public static final class InsensitiveClassNameComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if (CsmKindUtilities.isCsmObject(o1) && CsmKindUtilities.isCsmObject(o2)) {
                if (CsmKindUtilities.isClass((CsmObject)o1) && CsmKindUtilities.isClass((CsmObject)o2)){
                    return CharSequenceKey.ComparatorIgnoreCase.compare(((CsmClass)o1).getName(),((CsmClass)o2).getName());
                }
                if (CsmKindUtilities.isNamespace((CsmObject)o1) && CsmKindUtilities.isNamespace((CsmObject)o2)){
                    return CharSequenceKey.ComparatorIgnoreCase.compare(((CsmNamespace)o1).getName(),((CsmNamespace)o2).getName());
                }
            }
            
            return 0;
        }
        
    }
    
    public static final class NaturalMemberNameComparator implements Comparator {
        
        private boolean sensitive;
        
        public NaturalMemberNameComparator() {
            this(false);
        }
        
        private NaturalMemberNameComparator(boolean sensitive) {
            this.sensitive = sensitive;
        }
        
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if (CsmKindUtilities.isCsmObject(o1) && CsmKindUtilities.isCsmObject(o2)) {
                CsmObject csm1 = (CsmObject)o1;
                CsmObject csm2 = (CsmObject)o2;
                
                // variables
                boolean var1 = CsmKindUtilities.isVariable(csm1);
                boolean var2 = CsmKindUtilities.isVariable(csm2);
                if (var1 || var2) {
                    if (var1 && var2) {
                        return compareVariables((CsmVariable)csm1, (CsmVariable)csm2, sensitive);
                    } else if (var1) {
                        // variable is greater than others
                        return -1;
                    } else {
                        // variable is greater than others
                        assert (var2);
                        return 1;
                    }
                }                
                
                // enumerators (items of enumeration)
                boolean enmtr1 = CsmKindUtilities.isEnumerator(csm1);
                boolean enmtr2 = CsmKindUtilities.isEnumerator(csm2);
                if (enmtr1 || enmtr2) {
                    if (enmtr1 && enmtr2) {
                        return compareEnumerators((CsmEnumerator)csm1, (CsmEnumerator)csm2, sensitive);
                    } else if (enmtr1) {
                        // enumerator is greater than others
                        return -1;
                    } else {
                        // enumerator is greater than others
                        assert (enmtr2);
                        return 1;
                    }
                }                

                // functions
                boolean fun1 = CsmKindUtilities.isFunction(csm1);
                boolean fun2 = CsmKindUtilities.isFunction(csm2);
                if (fun1 || fun2){
                    if (fun1 && fun2) {
                        return compareFunctions((CsmFunction)csm1, (CsmFunction)csm2, sensitive);
                    } else if (fun1) {
                        // functions are lesser, than other elements
                        return -1;
                    } else {
                        assert (fun2);
                        // functions are lesser, than other elements
                        return 1;
                    }
                }

                // macros
                boolean mac1 = CsmKindUtilities.isMacro(csm1);
                boolean mac2 = CsmKindUtilities.isMacro(csm2);
                if (mac1 || mac2){
                    if (mac1 && mac2) {
                        return compareMacros((CsmMacro)csm1, (CsmMacro)csm2, sensitive);
                    } else if (mac1) {
                        // macros are lesser, than other elements
                        return -1;
                    } else {
                        assert (mac2);
                        // macros are lesser, than other elements
                        return 1;
                    }
                }
            }
            return 0;
        }
    }
    
    public static final class NsNaturalMemberNameComparator implements Comparator {
        
        private boolean sensitive;
        
        public NsNaturalMemberNameComparator() {
            this(false);
        }
        
        private NsNaturalMemberNameComparator(boolean sensitive) {
            this.sensitive = sensitive;
        }
        
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            if (CsmKindUtilities.isCsmObject(o1) && CsmKindUtilities.isCsmObject(o2)) {
                CsmObject csm1 = (CsmObject)o1;
                CsmObject csm2 = (CsmObject)o2;
                // NS > Classesifier > Var > Funs
                
                // namespaces
                boolean ns1 = CsmKindUtilities.isNamespace(csm1);
                boolean ns2 = CsmKindUtilities.isNamespace(csm2);
                if (ns1 || ns2) {
                    if (ns1 && ns2) {
                        return compareNames((CsmNamespace)csm1, (CsmNamespace)csm2, sensitive);
                    } else if (ns1) {
                        // namespace is greater than others
                        return -1;
                    } else {
                        // namespace is greater than others
                        assert (ns2);
                        return 1;
                    }
                }
                
                // classifiers
                boolean cls1 = CsmKindUtilities.isClassifier(csm1);
                boolean cls2 = CsmKindUtilities.isClassifier(csm2);
                if (cls1 || cls2) {
                    if (cls1 && cls2) {
                        return compareNames((CsmClassifier)csm1, (CsmClassifier)csm2, sensitive);
                    } else if (cls1) {
                        // classifier is greater than others
                        return -1;
                    } else {
                        // classifier is greater than others
                        assert (cls2);
                        return 1;
                    }
                }
                
                // variables
                boolean var1 = CsmKindUtilities.isVariable(csm1);
                boolean var2 = CsmKindUtilities.isVariable(csm2);
                if (var1 || var2) {
                    if (var1 && var2) {
                        return compareVariables((CsmVariable)csm1, (CsmVariable)csm2, sensitive);
                    } else if (var1) {
                        // variable is greater than others
                        return -1;
                    } else {
                        // variable is greater than others
                        assert (var2);
                        return 1;
                    }
                }                
                
                // functions
                boolean fun1 = CsmKindUtilities.isFunction(csm1);
                boolean fun2 = CsmKindUtilities.isFunction(csm2);
                if (fun1 || fun2){
                    if (fun1 && fun2) {
                        return compareFunctions((CsmFunction)csm1, (CsmFunction)csm2, sensitive);
                    } else if (fun1) {
                        // functions are lesser, than other elements
                        return -1;
                    } else {
                        assert (fun2);
                        // functions are lesser, than other elements
                        return 1;
                    }
                }
            }
            return 0;
        }
    }    
    
    private static int compareNames(CsmNamedElement elem1, CsmNamedElement elem2, boolean sensitive) {
        int order = sensitive ?
                        CharSequenceKey.Comparator.compare(elem1.getName(),elem2.getName()) :
                        CharSequenceKey.ComparatorIgnoreCase.compare(elem1.getName(),elem2.getName());        
        return order;
    }
    
    private static int compareVariables(CsmVariable var1, CsmVariable var2, boolean sensitive) {
        int order = compareNames(var1, var2, sensitive);

        //do not allow fields merge
        int sameName = CharSequenceKey.Comparator.compare(var1.getName(),var2.getName());
        if (order == 0 && sameName != 0) order = sameName;

        return order;
    }
    
    private static int compareEnumerators(CsmEnumerator enmtr1, CsmEnumerator enmtr2, boolean sensitive) {
        int order = compareNames(enmtr1, enmtr2, sensitive);

        //do not allow fields merge
        int sameName = CharSequenceKey.Comparator.compare(enmtr1.getName(),enmtr2.getName());
        if (order == 0 && sameName != 0) order = sameName;

        return order;
    }
    
    private static int compareFunctions(CsmFunction fun1, CsmFunction fun2, boolean sensitive) {
        int order = compareNames(fun1, fun2, sensitive);
        if (order == 0 ){
            CsmParameter[] param1 = (CsmParameter[]) fun1.getParameters().toArray(new CsmParameter[0]);
            CsmParameter[] param2 = (CsmParameter[]) fun2.getParameters().toArray(new CsmParameter[0]);

            int commonCnt = Math.min(param1.length, param2.length);
            for (int i = 0; i < commonCnt; i++) {
                
                try {
                    // TODO: access to getClassifier is too expensive (calls renderer)
                    // should be changed to cheap one
                    order = sensitive ?
                        CharSequenceKey.Comparator.compare(param1[i].getType().getText(),param2[i].getType().getText()) :
                        CharSequenceKey.ComparatorIgnoreCase.compare(param1[i].getType().getText(),param2[i].getType().getText());
                } catch (NullPointerException ex) {
                    order = 0;
                    // IZ #76035. Unfortunately getType() sometimes returns null  
                    // FIXUP: in fact varargs should have dummy type instead of null
                    // so check for var args
                    if (param1[i].isVarArgs() != param2[i].isVarArgs()) {
                        order = param1[i].isVarArgs() ? -1 : 1;
                    } else if (!param1[i].isVarArgs()) {
                        System.err.println("CsmSortUtilities.compareFunctions: error while checking parameter " + i 
                                + "of functions" + fun1 + " and " + fun2); // NOI18N
                        ex.printStackTrace(System.err);
                    }                    
                }

                if (order != 0) {
                    return order;
                }
            }
            order = param1.length - param2.length;
        }

        //do not allow methods merge
        int sameName = CharSequenceKey.Comparator.compare(fun1.getName(),fun2.getName());
        if (order == 0 && sameName != 0) order = sameName;

        return order;
    }

    
    private static int compareMacros(CsmMacro fun1, CsmMacro fun2, boolean sensitive) {
        int order = compareNames(fun1, fun2, sensitive);
        if (order == 0 ){
            int size1 = 0;
            if (fun1.getParameters() != null){
                size1 = fun1.getParameters().size();
            }
            int size2 = 0;
            if (fun2.getParameters() != null){
                size2 = fun2.getParameters().size();
            }
            order =  size1 - size2;
        }
        return order;
    }
}
