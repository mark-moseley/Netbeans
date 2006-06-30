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
package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class ToolTipCombo extends JComboBox {

    private final static boolean debug = false;

    private static final long serialVersionUID = 1189442122448524856L;

    ToolTipCombo(Object[] o) {
	super(o);
	this.setRenderer(new PathRenderer());
	addItemListener(new ItemListener() { 
		public void itemStateChanged(ItemEvent evt) { 
		    if(evt.getStateChange() == ItemEvent.SELECTED) { 
			setToolTipText(evt.getItem().toString()); 
		    }
		}
	    }); 
	if(o != null && o.length > 1) 
	    setToolTipText(o[0].toString()); 
    }
	    
    private void log(String s) { 
	System.out.println("ToolTipCombo" + s);
    }

    class PathRenderer extends JLabel implements ListCellRenderer { 

        private static final long serialVersionUID = 1323260132420573174L;
        
	public PathRenderer() {
	    setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    
	    if(debug) log("::getListCellRendererComponent()"); //NOI18N
	    if(debug) log("\t" + value.toString()); //NOI18N
	    setText(value.toString());
	    setToolTipText(value.toString()); 
	    return this;
	}
    }
} 

