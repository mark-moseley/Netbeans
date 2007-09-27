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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Insets;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableConstants {

    /**
     * constant for inset of column
     */
    public static final Insets COLUMN_INSETS = new Insets(1, 5, 0, 10);

    /**
     * constant for insets of table header
     */
    public static final Insets TABLE_HEADER_INSETS = new Insets(1, 0, 1, 0);

    /**
     * constant for insets of header cell
     */
    public static final Insets HEADER_CELL_INSETS = new Insets(1, 2, 1, 10);

    /**
     * constant for scrollbar width
     */
    public static final int TABLE_SCROLLBAR_WIDTH = 14;

    /**
     * constant for the gap between table and its header
     */
    public static final int TABLE_HEADER_GAP = 2;

    /**
     * constant that describe a column area having left port area
     */
    public static final int LEFT_PORT_AREA = 0;

    /**
     * constant that describe a column area having right port area
     */
    public static final int RIGHT_PORT_AREA = 1;

    /**
     * constants that describes a table being both input and output so that port appears
     * on both left and right side of it
     */
    public static final int INPUT_OUTPUT_TABLE = -1;

    /**
     * constants that describes a table being input so that port appears only on right
     * side of it
     */
    public static final int INPUT_TABLE = 0;

    /**
     * constants that describes a table being output so that port appears only on left
     * side of it
     */
    public static final int OUTPUT_TABLE = 1;
    
    public static final int NO_PORT_TABLE = 2;

}

