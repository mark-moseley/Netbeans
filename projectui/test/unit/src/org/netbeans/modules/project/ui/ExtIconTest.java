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

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.URL;
import javax.swing.ImageIcon;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ExtIconTest extends TestCase {
    
    public ExtIconTest(String testName) {
        super(testName);
    }
    
    public void testByteConversions() {
        ExtIcon ext = new ExtIcon();
        URL res = getClass().getClassLoader().getResource("org/netbeans/modules/project/ui/module.gif");
        assertNotNull(res);
        ImageIcon icon = new ImageIcon(res);
        ext.setIcon(icon);
        try {
            byte[] bytes1 = ext.getBytes();
            ExtIcon ext2 = new ExtIcon(bytes1);
            byte[] bytes2 = ext2.getBytes();
            ExtIcon ext3 = new ExtIcon(bytes2);
            byte[] bytes3 = ext3.getBytes();
            
            assertEquals(bytes1.length, bytes2.length);
            assertEquals(bytes3.length, bytes3.length);
            for (int i = 0; i < bytes1.length; i++) {
                assertEquals("Non equals at position " + i,bytes1[i], bytes2[i]);
                assertEquals("Non equals at position " + i,bytes1[i], bytes3[i]);
            }
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            fail();
        }
        
    }
    
}
