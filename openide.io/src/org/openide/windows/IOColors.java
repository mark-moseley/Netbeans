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

package org.openide.windows;

import java.awt.Color;
import org.openide.util.Lookup;

/**
 * Settings of current colors for out/err/in
 * @since 1.16
 * @author Tomas Holy
 */
public abstract class IOColors {

    private static IOColors find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOColors.class);
        }
        return null;
    }

    /**
     * output types
     */
    public enum OutputType {
        /** default output */
        OUTPUT,
        /** error output */
        ERROR,
        /** hyperlink */
        HYPERLINK,
        /** important hyperlink */
        HYPERLINK_IMPORTANT,
        /** input, could be supported in future */
        // INPUT,
    }

    /**
     * Gets current color for output
     * @param io InputOutput to operate on
     * @param type output type to get color for
     * @return current color for specified output type or null if not supported
     */
    public static Color getColor(InputOutput io, OutputType type) {
        IOColors ioc = find(io);
        return ioc != null ? ioc.getColor(type) : null;
    }

    /**
     * Sets specified color for output
     * @param io InputOutput to operate on
     * @param type output type to set color for
     * @param color new color for specified output type
     */
    public static void setColor(InputOutput io, OutputType type, Color color) {
        IOColors ioc = find(io);
        if (ioc != null) {
            ioc.setColor(type, color);
        }
    }

    /**
     * Gets current color for output
     * @param type output type to get color for
     * @return current color for specified output
     */
    abstract protected Color getColor(OutputType type);

    /**
     * Sets specified color for output
     * @param type output type to set color for
     * @param color new color for specified output type
     */
    abstract protected void setColor(OutputType type, Color color);
}
