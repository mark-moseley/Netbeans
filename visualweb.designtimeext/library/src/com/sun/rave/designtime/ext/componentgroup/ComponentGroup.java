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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.rave.designtime.ext.componentgroup;

import com.sun.rave.designtime.DesignBean;
import java.awt.Color;

/**
 *
 * @author mbohm
 */
public interface ComponentGroup {
   String getName();   //e.g., ajax transaction id
   Color getColor();
   void setColor(Color color);
   String getLegendEntryLabel();
   ComponentSubset[] getComponentSubsets();
   //DesignBean getAssociatedBean();   //just a precaution--in case we need to get back to the DesignBean for the Form or AjaxTransaction. Is this a good idea?     
}
