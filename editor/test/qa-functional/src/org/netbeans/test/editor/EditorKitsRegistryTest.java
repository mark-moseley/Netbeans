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

package org.netbeans.test.editor;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Vita Stejskal
 */
public class EditorKitsRegistryTest extends NbTestCase {
    
    /** Creates a new instance of EditorKitsRegistryTest */
    public EditorKitsRegistryTest(String name) {
        super(name);
    }
    
    public void testHTMLEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/html");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/html", kitFromJdk);
        assertTrue("Wrong JDK kit for text/html", kitFromJdk instanceof HTMLEditorKit);
        
        // Test Netbeans kit
        EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/html");
        assertNotNull("Can't find Nb kit for text/html", kitFromNb);
        assertEquals("Wrong Nb kit for text/html", 
            "org.netbeans.modules.editor.html.HTMLKit", kitFromNb.getClass().getName());
    }

    public void testPlainEditorKits() {
        // VIS: JEditorPane when constructed contains javax.swing.JEditorPane$PlainEditorKit
        // and calling JEP.setContenetType("text/plain") has no effect. IMO this is probably
        // a defect in JDK, becuase JEP should always honour its EditorKit registry.
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(new DefaultEditorKit() {
            public String getContentType() {
                return "text/whatever";
            }
        });
        setContentTypeInAwt(pane, "text/plain");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/plain", kitFromJdk);
        assertEquals("The kit for text/plain should not be from JDK", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromJdk.getClass().getName());

        // Test Netbeans kit
        EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("Can't find Nb kit for text/plain", kitFromNb);
        assertEquals("Wrong Nb kit for text/plain", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromNb.getClass().getName());
    }

    public void testTextRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }

    public void testApplicationRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "application/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for application/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }
    
    private void setContentTypeInAwt(final JEditorPane pane, final String mimeType) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    pane.setContentType(mimeType);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't set content type in AWT: " + e.getMessage());
        }
    }
}
