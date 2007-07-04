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

package org.netbeans.modules.db.sql.loader;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.UniFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Jesse Beaumont
 */
public class SQLDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo(UniFileLoader.class) };
        } catch (IntrospectionException ie) {
	    Exceptions.printStackTrace(ie);
            return null;
        }
    }

    public Image getIcon(int type) {
        if (type == java.beans.BeanInfo.ICON_COLOR_16x16 ||
                type == java.beans.BeanInfo.ICON_MONO_16x16) {
	    return Utilities.loadImage("org/netbeans/modules/db/sql/loader/resources/sql16.png"); // NOI18N
        } else {
	    return Utilities.loadImage ("org/netbeans/modules/db/sql/loader/resources/sql32.png"); // NOI18N
        }
    }
}
