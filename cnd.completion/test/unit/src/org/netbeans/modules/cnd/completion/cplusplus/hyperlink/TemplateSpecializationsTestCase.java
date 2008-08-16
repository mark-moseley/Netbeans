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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

/**
 *
 * @author Vladimir Kvashin
 */
public class TemplateSpecializationsTestCase extends HyperlinkBaseTestCase {

    public TemplateSpecializationsTestCase(String testName) {
        super(testName);
    }

    public void testIZ144156_func_partial_spec_pointer() throws Exception {
        performTest("template_fun_spec.cc", 12, 33, "template_fun_spec.cc", 40, 1); // partial spec. for T*
        performTest("template_fun_spec.cc", 40, 33, "template_fun_spec.cc", 12, 1); // and back
    }
    public void testIZ144156_func_full_spec_char() throws Exception {
        performTest("template_fun_spec.cc", 18, 26, "template_fun_spec.cc", 50, 1); // full spec. for char
        performTest("template_fun_spec.cc", 50, 26, "template_fun_spec.cc", 18, 1); // and back
    }
    public static class Failed extends HyperlinkBaseTestCase {

        @Override
        protected Class getTestCaseDataClass() {
            return TemplateSpecializationsTestCase.class;
        }

        public void testIZ144156_func_spec_main() throws Exception {
            performTest("template_fun_spec.cc", 9, 33, "template_fun_spec.cc", 35, 1); // base template
            performTest("template_fun_spec.cc", 35, 33, "template_fun_spec.cc", 9, 1); // and back
        }
        public void testIZ144156_func_partial_spec_pair() throws Exception {
            performTest("template_fun_spec.cc", 15, 33, "template_fun_spec.cc", 45, 1); // partial spec. for pair<T,T>
            performTest("template_fun_spec.cc", 45, 33, "template_fun_spec.cc", 15, 1); // and back
        }
        public void testIZ144156_func_full_spec_pair_char() throws Exception {
            performTest("template_fun_spec.cc", 21, 26, "template_fun_spec.cc", 55, 1); // full spec. for pair<char,char>
            performTest("template_fun_spec.cc", 55, 26, "template_fun_spec.cc", 21, 1); // and back
        }
        
        public Failed(String testName) {
            super(testName, true);
        }
    }
    
}
