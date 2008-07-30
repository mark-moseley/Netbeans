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
package org.netbeans.test.j2ee.addmethod;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.test.j2ee.*;
import org.netbeans.test.j2ee.lib.Utils;

/**
 *
 * @author lm97939
 */
public abstract class AddMethodBase extends J2eeTestCase {

    protected String beanName;
    protected String editorPopup;
    protected String dialogTitle;
    protected boolean isDDModified = false;
    protected String toSearchInEditor;
    protected boolean saveFile = false;

    /** Creates a new instance of AddMethodTest */
    public AddMethodBase(String name) {
        super(name);
    }

    protected void waitForEditorText(final EditorOperator editor, final String toSearchInEditor) {
        try {
            new Waiter(new Waitable() {

                public Object actionProduced(Object obj) {
                    return editor.contains(toSearchInEditor) ? Boolean.TRUE : null;
                }

                public String getDescription() {
                    return ("Editor contains " + toSearchInEditor); // NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ie) {
            throw new JemmyException("Interrupted.", ie);
        }
    }

    protected void compareFiles() throws IOException {
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        String beanNames[] = {beanName + "Bean.java",
            beanName + "Local.java",
            beanName + "LocalBusiness.java",
            beanName + "LocalHome.java",
            beanName + "Remote.java",
            beanName + "RemoteBusiness.java",
            beanName + "RemoteHome.java",
        };
        File EJB_PROJECT_FILE = new File(new File(getDataDir(), EJBValidation.EAR_PROJECT_NAME), EJBValidation.EAR_PROJECT_NAME + "-ejb");
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/java/test"), beanNames, getName() + "_");
        String ddNames[] = {"ejb-jar.xml",
            "sun-ejb-jar.xml"
        };
        utils.assertFiles(new File(EJB_PROJECT_FILE, "src/conf"), ddNames, isDDModified ? getName() + "_" : "");
    }
}
