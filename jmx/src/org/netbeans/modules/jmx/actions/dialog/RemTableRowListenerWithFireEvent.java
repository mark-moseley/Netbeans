/*
 * RemTableRowListenerWithFireEvent.java
 *
 * Created on July 8, 2005, 4:43 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
