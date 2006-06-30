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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JTable;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;

/**
 * Interceptor to fire an event to a class which implements FireEvent interface.
 * @author tl156378
 */
public class RemTableRowListenerWithFireEvent extends RemTableRowListener {

    private FireEvent fireEvent;
    
    /**
     * Constructor
     * @param table the Jtable in which a row is to remove
     * @param model the corresponding table model
     * @param remButton a reference to the remove line button
     * @param fireEvent the panel to notify for events
     */
    public RemTableRowListenerWithFireEvent(JTable table, AbstractJMXTableModel model, 
            JButton remButton, FireEvent fireEvent) {
        super(table, model, remButton);
        this.fireEvent = fireEvent;
    }
    
    /**
     * Method handling what to do if the listener has been invoked
     * Here: removes a row
     * @param e an ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        fireEvent.event();
    }
        
    
}
