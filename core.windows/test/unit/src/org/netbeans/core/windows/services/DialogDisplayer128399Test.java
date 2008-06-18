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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.core.windows.services;

import java.awt.Dialog;
import javax.swing.JButton;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** test issue #128399: Default option (specified in DialogDescriptor) is not implemented in DialogDisplayer
 *
 * @author Jiri Rechtacek
 */
public class DialogDisplayer128399Test extends NbTestCase {

    public DialogDisplayer128399Test (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    

    public void testDefaultOptionWithStandardOk () {
        JButton testButton = new JButton ("for-test-only");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] { NotifyDescriptor.CANCEL_OPTION, NotifyDescriptor.OK_OPTION }, // options
                NotifyDescriptor.OK_OPTION, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        dd.setAdditionalOptions (new JButton[] {testButton});
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("OK_OPTION is the default value.", NotifyDescriptor.OK_OPTION, dd.getDefaultValue ());
        assertEquals ("OK_OPTION is the default button on dialog",
                NbBundle.getBundle (NbPresenter.class).getString ("OK_OPTION_CAPTION"),
                testButton.getRootPane ().getDefaultButton ().getText ());
        //dlg.dispose ();
    }
    
    public void testDefaultOptionWithStandardCancel () {
        JButton testButton = new JButton ("for-test-only");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] { NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION }, // options
                NotifyDescriptor.CANCEL_OPTION, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        dd.setAdditionalOptions (new JButton[] {testButton});
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("CANCEL_OPTION is the default value.", NotifyDescriptor.CANCEL_OPTION, dd.getDefaultValue ());
        assertEquals ("CANCEL_OPTION is the default button on dialog",
                NbBundle.getBundle (NbPresenter.class).getString ("CANCEL_OPTION_CAPTION"),
                testButton.getRootPane ().getDefaultButton ().getText ());
        //dlg.dispose ();
    }
    
    public void testDefaultOptionWithCustomButton () {
        JButton myDefault = new JButton ("MyDefault");
        DialogDescriptor dd = new DialogDescriptor (
                "Hello", // innerPane
                "title", // title
                false, // isModal
                new Object[] {NotifyDescriptor.OK_OPTION, myDefault}, // options
                myDefault, // initialValue
                DialogDescriptor.DEFAULT_ALIGN, // optionsAlign
                HelpCtx.DEFAULT_HELP, // help
                null); // action listener
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
        //dlg.setVisible (true);
        assertEquals ("MyDefault is default value.", myDefault, dd.getDefaultValue ());
        assertTrue ("MyDefault is default capable.", myDefault.isDefaultCapable ());
        assertTrue ("MyDefault is default button.", myDefault.isDefaultButton ());
        assertEquals ("MyDefault is the default button on dialog", myDefault, myDefault.getRootPane ().getDefaultButton ());
        //dlg.dispose ();
    }
    

}
