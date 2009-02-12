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

package org.netbeans.modules.cnd.repository.impl;

import java.util.List;

/**
 *
 * @author sg155630
 */
public class RepositoryValidation extends RepositoryValidationBase {

    public RepositoryValidation(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.FALSE.toString()); //NOI18N
        System.setProperty("cnd.fix.IZ151567", Boolean.TRUE.toString()); //NOI18N
        assertNotNull("This test can only be run from suite", RepositoryValidationGoldens.getGoldenDirectory()); //NOI18N
        System.setProperty(PROPERTY_GOLDEN_PATH, RepositoryValidationGoldens.getGoldenDirectory());
        super.setUp();
    }

    public void testRepository() throws Exception {
        List<String> args = find();
        assert args.size() > 0;
        //args.add("-fq"); //NOI18N

        performTest(args.toArray(new String[]{}), nimi + ".out", nimi + ".err");
        assertNoExceptions();
    }
}
