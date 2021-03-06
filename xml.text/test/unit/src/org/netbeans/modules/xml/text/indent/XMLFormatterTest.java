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

package org.netbeans.modules.xml.text.indent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import javax.swing.JTextArea;
import junit.framework.*;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomasz Slota
 */
public class XMLFormatterTest extends NbTestCase {
    /**
     * Used for testing dynamic indentation ("smart enter")
     */
    public final static String LINE_BREAK_METATAG = "|";
    
    public XMLFormatterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(XMLFormatterTest.class);
        
        return suite;
    }

    public void testReformatSample1(){
        testReformat("web.xml");
    }
    
    public void testReformatSample2(){
        testReformat("netbeans_build.xml");
    }
    
    private String extractCRMetatag(String text){
        int indexOfMetatag = text.indexOf(LINE_BREAK_METATAG);
        
        if (indexOfMetatag == -1){
            return text;
        }
        
        String firstPart = text.substring(0, indexOfMetatag);
        String secondPart = text.substring(indexOfMetatag + LINE_BREAK_METATAG.length());
        
        if (secondPart.indexOf(LINE_BREAK_METATAG) > -1){
            throw new IllegalArgumentException("text contains more than one line break tag");
        }
        
        return firstPart + secondPart;
    }
    
    private void testReformat(String testFileName) {
        System.out.println("testReformat(" + testFileName + ")");
        XMLFormatter formatter = new XMLFormatter(XMLKit.class);
        BaseDocument doc = createEmptyBaseDocument();
         
        try{
            String txtRawXML = readStringFromFile(new File(new File(
                getTestFilesDir(), "testReformat"), testFileName));
                    
            doc.insertString(0, txtRawXML, null);
            formatter.reformat(doc, 0, doc.getLength(), false);
            getRef().print(doc.getText(0, doc.getLength()));
        }
        catch (Exception e){
            fail(e.getMessage()); // should never happen
        }
        
        compareReferenceFiles();
    }

    private String readStringFromFile(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        
        String line;
        
        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff.toString();
    }
    
    private File getTestFilesDir(){
        return new File(new File(getDataDir(), "input"), "XMLFormatterTest");
    }
    
    private BaseDocument createEmptyBaseDocument(){
        return new BaseDocument(XMLKit.class, false);
    }
}
