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

package org.netbeans.modules.refactoring.spi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public class RedoAction extends CallableSystemAction implements PropertyChangeListener {

    private UndoManager undoManager;

    public RedoAction() {
        putValue(Action.NAME, getString("LBL_Redo")); //NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        undoManager = UndoManager.getDefault();
        undoManager.addPropertyChangeListener(this);
        updateState();
    }
    
    public void propertyChange (PropertyChangeEvent event) {
        updateState();
    }
    
    private void updateState() {
        String desc = undoManager.getRedoDescription();
        String name = getString("LBL_Redo");
        if (desc != null) {
            name += " [" + desc + "]"; //NOI18N
        }
        
        final String n = name;
        final boolean b = undoManager.isRedoAvailable();
        Runnable r = new Runnable() {
            public void run() {
                setEnabled(b);
                putValue(Action.NAME, n);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    private static final String getString(String key) {
        return NbBundle.getMessage(RedoAction.class, key);
    }
    
    public void performAction() {
        undoManager.redo();
        undoManager.saveAll();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return (String) getValue(Action.NAME);
    }
    
    protected boolean asynchronous() {
        return true;
    }
}
