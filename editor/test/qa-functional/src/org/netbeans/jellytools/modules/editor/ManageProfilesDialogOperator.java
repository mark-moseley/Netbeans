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
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author tester
 */
class ManageProfilesDialogOperator extends NbDialogOperator {

    public ManageProfilesDialogOperator() {
        super("Manage Keymap Profiles");
    }

    public void checkProfileListContent(String... items) {
        for (String string : items) {
            new JListOperator(this).selectItem(string);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // ...
            }
        }
    }

    public void Restore(String profileName) {
        System.out.println("[TEST_DEBUG] Attempting to restore profile: " + profileName);
        new JListOperator(this).selectItem(profileName);
        new JButtonOperator(this, "Restore Defaults").push();
        System.out.println("[TEST_DEBUG] Profile restored: " + profileName);
    }

    public void Delete(String profileName) {
        System.out.println("[TEST_DEBUG] Attempting to delete profile: " + profileName);
        new JListOperator(this).selectItem(profileName);
        new JButtonOperator(this, "Delete").push();
        System.out.println("[TEST_DEBUG] Profile deleted: " + profileName);
    }

    public void Duplicate(String profileNameOrig, String profileNameNew) {
            System.out.println("[TEST_DEBUG] Attempting to duplicate profile: " + profileNameOrig);
            new JListOperator(this).selectItem(profileNameOrig);
            new JButtonOperator(this, "Duplicate").push();
            NbDialogOperator confirmDupl = new NbDialogOperator("Create New Profile Dialog");
            JTextFieldOperator newName = new JTextFieldOperator(confirmDupl);
            newName.setText(profileNameNew);
            new JButtonOperator(confirmDupl, "OK").push();
            System.out.println("[TEST_DEBUG] Profile duplicated: " + profileNameOrig);
    }

    @Override
    public void ok() {
        new JButtonOperator(this, "OK").push();
    }
}
