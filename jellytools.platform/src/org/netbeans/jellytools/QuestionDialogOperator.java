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

package org.netbeans.jellytools;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

/** Class implementing all necessary methods for handling "Question" dialog.
 *
 * @author Jiri.Kovalsky@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class QuestionDialogOperator extends NbDialogOperator {

    /** instance of JLabelOperator of question */
    private JLabelOperator _lblQuestion;


    /** Waits until dialog with "Question" title is found.
     * If dialog is not found, runtime exception is thrown.
     */
    public QuestionDialogOperator() {
        super(Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title"));
    }
    
    /** Waits until dialog with "Question" title and given text is found.
     * If dialog is not found, runtime exception is thrown.
     * @param questionLabelText text to be compared to text dialog
     */
    public QuestionDialogOperator(String questionLabelText) {
        this();
        _lblQuestion = new JLabelOperator(this, questionLabelText);
    }
    
    /** Returns operator of question's label.
     * @return JLabelOperator instance of question's label
     */
    public JLabelOperator lblQuestion() {
        if(_lblQuestion == null) {
            _lblQuestion = new JLabelOperator(this);
        }
        return _lblQuestion;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblQuestion();
    }
}
