/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.Component;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author thuy
 */
public class ServerLocationPanel  implements WizardDescriptor.Panel, ChangeListener {

     private ServerLocationVisual component;
    private WizardDescriptor wizard;
    private transient WLInstantiatingIterator instantiatingIterator;
    private Vector listeners = new Vector();

    public ServerLocationPanel(WLInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    public Component getComponent() {
         if (component == null) {
            component = new ServerLocationVisual(instantiatingIterator);
            component.addChangeListener(this);
        }
        return component;
    }

     private ServerLocationVisual getVisual() {
        return (ServerLocationVisual) getComponent();
    }

    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_weblogic_location"); // NOI18N
    }

    public boolean isValid() {
        return getVisual().valid(wizard);
    }

    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    public void storeSettings(Object settings) {
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        fireChangeEvent(event);
    }

    private void fireChangeEvent(ChangeEvent event) {
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }

        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }

    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
}
