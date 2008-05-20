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
package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ValidatingPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class ComponentDescriptorWizardPanel implements Panel, FinishablePanel,
        ValidatingPanel
{
    ComponentDescriptorWizardPanel() {
        myListeners = new CopyOnWriteArrayList<ChangeListener>();   
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    public Component getComponent() {
        if (myComponent == null) {
            myComponent = new ComponentDescriptorVisualPanel( this );
            myComponent.setName(
                    NbBundle.getMessage(NewComponentDescriptor.class, 
                            NewComponentDescriptor.COMPONENT_DESCR_STEP));
        }
        return myComponent;    
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    public HelpCtx getHelp() {
        return new HelpCtx( ComponentDescriptorWizardPanel.class);
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    public boolean isValid() {
        return myValid;
    }

    protected void setValid(boolean nueValid) {
        if (nueValid != myValid) {
            myValid = nueValid;
            fireChange();
        }
    }

    private void fireChange() {
        ChangeListener[] listeners;
        ChangeEvent event = new ChangeEvent(this);
        synchronized (myListeners) {
            listeners = myListeners.toArray(new ChangeListener[myListeners
                    .size()]);
        }
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    public void readSettings( Object settings ) {
        WizardDescriptor descriptor = (WizardDescriptor)settings;
        myComponent.readData( descriptor );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener( ChangeListener listener ) {
        myListeners.remove( listener );
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener( ChangeListener listener ) {
        myListeners.add( listener );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    public void storeSettings( Object settings ) {
        WizardDescriptor descriptor = (WizardDescriptor)settings;
        myComponent.storeData( descriptor );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    public boolean isFinishPanel() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.ValidatingPanel#validate()
     */
    public void validate() throws WizardValidationException {
        // TODO Auto-generated method stub

    }
    
    private List<ChangeListener> myListeners; 
    private ComponentDescriptorVisualPanel myComponent;
    private boolean myValid = true;

}
