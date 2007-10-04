/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.utils.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.infra.utils.style.checkers.Checker;
import org.netbeans.installer.infra.utils.style.checkers.LineLengthChecker;
import org.netbeans.installer.infra.utils.style.checkers.UnescapedStringChecker;

/**
 *
 * @author ks152834
 */
public class BasicStyleCheckerEngine {
    public static void main(String[] args) throws IOException {
        BasicStyleCheckerEngine engine = new BasicStyleCheckerEngine();
        
        for (String arg: args) {
            engine.check(new File(arg));
        }
    }
    
    private List<Checker> checkers;
    
    public BasicStyleCheckerEngine() {
        checkers = new LinkedList<Checker>();
        
        checkers.add(new LineLengthChecker());
        checkers.add(new UnescapedStringChecker());
    }
    
    public void check(
            final File file) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line = null;
        for (int i = 1; (line = reader.readLine()) != null; i++) {
            String error = "";
            
            for (Checker checker: checkers) {
                if (checker.accept(file)) {
                    final String message = checker.check(line);
                    
                    if (message != null) {
                        error += "        " + message + "\n";
                    }
                }
            }
            
            if (!error.equals("")) {
                System.out.println("    Line " + i + ":");
                System.out.println(error);
            }
        }
        
        reader.close();
    }
}
