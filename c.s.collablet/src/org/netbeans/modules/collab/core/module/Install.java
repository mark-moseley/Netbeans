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
package org.netbeans.modules.collab.core.module;

import com.sun.collablet.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.core.bridge.NbAccountManager;
import org.netbeans.modules.collab.core.bridge.NbAccountManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbCollabManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbCollabletFactoryManager;
import org.netbeans.modules.collab.core.bridge.NbCollabletFactoryManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbUserInterfaceLocator;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 * @version        jatotools/@version@ $Id$
 */
public class Install extends ModuleInstall {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!

    /**
     *
     *
     */
    public Install() {
        super();
    }

    /**
     *
     *
     */
    public void installed() {
        Introspector.flushCaches();
        restored();
    }

    /**
     *
     *
     */
    public void restored() {
        // Initialize the bridge between the collablet classes and NetBeans
        CollabManager.setLocator(new NbCollabManagerLocator());
        UserInterface.setLocator(new NbUserInterfaceLocator());
        AccountManager.setLocator(new NbAccountManagerLocator(new NbAccountManager()));
        CollabletFactoryManager.setLocator(new NbCollabletFactoryManagerLocator(new NbCollabletFactoryManager()));

        // Initialize the debug
        new Thread() {
                public void run() {
                    try {
                        sleep(15000);
                        Debug.initializeOut();
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                }
            }.start();
    }
}
