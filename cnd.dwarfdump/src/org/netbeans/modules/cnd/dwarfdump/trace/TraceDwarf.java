/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.cnd.dwarfdump.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;


/**
 *
 * @author Sergey Grinev
 */
public class TraceDwarf {
    
    public static boolean TRACED = false;

    private TraceDwarf() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TRACED = true;
        String objFileName = args[0];
        Dwarf dump = null;
        try {
            System.out.println("TraceDwarf.");  // NOI18N
            dump = new Dwarf(objFileName);
            List<CompilationUnit> units = dump.getCompilationUnits();
            int idx = 0;
            if (units != null && units.size() > 0) {
                System.out.println("\n**** Done. " + units.size() + " compilation units were found:"); // NOI18N
                for (CompilationUnit compilationUnit : units) {
                    System.out.println(++idx + ": " + compilationUnit.getSourceFileName());// NOI18N
                }

            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            System.out.println("File not found " + objFileName + ": " + ex.getMessage());  // NOI18N
        } catch (WrongFileFormatException ex) {
            System.out.println("Unsuported format of file " + objFileName + ": " + ex.getMessage());  // NOI18N
        } catch (IOException ex) {
            System.err.println("Exception in file " + objFileName);  // NOI18N
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Exception in file " + objFileName);  // NOI18N
            ex.printStackTrace();
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
    }
}
