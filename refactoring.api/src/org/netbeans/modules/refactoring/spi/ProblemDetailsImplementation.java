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

package org.netbeans.modules.refactoring.spi;

import javax.swing.Action;
import org.openide.util.Cancellable;

/**
 * Typical implementation will invoke UI component on showDetails() call
 * This UI component will display ProblemDetails. There will be a button, or
 * similar UI control, which will be connected to rerunRefactoringAction to
 * invoke refactoring again once the Problem is fixed.
 * @author Jan Becicka
 * @since 1.5.0
 */
public interface ProblemDetailsImplementation {

    /**
     * This method will typically invoke component with ProblemDetails.
     * It is fully upon clients, how this component will be implemented.
     * @param rerunRefactoringAction this action is passed to client component
     * @param parent parent component, than can be closed by cancel method.
     * to allow clients to rerun refactoring once the Problem is fixed.
     */
    void showDetails(Action rerunRefactoringAction, Cancellable parent);

    /**
     * Message that will be displayed in parameters panel as a hint to suggest user,
     * that there are more details available.
     * @return string representation of details hint
     */
    String getDetailsHint();
    
}
