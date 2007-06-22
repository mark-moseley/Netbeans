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
 *
 */
package org.netbeans.modules.vmd.api.io.serialization;

import org.netbeans.modules.vmd.api.model.TypeID;
import org.w3c.dom.Node;

/**
 * @author David Kaspar
 */
public final class ComponentElement {

    private long parentuid;
    private long uid;
    private TypeID typeid;
    private Node node;

    private ComponentElement (long parentuid, long uid, TypeID typeid, Node node) {
        this.parentuid = parentuid;
        this.uid = uid;
        this.typeid = typeid;
        this.node = node;
    }

    public long getParentUID () {
        return parentuid;
    }

    public long getUID () {
        return uid;
    }

    public TypeID getTypeID () {
        return typeid;
    }

    public Node getNode () {
        return node;
    }

    public static ComponentElement create (long parentuid, long uid, TypeID typeid, Node node) {
        return new ComponentElement (parentuid, uid, typeid, node);
    }

}
