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
package org.netbeans.modules.jmx.mbeanwizard.tablemodel;


import java.util.ArrayList;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.openide.util.NbBundle;



/**
 * Class implementing the table model for the mbean notification type
 * popup table
 *
 */
public class NotificationTypeTableModel extends AbstractJMXTableModel{

    public static final int IDX_NOTIF_TYPE          = 0;
    private String defaultTypeValue = "";// NOI18N

    /**
     * Constructor
     * @param defaultTypeValue the default notification type value for 
     * the popup
     */
    public NotificationTypeTableModel(String defaultTypeValue)
    {
        super();
        bundle = NbBundle.getBundle(OperationParameterTableModel.class);
        data = new ArrayList();
        columnNames = new String[1];
        String sopn = bundle.getString("LBL_NotificationType");// NOI18N
        columnNames[IDX_NOTIF_TYPE]        = sopn;
        this.defaultTypeValue = defaultTypeValue;
    }
    
    /**
     * Creates a new MBean notification type
     * @return MBeanNotificationType the created notification type
     */
    public MBeanNotificationType createNewNotificationType() {
        
        return new MBeanNotificationType(defaultTypeValue);
    }
    
    /**
     * Returns a notification type according to his index
     * @param index the index of the notification type in the list
     * @return MBeanNotificationType the notification type
     */
    public MBeanNotificationType getNotificationType(int index) {
        return (MBeanNotificationType)data.get(index);
    }
    
    /**
     * Sets the notification type at index index to a new object
     * @param index the index
     * @param notifType the Notification type to set
     */
    public void setNotificationType(int index, 
                                    MBeanNotificationType notifType) {
        if (index < data.size()) {
            data.set(index, notifType);
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanNotificationType notifType = (MBeanNotificationType)data.get(row);
        switch(col) {
            case 0: return notifType.getNotificationType();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "NotificationTypeTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanNotificationType notifType = 
                    (MBeanNotificationType)data.get(rowIndex);
            switch(columnIndex) {
                case 0: notifType.setNotificationType((String)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanAttributeTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanNotificationType mbnt = createNewNotificationType();
        data.add(mbnt);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
    
}
