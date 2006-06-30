/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import java.util.Collection;

/**
 * Identifies components rendering file differences.
 * It allows to access diff parameters, multifile setup.
 *
 * <p>It can be implemented directly by respective
 * TopComponets or it can be added in their lookup
 * (it allows proxing).
 *
 * @author Petr Kuzel
 */
public interface DiffSetupSource {

    /**
     * Access actually user-visible diff setup.
     *
     * @return read-only {@link Setup}s copy never <code>null</code>
     */
    Collection getSetups();

    /**
     * Prefered display name or null.
     */
    String getSetupDisplayName();
}
