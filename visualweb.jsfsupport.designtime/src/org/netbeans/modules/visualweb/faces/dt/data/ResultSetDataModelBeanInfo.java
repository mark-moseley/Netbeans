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

package org.netbeans.modules.visualweb.faces.dt.data;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import javax.faces.model.DataModelListener;
import javax.faces.model.ResultSetDataModel;
import org.openide.util.Exceptions;

public class ResultSetDataModelBeanInfo extends SimpleBeanInfo {

    public ResultSetDataModelBeanInfo() throws NoSuchMethodException {}

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(ResultSetDataModel.class, null);
    }

    //public int getDefaultEventIndex() {}

    public int getDefaultPropertyIndex() {
        return 0;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            EventSetDescriptor[] eventSetDescriptors;
            eventSetDescriptors = new EventSetDescriptor[] {
                new EventSetDescriptor(ResultSetDataModel.class, "dataModel", //NOI18N
                DataModelListener.class,
                new String[] {
                "rowSelected"
            }

            , //NOI18N
                "addDataModelListener", "removeDataModelListener") //NOI18N
            };

            return eventSetDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public Image getIcon(int iconKind) {
        switch (iconKind) {
            case BeanInfo.ICON_COLOR_16x16:
                return loadImage("ResultSetDataModelIconColor16.png"); //NOI18N
            case BeanInfo.ICON_COLOR_32x32:
                return loadImage("ResultSetDataModelIconColor32.gif"); //NOI18N
            case BeanInfo.ICON_MONO_16x16:
                return loadImage("ResultSetDataModelIconMono16.gif"); //NOI18N
            case BeanInfo.ICON_MONO_32x32:
                return loadImage("ResultSetDataModelIconMono32.gif"); //NOI18N
        }
        return null;
    }

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        try {
            MethodDescriptor[] methodDescriptors = new MethodDescriptor[] {
                    new MethodDescriptor(ResultSetDataModel.class.getMethod("getRowData", null)), //NOI18N
                    new MethodDescriptor(ResultSetDataModel.class.getMethod("getRowCount", null)) //NOI18N
                };

            return methodDescriptors;
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] propertyDescriptors;
            PropertyDescriptor rowData =
                new PropertyDescriptor("rowData", ResultSetDataModel.class, "getRowData", null); //NOI18N

            PropertyDescriptor rowIndex =
                new PropertyDescriptor("rowIndex", ResultSetDataModel.class); //NOI18N

            PropertyDescriptor wrappedData =
                new PropertyDescriptor("wrappedData", ResultSetDataModel.class); //NOI18N
            wrappedData.setPropertyEditorClass(RowSetPropertyEditor.class);

            propertyDescriptors = new PropertyDescriptor[] {
                rowData,
                rowIndex,
                wrappedData
            };
            return propertyDescriptors;
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
}
