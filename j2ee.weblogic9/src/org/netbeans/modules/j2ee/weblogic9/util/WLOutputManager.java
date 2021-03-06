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

package org.netbeans.modules.j2ee.weblogic9.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 * @deprecated Replace it with some common log api for J2EE servers.
 *             Consider this just a temporary solution until the api will be
 *             implemented.
 */
public class WLOutputManager {

    private final InputOutput io;

    private final WLTailer standardOutputTailer;

    private final WLTailer standardErrorTailer;

    private boolean finished;

    public WLOutputManager(Process process, String uri) {
        io = UISupport.getServerIO(uri);
        standardOutputTailer = new WLTailer(process.getInputStream(), io.getOut());
        standardErrorTailer = new WLTailer(process.getErrorStream(), io.getErr());
    }

    public final synchronized void start() {
        if (finished || io == null) {
            return;
        }

        try {
            // as described in the api we reset just ouptut
            io.getOut().reset();
        } catch (IOException ex) {
            Logger.getLogger(WLOutputManager.class.getName()).log(Level.INFO, null, ex);
        }

        io.select();
        standardOutputTailer.start();
        standardErrorTailer.start();
    }

    public final synchronized void finish() {
        finished = true;

        standardOutputTailer.finish();
        standardErrorTailer.finish();
    }

}
