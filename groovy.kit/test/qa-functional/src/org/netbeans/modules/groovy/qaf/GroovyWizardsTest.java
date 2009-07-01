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

package org.netbeans.modules.groovy.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests for Groovy specific new file wizards
 *
 * @author lukas
 */
public class GroovyWizardsTest extends GroovyTestCase {

    public GroovyWizardsTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "GroovyWizards"; //NOI18N
    }

    /**
     * Test create new Groovy class
     */
    public void testGroovy() {
        //Groovy Class
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.support.resources.Bundle", "Templates/Groovy/GroovyClass.groovy");
        createNewGroovyFile(getProject(), label);
        //in default package
        String name = "MyGroovyClass"; //NOI18N
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(""); //NOI18N
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package") < 0); //NOI18N
        //in a custom package
        name = "PkgGroovyClass"; //NOI18N
        String pkg = "my.groovy.pkg"; //NOI18N
        createNewGroovyFile(getProject(), label);
        op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(pkg);
        op.finish();
        eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package " + pkg) > -1); //NOI18N
    }

    /**
     * Test create new Groovy script
     */
    public void testGroovyScript() {
        //Groovy Script
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.support.resources.Bundle", "Templates/Groovy/GroovyScript.groovy");
        createNewGroovyFile(getProject(), label);
        //in default package
        String name = "MyGroovyScript"; //NOI18N
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(""); //NOI18N
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package") < 0); //NOI18N
        //in a custom package
        name = "PkgGroovyScript"; //NOI18N
        String pkg = "my.groovy.pkg"; //NOI18N
        createNewGroovyFile(getProject(), label);
        op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        op.setPackage(pkg);
        op.finish();
        eo = new EditorOperator(name + ".groovy"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("package " + pkg) > -1); //NOI18N
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GroovyWizardsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}
