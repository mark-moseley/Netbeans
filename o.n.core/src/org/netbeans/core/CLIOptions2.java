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

package org.netbeans.core;

import org.netbeans.CLIHandler;

/**
 * Shows the main window, so it is fronted when second instance of
 * NetBeans tries to start.
 *
 * @author Jaroslav Tulach
 */
public class CLIOptions2 extends CLIHandler implements Runnable {
    /** number of invocations */
    private int cnt;

    /**
     * Create a default handler.
     */
    public CLIOptions2 () {
        super(WHEN_INIT);
    }

    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }

    final int cli(String[] args) {
        if (cnt++ == 0) return 0;
        
        /*
        for (int i = 0; i < args.length; i++) {
            if ("--nofront".equals (args[i])) {
                return 0;
            }
        }
         */
        javax.swing.SwingUtilities.invokeLater (this);
        
        return 0;
    }
    
    public void run () {
        java.awt.Frame f = org.openide.windows.WindowManager.getDefault ().getMainWindow ();

        // makes sure the frame is visible
        f.setVisible(true);
        // uniconifies the frame if it is inconified
        if ((f.getExtendedState () & java.awt.Frame.ICONIFIED) != 0) {
            f.setExtendedState (~java.awt.Frame.ICONIFIED & f.getExtendedState ());
        }
        // moves it to front and requests focus
        f.toFront ();
        
    }
    
    
    protected void usage(java.io.PrintWriter w) {
        //w.println(NonGui.getString("TEXT_help"));
    }
    
}
