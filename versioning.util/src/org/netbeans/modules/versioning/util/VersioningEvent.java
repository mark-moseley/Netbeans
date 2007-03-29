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

package org.netbeans.modules.versioning.util;

import java.util.*;

/**
 * A versioning event.
 *
 * @author Maros Sandor
 */
public class VersioningEvent extends EventObject {

    private final Object    id;
    private final Object [] params;

    public VersioningEvent(Object source, Object id, Object [] params) {
        super(source);
        this.id = id;
        this.params = params;
    }

    public Object getId() {
        return id;
    }

    public Object[] getParams() {
        return params;
    }
}
