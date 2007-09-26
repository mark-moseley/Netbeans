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

package org.netbeans;

import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/** Bootstrap main class.
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class Main extends Object {
    private Main() {
    }

    /** Starts the NetBeans system.
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        // following code has to execute without java5 - e.g. do not use
        // NbBundle or any other library compiled against java5 only
        // also prevent usage of java5 methods and classes
        try {
            Class.forName("java.lang.StringBuilder"); // NOI18N
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                null,
                ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_InstallJava5"),
                ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_NeedsJava5"),
                JOptionPane.WARNING_MESSAGE
            );
            System.exit(10);
        }
        // end of java5 only code

        MainImpl.main(args);
    }
    
    /** Returns string describing usage of the system. Does that by talking to
     * all registered handlers and asking them to show their usage.
     *
     * @return the usage string for the system
     */
    public static String usage () throws Exception {
        return MainImpl.usage();
    }
        
    
    /**
     * Call when the system is up and running, to complete handling of
     * delayed command-line options like -open FILE.
     */
    public static void finishInitialization() {
        MainImpl.finishInitialization();
    }
}
