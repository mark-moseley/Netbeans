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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public final class DbUtilities {

    private DbUtilities() {
    }

    public static String formatError(String message, String exception) {
        Parameters.notNull("message", message); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); // NOI18N
        if (exception != null) {
            String format = bundle.getString("ERR_UnableTo_Detail"); // NOI18N
            StringBuilder formattedException = new StringBuilder(exception.trim());
            if (formattedException.length() > 0) {
                formattedException.setCharAt(0, Character.toUpperCase(formattedException.charAt(0)));
            }
            int index = formattedException.length();
            while (index > 0 && ".!?".indexOf(formattedException.charAt(index - 1)) >= 0) { // NOI18N
                index--;
            }
            formattedException.delete(index, formattedException.length());
            formattedException.append('.');
            return MessageFormat.format(format, new Object[] { message, formattedException });
        } else {
            String format = bundle.getString("ERR_UnableTo_NoDetail"); // NOI18N
            return MessageFormat.format(format, new Object[] { message });
        }
    }

    public static void reportError(String message, String exception) {
        String error = formatError(message, exception);
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(error, NotifyDescriptor.ERROR_MESSAGE));
    }
}
