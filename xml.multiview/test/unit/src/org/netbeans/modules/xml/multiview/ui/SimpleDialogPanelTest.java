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

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.JLabel;
import junit.framework.TestCase;

/**
 * This set of tests do test labels menominics.
 * There are 3 basic ways how mnemonics for labels may be used:
 * (1) labels with no mnemonics,
 * (2) labels with mnemonics specified as char[],
 * (3) labels with menomnics specified via '&' escape characters
 *
 * @author Petr Slechta
 */
public class SimpleDialogPanelTest extends TestCase {

    private static final String[] LABELS1 = {
        "Test label 1", "Test label 2", "Test label 3"
    };
    private static final char[] MNEMONICS1 = {
        'T', 'E', '3'
    };
    private static final String[] LABELS2 = {
        "&Test label 1", "T&est label 2", "Test label &3"
    };

    public SimpleDialogPanelTest(String testName) {
        super(testName);
    }

    public void testLabels1() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS1);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            assertEquals(labels[i].getDisplayedMnemonic(), 0);
        }
    }

    public void testLabels2() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS1);
        dd.setMnemonics(MNEMONICS1);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            assertEquals(labels[i].getDisplayedMnemonic(), MNEMONICS1[i]);
        }
    }

    public void testLabels3() {
        SimpleDialogPanel.DialogDescriptor dd = new SimpleDialogPanel.DialogDescriptor(LABELS2, true);
        SimpleDialogPanel sdp = new SimpleDialogPanel(dd);
        JLabel[] labels = sdp.getLabels();
        for (int i=0,maxi=LABELS1.length; i<maxi; i++) {
            assertEquals(labels[i].getDisplayedMnemonic(), MNEMONICS1[i]);
        }
    }

}
