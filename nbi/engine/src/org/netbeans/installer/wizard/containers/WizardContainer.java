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

package org.netbeans.installer.wizard.containers;

import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This interface represents the container for the UI of a {@link WizardComponent}.
 * Each {@link Wizard} "owns" an instance of this class and uses it to initialize 
 * the UI of its active component.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface WizardContainer {
    /**
     * Shows or hides the container. The behavior of this method is 
     * component-specific. A frame would probably map this method directly, while
     * a console-mode container could draw itself or clear the screen.
     * 
     * @param visible Whether to show the container - <code>true</code>, or hide 
     * it - <code>false</code>.
     */
    void setVisible(final boolean visible);
    
    /**
     * Updates the container with a new UI. This method is usually called by the 
     * wizard when the active component changes - the wizard wants to display its 
     * UI.
     * 
     * @param ui UI which needs to be shown.
     */
    void updateWizardUi(final WizardUi ui);
    
    /**
     * Opens(creates) the container. This method is usually called by the wizard upon 
     * container initialization
     *      
     */
    void open();    
    
    /**
     * Closes(destroyes) the container. This method is usually called by the wizard upon 
     * container closing
     *      
     */
    void close();
    
}
