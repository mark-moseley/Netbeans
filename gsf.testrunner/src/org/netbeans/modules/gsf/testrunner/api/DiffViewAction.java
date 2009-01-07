/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * An action for viewing differences between two strings, e.g. an assert_equals
 * failure.
 *
 * @author Erno Mononen
 */
public final class DiffViewAction extends AbstractAction {

    private final Trouble.ComparisonFailure comparisonFailure;

    public DiffViewAction(Trouble.ComparisonFailure comparisonFailure) {
        this.comparisonFailure = comparisonFailure;
        if (this.comparisonFailure == null) {
            setEnabled(false);
        }
    }

    public DiffViewAction(Testcase testcase) {
        this(testcase.getTrouble() != null ? testcase.getTrouble().getComparisonFailure() : null);
    }

    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return NbBundle.getMessage(DiffViewAction.class, "LBL_ViewDiff");
        }
        return super.getValue(key);
    }

    public void actionPerformed(ActionEvent e) {

        StringComparisonSource expected =
                new StringComparisonSource(
                NbBundle.getMessage(DiffViewAction.class, "LBL_Expected"),
                comparisonFailure.getExpected());

        StringComparisonSource actual =
                new StringComparisonSource(
                NbBundle.getMessage(DiffViewAction.class, "LBL_Actual"),
                comparisonFailure.getActual());


        try {
            JComponent diffComponent = DiffController.create(expected, actual).getJComponent();
            diffComponent.setPreferredSize(new Dimension(500, 250));
            JButton ok = new JButton(NbBundle.getMessage(DiffViewAction.class, "LBL_OK"));
            final DialogDescriptor descriptor = new DialogDescriptor(
                    diffComponent,
                    NbBundle.getMessage(DiffViewAction.class, "LBL_Diff"),
                    true,
                    new Object[]{ok},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);

            Dialog dialog = null;
            try {
                dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
            } finally {
                if (dialog != null) {
                    dialog.dispose();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static class StringComparisonSource extends StreamSource {

        private final String name;
        private final String source;

        public StringComparisonSource(String name, String source) {
            this.name = name;
            this.source = source;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return getName();
        }

        @Override
        public String getMIMEType() {
            return "text/plain"; //NOI18N
        }

        @Override
        public Reader createReader() throws IOException {
            return new StringReader(source);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}

