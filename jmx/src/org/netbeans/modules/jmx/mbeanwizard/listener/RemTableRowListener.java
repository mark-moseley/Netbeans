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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import javax.swing.JButton;
import javax.swing.JTable;


/**
 * Generic class handling the listeners which removes rows to the different
 * JTables contained in the wizard
 *
 */
public class RemTableRowListener implements ActionListener{

    private JTable t = null;
    private AbstractJMXTableModel m = null;
    private JButton b = null;
    
    /**
     * Constructor
     * @param table the Jtable in which a row is to remove
     * @param model the corresponding table model
     * @param remButton a reference to the remove line button
     */
    public RemTableRowListener(JTable table, AbstractJMXTableModel model, 
            JButton remButton) {
    
        this.t = table;
        m = model;
        b = remButton;
    }
    
    /**
     * Method handling what to do if the listener has been invoked
     * Here: removes a row
     * @param e an ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        final int selectedRow = t.getSelectedRow();
        
        //No row selected
        if (selectedRow == -1) return;
        
        try {            
            m.remRow(selectedRow, t);
            m.selectNextRow(selectedRow, t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // if the model has no rows, disable the remove button
        if (m.size() == 0)
            b.setEnabled(false);
    }
}
