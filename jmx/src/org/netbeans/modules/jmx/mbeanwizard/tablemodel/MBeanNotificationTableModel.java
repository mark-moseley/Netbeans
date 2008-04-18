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
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.MBeanNotificationType;


/**
 * Class implementing the table model for the mbean notification table
 *
 */
public class MBeanNotificationTableModel extends AbstractJMXTableModel {
    public static final int IDX_NOTIF_CLASS                = 0;
    public static final int IDX_NOTIF_DESCRIPTION          = 1;
    public static final int IDX_NOTIF_TYPE                 = 2;
    private String defaultTypeValue = "";// NOI18N
    
    /**
     * Constructor
     */
    public MBeanNotificationTableModel() {
        super();
        
        bundle = NbBundle.getBundle(MBeanNotificationTableModel.class);
        
        data = new ArrayList();
        
        columnNames = new String[3];
        
        String sc = bundle.getString("LBL_NotificationClass");// NOI18N
        String sd = bundle.getString("LBL_NotificationDescription");// NOI18N
        String st = bundle.getString("LBL_NotificationType");// NOI18N
        
        columnNames[IDX_NOTIF_CLASS]        = sc;
        columnNames[IDX_NOTIF_DESCRIPTION]  = sd;
        columnNames[IDX_NOTIF_TYPE]         = st;
    }
    
    /**
     * Sets the default notification type value
     * @param defaultValue value to be set
     */
    public void setDefaultTypeValue(String defaultValue) {
        this.defaultTypeValue = defaultValue;
    }
    
    /**
     * Gets the default notification type value
     * @return String the default notification type value
     */
    public String getDefaultTypeValue() {
        return defaultTypeValue;
    }
    
    /**
     * Instantiates a new notification; called when a line is added to the
     * table
     * @return MBeanNotification the created notification
     */
    public MBeanNotification createNewNotification() {
        
        return new MBeanNotification(
                WizardConstants.NOTIFICATION,
                WizardConstants.NOTIF_DESCR_DEFVALUE,
                new ArrayList<MBeanNotificationType>());
    }
    
    /**
     * Returns the notification at index index
     * @return MBeanNotification the notification at index index
     */
    public MBeanNotification getNotification(int index) {
        return (MBeanNotification)data.get(index);
    }
    
    /**
     * Overriden method from superclass
     */
    public Object getValueAt(int row, int col) {
        MBeanNotification notif = (MBeanNotification)data.get(row);
        switch(col) {
            case 0: return notif.getNotificationClass();
            case 1: return notif.getNotificationDescription();
            case 2: return notif.getNotificationTypeList();
            default: System.out.println("Error getValueAt " +// NOI18N
                    "MBeanMethodTableModel " + col);// NOI18N
            break;
        }
        return null;
    }
    
    /**
     * Overriden method from superclass
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex < this.size()){
            MBeanNotification notif = (MBeanNotification)data.get(rowIndex);
            switch(columnIndex) {
                case 0: notif.setNotificationClass((String)aValue);
                break;
                case 1: notif.setNotificationDescription((String)aValue);
                break;
                case 2: notif.setNotificationTypeList(
                        (ArrayList<MBeanNotificationType>)aValue);
                break;
                default: System.out.println("Error setValueAt " +// NOI18N
                        "MBeanMethodTableModel " + columnIndex);// NOI18N
                break;
            }
        }
    }
    
    /**
     * Overriden method from superclass
     */
    public void addRow() {
        
        MBeanNotification mbn = createNewNotification();
        data.add(mbn);
        
        //table is informed about the change to update the view
        this.fireTableDataChanged();
    }
}
