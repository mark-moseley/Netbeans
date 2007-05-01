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

package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;

import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.impl.GraphLink;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;

/**
 * @author radval
 */
public class SQLGraphLink extends GraphLink {

    public SQLGraphLink(IGraphPort fromP, IGraphPort toP) {
        super(fromP, toP);
        this.setDefaultBrush(JGoBrush.makeStockBrush(Color.darkGray));
        this.setDefaultPen(JGoPen.makeStockPen(Color.darkGray));
    }

}

