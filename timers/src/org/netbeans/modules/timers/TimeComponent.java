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
package org.netbeans.modules.timers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Lahoda
 */
public class TimeComponent extends TopComponent {

    private static final String PREFERRED_ID = "timers"; //NOI18N
            static final String ICON_PATH = "org/netbeans/modules/timers/resources/timer.png"; //NOI18N
    private static TimeComponent INSTANCE;
    
    /**
     * Creates a new instance of TimeComponent
     */
    public TimeComponent() {
        setName ("timers"); //NOI18N
        setDisplayName (NbBundle.getMessage ( TimeComponent.class, "LBL_TimeComponent" )); //NOI18N
        setIcon(Utilities.loadImage(ICON_PATH));
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(new TimeComponentPanel(), gridBagConstraints);
    }

    public @Override String preferredID () {
        return PREFERRED_ID;
    }
    

    public @Override int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized TimeComponent getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new TimeComponent();
        }
        return INSTANCE;
    }
    
    public static synchronized TimeComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find TimeComponent component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TimeComponent) {
            return (TimeComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
}
