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

/*
 * SerializeDOM.java
 *
 * Created on October 30, 2001, 5:58 PM
 */

package org.netbeans.xtest.util;

import org.w3c.dom.*;
import java.io.*;
import javax.xml.parsers.*;
import org.netbeans.xtest.util.XMLWriter;
import org.netbeans.xtest.util.XMLFactoryUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author  mb115822
 * @version
 */
public class SerializeDOM {

    /** Creates new SerializeDOM */
    public SerializeDOM() {
    }


    public static DocumentBuilder getDocumentBuilder() {
        try {
            return XMLFactoryUtil.newDocumentBuilder();
        }
        catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }    
    
    public static void serializeToStream(Document doc, OutputStream out) throws IOException {
        XMLWriter xmlWriter = new XMLWriter(out, "UTF-8");
        xmlWriter.write(doc);
        //out.close();
    }
    
    public static void serializeToFile(Document doc, File outFile) throws IOException {
        OutputStream out = new FileOutputStream(outFile);
        serializeToStream(doc,out);
        out.close();
    }
    
        public static Document parseStream(InputStream input) throws IOException {
        try {
            Document parsedDoc = getDocumentBuilder().parse(input);
            return parsedDoc;
        } catch (SAXException saxe) {
            throw new IOException(saxe.getMessage());
        }
    }
    
     public static Document parseFile(File inputFile) throws IOException {
        try {
            Document parsedDoc = getDocumentBuilder().parse(inputFile);
            return parsedDoc;
        } catch (SAXException saxe) {
            throw new IOException(saxe.getMessage());
        }
    }


}
