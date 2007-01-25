/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.iep.editor.ps;

import java.awt.datatransfer.DataFlavor;

/**
 * AttributeInfoDataFlavor.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class AttributeInfoDataFlavor extends DataFlavor {
    public static final DataFlavor ATTRIBUTE_INFO_FLAVOR;
    
    static {
        ATTRIBUTE_INFO_FLAVOR = new DataFlavor(java.lang.Object.class, "AttributeInfo");
    }
    
    public AttributeInfoDataFlavor() {
    }
    
}