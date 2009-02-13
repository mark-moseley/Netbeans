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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Pull Up refactoring.
 *
 * @author Martin Matula
 */
public class PullUpRefactoringUI implements RefactoringUI {
    // reference to pull up refactoring this UI object corresponds to
    private final PullUpRefactoring refactoring;
    // initially selected members
    private final Set initialMembers;
    // UI panel for collecting parameters
    private PullUpPanel panel;

    private String description;
    
    /** Creates a new instance of PullUpRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    public PullUpRefactoringUI(TreePathHandle selectedElements, CompilationInfo info) {
        initialMembers = new HashSet();
        initialMembers.add(MemberInfo.create(selectedElements.resolveElement(info),info));
        // compute source type and members that should be pre-selected from the
        // set of elements the action was invoked on
        
       // create an instance of pull up refactoring object
        Element selected = selectedElements.resolveElement(info);
        if (!(selected instanceof TypeElement))
            selected = info.getElementUtilities().enclosingTypeElement(selected);
        TreePath tp = info.getTrees().getPath(selected);
        TreePathHandle sourceType = TreePathHandle.create(tp, info);
        description = ElementHeaders.getHeader(tp, info, ElementHeaders.NAME);
        refactoring = new PullUpRefactoring(sourceType);
        refactoring.getContext().add(info.getClasspathInfo());
        
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new PullUpPanel(refactoring, initialMembers, parent);
        }
        return panel;
    }

    public Problem setParameters() {
        captureParameters();
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        captureParameters();
        return refactoring.fastCheckParameters();
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return NbBundle.getMessage(PullUpAction.class, "DSC_PullUp", description); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(PullUpAction.class, "LBL_PullUp"); // NOI18N
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PullUpRefactoringUI.class.getName());
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setTargetType(panel.getTargetType().getElementHandle());
        refactoring.setMembers(panel.getMembers());
    }
    
//    /** Method that computes the source type and initially selected members from
//     * elements the refactoring action was invoked on.
//     * It tries to find a common parent class for the elements to return it as the
//     * the source type. If not all elements
//     * have a common parent class, then the class that is a parent class for majority
//     * of the elements is chosen. The parent JavaClass, Field, Method or MultipartId that
//     * is part of interface names of a class is taken as the pre-selected member.
//     * @elements The elements the refactoring was invoked on.
//     * @initialMembers Should be an empty set that this method will add the members
//     *      that should be selected.
//     * @return Source type.
//     */
//    private static JavaClass getSourceType(Element[] elements, Set initialMembers) {
//        JavaClass result = null;
//        // map that will be used to compute final source type and pre-selected members
//        // maps suggested source type to a set of its suggested pre-selected members
//        HashMap elementsByClass = new HashMap();
//       
//        for (int i = 0; i < elements.length; i++) {
//            Element element = null;
//            Element temp = elements[i];
//            // iterate through the containers of the element (until we get to null
//            // or a resource)
//            while (temp != null && !(temp instanceof Resource)) {
//                if ((temp instanceof JavaClass) || (temp instanceof Field) || (temp instanceof Method)) {
//                    // if the current element is a class, field or a method, exit
//                    // the loop - we have an element that will likely be the member
//                    // to be initially selected
//                    break;
//                } else if (temp instanceof MultipartId) {
//                    // if the current element is a MultipartId, remember it, but
//                    // go further to find the top-most MultipartId
//                    // if the MultipartId is part of interface names of a class,
//                    // then its direct parent should be JavaClass, so when
//                    // we exit the loop, "element" variable will contain the MultipartId
//                    // and the "temp" variable will contain its parent class.
//                    element = temp;
//                }
//                temp = (Element) temp.refImmediateComposite();
//            }
//            // if temp is null, the element is not in a resource -> ignore it
//            if (temp == null) continue;
//            // if element is a resource, find the primary class in it
//            // (the class with the same name as the resource)
//            if (temp instanceof Resource) {
//                element = null;
//                String name = ((Resource) temp).getName();
//                int start = name.lastIndexOf('/') + 1;
//                int end = name.indexOf('.', start);
//                if (end < 0) end = name.length();
//                name = name.substring(start, end);
//                for (Iterator it = ((Resource) temp).getClassifiers().iterator(); it.hasNext();) {
//                    JavaClass cls = (JavaClass) it.next();
//                    temp = cls;
//                    // if the class of a same name is found, exit the loop
//                    if (name.equals(cls.getSimpleName())) break;
//                }
//                // if no class of the same name is found, then the last class in
//                // the resource is taken as the selected one
//            }
//            if (temp instanceof JavaClass) {
//                // if the found element is a class, check whether element is not null
//                // and is part of the interface names in class implements clause
//                if (element == null || !((JavaClass) temp).getInterfaceNames().contains(element)) {
//                    // if not, add the class as a suggested source type (i.e. in place of a key in the map)
//                    addToMap(elementsByClass, (JavaClass) temp, null);
//                } else {
//                    // if so, the selected element is the interface name
//                    // put the class into element variable (which will later be added as a key
//                    // - i.e. as the suggested source type - into the map) and
//                    // the interface name into the temp variable (which will later be added
//                    // as a value to the map - i.e. as a suggested pre-selected member)
//                    Element cls = temp;
//                    temp = element;
//                    element = cls;
//                }
//            }
//            if (temp instanceof Feature) {
//                // if the element found is a feature (i.e. either JavaClass, or Field or a Method)
//                // store its declaring class in element variable (as a suggested source type)
//                element = ((Feature) temp).getDeclaringClass();
//            }
//            // if the thing in the element variable (i.e. the suggested source type)
//            // is of a correct type (i.e. JavaClass) add the type and the member to the
//            // map that will be used to compute final source type and pre-selected members
//            if (element instanceof JavaClass) {
//                addToMap(elementsByClass, (JavaClass) element, temp);
//            }
//        }
//
//        // now go through the map and find the suggested source type corresponding
//        // to the highest number of the pre-selected members
//        Set maxMembers = Collections.EMPTY_SET;
//        for (Iterator it = elementsByClass.entrySet().iterator(); it.hasNext();) {
//            Map.Entry entry = (Map.Entry) it.next();
//            Set value = (Set) entry.getValue();
//            // if the number of members for this source type
//            // is higher than the last max., take it as max
//            // note that even when the number is equal, but the set of members contains
//            // null, it takes a precedence - this is to correctly handle the case when the
//            // only selected element is an inner class (the map will contain two records:
//            // 1 - outer->inner, 2 - inner->null). In this case the algorithm chooses the inner class
//            // to be the source type with no members pre-selected.
//            if ((maxMembers.size() < value.size()) || ((maxMembers.size() == value.size()) && value.contains(null))) {
//                maxMembers = value;
//                result = (JavaClass) entry.getKey();
//            }
//        }
//        initialMembers.addAll(maxMembers);
//        
//        return result;
//    }
    
//    /** Helper method that simplifies adding members to the map of
//     * suggested source types to the set of members.
//     * @param map Map to add a new record to.
//     * @param parentClass Map key - suggested source type.
//     * @param member A new value for the key - pre-selected member.
//     */
//    private static void addToMap(Map map, JavaClass parentClass, Element member) {
//        Set value = (Set) map.get(parentClass);
//        if (value == null) {
//            value = new HashSet();
//            map.put(parentClass, value);
//        }
//        value.add(member);
//    }
}