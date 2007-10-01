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
 * GenerateAbbreviationsList.java
 *
 * Created on August 29, 2002, 1:15 PM
 */

package org.netbeans.test.editor.suites.abbrevs;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.jellytools.modules.editor.Abbreviations;

/**
 *
 * @author  jl105142
 */
public class GenerateAbbreviationsList {

    /** Creates a new instance of GenerateAbbreviationsList */
    public GenerateAbbreviationsList() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Map map  = Abbreviations.listAbbreviations("Java Editor");
        Set keys = map.keySet();
        Iterator keysIterator = keys.iterator();
        
        try {
            PrintWriter pw =  new PrintWriter(new FileWriter("/tmp/abbrevs.xml"));
            
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            
            pw.println("<Test Name=\"Abbreviations\" Author=\"ehucka\" Version=\"1.1\">");
            pw.println("<TestSubTest Name=\"BasicTests\" Author=\"ehucka\" Version=\"1.0\" Own_logger=\"true\">");
            pw.println("<TestStep Name=\"InvokeAllActions\">");
            while (keysIterator.hasNext()) {
                Object key = keysIterator.next();
                Object value = map.get(key);
                
                System.err.println("\t{" + key.toString() + ", " + value.toString() + "},");
                pw.println("<TestStringAction Name=\"string\" String=\""+key.toString()+" \" />");
                pw.println("<TestLogAction Name=\"caret-end-line\" Command=\"\" />");
                pw.println("<TestLogAction Name=\"insert-break\" Command=\"\" />");
            }
            pw.println("</TestStep>");
            pw.println("</TestSubTest>");
            pw.println("<Comment>");
            pw.println("<![CDATA[\"Test of invoking all abbreviations.\"]]>");
            pw.println("</Comment>");
            pw.println("</Test>\n");
            
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
