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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class EditorOptionsPanelController extends OptionsPanelController {
    private EditorPropertySheet panel;
    private static final boolean TRACE = false;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    
    public EditorOptionsPanelController(){
        if (TRACE) System.out.println("EditorOptionsPanelController.ctor()"); // NOI18N
         panel = new EditorPropertySheet(this);
    }
    
    public void update() {
        if (TRACE) System.out.println("EditorOptionsPanelController.update()"); // NOI18N
        changed = false;
	panel.load();
    }
    
    public void applyChanges() {
        if (TRACE) System.out.println("EditorOptionsPanelController.applyChanges()"); // NOI18N
	panel.store();
    }
    
    public void cancel() {
        if (TRACE) System.out.println("EditorOptionsPanelController.cancel()"); // NOI18N
	panel.cancel();
    }
    
    public boolean isValid() {
        if (TRACE) System.out.println("EditorOptionsPanelController.isValid()"); // NOI18N
        return true;
    }
    
    public boolean isChanged() {
        if (TRACE) System.out.println("EditorOptionsPanelController.isChanged()"); // NOI18N
	return changed;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.advanced.formEditor"); // NOI18N
    }
    
    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
        
    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
