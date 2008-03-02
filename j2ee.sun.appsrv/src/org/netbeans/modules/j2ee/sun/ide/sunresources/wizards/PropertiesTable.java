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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/**
 * PropertiesTable.java
 *
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;     


class PropertiesTable extends JTable {

    private final static int margin = 6; 

    private boolean fontChanged = true;
    private int newHeight = 23; 
    
    private static final long serialVersionUID = -346761221423978739L;
    
    PropertiesTable() { 
	 super();
         initComponent();
    }

    void initComponent(){
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int col) {
	return super.getCellRenderer(row, col); 
    }
    
    @Override
    public void paint(Graphics g) {

	if (fontChanged) {
	    fontChanged = false; 

	    int height = 0; 
	    FontMetrics fm = g.getFontMetrics(getFont());
	    height = fm.getHeight() + margin;
	    if(height > newHeight) newHeight = height; 
	    this.setRowHeight(newHeight);
	    return;
	}

	super.paint(g);
    }
    
}
