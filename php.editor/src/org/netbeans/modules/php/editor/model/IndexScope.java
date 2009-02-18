/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.model;

import java.util.List;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.PHPIndex;

/**
 * @author Radek Matous
 */
public interface IndexScope extends Scope {
    //for now implemented on top of PHPIndex
    PHPIndex getIndex();
    //globally visible
    List<? extends InterfaceScope> findInterfaces(final String... ifaceName);
    List<? extends InterfaceScope> findInterfaces(final QuerySupport.Kind nameKind, final String... ifaceName);
    List<? extends TypeScope> findTypes(final String... typeName);
    List<? extends TypeScope> findTypes(final QuerySupport.Kind nameKind, final String... typeName);
    List<? extends ClassScope> findClasses(final String... className);
    List<? extends ClassScope> findClasses(final QuerySupport.Kind nameKind, final String... className);
    List<? extends FunctionScope> findFunctions(final String... fncName);
    List<? extends FunctionScope> findFunctions(final QuerySupport.Kind nameKind, final String... fncName);
    List<? extends ConstantElement> findConstants(final String... constName);
    List<? extends ConstantElement> findConstants(final QuerySupport.Kind nameKind, final String... constName);
    List<? extends VariableName> findVariables(final String... varName);
    List<? extends VariableName> findVariables(final QuerySupport.Kind nameKind, final String... varName);
    //class members
    List<? extends MethodScope> findMethods(TypeScope type, final String methName, final int... modifiers);
    List<? extends MethodScope> findMethods(TypeScope type, final QuerySupport.Kind nameKind, final String... methName);
    List<? extends MethodScope> findMethods(TypeScope type, final QuerySupport.Kind nameKind, final String methName, final int... modifiers);
    List<? extends MethodScope>  findInheritedMethods(TypeScope typeScope, String methName);
    List<? extends ClassConstantElement> findClassConstants(TypeScope type, String... clsConstName);
    List<? extends ClassConstantElement> findClassConstants(final QuerySupport.Kind nameKind, TypeScope type, final String... clsConstName);
    List<? extends ClassConstantElement> findInheritedClassConstants(ClassScope clsScope, String constName);
    List<? extends FieldElement> findFields(QuerySupport.Kind nameKind, ClassScope cls, String fieldName, int... modifiers);
    List<? extends FieldElement> findFields(ClassScope cls, String field, int... modifiers);
    List<? extends FieldElement>findFields(ClassScope cls, int... modifiers);
    List<? extends FieldElement> findInheritedFields(ClassScope clsScope, String fieldName);
}
