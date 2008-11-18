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

package org.netbeans.modules.autoupdate.featureondemand.api;

import java.awt.event.ActionListener;
import java.io.IOException;
import org.netbeans.modules.autoupdate.featureondemand.FeatureAction;
import org.netbeans.modules.autoupdate.featureondemand.ui.FeatureOnDemanWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** Factories for iterators, actions and other useful elements for feature
 * on demand UI.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
public final class Factory {
    private Factory() {}

    /** Creates new iterator for data provided by given file object.
     * 
     * @param fo file object describing the iterator
     * @return the Feature On Demand-ready iterator
     * @throws java.io.IOException 
     */
    public static WizardDescriptor.InstantiatingIterator newProject (FileObject fo) throws IOException {
        return FeatureOnDemanWizardIterator.newProject(fo);
    }
    
    /** Creates an action that can trigger Feature On Demand&tm; 
     * initialization.
     * 
     * @param fo file object to read the action from
     * @return ActionListener
     * @throws java.io.IOException 
     */
    public static ActionListener newDelegateAction(FileObject fo) throws IOException {
        return new FeatureAction (fo, true);
    }
    
    /** Creates an transient action that can trigger Feature On Demand&tm; 
     * initialization.
     * 
     * @param fo file object to read the action from
     * @return ActionListener
     * @throws java.io.IOException 
     */
    public static ActionListener newAction(FileObject fo) throws IOException {
        return new FeatureAction (fo, false);
    }
}
