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

package org.netbeans.modules.db.util;

import java.net.URL;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseExplorerInternalUIsTest extends TestBase {

    private JDBCDriver driver1 = null;
    private JDBCDriver driver2 = null;

    public DatabaseExplorerInternalUIsTest(String testName) {
        super(testName);
    }

    private void setUpDrivers() throws Exception {
        removeDrivers();

        driver1 = JDBCDriver.create("foo_driver", "FooDriver", "org.foo.FooDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver1);
        driver2 = JDBCDriver.create("bar_driver", "BarDriver", "org.bar.BarDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver2);
        assertEquals(2, JDBCDriverManager.getDefault().getDrivers().length);
    }

    private void removeDrivers() throws Exception {
        FileObject driversFO = Util.getDriversFolder();
        FileObject[] children = driversFO.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].delete();
        }
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);
    }

    public void testEmptyComboboxContent() throws Exception {
        removeDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 1);
    }

    public void testComboboxWithDrivers() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 4);
        JdbcUrl url = (JdbcUrl)combo.getItemAt(0);
        assertSame(driver2, url.getDriver());
        assertSame(driver2.getClassName(), url.getClassName());
        assertSame(driver2.getDisplayName(), url.getDisplayName());
        
        url = (JdbcUrl)combo.getItemAt(1);
        assertSame(driver1,  url.getDriver());
        assertSame(driver1.getClassName(), url.getClassName());
        assertSame(driver1.getDisplayName(), url.getDisplayName());
    }

    public void testComboBoxWithDriverClass() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault(), "org.bar.BarDriver");

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 1);
        JdbcUrl url = (JdbcUrl)combo.getItemAt(0);
        assertSame(driver2, url.getDriver());
        assertSame(driver2.getClassName(), url.getClassName());
        assertSame(driver2.getDisplayName(), url.getDisplayName());
    }
}
