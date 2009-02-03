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
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Tests for New REST web services from Database wizard
 *
 * @author lukas
 */
public class FromDBTest extends CRUDTest {

    public FromDBTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "FromDB"; //NOI18N
    }

    @Override
    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.fromdb"; //NOI18N
    }

    public void testFromDB() throws IOException {
        copyDBSchema();
        createPU();
        //RESTful Web Services from Database
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromDatabase");
        createNewWSFile(getProject(), restLabel);
        //Entity Classes from Database
        String fromDbLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "Templates/Persistence/RelatedCMP");
        WizardOperator wo = prepareEntityClasses(new WizardOperator(fromDbLabel), false);
        wo.next();
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".service"); //NOI18N
        jcbo = new JComboBoxOperator(wo, 2);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".converter"); //NOI18N
        wo.finish();
        String generationTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "TXT_EntityClassesGeneration");
        waitDialogClosed(generationTitle);
        new EventTool().waitNoEvent(1500);
        ProjectSupport.waitScanFinished();
        Set<File> files = getFiles(getRestPackage() + ".service"); //NOI18N
        files.addAll(getFiles(getRestPackage() + ".converter")); //NOI18N
        assertEquals("Some files were not generated", 30, files.size()); //NOI18N
        //make sure all REST services nodes are visible in project log. view
        assertEquals("missing nodes?", 14, getRestNode().getChildren().length);
    }

    private void createPU() {
        //Persistence
        String category = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.ui.resources.Bundle", "Templates/Persistence");
        //Persistence Unit
        String puLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.unit.Bundle", "Templates/Persistence/PersistenceUnit");
        createNewFile(getProject(), category, puLabel);
        String title = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.unit.Bundle", "LBL_NewPersistenceUnit");
        WizardOperator wo = new WizardOperator(title);
        new JComboBoxOperator(wo, 1).selectItem("jdbc/sample"); //NOI18N
        wo.finish();
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH, NbModuleSuite.createConfiguration(FromDBTest.class),
                "testFromDB", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy").enableModules(".*").clusters(".*")); //NOI18N
    }
}
